package com.fansz.apns.connection;

import com.fansz.apns.codec.ApnsPushNotificationEncoder;
import com.fansz.apns.model.RejectedNotification;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fansz.apns.PushManager;
import com.fansz.apns.codec.RejectedNotificationDecoder;
import com.fansz.apns.config.ApnsConnectionConfiguration;
import com.fansz.apns.config.ApnsEnvironment;
import com.fansz.apns.listener.ApnsConnectionListener;
import com.fansz.apns.model.KnownBadPushNotification;
import com.fansz.apns.model.SendableApnsPushNotification;
import com.fansz.apns.support.ApnsPushNotification;
import com.fansz.apns.support.RejectedNotificationReason;
import com.fansz.apns.support.SentNotificationBuffer;

/**
 * <p>
 * A connection to an APNs gateway. An {@code ApnsConnection} is responsible for sending push notifications to the APNs
 * gateway, and reports lifecycle events via its {@link ApnsConnectionListener}.
 * </p>
 * <p>
 * Generally, connections should be managed by a parent {@link PushManager} and not manipulated directly (although
 * connections are fully functional on their own). Connections are created in a disconnected state, and must be
 * explicitly connected before they can be used to send push notifications.
 * </p>
 *
 * @see PushManager
 */
public class ApnsConnection<T extends ApnsPushNotification> {

    private final ApnsEnvironment environment;

    private final SSLContext sslContext;

    private final NioEventLoopGroup eventLoopGroup;

    private final ApnsConnectionConfiguration configuration;

    private final ApnsConnectionListener<T> listener;

    private final String name;

    private ChannelFuture connectFuture;

    private volatile boolean handshakeCompleted = false;

    // We want to start the count at 1 here because the gateway will send back a sequence number of 0 if it doesn't know
    // which notification failed. This isn't 100% bulletproof (we'll legitimately get back to 0 after 2^32
    // notifications), but the probability of collision (or even sending 4 billion notifications without some recipient
    // having an expired token) is vanishingly small.
    private int sequenceNumber = 1;

    private int sendAttempts = 0;

    private SendableApnsPushNotification<KnownBadPushNotification> disconnectNotification;

    private ScheduledFuture<?> gracefulDisconnectionTimeoutFuture;

    private boolean rejectionReceived = false;

    private final SentNotificationBuffer<T> sentNotificationBuffer;

    private static final Logger log = LoggerFactory.getLogger(ApnsConnection.class);

    public static final int DEFAULT_SENT_NOTIFICATION_BUFFER_CAPACITY = 8192;

    private class ApnsConnectionHandler extends SimpleChannelInboundHandler<RejectedNotification> {

        private final ApnsConnection<T> apnsConnection;

        public ApnsConnectionHandler(final ApnsConnection<T> apnsConnection) {
            this.apnsConnection = apnsConnection;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RejectedNotification rejectedNotification) {
            log.debug("APNs gateway rejected notification with sequence number {} from {} ({}).",
                    new Object[]{rejectedNotification.getSequenceNumber(), this.apnsConnection.name,
                            rejectedNotification.getReason()});

            this.apnsConnection.rejectionReceived = true;
            this.apnsConnection.sentNotificationBuffer.clearNotificationsBeforeSequenceNumber(rejectedNotification
                    .getSequenceNumber());

            final boolean isKnownBadRejection = this.apnsConnection.disconnectNotification != null
                    && (rejectedNotification.getSequenceNumber() == this.apnsConnection.disconnectNotification
                    .getSequenceNumber() || (rejectedNotification.getSequenceNumber() == 0 && RejectedNotificationReason.MISSING_TOKEN
                    .equals(rejectedNotification.getReason())));

            // We only want to notify listeners of an actual rejection if something actually went wrong. We don't want
            // to notify listeners if a known-bad notification was rejected because that's an expected case, and we
            // don't want to notify listeners if the gateway is closing the connection, but still processed the
            // named notification successfully.
            if (!isKnownBadRejection && !RejectedNotificationReason.SHUTDOWN.equals(rejectedNotification.getReason())) {
                final T notification = this.apnsConnection.sentNotificationBuffer
                        .getNotificationWithSequenceNumber(rejectedNotification.getSequenceNumber());

                if (notification != null) {
                    if (this.apnsConnection.listener != null) {
                        this.apnsConnection.listener.handleRejectedNotification(this.apnsConnection, notification,
                                rejectedNotification.getReason());
                    }
                } else {
                    if (this.apnsConnection.sentNotificationBuffer.isEmpty()) {
                        log.error(
                                "{} failed to find rejected notification with sequence number {} (buffer is empty). "
                                        + "this may mean the sent notification buffer is too small. Please report this as a bug.",
                                this.apnsConnection.name, rejectedNotification.getSequenceNumber());
                    } else {
                        log.error(
                                "{} failed to find rejected notification with sequence number {} (buffer has range {} to "
                                        + "{}); this may mean the sent notification buffer is too small. Please report this as a bug.",
                                new Object[]{this.apnsConnection.name, rejectedNotification.getSequenceNumber(),
                                        this.apnsConnection.sentNotificationBuffer.getLowestSequenceNumber(),
                                        this.apnsConnection.sentNotificationBuffer.getHighestSequenceNumber()});
                    }
                }
            }

            if (rejectedNotification.getSequenceNumber() != 0) {
                final Collection<T> unprocessedNotifications = this.apnsConnection.sentNotificationBuffer
                        .getAllNotificationsAfterSequenceNumber(rejectedNotification.getSequenceNumber());

                if (!unprocessedNotifications.isEmpty()) {
                    if (this.apnsConnection.listener != null) {
                        this.apnsConnection.listener.handleUnprocessedNotifications(this.apnsConnection,
                                unprocessedNotifications);
                    }
                }
            }

            this.apnsConnection.sentNotificationBuffer.clearAllNotifications();
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
            // Since this is happening on the inbound side, the most likely case is that a read timed out or the remote
            // host closed the connection. We should log the problem, but generally assume that channel closure will be
            // handled by channelInactive.
            log.debug("{} caught an exception.", this.apnsConnection.name, cause);
        }

