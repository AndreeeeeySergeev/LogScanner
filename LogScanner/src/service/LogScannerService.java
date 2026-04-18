package service;

import alert.AlertService;
import alert.impl.SimpleAlertService;
import model.LogEvent;
import normalizer.LogNormalizer;
import normalizer.impl.SimpleLogNormalizer;
import processor.LogProcessor;
import processor.factory.LogProcessorFactory;
import util.EncodingDetector;
import util.FileScanner;
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

                String encoding = EncodingDetector.detectEncoding(filePath);

                String format = util.FileUtils.detectFormat(filePath);

                LogProcessor processor =
                        LogProcessorFactory.getProcessor(format);

                processor.process(filePath, encoding, event -> {

                    try {
                        LogEvent normalized = normalizer.normalize(event);

                        if (normalized == null) return;

                        alertService.process(normalized);
                        writer.write(normalized);

                    } catch (Exception e) {

                        System.err.println("❌ Ошибка обработки события:");
                        System.err.println("Файл: " + filePath);
                        System.err.println("Сообщение: " + event.getMessage());
                        e.printStackTrace();
                    }
                });
            }

        } finally {
            writer.close();
        }
    }
}