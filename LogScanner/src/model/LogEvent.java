package model;

import java.time.LocalDateTime;
import java.util.Map;

public class LogEvent {

    private String message;        // само сообщение
    private String level;          // уровень (error, warn и т.д.)
    private String source;         // источник (файл, система)
    private LocalDateTime timestamp; // время события

    private Map<String, Object> metadata; // дополнительные данные

    public LogEvent(String message,
                    String level,
                    String source,
                    LocalDateTime timestamp,
                    Map<String, Object> metadata) {

        this.message = message;
        this.level = level;
        this.source = source;
        this.timestamp = timestamp;
        this.metadata = metadata;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
