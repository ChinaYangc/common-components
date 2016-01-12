package com.fansz.apns.model;

import java.util.Arrays;
import java.util.Date;

import com.fansz.apns.util.TokenUtil;

/**
 * <p>
 * APNS Feedback Service返回的失效的DeviceToken,根据Apple文档:
 * </p>
 * <blockquote>When a push notification cannot be delivered because the intended app does not exist on the device, the
 * feedback service adds that device's token to its list. Push notifications that expire before being delivered are not
 * considered a failed delivery and don't impact the feedback service.</blockquote>
 * 
 * @see <a
 *      href="http://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW3">
 *      Local and Push Notification Programming Guide - Provider Communication with Apple Push Notification Service -
 *      The Feedback Service</a>
 */
public class ExpiredToken {
    private final byte[] token;

    private final Date expiration;

    public ExpiredToken(final byte[] token, final Date expiration) {
        this.token = Arrays.copyOf(token, token.length);
        this.expiration = new Date(expiration.getTime());
    }

    /**
     * 失效的device token
     * 
     * @return the expired token
     */
    public byte[] getToken() {
        return this.token;
    }

    /**
     * 失效时间
     */
    public Date getExpiration() {
        return this.expiration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expiration == null) ? 0 : expiration.hashCode());
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
        ExpiredToken other = (ExpiredToken)obj;
        if (expiration == null) {
            if (other.expiration != null)
                return false;
        }
        else if (!expiration.equals(other.expiration))
            return false;
        if (!Arrays.equals(token, other.token))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExpiredToken [token=" + TokenUtil.tokenBytesToString(token) + ", expiration=" + expiration + "]";
    }
}
