package perceptron;
/* CursorSet.java
 * Created on January 18, 2007, 2:55 AM
 * @author Michael Everett Rule
 */

import java.awt.AWTException;
import util.Matrix;
import util.ColorUtil;
import rendered3D.Tree3D;
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
import static perceptron.Misc.clip;

/**
 * 
 * @author mer49
 */
public final class ControlSet implements MouseListener, MouseMotionListener, KeyListener {
    //THESE OBJECTS ARE CONTROLLED AND MODIFIED BY THIS CLASS

    /** The Perceptron to which to bind Cursors */
    Perceptron P;
    
    /** The FractalMap to which to bind Cursors */
    FractalMap F;
    
    /** The Tree3D to which to bind Cursors */
    Tree3D tree;
    
    //MOUSE DATA
    //A robot to move the mouse to the location of selected cursors
    Robot robot;
    
    //known mouse location
    Point mouse_location = new Point(0, 0);
    
    //CURSOR DATA
    /** The rate at which the cursor position responds to mouse events. If
     *  this is set to one the cursor will follow the mouse exactly. If it
     *  is set to small values less than one, greater than zero the cursors
     *  will drift slowly to follow the mouse.
     */
    private float rate = 4f;
    
    //Scale from physical screen dimension to virtual screen dimensions
    float x_scalar, y_scalar;
    
    //CURSORS
    Cursor BranchingCursor,
            AlphaCursor,
            BranchLengthCursor,
            TreeOrientationCursor,
            MapCursor,
            MapRotationCursor,
            GradientCursor,
            ContrastCursor,
            TreeLocationCursor;
    //active cursor
    Cursor current;
    
    //weather or not to draw the cursors
    boolean draw_cursors = true;
    
    //Specific sets of cursors
    Set<Cursor> MapCursors, TreeCursors, AudioCursors, LifeCursors;
    
    //Set of active cursors (cursors become active, inactive based on the
    //activation state of the controlled objects
    Set<Cursor> active_cursors;
    Set<Cursor> all_cursors;
    
    /// PRESETS
    Preset[] presets;
    
    // Cursor cricle
    static BufferedImage create_cursor_ring() {
        var b = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        var g = ColorUtil.fancy(b.createGraphics());
        g.setColor(Color.WHITE);
        g.drawOval(2, 2, 60, 60);
        return b;
    }
    static final BufferedImage cursor_ring = create_cursor_ring();
    
    /**
     *
     */
    public int preset_number;
    
    /** 
     * Draw mouse trails. 
     */
    public boolean draw_futures;
    
    /**
     *
     */
    public boolean wanderer = false;
    
    /**
     * 
     */
    public boolean screensaver = false;

