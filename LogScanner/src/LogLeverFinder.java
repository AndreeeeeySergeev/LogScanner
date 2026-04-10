import netscape.javascript.JSObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogLeverFinder {
    String filePath;
    String fileOutPut;
    private final List<String> level = Arrays.asList("critical", "error", "warning");

    public LogLeverFinder(String filePath, String fileOutPut) throws IOException {
        this.filePath = filePath;
        this.fileOutPut = fileOutPut;
    }
    File file = new File(filePath);
    //Path path = new Path(filePath);
     boolean exist = Files.exists(Paths.get(filePath));
     boolean readable = Files.isReadable(Path.of(filePath));
    try (exist && readable) {

           // URLConnection.guessContentTypeFromName(filePath);
            //Files.probeContentType(Path.of(filePath));
        String format = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            switch (format.toLowerCase()) {
                case "json":
                    return findInJson(filePath);
                    break;
                case "log":
                case "txt":
                case "csv":
                    return findInText(filePath);
                    break;
                case "xml":
                    return findInXml(filePath);
                    break;
                case "dump":
                case "sql":
                case "db":
                case "sqlite":
                    return findInPostgreSQL(filePath);
                    break;
                default:
                    throw new IllegalArgumentException("Неподдерживаемый формат" + format);
                    break;
            }
        } catch (IOException e) {
        System.err.printlm("Ошибка в пути файла или прав доступа: " + e.getMessage());
    }
    public void findInJson(String filePath, String fileOutPut, List<String> level) throws IOException {
       try (
           BufferedReader reader = new BufferedReader(new FileReader(filePath));
           BufferedWriter writer = new BufferedWriter(new FileWriter(fileOutPut))) {

           String line;
           ObjectMapper
       }
    }
}
