package config;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigLoader {

    public static AppConfig load(String path) {

        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки config", e);
        }

        AppConfig config = new AppConfig();


        // DATABASE
        // MONGO
        String mongoRaw = props.getProperty("mongo.sources");

        if (mongoRaw != null && !mongoRaw.isBlank()) {

            List<MongoSource> mongoSources = Arrays.stream(mongoRaw.split(","))
                    .map(String::trim)
                    .filter(name -> props.getProperty("mongo." + name + ".uri") != null)
                    .map(name -> {

                        String uri = props.getProperty("mongo." + name + ".uri");

                        return new MongoSource(name, uri);
                    })
                    .collect(Collectors.toList());

            config.setMongoSources(mongoSources);
        }

        // DB
        String sourcesRaw = props.getProperty("db.sources");

        if (sourcesRaw != null && !sourcesRaw.isBlank()) {

            List<DbSource> dbSources = Arrays.stream(sourcesRaw.split(","))
                    .map(String::trim)
                    .filter(name -> props.getProperty("db." + name + ".url") != null)
                    .map(name -> {
                        String url = props.getProperty("db." + name + ".url");
                        String user = props.getProperty("db." + name + ".user");
                        String pass = props.getProperty("db." + name + ".password");

                        return new DbSource(name, url, user, pass);
                    })
                    .collect(Collectors.toList());

            config.setDbSources(dbSources);
        }

        // GRAPH
        String graphRaw = props.getProperty("graph.sources");

        if (graphRaw != null && !graphRaw.isBlank()) {

            List<GraphSource> graphSources = Arrays.stream(graphRaw.split(","))
                    .map(String::trim)
                    .filter(name -> props.getProperty("graph." + name + ".uri") != null)
                    .map(name -> {

                        String uri = props.getProperty("graph." + name + ".uri");
                        String user = props.getProperty("graph." + name + ".user");
                        String pass = props.getProperty("graph." + name + ".password");

                        return new GraphSource(name, uri, user, pass);
                    })
                    .collect(Collectors.toList());

            config.setGraphSources(graphSources);
        }

        config.setDbUrl(props.getProperty("db.url"));
        config.setDbUser(props.getProperty("db.user"));
        config.setDbPassword(props.getProperty("db.password"));


        // LEVELS (с дефолтом)
        String levelsRaw = props.getProperty(
                "levels",
                "CRITICAL,ERROR,WARNING,WARN,FATAL,ALERT,EMERGENCY"
        );

        List<String> levels = parse(levelsRaw);
        config.setLevels(levels);

        // ALERT LEVELS
        String alertLevelsRaw = props.getProperty(
                "alertLevels",
                "ERROR,CRITICAL,FATAL,ALERT,EMERGENCY"
        );

        List<String> alertLevels = parse(alertLevelsRaw);
        config.setAlertLevels(alertLevels);

        // PATHS
        config.setInputDir(props.getProperty("inputDir", "input"));
        config.setOutputFile(props.getProperty("outputFile", "output/result.log"));

        // EMAIL
        config.setSmtpHost(props.getProperty("smtpHost", "smtp.gmail.com"));
        config.setSmtpPort(props.getProperty("smtpPort", "587"));
        config.setEmailFrom(props.getProperty("emailFrom"));
        config.setEmailPassword(props.getProperty("emailPassword"));
        config.setEmailTo(props.getProperty("emailTo"));

        // TELEGRAM
        config.setTelegramBotToken(props.getProperty("telegramBotToken"));
        config.setTelegramChatId(props.getProperty("telegramChatId"));

        return config;
    }

    private static List<String> parse(String raw) {

        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())   //ВАЖНО
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }
}