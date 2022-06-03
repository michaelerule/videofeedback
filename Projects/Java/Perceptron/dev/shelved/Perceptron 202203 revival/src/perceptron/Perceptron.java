package perceptron;
/* Perceptron.java
 * Created on December 21,2006,5:27 PM
 */

/*
TODO
- Could we use ROBOT to grab the screen and pull it in to the fractal? 
https://stackoverflow.com/questions/18301429/java-print-screen-two-monitors
        Robot robot = new Robot();    
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage capture = new Robot().createScreenCapture(screenRect);
        ImageIO.write(capture, "bmp", new File("printscreen.bmp"));
*/

import rendered.TextMatrix;
import image.BlurSharpen;
import image.DoubleBuffer;
import rendered.Microphone;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import image.ImageCache;
import static java.awt.Color.BLACK;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import rendered.Object3D;
import rendered.TreeForm;
import rendered.Tree3D;
import rendered.Moths;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;

import java.util.Set;
import math.complex;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.atomic.AtomicBoolean;
import static javax.imageio.ImageIO.write;
import static math.complex.arg;
import static math.complex.mod;
import static perceptron.Preset.helpString;
import static perceptron.Map.Mapping;
import static perceptron.Parser.parseSettings;
import static color.ColorUtil.colorFilter;
import static color.ColorUtil.fast;
import static util.Misc.clip;
import static util.Misc.zip;
import static util.Misc.wrap;
import static util.Misc.fadeout;
import static util.Fullscreen.changeDisplayMode;
import static util.Fullscreen.getScreen;
import static util.Fullscreen.setFrame;
import static util.Sys.makeFullscreen;
import static util.Sys.CROSS;
import static util.Sys.NONE;
import static util.Sys.makeNotFullscreen;
import static util.Fullscreen.isFullscreenWindow;
import static util.Sys.serr;
import static util.Sys.sout;

/**
 * @author mer49
 */
public final class Perceptron extends javax.swing.JFrame {
    
    ////////////////////////////////////////////////////////////////////////////
    public final DoubleBuffer buf;
    Graphics2D                graph2D;
    ArrayList<Mapping>        maps;
    Preset[]                  presets;
    ImageCache                images = null;
    JFileChooser              savers;
    final Control             control;
    final Map             fractal;
    final Tree3D              tree;
    final TextMatrix          text;
    final BlurSharpen         blursharp;
    final Moths               moths;
    final Microphone          mic;
    
    int image_i = 0;
    public String image_directory = "resource/images/";
        
    ////////////////////////////////////////////////////////////////////////////
    // modifying these after initialization may cause undefined behavior.
    int half_screen_w;
    int half_screen_h;
    public int image_rotate_ms   = 5000;
    public int boredome_ms       = 100000;
    public int preset_rotate_ms  = 500000;
    public int screen_timeout_ms = 60000;
    public int max_frame_length  = 1000/20;
    public int audio_line        = -1;
    public int min_tree_depth    = 9;
    public int max_tree_depth    = 6;
    public int screen_width      = 480;
    public int screen_height     = 480;
    
    ////////////////////////////////////////////////////////////////////////////
    // Render control flags: all public, on the honor system. 
    public boolean objects_on_top     = true;
    public boolean cap_frame_rate     = true;
    public boolean write_animation    = false;
    public boolean rotate_images      = false;
    public boolean fore_grad          = false;
    public boolean draw_moths         = false; 
    public boolean draw_top_bars      = false;
    public boolean draw_side_bars     = false;
    public boolean draw_tree          = false;
    public boolean draw_dino          = false;
    public boolean do_color_transform = true;
    public boolean hide_mouse         = false;
    public int     hue_rate           = 0;
    public int     sat_rate           = 0;
    public int     lum_rate           = 0;
    public int     bri_rate           = 0;
    public int     con_rate           = 0;
    public int     blursharp_rate     = 0;
    
