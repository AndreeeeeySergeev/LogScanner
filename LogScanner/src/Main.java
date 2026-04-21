
import config.AppConfig;
import config.ConfigLoader;
import service.LogScannerService;

public class Main {

    public static void main(String[] args) {

        try {
            // 1. Путь к конфигу
            String configPath = "config/properties";

            if (args.length > 0) {
                configPath = args[0];
            }

            System.out.println("⚙️ Загрузка конфигурации: " + configPath);

            // 2. Загружаем config
            // =========================
            AppConfig config = ConfigLoader.load(configPath);

            // 3. Запуск сервиса
            LogScannerService service = new LogScannerService();

            long start = System.currentTimeMillis();

            service.processDirectory(config);

            long end = System.currentTimeMillis();

            System.out.println("⏱ Время выполнения: " + (end - start) + " ms");

        } catch (Exception e) {

            System.err.println(" Критическая ошибка приложения");
            e.printStackTrace();
        }
    }
}