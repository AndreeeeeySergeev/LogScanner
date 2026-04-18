import config.AppConfig;
import config.ConfigLoader;
import service.LogScannerService;

public class Main {

    public static void main(String[] args) throws Exception {

        AppConfig config = ConfigLoader.load("config.properties");

        LogScannerService service = new LogScannerService();

        service.process(
                config.getInputDir(),
                config.getOutputFile(),
                config.getLevels()
        );
    }
}