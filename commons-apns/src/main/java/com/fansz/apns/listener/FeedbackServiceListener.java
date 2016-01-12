package com.fansz.apns.listener;

import com.fansz.apns.connection.FeedbackServiceConnection;
import com.fansz.apns.model.ExpiredToken;

public interface FeedbackServiceListener {

    /**
     * 与APNS Feedback连接建立成功
     */
    void handleConnectionSuccess(FeedbackServiceConnection connection);

    /**
     * 与APNS Feedback连接建立失败
     */
    void handleConnectionFailure(FeedbackServiceConnection connection, Throwable cause);

    /**
     * 接收到ANPS Feedback发送的过期DeviceToken信息
     */
    void handleExpiredToken(FeedbackServiceConnection connection, ExpiredToken token);

    /**
     * 处理连接关闭
     */
    void handleConnectionClosure(FeedbackServiceConnection connection);
}
