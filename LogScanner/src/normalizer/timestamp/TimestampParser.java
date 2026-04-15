package normalizer.timestamp;

import java.time.Instant;

public interface TimestampParser {
    Instant parse(String message);
}
