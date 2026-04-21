package normalizer;

import normalizer.level.LevelParser;
import normalizer.level.JsonLevelParser;
import normalizer.level.RegexLevelParser;

import java.util.List;

public class LevelExtractor {

    private final List<LevelParser> parsers;

    public LevelExtractor(List<String> levels) {

        this.parsers = List.of(
                new JsonLevelParser(),      // быстрый
                new RegexLevelParser(levels) // универсальный
        );
    }

    public String extract(String message) {

        if (message == null || message.isBlank()) {
            return null;
        }

        for (LevelParser parser : parsers) {

            String level = parser.parse(message);

            if (level != null) {
                return level;
            }
        }

        return null;
    }
}