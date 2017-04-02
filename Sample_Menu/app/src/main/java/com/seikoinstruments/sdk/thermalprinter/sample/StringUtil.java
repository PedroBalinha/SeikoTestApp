package com.seikoinstruments.sdk.thermalprinter.sample;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {

    public static boolean isEmpty(CharSequence chsq) {
        boolean result;
        if(chsq == null) {
            result = true;

        } else if(chsq.length() == 0) {
            result = true;

        } else {
            result = false;
        }

        return result;
    }

    public static boolean isEmpty(String str) {
        boolean result;
        if(str == null) {
            result = true;

        } else if(str.length() == 0) {
            result = true;

        } else {
            result = false;
        }

        return result;
    }

    public static int getInt(String str) {
        return getInt(str, 0);
    }


    /**
     * String -> int
     *
     * @param str        string
     * @param defaultVal default value
     *
     * @return int value
     */
    public static int getInt(String str, int defaultVal) {
        int value;
        try {
            value = Integer.parseInt(str);
        } catch(NumberFormatException e) {
            value = defaultVal;
        }

        return value;
    }


    /**
     * Get date string
     *
     * @param format date format
     *
     * @return Date string
     */
    public static String getDateString(String format) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        return sdf.format(now);
    }

}
