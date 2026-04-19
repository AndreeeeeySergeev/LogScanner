package config;

import java.util.List;

public class AppConfig {

    // DB
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private List<DbSource> dbSources = List.of();

    // файлы
    private String inputDir;
    private String outputFile;

    // уровни
    private List<String> levels = List.of();
    private List<String> alertLevels = List.of();


    // email
    private String smtpHost;
    private String smtpPort;
    private String emailFrom;
    private String emailPassword;
    private String emailTo;

    // telegram
    private String telegramBotToken;
    private String telegramChatId;

    // --- getters / setters ---

    public String getDbUrl() { return dbUrl; }
    public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }

    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }

    public String getDbPassword() { return dbPassword; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }

    public String getInputDir() { return inputDir; }
    public void setInputDir(String inputDir) { this.inputDir = inputDir; }

    public String getOutputFile() { return outputFile; }
    public void setOutputFile(String outputFile) { this.outputFile = outputFile; }

    public List<String> getLevels() { return levels; }
    public void setLevels(List<String> levels) { this.levels = levels; }

    public List<String> getAlertLevels() { return alertLevels; }
    public void setAlertLevels(List<String> alertLevels) { this.alertLevels = alertLevels; }

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public String getSmtpPort() { return smtpPort; }
    public void setSmtpPort(String smtpPort) { this.smtpPort = smtpPort; }

    public String getEmailFrom() { return emailFrom; }
    public void setEmailFrom(String emailFrom) { this.emailFrom = emailFrom; }

    public String getEmailPassword() { return emailPassword; }
    public void setEmailPassword(String emailPassword) { this.emailPassword = emailPassword; }

    public String getEmailTo() { return emailTo; }
    public void setEmailTo(String emailTo) { this.emailTo = emailTo; }

    public String getTelegramBotToken() { return telegramBotToken; }
    public void setTelegramBotToken(String telegramBotToken) { this.telegramBotToken = telegramBotToken; }

    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; }

    public List<DbSource> getDbSources() { return dbSources; }

    public void setDbSources(List<DbSource> dbSources) { this.dbSources = dbSources; }
}