        @Override
        public void channelInactive(final ChannelHandlerContext context) throws Exception {
            super.channelInactive(context);

            // Channel closure implies that the connection attempt had fully succeeded, so we only want to notify
            // listeners if the handshake has completed. Otherwise, we'll notify listeners of a connection failure (as
            // opposed to closure) elsewhere.
            if (this.apnsConnection.gracefulDisconnectionTimeoutFuture != null) {
                this.apnsConnection.gracefulDisconnectionTimeoutFuture.cancel(false);
            }
        }

        @Override
        public void channelWritabilityChanged(final ChannelHandlerContext context) throws Exception {
            super.channelWritabilityChanged(context);

            if (this.apnsConnection.listener != null) {
                this.apnsConnection.listener.handleConnectionWritabilityChange(this.apnsConnection, context.channel()
                        .isWritable());
            }
        }

        @Override
        public void userEventTriggered(final ChannelHandlerContext context, final Object event) throws Exception {
            if (event instanceof IdleStateEvent) {
                log.debug("{} will disconnect gracefully due to inactivity.", this.apnsConnection.name);
                this.apnsConnection.disconnectGracefully();
            } else {
                super.userEventTriggered(context, event);
            }
        }
    }

    /**
     * Constructs a new APNs connection. The connection connects to the APNs gateway in the given environment with the
     * credentials and key/trust managers in the given SSL context.
     *
     * @param environment    the environment in which this connection will operate; must not be {@code null}
     * @param sslContext     an SSL context with the keys/certificates and trust managers this connection should use when
     *                       communicating with the APNs gateway; must not be {@code null}
     * @param eventLoopGroup the event loop group this connection should use for asynchronous network operations; must
     *                       not be {@code null}
     * @param configuration  the set of configuration options to use for this connection. The configuration object is
     *                       copied and changes to the original object will not propagate to the connection after creation. Must
     *                       not be {@code null}.
     * @param listener       the listener to which this connection will report lifecycle events; may be {@code null}
     * @param name           a human-readable name for this connection; names must not be {@code null}
     */
    public ApnsConnection(final ApnsEnvironment environment, final SSLContext sslContext,
                          final NioEventLoopGroup eventLoopGroup, final ApnsConnectionConfiguration configuration,
                          final ApnsConnectionListener<T> listener, final String name) {

        if (environment == null) {
            throw new NullPointerException("Environment must not be null.");
        }

        this.environment = environment;

        if (sslContext == null) {
            throw new NullPointerException("SSL context must not be null.");
        }

        this.sslContext = sslContext;

        if (eventLoopGroup == null) {
            throw new NullPointerException("Event loop group must not be null.");
        }

        this.eventLoopGroup = eventLoopGroup;

        if (configuration == null) {
            throw new NullPointerException("Connection configuration must not be null.");
        }

        this.configuration = configuration;
        this.listener = listener;

        if (name == null) {
            throw new NullPointerException("Connection name must not be null.");
        }

        this.name = name;

        this.sentNotificationBuffer = new SentNotificationBuffer<T>(configuration.getSentNotificationBufferCapacity());
    }

