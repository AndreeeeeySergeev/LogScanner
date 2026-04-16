import service.LogScannerService;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        try {

            LogScannerService service = new LogScannerService();

            List<String> levels = Arrays.asList(
                    "error",
                    "warn",
                    "critical"
            );

            service.processDirectory(
                    "test-data",          // входной файл
                    "output/output.log",        // выходной файл
                    levels
            );

            System.out.println("✅ Обработка завершена");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}