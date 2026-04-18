package util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

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
        if (dotIndex == -1) return "";
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}