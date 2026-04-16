package processor.impl;

import model.LogEvent;
import org.neo4j.driver.*;
import processor.LogProcessor;
import org.neo4j.driver.Record;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphLogProcessor implements LogProcessor {

    @Override
    public List<LogEvent> process(String uri, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        // (потом вынести
        String username = "neo4j";
        String password = "password";

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
             Session session = driver.session()) {

            String query = "MATCH (n) RETURN labels(n) AS labels, n{.*} AS props LIMIT 1000";

            Result result = session.run(query);

            while (result.hasNext()) {

                Record record = result.next();

                List<String> labels = record.get("labels").asList(Value::asString);
                Map<String, Object> properties = record.get("props").asMap();

                if (containsLevel(properties, levels)) {

                    String message = formatNode(labels, properties);

                    events.add(new LogEvent(
                            Instant.now(),
                            "GRAPH_DB",
                            "UNKNOWN",
                            message
                    ));
                }
            }
        }

        return events;
    }

    private boolean containsLevel(Map<String, Object> properties, List<String> levels) {

        for (Object value : properties.values()) {

            if (value instanceof String) {

                String str = ((String) value).toLowerCase();

                boolean match = levels.stream()
                        .anyMatch(level -> str.contains(level.toLowerCase()));

                if (match) return true;
            }
        }

        return false;
    }

    private String formatNode(List<String> labels, Map<String, Object> properties) {

        return "Labels: " + labels + " | Properties: " + properties;
    }
}