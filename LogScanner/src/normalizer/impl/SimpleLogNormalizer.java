package normalizer.impl;

import model.LogEvent;
import normalizer.LogNormalizer;

import java.time.Instant;
import java.util.List;

public class SimpleLogNormalizer implements LogNormalizer {

    private final List<String> levels;

    public SimpleLogNormalizer(List<String> levels) {
        this.levels = levels;
    }

    @Override
    public LogEvent normalize(LogEvent event) {

        String message = event.getMessage();

        // 1. Нормализация уровня
        String normalizedLevel = extractLevel(message);

        // 2. Нормализация timestamp (пока fallback)
        Instant timestamp = extractTimestamp(message);

        // 3. Источник (пока простой)
        String source = detectSource(message);

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

    private Instant extractTimestamp(String message) {

        // пока просто заглушка
        // потом добавим regex под реальные форматы

        return Instant.now();
    }

    private String detectSource(String message) {

        // пока просто
        if (message.contains("nginx")) return "NGINX";
        if (message.contains("firewall")) return "FIREWALL";

        return "UNKNOWN";
    }
}