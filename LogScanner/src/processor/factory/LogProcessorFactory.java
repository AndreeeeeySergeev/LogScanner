package processor.factory;

import processor.LogProcessor;
import processor.impl.*;

import java.util.HashMap;
import java.util.Map;

public class LogProcessorFactory {

    private static final Map<String, LogProcessor> processors = new HashMap<>();

    static {
        // файловые процессоры
        processors.put("json", new JsonLogProcessor());
        processors.put("xml", new XmlLogProcessor());

        LogProcessor textProcessor = new TextLogProcessor();
        processors.put("txt", textProcessor);
        processors.put("log", textProcessor);
        processors.put("csv", textProcessor);

        // реляционные БД
        LogProcessor relational = new RelationalDbProcessor();
        processors.put("sql", relational);
        processors.put("dump", relational);
        processors.put("dat", relational);
        processors.put("bak", relational);
        processors.put("du", relational);
        processors.put("mdf", relational);
        processors.put("ibd", relational);
        processors.put("db", relational);
        processors.put("sqlite", relational);

        // NoSQL
        LogProcessor nosql = new NoSqlDBProcessor();
        processors.put("wt", nosql);
        processors.put("couch", nosql);
        processors.put("bson", nosql);

        // Graph
        LogProcessor graph = new GraphLogProcessor();
        processors.put("store", graph);
        processors.put("index", graph);
    }

    public static LogProcessor getProcessor(String format) {

        LogProcessor processor = processors.get(format.toLowerCase());

        if (processor == null) {
            throw new IllegalArgumentException(
                    "Неподдерживаемый формат: " + format);
        }

        return processor;
    }
}