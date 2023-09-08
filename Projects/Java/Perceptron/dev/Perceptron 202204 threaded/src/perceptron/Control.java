 package perceptron;
/* CursorSet.java
 * Created on January 18, 2007, 2:55 AM
 * @author Michael Everett Rule
 */

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import static java.awt.MouseInfo.getPointerInfo;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Set;

import static java.awt.event.KeyEvent.*;
import static java.lang.Math.pow;
import static java.lang.Math.min;
import static java.lang.Math.random;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;
import java.util.TreeSet;
import static javax.swing.SwingUtilities.isEventDispatchThread;

import math.complex;
import color.ColorUtil;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static util.Fullscreen.isFullscreenWindow;
import static util.Misc.clip;
import static util.Misc.wrap;
import static util.Matrix.rotation;
import static util.Sys.getClip;
import static util.Sys.setClip;
import static util.Sys.serr;

/**
 * 
 * @author mer49
 */
public final class Control extends MouseAdapter implements KeyListener {

    // Number of dots (smoothing order) for cursors
    // Speed is logarithmic, 0 leaves unchanged.
    // Can range from -32 (1/16th) to 32 (16x)
    public static final int INITIAL_CURSOR_NDOTS = 10;
    public static final int INITIAL_SPEED        = 12;
    
    final Perceptron P;
    final Settings[] presets;
    final Robot      robot;
    
    /**
     * This point is important.
     * It contains the location on the perceptron screen to draw the mouse.
     * This will be in (0,0,screen_width,screen_height) coordinates. 
     */
    final Point      mouse = new Point(0, 0);
    
    private float rate = 4f;
    
    // Control flags
    public int     preset_i;
    public boolean draw_futures = true;
    public boolean draw_cursors = true;
    public boolean screensaver  = false;
    public boolean parked       = false;
    public void    toggleScreensaver() {screensaver = !screensaver;}
    
    Cursor 
        current,          // currently active cursor
        map_offset,       // constant offset i.e. z^2 + c
        map_rotation,     // rotational constant
        gradient,         // gradient bias and slope
        branching,        // tree branching angles
        alpha_cursor,     // tree brancing axial rotation (disabled)
        branch_length,    // tree branching length
        tree_orientation, // tree orientation (disabled)
        tree_location;    // location of tree root
    
    //Specific sets of cursors
    Set<Cursor> map_cursors, tree, on, all;
    
    public Control(
            Perceptron P_,
            Settings[] user_presets) {
        presets = user_presets;

        P = P_;

        Robot rb = null;
        try {rb = new Robot();} catch (AWTException e) {}
        robot = rb;

        branching = new Cursor("branchangle.png") {
        @Override
        public void step(float rate) {
            // Controls tree's branching angles
            super.step(rate);
            float px=x/P.screen_width-.5f, py=y/P.screen_height-.5f;
            if (px<0) py=py*(1+2*px)-px*Math.signum(py);
            var a1 = (float)px*(float)Math.PI;
            var a2 = (float)py*(float)Math.PI*2f;
            P.tree.form[0].setBeta(a1 + a2 / 2);
            P.tree.form[1].setBeta(a1 - a2 / 2);
        }};
        map_rotation = new Cursor("rotation.jpg") {
        @Override
        public void step(float rate) {
            super.step(rate);
            P.fractal.setNormalizedRotation(x, y);
        }};
        map_rotation.to.x = (int) (P.fractal.W - P.fractal.z2W - Map.UL.real);
        map_rotation.to.y = P.halfScreenHeight();
        map_offset = new Cursor("offset.png") {
        @Override
        public void step(float rate) {
            super.step(rate);
            P.fractal.setNormalizedConstant(x, y);
        }};
        tree_location = new Cursor("root.png") {public void step(float rate) {
            super.step(rate);
            P.tree.location.x = (int) (.5 + x);
            P.tree.location.y = (int) (.5 + y);
        }};
        alpha_cursor = new Cursor("axial.png") {
        @Override
        public void step(float rate) {
            super.step(rate);
            // DISABLED FOR AUTOSPIN SEE Perceptron.tree_spinner;
            /*var xo = x - P.halfScreenWidth();
            var yo = y - P.halfScreenHeight();
            if (xo*xo + yo*yo < 5) {
                P.tree.form[0].setAlpha(0);
                P.tree.form[1].setAlpha(0);
            } else {
                float a1 = (float) (x / P.halfScreenWidth() - 1) * complex.TWOPI;
                float a2 = (float) (y / P.halfScreenHeight() - 1) * complex.TWOPI;
                P.tree.form[0].setAlpha(a1 + a2 / 2);
                P.tree.form[1].setAlpha(a1 - a2 / 2);
            }
            */
        }};
        branch_length = new Cursor("branchlength.png") {
        @Override
        public void step(float rate) {
            super.step(rate);
            P.tree.form[0].d_r = (x / P.screenWidth() - .5f) * .5f + .7f;
            P.tree.form[1].d_r = (y / P.screenHeight() - .5f) * .5f + .7f;
        }};
        tree_orientation = new Cursor("treeorientation.jpg") {
        @Override
        public void step(float rate) {
            super.step(rate);
            P.tree.Y = rotation(3, 0, 2, (float)(x/P.halfScreenWidth ()-1)* complex.pi);
            P.tree.X = rotation(3, 1, 2, (float)(y/P.halfScreenHeight()-1)* complex.pi);
        }};
        gradient = new Cursor("gradient.jpg") {
        @Override
        public void step(float rate) {
            super.step(rate);
            P.fractal.setGradientParam(
                (float)pow(2.*x/P.screen_width,2),
                (float)y/P.screen_height*256-128);
        }};
        map_cursors  = Set.of(map_offset, 
                gradient, 
                map_rotation);
        tree = Set.of(//tree_orientation_cursor, 
                branching, 
                //alpha_cursor, 
                branch_length, 
                tree_location);
        all   = new TreeSet<>();
        all.addAll(map_cursors);
        all.addAll(tree);
        on = new TreeSet<>();
        on.addAll(map_cursors);
        if (P.draw_tree) on.addAll(tree);
        //Start with the map cursor
        current = map_offset;
    }
    
