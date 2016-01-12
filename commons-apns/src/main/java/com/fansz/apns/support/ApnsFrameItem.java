package com.fansz.apns.support;

public enum ApnsFrameItem {
    DEVICE_TOKEN((byte)1, 32), PAYLOAD((byte)2, 2048), SEQUENCE_NUMBER((byte)3, 4), DELIVERY_INVALIDATION_TIME((byte)4,
            4), PRIORITY((byte)5, 1);

    private final byte code;

    /** 最大容量,单位为字节,只有payload的容量可变，其他的元素容量都是固定的 */
    private final Integer size;

    private ApnsFrameItem(final byte code, final Integer size) {
        this.code = code;
        this.size = size;
    }

    public byte getCode() {
        return this.code;
    }

    public Integer getSize() {
        return size;
    }

    public static ApnsFrameItem getFrameItemFromCode(final byte code) {
        for (final ApnsFrameItem item : ApnsFrameItem.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }

        throw new IllegalArgumentException(String.format("No frame item found with code %d", code));
    }

}
