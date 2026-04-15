package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogTimestampParser implements TimestampParser {

    private static final Pattern PATTERN =
            Pattern.compile("[A-Za-z]{3} \\d{1,2} \\d{2}:\\d{2}:\\d{2}");

    @Override
    public Instant parse(String message) {

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                String ts = matcher.group();

                LocalDateTime ldt = LocalDateTime.parse(
                        ts,
                        DateTimeFormatter.ofPattern("MMM d HH:mm:ss")
                ).withYear(Year.now().getValue());

                return ldt.atZone(ZoneId.systemDefault()).toInstant();

            } catch (Exception ignored) {}
        }

        return null;
    }
}