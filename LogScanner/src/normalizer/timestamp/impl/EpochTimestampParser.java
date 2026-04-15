package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;
import normalizer.timestamp.TimestampParseResult;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpochTimestampParser implements TimestampParser {

    private static final Pattern PATTERN =
            Pattern.compile("\\b\\d{10}\\b"); // 10 цифр (секунды)

    @Override
    public TimestampParseResult parse(String message) {

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                long epoch = Long.parseLong(matcher.group());
                Instant ts = Instant.ofEpochSecond(epoch);

                return new TimestampParseResult(ts, true);
            } catch (Exception ignored) {}
        }

        return new TimestampParseResult(null, false);
    }
}