package normalizer;

import java.util.List;
import java.util.Map;

public class SourceDetector {

    private static final Map<String, List<String>> SOURCES = Map.of(
            "SIEM", List.of("splunk", "qradar"),
            "EDR", List.of("crowdstrike"),
            "FIREWALL", List.of("iptables"),
            "WAF", List.of("modsecurity")
    );

    public static String detect(String message) {

        if (message == null) return "UNKNOWN";

        String lower = message.toLowerCase();

        for (Map.Entry<String, List<String>> entry : SOURCES.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return "UNKNOWN";
    }
}
