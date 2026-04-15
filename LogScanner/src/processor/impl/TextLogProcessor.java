package processor.impl;

import model.LogEvent;
import processor.LogProcessor;
import util.FileUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TextLogProcessor implements LogProcessor {

    @Override
    public List<LogEvent> process(String filePath, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        String encoding = FileUtils.detectEncoding(filePath);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), Charset.forName(encoding)))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                String lowerLine = line.toLowerCase();

                boolean match = levels.stream()
                        .anyMatch(level -> lowerLine.contains(level.toLowerCase()));

                if (match) {

                    LogEvent event = new LogEvent(
                            Instant.now(),          // пока так (потом парсим из лога)
                            "TEXT_FILE",            // источник (потом улучшим)
                            "UNKNOWN",              // уровень (потом выделим отдельно)
                            line                    // оригинальное сообщение
                    );

                    events.add(event);
                }
            }
        }

        return events;
    }
}