/**
 * A character in the game. Characters speak when talked to and may give the
 * player a reward in exchange for a wanted item. Moving characters wander
 * into a random neighbouring room after every command.
 */
public class GameCharacter
{
    private String name;
    private String speech;
    private Room currentRoom;
    private boolean moving;
    private String wantedItemName;
    private Item reward;

    public GameCharacter(String name, String speech, Room startRoom, boolean moving)
    {
        this.name = name;
        this.speech = speech;
        this.moving = moving;
        this.currentRoom = startRoom;
        startRoom.addCharacter(this);
    }

    /** This character will give 'reward' to a player who gives it 'itemName'. */
    public void setRequest(String itemName, Item reward)
    {
        this.wantedItemName = itemName;
        this.reward = reward;
    }

    public String getName() { return name; }
    public Room getCurrentRoom() { return currentRoom; }
    public boolean isMoving() { return moving; }
    public String speak() { return name + " says: \"" + speech + "\""; }
    public boolean wants(String itemName) { return itemName.equals(wantedItemName); }
    public Item getReward() { return reward; }

    /** Move to a random adjoining room (does nothing for non-moving characters). */
    public void move(java.util.Random random)
    {
        if(!moving) {
            return;
        }
        Room next = currentRoom.getRandomNeighbor(random);
        currentRoom.removeCharacter(name);
        currentRoom = next;
        currentRoom.addCharacter(this);
    }
}
