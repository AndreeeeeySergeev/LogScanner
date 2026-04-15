package writer;

import model.LogEvent;

import java.util.List;

public interface LogWriter {
    void write(List<LogEvent> events, String outputPath) throws Exception;
}