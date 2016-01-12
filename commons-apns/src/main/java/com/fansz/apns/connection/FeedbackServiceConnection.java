package com.fansz.apns.connection;

import com.fansz.apns.PushManager;
import com.fansz.apns.listener.FeedbackServiceListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fansz.apns.codec.ExpiredTokenDecoder;
import com.fansz.apns.config.ApnsEnvironment;
import com.fansz.apns.config.FeedbackConnectionConfiguration;
import com.fansz.apns.model.ExpiredToken;

/**
 * <p>
 * 根据Apple技术文档描述:
 * </p>
 * <blockquote>
 * <p>
 * The Apple Push Notification Service includes a feedback service to give you information about failed push
 * notifications. When a push notification cannot be delivered because the intended app does not exist on the device,
 * the feedback service adds that device's token to its list. Push notifications that expire before being delivered are
 * not considered a failed delivery and don't impact the feedback service...
 * </p>
 * <p>
 * Query the feedback service daily to get the list of device tokens. Use the timestamp to verify that the device tokens
 * haven't been reregistered since the feedback entry was generated. For each device that has not been reregistered,
 * stop sending notifications.
 * </p>
 * </blockquote>
 * <p>
 * 一般而言，建议开发人员不要直接实例化 {@code FeedbackServiceConnection} ,而是调用
 * {@link PushManager#requestExpiredTokens()}
 * </p>
 *
 * @see <a
 *      href="http://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW3">
 *      Local and Push Notification Programming Guide - Provider Communication with Apple Push Notification Service -
 *      The Feedback Service</a>
 */
public class FeedbackServiceConnection {
    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceConnection.class);

    private final ApnsEnvironment environment;

    private final SSLContext sslContext;

    private final NioEventLoopGroup eventLoopGroup;

    private final FeedbackConnectionConfiguration configuration;

    private final FeedbackServiceListener listener;

    private final String name;

    private ChannelFuture connectFuture;

    private static class FeedbackClientHandler extends SimpleChannelInboundHandler<ExpiredToken> {

        private final FeedbackServiceConnection feedbackClient;

        public FeedbackClientHandler(final FeedbackServiceConnection feedbackClient) {
            this.feedbackClient = feedbackClient;
        }

        @Override
        protected void channelRead0(final ChannelHandlerContext context, final ExpiredToken expiredToken) {
            if (this.feedbackClient.listener != null) {
                this.feedbackClient.listener.handleExpiredToken(feedbackClient, expiredToken);
            }
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {

            if (!(cause instanceof ReadTimeoutException)) {
                log.debug("Caught an unexpected exception while waiting for expired tokens.", cause);
            }

            context.close();
        }

        @Override
        public void channelInactive(final ChannelHandlerContext context) throws Exception {
            super.channelInactive(context);

            final SslHandler sslHandler = context.pipeline().get(SslHandler.class);

            if (sslHandler != null && sslHandler.handshakeFuture().isSuccess()) {
                if (this.feedbackClient.listener != null) {
                    this.feedbackClient.listener.handleConnectionClosure(this.feedbackClient);
                }
            }
        }
    }

    public FeedbackServiceConnection(final ApnsEnvironment environment, final SSLContext sslContext,
            final NioEventLoopGroup eventLoopGroup, final FeedbackConnectionConfiguration configuration,
            final FeedbackServiceListener listener, final String name) {
        if (environment == null) {
            throw new NullPointerException("Environment must not be null.");
        }

        if (sslContext == null) {
            throw new NullPointerException("SSL context must not be null.");
        }

        if (eventLoopGroup == null) {
            throw new NullPointerException("Event loop group must not be null.");
        }

        if (configuration == null) {
            throw new NullPointerException("Feedback service connection configuration must not be null.");
        }

        if (name == null) {
            throw new NullPointerException("Feedback service connection name must not be null.");
        }

        this.environment = environment;
        this.sslContext = sslContext;
        this.eventLoopGroup = eventLoopGroup;
        this.configuration = configuration;
        this.listener = listener;
        this.name = name;
    }

    public synchronized void connect() {

        if (this.connectFuture != null) {
            throw new IllegalStateException(String.format("%s already started a connection attempt.", this.name));
        }

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);

        final LoggingHandler loggingHandler = new LoggingHandler();
        final FeedbackServiceConnection feedbackConnection = this;
        bootstrap.handler(new ChannelInitializer<SocketChannel>()
        {

            @Override
            protected void initChannel(final SocketChannel channel) throws Exception {
                final ChannelPipeline pipeline = channel.pipeline();

                pipeline.addFirst("logging", loggingHandler);
                final SSLEngine sslEngine = feedbackConnection.sslContext.createSSLEngine();
                sslEngine.setUseClientMode(true);

                pipeline.addLast("ssl", new SslHandler(sslEngine));
                pipeline.addLast("readTimeoutHandler",
                        new ReadTimeoutHandler(feedbackConnection.configuration.getReadTimeout()));
                pipeline.addLast("decoder", new ExpiredTokenDecoder());
                pipeline.addLast("handler", new FeedbackClientHandler(feedbackConnection));
            }
        });

        this.connectFuture = bootstrap.connect(this.environment.getFeedbackHost(), this.environment.getFeedbackPort());
        this.connectFuture.addListener(new GenericFutureListener<ChannelFuture>()
        {

            @Override
            public void operationComplete(final ChannelFuture connectFuture) {

                if (connectFuture.isSuccess()) {
                    log.debug("{} connected; waiting for TLS handshake.", feedbackConnection.name);

                    final SslHandler sslHandler = connectFuture.channel().pipeline().get(SslHandler.class);

                    try {
                        sslHandler.handshakeFuture().addListener(new GenericFutureListener<Future<Channel>>()
                        {

                            @Override
                            public void operationComplete(final Future<Channel> handshakeFuture) {
                                if (handshakeFuture.isSuccess()) {
                                    log.debug("{} successfully completed TLS handshake.", feedbackConnection.name);

                                    if (feedbackConnection.listener != null) {
                                        feedbackConnection.listener.handleConnectionSuccess(feedbackConnection);
                                    }

                                }
                                else {
                                    log.debug("{} failed to complete TLS handshake with APNs feedback service.",
                                            feedbackConnection.name, handshakeFuture.cause());

                                    connectFuture.channel().close();

                                    if (feedbackConnection.listener != null) {
                                        feedbackConnection.listener.handleConnectionFailure(feedbackConnection,
                                                handshakeFuture.cause());
                                    }
                                }
                            }
                        });
                    }
                    catch (NullPointerException e) {
                        log.warn("{} failed to get SSL handler and could not wait for a TLS handshake.",
                                feedbackConnection.name);

                        connectFuture.channel().close();

                        if (feedbackConnection.listener != null) {
                            feedbackConnection.listener.handleConnectionFailure(feedbackConnection, e);
                        }
                    }
                }
                else {
                    log.debug("{} failed to connect to APNs feedback service.", feedbackConnection.name,
                            connectFuture.cause());

                    if (feedbackConnection.listener != null) {
                        feedbackConnection.listener.handleConnectionFailure(feedbackConnection, connectFuture.cause());
                    }
                }
            }
        });
    }

    public synchronized void shutdownImmediately() {
        if (this.connectFuture != null) {
            this.connectFuture.channel().close();
            this.connectFuture.cancel(false);
        }
    }

    @Override
    public String toString() {
        return "FeedbackServiceConnection [name=" + name + "]";
    }
}
