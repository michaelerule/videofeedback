package perceptron;

import automata.*;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamStorage;
import cortex.VisualCortex;
import image.ImageCache;
import math.complex;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.monte.media.Buffer;
import org.monte.media.Codec;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.media.quicktime.QuickTimeWriter;
import org.monte.screenrecorder.ScreenRecorderMain;
import rendered2D.CellModel;
import rendered2D.DanceParty;
import rendered2D.GlyphLibrary;
import rendered2D.Moths;
import rendered3D.Tree3D;
import rendered3D.TreeForm;
import word.WordProcessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.GlyphVector;
import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import static java.lang.Math.min;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;
import static org.monte.media.VideoFormatKeys.DataClassKey;
import static org.monte.media.VideoFormatKeys.FrameRateKey;


/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 * @author Predrag Bokšić (junkerade@gmail.com)
 *         <p/>
 *         <p/>Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *         It evolves colored geometric patterns and visual images into the realm of infinite details
 *         and deepens the thought.</p>
 *         <p/>
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 */

public final class Perceptron extends JFrame implements ComponentListener, ItemListener {

    // whether to store program preferences in a local file, or in a system-dependent manner per user
    public boolean system_based_preferences = false;
    // program preferences stored in system-dependent manner. in windows, registry key
    // HKEY_CURRENT_USER\Software\JavaSoft\Prefs\perceptron stores some settings
    public Preferences PREFS;
    // program preferences stored in a local file in Perceptron folder
    public PropertiesConfiguration config;

    // graphics configuration
    public GraphicsConfiguration gc = Main.gc;

    // windowed or full screen mode
    public boolean windowed_mode = true;

    // fancy graphics effects for the drawn objects: 0 to 4, overridden by settings file
    public static int fancy_graphics = 0;

    // the effective screen dimensions (drawing canvas), overridden by settings file
    public int screen_width = 600;
    public int screen_height = 600;
    public int half_screen_width = screen_width >> 1;
    public int half_screen_height = screen_height >> 1;
    public Rectangle canvas;

    // rotate loaded images
    public boolean rotateImages = false;
    public int imageRotateMS = 5000;
    public int boredomeMS = 100000;
    //public int presetRotateMS = 500000;
    //public int screensaveTimeoutMS = 50000;

    // main operation control
    public boolean infinity = true;
    public boolean running = true;
    public boolean save_as_running = false;
    public boolean open_preset_running = false;
    public boolean write_animation = false;
    public boolean save_animation_menu_running = false;
    public boolean open_image_running = false;
    public boolean animation_menu_ran_once = false;
    boolean folder_selected = false;

    // time control
    long last_image_time = System.currentTimeMillis();
    long boredom_time = System.currentTimeMillis();
    long anim_frame = 0; // for animation
    long frame = 0; // for frame count, framerate and slow mode
    // time keepers in go()
    long time, start_time, last_time, framerate;

    // used to slow down operation (press u)
    public int max_frame_time_length = 0;

    // Whether or not to display the frame rate in the upper left corner
    public boolean frame_rate_display = false;

    // Cursors and letters as persistent initial set
    public boolean persistent_initial_set = true;

    // Load all the images from this folder.
    public String image_folder = "resource/images";

    // Default animation save folder
    public String animation_folder = "animations";

    // Set of core image procedures
    public DoubleBuffer double_buffer;

    // storage of equations
    public ArrayList<Mapping> user_maps = new ArrayList<>();

    // convolution
    Convolution convolution;
    // convolution algorithm selection
    public int convolution_degree = 1;

    // use less conservative image opener with image preview and thumbnails
    public boolean conservative_image_opener = false;

    // Object3D o = fetch();
    //
    // Object3D fetch() {
    // try {
    // return (new Object3D(new BufferedReader(
    // new FileReader(new File("resource/data/tetrahedron.txt")))));
    // } catch (Exception E) {
    // System.out.println("Could not load file resource/data/tetrahedron.txt");
    // System.exit(1);
    // }
    // return null;
    // }

    // Folders for saver and open menus (savers and openers)
    String current_save_folder;
    String current_open_folder;
    String current_open_image_folder;

    // The actual screen dimensions or JFrame window/component size
    int physical_width, physical_height;

    // JFrame borders, their thickness.
    Insets screen_insets, window_insets;
    int insetWide, insetTall, screen_insetWide, screen_insetTall;

    // Perceptron's icon
    ImageIcon icon;

    // PHYSICAL ONSCREEN GRAPHICS for displaying rendered frames.
    GraphicsDevice gd;
    DisplayMode display_mode;
    BufferStrategy bufferStrategy;
    Graphics graphics, draw_graphics, graphics_of_buffered_image;
    int display_width, display_height;

    // Image (data) buffers
    public BufferedImage buffered_image;
    BufferedImage output;
    BufferedImage background;
    BufferedImage display;

    // location of image to be drawn on screen
    int x_offset;
    int y_offset;

    // the default cache (storage) of image files from folder resource/images
    ImageCache images;

    // cursors keyboard controls and such
    ControlSet controls;

    // fractal mapping that draws Julia set through conformal mapping
    FractalMap fractal;

    // artistic 3D tree (artificial IFS fractal)
    Tree3D the_tree;
    public int min_tree_depth = 1;
    public int max_tree_depth = 12;

    // storage of presets
    Preset[] user_presets;
    ArrayList<Preset> list_of_presets = new ArrayList<>();

    // show the color-changing letters of the equation editor
    boolean salvia_mode = false;
    // Text (equation) Editor Data with implication in the text experiment
    // when the perceptron starts, turn of text cursor if salvia mode is disabled
    boolean CURSOR_ON = false;
    public String salvia_font = "Serif";
    Font F;
    // whiter letters mode
    boolean XOR_MODE = false;
    int COLUMNS = 18;
    int ROWS = 12;
    char[] BUFFER = new char[ROWS * COLUMNS];
    int SIZE = 36;
    int BUFFER_INDEX = 0;
    final int[] SALVIA_COLORS = {0x00ff00, 0xff0000, 0x0000ff, 0xffff00, 0x00ffff, 0xff00ff};
    final int[] spacer = {5, 35, 155, 300, 345};

    // text experiment
    BufferedReader infile;
    int read, bufferSourceIndex = 0;

    // print all fonts detected on a system, in a small, separate window
    public boolean print_all_fonts = false;

    // text help on the screen
    public boolean show_help = false;
    // transparency of on-screen help
    public int help_font_alpha = 255;
    // F1 help window
    WordProcessor wordProcessor;


    // The static help screen menu on the right side of the screen.
    String more_info = ".... o O      PERCEPTRON      O o ....\n"
            + "                       @                               \n" // map string protrudes from the left side here
            + "F1               @          help (press / for small help)\n"
            + "Page Down        @          settings window\n"
            + "Page Up          @          grabber window\n"
            + "Left Click       @          select next cursor\n"
            + "Right Click      @          select previous cursor\n"
            + "]    @  add dot to current cursor\n"
            + "[    @  remove dot from current cursor\n"
            + "-    @  slow down current cursor\n"
            + "=    @  speed up current cursor\n"
            + "Space    @  save state and screenshot\n"
            + "`    @  load state\n"
            + "\\    @  load image\n"
            + "Enter    @  execute equation\n"
            + "0-9, Fn  @  built-in presets\n"
            + "Alt    @  release mouse pointer\n"
            + "Pause    @  pause on/off\n"
            + "Esc    @  exit\n";
    // String value is injected into state string using show_help_screen() and it becomes the contents of the help screen
    // together with the live display of variable states. It is positioned on the left side of the screen.
    String state = "";

    // render rays
    int raytheta = 0;

    // Settings window
    public Settings_Window sw;

    // screen grabber
    Robot screen_grabber_robot;
    BufferedImage screenShot;
    Dimension dim;
    ScreenGrabber screen_grabber_frame;
    ResizeListener resize_listener = new ResizeListener() {
        @Override
        public void drawAreaChanged(int x, int y, int width, int height) {
            canvas.setBounds(x, y, width, height);
        }
    };

    /**
     * Movie recorder is based on Monte Media 0.7.7 by © 2012 Werner Randelshofer.
     * http://www.randelshofer.ch/monte
     * Monte Media is licensed under a Creative Commons Attribution 3.0 Unported License.
     * http://creativecommons.org/licenses/by/3.0/
     * Now we are downloading it from http://mvnrepository.com/artifact/com.pojosontheweb/monte-repack/
     */
    public QuickTimeWriter movie_writer_qt;
    Format movie_format;
    String movie_type = "QuickTime movie";
    // 0 is slow frame piling, 1 is fast cameraman
    int movie_recorder_type = 1;
    float movie_quality = 0.1f; // goes into the movie format and compression level
    int movie_track;
    // use timer to record a frame every 1000/movie_fps milliseconds = 40 for 25 fps or 20 ms -> 50 fps
    int movie_fps = 25;
    int timer_schedule = 40;
    File movie_file;
    // screen grabber/capture settings
    int live_grabber_count;
    Rectangle grabber_rectangle;
    // grab screen continuously
    boolean live_grab = false;
    // The new standalone Screen Recorder class for recording purposes
    public ScreenRecorderMain screenRecorderMain;


    /**
     * Webcam support by @author Bartosz Firyn (SarXos).
     * http://webcam-capture.sarxos.pl/
     * https://github.com/sarxos/webcam-capture
     * Copyright (C) 2012 - 2013 Bartosz Firyn
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
     */
    Webcam webcam;
    java.util.List<Webcam> webcams = new ArrayList<>();
    boolean webcam_grab = false; // grab webcam input continuously
    BufferedImage webcam_capture_formatted;
    int webcam_grabber_count;
    boolean cam_works = false;
    public boolean webcam_support = true; // tweak support in the settings file

    //// DISABLED - experimental   ///////////////////////////////
    // cellular automaton experiment
    int automata;
    CellModel life;
    SimpleAutomata1 trilife;
    Hallucination1 waves;
    Waves_1 waves1;
    Waves_11 waves11;
    Waves_5 waves5;
    Waves_6 waves6;
    DanceParty danceparty;
    // geometric visual hallucination patterns according to Bresloff and others. unused in production version.
    VisualCortex cortex;
    AbstractController cortexControls;
    // other
    Moths moths;
    GlyphLibrary glyphs;
    double GLYPH_THETA = 0;
    // audio support
    public int audio_line = -1;
    ///////////////////////////////////////////////////////////////

    //static { System.out.println("is Perceptron on edt? " + javax.swing.SwingUtilities.isEventDispatchThread()); }

    ////////////////////////////////////////////////////////////////
    ////////////            CONSTRUCTOR                 ////////////
    public Perceptron(String Settings, String CrashLog, String Presets) {

        // Call JFrame constructor that utilises this graphics configuration.
        super("Perceptron", Main.gc);
        // setup threads
        Thread.currentThread().setName("Perceptron : main window thread");
        // default graphics device
        gd = Main.gd;
        // setup of preferences
        preferences();
        // Set main window parameters.
        window_parameters();
        // parse settings and load presets and images
        parse_settings(Settings, Presets);
        // load log file
        parse_log(CrashLog);
        // initialize screen mode, window
        screen_mode();
        // restore window
        window_position();
        // conceal conventional mouse pointer
        hide_cursor();
        // Setup of various BufferedImages and the DoubleBuffer class according to the screen dimensions.
        buffer();
        // Graphics object for drawing.
        graphics();
        // Convolution or blurring with the parameter loaded from settings.
        convolution();
        // Initialize the Fractal, the Tree, and the image cache.
        objects();
        // Initialize user interaction.
        listeners();
        // Load the preset 0 by default, which refers to the first alphabetical file in presets folder.
        controls.apply_preset(0);
        // initialize screen capturing capability
        screen_grabber();
        // webcam setup
        if (webcam_support) {
            webcam();
        } else {
            cam_works = false;
        }
        // Print all fonts for reference
        if (print_all_fonts) print_fonts();
        // Initialize the main settings window.
        settings_window();
        /** Execute the active rendering loop - method go().  */
        Runnable G_run = this::go;
        Main.executor.execute(G_run);

    }
    ////////////////////////////////////////////////////////////////


    static GraphicsDevice graphics_device() {
        //GraphicsDevice gtest[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();  // TODO add screen chooser
        //int number_of_graphics_devices = gtest.length;
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }


    static GraphicsConfiguration getDeviceGraphicsConfig() {
        return ((graphics_device().getDefaultConfiguration()));
    }


