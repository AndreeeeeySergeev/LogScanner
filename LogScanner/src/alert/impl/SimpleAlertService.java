package alert.impl;

import alert.AlertService;
import model.LogEvent;

import java.util.Set;

public class SimpleAlertService implements AlertService {

    private static final Set<String> ALERT_LEVELS = Set.of(
            "ERROR",
            "CRITICAL",
            "FATAL",
            "ALERT",
            "EMERGENCY"
    );

    @Override
    public void process(LogEvent event) {

        String level = event.getLevel();

        if (level == null) return;

        if (ALERT_LEVELS.contains(level.toUpperCase())) {

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
}