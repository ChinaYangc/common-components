package com.fansz.apns.listener;

import java.util.Collection;

import com.fansz.apns.connection.ApnsConnection;
import com.fansz.apns.support.ApnsPushNotification;
import com.fansz.apns.support.RejectedNotificationReason;

public interface ApnsConnectionListener<T extends ApnsPushNotification> {

    /**
     * 与APNS的连接成功建立
     */
    void handleConnectionSuccess(ApnsConnection<T> connection);

    /**
     * 与APNS的连接建立失败
     */
    void handleConnectionFailure(ApnsConnection<T> connection, Throwable cause);

    /**
     * 连接的可写状态发送变化
     */
    void handleConnectionWritabilityChange(ApnsConnection<T> connection, boolean writable);

    /**
     * 与APNS的连接断开.
     */
    void handleConnectionClosure(ApnsConnection<T> connection);

    /**
     * 通过连接发送消息失败,通常这是临时IO问题引起的；
     */
    void handleWriteFailure(ApnsConnection<T> connection, T notification, Throwable cause);

    /**
     * ANPS拒绝了发送的消息，通常这是永久性的失败，及时重试也无法解决
     */
    void handleRejectedNotification(ApnsConnection<T> connection, T rejectedNotification,
                                    RejectedNotificationReason reason);

    /**
     * 发送给APNS的消息并没有被处理，应该重新进行发送;
     */
    void handleUnprocessedNotifications(ApnsConnection<T> connection, Collection<T> unprocessedNotifications);
}
