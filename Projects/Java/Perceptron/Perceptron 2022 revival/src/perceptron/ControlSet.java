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
public final class ControlSet implements MouseListener, MouseMotionListener, KeyListener {
    //THESE OBJECTS ARE CONTROLLED AND MODIFIED BY THIS CLASS

    Perceptron P;
    Point      mouse_location = new Point(0, 0);
    
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
        branching_cursor,
        alpha_cursor,
        branch_length_cursor,
        tree_orientation_cursor,
        map_offset_cursor,
        map_rotation_cursor,
        gradient_cursor,
        tree_location_cursor;
    
    //Specific sets of cursors
    Set<Cursor> map_cursors, tree_cursors, audio_cursors, life_cursors;
    
    //Set of active cursors (cursors become active, inactive based on the
    //activation state of the controlled objects
    Set<Cursor> active_cursors, all_cursors;
    
    /// PRESETS
    Preset[] presets;

    public ControlSet(
            Perceptron P_,
            Preset[] user_presets) {
        presets = user_presets;

        P = P_;
        xscale = (float)(P.screen_width )/P.display_w ;
        yscale = (float)(P.screen_height)/P.display_h;

        try {robot = new Robot();} catch (AWTException e) {}

        branching_cursor = new Cursor("c1.png") {public void step(float rate) {
            super.step(rate);
            var a1 = (float)(x/P.halfScreenWidth() -1)*complex.TWOPI;
            var a2 = (float)(y/P.halfScreenHeight()-1)*complex.TWOPI;
            P.tree.form[0].set_beta(a1 + a2 / 2);
            P.tree.form[1].set_beta(a1 - a2 / 2);
        }};
        map_rotation_cursor = new Cursor("c2.jpg") {public void step(float rate) {
            super.step(rate);
            P.fractal.setNormalizedRotation(x, y);
        }};
        map_rotation_cursor.to.x = (int) (P.fractal.W - P.fractal.z2W - FractalMap.UL.real);
        map_rotation_cursor.to.y = P.halfScreenHeight();
        map_offset_cursor = new Cursor("c7.png") {public void step(float rate) {
            super.step(rate);
            P.fractal.setNormalizedConstant(x, y);
        }};
        tree_location_cursor = new Cursor("c5.png") {public void step(float rate) {
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
        branch_length_cursor = new Cursor("c3.png") {
        public void step(float rate) {
            super.step(rate);
            P.tree.form[0].d_r = (x / P.screenWidth() - .5f) * .5f + .7f;
            P.tree.form[1].d_r = (y / P.screenHeight() - .5f) * .5f + .7f;
        }};
        tree_orientation_cursor = new Cursor("iupiter.jpg") {
        public void step(float rate) {
            super.step(rate);
            P.tree.Y = rotation(3, 0, 2, (float)(x/P.halfScreenWidth ()-1)* complex.pi);
            P.tree.X = rotation(3, 1, 2, (float)(y/P.halfScreenHeight()-1)* complex.pi);
        }};
        gradient_cursor = new Cursor("c8.jpg") {
        public void step(float rate) {
            super.step(rate);
            P.fractal.setGradientParam(
                (float)pow(2.*x/P.screen_width,2),
                (float)y/P.screen_height*256-128);
        }};
        map_cursors  = Set.of(
                map_offset_cursor, 
                gradient_cursor, 
                map_rotation_cursor);
        tree_cursors = Set.of(
                //tree_orientation_cursor, 
                branching_cursor, 
                //alpha_cursor, 
                branch_length_cursor, 
                tree_location_cursor);
        audio_cursors = Set.of();
        all_cursors   = new HashSet<>();
        all_cursors.addAll(map_cursors);
        all_cursors.addAll(tree_cursors);
        all_cursors.addAll(audio_cursors);
        active_cursors = new HashSet<>();
        active_cursors.addAll(map_cursors);
        if (P.draw_tree) active_cursors.addAll(tree_cursors);
        //Start with the map cursor
        current = map_offset_cursor;
    }

    private Point bound(Point p) {
        p.x = clip(p.x,0,P.screen_width);
        p.y = clip(p.y,0,P.screen_height);
        return p;
    }
    
    public synchronized void advance(int framerate) {
        if (active_cursors.size() <= 0) return;
        if (screensaver) for (var c :active_cursors) c.walk();

        // User-controlled cursor moves quickly
        if (framerate < 1) framerate = 1;
        float focus_drift   = (float) min(1, 1 - 1 / (1 + rate / framerate));
        if (current != null && !screensaver)
            current.step(focus_drift);
        // Background cursors move slowly
        float unfocus_drift = (float) min(1, 1 - 1 / (1 + .4 * rate / framerate));
        for (var c :all_cursors)
            c.step(unfocus_drift);
    }

    synchronized void cursorSelection(int step) {
        //if there are no active cursors do nothing
        if (active_cursors.isEmpty()) {current = null; return;}
        Cursor[] activeArray = (Cursor[]) active_cursors.toArray(Cursor[]::new);
        Arrays.sort(activeArray);
        int index = Arrays.binarySearch(activeArray,current);
        if (index < 0) {
            index = 0;
        } else {
            int size = active_cursors.size();
            index = index + step + size;
            if (index < 0) 
                index = size - 1 - (-index % size);
            else if (index >= size)
                index %= size;
        }
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
        if (P.fractal.rotate_mode == P.fractal.ROTATION_LOCKED) {
            active_cursors.remove(map_rotation_cursor);
            checkCursor();
        } else if (!active_cursors.contains(map_rotation_cursor)) {
            active_cursors.add(map_rotation_cursor);
            checkCursor();
        }
        // The map cannot translate if translation is locked
        if (P.fractal.offset_mode == P.fractal.TRANSLATION_LOCKED) {
            active_cursors.remove(map_offset_cursor);
            checkCursor();
        } else if (!active_cursors.contains(map_offset_cursor)) {
            active_cursors.add(map_offset_cursor);
            checkCursor();
        }
        // Gradient zero is no gradient at all 
        if (P.fractal.grad_mode == 0) {
            active_cursors.remove(gradient_cursor);
            checkCursor();
        } else if (!active_cursors.contains(gradient_cursor)) {
            active_cursors.add(gradient_cursor);
            checkCursor();
        }
        // Is the tree on? 
        if (!P.draw_tree) {
            active_cursors.removeAll(tree_cursors);
            checkCursor();
        } else {
            active_cursors.addAll(tree_cursors);
            checkCursor();
        }
    }
    
    
    /**
     * Ensure one of the visible cursors is under user control.
     */
    public synchronized void checkCursor() {
        if (!active_cursors.contains(current)) {
            if (active_cursors.isEmpty()) current=null;
            else {
                current = active_cursors.iterator().next(); 
                catchup();
            }
        }
    }

    /** Draw all active controls
     * @param G */
    public synchronized void drawAll(Graphics G) {
        //if there are no active cursors do nothing
        if (active_cursors.size() <= 0 || !draw_cursors) 
            return;
        for (Cursor c :active_cursors) 
            c.draw(G);
        if (!screensaver) {
            G.drawImage(cursor_ring, (int) (mouse_location.x) - 32, (int) (mouse_location.y) - 32, null);
            if (current != null)
                G.drawImage(focus, (int) current.x - 11, (int) current.y - 11, null);
        }
    }



    ////////////////////////////////////////////////////////////////////////////
    //MOUSE LISTENER IMPLEMENTATION
    @Override
    public synchronized void mouseEntered(MouseEvent e) {
        P.stimulate();
        //update mouse location to that of the cursor, if possible
        if (current != null) {
            int x = max(0,min(P.physical_width(), (int) (current.to.x / xscale)));
            int y = max(0,min(P.physical_height(), (int) (current.to.y / yscale)));
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
        if (active_cursors.size() <= 0) return;
        mouse_location = bound(new Point(
                (int) (xscale * e.getX()),
                (int) (yscale * e.getY())));
        //System.out.println("Control mouseMoved "+e);
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
     * Some inputs can only be seen via KeyTyped events
     * 
     * @param e
     * 
     */
    @Override
    public void keyTyped(KeyEvent e) {
        if (entry_mode) return;
        if (presets_mode) return;
        FractalMap F = P.fractal;
        if (F==null) return;
        switch (e.getKeyChar()) {
            case '`':P.running = !P.running; break;
            case '~':break;
            case '1':F.setMap("i*ln(z)/2/P*"  +FractalMap.size.real); break;
            case '!':F.setMap("ln(z)/2/P*"    +FractalMap.size.imag); break;
            case '2':F.setMap("2*i*ln(z)/2/P*"+FractalMap.size.real); break;
            case '@':F.setMap("2*ln(z)/2/P*"  +FractalMap.size.imag); break;
            case '3':F.setMap("3*i*ln(z)/2/P*"+FractalMap.size.real); break;
            case '#':
            case '£':F.setMap("3*ln(z)/2/P*"  +FractalMap.size.imag); break;
            case '4':F.setMap("4*i*ln(z)/2/P*"+FractalMap.size.real); break;
            case '$':F.setMap("4*ln(z)/2/P*"  +FractalMap.size.imag); break;
            case '5':F.setMap("z/abs(sqrt((absz)^2-1.5))"); break;
            case '%':break;
            case '6':F.setMap("z-(z^3-1)/(3*z^2)"); break;
            case '^':F.setMap("z-(z^4-1)/(4*z^3)"); break;
            case '7':F.setMap("z^(1.5)");break;
            case '&':P.tree.toggleLeaves(); break;
            case '8':break;
            case '*':P.tree.toggleSymmetry(); break;
            case '9':break;
            case '0':break;
            case '(':F.nextNoise(-8);break;
            case ')':F.nextNoise(8);break;
            case '-':current.adjustSpeed(-1); break;
            case '_':P.draw_top_bars = !P.draw_top_bars; break;
            case '=':
            case '+':current.adjustSpeed(1); break;
            // TODO: add noise
            //case '\t':break;
            case 'q':F.nextMap(1); break;
            case 'Q':F.nextMap(-1);break;
            case 'w':F.nextOutside(1); break;
            case 'W':F.nextOutside(-1); break;
            case 'e':F.nextBound(1); break;
            case 'E':F.nextBound(-1); break;
            case 'r':P.buf.toggleReflection(); break;
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
            case 'h':F.nextGradientShape(1);  break;
            case 'H':F.nextGradientShape(-1);  break;
            case 'j':F.toggleInversion();      break;
            case 'J':F.toggleFeedbackInvert(); break;
            case 'k':P.fore_grad=!P.fore_grad; break;
            case 'K':P.do_color_transform=!P.do_color_transform;break;
            case 'l':P.buf.toggleInterpolation(); break;
            case 'L':P.toggleFancy(); break;
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
        if (entry_mode) switch (e.getKeyCode()) {
            case VK_LEFT      : P.text.left(); break;
            case VK_RIGHT     : P.text.right(); break;
            case VK_UP        : P.text.up(); break;
            case VK_DOWN      : P.text.down(); break;
            case VK_BACK_SPACE: P.text.backspace(); break;
            case VK_ENTER     : P.text.toMap(); break;
            case VK_PAGE_UP   : P.text.scrollUp(); break;
            case VK_PAGE_DOWN : P.text.scrollDown(); break;
            case VK_ESCAPE    : System.exit(0); break;
            case VK_CAPS_LOCK : P.save(); break;
            case VK_TAB       : P.text.toggleCursor(); break;
            case VK_ALT       : break;
            case VK_SHIFT     : break;
            case VK_CONTROL   : entry_mode=P.text.cursor_on=false; break;
            default:P.text.append(e.getKeyChar());
            return;
        } 
        else if (presets_mode) switch (e.getKeyCode()) {    
            case VK_ESCAPE:System.exit(0); break; 
            case VK_STOP:System.exit(0); break; 
            case VK_0:setPreset(0); break; 
            case VK_1:setPreset(1); break; 
            case VK_2:setPreset(2); break; 
            case VK_3:setPreset(3); break; 
            case VK_4:setPreset(4); break; 
            case VK_5:setPreset(5); break; 
            case VK_6:setPreset(6); break; 
            case VK_7:setPreset(7); break; 
            case VK_8:setPreset(8); break; 
            case VK_9:setPreset(9); break; 
            case VK_A:setPreset(10); break; 
            case VK_B:setPreset(11); break; 
            case VK_C:setPreset(12); break; 
            case VK_D:setPreset(13); break; 
            case VK_E:setPreset(14); break; 
            case VK_F:setPreset(15); break; 
            case VK_G:setPreset(16); break; 
            case VK_H:setPreset(17); break; 
            case VK_I:setPreset(18); break; 
            case VK_J:setPreset(19); break; 
            case VK_K:setPreset(20); break; 
            case VK_L:setPreset(21); break; 
            case VK_M:setPreset(22); break; 
            case VK_N:setPreset(23); break; 
            case VK_O:setPreset(24); break; 
            case VK_P:setPreset(25); break; 
            case VK_Q:setPreset(26); break; 
            case VK_R:setPreset(27); break; 
            case VK_S:setPreset(28); break; 
            case VK_T:setPreset(29); break; 
            case VK_U:setPreset(30); break; 
            case VK_V:setPreset(31); break; 
            case VK_W:setPreset(32); break; 
            case VK_X:setPreset(33); break; 
            case VK_Y:setPreset(34); break; 
            case VK_Z:setPreset(35); break; 
            case VK_F1:setPreset(36); break; 
            case VK_F2:setPreset(37); break; 
            case VK_F3:setPreset(38); break; 
            case VK_F4:setPreset(39); break; 
            case VK_F5:setPreset(40); break; 
            case VK_F6:setPreset(41); break; 
            case VK_F7:setPreset(42); break; 
            case VK_F8:setPreset(43); break; 
            case VK_F9:setPreset(44); break; 
            case VK_F10:setPreset(45); break; 
            case VK_F11:setPreset(46); break; 
            case VK_F12:setPreset(47); break; 
            case VK_BACK_QUOTE:setPreset(48); break; 
            case VK_QUOTE:setPreset(49); break; 
            case VK_OPEN_BRACKET:setPreset(50); break; 
            case VK_CLOSE_BRACKET:setPreset(51); break; 
            case VK_EQUALS:setPreset(52); break; 
            case VK_MINUS:setPreset(53); break; 
            case VK_SLASH:setPreset(54); break; 
            case VK_BACK_SPACE:setPreset(55); break; 
            case VK_COMMA:setPreset(56); break; 
            case VK_PERIOD:setPreset(57); break; 
            case VK_SPACE:setPreset(58); break; 
            case VK_SEMICOLON:setPreset(59); break; 
            case VK_BACK_SLASH:setPreset(60); break; 
            case VK_ENTER:presets_mode = false; break; 
            default:return;
        } 
        else switch (e.getKeyCode()) {
            case VK_ESCAPE   : System.exit(0); break;
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
            preset_i = wrap(preset,presets.length);
            Preset ps = presets[preset_i];
            ps.set(P);
        } catch (Exception e) {
            System.out.println("Error applying preset " + preset + ":");
            e.printStackTrace();
        }
    }
    public void nextPreset(int n) {
        preset_i = wrap(preset_i + n, P.presets.length);
        setPreset(preset_i);
    }
    public synchronized void setAudio(boolean active) {
        if (active) active_cursors.addAll(audio_cursors);
        else active_cursors.removeAll(audio_cursors);
        checkCursor();
    }
    public synchronized void setFractal(boolean active) {
        if (active) active_cursors.addAll(map_cursors);
        else active_cursors.removeAll(map_cursors);
        checkCursor();
    }
    public synchronized void setTree(boolean active) {
        if (active) active_cursors.addAll(tree_cursors);
        else active_cursors.removeAll(tree_cursors);
        checkCursor();
        P.draw_tree = active;
    }

    public abstract class Cursor  implements Comparable<Cursor> {
        public final String  name;
        // Mouse-controlled target position (we'll smoothly approach this)
        private final Point to;
        float   x, y; // Current cursor location
        double  local_rate = 1.;
        int     speed = 0;
        boolean wanderer = false;
        Image   image;
        Point   offset;
        // Delay-line smoothed intermediate positions ("future")
        float[] dx = new float[4], dy = new float[4]; 
        
        public Cursor(String imagename) {
            name   = imagename;
            to     = new Point(P.halfScreenWidth(), P.halfScreenHeight());
            image  = defaultImage(imagename);
            offset = new Point(image.getWidth(null)/2, image.getHeight(null)/2);
        }
        
        public synchronized void addDot() {
            if (dx.length > 50) return;
            float[] new_dx = new float[dx.length + 1];
            float[] new_dy = new float[dy.length + 1];
            System.arraycopy(dx, 0, new_dx, 0, dx.length);
            System.arraycopy(dy, 0, new_dy, 0, dy.length);
            new_dx[dx.length] = dx[dx.length - 1];
            new_dy[dy.length] = dy[dy.length - 1];
            dx = new_dx;
            dy = new_dy;
        }

        public synchronized void removeDot() {
            if (dx.length <= 1) return;
            float[] new_dx = new float[dx.length - 1];
            float[] new_dy = new float[dy.length - 1];
            System.arraycopy(dx, 0, new_dx, 0, new_dx.length);
            System.arraycopy(dy, 0, new_dy, 0, new_dy.length);
            dx = new_dx;
            dy = new_dy;
        }

        public void adjustSpeed(int amt) {
            speed = max(-32,min(32,speed+amt));            
            local_rate = pow(2, speed / 8.);
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

        public synchronized void step(float drift_rate) {
            if (wanderer) walk();
            drift_rate *= local_rate;
            if (drift_rate > 1) drift_rate = 1;
            dx[0] += (to.x-dx[0]) * drift_rate;
            dy[0] += (to.y-dy[0]) * drift_rate;
            int i = 1;
            while (i < dx.length) {
                dx[i] += (dx[i-1]-dx[i]) * drift_rate;
                dy[i] += (dy[i-1]-dy[i]) * drift_rate;
                i++;
            }
            i--;
            x += (dx[i] - x) * drift_rate;
            y += (dy[i] - y) * drift_rate;
        }

        public synchronized void draw(Graphics G) {
            if (draw_futures) 
                for (int i = 0; i < dx.length; i++) 
                    G.drawImage(trailer, (int) (dx[i]) - offset.x, (int) (dy[i]) - offset.y, null);
            G.drawImage(image, (int) (x) - offset.x, (int) (y) - offset.y, null);
        }
        public void mouseDragged(MouseEvent e) {mouseMoved(e);}
        public void toggleWander() {wanderer = !wanderer;}
        public int  compareTo(Cursor other) {return this.name.compareTo(other.name);}
        public int  nDots() {return dy.length;}

    }
    
    Robot robot;
    private synchronized void moveTheMouse(int x, int y) {
        if (mouse_location.x==x && mouse_location.y==y) return;
        SwingUtilities.invokeLater(() -> {
            P.removeMouseListener(this);
            robot.mouseMove(x, y);
            //System.out.println("robot to "+x+" "+y);
            mouse_location = new Point(x,y);
            P.addMouseListener(this);
        });
    }
    
    // Use a robot ro move mouse to control point for active cursor
    private synchronized void catchup() {
        if (current==null) return;
        if (robot  ==null) return;
        int x = clip((int) (current.to.x / xscale + 0.5), 0, P.display_w);
        int y = clip((int) (current.to.y / yscale + 0.5), 0, P.display_h);
        robot.mouseMove(x, y);
    }

    public double XBranchingCursor()       { return branching_cursor.to.x       /(double)P.screen_width; }
    public double XAlphaCursor()           { return alpha_cursor.to.x           /(double)P.screen_width; }
    public double XBranchLengthCursor()    { return branch_length_cursor.to.x   /(double)P.screen_width; }
    public double XTreeOrientationCursor() { return tree_orientation_cursor.to.x/(double)P.screen_width; }
    public double XMapCursor()             { return map_offset_cursor.to.x      /(double)P.screen_width; }
    public double XMapRotationCursor()     { return map_rotation_cursor.to.x    /(double)P.screen_width; }
    public double XGradientCursor()        { return gradient_cursor.to.x        /(double)P.screen_width; }
    public double XTreeLocationCursor()    { return tree_location_cursor.to.x   /(double)P.screen_width; }
    
    public double YBranchingCursor()       { return branching_cursor.to.y       /(double)P.screen_height; }
    public double YAlphaCursor()           { return alpha_cursor.to.y           /(double)P.screen_height; }
    public double YBranchLengthCursor()    { return branch_length_cursor.to.y   /(double)P.screen_height; }
    public double YTreeOrientationCursor() { return tree_orientation_cursor.to.y/(double)P.screen_height; }
    public double YMapCursor()             { return map_offset_cursor.to.y      /(double)P.screen_height; }
    public double YMapRotationCursor()     { return map_rotation_cursor.to.y    /(double)P.screen_height; }
    public double YGradientCursor()        { return gradient_cursor.to.y        /(double)P.screen_height; }
    public double YTreeLocationCursor()    { return tree_location_cursor.to.y   /(double)P.screen_height; }
    
    public void   setXBranchingCursor(double x)       { branching_cursor.to.x        = (int)(x*P.screen_width+0.5); }
    public void   setXAlphaCursor(double x)           { alpha_cursor.to.x            = (int)(x*P.screen_width+0.5); }
    public void   setXBranchLengthCursor(double x)    { branch_length_cursor.to.x    = (int)(x*P.screen_width+0.5); }
    public void   setXTreeOrientationCursor(double x) { tree_orientation_cursor.to.x = (int)(x*P.screen_width+0.5); }
    public void   setXMapCursor(double x)             { map_offset_cursor.to.x       = (int)(x*P.screen_width+0.5); }
    public void   setXMapRotationCursor(double x)     { map_rotation_cursor.to.x     = (int)(x*P.screen_width+0.5); }
    public void   setXGradientCursor(double x)        { gradient_cursor.to.x         = (int)(x*P.screen_width+0.5); }
    public void   setXTreeLocationCursor(double x)    { tree_location_cursor.to.x    = (int)(x*P.screen_width+0.5); }
     
    public void   setYBranchingCursor(double y)       { branching_cursor.to.y        = (int)(y*P.screen_height+0.5); }
    public void   setYAlphaCursor(double y)           { alpha_cursor.to.y            = (int)(y*P.screen_height+0.5); }
    public void   setYBranchLengthCursor(double y)    { branch_length_cursor.to.y    = (int)(y*P.screen_height+0.5); }
    public void   setYTreeOrientationCursor(double y) { tree_orientation_cursor.to.y = (int)(y*P.screen_height+0.5); }
    public void   setYMapCursor(double y)             { map_offset_cursor.to.y       = (int)(y*P.screen_height+0.5); }
    public void   setYMapRotationCursor(double y)     { map_rotation_cursor.to.y     = (int)(y*P.screen_height+0.5); }
    public void   setYGradientCursor(double y)        { gradient_cursor.to.y         = (int)(y*P.screen_height+0.5); }
    public void   setYTreeLocationCursor(double y)    { tree_location_cursor.to.y    = (int)(y*P.screen_height+0.5); }
    
    public void   setBranchingCursor(double x,double y)       { 
        branching_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
    public void   setAlphaCursor(double x,double y)           {
        alpha_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
    public void   setBranchLengthCursor(double x,double y)    {
        branch_length_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
    public void   setTreeOrientationCursor(double x,double y) {
        tree_orientation_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
    public void   setMapOffsetCursor(double x,double y)       {
        map_offset_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
    public void   setMapRotationCursor(double x,double y)     {
        map_rotation_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
    public void   setGradientCursor(double x,double y)        {
        gradient_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
    public void   setTreeLocationCursor(double x,double y)    {
        tree_location_cursor.setDestination(
                (int)(x*P.screen_width +0.5),
                (int)(y*P.screen_height+0.5));
    }
     
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Cursor sprites / pre-rendered. //////////////////////////////////////////
    public static final BufferedImage cursor_ring = cursorRing();
    public static final BufferedImage trailer = trailer();
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
