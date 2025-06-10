package gitlet;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Repository.WITHOUT_INIT;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Felicity
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        String firstArg = args[0];
        if (firstArg.equals("init")) {
            Repository.init();
            System.exit(0);
        }
        if (!GITLET_DIR.exists()) {
            System.out.println(WITHOUT_INIT);
            System.exit(0);
        }
        switch (firstArg) {
            case "add":
                Repository.add(args[1]);
                break;
            case "commit":
                Repository.commit(args);
                break;
            case "rm":
                Repository.remove(args[1]);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                Repository.find(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                Repository.checkout(args);
                break;
            case "branch":
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
    }
}
