package writer.impl;

import model.LogEvent;
import writer.LogWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class FileLogWriter implements LogWriter {

    private final BufferedWriter writer;

    public FileLogWriter(String outputPath) throws IOException {

        File file = new File(outputPath);

        // проверка существования директории (без создания)
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            throw new IOException("Директория не существует: " + parent.getAbsolutePath());
        }

        this.writer = new BufferedWriter(new FileWriter(file, true)); // append = true
    }

    @Override
    public void write(LogEvent event) throws IOException {

        if (event == null) return;

        String formatted = format(event);

        writer.write(formatted);
        writer.newLine();
        writer.flush(); // важно для streaming
    }

    private String format(LogEvent event) {

        Instant timestamp = event.getTimestamp();
        if (timestamp == null) {
            timestamp = Instant.now();
        }

        String source = safe(event.getSource());
        String level = safe(event.getLevel());
        String message = safe(event.getMessage());

        return String.format(
                "[%s] [%s] [%s] %s",
                DateTimeFormatter.ISO_INSTANT.format(timestamp),
                source,
                level,
                message
        );
    }

    private String safe(String value) {
        return value != null ? value : "UNKNOWN";
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии writer");
            e.printStackTrace();
        }
    }
}