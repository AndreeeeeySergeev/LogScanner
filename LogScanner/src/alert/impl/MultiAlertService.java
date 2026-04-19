package alert.impl;

import alert.AlertService;
import model.LogEvent;

import java.util.List;

public class MultiAlertService implements AlertService {

    private final List<AlertService> services;

    public MultiAlertService(List<AlertService> services) {
        this.services = services;
    }

    @Override
    public void process(LogEvent event) {

        if (event == null) return;

        for (AlertService service : services) {
            service.process(event);
        }
    }
}