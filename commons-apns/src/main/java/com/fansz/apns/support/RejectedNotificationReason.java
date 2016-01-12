package com.fansz.apns.support;

/**
 * 
 枚举，代码APNS返回的错误代码
 */
public enum RejectedNotificationReason {
    NO_ERROR((byte)0), PROCESSING_ERROR((byte)1), MISSING_TOKEN((byte)2), MISSING_TOPIC((byte)3), MISSING_PAYLOAD(
            (byte)4), INVALID_TOKEN_SIZE((byte)5), INVALID_TOPIC_SIZE((byte)6), INVALID_PAYLOAD_SIZE((byte)7), INVALID_TOKEN(
            (byte)8),

    /**
     * <p>
     * Indicates that the notification was accepted, but the connection is being shut down for maintenance. According to
     * Apple's documentation:
     * </p>
     * <blockquote>A status code of 10 indicates that the APNs server closed the connection (for example, to perform
     * maintenance). The notification identifier in the error response indicates the last notification that was
     * successfully sent. Any notifications you sent after it have been discarded and must be resent. When you receive
     * this status code, stop using this connection and open a new connection.</blockquote>
     */
    SHUTDOWN((byte)10),

    UNKNOWN((byte)255);

    private final byte errorCode;

    private RejectedNotificationReason(final byte errorCode) {
        this.errorCode = errorCode;
    }

    public byte getErrorCode() {
        return this.errorCode;
    }

    public static RejectedNotificationReason getByErrorCode(final byte errorCode) {
        for (final RejectedNotificationReason error : RejectedNotificationReason.values()) {
            if (error.errorCode == errorCode) {
                return error;
            }
        }

        throw new IllegalArgumentException(String.format("Unrecognized error code: %d", errorCode));
    }
}
