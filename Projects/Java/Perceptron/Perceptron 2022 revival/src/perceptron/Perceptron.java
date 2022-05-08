package perceptron;
/* Perceptron.java
 * Created on December 21,2006,5:27 PM
 */

import rendered.TextMatrix;
import image.BlurSharpen;
import image.DoubleBuffer;
import util.Misc;
import rendered.Microphone;
import static java.lang.Math.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferStrategy;
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
import java.awt.Cursor;
import rendered.Object3D;
import rendered.TreeForm;
import rendered.Tree3D;
import rendered.Moths;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.Collection;
import static java.util.Collections.sort;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;

import static util.Misc.clip;
import static util.Misc.zip;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import math.complex;
import static math.complex.arg;
import static math.complex.mod;
import static util.Misc.wrap;
import static perceptron.Preset.helpString;
import static util.ColorUtil.makeHueSatLumaOperator;
import static util.Matrix.diag;
import static util.Matrix.multiply;
import static perceptron.Fractal.Mapping;
import util.Win;
import static util.Win.makeFullscreen;
import static util.Win.makeUndecoratedMainFrame;
import static math.complex.polar;

/**
 * @author mer49
 */
public final class Perceptron extends javax.swing.JFrame {
    
    ////////////////////////////////////////////////////////////////////////////
    public final DoubleBuffer buf;
    BufferStrategy            bufferStrategy;
    Graphics2D                graph2D;
    ArrayList<Mapping>        maps;
    Preset[]                  presets;
    ImageCache                images = null;
    JFileChooser              savers;
    final Control             control;
    final Fractal          fractal;
    final Tree3D              tree;
    final TextMatrix          text;
    final BlurSharpen         blursharp;
    final Moths               moths;
    final Microphone          mic;
    
    boolean running;
    int help_alpha = 255;
    int image_i = 0;
    public String image_directory = "resource/images/";
    
    ////////////////////////////////////////////////////////////////////////////
    // Timers and counters
    // last_image_time: Timer (m) for rotating input images
    // boredom_time: Time (ms) sine last user action
    // frame: total frame counter
    // animation_frame: frame counter when saving animations
    long last_image_time = System.currentTimeMillis();
    long boredom_time    = System.currentTimeMillis();
    long frame           = 0;
    long animation_frame = 0;
        
    ////////////////////////////////////////////////////////////////////////////
    // modifying these after initialization may cause undefined behavior.
    int display_w;
    int display_h; 
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
    // Render control flags
    public boolean objects_on_top     = true;
    public boolean cap_frame_rate     = true;
    public boolean write_animation    = false;
    public boolean show_help          = false;
    public boolean rotate_images      = false;
    public boolean fore_grad          = false;
    public boolean draw_moths         = false; 
    public boolean draw_top_bars      = false;
    public boolean draw_side_bars     = false;
    public boolean show_framerate     = false;
    public boolean draw_tree          = false;
    public boolean draw_dino          = false;
    public boolean show_notifications = true;
    public boolean do_color_transform = true;
    public int     hue_rate           = 0;
    public int     sat_rate           = 0;
    public int     lum_rate           = 0;
    public int     bri_rate           = 0;
    public int     con_rate           = 0;
    
