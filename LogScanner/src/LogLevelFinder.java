//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mongodb.MongoClient;
//import com.mongodb.client.*;
//import org.mozilla.universalchardet.UniversalDetector;
//import org.neo4j.driver.AuthTokens;
//import org.neo4j.driver.GraphDatabase;
//import org.neo4j.driver.Result;
//import org.neo4j.driver.Session;
//import org.w3c.dom.*;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXParseException;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.*;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class LogLeverFinder {
//    String filePath;
//    String fileOutPut;
//    String jdbcUrl;
//    String username;
//    String password;
//    String connectionString;
//    String uri;
//    private final List<String> level = Arrays.asList("critical", "error", "warning", "warn", "fatal", "alert", "emergency");
//
//    public LogLeverFinder(String filePath, String fileOutPut) throws Exception {
//        this.filePath = filePath;
//        this.fileOutPut = fileOutPut;
//    }
//
//    public void processFile() {
//        File file = new File(filePath);
//        boolean exist = Files.exists(Paths.get(filePath));
//        boolean readable = Files.isReadable(Path.of(filePath));
//
//        try {
//            if (exist && readable) {
//
//                String format = file.getName()
//                        .substring(file.getName().lastIndexOf(".") + 1);
//
//                switch (format.toLowerCase()) {
//                    case "json":
//                        findInJson(filePath, fileOutPut, level);
//                        break;
//
//                    case "log":
//                    case "txt":
//                    case "csv":
//                        findInText(filePath, fileOutPut, level);
//                        break;
//
//                    case "xml":
//                        findInXml(filePath, fileOutPut, level);
//                        break;
//
//                    case "bak":
//                    case "du":
//                    case "mdf":
//                    case "ibd":
//                    case "dat":
//                    case "dump":
//                    case "sql":
//                    case "db":
//                    case "sqlite":
//                        findInRelationalDB(jdbcUrl, username, password, fileOutPut, level);
//                        break;
//
//                    case "wt":
//                    case "couch":
//                    case "bson":
//                        findInNoSqlDB(connectionString, fileOutPut, level);
//                        break;
//
//                    case "store":
//                    case "index":
//                        findInGraphDB(uri, username, password, level, fileOutPut);
//                        break;
//
//                    default:
//                        throw new IllegalArgumentException("Неподдерживаемый формат " + format);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Ошибка: " + e.getMessage());
//        }
//    }
//
//    public void findInJson(String filePath, String fileOutPut, List<String> level) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//
//        // 1. Определяем кодировку файла
//        String encoding = detectEncoding(filePath);
//
//        JsonNode root;
//
//        // 2. Читаем JSON с учётом кодировки
//        try (InputStreamReader reader = new InputStreamReader(
//                new FileInputStream(filePath), Charset.forName(encoding))) {
//
//            root = mapper.readTree(reader);
//        }
//
//        // 3. Пишем результат в UTF-8
//        try (BufferedWriter writer = new BufferedWriter(
//                new OutputStreamWriter(new FileOutputStream(fileOutPut), StandardCharsets.UTF_8))) {
//
//            processJsonNode(root, level, writer);
//        }
//    }
//
//    private void processJsonNode(JsonNode node, List<String> level, BufferedWriter writer) throws IOException {
//
//        // Проверяем текущий узел
//        String nodeText = node.toString().toLowerCase();
//
//        if (level.stream().anyMatch(nodeText::contains)) {
//            writer.write(node.toString());
//            writer.newLine();
//        }
//
//        // Если объект — идём по полям
//        if (node.isObject()) {
//            node.fields().forEachRemaining(entry -> {
//                try {
//                    processJsonNode(entry.getValue(), level, writer);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        // Если массив — идём по элементам
//        if (node.isArray()) {
//            for (JsonNode item : node) {
//                processJsonNode(item, level, writer);
//            }
//        }
//    }
//
//public void findInText(String filePath, String fileOutPut, List<String> level) throws IOException {
//
//    String detectedEncoding = detectEncoding(filePath);
//
//    try (BufferedReader reader = new BufferedReader(
//            new InputStreamReader(new FileInputStream(filePath), Charset.forName(detectedEncoding)));
//
//         BufferedWriter writer = new BufferedWriter(
//                 new OutputStreamWriter(new FileOutputStream(fileOutPut), StandardCharsets.UTF_8))) {
//
//        String line;
//
//        while ((line = reader.readLine()) != null) {
//            line = line.trim();
//
//            if (line.isEmpty() || line.startsWith("//")) {
//                continue;
//            }
//
//            String lowerLine = line.toLowerCase();
//
//            if (level.stream().anyMatch(levelItem ->
//                    lowerLine.contains(levelItem.toLowerCase()))) {
//
//                writer.write(line);
//                writer.newLine();
//            }
//        }
//
//    } catch (IOException e) {
//        throw new IOException("Ошибка обработки файла: " + e.getMessage(), e);
//    }
//}
//
//    // Метод для определения кодировки
//    private  String detectEncoding(String filePath) throws IOException {
//        UniversalDetector detector = new UniversalDetector(null);
//        try (InputStream inputStream = new FileInputStream(filePath)) {
//            byte[] buf = new byte[4096];
//            int nread;
//            while ((nread = inputStream.read(buf)) > 0 && !detector.isDone()) {
//                detector.handleData(buf, 0, nread);
//            }
//        }
//        detector.dataEnd();
//        String encoding = detector.getDetectedCharset();
//        detector.reset();
//        return encoding != null ? encoding : "UTF-8"; // Используем UTF-8, если не определена
//    }
//
//    public void findInXml(String filePath, String fileOutPut, List<String> level) throws Exception {
//
//        // 1. Создаём фабрику и настраиваем безопасность
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//
//        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
//        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
//        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//        factory.setXIncludeAware(false);
//        factory.setExpandEntityReferences(false);
//
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        org.w3c.dom.Document document;
//
//        try {
//            // 2. Пробуем стандартный парсинг (XML сам определит кодировку)
//            document = builder.parse(new File(filePath));
//
//        } catch (SAXParseException e) {
//            // 3. Если не получилось — fallback на detectEncoding
//            try (InputStream fis = new FileInputStream(filePath)) {
//                InputSource source = new InputSource(fis);
//                source.setEncoding(detectEncoding(filePath));
//                document = builder.parse(source);
//            }
//        }
//
//        Element root = document.getDocumentElement();
//
//        // 4. Запись результата в UTF-8
//        try (BufferedWriter writer = new BufferedWriter(
//                new OutputStreamWriter(
//                        new FileOutputStream(fileOutPut),
//                        StandardCharsets.UTF_8))) {
//
//            processXmlElement(root, level, writer);
//        }
//    }
//
//    private void processXmlElement(Element element, List<String> level, BufferedWriter writer) throws IOException {
//
//        // 1. Обрабатываем только прямой текст узла (без вложенных элементов)
//        if (element.getFirstChild() != null &&
//                element.getFirstChild().getNodeType() == Node.TEXT_NODE) {
//
//            String textContent = element.getFirstChild().getTextContent().trim();
//
//            if (!textContent.isEmpty() && containsLogLevel(textContent, level)) {
//                writer.write(textContent);
//                writer.newLine();
//            }
//        }
//
//        // 2. Обрабатываем атрибуты
//        NamedNodeMap attributes = element.getAttributes();
//        for (int i = 0; i < attributes.getLength(); i++) {
//            Attr attribute = (Attr) attributes.item(i);
//
//            String attrValue = attribute.getValue();
//            if (attrValue != null) {
//                attrValue = attrValue.trim();
//
//                if (!attrValue.isEmpty() && containsLogLevel(attrValue, level)) {
//                    writer.write(attrValue);
//                    writer.newLine();
//                }
//            }
//        }
//
//        // 3. Рекурсивно обрабатываем дочерние элементы
//        NodeList children = element.getChildNodes();
//        for (int i = 0; i < children.getLength(); i++) {
//            Node child = children.item(i);
//
//            if (child.getNodeType() == Node.ELEMENT_NODE) {
//                processXmlElement((Element) child, level, writer);
//            }
//        }
//    }
//    private boolean containsLogLevel(String text, List<String> level) {
//        return level.stream().anyMatch(levelItem ->
//                text.toLowerCase().contains(levelItem.toLowerCase())
//        );
//    }
//
//    public void findInRelationalDB(String jdbcUrl, String username, String password,
//                                   String outputFile, List<String> levels) throws Exception {
//
//        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
//             BufferedWriter writer = new BufferedWriter(
//                     new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
//
//            if (!hasRequiredPrivileges(conn)) {
//                throw new SQLException("Недостаточно прав для выполнения поиска");
//            }
//
//            DatabaseMetaData meta = conn.getMetaData();
//
//            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
//                while (tables.next()) {
//                    String tableName = tables.getString("TABLE_NAME");
//
//                    System.out.println("Обработка таблицы: " + tableName);
//
//                    searchTableForLevels(conn, tableName, writer, levels);
//                }
//            }
//
//        } catch (SQLException e) {
//            throw new SQLException("Ошибка работы с БД: " + e.getMessage(), e);
//        }
//    }
//
//
//
//
//    private boolean hasRequiredPrivileges(Connection conn) throws SQLException {
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT 1 FROM information_schema.tables LIMIT 1")) {
//            return rs.next(); // Если запрос выполнился — права есть
//        } catch (SQLException e) {
//            System.err.println("Недостаточно прав для доступа к information_schema: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private void searchTableForLevels(Connection conn, String tableName,
//                                      BufferedWriter writer, List<String> levels) throws SQLException, IOException {
//        // Ищем колонки, которые могут содержать уровни логирования
//        List<String> candidateColumns = findCandidateColumns(conn, tableName);
//
//        for (String column : candidateColumns) {
//            searchColumnForLevels(conn, tableName, column, writer, levels);
//        }
//    }
//
//    private List<String> findCandidateColumns(Connection conn, String tableName) throws SQLException {
//        List<String> columns = new ArrayList<>();
//        List<String> keywords = Arrays.asList("level", "log", "severity", "status", "type");
//
//        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
//            while (rs.next()) {
//                String columnName = rs.getString("COLUMN_NAME").toLowerCase();
//                if (keywords.stream().anyMatch(keyword -> columnName.contains(keyword))) {
//                    columns.add(columnName);
//                }
//            }
//        }
//        return columns;
//    }
//
//    private void searchColumnForLevels(Connection conn, String tableName, String columnName,
//                                       BufferedWriter writer, List<String> levels)
//            throws SQLException, IOException {
//
//        // 1. Определяем тип БД
//        String dbProduct = conn.getMetaData().getDatabaseProductName();
//
//        String limitClause;
//
//        if (dbProduct.toLowerCase().contains("oracle")) {
//            limitClause = " FETCH FIRST 1000 ROWS ONLY";
//        } else if (dbProduct.toLowerCase().contains("microsoft")
//                || dbProduct.toLowerCase().contains("sql server")) {
//            limitClause = " OFFSET 0 ROWS FETCH NEXT 1000 ROWS ONLY";
//        } else {
//            limitClause = " LIMIT 1000";
//        }
//
//        // 2. Формируем WHERE
//        String whereClause = levels.stream()
//                .map(level -> "LOWER(" + columnName + ") LIKE '%" + level.toLowerCase() + "%'")
//                .collect(Collectors.joining(" OR "));
//
//        // 3. Формируем запрос
//        String query = "SELECT * FROM " + tableName +
//                " WHERE " + whereClause +
//                limitClause;
//
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            ResultSetMetaData rsMeta = rs.getMetaData();
//            int columnCount = rsMeta.getColumnCount();
//
//            while (rs.next()) {
//                StringBuilder row = new StringBuilder();
//
//                row.append("Таблица: ").append(tableName)
//                        .append(", Колонка: ").append(columnName).append(" | ");
//
//                for (int i = 1; i <= columnCount; i++) {
//                    String value = rs.getString(i);
//
//                    if (value != null) {
//                        row.append(rsMeta.getColumnName(i))
//                                .append(": ")
//                                .append(value)
//                                .append(" | ");
//                    }
//                }
//
//                writer.write(row.toString().trim());
//                writer.newLine();
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Ошибка при запросе к таблице " + tableName +
//                    ", колонке " + columnName + ": " + e.getMessage());
//        }
//    }
//
//    public void findInNoSqlDB(String connectionString,
//                              String fileOutPut,
//                              List<String> levels) throws Exception {
//
//        try (MongoClient mongoClient = (MongoClient) MongoClients.create(connectionString);
//             BufferedWriter writer = new BufferedWriter(
//                     new OutputStreamWriter(new FileOutputStream(fileOutPut), StandardCharsets.UTF_8))) {
//
//            // 1. Получаем ВСЕ базы
//            MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
//
//            for (String dbName : databaseNames) {
//
//                System.out.println("Обработка базы: " + dbName);
//
//                MongoDatabase database = mongoClient.getDatabase(dbName);
//
//                List<String> collectionNames =
//                        database.listCollectionNames().into(new ArrayList<>());
//
//                for (String collectionName : collectionNames) {
//
//                    System.out.println("  Коллекция: " + collectionName);
//
//                    searchCollectionForLevels(database, collectionName, writer, levels);
//                }
//            }
//
//        } catch (Exception e) {
//            throw new Exception("Ошибка при работе с MongoDB: " + e.getMessage(), e);
//        }
//    }
//
//
//    private void searchCollectionForLevels(MongoDatabase database, String collectionName,
//                                           BufferedWriter writer, List<String> levels) throws IOException {
//
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//
//        try (MongoCursor<Document> cursor = collection.find().limit(1000).iterator()) {
//
//            int foundCount = 0;
//
//            while (cursor.hasNext()) {
//                Document doc = cursor.next();
//
//                String json = doc.toJson().toLowerCase();
//
//                boolean match = levels.stream()
//                        .anyMatch(level -> json.contains(level.toLowerCase()));
//
//                if (match) {
//                    writer.write("Коллекция: " + collectionName + " | " + doc.toJson());
//                    writer.newLine();
//                    foundCount++;
//                }
//            }
//
//            if (foundCount > 0) {
//                System.out.println("В коллекции '" + collectionName +
//                        "' найдено " + foundCount + " документов");
//            }
//
//        } catch (Exception e) {
//            System.err.println("Ошибка при поиске в коллекции " + collectionName +
//                    ": " + e.getMessage());
//        }
//    }
//
//    public void findInGraphDB(String uri, String username, String password,
//                              List<String> levels, String outputFile) throws Exception {
//
//        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
//             Session session = ((org.neo4j.driver.Driver) driver).session();
//             BufferedWriter writer = new BufferedWriter(
//                     new FileWriter(outputFile, StandardCharsets.UTF_8))) {
//
//            String cypherQuery =
//                    "MATCH (n) RETURN labels(n) AS nodeLabels, n{.*} AS properties LIMIT 1000";
//
//            Result result = session.run(cypherQuery);
//
//            while (result.hasNext()) {
//                Record record = (Record) result.next();
//
//                List<String> nodeLabels = ((org.neo4j.driver.Record) record).get("nodeLabels").asList(Value::asString);
//                Map<String, Object> properties = ((org.neo4j.driver.Record) record).get("properties").asMap();
//
//                boolean foundMatch = false;
//
//                for (Map.Entry<String, Object> entry : properties.entrySet()) {
//
//                    if (entry.getValue() instanceof String) {
//
//                        String value = ((String) entry.getValue()).toLowerCase();
//
//                        if (levels.stream().anyMatch(level ->
//                                value.contains(level.toLowerCase()))) {
//
//                            foundMatch = true;
//                            break;
//                        }
//                    }
//                }
//
//                if (foundMatch) {
//                    String formattedRecord = String.format(
//                            "Labels: %s, Properties: %s",
//                            nodeLabels, properties
//                    );
//
//                    writer.write(formattedRecord);
//                    writer.newLine();
//                }
//            }
//
//        } catch (Exception e) {
//            throw new Exception("Ошибка при работе с Neo4j: " + e.getMessage(), e);
//        }
//    }
//}