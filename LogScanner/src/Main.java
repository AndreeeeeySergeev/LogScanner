import config.AppConfig;
import config.ConfigLoader;
import service.LogScannerService;

public class Main {

    public static void main(String[] args) {

        try {
            // 1. Загружаем конфиг
            AppConfig config = ConfigLoader.load("config/properties");

            // 2. Запускаем сервис
            LogScannerService service = new LogScannerService();

            service.processDirectory(
                    config.getInputDir(),
                    config.getOutputFile(),
                    config.getLevels()
            );

            System.out.println("Обработка завершена");

        } catch (Exception e) {
            System.err.println("Критическая ошибка:");
            e.printStackTrace();
        }
    }
}