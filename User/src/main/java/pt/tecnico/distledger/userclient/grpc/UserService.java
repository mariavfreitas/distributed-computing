package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import java.util.List;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.naming.NamingServerDistLedger;

public class UserService {

    private static boolean debug = false;
    private static String serviceName = "DistLedger";
    private static String host = "localhost";
    private static int myPort = 5001;
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stubNamingServer;
    private ManagedChannel channelNamingServer;

    UserServiceGrpc.UserServiceBlockingStub stub;
    ManagedChannel channel;

    public UserService(boolean debug) {
        this.debug = debug;
    }

    public UserServiceGrpc.UserServiceBlockingStub createStub(String qualifier) {
        try {
            final String target = host + ":" + myPort;
            channelNamingServer = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            stubNamingServer = NamingServerServiceGrpc.newBlockingStub(channelNamingServer);
            List<String> response = stubNamingServer.lookup(NamingServerDistLedger.LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build()).getServiceServersList();
            channel = ManagedChannelBuilder.forTarget(response.get(0)).usePlaintext().build();
            stub = UserServiceGrpc.newBlockingStub(channel);
            return stub;
        }
        catch (Exception e) {
            if (debug)
                System.err.println("Server " + qualifier + " not found\n");
            return null;
        }
    }

    public void createAccount(String userId, String qualifier) {
        try {
            UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder().setUserId(userId).build();
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            stub.createAccount(request);
            System.out.println("OK\n");

            if (debug)
                System.err.println("Account created successfully");
        }
        catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    // deleteAccount not supported for phase 3
    /*public void deleteAccount(String userId, String qualifier) {
        try {
            UserDistLedger.DeleteAccountRequest request = UserDistLedger.DeleteAccountRequest.newBuilder().setUserId(userId).build();
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            stub.deleteAccount(request);
            System.out.println("OK\n");
            if (debug)
                System.err.println("Account deleted successfully");
        }
        catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }*/

    public void balance(String userId, String qualifier) {
        try {
            UserDistLedger.BalanceRequest request = UserDistLedger.BalanceRequest.newBuilder().setUserId(userId).build();
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            UserDistLedger.BalanceResponse response = stub.balance(request);
            if (response.getValue() == 0)
                System.out.println("OK\n");
            else
                System.out.println("OK\n" + response.getValue() + "\n");
            if (debug)
                System.err.println("Checked balance with success");
        }
        catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void transferTo(String accountFrom, String accountTo, Integer amount, String qualifier) {
        try {
            UserDistLedger.TransferToRequest request = UserDistLedger.TransferToRequest
                    .newBuilder().setAccountFrom(accountFrom).setAccountTo(accountTo).setAmount(amount).build();
            stub = createStub(qualifier);
            if (stub == null) {
                System.out.println("Server " + qualifier + " not found\n");
                return;
            }
            stub.transferTo(request);
            System.out.println("OK\n");
            if (debug)
                System.err.println("Transfer done successfully");
        }
        catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void shutdown(){
        this.channel.shutdown();
    }
}
