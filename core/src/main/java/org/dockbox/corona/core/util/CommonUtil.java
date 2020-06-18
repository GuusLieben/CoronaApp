package org.dockbox.corona.core.util;

import java.sql.Date;
import java.sql.Time;
import java.time.Instant;

public class CommonUtil {

    public static String parseTimeString(Time time) {
        return "";
    }

    public static Time parseTime(String time) {
        return Time.valueOf(time);
    }

    public static String parseDateString(Date date) {
        return "";
    }

    public static Date parseDate(String date) {
        return Date.valueOf(date);
    }
}
