package com.fansz.apns.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.fansz.apns.model.SendableApnsPushNotification;

/**
 * <p>
 * A bounded-length buffer meant to store sent APNs notifications. This is necessary because the APNs protocol is
 * asynchronous, and notifications may be identified as failed or in need of retransmission after they've been
 * successfully written to the wire.
 * </p>
 * <p>
 * If a notification is present in the buffer, it is assumed to have been written to the outbound network buffer, but
 * its state is otherwise unknown.
 * </p>
 */
public class SentNotificationBuffer<E extends ApnsPushNotification> {

    private final int capacity;

    private final ArrayDeque<SendableApnsPushNotification<E>> sentNotifications;

    /**
     * Constructs a new sent notification buffer with the given maximum capacity.
     *
     * @param capacity the capacity of the buffer
     */
    public SentNotificationBuffer(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }

        this.capacity = capacity;
        this.sentNotifications = new ArrayDeque<SendableApnsPushNotification<E>>();
    }

    public synchronized void addSentNotification(final SendableApnsPushNotification<E> notification) {
        this.sentNotifications.addLast(notification);

        while (this.sentNotifications.size() > this.capacity) {
            this.sentNotifications.removeFirst();
        }
    }

    public synchronized void clearNotificationsBeforeSequenceNumber(final int sequenceNumber) {
        while (!this.sentNotifications.isEmpty()
                && sequenceNumber - this.sentNotifications.getFirst().getSequenceNumber() > 0) {
            this.sentNotifications.removeFirst();
        }
    }

    public synchronized E getNotificationWithSequenceNumber(final int sequenceNumber) {
        for (final SendableApnsPushNotification<E> sentNotification : this.sentNotifications) {
            if (sentNotification.getSequenceNumber() == sequenceNumber) {
                return sentNotification.getPushNotification();
            }
        }

        return null;
    }

    public synchronized List<E> getAllNotificationsAfterSequenceNumber(final int sequenceNumber) {
        final ArrayList<E> notifications = new ArrayList<E>(this.sentNotifications.size());

        for (final SendableApnsPushNotification<E> sentNotification : this.sentNotifications) {
            if (sentNotification.getSequenceNumber() - sequenceNumber > 0) {
                notifications.add(sentNotification.getPushNotification());
            }
        }

        notifications.trimToSize();
        return notifications;
    }

    public void clearAllNotifications() {
        this.sentNotifications.clear();
    }

    public boolean isEmpty() {
        return this.sentNotifications.isEmpty();
    }

    public int size() {
        return this.sentNotifications.size();
    }

    public Integer getLowestSequenceNumber() {
        try {
            return this.sentNotifications.getFirst().getSequenceNumber();
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }

    public Integer getHighestSequenceNumber() {
        try {
            return this.sentNotifications.getLast().getSequenceNumber();
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }
}