import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

/**
 * Graphical user interface for World of Zuul - the "Horror Castle" edition.
 *
 * The whole window is one big picture (ScenePanel) with small floating panels
 * on top of it, like the screen of a video game:
 *   top      - title, the name of the room, and the two meters
 *   left     - the castle map and Willow's pockets
 *   right    - the diary (the full history of messages)
 *   bottom   - Willow's speech box and the command bar
 * You can play by clicking things IN the scene (exit signs, items, friends),
 * by using the buttons, or by typing commands.
 *
 * This class is the "controller": it shows what the game reports (through
 * GameOutput) and turns clicks and typed text into commands. The game logic
 * itself lives in Game, Room, Player, ... and knows nothing about Swing.
 *
 * Start it with:  right-click GameGUI > void main(String[] args)
 */
public class GameGUI extends JFrame implements GameOutput
{
    private static final Color CARD = new Color(20, 9, 14, 226);

    private Game game;
    private final ScenePanel scene = new ScenePanel(this::send, this::note);
    private final MapPanel map = new MapPanel();
    private final BarMeter timeMeter = new BarMeter("COMMANDS LEFT", Theme.GOLD);
    private final BarMeter loadMeter = new BarMeter("POCKET WEIGHT (g)", Theme.PURPLE);
    private final JLabel locationLabel = new JLabel(" ", SwingConstants.CENTER);
    private final DialogueBox dialogue = new DialogueBox();
    private final JTextPane diary = new JTextPane();
    private final StyledDocument diaryDoc = diary.getStyledDocument();
    private final Hotbar pockets = new Hotbar(this::send);
    private final CommandField input = new CommandField();
    private FancyButton unlockBtn, chargeBtn, castBtn;
    private final ArrayList<String> history = new ArrayList<String>();
    private int historyPos = 0;
    private final Overlay overlay = new Overlay();
    private boolean diaryVisible = true;

