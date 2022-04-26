package perceptron;
/* Perceptron.java
 * Created on December 21, 2006, 5:27 PM
 */

import static java.lang.Math.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import image.ImageCache;
import rendered2D.GlyphLibrary;
import rendered3D.Object3D;
import rendered3D.TreeForm;
import rendered3D.Tree3D;
import rendered2D.Moths;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

import static perceptron.Misc.getDeviceGraphicsConfig;
import static perceptron.Misc.hideCursor;
import static perceptron.Misc.makeUndecoratedMainFrame;
import static perceptron.Misc.makeNotFullscreen;
import static perceptron.Misc.makeFullscreen;
import static perceptron.Misc.clip;
import static java.util.Objects.requireNonNull;
import static perceptron.Preset.helpString;

/**
 * @author mer49
 */
public final class Perceptron extends javax.swing.JFrame {

    public final static Font TEXTFONT = new Font(Font.MONOSPACED,Font.PLAIN,10);
    
    // Try to pull in the object data from disk (static)
    static Object3D fetch_3d_model_data() {
        try {
            return (new Object3D(new BufferedReader(
                    new FileReader(new File("resource/data/tetrahedron.txt")))));
        } catch (FileNotFoundException E) {
            System.out.println("Could not load file resource/data/tetrahedron.txt");
        }
        return null;
    }
    static final Object3D o = fetch_3d_model_data();
    
    /** Timers and counters
     *  last_image_time: Timer (m) for rotating input images
     *  boredom_time: Time (ms) sine last user action
     *  frame: total frame counter
     *  animation_frame: frame counter when saving animations
     */
    long last_image_time = System.currentTimeMillis();
    long boredom_time    = System.currentTimeMillis();
    long frame           = 0;
    long animation_frame = 0;
        
    /** User Editable Parameters.
     *  modifying these after initialization will cause undefined behavior.
     */  
    public int     imageRotateMS       = 5000;
    public int     boredomeMS          = 100000;
    public int     presetRotateMS      = 500000;
    public boolean rotateImages        = false;
    public int     screensaveTimeoutMS = 60000;
    public int     max_frame_length    = 1000 / 20;
    public int     audio_line          = -1;
    public int     min_tree_depth      = 9;
    public int     max_tree_depth      = 6;
    public int     screen_width        = 480;
    public int     screen_height       = 480;
    public int     half_screen_width;
    public int     half_screen_height;
    public boolean frame_rate_display = false;
    public boolean objects_on_top  = true;
    public boolean cap_frame_rate  = false;
    public boolean write_animation = false;
    public boolean draw_moths = false; 
    public boolean draw_top_bars = true;
    public boolean draw_side_bars = true;
    public String  image_directory = "resource/images/";
    
    /** Package accessible instance specific constants.
     *  These values are calculated during initialization and should not
     *  change value while the program is running.     
     */
    //The actual screen dimensions (don't let the rest of the program catch on)
    int physical_width, physical_height;
    
    /** PHYSICAL ONSCREEN GRAPHICS  */
    /** the screen graphics, for displaying rendered frames */
    public final DoubleBuffer buff;
    BufferStrategy     bufferStrategy;
    Graphics2D         graph2D;
    ImageCache         images = null;
    ControlSet         control;
    FractalMap         fractal;
    Tree3D             the_tree;
    TextBuffer         text;
    ArrayList<Mapping> user_maps;
    Preset[]           user_presets;
    BlurSharpen        blursharpen;
    
    boolean running;
    
    /** Save frame.     */
    JFileChooser saver;
    
    //Help Screen Data
    int     stateAlpha   = 255;
    boolean show_help = false;
    String  state = "";
    int     state_length = -1;
    
    AbstractController cortexControls;
    
    int automata = 0;
    Moths moths;
    GlyphLibrary glyphs;
    double GLYPH_THETA = 0;
    int sketchNum = 0;
        
