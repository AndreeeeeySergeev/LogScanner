package alert.impl;

import alert.AlertService;
import model.LogEvent;

import java.util.Set;
import java.util.stream.Collectors;

public class SimpleAlertService implements AlertService {

    private final Set<String> alertLevels;

    public SimpleAlertService(Iterable<String> alertLevels) {
        this.alertLevels = toUpperSet(alertLevels);
    }

    @Override
    public void process(LogEvent event) {

        if (event == null) return;

        String level = event.getLevel();
        if (level == null) return;

        if (alertLevels.contains(level.toUpperCase())) {
            sendAlert(event);
        }
    }

    private void sendAlert(LogEvent event) {

        System.out.println("🚨 ALERT!");
        System.out.println("Time: " + event.getTimestamp());
        System.out.println("Source: " + event.getSource());
        System.out.println("Level: " + event.getLevel());
        System.out.println("Message: " + event.getMessage());
        System.out.println("------------------------------------------------");
    }

    private Set<String> toUpperSet(Iterable<String> levels) {
        return levels == null ? Set.of() :
                ((java.util.Collection<String>) levels).stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.toSet());
    }
}