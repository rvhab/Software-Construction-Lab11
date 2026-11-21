import java.awt.*;
import java.awt.geom.*;
import java.util.Random;
import javax.swing.*;

/**
 * A panel with a gothic frame instead of rounded corners. Two shapes:
 *   CHAMFER - square with cut corners, iron rivets and blood drips (most panels)
 *   ARCH    - a pointed cathedral-window top (used for the map)
 * Only the shape and decoration are drawn here; what is inside is up to the owner.
 */
public class GothicPanel extends JPanel
{
    public enum Frame { CHAMFER, ARCH }

    private final Color fill;
    private final int cut;
    private final Frame kind;

    public GothicPanel(LayoutManager layout, Color fill, int cut, Frame kind)
    {
        super(layout);
        this.fill = fill;
        this.cut = cut;
        this.kind = kind;
        setOpaque(false);
    }

    public GothicPanel(LayoutManager layout, Color fill, int cut)
    {
        this(layout, fill, cut, Frame.CHAMFER);
    }

    private Path2D.Double outline(double inset)
    {
        double w = getWidth() - 1 - inset * 2, h = getHeight() - 4 - inset * 2, x = inset, y = inset;
        Path2D.Double p = new Path2D.Double();
        if(kind == Frame.ARCH) {
            double a = Math.min(h * 0.18, 46);
            p.moveTo(x, y + h - cut * 0.6);
            p.lineTo(x, y + a + 16);
            p.quadTo(x, y + a * 0.4, x + w / 2, y);
            p.quadTo(x + w, y + a * 0.4, x + w, y + a + 16);
            p.lineTo(x + w, y + h - cut * 0.6);
            p.lineTo(x + w - cut * 0.6, y + h);
            p.lineTo(x + cut * 0.6, y + h);
        }
        else {
            p.moveTo(x + cut, y);
            p.lineTo(x + w - cut, y);
            p.lineTo(x + w, y + cut);
            p.lineTo(x + w, y + h - cut);
            p.lineTo(x + w - cut, y + h);
            p.lineTo(x + cut, y + h);
            p.lineTo(x, y + h - cut);
            p.lineTo(x, y + cut);
        }
        p.closePath();
        return p;
    }

    protected void paintComponent(Graphics g0)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        int w = getWidth(), h = getHeight();
        Path2D.Double shape = outline(0);

        g.translate(2, 4);                                                // drop shadow
        g.setColor(new Color(0, 0, 0, 110));
        g.fill(shape);
        g.translate(-2, -4);

        g.setPaint(new GradientPaint(0, 0, Theme.blend(fill, Color.WHITE, 0.06f), 0, h, Theme.blend(fill, Theme.ROSE_DARK, 0.06f)));
        g.fill(shape);
        g.setColor(Theme.PANEL_BORDER);                                   // thick outer iron frame
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.draw(shape);
        g.setColor(new Color(255, 255, 255, 26));                         // thin inner line
        g.setStroke(new BasicStroke(1f));
        g.draw(outline(5));

        // blood drips hanging from the top edge
        Random r = new Random(w * 31L + h);
        g.setColor(new Color(0x9E1B2B));
        int drips = Math.max(2, w / 110);
        for(int i = 0; i < drips; i++) {
            double dx = cut + 14 + r.nextDouble() * Math.max(10, w - 2 * cut - 28);
            if(kind == Frame.ARCH && Math.abs(dx - w / 2.0) > w * 0.3) {
                continue;
            }
            double top = kind == Frame.ARCH ? 2 + (1 - Math.abs(dx - w / 2.0) / (w / 2.0)) * 0 : 1;
            double len = 6 + r.nextDouble() * 12;
            Path2D.Double d = new Path2D.Double();
            d.moveTo(dx - 3, top + (kind == Frame.ARCH ? 12 : 0));
            d.lineTo(dx + 3, top + (kind == Frame.ARCH ? 12 : 0));
            d.lineTo(dx + 1.5, top + len + (kind == Frame.ARCH ? 12 : 0));
            d.quadTo(dx, top + len + 5 + (kind == Frame.ARCH ? 12 : 0), dx - 1.5, top + len + (kind == Frame.ARCH ? 12 : 0));
            d.closePath();
            if(kind == Frame.CHAMFER) {
                g.fill(d);
            }
        }

        // rivets in the corners (or a pointed jewel at the top of the arch)
        g.setColor(new Color(0xCFC3AE));
        double rv = 3;
        if(kind == Frame.CHAMFER) {
            double[][] spots = { { cut * 0.55, cut * 0.55 }, { w - cut * 0.55, cut * 0.55 },
                                 { cut * 0.55, h - 4 - cut * 0.55 }, { w - cut * 0.55, h - 4 - cut * 0.55 } };
            for(double[] s : spots) {
                g.fill(new Ellipse2D.Double(s[0] - rv, s[1] - rv, rv * 2, rv * 2));
            }
        }
        else {
            g.setColor(Theme.ROSE_DARK);
            g.fill(new Ellipse2D.Double(w / 2.0 - 4, 6, 8, 8));
            g.setColor(new Color(0xCFC3AE));
            g.draw(new Ellipse2D.Double(w / 2.0 - 4, 6, 8, 8));
            g.fill(new Ellipse2D.Double(cut * 0.4, h - 4 - cut * 0.4 - rv, rv * 2, rv * 2));
            g.fill(new Ellipse2D.Double(w - cut * 0.4 - rv * 2, h - 4 - cut * 0.4 - rv, rv * 2, rv * 2));
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
