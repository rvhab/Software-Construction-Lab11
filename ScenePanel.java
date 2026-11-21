import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * The big picture behind the whole window. It shows the room Willow is in,
 * Willow herself (she walks in from the side she came from, or vanishes in a
 * puff of stars when she is teleported), the items on the floor, the friends
 * who are here and glowing signs for every exit.
 *
 * Everything on the scene can be clicked: an exit sign walks through that exit,
 * an item is picked up, a friend is talked to. The scene only DRAWS the game and
 * SENDS commands - the rules stay in Game, Room, Player...
 */
public class ScenePanel extends JComponent
{
    /** Something on the scene the mouse can click. */
    private static class Hot
    {
        Shape shape;
        String label;
        Runnable action;
    }

    private static class Particle
    {
        double x, y, vx, vy, life, decay, size;
        int kind;                           // 0 sparkle, 1 heart, 2 star, 3 firefly
        Color color;
    }

    private final Consumer<String> commands;
    private final Consumer<String> notes;
    private Game game;

    // layout: the part of the window that is not covered by panels
    private int insTop = 88, insLeft = 314, insBottom = 244, insRight = 324;

    private double time;
    private Room shownRoom;
    private int enterMode;                   // 0 none, 1 from the left, 2 from the right, 3 from the depth, 4 teleport
    private double enterT = 1;
    private double wx;
    private double walkPhase, moveAmt, facing, hop = -1;
    private Avatar.Mood mood = Avatar.Mood.NORMAL;
    private long moodUntil;
    private String emote = "";
    private long emoteUntil;
    private int celebrate;

    private final ArrayList<Hot> hots = new ArrayList<Hot>();
    private Hot hover;
    private Point mouse = new Point(-100, -100);
    private final ArrayList<Particle> particles = new ArrayList<Particle>();
    private final Random random = new Random();

    public ScenePanel(Consumer<String> commands, Consumer<String> notes)
    {
        this.commands = commands;
        this.notes = notes;
        setOpaque(true);
        addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e)
            {
                mouse = e.getPoint();
                updateHover();
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e)
            {
                mouse = new Point(-100, -100);
                updateHover();
            }

