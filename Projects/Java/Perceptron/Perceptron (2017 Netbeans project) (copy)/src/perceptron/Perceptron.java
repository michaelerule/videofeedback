package perceptron;

//import video.JWebCam;
import image.DoubleBuffer;
import image.ImageCache;
import vectorsources.CellModel;
import rendered3D.TreeForm;
import rendered3D.Tree3D;
import math.complex;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import static java.lang.Math.*;
import static java.awt.RenderingHints.*;

/** 
 * This is "manager" class of perceptron.
 * <p>
 * Class Perceptron manages the creation of a fullscreen window, and executes 
 * the main drawing loop. 
 * <p>
 * This class contains several publically settable variables. These variables
 * are designed to be automatically configured by the settings file when 
 * perceptron starts up, and should not actually be modified while the program
 * is running. 
 */

public class Perceptron extends javax.swing.JFrame {

    //The effective screen dimensions (size of output buffer) default 640 x 480
    /** Width of rendering panel */
    public int screen_width = 640;
    /** height of rendering panel */
    public int screen_height = 400;
    /** display the frame rate in the upper left corner? */
    public boolean frame_rate_display = false;
    /** perform the fractal mapping before or after drawing objects ?*/
    public boolean objects_on_top = true;
    /** upper limit on the program speed? */
    public boolean cap_frame_rate = false;
    /** write frames to disk ? */
    public boolean write_animation = false;
    /** fullscreen ? */
    public boolean fullscreen = true;

    boolean convolution_on = true;
    
    /** The actual screen dimensions */
    /** The actual screen dimensions */
    protected int physical_width,  physical_height;

    /** tracks the current frame */
    private long frame ;
    /** hard coded image firectory */
    private final String image_directory = "resource/images";

    /** the screen graphics, for displaying rendered frames */
    Graphics screen_graphics;
    BufferedImage screen_buffer;
    BufferStrategy bufferStrategy;
    Graphics2D graph2D;

    /** Perceptron buffer */
    public final DoubleBuffer buffer;
    
    private ImageCache images = null;
    ControlSet controls;
    public RenderingKernel fractal;
    public Tree3D the_tree;
    private Vector<Mapping> user_maps;
    private Preset[] user_presets;
    private boolean running;
    private JFileChooser saver;
    //final IntrospectiveTerminal term;
    //final TextLayer textlayer;
    boolean XOR_MODE;
    boolean salvia_mode;
    boolean rotateImages;
    boolean show_state;
    
    /**Toggles convolution operator
     * @return weather or not convolution is active
     */
    public boolean toggle_convolution() {
        return convolution_on = !convolution_on;
    }

    static GraphicsConfiguration getDeviceGraphicsConfig() {
        return (((GraphicsEnvironment.getLocalGraphicsEnvironment())
                .getDefaultScreenDevice()).getDefaultConfiguration());
    }

    /**
     *
     * @param Settings
     * @param Presets
     */
    public Perceptron(String Settings, String Presets) {

        System.out.println("ok");
        this.setBackground(Color.black);

        saver = new JFileChooser("Save State");
        saver.setBounds(0, 0, this.physical_width, this.physical_height);
        saver.setDialogTitle("Save the state and the screen as...");    // menu window title

        user_maps = new Vector<Mapping>();

        //RenderingKernel.init_static();
        parse_settings(Settings,Presets);//READ IN SETTINGS INFORMATION

        setup_frame();           //FRAME SETUP

        make_fullscreen();       //FULLSCREEN INITIALISATION
        hide_cursor();           //CONCEAL MOUSE POINTER

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
        gtest = new Convolution(2,buffer);

        // Store exc local pointer to the screen Graphics
        screen_buffer = getDeviceGraphicsConfig().createCompatibleImage(physical_width, physical_height, Transparency.OPAQUE);
        screen_graphics = screen_buffer.getGraphics();

        // Initialise the Fractal, the Tree, and their associated parameters
        initialise_objects();

        // Input event listeners setup, this is left until last to avoid
        // processing input until the entire object is ready
        initialise_listeners();

        controls.preset(0);

        System.out.println("buffer width : " + screen_width   + " height : " + screen_height);
        System.out.println("screen width : " + physical_width + " height : " + physical_height);

        //textlayer = new TextLayer(this);
        /*term = new IntrospectiveTerminal();
        term.setcols(textlayer.cols);
        term.out=textlayer.me;
        //term.debug=textlayer.me;
        term.register("perc",this);
        term.register("tree",the_tree);
        term.register("fractal",fractal);
        term.register("control",controls);
        term.register("quit",new Object(){public String toString(){System.exit(0);return "Exiting";}});
        final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        Thread go = new Thread()
        {   
            @Override
            public void run()
            {
                while (true) try {
                    System.out.print("> ");
                    String s = input.readLine();
                    if (s.equals("quit")) System.exit(0);
                    term.interpret(s,null);
                } catch(Exception e) { e.printStackTrace(); }
            }
        };
        go.setPriority( Thread.MIN_PRIORITY );
        go.start();
         *
         */
    }

