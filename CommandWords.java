import java.util.HashMap;

/**
 * Holds all valid command words of the game and maps the text typed by the
 * player to the matching CommandWord value.
 */
public class CommandWords
{
    private HashMap<String, CommandWord> validCommands;

    public CommandWords()
    {
        validCommands = new HashMap<String, CommandWord>();
        for(CommandWord command : CommandWord.values()) {
            if(command != CommandWord.UNKNOWN) {
                validCommands.put(command.toString(), command);
            }
        }
    }

    /**
     * Find the CommandWord for a string typed by the player.
     * @return The matching value, or UNKNOWN if there is none.
     */
    public CommandWord getCommandWord(String commandWord)
    {
        if(commandWord == null) {
            return CommandWord.UNKNOWN;
        }
        CommandWord command = validCommands.get(commandWord.toLowerCase());
        return (command == null) ? CommandWord.UNKNOWN : command;
    }

    /**
     * Produce (but do not print!) the list of valid commands.
     * Deciding how to show it is the job of the user-interface code.
     * @return A string like "go quit help ..."
     */
    public String getCommandList()
    {
        String list = "";
        for(String command : validCommands.keySet()) {
            list += command + " ";
        }
        return list.trim();
    }
}