    /**
     * Those little eyeball cursors.
     * Things you should know:
     * - The point `to` is always within (0,0,screen_width,screen_height)
     * - Cursor locations are saved in fractional coordinates (0,0,1,1)
     */
    public abstract class Cursor implements Comparable<Cursor> {
        public final String  name;
        // Mouse-controlled position (we smoothly approach this)
        private final Point to;
        float   x, y; // Current cursor location
        int     speed  = INITIAL_SPEED;
        boolean wander = false;
        Image   image;
        Point   offset;
        // Delay-line smoothed intermediate positions ("future")
        float[] dx = new float[INITIAL_CURSOR_NDOTS], 
                dy = new float[INITIAL_CURSOR_NDOTS]; 
        
        public Cursor(String imagename) {
            name   = imagename;
            to     = new Point(P.halfScreenWidth(), P.halfScreenHeight());
            image  = defaultImage(imagename);
            offset = new Point(image.getWidth(null)/2, image.getHeight(null)/2);
        }

        /**
         * Advance this cursor one animation frame.
         * Step `dt` should be greater than 0 and less than 1.
         * @param dt 
         */
        public synchronized void step(float dt) {
            if (wander) walk();     
            dt = (float)pow(clip(dt,1e-6f,1-1e-6f),pow(2, -speed/8.));
            dx[0]+=(to.x-dx[0])*dt;
            dy[0]+=(to.y-dy[0])*dt;
            int i=1;
            while (i < dx.length) {
                dx[i]+=(dx[i-1]-dx[i])*dt;
                dy[i]+=(dy[i-1]-dy[i])*dt;
                i++;
            }
            i--;
            x += (dx[i]-x)*dt;
            y += (dy[i]-y)*dt;
        }

        /**
         * Draw this cursor on screen.
         * @param g 
         */
        public synchronized void draw(Graphics g) {
            g.drawImage(image, 
                (int)x-offset.x, 
                (int)y-offset.y, null);
            if (draw_futures) for (int i=0; i<dx.length; i++) 
                g.drawImage(trail, 
                    (int)dx[i]-offset.x, 
                    (int)dy[i]-offset.y, null);
        }
        
        /**
         * Increase path dots.
         */
        public synchronized void addDot() {
            if (dx.length > 50) return;
            float[] new_dx = new float[dx.length+1],
                    new_dy = new float[dy.length+1];
            System.arraycopy(dx, 0, new_dx, 0, dx.length);
            System.arraycopy(dy, 0, new_dy, 0, dy.length);
            new_dx[dx.length] = dx[dx.length-1];
            new_dy[dy.length] = dy[dy.length-1];
            dx = new_dx;
            dy = new_dy;
        }

        /**
         * Decrease path dots.
         */
        public synchronized void removeDot() {
            if (dx.length <= 1) return;
            float[] new_dx = new float[dx.length-1],
                    new_dy = new float[dy.length-1];
            System.arraycopy(dx, 0, new_dx, 0, new_dx.length);
            System.arraycopy(dy, 0, new_dy, 0, new_dy.length);
            dx = new_dx;
            dy = new_dy;
        }

        /** 
         * Speed control.
         * Speed can range from -32 to 32; local_rate gets 2^(speed/8)
         * => exponent ranges from 2^-4 2^4 i.e. 0.125 to 16;
         * @param amt 
         */
        public void adjustSpeed(int amt) {
            speed = clip(speed+amt,-32,64);      
        }

