package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerServiceGrpc;
import java.util.List;

public class AdminService {

    private static boolean debug = false;
    private static String serviceName = "DistLedger";
    private static String host = "localhost";
    private static int myPort = 5001;
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stubNamingServer;
    private ManagedChannel channelNamingServer;

    AdminServiceGrpc.AdminServiceBlockingStub stub;
    ManagedChannel channel;

    public AdminService(boolean debug) {
        this.debug = debug;
    }

    public AdminServiceGrpc.AdminServiceBlockingStub createStub(String qualifier) {
        try {
            final String target = host + ":" + myPort;
            channelNamingServer = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            stubNamingServer = NamingServerServiceGrpc.newBlockingStub(channelNamingServer);
            List<String> response = stubNamingServer.lookup(NamingServerDistLedger.LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build()).getServiceServersList();
            channel = ManagedChannelBuilder.forTarget(response.get(0)).usePlaintext().build();
            stub = AdminServiceGrpc.newBlockingStub(channel);
            return stub;
        }
        catch (Exception e) {
            if (debug)
                System.err.println("Server " + qualifier + " not found\n");
            return null;
        }
    }

    public void activate(String qualifier) {
        try {
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            stub.activate(AdminDistLedger.ActivateRequest.getDefaultInstance());
            System.out.println("OK\n");
            if (debug)
                System.err.println("Server activated successfully");
        }
        catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    public void deactivate(String qualifier) {
        try {
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            stub.deactivate(AdminDistLedger.DeactivateRequest.getDefaultInstance());
            System.out.println("OK\n");
            if (debug)
                System.err.println("Server deactivated successfully");
        }
        catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    public void getLedgerState(String qualifier) {
        AdminDistLedger.getLedgerStateResponse response;
        try {
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            response = stub.getLedgerState(AdminDistLedger.getLedgerStateRequest.getDefaultInstance());
            System.out.println("OK\n" + response);
            if (debug)
                System.err.println("LedgerState printed successfully");
        }
        catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void gossip(String qualifier) {
        try {
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            stub.gossip(AdminDistLedger.GossipRequest.getDefaultInstance());
            System.out.println("OK\n");
            if (debug)
                System.err.println("Gossip forced successfully");
        }
        catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    public void shutdown(){
        this.channel.shutdown();
    }
}
