package com.fansz.apns.support;

import java.util.Date;

import com.fansz.apns.util.ApnsPayloadBuilder;
import com.fansz.apns.util.TokenUtil;

/**
 * <p>
 * APNS规范定义的推送消息，由device token和JSON payload已经一个可选的时间戳构成，在此时间之后，消息将不会被推送
 * </p>
 *
 * @see <a
 *      href="https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9">
 *      Apple Push Notification Service</a>
 * @see TokenUtil
 * @see ApnsPayloadBuilder
 */
public interface ApnsPushNotification {
    /**
     * 设备的Device Token，用于标识接收消息的终端
     */
    byte[] getToken();

    /**
     * JSON格式推送通知的Payload
     */
    String getPayload();

    /**
     * 定义消息的失效时间,如果为null,表示APN服务器不会存储消息，注意当消息不能实时到达时，APN只会为每个设备存储最近的一次消息
     * 
     * @see <a
     *      href="https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW1">
     *      Provider Communication with Apple Push Notification Service</a>
     */
    Date getDeliveryInvalidationTime();

    /**
     * 定义消息的优先级，如果为null,则默认为IMMEDIATE an immediate delivery priority is assumed.
     * 
     * @return the priority with which this push notification should be sent to the receiving device
     * @see <a
     *      href="https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW1">
     *      Provider Communication with Apple Push Notification Service</a>
     */
    DeliveryPriority getPriority();
}