        /**
         * Change the point that cursor is heading towards
         * @param x
         * @param y 
         */
        public synchronized void setDestination(int x, int y) {
            to.setLocation(
                clip(x,0,P.screen_width),
                clip(y,0,P.screen_height));
        }

        /**
         * Set destination using [0,1]^2 coordinates
         * @param x
         * @param y 
         */
        public synchronized void setDestination(float x, float y) {
            setDestination(
                (int)(x*P.screen_width+.5f),
                (int)(y*P.screen_height+.5f));
        }

        /**
         * Randomly perturb the destination. 
         */
        public synchronized void walk() {
            setDestination(
                to.x + (int)((random()-.5)*33),
                to.y + (int)((random()-.5)*33));
        }
        
        /**
         * React to a mouseMoved event.
         * Mouse Moved events are forwarded without modification.
         * getX/Y return mouse location relative to Perceptron JFrame
         * Subtract upper left corner of screen to get relative location.
         * Account for screen scaling.
         * @param e 
         */
        public void mouseMoved(MouseEvent e) {
            Rectangle b = P.getPerceptBounds();
            setDestination(
                (int)((e.getX()-b.x)*P.screen_width /(float)b.width +.5),
                (int)((e.getY()-b.y)*P.screen_height/(float)b.height+.5));
        }
        
        /** 
         * hmm
         * @param careful
         **/
        public synchronized void catchup(boolean careful) {
            if (this!=current) return;
            if (robot == null) return;
            
            // Mouse location
            Point m = getPointerInfo().getLocation();
            
            // Bounds of perceptron JFrame; absolute screen-pixel coordinates
            // ( Don't move if mouse outside window )
            Rectangle b = P.getBounds();
            b.setLocation(P.getLocationOnScreen());
            if (careful) if (!b.contains(m)) return;
            
            // True screen position of Cursor (move to here)
            // - Ensure cursor is clipped to screen area
            // - Account for any scaling of the screen area (always integer)
            // - Offset relative to location of screen area (p)
            // - Offset relative to location of Perceptron JFrame (b)
            // The padding by 3 is to prevent us from moving outside the window
            Rectangle p = P.getPerceptBounds();
            int mx = clip(to.x,3,P.screen_width -3)*(p.width /P.screen_width )+p.x+b.x;
            int my = clip(to.y,3,P.screen_height-3)*(p.height/P.screen_height)+p.y+b.y;
            
            // Don't move if the mouse is in the right spot
            if (m.x==mx && m.y==my) return;
            
            moveMouse(mx,my);
        }

        /** 
         * Moves the system mouse to (x,y) location.
         * Dangerous; This should only be called by Cursor.catchup()
         * We can end up in this code
         *  - From the event thread if the mouse is clicked
         *  - From the perceptron constructor when loading presets 
         *  - From the event thread when loading or setting presets 
         *  - From the go() loop thread when checking cursor state DISABLED
         * Perceptron is constructed within invokeLater(). Only the go() thread
         * is a problem; We've removed calls into the cursor state from that
         * thread (hopefully). 
         */
        private void moveMouse(int x, int y) {
            if (!isEventDispatchThread()) {
                serr("Expected to be on the event dispatch thread!");
                serr("(I'm refusing to move the system mouse)");
                return;
            }
            
            P.removeMouseListener(Control.this);
            robot.mouseMove(x,y);
            P.addMouseListener(Control.this);
            
            
            // The mouse point should be in screen coordinates
            // Move to relative to window
            Point p = P.getLocationOnScreen();
            x -= p.x;
            y -= p.y;
            // Move to relative to drawn area
            Rectangle b = P.getPerceptBounds();
            x -= b.x;
            y -= b.y;
            // Scale down to account for screen scaling
            x  = (x*P.screen_width )/b.width;
            y  = (y*P.screen_height)/b.height;
            // Set internal mouse point
            mouse.x = x;
            mouse.y = y;
        }
        
        // Boilerplate
        public void  toggleWander()        {wander = !wander;}
        public int   compareTo(Cursor c)   {return name.compareTo(c.name);}
        public int   nDots()               {return dy.length;}
        public float x()                   {return to.x/(float)P.screen_width;}
        public float y()                   {return to.y/(float)P.screen_height;}
        public void  set(float x, float y) {setDestination(x,y);catchup(true);}
    }
    
    /**
     * Advance (animate) cursor positions forward one frame.
     * @param framerate 
     */
    public synchronized void advance(int framerate) {
        if (on.size()<=0) return;
        if (screensaver) for (var c:on) c.walk();
        // User-controlled cursor moves quickly. Background cursors slowly.
        // both focus_drift and unfocus_drift must be >0 and <1
        if (framerate < 1) framerate = 1;
        float base_drift  = (float)min(1,1-1/(1+.2*rate/framerate));
        float focus_drift = (float)min(1,1-1/(1+1.*rate/framerate));
        all.forEach((c)->c.step(base_drift));
        if (current!=null && !screensaver) current.step(focus_drift);
    }
    
