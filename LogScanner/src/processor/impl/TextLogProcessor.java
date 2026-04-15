package processor.impl;

import processor.LogProcessor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static util.EncodingDetector.detectEncoding;

public class TextLogProcessor implements LogProcessor {

    @Override
    public void process(String filePath,
                        String fileOutput,
                        List<String> levels) throws Exception {

        // 1. Определяем кодировку
        String encoding = detectEncoding(filePath);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath),
                        Charset.forName(encoding)));

             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(
                             new FileOutputStream(fileOutput),
                             StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // пропускаем мусор
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                String lowerLine = line.toLowerCase();

                // проверяем наличие уровней
                if (levels.stream().anyMatch(level ->
                        lowerLine.contains(level.toLowerCase()))) {

                    writer.write(line);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            throw new IOException("Ошибка обработки текстового файла: " + e.getMessage(), e);
        }
    }
}