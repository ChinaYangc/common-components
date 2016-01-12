package com.fansz.apns.support;

/**
 * 消息推送优先级，影响消息被推送到设备；
 * 
 * @see <a
 *      href="https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW4">
 *      Local and Push Notification Programming Guide, Provider Communication with Apple Push Notification Service, The
 *      Binary Interface and Notification Format</a>
 */
public enum DeliveryPriority {

    /**
     * 消息被立即推送到设备
     * <p>
     * 消息推送会触发三种情形：
     * 1、显示alert；
     * 2、播放声音
     * 3、图标上显示badge;
     * The push notification must trigger an alert, sound, or badge on the device. It is an error to use this priority
     * for a push that contains only the {@code content-available} key.
     * </p>
     */
    IMMEDIATE((byte)10),

    /**
     * <p>
     * According to Apple's documentation:
     * </p>
     * <blockquote>
     * <p>
     * The push message is sent at a time that conserves power on the device receiving it.
     * </p>
     * </blockquote>
     */
    CONSERVE_POWER((byte)5);

    private final byte code;

    private DeliveryPriority(final byte code) {
        this.code = code;
    }

    public byte getCode() {
        return this.code;
    }

    public static DeliveryPriority getFromCode(final byte code) {
        for (final DeliveryPriority priority : DeliveryPriority.values()) {
            if (priority.getCode() == code) {
                return priority;
            }
        }

        throw new IllegalArgumentException(String.format("No delivery priority found with code %d", code));
    }
}
