package com.fansz.apns.config;

import com.fansz.apns.connection.ApnsConnection;

/**
 * APNS连接配置类
 */
public class ApnsConnectionConfiguration {

    private int sentNotificationBufferCapacity = ApnsConnection.DEFAULT_SENT_NOTIFICATION_BUFFER_CAPACITY;

    private Integer closeAfterInactivityTime = null;

    private Integer gracefulShutdownTimeout = null;

    private Integer sendAttemptLimit = null;

    public ApnsConnectionConfiguration() {
    }

    public ApnsConnectionConfiguration(final ApnsConnectionConfiguration configuration) {
        this.sentNotificationBufferCapacity = configuration.sentNotificationBufferCapacity;
        this.closeAfterInactivityTime = configuration.closeAfterInactivityTime;
        this.gracefulShutdownTimeout = configuration.gracefulShutdownTimeout;
        this.sendAttemptLimit = configuration.sendAttemptLimit;
    }

    public int getSentNotificationBufferCapacity() {
        return sentNotificationBufferCapacity;
    }

    public void setSentNotificationBufferCapacity(final int sentNotificationBufferCapacity) {
        this.sentNotificationBufferCapacity = sentNotificationBufferCapacity;
    }

    public Integer getCloseAfterInactivityTime() {
        return this.closeAfterInactivityTime;
    }

    public void setCloseAfterInactivityTime(final Integer closeAfterInactivityTime) {
        this.closeAfterInactivityTime = closeAfterInactivityTime;
    }

    public Integer getGracefulDisconnectionTimeout() {
        return this.gracefulShutdownTimeout;
    }

    public void setGracefulDisconnectionTimeout(final Integer gracefulShutdownTimeout) {
        this.gracefulShutdownTimeout = gracefulShutdownTimeout;
    }

    public Integer getSendAttemptLimit() {
        return this.sendAttemptLimit;
    }

    public void setSendAttemptLimit(final Integer sendAttemptLimit) {
        this.sendAttemptLimit = sendAttemptLimit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((closeAfterInactivityTime == null) ? 0 : closeAfterInactivityTime.hashCode());
        result = prime * result + ((gracefulShutdownTimeout == null) ? 0 : gracefulShutdownTimeout.hashCode());
        result = prime * result + ((sendAttemptLimit == null) ? 0 : sendAttemptLimit.hashCode());
        result = prime * result + sentNotificationBufferCapacity;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ApnsConnectionConfiguration other = (ApnsConnectionConfiguration)obj;
        if (closeAfterInactivityTime == null) {
            if (other.closeAfterInactivityTime != null)
                return false;
        }
        else if (!closeAfterInactivityTime.equals(other.closeAfterInactivityTime))
            return false;
        if (gracefulShutdownTimeout == null) {
            if (other.gracefulShutdownTimeout != null)
                return false;
        }
        else if (!gracefulShutdownTimeout.equals(other.gracefulShutdownTimeout))
            return false;
        if (sendAttemptLimit == null) {
            if (other.sendAttemptLimit != null)
                return false;
        }
        else if (!sendAttemptLimit.equals(other.sendAttemptLimit))
            return false;
        if (sentNotificationBufferCapacity != other.sentNotificationBufferCapacity)
            return false;
        return true;
    }

}
