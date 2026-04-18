package model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class LogEvent {

    private final String message;
    private final String level;
    private final String source;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    public LogEvent(Instant timestamp,
                    String source,
                    String level,
                    String message,
                    Map<String, Object> metadata) {

        this.timestamp = timestamp;
        this.source = source;
        this.level = level;
        this.message = message;
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
    }

    // удобный конструктор без metadata
    public LogEvent(Instant timestamp,
                    String source,
                    String level,
                    String message) {

        this(timestamp, source, level, message, null);
    }

    // ещё более простой (для processor'ов)
    public LogEvent(String message) {
        this(null, null, null, message, null);
    }

    public String getMessage() {
        return message;
    }

    public String getLevel() {
        return level;
    }

    public String getSource() {
        return source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}