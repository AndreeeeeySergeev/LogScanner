package normalizer.impl;

import model.LogEvent;
import normalizer.LevelExtractor;
import normalizer.LogNormalizer;
import normalizer.SourceDetector;
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

        if (message == null || message.isEmpty()) {
            return null;
        }

        // LEVEL
        String level = LevelExtractor.extract(message, levels);

        // ФИЛЬТРАЦИЯ
        if (level == null) {
            return null;
        }

        // TIMESTAMP
        Instant timestamp = extractTimestamp(message);

        // SOURCE
        String source = SourceDetector.detect(message);

        return new LogEvent(timestamp, source, level, message);
    }

    private Instant extractTimestamp(String message) {

        TimestampParseResult result = extractor.extract(message);

        if (result.isParsed() && result.getTimestamp() != null) {
            return result.getTimestamp();
        }

        return Instant.now(); // fallback
    }
}