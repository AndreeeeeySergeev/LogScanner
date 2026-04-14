package util;

public class FileUtils {

    public static String detectFormat(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }
}