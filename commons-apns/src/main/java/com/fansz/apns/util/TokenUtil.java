package com.fansz.apns.util;

/**
 * Device Token工具类，用于二进制和16进制字符串的转换
 */
public final class TokenUtil {
    private static final String NON_HEX_CHARACTER_PATTERN = "[^0-9a-fA-F]";

    private TokenUtil() {
    }

    /**
     * 将16进制字符串转换为2进制，所有非16进制字符(例如0-9 and A-F)都将被忽略
     *
     * @throws MalformedTokenStringException 如果无法被转换成二进制数组
     * @throws NullPointerException 如果传入参数为null
     */
    public static byte[] tokenStringToByteArray(final String tokenString) throws MalformedTokenStringException {

        if (tokenString == null) {
            throw new NullPointerException("Token string must not be null.");
        }

        final String strippedTokenString = tokenString.replaceAll(NON_HEX_CHARACTER_PATTERN, "");

        if (strippedTokenString.length() % 2 != 0) {
            throw new MalformedTokenStringException("Token strings must contain an even number of hexadecimal digits.");
        }

        final byte[] tokenBytes = new byte[strippedTokenString.length() / 2];

        for (int i = 0; i < strippedTokenString.length(); i += 2) {
            tokenBytes[i / 2] = (byte)(Integer.parseInt(strippedTokenString.substring(i, i + 2), 16));
        }

        return tokenBytes;
    }

    /**
     * 将二进制转换为16进制字符串.
     *
     * @throws NullPointerException 如果传入参数为null
     */
    public static String tokenBytesToString(final byte[] tokenBytes) {
        if (tokenBytes == null) {
            throw new NullPointerException("Token byte array must not be null.");
        }

        final StringBuilder builder = new StringBuilder();

        for (final byte b : tokenBytes) {
            final String hexString = Integer.toHexString(b & 0xff);

            if (hexString.length() == 1) {
                // We need a leading zero
                builder.append("0");
            }

            builder.append(hexString);
        }

        return builder.toString();
    }
}
