import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * A room (location) in the game. A room stores its exits, items and
 * characters and is responsible for describing itself.
 * All fields are private: other classes only use the methods below, so how
 * exits are stored can change without breaking other classes.
 */
public class Room
{
    private String name;
    private String description;
    private HashMap<String, Room> exits;
    private HashSet<String> lockedExits;
    private HashMap<String, Item> items;
    private HashMap<String, GameCharacter> characters;
    private boolean transporter;

    /** Create a room described e.g. "a kitchen". Initially it has no exits. */
    public Room(String description)
    {
        this(description, description);
    }

    /** Create a room with a short name (e.g. "Kitchen") and a description (e.g. "in a dusty kitchen"). */
    public Room(String name, String description)
    {
        this.name = name;
        this.description = description;
        exits = new HashMap<String, Room>();
        lockedExits = new HashSet<String>();
        items = new HashMap<String, Item>();
        characters = new HashMap<String, GameCharacter>();
        transporter = false;
    }

    public String getName() { return name; }

    /** @return a read-only view of all exits (direction -> room). */
    public Map<String, Room> getExits() { return Collections.unmodifiableMap(exits); }

    /** @return the items lying in this room. */
    public Collection<Item> getItems() { return items.values(); }

    /** Define one exit. Exits are one-way: set the reverse exit separately (or not, for a trapdoor). */
    public void setExit(String direction, Room neighbor)
    {
        exits.put(direction, neighbor);
    }

    /** @return The room reached in this direction, or null if there is none. */
    public Room getExit(String direction)
    {
        return exits.get(direction);
    }

    public void lockExit(String direction) { lockedExits.add(direction); }
    public void unlockExit(String direction) { lockedExits.remove(direction); }
    public boolean isLocked(String direction) { return lockedExits.contains(direction); }

    /** @return e.g. "Exits: north east(locked)" */
    public String getExitString()
    {
        String result = "Exits:";
        for(String direction : exits.keySet()) {
            result += " " + direction;
            if(lockedExits.contains(direction)) {
                result += "(locked)";
            }
        }
        return result;
    }

    /** @return Full description: location, exits, items and characters. */
    public String getLongDescription()
    {
        String result = "You are " + description + ".\n" + getExitString();
        if(!items.isEmpty()) {
            result += "\n" + getItemString();
        }
        if(!characters.isEmpty()) {
            result += "\n" + getCharacterString();
        }
        return result;
    }

    public String getItemString()
    {
        String result = "Items here:";
        for(Item item : items.values()) {
            result += "\n   " + item.getLongDescription();
        }
        return result;
    }

    public String getCharacterString()
    {
        String result = "People here:";
        for(String name : characters.keySet()) {
            result += " " + name;
        }
        return result;
    }

    public void addItem(Item item) { items.put(item.getName(), item); }
    public Item getItem(String name) { return items.get(name); }
    public Item removeItem(String name) { return items.remove(name); }

    public void addCharacter(GameCharacter c) { characters.put(c.getName(), c); }
    public GameCharacter getCharacter(String name) { return characters.get(name); }
    public GameCharacter removeCharacter(String name) { return characters.remove(name); }
    public Collection<GameCharacter> getCharacters() { return characters.values(); }

    public void setTransporter(boolean transporter) { this.transporter = transporter; }
    public boolean isTransporter() { return transporter; }

    /** @return A random adjoining room (this room if there are no exits). */
    public Room getRandomNeighbor(Random random)
    {
        if(exits.isEmpty()) {
            return this;
        }
        ArrayList<Room> neighbors = new ArrayList<Room>(exits.values());
        return neighbors.get(random.nextInt(neighbors.size()));
    }
}
