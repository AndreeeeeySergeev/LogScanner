package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;
import normalizer.timestamp.TimestampParseResult;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsoTimestampParser implements TimestampParser {

    private static final Pattern PATTERN =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");

    @Override
    public TimestampParseResult parse(String message) {

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                Instant ts = Instant.parse(matcher.group());
                return new TimestampParseResult(ts, true);
            } catch (Exception ignored) {}
        }

        return new TimestampParseResult(null, false);
    }
}