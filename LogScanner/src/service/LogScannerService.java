package service;

import java.util.List;

import processor.LogProcessor;
import processor.factory.LogProcessorFactory;
import util.FileUtils;

public class LogScannerService {

    public void process(String filePath,
                        String outputPath,
                        List<String> levels) throws Exception {

        String format = FileUtils.detectFormat(filePath);

        LogProcessor processor =
                LogProcessorFactory.getProcessor(format);

        processor.process(filePath, outputPath, levels);
    }
}
