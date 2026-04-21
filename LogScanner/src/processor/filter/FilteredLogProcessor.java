package processor.filter;

import model.LogEvent;
import processor.LogProcessor;

import java.util.function.Consumer;

public class FilteredLogProcessor implements LogProcessor {

    private final LogProcessor delegate;
    private final LevelMatcher matcher;

    public FilteredLogProcessor(LogProcessor delegate, LevelMatcher matcher) {
        this.delegate = delegate;
        this.matcher = matcher;
    }

    @Override
    public void process(String filePath,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        delegate.process(filePath, encoding, event -> {

            if (matcher.matches(event.getMessage())) {
                consumer.accept(event);
            }

        });
    }
}