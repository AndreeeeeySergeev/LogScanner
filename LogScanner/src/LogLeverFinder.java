import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.io.IOException;

public class LogLeverFinder {
    public File filePath;

    //   Optional<String> file = Optional.ofNullable(null);
    public LogLeverFinder(String filePath) {
        this.filePath = filePath;
    }
    try {
        boolean exists1 = Files.exists(Paths.get(filePath));
        if (exists1 && Files.isReadable(Path.of(filePath))) {
           // URLConnection.guessContentTypeFromName(filePath);
            //Files.probeContentType(Path.of(filePath));
            String format = filePath.getName().substring(filePath.getName().lastIndexOf(".") + 1);
        }
    } catch (IOException e) {
        System.err.printlm("Ошибка в пути файла или прав доступа: " + e.getMessage());
    }
}
