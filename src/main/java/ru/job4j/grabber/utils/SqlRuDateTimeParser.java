package ru.job4j.grabber.utils;

import com.mchange.v2.collection.MapEntry;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class SqlRuDateTimeParser implements DateTimeParser {

    private static final Map<String, String> MONTHS = Map.ofEntries(
            Map.entry("янв", "Jan"),
            Map.entry("фев", "Feb"),
            Map.entry("мар", "Mar"),
            Map.entry("апр", "Apr"),
            Map.entry("май", "May"),
            Map.entry("июн", "Jun"),
            Map.entry("июл", "Jul"),
            Map.entry("авг", "Aug"),
            Map.entry("сен", "Sep"),
            Map.entry("окт", "Oct"),
            Map.entry("ноя", "Nov"),
            Map.entry("дек", "Dec")
    );

    private static final String TODAY = "сегодня";
    private static final String YESTERDAY = "вчера";
    private static final String DATE_PATTERN = "d-MMM-yy";
    private static final String TIME_PATTERN = "HH:mm";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.US);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(TIME_PATTERN);

    @Override
    public LocalDateTime parse(String parse) {

        String[] dateAndTime = parse.split(",");
        String[] dateParts = dateAndTime[0].split(" ");

        LocalDate date = getDate(dateParts);
        LocalTime time = getTime(dateAndTime[1]);

        return LocalDateTime.of(date, time);
    }

    private LocalDate getDate(String[] dateParts) {
        LocalDate date = null;

        if (dateParts.length == 3) {
            dateParts[1] = MONTHS.get(dateParts[1]);
            date = LocalDate.parse(String.join("-", dateParts).trim(), DATE_FORMATTER);
        } else if (dateParts.length == 1) {
            if (TODAY.equals(dateParts[0])) {
                date = LocalDate.now();
            } else if (YESTERDAY.equals(dateParts[0])) {
                date = LocalDate.now().minus(Period.ofDays(1));
            }
        }

        return date;
    }

    private LocalTime getTime(String time) {
        return LocalTime.parse(time.trim(), TIME_FORMATTER);
    }
}