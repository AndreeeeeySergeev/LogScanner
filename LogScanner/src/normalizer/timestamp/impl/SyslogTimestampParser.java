package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;
import normalizer.timestamp.TimestampParseResult;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogTimestampParser implements TimestampParser {

    private static final Pattern PATTERN =
            Pattern.compile("[A-Z][a-z]{2}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2}");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM d HH:mm:ss", Locale.ENGLISH);

    @Override
    public TimestampParseResult parse(String message) {

        if (message == null || message.isBlank()) {
            return new TimestampParseResult(null, false);
        }

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                String raw = matcher.group();

                LocalDateTime ldt = LocalDateTime.parse(raw, FORMATTER);

                int currentYear = Year.now().getValue();
                ldt = ldt.withYear(currentYear);

                // фикс будущих дат
                LocalDateTime now = LocalDateTime.now();
                if (ldt.isAfter(now)) {
                    ldt = ldt.minusYears(1);
                }

                Instant ts = ldt
                        .atZone(ZoneId.systemDefault())
                        .toInstant();

                return new TimestampParseResult(ts, true);

            } catch (Exception e) {
                // можно логировать при необходимости
            }
        }

        return new TimestampParseResult(null, false);
    }
}