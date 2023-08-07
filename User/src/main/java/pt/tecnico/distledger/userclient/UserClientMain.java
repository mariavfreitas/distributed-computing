package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;

public class UserClientMain {

    private static final String debugFlag = "-debug";
    private static boolean debug = false;

    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());

        if (args.length == 1) {
            if (debugFlag.equals(args[0])) {
                System.err.println("User is in debug mode");
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

        CommandParser parser = new CommandParser(new UserService(debug));
        parser.parseInput();
    }
}
