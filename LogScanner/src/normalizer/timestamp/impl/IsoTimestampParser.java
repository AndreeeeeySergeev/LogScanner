package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;
import normalizer.timestamp.TimestampParseResult;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsoTimestampParser implements TimestampParser {

    // ISO с:
    // ✔ миллисекундами (опционально)
    // ✔ Z или timezone offset
    private static final Pattern PATTERN = Pattern.compile(
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})"
    );

    @Override
    public TimestampParseResult parse(String message) {

        if (message == null || message.isBlank()) {
            return new TimestampParseResult(null, false);
        }

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                Instant ts = Instant.parse(matcher.group());
                return new TimestampParseResult(ts, true);
            } catch (Exception e) {
                System.err.println("Проблема с парсером ISO " + e.getMessage());
            }
        }

        return new TimestampParseResult(null, false);
    }
}