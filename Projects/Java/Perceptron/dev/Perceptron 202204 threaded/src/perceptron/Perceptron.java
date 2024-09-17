package perceptron;
/* Perceptron.java
 * Created on December 21,2006,5:27 PM
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
import static perceptron.Settings.helpString;
import static perceptron.Map.Mapping;
import static perceptron.Parse.parseSettings;
import static color.ColorUtil.colorFilter;
import static color.ColorUtil.fast;
import java.awt.BasicStroke;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import util.CaptureRegion;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newFixedThreadPool;
import java.util.concurrent.Future;
import javax.swing.ImageIcon;
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
import util.ScreenCap;
import static util.Sys.centerWindow;
import static util.Sys.serr;
import static util.Sys.sout;

/**
 * @author mer49
 */
public final class Perceptron extends javax.swing.JFrame {
    
    ////////////////////////////////////////////////////////////////////////////
    // Create two threads for running two things at once
    private final ExecutorService executor = newFixedThreadPool(2);
    
    ////////////////////////////////////////////////////////////////////////////
    final Control       control;
    final Map           map;
    final DoubleBuffer  buf;
 
    ArrayList<Mapping>  maps;
    Settings[]          presets;
    ImageCache          images = null;
    final JFileChooser  saver;
    final Tree3D        tree;
    final TextMatrix    text;
    final BlurSharpen   blursharp;
    final Moths         moths;
    final Microphone    mic;
    final ScreenCap     cap;
    final CaptureRegion big;
    
    int image_i = 0;
    public String imdir = "resource/images/";
        
    ////////////////////////////////////////////////////////////////////////////
    // modifying these after initialization may cause undefined behavior.
    int half_screen_w;
    int half_screen_h;
    public int image_rotate_ms   = 5000;
    public int boredome_ms       = 100000;
    public int preset_rotate_ms  = 500000;
    public int screen_timeout_ms = 60000;
    public int max_frametime     = 1000/20;
    public int audio_line        = -1;
    public int min_tree_depth    = 9;
    public int max_tree_depth    = 6;
    public int screen_width      = 480;
    public int screen_height     = 480;
    
    ////////////////////////////////////////////////////////////////////////////
    // Render control flags: all public, on the honor system. 
    public boolean objects_on_top     = true;
    public boolean text_on_top        = true;
    public boolean capture_text       = true;
    public boolean capture_cursors    = true;
    public boolean cap_frame_rate     = true;
    public boolean write_animation    = false;
    public boolean rotate_images      = false;
    public boolean fore_tint          = false;
    public boolean draw_moths         = false; 
    public boolean draw_top_bars      = false;
    public boolean draw_side_bars     = false;
    public boolean draw_tree          = false;
    public boolean draw_dino          = false;
    public boolean draw_grid          = false;
    public boolean do_color_transform = true;
    public boolean hide_mouse         = false;
    public boolean capture_screen     = false;
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
    private int state_α = 255;
    private int info_α  = 255;
    private int note_α  = 255;
    
