import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Random;

public class BruCLI {
    private static TaskList tasks;
    private static final Ui ui = new Ui();
    private static final Storage storage = new Storage("tasks.txt");

    enum Command {
        TODO,
        DEADLINE,
        EVENT,
        LIST,
        MARK,
        UNMARK,
        DELETE,
        UNKNOWN,
        BYE,
        SUDO,
        GAME
    }

    enum DateFilterType {
        BEFORE,
        AFTER
    }

    /** Describes a date condition applied by the list command. */
    record ListFilter(DateFilterType type, LocalDateTime boundary) {}

    record ParsedCommand(
            Command command,
            String description,
            Integer taskId,
            LocalDateTime due,
            LocalDateTime start,
            LocalDateTime end,
            ListFilter listFilter
    ) {}

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

    //Parse commands
    static class Parser {

        public static ParsedCommand parse(String input) {
            input = input.trim();

            if (input.isEmpty()) {
                return emptyCommand(Command.UNKNOWN);
            }

            Command command = parseCommand(input);

            switch (command) {
                case TODO:
                    return parseTodo(input);

                case DEADLINE:
                    return parseDeadline(input);

                case EVENT:
                    return parseEvent(input);

                case MARK:
                case UNMARK:
                case DELETE:
                    return parseTaskIdCommand(command, input);

                case LIST:
                    return parseList(input);

                case BYE:
                case UNKNOWN:
                default:
                    return emptyCommand(command);
            }
        }

        //get command from string
        private static Command parseCommand(String input) {
            String commandWord = input.split("\\s+", 2)[0].toUpperCase();

            try {
                return Command.valueOf(commandWord);
            } catch (IllegalArgumentException e) {
                return Command.UNKNOWN;
            }
        }

