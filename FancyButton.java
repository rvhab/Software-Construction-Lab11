import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/** A flat, rounded button with an animated hover effect. */
public class FancyButton extends JButton
{
    public enum Kind { PRIMARY, SECONDARY, GHOST, DANGER }

    private final Kind kind;
    private float hover = 0f;
    private float hoverTarget = 0f;
    private boolean pressed;
    private boolean compact;
    private boolean locked;
    private final javax.swing.Timer timer;

    public FancyButton(String text, Kind kind)
    {
        super(text);
        this.kind = kind;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFocusable(false);                 // keep the keyboard in the command field
        setFont(Theme.ui(Font.BOLD, 13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        timer = new javax.swing.Timer(15, e -> {
            float d = hoverTarget - hover;
            if(Math.abs(d) < 0.04f) {
                hover = hoverTarget;
                ((javax.swing.Timer) e.getSource()).stop();
            }
            else {
                hover += d * 0.35f;
            }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(isEnabled()) { hoverTarget = 1f; timer.start(); } }
            public void mouseExited(MouseEvent e) { hoverTarget = 0f; pressed = false; timer.start(); }
            public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
            public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
        });
    }

    /** Smaller version for use inside lists. */
    public FancyButton compact()
    {
        compact = true;
        setFont(Theme.ui(Font.BOLD, 11));
        return this;
    }

    public void setLocked(boolean locked)
    {
        if(this.locked != locked) {
            this.locked = locked;
            repaint();
        }
    }

    public Dimension getPreferredSize()
    {
        if(isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(getText()) + (compact ? 24 : 34) + (locked ? 16 : 0);
        return new Dimension(w, compact ? 26 : 36);
    }

    protected void paintComponent(Graphics g0)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        int w = getWidth(), h = getHeight() - 3;          // 3px left for the soft shadow
        Color top, bottom, border, text;
        switch(kind) {
            case PRIMARY:
                top = Theme.blend(new Color(0xE0505F), new Color(0xF06A78), hover);
                bottom = Theme.blend(new Color(0x8E1B2C), new Color(0xAA2538), hover);
                border = new Color(0xFFB0B8);
                text = Color.WHITE;
                break;
            case DANGER:
                top = Theme.blend(new Color(0x4A2434), new Color(0x5E2B41), hover);
                bottom = Theme.blend(new Color(0x3C1D2B), new Color(0x4D2336), hover);
                border = Theme.blend(new Color(0x8F3A52), Theme.RED, hover);
                text = new Color(0xFF8FA3);
                break;
            case GHOST:
                top = Theme.alpha(Theme.PANEL_LIGHT, 120);
                bottom = Theme.alpha(Theme.PANEL_LIGHT, 200);
                border = Theme.PANEL_BORDER;
                text = Theme.TEXT;
                break;
            default:
                top = Theme.blend(new Color(0x3A1620), new Color(0x54202E), hover);
                bottom = Theme.blend(new Color(0x2A1018), new Color(0x401A26), hover);
                border = Theme.blend(new Color(0x7A2433), Theme.GOLD, hover);
                text = Theme.TEXT;
                break;
        }
        if(!isEnabled()) {
            top = new Color(0x1A0D12);
            bottom = new Color(0x160A0F);
            border = new Color(0x2E1620);
            text = new Color(0x70585F);
        }
        int arc = h;                                      // fully rounded "pill" ends
        int down = (pressed && isEnabled()) ? 1 : 0;
        if(isEnabled() && !pressed) {
            g.setColor(kind == Kind.PRIMARY ? new Color(170, 37, 56, 80) : new Color(0, 0, 0, 60));
            g.fillRoundRect(1, 3, w - 3, h, arc, arc);
        }
        g.setPaint(new GradientPaint(0, down, top, 0, h + down, bottom));
        g.fillRoundRect(0, down, w - 1, h - 1, arc, arc);
        g.setColor(border);
        g.drawRoundRect(0, down, w - 1, h - 1, arc, arc);
        if(kind == Kind.PRIMARY && isEnabled()) {          // glossy highlight
            g.setColor(new Color(255, 255, 255, 70));
            g.fillRoundRect(5, down + 2, w - 11, h / 2 - 2, arc, arc);
        }
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int shift = locked ? -7 : 0;
        int tx = (w - fm.stringWidth(getText())) / 2 + shift;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent() + down;
        g.setColor(text);
        g.drawString(getText(), tx, ty);
        if(locked) {
            Theme.drawLock(g, w - 15, h / 2 + 3 + down, Theme.GOLD_DARK);
        }
        g.dispose();
    }
}
