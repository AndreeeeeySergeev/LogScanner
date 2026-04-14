package util;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EncodingDetector {

    public static String detectEncoding(String filePath) throws IOException {

        UniversalDetector detector = new UniversalDetector(null);

        try (InputStream inputStream = new FileInputStream(filePath)) {

            byte[] buffer = new byte[4096];
            int nread;

            while ((nread = inputStream.read(buffer)) > 0 && !detector.isDone()) {
                detector.handleData(buffer, 0, nread);
            }
        }

        detector.dataEnd();

        String encoding = detector.getDetectedCharset();
        detector.reset();

        return encoding != null ? encoding : "UTF-8";
    }
}
