package normalizer.source;

import java.util.List;
import java.util.Map;

public class KeywordSourceParser implements SourceParser {

    private final Map<String, List<String>> sources;

    public KeywordSourceParser(Map<String, List<String>> sources) {
        this.sources = sources;
    }

    @Override
    public String parse(String message) {

        String lower = message.toLowerCase();

        for (Map.Entry<String, List<String>> entry : sources.entrySet()) {

            for (String keyword : entry.getValue()) {

                if (containsWord(lower, keyword.toLowerCase())) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    private boolean containsWord(String text, String word) {
        return text.matches(".*\\b" + java.util.regex.Pattern.quote(word) + "\\b.*");
    }
}