    ////////////////////////////////////////////////////////////////////////////
    // Try to pull in the object data from disk (static)
    final String OBJFILE = "resource/data/tetrahedron.txt";
    public Object3D o = null;
    public void reload3DModel() {
        //Object3D load3DModel(String filename) {
        try {
            Object3D o2 = new Object3D(new BufferedReader(
                    new FileReader(new File(OBJFILE))));
            o2.recenter(100);
            this.o = o2;
        } catch (FileNotFoundException E) {
            sout("Could not load file "+OBJFILE);
            this.o = null;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters (boilerplate!) //////////////////////////////////////
    public boolean isAntialiased()        {return buf.antialiased; }
    public void setAntialias(boolean s)   {if (s!=isAntialiased()) toggleAntialias();}
    public void setObjectsOnTop(boolean b){objects_on_top = b; }
    public void setTextOnTop(boolean b)   {text_on_top = b; }
    public void setHideMouse(boolean b)   {hide_mouse=b;setCursor(b?NONE:CROSS);}
    public void setBlurWeight(int k)      {blursharp_rate = clip(k,-256,256);}
    public void toggleAntialias()         {buf.toggleAntialias(); }
    public void toggleObjectsOnTop()      {objects_on_top  = !objects_on_top;  }
    public void toggleTextOnTop()         {text_on_top     = !text_on_top;     }
    public void toggleCaptureText()       {capture_text    = !capture_text;    }
    public void toggleCaptureCursors()    {capture_cursors = !capture_cursors; }
    public void toggleCapFramerate()      {cap_frame_rate  = !cap_frame_rate;  }
    public void toggleAnimation()         {write_animation = !write_animation; }
    public void toggleTree()              {draw_tree       = !draw_tree;       }
    public void toggleGrid()              {draw_grid       = !draw_grid;       }
    public void toggleShowNotices()       {show_notices    = !show_notices;    }
    public void toggleShowHelp()          {show_state      = !show_state;      }
    public void toggleShowFramerate()     {show_monitor    = !show_monitor;    }
    public void toggleHideMouse()         {setHideMouse(!hide_mouse);}
    public int  screenWidth()             {return screen_width;    }
    public int  screenHeight()            {return screen_height;   }
    public int  halfScreenWidth()         {return half_screen_w;   }
    public int  halfScreenHeight()        {return half_screen_h;   }

    public void setImage(int n) {
        try {
            image_i = n;
            buf.set(images.get(n));
            last_image_time = 30000+currentTimeMillis();
            map.cache.map_stale.set(true);
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
        map.cache.map_stale.set(true);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param settings_filename
     * @param crash_log_filename
     * @param presets_filename 
     */
    @SuppressWarnings("LeakingThisInConstructor") // LOL
    public Perceptron(
            String settings_filename,
            String crash_log_filename,
            String presets_filename) {
        
        super("Perceptron (threaded)"); 

        // Parse the settings to retrieve the window size
        // parseSettings modifies the screen_width and _height variables
        maps = new ArrayList<>();
        parseSettings(this,settings_filename,presets_filename); // apply config
        sout("Parsed screen_width="+screen_width+"; screen_height="+screen_height);
        half_screen_w = (short)(screen_width /2);
        half_screen_h = (short)(screen_height/2);
        
        // Tidy up parent container and parent window
        Component root = getRootPane();
        Component cont = getContentPane();
        this.setBackground(BLACK);
        root.setBackground(BLACK);
        cont.setBackground(BLACK);
        
        // Force component and window to match specified resolution
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Exit on window close
        setIgnoreRepaint(true); // ignore OS prompts for redrawing
        setResizable(false); // we turn this on again later
        Dimension dims = new Dimension(screen_width,screen_height);
        cont.setPreferredSize(dims);
        root.setPreferredSize(dims);
        cont.setMinimumSize(dims);
        root.setMinimumSize(dims);
        
        // Note that parseSettings() must have been called in order for
        // screen_height and screen_width to be available.
        // buf: multi-buffer object for rendering
        // blursharp: convolution operators attached to our buffers
        buf       = new DoubleBuffer(screen_width,screen_height);
        blursharp = new BlurSharpen(buf);
        imdir     = requireNonNull(imdir);
        images    = new ImageCache(imdir);
        map   = new Map(buf,maps,this);
        moths     = new Moths(screen_width,screen_height);
        mic       = new Microphone(buf, 0);  //mic.start();
        cap       = new ScreenCap();
        big       = new CaptureRegion(screen_width,screen_height);
        text      = new TextMatrix(screen_width,screen_height);  
        text.loadString(crash_log_filename);  
        
        // Load model file
        reload3DModel();
        
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

        // Make the saver window and remember its size
        saver = new JFileChooser("Save State");
        saver.setPreferredSize(new Dimension(800,600));
        
        // To avoid alarming the user, start in windowed mode (NOT full screen)
        setIconImage(new ImageIcon("resource/data/icon2.png").getImage());
        setMinimumSize(new Dimension(screen_width, getHeight()));
        setResizable(false);
        pack();
        centerWindow(this);
        setVisible(true);
        
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
    // Locks
    final AtomicBoolean is_fullscreen = new AtomicBoolean(false);
    final AtomicBoolean running       = new AtomicBoolean(false);
    public final boolean isFullscreen()  {return is_fullscreen.get();}
    public final boolean isRunning()     {return running.get();}
    public final void    toggleRunning() {running.set(!running.get());}
    
    long cache_time = 0, render_time = 0;
    public void go() {
        sout("Starting...");
        
        // Start timers
        // NB: `boredom_time` is not currently used, but it was previouly 
        // confifgured to activate an automated screensaver mode if users
        // had not interacted in a while. 
        long time = currentTimeMillis();
        frame           = 0;
        last_frame_time = time;
        last_image_time = 20000+time;
        boredom_time    = 50000+time;
        
        // Start
        sout("Entering Kernel...");
        running.set(true);
        map.cache.map_stale.set(true);
        while (true) {
            if (!running.get()) {
                try {Thread.sleep(50);} catch (InterruptedException ex) {}
                continue;
            }
            try {
                long frame_start = currentTimeMillis();

                // Handle screen capture if active
                if (isFullscreen()) {
                    // hide screencap tools if full screen and not multi-monitor
                    int nscreens = getLocalGraphicsEnvironment().getScreenDevices().length;
                    if (nscreens<=1) {
                        big.watcher.setVisible(false);
                        big.selector.setVisible(false);
                    }
                } else {
                    if (capture_screen) {
                        // We'll show these only on toggle using the Control class
                    } else {
                        // Capture off, hide the capture tools
                        big.watcher.setVisible(false);
                        big.selector.setVisible(false);
                    }
                }
                if (capture_screen) {
                    cap.screenRect.setRect(big.getBounds());
                    buf.set(cap.getScreenshot());
                    map.cache.map_stale.set(true);
                }

                // Run the (possibly multi-threaded) rendering loop
                // and caching loop in parallel. The caching loop monitors
                // for state changes and recomputes auxilary buffers as needed,
                // like the mapping lookup table or translucent gradient. 
                synchronized (is_fullscreen) {
                    Future<Long> runner = executor.submit(()->{
                        long start = currentTimeMillis();
                        doFrame(); // Rendering business in here
                        return currentTimeMillis()-start;
                    });
                    Future<Long> cacher = executor.submit(()->{
                        long start = currentTimeMillis();
                        map.cache.cache();
                        return currentTimeMillis()-start;
                    });
                    render_time = runner.get();
                    cache_time  = cacher.get();
                    map.cache.flip();
                }

                // Frame counters and framerate monitoring
                frame++;
                time = currentTimeMillis();
                if (time - last_frame_time >= 1000) {
                    last_frame_time = time;
                    framerate       = frame;
                    frame           = 0;
                }
                if (cap_frame_rate) {
                    long d = frame_start + max_frametime - currentTimeMillis();
                    if (d>0) Thread.sleep(d);
                }
            } catch (Exception e) {
                serr("Error in go!"); e.printStackTrace();
            }
        }
    }
        
    /** Render a single frame.
     * This code expects the "output" buffer to be ready for the next frame.
     * It touches both the `output` and `buffer` buffers. 
     */
    private void doFrame() {
        // Apply the fractal mapping. This draws into the "buf" buffer
        map.operate();
        // Exchange "out" and "buf" data buffers
        buf.flip();
        
        // Draw tree, moths, dinosaur, audio visualizer in `buf.out`.
        // and text overlay. These functions/objects have their own mutable
        // state which determines whether they actually draw anything. 
        if (objects_on_top) drawObjects();
        if (capture_text && text_on_top) text.renderTextBuffer(buf.out.g2D);
        
        // Advance the cursor states then render cursors to output
        control.advance((int)framerate);
        if (capture_cursors) control.drawAll(buf.out.g);
        
        // Update state machine for changing the translucency of the text
        // overlays (fading in/out); Renders to `buf.out`.
        if (capture_text) drawTextInfoOverlays(buf.out.g2D);
        
        // Save frame to disk; This captures the `buf.out` buffer. 
        if (write_animation) try {
            String filename = "animate/frame "+animation_frame+".png";
            animation_frame++;
            File file = new File(filename);
            ImageIO.write(buf.out.img,"png",file);
        } catch (IOException ex) {
            System.err.println("File write error in animation.");
            ex.printStackTrace();
        }
        // Store rendered frame in `buf.buf`;
        // Motion blur will use this in the next frame.
        buf.buf.img.getRaster().setDataElements(0,0,buf.out.img.getRaster());
        
        // Render any post-processing effects that are overlay-only
        // and not fed back into the video feedback. 
        buf.dsp.img.getRaster().setDataElements(0,0,buf.out.img.getRaster());
        if (!capture_text) {
            text.renderTextBuffer(buf.dsp.g2D);
            drawTextInfoOverlays(buf.dsp.g2D);
        }
        if (!capture_cursors) control.drawAll(buf.dsp.g);
        
        // Blit `buf.dsp.img` to screen with double buffering.
        BufferStrategy bs = getBufferStrategy();
        Graphics screen   = bs.getDrawGraphics();
        paint(screen); screen.dispose();
        bs.show();
        
        // Update fractal's color state registers
        map.updateColorRegisters(buf.out.buf);
        
        // Apply background-only effects and drawing.
        // All of these operate on `buf.out`.
        // You can see these objects through the map in the next frame, 
        // but won't see them drawn in screen coordinates on the current frame.
        if (do_color_transform) colorTransform(buf.out.buf); // in-place on `buf.out` 
        drawBars();
        if (!objects_on_top) drawObjects(); // to `buf.out`
        if (capture_text && !text_on_top) text.renderTextBuffer(buf.out.g2D);
        // Apply blur/sharpen convolution in-place on the output buffer.
        blursharp.operate(blursharp_rate);
    }
    
    private void drawTextInfoOverlays(final Graphics2D g) {
        if ((state_α = fadeout(state_α,show_state  ))>0) drawState(g);
        if ((info_α  = fadeout(info_α ,show_monitor))>0) drawInfo(g);
        if ((note_α  = fadeout(note_α ,show_notices))>0) noticeChanges(g);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Send the rendering image in buf.out to the screen.
     * @param g 
     */
    public void paint(Graphics g) {
        if (g instanceof Graphics2D graphics2D) g=fast(graphics2D);
        Rectangle b = getPerceptBounds();
        if (null!=buf && null!=buf.dsp && null!=buf.dsp.img && null!=g)
            g.drawImage(buf.dsp.img,b.x,b.y,b.width,b.height,null);
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
        } else 
            r = getRootPane().getBounds();
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
     * Enter or exit full-screen mode.
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
            } else serr("System doesn't support full-screen");
        }
        running.set(was_running);
    }
    
    
    /**
     * Toggle window border in windowed mode.
     * TODO: removing frame changes window size incorrectly
     */
    public void toggleFrame() {
        boolean was_running = isRunning();
        running.set(false);
        synchronized (is_fullscreen) {
            if (!is_fullscreen.get()&&!isFullscreenWindow(this)) 
                setFrame(this,isUndecorated(),true);
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
        int N = b.getSize();
        Future t1 = executor.submit(()->{colorFilter(b,0,N/2,hr,sr,lr,cr,br);});
        Future t2 = executor.submit(()->{colorFilter(b,N/2,N,hr,sr,lr,cr,br);});
        try {
            t1.get();
            t2.get();
        } catch (InterruptedException | ExecutionException ex) {
            colorFilter(b,0,b.getSize(),hr,sr,lr,cr,br); 
        }
    }
    
    /** Draw tree, moths, dinosaur, audio visualizer. */
    private float tree_spinner = 0f;
    private final BasicStroke dashed = new BasicStroke(0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1.0f, new float[]{1.0f}, 0.0f);
    private final BasicStroke solid  = new BasicStroke(0.5f);
    private void drawObjects() {
        if (draw_tree) {
            tree_spinner = wrap(tree_spinner-0.01f,2f*(float)Math.PI);
            tree.form[0].setAlpha(tree_spinner);
            tree.form[1].setAlpha(tree_spinner+(float)Math.PI);
            tree.render();
        }
        if (draw_moths) {
            moths.step((float)(20.0/framerate));
            moths.paint(buf.out.g);
        }
        if (draw_dino) {
            o.draw(buf.out.g, buf.out.img, screen_width/2, screen_height/2, 0);
            o.rotatex(0.01);
            o.rotatey(0.03);
            o.rotatez(0.05);
        }
        mic.render();
        
        if (draw_grid) drawGrid();
    }
    
    private void drawGrid() {
        Graphics2D g = buf.out.g2D;
        float sw = map.size.real;
        float sh = map.size.imag;
        int rx = (int) (this.screen_width/sw*1.0f);
        int ry = (int) (this.screen_height/sh*1.0f);
        g.setStroke(new BasicStroke(2));
        g.setColor(new Color(map.bar_color));
        g.drawOval(half_screen_w-rx, half_screen_h-ry, 2*rx, 2*ry);
        g.drawLine(0, half_screen_h, screen_width, half_screen_h);
        g.drawLine(half_screen_w, 0, half_screen_w, screen_height);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,24)); 
        g.drawString("ℜ", screen_width-30, half_screen_h-5);
        g.drawString("ℑ", half_screen_w+5, 30);
        int ix, iy;
        for (int i=-6; i<=6; i++) {
            for (int j=-6; j<= 6; j++) {
                iy = (int) (this.screen_height/sh*i*0.5f + this.half_screen_h);
                ix = (int) (this.screen_width /sw*j*0.5f + this.half_screen_w);
                g.fillOval(ix-3, iy-3,6,6);
                //this.text(g, String.format("%s+%si",j*.5,i*.5), ix, iy, 255);
            }
        }
        g.setStroke(dashed);
        ix = (int) (this.screen_width /sw*0*0.5f + this.half_screen_w);
        for (int i=-6; i<=6; i++) if (i!=0) {
            iy = (int) (this.screen_height/sh*i*0.5f + this.half_screen_h);
            buf.out.g.setColor(new Color(map.bar_color));
            g.drawLine(0,iy,screen_width,iy);
            this.text(g, String.format("%si",i*.5), ix, iy, 255);
        }
        iy = (int) (this.screen_height/sh*0*0.5f + this.half_screen_h);
        for (int j=-6; j<=6; j++) if (j!=0)  {
            ix = (int) (this.screen_width /sw*j*0.5f + this.half_screen_w);
            g.setColor(new Color(map.bar_color));
            g.drawLine(ix,0,ix,screen_height);
            this.text(g, String.format("%s",j*.5), ix, iy, 255);
        }
        g.setStroke(solid);
    }
    
    /** Register that the user has interacted and defer boredom timeout.
     */
    public void poke() {
        boredom_time = currentTimeMillis()+boredome_ms;
    }

    /** Save state and the screenshot, "save frame".
     */
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
                serr("File write error while saving images.");
            }
            try {
                Settings.write(this,new File(ap+".state"));
            } catch (IOException ex) {
                serr("File write error while saving state.");
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
        buf.out.g.setColor(new Color(map.bar_color));
        if (draw_top_bars) {
            buf.out.g.fillRect(0,0,screen_width,BAR_WIDTH);
            buf.out.g.fillRect(0,screen_height - BAR_WIDTH,screen_width,BAR_WIDTH);
        }
        if (draw_side_bars) {
            buf.out.g.fillRect(0,0,BAR_WIDTH,screen_height);
            buf.out.g.fillRect(screen_width-BAR_WIDTH,0,BAR_WIDTH,screen_height);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Text drawing routines
    
    // Larger font
    //public final Font  TEXTFONT = new Font(Font.MONOSPACED,Font.PLAIN,14);
    //public final FontMetrics fm = getFontMetrics(TEXTFONT);
    //public final int   LINEHEIGHT = 16;
    //public final int   STATE_COLWIDTH = 275;
    //public final int[] STATE_TABS = {0, 30, 150};
    // Smaller font
    public Font  TEXTFONT = new Font(Font.MONOSPACED,Font.PLAIN,10);
    public FontMetrics fm = getFontMetrics(TEXTFONT);
    public int   LINEHEIGHT = 11;
    public int   STATE_COLWIDTH = 210;
    public int[] STATE_TABS = {0, 25, 125};
    
    // Generic text drawing routine, used for help, status, and notifications.
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
    
    // Draws system state in the form of a help menu overlay. 
    private void drawState(final Graphics2D g) {
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
    
    /**
     * Draws status information on lower-right of screen (if active)
     */
    private void drawInfo(final Graphics2D g) {
        int dy = LINEHEIGHT;
        int  y = screen_height-4+dy;
        int  x = screen_width -4;
        rtext(g,framerate+"",x,y-=dy,info_α);
        rtext(g,render_time+" ms render time",x,y-=dy,info_α);
        rtext(g,cache_time+" ms cache time",x,y-=dy,info_α);
        switch (map.offset_mode) {
            case Map.LOCKED   -> rtext(g,"(constant locked at 0)",x,y-=dy,info_α);
            case Map.POSITION -> rtext(g,map.offset+" c"     ,x,y-=dy,info_α);
            case Map.VELOCITY -> rtext(g,map.offset+" dc/dt" ,x,y-=dy,info_α);
        }
        switch (map.rotate_mode) {
            case Map.LOCKED -> rtext(g,"(rotation locked at 1)",x,y-=dy,info_α);
            case Map.POSITION -> rtext(g,map.rotation.over(complex.I)+" ρ",x,y-=dy,info_α);
            case Map.VELOCITY -> {
                rtext(g,arg(map.rotation)*.01f+" dθ/dt",x,y-=dy,info_α);
                rtext(g,mod(map.rotation)+" r",x,y-=dy,info_α);
            }

        }
        if (map.grad_mode>0) {
            rtext(g,map.gslope+" grad slope",x,y-=dy,info_α);
            rtext(g,map.gbias +" grad bias ",x,y-=dy,info_α);
        }
        if (draw_tree) {
            rtext(g,"x="+tree.location.x+" y="+tree.location.y+" root",x,y-= dy,info_α);
            rtext(g,tree.form[0].d_r +" Lρ",x,y-=dy,info_α);
            rtext(g,tree.form[1].d_r +" Rρ",x,y-=dy,info_α);
            rtext(g,tree.form[0].beta+" Lθ",x,y-=dy,info_α);
            rtext(g,tree.form[1].beta+" Rθ",x,y-=dy,info_α);
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
    void noticeChanges(final Graphics2D g) {
        Collection<String> st = Arrays.asList(helpString(this).split("\n"));
        if (past!=null) zip(past,st,(s,S)->{if (!s.equals(S)) {
            String[] columns = S.split("@");
            notify(columns[1].replace('±',' ').strip()+" ⯇ "+columns[2].strip());
        }});
        past = st; 
        drawNotifications(g);
    }
    void drawNotifications(final Graphics2D G) {
        G.setFont(TEXTFONT);
        long  t = currentTimeMillis();
        final Set<Note> remove = new HashSet<>();
        int   y = 4;
        for (var n : notifications) try {
            long since = t-n.t;
            if (since>SHOW_MS) {remove.add(n); continue;} //remove
            int α = 
                (since<=255)? (int)(since) :
                (since>SHOW_MS-FADE_MS)? (int)((SHOW_MS-since)*255.0/FADE_MS+0.5) :
                255;
            trtext(G,n.s,screen_width-4,y,α*note_α+127>>8); // draw
            y+= LINEHEIGHT;
        } catch (Exception e) {
            System.err.println("Error in drawNotifications "+n.s);
            e.printStackTrace();
        }
        notifications.removeAll(remove);
        // Make room for the montior information, which has up to 11 lines
        int monitor_height = show_monitor? LINEHEIGHT*13 : 0; 
        int max_notify = (screen_height-monitor_height-2)/LINEHEIGHT;    
        while (notifications.size()>max_notify) notifications.pop();
    }
    
    public void textToMap() {
        map.setMap(text.get());
    }
}


/*
This is, er, was, called perceptron. 
It's a video feedback program from 2006. 
That's seventeen years ago. 

It's written in java. It's sort of an overgrown math homework assignment, 
crossed with the music visualizers in the aughties, in iTunes and milkdrop. 

With video feedback, 
You can render interactive fractals without too much processing power. 
The algorithm is simple. 

You know that infinite mirror effect when you screen share the screen share
on zoom? That's all this is. 
To render each new frame, we copy pixels from the pervious frame. 

For example, if we pull pixels from a location scaled further away, this 
will lead to a shrunk down image on the next iteration. 
This generates the familiar infinity mirror effect.
We used to be able to send in video using the webcam, but this is running on 
linux now, and I think Java Media Framework hasn't been supported in almost
a decade? 

But you can do a lot by just changing the mapping function used to pull 
pixels from the previous frame. 

This map is calculating the Julia, or should I say Fatou set, map, z^2 + c.
It's interpreting the horizontal and vertical parts of the screen as the
real and imaginary parts of a complex number, respectively.
This little eyeball cursor is controlling that "c" constant offset. 

So, this gives you a way of rendering some interactive fractals, wher the 
computational complexity per frame is closer to that of one iteration, rather
than needed to recalculate all iterations to generate the next frame. 

This thing is written in ordinary Java, so back in 2006 it needed all the
efficiency it could get. There were some extra rendering hacks in the original,
but what you see now is close to how it looked on th efirst day. 
The pixelation has its charm, I suppse.

Computers are faster now, so it can do more, but still isn't hardware accelerated. 
Simple settings render up to 60 FPS at 1080P, but usually it's slower. 
At low-resolution it can sustain 20 FPS reliably. 
It's ok, it's retro now. We'll say this is aesthetic.

People liked it at the time, but some of those people were on drugs so perhaps
we should take that with a grain of salt.

It's a bit finnicky to control the software to render what you want. 
We ended up programming lots of presets in. 

There are a bunch of built in (simple) filters now. Motion blur. Sharpen. Tint.
Drawing some retro little graphics. The precise contents have fluctuated over
the years. 

It has a slightly broken equation parser written by a less experienced student, 
and other quirks. 
The code has sedimentary layers. 
The oldest I can find dates to 05 July, 2004, so nearly 20 years ago. 


Let's take a tour of some other things we can see. 


& Basic infinity mirror retract
1

2 + ctl g
J
V
?
[
\ colored cosine rope 
=  corny tree
! Stellar cathedral
^ Follow up with rock cathedral
( garrish cathedeal 
@ log-cloverleaf zoom
# log-tiled zoom
: Better zoom 
% Mobius? 
{ oh thre's a dinosaur here


There used to be a way to use this like a music visualizer, but that han't
worked in over a decade. 

You can sorta get video via screen capture using
the Java Robot class. It almost works.


Solve 
z^3 - 1 = 0
by Newton's method.
z <- z - (z^3 - 1)/(3z^2)
z <- (3z^2 - z^3 + 1)/(3z)


z <- z - (z^3 - 2z + 2)/(3z^2 - 2)


*/