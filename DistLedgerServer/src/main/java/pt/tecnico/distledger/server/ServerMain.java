package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.domain.ServerState;
import java.io.IOException;

public class ServerMain {

    private static final String debugFlag = "-debug";
    private static boolean debug = false;
    private static String qualifier;
    private static final String A = "A";
    private static final String B = "B";
    private static String host = "localhost";
    private static String serviceName = "DistLedger";

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println(ServerMain.class.getSimpleName());

        if (args.length == 3) {
            if (debugFlag.equals(args[2])) {
                System.err.println("Server is in debug mode");
                debug = true;
            }
        }

        // receive and print arguments
        if (debug)
            System.err.printf("Received %d arguments%n", args.length);
        if (debug) {
            for (int i = 0; i < args.length; i++)
                System.err.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
            return;
        }

        final int port = Integer.parseInt(args[0]);

        qualifier = args[1];
        if (!(qualifier.equals(A) || qualifier.equals(B)))
            return;

        final ServerState serverState = new ServerState();
        final NamingServerService namingServerService = new NamingServerService(port, host, serviceName, qualifier);
        final CrossServerImpl crossServerService = new CrossServerImpl(serverState);
        final BindableService userService = new UserServiceImpl(serverState, qualifier);
        final BindableService adminService = new AdminServiceImpl(serverState, qualifier);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port)
                .addService(userService)
                .addService(adminService)
                .addService(crossServerService)
                .build();

        if (debug)
            System.err.println("Starting server");

        // Start the server
        server.start();
        namingServerService.register();

        // Server threads are running in the background.
        if (debug)
            System.err.println("Server started");

        // Do not exit the main thread. Wait until server is terminated.
        System.out.println("Press enter to shutdown");
        System.in.read();
        server.shutdown();
        namingServerService.delete();
    }

}
