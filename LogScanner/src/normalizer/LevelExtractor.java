package normalizer;

import java.util.List;

public class LevelExtractor {

    public static String extract(String message, List<String> levels) {

        if (message == null) return null;

        String lower = message.toLowerCase();

        for (String level : levels) {
            if (lower.contains(level.toLowerCase())) {
                return level.toUpperCase();
            }
        }

        return null;
    }
}