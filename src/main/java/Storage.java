import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Loads and saves BruCLI tasks using a text file. */
public class Storage {
    private final Path filePath;

    /**
     * Creates a storage component that uses the supplied file path.
     *
     * @param filePath location of the task data file
     */
    public Storage(String filePath) {
        this.filePath = Path.of(filePath);
    }

    /** Saves all tasks, replacing the previous contents of the data file. */
    public void save(List<BruCLI.Task> tasks) throws IOException {
        StringBuilder output = new StringBuilder();

        for (BruCLI.Task task : tasks) {
            output.append(task.serialize()).append("\n");
        }

        Files.writeString(filePath, output.toString());
    }

    /**
     * Loads tasks from the data file, or returns an empty list if it does not exist.
     */
    public ArrayList<BruCLI.Task> load() throws IOException {
        ArrayList<BruCLI.Task> loadedTasks = new ArrayList<>();

        if (Files.notExists(filePath)) {
            return loadedTasks;
        }

        try {
            for (String line : Files.readAllLines(filePath)) {
                loadedTasks.add(parseTask(line, loadedTasks.size()));
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new IOException("Task data file is malformed.", e);
        }

        return loadedTasks;
    }

    /** Reconstructs one task from its saved representation. */
    private BruCLI.Task parseTask(String line, int id) {
        String[] parts = line.split(" \\| ");

        String type = parts[0];
        boolean done = parts[1].equals("1");
        String description = parts[2];
        BruCLI.Task task;

        switch (type) {
        case "T":
            task = new BruCLI.Task.Todo(id, description);
            break;
        case "D":
            task = new BruCLI.Task.Deadline(
                    id,
                    description,
                    BruCLI.DateTimes.parseStored(parts[3])
            );
            break;
        case "E":
            task = new BruCLI.Task.Event(
                    id,
                    description,
                    BruCLI.DateTimes.parseStored(parts[3]),
                    BruCLI.DateTimes.parseStored(parts[4])
            );
            break;
        default:
            throw new IllegalArgumentException("Unknown task type: " + type);
        }

        if (done) {
            task.markDone();
        }

        return task;
    }
}
