package processor.impl;

import com.fasterxml.jackson.core.*;
import model.LogEvent;
import processor.LogProcessor;
import util.EncodingDetector;

import java.io.*;
import java.util.function.Consumer;

public class JsonLogProcessor implements LogProcessor {

    private final JsonFactory factory = new JsonFactory();

    @Override
    public void process(String filePath,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        if (encoding == null || encoding.isBlank()) {
            encoding = EncodingDetector.detectEncoding(filePath);
        }

        if (encoding == null) {
            encoding = "UTF-8";
        }

        try (InputStream is = new FileInputStream(filePath);
             Reader reader = new InputStreamReader(is, encoding);
             JsonParser parser = factory.createParser(reader)) {

            while (!parser.isClosed()) {

                JsonToken token = parser.nextToken();

                // берём только верхний объект
                if (token == JsonToken.START_OBJECT) {

                    String json = readFullObject(parser);

                    if (json == null || json.isBlank()) continue;

                    // лёгкий pre-filter
                    if (!containsLevel(json)) continue;

                    consumer.accept(new LogEvent(json));
                }
            }
        }
    }

    // читаем полный JSON объект (с вложенностью)
    private String readFullObject(JsonParser parser) throws IOException {

        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);

        int depth = 0;

        do {
            JsonToken token = parser.currentToken();

            if (token == JsonToken.START_OBJECT) depth++;
            if (token == JsonToken.END_OBJECT) depth--;

            generator.copyCurrentEvent(parser);

            if (depth == 0) break;

        } while (parser.nextToken() != null);

        generator.close();

        return writer.toString();
    }

    private boolean containsLevel(String text) {

        String lower = text.toLowerCase();

        return lower.contains("error")
                || lower.contains("critical")
                || lower.contains("warn")
                || lower.contains("info");
    }
}