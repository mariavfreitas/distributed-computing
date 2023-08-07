package pt.tecnico.distledger.namingserver;

import pt.tecnico.distledger.namingserver.domain.ServiceEntry;
import pt.tecnico.distledger.namingserver.domain.ServerEntry;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerDistLedger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    public HashMap<String, ServiceEntry> namingServices;

    public NamingServerServiceImpl() {
        namingServices = new HashMap<>();
    }

    private synchronized void addService(ServiceEntry serviceEntry) {
        namingServices.put(serviceEntry.getServiceName(), serviceEntry);
    }

    private synchronized void addServerEntry(String host, int port, String serviceName, String qualifier) {
        ServerEntry serverEntry = new ServerEntry(host, port, qualifier);
        if (namingServices.get(serviceName) == null)
            addService(new ServiceEntry(serviceName));
        namingServices.get(serviceName).addServerEntry(serverEntry);
    }

    private synchronized void removeServerEntry(String serviceName, String host, int port) {
        ServiceEntry serviceEntry = namingServices.get(serviceName);
        if (serviceEntry != null) {
            serviceEntry.removeServerEntry(ServerEntry.getHostPort(host, port));
            if (serviceEntry.getServerEntries().isEmpty())
                namingServices.remove(serviceName);
        }
    }

    private synchronized List<String> getNamingServices(String serviceName, String qualifier) {
        ServiceEntry serviceEntry = namingServices.get(serviceName);
        if (serviceEntry == null)
            return Collections.emptyList();
        List<ServerEntry> serverEntries = serviceEntry.getServerEntries();
        List<String> hostPort = serverEntries.stream()
            .filter(serverEntry -> serverEntry.checkQualifier(qualifier))
            .map(serverEntry -> serverEntry.getHostPort())
            .collect(Collectors.toList());
        return hostPort;
    }

    @Override
    public void register(NamingServerDistLedger.RegisterRequest request, StreamObserver<NamingServerDistLedger.RegisterResponse> responseObserver) {

        addServerEntry(request.getHost(), request.getPort(), request.getServiceName(), request.getQualifier());
        responseObserver.onNext(NamingServerDistLedger.RegisterResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(NamingServerDistLedger.DeleteRequest request, StreamObserver<NamingServerDistLedger.DeleteResponse> responseObserver) {

        removeServerEntry(request.getServiceName(), request.getHost(), request.getPort());

        responseObserver.onNext(NamingServerDistLedger.DeleteResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void lookup(NamingServerDistLedger.LookupRequest request, StreamObserver<NamingServerDistLedger.LookupResponse> responseObserver) {

        List<String> hostPort = getNamingServices(request.getServiceName(), request.getQualifier());

        responseObserver.onNext(NamingServerDistLedger.LookupResponse.newBuilder().addAllServiceServers(hostPort).build());
        responseObserver.onCompleted();
    }

}
