import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.mozilla.universalchardet.UniversalDetector;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.*;
import java.sql.*;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.stream.*;







public class LogLeverFinder {
    String filePath;
    String fileOutPut;
    String jdbcUrl;
    String username;
    String password;
    private final List<String> level = Arrays.asList("critical", "error", "warning", "warn", "fatal", "alert", "emergency");

    public LogLeverFinder(String filePath, String fileOutPut) throws Exception {
        this.filePath = filePath;
        this.fileOutPut = fileOutPut;
    }

    public void processFile() {
        File file = new File(filePath);
        boolean exist = Files.exists(Paths.get(filePath));
        boolean readable = Files.isReadable(Path.of(filePath));

        try {
            if (exist && readable) {

                String format = file.getName()
                        .substring(file.getName().lastIndexOf(".") + 1);

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
                        findInRelationalDB(jdbcUrl, username, password, fileOutPut, level);
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
                        throw new IllegalArgumentException("Неподдерживаемый формат " + format);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    public void findInJson(String filePath, String fileOutPut, List<String> level) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Определяем кодировку файла
        String encoding = detectEncoding(filePath);

        JsonNode root;

        // 2. Читаем JSON с учётом кодировки
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filePath), Charset.forName(encoding))) {

            root = mapper.readTree(reader);
        }

