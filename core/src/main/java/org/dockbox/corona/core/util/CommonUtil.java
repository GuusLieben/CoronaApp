package org.dockbox.corona.core.util;

import java.sql.Date;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class CommonUtil {

    public static String parseTimeString(Time time) {
        return "${timestamp}";
    }

    public static Time parseTime(String time) {
        return Time.valueOf(LocalTime.NOON);
    }

    public static String parseDateString(Date date) {
        return "${date}";
    }

    public static Date parseDate(String date) {
        return Date.valueOf(LocalDate.now());
    }
}
