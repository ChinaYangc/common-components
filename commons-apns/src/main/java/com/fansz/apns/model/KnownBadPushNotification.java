package com.fansz.apns.model;

import java.util.Date;

import com.fansz.apns.support.ApnsPushNotification;
import com.fansz.apns.support.DeliveryPriority;

/**
 * <p>
 * 格式错误的推送通知，用于触发APN连接的关闭
 * </p>
 */
public class KnownBadPushNotification implements ApnsPushNotification {

    public byte[] getToken() {
        return new byte[0];
    }

    public String getPayload() {
        return "";
    }

    public Date getDeliveryInvalidationTime() {
        return null;
    }

    public DeliveryPriority getPriority() {
        return DeliveryPriority.IMMEDIATE;
    }
}
