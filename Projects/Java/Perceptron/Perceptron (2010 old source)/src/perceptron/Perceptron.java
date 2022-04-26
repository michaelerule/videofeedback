package perceptron;
/* Perceptron.java
 * Created on December 21, 2006, 5:27 PM
 */

import image.ImageCache;
import rendered2D.GlyphLibrary;
import rendered2D.DanseParty;
import rendered2D.CellModel;
import rendered3D.Object3D;
import rendered3D.TreeForm;
import rendered3D.Tree3D;
import math.complex;
import automata.Waves_6;
import automata.SimpleAutomata1;
import automata.Waves_1;
import automata.Waves_5;
import automata.Hallucination1;
import automata.Waves_11;
import cortex.VisualCortex;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import java.awt.font.GlyphVector;
import java.util.ArrayList;
import rendered2D.Moths;

import static java.lang.Math.*;

public class Perceptron extends javax.swing.JFrame {

    Object3D o = fetch();

    Object3D fetch() {
        try {
            return (new Object3D(new BufferedReader(
                new FileReader(new File("resource/data/tetrahedron.txt")))));
        } catch (FileNotFoundException E) {
            System.out.println("Could not load file resource/data/tetrahedron.txt");
            System.exit(1);
        }
        return null;
    }
    
    long last_image_time = System.currentTimeMillis();
    long boredom_time = System.currentTimeMillis();
    long FRAME = 0;
    public int imageRotateMS = 5000;
    public int boredomeMS = 100000;
    public int presetRotateMS = 500000;
    public boolean rotateImages = false;
    public long frame;
    
    /** User Editable Parameters.
     */
    public int screensaveTimeoutMS = 60000;
    public int max_frame_length = 1000 / 20;
    public int audio_line = -1;
    public int min_tree_depth = 9;
    public int max_tree_depth = 6;
    // The effective screen dimensions (size of output buffer), overidden by settings file
    public int screen_width = 600;
    public int screen_height = 600;
    public int half_screen_width;
    public int half_screen_height;
    // Weather or not to display the frame rate in the upper left corner
    public boolean frame_rate_display = false;
    // Weather to perform the fractal mapping before or after drawing objects
    public boolean objects_on_top = true;
    // Upper limit on the program speed?
    public boolean cap_frame_rate = false;
    public boolean write_animation = false;
    /* Load all the images from this folder.     */
    public String image_directory = "resource/images";
    /** Package accessable instance specific constants
     *  These values are calculated during intialisation and should not
     *  change value while the program is running.     */
    //The actual screen dimensions (don't let the rest of the program catch on)
    protected int physical_width, physical_height;
    /** PHYSICAL ONSCREEN GRAPHICS  */
    /** the screen graphics, for displaying rendered frames */
    Graphics screen_graphics;
    BufferedImage screen_buffer;
    BufferStrategy bufferStrategy;
    Graphics2D graph2D;
    public final DoubleBuffer buffer;
    ImageCache images = null;
    ControlSet controls;
    FractalMap fractal;
    Tree3D the_tree;
    CellModel life;
    Vector<Mapping> user_maps;
    Preset[] user_presets;
    boolean running;
    /** Save frame.     */
    JFileChooser saver;
    //Help Screen Data
    int state_alpha = 255;
    boolean show_help = false;
    String state = "";
    int state_length = -1;
    //Text Editor Data
    boolean XOR_MODE = false;
    int COLUMNS = 18;
    int ROWS = 12;
    int SIZE = 36;
    static final int[] SALVIA_COLORS = {
        0x00ff00, 0xff0000, 0x0000ff,
        0xffff00, 0x00ffff, 0xff00ff
    };
    char[] BUFFER = new char[ROWS * COLUMNS];
    int BUFFER_INDEX = 0;
    boolean salvia_mode = true;
    boolean CURSOR_ON = true;
    SimpleAutomata1 trilife;
    Hallucination1 waves;
    Waves_1 waves1;
    Waves_11 waves11;
    Waves_5 waves5;
    Waves_6 waves6;
    DanseParty danceparty;
    VisualCortex cortex;
    AbstractController cortexControls;
    int automata = 0;
    Moths moths;
    GlyphLibrary glyphs;
    double GLYPH_THETA = 0;
    /** The additional information for the help screen.
     * (The help screen menu without the realtime
     * update of the setting state next to the setting
     * name.)     */
    String more_info =
            "...oO     PERCEPTRON     Oo...\n"
            + "                       @                               \n"
            + "Up Arrow    @          motion blur increase\n"
            + "Down Arrow    @          motion blur decrease\n"
            + "Left Arrow     @          color filter weight decrease\n"
            + "Right Arrow     @          color filter weight increase\n"
            + "Left Click       @          select next cursor\n"
            + "Right Click      @          select previous cursor\n"
            + "[    @  add dot to current cursor\n"
            + "]    @  remove dot from current cursor\n"
            + "- or _   @  slow down current cursor\n"
            + "= or +   @  speed up current cursor\n"
            + "Space    @  save state and screenshot\n"
            + "Enter    @  execute equation\n"
            + "0-9, Fn  @  built-in presets\n"
            + "Alt    @  pause\n"
            + "Esc    @  exit\n";
    final int[] spacer = {5, 35, 155, 300, 345};
    BufferedReader infile;
    int bufferSourceIndex = 0;
    int read;
    int sketchNum = 0;
    int raytheta = 0;