    /**
     * Move focus to the next/previous cursor.
     * n.b.: Java stdlib lacks an indexable sorted set collection.
     * @param i 
     */
    synchronized void cursorSelection(int i) {
        // if there are no active cursors do nothing
        if (on.isEmpty()) {current = null; return;}
        // get active set as a sorted list and move cursor by index i
        Object[] a = on.toArray(Cursor[]::new);
        sort(a);
        int ix = binarySearch(a,current);
        current = (Cursor)a[ix<0? 0 : wrap(ix + i, on.size())];
        
        // TODO
        // Try to move mouse to new cursor
        // Only do this if mouse is already over the window
        current.catchup(true);
    }
    
    /**
     * Ensure that cursor state is synchronize with Perceptron's state. 
     * Only cursors that control active rendering operation should be shown.
     */
    private void setActive(Cursor      c, boolean a) {if (a) on.add(c);    else on.remove(c);}
    private void setActive(Set<Cursor> c, boolean a) {if (a) on.addAll(c); else on.removeAll(c);}
    public synchronized void syncCursors() {
        Map F = P.fractal;
        setActive(map_rotation, F.rotate_mode != Map.LOCKED);
        setActive(map_offset  , F.offset_mode != Map.LOCKED);
        setActive(gradient    , F.grad_mode   != 0);
        setActive(tree        , P.draw_tree);
        checkCursor();
    }
    
    /**
     * Ensure a visible cursor is under user control.
     */
    public synchronized void checkCursor() {
        if (null!=current && on.contains(current)) return;
        if (on.isEmpty()) current=null;
        else {current=on.iterator().next(); current.catchup(true);}
    }

