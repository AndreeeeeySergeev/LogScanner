package processor.impl;

import processor.LogProcessor;

import com.mongodb.client.*;
import org.bson.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class NoSqlDBProcessor implements LogProcessor {

    @Override
    public void process(String connectionString,
                        String fileOutput,
                        List<String> levels) throws Exception {

        //нормализуем уровни один раз
        List<String> normalizedLevels = levels.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        try (MongoClient mongoClient = MongoClients.create(connectionString);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(fileOutput), StandardCharsets.UTF_8))) {

            MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();

            for (String dbName : databaseNames) {

                System.out.println("Обработка базы: " + dbName);

                MongoDatabase database = mongoClient.getDatabase(dbName);

                for (String collectionName : database.listCollectionNames()) {

                    System.out.println("  Коллекция: " + collectionName);

                    searchCollectionForLevels(database,
                            collectionName,
                            writer,
                            normalizedLevels);
                }
            }

        } catch (Exception e) {
            throw new Exception("Ошибка при работе с MongoDB: " + e.getMessage(), e);
        }
    }

    private void searchCollectionForLevels(MongoDatabase database,
                                           String collectionName,
                                           BufferedWriter writer,
                                           List<String> levels) throws IOException {

        MongoCollection<Document> collection = database.getCollection(collectionName);

        try (MongoCursor<Document> cursor = collection.find().limit(1000).iterator()) {

            int foundCount = 0;

            while (cursor.hasNext()) {

                Document doc = cursor.next();

                String jsonLower = doc.toJson().toLowerCase();

                boolean match = levels.stream()
                        .anyMatch(jsonLower::contains);

                if (match) {
                    writer.write("Коллекция: " + collectionName + " | " + doc.toJson());
                    writer.newLine();
                    foundCount++;
                }
            }

            if (foundCount > 0) {
                System.out.println("В коллекции '" + collectionName +
                        "' найдено " + foundCount + " документов");
            }

        } catch (Exception e) {
            System.err.println("Ошибка при поиске в коллекции "
                    + collectionName + ": " + e.getMessage());
        }
    }
}