    /**
     * Store window preferences.
     */
    void preferences() {
        // OS-dependent user preferences storage
        try {
            PREFS = Preferences.userNodeForPackage(Perceptron.class);
        } catch (NullPointerException | SecurityException e) {
            e.printStackTrace();
        }
        // setup portable configuration storage using a local file
        try {
            String username = System.getProperty("user.name");
            File configFile = new File("perceptron.preferences" + "." + username);
            if (configFile.exists()) {
                System.out.println("config file found: " + configFile);
                if (configFile.canRead()) {
                    System.out.println("config file can be read: " + configFile);
                    if (configFile.canWrite()) {
                        System.out.println("config file can be written: " + configFile);
                        config = new PropertiesConfiguration(configFile);
                    } else {
                        System.out.println("config file CANNOT be written: " + configFile);
                        System.out.println("switching to system dependent preferences storage.");
                        system_based_preferences = true;
                    }
                } else {
                    System.out.println("config file CANNOT be read: " + configFile);
                }
            } else {
                System.out.println("config file NOT found: " + configFile);
                // try to create configuration file for local storage
                if (configFile.createNewFile()) {
                    config = new PropertiesConfiguration(configFile);
                    System.out.println("config file CREATED: " + configFile);
                } else {
                    System.out.println("config file NOT CREATED" + "\n" + "switching to system dependent preferences storage.");
                    system_based_preferences = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Basic properties of Perceptron JFrame.
     */
    void window_parameters() {

        /**   window check       */
        try { // everything is beautiful :-)
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        //System.out.println(new NimbusLookAndFeel().getSupportsWindowDecorations());
                        break;
                    } else {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
                System.err.println("Nimbus look and feel not found on this Java system.");
            }
            setDefaultLookAndFeelDecorated(true);
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
            ToolTipManager.sharedInstance().setInitialDelay(0);
            ToolTipManager.sharedInstance().setDismissDelay(60000);

        } catch (IllegalComponentStateException e) {
            e.printStackTrace();
            System.err.println("Unsupported look and feel.");
        }

        // request focus
        setFocusable(true);
        setFocusableWindowState(true);
        setAutoRequestFocus(true);

        // set window color and repainting
        setBackground(Color.black);
        setForeground(Color.black);
        setIgnoreRepaint(true);

        // Exit on window close using the window adapter defined later
        try {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        } catch (SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not set default close operation EXIT_ON_CLOSE for the window.");
        }

        icon = new ImageIcon("resource/icons/icon.jpg");
        setIconImage(icon.getImage());

    }


    /**
     * Parser for the settings file. Loads screen resolution and other parameters such as default resource folders.
     * Moves on to read in presets and send them to the preset parser.
     */
    private void parse_settings(String settings_path, String presets_folder) {
        try {
            // load settings.txt file
            BufferedReader in = new BufferedReader(new FileReader(settings_path));
            // platform-specific pathname check
            if (File.separatorChar == '\\') {
                settings_path = settings_path.replace('/', File.separatorChar);
                settings_path = settings_path.replace('\\', File.separatorChar);
                System.out.println("parsing settings: " + settings_path);
            }
            if (File.separatorChar == '/') {
                settings_path = settings_path.replace('\\', File.separatorChar);
                settings_path = settings_path.replace('/', File.separatorChar);
                System.out.println("parsing settings: " + settings_path);
            }
            // Settings.txt reader, line by line, turns line into tokens
            String current_line;

            while ((current_line = in.readLine()) != null) { // read a line

                if (current_line.length() > 0 && current_line.charAt(0) != '*') { // skip lines that begin with *
                    StringTokenizer token = new StringTokenizer(current_line);

                    if (token.countTokens() == 1) { // single token lines are considered faulty
                        System.out.println("Could not interpret this line in Settings.txt: " + current_line);
                    }

                    if (token.countTokens() >= 2) {

                        String var = token.nextToken(); // var val pairs in single lines
                        String string_val = token.nextToken();
                        String val = string_val.toLowerCase(); // val is lower case?

                        if (var.equals("preset_folder") && string_val.startsWith("<")) { // load presets folder

                            try {
                                int i = current_line.indexOf("<") + 1;
                                StringBuilder s = new StringBuilder();
                                while (current_line.charAt(i) != '>') {
                                    char file_name_letter = current_line.charAt(i);
                                    if (File.separatorChar == '\\') { // platform check for pathname
                                        if (file_name_letter == '/' || file_name_letter == '\\') {
                                            s.append(File.separatorChar);
                                        } else {
                                            s.append(file_name_letter);
                                        }
                                    }
                                    if (File.separatorChar == '/') {
                                        if (file_name_letter == '\\' || file_name_letter == '/') {
                                            s.append(File.separatorChar);
                                        } else {
                                            s.append(file_name_letter);
                                        }
                                    }
                                    i++;
                                }
                                presets_folder = s.toString();
                                System.out.println("presets folder: " + presets_folder);
                                File presetsfolder = new File(presets_folder);
                                if (!presetsfolder.exists() && !presetsfolder.canRead()) {
                                    System.err.println("Presets folder denoted in settings file is invalid: \"" + presetsfolder.toString() + "\"");
                                    Main.exit();
                                }
                            } catch (Exception e) {
                                System.err.println("Errors found while parsing settings file.");
                                e.printStackTrace();
                                Main.exit();
                            }

                        } else if (var.equals("map")) { // load maps

                            try {
                                // attempt to remove spaces from maps since MathToken does not read spaces
                                current_line = current_line.replaceAll(" ", "");
                                val = current_line.replaceAll("map", "");
                                user_maps.add(FractalMap.makeMap(val));
                                System.out.println("map: " + val);
                            } catch (Exception e) {
                                System.err.println("Invalid map: " + val);
                                e.printStackTrace();
                            }

                        } else if (var.equals("image_folder") && string_val.startsWith("<")) { //load image folder

                            try {
                                int i = current_line.indexOf("<") + 1;
                                StringBuilder s = new StringBuilder();
                                while (current_line.charAt(i) != '>') {
                                    char file_name_letter = current_line.charAt(i);
                                    s.append(file_name_letter);
                                    i++;
                                }
                                image_folder = s.toString();
                                if (File.separatorChar == '\\') {
                                    image_folder = image_folder.replace('/', File.separatorChar);
                                    image_folder = image_folder.replace('\\', File.separatorChar);
                                }
                                if (File.separatorChar == '/') {
                                    image_folder = image_folder.replace('\\', File.separatorChar);
                                    image_folder = image_folder.replace('/', File.separatorChar);
                                }
                                System.out.println("image folder: " + image_folder);
                                File imagefolder = new File(image_folder);
                                if (!imagefolder.exists() && !imagefolder.canRead()) {
                                    System.err.println("Image folder denoted in settings file is invalid.");
                                    Main.exit();
                                }
                            } catch (Exception e) {
                                System.err.println("Errors found when parsing image folder: " + image_folder);
                                e.printStackTrace();
                            }

                        } else if (var.equals("salvia_font") && string_val.startsWith("\"")) {

                            try {
                                int i = current_line.indexOf("\"") + 1;
                                StringBuilder s = new StringBuilder();
                                while (current_line.charAt(i) != '\"') {
                                    char c = current_line.charAt(i);
                                    s.append(c);
                                    i++;
                                }
                                salvia_font = s.toString();
                                F = new java.awt.Font(salvia_font, Font.PLAIN, SIZE);
                                System.out.println("equation editor uses font: " + salvia_font);
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.err.println("Failed to set salvia font: " + salvia_font + ". Check settings.");
                            }

                        } else { // try to see which type is the val and load it
                            // val is string, but try to make another type of Object from it
                            Object value = new Object();
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
                                                        switch (val) {
                                                            case "true":
                                                                value = true;
                                                                break;
                                                            case "false":
                                                                value = false;
                                                                break;
                                                        }
                                                    } catch (Exception e7) { // no other type could be made from val string
                                                        System.err.println("NO VALID SETTING MATCHES: " + val);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            try { // some type Object called value was made from val
                                if (getClass().getField(var) != null) { // looking for var among fields in this class
                                    // prevent unplanned variable loading
                                    if (!var.equals("system_based_preferences") &&
                                            !var.equals("screen_width") &&
                                            !var.equals("screen_height") &&
                                            !var.equals("windowed_mode") &&
                                            !var.equals("fancy_graphics") &&
                                            !var.equals("convolution_degree") &&
                                            !var.equals("print_all_fonts") &&
                                            !var.equals("help_font_alpha") &&
                                            !var.equals("min_tree_depth") &&
                                            !var.equals("max_tree_depth") &&
                                            !var.equals("webcam_support")) {
                                        System.err.println("Settings.txt is trying load forbidden variable: " + var);
                                        System.err.println("Check settings.");
                                        Main.exit();
                                    }
                                    // set val to var field - a public variable in this class
                                    getClass().getField(var).set(this, value);
                                    System.out.println(var + " " + val);
                                    // check loaded variables
                                    if (!system_based_preferences && system_based_preferences)
                                        system_based_preferences = false;

                                    display_mode = gd.getDisplayMode();
                                    if (screen_width <= 0 || screen_height <= 0) {
                                        System.err.println("Screen width or height set to 0 or negative. Check settings.");
                                        System.err.println("Setting default screen size 600 x 600.");
                                        screen_width = 600;
                                        screen_height = 600;
                                    }
                                    if (screen_width > display_mode.getWidth()) screen_width = getWidth();
                                    if (screen_height > display_mode.getHeight()) screen_height = getHeight();

                                    if (fancy_graphics < 0 || fancy_graphics > 4) fancy_graphics = 1;

                                    if (convolution_degree < 1) convolution_degree = 1;
                                    if (convolution_degree > 16) convolution_degree = 1;

                                    if (!print_all_fonts && print_all_fonts) print_all_fonts = false;

                                    if (help_font_alpha < 0) help_font_alpha = 255;
                                    if (help_font_alpha > 255) help_font_alpha = 255;

                                    if (min_tree_depth < 1) min_tree_depth = 1;
                                    if (min_tree_depth > 29) min_tree_depth = 1;

                                    if (max_tree_depth < 1) max_tree_depth = 9;
                                    if (max_tree_depth > 29) max_tree_depth = 9;

                                } else {
                                    System.err.println("Could not interpret this line in Settings.txt: " + current_line);
                                }
                            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                                System.err.println("Could not interpret this line in Settings.txt: " + current_line);
                                //e.printStackTrace();
                            }
                        }
                    }
                }
            }
            in.close();
            // load all the presets
            File presets_file = new File(presets_folder);
            if (presets_file != null && presets_file.listFiles() != null) {
                Arrays.sort(presets_file.listFiles()); // load files alphabetically
                for (File file : presets_file.listFiles()) {
                    String name = file.getName();
                    try {
                        if (name.endsWith(".state")) {
                            in = new BufferedReader(new FileReader(file));
                            System.out.println("parsing preset: \"" + presets_file.getAbsoluteFile() + File.separatorChar + name + "\"");
                            list_of_presets.add(Preset.parse(in));
                            System.out.println("preset: \"" + presets_file.getAbsoluteFile() + File.separatorChar + name + "\" is number: " + (list_of_presets.size() - 1));
                        } else {
                            System.out.println("Discovered file name was not *.state");
                        }
                    } catch (Exception e) {
                        System.err.println("Something went wrong while parsing a preset: \"" + file.toString() + "\"");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Tried loading settings and presets and failed.");
        }
        user_presets = list_of_presets.toArray(new Preset[list_of_presets.size()]);
    }


    /**
     * Log file loader.
     *
     * @param string
     */
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


    /**
     * Set up screen mode.
     */
    void screen_mode() {
        // stop running to use [Enter] as a window position and size reset button. prevents buffers not ready in fullscreen mode
        running = false;

        display_mode = gd.getDisplayMode();
        display_width = display_mode.getWidth();
        display_height = display_mode.getHeight();

        // opaqueness of the GUI for active rendering
        //c = getContentPane();
        //if (c instanceof JComponent) ((JComponent) c).setOpaque(false);
        //else c.setBackground(new Color(0, 0, 0, 0));

        // insets on the given desktop screen (e.g. thickness of taskbar)
        try {
            screen_insets = Toolkit.getDefaultToolkit().getScreenInsets(Main.gc);
        } catch (HeadlessException e) {
            e.printStackTrace();
        }
        windowed_mode = false;
        // windowed mode
        if (windowed_mode) {

            running = false;

            try {
                // window setup
                setVisible(true);
                // thickness of window edges
                window_insets = getInsets();
                insetWide = window_insets.left + window_insets.right;
                insetTall = window_insets.top + window_insets.bottom;
                screen_insetWide = screen_insets.left + screen_insets.right;
                screen_insetTall = screen_insets.top + screen_insets.bottom;
                // check screen size specified in settings and adapt it if necessary
                if ((screen_width <= display_width - insetWide - screen_insetWide) && (screen_height <= display_height - insetTall - screen_insetTall)) {
                    // set size plus some pixels for wide window edge in Windows
                    setSize(screen_width + insetWide, screen_height + insetTall);
                    // position it on the middle of the screen
                    setLocation(((display_width - getWidth() - screen_insetWide) / 2), ((display_height - getHeight() - screen_insetTall) / 2));
                } else if ((screen_width > display_width - insetWide - screen_insetWide) || (screen_height > display_height - insetTall - screen_insetTall)) {
                    // trim the dimension that is sticking out
                    if ((screen_width > display_width - insetWide - screen_insetWide) && (screen_height <= display_height - insetTall - screen_insetTall))
                        screen_width = display_width - insetWide - screen_insetWide;
                    if ((screen_height > display_height - insetTall - screen_insetTall) && (screen_width <= display_width - insetWide - screen_insetWide))
                        screen_height = display_height - insetTall - screen_insetTall;
                    setSize(screen_width + insetWide, screen_height + insetTall);
                    setLocation(((display_width - getWidth()) / 2), ((display_height - getHeight()) / 2));
                }
                // drawing canvas setup
                physical_width = screen_width;
                physical_height = screen_height;
                half_screen_width = screen_width >> 1;
                half_screen_height = screen_height >> 1;
                canvas = new Rectangle(0, 0, screen_width, screen_height);
                resize_listener.drawAreaChanged(window_insets.left, window_insets.top, getWidth() - (window_insets.left + window_insets.right), getHeight() - (window_insets.top + window_insets.bottom));
                x_offset = canvas.x;
                y_offset = canvas.y;
                // graphics setup: create buffer strategy for jframe and derive graphics from it
                createBufferStrategy(2, gd.getDefaultConfiguration().getBufferCapabilities());
                bufferStrategy = getBufferStrategy();
                graphics = bufferStrategy.getDrawGraphics();
                // draw into jframe directly, but center graphics on the empty canvas/rectangle inside the window
                graphics.translate(canvas.x, canvas.y);

            } catch (NullPointerException | IllegalArgumentException | IllegalStateException | AWTException e) {
                e.printStackTrace();
                System.err.println("Could not establish drawing mode.");
            }

            // don't lose focus or keyboard controls
            requestFocus();
            running = true;

        } else { // FULLSCREEN MODE

            running = false;

            // First we setup things that make Perceptron drawing canvas a single, centered field of view that prevents
            // the mouse from leaving...
            //  This code block must precede the step in which we set the fullscreen mode itself.
            //  If its called later on after the initial run, dispose() must run instead of setVisible(false).
            setVisible(false); // cannot setUndecorated if frame is displayable
            dispose(); // release resources, the frame is not displayable and setUndecorated works
            try {
                setUndecorated(true);
            } catch (IllegalComponentStateException e) {
                e.printStackTrace();
                System.err.println("Undecorating the fullscreen frame failed.");
            }
            setResizable(false);
            ///////////////////////////////////////
            // determine the screen insets, if any
            screen_insetWide = screen_insets.left + screen_insets.right;
            screen_insetTall = screen_insets.top + screen_insets.bottom;

            setBounds(screen_insets.left, screen_insets.top, screen_insets.right, screen_insets.bottom); // testing

            // check screen size specified in settings and adapt it if necessary
            if ((screen_width <= display_width - screen_insetWide) && (screen_height <= display_height - screen_insetTall)) {
                // position it on the middle of the screen
                setLocation(((display_width - getWidth() - screen_insetWide) / 2), ((display_height - getHeight() - screen_insetTall) / 2));
                setSize(screen_width, screen_height);
            } else if ((screen_width > display_width - screen_insetWide) || (screen_height > display_height - screen_insetTall)) {
                if (screen_width > display_width - screen_insetWide) screen_width = display_width - screen_insetWide;
                if (screen_height > display_height - screen_insetTall)
                    screen_height = display_height - screen_insetTall;
                setLocation(((display_width - getWidth() - screen_insetWide) / 2), ((display_height - getHeight() - screen_insetTall) / 2));
                setSize(screen_width, screen_height);
            }
            // drawing canvas setup
            physical_width = display_width - screen_insetWide;
            physical_height = display_height - screen_insetTall;
            half_screen_width = screen_width >> 1;
            half_screen_height = screen_height >> 1;
            canvas = new Rectangle(0, 0, screen_width, screen_height);
            x_offset = (physical_width - screen_width) / 2;
            y_offset = (physical_height - screen_height) / 2;
            // enter fullscreen mode
            if (gd.isFullScreenSupported()) {
                try {
                    gd.setFullScreenWindow(this);
                    // Setting up Double Buffering. Once we create buffer strategy, we get the Graphics object for drawing.
                    try {
                        createBufferStrategy(2, gd.getDefaultConfiguration().getBufferCapabilities());
                    } catch (AWTException | IllegalArgumentException e) {
                        e.printStackTrace();
                        System.err.println("Could not set up double bufferring.");
                    }
                    bufferStrategy = getBufferStrategy();
                    graphics = bufferStrategy.getDrawGraphics();
                    // focus needs to go between main application and other windows
                    requestFocus();

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to enter FullScreen mode.");
                }
            } else {
                windowed_mode = true;
                screen_mode();
            }
            running = true;
        }
    }


    /**
     * Restore previous window position and size.
     */
    void window_position() {
        // load previous position and size. reset it by pressing enter. previous size still depends from the size set in settings file.
        if (windowed_mode) {
            if (system_based_preferences) {
                // safety check
                if ((PREFS.getInt("perceptron_location.x", 0) >= 0) && (PREFS.getInt("perceptron_location.x", 0) < display_mode.getWidth()) &&
                        (PREFS.getInt("perceptron_location.y", 0) >= 0) && (PREFS.getInt("perceptron_location.y", 0) < display_mode.getHeight()) &&
                        (PREFS.getInt("perceptron_size.width", 0) > 0) && (PREFS.getInt("perceptron_size.width", 0) <= display_mode.getWidth()) &&
                        (PREFS.getInt("perceptron_size.height", 0) > 0) && (PREFS.getInt("perceptron_size.height", 0) <= display_mode.getHeight())) {
                    // load and set window preferences
                    setLocation(PREFS.getInt("perceptron_location.x", ((display_width - getWidth()) / 2)), PREFS.getInt("perceptron_location.y", ((display_height - getHeight()) / 2)));
                    setSize(PREFS.getInt("perceptron_size.width", getWidth()), PREFS.getInt("perceptron_size.height", getHeight()));
                }
            } else {
                // safety check
                if ((config.getInt("perceptron_location.x", 0) >= 0) && (config.getInt("perceptron_location.x", 0) < display_mode.getWidth()) &&
                        (config.getInt("perceptron_location.y", 0) >= 0) && (config.getInt("perceptron_location.y", 0) < display_mode.getHeight()) &&
                        (config.getInt("perceptron_size.width", 0) > 0) && (config.getInt("perceptron_size.width", 0) <= display_mode.getWidth()) &&
                        (config.getInt("perceptron_size.height", 0) > 0) && (config.getInt("perceptron_size.height", 0) <= display_mode.getHeight())) {
                    // load and set window preferences
                    setLocation(config.getInt("perceptron_location.x", ((display_width - getWidth()) / 2)), config.getInt("perceptron_location.y", ((display_height - getHeight()) / 2)));
                    setSize(config.getInt("perceptron_size.width", getWidth()), config.getInt("perceptron_size.height", getHeight()));
                }
            }
        }
    }


    /**
     * Hide the conventional mouse pointer that would overlay the selected encircled cursor, and display only
     * custom cursors.
     */
    void hide_cursor() {
        try {
            Toolkit t = Toolkit.getDefaultToolkit();
            setCursor(t.createCustomCursor(t.getImage("resource/cursors/cat.png"), new Point(0, 0), null));
        } catch (AWTError | IndexOutOfBoundsException | HeadlessException e) {
            System.err.println("Cursor modification is unsupported.");
        }
    }


    /**
     * Setup of various BufferedImages and the DoubleBuffer class according to the screen dimensions.
     */
    void buffer() {
        // Initialize the frame rendering data_buffer
        output = new BufferedImage(screen_width, screen_height, BufferedImage.TYPE_INT_RGB);
        // Initialize the background data_buffer
        background = new BufferedImage(screen_width, screen_height, BufferedImage.TYPE_INT_RGB);
        // Initialize the display data_buffer
        display = new BufferedImage(screen_width, screen_height, BufferedImage.TYPE_INT_RGB);
        // Initiate double buffer class.
        double_buffer = new DoubleBuffer(this, output, background, null, display); // null goes to sketch
        // Fast BufferedImage with data model based on current graphics configuration.
        buffered_image = Main.gc.createCompatibleImage(screen_width, screen_height, Transparency.OPAQUE);
    }


    /**
     * Graphics object for drawing.
     */
    void graphics() {
        // Draw into this Graphics object
        graphics_of_buffered_image = buffered_image.createGraphics();
    }


    /**
     * Convolution is the process of blurring and softening images.
     */
    void convolution() {
        // Convolution or gaussian blurring with the parameter loaded from settings.
        convolution = new Convolution(convolution_degree, double_buffer);  // degree 2 is default, 1 is disabled/faster
    }


    /**
     * Initialize the Fractal, the Tree, the image cache and the rest.
     */
    void objects() {

        images = new ImageCache(this, image_folder);
        fractal = new FractalMap(double_buffer, user_maps, this);
        the_tree = new Tree3D(min_tree_depth, max_tree_depth, new float[][]{{0, 0, 0}, {0, (-(screen_height / 6)), 0}}, 0,
                new TreeForm[]{new TreeForm(.5f, -.2f, .7f, 7), new TreeForm(.5f, .2f, .7f, -7)},
                new Point(half_screen_width, half_screen_height), double_buffer, this);

        life = new CellModel(screen_width, screen_height); // unused but enabled everywhere
        // unused
        //trilife = new SimpleAutomata1();
        //waves = new Hallucination1();
        //waves1 = new Waves_1(this);
        //waves11 = new Waves_11();
        //waves5 = new Waves_5();
        //waves6 = new Waves_6();
        //danceparty = new DanceParty();
        //cortex = new VisualCortex();
        //cortexControls = cortex.makeController(new Point(screen_width / 2, screen_height / 2), min(screen_width, screen_height));
    }


    /**
     * Create and add input event listeners to this JFrame.
     */
    void listeners() {
        controls = new ControlSet(this);
        if (windowed_mode) {
            addMouseListener(controls);
            addMouseMotionListener(controls);
            addKeyListener(controls);
            // Listen for window resize event.
            addComponentListener(this);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // store main window settings
                    if (system_based_preferences) {
                        PREFS.putInt("perceptron_location.x", getLocation().x);
                        PREFS.putInt("perceptron_location.y", getLocation().y);
                        PREFS.putInt("perceptron_size.width", getSize().width);
                        PREFS.putInt("perceptron_size.height", getSize().height);
                    } else {
                        config.setProperty("perceptron_location.x", getLocation().x);
                        config.setProperty("perceptron_location.y", getLocation().y);
                        config.setProperty("perceptron_size.width", getSize().width);
                        config.setProperty("perceptron_size.height", getSize().height);
                        try {
                            config.save();
                        } catch (ConfigurationException exc) {
                            exc.printStackTrace();
                        }
                    }
                    // store settings window settings
                    if (sw != null && sw.sw_frame != null) {
                        if (system_based_preferences) {
                            PREFS.putInt("configomatics_location.x", sw.sw_frame.getLocation().x);
                            PREFS.putInt("configomatics_location.y", sw.sw_frame.getLocation().y);
                            PREFS.putInt("configomatics_size.width", sw.sw_frame.getSize().width);
                            PREFS.putInt("configomatics_size.height", sw.sw_frame.getSize().height);
                        } else {
                            config.setProperty("configomatics_location.x", sw.sw_frame.getLocation().x);
                            config.setProperty("configomatics_location.y", sw.sw_frame.getLocation().y);
                            config.setProperty("configomatics_size.width", sw.sw_frame.getSize().width);
                            config.setProperty("configomatics_size.height", sw.sw_frame.getSize().height);
                            try {
                                config.save();
                            } catch (ConfigurationException exc) {
                                exc.printStackTrace();
                            }
                        }
                    }
                    Main.exit();
                }
            });
        } else {
            addMouseListener(controls);
            addMouseMotionListener(controls);
            addKeyListener(controls);
            // Listen for window resize event.
            addComponentListener(this);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    Main.exit();
                }
            });
        }
    }