    /** 
     * Draw active cursors.
     * @param g */
    public synchronized void drawAll(Graphics g) {
        if (on.size()<=0 || !draw_cursors)  return;
        for (Cursor c :on) c.draw(g);
        if (!screensaver) {
            // Draw a ring around the current mouse position
            g.drawImage(cursor_ring,(int)(mouse.x)-32,(int)(mouse.y)-32,null);
            // Draw a smaller ring around the active cursor
            if (current != null) 
                g.drawImage(focus,(int)current.x-11,(int)current.y-11,null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //MOUSE LISTENER IMPLEMENTATION
    
    /**
     * Moving the mouse updates the location of the currently active cursor.
     * 
     * There are a few possible windowing configurations
     *      - Windowed, both decorated and not
     *      - Maximized, both decorated and not
     *      - Full screen, hopefully undecorated but no promises
     * 
     * In any of these modes, the screen area might be scaled up by an integer 
     * factor. It's location can change e.g. when window is resized.
     * 
     * If windowed, we track the mouse as-is. Cursor locations are clamped to 
     * the screen area. 
     * 
     * @param e 
     */
    public synchronized void mouseMoved(MouseEvent e) {
        P.poke();
        if (on.size() <= 0) return;
        synchronized (mouse) {
            // Note: this is NOT clamped to the screen; This allows the
            // mouse-tracking circle to exit the frame if the mouse does as well.    
            Rectangle b = P.getPerceptBounds();         
            mouse.setLocation(
                (int)((e.getX()-b.x)*P.screen_width /(float)b.width +.5),
                (int)((e.getY()-b.y)*P.screen_height/(float)b.height+.5));
        }
        if (!parked) current.mouseMoved(e);
    }
    
    /**
     * Dragging has same effect as moving. 
     * @param e 
     */
    public synchronized void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
    
    /**
     * Update mouse location to that of the cursor, if possible.
     * TODO: FIX ME
     * @param e 
     */
    public void mouseEntered(MouseEvent e) {
        P.poke();
        if (current!=null && isFullscreenWindow(P)) {
            //int x = max(0,min(P.getWidth(), (int)(current.to.x)));
            //int y = max(0,min(P.getHeight(),(int)(current.to.y)));
            // moveTheMouse(x, y);
        }
    }
    
    /**
     * Cycle between active cursors using left and right mouse buttons
     * @param e 
     */
    public synchronized void mousePressed(MouseEvent e) {
        P.poke();
        switch (e.getButton()) {
            case MouseEvent.BUTTON1 -> cursorSelection( 1);
            case MouseEvent.BUTTON3 -> cursorSelection(-1);
        }
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //KEY LISTENER IMPLEMENTATION
    // text entry mode while entering the equation by hand (press CTRL)
    boolean text_mode   = false;
    boolean presets_mode = false;

    /** 
     * Key typed.
     * 
     * Near as I can tell (OpenJDK 11), control+key DOES show up here, but 
     * always with a null key character and key code, so it is not very useful. 
     * 
     * @param e
     */
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (text_mode) {
            if (c=='\n' && (e.isShiftDown()||e.isControlDown())) P.textToMap();
            else P.text.append(c);
            return;
        }
        if (presets_mode) {
            if (c=='£') c='#';
            int which = ("0123456789abcdefghijklmnopqrstuvwxyz`-=[]\\;\',./)"+
                "!@#$%^&*(ABCDEFGHIJKLMNOPQRSTUVWXYZ~_+{}|:\"<>?").indexOf(c);
            if (which>=0) setPreset(which); 
            else P.notify("No preset bound to "+c);
            return;
        }
        Map F = P.fractal;
        if (F==null) return;
        
        switch (c) {
            case ' ', '`' -> P.toggleRunning();
            case '~' -> {}
            case '1' -> F.setMap("i*ln(z)/2/P*w");
            case '!' -> F.setMap("ln(z)/2/P*h");
            case '2' -> F.setMap("i*ln(z)/P*w");
            case '@' -> F.setMap("ln(z)/P*h");
            case '3' -> F.setMap("3*i*ln(z)/2/P*w");
            case '#', '£' -> F.setMap("3*ln(z)/2/P*h");
            case '4' -> F.setMap("4*i*ln(z)/2/P*w");
            case '$' -> F.setMap("4*ln(z)/2/P*h");
            case '5' -> F.setMap("z/abs(sqrt((absz)^2-1.5))");
            case '%' -> F.setMap("z^(1.5)");
            case '6' -> F.setMap("z-(z^3-1)/(3*z^2)");
            case '^' -> F.setMap("z-(z^4-1)/(4*z^3)");
            case '7' -> F.setMap("i*ln(z)/(2p)*sqrt(w*w+h*h)*e^(i*atan(h/w))");
            case '&' -> P.tree.toggleLeaves();
            case '8' -> F.setMap("2*i*ln(z)/(2p)*sqrt(w*w+h*h)*e^(i*atan(h/w))");
            case '*' -> P.tree.toggleSymmetry();
            case '9' -> F.setMap("i*ln(z)/(2p)*sqrt(w*w*9+h*h)*e^(i*atan(h/w/3))");
            case '0' -> F.setMap("2*i*ln(z)/(2p)*sqrt(w*w*9+h*h)*e^(i*atan(h/w/3))");
            case '(' -> F.nextNoise(-8);
            case ')' -> F.nextNoise(8);
            case '-' -> current.adjustSpeed(-1);
            case '_' -> P.draw_top_bars = !P.draw_top_bars;
            case '=', '+' -> current.adjustSpeed(1);
            case 'q' -> F.nextMap(1);
            case 'Q' -> F.nextMap(-1);
            case 'w' -> F.nextOutside(1);
            case 'W' -> F.nextOutside(-1);
            case 'e' -> F.nextBound(1);
            case 'E' -> F.nextBound(-1);
            case 'r' -> {P.buf.nextReflection(1); F.cache.map_stale.set(true);}
            case 'R' -> F.invert_bound=!F.invert_bound;
            case 't' -> setTree(!P.draw_tree);
            case 'T' -> P.toggleObjectsOnTop();
            case 'y' -> F.nextOutColor(1);
            case 'Y' -> F.nextOutColor(-1);
            case 'u' -> P.toggleShowFramerate();
            case 'U' -> P.toggleCapFramerate();
            case 'i' -> P.nextImage(1);
            case 'I' -> P.nextImage(-1);
            case 'o' -> F.nextOrthoMode(1);
            case 'O' -> F.nextOrthoMode(-1);
            case 'p' -> F.nextPolarMode(1);
            case 'P' -> F.nextPolarMode(-1);
            case '[' -> F.nextBarColor(1);
            case ']' -> F.nextBarColor(-1);
            case '{' -> current.removeDot();
            case '}' -> current.addDot();
            case '\\'-> P.rotate_images=!P.rotate_images;
            case '|' -> P.draw_side_bars=!P.draw_side_bars;
            case 'a' -> P.mic.nextVis(1);
            case 'A' -> P.mic.setActive(!P.mic.isActive());
            case 's' -> P.draw_dino=!P.draw_dino;
            case 'S' -> P.save();
            case 'd' -> F.nextColorDamp( 8);
            case 'D' -> F.nextColorDamp(-8);
            case 'f' -> F.nextGColor1(1);
            case 'F' -> F.nextGColor2(1);
            case 'g' -> F.nextGradient( 1);
            case 'G' -> F.nextGradient(-1);
            case 'h' -> F.nextGradientShape(1);
            case 'H' -> F.nextGradientShape(-1);
            case 'j' -> F.toggleInversion();
            case 'J' -> F.toggleFeedbackInvert();
            case 'k' -> P.fore_tint=!P.fore_tint;
            case 'K' -> P.do_color_transform=!P.do_color_transform;
            case 'l' -> {P.buf.toggleInterpolation(); F.cache.map_stale.set(true);}
            case 'L' -> P.toggleAntialias();
            case ';' -> P.hue_rate = wrap(P.hue_rate-4,256);
            case '\''-> P.hue_rate = wrap(P.hue_rate+4,256);
            case ':' -> P.sat_rate = clip(P.sat_rate-4,-256,256);
            case '"' -> P.sat_rate = clip(P.sat_rate+4,-256,256);
            case ',' -> P.con_rate = clip(P.con_rate-4,-256,256);
            case '.' -> P.con_rate = clip(P.con_rate+4,-256,256);
            case '<' -> P.bri_rate = clip(P.bri_rate-4,-256,256);
            case '>' -> P.bri_rate = clip(P.bri_rate+4,-256,256);
            case 'z' -> F.nextTintColor( 1);
            case 'Z' -> F.nextTintColor(-1);
            case 'x' -> F.nextTintLevel( 8);
            case 'X' -> F.nextTintLevel(-8);
            case 'c' -> draw_cursors = !draw_cursors;
            case 'C' -> draw_futures = !draw_futures;
            case 'v' -> {if (current!=null) current.toggleWander();}
            case 'V' -> toggleScreensaver();
            case 'b' -> P.text.toggle();
            case 'B' -> P.text.toggleCursor();
            case 'n' -> {
                P.capture_screen =! P.capture_screen;
                if (P.capture_screen) {
                    if (!P.isFullscreen()||getLocalGraphicsEnvironment().getScreenDevices().length>1) {
                        P.big.watcher.setVisible(true);
                    }
                } else {
                    P.big.watcher.setVisible(false);
                    P.big.selector.setVisible(false);
                }
            }
            case 'N' -> P.show_notices =! P.show_notices;
            case 'm' -> F.nexMirrorMode(1);
            case 'M' -> P.draw_moths = !P.draw_moths;
            case '/', '?' -> P.toggleShowHelp();
        }
        //case '\t':break; // Handled in keyPressed
        syncCursors();
    }
    
    /**
     * Handle control + [KEYS] commands.
     * Only "control" is truly reliable.
     * 
     * Shift is already incorporated into the keyChar;
     * Alt/altGr/meta are used to enter foreign letters and special symbols. 
     * Macs have a "command" key, but OSX hasn't supported Java in years,
     * and macs always have control as well.
     * There isn't a reliable equivalent on Linux/Windows. 
     *  The e.getModifiersEx returns a bitmask of ALL modifiers, and is a bit 
     * exciting. Ex stands for extended. Mouse buttons are included in the 
     * modifiers. There really isn't any new info here that you can't get 
     * from e.isControlDown, etc. 
     * 
     * Detecting key using e.getKeyCode():
     * - A-Z 0-9, []-=,.;\/ behave as expected
     * - (`¬)('@)(#~) show up as special Latin characters À, Þ, Ï
     * - Placement varies with language, don't use! 
     * - Fn keys show up as pqrstuvwxyz{, 
     * - home and end show up as $ and #, respectively
     * - pgup, up, pgdn, left, down, right show up as !&"%('
     * 
     * The correct way to do this is to check modifiers separately
     * and refer to keys entirely by their VK_ codes. This is awkward
     * if we want to map the same command to multiple modifier combinations.
     * 
     * Note: most commands work similarly regardless of the entry
     * or preset mode, with the exception of control+C control+V, which will
     * copy/paste the perceptron state in regular and in presets mode, and
     * copy/paste the text buffer contents in text editor mode. 
     * 
     * 
     * @param e
     */
    public void handleCommand(KeyEvent e) {
        if (!e.isControlDown()) return;

        boolean option = e.isAltDown()||e.isMetaDown()||e.isAltGraphDown();
        boolean shift  = e.isShiftDown();

        int c = e.getKeyCode();
        
        String       modifiers = "ctrl+";
        if (option)  modifiers+= "alt+";
        if (shift )  modifiers+= "shift+";
        String cmd = modifiers + (char)c;
        P.notify(cmd);
        
        // ABCDEFGHIJKLMNOPQRSTUVWXYZ0987654321 are safe
        // pqrstuvwxyz( are f1-f12; support may vary
        // support may vary for -=[];,./\ 
        
        // Commands not affected by additional modifiers
        // These execute for any modifier combo that includes ctrl
        switch (c) {
            case 'Q': System.exit(0);
        }
        // Copy, cut, paste behave differently in text mode.
        // In preset and regular mode these copy/paste preset state.
        // In text mode these copy/past text.
        if (text_mode) {
            switch (modifiers) {
                case "ctrl+": switch (c) {
                    case 'C':setClip(P.text.get()); return;
                    case 'X':setClip(P.text.get()); P.text.clear(); return;
                    case 'V':{String s=getClip(); if (s.length()>0) P.text.append(s);} return;
                    case 'D'     :P.text.clear();    return; 
                    case VK_LEFT :P.text.prevWord(); return;
                    case VK_RIGHT:P.text.nextWord(); return;
                } break;
                case "ctrl+alt+"      : switch (c) {} break;
                case "ctrl+shift+"    : switch (c) {} break;
                case "ctrl+alt+shift+": switch (c) {} break;
            }
        } else {
            switch (modifiers) {
                case "ctrl+": switch (c) {
                    case 'C': setClip(Settings.settings(P)); return;
                    case 'X': setClip(Settings.settings(P)); return;
                    case 'V': {String s=getClip(); if (s.length()>0) Settings.parse(s).set(P);} return;
                } break;
                case "ctrl+alt+"      : switch (c) {} break;
                case "ctrl+shift+"    : switch (c) {} break;
                case "ctrl+alt+shift+": switch (c) {} break;
            }
        }
        // commands that are the same everywhere
        switch (modifiers) {
            case "ctrl+": switch (c) {
                case 'M': P.toggleHideMouse();  return;
                case 'F': P.toggleFullscreen(); return;
                case 'E': P.toggleFrame();      return; // toggle window frame 
                case 'S': P.save();             return;
                case 'P': parked = !parked;     return;
            } break;
            case "ctrl+alt+"      : switch (c) {
                case 'S': P.toggleAnimation(); return;
            } break;
            case "ctrl+shift+"    : switch (c) {
                case 'T': P.toggleCaptureText(); return;
                case 'C': P.toggleCaptureCursors(); return;
            } break;
            case "ctrl+alt+shift+": switch (c) {} break;
        }
        P.notify("not mapped");
        syncCursors();
    }
    
    /**
     * Key Pressed.
     * 
     * Handle unmodified command keys. Key-pressed events can be re-triggered
     * rapidly if a key is held down. Only include actions that are safe to
     * repeat here. 
     * 
     * Do not react to SHIFT, ALT, ALTGR, OPTION, META, COMPOSE, or CAPSLOCK. 
     * These are used to type capital letters or special characters. These 
     * can be handled by keyTyped. s
     * 
     * Unfortunately, we DO need to handle commands like Control+[KEY] here,
     * even though they can repeat rapidly. The reason for this is that 
     * there is no guarantee that the user will release [KEY] last when typing
     * a command. 
     * 
     * For example, if a user enters CONTROL+ALT+SHIFT+M, the only guarantee
     * is this: CONTROL, ALT, SHIFT were all be depressed in some order. 
     * THEN "m" should be pressed. THEN these four keys may be released in 
     * any order. 
     * 
     * @param e 
     */
    @Override
    public void keyPressed(KeyEvent e) {
        Map F = P.fractal;
        int code = e.getKeyCode();
        //if (code==VK_ESCAPE||code==VK_STOP) System.exit(0);
        if (code==VK_STOP) System.exit(0); 
        /*
        // For debugging
        P.notify("-------------");
        P.notify("Control: "+e.isControlDown());
        P.notify("Shift: "+e.isShiftDown());
        P.notify("AltDown: "+e.isAltDown());
        P.notify("Meta: "+e.isMetaDown());
        P.notify("GraphDown: "+e.isAltGraphDown()); 
        P.notify("IsAction: "+e.isActionKey());
        P.notify("ExtendedKeyCode: "+e.getExtendedKeyCode());
        P.notify("ModifiersEx: "+e.getModifiersEx());
        P.notify("KeyCode: "+e.getKeyCode());
        */
        if (e.isControlDown()) {handleCommand(e); return;}
        if (text_mode) {
            // keyTyped handles backspace and delete
            // Some other commands handled if isControlDown()
            switch (code) {
                case VK_TAB       -> text_mode=P.text.cursor_on=false;
                case VK_LEFT      -> P.text.left();
                case VK_RIGHT     -> P.text.right();
                case VK_UP        -> P.text.up();
                case VK_DOWN      -> P.text.down();
                case VK_ENTER     -> {if (e.isShiftDown()||e.isControlDown()) P.textToMap();}
                case VK_PAGE_UP   -> P.text.pageUp();
                case VK_PAGE_DOWN -> P.text.pageDown();
                case VK_HOME      -> P.text.home();
                case VK_END       -> P.text.end();
                case VK_INSERT    -> P.text.toggleInsert();
            }
            return;
        } 
        if (presets_mode) {
            if (code==VK_ENTER) presets_mode = false;
            else if (code==VK_TAB) text_mode=P.text.on=P.text.cursor_on=true;
            return;
        }
        // Neither text-entry or presets mode
        switch (code) {
            case VK_TAB       -> text_mode=P.text.on=P.text.cursor_on=true;
            case VK_ENTER     -> presets_mode = true;
            case VK_UP        -> F.setMotionBlur(F.motion_blur + 16);
            case VK_DOWN      -> F.setMotionBlur(F.motion_blur - 16);
            case VK_LEFT      -> P.setBlurWeight(P.blursharp_rate-16);
            case VK_RIGHT     -> P.setBlurWeight(P.blursharp_rate+16);
            case VK_PAGE_UP   -> P.mic.adjustVolume(0.2f);
            case VK_PAGE_DOWN -> P.mic.adjustVolume(-0.2f);
            case VK_HOME      -> P.mic.adjustSpeed(0.2f);
            case VK_END       -> P.mic.adjustSpeed(-0.2f);
            case VK_INSERT    -> {}
            case VK_DELETE    -> P.toggleAnimation();
            case VK_F1  -> F.setMap("z*z");
            case VK_F2  -> F.setMap("z*abs(z)");
            case VK_F3  -> F.setMap("f/z+i*z");
            case VK_F4  -> F.setMap("z");
            case VK_F5  -> F.setMap("1/z");
            case VK_F6  -> F.setMap("e^z+e^(iz)");
            case VK_F7  -> F.setMap("conj(e^z+e^(z*e^(i*p/4)))");
            case VK_F8  -> F.setMap("z*z*e^(i*abs(z))");
            case VK_F9  -> F.setMap("abs(z)*e^(i*arg(z)*2)*2");
            case VK_F10 -> F.setMap("z*e^(i*abs(z))*abs(z)/f");
            case VK_F11 -> F.setMap("(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*"+F.W+"/"+F.H);
            case VK_F12 -> F.setMap("(imag(z)*i+ln(abs(real(z))))*.3/(abs(real(z)))*"+F.W+"/"+F.H);
        }
        syncCursors();
    }
    
    /**
     * Key Released. 
     * Control + [key] don't show up in keyTyped in a useful way. They DO show 
     * up in keyPressed, but can also repeat (quickly) if held down. For 
     * simplicity we detect Control+[KEY] commands here. They should only 
     * trigger once. 
     * 
     * @param e 
     */
    @Override
    public void keyReleased(KeyEvent e) {
        syncCursors(); 
    }
    
    public synchronized void setPreset(int preset) {
        if (presets.length<=0) return;
        try {
            if (preset<0 || preset>=presets.length) {
                P.notify("Preset #"+preset+" isn't defined.");
                return;
            }
            preset_i = preset;
            Settings ps = presets[preset_i];
            ps.set(P);
            P.notify("Applied preset #"+preset+" ("+ps.name()+")");
        } catch (Exception e) {
            P.notify("Error applying preset " + preset);
            serr("Error applying preset " + preset + ":");
            e.printStackTrace();
        }
        syncCursors();
    }
    public void nextPreset(int n) {
        setPreset(preset_i = wrap(preset_i + n, P.presets.length));
    }
    public synchronized void setFractal(boolean active) {
        if (active) this.on.addAll(map_cursors);
        else this.on.removeAll(map_cursors);
        checkCursor();
    }
    public synchronized void setTree(boolean active) {
        if (active) this.on.addAll(tree);
        else this.on.removeAll(tree);
        checkCursor();
        P.draw_tree = active;
    }
     
    ////////////////////////////////////////////////////////////////////////////
    // Cursor sprites / pre-rendered. //////////////////////////////////////////
    public static final BufferedImage cursor_ring = cursorRing();
    public static final BufferedImage trail = trailer();
    public static final BufferedImage focus = focus();
    public static final BufferedImage cursorRing() {
        var b = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        var g = ColorUtil.fancy(b.createGraphics());
        g.setColor(Color.WHITE);
        g.drawOval(2, 2, 60, 60);
        return b;
    }
    public static final BufferedImage trailer() {
        BufferedImage b = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtil.fancy(b.createGraphics());
        image_graphics.setColor(new Color(255, 255, 255, 128));
        image_graphics.drawOval(19, 19, 3, 3);
        return b;
    }
    public static final  BufferedImage focus() {
        BufferedImage b = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtil.fancy(b.createGraphics());
        image_graphics.setColor(Color.WHITE);
        image_graphics.drawOval(1, 1, 19, 19);
        return b;
    }
    public static final BufferedImage defaultImage(String imagename) {
        var b = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        var g = ColorUtil.fancy(b.createGraphics());
        g.fillOval(13, 13, 13, 13);
        try {
            BufferedImage image1 = ImageIO.read(new File("resource/cursors/" + imagename));
            for (int y = 0; y < 40; y++)
                for (int x = 0; x < 40; x++) 
                    b.setRGB(x, y, b.getRGB(x, y) & 0xff000000 | 0x00ffffff & image1.getRGB(x, y));
        } catch (IOException e) {
            System.err.println("Error loading cursor image "+imagename);
            e.printStackTrace();
        }
        return b;
    }
}
