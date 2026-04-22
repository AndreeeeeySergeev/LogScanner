package processor.impl;

import com.mongodb.client.*;
import config.MongoSource;
import model.LogEvent;
import org.bson.Document;
import processor.LogProcessor;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.*;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public class NoSqlDBProcessor implements LogProcessor {
    private final List<String> levels;

    public NoSqlDBProcessor(List<String> levels) {
        this.levels = levels;
    }

    private Bson buildFilter() {

        if (levels == null || levels.isEmpty()) {
            return new Document();
        }

        String pattern = String.join("|", levels);

        // $expr + $regexMatch по всему JSON (через toString)
        return expr(new Document("$regexMatch",
                new Document("input", new Document("$toString", "$$ROOT"))
                        .append("regex", pattern)
                        .append("options", "i")
        ));
    }

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
                     collection.find(buildFilter()).limit(1000).iterator()) {

            while (cursor.hasNext()) {

                Document doc = cursor.next();

                String json = doc.toJson();

                if (json == null || json.isBlank()) continue;

                consumer.accept(new LogEvent(
                        Instant.now(),
                        "MONGO",
                        null,
                        json
                ));
            }

        } catch (Exception e) {
            System.err.println(" Ошибка коллекции: " + collectionName);
            e.printStackTrace();
        }
    }
}