import java.io.IOException;

/** Coordinates BruCLI's parser, task list, storage, and user interface. */
public class BruCLI {
    private static final String BANNER =
            ".@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                    ".@...................@......@.............@@\n" +
                    ".@..................@@......@@............@@\n" +
                    ".@...................@@......@@...........@@\n" +
                    ".@....................@@@.....@@@@@@......@@\n" +
                    ".@.............................@@@@@@.....@@\n" +
                    ".@....................@@@...........@@....@@\n" +
                    ".@...................@@@.............@@@..@@\n" +
                    ".@..................@@.................@@.@@\n" +
                    ".@................@@@...@..........@@@.@@.@@\n" +
                    ".@.......@@..@@@@@@...@.@.........@@@@@@@.@@\n" +
                    ".@.....@@@@@@@@....@@@..@........@@...@@..@@\n" +
                    ".@.....@......@@@@@@...@@........@........@@\n" +
                    ".@.....@@@@@@@@@@....@@@.........@@@......@@\n" +
                    ".@................@@@@.............@@.....@@\n" +
                    ".@..............@@@@................@@@...@@\n" +
                    ".@.............@@@...................@@@@.@@\n" +
                    ".@...........@@@..........@@@..........@@@@@\n" +
                    ".@..........@@@.........@@@@@@@@.........@@@\n" +
                    ".@.........@@........@@@@@...@@@@.........@@\n" +
                    ".@........@@........@@@.........@.........@@\n" +
                    ".@.......@@.......@@@...........@........@@@\n" +
                    ".@......@@.......@@@............@.......@@@@\n" +
                    ".@......@@.....@@@.............@@......@@.@@\n" +
                    ".@.....@@....@@@@..............@@..@@@@@..@@\n" +
                    ".@.....@..@@@@@................@...@.@@...@@\n" +
                    ".@..@@@@..@@@..................@@..@@.....@@\n" +
                    ".@@@@...@@.......................@..@@....@@\n" +
                    ".@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                    "+------------------------------------------+\n" +
                    "|   ____              ____ _     ___       |\n" +
                    "|  | __ ) _ __ _   _ / ___| |   |_ _|      |\n" +
                    "|  |  _ \\| '__| | | | |   | |    | |       |\n" +
                    "|  | |_) | |  | |_| | |___| |___ | |       |\n" +
                    "|  |____/|_|   \\__,_|\\____|_____|___|      |\n" +
                    "|                 BruCLI                   |\n" +
                    "+------------------------------------------+";

    private final TaskList tasks;
    private final Ui ui;
    private final Storage storage;
    private final Parser parser;
    private final boolean loadingFailed;

    /** Creates a BruCLI application backed by the given task file. */
    public BruCLI(String filePath) {
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

    /** Starts BruCLI's command-processing loop. */
    public void run() {
        ui.showWelcome(BANNER);

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

    /** Starts BruCLI using the default task data file. */
    public static void main(String[] args) {
        new BruCLI("tasks.txt").run();
    }
}
