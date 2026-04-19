package service;

import alert.AlertService;
import alert.impl.EmailAlertService;
import alert.impl.MultiAlertService;
import alert.impl.SimpleAlertService;
import alert.impl.TelegramAlertService;
import config.AppConfig;
import config.DbSource;
import config.MongoSource;
import model.LogEvent;
import normalizer.LogNormalizer;
import normalizer.impl.SimpleLogNormalizer;
import processor.LogProcessor;
import processor.factory.LogProcessorFactory;
import processor.impl.NoSqlDBProcessor;
import processor.impl.RelationalDbProcessor;
import util.EncodingDetector;
import util.FileScanner;
import util.FileUtils;
import writer.impl.FileLogWriter;

import java.util.ArrayList;
import java.util.List;

public class LogScannerService {

    public void processDirectory(AppConfig config) throws Exception {

        // 1. Сканируем файлы
        List<String> files = FileScanner.scan(config.getInputDir());

        // 2. Normalizer
        LogNormalizer normalizer =
                new SimpleLogNormalizer(config.getLevels());

        // 3. AlertService
        AlertService alertService = buildAlertService(config);

        // 4. Writer
        FileLogWriter writer =
                new FileLogWriter(config.getOutputFile());

        try {

            // ФАЙЛЫ
            for (String filePath : files) {

                System.out.println("📄 Обработка файла: " + filePath);

                try {
                    String encoding =
                            EncodingDetector.detectEncoding(filePath);

                    String format =
                            FileUtils.detectFormat(filePath);

                    LogProcessor processor =
                            LogProcessorFactory.getProcessor(format, config);

                    processor.process(filePath, encoding, event -> {

                        try {
                            LogEvent normalized =
                                    normalizer.normalize(event);

                            if (normalized == null) return;

                            alertService.process(normalized);
                            writer.write(normalized);

                        } catch (Exception e) {
                            System.err.println(" Ошибка события:");
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    System.err.println(" Ошибка файла: " + filePath);
                    e.printStackTrace();
                }
            }

            //  DATABASES
            for (DbSource db : config.getDbSources()) {

                System.out.println("🗄 Подключение к БД: " + db.getName());

                try {
                    LogProcessor processor =
                            new RelationalDbProcessor(
                                    db.getUser(),
                                    db.getPassword()
                            );

                    processor.process(db.getUrl(), null, event -> {

                        try {
                            LogEvent normalized =
                                    normalizer.normalize(event);

                            if (normalized == null) return;

                            alertService.process(normalized);
                            writer.write(normalized);

                        } catch (Exception e) {
                            System.err.println("Ошибка события (DB):");
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    System.err.println(" Ошибка подключения к БД: " + db.getName());
                    e.printStackTrace();
                }
            }

            try {

                //  MONGO
                for (MongoSource mongo : config.getMongoSources()) {

                    System.out.println("Подключение к Mongo: " + mongo.getName());

                    try {
                        LogProcessor processor = new NoSqlDBProcessor();

                        processor.process(mongo.getUri(), null, event -> {

                            try {
                                LogEvent normalized = normalizer.normalize(event);

                                if (normalized == null) return;

                                alertService.process(normalized);
                                writer.write(normalized);

                            } catch (Exception e) {
                                System.err.println("❌ Ошибка события (Mongo):");
                                e.printStackTrace();
                            }
                        });

                    } catch (Exception e) {
                        System.err.println("❌ Ошибка подключения к Mongo: " + mongo.getName());
                        e.printStackTrace();
                    }
                }

            } finally {
                writer.close();
            }

        } finally {
            writer.close();
        }

        System.out.println("✅ Обработка завершена");
    }

    // Alert builder
    private AlertService buildAlertService(AppConfig config) {

        List<AlertService> services = new ArrayList<>();

        services.add(new SimpleAlertService(config.getAlertLevels()));

        if (config.getEmailFrom() != null &&
                config.getEmailPassword() != null &&
                config.getEmailTo() != null) {

            services.add(new EmailAlertService(
                    config.getSmtpHost(),
                    config.getSmtpPort(),
                    config.getEmailFrom(),
                    config.getEmailPassword(),
                    config.getEmailTo()
            ));
        }

        if (config.getTelegramBotToken() != null &&
                config.getTelegramChatId() != null) {

            services.add(new TelegramAlertService(
                    config.getTelegramBotToken(),
                    config.getTelegramChatId()
            ));
        }

        return new MultiAlertService(services);
    }
}