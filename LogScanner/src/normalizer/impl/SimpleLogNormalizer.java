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
    private final TimestampExtractor timestampExtractor;

    public SimpleLogNormalizer(List<String> levels) {
        this.levelExtractor = new LevelExtractor(levels);
        this.sourceDetector = new SourceDetector();
        this.timestampExtractor = new TimestampExtractor();
    }

    @Override
    public LogEvent normalize(LogEvent event) {

        if (event == null) return null;

        String message = event.getMessage();

        if (message == null || message.isBlank()) {
            return null;
        }

        //  LEVEL
        String level = event.getLevel();
        if (level == null) {
            level = levelExtractor.extract(message);
        }

        // фильтрация (ключевой этап)
        if (level == null) {
            return null;
        }

        //  TIMESTAMP
        Instant timestamp = event.getTimestamp();

        if (timestamp == null) {
            timestamp = extractTimestamp(message);
        }

        // финальный fallback (важно!)
        if (timestamp == null) {
            timestamp = Instant.now();
        }

        // SOURCE
        String source = event.getSource();

        if (source == null) {
            source = sourceDetector.detect(message);
        }

        if (source == null || source.isBlank()) {
            source = "UNKNOWN";
        }

        // RESULT
        return new LogEvent(timestamp, source, level, message);
    }


    private Instant extractTimestamp(String message) {

        TimestampParseResult result = timestampExtractor.extract(message);

        if (result.isParsed()) {
            return result.getTimestamp();
        }

        return null;
    }
}