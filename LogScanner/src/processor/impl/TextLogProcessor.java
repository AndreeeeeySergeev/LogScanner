package processor.impl;

import model.LogEvent;
import normalizer.timestamp.TimestampExtractor;
import normalizer.timestamp.TimestampParseResult;
import processor.LogProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TextLogProcessor implements LogProcessor {

    private final TimestampExtractor extractor = new TimestampExtractor();

    @Override
    public List<LogEvent> process(String filePath, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                // извлекаем уровень
                String level = extractLevel(line, levels);

                // фильтрация
                if (level == null || !isLevelAllowed(level, levels)) {
                    continue;
                }

                // ⏱ timestamp через extractor
                Instant timestamp = extractTimestamp(line);

                // source (пока простой)
                String source = detectSource(line);

                events.add(new LogEvent(
                        timestamp,
                        source,
                        level,
                        line
                ));
            }
        }

        return events;
    }

    // LEVEL
    private String extractLevel(String line, List<String> levels) {

        String lower = line.toLowerCase();

        for (String level : levels) {
            if (lower.contains(level.toLowerCase())) {
                return level.toUpperCase();
            }
        }

        return null;
    }

    // FILTER
    private boolean isLevelAllowed(String level, List<String> levels) {

        return levels.stream()
                .anyMatch(l -> l.equalsIgnoreCase(level));
    }

    // TIMESTAMP через общий extractor
    private Instant extractTimestamp(String line) {

        TimestampParseResult result = extractor.extract(line);

        if (result.isParsed()) {
            return result.getTimestamp();
        }

        return Instant.now(); // fallback
    }

    // SOURCE
    private String detectSource(String line) {

        String lower = line.toLowerCase();

        if (lower.contains("nginx")) return "NGINX";
        if (lower.contains("firewall")) return "FIREWALL";
        if (lower.contains("database") || lower.contains("db")) return "DATABASE";

        return "UNKNOWN";
    }
}