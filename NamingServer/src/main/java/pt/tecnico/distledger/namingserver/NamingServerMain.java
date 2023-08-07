package pt.tecnico.distledger.namingserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class NamingServerMain {

    private static final String debugFlag = "-debug";
    private static boolean debug = false;
    private static int port = 5001;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(NamingServerMain.class.getSimpleName());

        if (args.length == 1) {
            if (debugFlag.equals(args[0])) {
                System.err.println("Server is in debug mode");
                debug = true;
            }
        }

        if (debug)
            System.out.printf("Received %d arguments%n", args.length);
        if (debug) {
            for (int i = 0; i < args.length; i++)
                System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", NamingServerMain.class.getName());
            return;
        }

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port)
                .addService(new NamingServerServiceImpl())
                .build();

        if (debug)
            System.err.println("Starting server");

        // Start the server
        server.start();

        // Server threads are running in the background.
        if (debug)
            System.out.println("Server started");

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }
}


