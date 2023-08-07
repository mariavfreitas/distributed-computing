package pt.tecnico.distledger.namingserver.domain;

public class ServerEntry {
    private String host;
    private int port;
    private String qualifier;

    public ServerEntry(String host, int port, String qualifier) {
        this.host = host;
        this.port = port;
        this.qualifier = qualifier;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getHostPort() {
        return getHost() + ":" + getPort();
    }

    public String getQualifier() {
        return qualifier;
    }

    public boolean checkQualifier(String qualifier){
        if (qualifier.isEmpty())
            return true;
        return qualifier.equals(getQualifier());
    }

    public static String getHostPort(String host, int port) {
        return host + ":" + port;
    }
}
