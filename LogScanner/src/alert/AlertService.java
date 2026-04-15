package alert;

import model.LogEvent;

public interface AlertService {

    void process(LogEvent event);
}