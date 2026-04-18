package util;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileScanner {

    public static final List<String> SUPPORTED_FORMATS = List.of(
            "log", "txt", "json", "xml", "csv"
    );

    public static List<String> scan(String directoryPath) {

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {

            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(FileScanner::isSupported)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения директории", e);
        }
    }

    private static boolean isSupported(String filePath) {
        String format = FileUtils.detectFormat(filePath);
        return SUPPORTED_FORMATS.contains(format);
    }
}