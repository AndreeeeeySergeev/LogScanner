package normalizer.timestamp;

import normalizer.timestamp.impl.EpochTimestampParser;
import normalizer.timestamp.impl.IsoTimestampParser;
import normalizer.timestamp.impl.SyslogTimestampParser;

import java.time.Instant;
import java.util.List;

public class TimestampExtractor {

    private final List<TimestampParser> parsers = List.of(
            new IsoTimestampParser(),
            new EpochTimestampParser(),
            new SyslogTimestampParser()
    );

    public Instant extract(String message) {

        for (TimestampParser parser : parsers) {
            Instant result = parser.parse(message);

            if (result != null) {
                return result;
            }
        }

        return Instant.now(); // fallback
    }
}
