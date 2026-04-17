package processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.LogEvent;
import normalizer.timestamp.TimestampExtractor;
import normalizer.timestamp.TimestampParseResult;
import processor.LogProcessor;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JsonLogProcessor implements LogProcessor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TimestampExtractor extractor = new TimestampExtractor();

    @Override
    public List<LogEvent> process(String filePath, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        JsonNode root = mapper.readTree(new File(filePath));

        extractEvents(root, events, levels);

        return events;
    }


    private void extractEvents(JsonNode node,
                               List<LogEvent> events,
                               List<String> levels) {

        if (node.isObject()) {

            String level = extractLevel(node);
            String message = extractMessage(node);

            if (level != null && isLevelAllowed(level, levels)) {


                Instant timestamp = extractTimestamp(node);

                events.add(new LogEvent(
                        timestamp,
                        detectSource(node),
                        level,
                        message != null ? message : node.toString()
                ));
            }

            node.elements().forEachRemaining(child ->
                    extractEvents(child, events, levels)
            );

        } else if (node.isArray()) {

            for (JsonNode item : node) {
                extractEvents(item, events, levels);
            }
        }
    }


    private String extractLevel(JsonNode node) {

        if (node.has("level")) {
            return node.get("level").asText().toUpperCase();
        }

        if (node.has("severity")) {
            return node.get("severity").asText().toUpperCase();
        }

        if (node.has("status")) {
            return node.get("status").asText().toUpperCase();
        }

        return null;
    }


    private String extractMessage(JsonNode node) {

        if (node.has("message")) {
            return node.get("message").asText();
        }

        if (node.has("msg")) {
            return node.get("msg").asText();
        }

        if (node.has("log")) {
            return node.get("log").asText();
        }

        return null;
    }


    private String detectSource(JsonNode node) {

        if (node.has("system")) {
            return node.get("system").asText().toUpperCase();
        }

        if (node.has("service")) {
            return node.get("service").asText().toUpperCase();
        }

        if (node.has("app")) {
            return node.get("app").asText().toUpperCase();
        }

        if (node.has("source")) {
            return node.get("source").asText().toUpperCase();
        }

        return "UNKNOWN";
    }


    private boolean isLevelAllowed(String level, List<String> levels) {

        return levels.stream()
                .anyMatch(l -> l.equalsIgnoreCase(level));
    }

    private Instant extractTimestamp(JsonNode node) {

        // пробуем найти timestamp поле
        String raw = null;

        if (node.has("timestamp")) raw = node.get("timestamp").asText();
        else if (node.has("@timestamp")) raw = node.get("@timestamp").asText();
        else if (node.has("time")) raw = node.get("time").asText();
        else if (node.has("date")) raw = node.get("date").asText();
        else if (node.has("ts")) raw = node.get("ts").asText();

        if (raw != null) {

            // 👇 используем уже существующий extractor
            TimestampParseResult result = extractor.extract(raw);

            if (result.isParsed()) {
                return result.getTimestamp();
            }
        }

        // fallback
        return Instant.now();
    }
}