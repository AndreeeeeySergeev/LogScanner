package processor.factory;

import config.AppConfig;
import processor.LogProcessor;
import processor.impl.*;

public class LogProcessorFactory {

    public static LogProcessor getProcessor(String format, AppConfig config) {

        switch (format.toLowerCase()) {

            // TEXT
            case "txt":
            case "log":
            case "csv":
                return new TextLogProcessor();


            // JSON
            case "json":
                return new JsonLogProcessor();

            // XML
            case "xml":
                return new XmlLogProcessor();

            // RELATIONAL DB
            case "sql":
            case "dump":
            case "dat":
            case "bak":
            case "du":
            case "mdf":
            case "ibd":
            case "db":
            case "sqlite":
                return buildDbProcessor(config);

            // NoSQL
            case "wt":
            case "couch":
            case "bson":
                return new NoSqlDBProcessor();

            // Graph
            case "store":
            case "index":
                return new GraphLogProcessor();

            default:
                throw new IllegalArgumentException(
                        "Неподдерживаемый формат: " + format);
        }
    }


    // DB builder
    private static LogProcessor buildDbProcessor(AppConfig config) {

        if (config.getDbUrl() == null || config.getDbUrl().isBlank()) {
            throw new RuntimeException("db.url не задан в config");
        }

        return new RelationalDbProcessor(
                config.getDbUser(),
                config.getDbPassword()
        );
    }
}