    @Override
    public String toString() {
        return "Percrptron";
    }

    /** Set JFrame parameters upon initialisatrion*/
    private void setup_frame() {
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
        if (fullscreen) try {
            this.dispose();
            this.setUndecorated(true);
            this.setResizable(false);
        } catch (Exception e) {
        }
    }

    /** Set up fullscreen mode */
    void make_fullscreen() {
        if (!fullscreen) { 
            make_not_fullscreen();
            physical_width=screen_width;
            physical_height=screen_height;
            setVisible(true);
            setSize(physical_width,physical_height);
            graph2D = (Graphics2D) this.getGraphics();
            return;
        }
        GraphicsDevice g = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode current = g.getDisplayMode();
        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window) this);
            DisplayMode[] possible_modes = g.getDisplayModes();
            DisplayMode best_mode = null;
            for (DisplayMode m : possible_modes) {
                System.out.println(m.getBitDepth()+" "+m.getHeight()+" "+m.getRefreshRate()+" "+m.getWidth());
                if (m.getWidth() >= screen_width && 
                    m.getHeight() >= screen_height && 
                    m.getBitDepth() >= Math.min(32, current.getBitDepth())) {
                    System.out.println("Useable mode found for "+screen_width+" x "+screen_height);
                    if (best_mode == null 
                        || m.getWidth() * m.getHeight() < best_mode.getWidth() * best_mode.getHeight()) 
                        best_mode = m;
                }
            }
            if (best_mode != null) {
                //I had to turn this off because it doesn't work on all systems
                //TODO : detect if this is supported
                //or make it configurable in the settings file
                System.out.println("Setting mode"+ best_mode);
                System.out.println(best_mode.getWidth());
                System.out.println(best_mode.getHeight());
                g.setDisplayMode(best_mode);
                //System.exit(-5);
            }
        } else {
            DisplayMode m = g.getDisplayMode();
            this.setBounds(0, 0, m.getWidth(), m.getHeight());
            this.setVisible(true);
        }
        graph2D = (Graphics2D) this.getGraphics();
        physical_width = getWidth();
        physical_height = getHeight();
    }

    void make_not_fullscreen() {
        GraphicsDevice g = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode current = g.getDisplayMode();
        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window) null);

        }
    }

    /** 
     * Create and add input event listeners to this JFrame
     */
    private final void initialise_listeners() {
        //this.addKeyListener(textlayer);
        controls = new ControlSet(this, user_presets);
        this.addMouseListener(controls);
        this.addMouseMotionListener(controls);
        this.addKeyListener(controls);
    }

    /** 
     * Initialise the Fractal, Tree, and associated parameters
     */
    private final void initialise_objects() {
        images = new ImageCache(image_directory);
        fractal = new RenderingKernel(buffer, user_maps, this);
        the_tree = new Tree3D(
                6,
                9,
                new float[][]{{0, 0, 0}, {0, (float) ((screen_height / 6
                        )), 0}}, 0,
                //new float[][]{{0, 0, 0}, {0, (float) (-(screen_height / 6)), 0}}, 0,
                new TreeForm[]{new TreeForm(.5f, -.2f, .7f, 7),
                new TreeForm(.5f, .2f, .7f, -7)
        },
                new Point((short) (screen_width / 2), (short) (screen_height / 2)), buffer);
    }

    /** 
     * Hide the mouse pointer, if possible
     */
    public void hide_cursor() {
        try {
            // Switch to the line below to get a crosshair 
            // setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            setCursor(Toolkit.getDefaultToolkit().
                    createCustomCursor(Toolkit.getDefaultToolkit().
                    getImage("xparent.gif"), new Point(0, 0), null));
        } catch (Exception e) {
            System.err.println("Cursor modification is unsupported.");
        }
    }
    
    static double gaussian( double x, double sigma ) {
        return exp(-.5*pow(x/sigma,2))/(sigma*sqrt(2*PI));
    }

    Kernel make_gaussian(float std)
    {
        int s = (int)(4*std);
        float sum = 0f;
        float [] d = new float[s*s];
        for (int i=0; i<s; i++ ) for (int j=0; j<s; j++ )
            sum += d[i*s+j] = (float)gaussian(i-s/2,std)*(float)gaussian(j-s/2,std);
        sum = 1.0f/sum;
        for (int i=0; i<s*s; i++ )
            d[i] *= sum;
        return new Kernel(s,s,d);
    }

    Kernel make_unsharp(float std){
        int s = (int)(4*std);
        float sum = 0f;
        float [] d = new float[s*s];
        for (int i=0; i<s; i++ ) for (int j=0; j<s; j++ )
            sum += d[i*s+j] = -(float)gaussian(i-s/2,std)*(float)gaussian(j-s/2,std);
        d[s*(s/2)+(s/2)] += 1.0f-sum;
        return new Kernel(s,s,d);
    }

    Kernel make_cortex(float es, float is, float t) {
        int s = (int)(4*max(es,is));
        float sum = 0f;
        float [] d = new float[s*s];
        for (int i=0; i<s; i++ ) for (int j=0; j<s; j++ )
            sum += d[i*s+j] = (float)gaussian(i-s/2,es)*(float)gaussian(j-s/2,es)
                    -t*(float)gaussian(i-s/2,is)*(float)gaussian(j-s/2,is);
        sum = 1.0f/sum;
        for (int i=0; i<s*s; i++ )
            d[i] *= sum;
        return new Kernel(s,s,d);

    }

    ConvolveOp gconv = new ConvolveOp(make_unsharp(4),ConvolveOp.EDGE_NO_OP,new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_COLOR_RENDER_SPEED));
    Convolution gtest;

    long last_image_time = System.currentTimeMillis();
    long FRAME = 0;

    /**
     *
     */
    @SuppressWarnings("empty-statement")
    public void go() {
        frame = 0 ;
        
        int x_offset = (physical_width - screen_width) / 2;
        int y_offset = (physical_height - screen_height) / 2;
        screen_graphics.setColor(Color.BLACK);

        long last_time = System.currentTimeMillis();
        long framerate = 0;

        running = true;
        increment_sketch(1);
        
        controls.preset(0);
        while (true) {
            if (running) {
                try {
                    long start_time = System.currentTimeMillis();
                    
                    if (convolution_on) gtest.operate(fractal.filterweight);
                    else
                        buffer.flip();

                    fractal.operate();

                    the_tree.render();
                    Graphics draw_graphics = buffer.output.graphics;

                    controls.advance((int) framerate);
                    controls.drawAll(buffer.output.graphics);
                    
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

                    BufferedImage drawn = buffer.output.image ;
                    screen_graphics.fillRect(0,0,physical_width,physical_height);
                    screen_graphics.drawImage(drawn,x_offset,y_offset,null);
                    graph2D.drawImage(screen_buffer,0,0,null);
                    if ( write_animation ) {
                        try {
                            String filename = "frame " + (FRAME++) + ".png";
                            File file = new File(filename);
                            ImageIO.write( drawn , "png", file );
                        } catch (Exception ex) {
                            System.err.println("write error");
                            ex.printStackTrace();
                        }
                    }

                    frame++;
                    if (cap_frame_rate)
                        while (System.currentTimeMillis() < start_time) ;

                } catch (Exception e) {
                    System.out.println("SOMETHING BAD HAPPENED!!!");
                    e.printStackTrace();
                }
                
            } else {
                save_frame();
            }

        }
    }

    private void save_frame() {
        //turns out turning off full screen temporarilty helps
        make_not_fullscreen();
        try {
            // JFrame is set in void setup_frame() and in make_fullscreen
            // saver is a class variableof type JFileChooser
            // hopedully these edits make it visible

            //trying everything I can to bring it to front
            //saver.setAlwaysOnTop(true);
            //saver.setAlwaysOnTop(false);
            saver.setVisible(true);
            //saver.toFront();
            saver.setFocusable(true);
            //saver.setFocusableWindowState(true);
            saver.requestFocus();
            
            int approved = saver.showSaveDialog(this);
            saver.grabFocus();


            // show the menu, seems to work but the menu is visible only on desktop,
            // use Alt+TAB to go there, works even when invisible in fullscreen
            File file = saver.getSelectedFile();
            System.out.println("...saving...");
            if (file != null && approved == saver.APPROVE_OPTION) {
                //TODO : check for file overwite
                System.out.println("writing image");
                ImageIO.write(buffer.output.image, "png", new File(file.getAbsolutePath() + ".output.png"));    // same as screenshot?
                ImageIO.write(buffer.buffer.image, "png", new File(file.getAbsolutePath() + ".buffer.png"));    // screenshot
                ImageIO.write(buffer.disply.image, "png", new File(file.getAbsolutePath() + ".display.png"));   // unknown
                ImageIO.write(buffer.image.image, "png", new File(file.getAbsolutePath() + ".sketch.png"));     // sketch used for one of the edge extend functions??
                System.out.println("writing state");
                Preset.write(this, new File(file.getAbsolutePath() + ".state")); // save *.state
                System.out.println("...saved...");
            } else {
                System.out.println("...did not save...");
            }
            System.out.println("done...");
        } catch (Exception E) {
            E.printStackTrace();
        }
        make_fullscreen();
        hide_cursor();
        running = true;
    }

    /** 
     * Read in the settings file
     */
    final void parse_settings(String settings_path,String presets_path) {
        System.out.println("Parsing Settings");
        ArrayList<Preset> presets = new ArrayList<Preset>();
        try {
            //Create exc FileReader for reading the file
            BufferedReader in = new BufferedReader(new FileReader(settings_path));

            String current_line;

            while ((current_line = in.readLine()) != null) {
                if (current_line.length() > 0 &&
                        current_line.charAt(0) != '*') {

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
                                user_maps.add(RenderingKernel.makeMap(val));
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
                            //    System.err.println("NO VALID FORM MATCHING " + val);
                            }
                            }
                            }
                            }
                            }
                            }
                            }

                            try {
                                this.getClass().getField(var).set(this, value);
                           //     System.out.println("set " + var + " to " + value);
                            } catch (Exception e) {
                            //    System.err.println("failed to assign " + value + " to " + var);
                            }

                        }
                    }
                }
            }
            in.close();
            
            File f = new File( presets_path ) ;
        
            if ( f != null && f.listFiles() != null ) 
                for ( File file : f.listFiles() ) {
                    String name = file.getName() ;
                    try {
                        if ( name.endsWith(".state") ) 
                        {
                            in = new BufferedReader(new FileReader(file));
                            System.out.println("parsing preset " + name + ":");
                            presets.add(Preset.parse(in));
                            System.out.println("loaded preset " + name + " into slot "+presets.size());
                        }
                    } catch ( Exception e ) {
                        //System.err.println("could not open preset " + name );
                    }
                }
        } catch (Exception e) {
            //System.err.println("Error reading Settings file");
            //e.printStackTrace();
        }
        user_presets = presets.toArray(new Preset[presets.size()]);
    }

    /** @return display area width */
    public int screen_width() { return screen_width; }
    /** @return display area height */
    public int screen_height() { return screen_height; }
    /** @return actual physical screen area width */
    public int physical_width() { return physical_width; }
    /** @return actual physical screen area height */
    public int physical_height() { return physical_height; }

    /**
     *
     */
    public void toggle_objects_on_top() {
        objects_on_top = !objects_on_top;
    }

    /**
     *
     */
    public void toggle_frame_rate_display() {
        frame_rate_display = !frame_rate_display;
    }

    /**
     *
     */
    public void toggle_cap_frame_rate() {
        cap_frame_rate = !cap_frame_rate;
    }

    /**
     *
     * @param b
     */
    public void set_objects_on_top(boolean b) {
        objects_on_top = b;
    }

    /**
     *
     */
    public void save() {
        running = false;
    }

    /**
     *
     * @param n
     */
    public void increment_sketch(int n) {
            buffer.load_sketch(images.advance(n),this.screen_width,true,this);
            sketchNum = images.current() ;
    }

    int sketchNum = 0 ;

    /**
     *
     * @param n
     */
    public void set_sketch(int n) {
        try {
            sketchNum = n ;
            buffer.load_sketch(images.get(n),this.screen_width,true,this) ;
            last_image_time = 30000 + System.currentTimeMillis() ;
        } catch ( Exception E ) {
            E.printStackTrace();
        }
    }

    /**
     *
     */
    public void toggle_fancy() {
        buffer.toggle_fancy();
    }

    /**
     *
     * @return
     */
    public boolean is_fancy() {
        return buffer.fancy();
    }

    void set_fancy(boolean s) {
        if (s != is_fancy()) {
            toggle_fancy();
        }
    }

    public void eval(String s) {
        /*
        if (s.startsWith("clear")||s.matches("[ \\t\\n]*clear[ \\t\\n]*")) {
            for (int i=0; i<textlayer.cols; i++)
                textlayer.me.println();
            return;
        }
        String [] ss = s.split("=");
         * 
         */
        ////term.out.println("LHS="+ss[0]+" RHS="+(ss.length>1?ss[1]:""));
        //term.interpret(ss[0],ss.length>1?ss[1]:null);
    }

    void help_screen() {
        this.show_state = !this.show_state;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}

