package normalizer;

import normalizer.source.SourceParser;
import normalizer.source.KeywordSourceParser;

import java.util.List;
import java.util.Map;

public class SourceDetector {

    private static final Map<String, List<String>> DEFAULT_SOURCES = Map.of(
            "SIEM", List.of("splunk", "qradar", "arcSight"),
            "EDR", List.of("crowdstrike", "sentinelone"),
            "FIREWALL", List.of("iptables", "firewall", "paloalto"),
            "WAF", List.of("modsecurity", "cloudflare"),
            "DATABASE", List.of("postgres", "mysql", "db", "database")
    );

    private final List<SourceParser> parsers;


    public SourceDetector() {
        this(DEFAULT_SOURCES);
    }

    // конструктор с кастомом (на будущее)
    public SourceDetector(Map<String, List<String>> sources) {
        this.parsers = List.of(
                new KeywordSourceParser(sources)
        );
    }

    public String detect(String message) {

        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }

        for (SourceParser parser : parsers) {

            String source = parser.parse(message);

            if (source != null) {
                return source;
            }
        }

        return "UNKNOWN";
    }
}