        // 3. Пишем результат в UTF-8
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileOutPut), StandardCharsets.UTF_8))) {

            processJsonNode(root, level, writer);
        }
    }

    private void processJsonNode(JsonNode node, List<String> level, BufferedWriter writer) throws IOException {

        // Проверяем текущий узел
        String nodeText = node.toString().toLowerCase();

        if (level.stream().anyMatch(nodeText::contains)) {
            writer.write(node.toString());
            writer.newLine();
        }

        // Если объект — идём по полям
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                try {
                    processJsonNode(entry.getValue(), level, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // Если массив — идём по элементам
        if (node.isArray()) {
            for (JsonNode item : node) {
                processJsonNode(item, level, writer);
            }
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

    String detectedEncoding = detectEncoding(filePath);

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(filePath), Charset.forName(detectedEncoding)));

         BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(new FileOutputStream(fileOutPut), StandardCharsets.UTF_8))) {

        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            String lowerLine = line.toLowerCase();

            if (level.stream().anyMatch(levelItem ->
                    lowerLine.contains(levelItem.toLowerCase()))) {

                writer.write(line);
                writer.newLine();
            }
        }

    } catch (IOException e) {
        throw new IOException("Ошибка обработки файла: " + e.getMessage(), e);
    }
}

    // Метод для определения кодировки
    private  String detectEncoding(String filePath) throws IOException {
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

        // 1. Создаём фабрику и настраиваем безопасность
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document document;

        try {
            // 2. Пробуем стандартный парсинг (XML сам определит кодировку)
            document = builder.parse(new File(filePath));

        } catch (SAXParseException e) {
            // 3. Если не получилось — fallback на detectEncoding
            try (InputStream fis = new FileInputStream(filePath)) {
                InputSource source = new InputSource(fis);
                source.setEncoding(detectEncoding(filePath));
                document = builder.parse(source);
            }
        }

        Element root = document.getDocumentElement();

        // 4. Запись результата в UTF-8
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileOutPut),
                        StandardCharsets.UTF_8))) {

            processXmlElement(root, level, writer);
        }
    }

    private void processXmlElement(Element element, List<String> level, BufferedWriter writer) throws IOException {

        // 1. Обрабатываем только прямой текст узла (без вложенных элементов)
        if (element.getFirstChild() != null &&
                element.getFirstChild().getNodeType() == Node.TEXT_NODE) {

            String textContent = element.getFirstChild().getTextContent().trim();

            if (!textContent.isEmpty() && containsLogLevel(textContent, level)) {
                writer.write(textContent);
                writer.newLine();
            }
        }

        // 2. Обрабатываем атрибуты
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);

            String attrValue = attribute.getValue();
            if (attrValue != null) {
                attrValue = attrValue.trim();

                if (!attrValue.isEmpty() && containsLogLevel(attrValue, level)) {
                    writer.write(attrValue);
                    writer.newLine();
                }
            }
        }

        // 3. Рекурсивно обрабатываем дочерние элементы
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
                                   String outputFile, List<String> levels) throws Exception {

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Проверка прав доступа
            if (!hasRequiredPrivileges(conn)) {
                throw new SQLException("Недостаточно прав для выполнения поиска");
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

                DatabaseMetaData meta = conn.getMetaData();

                // Получаем все таблицы базы данных
                try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        searchTableForLevels(conn, tableName, writer, levels);
                    }
                }
            }
        }
    }




    private boolean hasRequiredPrivileges(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM information_schema.tables LIMIT 1")) {
            return rs.next(); // Если запрос выполнился — права есть
        } catch (SQLException e) {
            System.err.println("Недостаточно прав для доступа к information_schema: " + e.getMessage());
            return false;
        }
    }

    private void searchTableForLevels(Connection conn, String tableName,
                                      BufferedWriter writer, List<String> levels) throws SQLException, IOException {
        // Ищем колонки, которые могут содержать уровни логирования
        List<String> candidateColumns = findCandidateColumns(conn, tableName);

        for (String column : candidateColumns) {
            searchColumnForLevels(conn, tableName, column, writer, levels);
        }
    }

    private List<String> findCandidateColumns(Connection conn, String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        List<String> keywords = Arrays.asList("level", "log", "severity", "status", "type");

        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME").toLowerCase();
                if (keywords.stream().anyMatch(keyword -> columnName.contains(keyword))) {
                    columns.add(columnName);
                }
            }
        }
        return columns;
    }

    private void searchColumnForLevels(Connection conn, String tableName, String columnName,
                                       BufferedWriter writer, List<String> levels) throws SQLException, IOException {
        String levelsList = levels.stream()
                .map(level -> "'" + level.replace("'", "''") + "'")  // Экранирование кавычек
                .collect(Collectors.joining(", "));

        String query = "SELECT * FROM " + tableName +
                " WHERE " + columnName + " IN (" + levelsList + ")";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCount = rsMeta.getColumnCount();

            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                row.append("Таблица: ").append(tableName)
                        .append(", Колонка: ").append(columnName).append(" | ");

                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value != null) {
                        row.append(rsMeta.getColumnName(i))
                                .append(": ")
                                .append(value)
                                .append(" | ");
                    }
                }

                if (row.length() > 0) {
                    writer.write(row.toString().trim() + "\n");
                }
            }
        } catch (SQLException e) {
            // Игнорируем ошибки выполнения запроса (например, если тип колонки не подходит для IN)
            System.err.println("Ошибка при запросе к таблице " + tableName +
                    ", колонке " + columnName + ": " + e.getMessage());
        }
    }

    public void findInNoSqlDB(String connectionString, String databaseName,
                              String fileOutPut, List<String> level) throws Exception {

        try (MongoClient mongoClient = MongoClients.create(connectionString);
             BufferedWriter writer = new BufferedWriter(
                     (new OutputStreamWriter(new FileOutputStream(fileOutPut), StandardCharsets.UTF_8))) {

                 MongoDatabase database = mongoClient.getDatabase(databaseName);
                 List<String> collectionNames = database.listCollectionNames().into(new ArrayList<>());

        System.out.println("Найдено коллекций: " + collectionNames.size());

        for (String collectionName : collectionNames) {
                     searchCollectionForLevels(database, collectionName, writer, levels);
                 }
             } catch (IOException e) {
                System.err.println(e.getMessage);
             }
        }
    }

    private void searchCollectionForLevels(MongoDatabase database, String collectionName,
                                           BufferedWriter writer, List<String> levels) throws IOException {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // Создаём фильтр: ищем документы, где ЛЮБОЕ поле содержит одно из значений levels
        List<Bson> orConditions = new ArrayList<>();
        for (String level : levels) {
            orConditions.add(Filters.regex("\\$**", level, "i")); // Поиск по всем полям, без учёта регистра
        }

        Bson filter = Filters.or(orConditions);

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            int foundCount = 0;
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                writer.write("Коллекция: " + collectionName + " | " + doc.toJson() + "\n");
                foundCount++;
            }
            if (foundCount > 0) {
                System.out.println("В коллекции '" + collectionName + "' найдено " + foundCount + " документов");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при поиске в коллекции " + collectionName + ": " + e.getMessage());
        }
    }

public void findInGraphDB(String uri, String username, String password,
                          List<String> levels, String outputFile) throws Exception {

    Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));

    try (Session session = driver.session();
         BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))) {

        // Запрос: получаем все узлы и их свойства
        String cypherQuery = "MATCH (n) RETURN labels(n) AS nodeLabels, n{.*} AS properties";
        Result result = session.run(cypherQuery);

        while (result.hasNext()) {
            Record record = result.next();
            List<String> nodeLabels = record.get("nodeLabels").asList(Value::asString);
            Map<String, Object> properties = record.get("properties").asMap();

            // Проверяем каждое свойство на соответствие уровням логирования
            boolean foundMatch = false;
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String propValue = (String) entry.getValue();
                    if (levels.contains(propValue)) {
                        foundMatch = true;
                        break;
                    }
                }
            }

            if (foundMatch) {
                String formattedRecord = String.format("Labels: %s, Properties: %s",
                        nodeLabels, properties);
                writer.write(formattedRecord + "\n");
            }
        }
    } finally {
        driver.close();
    }
}
}