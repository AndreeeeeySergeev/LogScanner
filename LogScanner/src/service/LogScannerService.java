package service;

import alert.AlertService;
import alert.impl.SimpleAlertService;
import model.LogEvent;
import normalizer.LogNormalizer;
import normalizer.impl.SimpleLogNormalizer;
import processor.LogProcessor;
import processor.factory.LogProcessorFactory;
import util.FileUtils;
import writer.impl.FileLogWriter;

import java.util.List;

public class LogScannerService {

    public void process(String input,
                        String output,
                        List<String> levels) throws Exception {

        // 1. Определяем формат
        String format = FileUtils.detectFormat(input);

        // 2. Получаем processor
        LogProcessor processor =
                LogProcessorFactory.getProcessor(format);

        // 3. Читаем события
        List<LogEvent> events =
                processor.process(input, levels);

        // 4. Нормализация
        LogNormalizer normalizer =
                new SimpleLogNormalizer(levels);

        // 5. Alert service
        AlertService alertService =
                new SimpleAlertService();

        // 6. Writer
        FileLogWriter writer =
                new FileLogWriter(output);

        try {
            for (LogEvent event : events) {


                LogEvent normalized =
                        normalizer.normalize(event);


                alertService.process(normalized);

                writer.write(normalized);
            }

        } finally {
            writer.close();
        }
    }
}