    private GothicPanel titleCard, locationCard, meterCard, mapCard, pocketCard, diaryCard, commandBar;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
    }

    public GameGUI()
    {
        super("Horror Castle - World of Zuul (Willow the witch)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        MessageStyles.install(diaryDoc, 13);
        buildUI();
        setGlassPane(overlay);
        game = new Game(this);
        map.setGame(game);
        scene.setGame(game);
        setSize(1340, 860);
        setMinimumSize(new Dimension(1260, 780));
        setLocationRelativeTo(null);
        refresh();
        overlay.setMood(Avatar.Mood.WAVE);
        overlay.present("THE HORROR CASTLE", Theme.GOLD,
            "Dark magic has sealed Willow inside a cursed, blood-soaked castle.<br>"
            + "Find the <b>rusty iron key</b>, unlock the gate in the great hall and escape before the midnight bell tolls.<br><br>"
            + "<span style='color:#9D93C9'>Click the glowing signs, items and friends in the scene, "
            + "or type commands such as <b>go east</b>, <b>take lantern</b> or <b>help</b>.</span>",
            "Begin the escape", this::begin, "Exit", this::dispose);
    }

    // ------------------------------------------------------------------ game control

    private void begin()
    {
        overlay.setVisible(false);
        dialogue.beginTurn();
        game.start();
        refresh();
        input.requestFocusInWindow();
    }

    private void newGame()
    {
        overlay.setVisible(false);
        try {
            diaryDoc.remove(0, diaryDoc.getLength());
        }
        catch(BadLocationException ex) {
            // cannot happen: we remove what exists
        }
        history.clear();
        historyPos = 0;
        game = new Game(this);
        map.setGame(game);
        scene.setGame(game);
        dialogue.beginTurn();
        game.start();
        refresh();
        input.requestFocusInWindow();
    }

    /** Send a command to the game (typed, from a button or from a click in the scene). */
    private void send(String text)
    {
        text = text.trim();
        if(text.isEmpty() || game.isFinished() || overlay.isVisible()) {
            return;
        }
        history.add(text);
        historyPos = history.size();
        dialogue.beginTurn();
        show(MessageType.ECHO, text);
        game.processLine(text);
        refresh();
        if(game.isFinished()) {
            Timer wait = new Timer(1700, e -> showEnd());   // let Willow celebrate (or cry) for a moment
            wait.setRepeats(false);
            wait.start();
        }
    }

    /** A message that is not a game command, e.g. "the cauldron is too heavy". */
    private void note(String message)
    {
        if(game.isFinished() || overlay.isVisible()) {
            return;
        }
        dialogue.beginTurn();
        dialogue.add(MessageType.INFO, message);
        dialogue.react(Avatar.Mood.SAD);
        scene.react(Avatar.Mood.SAD, "...");
    }

    private void showEnd()
    {
        switch(game.getOutcome()) {
            case WON:
                overlay.setMood(Avatar.Mood.HAPPY);
                overlay.present("YOU ESCAPED!", Theme.GREEN,
                    "Willow fled through the graveyard gate using only <b>" + game.getCommandsUsed()
                    + "</b> of " + game.getMaxCommands() + " commands.<br>The castle will never hold her again.",
                    "Play again", this::newGame, "Exit", this::dispose);
                break;
            case LOST:
                overlay.setMood(Avatar.Mood.SAD);
                overlay.present("THE BELL RINGS", Theme.RED,
                    "Midnight came too soon... the castle keeps Willow for another night.<br>"
                    + "Plan a shorter route and try again.",
                    "Try again", this::newGame, "Exit", this::dispose);
                break;
            default:
                overlay.setMood(Avatar.Mood.WAVE);
                overlay.present("SWEET DREAMS", Theme.PURPLE,
                    "You stopped the escape.<br>The castle waits patiently for Willow's return.",
                    "Play again", this::newGame, "Exit", this::dispose);
        }
    }

    // ------------------------------------------------------------------ GameOutput

    public void show(MessageType type, String text)
    {
        // the diary keeps everything
        if(type == MessageType.ECHO) {
            diaryLine("\u203A " + text, "echo");
            return;
        }
        String[] lines = text.split("\n");
        for(int i = 0; i < lines.length; i++) {
            diaryLine(lines[i], MessageStyles.nameFor(type, i == 0));
        }
        // the speech box shows the news; the scene already shows exits and items
        switch(type) {
            case ROOM:
                dialogue.add(type, lines[0]);
                break;
            case ERROR:
                dialogue.add(type, text);
                dialogue.react(Avatar.Mood.SAD);
                scene.react(Avatar.Mood.SAD, "!");
                break;
            case SUCCESS:
                dialogue.add(type, text);
                dialogue.react(Avatar.Mood.HAPPY);
                scene.react(Avatar.Mood.HAPPY, "heart");
                break;
            case SPEECH:
                dialogue.add(type, text);
                dialogue.react(Avatar.Mood.WAVE);
                scene.react(Avatar.Mood.WAVE, "...");
                break;
            case STORY:
                dialogue.add(type, text);
                dialogue.react(Avatar.Mood.WAVE);
                break;
            default:
                dialogue.add(type, text);
        }
    }

    private void diaryLine(String text, String styleName)
    {
        try {
            int start = diaryDoc.getLength();
            Style style = diaryDoc.getStyle(styleName);
            diaryDoc.insertString(start, text + "\n", style);
            diaryDoc.setParagraphAttributes(start, text.length() + 1, style, false);
            diary.setCaretPosition(diaryDoc.getLength());
        }
        catch(BadLocationException ex) {
            // cannot happen: we always insert at the end
        }
    }

    // ------------------------------------------------------------------ refresh the panels

    private void refresh()
    {
        Player p = game.getPlayer();
        Room room = p.getCurrentRoom();
        timeMeter.setValues(game.getCommandsLeft(), game.getMaxCommands());
        timeMeter.setWarn(game.getCommandsLeft() <= 10);
        loadMeter.setValues(p.getTotalWeight(), p.getMaxWeight());
        loadMeter.setWarn(p.getTotalWeight() > 0.85 * p.getMaxWeight());
        locationLabel.setText(room.getName());
        pockets.setContents(p, room);

        unlockBtn.setEnabled(firstLockedDirection(room) != null && p.hasItem("key"));
        Beamer beamer = p.getBeamer();
        chargeBtn.setEnabled(beamer != null);
        castBtn.setEnabled(beamer != null && beamer.isCharged());
        map.repaint();
    }

    private String firstLockedDirection(Room room)
    {
        for(String direction : room.getExits().keySet()) {
            if(room.isLocked(direction)) {
                return direction;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ building the window

    /** The layered pane that puts the panels on top of the scene. */
    private class Stage extends JLayeredPane
    {
        public void doLayout()
        {
            int w = getWidth(), h = getHeight(), m = 14;
            scene.setBounds(0, 0, w, h);
            titleCard.setBounds(m, m, 300, 66);
            meterCard.setBounds(w - m - 372, m, 372, 66);
            locationCard.setBounds((w - 360) / 2, m, 360, 66);

            int colTop = m + 66 + 8;
            mapCard.setBounds(m, colTop, 290, 268);
            pocketCard.setBounds(m, colTop + 268 + 10, 290, 240);

            int diaryW = 300;
            diaryCard.setVisible(diaryVisible);
            diaryCard.setBounds(w - m - diaryW, colTop, diaryW, h - colTop - m);

            int bx = m + 290 + 12;
            int bRight = diaryVisible ? w - m - diaryW - 12 : w - m;
            int cmdH = 100, dlgH = 122;
            commandBar.setBounds(bx, h - m - cmdH, bRight - bx, cmdH);
            dialogue.setBounds(bx, h - m - cmdH - 8 - dlgH, bRight - bx, dlgH);
            scene.setInsets(colTop - 2, bx, cmdH + dlgH + m + 16, w - bRight);
        }
    }

    private void buildUI()
    {
        Stage stage = new Stage();
        stage.setBackground(Theme.BG_TOP);
        setContentPane(stage);
        stage.add(scene, JLayeredPane.DEFAULT_LAYER);

        titleCard = buildTitleCard();
        locationCard = buildLocationCard();
        meterCard = buildMeterCard();
        mapCard = buildMapCard();
        pocketCard = buildPocketCard();
        diaryCard = buildDiaryCard();
        commandBar = buildCommandBar();
        for(JComponent c : new JComponent[] { titleCard, locationCard, meterCard, mapCard, pocketCard, diaryCard,
                                              dialogue, commandBar }) {
            stage.add(c, JLayeredPane.PALETTE_LAYER);
        }
    }

    private GothicPanel card(LayoutManager layout, int vPad, int hPad)
    {
        GothicPanel p = new GothicPanel(layout, CARD, 16);
        p.setBorder(new EmptyBorder(vPad, hPad, vPad, hPad));
        return p;
    }

    private GothicPanel buildTitleCard()
    {
        GothicPanel c = card(new BorderLayout(), 8, 16);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        GradientTitle title = new GradientTitle("Horror Castle", Theme.ui(Font.BOLD, 22));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("WORLD OF ZUUL  \u00B7  ESCAPE IF YOU DARE");
        sub.setFont(Theme.ui(Font.BOLD, 10));
        sub.setForeground(Theme.MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(Box.createVerticalGlue());
        titles.add(title);
        titles.add(sub);
        titles.add(Box.createVerticalGlue());
        c.add(titles, BorderLayout.CENTER);
        return c;
    }

    private GothicPanel buildLocationCard()
    {
        GothicPanel c = card(new BorderLayout(), 6, 16);
        JLabel caption = new JLabel("YOU ARE IN", SwingConstants.CENTER);
        caption.setFont(Theme.ui(Font.BOLD, 10));
        caption.setForeground(Theme.MUTED);
        locationLabel.setFont(Theme.story(Font.BOLD, 22));
        locationLabel.setForeground(Theme.GOLD);
        c.add(caption, BorderLayout.NORTH);
        c.add(locationLabel, BorderLayout.CENTER);
        return c;
    }

    private GothicPanel buildMeterCard()
    {
        GothicPanel c = card(new FlowLayout(FlowLayout.CENTER, 14, 0), 8, 0);
        timeMeter.setPreferredSize(new Dimension(165, 46));
        loadMeter.setPreferredSize(new Dimension(165, 46));
        c.add(timeMeter);
        c.add(loadMeter);
        return c;
    }

    private GothicPanel buildMapCard()
    {
        GothicPanel c = new GothicPanel(new BorderLayout(), CARD, 16, GothicPanel.Frame.ARCH);
        c.setBorder(new EmptyBorder(30, 14, 12, 14));
        JLabel mapTitle = sectionTitle("CASTLE MAP");
        mapTitle.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(mapTitle, BorderLayout.NORTH);
        c.add(map, BorderLayout.CENTER);
        return c;
    }

    private GothicPanel buildPocketCard()
    {
        GothicPanel c = card(new BorderLayout(), 12, 14);
        c.add(sectionTitle("POCKETS  (click an item)"), BorderLayout.NORTH);
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);
        holder.add(pockets);
        c.add(holder, BorderLayout.CENTER);
        return c;
    }

    private GothicPanel buildDiaryCard()
    {
        GothicPanel c = card(new BorderLayout(), 14, 14);
        c.add(sectionTitle("DIARY"), BorderLayout.NORTH);
        diary.setEditable(false);
        diary.setFocusable(false);
        diary.setOpaque(false);
        diary.setBackground(new Color(0, 0, 0, 0));
        diary.setBorder(new EmptyBorder(2, 2, 2, 8));
        JScrollPane scroll = new JScrollPane(diary);
        Theme.styleScroll(scroll);
        c.add(scroll, BorderLayout.CENTER);
        return c;
    }

    private JLabel sectionTitle(String text)
    {
        JLabel l = new JLabel(text);
        l.setFont(Theme.ui(Font.BOLD, 11));
        l.setForeground(Theme.MUTED);
        l.setBorder(new EmptyBorder(0, 2, 8, 0));
        return l;
    }

    private GothicPanel buildCommandBar()
    {
        GothicPanel bar = card(new BorderLayout(0, 8), 10, 14);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.add(chip("Look", "look"));
        chips.add(chip("Back", "back"));
        chips.add(chip("Items", "items"));
        chips.add(chip("Help", "help"));
        unlockBtn = new FancyButton("Unlock gate", FancyButton.Kind.SECONDARY).compact();
        unlockBtn.addActionListener(e -> {
            String d = firstLockedDirection(game.getPlayer().getCurrentRoom());
            if(d != null) {
                send("unlock " + d);
            }
        });
        chips.add(unlockBtn);
        chargeBtn = chip("Charge orb", "charge");
        castBtn = chip("Cast orb", "fire");
        chips.add(chargeBtn);
        chips.add(castBtn);
        FancyButton diaryBtn = new FancyButton("Diary", FancyButton.Kind.GHOST).compact();
        diaryBtn.addActionListener(e -> {
            diaryVisible = !diaryVisible;
            getContentPane().doLayout();
            getContentPane().repaint();
        });
        chips.add(diaryBtn);
        FancyButton quit = new FancyButton("Quit", FancyButton.Kind.DANGER).compact();
        quit.addActionListener(e -> send("quit"));
        chips.add(quit);
        bar.add(chips, BorderLayout.NORTH);

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);
        inputRow.add(input, BorderLayout.CENTER);
        FancyButton sendBtn = new FancyButton("Cast", FancyButton.Kind.PRIMARY);
        sendBtn.setPreferredSize(new Dimension(90, 38));
        inputRow.add(sendBtn, BorderLayout.EAST);
        bar.add(inputRow, BorderLayout.CENTER);

        ActionListener submit = e -> {
            send(input.getText());
            input.setText("");
        };
        input.addActionListener(submit);
        sendBtn.addActionListener(submit);
        input.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyCode() == KeyEvent.VK_UP && historyPos > 0) {
                    historyPos--;
                    input.setText(history.get(historyPos));
                }
                else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if(historyPos < history.size() - 1) {
                        historyPos++;
                        input.setText(history.get(historyPos));
                    }
                    else {
                        historyPos = history.size();
                        input.setText("");
                    }
                }
            }
        });
        return bar;
    }

    private FancyButton chip(String label, String command)
    {
        FancyButton b = new FancyButton(label, FancyButton.Kind.SECONDARY).compact();
        b.addActionListener(e -> send(command));
        return b;
    }

    // ------------------------------------------------------------------ small custom components

    /** A title painted with a gold-to-pink gradient. */
    private static class GradientTitle extends JComponent
    {
        private final String text;

        GradientTitle(String text, Font font)
        {
            this.text = text;
            setFont(font);
            setOpaque(false);
        }

        public Dimension getPreferredSize()
        {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(text) + 6, fm.getAscent() + 6);
        }

        public Dimension getMaximumSize()
        {
            return getPreferredSize();
        }

        protected void paintComponent(Graphics g0)
        {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.quality(g);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(text);
            g.setColor(new Color(150, 90, 200, 50));
            g.drawString(text, 1, fm.getAscent() + 2);
            g.setPaint(new GradientPaint(0, 0, Theme.GOLD, w, 0, Theme.ROSE));
            g.drawString(text, 0, fm.getAscent());
            g.dispose();
        }
    }

    /** The text field where commands are typed. */
    private static class CommandField extends JTextField
    {
        CommandField()
        {
            setOpaque(false);
            setForeground(Theme.TEXT);
            setCaretColor(Theme.GOLD);
            setFont(Theme.ui(Font.PLAIN, 15));
            setBorder(new EmptyBorder(8, 20, 8, 20));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { repaint(); }
                public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        protected void paintComponent(Graphics g0)
        {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.quality(g);
            g.setColor(new Color(0x0C0508));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.setColor(isFocusOwner() ? Theme.GOLD : Theme.PANEL_BORDER);
            g.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.2f));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            if(getText().isEmpty()) {
                g.setFont(getFont().deriveFont(Font.PLAIN));
                g.setColor(Theme.MUTED);
                FontMetrics fm = g.getFontMetrics();
                g.drawString("Type a command... e.g. go east   (or click things in the scene)", getInsets().left,
                             (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }
}
