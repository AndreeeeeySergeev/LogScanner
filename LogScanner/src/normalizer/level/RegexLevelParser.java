package normalizer.level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class RegexLevelParser implements LevelParser {

    private final List<String> levels;
    private final List<Pattern> patterns;

    public RegexLevelParser(List<String> levels) {

        this.levels = new ArrayList<>(levels);
        this.levels.sort(Comparator.comparingInt(String::length).reversed());

        this.patterns = new ArrayList<>();

        for (String level : this.levels) {
            String escaped = Pattern.quote(level.toLowerCase());
            patterns.add(Pattern.compile("\\b" + escaped + "\\b"));
        }
    }

    @Override
    public String parse(String message) {

        String lower = message.toLowerCase();

        for (int i = 0; i < patterns.size(); i++) {
            if (patterns.get(i).matcher(lower).find()) {
                return levels.get(i).toUpperCase();
            }
        }

        return null;
    }
}