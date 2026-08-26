package brucli.task;

import java.time.LocalDateTime;

/** Represents a task that must be completed by a due time. */
public class Deadline extends Task {
    private final LocalDateTime due;

    /** Creates a deadline with its internal ID, description, and due time. */
    public Deadline(int id, String description, LocalDateTime due) {
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
        return super.toString() + " (by: " + DateTimes.display(due) + ")";
    }
}
