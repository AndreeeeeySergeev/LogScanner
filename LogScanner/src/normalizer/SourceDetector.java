package normalizer;

import model.LogEvent;
import normalizer.source.SourceParser;
import normalizer.source.KeywordSourceParser;

import java.util.List;
import java.util.Map;

public class SourceDetector {

    private static final Map<String, List<String>> DEFAULT_SOURCES = Map.of(
            "SIEM", List.of("splunk", "qradar", "arcsight"),
            "EDR", List.of("crowdstrike", "sentinelone"),
            "FIREWALL", List.of("iptables", "firewall", "paloalto"),
            "WAF", List.of("modsecurity", "cloudflare"),
            "DATABASE", List.of("postgres", "mysql", "db", "database")
    );

    private final List<SourceParser> parsers;

    public SourceDetector() {
        this(DEFAULT_SOURCES);
    }

    public SourceDetector(Map<String, List<String>> sources) {
        this.parsers = List.of(
                new KeywordSourceParser(sources)
        );
    }

    public String detect(LogEvent event) {

        if (event == null) return "UNKNOWN";

        // MESSAGE
        String message = event.getMessage();

        String detected = tryParse(message);
        if (detected != null) return detected;


        // SOURCE (например DB/MONGO/GRAPH)
        String source = event.getSource();
        if (source != null) {
            detected = tryParse(source);
            if (detected != null) return detected;
        }

        return "UNKNOWN";
    }

    private String tryParse(String text) {

        if (text == null || text.isBlank()) return null;

        for (SourceParser parser : parsers) {

            String source = parser.parse(text.toLowerCase());

            if (source != null) {
                return source;
            }
        }

        return null;
    }
}