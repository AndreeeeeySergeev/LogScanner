package processor.impl;

import processor.LogProcessor;
import util.FileUtils;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static util.EncodingDetector.detectEncoding;

public class XmlLogProcessor implements LogProcessor {

    @Override
    public void process(String filePath,
                        String fileOutput,
                        List<String> levels) throws Exception {

        // 1. Нормализуем уровни один раз
        List<String> normalizedLevels = levels.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // 2. Определяем кодировку
        String encoding = FileUtils.detectEncoding(filePath);

        // 3. Настраиваем безопасный XML
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        try (InputStream is = new FileInputStream(filePath);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(
                             new FileOutputStream(fileOutput),
                             StandardCharsets.UTF_8))) {

            XMLStreamReader reader = factory.createXMLStreamReader(is, encoding);

            StringBuilder textBuffer = new StringBuilder();

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {

                    case XMLStreamConstants.START_ELEMENT:
                        // очищаем буфер при новом элементе
                        textBuffer.setLength(0);

                        // 1. Проверяем атрибуты
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            String value = reader.getAttributeValue(i);

                            if (value != null) {
                                processAndWrite(value, normalizedLevels, writer);
                            }
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        // 2. Накопление текста (ВАЖНО!)
                        textBuffer.append(reader.getText());
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        // 3. Обрабатываем накопленный текст
                        String text = textBuffer.toString().trim();

                        if (!text.isEmpty()) {
                            processAndWrite(text, normalizedLevels, writer);
                        }

                        textBuffer.setLength(0);
                        break;
                }
            }

            reader.close();
        }
    }

    private void processAndWrite(String text,
                                 List<String> levels,
                                 BufferedWriter writer) throws IOException {

        String lower = text.toLowerCase();

        if (levels.stream().anyMatch(lower::contains)) {
            writer.write(text);
            writer.newLine();
        }
    }
}