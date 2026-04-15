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
            Pattern.compile("[A-Z][a-z]{2} \\d{1,2} \\d{2}:\\d{2}:\\d{2}");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM d HH:mm:ss", Locale.ENGLISH);

    @Override
    public TimestampParseResult parse(String message) {

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                String raw = matcher.group();

                LocalDateTime ldt = LocalDateTime.parse(raw, FORMATTER);

                // добавляем текущий год (в syslog его нет)
                ldt = ldt.withYear(Year.now().getValue());

                Instant ts = ldt
                        .atZone(ZoneId.systemDefault())
                        .toInstant();

                return new TimestampParseResult(ts, true);

            } catch (Exception ignored) {}
        }

        return new TimestampParseResult(null, false);
    }
}