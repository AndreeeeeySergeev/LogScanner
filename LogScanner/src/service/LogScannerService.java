package service;

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
                processor.process(filePath, levels);


        LogNormalizer normalizer = new SimpleLogNormalizer(levels);

        List<LogEvent> normalizedEvents = events.stream()
                .map(normalizer::normalize)
                .collect(Collectors.toList());

        writer.write(normalizedEvents, outputPath);

        System.out.println("Обработано событий: " + normalizedEvents.size());
    }
}