    JFileChooser saver;
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Try to pull in the object data from disk (static)
    final Object3D o = fetch_3d_model_data();
    static Object3D fetch_3d_model_data() {
        try {
            return (new Object3D(new BufferedReader(
                new FileReader(new File("resource/data/tetrahedron.txt")))));
        } catch (FileNotFoundException E) {
            System.out.println("Could not load file resource/data/tetrahedron.txt");
        }
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters (boilerplate!) //////////////////////////////////////
    public int     screenWidth()             {return screen_width; }
    public int     screenHeight()            {return screen_height; }
    public int     displayWidth()            {return display_w; }
    public int     displayHeight()           {return display_h; }
    public int     halfScreenWidth()         {return half_screen_w; }
    public int     halfScreenHeight()        {return half_screen_h; }
    public boolean isAntialiased()           {return buf.antialiased; }
    public void    setObjectsOnTop(boolean b){objects_on_top = b; }
    public void    setAntialias(boolean s)   {if (s!=isAntialiased()) toggleAntialias();}
    public void    toggleAntialias()         {buf.toggleAntialias(); }
    public void    toggleObjectsOnTop()      {objects_on_top = !objects_on_top; }
    public void    toggleCapFramerate()      {cap_frame_rate = !cap_frame_rate; }
    public void    toggleAnimation()         {write_animation = !write_animation; }
    public void    toggleShowHelp()          {show_help = !show_help; }
    public void    toggleTree()              {draw_tree = !draw_tree; }
    public void    toggleShowFramerate()     {show_framerate = !show_framerate; }

    public void setImage(int n) {
        try {
            image_i = n;
            buf.set(images.get(n));
            last_image_time = 30000+System.currentTimeMillis();
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
        last_image_time = image_rotate_ms+System.currentTimeMillis();
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
        super("Perceptron",Win.getDeviceGraphicsConfig());
        this.setBackground(Color.black);

        // Make the saver window and remember its (smaller) size)
        saver = new JFileChooser("Save State");
        saver.setBounds(0,0,this.display_w,this.display_h);

        // We must parse the settings file before going to full-screen,
        // because we need to know the desired canvas size to choose a
        // screen resolution.
        maps = new ArrayList<>();
        parseSettings(settings_filename,presets_filename);  //READ IN SETTINGS
        
        // Make us full-screen
        makeUndecoratedMainFrame(this); //FRAME SETUP
        makeFullscreen(this);           //FULLSCREEN INITIALISATION
        display_w     = getWidth();
        display_h     = getHeight();
        half_screen_w = (short) (screen_width  / 2);
        half_screen_h = (short) (screen_height / 2);
        System.out.println("buffer width : "+screen_width+" height : "+screen_height);
        System.out.println("screen width : "+display_w+" height : "+display_h);
        
        // screen_height and screen_width are only initialized AFTER parsing
        // the settings file. 
        // Initialise the frame rendering,background,and display buffers
        buf             = new DoubleBuffer(screen_width,screen_height);
        blursharp       = new BlurSharpen(buf);
        text            = new TextMatrix(screen_width,screen_height);  
        text.loadString(crash_log_filename);  
        image_directory = requireNonNull(image_directory);
        images          = new ImageCache(image_directory);
        fractal         = new Fractal(buf,maps,this);
        moths           = new Moths(screen_width,screen_height);
        mic             = new Microphone(buf, 0); 
        //mic.start();
        
        tree = new Tree3D(
            min_tree_depth,
            max_tree_depth,
            new float[][]{{0,0,0},{0,(float) (-(screen_height / 10)),0}},0,
            new TreeForm[]{new TreeForm(.5f,-.2f,.7f,7),new TreeForm(.5f,.2f,.7f,-7)},
            new Point(half_screen_w,half_screen_h),buf);
        
        control = new Control(this,presets);
        this.addMouseListener(control);
        this.addMouseMotionListener(control);
        this.addKeyListener(control);
        this.setFocusTraversalKeysEnabled(false); // don't consume VK_TAB
    }
    

    
    ////////////////////////////////////////////////////////////////////////////
    // Color transform /////////////////////////////////////////////////////////
    public int blursharp_rate = 0;
    void setColorFilterWeight(int k) {this.blursharp_rate = clip(k,-256,256);}
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
        float gamma;
        float beta;
        if (br<=0.5) {
            gamma = cr*br*2;
            beta  = br*(1-cr);
        } else {
            gamma = 2*cr*(1-br);
            beta = br - cr+br*cr;
        }
        beta *= 255;
        float [] D = {beta,beta,beta};
        float [][] H = makeHueSatLumaOperator(hr,sr,lr);
        float [][] B = diag(gamma,gamma,gamma);
        H = multiply(B,H);
        int beta8 = (int)(beta*256+0.5);
        //int [] D8 = {beta8,beta8,beta8};
        //int [][] H8 = new int[3][3];
        //for (int r=0;r<3;r++) for (int c=0;c<3;c++) 
        //    System.out.println(H8[r][c] = (int)(H[r][c]*256+0.5));
        //System.out.println(beta8+"--------");
        int M00 = (int)(H[0][0]*256+0.5);
        int M01 = (int)(H[0][1]*256+0.5);
        int M02 = (int)(H[0][2]*256+0.5);
        int M10 = (int)(H[1][0]*256+0.5);
        int M11 = (int)(H[1][1]*256+0.5);
        int M12 = (int)(H[1][2]*256+0.5);
        int M20 = (int)(H[2][0]*256+0.5);
        int M21 = (int)(H[2][1]*256+0.5);
        int M22 = (int)(H[2][2]*256+0.5);
        for (int i=0; i<fractal.MLEN; i++) {
            //b.setElem(i,applyColorAffine8bit(H8,D8,b.getElem(i)));
            //b.setElem(i,applyColorAffine(H,D,b.getElem(i)));
            int c = b.getElem(i);
            int r0 = (c & 0xff0000) >> 16;
            int g0 = (c & 0xff00) >> 8;
            int b0 = (c & 0xff);
            int r1 = M00*r0+M01*g0+M02*b0+beta8;
            int g1 = M10*r0+M11*g0+M12*b0+beta8;
            int b1 = M20*r0+M21*g0+M22*b0+beta8;
            r1 = clip((r1+127)>>8,0,255);
            g1 = clip((g1+127)>>8,0,255);
            b1 = clip((b1+127)>>8,0,255);
            b.setElem(i,(r1<<16)|(g1<<8)|b1);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    long framerate = 0;
    @SuppressWarnings({"SleepWhileInLoop","UseSpecificCatch"})
    public void go() {
        System.out.println("Starting...");
        //hideCursor(this);
        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        last_image_time = 20000+System.currentTimeMillis();
        boredom_time    = 50000+System.currentTimeMillis();
        o.recenter(200);
        frame = 0;
        // If our virtual screen is smaller than the physical screen,we will
        // pad the edges with blac. These are the (x,y) coordinates of the
        // virtual screen on the physica screen device.
        int x_offset = (display_w  - screen_width) / 2;
        int y_offset = (display_h - screen_height) / 2;
        System.out.println(" x_offset : "+x_offset);
        System.out.println(" y_offset : "+y_offset);
        long last_time = System.currentTimeMillis();
        running = true;
        nextImage(1);
        control.setPreset(control.preset_i);
        //Setting up Double Buffering
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
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
                    control.drawAll(buf.out.g);
                    
                    drawState((Graphics2D)buf.out.g);
                    if (show_framerate) drawFramerate();
                    if (show_notifications) noticeChanges();
                    
                    // Copy to physical screen with double buffering.
                    Graphics screen = bufferStrategy.getDrawGraphics();
                    screen.drawImage(buf.out.img,x_offset,y_offset,null);
                    screen.dispose();
                    bufferStrategy.show();
                    
                    // Save frame to disk
                    if (write_animation) {
                        try {
                            String filename = "animate/frame "+animation_frame+".png";
                            animation_frame++;
                            File file = new File(filename);
                            ImageIO.write(buf.out.img,"png",file);
                        } catch (IOException ex) {
                            System.err.println("File write error in animation.");
                            ex.printStackTrace();
                        }
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
                    
                    // Frame counters and framerate monitoring
                    frame++;
                    long time = System.currentTimeMillis();
                    if (time - last_time >= 1000) {
                        last_time = time;
                        framerate = frame;
                        frame = 0;
                    }
                    start_time+= max_frame_length;
                    if (cap_frame_rate) {
                        long delta = start_time - System.currentTimeMillis();
                        if (delta>0) Thread.sleep(delta);
                    }          
                    
                    // We shouldn't need to do this on every frame but
                    // there is a bug somewhere. This ensures that all active
                    // cursors are visible and inactive ones invisible.
                    control.syncCursors();
                    
                } catch (Exception e) {
                    System.out.println("SOMETHING BAD HAPPENED in Perceptron.java go()!");
                    e.printStackTrace();
                }
            } else {
                try {Thread.sleep(50);} catch (InterruptedException ex) {}
            }
        }
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
            moths.paint((Graphics2D)buf.out.g);
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
    public void stimulate() {
        boredom_time = System.currentTimeMillis()+boredome_ms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Save state and the screenshot,"save frame".
    public void save() 
    {
        boolean was_running = this.running;
        this.running = false;
        Win.makeNotFullscreen();
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
                ImageIO.write(buf.out.img,"png",new File(file.getAbsolutePath()+".out.png"));// screenshot
                ImageIO.write(buf.buf.img,"png",new File(file.getAbsolutePath()+".in.png")); // screenshot of the frame - 1
            } catch (IOException ex) {
                System.err.println("File write error while saving images.");
                Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
            }
            try {
                System.out.println("...writing state...");
                Preset.write(this,new File(file.getAbsolutePath()+".state")); // save with extension *.state
            } catch (IOException ex) {
                System.err.println("File write error while saving state.");
                Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
            }
            System.out.println("...saved.");
        } else {
            System.out.println("...did not save.");
        }
        System.out.println("Done.");
        Win.makeFullscreen(this);
        Win.hideCursor(this);
        this.running = was_running;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    /** Read in the settings file. */
    @SuppressWarnings({"ConvertToStringSwitch", "unchecked"})
    final void parseSettings(String settings_path,String presets_path) 
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
                            System.out.println("parsing preset "+val+":");
                            presets.add(Preset.parse(val,in));
                        } else if (var.equals("map")) {
                            try {
                                maps.add(Fractal.makeMapStatic(val));
                                System.out.println("map : "+val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } 
                        } else {
                            //parse primitive
                            Object value = Parser.bestEffortParse(val);
                            if (null != value)
                                try {
                                    this.getClass().getField(var).set(this,value);
                                } catch (NoSuchFieldException 
                                        | SecurityException 
                                        | IllegalArgumentException
                                        | IllegalAccessException ex) {
                                    System.err.println("I could not set "+var+" to "+val+"; parsed as "+value);
                                    Logger.getLogger(Perceptron.class.getName()).log(Level.WARNING,null,ex);
                                }
                        }
                    }
                }
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("Error reading settings file");
            Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
        }
        File f = new File(presets_path);
        List<String> fileList = asList(f.list());
        sort(fileList);
        for (String name : fileList) {
            if (name.endsWith(".state")) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(new File(presets_path+name)));
                    System.out.println("parsing preset "+name+":");
                    presets.add(Preset.parse(name, in));
                    in.close();
                } catch (FileNotFoundException ex) {
                    System.err.println("Could not find preset file "+name);
                    Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
                } catch (IOException ex) {
                    System.err.println("Error loading preset "+name);
                    Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
                }
            }
        }
        this.presets = (Preset[])(presets.toArray(Preset[]::new));
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
    public final Font TEXTFONT = new Font(Font.MONOSPACED,Font.PLAIN,10);
    public final FontMetrics fm = getFontMetrics(TEXTFONT);
    public final int LINEHEIGHT = 11;
    
    private void drawString(Graphics2D g,String s,int x,int y,int a) {
        g.setFont(TEXTFONT);
        // Fill outline sides
        g.setColor(new Color(0,0,0,a));
        g.drawString(s,x-1,y); g.drawString(s,x+1,y);
        g.drawString(s,x,y-1); g.drawString(s,x,y+1);
        // Fill outline corners
        g.setColor(new Color(0,0,0,a>>1));
        g.drawString(s,x-1,y+1); g.drawString(s,x+1,y+1);
        g.drawString(s,x-1,y-1); g.drawString(s,x+1,y-1);
        g.drawString(s,x+1,y-1); g.drawString(s,x+1,y+1);
        g.drawString(s,x-1,y-1); g.drawString(s,x-1,y+1);
        g.setColor(new Color(0xff,0xff,0xff,a));
        g.drawString(s,x,y);
    }
    
    private void drawTopRightString(Graphics2D g,String s,int x,int y,int a) {
        Rectangle2D b = fm.getStringBounds(s,g);
        int w = (int)round(b.getWidth()), h = (int)round(b.getHeight());
        drawString(g,s,x-w,h+y,a);
    }
    
    private void drawRightString(Graphics2D g,String s,int x,int y,int a) {
        int w = (int)round(fm.getStringBounds(s,g).getWidth());
        drawString(g,s,x-w,y,a);
    }

    private void drawFramerate() {
        Graphics2D g = buf.out.g;
        int y = screen_height-2;
        int x = screen_width-2;
        drawRightString(g,framerate+" framerate",x,y,255);
        y-= LINEHEIGHT;
        switch (fractal.offset_mode) {
            case Fractal.LOCKED:
                drawRightString(g,"(constant locked at 0)",x,y,255);
                break;
            case Fractal.POSITION:
                drawRightString(g,fractal.offset+" c",x,y,255);
                break;
            case Fractal.VELOCITY:
                drawRightString(g,fractal.offset+" dc/dt",x,y,255);
                break;
        }
        y-= LINEHEIGHT;
        switch (fractal.rotate_mode) {
            case Fractal.LOCKED:
                drawRightString(g,"(rotation locked at 1)",x,y,255);
                break;
            case Fractal.POSITION:
                drawRightString(g,fractal.rotation.over(complex.I)+" ρ",x,y,255);
                break;
            case Fractal.VELOCITY:
                drawRightString(g,arg(fractal.rotation)*.01f+" dθ/dt",x,y,255);
                y-= LINEHEIGHT;
                drawRightString(g,mod(fractal.rotation)+" r",x,y,255);
                break;
        }
        if (fractal.grad_mode>0) {
            y-= LINEHEIGHT;
            drawRightString(g,fractal.gslope+" grad slope",x,y,255);
            y-= LINEHEIGHT;
            drawRightString(g,fractal.gbias+" grad bias",x,y,255);
        }
        if (draw_tree) {
            y-= LINEHEIGHT;
            drawRightString(g,"x="+tree.location.x+" y="+tree.location.y+" root",x,y,255);
            y-= LINEHEIGHT;
            drawRightString(g,tree.form[0].d_r+" Lρ",x,y,255);
            y-= LINEHEIGHT;
            drawRightString(g,tree.form[1].d_r+" Rρ",x,y,255);
            y-= LINEHEIGHT;
            drawRightString(g,tree.form[0].beta+" Lθ",x,y,255);
            y-= LINEHEIGHT;
            drawRightString(g,tree.form[1].beta+" Rθ",x,y,255);
        }
    }
    
    
    private void drawState(Graphics2D g) {
        // Fade to visible if on,otherwise fade away
        help_alpha = clip(show_help?max(1,help_alpha<<1):help_alpha>>1,0,255);
        if (help_alpha <=0 ) return;
        String info =
            "───┤    PERCEPTRON    ├───\n"
           +"Esc     @exit\n"
           +"LClick  @next cursor\n"
           +"RClick  @previous cursor\n"
           +"Space   @save\n"
           +"⇧+↩     @execute equation\n"
           +"0-9,Fn  @built-in presets\n"
           +helpString(this);
        final int[] tabs = {5,65,195,330,385,515};
        int y = -2;
        int tab = 0;
        for (var l : info.split("\n")) {
            y+= LINEHEIGHT;
            if (y>screen_height-1) {
                y = LINEHEIGHT-2;
                tab = 3;
            }
            int si = 0;
            for (var s:l.split("@")) drawString(g,s,tabs[tab+si++],y,help_alpha);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    final long SHOWFORMS = 30000; // How long to show each notification in ms
    final long FADEOUTMS = 1000;  // Fade in/out time in ms
    final int  MAXNOTIFY = 40;    // Maximum notifications shown
    class Note {
        public final String s;
        public final long   t;
        public Note(String S) {s=S; t=System.currentTimeMillis();}}
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
        long t = System.currentTimeMillis();
        final Set<Note> remove = new HashSet<>();
        int y = 0;
        final Deque<Note> notes = new ArrayDeque<>(notifications);
        for (var n : notes) {
            try{
                long since = t-n.t;
                if (since>SHOWFORMS) {remove.add(n); continue;}
                int alpha = 
                    (since<=255)? (int)(since) :
                    (since>SHOWFORMS-FADEOUTMS)? (int)((SHOWFORMS-since)*255.0/FADEOUTMS+0.5) :
                    255;
                drawTopRightString(G,n.s,screen_width-2,y,alpha);
                y+= LINEHEIGHT;
            } catch (Exception e) {
                System.err.println("Error in drawNotifications "+n.s);
                e.printStackTrace();
            }
        };
        notifications.removeAll(remove);
        while (notifications.size()>MAXNOTIFY) notifications.pop();
    }
    
    
    public void textToMap() {
        fractal.setMap(text.get());
    }
}