    /**
     * 异步方式连接到APNS，可以通过Connection Listener监听连接的状态变化
     *
     * @see ApnsConnectionListener#handleConnectionSuccess(ApnsConnection)
     * @see ApnsConnectionListener#handleConnectionFailure(ApnsConnection, Throwable)
     */
    public synchronized void connect() {

        final ApnsConnection<T> apnsConnection = this;

        if (this.connectFuture != null) {
            throw new IllegalStateException(String.format("%s already started a connection attempt.", this.name));
        }

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        final LoggingHandler loggingHandler = new LoggingHandler();

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(final SocketChannel channel) {
                final ChannelPipeline pipeline = channel.pipeline();
                pipeline.addFirst("logging", loggingHandler);

                final SSLEngine sslEngine = apnsConnection.sslContext.createSSLEngine();
                sslEngine.setUseClientMode(true);

                pipeline.addLast("ssl", new SslHandler(sslEngine));
                pipeline.addLast("decoder", new RejectedNotificationDecoder());
                pipeline.addLast("encoder", new ApnsPushNotificationEncoder<T>());

                if (ApnsConnection.this.configuration.getCloseAfterInactivityTime() != null) {
                    pipeline.addLast("idleStateHandler",
                            new IdleStateHandler(0, 0, apnsConnection.configuration.getCloseAfterInactivityTime()));
                }

                pipeline.addLast("handler", new ApnsConnectionHandler(apnsConnection));
            }
        });

        log.debug("{} beginning connection process.", apnsConnection.name);
        this.connectFuture = bootstrap.connect(this.environment.getApnsGatewayHost(),
                this.environment.getApnsGatewayPort());
        this.connectFuture.addListener(new GenericFutureListener<ChannelFuture>() {

            @Override
            public void operationComplete(final ChannelFuture connectFuture) {
                if (connectFuture.isSuccess()) {
                    log.debug("{} connected; waiting for TLS handshake.", apnsConnection.name);

                    final SslHandler sslHandler = connectFuture.channel().pipeline().get(SslHandler.class);

                    try {
                        sslHandler.handshakeFuture().addListener(new GenericFutureListener<Future<Channel>>() {

                            @Override
                            public void operationComplete(final Future<Channel> handshakeFuture) {
                                if (handshakeFuture.isSuccess()) {
                                    log.debug("{} successfully completed TLS handshake.", apnsConnection.name);

                                    apnsConnection.handshakeCompleted = true;

                                    if (apnsConnection.listener != null) {
                                        apnsConnection.listener.handleConnectionSuccess(apnsConnection);
                                    }
                                } else {
                                    log.debug("{} failed to complete TLS handshake with APNs gateway.",
                                            apnsConnection.name, handshakeFuture.cause());

                                    connectFuture.channel().close();

                                    if (apnsConnection.listener != null) {
                                        apnsConnection.listener.handleConnectionFailure(apnsConnection,
                                                handshakeFuture.cause());
                                    }
                                }
                            }
                        });
                    } catch (NullPointerException e) {
                        log.warn("{} failed to get SSL handler and could not wait for a TLS handshake.",
                                apnsConnection.name);

                        connectFuture.channel().close();

                        if (apnsConnection.listener != null) {
                            apnsConnection.listener.handleConnectionFailure(apnsConnection, e);
                        }
                    }
                } else {
                    log.debug("{} failed to connect to APNs gateway.", apnsConnection.name, connectFuture.cause());

                    if (apnsConnection.listener != null) {
                        apnsConnection.listener.handleConnectionFailure(apnsConnection, connectFuture.cause());
                    }
                }
            }
        });
    }

    /**
     * 异步的发送消息到APNS服务，如果消息成功接收，APNS不会返回结果，如果失败，APNS会返回失败消息
     *
     * @param notification the notification to send
     * @see ApnsConnectionListener#handleWriteFailure(ApnsConnection, ApnsPushNotification, Throwable)
     * @see ApnsConnectionListener#handleRejectedNotification(ApnsConnection, ApnsPushNotification,
     * RejectedNotificationReason)
     */
    public synchronized void sendNotification(final T notification) {
        if (!this.handshakeCompleted) {
            throw new IllegalStateException(String.format("%s has not completed handshake.", this.name));
        }

        if (this.disconnectNotification == null) {
            final SendableApnsPushNotification<T> sendableNotification = new SendableApnsPushNotification<T>(
                    notification, this.sequenceNumber++);

            log.debug("{} sending {}", this.name, sendableNotification);

            this.connectFuture.channel().writeAndFlush(sendableNotification)
                    .addListener(new GenericFutureListener<ChannelFuture>() {

                        @Override
                        public void operationComplete(final ChannelFuture writeFuture) {
                            if (writeFuture.isSuccess()) {
                                log.debug("{} successfully wrote notification {}", ApnsConnection.this.name,
                                        sendableNotification.getSequenceNumber());

                                if (ApnsConnection.this.rejectionReceived) {
                                    // Even though the write succeeded, we know for sure that this notification was
                                    // never
                                    // processed by the gateway because it had already rejected another notification
                                    // from
                                    // this connection.
                                    if (ApnsConnection.this.listener != null) {
                                        ApnsConnection.this.listener.handleUnprocessedNotifications(
                                                ApnsConnection.this, java.util.Collections.singletonList(notification));
                                    }
                                } else {
                                    ApnsConnection.this.sentNotificationBuffer
                                            .addSentNotification(sendableNotification);
                                }
                            } else {
                                log.debug("{} failed to write notification {}", new Object[]{ApnsConnection.this.name,
                                        sendableNotification, writeFuture.cause()});

                                // Assume this is a temporary failure (we know it's not a permanent rejection because we
                                // didn't
                                // even manage to write the notification to the wire) and re-enqueue for another send
                                // attempt.
                                if (ApnsConnection.this.listener != null) {
                                    ApnsConnection.this.listener.handleWriteFailure(ApnsConnection.this, notification,
                                            writeFuture.cause());
                                }
                            }
                        }
                    });
        } else {
            if (this.listener != null) {
                this.listener.handleWriteFailure(this, notification, new IllegalStateException(
                        "Connection is disconnecting."));
            }
        }

        if (this.configuration.getSendAttemptLimit() != null
                && ++this.sendAttempts >= this.configuration.getSendAttemptLimit()) {
            log.debug("{} reached send attempt limit and will disconnect gracefully.", this.name);
            this.disconnectGracefully();
        }
    }

    /**
     * 异步并优雅的关闭连接， 通过发送一个格式错误的消息给APN服务，当APNS服务拒绝了该消息，APNS服务会确保之前的所有消息被成功处理；在此之后的消息将不会被处理；
     * 在拒绝消息之后，APNS会关闭连接，并且通知连接Listener;
     * <p>
     * 当格式错误的消息被APNS拒绝时，Listner并不会收到通知
     * </p>
     * <p>
     * 在连接未建立之前或者已经调用了关闭方法，将不会产生任何效果;
     * </p>
     *
     * @return {@code true} if this connection started a graceful disconnection attempt or {@code false} otherwise (i.e.
     * because no connection was ever established or the connection is already closed).
     * @see ApnsConnectionListener#handleRejectedNotification(ApnsConnection, ApnsPushNotification,
     * RejectedNotificationReason)
     * @see ApnsConnectionListener#handleConnectionClosure(ApnsConnection)
     */
    public synchronized boolean disconnectGracefully() {

        // We only need to send a known-bad notification if we were ever connected in the first place and if we're
        // still connected.
        if (this.handshakeCompleted && this.connectFuture.channel().isActive()) {

            // Don't send a second disconnection notification if we've already started the graceful disconnection
            // process.
            if (this.disconnectNotification == null) {

                log.debug("{} sending known-bad notification to disconnect.", this.name);

                this.disconnectNotification = new SendableApnsPushNotification<KnownBadPushNotification>(
                        new KnownBadPushNotification(), this.sequenceNumber++);

                if (this.configuration.getGracefulDisconnectionTimeout() != null) {
                    ApnsConnection.this.gracefulDisconnectionTimeoutFuture = ApnsConnection.this.connectFuture
                            .channel().eventLoop().schedule(new Runnable() {
                                @Override
                                public void run() {
                                    ApnsConnection.this.disconnectImmediately();
                                }
                            }, ApnsConnection.this.configuration.getGracefulDisconnectionTimeout(), TimeUnit.SECONDS);
                }

                this.connectFuture.channel().writeAndFlush(this.disconnectNotification)
                        .addListener(new GenericFutureListener<ChannelFuture>() {

                            @Override
                            public void operationComplete(final ChannelFuture writeFuture) {
                                if (writeFuture.isSuccess()) {
                                    log.debug("{} successfully wrote known-bad notification {}",
                                            ApnsConnection.this.name,
                                            ApnsConnection.this.disconnectNotification.getSequenceNumber());
                                } else {
                                    log.debug("{} failed to write known-bad notification {}", new Object[]{ApnsConnection.this.name,
                                            ApnsConnection.this.disconnectNotification, writeFuture.cause()});

                                    // Try again!
                                    ApnsConnection.this.disconnectNotification = null;
                                    ApnsConnection.this.disconnectGracefully();
                                }
                            }
                        });
            }

            return true;
        } else {
            // While we can't guarantee that the handshake won't complete in another thread, we CAN guarantee that no
            // new notifications will be sent until disconnectImmediately happens because everything is synchronized.
            this.disconnectImmediately();

            return false;
        }
    }

    public synchronized void disconnectImmediately() {
        if (this.connectFuture != null) {
            this.connectFuture.channel().close();
        }
    }

    @Override
    public String toString() {
        return "ApnsConnection [name=" + name + "]";
    }
}
