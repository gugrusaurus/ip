package brucli;

import java.io.IOException;

import brucli.command.Command;
import brucli.parser.Parser;
import brucli.storage.Storage;
import brucli.task.TaskList;
import brucli.ui.Messages;
import brucli.ui.Ui;

/**
 * Coordinates BruCLI's parser, task list, storage, and user interface.
 */
public class BruCliApp {
    private final TaskList tasks;
    private final Ui ui;
    private final Storage storage;
    private final Parser parser;
    private final boolean loadingFailed;

    /**
     * Creates a BruCLI application backed by the given task file.
     */
    public BruCliApp(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        parser = new Parser();

        TaskList loadedTasks;
        boolean loadHadError = false;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (IOException e) {
            loadedTasks = new TaskList();
            loadHadError = true;
        }

        tasks = loadedTasks;
        loadingFailed = loadHadError;
    }

    /**
     * Starts BruCLI's command-processing loop.
     */
    public void run() {
        ui.showWelcome(Messages.banner());

        if (loadingFailed) {
            ui.showLoadingError();
        }

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            try {
                Command command = parser.parse(input);
                command.execute(tasks, ui, storage);
                if (command.isExit()) {
                    return;
                }
            } catch (IllegalArgumentException e) {
                ui.showError(e.getMessage());
            } catch (IOException e) {
                ui.showSavingError();
            }
        }
    }

    /**
     * Starts BruCLI using the default task data file.
     */
    public static void main(String[] args) {
        new BruCliApp("tasks.txt").run();
    }
}
