import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.conversions.Bson;
import org.mozilla.universalchardet.UniversalDetector;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.*;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.*;
import java.sql.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.neo4j.driver.*;






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
    try  { if (exist && readable) {
        // URLConnection.guessContentTypeFromName(filePath);
        //Files.probeContentType(Path.of(filePath));
            String format = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            switch (format.toLowerCase()) {
                case "json":
                    findInJson(filePath, fileOutPut, level);
                break;
                case "log":
                case "txt":
                case "csv":
                    findInText(filePath, fileOutPut, level);
                break;
                case "xml":
                    findInXml(filePath, fileOutPut, level);
                break;
                case "bak":
                case "du":
                case "mdf":
                case "ibd":
                case "dat":
                case "dump":
                case "sql":
                case "db":
                case "sqlite":
                    findInRelationalDB(filePath, fileOutPut, level);
                    break;
                case "wt":
                case "couch":
                case "bson":
                    findInNoSqlDB();
                    break;
                case "store":
                case "index":
                    findInGraphDB();
                    break;
                default:
                    throw new IllegalArgumentException("Неподдерживаемый формат" + format);
                    break;
                }
        }
    } catch (IOException e) {
        System.err.println("Ошибка в пути файла или прав доступа: " + e.getMessage());
    }

    public void findInJson(String filePath, String fileOutPut, List<String> level) throws IOException {
      ObjectMapper mapper = new ObjectMapper();

      try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
           BufferedWriter writer = new BufferedWriter(new FileWriter(fileOutPut))) {

           String line;
            // Построчное чтение файла
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue; // Пропускаем пустые строки и с комментариями
                }
                try {
                    // Парсим JSON из строки
                    JsonNode logEntry = mapper.readTree(line);
                    JsonNode levelsNode = logEntry.findValue(String.valueOf(level));

                    if (levelsNode != null) {
                        String outPutLine = String.format(levelsNode.toString());
                        writer.write(line + "\n");
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка парсинга JSON в строке: " + line);
                    System.err.println("Детали ошибки: " + e.getMessage());
                    // Продолжаем обработку следующих строк
                }
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Файл не найден: " + filePath);
        } catch (IOException e) {
            throw new IOException("Произошла ошибка при обработке файла: " + e.getMessage(), e);
        }
    }

