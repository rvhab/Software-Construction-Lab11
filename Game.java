import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * World of Zuul - "Horror Castle".
 *
 * Willow, a brave little witch, is trapped inside a cursed horror castle.
 * Find the rusty key, unlock the iron gate in the great hall and escape - before
 * the midnight bell tolls (you run out of commands)!
 *
 * Game coordinates the world and dispatches commands; the real work is
 * delegated to Room, Player, Item, etc. (responsibility-driven design).
 * All text goes through a GameOutput, so the same game runs in the console
 * (class Game) or in the graphical interface (class GameGUI).
 * Commands are dispatched through a map of actions, so adding a new command
 * means adding one CommandWord value, one method and one registration line.
 */
public class Game
{
    /** How the game stands. */
    public enum Outcome { PLAYING, WON, LOST, QUIT }

    /** One executable game command. Returns true if the game should end. */
    private interface Action
    {
        boolean execute(Command command);
    }

    private static final int MAX_COMMANDS = 60;

    private GameOutput output;
    private Parser parser;
    private Player player;
    private Room goalRoom;
    private ArrayList<Room> allRooms;
    private ArrayList<Room> teleportTargets;
    private ArrayList<GameCharacter> movingCharacters;
    private HashMap<CommandWord, Action> actions;
    private Random random;
    private int commandsUsed;
    private Outcome outcome;

    /** Create a console game. */
    public Game()
    {
        this(new ConsoleOutput());
    }

    /** Create a game that shows its messages through the given output. */
    public Game(GameOutput output)
    {
        this.output = output;
        parser = new Parser();
        random = new Random();
        allRooms = new ArrayList<Room>();
        teleportTargets = new ArrayList<Room>();
        movingCharacters = new ArrayList<GameCharacter>();
        commandsUsed = 0;
        outcome = Outcome.PLAYING;
        Room start = createRooms();
        player = new Player("Willow", start, 2500);
        registerActions();
    }

    /** Start the console game from outside BlueJ. */
    public static void main(String[] args)
    {
        new Game().play();
    }

    // ---------------------------------------------------------------- state for views

    public Player getPlayer() { return player; }
    public List<Room> getAllRooms() { return allRooms; }
    public int getMaxCommands() { return MAX_COMMANDS; }
    public int getCommandsUsed() { return commandsUsed; }
    public int getCommandsLeft() { return MAX_COMMANDS - commandsUsed; }
    public Outcome getOutcome() { return outcome; }
    public boolean isFinished() { return outcome != Outcome.PLAYING; }

    // ---------------------------------------------------------------- world

    private Room createRooms()
    {
        Room hall = new Room("Great Hall", "in a vast, freezing great hall where the fire burns blood-red");
        Room kitchen = new Room("Cursed Kitchen", "in a cursed kitchen, where something red bubbles in the cauldron");
        Room library = new Room("Forbidden Library", "in a forbidden library where the books whisper your name");
        Room tower = new Room("Bell Tower", "at the top of the bell tower, under a blood-red moon");
        Room cellar = new Room("Dungeon", "in a dripping dungeon, chains rattling in the dark");
        Room portal = new Room("Cursed Portal", "in a room where a cursed portal swirls with crimson light");
        Room garden = new Room("Graveyard Gate", "at the graveyard gate, free at last");
        allRooms.add(hall); allRooms.add(kitchen); allRooms.add(library); allRooms.add(tower);
        allRooms.add(cellar); allRooms.add(portal); allRooms.add(garden);

        hall.setExit("north", library);
        hall.setExit("east", kitchen);
        hall.setExit("south", garden);
        hall.setExit("up", tower);
        kitchen.setExit("west", hall);
        library.setExit("south", hall);
        library.setExit("down", cellar);     // TRAPDOOR: no "up" exit back
        tower.setExit("down", hall);
        cellar.setExit("east", portal);
        portal.setExit("west", cellar);
        garden.setExit("north", hall);

        hall.lockExit("south");              // LOCKED DOOR: needs the key
        portal.setTransporter(true);         // TRANSPORTER ROOM
        goalRoom = garden;

        teleportTargets.add(hall);
        teleportTargets.add(kitchen);
        teleportTargets.add(library);
        teleportTargets.add(tower);
        teleportTargets.add(cellar);

        hall.addItem(new Item("lantern", "a flickering lantern burning with a ghostly flame", 800, true));
        library.addItem(new Item("grimoire", "a cursed black book that groans when opened", 1200, true));
        library.addItem(new Item("cauldron", "a giant iron cauldron, far too heavy to lift", 90000, false));
        cellar.addItem(new Item("key", "a rusty iron key, cold as a grave", 50, true));
        kitchen.addItem(new Beamer());
        Item cookie = new Item("cookie", "a strange cookie that glows an eerie green", 100, true);
        cookie.setMagicBonus(1500);
        kitchen.addItem(cookie);

        GameCharacter cat = new GameCharacter("cat",
            "Hisss... bring me the lantern, the dark hurts my eyes, and I shall tell you where the key hides.", tower, false);
        cat.setRequest("lantern", new Item("scroll",
            "a scroll: 'The key lies BELOW the library. Beware the one-way trapdoor!'", 10, true));
        GameCharacter ghost = new GameCharacter("ghost", "Wooooo... you shall never leave!", library, true);
        movingCharacters.add(ghost);

        return hall;
    }

