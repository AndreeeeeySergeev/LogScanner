package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;
import normalizer.timestamp.TimestampParseResult;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpochTimestampParser implements TimestampParser {

    // 10 (секунды) или 13 (миллисекунды)
    private static final Pattern PATTERN =
            Pattern.compile("\\b\\d{10,13}\\b");

    @Override
    public TimestampParseResult parse(String message) {

        if (message == null || message.isBlank()) {
            return new TimestampParseResult(null, false);
        }

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                long value = Long.parseLong(matcher.group());

                Instant ts;

                if (String.valueOf(value).length() == 13) {
                    // миллисекунды
                    ts = Instant.ofEpochMilli(value);
                } else {
                    // секунды
                    ts = Instant.ofEpochSecond(value);
                }

                return new TimestampParseResult(ts, true);

            } catch (Exception e) {
                // можно логировать
            }
        }

        return new TimestampParseResult(null, false);
    }
}