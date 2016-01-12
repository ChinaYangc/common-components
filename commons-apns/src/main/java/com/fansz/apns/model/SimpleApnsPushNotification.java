package com.fansz.apns.model;

import java.util.Arrays;
import java.util.Date;

import com.fansz.apns.support.ApnsPushNotification;
import com.fansz.apns.support.DeliveryPriority;
import com.fansz.apns.util.ApnsPayloadBuilder;
import com.fansz.apns.util.TokenUtil;

/**
 * {@link ApnsPushNotification}接口的简单实现.
 * 
 * @see ApnsPayloadBuilder
 * @see TokenUtil
 */
public class SimpleApnsPushNotification implements ApnsPushNotification {

    private final byte[] token;

    private final String payload;

    private final Date invalidationTime;

    private final DeliveryPriority priority;

    public SimpleApnsPushNotification(final byte[] token, final String payload) {
        this(token, payload, null, DeliveryPriority.IMMEDIATE);
    }

    public SimpleApnsPushNotification(final byte[] token, final String payload, final Date invalidationTime) {
        this(token, payload, invalidationTime, DeliveryPriority.IMMEDIATE);
    }

    public SimpleApnsPushNotification(final byte[] token, final String payload, final DeliveryPriority priority) {
        this(token, payload, null, priority);
    }

    public SimpleApnsPushNotification(final byte[] token, final String payload, final Date invalidationTime,
            final DeliveryPriority priority) {
        this.token = token;
        this.payload = payload;
        this.invalidationTime = invalidationTime;
        this.priority = priority;
    }

    public byte[] getToken() {
        return this.token;
    }

    public String getPayload() {
        return this.payload;
    }

    public Date getDeliveryInvalidationTime() {
        return this.invalidationTime;
    }

    public DeliveryPriority getPriority() {
        return this.priority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((invalidationTime == null) ? 0 : invalidationTime.hashCode());
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + Arrays.hashCode(token);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleApnsPushNotification other = (SimpleApnsPushNotification)obj;
        if (invalidationTime == null) {
            if (other.invalidationTime != null)
                return false;
        }
        else if (!invalidationTime.equals(other.invalidationTime))
            return false;
        if (payload == null) {
            if (other.payload != null)
                return false;
        }
        else if (!payload.equals(other.payload))
            return false;
        if (priority != other.priority)
            return false;
        if (!Arrays.equals(token, other.token))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SimpleApnsPushNotification [token=" + Arrays.toString(token) + ", payload=" + payload
                + ", invalidationTime=" + invalidationTime + ", priority=" + priority + "]";
    }
}
