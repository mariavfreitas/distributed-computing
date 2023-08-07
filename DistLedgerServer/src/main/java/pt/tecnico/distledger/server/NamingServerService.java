package pt.tecnico.distledger.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerServiceGrpc;
import java.util.Collections;
import java.util.List;

public class NamingServerService {

    private static ManagedChannel channel;
    private static NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private static int namingServerPort = 5001;
    private static int port;
    private static String host;
    private static String serviceName;
    private static String qualifier;

    public NamingServerService(int port, String host, String serviceName, String qualifier) {
        this.port = port;
        this.host = host;
        this.serviceName = serviceName;
        this.qualifier = qualifier;
        final String target = host + ":" + namingServerPort;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public static void register() {
        try {
            NamingServerDistLedger.RegisterRequest request = NamingServerDistLedger.RegisterRequest.newBuilder()
                    .setPort(port).setHost(host).setServiceName(serviceName).setQualifier(qualifier).build();
            stub.register(request);
        }
        catch (StatusRuntimeException e) {
            System.out.println("Not possible to register the server\n");
        }
    }

    public static void delete() {
        try {
            NamingServerDistLedger.DeleteRequest request = NamingServerDistLedger.DeleteRequest.newBuilder().setPort(port).setHost(host).setServiceName(serviceName).build();
            stub.delete(request);
        }
        catch (StatusRuntimeException e){
            System.out.println("Not possible to remove the server");
        }
    }

    public static List<String> lookup(String serviceName, String qualifier){

        NamingServerDistLedger.LookupRequest request = NamingServerDistLedger.LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build();
        NamingServerDistLedger.LookupResponse response = stub.lookup(request);
        if (!response.getServiceServersList().isEmpty())
            return response.getServiceServersList();
        else
            return Collections.emptyList();
    }
}
