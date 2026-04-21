package processor.filter;

import java.util.List;
import java.util.stream.Collectors;

public class LevelMatcher {

    private final List<String> levels;

    public LevelMatcher(List<String> levels) {
        this.levels = levels.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public boolean matches(String message) {
        if (message == null || message.isBlank()) return false;

        String lower = message.toLowerCase();

        for (String level : levels) {
            if (lower.contains(level)) {
                return true;
            }
        }

        return false;
    }
}