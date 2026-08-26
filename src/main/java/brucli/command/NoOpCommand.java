package brucli.command;

import brucli.storage.Storage;
import brucli.task.TaskList;
import brucli.ui.Ui;

/** Represents a recognized command that currently has no behavior. */
public class NoOpCommand extends Command {

    /** Deliberately performs no action. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // The GAME command was previously silent, so preserve that behavior.
    }
}
