package com.fansz.apns.listener;

import java.util.Collection;

import com.fansz.apns.PushManager;
import com.fansz.apns.model.ExpiredToken;
import com.fansz.apns.support.ApnsPushNotification;

/**
 * <p>
 * 侦听Feedback Service返回的失效Device Token
 * </p>
 *
 * @see PushManager#registerExpiredTokenListener(ExpiredTokenListener)
 * @see PushManager#unregisterExpiredTokenListener(ExpiredTokenListener)
 * @see PushManager#requestExpiredTokens()
 */
public interface ExpiredTokenListener<T extends ApnsPushNotification> {

    /**
     * 处理Feedback Service返回的失效的Device Token
     */
    void handleExpiredTokens(PushManager<? extends T> pushManager, Collection<ExpiredToken> expiredTokens);
}
