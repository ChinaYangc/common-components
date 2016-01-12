package com.fansz.pub.utils;

import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时间工具类
 *
 * @author allan
 */
public final class DateTools {
    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static Date getSysDate() {
        return new Date();
    }

    /**
     * 获取系统当前年月，类似2016-05
     *
     * @return
     */
    public static String getCurYearMonth() {
        return date2String(new Date(), "yyyy-MM");
    }

    /**
     * 获取当前的分钟,类似190305
     *
     * @return
     */
    public static String getMinuteOfDay() {
        return date2String(new Date(), "ddHHmm");
    }

    public static String date2String(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    /**
     * 使用域token和偏离值来改变日期
     *
     * @param date
     * @param fieldToken Y:年,M:月,D:日,h:时,m:分,s：秒,S:毫秒
     * @param wrapValue  +数字:增加相应值;-数字:减少相应值;数字:设置相应值
     * @return
     */
    public static java.util.Date wrapDate(java.util.Date date, String fieldToken, String wrapValue) {
        return DateWrapper.wrapDate(date, fieldToken, wrapValue);
    }

    /**
     * 使用域token和偏离值的组合来改变日期 如：D1M-1或M-1D1表示上个月一号
     *
     * @param date
     * @param wrapString
     * @return
     * @see #wrapDate(java.util.Date, String, String)
     */
    public static java.util.Date wrapDate(java.util.Date date, String wrapString) {
        if (StringTools.isEmpty(wrapString)) {
            return date;
        }

        StringTokenizer tokenizer = new StringTokenizer(wrapString, DateWrapper.getTokens(), true);
        while (tokenizer.hasMoreTokens()) {
            String fieldToken = tokenizer.nextToken();
            String wrapValue = tokenizer.nextToken();
            date = wrapDate(date, fieldToken, wrapValue);
        }
        return date;
    }

    /**
     * 日期域字符描述
     */
    private static class TokenField {
        private final String token;

        private final int field;

        static TokenField TOKEN_YEAR = new TokenField("Y", Calendar.YEAR);

        static TokenField TOKEN_MONTH = new TokenField("M", Calendar.MONTH);

        static TokenField TOKEN_DATE = new TokenField("D", Calendar.DAY_OF_MONTH);

        static TokenField TOKEN_HOUR = new TokenField("h", Calendar.HOUR);

        static TokenField TOKEN_MINUTE = new TokenField("m", Calendar.MINUTE);

        static TokenField TOKEN_SECOND = new TokenField("s", Calendar.SECOND);

        static TokenField TOKEN_MILLISECOND = new TokenField("S", Calendar.MILLISECOND);

        private TokenField(String token, int field) {
            super();
            this.token = token;
            this.field = field;
        }

        public int getField() {
            return field;
        }

        public String getToken() {
            return token;
        }
    }

    private static class DateWrapper {
        private static Map<String, TokenField> fieldTokens = new HashMap<String, TokenField>();

        private static String tokens = "";

        private static void addToken(TokenField tokenField) {
            fieldTokens.put(tokenField.getToken(), tokenField);
            if (tokens.indexOf(tokenField.getToken()) < 0) {
                tokens = tokens + tokenField.getToken();
            }
        }

        static {
            addToken(TokenField.TOKEN_YEAR);
            addToken(TokenField.TOKEN_MONTH);
            addToken(TokenField.TOKEN_DATE);
            addToken(TokenField.TOKEN_HOUR);
            addToken(TokenField.TOKEN_MINUTE);
            addToken(TokenField.TOKEN_SECOND);
            addToken(TokenField.TOKEN_MILLISECOND);

        }

        public static String getTokens() {
            return tokens;
        }

        static java.util.Date wrapDate(java.util.Date date, String fieldToken, String wrapValue) {
            TokenField tokenField = fieldTokens.get(fieldToken);
            if (tokenField == null) {
                throw new IllegalArgumentException("Token [" + fieldToken + "] unsupported!");
            }
            boolean add = wrapValue.startsWith("+") || wrapValue.startsWith("-");
            if (wrapValue.startsWith("+")) {
                wrapValue = wrapValue.substring(1);
            }
            if (!NumberUtils.isNumber(wrapValue)) {
                throw new IllegalArgumentException("[" + wrapValue + "] is not a number!");
            }

            if (date == null) {
                date = new java.util.Date();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if (add) {
                calendar.add(tokenField.field, Integer.parseInt(wrapValue));
            } else {
                calendar.set(tokenField.field, Integer.parseInt(wrapValue));
            }
            return calendar.getTime();
        }
    }
}
