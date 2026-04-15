package normalizer.timestamp;

import java.time.Instant;

public class TimestampParseResult {

    private final Instant timestamp;
    private final boolean parsed;

    public TimestampParseResult(Instant timestamp, boolean parsed) {
        this.timestamp = timestamp;
        this.parsed = parsed;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isParsed() {
        return parsed;
    }
}
