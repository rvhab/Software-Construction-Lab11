import java.util.Scanner;

/**
 * Reads a line from the terminal and turns it into a Command.
 * Lines with no words, or with more than two words, give an UNKNOWN command,
 * so they are reported as errors by the game.
 */
public class Parser
{
    private CommandWords commands;
    private Scanner reader;

    public Parser()
    {
        commands = new CommandWords();
        reader = new Scanner(System.in);
    }

    /** @return The next command from the user (console). */
    public Command getCommand()
    {
        return parse(readLine());
    }

    /** Show the prompt and read one line from the terminal. */
    public String readLine()
    {
        System.out.print("> ");
        return reader.nextLine();
    }

    /** Turn a line of text into a Command (used by console and GUI). */
    public Command parse(String inputLine)
    {
        String word1 = null;
        String word2 = null;
        boolean tooManyWords = false;

        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next();
            if(tokenizer.hasNext()) {
                word2 = tokenizer.next().toLowerCase();
                tooManyWords = tokenizer.hasNext();
            }
        }
        if(tooManyWords) {
            return new Command(CommandWord.UNKNOWN, null);
        }
        return new Command(commands.getCommandWord(word1), word2);
    }

    /** @return A String listing all valid command words. */
    public String getCommands()
    {
        return commands.getCommandList();
    }
}
