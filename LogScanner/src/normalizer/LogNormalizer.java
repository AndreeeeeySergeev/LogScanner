package normalizer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;
import model.LogEvent;

public interface LogNormalizer {
    LogEvent normalize(LogEvent event);

    private final TimestampExtractor extractor = new TimestampExtractor();

    private Instant extractTimestamp(String message) {
        return extractor.extract(message);
    }
}