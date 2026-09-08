package brucli.ui;

import java.util.Random;

/**
 * Generates BruCLI's randomized response text.
 */
final class Messages {
    private static final Random RANDOM = new Random();

    private static final String[] WELCOME_MESSAGES = {
        "Empty your mind. What task shall we master today?",
        "Knowing is not enough, we must execute. Ready when you are.",
        "Adapt to the workflow. How can BruCLI assist your setup?",
        "Be formless, shapeless—like input. Type your command to begin.",
        "I fear not the user who runs 10,000 commands once, but the user "
                    + "who masters one command 10,000 times. Welcome back."
    };
    private static final String[] GOODBYE_MESSAGES = {
        "Do not pray for an easy runtime, pray for the strength to endure "
                    + "complex tasks. Farewell!",
        "Absorb what was useful, discard what was useless. Session closed.",
        "Be water, my friend... until the next execution.",
        "Task applied. Action completed. Walk on!",
        "Laser-like focus maintained to the end. See you next time.",
        "Keep practicing the fundamentals. Session closed."
    };
    private static final String[] TODO_MESSAGES = {
        "A goal is not always meant to be reached, it often serves simply "
                    + "as something to aim at. Task added.",
        "Do not turn away from the workload. Record your target.",
        "Notice that the stiffest tree is most easily cracked. Break your "
                    + "goal down into a task.",
        "To heavy minds, a task is a burden; to a warrior, it is an "
                    + "objective. Logging todo.",
        "Real living is living for others—and keeping track of your commitments."
    };
    private static final String[] DEADLINE_MESSAGES = {
        "Time waits for no process. Deadline anchored.",
        "To control time is to control oneself. Target date set.",
        "The quiet before the storm is preparation. Time limit registered.",
        "Do not let tomorrow steal the energy of today. Target set.",
        "A deadline sharpens the edge of intent. Date locked."
    };
    private static final String[] EVENT_MESSAGES = {
        "Be present in the moment, but map the ground ahead. Event scheduled.",
        "Flow into the schedule without friction. Time entry created.",
        "Preparation is the root of fluid action. Calendar updated.",
        "A warrior moves with rhythm, not chaos. Event locked.",
        "Honor the commitment of time. Marker placed on the schedule."
    };
    private static final String[] LIST_MESSAGES = {
        "Clear vision precedes effective action. Fetching your active inventory:",
        "Look closely at what remains; simplify to move forward:",
        "To know oneself is to study one's open commitments in action:",
        "Unclutter your view to sharpen your focus. Displaying tasks:",
        "Review your path without judgment, then strike again:"
    };
    private static final String[] MARK_MESSAGES = {
        "Strike complete! Task conquered.",
        "One clean move—item resolved.",
        "Offense turns into defense, intent turns into completion. Marked isDone!",
        "Shattered through the obstacle. Task marked complete.",
        "Execution without hesitation. Done!"
    };
    private static final String[] UNMARK_MESSAGES = {
        "The opponent rises again; face it with renewed energy. Task reopened.",
        "Flexibility allows a warrior to reset position. Task unmarked.",
        "Do not fear stepping back to build stronger momentum. Status restored.",
        "Flow backward, correct posture, strike again. Task reactivated.",
        "No motion is wasted if intent remains clear. Task restored."
    };
    private static final String[] DELETE_MESSAGES = {
        "Stripped away the unnecessary. Erased!",
        "Purge the dead weight to keep your form light and agile. Deleted.",
        "Severed from the record.",
        "Simplicity is the key to brilliance. Task eliminated.",
        "Cast aside what no longer serves the objective. Removed."
    };
    private static final String[] UNKNOWN_MESSAGES = {
        "Unfocused energy yields no force. Command not recognized.",
        "A strike without direction misses the target. Check your syntax.",
        "If you push against the wall, the wall pushes back. Invalid input.",
        "Refine your stance; BruCLI does not understand this motion.",
        "Do not strike blindly in the dark. Type a valid command."
    };
    private static final String[] BRUCE_LEE_SOUNDS = {
        "*HI-YA!*",
        "*WATAAAH!*",
        "*HOOO-AAAH!*",
        "*HYAA-TCHAA!*",
        "*WHACK*"
    };

    private Messages() {
    }

    static String welcome() {
        return randomMessage(WELCOME_MESSAGES);
    }

    static String goodbye() {
        return randomMessage(GOODBYE_MESSAGES);
    }

    static String todoAdded() {
        return randomMessage(TODO_MESSAGES);
    }

    static String deadlineAdded() {
        return randomMessage(DEADLINE_MESSAGES);
    }

    static String eventAdded() {
        return randomMessage(EVENT_MESSAGES);
    }

    static String listHeader() {
        return randomMessage(LIST_MESSAGES);
    }

    static String taskMarked() {
        return randomMessage(MARK_MESSAGES);
    }

    static String taskUnmarked() {
        return randomMessage(UNMARK_MESSAGES);
    }

    static String taskDeleted() {
        return randomMessage(DELETE_MESSAGES);
    }

    static String unknownCommand() {
        return randomMessage(UNKNOWN_MESSAGES);
    }

    private static String randomMessage(String[] messages) {
        return String.format(
                "%s %s",
                BRUCE_LEE_SOUNDS[RANDOM.nextInt(BRUCE_LEE_SOUNDS.length)],
                messages[RANDOM.nextInt(messages.length)]
        );
    }
}
