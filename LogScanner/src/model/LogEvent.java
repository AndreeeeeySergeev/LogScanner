package model;

import java.time.Instant;

import java.time.LocalDateTime;
import java.util.Map;

public class LogEvent {

    private String message;        // само сообщение
    private String level;          // уровень (error, warn и т.д.)
    private String source;         // источник (файл, система)
    private Instant timestamp; // время события

    private Map<String, Object> metadata; // дополнительные данные

    public LogEvent(String message,
                    String level,
                    String source,
                    Instant timestamp,
                    Map<String, Object> metadata) {

        this.timestamp = timestamp;
        this.source = source;
        this.level = level;
        this.message = message;
        this.metadata = metadata;
    }

    public LogEvent(Instant timestamp, String source, String level, String message) {
        this(message, level, source, timestamp, null);
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
