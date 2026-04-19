package service;

import alert.AlertService;
import alert.impl.EmailAlertService;
import alert.impl.MultiAlertService;
import alert.impl.SimpleAlertService;
import alert.impl.TelegramAlertService;
import config.AppConfig;
import model.LogEvent;
import normalizer.LogNormalizer;
import normalizer.impl.SimpleLogNormalizer;
import processor.LogProcessor;
import processor.factory.LogProcessorFactory;
import util.EncodingDetector;
import util.FileScanner;
import util.FileUtils;
import writer.impl.FileLogWriter;

import java.util.ArrayList;
import java.util.List;

public class LogScannerService {

    public void processDirectory(AppConfig config) throws Exception {

        // 📂 1. Сканируем файлы
        List<String> files = FileScanner.scan(config.getInputDir());

        // 🧠 2. Normalizer
        LogNormalizer normalizer =
                new SimpleLogNormalizer(config.getLevels());

        // 🚨 3. AlertService (сборка через config)
        AlertService alertService = buildAlertService(config);

        // 📝 4. Writer
        FileLogWriter writer =
                new FileLogWriter(config.getOutputFile());

        try {
            for (String filePath : files) {

                System.out.println("📄 Обработка файла: " + filePath);

                try {
                    // 🔍 5. Кодировка
                    String encoding =
                            EncodingDetector.detectEncoding(filePath);

                    // 📄 6. Формат
                    String format =
                            FileUtils.detectFormat(filePath);

                    // 🧩 7. Processor
                    LogProcessor processor =
                            LogProcessorFactory.getProcessor(format);

                    // 🚀 8. Стриминг
                    processor.process(filePath, encoding, event -> {

                        try {
                            // 🧠 нормализация
                            LogEvent normalized =
                                    normalizer.normalize(event);

                            if (normalized == null) return;

                            // 🚨 alert
                            alertService.process(normalized);

                            // 📝 запись
                            writer.write(normalized);

                        } catch (Exception e) {
                            System.err.println("❌ Ошибка обработки события:");
                            System.err.println("Файл: " + filePath);
                            System.err.println("Сообщение: " + event.getMessage());
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    System.err.println("❌ Ошибка обработки файла: " + filePath);
                    e.printStackTrace();
                }
            }

        } finally {
            writer.close();
        }

        System.out.println("✅ Обработка завершена");
    }

    // =========================
    // 🔧 Сборка AlertService
    // =========================
    private AlertService buildAlertService(AppConfig config) {

        List<AlertService> services = new ArrayList<>();

        // 📟 Console alert (всегда)
        services.add(new SimpleAlertService(config.getAlertLevels()));

        // 📧 Email (если настроен)
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

        // 🤖 Telegram (если настроен)
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