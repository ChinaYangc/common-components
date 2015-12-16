package com.fansz.pub.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 * 
 * @author Administrator
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
}
