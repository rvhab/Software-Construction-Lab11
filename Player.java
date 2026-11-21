import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * The player: stores the current room, the carried items, the maximum weight
 * that can be carried and the history of visited rooms (for "back").
 */
public class Player
{
    private String name;
    private Room currentRoom;
    private HashMap<String, Item> items;
    private int maxWeight;
    private Stack<Room> history;
    private HashSet<Room> visited;

    public Player(String name, Room startRoom, int maxWeight)
    {
        this.name = name;
        this.currentRoom = startRoom;
        this.maxWeight = maxWeight;
        items = new HashMap<String, Item>();
        history = new Stack<Room>();
        visited = new HashSet<Room>();
        visited.add(startRoom);
    }

    public String getName() { return name; }
    public Room getCurrentRoom() { return currentRoom; }

    /** Move to another room, remembering where we came from. */
    public void moveTo(Room room)
    {
        history.push(currentRoom);
        currentRoom = room;
        visited.add(room);
    }

    /** Go back to the previous room. @return false if there is none. */
    public boolean goBack()
    {
        if(history.isEmpty()) {
            return false;
        }
        currentRoom = history.pop();
        return true;
    }

    public boolean hasVisited(Room room) { return visited.contains(room); }
    public int getMaxWeight() { return maxWeight; }
    public Collection<Item> getItems() { return items.values(); }

    public int getTotalWeight()
    {
        int total = 0;
        for(Item item : items.values()) {
            total += item.getWeight();
        }
        return total;
    }

    public boolean hasItem(String name) { return items.containsKey(name); }

    /** @return The carried beamer, or null if the player has none. */
    public Beamer getBeamer()
    {
        for(Item item : items.values()) {
            if(item instanceof Beamer) {
                return (Beamer) item;
            }
        }
        return null;
    }

    /** Pick up an item from the current room (checks existence, pick-up flag and weight). */
    public String takeItem(String itemName)
    {
        Item item = currentRoom.getItem(itemName);
        if(item == null) {
            return "There is no " + itemName + " here.";
        }
        if(!item.canBePickedUp()) {
            return "The " + itemName + " cannot be picked up.";
        }
        if(getTotalWeight() + item.getWeight() > maxWeight) {
            return "The " + itemName + " is too heavy. You can carry at most "
                   + maxWeight + "g and already carry " + getTotalWeight() + "g.";
        }
        currentRoom.removeItem(itemName);
        items.put(itemName, item);
        return "You picked up the " + itemName + ".";
    }

    /** Drop a carried item into the current room. */
    public String dropItem(String itemName)
    {
        Item item = items.remove(itemName);
        if(item == null) {
            return "You are not carrying a " + itemName + ".";
        }
        currentRoom.addItem(item);
        return "You dropped the " + itemName + ".";
    }

    /** Eat a carried item; magic items raise the carrying limit. */
    public String eat(String itemName)
    {
        Item item = items.get(itemName);
        if(item == null) {
            return "You are not carrying a " + itemName + ".";
        }
        if(!item.isEdible()) {
            return "You cannot eat the " + itemName + "!";
        }
        items.remove(itemName);
        maxWeight += item.getMagicBonus();
        return "You eat the " + itemName + ". You feel stronger! You can now carry up to "
               + maxWeight + "g.";
    }

    /** Give an item to a character in the room who wants it. */
    public String giveItem(String itemName)
    {
        Item item = items.get(itemName);
        if(item == null) {
            return "You are not carrying a " + itemName + ".";
        }
        for(GameCharacter character : currentRoom.getCharacters()) {
            if(character.wants(itemName)) {
                items.remove(itemName);
                String result = character.getName() + " takes the " + itemName + " and thanks you.";
                Item reward = character.getReward();
                if(reward != null) {
                    items.put(reward.getName(), reward);
                    result += " You receive: " + reward.getLongDescription();
                }
                return result;
            }
        }
        return "Nobody here wants the " + itemName + ".";
    }

    /** @return A description of the carried items and total weight. */
    public String getInventoryString()
    {
        if(items.isEmpty()) {
            return "You are carrying nothing. (Limit: " + maxWeight + "g)";
        }
        String result = "You are carrying:";
        for(Item item : items.values()) {
            result += "\n   " + item.getLongDescription();
        }
        result += "\nTotal weight: " + getTotalWeight() + "g (limit " + maxWeight + "g)";
        return result;
    }
}
