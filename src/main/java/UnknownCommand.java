/** Handles input that does not match a supported BruCLI command. */
public class UnknownCommand extends Command {

    /** Displays a command-not-recognized response. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showUnknownCommand();
    }
}
