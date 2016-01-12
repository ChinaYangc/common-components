package com.fansz.apns.listener;

import com.fansz.apns.PushManager;
import com.fansz.apns.support.ApnsPushNotification;

/**
 * @see PushManager#registerFailedConnectionListener(FailedConnectionListener)
 * @see PushManager#unregisterFailedConnectionListener(FailedConnectionListener)
 */
public interface FailedConnectionListener<T extends ApnsPushNotification> {

    /**
     * 处理连接失败，例如SSLHandshakeException
     */
    void handleFailedConnection(PushManager<? extends T> pushManager, Throwable cause);
}
