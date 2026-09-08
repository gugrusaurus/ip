package brucli.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import brucli.command.Command;
import brucli.command.FindCommand;

public class ParserTest {

    @Test
    public void parse_findWithKeyword_returnsFindCommand() {
        Parser parser = new Parser();

        Command command = parser.parse("find library book");

        assertInstanceOf(FindCommand.class, command);
    }

    @Test
    public void parse_findWithoutKeyword_exceptionThrown() {
        Parser parser = new Parser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse("find"));
    }
}
