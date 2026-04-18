package config;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigLoader {

    public static AppConfig load(String path) {

        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки config", e);
        }

        // levels
        String levelsRaw = props.getProperty("levels", "critical, error, warning, warn, fatal, alert, emergency");

        List<String> levels = Arrays.stream(levelsRaw.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        // input / output
        String inputDir = props.getProperty("inputDir", "input");
        String outputFile = props.getProperty("outputFile", "output/result.log");

        return new AppConfig(levels, inputDir, outputFile);
    }
}