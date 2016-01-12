package com.fansz.apns.model;

import com.fansz.apns.support.RejectedNotificationReason;

/**
 * <p>
 * A tuple of a notification sequence number rejected by APNs and the reason for its rejection.
 * </p>
 *
 */
public class RejectedNotification {
    private final int sequenceNumber;

    private final RejectedNotificationReason rejectionReason;

    /**
     * Constructs a new rejected notification tuple with the given sequence number and rejection reason.
     * 
     * @param sequenceNumber the sequence number of the rejected notification
     * @param rejectionReason the reason reported by APNs for the rejection
     */
    public RejectedNotification(final int sequenceNumber, final RejectedNotificationReason rejectionReason) {
        this.sequenceNumber = sequenceNumber;
        this.rejectionReason = rejectionReason;
    }

    /**
     * Returns the sequence number of the notification rejected by APNs.
     * 
     * @return the sequence number of the notification rejected by APNs
     */
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * Returns the reason the notification was rejected by APNs.
     * 
     * @return the reason the notification was rejected by APNs
     */
    public RejectedNotificationReason getReason() {
        return this.rejectionReason;
    }
}
