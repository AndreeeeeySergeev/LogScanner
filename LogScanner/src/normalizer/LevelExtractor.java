package normalizer;

import java.util.List;

public class LevelExtractor {

    public static String extract(String message, List<String> levels) {

        String lowerMessage = message.toLowerCase();

        for (String level : levels) {

            String lowerLevel = level.toLowerCase();

            // более точное совпадение
            if (containsWord(lowerMessage, lowerLevel)) {
                return level.toUpperCase();
            }
        }

        return null;
    }

    private static boolean containsWord(String text, String word) {

        return text.equals(word)
                || text.startsWith(word + " ")
                || text.endsWith(" " + word)
                || text.contains(" " + word + " ");
    }
}