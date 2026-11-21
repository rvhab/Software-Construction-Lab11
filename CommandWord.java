/**
 * Representations for all the valid command words of the game, together with
 * the text a player types for each one. Changing the text here (e.g. to
 * another language) is the ONLY change needed to "translate" a command.
 * Each word also knows whether it needs a second word (e.g. "go north").
 *
 * @author World of Zuul - Lab 11
 */
public enum CommandWord
{
    GO("go", true), QUIT("quit", false), HELP("help", false),
    LOOK("look", false), BACK("back", false), ITEMS("items", false),
    TAKE("take", true), DROP("drop", true), EAT("eat", true),
    UNLOCK("unlock", true), TALK("talk", true), GIVE("give", true),
    CHARGE("charge", false), FIRE("fire", false),
    UNKNOWN("?", false);

    private String commandString;
    private boolean needsSecondWord;

    CommandWord(String commandString, boolean needsSecondWord)
    {
        this.commandString = commandString;
        this.needsSecondWord = needsSecondWord;
    }

    /** @return true if this command must be followed by a second word. */
    public boolean needsSecondWord()
    {
        return needsSecondWord;
    }

    /** @return The command word as a string. */
    public String toString()
    {
        return commandString;
    }
}
