import java.util.Scanner;

/** Handles all terminal input and output for BruCLI. */
public class Ui {
    private final Scanner scanner;

    /** Creates a UI that reads commands from standard input. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /** Displays the application's opening banner. */
    public void showBanner(String banner) {
        System.out.println(banner);
    }

    /** Returns whether another command is available from the user. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads and returns the next command entered by the user. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Displays a message using BruCLI's standard prompt layout. */
    public void showMessage(String message) {
        System.out.println("BruCLI |");
        System.out.println("     " + message.replace("\n", "\n     "));
    }
}
