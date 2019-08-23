package com.fifthera.ecmall.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DateUtil {

    public static final String FORMAT_MDHM = "MM-dd HH:mm";
    public static final String FORMAT_YMD = "yyyy-MM-dd";
    public static final String FORMAT_YMDHM = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_GMT = "EEE, dd-MMM-yyyy HH:mm:ss 'GMT'";

    private static final String TAG = DateUtil.class.getSimpleName();
    private static final Locale DEFAULT_LOCALE = Locale.CHINA;

    private static ThreadLocal<Map<String, SimpleDateFormat>> threadLocal = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        protected synchronized Map<String, SimpleDateFormat> initialValue() {
            Map<String, SimpleDateFormat> map = new HashMap<String, SimpleDateFormat>();
            map.put(FORMAT_MDHM, new SimpleDateFormat(FORMAT_MDHM, DEFAULT_LOCALE));
            map.put(FORMAT_YMD, new SimpleDateFormat(FORMAT_YMD, DEFAULT_LOCALE));
            map.put(FORMAT_YMDHM, new SimpleDateFormat(FORMAT_YMDHM, DEFAULT_LOCALE));
            map.put(FORMAT_YMDHMS, new SimpleDateFormat(FORMAT_YMDHMS, DEFAULT_LOCALE));
            map.put(FORMAT_GMT, new SimpleDateFormat(FORMAT_GMT, DEFAULT_LOCALE));
            return map;
        }
    };

    private DateUtil() {
    }

    public static SimpleDateFormat getDateFormat(String format) {
        Map<String, SimpleDateFormat> map = (Map<String, SimpleDateFormat>) threadLocal.get();
        SimpleDateFormat sdf = map.get(format);
        if (sdf != null) {
            return sdf;
        }
        try {
            sdf = new SimpleDateFormat(format, DEFAULT_LOCALE);
            map.put(format, sdf);
        } catch (Exception e) {

        }
        return sdf;
    }

    public static Date parse(String textDate, String format) {
        if (textDate == null || textDate.length() <= 0) {
            return null;
        }
        try {
            SimpleDateFormat sdf = getDateFormat(format);
            if (sdf == null) {
                return null;
            }
            return sdf.parse(textDate);
        } catch (Exception e) {

            return null;
        }

    }

    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = getDateFormat(format);
        if (sdf == null) {
            return null;
        }
        return sdf.format(date);
    }

    public static Date east8ToGmt(Date src) {
        if (src == null) {
            return null;
        }
        TimeZone srcTimeZone = TimeZone.getTimeZone("GMT+8");
        TimeZone destTimeZone = TimeZone.getTimeZone("GMT");
        long targetTime = src.getTime() - srcTimeZone.getRawOffset() + destTimeZone.getRawOffset();
        return new Date(targetTime);
    }
}