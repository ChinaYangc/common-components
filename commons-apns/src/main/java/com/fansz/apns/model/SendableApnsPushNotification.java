package com.fansz.apns.model;

import com.fansz.apns.support.ApnsPushNotification;
import com.fansz.apns.util.TokenUtil;

/**
 * 推送消息包装类，包含推送identifier,当消息推送出错时，可以通过identifier进行后续处理
 */
public class SendableApnsPushNotification<T extends ApnsPushNotification> {
    private final T pushNotification;

    private final int sequenceNumber;

    /**
     * 构造器
     * 
     * @param pushNotification 推送消息
     * @param sequenceNumber 推送identifier
     */
    public SendableApnsPushNotification(final T pushNotification, final int sequenceNumber) {
        this.pushNotification = pushNotification;
        this.sequenceNumber = sequenceNumber;
    }

    public T getPushNotification() {
        return this.pushNotification;
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    @Override
    public String toString() {
        return String.format(
                "SendableApnsPushNotification [sequenceNumber=%d, token=%s, payload=%s, deliveryInvalidation=%s]",
                this.sequenceNumber, TokenUtil.tokenBytesToString(this.pushNotification.getToken()),
                this.pushNotification.getPayload(), this.pushNotification.getDeliveryInvalidationTime());
    }
}
