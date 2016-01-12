package com.fansz.apns.codec;

import com.fansz.apns.model.RejectedNotification;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fansz.apns.support.RejectedNotificationReason;

public class RejectedNotificationDecoder extends ByteToMessageDecoder {

    private static final int EXPECTED_BYTES = 6;

    private static final byte EXPECTED_COMMAND = 8;
    
    private static final Logger log = LoggerFactory.getLogger(RejectedNotificationDecoder.class);

    /**
     * 根据app的推送文档, APNS错误由command（1）+status(1)+notification ID(4)组成，共6字节
     */
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        if (in.readableBytes() >= EXPECTED_BYTES) {
            final byte command = in.readByte();
            final byte code = in.readByte();

            final int notificationId = in.readInt();

            if (command != EXPECTED_COMMAND) {
                log.error("Unexpected command: {}", command);
            }

            final RejectedNotificationReason errorCode = RejectedNotificationReason.getByErrorCode(code);

            out.add(new RejectedNotification(notificationId, errorCode));
        }
    }
}