    /**
     * Sends a window closing event when ESC is pressed.
     */
    void window_closing_event() {
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }


    /**
     * Prints all discovered fonts on a given system for reference and use in equation editor.
     */
    void print_fonts() {
        invokeLater(() -> {
            JFrame f = new JFrame("installed fonts");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setSize(400, 400);
            f.setLocation(600, 600);
            JTextArea ta = new JTextArea();
            JScrollPane sp = new JScrollPane(ta);
            f.add(sp);
            ta.setAutoscrolls(true);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontFamilies = ge.getAvailableFontFamilyNames();
            for (String fontFamily : fontFamilies) {
                ta.append(fontFamily + "\n");
                //System.out.println(fontFamilies[i]);
            }
            f.setVisible(true);
            f.requestFocus();
            f.setAlwaysOnTop(true);
                /*
                Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
                for ( int i = 0; i < fonts.length; ++i ) {
                    System.out.print( fonts[i].getFontName() + " : " );
                    System.out.print( fonts[i].getFamily() + " : " );
                    System.out.println( fonts[i].getName() );
                }
                */
        });
    }

    /**
     * Restart the window/screen mode.
     */
    public void window_mode() {
        if (windowed_mode) {
            screen_mode();
        } else {
            screen_mode();
        }
    }

    /**
     * Show desktop in order to display open/save preset menu, to save animation or load an image.
     */
    void show_desktop() {
        if (windowed_mode) {
            setVisible(false);
        } else {
            try {
                setVisible(false);
                dispose();
                if (gd.isFullScreenSupported())
                    gd.setFullScreenWindow(null);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to exit active FullScreen/Windowed mode.");
            }
        }
    }

