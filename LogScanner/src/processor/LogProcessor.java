package processor;

import model.LogEvent;
import java.util.List;

public interface LogProcessor {

    List<LogEvent> process(String source,
                           List<String> levels) throws Exception;
}