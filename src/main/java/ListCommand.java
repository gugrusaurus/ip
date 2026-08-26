/** Displays all tasks or only dated tasks that satisfy a filter. */
public class ListCommand extends Command {
    private final BruCLI.ListFilter filter;

    /** Creates a list command with an optional date filter. */
    public ListCommand(BruCLI.ListFilter filter) {
        this.filter = filter;
    }

    /** Builds and displays the matching task list. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        StringBuilder output = new StringBuilder();

        for (TaskList.IndexedTask indexedTask : tasks.matching(filter)) {
            output.append(String.format(
                    "%d: %s%n",
                    indexedTask.number(),
                    indexedTask.task()
            ));
        }

        ui.showMessage(BruCLI.Messages.listMessage());
        ui.showMessage(
                output.isEmpty()
                        ? "No matching tasks."
                        : output.toString().stripTrailing()
        );
    }
}
