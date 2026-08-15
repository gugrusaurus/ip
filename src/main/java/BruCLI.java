import java.util.Random;
import java.util.Scanner;

public class BruCLI {
    private static final String name = "BruCLI";
    private static String[] entries = new String[100];
    private static String banner =
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

    private static class Messages {
        private static final Random RANDOM = new Random();

        private static String randomMessage(String[] messages) {
            int msgIndex = RANDOM.nextInt(messages.length);
            int effectIndex = RANDOM.nextInt(bruceLeeSounds.length);
            return String.format("%s %s", bruceLeeSounds[effectIndex], messages[msgIndex]);
        }

        public static String welcomeMessage() {
            return randomMessage(welcomeMessages);
        }

        public static String goodbyeMessage() {
            return randomMessage(goodbyeMessages);
        }

        public static String soundEffect() {
            return randomMessage(bruceLeeSounds);
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
        private static final String[] bruceLeeSounds = {
                "*HI-YA!*",
                "*WATAAAH!*",
                "*HOOO-AAAH!*",
                "*HYAA-TCHAA!*"
        };
    }



    private static void say(String msg){
        System.out.println(name + "|");
        System.out.println("     " + msg.replace("\n", "\n     "));
    }

    private static void run() {
        int count = 0;
        System.out.println(banner);
        say(Messages.welcomeMessage());
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            switch(input.toLowerCase()) {
                case "bye":
                    say(Messages.goodbyeMessage());
                    return;

                case "list":
                    String out = "";
                    for (int i = 0; i < count; i++){
                        out += String.format("%d: %s\n",i + 1, entries[i]);
                    }
                    say(out.stripTrailing());
                    break;

                default:
                    entries[count] = input;
                    count++;
                    say("Added: " + input);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        BruCLI.run();
    }
}
