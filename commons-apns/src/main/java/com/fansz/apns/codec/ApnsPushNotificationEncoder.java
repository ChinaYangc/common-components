package com.fansz.apns.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.util.Date;

import com.fansz.apns.model.SendableApnsPushNotification;
import com.fansz.apns.support.ApnsFrameItem;
import com.fansz.apns.support.ApnsPushNotification;
import com.fansz.apns.support.DeliveryPriority;

/**
 * 推送消息编码器，将Java对象转换为二进制流
 * 
 * @author allan
 * @param <T>
 */
public class ApnsPushNotificationEncoder<T extends ApnsPushNotification> extends
        MessageToByteEncoder<SendableApnsPushNotification<T>> {

    private static final byte BINARY_PUSH_NOTIFICATION_COMMAND = 2;

    private static final int INVALIDATE_IMMEDIATELY = 0;

    private static final int FRAME_ITEM_ID_SIZE = 1;

    private static final int FRAME_ITEM_LENGTH_SIZE = 2;

    private final Charset utf8 = Charset.forName("UTF-8");

    @Override
    protected void encode(final ChannelHandlerContext context,
            final SendableApnsPushNotification<T> sendablePushNotification, final ByteBuf out) throws Exception {
        out.writeByte(BINARY_PUSH_NOTIFICATION_COMMAND);// 按照Apple规范，一个字节，固定为2
        out.writeInt(this.getFrameLength(sendablePushNotification));

        out.writeByte(ApnsFrameItem.SEQUENCE_NUMBER.getCode());
        out.writeShort(ApnsFrameItem.SEQUENCE_NUMBER.getSize());
        out.writeInt(sendablePushNotification.getSequenceNumber());

        out.writeByte(ApnsFrameItem.DEVICE_TOKEN.getCode());
        out.writeShort(sendablePushNotification.getPushNotification().getToken().length);
        out.writeBytes(sendablePushNotification.getPushNotification().getToken());

        final byte[] payloadBytes = sendablePushNotification.getPushNotification().getPayload().getBytes(utf8);

        out.writeByte(ApnsFrameItem.PAYLOAD.getCode());
        out.writeShort(payloadBytes.length);
        out.writeBytes(payloadBytes);

        out.writeByte(ApnsFrameItem.DELIVERY_INVALIDATION_TIME.getCode());
        out.writeShort(ApnsFrameItem.DELIVERY_INVALIDATION_TIME.getSize());

        final int deliveryInvalidationTime;

        if (sendablePushNotification.getPushNotification().getDeliveryInvalidationTime() != null) {
            deliveryInvalidationTime = this.getTimestampInSeconds(sendablePushNotification.getPushNotification()
                    .getDeliveryInvalidationTime());
        }
        else {
            deliveryInvalidationTime = INVALIDATE_IMMEDIATELY;
        }

        out.writeInt(deliveryInvalidationTime);

        final DeliveryPriority priority = sendablePushNotification.getPushNotification().getPriority() != null ? sendablePushNotification
                .getPushNotification().getPriority() : DeliveryPriority.IMMEDIATE;

        out.writeByte(ApnsFrameItem.PRIORITY.getCode());
        out.writeShort(ApnsFrameItem.PRIORITY.getSize());
        out.writeByte(priority.getCode());
    }

    private int getTimestampInSeconds(final Date date) {
        return (int)(date.getTime() / 1000);
    }

    /**
     * 获取发送消息的大小
     * 
     * @param sendableApnsPushNotification
     * @return
     */
    private int getFrameLength(final SendableApnsPushNotification<T> sendableApnsPushNotification) {
        return ApnsFrameItem.values().length * (FRAME_ITEM_ID_SIZE + FRAME_ITEM_LENGTH_SIZE)
                + sendableApnsPushNotification.getPushNotification().getToken().length
                + sendableApnsPushNotification.getPushNotification().getPayload().getBytes(utf8).length
                + ApnsFrameItem.SEQUENCE_NUMBER.getSize() + ApnsFrameItem.DELIVERY_INVALIDATION_TIME.getSize()
                + ApnsFrameItem.PRIORITY.getSize();
    }
}
