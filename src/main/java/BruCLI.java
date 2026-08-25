import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class BruCLI {
    private static final String name = "BruCLI";
    private static final ArrayList<Task> tasks = new ArrayList<>();

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

    record ParsedCommand(
            Command command,
            String description,
            Integer taskId,
            String due,
            String start,
            String end
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

            String due =
                    arguments.substring(byIndex + "/by".length()).trim();

            if (description.isEmpty() || due.isEmpty()) {
                throw new IllegalArgumentException(
                        "Usage: deadline DESCRIPTION /by TIME"
                );
            }

            return new ParsedCommand(
                    Command.DEADLINE,
                    description,
                    null,
                    due,
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

            String start =
                    arguments.substring(
                            fromIndex + "/from".length(),
                            toIndex
                    ).trim();

            String end =
                    arguments.substring(
                            toIndex + "/to".length()
                    ).trim();

            if (description.isEmpty()
                    || start.isEmpty()
                    || end.isEmpty()) {

                throw new IllegalArgumentException(
                        "Usage: event DESCRIPTION /from START /to END"
                );
            }

            return new ParsedCommand(
                    Command.EVENT,
                    description,
                    null,
                    null,
                    start,
                    end
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
                    null
            );
        }
    }

    //AI used to generate message responses
    static class Messages {
        private static final Random RANDOM = new Random();

        public static void say(String msg) {
            System.out.println(name + " |");
            System.out.println(
                    "     " + msg.replace("\n", "\n     ")
            );
        }

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

        public static void welcomeMessage() {
            say(randomMessage(welcomeMessages));
        }

        public static void goodbyeMessage() {
            say(randomMessage(goodbyeMessages));
        }

        public static void todoMessage() {
            say(randomMessage(todoMessages));
        }

        public static void deadlineMessage() {
            say(randomMessage(deadlineMessages));
        }

        public static void eventMessage() {
            say(randomMessage(eventMessages));
        }

        public static void listMessage() {
            say(randomMessage(listMessages));
        }

        public static void markMessage() {
            say(randomMessage(markMessages));
        }

        public static void unmarkMessage() {
            say(randomMessage(unmarkMessages));
        }

        public static void deleteMessage() {
            say(randomMessage(deleteMessages));
        }

        public static void unknownMessage() {
            say(randomMessage(unknownMessages));
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

    static class Storage {

        private static final Path FILE_PATH =
                Path.of("tasks.txt");

        public static void save(ArrayList<Task> tasks) {
            try {
                StringBuilder out = new StringBuilder();

                for (Task task : tasks) {
                    out.append(task.serialize())
                            .append("\n");
                }

                Files.writeString(
                        FILE_PATH,
                        out.toString()
                );

            } catch (IOException e) {
                Messages.say("Error, could not save tasks.");
            }
        }

        public static ArrayList<Task> load() {
            ArrayList<Task> loadedTasks = new ArrayList<>();

            try {
                if (Files.notExists(FILE_PATH)) {
                    return loadedTasks;
                }

                for (String line : Files.readAllLines(FILE_PATH)) {
                    Task task = parseTask(
                            line,
                            loadedTasks.size()
                    );

                    loadedTasks.add(task);
                }

            } catch (IOException e) {
                Messages.say("Error, could not load tasks.");
            }

            return loadedTasks;
        }

        private static Task parseTask(String line, int id) {
            String[] parts = line.split(" \\| ");

            String type = parts[0];
            boolean done = parts[1].equals("1");
            String description = parts[2];

            Task task;

            switch (type) {
                case "T":
                    task = new Task.Todo(
                            id,
                            description
                    );
                    break;

                case "D":
                    task = new Task.Deadline(
                            id,
                            description,
                            parts[3]
                    );
                    break;

                case "E":
                    task = new Task.Event(
                            id,
                            description,
                            parts[3],
                            parts[4]
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown task type: " + type
                    );
            }

            if (done) {
                task.markDone();
            }

            return task;
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
            private final String due;

            public Deadline(
                    int id,
                    String description,
                    String due
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
                        due
                );
            }

            @Override
            protected String getType() {
                return "D";
            }

            @Override
            public String toString() {
                return super.toString()
                        + " (by: " + due + ")";
            }
        }

        static class Event extends Task {
            private final String start;
            private final String end;

            public Event(
                    int id,
                    String description,
                    String start,
                    String end
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
                        start,
                        end
                );
            }

            @Override
            protected String getType() {
                return "E";
            }

            @Override
            public String toString() {
                return super.toString()
                        + " (from: "
                        + start
                        + " to: "
                        + end
                        + ")";
            }
        }
    }

    private static void run() {
        System.out.println(BANNER);
        Messages.welcomeMessage();

        Scanner scanner = new Scanner(System.in);

        if (Files.notExists(Path.of("tasks.txt"))) {
            Storage.save(tasks);
        } else {
            tasks.addAll(Storage.load());
        }

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            try {
                ParsedCommand parsed = Parser.parse(input);

                switch (parsed.command()) {

                    case TODO: {
                        Task task = new Task.Todo(
                                tasks.size(),
                                parsed.description()
                        );

                        tasks.add(task);
                        Messages.todoMessage();
                        Storage.save(tasks);
                        break;
                    }

                    case DEADLINE: {
                        Task task = new Task.Deadline(
                                tasks.size(),
                                parsed.description(),
                                parsed.due()
                        );

                        tasks.add(task);
                        Messages.deadlineMessage();
                        Storage.save(tasks);
                        break;
                    }

                    case EVENT: {
                        Task task = new Task.Event(
                                tasks.size(),
                                parsed.description(),
                                parsed.start(),
                                parsed.end()
                        );

                        tasks.add(task);
                        Messages.eventMessage();
                        Storage.save(tasks);
                        break;
                    }

                    case LIST: {
                        StringBuilder out = new StringBuilder();

                        for (int i = 0; i < tasks.size(); i++) {
                            out.append(String.format(
                                    "%d: %s%n",
                                    i + 1,
                                    tasks.get(i)
                            ));
                        }
                        Messages.listMessage();
                        Messages.say(
                                out.toString().stripTrailing()
                        );
                        break;
                    }

                    case MARK: {
                        Task task = getTask(parsed.taskId());

                        task.markDone();

                        Messages.markMessage();
                        Storage.save(tasks);
                        break;
                    }

                    case UNMARK: {
                        Task task = getTask(parsed.taskId());

                        task.unmarkDone();

                        Messages.unmarkMessage();
                        Storage.save(tasks);
                        break;
                    }

                    case DELETE: {
                        Task task = getTask(parsed.taskId());

                        tasks.remove((int) parsed.taskId());

                        reindexTasks();

                        Messages.deleteMessage();
                        Storage.save(tasks);
                        break;
                    }

                    case BYE:
                        Messages.goodbyeMessage();
                        return;

                    case UNKNOWN:
                        Messages.unknownMessage();
                        break;

                    case SUDO:
                        Messages.say("You have no power here.");
                        break;
                }

            } catch (IllegalArgumentException e) {
                Messages.say(e.getMessage());
            }
        }
    }

    private static Task getTask(int taskId) {
        if (taskId < 0 || taskId >= tasks.size()) {
            throw new IllegalArgumentException(
                    "That task does not exist!"
            );
        }

        return tasks.get(taskId);
    }

    private static void reindexTasks() {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).id = i;
        }
    }

    public static void main(String[] args) {
        BruCLI.run();
    }
}