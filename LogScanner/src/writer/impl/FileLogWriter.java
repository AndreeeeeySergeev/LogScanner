package writer.impl;

import model.LogEvent;
import writer.LogWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileLogWriter implements LogWriter {

    @Override
    public void write(List<LogEvent> events, String outputPath) throws Exception {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            for (LogEvent event : events) {

                String line = String.format(
                        "[%s] [%s] [%s] %s",
                        event.getTimestamp(),
                        event.getSource(),
                        event.getLevel(),
                        event.getMessage()
                );

                writer.write(line);
                writer.newLine();
            }
        }
    }
}