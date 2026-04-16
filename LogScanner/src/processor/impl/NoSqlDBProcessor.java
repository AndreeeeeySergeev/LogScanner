package processor.impl;

import com.mongodb.client.*;
import model.LogEvent;
import org.bson.Document;
import processor.LogProcessor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NoSqlDBProcessor implements LogProcessor {

    @Override
    public List<LogEvent> process(String connectionString, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {

            MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();

            for (String dbName : databaseNames) {

                MongoDatabase database = mongoClient.getDatabase(dbName);

                List<String> collections =
                        database.listCollectionNames().into(new ArrayList<>());

                for (String collectionName : collections) {

                    searchCollection(database, collectionName, levels, events);
                }
            }

        }

        return events;
    }

    private void searchCollection(MongoDatabase database,
                                  String collectionName,
                                  List<String> levels,
                                  List<LogEvent> events) {

        MongoCollection<Document> collection =
                database.getCollection(collectionName);

        try (MongoCursor<Document> cursor =
                     collection.find().limit(1000).iterator()) {

            while (cursor.hasNext()) {

                Document doc = cursor.next();

                String json = doc.toJson();
                String lower = json.toLowerCase();

                boolean match = levels.stream()
                        .anyMatch(level -> lower.contains(level.toLowerCase()));

                if (match) {

                    events.add(new LogEvent(
                            Instant.now(),
                            "MONGO",
                            "UNKNOWN",
                            json
                    ));
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка в коллекции " + collectionName + ": " + e.getMessage());
        }
    }
}