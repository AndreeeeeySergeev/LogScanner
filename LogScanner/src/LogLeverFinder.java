import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class LogLeverFinder {
    String filePath;


    public LogLeverFinder(String filePath) {
        this.filePath = filePath;
    }
    File file = new File(filePath);
    //Path path = new Path(filePath);

    try {
        boolean exists1 = Files.exists(Paths.get(filePath));
        if (exists1 && Files.isReadable(Path.of(filePath))) {
           // URLConnection.guessContentTypeFromName(filePath);
            //Files.probeContentType(Path.of(filePath));
            String format = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            switch (format.toLowerCase()) {
                case "json":
                    return findInJson(filePath);
                    break;
                case "log":
                case "txt":
                    return findInText(filePath);
                    break;
                case "xml":
                    return findInXml(filePath);
                    break;
                case "postgresql":
                    return findInPostgreSQL(filePath);
                    break;
                default:
                    throw new IllegalArgumentException("Неподдерживаемый формат" + format);
                    break;
            }
        }
    } catch (IOException e) {
        System.err.printlm("Ошибка в пути файла или прав доступа: " + e.getMessage());
    }
}
