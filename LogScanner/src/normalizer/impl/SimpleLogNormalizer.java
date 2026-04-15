package normalizer.impl;

import model.LogEvent;
import normalizer.LogNormalizer;
import normalizer.timestamp.TimestampExtractor;
import normalizer.timestamp.TimestampParseResult;

import java.time.Instant;
import java.util.List;

public class SimpleLogNormalizer implements LogNormalizer {

    private final List<String> levels;
    private final TimestampExtractor timestampExtractor = new TimestampExtractor();

    public SimpleLogNormalizer(List<String> levels) {
        this.levels = levels;
    }

    @Override
    public LogEvent normalize(LogEvent event) {

        String message = event.getMessage();

        // 1. Нормализация уровня
        String normalizedLevel = extractLevel(message);

        // 2. Парсинг timestamp
        TimestampParseResult tsResult = timestampExtractor.extract(message);
        Instant timestamp = tsResult.getTimestamp();

        // 🔥 (опционально, но очень полезно)
        if (!tsResult.isParsed()) {
            System.out.println("⚠ Timestamp не найден, используем fallback: " + message);
        }

        // 3. Определение источника
        String source = detectSource(message);

        // 4. Создание нормализованного события
        return new LogEvent(
                timestamp,
                source,
                normalizedLevel,
                message
        );
    }

    private String extractLevel(String message) {

        String lower = message.toLowerCase();

        for (String level : levels) {
            if (lower.contains(level.toLowerCase())) {
                return level.toUpperCase();
            }
        }

        return "UNKNOWN";
    }

    private String detectSource(String message) {

        String lower = message.toLowerCase();

        if (lower.contains("nginx")) return "NGINX";
        if (lower.contains("firewall")) return "FIREWALL";
        if (lower.contains("postgres")) return "POSTGRES";
        if (lower.contains("mysql")) return "MYSQL";

        return "UNKNOWN";
    }
}