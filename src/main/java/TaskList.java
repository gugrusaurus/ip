import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Owns the collection of tasks and operations that change it. */
public class TaskList {
    private final ArrayList<BruCLI.Task> tasks;

    /** Pairs a task with its one-based number shown to the user. */
    public record IndexedTask(int number, BruCLI.Task task) {}

    /** Creates an empty task list. */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /** Creates a task list containing tasks loaded from storage. */
    public TaskList(List<BruCLI.Task> initialTasks) {
        tasks = new ArrayList<>(initialTasks);
        reindexTasks();
    }

    /** Adds a todo and assigns it the next available ID. */
    public void addTodo(String description) {
        tasks.add(new BruCLI.Task.Todo(tasks.size(), description));
    }

    /** Adds a deadline and assigns it the next available ID. */
    public void addDeadline(String description, LocalDateTime due) {
        tasks.add(new BruCLI.Task.Deadline(tasks.size(), description, due));
    }

    /** Adds an event and assigns it the next available ID. */
    public void addEvent(
            String description,
            LocalDateTime start,
            LocalDateTime end
    ) {
        tasks.add(new BruCLI.Task.Event(
                tasks.size(),
                description,
                start,
                end
        ));
    }

    /** Marks the task at the zero-based ID as done. */
    public void markDone(int taskId) {
        getTask(taskId).markDone();
    }

    /** Marks the task at the zero-based ID as not done. */
    public void unmarkDone(int taskId) {
        getTask(taskId).unmarkDone();
    }

    /** Deletes the task at the zero-based ID and restores consecutive IDs. */
    public void delete(int taskId) {
        getTask(taskId);
        tasks.remove(taskId);
        reindexTasks();
    }

    /** Returns tasks matching the filter with their original user-facing numbers. */
    public List<IndexedTask> matching(BruCLI.ListFilter filter) {
        ArrayList<IndexedTask> matchingTasks = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            BruCLI.Task task = tasks.get(i);
            if (task.matches(filter)) {
                matchingTasks.add(new IndexedTask(i + 1, task));
            }
        }

        return matchingTasks;
    }

    /** Returns an immutable snapshot for persistence. */
    public List<BruCLI.Task> snapshot() {
        return List.copyOf(tasks);
    }

    /** Finds a task by its zero-based ID and validates that it exists. */
    private BruCLI.Task getTask(int taskId) {
        if (taskId < 0 || taskId >= tasks.size()) {
            throw new IllegalArgumentException("That task does not exist!");
        }

        return tasks.get(taskId);
    }

    /** Updates internal IDs after loading or deleting tasks. */
    private void reindexTasks() {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).id = i;
        }
    }
}
