package writer.impl;

import model.LogEvent;
import writer.LogWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class FileLogWriter implements LogWriter {

    private final BufferedWriter writer;

    public FileLogWriter(String outputPath) throws Exception {
        this.writer = new BufferedWriter(new FileWriter(outputPath, true));
    }

    @Override
    public void write(LogEvent event) throws Exception {

        String formatted = format(event);

        writer.write(formatted);
        writer.newLine();
    }

    private String format(LogEvent event) {

        Instant timestamp = event.getTimestamp();

        if (timestamp == null) {
            timestamp = Instant.now();
        }

        return String.format(
                "[%s] [%s] [%s] %s",
                DateTimeFormatter.ISO_INSTANT.format(timestamp),
                event.getSource(),
                event.getLevel(),
                event.getMessage()
        );
    }

    public void close() throws Exception {
        writer.close();
    }
}