    private void registerActions()
    {
        actions = new HashMap<CommandWord, Action>();
        actions.put(CommandWord.GO, this::goRoom);
        actions.put(CommandWord.QUIT, this::quit);
        actions.put(CommandWord.HELP, this::printHelp);
        actions.put(CommandWord.LOOK, this::look);
        actions.put(CommandWord.BACK, this::back);
        actions.put(CommandWord.ITEMS, this::items);
        actions.put(CommandWord.TAKE, this::take);
        actions.put(CommandWord.DROP, this::drop);
        actions.put(CommandWord.EAT, this::eat);
        actions.put(CommandWord.UNLOCK, this::unlock);
        actions.put(CommandWord.TALK, this::talk);
        actions.put(CommandWord.GIVE, this::give);
        actions.put(CommandWord.CHARGE, this::charge);
        actions.put(CommandWord.FIRE, this::fire);
    }

    // ---------------------------------------------------------------- running

    /** Console play routine. Loops until end of play. */
    public void play()
    {
        start();
        while(!isFinished()) {
            processLine(parser.readLine());
        }
    }

    /** Show the opening text. */
    public void start()
    {
        say(MessageType.STORY, "You wake up in a cursed horror castle, the doors sealed by dark magic. Find the rusty key "
            + "and escape through the graveyard gate - the midnight bell tolls in " + MAX_COMMANDS + " commands!");
        say(MessageType.SYSTEM, "Type '" + CommandWord.HELP + "' if you need help.");
        say(MessageType.ROOM, player.getCurrentRoom().getLongDescription());
    }

    /** Process one line typed (or clicked) by the player. */
    public void processLine(String line)
    {
        if(isFinished()) {
            return;
        }
        boolean over = processCommand(parser.parse(line));
        if(over) {
            if(outcome == Outcome.PLAYING) {
                outcome = Outcome.QUIT;
            }
            say(MessageType.SYSTEM, "Thank you for playing.  Good bye.");
        }
    }

    private void say(MessageType type, String text)
    {
        output.show(type, text);
    }

    /** Validate a command, execute it, then advance time. @return true if the game is over. */
    private boolean processCommand(Command command)
    {
        CommandWord word = command.getCommandWord();
        if(word == CommandWord.UNKNOWN) {
            say(MessageType.ERROR, "I don't know what you mean...");
            return false;
        }
        if(word.needsSecondWord() && !command.hasSecondWord()) {
            say(MessageType.ERROR, capitalize(word.toString()) + " what?");
            return false;
        }
        if(!word.needsSecondWord() && command.hasSecondWord()) {
            say(MessageType.ERROR, "'" + word + "' does not take a second word.");
            return false;
        }

        boolean finished = actions.get(word).execute(command);
        commandsUsed++;
        if(finished) {
            return true;
        }
        moveCharacters();
        int left = getCommandsLeft();
        if(left <= 0) {
            outcome = Outcome.LOST;
            say(MessageType.ERROR, "The midnight bell tolls. The castle claims you forever... YOU LOSE!");
            return true;
        }
        if(left <= 10) {
            say(MessageType.SYSTEM, "(" + left + " commands left)");
        }
        return false;
    }

