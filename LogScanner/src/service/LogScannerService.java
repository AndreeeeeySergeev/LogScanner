package service;

import alert.AlertService;
import alert.impl.SimpleAlertService;
import model.LogEvent;
import normalizer.LogNormalizer;
import normalizer.impl.SimpleLogNormalizer;
import processor.LogProcessor;
import processor.factory.LogProcessorFactory;
import util.FileScanner;
import util.FileUtils;
import writer.impl.FileLogWriter;

import java.util.List;

public class LogScannerService {

    public void processDirectory(String inputDir,
                                 String output,
                                 List<String> levels) throws Exception {

        List<String> files = FileScanner.scan(inputDir);

        LogNormalizer normalizer = new SimpleLogNormalizer(levels);
        AlertService alertService = new SimpleAlertService();
        FileLogWriter writer = new FileLogWriter(output);

        try {
            for (String filePath : files) {

                System.out.println("📄 Обработка файла: " + filePath);

                processSingleFile(filePath, normalizer, alertService, writer);
            }

        } finally {
            writer.close();
        }
    }

    private void processSingleFile(String filePath,
                                   LogNormalizer normalizer,
                                   AlertService alertService,
                                   FileLogWriter writer) throws Exception {

        String format = FileUtils.detectFormat(filePath);

        LogProcessor processor =
                LogProcessorFactory.getProcessor(format);

        List<LogEvent> events =
                processor.process(filePath);

        for (LogEvent event : events) {

            LogEvent normalized = normalizer.normalize(event);

            if (normalized == null) {
                continue;
            }

            alertService.process(normalized);
            writer.write(normalized);
        }
    }
}