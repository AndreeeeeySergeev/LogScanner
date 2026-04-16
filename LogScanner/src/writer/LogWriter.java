package writer;

import model.LogEvent;

public interface LogWriter {

    void write(LogEvent event) throws Exception;
}