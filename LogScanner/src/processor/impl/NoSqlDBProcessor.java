package processor.impl;

import com.mongodb.client.*;
import model.LogEvent;
import org.bson.Document;
import processor.LogProcessor;

import java.time.Instant;
import java.util.function.Consumer;

public class NoSqlDBProcessor implements LogProcessor {

    @Override
    public void process(String connectionString,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {

            System.out.println("Подключение к MongoDB");

            MongoIterable<String> databaseNames =
                    mongoClient.listDatabaseNames();

            for (String dbName : databaseNames) {

                MongoDatabase database =
                        mongoClient.getDatabase(dbName);

                for (String collectionName : database.listCollectionNames()) {

                    processCollection(database, collectionName, consumer);
                }
            }
        }
    }

    // Коллекция
    private void processCollection(MongoDatabase database,
                                   String collectionName,
                                   Consumer<LogEvent> consumer) {

        MongoCollection<Document> collection =
                database.getCollection(collectionName);

        try (MongoCursor<Document> cursor =
                     collection.find().limit(1000).iterator()) {

            while (cursor.hasNext()) {

                Document doc = cursor.next();

                String json = doc.toJson();

                if (json == null || json.isBlank()) continue;

                consumer.accept(new LogEvent(
                        Instant.now(),
                        "MONGO",
                        "UNKNOWN",
                        json
                ));
            }

        } catch (Exception e) {
            System.err.println(" Ошибка коллекции: " + collectionName);
            e.printStackTrace();
        }
    }
}