            public void mouseClicked(MouseEvent e)
            {
                updateHover();
                if(hover != null && game != null && !game.isFinished()) {
                    hover.action.run();
                }
            }
        });
        new javax.swing.Timer(33, e -> tick()).start();
    }

    public void setGame(Game game)
    {
        this.game = game;
        shownRoom = null;
        enterT = 1;
        enterMode = 0;
        particles.clear();
        celebrate = 0;
        hover = null;
        repaint();
    }

    /** Tell the scene which parts of the window are covered by panels. */
    public void setInsets(int top, int left, int bottom, int right)
    {
        insTop = top;
        insLeft = left;
        insBottom = bottom;
        insRight = right;
    }

    /** Willow reacts to something for a moment. */
    public void react(Avatar.Mood m, String emoteKind)
    {
        mood = m;
        moodUntil = System.currentTimeMillis() + 1500;
        emote = emoteKind;
        emoteUntil = System.currentTimeMillis() + 1700;
    }

    // ------------------------------------------------------------------ geometry

    private double left() { return insLeft; }
    private double right() { return getWidth() - insRight; }
    private double top() { return insTop; }
    private double bottom() { return getHeight() - insBottom; }
    private double centerX() { return (left() + right()) / 2; }
    private double centerY() { return (top() + bottom()) / 2; }
    private double safeH() { return Math.max(200, bottom() - top()); }
    private double wallLine() { return top() + safeH() * 0.50; }
    private double feetY() { return top() + safeH() * 0.82; }
    private double unit() { return Math.max(0.7, Math.min(1.5, safeH() / 520.0)); }
    private double witchScale() { return Math.max(1.2, Math.min(3.4, safeH() * 0.5 / 96.0)); }

    // ------------------------------------------------------------------ animation

    private void tick()
    {
        time += 0.033;
        if(game != null) {
            syncRoom();
            stepWitch();
            stepParticles();
            ambient();
        }
        if(isShowing()) {
            repaint();
        }
    }

    private String opposite(String d)
    {
        switch(d) {
            case "north": return "south";
            case "south": return "north";
            case "east": return "west";
            case "west": return "east";
            case "up": return "down";
            default: return "up";
        }
    }

    /** In which direction did the player walk to get from one room to another? (null = teleported) */
    private String directionBetween(Room from, Room to)
    {
        for(String d : from.getExits().keySet()) {
            if(from.getExit(d) == to) {
                return d;
            }
        }
        for(String d : to.getExits().keySet()) {
            if(to.getExit(d) == from) {
                return opposite(d);
            }
        }
        return null;
    }

    private void syncRoom()
    {
        Room cur = game.getPlayer().getCurrentRoom();
        if(shownRoom == null) {
            shownRoom = cur;
            wx = centerX();
            enterT = 1;
        }
        else if(cur != shownRoom) {
            String d = directionBetween(shownRoom, cur);
            if(d == null) {
                burst(wx, feetY() - 80, 12, 0);                       // poof! she vanishes ...
            }
            shownRoom = cur;
            enterMode = d == null ? 4 : d.equals("east") ? 1 : d.equals("west") ? 2 : 3;
            enterT = 0;
        }
    }

    private void stepWitch()
    {
        double target = centerX();
        boolean entering = enterT < 1;
        double prev = wx;
        if(entering) {
            enterT = Math.min(1, enterT + 0.033 / (enterMode == 4 ? 0.8 : 1.0));
        }
        double ease = 1 - Math.pow(1 - enterT, 3);
        if(enterMode == 1) {
            wx = (left() - 50) + (target - (left() - 50)) * ease;
        }
        else if(enterMode == 2) {
            wx = (right() + 50) + (target - (right() + 50)) * ease;
        }
        else {
            wx = target;
        }
        boolean walking = entering && (enterMode == 1 || enterMode == 2);
        if(walking) {
            walkPhase += 0.35;
            moveAmt = Math.min(1, moveAmt + 0.25);
            facing += (Math.signum(wx - prev) - facing) * 0.3;
        }
        else {
            moveAmt = Math.max(0, moveAmt - 0.2);
            facing *= 0.8;
        }
        if(entering && enterT >= 1) {                                    // she has just arrived
            hop = 0;
            burst(wx, feetY() - 80, enterMode == 4 ? 12 : 5, enterMode == 4 ? 2 : 1);
        }
        if(hop >= 0) {
            hop += 0.08;
            if(hop >= 1) {
                hop = -1;
            }
        }
        if(game.isFinished() && game.getOutcome() == Game.Outcome.WON && ++celebrate % 6 == 0) {
            burst(wx + random.nextGaussian() * 90, feetY() - 150, 1, random.nextBoolean() ? 1 : 2);
        }
    }

    private void ambient()
    {
        if(random.nextDouble() < 0.05) {                                  // drifting fireflies
            Particle p = new Particle();
            p.x = random.nextDouble() * getWidth();
            p.y = wallLine() + random.nextDouble() * (getHeight() - wallLine());
            p.vx = (random.nextDouble() - 0.5) * 0.5;
            p.vy = -0.2 - random.nextDouble() * 0.3;
            p.life = 1;
            p.decay = 0.004;
            p.size = 3 + random.nextDouble() * 3;
            p.kind = 3;
            p.color = random.nextBoolean() ? new Color(0xFFE08A) : Theme.MINT;
            particles.add(p);
        }
        if(enterT >= 1 && random.nextDouble() < 0.07) {                   // magic dust from the hat
            double s = witchScale();
            Particle p = new Particle();
            p.x = wx + 14 * s + random.nextGaussian() * 6;
            p.y = feetY() - 88 * s + random.nextGaussian() * 6;
            p.vx = random.nextGaussian() * 0.3;
            p.vy = 0.3 + random.nextDouble() * 0.4;
            p.life = 1;
            p.decay = 0.025;
            p.size = 4 + random.nextDouble() * 4;
            p.kind = 0;
            p.color = random.nextBoolean() ? Theme.GOLD : Theme.ROSE;
            particles.add(p);
        }
    }

    private void burst(double x, double y, int count, int kind)
    {
        Color[] palette = { Theme.ROSE, Theme.LAVENDER, Theme.GOLD, Theme.MINT, Color.WHITE };
        for(int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = x + random.nextGaussian() * 8;
            p.y = y + random.nextGaussian() * 12;
            double a = random.nextDouble() * Math.PI * 2, sp = 1 + random.nextDouble() * 2.6;
            p.vx = Math.cos(a) * sp;
            p.vy = Math.sin(a) * sp - 1.2;
            p.life = 1;
            p.decay = 0.02;
            p.size = 6 + random.nextDouble() * 8;
            p.kind = kind;
            p.color = kind == 1 ? (random.nextBoolean() ? Theme.ROSE : Theme.ROSE_DARK) : palette[random.nextInt(palette.length)];
            particles.add(p);
        }
    }

    private void stepParticles()
    {
        for(Iterator<Particle> it = particles.iterator(); it.hasNext(); ) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            if(p.kind != 3) {
                p.vx *= 0.97;
                p.vy = p.vy * 0.97 + 0.02;
            }
            p.life -= p.decay;
            if(p.life <= 0) {
                it.remove();
            }
        }
    }

    private void updateHover()
    {
        Hot found = null;
        for(int i = hots.size() - 1; i >= 0 && found == null; i--) {
            if(hots.get(i).shape.contains(mouse)) {
                found = hots.get(i);
            }
        }
        hover = found;
        setCursor(Cursor.getPredefinedCursor(found != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    // ------------------------------------------------------------------ drawing

    protected void paintComponent(Graphics g0)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        int w = getWidth(), h = getHeight();
        if(game == null || shownRoom == null) {
            g.setColor(Theme.BG_TOP);
            g.fillRect(0, 0, w, h);
            g.dispose();
            return;
        }
        Room room = shownRoom;
        RoomArt.draw(g, room.getName(), w, h, (int) centerX(), (int) wallLine(), unit(), time);

        float[] f = { 0f, 0.55f, 1f };                                  // dark edges keep the panels readable
        g.setPaint(new RadialGradientPaint(new Point2D.Double(w / 2.0, h * 0.45), (float) (Math.max(w, h) * 0.72), f,
                   new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(4, 2, 14, 190) }));
        g.fillRect(0, 0, w, h);

        hots.clear();
        drawItems(g, room);
        drawFriends(g, room);
        drawWitch(g);
        drawParticles(g);
        drawExits(g, room);
        drawEmote(g);
        drawHoverLabel(g);

        if(enterT < 1) {                                                // fade in from the dark
            g.setColor(new Color(6, 4, 22, (int) (210 * (1 - enterT))));
            g.fillRect(0, 0, w, h);
        }
        g.dispose();
    }

    private Avatar.Mood currentMood()
    {
        if(game.isFinished()) {
            return game.getOutcome() == Game.Outcome.WON ? Avatar.Mood.HAPPY
                 : game.getOutcome() == Game.Outcome.LOST ? Avatar.Mood.SAD : Avatar.Mood.WAVE;
        }
        return System.currentTimeMillis() < moodUntil ? mood : Avatar.Mood.NORMAL;
    }

    private void drawWitch(Graphics2D g)
    {
        double s = witchScale(), feet = feetY();
        double ease = 1 - Math.pow(1 - enterT, 3);
        double alpha = (enterMode == 3 || enterMode == 4) ? Math.max(0, ease) : 1;
        double size = enterMode == 3 ? 0.75 + 0.25 * ease : enterMode == 4 ? 0.4 + 0.6 * ease : 1;
        float[] f = { 0f, 1f };                                         // magic glow on the floor
        g.setPaint(new RadialGradientPaint(new Point2D.Double(wx, feet), (float) (70 * s), f,
                   new Color[] { Theme.alpha(Theme.LAVENDER, (int) (90 * alpha)), Theme.alpha(Theme.LAVENDER, 0) }));
        g.fill(new Ellipse2D.Double(wx - 70 * s, feet - 22 * s, 140 * s, 44 * s));
        double lift = hop >= 0 ? Math.sin(hop * Math.PI) * 14 : 0;
        Avatar.drawHero(g, wx, feet, s * size, walkPhase, moveAmt, facing, time, currentMood(), lift, alpha);
    }

    private void drawItems(Graphics2D g, Room room)
    {
        double ws = witchScale();
        int i = 0;
        for(Item item : room.getItems()) {
            double ix = centerX() - 62 * ws * 0.9 - (i % 3) * 84;
            double iy = feetY() + 4 - (i / 3) * 56;
            double bob = Math.sin(time * 2.4 + i) * 4;
            g.setColor(new Color(0, 0, 0, 80));
            g.fill(new Ellipse2D.Double(ix - 24, iy - 5, 48, 12));
            ItemArt.draw(g, item.getName(), ix, iy - 28 + bob, 56, time, true);
            if(i == 0 || item.canBePickedUp()) {
                Theme.drawSparkle(g, ix + 22, iy - 52 + bob, 5 + Math.sin(time * 4 + i) * 1.5, Theme.alpha(Color.WHITE, 220));
            }
            final String name = item.getName();
            final boolean pick = item.canBePickedUp();
            Hot hot = new Hot();
            hot.shape = new Ellipse2D.Double(ix - 32, iy - 62, 64, 70);
            hot.label = pick ? "Take the " + name : "The " + name + " (too heavy)";
            hot.action = () -> {
                if(pick) {
                    commands.accept("take " + name);
                }
                else {
                    notes.accept("The " + name + " is far too heavy to lift.");
                }
            };
            hots.add(hot);
            i++;
        }
    }

    private void drawFriends(Graphics2D g, Room room)
    {
        double ws = witchScale();
        int i = 0;
        for(GameCharacter c : room.getCharacters()) {
            double fx = centerX() + 62 * ws * 0.95 + i * 110;
            double feet = feetY() + 6;
            if(c.isMoving()) {
                Avatar.drawGhost(g, fx, feet, ws * 1.2, time + i);
            }
            else {
                Avatar.drawCat(g, fx, feet, ws * 1.25, time + i);
            }
            final String name = c.getName();
            Hot hot = new Hot();
            hot.shape = new Rectangle2D.Double(fx - 48, feet - 130, 96, 140);
            hot.label = "Talk to the " + name;
            hot.action = () -> commands.accept("talk " + name);
            hots.add(hot);
            i++;
        }
    }

    private void drawParticles(Graphics2D g)
    {
        for(Particle p : particles) {
            double life = Math.max(0, Math.min(1, p.life));
            int a = (int) (255 * (p.kind == 3 ? 0.55 + 0.45 * Math.sin(time * 6 + p.x) : 1) * Math.min(1, life * 2));
            a = Math.max(0, Math.min(255, a));
            double sz = p.size * (0.5 + 0.5 * life);
            if(p.kind == 1) {
                Theme.drawHeart(g, p.x, p.y, sz * 1.6, Theme.alpha(p.color, a));
            }
            else if(p.kind == 2) {
                Theme.drawStar(g, p.x, p.y, sz * 0.9, Theme.alpha(p.color, a));
            }
            else if(p.kind == 3) {
                float[] f = { 0f, 1f };
                g.setPaint(new RadialGradientPaint(new Point2D.Double(p.x, p.y), (float) (sz * 3), f,
                           new Color[] { Theme.alpha(p.color, a), Theme.alpha(p.color, 0) }));
                g.fill(new Ellipse2D.Double(p.x - sz * 3, p.y - sz * 3, sz * 6, sz * 6));
            }
            else {
                Theme.drawSparkle(g, p.x, p.y, sz, Theme.alpha(p.color, a));
            }
        }
    }

    // ---- exit signs

    private Point2D.Double exitSpot(String dir)
    {
        double l = left(), r = right(), t = top(), b = bottom(), cx = centerX(), cy = centerY();
        switch(dir) {
            case "north": return new Point2D.Double(cx, t + 40);
            case "south": return new Point2D.Double(cx, b - 52);
            case "east":  return new Point2D.Double(r - 48, cy + 40);
            case "west":  return new Point2D.Double(l + 48, cy + 40);
            case "up":    return new Point2D.Double(cx + (r - cx) * 0.62, t + 50);
            case "down":  return new Point2D.Double(cx - (cx - l) * 0.62, b - 60);
            default:      return null;
        }
    }

    private void drawExits(Graphics2D g, Room room)
    {
        for(String dir : room.getExits().keySet()) {
            Point2D.Double p = exitSpot(dir);
            if(p == null) {
                continue;
            }
            boolean locked = room.isLocked(dir);
            String label = locked ? "Go " + dir + " (locked)" : "Go " + dir;
            boolean hov = hover != null && hover.label.equals(label);
            double r = hov ? 31 : 28, pulse = 0.5 + 0.5 * Math.sin(time * 3 + dir.length());
            Color accent = locked ? Theme.GOLD : Theme.ROSE;

            float[] f = { 0f, 1f };
            g.setPaint(new RadialGradientPaint(p, (float) (r * 2.2), f,
                       new Color[] { Theme.alpha(accent, (int) (60 + 70 * pulse + (hov ? 60 : 0))), Theme.alpha(accent, 0) }));
            g.fill(new Ellipse2D.Double(p.x - r * 2.2, p.y - r * 2.2, r * 4.4, r * 4.4));
            g.setColor(new Color(0, 0, 0, 90));
            g.fill(new Ellipse2D.Double(p.x - r, p.y - r + 4, r * 2, r * 2));
            g.setPaint(new GradientPaint(0f, (float) (p.y - r), new Color(0x4A1A24), 0f, (float) (p.y + r), new Color(0x1E080E)));
            g.fill(new Ellipse2D.Double(p.x - r, p.y - r, r * 2, r * 2));
            g.setColor(hov ? Color.WHITE : accent);
            g.setStroke(new BasicStroke(hov ? 3f : 2.4f));
            g.draw(new Ellipse2D.Double(p.x - r, p.y - r, r * 2, r * 2));
            g.setStroke(new BasicStroke(1f));
            drawArrow(g, p.x, p.y, dir, hov ? Color.WHITE : Theme.TEXT);
            if(locked) {
                g.setColor(new Color(0x1E080E));
                g.fill(new Ellipse2D.Double(p.x + r * 0.35, p.y - r * 1.2, 22, 22));
                Theme.drawLock(g, (int) (p.x + r * 0.35 + 11), (int) (p.y - r * 1.2 + 15), Theme.GOLD);
            }
            g.setFont(Theme.ui(Font.BOLD, 11));
            String name = dir.toUpperCase();
            g.setColor(Theme.alpha(Theme.TEXT, 230));
            g.drawString(name, (float) (p.x - g.getFontMetrics().stringWidth(name) / 2.0), (float) (p.y + r + 15));

            Hot hot = new Hot();
            hot.shape = new Ellipse2D.Double(p.x - r - 4, p.y - r - 4, r * 2 + 8, r * 2 + 8);
            hot.label = label;
            final String command = "go " + dir;
            hot.action = () -> commands.accept(command);
            hots.add(hot);
        }
    }

    private void drawArrow(Graphics2D g, double x, double y, String dir, Color c)
    {
        double angle = dir.equals("north") || dir.equals("up") ? -Math.PI / 2
                     : dir.equals("south") || dir.equals("down") ? Math.PI / 2
                     : dir.equals("west") ? Math.PI : 0;
        AffineTransform keep = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);
        g.setColor(c);
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        boolean stairs = dir.equals("up") || dir.equals("down");
        if(stairs) {                                                    // two chevrons = change of floor
            for(int k = -1; k <= 1; k += 2) {
                Path2D.Double chev = new Path2D.Double();
                chev.moveTo(k * 6 - 4, -8);
                chev.lineTo(k * 6 + 4, 0);
                chev.lineTo(k * 6 - 4, 8);
                g.draw(chev);
            }
        }
        else {
            g.draw(new Line2D.Double(-9, 0, 8, 0));
            Path2D.Double head = new Path2D.Double();
            head.moveTo(1, -8);
            head.lineTo(10, 0);
            head.lineTo(1, 8);
            g.draw(head);
        }
        g.setStroke(new BasicStroke(1f));
        g.setTransform(keep);
    }

    // ---- speech emote and hover label

    private void drawEmote(Graphics2D g)
    {
        long left = emoteUntil - System.currentTimeMillis();
        if(left <= 0 || emote.isEmpty()) {
            return;
        }
        double s = witchScale();
        double bx = wx + 40 * s * 0.8, by = feetY() - 98 * s - 12 + Math.sin(time * 6) * 2;
        float alpha = left < 400 ? left / 400f : 1f;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.setColor(new Color(255, 255, 255, 235));
        g.fill(new Ellipse2D.Double(bx - 26, by - 20, 52, 40));
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(bx - 14, by + 14);
        tail.lineTo(bx - 28, by + 28);
        tail.lineTo(bx - 4, by + 18);
        tail.closePath();
        g.fill(tail);
        switch(emote) {
            case "heart":
                Theme.drawHeart(g, bx, by, 26, Theme.ROSE_DARK);
                break;
            case "star":
                Theme.drawStar(g, bx, by, 13, Theme.GOLD_DARK);
                break;
            default:
                g.setFont(Theme.ui(Font.BOLD, 26));
                g.setColor(emote.equals("!") ? Theme.RED : new Color(0x7B4DE0));
                String t = emote;
                g.drawString(t, (float) (bx - g.getFontMetrics().stringWidth(t) / 2.0), (float) (by + 9));
        }
        g.setComposite(old);
    }

    private void drawHoverLabel(Graphics2D g)
    {
        if(hover == null) {
            return;
        }
        g.setFont(Theme.ui(Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(hover.label) + 24, h = 28;
        int x = Math.max(8, Math.min(getWidth() - w - 8, mouse.x - w / 2)), y = Math.max(8, mouse.y - 52);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(x + 1, y + 3, w, h, h, h);
        g.setColor(new Color(0x1E080E));
        g.fillRoundRect(x, y, w, h, h, h);
        g.setColor(Theme.GOLD);
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect(x, y, w, h, h, h);
        g.setStroke(new BasicStroke(1f));
        g.setColor(Theme.TEXT);
        g.drawString(hover.label, x + 12, y + (h - fm.getHeight()) / 2 + fm.getAscent());
    }
}