    /**
     * BEGIN EXECUTION of active image rendering.
     */
    public void go() {
        Thread.currentThread().setName("Perceptron : active rendering thread");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        //System.out.println("is go() on edt? " + javax.swing.SwingUtilities.isEventDispatchThread());
        last_image_time = 20000 + System.currentTimeMillis();  // TODO Tests show that nanoTime is faster than milliTime, but milliTime is used many times all around
        boredom_time = 20000 + System.currentTimeMillis();
        // o.recenter(200); // 3DObject fetched at the beggining of this file
        // location of image to be drawn on screen;
        // System.out.println(" x_offset : " + x_offset);
        // System.out.println(" y_offset : " + y_offset);
        frame = 0;
        time = 0;
        start_time = 0;
        last_time = System.nanoTime();
        framerate = 0;
        // life.seedUniformRandom(10);

        // the first graphics object by which we draw to the buffer and then to the screen
        draw_graphics = double_buffer.output.graphics2D;

        running = true;

        // infinite execution loop
        while (infinity) {
            if (running //&& !save_animation_menu_running
                    ) {
                try {
                    start_time = System.currentTimeMillis();
                    /*
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
                    */

                    // frame count and time record
                    if (frame_rate_display) frame++;

                    // Keep the initial set (such as images of cursors) on the screen and keep seeding it
                    // into the flow of visual data or treat the initial set as a short-lasting initial seed.
                    // This executes first in order to print the framerate well.
                    if (persistent_initial_set) {
                        convolution.operate(fractal.filterweight);
                        fractal.operate();
                    } else {
                        fractal.operate();
                        convolution.operate(fractal.filterweight);
                    }

                    // display framerate
                    if (frame_rate_display) {
                        time = System.nanoTime();
                        if (time - last_time >= 1000000000) {
                            last_time = time;
                            framerate = frame;
                            frame = 0;
                        }
                        draw_graphics.setColor(Color.white);
                        draw_graphics.drawString("" + framerate, 5, 14);
                    }

                    // update cursors and assign parameters correlated with cursor' positions
                    //controls.advance((int) framerate);  // large numbers slow cursors
                    controls.advance(10);
                    controls.drawAll(draw_graphics);

                    // Render help screen to the screen if show_help is true.
                    render_help(draw_graphics);

                    // render psychedelic equation editor if active
                    render_salvia_mode(double_buffer.output.graphics2D);

                    // render tree if active
                    the_tree.render();

                    // live screen grabber  // TODO framerate for grabbers
                    if ((live_grabber_count++) > 1) {   // grab every third screenshot
                        if (live_grab && grabber_rectangle != null) {
                            live_grabber_count = 0;
                            grab_screen(grabber_rectangle);
                        }
                    }

                    // live webcam grabber
                    if (cam_works)
                        if ((webcam_grabber_count++) > 1) {
                            if (webcam_grab && webcam.isOpen()) {
                                webcam_grabber_count = 0;
                                webcam_capture_formatted.getGraphics().drawImage(webcam.getImage(), 0, 0, null);
                                double_buffer.load_image_from_screen_grabber(webcam_capture_formatted, screen_width);
                            }
                        }

                    // write output data_buffer from DoubleBuffer class to screen via graphics_of_buffered_image
                    graphics_of_buffered_image.drawImage(double_buffer.output.image, 0, 0, null);

                    // draw on screen
                    if (windowed_mode) {
                        graphics = bufferStrategy.getDrawGraphics();
                        graphics.drawImage(buffered_image, canvas.x, canvas.y, canvas.width, canvas.height, null);
                        // show it on screen
                        //if (!bufferStrategy.contentsLost())
                        bufferStrategy.show();
                    } else {
                        graphics = bufferStrategy.getDrawGraphics();
                        graphics.drawImage(buffered_image, x_offset, y_offset, null);
                        // show it on screen
                        //if (!bufferStrategy.contentsLost())
                        bufferStrategy.show();
                    }

                    // write animation into a movie file or into frames
                    /*
                    if (write_animation && folder_selected) {
                        try {
                            /*
                            // record movie frame by frame (old)
                            String filename = animation_folder + "/frame" + (anim_frame++);
                            filename = filename + ".tga";
                            File file = new File(filename);
                            tga.writeTGA(drawn, file);
                            *//*
                            if (movie_recorder_type == 0) {
                                add_frame_to_movie(double_buffer.output.image); // immediate movie recording - slow, reliable, requires variable fps
                                // fast movie recording does not require any code in go()
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.err.println("File write error during animation.");
                        }
                    }                    */

                    // frame count and time record again
                    if (frame_rate_display) frame++;

                    // slow down operation :-)
                    if (max_frame_time_length != 0) {
                        start_time += max_frame_time_length;
                        do {
                        } while (System.currentTimeMillis() < start_time);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("SOMETHING BAD HAPPENED in Perceptron.java go()!!!");
                }

            } else { // not running while any of the menus is running
                if (save_as_running) {
                    save_frame();
                }
                if (open_preset_running) {
                    open_preset();
                }
                /*
                if (save_animation_menu_running) {
                    anim_frame = 0;
                    save_animation();
                }
                */
                if (open_image_running) {
                    open_image();
                }
            }
        }

        //draw_graphics.dispose();
        //graphics_of_buffered_image.dispose();
        //graphics.dispose();

    }


    /**
     * Render help screen to the screen if show_help is true.
     *
     * @param draw_graphics
     */
    private void render_help(Graphics draw_graphics) {
        /* // fade in and out for the help screen
        if (show_help) {
            if (help_font_alpha < 1) {
                help_font_alpha = 1;
            }
            help_font_alpha <<= 1; // x = a << b then x = a*2^b;
            if (help_font_alpha > 255) {
                help_font_alpha = 0xFF;
            }
        } else {
            help_font_alpha >>= 1;
            if (help_font_alpha < 0) {
                help_font_alpha = 0;
            }
        }  */

        //if (help_font_alpha > 0) { // old for fade in fade out
        if (show_help) {
            //System.out.println("STATE ALPHA" + help_font_alpha);
            draw_graphics.setColor(new Color(0xFF, 0xFF, 0xFF, help_font_alpha));
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


    /**
     * Show help screen when you press /.
     */
    public void show_help_screen() {
        state = Preset.display_help_screen(this);
    }


    /**
     * Effect of slowing down and dragging the mouse cursor as you are moving the mouse.
     */
    public void stimulate() {
        boredom_time = System.currentTimeMillis() + boredomeMS;
    }


    /**
     * Press ; for salvia mode, D for XOR mode.
     */
    private void render_salvia_mode(Graphics2D draw_graphics) {
        if (salvia_mode) {
            try {
                Font old = draw_graphics.getFont(); // affects framerate display
                draw_graphics.setFont(F); // font for equation editor, set in settings
                float XOFF = (float) (.3 * screen_width / COLUMNS);
                float YOFF = (float) (screen_height / ROWS);
                int ROWS1 = ROWS + 1;
                // whiter letters mode
                if (XOR_MODE) {
                    draw_graphics.setXORMode(new Color(0xffffff));
                }
                int k = 0;
                for (int j = 0; j < ROWS; j++) {
                    for (int i = 0; i < COLUMNS; i++) {
                        String CHAR = "" + BUFFER[k];
                        k = (k + 1) % BUFFER.length;
                        draw_graphics.setColor(new Color(SALVIA_COLORS[(int) (Math.random() * SALVIA_COLORS.length)]));
                        GlyphVector G = draw_graphics.getFont().createGlyphVector(draw_graphics.getFontRenderContext(), CHAR);
                        draw_graphics.drawGlyphVector(G, (int) (i * screen_width / COLUMNS + XOFF), (int) (j * screen_height / ROWS1 + YOFF));
                    }
                }
                draw_graphics.setPaintMode(); // affects framerate display
                draw_graphics.setColor(new Color(SALVIA_COLORS[(int) (Math.random() * SALVIA_COLORS.length)]));
                draw_graphics.drawString("" + BUFFER[BUFFER_INDEX], (int) ((BUFFER_INDEX % COLUMNS) * screen_width / COLUMNS + XOFF),
                        (int) ((BUFFER_INDEX / COLUMNS) * screen_height / ROWS1 + YOFF));
                draw_graphics.setFont(old); // affects framerate display
                if (CURSOR_ON) {
                    draw_graphics.setColor(new Color(0x000000));
                    int x = screen_width * (BUFFER_INDEX % COLUMNS) / COLUMNS;
                    int y = screen_height * (BUFFER_INDEX / COLUMNS) / ROWS;
                    draw_graphics.setXORMode(new Color(0xffffff));
                    draw_graphics.fillRect((int) (x + XOFF), (y), screen_width / COLUMNS, screen_height / ROWS);
                    draw_graphics.setPaintMode(); // affects framerate display
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
            /**
             * Consciousness is evolution on infinitely faster time scales.
             */
            draw_graphics.setPaintMode();  // affects framerate display
        }
    }


    /**
     * Press D for Salvia xor mode. This makes the color-changing letter of the equation editor less obtrusive.
     */
    void toggle_xor() {
        XOR_MODE = !XOR_MODE;
        if (sw != null) {
            if (XOR_MODE) sw.jcb_XOR_salvia_mode.setSelectedIndex(1);
            else sw.jcb_XOR_salvia_mode.setSelectedIndex(0);
        }
    }


    /**
     * Take snapshot of every frame - animate. Press Home. This interrupts the active rendering loop in Perceptron.go().
     * Then the execution moves on to save_animation().
     */
    public void toggle_animation() {
        /*
        if (animation_menu_ran_once) {
            save_animation_menu_running = false;
            animation_menu_ran_once = false;
            write_animation = false;
            if (sw != null) {
                sw.jtb_animate.setSelected(false);
                sw.jtb_animate.setText("record animation!");
            }
            try {
                if (movie_writer_qt != null) {
                    if (movie_recorder_type == 1) if (clock_timer != null) clock_timer.stop();
                    movie_writer_qt.close();
                    System.out.println("closed movie writer");
                }
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
            System.out.println("DEACTIVATED ANIMATION");
        } else {
            save_animation_menu_running = true;
            animation_menu_ran_once = true;
            if (sw != null) {
                sw.jtb_animate.setSelected(true);
                sw.jtb_animate.setText("now animating...");
            }
            System.out.println("ACTIVATED ANIMATION");
        }
        */
            animation_menu_ran_once = !animation_menu_ran_once;

            if (sw != null) {
                sw.jtb_animate.setSelected(animation_menu_ran_once);
            } else {
                init_movie_writer(animation_menu_ran_once);
            }

    }


    @Override
    public void componentResized(ComponentEvent e) {
        window_insets = getInsets();
        resize_listener.drawAreaChanged(window_insets.left, window_insets.top, getWidth() - (window_insets.left + window_insets.right), getHeight() - (window_insets.top + window_insets.bottom));
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
    }


    /**
     * Save state and the screenshot, "save frame". Press Space.
     */
    private void save_frame() {
        show_desktop();
        controls.active_cursors.add(controls.TempCursor);
        controls.current_cursor = controls.TempCursor;
        if (windowed_mode) {
            try {
                invokeAndWait(() -> _save_frame());
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
            setVisible(true);
        } else {
            _save_frame();
            screen_mode();
        }
        controls.active_cursors.remove(controls.TempCursor);
        hide_cursor();
        toFront();
        save_as_running = false;
        running = true;
    }


    /**
     * Save frame routine.
     */
    void _save_frame() {
        try {
            if (current_save_folder == null) {
                current_save_folder = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory().toString();
            }
            final JFileChooser saver = new JFileChooser();
            saver.setCurrentDirectory(new File(current_save_folder));
            saver.setDialogTitle("Save two frames and state as...");
            saver.setFileSelectionMode(JFileChooser.FILES_ONLY);
            saver.setAcceptAllFileFilterUsed(true);
            int approval = saver.showSaveDialog(this);
            File file = saver.getSelectedFile();
            if (approval == JFileChooser.APPROVE_OPTION && file != null) {
                file = new File(file.getAbsolutePath());
                current_save_folder = file.getParent();
                File file_in_png = new File(file.getAbsolutePath());
                File file_out_png = new File(file.getAbsolutePath());
                File file_state = new File(file.getAbsolutePath());
                String file_lowercase = file.getAbsolutePath().toLowerCase();
                if (file_lowercase.endsWith(".in.png")) {
                    file = new File(file.getAbsolutePath().replace(".in.png", ""));
                } else {
                    file_in_png = new File(file.getAbsolutePath() + ".in.png");
                }
                if (file_lowercase.endsWith(".out.png")) {
                    file = new File(file.getAbsolutePath().replace(".out.png", ""));
                } else {
                    file_out_png = new File(file.getAbsolutePath() + ".out.png");
                }
                if (file_lowercase.endsWith(".state")) {
                    file = new File(file.getAbsolutePath().replace(".state", ""));
                } else {
                    file_state = new File(file.getAbsolutePath() + ".state");
                }
                if (file.exists() || file_in_png.exists() || file_out_png.exists() || file_state.exists()) {
                    shortName name = new shortName(saver.getSelectedFile().getName());
                    int answer = JOptionPane.showConfirmDialog(this, "File " + name.getShortName(30) + " already exists!\nOverwrite??", "Yes or No?", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (answer) {
                        case JOptionPane.YES_OPTION:
                            saver.approveSelection();
                            try {
                                System.out.println("saving...");
                                ImageIO.write(double_buffer.output.image, "png", new File(file.getAbsolutePath() + ".out.png"));   // save frame
                                ImageIO.write(double_buffer.buffer.image, "png", new File(file.getAbsolutePath() + ".in.png"));
                                // ImageIO.write(data_buffer.display.image, "png", new File(file.getAbsolutePath() + ".display.png"));   // cursor movements
                                // ImageIO.write(data_buffer.image.image, "png", new File(file.getAbsolutePath() + ".sketch.png"));      // preloaded image
                                Preset.write(this, new File(file.getAbsolutePath() + ".state")); // save with extension *.state
                                System.out.println("...saved.");
                                current_save_folder = file.getParent();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        case JOptionPane.NO_OPTION:
                            System.out.println("save file not accepted");
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            System.out.println("save menu closed");
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            saver.cancelSelection();
                            System.out.println("save menu cancelled");
                            return;
                    }
                } else {
                    try {
                        System.out.println("saving...");
                        ImageIO.write(double_buffer.output.image, "png", new File(file.getAbsolutePath() + ".out.png"));   // save frame
                        ImageIO.write(double_buffer.buffer.image, "png", new File(file.getAbsolutePath() + ".in.png"));
                        // ImageIO.write(data_buffer.display.image, "png", new File(file.getAbsolutePath() + ".display.png"));   // cursor movements
                        // ImageIO.write(data_buffer.image.image, "png", new File(file.getAbsolutePath() + ".sketch.png"));      // preloaded image
                        Preset.write(this, new File(file.getAbsolutePath() + ".state")); // save with extension *.state
                        System.out.println("...saved.");
                        current_save_folder = file.getParent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception E) {
            System.err.println("File write error while saving screenshot and state.");
            JOptionPane.showMessageDialog(null, "Could not write files.", "Error", JOptionPane.ERROR_MESSAGE);
            E.printStackTrace();
        }
    }


    /**
     * Save animation. Press Home.
     */
    private void save_animation() {
        show_desktop();
        controls.active_cursors.add(controls.TempCursor);
        controls.current_cursor = controls.TempCursor;
        if (windowed_mode) {
            try {
                invokeAndWait(() -> _save_animation());
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
            setVisible(true);
        } else {
            _save_animation();
            screen_mode();
        }
        controls.active_cursors.remove(controls.TempCursor);
        hide_cursor();
        toFront();
        save_animation_menu_running = false;
        System.out.println("ANIMATION Menu EXIT");
    }


    /**
     * Save animation into movie file.
     */
    void _save_animation() {
        try { // prepare animation saver menu
            System.out.println("ANIMATION Menu ENTRY");
            final JFileChooser saver = new JFileChooser();
            saver.setCurrentDirectory(new File(animation_folder));
            saver.setDialogTitle("Select movie file name to record...");
            saver.setApproveButtonText("Select file");
            saver.setFileSelectionMode(JFileChooser.FILES_ONLY);
            saver.setAcceptAllFileFilterUsed(false);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");
            String uni_name = "movie " + dateFormat.format(new Date()) + ".mov";
            saver.setSelectedFile(new File(uni_name));

            /*************** animation setup *****************************/
            JFrame jf_animation_settings = new JFrame("animation setup");
            jf_animation_settings.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            jf_animation_settings.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            jf_animation_settings.setSize(882, 85);
            int posx, posy;
            posx = display_width / 2 - 812 / 2;
            posy = display_height / 2 - 300 / 2 - 200;
            if (posy < 0) posy = display_height / 2 - 300 / 2;
            jf_animation_settings.setLocation(posx, posy);
            jf_animation_settings.setLayout(new FlowLayout());

            /////////////////////////////////////////////////////
            JSpinner js_anim_type = new JSpinner();
            ArrayList<String> al_anim_type = new ArrayList<>();
            al_anim_type.add("Slow QuickTime movie");
            al_anim_type.add("Fast QuickTime movie");
            js_anim_type.setModel(new SpinnerListModel(al_anim_type));
            ChangeListener cl_anim_type = e -> {
                String s = (String) ((JSpinner) e.getSource()).getValue();
                if (s.equals("Slow QuickTime movie")) {
                    movie_recorder_type = 0;
                }
                if (s.equals("Fast QuickTime movie")) {
                    movie_recorder_type = 1;
                }
            };
            js_anim_type.addChangeListener(cl_anim_type);
            if (movie_recorder_type == 0) js_anim_type.setValue("Slow QuickTime movie");
            else js_anim_type.setValue("Fast QuickTime movie");
            JLabel jl_anim_type = new JLabel("Animation encoding");
            jl_anim_type.setLabelFor(js_anim_type);
            String tooltip_anim_type = "<html>\n" +
                    "<p>Fast mode takes periodic screen snapshots and slow mode captures every frame.</p>\n" +
                    "<p>QuickTime movie uses compression. Quality ranges from 0.1 to 1.    </p>\n" +
                    "</html>\n";
            js_anim_type.setToolTipText(tooltip_anim_type);
            jl_anim_type.setToolTipText(tooltip_anim_type);

            /////////////////////////////////////////////////////
            JSpinner js_anim_quality = new JSpinner();
            /*
            JFormattedTextField field = ((JSpinner.DefaultEditor)js_anim_quality.getEditor()).getTextField();
            DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
            formatter.setCommitsOnValidEdit(true);  // immediate responsiveness to edits
            */
            float[] quality = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
            ArrayList<Float> al_quality = new ArrayList<>();
            for (int i = 0; i < quality.length; i++) al_quality.add(quality[i]);
            js_anim_quality.setModel(new SpinnerListModel(al_quality));
            js_anim_quality.setValue(movie_quality);
            js_anim_quality.addChangeListener(e -> {
                String s = "movie quality: " + js_anim_quality.getValue();
                System.out.println(s);
                movie_quality = (float) js_anim_quality.getValue();
            });
            JLabel jl_anim_quality = new JLabel("Movie quality");
            jl_anim_quality.setLabelFor(js_anim_quality);
            js_anim_quality.setToolTipText(tooltip_anim_type);
            jl_anim_quality.setToolTipText(tooltip_anim_type);

            /////////////////////////////////////////////////////
            JSpinner js_fps = new JSpinner();
            js_fps.setModel(new SpinnerNumberModel(50, 1, 300, 1));
            ChangeListener cl_fps = e -> movie_fps = (Integer) ((JSpinner) e.getSource()).getValue();
            js_fps.addChangeListener(cl_fps);
            js_fps.setValue(movie_fps);
            JLabel jl_fps = new JLabel("Frames per second");
            jl_fps.setLabelFor(js_fps);
            Dimension dfps = js_fps.getPreferredSize();
            dfps.width = 60;
            js_fps.setPreferredSize(dfps);
            String tooltip_fps = "<html>\n" +
                    "<p>The number of frames per second instructs the movie recorder to make a movie file that allegedly contains</p>\n" +
                    "<p>this number of images per second. Old television uses 25 to 30 FPS, which satisfies the demand for visual</p>\n" +
                    "<p>quality, and LCD/LED monitors work at 60 FPS, which puts the upper limit on FPS that you can display. The</p>\n" +
                    "<p>limiting factor is also the speed at which your computer can write all those frames into a movie file.   </p>\n" +
                    "<p></p>\n" +
                    "</html>\n";
            js_fps.setToolTipText(tooltip_fps);
            jl_fps.setToolTipText(tooltip_fps);

            /////////////////////////////////////////////////////
            JSpinner js_timer = new JSpinner();
            js_timer.setModel(new SpinnerNumberModel(20, 1, 600000, 1));
            ChangeListener cl_timer = e -> timer_schedule = (Integer) ((JSpinner) e.getSource()).getValue();
            js_timer.addChangeListener(cl_timer);
            js_timer.setValue(timer_schedule);
            JLabel jl_timer = new JLabel("Snapshot interval in milliseconds");
            jl_timer.setLabelFor(js_timer);
            Dimension dtimer = js_timer.getPreferredSize();
            dtimer.width = 80;
            js_timer.setPreferredSize(dfps);
            String tooltip_timer = "<html>\n" +
                    "<p>Fast recorder does not store all the frames that Perceptron is producing, because Perceptron speed </p>\n" +
                    "<p>varies in time depending from the type of fractal, size and other calculations that your computer is</p>\n" +
                    "<p>performing. Movie recorder instead, is similar to a camera pointing at the screen and recording the </p>\n" +
                    "<p>portion of your screen as it appears to you in real time. However, the \"real time\" cannot be      </p>\n" +
                    "<p>captured continuously, but every N milliseconds. The value of 20 milliseconds does not capture the  </p>\n" +
                    "<p>output from Perceptron entirely, if Perceptron is producing a video at more than 50 frames per      </p>\n" +
                    "<p>second. If Perceptron is producing 25 images per second, this value can be set to 40. The formula is</p>\n" +
                    "<p>N = 1000 / number of frames per second = 40 milliseconds for 25 fps, or N = 20 for 50 fps.          </p>\n" +
                    "</html>\n";
            js_timer.setToolTipText(tooltip_timer);
            jl_timer.setToolTipText(tooltip_timer);

            /*
            JPanel jPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            jPanel.add(jl_anim_type, c);
            c.gridx = 1;
            jPanel.add(js_anim_type, c);
            c.gridx = 2;
            jPanel.add(jl_anim_quality, c);
            c.gridx = 3;
            jPanel.add(js_anim_quality, c);
            c.gridx = 4;
            jPanel.add(jl_fps, c);
            c.gridx = 5;
            jPanel.add(js_fps, c);
            c.gridx = 6;
            jPanel.add(jl_timer, c);
            c.gridx = 7;
            jPanel.add(js_timer, c);
            jf_animation_settings.add(jPanel);
            */


            jf_animation_settings.add(jl_anim_type);
            jf_animation_settings.add(js_anim_type);
            jf_animation_settings.add(jl_anim_quality);
            jf_animation_settings.add(js_anim_quality);
            jf_animation_settings.add(jl_fps);
            jf_animation_settings.add(js_fps);
            jf_animation_settings.add(jl_timer);
            jf_animation_settings.add(js_timer);


            jf_animation_settings.pack();
            jf_animation_settings.setVisible(true);


            /*************** animation setup end *****************************/

            int approval = saver.showDialog(saver, "Select movie file name");
            File file = saver.getSelectedFile();
            if (approval == JFileChooser.APPROVE_OPTION && file != null) {
                file = new File(file.getAbsolutePath());
                File file_mov = new File(file.getAbsolutePath());
                String file_lowercase = file.getAbsolutePath().toLowerCase();
                if (file_lowercase.endsWith(".mov")) {
                    file = new File(file.getAbsolutePath().replace(".mov", ""));
                }
                if (file.exists() || file_mov.exists()) {
                    shortName name = new shortName(saver.getSelectedFile().getName());
                    int answer = JOptionPane.showConfirmDialog(this, "File " + name.getShortName(30) + " already exists!\nOverwrite??", "Yes or No?", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (answer) {
                        case JOptionPane.YES_OPTION:
                            jf_animation_settings.setVisible(false);
                            saver.approveSelection();
                            animation_folder = file.getParent();
                            movie_file = new File(file.getAbsolutePath() + ".mov");
                            folder_selected = true;
                            write_animation = true;
                            //init_movie_writer();
                            System.out.println("NOW ANIMATING");
                            return;
                        case JOptionPane.NO_OPTION:
                            jf_animation_settings.setVisible(false);
                            animation_folder = file.getParent();
                            folder_selected = false;
                            write_animation = false;
                            toggle_animation();
                            System.out.println("ANIMATION CANCELLED");
                            System.out.println("save folder not accepted");
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            jf_animation_settings.setVisible(false);
                            animation_folder = file.getParent();
                            folder_selected = false;
                            write_animation = false;
                            toggle_animation();
                            System.out.println("ANIMATION CANCELLED");
                            System.out.println("save menu closed");
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            jf_animation_settings.setVisible(false);
                            saver.cancelSelection();
                            animation_folder = file.getParent();
                            folder_selected = false;
                            write_animation = false;
                            toggle_animation();
                            System.out.println("ANIMATION CANCELLED");
                            System.out.println("save menu cancelled");
                            return;
                    }
                } else {
                    jf_animation_settings.setVisible(false);
                    animation_folder = file.getParent();
                    movie_file = new File(file.getAbsolutePath() + ".mov");
                    //init_movie_writer();
                    folder_selected = true;
                    write_animation = true;
                    System.out.println("NOW ANIMATING");
                }
            } else {
                jf_animation_settings.setVisible(false);
                folder_selected = false;
                write_animation = false;
                toggle_animation();
                System.out.println("ANIMATION CANCELLED");
            }
        } catch (Exception E) {
            folder_selected = false;
            write_animation = false;
            System.err.println("Could not select the folder/file for animation recording.");
            System.out.println("DEACTIVATED ANIMATION");
            JOptionPane.showMessageDialog(null, "Could not write files.", "Error", JOptionPane.ERROR_MESSAGE);
            E.printStackTrace();
        }

    }


    /**
     * Save animation into folder as a series of images.
     */
    void _save_animation_into_dir() {
        try {
            System.out.println("ANIMATION Menu ENTRY");
            final JFileChooser saver = new JFileChooser();
            saver.setCurrentDirectory(new File(animation_folder));
            saver.setDialogTitle("Select or create an empty folder in which to record the animation...");
            saver.setApproveButtonText("Select folder");
            saver.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            saver.setAcceptAllFileFilterUsed(false);
            int approval = saver.showDialog(saver, "Select folder");
            File folder = saver.getSelectedFile();
            if (approval == JFileChooser.APPROVE_OPTION && folder != null) {
                folder = new File(folder.getAbsolutePath());
                if (folder.listFiles().length != 0) {
                    Arrays.sort(folder.listFiles());
                    shortName name = new shortName(saver.getSelectedFile().getName());
                    int answer = JOptionPane.showConfirmDialog(saver, "Folder " + name.getShortName(30) + " is not empty!\nOverwrite the previous set of frames??", "Yes or No?", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (answer) {
                        case JOptionPane.YES_OPTION:
                            saver.approveSelection();
                            animation_folder = folder.toString();
                            //init_movie_writer();
                            folder_selected = true;
                            write_animation = true;
                            System.out.println("NOW ANIMATING");
                            return;
                        case JOptionPane.NO_OPTION:
                            folder_selected = false;
                            write_animation = false;
                            toggle_animation();
                            System.out.println("ANIMATION CANCELLED");
                            System.out.println("save folder not accepted");
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            folder_selected = false;
                            write_animation = false;
                            toggle_animation();
                            System.out.println("ANIMATION CANCELLED");
                            System.out.println("save menu closed");
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            saver.cancelSelection();
                            folder_selected = false;
                            write_animation = false;
                            toggle_animation();
                            System.out.println("ANIMATION CANCELLED");
                            System.out.println("save menu cancelled");
                            return;
                    }
                } else {
                    animation_folder = folder.toString();
                    //init_movie_writer();
                    folder_selected = true;
                    write_animation = true;
                    System.out.println("NOW ANIMATING");
                }
            } else {
                folder_selected = false;
                write_animation = false;
                toggle_animation();
                System.out.println("ANIMATION CANCELLED");
            }
        } catch (Exception E) {
            folder_selected = false;
            write_animation = false;
            System.err.println("Could not select the folder/file for animation recording.");
            System.out.println("DEACTIVATED ANIMATION");
            JOptionPane.showMessageDialog(null, "Could not write files.", "Error", JOptionPane.ERROR_MESSAGE);
            E.printStackTrace();
        }

    }


    /**
     * Open state (preset). Press `.
     */
    private void open_preset() {
        show_desktop();
        if (sw != null) {
            sw.jtb_load_preset.setSelected(true);
            sw.jtb_load_preset.setText("now loading preset file...");
        }
        controls.active_cursors.add(controls.TempCursor);
        controls.current_cursor = controls.TempCursor;
        if (windowed_mode) {
            try {
                invokeAndWait(() -> _open_preset());
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
            setVisible(true);
        } else {
            _open_preset();
            screen_mode();
        }
        controls.active_cursors.remove(controls.TempCursor);
        hide_cursor();
        toFront();
        if (sw != null) {
            sw.jtb_load_preset.setSelected(false);
            sw.jtb_load_preset.setText("load preset");
        }
        open_preset_running = false;
        running = true;
    }


    void _open_preset() {
        try {
            final JFileChooser opener = new JFileChooser();
            if (current_open_folder == null) {
                current_open_folder = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory().toString();
            }
            opener.setCurrentDirectory(new File(current_open_folder));
            opener.setDialogTitle("Open preset (.state file)...");
            int approval = opener.showDialog(opener, "Open");
            opener.setFileSelectionMode(JFileChooser.FILES_ONLY);
            opener.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            opener.setFileFilter(new extNameFilter("state", "State files (.state)"));
            opener.setAcceptAllFileFilterUsed(false);
            File file = opener.getSelectedFile();
            if (file != null && file.exists() && file.canRead() && approval == JFileChooser.APPROVE_OPTION) {
                try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                    String name = file.getAbsolutePath();
                    System.out.println("Loading preset " + name + " :");
                    list_of_presets.add(Preset.parse(in));
                    user_presets = list_of_presets.toArray(new Preset[list_of_presets.size()]);
                    int number_of_presets = user_presets.length;
                    System.out.println("number of presets after loading: \"" + name + "\" is: " + number_of_presets);
                    controls.apply_preset(number_of_presets - 1);
                    current_open_folder = file.getParent();
                    controls.preset_number = number_of_presets - 1;
                }
            }
        } catch (IOException E) {
            System.err.println("Could not open the preset.");
            JOptionPane.showMessageDialog(null, "Could not open preset.", "Error", JOptionPane.ERROR_MESSAGE);
            E.printStackTrace();
        }
    }


    /**
     * Open image file to transform. Press \.
     */
    private void open_image() {
        show_desktop();
        controls.active_cursors.add(controls.TempCursor);
        controls.current_cursor = controls.TempCursor;
        if (windowed_mode) {
            try {
                invokeAndWait(() -> _open_image());
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
            setVisible(true);
        } else {
            _open_image();
            screen_mode();
        }
        controls.active_cursors.remove(controls.TempCursor);
        hide_cursor();
        toFront();
        open_image_running = false;
        running = true;
    }


    void _open_image() {
        try {
            final JFileChooser opener = new Image_opener();
            if (conservative_image_opener)
                opener.setFileView(new ImageFileView()); // activates Oracle's custom image icons
            opener.setAccessory(new ImagePreview(opener));
            opener.setAcceptAllFileFilterUsed(true);
            ImageFilter img_filter = new ImageFilter();
            opener.addChoosableFileFilter(img_filter);
            opener.setFileFilter(img_filter);
            if (current_open_image_folder == null) {
                current_open_image_folder = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory().toString();
            }
            opener.setCurrentDirectory(new File(current_open_image_folder));
            opener.setDialogTitle("Open image...");
            int approval = opener.showOpenDialog(opener);
            File file = opener.getSelectedFile();
            boolean is_image = true;
            if (file != null && file.exists() && file.canRead() && approval == JFileChooser.APPROVE_OPTION) {
                String name = file.getAbsolutePath();
                System.out.println("Loading external image: \"" + name + "\"");
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img == null) {
                        is_image = false;
                    }
                } catch (Exception e) {
                    System.err.println("could not understand the image file: \"" + file.toString() + "\"");
                }
                if (is_image) {
                    images.images.add(file);
                    if (sw != null) {
                        sw.jcb_select_image.addItem(file.getName());
                        sw.jcb_select_image.setSelectedIndex(images.images.size() - 1);
                    }
                    set_image_by_number(images.images.size() - 1);
                    System.out.println("number of images after loading: \"" + file.toString() + "\" is: " + images.images.size());
                } else {
                    System.out.println("did not load");
                }
                current_open_image_folder = file.getParent();
            }
        } catch (Exception E) {
            System.err.println("Could not open the image.");
            JOptionPane.showMessageDialog(null, "Could not open image. ", "Error", JOptionPane.ERROR_MESSAGE);
            E.printStackTrace();
        }
    }


    /**
     * Unused.
     */
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


    /**
     * Press Z to see the frame rate.
     */
    public void toggle_frame_rate_display() {
        frame_rate_display = !frame_rate_display;
        if (sw != null) {
            if (frame_rate_display) {
                sw.jcb_show_frame_rate.setSelectedIndex(0);
            } else {
                sw.jcb_show_help.setSelectedIndex(1);
            }
        }
    }


    /**
     * Press M to go to the next image loaded in image cache, or N to go back. Used in the image mode (press L)
     * and in some of the outside coloring procedures.
     */
    public void increment_image(int image_index_increment) {
        try {
            double_buffer.load_image(images.advance_image(image_index_increment), screen_width, this);
            last_image_time = imageRotateMS + System.currentTimeMillis();
        } catch (Exception E) {
            E.printStackTrace();
        }
    }


    public void increment_image_fast(int image_index_increment) {
        try {
            double_buffer.load_image_from_screen_grabber(images.advance_image(image_index_increment), screen_width);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }


    /**
     * Set image by its number in ArrayList of images in ImageCache.
     */
    public void set_image_by_number(int image_index) {
        try {
            double_buffer.load_image(images.load_image_to_cache(image_index), screen_width, this);
            last_image_time = 20000 + System.currentTimeMillis();
        } catch (Exception E) {
            E.printStackTrace();
        }
    }


    /**
     * set_image() is used only to read the original_image_file denoted in a preset file.
     */
    public void set_image(String f) {
        try {
            if (f == null) {
                System.out.println("The image file name denoted in preset is null.");
                return;
            }
            // platform compatibility check for pathnames
            if (File.separatorChar == '\\') {
                f = f.replace('/', File.separatorChar);
                f = f.replace('\\', File.separatorChar);
            }
            if (File.separatorChar == '/') {
                f = f.replace('\\', File.separatorChar);
                f = f.replace('/', File.separatorChar);
            }
            File file = new File(f);
            if (!file.exists()) {
                System.out.println("The image file denoted in current preset does not exist: \"" + file.toString() + "\"");
                Main.exit();
                return;
            }
            if (!file.canRead()) {
                System.out.println("The image file denoted in preset cannot be read: \"" + file.toString() + "\"");
                Main.exit();
                return;
            }
            if (ImageIO.read(file) == null) {
                System.out.println("could not understand the image file denoted in preset: \"" + file.toString() + "\"");
                return;
            }
            if (!images.images.contains(file)) {
                images.images.add(file);
                if (sw != null) {
                    sw.jcb_select_image.addItem(file.getName());
                }
                set_image_by_number(images.images.size() - 1);
                System.out.println("Image denoted in preset loaded: " + file.toString());
            }
            if (images.images.contains(file)) {
                set_image_by_number(images.images.indexOf(file));
            }
        } catch (Exception E) {
            E.printStackTrace();
        }
    }


    /**
     * Press U to slow down.
     */
    public void slow_down() {
        max_frame_time_length -= 100;
        max_frame_time_length = wrap(max_frame_time_length, 500);
        if (sw != null) {
            switch (max_frame_time_length) {
                case 0:
                    sw.jcb_slow_mode.setSelectedIndex(0);
                    break;
                case 400:
                    sw.jcb_slow_mode.setSelectedIndex(1);
                    break;
                case 300:
                    sw.jcb_slow_mode.setSelectedIndex(2);
                    break;
                case 200:
                    sw.jcb_slow_mode.setSelectedIndex(3);
                    break;
                case 100:
                    sw.jcb_slow_mode.setSelectedIndex(4);
                    break;
            }
        }
    }


    /**
     * Wrapper.
     */
    int wrap(int n, int m) {
        return n < 0 ? m - (-n % m) : n % m;
    }


    /**
     * Press / for Help.
     */
    void help_screen() {
        show_help = !show_help;
        if (sw != null) {
            if (show_help) sw.jcb_show_help.setSelectedIndex(0);
            else sw.jcb_show_help.setSelectedIndex(1);
        }
    }


    /**
     * Press B to turn on or off cursors and letters (in salvia mode) as the
     * persistent initial set.
     */
    void toggle_persistent_initial_set() {
        persistent_initial_set = !persistent_initial_set;
        if (sw != null) {
            if (persistent_initial_set) sw.jcb_initial_set.setSelectedIndex(0);
            else sw.jcb_initial_set.setSelectedIndex(1);
        }
    }


    /**
     * Press ; for Salvia mode to make visible colorful letters of the equation editor on the screen.
     */
    void toggle_salvia_mode() {
        salvia_mode = !salvia_mode;
        if (sw != null) {
            if (salvia_mode) sw.jcb_salvia_mode.setSelectedIndex(1);
            else sw.jcb_salvia_mode.setSelectedIndex(0);
        }
    }


    /**
     * Press CTRL and start typing the equation...
     */
    void append_to_buffer(char c) {
        BUFFER[BUFFER_INDEX] = c;
        BUFFER_INDEX = (BUFFER_INDEX + 1) % BUFFER.length;
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


    /**
     * Press CTRL, type equation, press enter.
     */
    void BUFFER_TO_MAP() {
        fractal.setMapping(new String(BUFFER));
    }


    /**
     * Unused.
     */
    void rotate_automata() {
        try {
            switch (automata = (automata + 1) % 9) {
                case 0:
                    break;
                case 1:
                    double_buffer.load_image(trilife.draw, this.screen_width, this);
                    break;
                case 2:
                    double_buffer.load_image(waves.draw, this.screen_width, this);
                    break;
                case 3:
                    double_buffer.load_image(cortex.activationmap, this.screen_width, this);
                    break;
                case 4:
                    double_buffer.load_image(waves1.draw, this.screen_width, this);
                    break;
                case 5:
                    double_buffer.load_image(waves11.draw, this.screen_width, this);
                    break;
                case 6:
                    double_buffer.load_image(waves5.draw, this.screen_width, this);
                    break;
                case 7:
                    double_buffer.load_image(waves6.draw, this.screen_width, this);
                    break;
                case 8:
                    double_buffer.load_image(danceparty.draw, this.screen_width, this);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Unused.
     */
    void renderrays(Graphics g) {
        g.setColor(Color.GREEN);
        float offset = raytheta * .001f;
        float scale = (float) (2 * Math.PI / 25);
        float xo = this.screen_width / 2f;
        float yo = this.screen_height / 2f;
        float r = min(this.screen_width, this.screen_height) * .5f;
        for (int i = 0; i < 25; i++) {
            double theta = i * scale + offset;
            int x = (int) (xo + r * Math.cos(theta));
            int y = (int) (yo + r * Math.sin(theta));
            g.drawLine((int) xo, (int) yo, x, y);
        }
        raytheta++;
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


    /**
     * Grabs an image from the area selected using the screen grabber frame.
     */
    public void grab_screen(Rectangle r) {
        // screen grabber implementation
        screen_grabber_frame.dispose(); // dispose runs here to improve linux issue
        screenShot = screen_grabber_robot.createScreenCapture(r);
        double_buffer.load_image_from_screen_grabber(screenShot, screen_width);
    }


    /**
     * Turn on or off the special transparent frame that marks area of the screen for capture (screen grabbing).
     */
    public void toggle_grabber_window() {
        if (screen_grabber_frame == null) {
            screen_grabber_frame = new ScreenGrabber(this);
        }
        if (screen_grabber_frame.isVisible()) {
            if (system_based_preferences) {
                PREFS.putInt("grabber_location.x", screen_grabber_frame.getLocationOnScreen().x);
                PREFS.putInt("grabber_location.y", screen_grabber_frame.getLocationOnScreen().y);
                PREFS.putInt("grabber_size.width", screen_grabber_frame.getSize().width);
                PREFS.putInt("grabber_size.height", screen_grabber_frame.getSize().height);
            } else {
                config.setProperty("grabber_location.x", screen_grabber_frame.getLocationOnScreen().x);
                config.setProperty("grabber_location.y", screen_grabber_frame.getLocationOnScreen().y);
                config.setProperty("grabber_size.width", screen_grabber_frame.getSize().width);
                config.setProperty("grabber_size.height", screen_grabber_frame.getSize().height);
                try {
                    config.save();
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                }
            }
            screen_grabber_frame.setVisible(false);
            this.requestFocus(true);
        } else {
            grabber_windows_prefs();
            screen_grabber_frame.setVisible(true);
            this.requestFocus(true);
        }
    }


    /**
     * Keep window preferences.
     */
    public void grabber_windows_prefs() {
        if (system_based_preferences) {
            // safety check
            if ((PREFS.getInt("grabber_location.x", 0) >= 0) && (PREFS.getInt("grabber_location.x", 0) < display_mode.getWidth()) &&
                    (PREFS.getInt("grabber_location.y", 0) >= 0) && (PREFS.getInt("grabber_location.y", 0) < display_mode.getHeight()) &&
                    (PREFS.getInt("grabber_size.width", 0) > 0) && (PREFS.getInt("grabber_size.width", 0) <= display_mode.getWidth()) &&
                    (PREFS.getInt("grabber_size.height", 0) > 0) && (PREFS.getInt("grabber_size.height", 0) <= display_mode.getHeight())) {
                // load and set window preferences
                screen_grabber_frame.setLocation(PREFS.getInt("grabber_location.x", 0), PREFS.getInt("grabber_location.y", 0));
                screen_grabber_frame.setSize(PREFS.getInt("grabber_size.width", screen_width), PREFS.getInt("grabber_size.height", screen_height));
            } else { // use safe preferences
                screen_grabber_frame.setSize(screen_width, screen_height);
            }
        } else {
            // safety check
            if ((config.getInt("grabber_location.x", 0) >= 0) && (config.getInt("grabber_location.x", 0) < display_mode.getWidth()) &&
                    (config.getInt("grabber_location.y", 0) >= 0) && (config.getInt("grabber_location.y", 0) < display_mode.getHeight()) &&
                    (config.getInt("grabber_size.width", 0) > 0) && (config.getInt("grabber_size.width", 0) <= display_mode.getWidth()) &&
                    (config.getInt("grabber_size.height", 0) > 0) && (config.getInt("grabber_size.height", 0) <= display_mode.getHeight())) {
                // load and set window preferences
                screen_grabber_frame.setLocation(config.getInt("grabber_location.x", 0), config.getInt("grabber_location.y", 0));
                screen_grabber_frame.setSize(config.getInt("grabber_size.width", screen_width), config.getInt("grabber_size.height", screen_height));
            } else { // use safe preferences
                screen_grabber_frame.setSize(screen_width, screen_height);
            }
        }
    }


    /**
     * Initialize the Monte Media movie writer...
     */
    public void init_movie_writer(boolean onoff) {
        try {
            /*
            if (movie_type.equals("QuickTime movie")) {
                movie_format = new Format(
                        MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_QUICKTIME_JPEG,
                        CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_JPEG,
                        WidthKey, screen_width,
                        HeightKey, screen_height,
                        DepthKey, 24,
                        FrameRateKey, new Rational(movie_fps, 1),
                        FixedFrameRateKey, false,
                        QualityKey, movie_quality,
                        KeyFrameIntervalKey, (movie_fps * 60)
                );

                movie_writer_qt = new QuickTimeWriter(movie_file);
                movie_writer_qt.setVideoColorTable(movie_track, double_buffer.output.image.getColorModel());
                movie_track = movie_writer_qt.addTrack(movie_format);
            }
            */

            if (onoff) {
                if (screenRecorderMain == null) {
                    screenRecorderMain = new ScreenRecorderMain(Main.perceptron);
                    screenRecorderMain.setVisible(true);
                } else {
                    if (!screenRecorderMain.isVisible()) {
                        screenRecorderMain.setVisible(true);
                    }
                }
            } else {
                if (screenRecorderMain != null) {
                    EventQueue.invokeLater(() -> {
                        screenRecorderMain.dispatchEvent(new WindowEvent(screenRecorderMain, WindowEvent.WINDOW_CLOSING));
                        if (screenRecorderMain.isVisible()) {
                            screenRecorderMain.stop();
                            screenRecorderMain.setVisible(false);
                            screenRecorderMain.dispose();
                        } else {
                            screenRecorderMain.stop();
                            screenRecorderMain.dispose();
                        }
                    });
                }
            }

            // fast movie recorder runs every n milliseconds and take a snapshot and record movie
            //if (movie_recorder_type == 1) Main.executor.execute(movie_recorder_cameraman);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not initialize Monte recorder.");
        }
    }



    // on every tick of the timer, do the tick
    ActionListener tick;
    // use timer to add a frame to the movie every 40 milliseconds = 1000/40 = 25 fps or 20 ms -> 50 fps
    Timer clock_timer;
    // use a thread to take snapshots of drawing canvas and put them into movie
    Runnable movie_recorder_cameraman = () -> {
        Thread.currentThread().setName("Perceptron : cameraman thread");
        tick = e -> {
            if (running && write_animation) {
                add_frame_to_movie(buffered_image);
            }
        };
        clock_timer = new Timer(timer_schedule, tick);
        clock_timer.setRepeats(true);
        clock_timer.setCoalesce(true);
        clock_timer.start();
    };


    /**
     * Adds frame to the currently open Monte Media movie writer.
     */
    void add_frame_to_movie(BufferedImage captured_screen_frame) {
        if (write_animation)
            try {
                {
                    Buffer buf = new Buffer();
                    buf.format = new Format(DataClassKey, BufferedImage.class);
                    buf.sampleDuration = movie_format.get(FrameRateKey).inverse();
                    buf.data = captured_screen_frame;

                    Buffer wbuf = new Buffer();
                    Codec encoder = Registry.getInstance().getEncoder(movie_writer_qt.getFormat(movie_track));
                    ArrayBlockingQueue<Buffer> writerQueue = new ArrayBlockingQueue<>(movie_format.get(FrameRateKey).intValue() + 1);
                    encoder.process(buf, wbuf);
                    try {
                        writerQueue.put(wbuf);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Runnable free = () -> {
                        try {
                            while (!writerQueue.isEmpty()) {
                                try {
                                    Buffer buf1 = writerQueue.take();
                                    movie_writer_qt.write(movie_track, buf1);
                                } catch (InterruptedException ex) {
                                    // We have been interrupted, terminate
                                    break;
                                }
                            }
                        } catch (Throwable e) {
                            System.err.println("Could not run the movie writer process.");
                            e.printStackTrace();
                        }
                    };

                    Main.executor.execute(free);

                /*
                if (write_animation == false) {
                    try {
                        if (movie_writer_qt != null) {
                            if (movie_recorder_type == 1) clock_timer.stop();
                            movie_writer_qt.finish();
                            movie_writer_qt.close();
                            System.out.println("closed movie writer");
                        }
                    } catch (IOException | SecurityException e) {
                        e.printStackTrace();
                    }
                }
                */

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    /**
     * Screen grabber setup.
     */
    void screen_grabber() {
        try {
            screen_grabber_robot = new Robot();
            dim = new Dimension(screen_width, screen_height);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    /**
     * Webcam setup.
     */
    void webcam() {
        try {
            Webcam.setDriver(new WebcamCompositeDriver(
                    new WebcamDefaultDriver(),
                    new IpCamDriver(new IpCamStorage("resource/cameras.xml")) // read IP webcams from file
            ));
            /*
            // PULL = static JPEG (client pull image from the server)
            // PUSH = MJPEG (Motion JPEG, server push image to client)
            try {
                String name = "Test #255";
                String url = "http://ce3014.myfoscam.org:20054/videostream.cgi";
                IpCamMode mode = IpCamMode.PUSH;
                IpCamAuth auth = new IpCamAuth("user", "user");
                IpCamDeviceRegistry.register(name, url, mode, auth);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            */
            webcams = Webcam.getWebcams(60, TimeUnit.SECONDS);
            if (webcams != null) {
                webcam = webcams.get(0);
                if (webcam != null) {
                    for (int i = 0; i < webcams.size(); i++) {
                        System.out.println("Webcam " + (i + 1) + " : " + webcams.get(i).getName());
                    }
                    // size of webcam raw image and the buffered image that will be filled with that data
                    Dimension[] webcamViewSizes = webcam.getViewSizes();
                    for (int i = 0; i < webcam.getViewSizes().length; i++ )
                    {
                        System.out.println("webcam 0 resolutions: " + webcamViewSizes[i]);
                    }
                    System.out.println("setting the highest webcam 0 resolution: " + webcamViewSizes[webcam.getViewSizes().length - 1]);
                    webcam.setViewSize(webcamViewSizes[webcam.getViewSizes().length - 1]);
                    webcam_capture_formatted = new BufferedImage(webcamViewSizes[webcam.getViewSizes().length - 1].width, webcamViewSizes[webcam.getViewSizes().length - 1].height, BufferedImage.TYPE_INT_RGB);
                    Webcam.getDiscoveryServiceRef().stop(); // not used with composite driver
                    cam_works = true;
                } else {
                    System.out.println("No webcam detected");
                    cam_works = false;
                }
            } else {
                System.out.println("No webcam detected");
                cam_works = false;
            }
        } catch (WebcamException | TimeoutException e) {
            cam_works = false;
            System.err.println("Webcam was not initialized.");
            e.printStackTrace();
        }
    }


    /**
     * General settings window.
     */
    void settings_window() {
        try {
            sw = new Settings_Window();
            sw.define(sw);
        } catch (Exception e) {
            System.err.println("Could not initialize settings window.");
            e.printStackTrace();
        }
    }


    /**
     * General settings window. Press page down to toggle. Does not work in equation editing mode.
     */
    void toggle_settings_window() {
        if (sw != null) {
            if (!sw.sw_frame.isVisible()) {
                sw.sw_frame.setVisible(true);
                sw.sw_frame.toFront();
                //sw.refresh_settings_window();
            } else {
                if (system_based_preferences) {
                    PREFS.putInt("configomatics_location.x", sw.sw_frame.getLocation().x);
                    PREFS.putInt("configomatics_location.y", sw.sw_frame.getLocation().y);
                    PREFS.putInt("configomatics_size.width", sw.sw_frame.getSize().width);
                    PREFS.putInt("configomatics_size.height", sw.sw_frame.getSize().height);
                } else {
                    config.setProperty("configomatics_location.x", sw.sw_frame.getLocation().x);
                    config.setProperty("configomatics_location.y", sw.sw_frame.getLocation().y);
                    config.setProperty("configomatics_size.width", sw.sw_frame.getSize().width);
                    config.setProperty("configomatics_size.height", sw.sw_frame.getSize().height);
                    try {
                        config.save();
                    } catch (ConfigurationException exc) {
                        exc.printStackTrace();
                    }
                }
                sw.sw_frame.setVisible(false);
            }
        }
    }


    /**
     * Listen for Perceptron resizing and update the drawArea rectangle.
     */
    interface ResizeListener {
        void drawAreaChanged(int x, int y, int width, int height);
    }


    /**
     * Press F1 for help screen, options and about screen.
     */
    void f1_for_help() {
        if (wordProcessor == null) wordProcessor = new WordProcessor();
        if (!wordProcessor.isVisible()) wordProcessor.setVisible(true);
        else wordProcessor.setVisible(false);
    }


    /**
     * Read and write TGA image files. Used for recording animation as a series of frames.tga.
     *
     * @author Riven
     *         http://riven8192.blogspot.com/2010/02/image-readwrite-tga.html
     *         <p>
     *         Used with permission.
     */
    static class tga {

        static int[] accessRasterIntArray(BufferedImage src) {
            return ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        }

        static byte[] accessRasterByteArray(BufferedImage src) {
            return ((DataBufferByte) src.getRaster().getDataBuffer()).getData();
        }

        static BufferedImage readTGA(File file) throws IOException {
            if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
            byte[] header = new byte[18];
            int len = (int) file.length() - header.length;
            if (len < 0)
                throw new IllegalStateException("file not big enough to contain header: " + file.getAbsolutePath());
            byte[] data = new byte[len];
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.read(header);
                raf.read(data);
            }
            if ((header[0] | header[1]) != 0) throw new IllegalStateException(file.getAbsolutePath());
            if (header[2] != 2) throw new IllegalStateException(file.getAbsolutePath());
            int w = 0, h = 0;
            w |= (header[12] & 0xFF);
            w |= (header[13] & 0xFF) << 8;
            h |= (header[14] & 0xFF);
            h |= (header[15] & 0xFF) << 8;
            boolean alpha;
            if ((w * h) * 3 == data.length) alpha = false;
            else if ((w * h) * 4 == data.length) alpha = true;
            else throw new IllegalStateException(file.getAbsolutePath());
            if (!alpha && (header[16] != 24)) throw new IllegalStateException(file.getAbsolutePath());
            if (alpha && (header[16] != 32)) throw new IllegalStateException(file.getAbsolutePath());
            if ((header[17] & 15) != (alpha ? 8 : 0)) throw new IllegalStateException(file.getAbsolutePath());
            BufferedImage dst = new BufferedImage(w, h, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            int[] pixels = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
            if (pixels.length != w * h) throw new IllegalStateException(file.getAbsolutePath());
            if (data.length != pixels.length * (alpha ? 4 : 3)) throw new IllegalStateException(file.getAbsolutePath());
            if (alpha) {
                for (int i = 0, p = (pixels.length - 1) * 4; i < pixels.length; i++, p -= 4) {
                    pixels[i] |= ((data[p]) & 0xFF);
                    pixels[i] |= ((data[p + 1]) & 0xFF) << 8;
                    pixels[i] |= ((data[p + 2]) & 0xFF) << 16;
                    pixels[i] |= ((data[p + 3]) & 0xFF) << 24;
                }
            } else {
                for (int i = 0, p = (pixels.length - 1) * 3; i < pixels.length; i++, p -= 3) {
                    pixels[i] |= ((data[p]) & 0xFF);
                    pixels[i] |= ((data[p + 1]) & 0xFF) << 8;
                    pixels[i] |= ((data[p + 2]) & 0xFF) << 16;
                }
            }
            if ((header[17] >> 4) == 1) ;
            else if ((header[17] >> 4) == 0) {
                for (int y = 0; y < h; y++) {
                    int w2 = w / 2;
                    for (int x = 0; x < w2; x++) {
                        int a = (y * w) + x;
                        int b = (y * w) + (w - 1 - x);
                        int t = pixels[a];
                        pixels[a] = pixels[b];
                        pixels[b] = t;
                    }
                }
            } else throw new UnsupportedOperationException(file.getAbsolutePath());
            return dst;
        }

        static void writeTGA(BufferedImage src, File file) throws IOException {
            DataBuffer buffer = src.getRaster().getDataBuffer();
            boolean alpha = src.getColorModel().hasAlpha();
            byte[] data;
            if (buffer instanceof DataBufferByte) {
                byte[] pixels = ((DataBufferByte) src.getRaster().getDataBuffer()).getData();
                if (pixels.length != src.getWidth() * src.getHeight() * (alpha ? 4 : 3))
                    throw new IllegalStateException();
                data = new byte[pixels.length];
                for (int i = 0, p = pixels.length - 1; i < data.length; i++, p--) data[i] = pixels[p];
            } else if (buffer instanceof DataBufferInt) {
                int[] pixels = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
                if (pixels.length != src.getWidth() * src.getHeight()) throw new IllegalStateException();
                data = new byte[pixels.length * (alpha ? 4 : 3)];
                if (alpha) {
                    for (int i = 0, p = pixels.length - 1; i < data.length; i += 4, p--) {
                        data[i] = (byte) ((pixels[p]) & 0xFF);
                        data[i + 1] = (byte) ((pixels[p] >> 8) & 0xFF);
                        data[i + 2] = (byte) ((pixels[p] >> 16) & 0xFF);
                        data[i + 3] = (byte) ((pixels[p] >> 24) & 0xFF);
                    }
                } else {
                    for (int i = 0, p = pixels.length - 1; i < data.length; i += 3, p--) {
                        data[i] = (byte) ((pixels[p]) & 0xFF);
                        data[i + 1] = (byte) ((pixels[p] >> 8) & 0xFF);
                        data[i + 2] = (byte) ((pixels[p] >> 16) & 0xFF);
                    }
                }
            } else throw new UnsupportedOperationException();
            byte[] header = new byte[18];
            header[2] = 2; // uncompressed, true-color image
            header[12] = (byte) ((src.getWidth()) & 0xFF);
            header[13] = (byte) ((src.getWidth() >> 8) & 0xFF);
            header[14] = (byte) ((src.getHeight()) & 0xFF);
            header[15] = (byte) ((src.getHeight() >> 8) & 0xFF);
            header[16] = (byte) (alpha ? 32 : 24); // bits per pixel
            header[17] = (byte) ((alpha ? 8 : 0) | (1 << 4));
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.write(header);
                raf.write(data);
                raf.setLength(raf.getFilePointer());
            }
        }
    /*
     * Read and write TGA files support ends here.
     *
     ******************************************************************/
    }

    /**
     * extNameFilter is used with FileChoosers to choose only .ext files
     */
    class extNameFilter extends javax.swing.filechooser.FileFilter {

        String extension, description;

        public extNameFilter(String ext, String des) {
            extension = "." + ext;
            description = des;
        }

        @Override
        public boolean accept(File f) {
            String lcname = f.getName().toLowerCase();
            return (lcname.endsWith(extension) || f.isDirectory());
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    /**
     * String wrapper that returns "..." plus the last N-3 * characters of the
     * string, or the whole string if * less than N characters. *
     */
    class shortName {

        String name;

        shortName(String s) {
            setName(s);
        }

        public void setName(String s) {
            name = s;
        }

        public String getFullName() {
            return name;
        }

        public String getShortName(int L) {
            if (name.length() > L) {
                return "..." + name.substring(name.length() - L + 3);
            }
            return name;
        }
    }

    public class Settings_Window extends JPanel implements PropertyChangeListener, ActionListener {

        public JLabel
                jl_show_help,
                jl_fractal_map,
                jl_type_equation,
                jl_pullback,
                jl_boundary_condition,
                jl_outside_coloring,
                jl_reflection_map,
                jl_gradient_mode,
                jl_gradient_shape,
                jl_gradient_direction,
                jl_fade_color_mode,
                jl_dampen_fade_color,
                jl_color_accent,
                jl_color_filter,
                jl_partial_inversion,
                jl_total_inversion,
                jl_initial_set,
                jl_salvia_mode,
                jl_XOR_salvia_mode,
                jl_image_mode,
                jl_select_image,
                jl_shuffle_images,
                jl_draw_tree,
                jl_convolution,
                jl_hide_show_cursors,
                jl_cursor_trails,
                jl_slow_mode,
                jl_filter_weight,
                jl_motion_blur,
                jl_show_frame_rate,
                jl_load_preset,
                jl_animate,
                jl_autorotate_ortho,
                jl_autorotate_polar,
                jl_autopilot,
                jl_wander,
                jl_live_grabber,
                jl_webcam,
                jl_webcams;
        public JComboBox<String>
                jcb_show_help,
                jcb_fractal_map,
                jcb_pullback,
                jcb_boundary_condition,
                jcb_outside_coloring,
                jcb_reflection_map,
                jcb_gradient_mode,
                jcb_gradient_shape,
                jcb_gradient_direction,
                jcb_fade_color_mode,
                jcb_dampen_fade_color,
                jcb_color_accent,
                jcb_color_filter,
                jcb_partial_inversion,
                jcb_total_inversion,
                jcb_initial_set,
                jcb_salvia_mode,
                jcb_XOR_salvia_mode,
                jcb_image_mode,
                jcb_select_image,
                jcb_shuffle_images,
                jcb_draw_tree,
                jcb_convolution,
                jcb_hide_show_cursors,
                jcb_cursor_trails,
                jcb_slow_mode,
                jcb_filter_weight,
                jcb_motion_blur,
                jcb_show_frame_rate,
                jcb_autorotate_ortho,
                jcb_autorotate_polar,
                jcb_webcams;
        public JToggleButton jtb_type_equation, jtb_load_preset, jtb_animate, jtb_autopilot, jtb_wander, jtb_live_grabber, jtb_webcam;

        JFrame sw_frame = new JFrame();


        public Settings_Window() {

            super(new BorderLayout());

            Thread.currentThread().setName("Perceptron : settings window thread");

            // make all tooltips stay for the same, limited amount of time
            ToolTipManager.sharedInstance().setInitialDelay(500);
            ToolTipManager.sharedInstance().setDismissDelay(20000);

            // labels list all classical Perceptron options
            jl_show_help = new JLabel("show help         ");
            jl_fractal_map = new JLabel("fractal map       ");
            jl_type_equation = new JLabel("type equation     ");
            jl_pullback = new JLabel("pullback          ");
            jl_boundary_condition = new JLabel("boundary condition");
            jl_outside_coloring = new JLabel("outside coloring  ");
            jl_reflection_map = new JLabel("reflection map    ");
            jl_gradient_mode = new JLabel("gradient mode     ");
            jl_gradient_shape = new JLabel("gradient shape    ");
            jl_gradient_direction = new JLabel("gradient direction");
            jl_fade_color_mode = new JLabel("fade color mode   ");
            jl_dampen_fade_color = new JLabel("dampen fade color ");
            jl_color_accent = new JLabel("color accent      ");
            jl_color_filter = new JLabel("color filter      ");
            jl_partial_inversion = new JLabel("partial inversion ");
            jl_total_inversion = new JLabel("total inversion   ");
            jl_initial_set = new JLabel("initial set       ");
            jl_salvia_mode = new JLabel("salvia mode       ");
            jl_XOR_salvia_mode = new JLabel("XOR salvia mode   ");
            jl_image_mode = new JLabel("image mode        ");
            jl_select_image = new JLabel("select image      ");
            jl_shuffle_images = new JLabel("shuffle images    ");
            jl_draw_tree = new JLabel("draw tree         ");
            jl_convolution = new JLabel("convolution       ");
            jl_hide_show_cursors = new JLabel("hide/show cursors ");
            jl_cursor_trails = new JLabel("cursor trails     ");
            jl_slow_mode = new JLabel("slow mode         ");
            jl_filter_weight = new JLabel("filter weight     ");
            jl_motion_blur = new JLabel("motion blur       ");
            jl_show_frame_rate = new JLabel("show frame rate   ");
            jl_load_preset = new JLabel("load preset       ");
            jl_animate = new JLabel("animate!          ");
            jl_autorotate_ortho = new JLabel("autorotate ortho  ");
            jl_autorotate_polar = new JLabel("autorotate polar  ");
            jl_autopilot = new JLabel("autopilot         ");
            jl_wander = new JLabel("wander            ");
            jl_live_grabber = new JLabel("screen capture");
            jl_webcam = new JLabel("webcam capture");
            jl_webcams = new JLabel("webcam selection");


            // show help option
            // VK_SLASH:
            // perceptron.help_screen();
            String[] s_show_help = {"show help", "hide help"};
            jcb_show_help = new JComboBox<>(s_show_help);
            if (show_help) jcb_show_help.setSelectedIndex(0);
            else jcb_show_help.setSelectedIndex(1);
            jl_show_help.setLabelFor(jcb_show_help);
            ActionListener al_show_help = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("show help")) {
                    show_help = true;
                    show_help_screen(); // update the textual on-screen help with new values of settings
                    Main.perceptron.requestFocus();
                } else if (selected.equals("hide help")) {
                    show_help = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_show_help.addActionListener(al_show_help);
            MouseListener ml_show_help = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_show_help.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_show_help.hidePopup();
                }
            };
            jcb_show_help.addMouseListener(ml_show_help);


            // change fractal map option
            /*  case VK_Q:  case VK_W: fractal.increment_map(1); set_map load_equation   */
            jcb_fractal_map = new JComboBox<>();
            for (int i = 0; i < fractal.mappings.size(); i++) {
                jcb_fractal_map.addItem(fractal.mappings.get(i).toString());
            }
            jl_fractal_map.setLabelFor(jcb_fractal_map);
            ActionListener al_fractal_map = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_map(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_fractal_map.addActionListener(al_fractal_map);
            MouseListener ml_fractal_map = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_fractal_map.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_fractal_map.hidePopup();
                }
            };
            jcb_fractal_map.addMouseListener(ml_fractal_map);

            // type equation option
            jtb_type_equation = new JToggleButton();
            if (controls.ENTRY_MODE) {
                jtb_type_equation.setSelected(true);
                jtb_type_equation.setText("now reading in equation...");
            } else {
                jtb_type_equation.setSelected(false);
                jtb_type_equation.setText("Type equation");
            }
            ItemListener il_type_equation = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    controls.ENTRY_MODE = salvia_mode = CURSOR_ON = true;
                    jtb_type_equation.setText("now reading in equation...");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    controls.ENTRY_MODE = CURSOR_ON = false;
                    jtb_type_equation.setText("Type equation");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jtb_type_equation.addItemListener(il_type_equation);
            /*
            ActionListener al_type_equation = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (((JToggleButton) e.getSource()).isSelected()) {
                        controls.ENTRY_MODE = salvia_mode = CURSOR_ON = true;
                        jtb_type_equation.setText("now reading in equation...");
                    }
                    if (!((JToggleButton) e.getSource()).isSelected()) {
                        controls.ENTRY_MODE = CURSOR_ON = false;
                        jtb_type_equation.setText("Type equation");
                    }
                }
            };
            jtb_type_equation.addActionListener(al_type_equation);
            */

            // pullback option
            String[] s_pullback = {"on", "off"};
            jcb_pullback = new JComboBox<>(s_pullback);
            if (fractal.pullback_flag) jcb_pullback.setSelectedIndex(0);
            else jcb_pullback.setSelectedIndex(1);
            jl_pullback.setLabelFor(jcb_pullback);
            ActionListener al_pullback = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("on")) {
                    fractal.pullback_flag = true;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (selected.equals("off")) {
                    fractal.pullback_flag = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_pullback.addActionListener(al_pullback);
            MouseListener ml_pullback = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_pullback.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_pullback.hidePopup();
                }
            };
            jcb_pullback.addMouseListener(ml_pullback);

            // boundary condition option
            String[] s_boundary_condition = {
                    "Rectangular Window",
                    "Limit Circle",
                    "Elastic Limit Circle",
                    "Horizontal Window",
                    "Vertical Window",
                    "Inverse Oval Window",
                    "No Window",
                    "Framed Window",
                    "Convergent bailout"
            };
            jcb_boundary_condition = new JComboBox<>(s_boundary_condition);
            jcb_boundary_condition.setSelectedIndex(fractal.boundary_condition_number);
            jl_boundary_condition.setLabelFor(jcb_boundary_condition);
            ActionListener al_boundary_condition = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_boundary_condition(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_boundary_condition.addActionListener(al_boundary_condition);
            MouseListener ml_boundary_condition = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_boundary_condition.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_boundary_condition.hidePopup();
                }
            };
            jcb_boundary_condition.addMouseListener(ml_boundary_condition);

            // outside coloring option
            String[] s_outside_coloring = {
                    "Fill With Fade-to Color",
                    "Edge Extend",
                    "Just Pass on the Color",
                    "Paint With Image",
                    "Paint With Image II",
                    "Fuzzy"
            };
            jcb_outside_coloring = new JComboBox<>(s_outside_coloring);
            jcb_outside_coloring.setSelectedIndex(fractal.outside_coloring_number);
            jl_outside_coloring.setLabelFor(jcb_outside_coloring);
            ActionListener al_outside_coloring = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_outside_coloring(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_outside_coloring.addActionListener(al_outside_coloring);
            MouseListener ml_outside_coloring = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_outside_coloring.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_outside_coloring.hidePopup();
                }
            };
            jcb_outside_coloring.addMouseListener(ml_outside_coloring);

            // reflection map option
            String[] s_reflection_map = {
                    "x=W\u2013x mod W",
                    "x/W even x=x mod W;odd x=W\u20131\u2013x mod W;no PI",
                    "same, but with Pixel Interpolation",
                    "x=x mod W/2, y=y mod H/2",
                    "x>W x=x mod W else x=W\u20131\u2013x mod W",
                    "x<W x=x mod W/2 else x=W\u20131\u2013x mod W",
                    "x/W even x=x mod W/2;odd x=W\u20131\u2013x mod W/2",
                    "x/W even x=x mod W/2;odd x=(W\u20131)/2\u2013x mod W/2",
                    "simple"
            };
            jcb_reflection_map = new JComboBox<>(s_reflection_map);
            jcb_reflection_map.setSelectedIndex(double_buffer.reflection);
            jl_reflection_map.setLabelFor(jcb_reflection_map);
            ActionListener al_reflection_map = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    double_buffer.set_reflection(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_reflection_map.addActionListener(al_reflection_map);
            MouseListener ml_reflection_map = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_reflection_map.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_reflection_map.hidePopup();
                }
            };
            jcb_reflection_map.addMouseListener(ml_reflection_map);

            // gradient mode option
            String[] s_gradient_mode = {
                    "No Gradient",
                    "Simple Gradient",
                    "Accented Gradient"
            };
            jcb_gradient_mode = new JComboBox<>(s_gradient_mode);
            jcb_gradient_mode.setSelectedIndex(fractal.gradient_number);
            jl_gradient_mode.setLabelFor(jcb_gradient_mode);
            ActionListener al_gradient_mode = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_gradient(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_gradient_mode.addActionListener(al_gradient_mode);
            MouseListener ml_gradient_mode = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_gradient_mode.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_gradient_mode.hidePopup();
                }
            };
            jcb_gradient_mode.addMouseListener(ml_gradient_mode);

            // gradient shape option
            String[] s_gradient_shape = {
                    /* "512*(x*x/(W*W)+y*y/(H*H))",
                    "(x+.5W)*255/W)",
                    "(y+.5H)*255/H)",
                    "(y+.5H)*(x+.5W)*255/(W*H))",
                    "(abs(x)+abs(y))*255/(.5W+.5H))",
                    "(abs(x)*abs(y))*255/(.5W*.5H))",
                    "(1-min(1,sqrt(x*x+y*y)/min(.5H,.5W))))",
                    "(1-pow(min(1,sqrt(x*x+y*y)/min(.5H,.5W)),9)))",
                    "(1-pow(max(0,min(1,1.3-(sqrt(x*x+y*y))/(min(.5H,.5W)))),9))" */
                    "circle",
                    "horizontal",
                    "vertical",
                    "diagonal",
                    "linear bevel",
                    "quadratic bevel",
                    "image projmask1",
                    "image projmask2",
                    "image projmask3"
            };
            jcb_gradient_shape = new JComboBox<>(s_gradient_shape);
            jcb_gradient_shape.setSelectedIndex(fractal.gradient_number);
            jl_gradient_shape.setLabelFor(jcb_gradient_shape);
            ActionListener al_gradient_shape = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_gradient_shape(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_gradient_shape.addActionListener(al_gradient_shape);
            MouseListener ml_gradient_shape = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_gradient_shape.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_gradient_shape.hidePopup();
                }
            };
            jcb_gradient_shape.addMouseListener(ml_gradient_shape);

            // gradient direction option
            String[] s_gradient_direction = {
                    "forward",
                    "backward"
            };
            jcb_gradient_direction = new JComboBox<>(s_gradient_direction);
            if (fractal.gradient_switch == 0) {
                jcb_gradient_direction.setSelectedIndex(0);
            }
            if (fractal.gradient_switch == 255) {
                jcb_gradient_direction.setSelectedIndex(1);
            }
            jl_gradient_direction.setLabelFor(jcb_gradient_direction);
            ActionListener al_gradient_direction = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i == 0) {
                    fractal.gradient_switch = 0;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
                if (i == 1) {
                    fractal.gradient_switch = 255;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_gradient_direction.addActionListener(al_gradient_direction);
            MouseListener ml_gradient_direction = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_gradient_direction.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_gradient_direction.hidePopup();
                }
            };
            jcb_gradient_direction.addMouseListener(ml_gradient_direction);

            // fade color option
            String[] s_fade_color_mode = {
                    "Black",
                    "White",
                    "Mid-screen Pixel Hue",
                    "Not Mid-screen Pixel Hue",
                    "Mid-screen Pixel Hue Rotate",
                    "Hue Rotate"
            };
            jcb_fade_color_mode = new JComboBox<>(s_fade_color_mode);
            jcb_fade_color_mode.setSelectedIndex(fractal.fade_number);
            jl_fade_color_mode.setLabelFor(jcb_fade_color_mode);
            ActionListener al_fade_color_mode = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_fader(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_fade_color_mode.addActionListener(al_fade_color_mode);
            MouseListener ml_fade_color_mode = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_fade_color_mode.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_fade_color_mode.hidePopup();
                }
            };
            jcb_fade_color_mode.addMouseListener(ml_fade_color_mode);

            // dampen fade color option
            String[] s_dampen_fade_color = {
                    "do it",
                    "don't do it"
            };
            jcb_dampen_fade_color = new JComboBox<>(s_dampen_fade_color);
            if (fractal.dampen_colors) {
                jcb_dampen_fade_color.setSelectedIndex(0);
            } else {
                jcb_dampen_fade_color.setSelectedIndex(1);
            }
            jl_dampen_fade_color.setLabelFor(jcb_dampen_fade_color);
            ActionListener al_dampen_fade_color = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("do it")) {
                    fractal.dampen_colors = true;
                    show_help_screen(); // update the textual on-screen help with new values of settings
                    Main.perceptron.requestFocus();
                } else if (selected.equals("don't do it")) {
                    fractal.dampen_colors = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_dampen_fade_color.addActionListener(al_dampen_fade_color);
            MouseListener ml_dampen_fade_color = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_dampen_fade_color.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_dampen_fade_color.hidePopup();
                }
            };
            jcb_dampen_fade_color.addMouseListener(ml_dampen_fade_color);

            // color accent option
            String[] s_color_accent = {
                    "0",
                    "FFFFFF",
                    "FFFF00",
                    "8000FF"
            };
            jcb_color_accent = new JComboBox<>(s_color_accent);
            jcb_color_accent.setSelectedIndex(fractal.accent_color_index);
            jl_color_accent.setLabelFor(jcb_color_accent);
            ActionListener al_color_accent = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_accent(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_color_accent.addActionListener(al_color_accent);
            MouseListener ml_color_accent = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_color_accent.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_color_accent.hidePopup();
                }
            };
            jcb_color_accent.addMouseListener(ml_color_accent);

            // color filter option
            String[] s_color_filter = {
                    "None",
                    "RGB",
                    "Mush"
            };
            jcb_color_filter = new JComboBox<>(s_color_filter);
            jcb_color_filter.setSelectedIndex(fractal.color_filter_number);
            jl_color_filter.setLabelFor(jcb_color_filter);
            ActionListener al_color_filter = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_colorfilter(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_color_filter.addActionListener(al_color_filter);
            MouseListener ml_color_filter = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_color_filter.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_color_filter.hidePopup();
                }
            };
            jcb_color_filter.addMouseListener(ml_color_filter);

            // option partial inversion
            String[] s_partial_inversion = {
                    "disable",
                    "enable"
            };
            jcb_partial_inversion = new JComboBox<>(s_partial_inversion);
            if (fractal.partial_gradient_inversion_flag) {
                jcb_partial_inversion.setSelectedIndex(1);
            } else {
                jcb_partial_inversion.setSelectedIndex(0);
            }
            jl_partial_inversion.setLabelFor(jcb_partial_inversion);
            ActionListener al_partial_inversion = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("disable")) {
                    fractal.partial_gradient_inversion_flag = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (selected.equals("enable")) {
                    fractal.partial_gradient_inversion_flag = true;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_partial_inversion.addActionListener(al_partial_inversion);
            MouseListener ml_partial_inversion = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_partial_inversion.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_partial_inversion.hidePopup();
                }
            };
            jcb_partial_inversion.addMouseListener(ml_partial_inversion);

            // option total inversion
            String[] s_total_inversion = {
                    "disable",
                    "enable"
            };
            jcb_total_inversion = new JComboBox<>(s_total_inversion);
            if (fractal.gradient_inversion == 0xFFFFFF && !fractal.gradient_inversion_flag) {
                jcb_total_inversion.setSelectedIndex(0);
            } else if (fractal.gradient_inversion == 0x0 && fractal.gradient_inversion_flag) {
                jcb_total_inversion.setSelectedIndex(1);
            }
            jl_total_inversion.setLabelFor(jcb_total_inversion);
            ActionListener al_total_inversion = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i == 0) {
                    fractal.gradient_inversion = 0xFFFFFF;
                    fractal.gradient_inversion_flag = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (i == 1) {
                    fractal.gradient_inversion = 0x0;
                    fractal.gradient_inversion_flag = true;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_total_inversion.addActionListener(al_total_inversion);
            MouseListener ml_total_inversion = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_total_inversion.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_total_inversion.hidePopup();
                }
            };
            jcb_total_inversion.addMouseListener(ml_total_inversion);

            // option initial set
            String[] s_initial_set = {
                    "enable",
                    "disable"
            };
            jcb_initial_set = new JComboBox<>(s_initial_set);
            if (persistent_initial_set) {
                jcb_initial_set.setSelectedIndex(0);
            } else {
                jcb_initial_set.setSelectedIndex(1);
            }
            jl_initial_set.setLabelFor(jcb_initial_set);
            ActionListener al_initial_set = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i == 0) {
                    persistent_initial_set = true;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (i == 1) {
                    persistent_initial_set = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_initial_set.addActionListener(al_initial_set);
            MouseListener ml_initial_set = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_initial_set.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_initial_set.hidePopup();
                }
            };
            jcb_initial_set.addMouseListener(ml_initial_set);

            // option salvia mode
            String[] s_salvia_mode = {
                    "disable",
                    "enable"
            };
            jcb_salvia_mode = new JComboBox<>(s_salvia_mode);
            if (salvia_mode) {
                jcb_salvia_mode.setSelectedIndex(1);
            } else {
                jcb_salvia_mode.setSelectedIndex(0);
            }
            jl_salvia_mode.setLabelFor(jcb_salvia_mode);
            ActionListener al_salvia_mode = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i == 0) {
                    salvia_mode = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (i == 1) {
                    salvia_mode = true;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_salvia_mode.addActionListener(al_salvia_mode);
            MouseListener ml_salvia_mode = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_salvia_mode.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_salvia_mode.hidePopup();
                }
            };
            jcb_salvia_mode.addMouseListener(ml_salvia_mode);

            // option XOR salvia mode
            String[] s_XOR_salvia_mode = {
                    "disable",
                    "enable"
            };
            jcb_XOR_salvia_mode = new JComboBox<>(s_XOR_salvia_mode);
            if (XOR_MODE) {
                jcb_XOR_salvia_mode.setSelectedIndex(1);
            } else {
                jcb_XOR_salvia_mode.setSelectedIndex(0);
            }
            jl_XOR_salvia_mode.setLabelFor(jcb_salvia_mode);
            ActionListener al_XOR_salvia_mode = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i == 0) {
                    XOR_MODE = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (i == 1) {
                    XOR_MODE = true;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_XOR_salvia_mode.addActionListener(al_XOR_salvia_mode);
            MouseListener ml_XOR_salvia_mode = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_XOR_salvia_mode.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_XOR_salvia_mode.hidePopup();
                }
            };
            jcb_XOR_salvia_mode.addMouseListener(ml_XOR_salvia_mode);

            // image mode option
            String[] s_image_mode = {
                    "Primary Renderer",
                    "Image mode 1",
                    "Image mode 2"
            };
            jcb_image_mode = new JComboBox<>(s_image_mode);
            jcb_image_mode.setSelectedIndex(fractal.renderer_number);
            jl_image_mode.setLabelFor(jcb_image_mode);
            ActionListener al_image_mode = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_renderer(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_image_mode.addActionListener(al_image_mode);
            MouseListener ml_image_mode = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_image_mode.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_image_mode.hidePopup();
                }
            };
            jcb_image_mode.addMouseListener(ml_image_mode);

            // select image option
            jcb_select_image = new JComboBox<>();
            if (images.images != null) for (int i = 0; i < images.images.size(); i++) {
                jcb_select_image.addItem(images.images.get(i).getName());
            }
            jl_select_image.setLabelFor(jcb_image_mode);
            ActionListener al_select_image = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    set_image_by_number(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_select_image.addActionListener(al_select_image);
            MouseListener ml_select_image = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_select_image.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_select_image.hidePopup();
                }
            };
            jcb_select_image.addMouseListener(ml_select_image);


            // shuffle images option
            String[] s_shuffle_images = {"don't", "do it"};
            jcb_shuffle_images = new JComboBox<>(s_shuffle_images);
            if (rotateImages) {
                jcb_shuffle_images.setSelectedIndex(1);
            } else {
                jcb_shuffle_images.setSelectedIndex(0);
            }
            jl_shuffle_images.setLabelFor(jcb_shuffle_images);
            ActionListener al_shuffle_images = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("do it")) {
                    rotateImages = true;
                    show_help_screen(); // update the textual on-screen help with new values of settings
                    Main.perceptron.requestFocus();
                } else if (selected.equals("don't")) {
                    rotateImages = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_shuffle_images.addActionListener(al_shuffle_images);
            MouseListener ml_shuffle_images = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_shuffle_images.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_shuffle_images();
                }
            };
            jcb_shuffle_images.addMouseListener(ml_shuffle_images);

            // option draw tree
            jcb_draw_tree = new JComboBox<>();
            String[] s_draw_tree = {"don't", "do it"};
            jcb_draw_tree = new JComboBox<>(s_draw_tree);
            if (the_tree.is_active()) {
                jcb_draw_tree.setSelectedIndex(1);
            } else {
                jcb_draw_tree.setSelectedIndex(0);
            }
            jl_draw_tree.setLabelFor(jcb_draw_tree);
            ActionListener al_draw_tree = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("do it")) {
                    controls.setTree(true);
                    show_help_screen(); // update the textual on-screen help with new values of settings
                    Main.perceptron.requestFocus();
                } else if (selected.equals("don't")) {
                    controls.setTree(false);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_draw_tree.addActionListener(al_draw_tree);
            MouseListener ml_draw_tree = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_draw_tree.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_draw_tree();
                }
            };
            jcb_draw_tree.addMouseListener(ml_draw_tree);

            // convolution option
            String[] s_convolution = {
                    "minimal",
                    "average",
                    "high",
                    "alternative"
            };
            jcb_convolution = new JComboBox<>(s_convolution);
            jcb_convolution.setSelectedIndex(double_buffer.convolution);
            jl_convolution.setLabelFor(jcb_convolution);
            ActionListener al_convolution = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.set_convolution(i);
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_convolution.addActionListener(al_convolution);
            MouseListener ml_convolution = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_convolution.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_convolution.hidePopup();
                }
            };
            jcb_convolution.addMouseListener(ml_convolution);

