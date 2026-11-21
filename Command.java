/**
 * A command typed by the player: a command word and an optional second word.
 */
public class Command
{
    private CommandWord commandWord;
    private String secondWord;

    public Command(CommandWord commandWord, String secondWord)
    {
        this.commandWord = commandWord;
        this.secondWord = secondWord;
    }

    public CommandWord getCommandWord() { return commandWord; }
    public String getSecondWord() { return secondWord; }
    public boolean isUnknown() { return commandWord == CommandWord.UNKNOWN; }
    public boolean hasSecondWord() { return secondWord != null; }
}