    private String capitalize(String text)
    {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void moveCharacters()
    {
        for(GameCharacter character : movingCharacters) {
            Room before = character.getCurrentRoom();
            character.move(random);
            Room here = player.getCurrentRoom();
            if(character.getCurrentRoom() == here && before != here) {
                say(MessageType.SYSTEM, "The " + character.getName() + " drifts into the room.");
            }
            else if(before == here && character.getCurrentRoom() != here) {
                say(MessageType.SYSTEM, "The " + character.getName() + " drifts away.");
            }
        }
    }

    // ---------------------------------------------------------------- commands (true = game ends)

    private boolean printHelp(Command command)
    {
        say(MessageType.INFO, "You are trapped in a cursed horror castle.\nFind the key and escape!\n\n"
            + "Your command words are:\n   " + parser.getCommands());
        return false;
    }

    private boolean goRoom(Command command)
    {
        String direction = command.getSecondWord();
        Room current = player.getCurrentRoom();
        Room next = current.getExit(direction);
        if(next == null) {
            say(MessageType.ERROR, "There is no door!");
            return false;
        }
        if(current.isLocked(direction)) {
            say(MessageType.ERROR, "The gate is locked. You need the rusty key.");
            return false;
        }
        player.moveTo(next);
        return arrive();
    }

    /** Called after the player entered a room: handles the transporter and the goal. */
    private boolean arrive()
    {
        if(player.getCurrentRoom().isTransporter()) {
            say(MessageType.STORY, "A cold crimson light grabs you and drags you away!");
            player.moveTo(teleportTargets.get(random.nextInt(teleportTargets.size())));
        }
        return showLocation();
    }

    private boolean showLocation()
    {
        say(MessageType.ROOM, player.getCurrentRoom().getLongDescription());
        if(player.getCurrentRoom() == goalRoom) {
            outcome = Outcome.WON;
            say(MessageType.SUCCESS, "You step out through the gate and breathe the cold night air.\n"
                + "CONGRATULATIONS - Willow escaped in " + (commandsUsed + 1) + " commands! YOU WIN!");
            return true;
        }
        return false;
    }

    private boolean look(Command command)
    {
        say(MessageType.ROOM, player.getCurrentRoom().getLongDescription());
        return false;
    }

    private boolean back(Command command)
    {
        if(!player.goBack()) {
            say(MessageType.ERROR, "You cannot go back - you are at the start of your journey.");
            return false;
        }
        return showLocation();
    }

    private boolean items(Command command)
    {
        say(MessageType.INFO, player.getInventoryString());
        return false;
    }

    private boolean take(Command command)
    {
        String name = command.getSecondWord();
        boolean had = player.hasItem(name);
        String result = player.takeItem(name);
        say(player.hasItem(name) && !had ? MessageType.SUCCESS : MessageType.ERROR, result);
        return false;
    }

    private boolean drop(Command command)
    {
        String name = command.getSecondWord();
        boolean had = player.hasItem(name);
        String result = player.dropItem(name);
        say(had && !player.hasItem(name) ? MessageType.SUCCESS : MessageType.ERROR, result);
        return false;
    }

    private boolean eat(Command command)
    {
        String name = command.getSecondWord();
        boolean had = player.hasItem(name);
        String result = player.eat(name);
        say(had && !player.hasItem(name) ? MessageType.SUCCESS : MessageType.ERROR, result);
        return false;
    }

    private boolean give(Command command)
    {
        String name = command.getSecondWord();
        boolean had = player.hasItem(name);
        String result = player.giveItem(name);
        say(had && !player.hasItem(name) ? MessageType.SUCCESS : MessageType.ERROR, result);
        return false;
    }

    private boolean talk(Command command)
    {
        GameCharacter character = player.getCurrentRoom().getCharacter(command.getSecondWord());
        if(character == null) {
            say(MessageType.ERROR, "There is no " + command.getSecondWord() + " here to talk to.");
        }
        else {
            say(MessageType.SPEECH, character.speak());
        }
        return false;
    }

    private boolean unlock(Command command)
    {
        String direction = command.getSecondWord();
        Room room = player.getCurrentRoom();
        if(room.getExit(direction) == null) {
            say(MessageType.ERROR, "There is no gate or door in that direction.");
        }
        else if(!room.isLocked(direction)) {
            say(MessageType.ERROR, "That door is not locked.");
        }
        else if(!player.hasItem("key")) {
            say(MessageType.ERROR, "You need a key to unlock this door.");
        }
        else {
            room.unlockExit(direction);
            say(MessageType.SUCCESS, "You unlock the gate to the " + direction + " with the rusty key. Click!");
        }
        return false;
    }

    private boolean charge(Command command)
    {
        Beamer beamer = player.getBeamer();
        if(beamer == null) {
            say(MessageType.ERROR, "You do not have the skull orb.");
        }
        else {
            beamer.charge(player.getCurrentRoom());
            say(MessageType.SUCCESS, "The orb flares red and memorises this room.");
        }
        return false;
    }

    private boolean fire(Command command)
    {
        Beamer beamer = player.getBeamer();
        if(beamer == null) {
            say(MessageType.ERROR, "You do not have the skull orb.");
            return false;
        }
        if(!beamer.isCharged()) {
            say(MessageType.ERROR, "The orb is not charged. Use 'charge' first.");
            return false;
        }
        say(MessageType.STORY, "POOF! The orb drags you back in a swirl of smoke!");
        player.moveTo(beamer.fire());
        return showLocation();
    }

    private boolean quit(Command command)
    {
        outcome = Outcome.QUIT;
        return true;
    }
}
