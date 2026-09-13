package brucli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import brucli.ui.CommandResponse;
import brucli.ui.ResponseType;

public class BruCliAppTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    public void dummyTest() {
        assertEquals(2, 2);
    }

    @Test
    public void anotherDummyTest() {
        assertEquals(4, 4);
    }

    @Test
    public void getResponse_invalidCommandSyntax_returnsErrorResponse() {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        BruCliApp app = new BruCliApp(taskFile.toString());

        CommandResponse response = app.getResponse("todo");

        assertEquals(ResponseType.ERROR, response.type());
        assertFalse(response.text().isBlank());
    }
}
