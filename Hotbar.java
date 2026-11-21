import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * Willow's pockets: a grid of slots, one slot per carried item. Clicking a slot
 * opens a small menu (Eat / Give / Drop). It only shows state and sends
 * commands; it never changes the game itself.
 */
public class Hotbar extends JComponent
{
    private static final int COLS = 4, ROWS = 3, SLOT = 58, GAP = 8;

    private final Consumer<String> commands;
    private final ArrayList<Item> shown = new ArrayList<Item>();
    private Room room;
    private int hover = -1;
    private double time;

    public Hotbar(Consumer<String> commands)
    {
        this.commands = commands;
        setOpaque(false);
        setPreferredSize(new Dimension(COLS * SLOT + (COLS - 1) * GAP, ROWS * SLOT + (ROWS - 1) * GAP));
        ToolTipManager.sharedInstance().registerComponent(this);
        addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e)
            {
                int i = slotAt(e.getX(), e.getY());
                if(i != hover) {
                    hover = i;
                    setCursor(Cursor.getPredefinedCursor(i >= 0 && i < shown.size() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) { hover = -1; }
            public void mouseClicked(MouseEvent e)
            {
                int i = slotAt(e.getX(), e.getY());
                if(i >= 0 && i < shown.size()) {
                    popup(shown.get(i), e.getX(), e.getY());
                }
            }
        });
        new javax.swing.Timer(50, e -> {
            time += 0.05;
            if(isShowing()) {
                repaint();
            }
        }).start();
    }

    /** Show what the player carries; the room tells us whether somebody is here to give things to. */
    public void setContents(Player player, Room room)
    {
        shown.clear();
        shown.addAll(player.getItems());
        this.room = room;
        repaint();
    }

    public String getToolTipText(MouseEvent e)
    {
        int i = slotAt(e.getX(), e.getY());
        if(i >= 0 && i < shown.size()) {
            Item it = shown.get(i);
            return it.getName() + ": " + it.getDescription() + " (" + it.getWeight() + " g)";
        }
        return null;
    }

    /** A coffin-shaped slot: narrow head, wide shoulders, narrow feet. */
    private Path2D.Double coffin(int x, int y)
    {
        double w = SLOT, hh = SLOT;
        Path2D.Double p = new Path2D.Double();
        p.moveTo(x + w * 0.30, y);
        p.lineTo(x + w * 0.70, y);
        p.lineTo(x + w, y + hh * 0.26);
        p.lineTo(x + w * 0.80, y + hh);
        p.lineTo(x + w * 0.20, y + hh);
        p.lineTo(x, y + hh * 0.26);
        p.closePath();
        return p;
    }

    private int slotAt(int x, int y)
    {
        int col = x / (SLOT + GAP), row = y / (SLOT + GAP);
        if(col >= COLS || row >= ROWS || x % (SLOT + GAP) >= SLOT || y % (SLOT + GAP) >= SLOT) {
            return -1;
        }
        return row * COLS + col;
    }

    private void popup(Item item, int x, int y)
    {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.PANEL);
        menu.setBorder(BorderFactory.createLineBorder(Theme.PANEL_BORDER, 1));
        JLabel title = new JLabel("  " + item.getName().toUpperCase() + "  ");
        title.setFont(Theme.ui(Font.BOLD, 11));
        title.setForeground(Theme.GOLD);
        menu.add(title);
        if(item.isEdible()) {
            menu.add(entry("Eat it", "eat " + item.getName()));
        }
        if(room != null && !room.getCharacters().isEmpty()) {
            menu.add(entry("Give it away", "give " + item.getName()));
        }
        menu.add(entry("Drop it", "drop " + item.getName()));
        menu.show(this, x, y);
    }

    private JMenuItem entry(String label, String command)
    {
        JMenuItem mi = new JMenuItem(label);
        mi.setFont(Theme.ui(Font.PLAIN, 13));
        mi.setForeground(Theme.TEXT);
        mi.setBackground(Theme.PANEL);
        mi.addActionListener(e -> commands.accept(command));
        return mi;
    }

    protected void paintComponent(Graphics g0)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        for(int i = 0; i < COLS * ROWS; i++) {
            int x = (i % COLS) * (SLOT + GAP), y = (i / COLS) * (SLOT + GAP);
            boolean full = i < shown.size();
            boolean over = i == hover && full;
            Path2D.Double coffin = coffin(x, y);
            g.setPaint(new GradientPaint(0, y, new Color(0x2E1319), 0, y + SLOT, new Color(0x1A0A0E)));
            g.fill(coffin);
            g.setColor(over ? Theme.GOLD : full ? Theme.alpha(Theme.ROSE, 170) : new Color(0x4A1E28));
            g.setStroke(new BasicStroke(over ? 2f : 1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g.draw(coffin);
            if(full) {
                Item it = shown.get(i);
                boolean charged = it instanceof Beamer && ((Beamer) it).isCharged();
                double bob = Math.sin(time * 2.2 + i) * 1.4;
                ItemArt.draw(g, it.getName(), x + SLOT / 2.0, y + SLOT / 2.0 - 5 + bob, 34, time, charged);
                g.setFont(Theme.ui(Font.BOLD, 9));
                g.setColor(Theme.MUTED);
                String n = it.getName();
                g.drawString(n, x + (SLOT - g.getFontMetrics().stringWidth(n)) / 2, y + SLOT - 6);
            }
        }
        g.dispose();
    }
}
