package gitlet;

import java.io.File;

import static gitlet.GitletException.validateNumArgs;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     * java gitlet.Main
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs("init", args, 1);
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            case "commit":

                break;
            case "rm":

                break;
            case "log":

                break;
            case "global-log":

                break;
            case "find":

                break;
            case "status":

                break;
            case "checkout":

                break;
            case "branch":

                break;
            case "rm-branch":

                break;
            case "reset":

                break;
            case "merge":

                break;
            default:
                System.out.println("Command does not exist.");
                break;
        }
    }




}
