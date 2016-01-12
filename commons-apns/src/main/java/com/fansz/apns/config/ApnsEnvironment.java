package com.fansz.apns.config;

/**
 * <p>
 * APNS环境相关的参数，APNS提供了生产环境和测试环境 <a href=
 * "http://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ProvisioningDevelopment.html#//apple_ref/doc/uid/TP40008194-CH104-SW1"
 * > &quot;Provisioning and Development&quot;</a>.
 * </p>
 */
public class ApnsEnvironment {
    private final String apnsGatewayHost;

    private final int apnsGatewayPort;

    private final String feedbackHost;

    private final int feedbackPort;

    public ApnsEnvironment(final String apnsGatewayHost, final int apnsGatewayPort, final String feedbackHost,
            final int feedbackPort) {
        this.apnsGatewayHost = apnsGatewayHost;
        this.apnsGatewayPort = apnsGatewayPort;

        this.feedbackHost = feedbackHost;
        this.feedbackPort = feedbackPort;
    }

    public String getApnsGatewayHost() {
        return this.apnsGatewayHost;
    }

    public int getApnsGatewayPort() {
        return this.apnsGatewayPort;
    }

    public String getFeedbackHost() {
        return this.feedbackHost;
    }

    public int getFeedbackPort() {
        return this.feedbackPort;
    }

    /**
     * 返回生产环境配置信息
     */
    public static ApnsEnvironment getProductionEnvironment() {
        return new ApnsEnvironment("gateway.push.apple.com", 2195, "feedback.push.apple.com", 2196);
    }

    /**
     * 返回测试环境配置信息
     */
    public static ApnsEnvironment getSandboxEnvironment() {
        return new ApnsEnvironment("gateway.sandbox.push.apple.com", 2195, "feedback.sandbox.push.apple.com", 2196);
    }
}
