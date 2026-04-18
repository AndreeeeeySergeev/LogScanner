package processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.LogEvent;
import processor.LogProcessor;

import java.io.File;
import java.util.Iterator;
import java.util.function.Consumer;

public class JsonLogProcessor implements LogProcessor {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void process(String filePath,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        JsonNode root = mapper.readTree(new File(filePath));

        extract(root, consumer);
    }

    private void extract(JsonNode node, Consumer<LogEvent> consumer) {

        if (node.isObject()) {

            String message = extractMessage(node);

            if (message != null && !message.isEmpty()) {
                consumer.accept(new LogEvent(null, null, null, message));
            } else {
                consumer.accept(new LogEvent(null, null, null, node.toString()));
            }

            Iterator<JsonNode> it = node.elements();
            while (it.hasNext()) {
                extract(it.next(), consumer);
            }

        } else if (node.isArray()) {

            for (JsonNode item : node) {
                extract(item, consumer);
            }
        }
    }

    private String extractMessage(JsonNode node) {

        if (node.has("message")) return node.get("message").asText();
        if (node.has("msg")) return node.get("msg").asText();
        if (node.has("log")) return node.get("log").asText();
        if (node.has("event")) return node.get("event").asText();

        return null;
    }
}