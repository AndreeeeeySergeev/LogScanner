package util;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class FileUtils {


    public static final List<String> SUPPORTED_FORMATS = List.of(
            "log", "txt", "json", "xml", "csv"
    );

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
            return ""; // нет расширения
        }

        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    public static boolean isSupportedFormat(String format) {
        return SUPPORTED_FORMATS.contains(format);
    }


    public static List<String> listFiles(String directoryPath) {

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {

            return paths
                    .filter(Files::isRegularFile)

                    // фильтр по расширению
                    .filter(path -> {
                        String format = detectFormat(path.toString());
                        return isSupportedFormat(format);
                    })

                    // фильтр читаемости
                    .filter(Files::isReadable)

                    .map(Path::toString)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения директории: " + directoryPath, e);
        }
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
            return "UTF-8";
        }

        String encoding = detector.getDetectedCharset();
        detector.reset();

        return encoding != null ? encoding : "UTF-8";
    }
}