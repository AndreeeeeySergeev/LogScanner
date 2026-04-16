package normalizer.impl;

import model.LogEvent;
import normalizer.LogNormalizer;
import normalizer.timestamp.TimestampExtractor;
import normalizer.timestamp.TimestampParseResult;

import java.time.Instant;
import java.util.List;

public class SimpleLogNormalizer implements LogNormalizer {

    private final List<String> levels;
    private final TimestampExtractor extractor = new TimestampExtractor();

    public SimpleLogNormalizer(List<String> levels) {
        this.levels = levels;
    }

    @Override
    public LogEvent normalize(LogEvent event) {

        String message = event.getMessage();


        if (message == null) {
            message = "";
        }

        // 1. Уровень
        String normalizedLevel = extractLevel(message);

        // 2. Timestamp
        Instant timestamp = extractTimestamp(message);

        // 3. Источник
        String source = detectSource(message);

        return new LogEvent(
                timestamp,
                source,
                normalizedLevel,
                message
        );
    }

    private String extractLevel(String message) {


        if (message.isEmpty()) {
            return "UNKNOWN";
        }

        String lower = message.toLowerCase();

        for (String level : levels) {
            if (lower.contains(level.toLowerCase())) {
                return level.toUpperCase();
            }
        }

        return "UNKNOWN";
    }

    private Instant extractTimestamp(String message) {

        TimestampParseResult result = extractor.extract(message);

        if (result.getTimestamp() == null) {
            return Instant.now(); // fallback
        }

        return result.getTimestamp();
    }

    private String detectSource(String message) {

        if (message.isEmpty()) {
            return "UNKNOWN";
        }

        String lower = message.toLowerCase();

        if (lower.contains("nginx")) return "NGINX";
        if (lower.contains("firewall")) return "FIREWALL";
        if (lower.contains("db") || lower.contains("database")) return "DATABASE";

        return "UNKNOWN";
    }
}