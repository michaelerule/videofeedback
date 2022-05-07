package perceptron;
/* CursorSet.java
 * Created on January 18, 2007, 2:55 AM
 * @author Michael Everett Rule
 */

import java.awt.AWTException;
import util.ColorUtil;
import math.complex;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static java.awt.event.KeyEvent.*;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import static java.lang.Math.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import static util.Misc.clip;
import static util.Misc.wrap;
import static util.Matrix.rotation;

/**
 * 
 * @author mer49
 */
public final class Controls implements MouseListener, MouseMotionListener, KeyListener {
    //THESE OBJECTS ARE CONTROLLED AND MODIFIED BY THIS CLASS

    Perceptron P;
    Point      mouse = new Point(0, 0);
    
    private float rate = 4f;
    
    //Scale from physical screen dimension to virtual screen dimensions
    float xscale, yscale;
    
    //Control flags
    public int     preset_i;
    public boolean draw_futures = true;
    public boolean draw_cursors = true;
    public boolean screensaver  = false;
    public void    toggleScreensaver() {screensaver = !screensaver;}
    
    //CURSORS
    Cursor 
        current, //active cursor
        branching,
        alpha_cursor,
        branch_length,
        tree_orientation,
        map_offset,
        map_rotation,
        gradient,
        tree_location;
    
    //Specific sets of cursors
    Set<Cursor> map_cursors, tree, audio_cursors, life_cursors;
    
    //Set of active cursors (cursors become active, inactive based on the
    //activation state of the controlled objects
    Set<Cursor> active, all_cursors;
    
    /// PRESETS
    Preset[] presets;

