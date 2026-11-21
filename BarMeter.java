import java.awt.*;
import javax.swing.*;

/** An animated progress bar with a label and a "value / max" read-out. */
public class BarMeter extends JComponent
{
    private final String label;
    private final Color color;
    private int value;
    private int max = 1;
    private float shown;
    private boolean warn;
    private final javax.swing.Timer timer;

    public BarMeter(String label, Color color)
    {
        this.label = label;
        this.color = color;
        setPreferredSize(new Dimension(210, 46));
        timer = new javax.swing.Timer(15, e -> {
            float d = value - shown;
            if(Math.abs(d) < 0.3f) {
                shown = value;
                ((javax.swing.Timer) e.getSource()).stop();
            }
            else {
                shown += d * 0.2f;
            }
            repaint();
        });
    }

    public void setValues(int value, int max)
    {
        this.value = value;
        this.max = Math.max(1, max);
        if(!timer.isRunning()) {
            timer.start();
        }
    }

    public void setWarn(boolean warn)
    {
        if(this.warn != warn) {
            this.warn = warn;
            repaint();
        }
    }

    protected void paintComponent(Graphics g0)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        int w = getWidth();
        g.setFont(Theme.ui(Font.BOLD, 10));
        g.setColor(Theme.MUTED);
        g.drawString(label, 0, 14);
        String number = Math.round(shown) + " / " + max;
        g.setFont(Theme.ui(Font.BOLD, 13));
        g.setColor(warn ? Theme.RED : Theme.TEXT);
        g.drawString(number, w - g.getFontMetrics().stringWidth(number), 14);

        int y = 24, h = 12;
        g.setColor(new Color(0x2A1219));
        g.fillRoundRect(0, y, w, h, h, h);
        float ratio = Math.min(1f, shown / max);
        int fw = Math.max(ratio > 0 ? h : 0, (int) (w * ratio));
        Color c = warn ? Theme.RED : color;
        g.setPaint(new GradientPaint(0, 0, Theme.blend(c, Color.WHITE, 0.25f), w, 0, c));
        g.fillRoundRect(0, y, fw, h, h, h);
        g.setColor(new Color(255, 255, 255, 110));                  // shine
        g.fillRoundRect(3, y + 1, Math.max(0, fw - 6), 3, 3, 3);
        if(fw > h) {
            Theme.drawSparkle(g, fw - h / 2.0 - 1, y + h / 2.0, 4.5, Color.WHITE);
        }
        g.dispose();
    }
}
