package normalizer.level;

public class JsonLevelParser implements LevelParser {

    @Override
    public String parse(String message) {

        String lower = message.toLowerCase();

        int idx = lower.indexOf("\"level\"");

        if (idx == -1) return null;

        int colon = message.indexOf(":", idx);
        if (colon == -1) return null;

        int start = message.indexOf("\"", colon);
        int end = message.indexOf("\"", start + 1);

        if (start != -1 && end != -1) {
            return message.substring(start + 1, end).toUpperCase();
        }

        return null;
    }
}