            // show cursors option
            jcb_hide_show_cursors = new JComboBox<>();
            String[] s_hide_show_cursors = {
                    "show",
                    "hide"
            };
            jcb_hide_show_cursors = new JComboBox<>(s_hide_show_cursors);
            if (controls.draw_cursors) {
                jcb_hide_show_cursors.setSelectedIndex(0);
            } else {
                jcb_hide_show_cursors.setSelectedIndex(1);
            }
            jl_hide_show_cursors.setLabelFor(jcb_hide_show_cursors);
            ActionListener al_hide_show_cursors = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("show")) {
                    controls.draw_cursors = true;
                    show_help_screen(); // update the textual on-screen help with new values of settings
                    Main.perceptron.requestFocus();
                } else if (selected.equals("hide")) {
                    controls.draw_cursors = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_hide_show_cursors.addActionListener(al_hide_show_cursors);
            MouseListener ml_hide_show_cursors = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_hide_show_cursors.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_hide_show_cursors();
                }
            };
            jcb_hide_show_cursors.addMouseListener(ml_hide_show_cursors);

            // cursor trails option
            jcb_cursor_trails = new JComboBox<>();
            String[] s_cursor_trails = {
                    "draw",
                    "don't"
            };
            jcb_cursor_trails = new JComboBox<>(s_cursor_trails);
            if (controls.draw_futures) {
                jcb_cursor_trails.setSelectedIndex(0);
            } else {
                jcb_cursor_trails.setSelectedIndex(1);
            }
            jl_cursor_trails.setLabelFor(jcb_cursor_trails);
            ActionListener al_cursor_trails = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("draw")) {
                    controls.draw_futures = true;
                    show_help_screen(); // update the textual on-screen help with new values of settings
                    Main.perceptron.requestFocus();
                } else if (selected.equals("don't")) {
                    controls.draw_futures = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_cursor_trails.addActionListener(al_cursor_trails);
            MouseListener ml_cursor_trails = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_cursor_trails.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_cursor_trails();
                }
            };
            jcb_cursor_trails.addMouseListener(ml_cursor_trails);

            // slow mode option
            String[] s_slow_mode = {
                    "0",
                    "400",
                    "300",
                    "200",
                    "100"
            };
            jcb_slow_mode = new JComboBox<>(s_slow_mode);
            switch (max_frame_time_length) {
                case 0:
                    jcb_slow_mode.setSelectedIndex(0);
                    break;
                case 400:
                    jcb_slow_mode.setSelectedIndex(1);
                    break;
                case 300:
                    jcb_slow_mode.setSelectedIndex(2);
                    break;
                case 200:
                    jcb_slow_mode.setSelectedIndex(3);
                    break;
                case 100:
                    jcb_slow_mode.setSelectedIndex(4);
                    break;
            }
            jl_slow_mode.setLabelFor(jcb_slow_mode);
            ActionListener al_slow_mode = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    switch (i) {
                        case 0:
                            max_frame_time_length = 0;
                            break;
                        case 1:
                            max_frame_time_length = 400;
                            break;
                        case 2:
                            max_frame_time_length = 300;
                            break;
                        case 3:
                            max_frame_time_length = 200;
                            break;
                        case 4:
                            max_frame_time_length = 100;
                            break;
                    }
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_slow_mode.addActionListener(al_slow_mode);
            MouseListener ml_slow_mode = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_slow_mode.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_slow_mode.hidePopup();
                }
            };
            jcb_slow_mode.addMouseListener(ml_slow_mode);

            // filter weight option
            String[] s_filter_weight = {
                    "0",
                    "32",
                    "64",
                    "96",
                    "128",
                    "160",
                    "192",
                    "224",
                    "256"
            };
            jcb_filter_weight = new JComboBox<>(s_filter_weight);
            switch (fractal.filterweight) {
                case 0:
                    jcb_filter_weight.setSelectedIndex(0);
                    break;
                case 32:
                    jcb_filter_weight.setSelectedIndex(1);
                    break;
                case 64:
                    jcb_filter_weight.setSelectedIndex(2);
                    break;
                case 96:
                    jcb_filter_weight.setSelectedIndex(3);
                    break;
                case 128:
                    jcb_filter_weight.setSelectedIndex(4);
                    break;
                case 160:
                    jcb_filter_weight.setSelectedIndex(5);
                    break;
                case 192:
                    jcb_filter_weight.setSelectedIndex(6);
                    break;
                case 224:
                    jcb_filter_weight.setSelectedIndex(7);
                    break;
                case 256:
                    jcb_filter_weight.setSelectedIndex(8);
                    break;
            }
            jl_filter_weight.setLabelFor(jcb_filter_weight);
            ActionListener al_filter_weight = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    switch (i) {
                        case 0:
                            fractal.filterweight = 0;
                            break;
                        case 1:
                            fractal.filterweight = 32;
                            break;
                        case 2:
                            fractal.filterweight = 64;
                            break;
                        case 3:
                            fractal.filterweight = 96;
                            break;
                        case 4:
                            fractal.filterweight = 128;
                            break;
                        case 5:
                            fractal.filterweight = 160;
                            break;
                        case 6:
                            fractal.filterweight = 192;
                            break;
                        case 7:
                            fractal.filterweight = 224;
                            break;
                        case 8:
                            fractal.filterweight = 256;
                            break;
                    }
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_filter_weight.addActionListener(al_filter_weight);
            MouseListener ml_filter_weight = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_filter_weight.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_filter_weight.hidePopup();
                }
            };
            jcb_filter_weight.addMouseListener(ml_filter_weight);

            // motion blur option
            String[] s_motion_blur = {
                    "0",
                    "32",
                    "64",
                    "96",
                    "128",
                    "160",
                    "192",
                    "224",
                    "256"
            };
            jcb_motion_blur = new JComboBox<>(s_motion_blur);
            switch (fractal.motionblurp) {
                case 0:
                    jcb_motion_blur.setSelectedIndex(0);
                    break;
                case 32:
                    jcb_motion_blur.setSelectedIndex(1);
                    break;
                case 64:
                    jcb_motion_blur.setSelectedIndex(2);
                    break;
                case 96:
                    jcb_motion_blur.setSelectedIndex(3);
                    break;
                case 128:
                    jcb_motion_blur.setSelectedIndex(4);
                    break;
                case 160:
                    jcb_motion_blur.setSelectedIndex(5);
                    break;
                case 192:
                    jcb_motion_blur.setSelectedIndex(6);
                    break;
                case 224:
                    jcb_motion_blur.setSelectedIndex(7);
                    break;
                case 256:
                    jcb_motion_blur.setSelectedIndex(8);
                    break;
            }
            jl_motion_blur.setLabelFor(jcb_motion_blur);
            ActionListener al_motion_blur = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    switch (i) {
                        case 0:
                            fractal.setMotionBlur(0);
                            break;
                        case 1:
                            fractal.setMotionBlur(32);
                            break;
                        case 2:
                            fractal.setMotionBlur(64);
                            break;
                        case 3:
                            fractal.setMotionBlur(96);
                            break;
                        case 4:
                            fractal.setMotionBlur(128);
                            break;
                        case 5:
                            fractal.setMotionBlur(160);
                            break;
                        case 6:
                            fractal.setMotionBlur(192);
                            break;
                        case 7:
                            fractal.setMotionBlur(224);
                            break;
                        case 8:
                            fractal.setMotionBlur(256);
                            break;
                    }
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_motion_blur.addActionListener(al_motion_blur);
            MouseListener ml_motion_blur = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_motion_blur.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_motion_blur.hidePopup();
                }
            };
            jcb_motion_blur.addMouseListener(ml_motion_blur);

            // show frame rate option
            String[] s_show_frame_rate = {"show fps", "hide fps"};
            jcb_show_frame_rate = new JComboBox<>(s_show_frame_rate);
            if (frame_rate_display) {
                jcb_show_frame_rate.setSelectedIndex(0);
            } else {
                jcb_show_frame_rate.setSelectedIndex(1);
            }
            jl_show_frame_rate.setLabelFor(jcb_show_frame_rate);
            ActionListener al_show_frame_rate = e -> {
                String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (selected.equals("show fps")) {
                    frame_rate_display = true;
                    show_help_screen(); // update the textual on-screen help with new values of settings
                    Main.perceptron.requestFocus();
                } else if (selected.equals("hide fps")) {
                    frame_rate_display = false;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_show_frame_rate.addActionListener(al_show_frame_rate);
            MouseListener ml_show_frame_rate = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_show_frame_rate.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_show_frame_rate.hidePopup();
                }
            };
            jcb_show_frame_rate.addMouseListener(ml_show_frame_rate);

            // load preset option
            jtb_load_preset = new JToggleButton();
            jtb_load_preset.setSelected(false);
            jtb_load_preset.setText("load preset");
            ItemListener il_load_preset = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    open_preset_running = true; // calls open_preset() in Perceptron.java
                    running = false;
                    jtb_load_preset.setText("now loading preset file...");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    open_preset_running = false;
                    running = true;
                    jtb_load_preset.setText("load preset");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jtb_load_preset.addItemListener(il_load_preset);

            // animate option
            jtb_animate = new JToggleButton();
            jtb_animate.setSelected(animation_menu_ran_once);
            jtb_animate.setText("movie recorder off");
            ItemListener il_animate = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    /*
                    if (!animation_menu_ran_once) {
                        save_animation_menu_running = true;
                        animation_menu_ran_once = true;
                    }
                    */
                    animation_menu_ran_once = true;
                    init_movie_writer(true);
                    jtb_animate.setText("movie recorder on");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    /*
                    if (animation_menu_ran_once) {
                        save_animation_menu_running = false;
                        animation_menu_ran_once = false;
                        write_animation = false;
                    }
                    */
                    animation_menu_ran_once = false;
                    init_movie_writer(false);
                    jtb_animate.setText("movie recorder off");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jtb_animate.addItemListener(il_animate);

            // option autorotate ortho
            String[] s_autorotate_ortho = {
                    "0",
                    "1",
                    "2"
            };
            jcb_autorotate_ortho = new JComboBox<>(s_autorotate_ortho);
            jcb_autorotate_ortho.setSelectedIndex(fractal.ortho_type);
            jl_autorotate_ortho.setLabelFor(jcb_autorotate_ortho);
            ActionListener al_autorotate_ortho = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.ortho_type = i;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_autorotate_ortho.addActionListener(al_autorotate_ortho);
            MouseListener ml_autorotate_ortho = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_autorotate_ortho.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_autorotate_ortho.hidePopup();
                }
            };
            jcb_autorotate_ortho.addMouseListener(ml_autorotate_ortho);

            // option autorotate polar
            String[] s_autorotate_polar = {
                    "0",
                    "1",
                    "2"
            };
            jcb_autorotate_polar = new JComboBox<>(s_autorotate_polar);
            jcb_autorotate_polar.setSelectedIndex(fractal.polar_type);
            jl_autorotate_polar.setLabelFor(jcb_autorotate_polar);
            ActionListener al_autorotate_polar = e -> {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                if (i >= 0) {
                    fractal.polar_type = i;
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jcb_autorotate_polar.addActionListener(al_autorotate_polar);
            MouseListener ml_autorotate_polar = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    jcb_autorotate_polar.showPopup();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //jcb_autorotate_polar.hidePopup();
                }
            };
            jcb_autorotate_polar.addMouseListener(ml_autorotate_polar);

            // autopilot option
            jtb_autopilot = new JToggleButton();
            jtb_autopilot.setSelected(false);
            jtb_autopilot.setText("autopilot");
            ItemListener il_autopilot = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    controls.screensaver = true;
                    jtb_autopilot.setText("pilot ON");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    controls.screensaver = false;
                    jtb_autopilot.setText("autopilot");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jtb_autopilot.addItemListener(il_autopilot);

            // wander option
            jtb_wander = new JToggleButton();
            jtb_wander.setSelected(false);
            jtb_wander.setText("wanderer");
            ItemListener il_wander = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    controls.wanderer = true;
                    jtb_wander.setText("wanderer ON");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    controls.wanderer = false;
                    jtb_wander.setText("wanderer");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jtb_wander.addItemListener(il_wander);

            // live grabber, screen capture option
            jtb_live_grabber = new JToggleButton("live grabber");
            if (live_grab) {
                webcam_grab = false; // deactivate webcam during live screen grabbing
                if (cam_works) jtb_webcam.setSelected(false);
                jtb_live_grabber.setSelected(true);
                jtb_live_grabber.setText("live grabber ON");
            } else {
                jtb_live_grabber.setSelected(false);
                jtb_live_grabber.setText("live grabber off");
            }
            ItemListener il_live_grabber = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    webcam_grab = false; // deactivate webcam during live screen grabbing
                    if (cam_works) jtb_webcam.setSelected(false);
                    live_grab = true;
                    jtb_live_grabber.setText("live grabber ON");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    live_grab = false;
                    jtb_live_grabber.setText("live grabber off");
                    show_help_screen();
                    Main.perceptron.requestFocus();
                }
            };
            jtb_live_grabber.addItemListener(il_live_grabber);

            // webcam option
            if (cam_works) {
                jtb_webcam = new JToggleButton("webcam grabber");
                if (webcam_grab && webcam.isOpen()) {
                    live_grab = false; // deactivate live screen grabbing during webcam input
                    jtb_live_grabber.setSelected(false);
                    jtb_webcam.setSelected(true);
                    jtb_webcam.setText("webcam grabber ON");
                } else {
                    jtb_webcam.setSelected(false);
                    jtb_webcam.setText("webcam grabber off");
                }
                ItemListener il_webcam = e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        live_grab = false; // deactivate live screen grabbing during webcam input
                        jtb_live_grabber.setSelected(false);
                        webcam.open(true);
                        webcam_grab = true;
                        jtb_webcam.setText("webcam grabber ON");
                        show_help_screen();
                        Main.perceptron.requestFocus();
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        webcam_grab = false;
                        webcam.close();
                        jtb_webcam.setText("webcam grabber off");
                        show_help_screen();
                        Main.perceptron.requestFocus();
                    }
                };
                jtb_webcam.addItemListener(il_webcam);

                // webcams list
                jcb_webcams = new JComboBox<>();
                for (int w = 0; w < webcams.size(); w++) {
                    jcb_webcams.addItem(webcams.get(w).getName());
                }
                jl_webcams.setLabelFor(jcb_webcams);
                ActionListener al_webcams = e -> {
                    int i = ((JComboBox) e.getSource()).getSelectedIndex();
                    if (i >= 0) {
                        if (jtb_webcam.isSelected()) {
                            jtb_webcam.setSelected(false);
                            webcam = webcams.get(i);
                            Dimension[] webcamViewSizes = webcam.getViewSizes();
                            for (int j = 0; j < webcam.getViewSizes().length; j++ )
                            {
                                System.out.println("webcam " + i + "  resolutions: " + webcamViewSizes[j]);
                            }
                            System.out.println("setting the highest webcam " + i + " resolution: " + webcamViewSizes[webcam.getViewSizes().length - 1]);
                            webcam.setViewSize(webcamViewSizes[webcam.getViewSizes().length - 1]);
                            webcam_capture_formatted = new BufferedImage(webcamViewSizes[webcam.getViewSizes().length - 1].width, webcamViewSizes[webcam.getViewSizes().length - 1].height, BufferedImage.TYPE_INT_RGB);
                            jtb_webcam.setSelected(true);
                        } else {
                            webcam = webcams.get(i);
                            Dimension[] webcamViewSizes = webcam.getViewSizes();
                            for (int j = 0; j < webcam.getViewSizes().length; j++ )
                            {
                                System.out.println("webcam " + i + "  resolutions: " + webcamViewSizes[j]);
                            }
                            System.out.println("setting the highest webcam " + i + " resolution: " + webcamViewSizes[webcam.getViewSizes().length - 1]);
                            webcam.setViewSize(webcamViewSizes[webcam.getViewSizes().length - 1]);
                            webcam_capture_formatted = new BufferedImage(webcamViewSizes[webcam.getViewSizes().length - 1].width, webcamViewSizes[webcam.getViewSizes().length - 1].height, BufferedImage.TYPE_INT_RGB);
                            jtb_webcam.setSelected(true);
                        }
                        show_help_screen();
                        Main.perceptron.requestFocus();
                    }
                };
                jcb_webcams.addActionListener(al_webcams);
                MouseListener ml_webcams = new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        jcb_webcams.showPopup();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        //jcb_webcams.hidePopup();
                    }
                };
                jcb_webcams.addMouseListener(ml_webcams);

            }


            // fixate the width of jcomboboxes
            jcb_show_help.setPrototypeDisplayValue("0123456           890123456789012345");
            jcb_fractal_map.setPrototypeDisplayValue("0123456  9012345  890123456789012345");
            jcb_pullback.setPrototypeDisplayValue("0123456  9012345  890123456789012345");
            jcb_boundary_condition.setPrototypeDisplayValue("0123456           890123456789012345");
            jcb_outside_coloring.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_reflection_map.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_gradient_mode.setPrototypeDisplayValue("012345678901234567890123456789012345");
            jcb_gradient_shape.setPrototypeDisplayValue("0123456           890123456789012345");
            jcb_gradient_direction.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_fade_color_mode.setPrototypeDisplayValue("0123456       4567890123456789012345");
            jcb_dampen_fade_color.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_color_accent.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_color_filter.setPrototypeDisplayValue("0123456           890123456789012345");
            jcb_partial_inversion.setPrototypeDisplayValue("012345678901234567890123456789012345");
            jcb_total_inversion.setPrototypeDisplayValue("012345678901234567890123456789012345");
            jcb_initial_set.setPrototypeDisplayValue("0123456           890123456789012345");
            jcb_salvia_mode.setPrototypeDisplayValue("0123456  9012345  890123456789012345");
            jcb_XOR_salvia_mode.setPrototypeDisplayValue("0123456  90123    890123456789012345");
            jcb_image_mode.setPrototypeDisplayValue("0123456      34567890123456789012345");
            jcb_select_image.setPrototypeDisplayValue("0123456  90   4567890123456789012345");
            jcb_shuffle_images.setPrototypeDisplayValue("0123456  9012   67890123456789012345");
            jcb_draw_tree.setPrototypeDisplayValue("012345678901234567890123456789012345");
            jcb_convolution.setPrototypeDisplayValue("0123456           890123456789012345");
            jcb_hide_show_cursors.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_cursor_trails.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_slow_mode.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_filter_weight.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            jcb_motion_blur.setPrototypeDisplayValue("0123456          7890123456789012345");
            jcb_show_frame_rate.setPrototypeDisplayValue("012345678901234567890123456789012345");
            jcb_autorotate_ortho.setPrototypeDisplayValue("01 345678901234567890123456789012345");
            jcb_autorotate_polar.setPrototypeDisplayValue("0123456          7890123456789012345");
            if (cam_works) {
                jcb_webcams.setPrototypeDisplayValue("0123456  901234567890123456789012345");
            }

            /*
            try {
                Desktop.getDesktop().edit(new File ("resource" + File.separatorChar + "Settings.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            */


            //// labels and fields arranged in two columns in sw_frame
            // labels in a panel
            JPanel labelPane = new JPanel(new GridLayout(0, 1));
            labelPane.add(jl_show_help);
            labelPane.add(jl_fractal_map);
            labelPane.add(jl_type_equation);
            labelPane.add(jl_pullback);
            labelPane.add(jl_boundary_condition);
            labelPane.add(jl_outside_coloring);
            labelPane.add(jl_reflection_map);
            labelPane.add(jl_gradient_mode);
            labelPane.add(jl_gradient_shape);
            labelPane.add(jl_gradient_direction);
            labelPane.add(jl_fade_color_mode);
            labelPane.add(jl_dampen_fade_color);
            labelPane.add(jl_color_accent);
            labelPane.add(jl_color_filter);
            labelPane.add(jl_partial_inversion);
            labelPane.add(jl_total_inversion);
            labelPane.add(jl_initial_set);
            labelPane.add(jl_salvia_mode);
            labelPane.add(jl_XOR_salvia_mode);
            labelPane.add(jl_image_mode);
            labelPane.add(jl_select_image);
            labelPane.add(jl_shuffle_images);
            labelPane.add(jl_draw_tree);
            labelPane.add(jl_convolution);
            labelPane.add(jl_hide_show_cursors);
            labelPane.add(jl_cursor_trails);
            labelPane.add(jl_slow_mode);
            labelPane.add(jl_filter_weight);
            labelPane.add(jl_motion_blur);
            labelPane.add(jl_show_frame_rate);
            labelPane.add(jl_load_preset);
            labelPane.add(jl_animate);
            labelPane.add(jl_autorotate_ortho);
            labelPane.add(jl_autorotate_polar);
            labelPane.add(jl_autopilot);
            labelPane.add(jl_wander);
            labelPane.add(jl_live_grabber);
            if (cam_works) {
                labelPane.add(jl_webcam);
                labelPane.add(jl_webcams);
            }


            // fields in a panel
            JPanel fieldPane = new JPanel(new GridLayout(0, 1));
            fieldPane.add(jcb_show_help);
            fieldPane.add(jcb_fractal_map);
            fieldPane.add(jtb_type_equation);
            fieldPane.add(jcb_pullback);
            fieldPane.add(jcb_boundary_condition);
            fieldPane.add(jcb_outside_coloring);
            fieldPane.add(jcb_reflection_map);
            fieldPane.add(jcb_gradient_mode);
            fieldPane.add(jcb_gradient_shape);
            fieldPane.add(jcb_gradient_direction);
            fieldPane.add(jcb_fade_color_mode);
            fieldPane.add(jcb_dampen_fade_color);
            fieldPane.add(jcb_color_accent);
            fieldPane.add(jcb_color_filter);
            fieldPane.add(jcb_partial_inversion);
            fieldPane.add(jcb_total_inversion);
            fieldPane.add(jcb_initial_set);
            fieldPane.add(jcb_salvia_mode);
            fieldPane.add(jcb_XOR_salvia_mode);
            fieldPane.add(jcb_image_mode);
            fieldPane.add(jcb_select_image);
            fieldPane.add(jcb_shuffle_images);
            fieldPane.add(jcb_draw_tree);
            fieldPane.add(jcb_convolution);
            fieldPane.add(jcb_hide_show_cursors);
            fieldPane.add(jcb_cursor_trails);
            fieldPane.add(jcb_slow_mode);
            fieldPane.add(jcb_filter_weight);
            fieldPane.add(jcb_motion_blur);
            fieldPane.add(jcb_show_frame_rate);
            fieldPane.add(jtb_load_preset);
            fieldPane.add(jtb_animate);
            fieldPane.add(jcb_autorotate_ortho);
            fieldPane.add(jcb_autorotate_polar);
            fieldPane.add(jtb_autopilot);
            fieldPane.add(jtb_wander);
            fieldPane.add(jtb_live_grabber);
            if (cam_works) {
                fieldPane.add(jtb_webcam);
                fieldPane.add(jcb_webcams);
            }

            // Put the panels in this panel, labels on left, text fields on right.
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            add(labelPane, BorderLayout.CENTER);
            add(fieldPane, BorderLayout.LINE_END);

        }


        private void define(Settings_Window sw) {
            //Create and set up the window.
            sw_frame = new JFrame("configomatics");
            sw_frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            sw_frame.setIconImage(icon.getImage());
            sw_frame.setAutoRequestFocus(false);

            //Add contents to the window.
            sw_frame.add(sw);

            // set saved position and size
            if (system_based_preferences) {
                if (PREFS.getInt("configomatics_location.x", 0) >= 0 && PREFS.getInt("configomatics_location.x", 0) < display_mode.getWidth() &&
                        PREFS.getInt("configomatics_location.y", 0) >= 0 && PREFS.getInt("configomatics_location.y", 0) < display_mode.getHeight() &&
                        PREFS.getInt("configomatics_size.width", 0) >= 0 && PREFS.getInt("configomatics_size.width", 0) < display_mode.getWidth() &&
                        PREFS.getInt("configomatics_size.height", 0) >= 0 && PREFS.getInt("configomatics_size.height", 0) < display_mode.getHeight()) {
                    sw_frame.setLocation(PREFS.getInt("configomatics_location.x", 0), PREFS.getInt("configomatics_location.y", 0));
                    sw_frame.setSize(PREFS.getInt("configomatics_size.width", 0), PREFS.getInt("configomatics_size.height", 0));
                    if (sw_frame.getSize().width < 80 || sw_frame.getSize().height < 80) sw_frame.pack();
                } else {
                    sw_frame.pack();
                }

            } else {

                if (config.getInt("configomatics_location.x", 0) >= 0 && config.getInt("configomatics_location.x", 0) < display_mode.getWidth() &&
                        config.getInt("configomatics_location.y", 0) >= 0 && config.getInt("configomatics_location.y", 0) < display_mode.getHeight() &&
                        config.getInt("configomatics_size.width", 0) >= 0 && config.getInt("configomatics_size.width", 0) < display_mode.getWidth() &&
                        config.getInt("configomatics_size.height", 0) >= 0 && config.getInt("configomatics_size.height", 0) < display_mode.getHeight()) {
                    sw_frame.setLocation(config.getInt("configomatics_location.x", 0), config.getInt("configomatics_location.y", 0));
                    sw_frame.setSize(config.getInt("configomatics_size.width", 0), config.getInt("configomatics_size.height", 0));
                    if (sw_frame.getSize().width < 80 || sw_frame.getSize().height < 80) sw_frame.pack();
                } else {
                    sw_frame.pack();
                }
            }

            // add listener that saves window position and size
            sw_frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (system_based_preferences) {
                        PREFS.putInt("configomatics_location.x", sw_frame.getLocation().x);
                        PREFS.putInt("configomatics_location.y", sw_frame.getLocation().y);
                        PREFS.putInt("configomatics_size.width", sw_frame.getSize().width);
                        PREFS.putInt("configomatics_size.height", sw_frame.getSize().height);
                    } else {
                        config.setProperty("configomatics_location.x", sw_frame.getLocation().x);
                        config.setProperty("configomatics_location.y", sw_frame.getLocation().y);
                        config.setProperty("configomatics_size.width", sw_frame.getSize().width);
                        config.setProperty("configomatics_size.height", sw_frame.getSize().height);
                        try {
                            config.save();
                        } catch (ConfigurationException exc) {
                            exc.printStackTrace();
                        }
                    }
                }
            });

        }


        public void refresh_settings_window() {
            if (sw != null) {
                if (controls.ENTRY_MODE) {
                    jtb_type_equation.setSelected(true);
                    jtb_type_equation.setText("now reading in equation...");
                } else {
                    jtb_type_equation.setSelected(false);
                    jtb_type_equation.setText("Type equation");
                }
                Main.perceptron.requestFocus();
            }
        }


        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
        }

    }
}



