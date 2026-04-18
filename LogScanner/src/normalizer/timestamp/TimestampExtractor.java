package normalizer.timestamp;

import normalizer.timestamp.impl.*;

import java.time.Instant;
import java.util.List;

public class TimestampExtractor {

    private final List<TimestampParser> parsers = List.of(
            new IsoTimestampParser(),
            new EpochTimestampParser(),
            new SyslogTimestampParser()
    );

    public TimestampParseResult extract(String message) {

        if (message == null || message.isBlank()) {
            return new TimestampParseResult(null, false);
        }

        for (TimestampParser parser : parsers) {

            TimestampParseResult result = parser.parse(message);

            if (result.isParsed()) {
                return result;
            }
        }

        //fallback
        return new TimestampParseResult(null, false);
    }
}