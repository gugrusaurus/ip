import java.io.IOException;

/** Base class for commands that add and persist a new task. */
public abstract class AddCommand extends Command {
    protected final String description;

    /** Creates an add command with the task description. */
    protected AddCommand(String description) {
        this.description = description;
    }

    /** Persists the current immutable snapshot of the task list. */
    protected void saveTasks(TaskList tasks, Storage storage) throws IOException {
        storage.save(tasks.snapshot());
    }
}
