/**
 * A beamer (here: the witch's crystal orb) is an item that can be charged and fired. Charging memorises a
 * room; firing transports the player back to that room.
 */
public class Beamer extends Item
{
    private Room chargedRoom;

    public Beamer()
    {
        super("orb", "a cracked crystal orb with a skull inside (the beamer)", 500, true);
        chargedRoom = null;
    }

    public void charge(Room room) { chargedRoom = room; }
    public boolean isCharged() { return chargedRoom != null; }

    /** Fire the beamer: returns the memorised room and discharges the device. */
    public Room fire()
    {
        Room destination = chargedRoom;
        chargedRoom = null;
        return destination;
    }
}