    static GraphicsConfiguration getDeviceGraphicsConfig() {
        return (((GraphicsEnvironment.getLocalGraphicsEnvironment()).getDefaultScreenDevice()).getDefaultConfiguration());
    }

    //CONSTRUCTOR
    public Perceptron(String Settings, String CrashLog, String Presets) {
        super("Perceptron", getDeviceGraphicsConfig());
        this.setBackground(Color.black);

        saver = new JFileChooser("Save State");
        saver.setBounds(0, 0, this.physical_width, this.physical_height);

        user_maps = new Vector<Mapping>();

        parse_settings(Settings, Presets); //READ IN SETTINGS INFORMATION
        parse_log(CrashLog);
        setup_frame();           //FRAME SETUP

        make_fullscreen();       //FULLSCREEN INITIALISATION
        hide_cursor();           //CONCEAL MOUSE POINTER
        physical_width = getWidth();
        physical_height = getHeight();
        // Make sure eveything fits togther and makes sense
        check_resolutions();

        // Initialise the frame rendering buffer
        BufferedImage output = new BufferedImage(
                screen_width, screen_height, BufferedImage.TYPE_INT_RGB);

        // Initialise the background buffer
        BufferedImage background = new BufferedImage(
                screen_width, screen_height, BufferedImage.TYPE_INT_RGB);

        // Initialise the display buffer
        BufferedImage display = new BufferedImage(
                screen_width, screen_height, BufferedImage.TYPE_INT_RGB);

        buffer = new DoubleBuffer(output, background, null, display);
        gtest = new Convolution(2, buffer);

        // Store exc local pointer to the screen Graphics
        screen_buffer = getDeviceGraphicsConfig().createCompatibleImage(physical_width, physical_height, Transparency.OPAQUE);
        screen_graphics = screen_buffer.getGraphics();

        // Initialise the Fractal, the Tree, and their associated parameters
        initialise_objects();

        /** Initialize user interaction. */
        initialise_listeners();

        /** Load the preset 0 by default. */
        controls.apply_preset(0);

        System.out.println("buffer width : " + screen_width + " height : " + screen_height);
        System.out.println("screen width : " + physical_width + " height : " + physical_height);
    }

    /** Set JFrame parameters upon initialisatrion*/
    void setup_frame() {
        // Exit on window close
        try {
            this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        } catch (Exception e) {
        }
        //ignore prompts for redrawing from the operating system
        try {
            this.setIgnoreRepaint(true);
        } catch (Exception e) {
        }
        //remove the frame decorations titlebar etc..
        //this frame is not to be re-sized
        try {
            this.dispose();
            this.setUndecorated(true);
            this.setResizable(false);
        } catch (Exception e) {
        }
    }

