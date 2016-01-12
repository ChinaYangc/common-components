package com.fansz.apns;

import com.fansz.apns.connection.ApnsConnection;
import com.fansz.apns.connection.FeedbackServiceConnection;
import com.fansz.apns.listener.ApnsConnectionListener;
import com.fansz.apns.listener.FeedbackServiceListener;
import com.fansz.apns.support.ApnsPushNotification;
import io.netty.channel.nio.NioEventLoopGroup;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fansz.apns.config.ApnsEnvironment;
import com.fansz.apns.config.PushManagerConfiguration;
import com.fansz.apns.listener.ExpiredTokenListener;
import com.fansz.apns.listener.FailedConnectionListener;
import com.fansz.apns.listener.RejectedNotificationListener;
import com.fansz.apns.model.ExpiredToken;
import com.fansz.apns.support.RejectedNotificationReason;

/**
 * <p>
 * PushManager管理和Apple Gateway的连接已经消息的推送
 * </p>
 * <h2>队列</h2>
 * <p>
 * A push manager 有两个队列: 调用者要推送消息队列和发送失败的重试队列, 调用者将消息添加到队列，从而实现消息的发送; (see {@link PushManager#getQueue()}).
 * 在准备就绪之后，PushManager 将从队列获取要推送的消息, 无论发送成功失败，消息都将从发送队列中删除. 如果消息发送失败，将会被加入内部的重试队列；在处 理发送队列中的新消息之前
 * ，PushManager总会试发送重试队列中的消息;
 * </p>
 * <h2>关闭</h2>
 * <p>
 * PushManager支持带timeout和无timeout的关闭方式，一旦进入关闭状态，PushManager将不会接受新的消息，PushManager通过要求所有连接都发送格式错误的消息给APNS，从而实现
 * 优雅的关闭；PushManager将会保留所有关闭的连接，并且尝试发送重试队列中的消息，则到消息队列为空；如果在timeout的时间内，仍然有未关闭的连接，那么所有的连接都会被立即关闭
 * </p>
 * <p>
 * 当调用无timeout参数的关闭方法时，系统会确保重试队列中的所有消息都成功传送到Apple gateway
 * </p>
 * <h2>错误处理/h2>
 * <p>
 * 调用者可以注册listener去处理APNS连接失败和消息发送失败的场景；
 * </p>
 * <p>
 * 当推送的消息被APNS拒绝时，通常是因为消息格式存在问题，因此不建议在Listener中重复发送消息.
 * 当连接到APNS服务失败时，系统会尝试重新连接；但在某些特殊情况下，比如SSL握手失败，此时不建议重新连接，因为通常这种场景是因为证书存在问题；
 * </p>
 *
 * @see PushManager#getQueue()
 */
public class PushManager<T extends ApnsPushNotification> implements ApnsConnectionListener<T>, FeedbackServiceListener {
    private final BlockingQueue<T> queue;

    private final LinkedBlockingQueue<T> retryQueue = new LinkedBlockingQueue<T>();

    private final ApnsEnvironment environment;

    private final SSLContext sslContext;

    private final PushManagerConfiguration configuration;

    private final String name;

    private static final AtomicInteger pushManagerCounter = new AtomicInteger(0);

    private AtomicInteger connectionCounter = new AtomicInteger(0);

    private int feedbackConnectionCounter = 0;

    private final HashSet<ApnsConnection<T>> activeConnections = new HashSet<ApnsConnection<T>>();

    private final LinkedBlockingQueue<ApnsConnection<T>> writableConnections = new LinkedBlockingQueue<ApnsConnection<T>>();

    private final Object feedbackConnectionMonitor = new Object();

    private FeedbackServiceConnection feedbackConnection;

    private List<ExpiredToken> expiredTokens;

    private final List<RejectedNotificationListener<? super T>> rejectedNotificationListeners = new ArrayList<RejectedNotificationListener<? super T>>();

    private final List<FailedConnectionListener<? super T>> failedConnectionListeners = new ArrayList<FailedConnectionListener<? super T>>();

    private final List<ExpiredTokenListener<? super T>> expiredTokenListeners = new ArrayList<ExpiredTokenListener<? super T>>();

