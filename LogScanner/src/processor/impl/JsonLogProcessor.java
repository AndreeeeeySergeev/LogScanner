package processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import processor.LogProcessor;
import util.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static util.EncodingDetector.detectEncoding;


public class JsonLogProcessor implements LogProcessor {

    @Override
    public void process(String filePath,
                        String fileOutput,
                        List<String> levels) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        // 1. Определяем кодировку
        String encoding = FileUtils.detectEncoding(filePath);

        JsonNode root;

        // 2. Читаем JSON
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filePath),
                Charset.forName(encoding))) {

            root = mapper.readTree(reader);
        }

        // 3. Пишем результат
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileOutput),
                        StandardCharsets.UTF_8))) {

            processJsonNode(root, levels, writer);
        }
    }

    private void processJsonNode(JsonNode node,
                                 List<String> levels,
                                 BufferedWriter writer) throws IOException {

        String nodeText = node.toString().toLowerCase();

        if (levels.stream().anyMatch(nodeText::contains)) {
            writer.write(node.toString());
            writer.newLine();
        }

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                try {
                    processJsonNode(entry.getValue(), levels, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                processJsonNode(item, levels, writer);
            }
        }
    }
}
