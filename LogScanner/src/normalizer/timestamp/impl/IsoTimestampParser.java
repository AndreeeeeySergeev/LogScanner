package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsoTimestampParser implements TimestampParser {

    private static final Pattern PATTERN =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}");

    @Override
    public Instant parse(String message) {

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                String ts = matcher.group().replace("T", " ");

                LocalDateTime ldt = LocalDateTime.parse(
                        ts,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                return ldt.atZone(ZoneId.systemDefault()).toInstant();

            } catch (Exception ignored) {}
        }

        return null;
    }
}