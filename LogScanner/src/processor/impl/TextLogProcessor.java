package processor.impl;

import model.LogEvent;
import processor.LogProcessor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class TextLogProcessor implements LogProcessor {

    @Override
    public void process(String filePath,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), encoding))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                consumer.accept(new LogEvent(
                        null,
                        null,
                        null,
                        line
                ));
            }
        }
    }
}