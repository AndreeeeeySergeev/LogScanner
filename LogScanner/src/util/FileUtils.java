package util;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static boolean exists(String filePath) {
        return Files.exists(Path.of(filePath));
    }

    public static boolean isReadable(String filePath) {
        return Files.isReadable(Path.of(filePath));
    }

    public static String detectFormat(String filePath) {
        String fileName = new File(filePath).getName();

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            throw new IllegalArgumentException("Формат файла не определён: " + fileName);
        }

        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    public static String detectEncoding(String filePath) {
        UniversalDetector detector = new UniversalDetector(null);

        try (InputStream is = new FileInputStream(filePath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) > 0 && !detector.isDone()) {
                detector.handleData(buffer, 0, bytesRead);
            }

            detector.dataEnd();

        } catch (Exception e) {
            System.err.println("Ошибка определения кодировки: " + e.getMessage());
            return "UTF-8"; // fallback
        }

        String encoding = detector.getDetectedCharset();
        detector.reset();

        return encoding != null ? encoding : "UTF-8";
    }

    public static List<String> listFiles(String directoryPath) {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения директории", e);
        }
    }
}