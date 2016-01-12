package com.fansz.apns.listener;

import com.fansz.apns.PushManager;
import com.fansz.apns.support.ApnsPushNotification;
import com.fansz.apns.support.RejectedNotificationReason;

/**
 * @see <a
 *      href="http://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW4">
 *      Local and Push Notification Programming Guide - Provider Communication with Apple Push Notification Service -
 *      The Binary Interface and Notification Formats</a>
 * @see PushManager#registerRejectedNotificationListener(RejectedNotificationListener)
 * @see PushManager#unregisterRejectedNotificationListener(RejectedNotificationListener)
 */
public interface RejectedNotificationListener<T extends ApnsPushNotification> {

    /**
     * 处理APNS返回的推送失败消息
     */
    void handleRejectedNotification(PushManager<? extends T> pushManager, T notification,
                                    RejectedNotificationReason rejectionReason);
}
