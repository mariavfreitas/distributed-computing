package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;

public class AdminClientMain {

    private static final String debugFlag = "-debug";
    private static boolean debug = false;

    public static void main(String[] args) {

        System.out.println(AdminClientMain.class.getSimpleName());

        if (args.length == 1) {
            if (debugFlag.equals(args[0])) {
                System.err.println("Admin is in debug mode");
                debug = true;
            }
        }

        // check arguments in debug mode
        if (debug) {
            if (args.length != 1) {
                System.err.println("Argument(s) missing!");
                System.err.println("Usage: mvn exec:java -Dexec.args=<debug>");
                return;
            }
        }

        CommandParser parser = new CommandParser(new AdminService(debug));
        parser.parseInput();

    }
}
