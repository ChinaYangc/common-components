package com.fansz.common.provider.utils;

import com.fansz.pub.utils.UUIDTools;

/**
 * Created by allan on 16/1/7.
 */
public final class RedisKeyUtils {
    public final static String FRIEND_PREFIX = "freind:{";//我的好友

    public final static String SP_FRIEND_PREFIX = "sfriend:{";//我的特别好友

    public final static String MY_REQUEST = "request:{";//我发出的好友请求

    public final static String MY_REQUESTED = "requested:{";//我接收到的好友请求

    public final static String HASH_TAG_SUFFIX = "}";//HASH TAG后缀

    public final static String TMP_PREFIX="tmp:";//临时key

    public final static String FRIEND_REMARK_PREFIX="friend_remark:{";

    /**
     * 我的好友
     *
     * @param sn
     * @return
     */
    public static String getFriendKey(String sn) {
        return FRIEND_PREFIX + sn + HASH_TAG_SUFFIX;
    }

    /**
     * 我发出的好友请求
     *
     * @param sn
     * @return
     */
    public static String getMyRequestKey(String sn) {
        return MY_REQUEST + sn + HASH_TAG_SUFFIX;
    }

    /**
     * 我接收到的好友请求
     *
     * @param sn
     * @return
     */
    public static String getMyRequestedKey(String sn) {
        return MY_REQUESTED + sn + HASH_TAG_SUFFIX;
    }

    /**
     * 我的特殊好友
     *
     * @param sn
     * @return
     */
    public static String getSpeicalFriendKey(String sn) {
        return SP_FRIEND_PREFIX + sn + HASH_TAG_SUFFIX;
    }

    /**
     * 临时key
     * @param key
     * @return
     */
    public static String getUnionKey(String key) {
        return TMP_PREFIX + UUIDTools.generate() + ":{" + key + HASH_TAG_SUFFIX;
    }

    public static String getFriendRemarkKey(String sn) {
      return  FRIEND_REMARK_PREFIX + sn + "}";
    }
}

