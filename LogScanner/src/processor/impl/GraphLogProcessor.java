package processor.impl;

import model.LogEvent;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import processor.LogProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GraphLogProcessor implements LogProcessor {

    private final String username;
    private final String password;

    public GraphLogProcessor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void process(String uri,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
             Session session = driver.session()) {

            System.out.println(" Подключение к Graph DB");

            String query = "MATCH (n) RETURN labels(n) AS labels, n{.*} AS props LIMIT 1000";

            Result result = session.run(query);

            while (result.hasNext()) {

                Record record = result.next();

                List<String> labels =
                        record.get("labels").asList(Value::asString);

                Map<String, Object> properties =
                        record.get("props").asMap();

                String message = formatNode(labels, properties);

                if (message == null || message.isBlank()) continue;

                consumer.accept(new LogEvent(
                        Instant.now(),
                        "GRAPH_DB",
                        "UNKNOWN",
                        message
                ));
            }
        }
    }


    // Форматирование

    private String formatNode(List<String> labels,
                              Map<String, Object> properties) {

        return "Labels: " + labels + " | Properties: " + properties;
    }
}