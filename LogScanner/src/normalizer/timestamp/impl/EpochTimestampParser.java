package normalizer.timestamp.impl;

import normalizer.timestamp.TimestampParser;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpochTimestampParser implements TimestampParser {

    private static final Pattern PATTERN =
            Pattern.compile("\\b\\d{10}\\b");

    @Override
    public Instant parse(String message) {

        Matcher matcher = PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                long epoch = Long.parseLong(matcher.group());
                return Instant.ofEpochSecond(epoch);
            } catch (Exception ignored) {}
        }

        return null;
    }
}