        private static ParsedCommand parseTodo(String input) {
            String description = getArguments(input);

            if (description.isEmpty()) {
                throw new IllegalArgumentException(
                        "A todo needs a description!"
                );
            }

            return new ParsedCommand(
                    Command.TODO,
                    description,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        private static ParsedCommand parseDeadline(String input) {
            String arguments = getArguments(input);

            int byIndex = arguments.indexOf("/by");

            if (byIndex == -1) {
                throw new IllegalArgumentException(
                        "A deadline needs /by!"
                );
            }

            String description =
                    arguments.substring(0, byIndex).trim();

            String dueText =
                    arguments.substring(byIndex + "/by".length()).trim();

            if (description.isEmpty() || dueText.isEmpty()) {
                throw new IllegalArgumentException(
                        "Usage: deadline DESCRIPTION /by yyyy-MM-dd HHmm"
                );
            }

            return new ParsedCommand(
                    Command.DEADLINE,
                    description,
                    null,
                    DateTimes.parse(dueText),
                    null,
                    null,
                    null
            );
        }

        private static ParsedCommand parseEvent(String input) {
            String arguments = getArguments(input);

            int fromIndex = arguments.indexOf("/from");
            int toIndex = arguments.indexOf("/to");

            if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {
                throw new IllegalArgumentException(
                        "Usage: event DESCRIPTION /from START /to END"
                );
            }

            String description =
                    arguments.substring(0, fromIndex).trim();

            String startText =
                    arguments.substring(
                            fromIndex + "/from".length(),
                            toIndex
                    ).trim();

            String endText =
                    arguments.substring(
                            toIndex + "/to".length()
                    ).trim();

            if (description.isEmpty()
                    || startText.isEmpty()
                    || endText.isEmpty()) {

                throw new IllegalArgumentException(
                        "Usage: event DESCRIPTION /from yyyy-MM-dd HHmm "
                                + "/to yyyy-MM-dd HHmm"
                );
            }

            return new ParsedCommand(
                    Command.EVENT,
                    description,
                    null,
                    null,
                    DateTimes.parse(startText),
                    DateTimes.parse(endText),
                    null
            );
        }

        /** Parses either a plain list command or a date-filtered list command. */
        private static ParsedCommand parseList(String input) {
            String arguments = getArguments(input);

            if (arguments.isEmpty()) {
                return emptyCommand(Command.LIST);
            }

            String[] parts = arguments.split("\\s+", 2);
            if (parts.length < 2) {
                throw new IllegalArgumentException(
                        "Usage: list BEFORE|AFTER yyyy-MM-dd HHmm"
                );
            }

            DateFilterType type;
            try {
                type = DateFilterType.valueOf(parts[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "List filter must be BEFORE or AFTER."
                );
            }

            return new ParsedCommand(
                    Command.LIST,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new ListFilter(type, DateTimes.parse(parts[1]))
            );
        }

        private static ParsedCommand parseTaskIdCommand(
                Command command,
                String input
        ) {
            String arguments = getArguments(input);

            if (arguments.isEmpty()) {
                throw new IllegalArgumentException(
                        command.toString().toLowerCase()
                                + " needs a task number!"
                );
            }

            int taskNumber;

            try {
                taskNumber = Integer.parseInt(arguments);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Task number must be a number!"
                );
            }

            if (taskNumber <= 0) {
                throw new IllegalArgumentException(
                        "Task number must be at least 1!"
                );
            }

            // User sees tasks starting from 1,
            // ArrayList indexes start from 0.
            int taskId = taskNumber - 1;

            return new ParsedCommand(
                    command,
                    null,
                    taskId,
                    null,
                    null,
                    null,
                    null
            );
        }

        private static String getArguments(String input) {
            String[] parts = input.trim().split("\\s+", 2);

            if (parts.length < 2) {
                return "";
            }

            return parts[1].trim();
        }

        private static ParsedCommand emptyCommand(Command command) {
            return new ParsedCommand(
                    command,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
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

    //AI used to generate message responses
    static class Messages {
        private static final Random RANDOM = new Random();

        private static String randomMessage(String[] messages) {
            int msgIndex = RANDOM.nextInt(messages.length);
            int effectIndex =
                    RANDOM.nextInt(bruceLeeSounds.length);

            return String.format(
                    "%s %s",
                    bruceLeeSounds[effectIndex],
                    messages[msgIndex]
            );
        }

        public static String welcomeMessage() {
            return randomMessage(welcomeMessages);
        }

        public static String goodbyeMessage() {
            return randomMessage(goodbyeMessages);
        }

        public static String todoMessage() {
            return randomMessage(todoMessages);
        }

        public static String deadlineMessage() {
            return randomMessage(deadlineMessages);
        }

        public static String eventMessage() {
            return randomMessage(eventMessages);
        }

        public static String listMessage() {
            return randomMessage(listMessages);
        }

        public static String markMessage() {
            return randomMessage(markMessages);
        }

        public static String unmarkMessage() {
            return randomMessage(unmarkMessages);
        }

        public static String deleteMessage() {
            return randomMessage(deleteMessages);
        }

        public static String unknownMessage() {
            return randomMessage(unknownMessages);
        }

        public static String soundEffect() {
            return bruceLeeSounds[
                    RANDOM.nextInt(bruceLeeSounds.length)
                    ];
        }

        private static final String[] welcomeMessages = {
                "Empty your mind. What task shall we master today?",
                "Knowing is not enough, we must execute. Ready when you are.",
                "Adapt to the workflow. How can BruCLI assist your setup?",
                "Be formless, shapeless—like input. Type your command to begin.",
                "I fear not the user who runs 10,000 commands once, but the user who masters one command 10,000 times. Welcome back."
        };

        private static final String[] goodbyeMessages = {
                "Do not pray for an easy runtime, pray for the strength to endure complex tasks. Farewell!",
                "Absorb what was useful, discard what was useless. Session closed.",
                "Be water, my friend... until the next execution.",
                "Task applied. Action completed. Walk on!",
                "Laser-like focus maintained to the end. See you next time.",
                "Keep practicing the fundamentals. Session closed."
        };

        private static final String[] todoMessages = {
                "A goal is not always meant to be reached, it often serves simply as something to aim at. Task added.",
                "Do not turn away from the workload. Record your target.",
                "Notice that the stiffest tree is most easily cracked. Break your goal down into a task.",
                "To heavy minds, a task is a burden; to a warrior, it is an objective. Logging todo.",
                "Real living is living for others—and keeping track of your commitments."
        };

        private static final String[] deadlineMessages = {
                "Time waits for no process. Deadline anchored.",
                "To control time is to control oneself. Target date set.",
                "The quiet before the storm is preparation. Time limit registered.",
                "Do not let tomorrow steal the energy of today. Target set.",
                "A deadline sharpens the edge of intent. Date locked."
        };

        private static final String[] eventMessages = {
                "Be present in the moment, but map the ground ahead. Event scheduled.",
                "Flow into the schedule without friction. Time entry created.",
                "Preparation is the root of fluid action. Calendar updated.",
                "A warrior moves with rhythm, not chaos. Event locked.",
                "Honor the commitment of time. Marker placed on the schedule."
        };

        private static final String[] listMessages = {
                "Clear vision precedes effective action. Fetching your active inventory:",
                "Look closely at what remains; simplify to move forward:",
                "To know oneself is to study one's open commitments in action:",
                "Unclutter your view to sharpen your focus. Displaying tasks:",
                "Review your path without judgment, then strike again:"
        };

        private static final String[] markMessages = {
                "Strike complete! Task conquered.",
                "One clean move—item resolved.",
                "Offense turns into defense, intent turns into completion. Marked done!",
                "Shattered through the obstacle. Task marked complete.",
                "Execution without hesitation. Done!"
        };

        private static final String[] unmarkMessages = {
                "The opponent rises again; face it with renewed energy. Task reopened.",
                "Flexibility allows a warrior to reset position. Task unmarked.",
                "Do not fear stepping back to build stronger momentum. Status restored.",
                "Flow backward, correct posture, strike again. Task reactivated.",
                "No motion is wasted if intent remains clear. Task restored."
        };

        private static final String[] deleteMessages = {
                "Stripped away the unnecessary. Erased!",
                "Purge the dead weight to keep your form light and agile. Deleted.",
                "Severed from the record.",
                "Simplicity is the key to brilliance. Task eliminated.",
                "Cast aside what no longer serves the objective. Removed."
        };

        private static final String[] unknownMessages = {
                "Unfocused energy yields no force. Command not recognized.",
                "A strike without direction misses the target. Check your syntax.",
                "If you push against the wall, the wall pushes back. Invalid input.",
                "Refine your stance; BruCLI does not understand this motion.",
                "Do not strike blindly in the dark. Type a valid command."
        };



        private static final String[] bruceLeeSounds = {
                "*HI-YA!*",
                "*WATAAAH!*",
                "*HOOO-AAAH!*",
                "*HYAA-TCHAA!*",
                "*WHACK*"
        };
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

    private static void run() {
        ui.showBanner(BANNER);
        ui.showMessage(Messages.welcomeMessage());

        try {
            tasks = new TaskList(storage.load());
        } catch (IOException e) {
            ui.showMessage("Error, could not load tasks.");
            tasks = new TaskList();
        }

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            try {
                ParsedCommand parsed = Parser.parse(input);

                switch (parsed.command()) {

                    case TODO: {
                        tasks.addTodo(parsed.description());
                        ui.showMessage(Messages.todoMessage());
                        saveTasks();
                        break;
                    }

                    case DEADLINE: {
                        tasks.addDeadline(
                                parsed.description(),
                                parsed.due()
                        );

                        ui.showMessage(Messages.deadlineMessage());
                        saveTasks();
                        break;
                    }

                    case EVENT: {
                        tasks.addEvent(
                                parsed.description(),
                                parsed.start(),
                                parsed.end()
                        );

                        ui.showMessage(Messages.eventMessage());
                        saveTasks();
                        break;
                    }

                    case LIST: {
                        StringBuilder out = new StringBuilder();

                        for (TaskList.IndexedTask indexedTask
                                : tasks.matching(parsed.listFilter())) {
                            out.append(String.format(
                                    "%d: %s%n",
                                    indexedTask.number(),
                                    indexedTask.task()
                            ));
                        }
                        ui.showMessage(Messages.listMessage());
                        ui.showMessage(
                                out.isEmpty()
                                        ? "No matching tasks."
                                        : out.toString().stripTrailing()
                        );
                        break;
                    }

                    case MARK: {
                        tasks.markDone(parsed.taskId());
                        ui.showMessage(Messages.markMessage());
                        saveTasks();
                        break;
                    }

                    case UNMARK: {
                        tasks.unmarkDone(parsed.taskId());
                        ui.showMessage(Messages.unmarkMessage());
                        saveTasks();
                        break;
                    }

                    case DELETE: {
                        tasks.delete(parsed.taskId());
                        ui.showMessage(Messages.deleteMessage());
                        saveTasks();
                        break;
                    }

                    case BYE:
                        ui.showMessage(Messages.goodbyeMessage());
                        return;

                    case UNKNOWN:
                        ui.showMessage(Messages.unknownMessage());
                        break;

                    case SUDO:
                        ui.showMessage("You have no power here.");
                        break;
                }

            } catch (IllegalArgumentException e) {
                ui.showMessage(e.getMessage());
            }
        }
    }

    /** Saves the current task list and reports any file error to the user. */
    private static void saveTasks() {
        try {
            storage.save(tasks.snapshot());
        } catch (IOException e) {
            ui.showMessage("Error, could not save tasks.");
        }
    }

    public static void main(String[] args) {
        BruCLI.run();
    }
}