    // For fading the text overlays
    public boolean show_state   = false;
    public boolean show_monitor = false;
    public boolean show_notices = true;
    int state_α = 255;
    int info_α  = 255;
    int note_α  = 255;
    
    JFileChooser saver;
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Try to pull in the object data from disk (static)
    final Object3D o = load3DModel("resource/data/tetrahedron.txt");
    static Object3D load3DModel(String filename) {
        try {
            Object3D o = new Object3D(new BufferedReader(new FileReader(new File(filename))));
            o.recenter(200);
            return o;
        } catch (FileNotFoundException E) {
            sout("Could not load file "+filename);
        }
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters (boilerplate!) //////////////////////////////////////
    public int     screenWidth()             {return screen_width; }
    public int     screenHeight()            {return screen_height; }
    public int     halfScreenWidth()         {return half_screen_w; }
    public int     halfScreenHeight()        {return half_screen_h; }
    public boolean isAntialiased()           {return buf.antialiased; }
    public void    setObjectsOnTop(boolean b){objects_on_top = b; }
    public void    setAntialias(boolean s)   {if (s!=isAntialiased()) toggleAntialias();}
    public void    setHideMouse(boolean b)   {hide_mouse=b;setCursor(b?NONE:CROSS);}
    public void    setBlurWeight(int k)      {blursharp_rate = clip(k,-256,256);}
    public void    toggleAntialias()         {buf.toggleAntialias(); }
    public void    toggleObjectsOnTop()      {objects_on_top = !objects_on_top; }
    public void    toggleCapFramerate()      {cap_frame_rate = !cap_frame_rate; }
    public void    toggleAnimation()         {write_animation = !write_animation; }
    public void    toggleTree()              {draw_tree    = !draw_tree; }
    public void    toggleShowNotices()       {show_state   = !show_state;}
    public void    toggleShowHelp()          {show_state   = !show_state;}
    public void    toggleShowFramerate()     {show_monitor = !show_monitor; }
    public void    toggleHideMouse()         {setHideMouse(!hide_mouse);}

    public void setImage(int n) {
        try {
            image_i = n;
            buf.set(images.get(n));
            last_image_time = 30000+currentTimeMillis();
        } catch (Exception E) {
            E.printStackTrace();
        }
    }
    
    public void setImage(String n) {
        n = n.strip();
        if (n.length()<=0) return;
        int i = images.where(n);
        if (i<0) notify("Could not find image named \""+n+"\"");
        else setImage(i);
    }
    
    public void nextImage(int n) {
        buf.set(images.next(n));
        image_i = images.current();
        last_image_time = image_rotate_ms+currentTimeMillis();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /**
     * This entire constructor and most of the class design is one giant bug. 
     * Initialization must be done carefully,the order of operations matter.
     * 
     * @param settings_filename
     * @param crash_log_filename
     * @param presets_filename 
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Perceptron(
            String settings_filename,
            String crash_log_filename,
            String presets_filename) {
        
        super("Perceptron"); 
        Component root = getRootPane();
        Component cont = getContentPane();
        this.setBackground(BLACK);
        root.setBackground(BLACK);
        cont.setBackground(BLACK);

        // We must parse the settings file before going to full-screen,
        // because we need to know the desired canvas size to choose a
        // screen resolution.
        maps = new ArrayList<>();
        parseSettings(this,settings_filename,presets_filename); // apply config
        
        sout("Parsed screen_width="+screen_width+"; screen_height="+screen_height);
        
        // parseSettings may change the screen_width and _height variables
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Exit on window close
        setIgnoreRepaint(true); // ignore OS prompts for redrawing
        setResizable(false); // we turn this on again later
        Dimension dims = new Dimension(screen_width,screen_height);
        cont.setPreferredSize(dims);
        root.setPreferredSize(dims);
        cont.setMinimumSize(dims);
        root.setMinimumSize(dims);
        half_screen_w = (short)(screen_width /2);
        half_screen_h = (short)(screen_height/2);
        
        // screen_height and screen_width are only initialized AFTER parsing
        // the settings file. 
        // Initialise the frame rendering,background,and display buffers
        buf             = new DoubleBuffer(screen_width,screen_height);
        blursharp       = new BlurSharpen(buf);
        text            = new TextMatrix(screen_width,screen_height);  
        text.loadString(crash_log_filename);  
        image_directory = requireNonNull(image_directory);
        images          = new ImageCache(image_directory);
        fractal         = new Map(buf,maps,this);
        moths           = new Moths(screen_width,screen_height);
        mic             = new Microphone(buf, 0); 
        //mic.start();
        
        tree = new Tree3D(min_tree_depth, max_tree_depth,
            new float[][]{{0,0,0},{0,(float) (-(screen_height/12)),0}},0,
            new TreeForm[]{new TreeForm(.5f,-.2f,.7f,7),new TreeForm(.5f,.2f,.7f,-7)},
            new Point(half_screen_w,half_screen_h),buf);
        
        control = new Control(this,presets);
        addMouseListener(control);
        addMouseMotionListener(control);
        addKeyListener(control);
        setFocusTraversalKeysEnabled(false); // don't consume VK_TAB
        setHideMouse(false);
        
        // For sanity, we now start in windowed mode (NOT full screen)
        pack();
        setVisible(true);
        
        setMinimumSize(new Dimension(screen_width, getHeight()));
        setResizable(true);

        // Make the saver window and remember its size
        saver = new JFileChooser("Save State");
        saver.setPreferredSize(new Dimension(800,600));
        
        // Set up Double Buffering
        createBufferStrategy(2);
        
        // Apply initial state
        nextImage(1);
        control.setPreset(control.preset_i);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Timers and counters
    // last_image_time: Timer (m) for rotating input images
    // boredom_time: Time (ms) sine last user action
    // frame: total frame counter
    // animation_frame: frame counter when saving animations
    long last_image_time = -1;
    long last_frame_time = -1;
    long boredom_time    = -1;
    long frame           = 0;
    long framerate       = 20;
    long animation_frame = 0;
    
    ////////////////////////////////////////////////////////////////////////////
    // Locks: these are important but I haven't got them quite right yet!
    final AtomicBoolean is_fullscreen = new AtomicBoolean(false);
    final AtomicBoolean running       = new AtomicBoolean(false);
    public final boolean isFullscreen() {return is_fullscreen.get();}
    public final boolean isRunning()    {return running.get();}
    public final void    toggleRunning(){running.set(!running.get());}
    
    /*
     * Window scenarios: 
     *  - True full-screen window with matched display resolution
     *      * Don't scale image; Center and pad it. 
     *  - Full-screen with wrong resolution
     *      * Scale image up during drawing
     *      * Restrict to integer scale factors? What's fast?
     *  - Fake full screen window: maximized and undecorated
     *      Don't allow this, interaction with system menus unpredictable
     *  - Normal window, exact size
     *      Ensure 
     * 
     */
    
    ////////////////////////////////////////////////////////////////////////////
    public void go() {
        sout("Starting...");
        
        // Start all timers
        long time = currentTimeMillis();
        frame           = 0;
        last_frame_time = time;
        last_image_time = 20000+time;
        boredom_time    = 50000+time;
        
        // Start
        sout("Entering Kernel...");
        running.set(true);
        while (true) 
            if (running.get()) try {
                long frame_started_at = currentTimeMillis();

                synchronized (is_fullscreen) {doFrame();}

                // Frame counters and framerate monitoring
                frame++;
                time = currentTimeMillis();
                if (time - last_frame_time >= 1000) {
                    last_frame_time = time;
                    framerate       = frame;
                    frame           = 0;
                }
                if (cap_frame_rate) {
                    long delta = frame_started_at + max_frame_length 
                               - currentTimeMillis();
                    if (delta>0) Thread.sleep(delta);
                }          
            } catch (Exception e) {serr("Error in go!"); e.printStackTrace();}
            else try {Thread.sleep(50);} catch (InterruptedException ex) {}
    }
        
    /**
     * Render a single frame.
     */
    private void doFrame() {

        // Apply the fractal mapping
        fractal.operate();

        // We will draw these later if not objects_on_top
        if (objects_on_top) drawObjects();

        control.advance((int)framerate);
        control.drawAll(buf.out.g);

        // Fade to visible if on,otherwise fade away
        state_α = fadeout(state_α,show_state  );
        info_α  = fadeout(info_α ,show_monitor);
        note_α  = fadeout(note_α ,show_notices);
        if (state_α>0) drawState();
        if (info_α >0) drawInfo();
        if (note_α >0) noticeChanges();

        // Save frame to disk
        if (write_animation) try {
            String filename = "animate/frame "+animation_frame+".png";
            animation_frame++;
            File file = new File(filename);
            ImageIO.write(buf.out.img,"png",file);
        } catch (IOException ex) {
            System.err.println("File write error in animation.");
            ex.printStackTrace();
        }

        // Motion blur averages the next color with the previous 
        // output buffer,if `objects_on_top`; or with the previous
        // buffer buffer if the objects are in the background. 
        // But,some objects and transforms,like borders and hue
        // rotation,we want to behave as if they are always in 
        // the background. I think this means I always need to
        // save a copy in buf.buf,and switch motion blur to only
        // use this copy.
        //buf.buf.g2D.drawImage(buf.out.img,0,0,null);
        buf.buf.img.getRaster().setDataElements(0,0,buf.out.img.getRaster());

        // Copy to physical screen with double buffering.
        showOnScreen();

        // This must be done before the copy in objects on top? 
        if (do_color_transform) colorTransform(buf.out.buf);
        drawBars();

        // Background objects: the .output buffer must not be used
        // to draw these,since this will cause motion blur to 
        // reveal the drawn objects in the foreground. Neverthelss,
        // the .output buffer must contain these objects by the time
        // that the fractal map is applied. 
        // If we didn't draw objects,draw them now. They will 
        // show up behind the map in the next frame. 
        // So,we need to save a copy of the untainted output for
        // the motion blur code (FractalMap) to use later.
        if (!objects_on_top) drawObjects();

        // Applies the blur or sharpen convolution operation.
        // This acts in-place on the output buffer.
        blursharp.operate(blursharp_rate);

        // We shouldn't need to do this on every frame but
        // there is a bug somewhere. This ensures that all active
        // cursors are visible and inactive ones invisible.
        //control.syncCursors();
        // Don't call this! This can trigger the robot to move the mouse.
        // We are trying to ensure that all changes to the system mouse
        // are triggered immediately by user input, so the mouse-moving code
        // will get upset if we call it from the rendering loop.
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Double-buffered component painting //////////////////////////////////////
    
    /**
     * Draw using double buffering and render strategy. 
     */
    private void showOnScreen() {
        BufferStrategy bs = getBufferStrategy();
        Graphics screen = bs.getDrawGraphics();
        paint(screen);
        screen.dispose();
        bs.show();
    }
    
    /**
     * Send the rendering image in buf.out to the screen.
     * @param g 
     */
    public void paint(Graphics g) {
        if (g instanceof Graphics2D) g=fast((Graphics2D)g);
        Rectangle b = getPerceptBounds();
        if (null!=buf && null!=buf.out && null!=buf.out.img && null!=g)
            g.drawImage(buf.buf.img,b.x,b.y,b.width,b.height,null);
    }
    
    /**
     * Drawn area of Perceptron bounds as a Rectangle.
     * BUG TODO: drawn area is wrong in undecorated windows
     * @return 
     */
    public Rectangle getPerceptBounds() {
        Rectangle r;
        if (isFullscreenWindow(this)) {
            r = getGraphicsConfiguration().getBounds();
            r.setLocation(0,0);
            //serr("getPerceptBounds getGraphicsConfiguration getBounds "+r);
        } else {
            r = getRootPane().getBounds();
            //serr("getPerceptBounds getRootPane getBounds "+r);
        }
        int factor = (r.width>=2*screen_width && r.height>=2*screen_height)
                ? min(r.width /  screen_width,   r.height /  screen_height)
                : 1;
        int sw = screen_width *factor,
            sh = screen_height*factor;
        r.x     += (r.width - sw)/2;
        r.y     += (r.height- sh)/2;
        r.width  = sw;
        r.height = sh;
        //serr("getPerceptBounds "+r);
        return r;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Fullscreen control //////////////////////////////////////////////////////
    
    /**
     * Enter/exit full-screen mode.
     */
    public void toggleFullscreen() {
        boolean was_running = isRunning();
        running.set(false);
        synchronized (is_fullscreen) {
            GraphicsDevice gd = getScreen(this);
            boolean supported = gd.isFullScreenSupported();
            if (is_fullscreen.get()) { 
                sout("Exiting fullscreen...");
                if (supported) gd.setFullScreenWindow((Window)null);
                setFrame(this,true,false);
                is_fullscreen.set(false);
                this.repaint();
                if (null!=control.current) control.current.catchup(false);
            } else if (supported) {
                sout("Entering fullscreen...");
                setFrame(this,false,true);
                gd.setFullScreenWindow((Window)this);
                if (gd.isDisplayChangeSupported()) 
                    changeDisplayMode(this,screen_width,screen_height);
                is_fullscreen.set(true);
                this.repaint();
                if (null!=control.current) control.current.catchup(false);
            } else {
                serr("System doesn't support full-screen");
            }
        }
        running.set(was_running);
    }
    
    
    /**
     * Toggle window border in windowed mode.
     * BUG TODO: removing frame changes window size incorrectly
     */
    public void toggleFrame() {
        boolean was_running = isRunning();
        running.set(false);
        synchronized (is_fullscreen) {
            if (!is_fullscreen.get()&&!isFullscreenWindow(this)) {
                setFrame(this,isUndecorated(),true);
            }
        }
        running.set(was_running);
        this.requestFocus();
        if (null!=control.current) control.current.catchup(false);
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // Color transform /////////////////////////////////////////////////////////
    private void colorTransform(DataBuffer b) {
        hue_rate = wrap(hue_rate,256);
        sat_rate = clip(sat_rate,-256,256);
        lum_rate = clip(lum_rate,-256,256);
        bri_rate = clip(bri_rate,-256,256);
        con_rate = clip(con_rate,-256,256);
        float hr = (float)(hue_rate*2*Math.PI/256);
        float sr = (float)(pow(2.0,sat_rate/256f));
        float lr = (float)(pow(2.0,lum_rate/256f));
        float cr = (float)(pow(2.0,con_rate/256f));
        float br = (1+bri_rate/256f)/2;
        colorFilter(b,hr,sr,lr,cr,br);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    private float tree_spinner = 0f;
    private void drawObjects() {
        if (draw_tree) {
            tree_spinner = wrap(tree_spinner-0.01f,2f*(float)Math.PI);
            tree.form[0].setAlpha(tree_spinner);
            tree.form[1].setAlpha(tree_spinner+(float)Math.PI);
            tree.render();
        }
        text.renderTextBuffer(buf.out.g2D);
        if (draw_moths) {
            moths.step((float)(20.0/framerate));
        }
        if (draw_dino) {
            o.draw(buf.out.g, buf.out.img, screen_width/2, screen_height/2, 0);
            o.rotatex(0.01);
            o.rotatey(0.03);
            o.rotatez(0.05);
        }
        mic.render();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    public void poke() {
        boredom_time = currentTimeMillis()+boredome_ms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Save state and the screenshot,"save frame".
    public void save() {
        boolean was_running = running.get();
        running.set(false);
        
        makeNotFullscreen(this);
        saver.setVisible(true);
        saver.setFocusable(true);
        saver.requestFocus();
        
        int approved = saver.showSaveDialog(this);
        saver.grabFocus();
        File file = saver.getSelectedFile();
        sout("saving...");
        if (file != null && approved == JFileChooser.APPROVE_OPTION) {
            String ap = file.getAbsolutePath();
            try {
                write(buf.out.img,"png",new File(ap+".out.png"));
                write(buf.buf.img,"png",new File(ap+".in.png" ));
            } catch (IOException ex) {
                System.err.println("File write error while saving images.");
            }
            try {
                Preset.write(this,new File(ap+".state"));
            } catch (IOException ex) {
                System.err.println("File write error while saving state.");
            }
            sout("...saved.");
        } else sout("...did not save.");
        sout("Done.");
        makeFullscreen(this,screen_width,screen_height);
        setCursor(hide_mouse? NONE : CROSS);
        running.set(was_running);
    }

    ////////////////////////////////////////////////////////////////////////////
    void drawBars() {
        final int BAR_WIDTH = 8;
        // Bars accentuate the frame edges for a nice effect
        if (draw_top_bars) {
            buf.out.g.setColor(new Color(fractal.bar_color));
            buf.out.g.fillRect(0,0,screen_width,BAR_WIDTH);
            buf.out.g.fillRect(0,screen_height - BAR_WIDTH,screen_width,BAR_WIDTH);
        }
        if (draw_side_bars) {
            buf.out.g.setColor(new Color(fractal.bar_color));
            buf.out.g.fillRect(0,0,BAR_WIDTH,screen_height);
            buf.out.g.fillRect(screen_width-BAR_WIDTH,0,BAR_WIDTH,screen_height);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Text drawing routines
    public final Font  TEXTFONT = new Font(Font.MONOSPACED,Font.PLAIN,14);
    public final FontMetrics fm = getFontMetrics(TEXTFONT);
    public final int   LINEHEIGHT = 16;
    public final int   STATE_COLWIDTH = 275;
    public final int[] STATE_TABS = {0, 30, 150};
    
    private void text(Graphics2D g,String s,int x,int y,int a) {
        g.setFont(TEXTFONT);
        // Fill outline sides
        g.setColor(new Color(0,0,0,a));
        g.drawString(s,x-1,y); g.drawString(s,x+1,y);
        g.drawString(s,x,y-1); g.drawString(s,x,y+1);
        // Fill outline corners
        g.setColor(new Color(0,0,0,a>>2));
        g.drawString(s,x-1,y+1); g.drawString(s,x+1,y+1);
        g.drawString(s,x-1,y-1); g.drawString(s,x+1,y-1);
        g.drawString(s,x+1,y-1); g.drawString(s,x+1,y+1);
        g.drawString(s,x-1,y-1); g.drawString(s,x-1,y+1);
        g.setColor(new Color(0xff,0xff,0xff,a));
        g.drawString(s,x,y);
    }
    
    private void trtext(Graphics2D g,String s,int x,int y,int a) {
        Rectangle2D b = fm.getStringBounds(s,g);
        int w = (int)round(b.getWidth()), h = (int)round(b.getHeight());
        text(g,s,x-w,h+y,a);
    }
    
    private void rtext(Graphics2D g,String s,int x,int y,int a) {
        int w = (int)round(fm.getStringBounds(s,g).getWidth());
        text(g,s,x-w,y,a);
    }

    private void drawInfo() {
        Graphics2D g = buf.out.g;
        int dy = LINEHEIGHT;
        int  y = screen_height-4+dy;
        int  x = screen_width -4;
        rtext(g,framerate+"",x,y-=dy,info_α);
        switch (fractal.offset_mode) {
            case Map.LOCKED  :rtext(g,"(constant locked at 0)",x,y-=dy,info_α); break;
            case Map.POSITION:rtext(g,fractal.offset+" c"     ,x,y-=dy,info_α); break;
            case Map.VELOCITY:rtext(g,fractal.offset+" dc/dt" ,x,y-=dy,info_α); break;
        }
        switch (fractal.rotate_mode) {
            case Map.LOCKED  :rtext(g,"(rotation locked at 1)",x,y-=dy,info_α);break;
            case Map.POSITION:rtext(g,fractal.rotation.over(complex.I)+" ρ",x,y-=dy,info_α);break;
            case Map.VELOCITY:
                rtext(g,arg(fractal.rotation)*.01f+" dθ/dt",x,y-=dy,info_α);
                rtext(g,mod(fractal.rotation)+" r",x,y-=dy,info_α);
                break;
        }
        if (fractal.grad_mode>0) {
            rtext(g,fractal.gslope+" grad slope",x,y-=dy,info_α);
            rtext(g,fractal.gbias +" grad bias ",x,y-=dy,info_α);
        }
        if (draw_tree) {
            rtext(g,"x="+tree.location.x+" y="+tree.location.y+" root",x,y-= dy,info_α);
            rtext(g,tree.form[0].d_r +" Lρ",x,y-=dy,info_α);
            rtext(g,tree.form[1].d_r +" Rρ",x,y-=dy,info_α);
            rtext(g,tree.form[0].beta+" Lθ",x,y-=dy,info_α);
            rtext(g,tree.form[1].beta+" Rθ",x,y-=dy,info_α);
        }
    }
    
    private void drawState() {
        Graphics2D g = buf.out.g;
        String info = helpString(this);
        int y   = -2;
        int col = 0;
        for (var l : info.split("\n")) {
            l = l.strip();
            y += LINEHEIGHT;
            if (y>screen_height-1) {y = LINEHEIGHT-2; col += STATE_COLWIDTH;}
            int tab = 0;
            for (var s:l.split("@")) 
                text(g,s,2+col+STATE_TABS[tab++],y,state_α);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    final long SHOW_MS = 6000; // How long to show each notification in ms
    final long FADE_MS = 2000; // Fade in/out time in ms
    class Note {
        public final String s;
        public final long   t;
        public Note(String S) {s=S; t=currentTimeMillis();}}
    Deque<Note>  notifications = new ArrayDeque<>();
    public void notify(String s) {notifications.add(new Note(s));}
    Collection<String> past = null;
    void noticeChanges() {
        Collection<String> st = Arrays.asList(helpString(this).split("\n"));
        if (past!=null) zip(past,st,(s,S)->{if (!s.equals(S)) {
            String[] columns = S.split("@");
            notify(columns[1].replace('±',' ').strip()+" ⯇ "+columns[2].strip());
        }});
        past = st; 
        drawNotifications(buf.out.g);
    }
    void drawNotifications(final Graphics2D G) {
        G.setFont(TEXTFONT);
        long t = currentTimeMillis();
        final Set<Note> remove = new HashSet<>();
        int y = 4;
        final Deque<Note> notes = new ArrayDeque<>(notifications);
        for (var n : notes) {
            try{
                long since = t-n.t;
                if (since>SHOW_MS) {remove.add(n); continue;}
                int α = 
                    (since<=255)? (int)(since) :
                    (since>SHOW_MS-FADE_MS)? (int)((SHOW_MS-since)*255.0/FADE_MS+0.5) :
                    255;
                trtext(G,n.s,screen_width-4,y,α*note_α+127>>8);
                y+= LINEHEIGHT;
            } catch (Exception e) {
                System.err.println("Error in drawNotifications "+n.s);
                e.printStackTrace();
            }
        };
        notifications.removeAll(remove);
        // Make room for the montior information, which has up to 11 lines
        int monitor_height = show_monitor? LINEHEIGHT*11 : 0; 
        int max_notify = (screen_height-monitor_height-2)/LINEHEIGHT;    
        while (notifications.size()>max_notify) notifications.pop();
    }
    
    
    public void textToMap() {
        fractal.setMap(text.get());
    }
}
