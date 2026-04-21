package processor.impl;

import com.fasterxml.jackson.core.*;
import model.LogEvent;
import processor.LogProcessor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public class JsonLogProcessor implements LogProcessor {

    private final List<String> levels;

    public JsonLogProcessor(List<String> levels) {
        this.levels = levels;
    }

    @Override
    public void process(String filePath,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        JsonFactory factory = new JsonFactory();

        try (InputStream is = new FileInputStream(filePath);
             JsonParser parser = factory.createParser(is)) {

            while (!parser.isClosed()) {

                JsonToken token = parser.nextToken();

                // ищем начало объекта
                if (token == JsonToken.START_OBJECT) {

                    String json = readFullObject(parser);

                    if (json == null || json.isBlank()) continue;

                    //  лёгкий pre-filter
                    if (!matchesLevel(json)) continue;

                    consumer.accept(new LogEvent(json));
                }
            }
        }
    }

    // ЧТЕНИЕ ОДНОГО JSON ОБЪЕКТА

    private String readFullObject(JsonParser parser) throws Exception {

        StringBuilder sb = new StringBuilder();

        int depth = 0;

        do {
            JsonToken token = parser.currentToken();

            if (token == JsonToken.START_OBJECT) depth++;
            if (token == JsonToken.END_OBJECT) depth--;

            sb.append(tokenToString(parser));

            if (depth == 0) {
                break;
            }

        } while (parser.nextToken() != null);

        return sb.toString();
    }

    private String tokenToString(JsonParser parser) throws Exception {

        JsonToken token = parser.currentToken();

        switch (token) {
            case START_OBJECT: return "{";
            case END_OBJECT: return "}";
            case START_ARRAY: return "[";
            case END_ARRAY: return "]";
            case FIELD_NAME: return "\"" + parser.getCurrentName() + "\":";
            case VALUE_STRING: return "\"" + parser.getValueAsString() + "\"";
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT: return parser.getNumberValue().toString();
            case VALUE_TRUE: return "true";
            case VALUE_FALSE: return "false";
            case VALUE_NULL: return "null";
            default: return "";
        }
    }

    // PRE-FILTER

    private boolean matchesLevel(String json) {

        if (levels == null || levels.isEmpty()) return true;

        String lower = json.toLowerCase();

        for (String lvl : levels) {
            if (lower.contains(lvl.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}