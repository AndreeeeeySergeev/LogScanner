package writer;

import model.LogEvent;

import java.io.IOException;

public interface LogWriter {

    void write(LogEvent event) throws IOException;
}