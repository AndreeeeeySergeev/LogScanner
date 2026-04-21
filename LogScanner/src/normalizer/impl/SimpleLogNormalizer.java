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
    private final LevelExtractor levelExtractor;
    private final SourceDetector sourceDetector;

    private final TimestampExtractor extractor = new TimestampExtractor();

    public SimpleLogNormalizer(List<String> levels) {
        this.levelExtractor  = new LevelExtractor(levels);
        this.sourceDetector = new SourceDetector();
    }


    @Override
    public LogEvent normalize(LogEvent event) {

        String message = event.getMessage();

        if (message == null || message.isBlank()) {
            return null;
        }

        // LEVEL (не перетираем, если уже есть)
        String level = event.getLevel();
        if (level == null) {
            level = levelExtractor.extract(message);
        }

        // ФИЛЬТРАЦИЯ
        if (level == null) {
            return null;
        }

        // TIMESTAMP (не перетираем)
        Instant timestamp = event.getTimestamp();
        if (timestamp == null) {
            timestamp = extractTimestamp(message);
        }

        // SOURCE (не перетираем)
        String source = event.getSource();
        if (source == null) {
            source = sourceDetector.detect(message);
        }

        return new LogEvent(timestamp, source, level, message);
    }

    private Instant extractTimestamp(String message) {

        TimestampParseResult result = extractor.extract(message);

        if (result.isParsed() && result.getTimestamp() != null) {
            return result.getTimestamp();
        }

        return Instant.now();
    }
}