    private Thread dispatchThread;

    private boolean dispatchThreadShouldContinue = true;

    private final NioEventLoopGroup eventLoopGroup;

    private final boolean shouldShutDownEventLoopGroup;

    private final ExecutorService listenerExecutorService;

    private final boolean shouldShutDownListenerExecutorService;

    private boolean shutDownStarted = false;

    private boolean shutDownFinished = false;

    private static final Logger log = LoggerFactory.getLogger(PushManager.class);

    private static class DispatchThreadExceptionHandler<T extends ApnsPushNotification> implements
            UncaughtExceptionHandler {
        private final Logger log = LoggerFactory.getLogger(DispatchThreadExceptionHandler.class);

        final PushManager<T> manager;

        public DispatchThreadExceptionHandler(final PushManager<T> manager) {
            this.manager = manager;
        }

        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            log.error("Dispatch thread for {} died unexpectedly. Please file a bug with the exception details.",
                    this.manager.name, e);

            if (this.manager.isStarted()) {
                this.manager.createAndStartDispatchThread();
            }
        }
    }

    /**
     * 构造器提供了NioEventLoopGroup参数，一旦调用者传入了该参数，则调用者需要维护NioEventLoopGroup的生命周期，也就是当所有PushManager被关闭之后，
     * 需要将NioEventLoopGroup关闭； 另外构造器提供了{@link ExecutorService}
     * 参数，Eeecutor主要是将消息转发到注册的listenr上，如果传入的参数非空，需要采取与NioEventLoopGroup相同的处理；
     *
     * @seeAlso http://developer.apple.com/library/mac/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters
     * /CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW6
     */
    public PushManager(final ApnsEnvironment environment, final SSLContext sslContext,
                       final NioEventLoopGroup eventLoopGroup, final ExecutorService listenerExecutorService,
                       final BlockingQueue<T> queue, final PushManagerConfiguration configuration, final String name) {

        this.queue = queue != null ? queue : new LinkedBlockingQueue<T>();

        if (environment == null) {
            throw new NullPointerException("Environment must not be null.");
        }

        this.environment = environment;

        if (sslContext == null) {
            throw new NullPointerException("SSL context must not be null.");
        }

        this.sslContext = sslContext;

        if (configuration == null) {
            throw new NullPointerException("Configuration object must not be null.");
        }

        this.configuration = new PushManagerConfiguration(configuration);
        this.name = name == null ? String.format("PushManager-%d", PushManager.pushManagerCounter.getAndIncrement())
                : name;

        if (eventLoopGroup != null) {
            this.eventLoopGroup = eventLoopGroup;
            this.shouldShutDownEventLoopGroup = false;
        } else {
            // Never use more threads than concurrent connections (Netty binds a channel to a single thread, so the
            // excess threads would always go unused)
            final int threadCount = Math.min(this.configuration.getConcurrentConnectionCount(), Runtime.getRuntime()
                    .availableProcessors() * 2);

            this.eventLoopGroup = new NioEventLoopGroup(threadCount);
            this.shouldShutDownEventLoopGroup = true;
        }

        if (listenerExecutorService != null) {
            this.listenerExecutorService = listenerExecutorService;
            this.shouldShutDownListenerExecutorService = false;
        } else {
            this.listenerExecutorService = Executors.newSingleThreadExecutor();
            this.shouldShutDownListenerExecutorService = true;
        }
    }

    /**
     * 启动PushManager，开始消息发送，在未调用该方式前，队列中的消息不会被发送；
     *
     * @throws IllegalStateException 如果PushManager已经启动
     */
    public synchronized void start() {
        if (this.isStarted()) {
            throw new IllegalStateException("Push manager has already been started.");
        }

        if (this.isShutDown()) {
            throw new IllegalStateException("Push manager has already been shut down and may not be restarted.");
        }

        log.info("{} starting.", this.name);

        for (int i = 0; i < this.configuration.getConcurrentConnectionCount(); i++) {
            this.startNewConnection();
        }

        this.createAndStartDispatchThread();
    }

    private void createAndStartDispatchThread() {
        this.dispatchThread = createDispatchThread();
        this.dispatchThread.setUncaughtExceptionHandler(new DispatchThreadExceptionHandler<T>(this));
        this.dispatchThread.start();
    }

    protected Thread createDispatchThread() {
        return new Thread(new Runnable() {

            @Override
            public void run() {
                while (dispatchThreadShouldContinue) {
                    try {
                        final ApnsConnection<T> connection = writableConnections.take();

                        writableConnections.add(connection);

                        final T notificationToRetry = retryQueue.poll();

                        if (notificationToRetry != null) {
                            connection.sendNotification(notificationToRetry);
                        } else {
                            if (shutDownStarted) {
                                connection.disconnectGracefully();
                                writableConnections.remove(connection);
                            } else {
                                connection.sendNotification(queue.take());
                            }
                        }
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }

        });
    }

    public boolean isStarted() {
        if (this.isShutDown()) {
            return false;
        } else {
            return this.dispatchThread != null;
        }
    }

    public boolean isShutDown() {
        return this.shutDownStarted;
    }

    public synchronized void shutdown() throws InterruptedException {
        this.shutdown(0);
    }

    public synchronized List<T> shutdown(long timeout) throws InterruptedException {
        if (this.isShutDown()) {
            log.warn("{} has already been shut down; shutting down multiple times is harmless, but may "
                    + "indicate a problem elsewhere.", this.name);
        } else {
            log.info("{} shutting down.", this.name);
        }

        if (this.shutDownFinished) {
            // We COULD throw an IllegalStateException here, but it seems unnecessary when we could just silently return
            // the same result without harm.
            return new ArrayList<T>(this.retryQueue);
        }

        if (!this.isStarted()) {
            throw new IllegalStateException("Push manager has not yet been started and cannot be shut down.");
        }

        this.shutDownStarted = true;

        synchronized (this.feedbackConnectionMonitor) {
            if (this.feedbackConnection != null) {
                this.feedbackConnection.shutdownImmediately();
            }
        }

        this.dispatchThread.interrupt();

        final Date deadline = timeout > 0 ? new Date(System.currentTimeMillis() + timeout) : null;

        // The dispatch thread will close connections when the retry queue is empty
        this.waitForAllConnectionsToFinish(deadline);

        this.dispatchThreadShouldContinue = false;
        this.dispatchThread.interrupt();
        this.dispatchThread.join();

        if (deadline == null) {
            assert this.retryQueue.isEmpty();
            assert this.activeConnections.isEmpty();
        }

        synchronized (this.activeConnections) {
            for (final ApnsConnection<T> connection : this.activeConnections) {
                connection.disconnectImmediately();
            }
        }

        synchronized (this.rejectedNotificationListeners) {
            this.rejectedNotificationListeners.clear();
        }

        synchronized (this.failedConnectionListeners) {
            this.failedConnectionListeners.clear();
        }

        synchronized (this.expiredTokenListeners) {
            this.expiredTokenListeners.clear();
        }

        if (this.shouldShutDownListenerExecutorService) {
            this.listenerExecutorService.shutdown();
        }

        if (this.shouldShutDownEventLoopGroup) {
            this.eventLoopGroup.shutdownGracefully().await();
        }

        this.shutDownFinished = true;

        return new ArrayList<T>(this.retryQueue);
    }

    public void registerRejectedNotificationListener(final RejectedNotificationListener<? super T> listener) {
        if (this.isShutDown()) {
            throw new IllegalStateException(
                    "Rejected notification listeners may not be registered after a push manager has been shut down.");
        }

        synchronized (this.rejectedNotificationListeners) {
            this.rejectedNotificationListeners.add(listener);
        }
    }

    public boolean unregisterRejectedNotificationListener(final RejectedNotificationListener<? super T> listener) {
        synchronized (this.rejectedNotificationListeners) {
            return this.rejectedNotificationListeners.remove(listener);
        }
    }

    public void registerFailedConnectionListener(final FailedConnectionListener<? super T> listener) {
        if (this.isShutDown()) {
            throw new IllegalStateException(
                    "Failed connection listeners may not be registered after a push manager has been shut down.");
        }

        synchronized (this.failedConnectionListeners) {
            this.failedConnectionListeners.add(listener);
        }
    }

    public boolean unregisterFailedConnectionListener(final FailedConnectionListener<? super T> listener) {
        synchronized (this.failedConnectionListeners) {
            return this.failedConnectionListeners.remove(listener);
        }
    }

    public String getName() {
        return this.name;
    }

    public BlockingQueue<T> getQueue() {
        return this.queue;
    }

    public BlockingQueue<T> getRetryQueue() {
        return this.retryQueue;
    }

    public void registerExpiredTokenListener(final ExpiredTokenListener<? super T> listener) {
        if (this.isShutDown()) {
            throw new IllegalStateException(
                    "Expired token listeners may not be registered after a push manager has been shut down.");
        }

        synchronized (this.expiredTokenListeners) {
            this.expiredTokenListeners.add(listener);
        }
    }

    public boolean unregisterExpiredTokenListener(final ExpiredTokenListener<? super T> listener) {
        synchronized (this.expiredTokenListeners) {
            return this.expiredTokenListeners.remove(listener);
        }
    }

    public synchronized void requestExpiredTokens() {
        if (!this.isStarted()) {
            throw new IllegalStateException("Push manager has not been started yet.");
        }

        if (this.isShutDown()) {
            throw new IllegalStateException("Push manager has already been shut down.");
        }

        synchronized (this.feedbackConnectionMonitor) {
            // If we already have a feedback connection in play, let it finish
            if (this.feedbackConnection == null) {
                this.expiredTokens = new ArrayList<ExpiredToken>();

                this.feedbackConnection = new FeedbackServiceConnection(this.environment, this.sslContext,
                        this.eventLoopGroup, this.configuration.getFeedbackConnectionConfiguration(), this,
                        String.format("%s-feedbackConnection-%d", this.name, this.feedbackConnectionCounter++));

                this.feedbackConnection.connect();
            }
        }
    }

    @Override
    public void handleConnectionSuccess(final FeedbackServiceConnection connection) {
        log.debug("Feedback connection succeeded: {}", connection);
    }

    @Override
    public void handleConnectionFailure(final FeedbackServiceConnection connection, final Throwable cause) {
        log.debug("Feedback connection failed: {}", connection, cause);

        synchronized (this.feedbackConnectionMonitor) {
            this.feedbackConnection = null;
        }

        synchronized (this.failedConnectionListeners) {
            final PushManager<T> pushManager = this;

            for (final FailedConnectionListener<? super T> listener : this.failedConnectionListeners) {

                // Handle connection failures in a separate thread in case a handler takes a long time to run
                this.listenerExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        listener.handleFailedConnection(pushManager, cause);
                    }
                });
            }
        }
    }

    @Override
    public void handleExpiredToken(final FeedbackServiceConnection connection, final ExpiredToken token) {
        log.debug("Received expired token {} from feedback connection {}.", token, connection);
        this.expiredTokens.add(token);
    }

    @Override
    public void handleConnectionClosure(final FeedbackServiceConnection connection) {
        log.debug("Feedback connection closed: {}", connection);

        final PushManager<T> pushManager = this;
        final List<ExpiredToken> expiredTokens = new ArrayList<ExpiredToken>(this.expiredTokens);

        synchronized (this.expiredTokenListeners) {
            for (final ExpiredTokenListener<? super T> listener : this.expiredTokenListeners) {
                this.listenerExecutorService.submit(new Runnable() {

                    @Override
                    public void run() {
                        listener.handleExpiredTokens(pushManager, expiredTokens);
                    }
                });
            }
        }

        synchronized (this.feedbackConnectionMonitor) {
            this.feedbackConnection = null;
            this.expiredTokens = null;
        }
    }

    @Override
    public void handleConnectionSuccess(final ApnsConnection<T> connection) {
        log.debug("Connection succeeded: {}", connection);

        if (this.dispatchThreadShouldContinue) {
            this.writableConnections.add(connection);
        } else {
            // There's no dispatch thread to use this connection, so shut it down immediately
            connection.disconnectImmediately();
        }
    }

    @Override
    public void handleConnectionFailure(final ApnsConnection<T> connection, final Throwable cause) {

        log.debug("Connection failed: {}", connection, cause);

        this.removeActiveConnection(connection);

        synchronized (this.failedConnectionListeners) {
            final PushManager<T> pushManager = this;

            for (final FailedConnectionListener<? super T> listener : this.failedConnectionListeners) {

                // Handle connection failures in a separate thread in case a handler takes a long time to run
                this.listenerExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        listener.handleFailedConnection(pushManager, cause);
                    }
                });
            }
        }

        // As long as we're not shut down, try to open a replacement connection.
        if (this.shouldReplaceClosedConnection()) {
            this.startNewConnection();
        }
    }

    @Override
    public void handleConnectionWritabilityChange(final ApnsConnection<T> connection, final boolean writable) {

        log.debug("Writability for {} changed to {}", connection, writable);

        if (writable) {
            this.writableConnections.add(connection);
        } else {
            this.writableConnections.remove(connection);
            this.dispatchThread.interrupt();
        }
    }

    @Override
    public void handleConnectionClosure(final ApnsConnection<T> connection) {

        log.debug("Connection closed: {}", connection);

        this.writableConnections.remove(connection);
        this.dispatchThread.interrupt();

        if (this.shouldReplaceClosedConnection()) {
            this.startNewConnection();
        }

        removeActiveConnection(connection);
    }

    @Override
    public void handleWriteFailure(ApnsConnection<T> connection, T notification, Throwable cause) {
        this.retryQueue.add(notification);
        this.dispatchThread.interrupt();
    }

    @Override
    public void handleRejectedNotification(final ApnsConnection<T> connection, final T rejectedNotification,
                                           final RejectedNotificationReason reason) {

        log.debug("{} rejected {}: {}", new Object[]{connection, rejectedNotification, reason});

        final PushManager<T> pushManager = this;

        synchronized (this.rejectedNotificationListeners) {
            for (final RejectedNotificationListener<? super T> listener : this.rejectedNotificationListeners) {

                // Handle the notifications in a separate thread in case a listener takes a long time to run
                this.listenerExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.handleRejectedNotification(pushManager, rejectedNotification, reason);
                    }
                });
            }
        }
    }

    @Override
    public void handleUnprocessedNotifications(ApnsConnection<T> connection, Collection<T> unprocessedNotifications) {

        log.debug("{} returned {} unprocessed notifications", connection, unprocessedNotifications.size());

        this.retryQueue.addAll(unprocessedNotifications);

        this.dispatchThread.interrupt();
    }

    private void startNewConnection() {
        final ApnsConnection<T> connection = new ApnsConnection<T>(this.environment, this.sslContext,
                this.eventLoopGroup, this.configuration.getConnectionConfiguration(), this, String.format(
                "%s-connection-%d", this.name, this.connectionCounter.getAndIncrement()));

        connection.connect();

        synchronized (this.activeConnections) {
            this.activeConnections.add(connection);
        }
    }

    private void removeActiveConnection(final ApnsConnection<T> connection) {
        synchronized (this.activeConnections) {
            final boolean removedConnection = this.activeConnections.remove(connection);
            assert removedConnection;

            if (this.activeConnections.isEmpty()) {
                this.activeConnections.notifyAll();
            }
        }
    }

    private void waitForAllConnectionsToFinish(final Date deadline) throws InterruptedException {
        synchronized (this.activeConnections) {
            while (!this.activeConnections.isEmpty() && !PushManager.hasDeadlineExpired(deadline)) {
                if (deadline != null) {
                    this.activeConnections.wait(PushManager.getMillisToWaitForDeadline(deadline));
                } else {
                    this.activeConnections.wait();
                }
            }
        }
    }

    private static long getMillisToWaitForDeadline(final Date deadline) {
        return Math.max(deadline.getTime() - System.currentTimeMillis(), 1);
    }

    private static boolean hasDeadlineExpired(final Date deadline) {
        if (deadline != null) {
            return System.currentTimeMillis() > deadline.getTime();
        } else {
            return false;
        }
    }

    private boolean shouldReplaceClosedConnection() {
        if (this.shutDownStarted) {
            if (this.dispatchThreadShouldContinue) {
                return !this.retryQueue.isEmpty();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
