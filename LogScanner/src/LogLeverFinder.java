import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LogLeverFinder {
    String filePath;
    String fileOutPut;
    private final List<String> level = Arrays.asList("critical", "error", "warning", "warn", "fatal", "alert", "emergency");

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
                return findInJson();
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
        System.err.println("Ошибка в пути файла или прав доступа: " + e.getMessage());
    }

    public void findInJson(String filePath, String fileOutPut, List<String> level) throws IOException {
      ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(fileOutPut))) {

            String line;
            int foundCount = 0;

            // Построчное чтение файла
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue; // Пропускаем пустые строки
                }
                try {
                    // Парсим JSON из строки
                    JsonNode logEntry = mapper.readTree(line);
                    JsonNode levelsNode = logEntry.findValue(String.valueOf(level));

                    if (levelsNode != null) {
                        String outPutLine = String.format(levelsNode.toString());
                        writer.write(line);
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка парсинга JSON в строке: " + line);
                    System.err.println("Детали ошибки: " + e.getMessage());
                    // Продолжаем обработку следующих строк
                }
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Файл не найден: " + filePath);
        } catch (Exception e) {
            throw new IOException("Произошла ошибка при обработке файла: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяет, соответствует ли запись одному из целевых уровней логирования.
     */
    private static boolean matchesLogLevel(JsonNode logEntry, List<String> level) {
        // Проверяем наличие поля "level" и его значение
        if (logEntry.has("level")) {
            String levels = logEntry.get("level").asText().toLowerCase();
            return targetLevels.contains(level);
        }
        return false;
    }
    }
}