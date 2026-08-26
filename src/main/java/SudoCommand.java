/** Handles BruCLI's deliberately unsupported sudo command. */
public class SudoCommand extends Command {

    /** Explains that elevated privileges are unavailable. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showMessage("You have no power here.");
    }
}
