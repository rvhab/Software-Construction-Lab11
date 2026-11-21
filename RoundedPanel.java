import java.awt.*;
import javax.swing.*;

/** A panel with rounded corners, a soft shadow and a thin border. */
public class RoundedPanel extends JPanel
{
    private final Color fill;
    private final int arc;

    public RoundedPanel(LayoutManager layout, Color fill, int arc)
    {
        super(layout);
        this.fill = fill;
        this.arc = arc;
        setOpaque(false);
    }

    protected void paintComponent(Graphics g0)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        int w = getWidth(), h = getHeight();
        for(int i = 5; i >= 1; i--) {                     // soft lilac shadow
            g.setColor(new Color(0, 0, 0, 22));
            g.fillRoundRect(2 - i, 4 - i + 2, w - 4 + 2 * i, h - 4 + 2 * i, arc + i * 2, arc + i * 2);
        }
        g.setPaint(new GradientPaint(0, 0, Theme.blend(fill, Color.WHITE, 0.07f), 0, h, Theme.blend(fill, Theme.ROSE, 0.04f)));
        g.fillRoundRect(0, 0, w - 1, h - 3, arc, arc);
        g.setColor(Theme.PANEL_BORDER);
        g.drawRoundRect(0, 0, w - 1, h - 3, arc, arc);
        g.setColor(new Color(255, 255, 255, 18));             // inner rim = glassy edge
        g.drawRoundRect(1, 1, w - 3, h - 5, arc - 2, arc - 2);
        g.dispose();
        super.paintComponent(g0);
    }
}
