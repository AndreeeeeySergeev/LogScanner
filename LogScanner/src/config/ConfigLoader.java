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