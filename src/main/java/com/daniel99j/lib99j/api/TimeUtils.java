package com.daniel99j.lib99j.api;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings({"unused"})
public class TimeUtils {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final LocalDateTime startOfTimeLocal = LocalDateTime.of(0, Month.JANUARY, 1, 0, 0);
    public static final Date startOfTime = new Date(0, Calendar.JANUARY, 0);
}
