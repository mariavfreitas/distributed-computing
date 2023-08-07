package pt.tecnico.distledger.namingserver.domain;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class NamingServer {

    private String serviceName;
    private HashMap<String, ServiceEntry> serverInfo = new LinkedHashMap<String, ServiceEntry>();

    public NamingServer(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

}