    /** Set up fullscreen mode */
    void make_fullscreen() {

        GraphicsDevice g = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode current = g.getDisplayMode();

        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window) this);

            DisplayMode[] possible_modes = g.getDisplayModes();

            DisplayMode best_mode = null;

            for (DisplayMode m : possible_modes) {
                System.out.println(m.getBitDepth() + " " + m.getHeight() + " " + m.getRefreshRate() + " " + m.getWidth());
                if ((m.getWidth() >= screen_width && m.getHeight() >= screen_height && m.getBitDepth() >= Math.min(32, current.getBitDepth()))
                        && (best_mode == null || m.getWidth() * m.getHeight() < best_mode.getWidth() * best_mode.getHeight())) {
                    best_mode = m;
                }
            }
            if (best_mode != null) {
            }

        } else {
            DisplayMode m = g.getDisplayMode();
            this.setBounds(0, 0, m.getWidth(), m.getHeight());
            this.setVisible(true);
        }

        //Setting up Double Buffering
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
        graph2D = (Graphics2D) bufferStrategy.getDrawGraphics();
    }

    /** Make windowed, "not fullscreen" */
    void make_not_fullscreen() {
        GraphicsDevice g = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window) null);

        }
    }

    /** Check the screen and background resolutions for consistancy */
    void check_resolutions() {
        half_screen_width = (short) (screen_width / 2);
        half_screen_height = (short) (screen_height / 2);
    }

    /** Create and add input event listeners to this JFrame */
    final void initialise_listeners() {
        controls = new ControlSet(this, user_presets);
        this.addMouseListener(controls);
        this.addMouseMotionListener(controls);
        this.addKeyListener(controls);
    }

    /** Initialise the Fractal, Tree, and associated parameters */
    final void initialise_objects() {
        images = new ImageCache(image_directory);
        fractal = new FractalMap(buffer, user_maps, this);
        the_tree = new Tree3D(
                min_tree_depth,
                max_tree_depth,
                new float[][]{{0, 0, 0}, {0, (float) (-(screen_height / 6)), 0}}, 0,
                new TreeForm[]{new TreeForm(.5f, -.2f, .7f, 7),
                    new TreeForm(.5f, .2f, .7f, -7)
                },
                new Point(half_screen_width, half_screen_height), buffer);

        life = new CellModel(screen_width, screen_height);

        trilife = new SimpleAutomata1();
        waves = new Hallucination1();
        waves1 = new Waves_1(this);
        waves11 = new Waves_11();
        waves5 = new Waves_5();
        waves6 = new Waves_6();
        danceparty = new DanseParty();
        cortex = new VisualCortex();
        cortexControls = cortex.makeController(
                new Point(screen_width / 2, screen_height / 2),
                min(screen_width, screen_height));
    }

    /** Hide the mouse pointer, if possible */
    public void hide_cursor() {
        try {
            setCursor(Toolkit.getDefaultToolkit().
                    createCustomCursor(Toolkit.getDefaultToolkit().
                    getImage("xparent.gif"), new Point(0, 0), null));
        } catch (Exception e) {
            System.err.println("Cursor modification is unsupported.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //MEMBER FUNCTIONS
    public void show_help_screen() {
        state = Preset.display_help_screen(this);
        state_length = state.length();
    }

    static double gaussian(double x, double sigma) {
        return exp(-.5 * pow(x / sigma, 2)) / (sigma * sqrt(2 * PI));
    }

    Kernel make_gaussian(float std) {
        int s = (int) (4 * std);
        float sum = 0f;
        float[] d = new float[s * s];
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                sum += d[i * s + j] = (float) gaussian(i - s / 2, std) * (float) gaussian(j - s / 2, std);
            }
        }
        sum = 1.0f / sum;
        for (int i = 0; i < s * s; i++) {
            d[i] *= sum;
        }
        return new Kernel(s, s, d);
    }

    Kernel make_unsharp(float std) {
        int s = (int) (4 * std);
        float sum = 0f;
        float[] d = new float[s * s];
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                sum += d[i * s + j] = -(float) gaussian(i - s / 2, std) * (float) gaussian(j - s / 2, std);
            }
        }
        d[s * (s / 2) + (s / 2)] += 1.0f - sum;
        return new Kernel(s, s, d);
    }
    ConvolveOp gconv = new ConvolveOp(make_unsharp(4), ConvolveOp.EDGE_NO_OP, new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));
    Convolution gtest;

    /**
     *Begin execution
     */
    public void go() {

        last_image_time = 20000 + System.currentTimeMillis();
        boredom_time = 50000 + System.currentTimeMillis();

        o.recenter(200);

        frame = 0;

        int x_offset = (physical_width - screen_width) / 2;
        int y_offset = (physical_height - screen_height) / 2;
        screen_graphics.setColor(Color.BLACK);

        System.out.println(" x_offset : " + x_offset);
        System.out.println(" y_offset : " + y_offset);

        long last_time = System.currentTimeMillis();
        long framerate = 0;

        show_help_screen();

        running = true;
        increment_sketch(1);
        life.seedUniformRandom(10);

        /** Load the preset 0 by default. */
        controls.apply_preset(controls.preset_number);
        while (true) {
            if (running) {
                try {
                    long start_time = System.currentTimeMillis();

                    switch (automata) {
                        case 0:
                            break;
                        case 1:
                            trilife.step();
                            break;
                        case 2:
                            waves.step();
                            waves.color1 = fractal.fade_color;
                            waves.color2 = ~waves.color1 & 0xff;
                            break;
                        case 3:
                            cortex.step();
                            cortexControls.stepFrame(.2);
                            break;
                        case 4:
                            waves1.step();
                            break;
                        case 5:
                            waves11.step();
                            break;
                        case 6:
                            waves5.step();
                            break;
                        case 7:
                            waves6.step();
                            break;
                        case 8:
                            danceparty.step();
                            break;
                    }
                    gtest.operate(fractal.filterweight);
                    fractal.operate();
                    the_tree.render();
                    render_salvia_mode(buffer.output.graphics2D);
                    Graphics draw_graphics = buffer.output.graphics;
                    controls.advance((int) framerate);
                    controls.drawAll(buffer.output.graphics);
                    controls.drawAll(buffer.display.graphics);
                    render_state(draw_graphics);
                    frame++;
                    long time = System.currentTimeMillis();
                    if (time - last_time >= 1000) {
                        last_time = time;
                        framerate = frame;
                        frame = 0;
                    }
                    if (frame_rate_display) {
                        draw_graphics.setColor(Color.WHITE);
                        draw_graphics.drawString("" + framerate, 05, 14);
                    }
                    BufferedImage drawn = buffer.output.image;
                    screen_graphics.drawImage(drawn, 0, 0, null);
                    graph2D.drawImage(screen_buffer, x_offset, y_offset, null);
                    if (write_animation) {
                        try {
                            String filename = "animate/frame " + (FRAME++) + ".png";
                            File file = new File(filename);
                            ImageIO.write(drawn, "png", file);
                        } catch (Exception ex) {
                            System.err.println("File write error during animation.");
                            ex.printStackTrace();
                        }
                    }
                    graph2D.dispose();
                    bufferStrategy.show();
                    graph2D = (Graphics2D) bufferStrategy.getDrawGraphics();
                    frame++;
                    start_time += max_frame_length;
                    if (cap_frame_rate) 
                        while (System.currentTimeMillis() < start_time);
                } catch (Exception e) {
                    System.out.println("SOMETHING BAD HAPPENED in Perceptron.java go()!!!");
                    e.printStackTrace();
                }
            } else {
                save_frame();
            }
        }
    }

    public void stimulate() {
        boredom_time = System.currentTimeMillis() + boredomeMS;
    }

    /** Press S for salvia mode, D for XOR mode. */
    private void render_salvia_mode(Graphics2D draw_graphics) {
        if (salvia_mode) {
            try {
                float YOFF = (float) screen_height / ROWS;
                float XOFF = (float) (.3 * screen_width / COLUMNS);
                int ROWS1 = ROWS + 1;
                Font old = draw_graphics.getFont();
                Font F = new java.awt.Font("Monaco", Font.PLAIN, SIZE);
                draw_graphics.setFont(F);
                if (XOR_MODE) 
                    draw_graphics.setXORMode(new Color(0xffffff));
                int k = 0;
                for (int j = 0; j < ROWS; j++) {
                    for (int i = 0; i < COLUMNS; i++) {
                        String CHAR = "" + BUFFER[k];
                        k = (k + 1) % BUFFER.length;
                        draw_graphics.setColor(new Color(SALVIA_COLORS[(int) (Math.random() * SALVIA_COLORS.length)]));
                        GlyphVector G = draw_graphics.getFont().createGlyphVector(draw_graphics.getFontRenderContext(), CHAR);
                        draw_graphics.drawGlyphVector(G, (int) ((double) i * screen_width / COLUMNS + XOFF), (int) (j * screen_height / ROWS1 + YOFF));
                    }
                }
                draw_graphics.setPaintMode();
                draw_graphics.setColor(new Color(SALVIA_COLORS[(int) (Math.random() * SALVIA_COLORS.length)]));
                draw_graphics.drawString("" + BUFFER[BUFFER_INDEX], (int) ((double) (BUFFER_INDEX % COLUMNS) * screen_width / COLUMNS + XOFF), (int) ((BUFFER_INDEX / COLUMNS) * screen_height / ROWS1 + YOFF));
                draw_graphics.setFont(old);
                if (CURSOR_ON) {
                    draw_graphics.setColor(new Color(0x000000));
                    int x = (int) ((double) screen_width * (BUFFER_INDEX % COLUMNS) / COLUMNS);
                    int y = (int) ((double) screen_height * (BUFFER_INDEX / COLUMNS) / ROWS);
                    draw_graphics.setXORMode(new Color(0xffffff));
                    draw_graphics.fillRect((int) (x + XOFF), (int) (y), screen_width / COLUMNS, screen_height / ROWS);
                    draw_graphics.setPaintMode();
                }

            } catch (Exception x) {
                x.printStackTrace();
            }
            // Consciousness is evolution on infinitely faster time scales.
            draw_graphics.setPaintMode();
        }
    }

    private void render_state(Graphics draw_graphics) {
        if (show_help) {
            if (state_alpha < 1) {
                state_alpha = 1;
            }
            state_alpha <<= 1;
            if (state_alpha > 255) {
                state_alpha = 0xFF;
            }
        } else {
            state_alpha >>= 1;
            if (state_alpha < 0) {
                state_alpha = 0;
            }
        }
        if (state_alpha > 0) {
            draw_graphics.setColor(new Color(0xFF, 0xFF, 0xFF, (int) state_alpha));
            StringTokenizer hack = new StringTokenizer(state, "\n");
            int k = 14;
            while (hack.hasMoreTokens()) {
                k += 16;
                StringTokenizer hack2 = new StringTokenizer(hack.nextToken(), "@");
                int si = 0;
                while (hack2.hasMoreTokens()) {
                    draw_graphics.drawString(hack2.nextToken(), spacer[si++], k);
                }
            }
            hack = new StringTokenizer(more_info, "\n");
            k = 14;
            while (hack.hasMoreTokens()) {
                k += 16;
                StringTokenizer hack2 = new StringTokenizer(hack.nextToken(), "@");
                int si = 3;
                while (hack2.hasMoreTokens()) {
                    draw_graphics.drawString(hack2.nextToken(), spacer[si++], k);
                }
            }
        }
    }

    /** Save state and the screenshot, "save frame".
     * Press Space.      */
    /** TODO check for file overwrite with the proper extensions and all... */
    private void save_frame() {
        make_not_fullscreen();
        try {
            saver.setVisible(true);
            saver.setFocusable(true);
            saver.requestFocus();
            int approved = saver.showSaveDialog(this);
            saver.grabFocus();
            File file = saver.getSelectedFile();
            System.out.println("saving...");
            if (file != null && approved == JFileChooser.APPROVE_OPTION) {
                System.out.println("...writing image...");
                ImageIO.write(buffer.output.image, "png", new File(file.getAbsolutePath() + ".out.png"));    // screenshot
                ImageIO.write(buffer.buffer.image, "png", new File(file.getAbsolutePath() + ".in.png"));    // screenshot of the frame - 1
                //ImageIO.write(buffer.display.image, "png", new File(file.getAbsolutePath() + ".display.png"));   // cursor movements
                //ImageIO.write(buffer.image.image, "png", new File(file.getAbsolutePath() + ".sketch.png"));     // sketch used for one of the edge extend functions
                System.out.println("...writing state...");
                Preset.write(this, new File(file.getAbsolutePath() + ".state")); // save with extension *.state
                System.out.println("...saved.");
            } else {
                System.out.println("...did not save.");
            }
            System.out.println("Done.");
        } catch (Exception E) {
            System.err.println("File write error while saving screenshot and state.");
            E.printStackTrace();
        }
        make_fullscreen();
        hide_cursor();
        running = true;
    }

    void parse_log(String string) {

        for (int i = 0; i < BUFFER.length; i++) {
            BUFFER[i] = ' ';
        }
        try {
            infile = new BufferedReader(new FileReader(string));
            read = infile.read();
            while (read > 0 && bufferSourceIndex < BUFFER.length) {
                BUFFER[bufferSourceIndex % BUFFER.length] = ' ';
                read = infile.read();
                bufferSourceIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Unused. */
    void getMoreText() {
        if (infile != null) {
            try {
                bufferSourceIndex = 0;
                while (bufferSourceIndex < BUFFER.length) {
                    BUFFER[bufferSourceIndex % BUFFER.length] = (char) read;
                    read = infile.read();
                    if (read < 0) {
                        infile.reset();
                        read = infile.read();
                    }
                    bufferSourceIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** TODO read only the default preset, all others with Open File menu */
    /** Read in the settings file. */
    final void parse_settings(String settings_path, String presets_path) {
        ArrayList<Preset> presets = new ArrayList<Preset>();
        try {
            //Create exc FileReader for reading the file
            BufferedReader in = new BufferedReader(new FileReader(settings_path));

            String current_line;

            while ((current_line = in.readLine()) != null) {
                if (current_line.length() > 0
                        && current_line.charAt(0) != '*') {

                    StringTokenizer token = new StringTokenizer(current_line);

                    if (token.countTokens() >= 2) {

                        String var = token.nextToken();
                        String val = token.nextToken();

                        if (var.equals("preset")) {
                            try {
                                System.out.println("parsing preset " + val + ":");
                                presets.add(Preset.parse(in));
                            } catch (Exception e) {
                            }

                        } else if (var.equals("map")) {
                            try {
                                user_maps.add(FractalMap.makeMap(val));
                                System.out.println("map : " + val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else { //parse primitive

                            Object value = null;

                            try {
                                value = new Byte(val);
                            } catch (Exception e1) {
                                try {
                                    value = new Short(val);
                                } catch (Exception e2) {
                                    try {
                                        value = new Integer(val);
                                    } catch (Exception e3) {
                                        try {
                                            value = new Float(val);
                                        } catch (Exception e4) {
                                            try {
                                                value = new Double(val);
                                            } catch (Exception e5) {
                                                try {
                                                    value = new complex(val);
                                                } catch (Exception e6) {
                                                    try {
                                                        value = new Boolean(val.equals("ON") || val.equals("TRUE") || val.equals("T"));
                                                    } catch (Exception e7) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            try {
                                this.getClass().getField(var).set(this, value);
                            } catch (Exception e) {
                            }

                        }
                    }
                }
            }
            in.close();

            File f = new File(presets_path);

            if (f != null && f.listFiles() != null) {
                for (File file : f.listFiles()) {
                    String name = file.getName();
                    try {
                        if (name.endsWith(".state")) {
                            in = new BufferedReader(new FileReader(file));
                            System.out.println("parsing preset " + name + ":");
                            presets.add(Preset.parse(in));
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
        user_presets = presets.toArray(new Preset[presets.size()]);
    }

    public int screen_width() {
        return screen_width;
    }

    public int screen_height() {
        return screen_height;
    }

    public int half_screen_width() {
        return half_screen_width;
    }

    public int half_screen_height() {
        return half_screen_height;
    }

    public int physical_width() {
        return physical_width;
    }

    public int physical_height() {
        return physical_height;
    }

    public void toggle_objects_on_top() {
        objects_on_top = !objects_on_top;
    }

    /** Press Z to see the frame rate. */
    public void toggle_frame_rate_display() {
        frame_rate_display = !frame_rate_display;
    }

    /** Press U to freeze the frame rate. */
    public void toggle_cap_frame_rate() {
        cap_frame_rate = !cap_frame_rate;
    }

    /** Press B. */
    public void set_objects_on_top(boolean b) {
        objects_on_top = b;
    }

    /** Press backquote Space to call the save menu.
     * The save_frame() appears when not running.     */
    public void save() {
        running = false;
    }

    /** TODO load images with Open File menu. */
    /** Press M to go to the next image located in
     * the resource/imagesor N to go  back. Used
     * in the image mode (press L).      */
    public void increment_sketch(int n) {
        buffer.load_sketch(images.advance(n), this.screen_width, true, this);
        sketchNum = images.current();
        last_image_time = imageRotateMS + System.currentTimeMillis();
    }

    public void set_sketch(int n) {
        try {
            sketchNum = n;
            buffer.load_sketch(images.get(n), this.screen_width, true, this);
            last_image_time = 30000 + System.currentTimeMillis();
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    /** Press Y to set fancy graphics for 3D tree. */
    public void toggle_fancy() {
        buffer.toggle_fancy();
    }

    /**Deprecated.
     * Press A to set fancy, anti-alias graphics for
     * the entire program. Defaults to yes. */
    public boolean is_fancy() {
        return buffer.fancy;
    }

    /** Take snapshot of every frame - animate.
     * Press Home.      */
    public void toggle_animation() {
        write_animation = !write_animation;
    }

    /** Press CTRL and start typing... */
    void append_to_buffer(char c) {
        BUFFER[BUFFER_INDEX] = c;
        BUFFER_INDEX =
                (BUFFER_INDEX + 1) % BUFFER.length;
    }

    void BUFFER_LEFT() {
        BUFFER_INDEX = (BUFFER_INDEX + BUFFER.length + -1) % BUFFER.length;
    }

    void BUFFER_RIGHT() {
        BUFFER_INDEX = (BUFFER_INDEX + 1) % BUFFER.length;
    }

    void BUFFER_UP() {
        BUFFER_INDEX = (BUFFER_INDEX + BUFFER.length - COLUMNS) % BUFFER.length;
    }

    void BUFFER_DOWN() {
        BUFFER_INDEX = (BUFFER_INDEX + BUFFER.length + COLUMNS) % BUFFER.length;
    }

    void BUFFER_BACKSPACE() {
        BUFFER_INDEX = (BUFFER_INDEX + BUFFER.length + -1) % BUFFER.length;
        BUFFER[BUFFER_INDEX] = ' ';
    }

    void BUFFER_SCROLL_UP() {
    }

    void BUFFER_SCROLL_DOWN() {
    }

    /** Press C to turn off/on cursors. */
    void toggle_cursor() {
        CURSOR_ON = !CURSOR_ON;
    }

    /** Press /.    */
    void help_screen() {
        show_help = !show_help;
    }

    /** Press S.    */
    boolean toggle_salvia_mode() {
        return salvia_mode = !salvia_mode;
    }

    /** Press CTRL, type equation, press enter. */
    void BUFFER_TO_MAP() {
        fractal.setMapping(new String(BUFFER));
    }

    /** toggle_xor is unused, but the variable XOR_MODE
     * is used directly.     */
    boolean toggle_xor() {
        return XOR_MODE = !XOR_MODE;
    }

    /** Press Y to anti-alias tree. */
    void set_fancy(boolean s) {
        if (s != is_fancy()) {
            toggle_fancy();
        }
    }

    /** Unused. */
    synchronized void rotate_automata() {
        try {
            switch (automata = (automata + 1) % 9) {
                case 0:
                    break;
                case 1:
                    buffer.load_sketch(trilife.draw, this.screen_width, true, this);
                    break;
                case 2:
                    buffer.load_sketch(waves.draw, this.screen_width, true, this);
                    break;
                case 3:
                    buffer.load_sketch(cortex.activationmap, this.screen_width, true, this);
                    break;
                case 4:
                    buffer.load_sketch(waves1.draw, this.screen_width, true, this);
                    break;
                case 5:
                    buffer.load_sketch(waves11.draw, this.screen_width, true, this);
                    break;
                case 6:
                    buffer.load_sketch(waves5.draw, this.screen_width, true, this);
                    break;
                case 7:
                    buffer.load_sketch(waves6.draw, this.screen_width, true, this);
                    break;
                case 8:
                    buffer.load_sketch(danceparty.draw, this.screen_width, true, this);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ;
        }
    }

    /** Unused. */
    void renderrays(Graphics g) {
        g.setColor(Color.GREEN);
        float offset = raytheta * .001f;
        float scale = (float) (2 * Math.PI / 25);
        float xo = this.screen_width / 2f;
        float yo = this.screen_height / 2f;
        float r = Math.min(this.screen_width, this.screen_height) * .5f;
        for (int i = 0; i < 25; i++) {
            double theta = i * scale + offset;
            int x = (int) (xo + r * Math.cos(theta));
            int y = (int) (yo + r * Math.sin(theta));
            g.drawLine((int) xo, (int) yo, x, y);
        }
        raytheta++;
    }
}
