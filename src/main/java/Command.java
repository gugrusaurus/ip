import java.io.IOException;

/** Represents an executable user command. */
public abstract class Command {

    /** Executes this command using the application's collaborators. */
    public abstract void execute(
            TaskList tasks,
            Ui ui,
            Storage storage
    ) throws IOException;

    /** Returns whether executing this command should end the application. */
    public boolean isExit() {
        return false;
    }
}
