package processor.impl;

import config.MongoSource;
import model.LogEvent;
import processor.LogProcessor;

import java.util.List;
import java.util.function.Consumer;

public class CompositeNoSqlProcessor implements LogProcessor {

    private final List<MongoSource> sources;

    public CompositeNoSqlProcessor(List<MongoSource> sources) {
        this.sources = sources;
    }

    @Override
    public void process(String ignored, String encoding, Consumer<LogEvent> consumer) {

        for (MongoSource source : sources) {

            System.out.println("Mongo: " + source.getName());

            try {
                LogProcessor processor = new NoSqlDBProcessor();

                processor.process(source.getUri(), null, consumer);

            } catch (Exception e) {
                System.err.println("Ошибка Mongo: " + source.getName());
                e.printStackTrace();
            }
        }
    }
}