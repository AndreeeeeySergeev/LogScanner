package normalizer.timestamp;

public interface TimestampParser {

    TimestampParseResult parse(String message);
}
