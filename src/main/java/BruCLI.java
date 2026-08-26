import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class BruCLI {
    private final TaskList tasks;
    private final Ui ui;
    private final Storage storage;
    private final Parser parser;
    private final boolean loadingFailed;

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

    /**
     * Creates a BruCLI application backed by the given task file.
     *
     * @param filePath location used to load and save tasks
     */
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

    /** Converts date-time values between user input, storage, and display formats. */
    static class DateTimes {
        private static final DateTimeFormatter INPUT_FORMAT =
                DateTimeFormatter.ofPattern("uuuu-MM-dd HHmm")
                        .withResolverStyle(java.time.format.ResolverStyle.STRICT);
        private static final DateTimeFormatter DISPLAY_FORMAT =
                DateTimeFormatter.ofPattern("MMM d uuuu, h:mm a", Locale.ENGLISH);

        /**
         * Parses a date and time in the format accepted by BruCLI.
         *
         * @param text date-time text such as {@code 2026-08-26 1830}
         * @return the parsed date and time
         * @throws IllegalArgumentException if the text is not a valid date and time
         */
        public static LocalDateTime parse(String text) {
            try {
                return LocalDateTime.parse(text, INPUT_FORMAT);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Date and time must use yyyy-MM-dd HHmm "
                                + "(for example, 2026-08-26 1830)."
                );
            }
        }

        /** Returns the ISO-8601 date-time representation used in the save file. */
        public static String serialize(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        /** Parses an ISO-8601 date-time read from the save file. */
        public static LocalDateTime parseStored(String text) {
            try {
                return LocalDateTime.parse(
                        text,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Invalid date and time in save file: " + text
                );
            }
        }

        /** Returns a human-friendly date-time representation for task listings. */
        public static String display(LocalDateTime dateTime) {
            return dateTime.format(DISPLAY_FORMAT);
        }
    }

    static abstract class Task {
        protected int id;
        protected String description;
        protected boolean done;

        public Task(int id, String description) {
            this.id = id;
            this.description = description;
            this.done = false;
        }

        public void markDone() {
            done = true;
        }

        public void unmarkDone() {
            done = false;
        }

        abstract String serialize();

        protected abstract String getType();

        protected String getStatus() {
            return done ? "X" : " ";
        }

        /**
         * Checks whether this task satisfies a list date filter.
         * Tasks without a date are omitted from filtered lists.
         */
        public boolean matches(ListFilter filter) {
            if (filter == null) {
                return true;
            }

            LocalDateTime dateTime = getDateTime();
            if (dateTime == null) {
                return false;
            }

            return switch (filter.type()) {
                case BEFORE -> !dateTime.isAfter(filter.boundary());
                case AFTER -> dateTime.isAfter(filter.boundary());
            };
        }

        /** Returns the date used for filtering, or {@code null} for undated tasks. */
        protected LocalDateTime getDateTime() {
            return null;
        }

        @Override
        public String toString() {
            return String.format(
                    "[%s][%s] %s",
                    getType(),
                    getStatus(),
                    description
            );
        }

        static class Todo extends Task {

            public Todo(int id, String description) {
                super(id, description);
            }

            @Override
            protected String getType() {
                return "T";
            }

            @Override
            public String serialize() {
                return String.format(
                        "T | %d | %s",
                        done ? 1 : 0,
                        description
                );
            }

        }

        static class Deadline extends Task {
            private final LocalDateTime due;

            public Deadline(
                    int id,
                    String description,
                    LocalDateTime due
            ) {
                super(id, description);
                this.due = due;
            }

            @Override
            public String serialize() {
                return String.format(
                        "D | %d | %s | %s",
                        done ? 1 : 0,
                        description,
                        DateTimes.serialize(due)
                );
            }

            @Override
            protected String getType() {
                return "D";
            }

            @Override
            protected LocalDateTime getDateTime() {
                return due;
            }

            @Override
            public String toString() {
                return super.toString()
                        + " (by: " + DateTimes.display(due) + ")";
            }
        }

        static class Event extends Task {
            private final LocalDateTime start;
            private final LocalDateTime end;

            public Event(
                    int id,
                    String description,
                    LocalDateTime start,
                    LocalDateTime end
            ) {
                super(id, description);
                this.start = start;
                this.end = end;
            }

            @Override
            public String serialize() {
                return String.format(
                        "E | %d | %s | %s | %s",
                        done ? 1 : 0,
                        description,
                        DateTimes.serialize(start),
                        DateTimes.serialize(end)
                );
            }

            @Override
            protected String getType() {
                return "E";
            }

            @Override
            protected LocalDateTime getDateTime() {
                return start;
            }

            @Override
            public String toString() {
                return super.toString()
                        + " (from: "
                        + DateTimes.display(start)
                        + " to: "
                        + DateTimes.display(end)
                        + ")";
            }
        }
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

    public static void main(String[] args) {
        new BruCLI("tasks.txt").run();
    }
}
