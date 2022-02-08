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

    public static final String TODAY = "сегодня";
    public static final String YESTERDAY = "вчера";

    @Override
    public LocalDateTime parse(String parse) {
        LocalDateTime dateTime = null;

        String[] dateAndTime = parse.split(",");
        String[] dateParts = dateAndTime[0].split(" ");

        LocalDate date = getDate(dateParts);
        LocalTime time = getTime(dateAndTime[1]);

        return LocalDateTime.of(date, time);
    }

    private LocalDate getDate(String[] dateParts) {
        LocalDate date = null;

        if (dateParts.length == 3) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yy", Locale.US);
            dateParts[1] = MONTHS.get(dateParts[1]);
            date = LocalDate.parse(String.join("-", dateParts).trim(), formatter);
        } else if (dateParts.length == 1) {
            if (dateParts[0].equals(TODAY)) {
                date = LocalDate.now();
            } else if (dateParts[0].equals(YESTERDAY)) {
                date = LocalDate.now().minus(Period.ofDays(1));
            }
        }

        return date;
    }

    private LocalTime getTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(time.trim(), formatter);
    }
}