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

    // 🔹 Старый метод — НЕ ТРОГАЕМ (важно)
    public void process(String input,
                        String output,
                        List<String> levels) throws Exception {

        String format = FileUtils.detectFormat(input);

        LogProcessor processor =
                LogProcessorFactory.getProcessor(format);

        List<LogEvent> events =
                processor.process(input, levels);

        LogNormalizer normalizer =
                new SimpleLogNormalizer(levels);

        AlertService alertService =
                new SimpleAlertService();

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

    // НОВЫЙ МЕТОД — обработка директории
    public void processDirectory(String inputDir,
                                 String output,
                                 List<String> levels) throws Exception {

        List<String> files = FileUtils.listFiles(inputDir);

        LogNormalizer normalizer =
                new SimpleLogNormalizer(levels);

        AlertService alertService =
                new SimpleAlertService();

        FileLogWriter writer =
                new FileLogWriter(output);

        try {
            for (String filePath : files) {

                String format = FileUtils.detectFormat(filePath);

                LogProcessor processor =
                        LogProcessorFactory.getProcessor(format);

                List<LogEvent> events =
                        processor.process(filePath, levels);

                for (LogEvent event : events) {

                    LogEvent normalized =
                            normalizer.normalize(event);

                    alertService.process(normalized);

                    writer.write(normalized);
                }
            }

        } finally {
            writer.close();
        }
    }
}