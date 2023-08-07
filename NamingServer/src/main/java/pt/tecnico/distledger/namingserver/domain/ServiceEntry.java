package pt.tecnico.distledger.namingserver.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ServiceEntry {
    private String serviceName;
    private HashMap<String, ServerEntry> serverEntries = new LinkedHashMap<String, ServerEntry>();

    public ServiceEntry(String serviceName) {
        this.serviceName = serviceName;
    }

    public void addServerEntry(ServerEntry serverEntry){
        serverEntries.put(serverEntry.getHostPort(), serverEntry);
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<ServerEntry> getServerEntries() {
        return new ArrayList<ServerEntry>(serverEntries.values());
    }

    public void removeServerEntry(String serverHostPort) {
        serverEntries.remove(serverHostPort);
    }

}

