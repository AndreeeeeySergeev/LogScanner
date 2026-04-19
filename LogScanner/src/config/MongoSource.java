package config;

public class MongoSource {

    private String name;
    private String uri;

    public MongoSource(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() { return name; }
    public String getUri() { return uri; }
}