package normalizer;

import model.LogEvent;

public interface LogNormalizer {
    LogEvent normalize(LogEvent event);
}