package processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.LogEvent;
import processor.LogProcessor;
import util.FileUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JsonLogProcessor implements LogProcessor {

    @Override
    public List<LogEvent> process(String filePath, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        String encoding = FileUtils.detectEncoding(filePath);

        JsonNode root;

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filePath), Charset.forName(encoding))) {

            root = mapper.readTree(reader);
        }

        //рекурсивный обход
        processJsonNode(root, levels, events);

        return events;
    }

    private void processJsonNode(JsonNode node,
                                 List<String> levels,
                                 List<LogEvent> events) {

        String nodeText = node.toString().toLowerCase();

        boolean match = levels.stream()
                .anyMatch(level -> nodeText.contains(level.toLowerCase()));

        if (match) {
            LogEvent event = new LogEvent(
                    Instant.now(),   // потом заменить через normalizer
                    "JSON",
                    "UNKNOWN",
                    node.toString()
            );

            events.add(event);
        }

        // объект
        if (node.isObject()) {
            node.fields().forEachRemaining(entry ->
                    processJsonNode(entry.getValue(), levels, events)
            );
        }

        // массив
        if (node.isArray()) {
            for (JsonNode item : node) {
                processJsonNode(item, levels, events);
            }
        }
    }
}