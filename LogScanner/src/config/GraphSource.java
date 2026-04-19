package config;

public class GraphSource {

    private String name;
    private String uri;
    private String user;
    private String password;

    public GraphSource(String name, String uri, String user, String password) {
        this.name = name;
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    public String getName() { return name; }
    public String getUri() { return uri; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
}