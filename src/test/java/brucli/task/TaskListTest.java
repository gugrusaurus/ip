package brucli.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TaskListTest {

    @Test
    public void containing_matchingSubstring_returnsMatchingTasks() {
        TaskList tasks = createTaskList();

        List<TaskList.IndexedTask> matches = tasks.containing("book");

        assertEquals(2, matches.size());
        assertEquals(1, matches.get(0).number());
        assertEquals(3, matches.get(1).number());
    }

    @Test
    public void containing_keywordWithDifferentCase_returnsMatchingTasks() {
        TaskList tasks = createTaskList();

        List<TaskList.IndexedTask> matches = tasks.containing("BOOK FLIGHT");

        assertEquals(1, matches.size());
        assertEquals(1, matches.getFirst().number());
    }

    @Test
    public void containing_noMatchingKeyword_returnsEmptyList() {
        TaskList tasks = createTaskList();

        List<TaskList.IndexedTask> matches = tasks.containing("exercise");

        assertEquals(List.of(), matches);
    }

    @Test
    public void containing_blankKeyword_exceptionThrown() {
        TaskList tasks = createTaskList();

        assertThrows(IllegalArgumentException.class, () -> tasks.containing("  "));
    }

    @Test
    public void containing_nullKeyword_exceptionThrown() {
        TaskList tasks = createTaskList();

        assertThrows(IllegalArgumentException.class, () -> tasks.containing(null));
    }

    private TaskList createTaskList() {
        return new TaskList(List.of(
                new Todo(0, "book flight"),
                new Todo(1, "read notes"),
                new Todo(2, "return library book")
        ));
    }
}