    /**
     * This entire constructor and most of the class design is one giant bug. 
     * Intialization must be done very carefully, the order of operations matter.
     * 
     * @param Settings
     * @param CrashLog
     * @param Presets 
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Perceptron(String Settings, String CrashLog, String Presets) {
        super("Perceptron", getDeviceGraphicsConfig());
        this.setBackground(Color.black);

        // Make the saver window and remember its (smaller) size)
        saver = new JFileChooser("Save State");
        saver.setBounds(0, 0, this.physical_width, this.physical_height);

        // We must parse the settings file before going to full-screen, 
        // because we need to know the desired canvas size to choose a
        // screen resolution.
        user_maps = new ArrayList<>();
        parseSettings(Settings, Presets);  //READ IN SETTINGS INFORMATION
        
        // Make us full-screen
        makeUndecoratedMainFrame(this); //FRAME SETUP
        makeFullscreen(this);           //FULLSCREEN INITIALISATION
        physical_width  = getWidth();
        physical_height = getHeight();

        // Make sure eveything fits togther and makes sense
        checkResolutions();
        
        // screen_height and screen_width are only initialized AFTER parsing
        // the settings file. 
        // Initialise the frame rendering, background, and display buffers
        BufferedImage output = new BufferedImage(
                screen_width, screen_height, BufferedImage.TYPE_INT_RGB);
        BufferedImage bkgnd  = new BufferedImage(
                screen_width, screen_height, BufferedImage.TYPE_INT_RGB);
        BufferedImage dsply  = new BufferedImage(
                screen_width, screen_height, BufferedImage.TYPE_INT_RGB);

        buff = new DoubleBuffer(this, output, bkgnd, null, dsply);
        blursharpen  = new BlurSharpen(buff);
        
        
        //READ IN TRIPPY TEXT FILE
        text = new TextBuffer(this);
        text.loadString(CrashLog);     

        // Initialise the Fractal, the Tree, and their associated parameters
        initialiseObjects();

        /** Initialize user interaction. */
        initialiseListeners();
        
        System.out.println("buffer width : " + screen_width + " height : " + screen_height);
        System.out.println("screen width : " + physical_width + " height : " + physical_height);
        
        moths = new Moths(screen_width,screen_height);