//    public void findInText (String filePath, String fileOutPut, List<String> level) throws IOException {
//       try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
//            BufferedWriter writer = new BufferedWriter(new FileWriter(fileOutPut))) {
//
//           String line = reader.readLine();
//           // Построчное чтение файла
//           while (line != null) {
//               line = line.trim();
//               if (line.isEmpty() || line.startsWith("//")) {
//                   continue; // Пропускаем пустые строки и с комментариями
//               }
//               if (level.stream().anyMatch(levelItem -> line.toLowerCase().contains(levelItem.toLowerCase()))) {
//                   writer.write(line +"\n");
//               }
//               //line = reader.readLine();
//           }
//       } catch (IOException e) {
//           throw new IOException("Произошла ошибка при обработке файла: " + e.getMessage(), e);
//       }
//    }
public void findInText(String filePath, String fileOutPut, List<String> level) throws IOException {
    // 1. Определяем кодировку файла
    String detectedEncoding = detectEncoding(filePath);

    // 2. Читаем файл в обнаруженной кодировке
    try (InputStreamReader inputStreamReader = new InputStreamReader(
            new FileInputStream(filePath), Charset.forName(detectedEncoding));
         BufferedReader reader = new BufferedReader(inputStreamReader);
         // 4. Записываем в UTF-8
         BufferedWriter writer = new BufferedWriter(
                 (new FileWriter(fileOutPut, Charset.forName("UTF-8")))) {

                 String line;
                 while ((line = reader.readLine()) != null) {
                     line = line.trim();

                     // 3. Обрабатываем данные
                     if (line.isEmpty() || line.startsWith("//")) {
                         continue; // Пропускаем пустые строки и комментарии
                     }

                     if (level.stream().anyMatch(levelItem ->
                             line.toLowerCase().contains(levelItem.toLowerCase()))) {
                         writer.write(line + "\n");
                     }
                 }
         } catch (IOException e) {
        throw new IOException("Произошла ошибка при обработке файла: " + e.getMessage(), e);
    }
}

    // Метод для определения кодировки
    private static String detectEncoding(String filePath) throws IOException {
        UniversalDetector detector = new UniversalDetector(null);
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] buf = new byte[4096];
            int nread;
            while ((nread = inputStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        return encoding != null ? encoding : "UTF-8"; // Используем UTF-8, если не определена
    }

    public void findInXml(String filePath, String fileOutPut, List<String> level) throws Exception {
        String detectedEncoding = detectEncoding(filePath);

        // Парсим XML-файл
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            InputSource source = new InputSource(bis);
            source.setEncoding(detectedEncoding);

            Document document = builder.parse(source);
            Element root = document.getDocumentElement();

            try (BufferedWriter writer = new BufferedWriter(
                    (new FileWriter(fileOutPut, StandardCharsets.UTF_8))) {
                processXmlElement(root, level, writer);
            }
        } catch (SAXParseException e) {
            System.err.println("Ошибка парсинга XML в файле " + filePath +
                    ": строка " + e.getLineNumber() + ", колонка " + e.getColumnNumber());
            throw e;
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла " + filePath + ": " + e.getMessage());
            throw e;
        }
    }

    private void processXmlElement(Element element, List<String> level, BufferedWriter writer) throws IOException {
        // Проверяем содержимое текущего элемента
        String textContent = element.getTextContent().trim();
        if (!textContent.isEmpty() && containsLogLevel(textContent, level)) {
            writer.write(textContent + "\n");
        }

        // Обрабатываем атрибуты элемента
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);
            String attrValue = attribute.getValue().trim();
            if (!attrValue.isEmpty() && containsLogLevel(attrValue, level)) {
                writer.write(attrValue + "\n");
            }
        }

        // Рекурсивно обрабатываем дочерние элементы
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                processXmlElement((Element) child, level, writer);
            }
        }
    }
    private boolean containsLogLevel(String text, List<String> level) {
        return level.stream().anyMatch(levelItem ->
                text.toLowerCase().contains(levelItem.toLowerCase())
        );
    }

    public void findInRelationalDB(String jdbcUrl, String username, String password,
                                   String query, String outputFile, List<String> levels) throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))) {

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        if (value != null && containsLogLevel(value, levels)) {
                            row.append(meta.getColumnName(i))
                                    .append(": ")
                                    .append(value)
                                    .append(" | ");
                        }
                    }
                    if (row.length() > 0) {
                        writer.write(row.toString().trim() + "\n");
                    }
                }
            }
        }

        private boolean containsLogLevel (String text, List<String> level) {
            return levels.stream().anyMatch(level ->
                    text.toLowerCase().contains(level.toLowerCase())
            );
        }
    }

    public void findInNoSqlDB(String connectionString, String databaseName,
                              String collectionName, String filterJson,
                              String outputFile, List<String> levels) throws Exception {

        try (MongoClient mongoClient = MongoClients.create(connectionString);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))) {

            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            Bson filter = filterJson != null ?
                    com.mongodb.client.model.Filters.parse(filterJson) :
                    new Document();

            try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    String json = doc.toJson();
                    if (containsLogLevel(json, levels)) {
                        writer.write(json + "\n");
                    }
                }
            }
        }
    }

    public void findInGraphDB(String uri, String username, String password,
                              String cypherQuery, String outputFile,
                              List<String> levels) throws Exception {

        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));


        try (Session session = driver.session();
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))) {

            Result result = session.run(cypherQuery);

            while (result.hasNext()) {
                Record record = result.next();
                String recordStr = record.toString();
                if (containsLogLevel(recordStr, levels)) {
                    writer.write(recordStr + "\n");
                }
            }
        } finally {
            driver.close();
        }
    }


}