    /**
     * 
     * @param Cursor_percept
     * @param user_presets 
     */
    public ControlSet(
            Perceptron Cursor_percept,
            Preset[] user_presets) {
        presets = user_presets;

        P = Cursor_percept;
        F = P.fractal;
        tree = P.the_tree;

        x_scalar = (float) P.screen_width() / P.physical_width();
        y_scalar = (float) P.screen_height() / P.physical_height();

        //TRY TO INITIALIZE A ROBOT TO MOVE THE CURSOR
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Could not initialise a robot.");
        }
        initialise_cursors();
    }

    /** Cursor initialization */
    public void initialise_cursors() {
        BranchingCursor = new Cursor("c1.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                var a1 = (float) (x / P.half_screen_width() - 1) * complex.TWOPI;
                var a2 = (float) (y / P.half_screen_height()- 1) * complex.TWOPI;
                tree.form[0].set_beta(a1 + a2 / 2);
                tree.form[1].set_beta(a1 - a2 / 2);
            }};
        MapRotationCursor = new Cursor("c2.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                F.setNormalizedRotation(x, y);
            }};
        MapRotationCursor.destination.x = (int) (F.W - F.z_to_W_scalar - F.upper_left.real);
        MapRotationCursor.destination.y = P.half_screen_height();

        MapCursor = new Cursor("c7.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                F.setNormalizedConstant(x, y);
            }};
        TreeLocationCursor = new Cursor("c5.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                tree.location.x = (int) (.5 + x);
                tree.location.y = (int) (.5 + y);
            }};
        AlphaCursor = new Cursor("c4.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);

                var x_o = x - P.half_screen_width();
                var y_o = y - P.half_screen_height();

                if (x_o * x_o + y_o * y_o < 5) {
                    tree.form[0].set_alpha(0);
                    tree.form[1].set_alpha(0);
                } else {
                    float a1 = (float) (x / P.half_screen_width() - 1) * complex.TWOPI;
                    float a2 = (float) (y / P.half_screen_height() - 1) * complex.TWOPI;
                    tree.form[0].set_alpha(a1 + a2 / 2);
                    tree.form[1].set_alpha(a1 - a2 / 2);
                }
            }
        };
        BranchLengthCursor = new Cursor("c3.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                tree.form[0].d_r = (x / P.screen_width() - .5f) * .5f + .7f;
                tree.form[1].d_r = (y / P.screen_height() - .5f) * .5f + .7f;
            }
        };
        TreeOrientationCursor = new Cursor("iupiter.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                tree.Y = Matrix.rotation(3, 0, 2, (float) (x / P.half_screen_width() - 1) * complex.pi);
                tree.X = Matrix.rotation(3, 1, 2, (float) (y / P.half_screen_height() - 1) * complex.pi);
            }
        };
        GradientCursor = new Cursor("c8.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                F.setGradientParam(
                        (float) pow(2. * x / P.screen_width, 2),
                        (float) y / P.screen_height * 256 - 128);
            }
        };
        ContrastCursor = new Cursor("dragonfly.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                int X = 0xff & (int) (0xff * x / P.screen_width);
                int Y = 0xff & (int) (0xff * y / P.screen_height);
                F.setContrastParameters(X, Y);
            }
        };

        MapCursors     = Set.of(MapCursor, GradientCursor, MapRotationCursor, ContrastCursor);
        TreeCursors    = Set.of(TreeOrientationCursor, BranchingCursor, AlphaCursor, BranchLengthCursor, TreeLocationCursor);
        AudioCursors   = Set.of();
        all_cursors    = new HashSet<>();
        all_cursors.addAll(MapCursors);
        all_cursors.addAll(TreeCursors);
        all_cursors.addAll(AudioCursors);
        
        active_cursors = new HashSet<>();
        if (F.isActive())
            active_cursors.addAll(MapCursors);
        if (tree != null && tree.isActive())
            active_cursors.addAll(TreeCursors);

        //Start with the map cursor
        current = MapCursor;
        
        /** Draw mouse trails. */
        draw_futures = true;
    }

    /** Tests to see if a point is offscreen, and makes a correction if so */
    private Point bound(Point p) {
        if (p.x < 0) {
            p.x = 0;
        } else if (p.x >= P.screen_width()) {
            p.x = P.screen_width();
        }
        if (p.y < 0) {
            p.y = 0;
        } else if (p.y >= P.screen_height()) {
            p.y = P.screen_height();
        }
        return p;
    }

    /** Tells all the cursors to safely update to the same mouseEvent.
     * @param e */
    public void moveAll(MouseEvent e) {
        for (var c : active_cursors)
            c.destination = bound(e.getPoint());
    }

    /**
     *
     * @param framerate
     */
    public void advance(int framerate) {
        if (active_cursors.size() <= 0) return;
        
        if (screensaver) for (var c : active_cursors) c.walk();
        
        // User-controlled cursor moves quickly
        if (framerate < 1) framerate = 1;
        float focus_drift   = (float) min(1, 1 - 1 / (1 + rate / framerate));
        if (current != null && !screensaver)
            current.advance_location(focus_drift);

        // Background cursors move slowly
        float unfocus_drift = (float) min(1, 1 - 1 / (1 + .4 * rate / framerate));
        for (var c : all_cursors)
            c.advance_location(unfocus_drift);
    }

    /** Handle a cursor activation event : move to next or previous cursor*/
    void cursorSelection(int step) {
        //if there are no active cursors do nothing
        if (active_cursors.isEmpty()) {
            current = null;
            return;
        }
        
        Cursor[] activeArray = (Cursor[]) active_cursors.toArray(Cursor[]::new);
        Arrays.sort(activeArray);
        
        int index = Arrays.binarySearch(activeArray,current);
        
        if (index < 0) {
            index = 0;
        } else {
            int size = active_cursors.size();
            index = index + step + size;
            if (index < 0) {
                index = size - 1 - (-index % size);
            } else if (index >= size) {
                index %= size;
            }
        }

        current = activeArray[index];

        //update mouse location to that of the new cursor, if possible
        if (robot != null) {
            int x = (int) (current.x / x_scalar);
            if (x < 0) {
                x = 0;
            }
            if (x > P.physical_width()) {
                x = P.physical_width();
            }
            int y = (int) (current.y / y_scalar);
            if (y < 0) {
                y = 0;
            }
            if (y > P.physical_height()) {
                y = P.physical_height();
            }
            robot.mouseMove(x, y);
        }
    }
    
    /**
     * Ensure that cursor state is synchronize with Perceptron's state. 
     * Only cursors that control active rendering operation should be shown.
     */
    public void syncCursors() {
        
        // The map cannot rotate if the rotation is locked
        if (P.fractal.polar_type == FractalMap.ROTATION_LOCKED) {
            active_cursors.remove(MapRotationCursor);
            checkCursor();
        } else if (!active_cursors.contains(MapRotationCursor)) {
            active_cursors.add(MapRotationCursor);
            checkCursor();
        }
        
        // The map cannot translate if translation is locked
        if (P.fractal.ortho_type == FractalMap.TRANSLATION_LOCKED) {
            active_cursors.remove(MapCursor);
            checkCursor();
        } else if (!active_cursors.contains(MapCursor)) {
            active_cursors.add(MapCursor);
            checkCursor();
        }
        
        // Gradient zero is no gradient at all 
        if (P.fractal.grad_mode == 0) {
            active_cursors.remove(GradientCursor);
            checkCursor();
        } else if (!active_cursors.contains(GradientCursor)) {
            active_cursors.add(GradientCursor);
            checkCursor();
        }
        
        // We don't need the dragonfly if no color filters active
        if (P.fractal.color_i == 0) {
            active_cursors.remove(ContrastCursor);
            checkCursor();
        } else if (!active_cursors.contains(ContrastCursor)) {
            active_cursors.add(ContrastCursor);
            checkCursor();
        }
    }
    
    
    /**
     * Ensure one of the visible cursors is under user control.
     */
    public void checkCursor() {
        if (!active_cursors.contains(current))
            current = active_cursors.isEmpty()
                ? null
                : active_cursors.iterator().next();
    }

    /** Draw all active controls
     * @param G */
    public void drawAll(Graphics G) {
        //if there are no active cursors do nothing
        if (active_cursors.size() <= 0 || !draw_cursors) 
            return;
        for (Cursor c : active_cursors) 
            c.drawTo(G);
        if (!screensaver) {
            G.drawImage(cursor_ring, (int) (mouse_location.x) - 32, (int) (mouse_location.y) - 32, null);
            if (current != null)
                G.drawImage(focus, (int) current.x - 11, (int) current.y - 11, null);
        }
    }


    /** Press P for autopilot - the screensaver mode. */
    public void toggle_screensaver_mode() {
        screensaver = !screensaver;
    }

    ////////////////////////////////////////////////////////////////////////////
    //MOUSE LISTENER IMPLEMENTATION

    /**
     *
     * @param e
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        P.stimulate();
        //update mouse location to that of the cursor, if possible
        if (robot != null && current != null) {
            int x = max(0,min(P.physical_width(),
                    (int) (current.x / x_scalar)));
            int y = max(0,min(P.physical_height(),
                    (int) (current.y / y_scalar)));
            robot.mouseMove(x, y);
        }
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     *
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {
        P.stimulate();
        if (e.getButton() == MouseEvent.BUTTON1)
            cursorSelection(1);
        else if (e.getButton() == MouseEvent.BUTTON3)
            cursorSelection(-1);
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        P.stimulate();
        if (active_cursors.size() <= 0) return;
        mouse_location = bound(new Point(
                (int) (x_scalar * e.getX()),
                (int) (y_scalar * e.getY())));
        current.mouseMoved(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
    ////////////////////////////////////////////////////////////////////////////
    //KEY LISTENER IMPLEMENTATION
    // text entry mode while entering the equation by hand (press CTRL)
    boolean entry_mode = false;
    
    // presets mode unused - (works but should replace the help screen with new options then)
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
        switch (e.getKeyChar()) {
            case '`': P.running = !P.running;                             break;
            case '~': break;
            case '1': F.setMap("i*ln(z)/2/P*"  +FractalMap.size.real);    break;
            case '!': F.setMap("ln(z)/2/P*"    +FractalMap.size.imag);    break;
            case '2': F.setMap("2*i*ln(z)/2/P*"+FractalMap.size.real);    break;
            case '@': F.setMap("2*ln(z)/2/P*"  +FractalMap.size.imag);    break;
            case '3': F.setMap("3*i*ln(z)/2/P*"+FractalMap.size.real);    break;
            case '#': F.setMap("3*ln(z)/2/P*"  +FractalMap.size.imag);    break;
            case '4': F.setMap("4*i*ln(z)/2/P*"+FractalMap.size.real);    break;
            case '$': F.setMap("4*ln(z)/2/P*"  +FractalMap.size.imag);    break;
            case '5': F.setMap("z/abs(sqrt((absz)^2-1.5))");              break;
            // The Newton's method for z^3 - 1 = 0
            case '%': break;
            case '6':F.setMap("z-(z^3-1)/(3*z^2)"); break;
            case '^':F.setMap("z-(z^4-1)/(4*z^3)"); break;
            case '7':F.setMap("z^(1.5)");break;
            case '&': break;
            case '8':F.setMap(
                        "(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*"
                        + P.buff.out.W + "/" + P.buff.out.H);
                        break;
            case '*': break;
            case '9':F.setMap(
                        "(imag(z)*i+ln(abs(real(z))))*.3/(abs(real(z)))*"
                        + P.buff.out.W + "/" + P.buff.out.H);
                        break;
            case '(': break;
            case '0':F.setMap(
                        "(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*" 
                        + P.buff.out.W + "/" + P.buff.out.H);
                        break;
            case ')': break;
            case '-': current.incrementRateConstant(-1); break;
            case '_': P.draw_top_bars = !P.draw_top_bars; break;
            case '=': 
            case '+': current.incrementRateConstant(1); break;
            case '[': current.addDot(); break;
            case '{': break;
            case ']': current.removeDot(); break;
            case '}': break;
            case '\\': P.rotateImages=!P.rotateImages; break;
            case '|': P.draw_side_bars=!P.draw_side_bars; break;
        }
        mouseEntered(null);
    }
    
    /** A couple commands that only trigger on keyPressed
     * 
     * @param e 
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (entry_mode) {
            switch (e.getKeyCode()) {
                case VK_LEFT:       P.text.left();                        break;
                case VK_RIGHT:      P.text.right();                       break;
                case VK_UP:         P.text.up();                          break;
                case VK_DOWN:       P.text.down();                        break;
                case VK_BACK_SPACE: P.text.backspace();                   break;
                case VK_ENTER:      P.text.toMap();                       break;
                case VK_PAGE_UP:    P.text.scrollUp();                    break;
                case VK_PAGE_DOWN:  P.text.scrollDown();                  break;
                case VK_ESCAPE:     System.exit(0);                       break;
                case VK_CAPS_LOCK:  P.save();                             break;
                case VK_TAB:        P.text.toggle_cursor();               break;
                case VK_ALT:                                              break;
                case VK_SHIFT:                                            break;
                case VK_CONTROL:    entry_mode=P.text.cursor_on=false;    break;
                default:            P.text.append(e.getKeyChar());
                return;
            }
        } else if (presets_mode) {
            switch (e.getKeyCode()) {    
                case VK_ESCAPE:        System.exit(0);       break; 
                case VK_STOP:          System.exit(0);       break; 
                case VK_0:             applyPreset(0);       break; 
                case VK_1:             applyPreset(1);       break; 
                case VK_2:             applyPreset(2);       break; 
                case VK_3:             applyPreset(3);       break; 
                case VK_4:             applyPreset(4);       break; 
                case VK_5:             applyPreset(5);       break; 
                case VK_6:             applyPreset(6);       break; 
                case VK_7:             applyPreset(7);       break; 
                case VK_8:             applyPreset(8);       break; 
                case VK_9:             applyPreset(9);       break; 
                case VK_A:             applyPreset(10);      break; 
                case VK_B:             applyPreset(11);      break; 
                case VK_C:             applyPreset(12);      break; 
                case VK_D:             applyPreset(13);      break; 
                case VK_E:             applyPreset(14);      break; 
                case VK_F:             applyPreset(15);      break; 
                case VK_G:             applyPreset(16);      break; 
                case VK_H:             applyPreset(17);      break; 
                case VK_I:             applyPreset(18);      break; 
                case VK_J:             applyPreset(19);      break; 
                case VK_K:             applyPreset(20);      break; 
                case VK_L:             applyPreset(21);      break; 
                case VK_M:             applyPreset(22);      break; 
                case VK_N:             applyPreset(23);      break; 
                case VK_O:             applyPreset(24);      break; 
                case VK_P:             applyPreset(25);      break; 
                case VK_Q:             applyPreset(26);      break; 
                case VK_R:             applyPreset(27);      break; 
                case VK_S:             applyPreset(28);      break; 
                case VK_T:             applyPreset(29);      break; 
                case VK_U:             applyPreset(30);      break; 
                case VK_V:             applyPreset(31);      break; 
                case VK_W:             applyPreset(32);      break; 
                case VK_X:             applyPreset(33);      break; 
                case VK_Y:             applyPreset(34);      break; 
                case VK_Z:             applyPreset(35);      break; 
                case VK_F1:            applyPreset(36);      break; 
                case VK_F2:            applyPreset(37);      break; 
                case VK_F3:            applyPreset(38);      break; 
                case VK_F4:            applyPreset(39);      break; 
                case VK_F5:            applyPreset(40);      break; 
                case VK_F6:            applyPreset(41);      break; 
                case VK_F7:            applyPreset(42);      break; 
                case VK_F8:            applyPreset(43);      break; 
                case VK_F9:            applyPreset(44);      break; 
                case VK_F10:           applyPreset(45);      break; 
                case VK_F11:           applyPreset(46);      break; 
                case VK_F12:           applyPreset(47);      break; 
                case VK_BACK_QUOTE:    applyPreset(48);      break; 
                case VK_QUOTE:         applyPreset(49);      break; 
                case VK_OPEN_BRACKET:  applyPreset(50);      break; 
                case VK_CLOSE_BRACKET: applyPreset(51);      break; 
                case VK_EQUALS:        applyPreset(52);      break; 
                case VK_MINUS:         applyPreset(53);      break; 
                case VK_SLASH:         applyPreset(54);      break; 
                case VK_BACK_SPACE:    applyPreset(55);      break; 
                case VK_COMMA:         applyPreset(56);      break; 
                case VK_PERIOD:        applyPreset(57);      break; 
                case VK_SPACE:         applyPreset(58);      break; 
                case VK_SEMICOLON:     applyPreset(59);      break; 
                case VK_BACK_SLASH:    applyPreset(60);      break; 
                case VK_ENTER:         presets_mode = false; break; 
                default:return;
            }
        } else {
            switch (e.getKeyCode()) {
                case VK_ESCAPE:    System.exit(0);                        break;
                case VK_TAB:       P.text.toggle_cursor();                break;
                case VK_Q:         F.nextMap(1);                          break;
                case VK_W:         F.nextMap(-1);                         break;
                case VK_E:         F.nextOutsideColoring(1);              break;
                case VK_R:         F.nextBounds(1);                       break;
                case VK_T:         setTree(!tree.isActive());             break;
                case VK_Y:         P.buff.toggleReflection();             break;
                case VK_U:         P.toggleCapFramerate();                break;
                case VK_I:         P.buff.toggleInterpolation();          break;
                case VK_O:         draw_futures = !draw_futures;          break;
                case VK_P:         toggle_screensaver_mode();             break;
                case VK_CAPS_LOCK:                                        break;
                case VK_A:         P.toggleFancy();                       break;
                case VK_S:         P.toggleFramerateDisplay();            break;
                case VK_D:         F.bounds_invert=!F.bounds_invert;      break;
                case VK_F:         F.nextFader(1);                        break;
                case VK_G:         F.nextGradient(1);                     break;
                case VK_H:         F.nextAccent(1);                       break;
                case VK_J:         F.toggleInversion();                   break;
                case VK_K:         F.grad_switch^=0xff;                   break;
                case VK_L:         break;
                case VK_SEMICOLON: P.text.toggle();                       break;
                case VK_QUOTE:     F.nextGradientShape(1);                break;
                case VK_ENTER:     presets_mode = true;                   break;
                case VK_Z:         P.incrementSketch(-1);                 break;
                case VK_X:         P.incrementSketch(1); syncCursors();   break;
                case VK_C:         draw_cursors = !draw_cursors;          break;
                case VK_V:         F.toggleFadeColorSmoothing();          break;
                case VK_B:         P.toggleObjectsOnTop();                break;
                case VK_N:         F.nextColorFilter(1);                  break;
                case VK_M:         P.draw_moths = !P.draw_moths;          break;
                case VK_COMMA:     F.inc_ortho(); syncCursors();          break;
                case VK_PERIOD:    F.inc_polar(); syncCursors();          break;
                case VK_PROPS:     
                case VK_SLASH:     P.helpScreen();                        break;
                case VK_DELETE:    current.toggleWander();                break;
                case VK_CONTROL:   entry_mode=P.text.on=P.text.cursor_on=true;break;
                case VK_ALT:                                                 break;
                case VK_SPACE:     P.save();                                 break;
                case VK_UP:        F.setMotionBlur(F.motionblurp - 16);      break;
                case VK_DOWN:      F.setMotionBlur(F.motionblurp + 16);      break;
                case VK_LEFT:      F.setColorFilterWeight(F.filterweight-16);break;
                case VK_RIGHT:     F.setColorFilterWeight(F.filterweight+16);break;
                case VK_HOME:      P.toggleAnimation();                      break;
                case VK_INSERT:                                              break;
                case VK_PAGE_UP:                                             break;
                case VK_PAGE_DOWN:                                           break;
                case VK_F1:        F.setMap("z*z");                          break;
                case VK_F2:        F.setMap("z*abs(z)");                     break;
                case VK_F3:        F.setMap("f/z+i*z");                      break;
                case VK_F4:        F.setMap("z");                            break;
                case VK_F5:        F.setMap("1/z");                          break;
                case VK_F6:        F.setMap("e^z+e^(iz)");                   break;
                case VK_F7:        F.setMap("conj(e^z+e^(z*e^(i*p/4)))");    break;
                case VK_F8:        F.setMap("z*z*e^(i*abs(z))");             break;
                case VK_F9:        F.setMap("abs(z)*e^(i*arg(z)*2)*2");      break;
                case VK_F10:       F.setMap("z*e^(i*abs(z))*abs(z)/f");      break;
                case VK_F11:       F.setMap("(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*"+ P.buff.out.W + "/" + P.buff.out.H);break;
                case VK_F12:       F.setMap("(imag(z)*i+ln(abs(real(z))))*.3/(abs(real(z)))*"+ P.buff.out.W + "/" + P.buff.out.H);break;
            }
        }
        syncCursors();
        mouseEntered(null);
    }
    
    // Keys that use shift seem to ??

    /** Apply preset.
     * @param preset */
    public void applyPreset(int preset) {
        try {
            presets[preset].set(P);
        } catch (Exception e) {
            System.out.println("Error applying preset " + preset + ":");
            e.printStackTrace();
        }
    }

    /** Load the preset by cycling through a number of
     * different presets in the resource folder.
     * @param n */
    public void incrementPreset(int n) {
        preset_number = preset_number + n;
        int number_of_presets = P.user_presets.length;
        if (preset_number == number_of_presets)
            preset_number = 0;
        if (preset_number == -1)
            preset_number = number_of_presets - 1;
        applyPreset(preset_number);
    }

    /**
     *
     * @param active
     */
    public void setAudio(boolean active) {
        if (active) active_cursors.addAll(AudioCursors);
        else active_cursors.removeAll(AudioCursors);
        checkCursor();
    }

    /**
     *
     * @param active
     */
    public void setFractal(boolean active) {
        if (active) active_cursors.addAll(MapCursors);
        else active_cursors.removeAll(MapCursors);
        checkCursor();
        F.setActive(active);
    }

    /**
     *
     * @param active
     */
    public void setTree(boolean active) {
        if (active) active_cursors.addAll(TreeCursors);
        else active_cursors.removeAll(TreeCursors);
        checkCursor();
        tree.setActive(active);
    }

    /**
     *
     * @param e
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }


////////////////////////////////////////////////////////////////////////////
//INNER CLASSES
    /** A class representing an object that responds to mouse envents in ways
     *  similar to the normal mouse curosor */
    abstract class Cursor  implements Comparable<Cursor> {

        /** The point on the screen that the virtual cursor should finally come
         *  to rest at. */
        protected Point destination;
        /** The point where the cursor is currently located */
        protected float x, y;
        /** err... thingy */
        protected float[] dx = new float[2], dy = new float[2];
        protected double local_rate = 1.;
        protected int rate_constant = 0;
        protected String myname;
        //protected boolean wanderer = false; // moved to top to be visible

        public synchronized void addDot() {
            if (dx.length > 250) {
                return;
            }
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

        public void incrementRateConstant(int amt) {
            rate_constant = max(-32,min(32,rate_constant+amt));            
            local_rate = pow(2, rate_constant / 8.);
        }
        
        /** A BufferedImage representing this cursor when drawn on screen */
        protected Image image;
        
        /** The center of the cursor */
        protected Point offset;

        public Cursor(String imagename) {
            myname = imagename;
            destination = new Point(P.half_screen_width(), P.half_screen_height());
            image = defaultImage(imagename);
            offset = new Point(image.getWidth(null) / 2, image.getHeight(null) / 2);
        }

        public void mouseMoved(MouseEvent e) {
            destination.x = (int) (x_scalar * e.getX());
            destination.y = (int) (y_scalar * e.getY());
            bound(destination);
        }

        public void mouseDragged(MouseEvent e) {
            mouseMoved(e);
        }

        public void toggleWander() {
            wanderer = !wanderer;
        }

        //OTHER MEMBER FUNCTIONS
        /** Random walker */
        public synchronized void walk() {
            destination.x = clip(
                    destination.x + (int)(random()-.5) * 44,
                    0, P.physical_width());
            destination.y  = clip(
                    destination.y + (int)(random()-.5) * 44,
                    0, P.physical_height()-1);
        }

        /** Advances this cursor towards its destination at a
         *  custom drift_rate */
        public synchronized void advance_location(float drift_rate) {
            if (wanderer) walk();

            drift_rate *= local_rate;
            if (drift_rate > 1) drift_rate = 1;
            dx[0] += (destination.x - dx[0]) * drift_rate;
            dy[0] += (destination.y - dy[0]) * drift_rate;
            int i = 1;
            while (i < dx.length) {
                dx[i] += (dx[i - 1] - dx[i]) * drift_rate;
                dy[i] += (dy[i - 1] - dy[i]) * drift_rate;
                i++;
            }
            i--;
            x += (dx[i] - x) * drift_rate;
            y += (dy[i] - y) * drift_rate;
        }

        /** Draws this cursor to the given Graphics in the given Dimension */
        public synchronized void drawTo(Graphics G) {
            if (draw_futures) {
                for (int i = 0; i < dx.length; i++) {
                    G.drawImage(trailer, (int) (dx[i]) - offset.x, (int) (dy[i]) - offset.y, null);
                }
            }
            G.drawImage(image, (int) (x) - offset.x, (int) (y) - offset.y, null);
        }
        

        @Override
        public int compareTo(Cursor other)  // compareTo method performs the comparisons 
        {
            return this.myname.compareTo(other.myname);
        }

        public int nDots() {
            return dy.length;
        }
    }
    static BufferedImage trailer = trailer();

    static BufferedImage trailer() {
        BufferedImage b = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtil.fancy(b.createGraphics());
        image_graphics.setColor(new Color(255, 255, 255, 128));
        image_graphics.drawOval(19, 19, 3, 3);
        return b;
    }
    static BufferedImage focus = focus();

    static BufferedImage focus() {
        BufferedImage b = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtil.fancy(b.createGraphics());
        image_graphics.setColor(Color.WHITE);
        image_graphics.drawOval(1, 1, 19, 19);
        return b;
    }

    /**
     * Default cursor image
     */
    private static BufferedImage defaultImage(String imagename) {
        var b = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        var g = ColorUtil.fancy(b.createGraphics());
        g.fillOval(13, 13, 13, 13);
        try {
            BufferedImage image1 = ImageIO.read(new File("resource/cursors/" + imagename));
            for (int y = 0; y < 40; y++)
                for (int x = 0; x < 40; x++) 
                    b.setRGB(x, y, b.getRGB(x, y) & 0xff000000 | 0x00ffffff & image1.getRGB(x, y));
        } catch (IOException e) {
        }
        return b;
    }

    /**
     *
     * @return
     */
    public double XBranchingCursor() {
        return BranchingCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double XAlphaCursor() {
        return AlphaCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double XBranchLengthCursor() {
        return BranchLengthCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double XTreeOrientationCursor() {
        return TreeOrientationCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double XMapCursor() {
        return MapCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double XMapRotationCursor() {
        return MapRotationCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double XGradientCursor() {
        return GradientCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double XTreeLocationCursor() {
        return TreeLocationCursor.destination.x;
    }

    /**
     *
     * @return
     */
    public double YBranchingCursor() {
        return BranchingCursor.destination.y;
    }

    /**
     *
     * @return
     */
    public double YAlphaCursor() {
        return AlphaCursor.destination.y;
    }

    /**
     *
     * @return
     */
    public double YBranchLengthCursor() {
        return BranchLengthCursor.destination.y;
    }

    /**
     *
     * @return
     */
    public double YTreeOrientationCursor() {
        return TreeOrientationCursor.destination.y;
    }

    /**
     *
     * @return
     */
    public double YMapCursor() {
        return MapCursor.destination.y;
    }

    /**
     *
     * @return
     */
    public double YMapRotationCursor() {
        return MapRotationCursor.destination.y;
    }

    /**
     *
     * @return
     */
    public double YGradientCursor() {
        return GradientCursor.destination.y;
    }

    /**
     *
     * @return
     */
    public double YTreeLocationCursor() {
        return TreeLocationCursor.destination.y;
    }

    /**
     *
     * @param x
     */
    public void setXBranchingCursor(double x) {
        BranchingCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setXAlphaCursor(double x) {
        AlphaCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setXBranchLengthCursor(double x) {
        BranchLengthCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setXTreeOrientationCursor(double x) {
        TreeOrientationCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setXMapCursor(double x) {
        MapCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setXMapRotationCursor(double x) {
        MapRotationCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setXGradientCursor(double x) {
        GradientCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setXTreeLocationCursor(double x) {
        TreeLocationCursor.destination.x = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYBranchingCursor(double x) {
        BranchingCursor.destination.y = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYAlphaCursor(double x) {
        AlphaCursor.destination.y = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYBranchLengthCursor(double x) {
        BranchLengthCursor.destination.y = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYTreeOrientationCursor(double x) {
        TreeOrientationCursor.destination.y = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYMapCursor(double x) {
        MapCursor.destination.y = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYMapRotationCursor(double x) {
        MapRotationCursor.destination.y = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYGradientCursor(double x) {
        GradientCursor.destination.y = (int) x;
    }

    /**
     *
     * @param x
     */
    public void setYTreeLocationCursor(double x) {
        TreeLocationCursor.destination.y = (int) x;
    }
}
