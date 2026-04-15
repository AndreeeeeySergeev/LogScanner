package processor.impl;

import org.neo4j.driver.*;
import processor.LogProcessor;
import org.neo4j.driver.Record;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GraphLogProcessor implements LogProcessor {

    @Override
    public void process(String uri,
                        String outputFile,
                        List<String> levels) throws Exception {

        try (Driver driver = GraphDatabase.driver(uri);
             Session session = driver.session();
             BufferedWriter writer = new BufferedWriter(
                     new FileWriter(outputFile, StandardCharsets.UTF_8))) {

            String query = buildQuery();

            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();

                List<String> labels = record.get("nodeLabels").asList(Value::asString);
                Map<String, Object> properties = record.get("properties").asMap();

                if (containsLogLevel(properties, levels)) {
                    writeResult(writer, labels, properties);
                }
            }

        } catch (Exception e) {
            throw new Exception("Ошибка при работе с Graph DB: " + e.getMessage(), e);
        }
    }

    private String buildQuery() {
        return "MATCH (n) RETURN labels(n) AS nodeLabels, n{.*} AS properties LIMIT 1000";
    }

    private boolean containsLogLevel(Map<String, Object> properties, List<String> levels) {

        for (Object value : properties.values()) {
            if (value instanceof String) {
                String strValue = (String) value;

                String lower = strValue.toLowerCase();

                if (levels.stream().anyMatch(level ->
                        lower.contains(level.toLowerCase()))) {

                    return true;
                }
            }
        }

        return false;
    }

    private void writeResult(BufferedWriter writer,
                             List<String> labels,
                             Map<String, Object> properties) throws Exception {

        String result = String.format(
                "Labels: %s | Properties: %s",
                labels, properties
        );

        writer.write(result);
        writer.newLine();
    }
}