    public Controls(
            Perceptron P_,
            Preset[] user_presets) {
        presets = user_presets;

        P = P_;
        xscale = (float)(P.screen_width )/P.display_w;
        yscale = (float)(P.screen_height)/P.display_h;

        try {robot = new Robot();} catch (AWTException e) {}

        branching = new Cursor("c1.png") {public void step(float rate) {
            super.step(rate);
            var a1 = (float)(x/P.halfScreenWidth() -1)*complex.TWOPI;
            var a2 = (float)(y/P.halfScreenHeight()-1)*complex.TWOPI;
            P.tree.form[0].set_beta(a1 + a2 / 2);
            P.tree.form[1].set_beta(a1 - a2 / 2);
        }};
        map_rotation = new Cursor("c2.jpg") {public void step(float rate) {
            super.step(rate);
            P.fractal.setNormalizedRotation(x, y);
        }};
        map_rotation.to.x = (int) (P.fractal.W - P.fractal.z2W - FractalMap.UL.real);
        map_rotation.to.y = P.halfScreenHeight();
        map_offset = new Cursor("c7.png") {public void step(float rate) {
            super.step(rate);
            P.fractal.setNormalizedConstant(x, y);
        }};
        tree_location = new Cursor("c5.png") {public void step(float rate) {
            super.step(rate);
            P.tree.location.x = (int) (.5 + x);
            P.tree.location.y = (int) (.5 + y);
        }};
        alpha_cursor = new Cursor("c4.png") {public void step(float rate) {
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
        branch_length = new Cursor("c3.png") {
        public void step(float rate) {
            super.step(rate);
            P.tree.form[0].d_r = (x / P.screenWidth() - .5f) * .5f + .7f;
            P.tree.form[1].d_r = (y / P.screenHeight() - .5f) * .5f + .7f;
        }};
        tree_orientation = new Cursor("iupiter.jpg") {
        public void step(float rate) {
            super.step(rate);
            P.tree.Y = rotation(3, 0, 2, (float)(x/P.halfScreenWidth ()-1)* complex.pi);
            P.tree.X = rotation(3, 1, 2, (float)(y/P.halfScreenHeight()-1)* complex.pi);
        }};
        gradient = new Cursor("c8.jpg") {
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
        audio_cursors = Set.of();
        all_cursors   = new HashSet<>();
        all_cursors.addAll(map_cursors);
        all_cursors.addAll(tree);
        all_cursors.addAll(audio_cursors);
        active = new HashSet<>();
        active.addAll(map_cursors);
        if (P.draw_tree) active.addAll(tree);
        //Start with the map cursor
        current = map_offset;
    }

    private Point bound(Point p) {
        p.x = clip(p.x,0,P.screen_width);
        p.y = clip(p.y,0,P.screen_height);
        return p;
    }
    
    public synchronized void advance(int framerate) {
        if (active.size() <= 0) return;
        if (screensaver) for (var c :active) c.walk();
        // User-controlled cursor moves quickly
        // both focus_drift and unfocus_drift must be >0 and <1
        if (framerate < 1) framerate = 1;
        float focus_drift = (float)min(1,1-1/(1+rate/framerate));
        if (current != null && !screensaver)
            current.step(focus_drift);
        // Background cursors move slowly
        float unfocus_drift = (float)min(1,1-1/(1+.2*rate/framerate));
        all_cursors.forEach((c)->c.step(unfocus_drift));
    }
    

    synchronized void cursorSelection(int step) {
        //if there are no active cursors do nothing
        if (active.isEmpty()) {current = null; return;}
        Cursor[] activeArray = (Cursor[]) active.toArray(Cursor[]::new);
        Arrays.sort(activeArray);
        int index = Arrays.binarySearch(activeArray,current);
        index = index<0? 0 : wrap(index + step, active.size());
        current = activeArray[index];
        //update mouse location to that of the new cursor, if possible
        catchup();
    }
    
    /**
     * Ensure that cursor state is synchronize with Perceptron's state. 
     * Only cursors that control active rendering operation should be shown.
     */
    public synchronized void syncCursors() {
        FractalMap F = P.fractal;
        // The map cannot rotate if the rotation is locked
        if (F.rotate_mode == F.ROTATION_LOCKED) active.remove(map_rotation);
        else if (!active.contains(map_rotation)) active.add(map_rotation);
        // The map cannot translate if translation is locked
        if (F.offset_mode == F.TRANSLATION_LOCKED) active.remove(map_offset);
        else if (!active.contains(map_offset)) active.add(map_offset);
        // Gradient zero is no gradient at all 
        if (F.grad_mode == 0) active.remove(gradient);
        else if (!active.contains(gradient)) active.add(gradient);
        // Is the tree on? 
        if (!P.draw_tree)  active.removeAll(tree);
        else active.addAll(tree);
        checkCursor();
    }
    
    
    /**
     * Ensure one of the visible cursors is under user control.
     */
    public synchronized void checkCursor() {
        if (active.contains(current)) return;
        if (active.isEmpty()) current=null;
        else {current = active.iterator().next(); catchup();}
    }

    /** Draw all active controls
     * @param G */
    public synchronized void drawAll(Graphics G) {
        //if there are no active cursors do nothing
        if (active.size()<=0 || !draw_cursors)  return;
        for (Cursor c :active) c.draw(G);
        if (!screensaver) {
            G.drawImage(cursor_ring,(int)(mouse.x)-32,(int)(mouse.y)-32,null);
            if (current != null) G.drawImage(focus,(int)current.x-11,(int)current.y-11,null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //MOUSE LISTENER IMPLEMENTATION
    @Override
    public synchronized void mouseEntered(MouseEvent e) {
        P.stimulate();
        //update mouse location to that of the cursor, if possible
        if (current != null) {
            int x = max(0,min(P.displayWidth(), (int)(current.to.x/xscale)));
            int y = max(0,min(P.displayHeight(),(int)(current.to.y/yscale)));
            moveTheMouse(x, y);
        }
    }
    @Override
    public synchronized void mousePressed(MouseEvent e) {
        P.stimulate();
        if (e.getButton() == MouseEvent.BUTTON1) cursorSelection(1);
        else if (e.getButton() == MouseEvent.BUTTON3) cursorSelection(-1);
    }
    @Override
    public synchronized void mouseMoved(MouseEvent e) {
        P.stimulate();
        if (active.size() <= 0) return;
        mouse = bound(new Point(
                (int) (xscale * e.getX()),
                (int) (yscale * e.getY())));
        current.mouseMoved(e);
    }
    @Override
    public synchronized void mouseDragged(MouseEvent e) {mouseMoved(e);}
    @Override
    public synchronized void mouseExited(MouseEvent e) {}
    @Override
    public synchronized void mouseReleased(MouseEvent e) {}
    @Override
    public synchronized void mouseClicked(MouseEvent e) {}
    
    ////////////////////////////////////////////////////////////////////////////
    //KEY LISTENER IMPLEMENTATION
    // text entry mode while entering the equation by hand (press CTRL)
    boolean entry_mode   = false;
    boolean presets_mode = false;
    /** 
     * @param e
     */
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (entry_mode) {
            switch (c) {
                case '\n':
                    P.text.down();
                    P.text.home();
                    break;
                case '\b':
                    break;
                case '\r':
                    break;
                default:
                    P.text.append(c);
            }
            return;
        }
        if (presets_mode) {
            if (c=='£') c='#';
            int which = ("0123456789abcdefghijklmnopqrstuvwxyz`-=[]\\;\',./)"+
                "!@#$%^&*(ABCDEFGHIJKLMNOPQRSTUVWXYZ~_+{}|:\"<>?").indexOf(c);
            if (which>=0) setPreset(which); 
            else P.notify("Character input "+c+" is not yet supported.");
            return;
        }
        FractalMap F = P.fractal;
        if (F==null) return;
        switch (c) {
            case '`':P.running = !P.running; break;
            case '~':break;
            case '1':F.setMap("i*ln(z)/2/P*w"); break;
            case '!':F.setMap("ln(z)/2/P*h"); break;
            case '2':F.setMap("2*i*ln(z)/2/P*w"); break;
            case '@':F.setMap("2*ln(z)/2/P*h"); break;
            case '3':F.setMap("3*i*ln(z)/2/P*w"); break;
            case '#':case '£':F.setMap("3*ln(z)/2/P*h"); break;
            case '4':F.setMap("4*i*ln(z)/2/P*w"); break;
            case '$':F.setMap("4*ln(z)/2/P*h"); break;
            case '5':F.setMap("z/abs(sqrt((absz)^2-1.5))"); break;
            case '%':F.setMap("z^(1.5)"); break;
            case '6':F.setMap("z-(z^3-1)/(3*z^2)"); break;
            case '^':F.setMap("z-(z^4-1)/(4*z^3)"); break;
            case '7':F.setMap("i*ln(z)/(2p)*sqrt(w*w+h*h)*e^(i*atan(h/w))");break;
            case '&':P.tree.toggleLeaves(); break;
            case '8':F.setMap("2*i*ln(z)/(2p)*sqrt(w*w+h*h)*e^(i*atan(h/w))");break;
            case '*':P.tree.toggleSymmetry(); break;
            case '9':F.setMap("i*ln(z)/(2p)*sqrt(w*w*9+h*h)*e^(i*atan(h/w/3))");break;
            case '0':F.setMap("2*i*ln(z)/(2p)*sqrt(w*w*9+h*h)*e^(i*atan(h/w/3))");break;
            case '(':F.nextNoise(-8);break;
            case ')':F.nextNoise(8);break;
            case '-':current.adjustSpeed(-1); break;
            case '_':P.draw_top_bars = !P.draw_top_bars; break;
            case '=':
            case '+':current.adjustSpeed(1); break;
            //case '\t':break;
            case 'q':F.nextMap(1); break;
            case 'Q':F.nextMap(-1);break;
            case 'w':F.nextOutside(1); break;
            case 'W':F.nextOutside(-1); break;
            case 'e':F.nextBound(1); break;
            case 'E':F.nextBound(-1); break;
            case 'r':P.buf.nextReflection(1); break;
            case 'R':F.invert_bound=!F.invert_bound; break;
            case 't':setTree(!P.draw_tree); break;
            case 'T':P.toggleObjectsOnTop(); break;
            case 'y':F.nextOutColor(1); break;
            case 'Y':F.nextOutColor(-1); break;
            case 'u':P.toggleShowFramerate(); break;
            case 'U':P.toggleCapFramerate(); break;
            case 'i':P.nextImage(1); break;
            case 'I':P.nextImage(-1); break;
            case 'o':F.nextOrthoMode(1); break;
            case 'O':F.nextOrthoMode(-1); break;
            case 'p':F.nextPolarMode(1); break;
            case 'P':F.nextPolarMode(-1); break;
            case '[':F.nextBarColor(1); break;
            case ']':F.nextBarColor(-1); break;
            case '{':current.removeDot(); break;
            case '}':current.addDot(); break;
            case '\\':P.rotate_images=!P.rotate_images; break;
            case '|':P.draw_side_bars=!P.draw_side_bars; break;
            case 'a':P.mic.nextVis(1); break;
            case 'A':P.mic.setActive(!P.mic.isActive()); break;
            case 's':P.draw_dino=!P.draw_dino; break;
            case 'S':P.save();                 break;
            case 'd':F.nextColorDamp( 8);      break;
            case 'D':F.nextColorDamp(-8);      break;
            case 'f':F.nextGColor1(1);         break;
            case 'F':F.nextGColor2(1);         break;
            case 'g':F.nextGradient( 1);       break;
            case 'G':F.nextGradient(-1);       break;
            case 'h':F.nextGradientShape(1);   break;
            case 'H':F.nextGradientShape(-1);  break;
            case 'j':F.toggleInversion();      break;
            case 'J':F.toggleFeedbackInvert(); break;
            case 'k':P.fore_grad=!P.fore_grad; break;
            case 'K':P.do_color_transform=!P.do_color_transform;break;
            case 'l':P.buf.toggleInterpolation(); break;
            case 'L':P.toggleAntialias(); break;
            case ';' :P.hue_rate = wrap(P.hue_rate-4,256); break;
            case '\'':P.hue_rate = wrap(P.hue_rate+4,256); break;
            case ':' :P.sat_rate = clip(P.sat_rate-4,-256,256); break;
            case '"' :P.sat_rate = clip(P.sat_rate+4,-256,256); break;
            case ',' :P.con_rate = clip(P.con_rate-4,-256,256); break;
            case '.' :P.con_rate = clip(P.con_rate+4,-256,256); break;
            case '<' :P.bri_rate = clip(P.bri_rate-4,-256,256); break;
            case '>' :P.bri_rate = clip(P.bri_rate+4,-256,256); break;
            case 'z':F.nextTintColor( 1); break;
            case 'Z':F.nextTintColor(-1); break;
            case 'x':F.nextTintLevel( 8); break;
            case 'X':F.nextTintLevel(-8); break;
            case 'c':draw_cursors = !draw_cursors; break;
            case 'C':draw_futures = !draw_futures; break;
            case 'v':if (current!=null) current.toggleWander(); break;
            case 'V':toggleScreensaver(); break;
            case 'b':P.text.toggle(); break;
            case 'B':P.text.toggleCursor(); break;
            case 'n':break;
            case 'N':P.show_notifications =!P.show_notifications; break;
            case 'm':F.nexMirrorMode(1); break;
            case 'M':P.draw_moths = !P.draw_moths; break;
            case '/':
            case '?':P.toggleShowHelp(); break;
            case ' ':P.save(); break;
        }
        mouseEntered(null);
    }
    
    /** A couple commands that only trigger on keyPressed
     * 
     * @param e 
     */
    @Override
    public void keyPressed(KeyEvent e) {
        FractalMap F = P.fractal;
        int code = e.getKeyCode();
        if (code==VK_ESCAPE||code==VK_STOP) System.exit(0); 
        
        if (entry_mode) switch (code) {
            case VK_LEFT      : P.text.left(); break;
            case VK_RIGHT     : P.text.right(); break;
            case VK_UP        : P.text.up(); break;
            case VK_DOWN      : P.text.down(); break;
            case VK_BACK_SPACE: P.text.backspace(); break;
            case VK_ENTER     : P.textToMap(); break;
            case VK_PAGE_UP   : P.text.scrollUp(); break;
            case VK_PAGE_DOWN : P.text.scrollDown(); break;
            case VK_CAPS_LOCK : P.save(); break;
            case VK_TAB       : P.text.toggleCursor(); break;
            case VK_ALT       : break;
            case VK_SHIFT     : break;
            case VK_CONTROL   : entry_mode=P.text.cursor_on=false; break;
            default:
            return;
        } 
        else if (presets_mode) {
            if (code==VK_ENTER) presets_mode = false;
            return;
            // Remaining codes handled by key pressed
        }
        else switch (code) {
            case VK_TAB      : break;
            case VK_CAPS_LOCK: break;
            case VK_ENTER    : presets_mode = true; break;
            case VK_CONTROL  : entry_mode=P.text.on=P.text.cursor_on=true; break;
            case VK_ALT      : break;
            case VK_UP       : F.setMotionBlur(F.motion_blur + 16); break;
            case VK_DOWN     : F.setMotionBlur(F.motion_blur - 16); break;
            case VK_LEFT     : P.setColorFilterWeight(P.blursharp_rate-16); break;
            case VK_RIGHT    : P.setColorFilterWeight(P.blursharp_rate+16); break;
            case VK_PAGE_UP  : P.mic.adjustVolume(0.2f); break;
            case VK_PAGE_DOWN: P.mic.adjustVolume(-0.2f); break;
            case VK_HOME     : P.mic.adjustSpeed(0.2f); break;
            case VK_END      : P.mic.adjustSpeed(-0.2f); break;
            case VK_INSERT   : break;
            case VK_DELETE   : P.toggleAnimation(); break;
            case VK_F1 : F.setMap("z*z"); break;
            case VK_F2 : F.setMap("z*abs(z)"); break;
            case VK_F3 : F.setMap("f/z+i*z"); break;
            case VK_F4 : F.setMap("z"); break;
            case VK_F5 : F.setMap("1/z"); break;
            case VK_F6 : F.setMap("e^z+e^(iz)"); break;
            case VK_F7 : F.setMap("conj(e^z+e^(z*e^(i*p/4)))"); break;
            case VK_F8 : F.setMap("z*z*e^(i*abs(z))"); break;
            case VK_F9 : F.setMap("abs(z)*e^(i*arg(z)*2)*2"); break;
            case VK_F10: F.setMap("z*e^(i*abs(z))*abs(z)/f"); break;
            case VK_F11: F.setMap("(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*"+F.W+"/"+F.H);break;
            case VK_F12: F.setMap("(imag(z)*i+ln(abs(real(z))))*.3/(abs(real(z)))*"+F.W+"/"+F.H);break;
        }
        syncCursors();
        mouseEntered(null);
    }
    @Override
    public void keyReleased(KeyEvent e) {}
    
    public synchronized void setPreset(int preset) {
        if (presets.length<=0) return;
        try {
            if (preset<0 || preset>=presets.length) {
                P.notify("Preset #"+preset+" isn't defined.");
                return;
            }
            preset_i = preset;
            Preset ps = presets[preset_i];
            ps.set(P);
            P.notify("Applied preset #"+preset+" ("+ps.name()+")");
        } catch (Exception e) {
            P.notify("Error applying preset " + preset);
            System.out.println("Error applying preset " + preset + ":");
            e.printStackTrace();
        }
    }
    public void nextPreset(int n) {
        setPreset(preset_i = wrap(preset_i + n, P.presets.length));
    }
    public synchronized void setAudio(boolean active) {
        if (active) this.active.addAll(audio_cursors);
        else this.active.removeAll(audio_cursors);
        checkCursor();
    }
    public synchronized void setFractal(boolean active) {
        if (active) this.active.addAll(map_cursors);
        else this.active.removeAll(map_cursors);
        checkCursor();
    }
    public synchronized void setTree(boolean active) {
        if (active) this.active.addAll(tree);
        else this.active.removeAll(tree);
        checkCursor();
        P.draw_tree = active;
    }

    // Number of dots (smoothing order) for cursors
    // Speed is logarithmic, 0 leaves unchanged.
    // Can range from -32 (1/16th) to 32 (16x)
    public static final int INITIAL_CURSOR_NDOTS = 10;
    public static final int INITIAL_SPEED        = 12;
    
    public abstract class Cursor  implements Comparable<Cursor> {
        public final String  name;
        // Mouse-controlled target position (we'll smoothly approach this)
        private final Point to;
        float   x, y; // Current cursor location
        int     speed    = INITIAL_SPEED;
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

        public synchronized void removeDot() {
            if (dx.length <= 1) return;
            float[] new_dx = new float[dx.length-1],
                    new_dy = new float[dy.length-1];
            System.arraycopy(dx, 0, new_dx, 0, new_dx.length);
            System.arraycopy(dy, 0, new_dy, 0, new_dy.length);
            dx = new_dx;
            dy = new_dy;
        }

        /** Speed control.
         * Speed can range from -32 to 32
         * local_rate gets 2^(speed/8)
         * So that exponent ranges from 2^-4 2^4 i.e. .125 to 16
         * @param amt 
         */
        public void adjustSpeed(int amt) {
            speed = clip(speed+amt,-32,64);      
        }

        public synchronized void setDestination(int x, int y) {
            to.x = clip(x,0,P.display_w);
            to.y = clip(y,0,P.display_h);
            if (current!=this) return;
            int X = clip((int) (x/xscale + .5f), 0, P.display_w);
            int Y = clip((int) (y/yscale + .5f), 0, P.display_h);
            moveTheMouse(X, Y);
        }
        
        public void mouseMoved(MouseEvent e) {
            setDestination((int)(xscale*e.getX()),(int)(yscale*e.getY()));
        }

        public void walk() {
            to.x = clip(to.x + (int)((random()-.5)*33), 0, P.display_w);
            to.y = clip(to.y + (int)((random()-.5)*33), 0, P.display_h);
        }

        /**
         * Drift rate should be greater than 0 and less than 1
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

        public synchronized void draw(Graphics G) {
            if (draw_futures) 
                for (int i = 0; i < dx.length; i++) 
                    G.drawImage(trail, (int) (dx[i]) - offset.x, (int) (dy[i]) - offset.y, null);
            G.drawImage(image, (int) (x) - offset.x, (int) (y) - offset.y, null);
        }
        public void  mouseDragged(MouseEvent e) {mouseMoved(e);}
        public void  toggleWander() {wander = !wander;}
        public int   compareTo(Cursor other) {return this.name.compareTo(other.name);}
        public int   nDots() {return dy.length;}
        // Used to set/retrieve data from presets
        public float x() {return to.x/(float)P.displayWidth();}
        public float y() {return to.y/(float)P.displayHeight();}
        public void  set(float x, float y) {
            setDestination(
                    (int)(x*P.displayWidth()+.5f),
                    (int)(y*P.displayHeight()+.5f)
            );}
    }
    
    Robot robot;
    private synchronized void moveTheMouse(int x, int y) {
        if (mouse.x==x && mouse.y==y) return;
        SwingUtilities.invokeLater(() -> {
            P.removeMouseListener(this);
            robot.mouseMove(x, y);
            mouse = new Point(x,y);
            P.addMouseListener(this);
        });
    }
    
    // Use a robot ro move mouse to control point for active cursor
    private synchronized void catchup() {
        if (current==null) return;
        if (robot  ==null) return;
        int x = clip((int) (current.to.x / xscale + 0.5), 0, P.displayWidth());
        int y = clip((int) (current.to.y / yscale + 0.5), 0, P.displayHeight());
        robot.mouseMove(x, y);
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