        // Load the preset 0 by default.
        control.applyPreset(0);
    }
    
    /** Check the screen and background resolutions for consistency.
     */
    void checkResolutions() {
        half_screen_width  = (short) (screen_width / 2);
        half_screen_height = (short) (screen_height / 2);
    }

    /** Create and add input event listeners to this JFrame. 
     */
    final void initialiseListeners() {
        control = new ControlSet(this, user_presets);
        this.addMouseListener(control);
        this.addMouseMotionListener(control);
        this.addKeyListener(control);
    }

    /** Initialize the Fractal, Tree, and associated parameters.
     */
    final void initialiseObjects() {
        image_directory = requireNonNull(image_directory);
        images   = new ImageCache(image_directory);
        // Fractal cannot be initialized until the buffers are available
        fractal  = new FractalMap(buff, user_maps, this);
        the_tree  = new Tree3D(
            min_tree_depth,
            max_tree_depth,
            new float[][]{{0, 0, 0}, {0, (float) (-(screen_height / 8)), 0}}, 0,
            new TreeForm[]{new TreeForm(.5f, -.2f, .7f, 7), new TreeForm(.5f, .2f, .7f, -7)},
            new Point(half_screen_width, half_screen_height), buff);
    }

    /**
     * 
     */
    public void showHelp() {
        state = Preset.helpString(this);
        state_length = state.length();
    }
    
    long framerate = 0;

    /**
     *Begin execution
     */
    @SuppressWarnings({"SleepWhileInLoop", "UseSpecificCatch"})
    public void go() {
        System.out.println("Starting...");
        
        hideCursor(this);

        last_image_time = 20000 + System.currentTimeMillis();
        boredom_time    = 50000 + System.currentTimeMillis();

        o.recenter(200);

        frame = 0;

        // If our virtual screen is smaller than the physical screen, we will
        // pad the edges with blac. These are the (x,y) coordinates of the
        // virtual screen on the physica screen device.
        int x_offset = (physical_width  - screen_width) / 2;
        int y_offset = (physical_height - screen_height) / 2;
        System.out.println(" x_offset : " + x_offset);
        System.out.println(" y_offset : " + y_offset);

        long last_time = System.currentTimeMillis();

        showHelp();

        running = true;
        incrementSketch(1);

        /** Load the preset 0 by default. */
        control.applyPreset(control.preset_number);
        
        
        System.out.println("Entering Kernel Loop...");
        while (true) {
            if (running) {
                try {
                    long start_time = System.currentTimeMillis();
                                        
                    // Apply the fractal mapping
                    fractal.operate();
                    
                    // We will draw these later if not objects_on_top
                    if (objects_on_top) drawObjects();
                    
                    control.advance((int)framerate);
                    control.drawAll(buff.out.g);
                    
                    Graphics2D out = (Graphics2D)buff.out.g;
                    renderState(out);
                    renderFramerateDisplay(out);
                    
                    // Copy to physical screen with double buffering.
                    graph2D.drawImage(buff.out.img, x_offset, y_offset, null);
                    graph2D.dispose();
                    bufferStrategy.show();
                    graph2D = (Graphics2D)bufferStrategy.getDrawGraphics();
                    
                    // Save frame to disk
                    if (write_animation) {
                        try {
                            String filename = "animate/frame " + animation_frame + ".png";
                            animation_frame ++;
                            File file = new File(filename);
                            ImageIO.write(buff.out.img, "png", file);
                        } catch (IOException ex) {
                            System.err.println("File write error in animation.");
                            ex.printStackTrace();
                        }
                    }
                    
                    // Background objects: the .output buffer must not be used
                    // to draw these, since this will cause motion blur to 
                    // reveal the drawn objects in the foreground. Neverthelss,
                    // the .output buffer must contain these objects by the time
                    // that the fractal map is applied. 
                    // If we didn't draw objects, draw them now. They will 
                    // show up behind the map in the next frame. 
                    // So, we need to save a copy of the untainted output for
                    // the motion blur code (FractalMap) to use later.
                    if (draw_top_bars) {
                        buff.out.g.setColor(new Color(fractal.fade_color));
                        buff.out.g.fillRect(0, 0, screen_width, BAR_WIDTH);
                        buff.out.g.fillRect(0, screen_height - BAR_WIDTH, screen_width, BAR_WIDTH);
                    }
                    if (draw_side_bars) {
                        buff.out.g.setColor(new Color(fractal.fade_color));
                        buff.out.g.fillRect(0, 0, BAR_WIDTH, screen_height);
                        buff.out.g.fillRect(screen_width-BAR_WIDTH, 0, BAR_WIDTH, screen_height);
                    }
                    if (!objects_on_top) {
                        buff.buf.g2D.drawImage(buff.out.img,0,0,null);
                        drawObjects();
                    }
                    
                    // Applies the blur or sharpen convolution operation.
                    // This acts in-place on the output buffer.
                    blursharpen.operate(fractal.filterweight);
                    
                    // Frame counters and framerate monitoring
                    frame++;
                    long time = System.currentTimeMillis();
                    if (time - last_time >= 1000) {
                        last_time = time;
                        framerate = frame;
                        frame = 0;
                    }
                    start_time += max_frame_length;
                    if (cap_frame_rate) {
                        long delta = start_time - System.currentTimeMillis();
                        if (delta>0) Thread.sleep(delta);
                    }          
                    
                    // We shouldn't need to do this on every frame but
                    // there is a bug somewhere with respect to showing/
                    // hiding the gradient cursor.
                    control.syncCursors();
                    
                } catch (Exception e) {
                    System.out.println("SOMETHING BAD HAPPENED in Perceptron.java go()!!!");
                    e.printStackTrace();
                }
            } else {
                saveFrame();
            }
        }
    }
    
    /**
     * 
     */
    public static final int BAR_WIDTH = 5;
    private void drawObjects() {
        the_tree.render();
        text.renderTextBuffer(buff.out.g2D);
        if (draw_moths) {
            moths.step((float)(20.0/framerate));
            moths.paint((Graphics2D)buff.out.g);
        }
    }
    
    /**
     * 
     */
    public void stimulate() {
        boredom_time = System.currentTimeMillis() + boredomeMS;
    }
    
    /**
     * 
     * @param G 
     */
    private void renderState(Graphics G) {
        /** The additional information for the help screen.
         * (The help screen menu without the realtime
         * update of the setting state next to the setting
         * name.)     */
        final String more_info =
                "---{    PERCEPTRON    }---\n"
                + "LClick@next cursor\n"
                + "RClick@previous cursor\n"
                + "Space   @save\n"
                + "Enter   @execute equation\n"
                + "0-9, Fn @built-in presets\n"
                + "Alt     @pause\n"
                + "Esc     @exit\n"
                + helpString(this);
        final int[] spacer = {5, 65, 195, 330, 385, 515};
        
        // Fade to visible if on, otherwise fade away
        stateAlpha = clip(show_help?max(1,stateAlpha<<1):stateAlpha>>1,0,255);
        
        final var vspace = 11;
        
        if (stateAlpha > 0) {
            G.setColor(new Color(0xFF, 0xFF, 0xFF, (int) stateAlpha));
            G.setFont(TEXTFONT);
            
            StringTokenizer hack = new StringTokenizer(more_info + state, "\n");
            int k = -2;
            int tab = 0;
            while (hack.hasMoreTokens()) {
                k += vspace;
                if (k>screen_height-1) {
                    k = vspace-2;
                    tab = 3;
                }
                StringTokenizer hack2 = new StringTokenizer(hack.nextToken(), "@");
                int si = 0;
                while (hack2.hasMoreTokens()) 
                    G.drawString(hack2.nextToken(), spacer[tab+si++], k);
            }
        }
    }

    /** Save state and the screenshot, "save frame".
     *  Press SPACE.      
     */
    private void saveFrame() 
    {
        makeNotFullscreen();
        saver.setVisible(true);
        saver.setFocusable(true);
        saver.requestFocus();
        int approved = saver.showSaveDialog(this);
        saver.grabFocus();
        File file = saver.getSelectedFile();
        System.out.println("saving...");
        if (file != null && approved == JFileChooser.APPROVE_OPTION) {
            try {
                System.out.println("...writing image...");
                ImageIO.write(buff.out.img, "png", new File(file.getAbsolutePath() + ".out.png"));    // screenshot
                ImageIO.write(buff.buf.img, "png", new File(file.getAbsolutePath() + ".in.png"));    // screenshot of the frame - 1
            } catch (IOException ex) {
                System.err.println("File write error while saving images.");
                Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                System.out.println("...writing state...");
                Preset.write(this, new File(file.getAbsolutePath() + ".state")); // save with extension *.state
            } catch (IOException ex) {
                System.err.println("File write error while saving state.");
                Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("...saved.");
        } else {
            System.out.println("...did not save.");
        }
        System.out.println("Done.");
        makeFullscreen(this);
        hideCursor(this);
        running = true;
    }
    
    
    /** Read in the settings file. 
     */
    @SuppressWarnings("ConvertToStringSwitch")
    final void parseSettings(String settings_path, String presets_path) 
    {
        ArrayList<Preset> presets = new ArrayList<>();
        //Create exc FileReader for reading the file
        try {
            BufferedReader in = new BufferedReader(new FileReader(settings_path));
            String thisLine;
            while ((thisLine = in.readLine()) != null) {
                if (thisLine.length() > 0 && thisLine.charAt(0) != '*') {
                    
                    StringTokenizer token = new StringTokenizer(thisLine);
                    if (token.countTokens() >= 2) {
                        
                        String var = token.nextToken();
                        String val = token.nextToken();
                        
                        if (var.equals("preset")) {
                            try {
                                System.out.println("parsing preset " + val + ":");
                                presets.add(Preset.parse(in));
                            } catch (IOException e) {
                                System.err.println("(failed)");
                            }
                        } else if (var.equals("map")) {
                            try {
                                user_maps.add(FractalMap.makeMapStatic(val));
                                System.out.println("map : " + val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } 
                        } else {
                            //parse primitive
                            Object value = Misc.hackyParse(val);
                            if (null != value)
                                try {
                                    this.getClass().getField(var).set(this, value);
                                } catch (NoSuchFieldException 
                                        | SecurityException 
                                        | IllegalArgumentException
                                        | IllegalAccessException ex) {
                                    System.err.println("I could not set "+var+" to "+val+"; parsed as "+value);
                                    Logger.getLogger(Perceptron.class.getName()).log(Level.WARNING, null, ex);
                                }
                        }
                    }
                }
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("Error reading settings file");
            Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE, null, ex);
        }

        File f = new File(presets_path);

        if (f.listFiles() != null) 
        {
            for (var file : f.listFiles()) {
                String name = file.getName();
                if (name.endsWith(".state")) {
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(file));
                        System.out.println("parsing preset " + name + ":");
                        presets.add(Preset.parse(in));
                        in.close();
                    } catch (FileNotFoundException ex) {
                        System.err.println("Could not find preset file "+name);
                        Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        System.err.println("Error loading preset "+name);
                        Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        user_presets = (Preset[])(presets.toArray(Preset[]::new));
    }

    /**
     *
     * @return
     */
    public int screen_width() {
        return screen_width;
    }

    /**
     *
     * @return
     */
    public int screen_height() {
        return screen_height;
    }

    /**
     *
     * @return
     */
    public int half_screen_width() {
        return half_screen_width;
    }

    /**
     *
     * @return
     */
    public int half_screen_height() {
        return half_screen_height;
    }

    /**
     *
     * @return
     */
    public int physical_width() {
        return physical_width;
    }

    /**
     *
     * @return
     */
    public int physical_height() {
        return physical_height;
    }

    /**
     *
     */
    public void toggleObjectsOnTop() {
        objects_on_top = !objects_on_top;
    }

    /** Press Z to see the frame rate. */
    public void toggleFramerateDisplay() {
        frame_rate_display = !frame_rate_display;
    }

    /** Press U to freeze the frame rate. */
    public void toggleCapFramerate() {
        cap_frame_rate = !cap_frame_rate;
    }

    /** Press B.
     * @param b */
    public void setObjectsOnTop(boolean b) {
        objects_on_top = b;
    }

    /** Press backquote Space to call the save menu.
     * The save_frame() appears when not running.     */
    public void save() {
        running = false;
    }

    /** 
     * Press M to go to the next image located in the resource images 
     * or N to go back.
     * Used in the image mode (press L).
     * @param n */
    public void incrementSketch(int n) {
        buff.loadImage(images.advance(n));
        sketchNum = images.current();
        last_image_time = imageRotateMS + System.currentTimeMillis();
    }

    /**
     * 
     * @param n 
     */
    public void setSketch(int n) {
        try {
            sketchNum = n;
            buff.loadImage(images.get(n));
            last_image_time = 30000 + System.currentTimeMillis();
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    /** Press Y to set fancy graphics for 3D tree. */
    public void toggleFancy() {
        buff.toggleFancy();
    }

    /** * Press A to set fancy, anti-alias graphics for the entire program.
     * Defaults to yes.
     * @return  */
    public boolean isFancy() {
        return buff.fancy;
    }

    void setFancy(boolean s) {
        if (s != isFancy()) toggleFancy();
    }

    /** Take snapshot of every frame - animate.
     * Press Home.      
     */
    public void toggleAnimation() {
        write_animation = !write_animation;
    }

    /** Press /.    */
    void helpScreen() {
        show_help = !show_help;
    }

    private void renderFramerateDisplay(Graphics2D out) {
        if (frame_rate_display) {
            out.setColor(Color.WHITE);
            out.setFont(TEXTFONT);
            String s = ""+framerate;
            FontMetrics fm = getFontMetrics(TEXTFONT);
            Rectangle2D b = fm.getStringBounds(s, out);
            int w = (int)round(b.getWidth());
            int h = (int)round(b.getHeight());
            out.drawString(s, screen_width-2-w, screen_height-2);
            out.drawString(s, 2, h);
            out.drawString(s, screen_width-2-w, h);
            out.drawString(s, -2-w, screen_height-2);
         }
    }
    
}
