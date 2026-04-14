package processor;

import java.util.List;

public interface LogProcessor {

    void process(String inputPath,
                 String outputPath,
                 List<String> levels) throws Exception;
}