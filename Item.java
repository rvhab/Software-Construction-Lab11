/**
 * An item in the game: it has a name, a description and a weight.
 * Some items cannot be picked up; some (like the magic cookie) have a magic
 * effect when eaten.
 */
public class Item
{
    private String name;
    private String description;
    private int weight;
    private boolean canBePickedUp;
    private int magicBonus;   // extra carrying capacity gained when eaten

    public Item(String name, String description, int weight, boolean canBePickedUp)
    {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.canBePickedUp = canBePickedUp;
        this.magicBonus = 0;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWeight() { return weight; }
    public boolean canBePickedUp() { return canBePickedUp; }

    /** Make this item edible; eating it raises the carrying limit by bonus. */
    public void setMagicBonus(int bonus) { magicBonus = bonus; }
    public int getMagicBonus() { return magicBonus; }
    public boolean isEdible() { return magicBonus > 0; }

    /** @return e.g. "book: an old dusty book (1200g)" */
    public String getLongDescription()
    {
        return name + ": " + description + " (" + weight + "g)";
    }
}
