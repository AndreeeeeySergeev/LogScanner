package processor.impl;

import config.GraphSource;
import model.LogEvent;
import processor.LogProcessor;

import java.util.List;
import java.util.function.Consumer;

public class CompositeGraphProcessor implements LogProcessor {

    private final List<GraphSource> sources;

    public CompositeGraphProcessor(List<GraphSource> sources) {
        this.sources = sources;
    }

    @Override
    public void process(String ignored, String encoding, Consumer<LogEvent> consumer) {

        for (GraphSource source : sources) {

            System.out.println("Graph DB: " + source.getName());

            try {
                LogProcessor processor = new GraphLogProcessor(
                        source.getUser(),
                        source.getPassword()
                );

                processor.process(source.getUri(), null, consumer);

            } catch (Exception e) {
                System.err.println("Ошибка Graph DB: " + source.getName());
                e.printStackTrace();
            }
        }
    }
}