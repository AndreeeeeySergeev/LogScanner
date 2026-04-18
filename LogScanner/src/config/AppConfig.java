package config;

import java.util.List;

public class AppConfig {

    private List<String> levels;
    private String inputDir;
    private String outputFile;

    public AppConfig(List<String> levels, String inputDir, String outputFile) {
        this.levels = levels;
        this.inputDir = inputDir;
        this.outputFile = outputFile;
    }

    public List<String> getLevels() {
        return levels;
    }

    public String getInputDir() {
        return inputDir;
    }

    public String getOutputFile() {
        return outputFile;
    }
}