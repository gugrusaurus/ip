/** Converts raw user input into commands that BruCLI can execute. */
public class Parser {

    /** Parses one line of user input. */
    public BruCLI.ParsedCommand parse(String input) {
        String trimmedInput = input.trim();

        if (trimmedInput.isEmpty()) {
            return emptyCommand(BruCLI.CommandType.UNKNOWN);
        }

        BruCLI.CommandType command = parseCommand(trimmedInput);

        return switch (command) {
        case TODO -> parseTodo(trimmedInput);
        case DEADLINE -> parseDeadline(trimmedInput);
        case EVENT -> parseEvent(trimmedInput);
        case MARK, UNMARK, DELETE -> parseTaskIdCommand(command, trimmedInput);
        case LIST -> parseList(trimmedInput);
        default -> emptyCommand(command);
        };
    }

    /** Identifies the command word at the start of the input. */
    private BruCLI.CommandType parseCommand(String input) {
        String commandWord = input.split("\\s+", 2)[0].toUpperCase();

        try {
            return BruCLI.CommandType.valueOf(commandWord);
        } catch (IllegalArgumentException e) {
            return BruCLI.CommandType.UNKNOWN;
        }
    }

    /** Parses a todo command and its required description. */
    private BruCLI.ParsedCommand parseTodo(String input) {
        String description = getArguments(input);

        if (description.isEmpty()) {
            throw new IllegalArgumentException("A todo needs a description!");
        }

        return new BruCLI.ParsedCommand(
                BruCLI.CommandType.TODO,
                description,
                null,
                null,
                null,
                null,
                null
        );
    }

    /** Parses a deadline command and converts its due time. */
    private BruCLI.ParsedCommand parseDeadline(String input) {
        String arguments = getArguments(input);
        int byIndex = arguments.indexOf("/by");

        if (byIndex == -1) {
            throw new IllegalArgumentException("A deadline needs /by!");
        }

        String description = arguments.substring(0, byIndex).trim();
        String dueText = arguments.substring(byIndex + "/by".length()).trim();

        if (description.isEmpty() || dueText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Usage: deadline DESCRIPTION /by yyyy-MM-dd HHmm"
            );
        }

        return new BruCLI.ParsedCommand(
                BruCLI.CommandType.DEADLINE,
                description,
                null,
                BruCLI.DateTimes.parse(dueText),
                null,
                null,
                null
        );
    }

    /** Parses an event command and converts its start and end times. */
    private BruCLI.ParsedCommand parseEvent(String input) {
        String arguments = getArguments(input);
        int fromIndex = arguments.indexOf("/from");
        int toIndex = arguments.indexOf("/to");

        if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {
            throw new IllegalArgumentException(
                    "Usage: event DESCRIPTION /from START /to END"
            );
        }

        String description = arguments.substring(0, fromIndex).trim();
        String startText = arguments.substring(
                fromIndex + "/from".length(),
                toIndex
        ).trim();
        String endText = arguments.substring(toIndex + "/to".length()).trim();

        if (description.isEmpty() || startText.isEmpty() || endText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Usage: event DESCRIPTION /from yyyy-MM-dd HHmm "
                            + "/to yyyy-MM-dd HHmm"
            );
        }

        return new BruCLI.ParsedCommand(
                BruCLI.CommandType.EVENT,
                description,
                null,
                null,
                BruCLI.DateTimes.parse(startText),
                BruCLI.DateTimes.parse(endText),
                null
        );
    }

    /** Parses either a plain list command or a date-filtered list command. */
    private BruCLI.ParsedCommand parseList(String input) {
        String arguments = getArguments(input);

        if (arguments.isEmpty()) {
            return emptyCommand(BruCLI.CommandType.LIST);
        }

        String[] parts = arguments.split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                    "Usage: list BEFORE|AFTER yyyy-MM-dd HHmm"
            );
        }

        BruCLI.DateFilterType type;
        try {
            type = BruCLI.DateFilterType.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "List filter must be BEFORE or AFTER."
            );
        }

        return new BruCLI.ParsedCommand(
                BruCLI.CommandType.LIST,
                null,
                null,
                null,
                null,
                null,
                new BruCLI.ListFilter(type, BruCLI.DateTimes.parse(parts[1]))
        );
    }

    /** Parses a one-based task number and converts it to a zero-based ID. */
    private BruCLI.ParsedCommand parseTaskIdCommand(
            BruCLI.CommandType command,
            String input
    ) {
        String arguments = getArguments(input);

        if (arguments.isEmpty()) {
            throw new IllegalArgumentException(
                    command.toString().toLowerCase() + " needs a task number!"
            );
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(arguments);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Task number must be a number!");
        }

        if (taskNumber <= 0) {
            throw new IllegalArgumentException("Task number must be at least 1!");
        }

        return new BruCLI.ParsedCommand(
                command,
                null,
                taskNumber - 1,
                null,
                null,
                null,
                null
        );
    }

    /** Returns everything after the command word. */
    private String getArguments(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        return parts.length < 2 ? "" : parts[1].trim();
    }

    /** Creates a parsed command that has no arguments. */
    private BruCLI.ParsedCommand emptyCommand(BruCLI.CommandType command) {
        return new BruCLI.ParsedCommand(
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
