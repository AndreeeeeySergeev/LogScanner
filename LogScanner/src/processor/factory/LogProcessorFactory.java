package processor.factory;

public class LogProcessorFactory {

    public static LogProcessor getProcessor(String format) {

        return switch (format.toLowerCase()) {

            case "json" -> new JsonLogProcessor();

            case "xml" -> new XmlLogProcessor();

            case "txt", "log", "csv" -> new TextLogProcessor();

            case "bak","du", "mdf", "ibd", "dat", "dump", "sql", "db", "sqlite" -> new RelationalDbProcessor();

            case "couch", "wt", "bson" -> new MongoLogProcessor();

            case "store", "index" -> new GraphLogProcessur();

            default -> throw new IllegalArgumentException(
                    "Неподдерживаемый формат: " + format);
        };
    }
}
