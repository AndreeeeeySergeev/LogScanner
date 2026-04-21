package processor.factory;

import config.AppConfig;
import processor.LogProcessor;
import processor.impl.*;
import processor.filter.FilteredLogProcessor;
import processor.filter.LevelMatcher;

public class LogProcessorFactory {

    public static LogProcessor getProcessor(String format, AppConfig config) {

        LogProcessor base;

        switch (format.toLowerCase()) {

            // TEXT
            case "txt":
            case "log":
            case "csv":
                base = new TextLogProcessor();
                break;


            // JSON
            case "json":
                base = new JsonLogProcessor(config.getLevels());
                break;

            // XML
            case "xml":
                base = new XmlLogProcessor(config.getLevels());
                break;

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
                base =  buildDbProcessor(config);
                break;

            // NoSQL
            case "wt":
            case "couch":
            case "bson":
                base =  buildMongoProcessor(config);
                break;

            // Graph
            case "store":
            case "index":
                base = buildGraphProcessor(config);
                break;

            default:
                throw new IllegalArgumentException(
                        "Неподдерживаемый формат: " + format);
        }
        return new FilteredLogProcessor(
                base,
                new LevelMatcher(config.getLevels())
        );
    }


    // DB builder
    private static LogProcessor buildDbProcessor(AppConfig config) {

        if (config.getDbUrl() == null || config.getDbUrl().isBlank()) {
            throw new RuntimeException("db.url не задан в config");
        }

        return new RelationalDbProcessor(
                config.getDbUser(),
                config.getDbPassword(),
                config.getLevels()
        );
    }

    private static LogProcessor buildGraphProcessor(AppConfig config) {

        if (config.getGraphSources() == null || config.getGraphSources().isEmpty()) {
            throw new RuntimeException("graph.sources не задан");
        }

        return new CompositeGraphProcessor(config.getGraphSources(), config.getLevels());
    }

    private static LogProcessor buildMongoProcessor(AppConfig config) {

        if (config.getMongoSources() == null || config.getMongoSources().isEmpty()) {
            throw new RuntimeException("mongo.sources не заданы");
        }

        return new CompositeNoSqlProcessor(config.getMongoSources(), config.getLevels());
    }
}