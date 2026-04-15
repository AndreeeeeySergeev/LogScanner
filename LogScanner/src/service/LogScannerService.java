package service;

import alert.AlertService;
import alert.impl.SimpleAlertService;
import model.LogEvent;
import processor.LogProcessor;
import processor.factory.LogProcessorFactory;
import util.FileUtils;
import writer.LogWriter;
import writer.impl.FileLogWriter;
import normalizer.LogNormalizer;
import normalizer.impl.SimpleLogNormalizer;

import java.util.List;
import java.util.stream.Collectors;

public class LogScannerService {

    private final LogWriter writer;
    private final AlertService alertService = new SimpleAlertService();

    // 👉 можно потом подменять (например, на DB writer)
    public LogScannerService() {
        this.writer = new FileLogWriter();
    }
    public LogScannerService(LogWriter writer) {
        this.writer = writer;
    }

    public void process(String filePath,
                        String outputPath,
                        List<String> levels) throws Exception {

        String format = FileUtils.detectFormat(filePath);

        LogProcessor processor =
                LogProcessorFactory.getProcessor(format);

        List<LogEvent> events =
                processor.process(filePath, outputPath, levels);

        LogNormalizer normalizer = new SimpleLogNormalizer(levels);

        for (LogEvent event : events) {

            LogEvent normalized = normalizer.normalize(event);


            alertService.process(normalized);
        }
    }
}