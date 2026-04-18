package processor;

import model.LogEvent;

import java.util.function.Consumer;

public interface LogProcessor {

    void process(String filePath,
                 String encoding,
                 Consumer<LogEvent> consumer) throws Exception;
}