package writer.impl;

import model.LogEvent;
import writer.LogWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class FileLogWriter implements LogWriter {

    private final BufferedWriter writer;

    public FileLogWriter(String outputPath) throws IOException {

        File file = new File(outputPath);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            throw new IOException("Директория не существует: " + parent.getAbsolutePath());
        }

        this.writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file, true),
                        StandardCharsets.UTF_8
                )
        );
    }

    @Override
    public void write(LogEvent event) throws IOException {

        if (event == null) return;

        writer.write(format(event));
        writer.newLine();

        writer.flush();
    }

    private String format(LogEvent event) {

        Instant timestamp = event.getTimestamp();
        if (timestamp == null) {
            timestamp = Instant.now();
        }

        return String.format(
                "[%s] [%s] [%s] %s",
                DateTimeFormatter.ISO_INSTANT.format(timestamp),
                safe(event.getSource()),
                safe(event.getLevel()),
                safe(event.getMessage())
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