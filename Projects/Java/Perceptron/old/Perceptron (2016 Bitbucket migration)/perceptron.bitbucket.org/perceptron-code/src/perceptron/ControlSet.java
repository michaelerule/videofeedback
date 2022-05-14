package perceptron;

import math.complex;
import rendered3D.Tree3D;
import util.ColorUtility;
import util.Matrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static java.awt.event.KeyEvent.*;

/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 * @author Predrag Bokšić (junkerade@gmail.com)
 *         <p/>
 *         Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *         It evolves colored geometric patterns and visual images into the realm of infinite details
 *         and deepens the thought. </p>
 *         <p/>
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 */


public final class ControlSet implements MouseListener, MouseMotionListener, KeyListener {

    /**
     * The Perceptron to which to bind Cursors
     */
    Perceptron percept;
    /**
     * The FractalMap to which to bind Cursors
     */
    FractalMap fractal;
    /**
     * The Tree3D to which to bind Cursors
     */
    Tree3D tree;
    // A robot to move the mouse to the location of selected cursors.
    Robot robot;
    // Known mouse location
    Point mouse_location;
    // CURSOR DATA
    // Scale from physical screen dimension to virtual screen dimensions
    float x_scaler, y_scaler;
    // CURSORS
    Cursor BranchingCursor, AlphaCursor, BranchLengthCursor, TreeOrientationCursor, MapCursor, MapRotationCursor,
            GradientCursor, ContrastCursor, TreeLocationCursor, TempCursor;
    // active cursor
    Cursor current_cursor;
    // Specific sets of cursors
    Cursor[] MapCursors, TreeCursors, AudioCursors, LifeCursors;
    /**
     * Set of active cursors (cursors become active, inactive based on the
     * activation state of the controlled objects
     */
    ArrayList<Cursor> active_cursors;
    ArrayList<Cursor> all_cursors;
    static String cursors_location = "resource/cursors";
    // PRESETS
    public int preset_number;
    // weather or not to draw the cursors (mouse pointers). Press c.
    boolean draw_cursors = true;
    /**
     * Draw mouse trails.
     */
    boolean draw_futures = true;
    // toggle random walk
    protected boolean wanderer = false;
    // autopilot
    boolean screensaver = false;
    // text entry mode while entering the equation by hand (press CTRL)
    boolean ENTRY_MODE = false;
    // presets mode unused - TODO: enable presets mode, update the help screen
    boolean PRESETS_MODE = false;


    /**
     * CONSTRUCTOR
     */
    public ControlSet(Perceptron p) {

        percept = p;
        fractal = percept.fractal;
        tree = percept.the_tree;

        mouse_location = new Point(0, 0);

        // scaling for cursor coordinates in windowed and in fullscreen mode
        if (percept.windowed_mode) {
            x_scaler = 1.0f;
            y_scaler = 1.0f;
        } else {
            x_scaler = (float) percept.screen_width() / (float) percept.physical_width();
            y_scaler = (float) percept.screen_height() / (float) percept.physical_height();
        }

        // TRY TO INITIALIZE A ROBOT TO MOVE THE CURSOR
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            System.err.println("Could not initialise the mouse robot.");
        }

        initialise_cursors();

    }


    /**
     * Cursor initialization.
     */
    final void initialise_cursors() {

        BranchingCursor = new Cursor("c1.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                float a1 = (x / percept.half_screen_width() - 1) * complex.two_pi;
                float a2 = (y / percept.half_screen_height() - 1) * complex.two_pi;
                tree.form[0].set_beta(a1 + a2 / 2);
                tree.form[1].set_beta(a1 - a2 / 2);
            }
        };

        MapRotationCursor = new Cursor("c2.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                fractal.set_normalized_rotation(x, y);
            }
        };

        MapRotationCursor.destination.x = (int) (fractal.W - fractal.complex_to_W_scalar - FractalMap.upper_left.real);
        MapRotationCursor.destination.y = percept.half_screen_height();

        MapCursor = new Cursor("c7.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                fractal.set_norm_c(x, y);
            }
        };

        TreeLocationCursor = new Cursor("c5.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                tree.location.x = (int) (.5 + x);
                tree.location.y = (int) (.5 + y);
            }
        };

        AlphaCursor = new Cursor("c4.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);

                float x_o = x - percept.half_screen_width();
                float y_o = y - percept.half_screen_height();

                if (x_o * x_o + y_o * y_o < 5) {
                    tree.form[0].set_alpha(0);
                    tree.form[1].set_alpha(0);
                } else {
                    float a1 = (x / percept.half_screen_width() - 1) * complex.two_pi;
                    float a2 = (y / percept.half_screen_height() - 1) * complex.two_pi;
                    tree.form[0].set_alpha(a1 + a2 / 2);
                    tree.form[1].set_alpha(a1 - a2 / 2);
                }
            }
        };

        BranchLengthCursor = new Cursor("c3.png") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                tree.form[0].d_r = (x / percept.screen_width() - .5f) * .5f + .7f;
                tree.form[1].d_r = (y / percept.screen_height() - .5f) * .5f + .7f;
            }
        };

        TreeOrientationCursor = new Cursor("iupiter.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                tree.Y = Matrix.rotation(3, 0, 2, (x / percept.half_screen_width() - 1) * complex.pi);
                tree.X = Matrix.rotation(3, 1, 2, (y / percept.half_screen_height() - 1) * complex.pi);
            }
        };

        GradientCursor = new Cursor("c8.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                fractal.set_gradient_param((float) Math.pow(2. * x / percept.screen_width, 2), y / percept.screen_height * 256 - 128);
            }
        };

        ContrastCursor = new Cursor("dragonfly.jpg") {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
                int X = 0xff & (int) (0xff * x / percept.screen_width);
                int Y = 0xff & (int) (0xff * y / percept.screen_height);
                fractal.setContrastParameters(X, Y);
            }
        };

        TempCursor = new Cursor(null) {
            @Override
            public void advance_location(float rate) {
                super.advance_location(rate);
            }
        };

        MapCursors = new Cursor[]{MapCursor, GradientCursor, MapRotationCursor, ContrastCursor};
        TreeCursors = new Cursor[]{TreeOrientationCursor, BranchingCursor, AlphaCursor, BranchLengthCursor, TreeLocationCursor};
        AudioCursors = new Cursor[]{};

        active_cursors = new ArrayList<>();
        all_cursors = new ArrayList<>();
        active_cursors.addAll(Arrays.asList(MapCursors));

        if (tree != null) {
            if (tree.is_active()) {
                active_cursors.addAll(Arrays.asList(TreeCursors));
            }
        }

        for (Cursor[] set : new Cursor[][]{MapCursors, TreeCursors, AudioCursors}) {
            all_cursors.addAll(Arrays.asList(set));
        }

        /**
         * Start with the map cursor.
         */
        current_cursor = MapCursor;
        /**
         * Draw mouse trails.
         */
        draw_futures = true;

    }


    /**
     * Custom cursor object that responds to mouse events in ways similar to the normal mouse cursor.
     */
    abstract class Cursor {

        /**
         * The point where the cursor is currently located.
         */
        protected float x = 0, y = 0;
        /**
         * The center of the cursor depends from the image that represents cursor.
         */
        protected Point offset;
        /**
         * The point on the screen that the virtual cursor should finally come
         * to rest at.
         */
        protected Point destination;
        /**
         * motion of cursors
         */
        protected float[] dx = new float[2], dy = new float[2];
        protected double local_rate = 1.0d;
        protected int rate_constant = 0;
        /**
         * A BufferedImage that represents cursor when drawn on screen.
         */
        protected Image image;


        /**
         * Constructor of the abstract object Cursor. Temp cursor has null image, so corrections are made thereof.
         */
        public Cursor(String imagename) {
            offset = new Point(0, 0);
            destination = new Point(percept.half_screen_width(), percept.half_screen_height());
            if (imagename != null) {
                image = default_image(imagename);
                offset = new Point(image.getWidth(null) / 2, image.getHeight(null) / 2);
            }
        }


        /**
         * Default cursor image.
         */
        private BufferedImage default_image(String imagename) {
            BufferedImage b = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
            image_graphics.fillOval(13, 13, 13, 13);
            // platform compatibility for pathnames
            try {
                if (File.separatorChar == '\\') {
                    cursors_location = cursors_location.replace('/', File.separatorChar);
                    cursors_location = cursors_location.replace('\\', File.separatorChar);
                    System.out.println("loading cursor: \"" + cursors_location + File.separatorChar + imagename + "\"");
                }
                if (File.separatorChar == '/') {
                    cursors_location = cursors_location.replace('\\', File.separatorChar);
                    cursors_location = cursors_location.replace('/', File.separatorChar);
                    System.out.println("loading cursor: \"" + cursors_location + File.separatorChar + imagename + "\"");
                }
                BufferedImage image = ImageIO.read(new File(cursors_location + File.separatorChar + imagename));
                for (int y = 0; y < 40; y++) {
                    for (int x = 0; x < 40; x++) {
                        int C;
                        C = image.getRGB(x, y);
                        b.setRGB(x, y, b.getRGB(x, y) & 0xff000000 | 0x00ffffff & C);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return b;
        }


        /**
         * Add dot dragging behind the cursor.
         */
        public void add_dot() {
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


        /**
         * Remove dot dragging behind the cursor.
         */
        public void remove_dot() {
            if (dx.length <= 1) {
                return;
            }
            float[] new_dx = new float[dx.length - 1];
            float[] new_dy = new float[dy.length - 1];
            System.arraycopy(dx, 0, new_dx, 0, new_dx.length);
            System.arraycopy(dy, 0, new_dy, 0, new_dy.length);
            dx = new_dx;
            dy = new_dy;
        }


        /**
         * Set the speed of delayed motion (dragging) of cursor.
         */
        public void increment_rate_constant(int amt) {
            rate_constant += amt;
            if (rate_constant > 32) {
                rate_constant = 32;
            } else if (rate_constant < -32) {
                rate_constant = -32;
            }
            local_rate = Math.pow(2, rate_constant / 8.);
        }


        /**
         * Mouse event handler in abstract Cursor class. If the large circle is moving linearly with the mouse,
         * the small circle will be around the selected cursor and move this many times slower or faster, so the scaler
         * is here to enable a match between the mouse motion on screen and in program, large and small circles and the
         * cursor.
         */
        public void mouseMoved(MouseEvent e) {
            destination.x = (int) (x_scaler * (e.getX() - percept.window_insets.left));
            destination.y = (int) (y_scaler * (e.getY() - percept.window_insets.top));
            //System.out.println(destination.x + "  " + destination.y);
            bound(destination);
        }


        /**
         * Mouse event handler in abstract Cursor class.
         */
        public void mouseDragged(MouseEvent e) {
            mouseMoved(e);
        }


        /**
         * Toggle random walk for mouse (cursors).
         */
        public void toggle_wander() {
            wanderer = !wanderer;
            if (percept.sw != null) {
                if (wanderer) {
                    percept.sw.jtb_wander.setSelected(true);
                } else {
                    percept.sw.jtb_wander.setSelected(false);
                }
            }
        }


        /**
         * Random walker for cursor(s).
         */
        public void walk() {
            destination.x += (int) ((Math.random() - .5) * 44);
            if (destination.x < 0) {
                destination.x = 0;
            } else if (destination.x >= percept.physical_width()) {
                destination.x = percept.physical_width() - 1;
            }
            destination.y += (int) ((Math.random() - .5) * 44);
            if (destination.y < 0) {
                destination.y = 0;
            } else if (destination.y >= percept.physical_height()) {
                destination.y = percept.physical_height() - 1;
            }
        }


        /**
         * Advances this cursor towards its destination at a custom drift_rate
         */
        public void advance_location(float drift_rate) {
            if (wanderer) {
                walk();
            }
            drift_rate *= local_rate;
            if (drift_rate > 1) {
                drift_rate = 1;
            }
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


        BufferedImage trailer = create_trailer();

        /**
         * Draws this cursor to the given Graphics in the given Dimension
         */
        public void drawTo(Graphics G) {
            if (draw_futures && image != null) {
                for (int i = 0; i < dx.length; i++) {
                    G.drawImage(trailer, (int) (dx[i]) - offset.x, (int) (dy[i]) - offset.y, null);
                }
            }
            if (image != null)
                G.drawImage(image, (int) (x) - offset.x, (int) (y) - offset.y, null);
        }

    }


    BufferedImage cursor_ring = create_cursor_ring();

    /**
     * Draw a large circle around the selected cursor.
     */
    BufferedImage create_cursor_ring() {
        BufferedImage b = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
        image_graphics.setColor(Color.WHITE);
        image_graphics.drawOval(2, 2, 60, 60);
        return b;
    }


    BufferedImage cursor_ring_small = create_cursor_ring_small();

    /**
     * Draw a small circle within the large circle around the selected cursor.
     */
    BufferedImage create_cursor_ring_small() {
        BufferedImage b = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
        image_graphics.setColor(Color.WHITE);
        image_graphics.drawOval(1, 1, 19, 19);
        return b;
    }


    /**
     * Mouse cursor is leaving trails of varying length, dragging behind the selected cursor.
     */
    BufferedImage create_trailer() {
        BufferedImage b = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
        image_graphics.setColor(new Color(255, 255, 255, 128));
        image_graphics.drawOval(19, 19, 3, 3);
        return b;
    }


    /**
     * Tests to see if a point is offscreen, and makes a correction if so.
     */
    private Point bound(Point p) {
        if (p.x < 0) {
            p.x = 0;
        } else if (p.x >= percept.screen_width()) {
            p.x = percept.screen_width();
        }
        if (p.y < 0) {
            p.y = 0;
        } else if (p.y >= percept.screen_height()) {
            p.y = percept.screen_height();
        }
        return p;
    }


    /**
     * Tells all the cursors to safely update to the same mouseEvent.
     */
    public void moveAll(MouseEvent e) {
        for (Cursor c : active_cursors) {
            c.destination = bound(e.getPoint());
        }
    }


    public void advance(int framerate) {
        // screensaver mode randomly walks cursors
        if (screensaver) {
            for (Cursor active_cursor : active_cursors) {
                active_cursor.walk();
            }
        }
        if (active_cursors.size() <= 0) {
            return;
        }
        if (framerate < 1) {
            framerate = 1;
        }

        /**
         The rate at which the cursor position responds to mouse events. If this
         is set to one the cursor will follow the mouse exactly. If it is set to
         small values less than one, greater than zero the cursors will drift
         slowly to follow the mouse. Default is 6f. We set this in options.
         */
        float rate = 6f;
        float focus_drift = Math.min(1, 1 - 1 / (1 + rate / framerate));
        float unfocus_drift = (float) Math.min(1, 1 - 1 / (1 + .4 * rate / framerate));
        if (current_cursor != null && !screensaver) {
            current_cursor.advance_location(focus_drift);
        }
        for (Cursor c : all_cursors) {
            c.advance_location(unfocus_drift);
        }
    }


    /**
     * Click left or right mouse button to select the next or previous cursor. Handles a cursor activation event:
     * moves to next or previous cursor. Transforms mouse coordinates to the appropriate numbers that can be used
     * in Perceptron.
     */
    void cursorSelection(int step) {
        // if there are no active cursors do nothing
        if (active_cursors.size() <= 0) {
            current_cursor = null;
            return;
        }
        // check stored cursors in the array list of cursors and find the index of what will be the current cursor
        int index = active_cursors.indexOf(current_cursor);
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

        current_cursor = (active_cursors.get(index));

        // use a robot to move the selection circles to the next or previous cursor location (new current cursor)
        if (robot != null) {
            int x, y;
            // In order to move the selection circles to the next or previous cursor, we need to transform the in-window
            // coordinates of current_cursor.(x,y) to the coordinates that we can supply to the robot in the coordinate
            // system of the screen. perceptron.getLocationOnScreen().(x,y) are the coordinates in pixels on desktop.
            // For example, current_cursor.(x,y) goes from 0 to screen height and width - 1 and is tied to the window.
            // getLocationOnScreen() returns objective coordinates on the physical screen. Insets are the edges of window.
            if (percept.windowed_mode) {
                x_scaler = (float) percept.screen_width / (float) percept.canvas.width;
                y_scaler = (float) percept.screen_height / (float) percept.canvas.height;
                // window stretching transform applied to current_cursor.(x,y)
                x = (int) (((float) percept.canvas.width / (float) percept.screen_width) * current_cursor.x + percept.getLocation().x + percept.window_insets.left);
                y = (int) (((float) percept.canvas.height / (float) percept.screen_height) * current_cursor.y + percept.getLocation().y + percept.window_insets.top);
            } else {
                x_scaler = (float) percept.screen_width() / (float) percept.physical_width();
                y_scaler = (float) percept.screen_height() / (float) percept.physical_height();
                x = (int) (current_cursor.x / x_scaler);
                y = (int) (current_cursor.y / y_scaler);
            }

            bound(new Point(x, y));
            robot.mouseMove(x, y);

        }

    }


    /**
     * Check whether a cursor is contained in the array list of cursors.
     */
    private void check_cursor() {
        if (!active_cursors.contains(current_cursor)) {
            if (active_cursors.size() <= 0) {
                current_cursor = null;
            } else {
                current_cursor = (active_cursors.get(0));
            }
        }
    }


    /**
     * Draw all active controls.
     */
    public void drawAll(Graphics G) {
        // if there are no active cursors do nothing
        if (active_cursors.size() <= 0 || !draw_cursors) {
            return;
        }
        for (Cursor active_cursor : active_cursors) {
            active_cursor.drawTo(G);
        }
        if (!screensaver) {
            G.drawImage(cursor_ring, (mouse_location.x) - 32, (mouse_location.y) - 32, null);
            if (current_cursor != null) {
                G.drawImage(cursor_ring_small, (int) current_cursor.x - 11, (int) current_cursor.y - 11, null);
            }
        }
    }


    /**
     * Press T to activate the 3D tree.
     */
    public void setTree(boolean active) {
        if (active) {
            for (Cursor c : TreeCursors) {
                if (!active_cursors.contains(c)) {
                    active_cursors.add(c);
                }
            }
        } else {
            for (Cursor c : TreeCursors) {
                active_cursors.remove(c);
            }
        }
        check_cursor();
        tree.set_active(active);
    }


    /**
     * Press P for autopilot - the screensaver mode.
     */
    public void toggle_screensaver_mode() {
        screensaver = !screensaver;
        if (percept.sw != null) {
            if (screensaver) {
                percept.sw.jtb_autopilot.setSelected(true);
            } else {
                percept.sw.jtb_autopilot.setSelected(false);
            }
        }
    }


    public double XBranchingCursor() {
        return BranchingCursor.destination.x;
    }

    public double XAlphaCursor() {
        return AlphaCursor.destination.x;
    }

    public double XBranchLengthCursor() {
        return BranchLengthCursor.destination.x;
    }

    public double XTreeOrientationCursor() {
        return TreeOrientationCursor.destination.x;
    }

    public double XMapCursor() {
        return MapCursor.destination.x;
    }

    public double XMapRotationCursor() {
        return MapRotationCursor.destination.x;
    }

    public double XGradientCursor() {
        return GradientCursor.destination.x;
    }

    public double XTreeLocationCursor() {
        return TreeLocationCursor.destination.x;
    }

    public double YBranchingCursor() {
        return BranchingCursor.destination.y;
    }

    public double YAlphaCursor() {
        return AlphaCursor.destination.y;
    }

    public double YBranchLengthCursor() {
        return BranchLengthCursor.destination.y;
    }

    public double YTreeOrientationCursor() {
        return TreeOrientationCursor.destination.y;
    }

    public double YMapCursor() {
        return MapCursor.destination.y;
    }

    public double YMapRotationCursor() {
        return MapRotationCursor.destination.y;
    }

    public double YGradientCursor() {
        return GradientCursor.destination.y;
    }

    public double YTreeLocationCursor() {
        return TreeLocationCursor.destination.y;
    }

    public void setXBranchingCursor(double x) {
        BranchingCursor.destination.x = (int) x;
    }

    public void setXAlphaCursor(double x) {
        AlphaCursor.destination.x = (int) x;
    }

    public void setXBranchLengthCursor(double x) {
        BranchLengthCursor.destination.x = (int) x;
    }

    public void setXTreeOrientationCursor(double x) {
        TreeOrientationCursor.destination.x = (int) x;
    }

    public void setXMapCursor(double x) {
        MapCursor.destination.x = (int) x;
    }

    public void setXMapRotationCursor(double x) {
        MapRotationCursor.destination.x = (int) x;
    }

    public void setXGradientCursor(double x) {
        GradientCursor.destination.x = (int) x;
    }

    public void setXTreeLocationCursor(double x) {
        TreeLocationCursor.destination.x = (int) x;
    }

    public void setYBranchingCursor(double x) {
        BranchingCursor.destination.y = (int) x;
    }

    public void setYAlphaCursor(double x) {
        AlphaCursor.destination.y = (int) x;
    }

    public void setYBranchLengthCursor(double x) {
        BranchLengthCursor.destination.y = (int) x;
    }

    public void setYTreeOrientationCursor(double x) {
        TreeOrientationCursor.destination.y = (int) x;
    }

    public void setYMapCursor(double x) {
        MapCursor.destination.y = (int) x;
    }

    public void setYMapRotationCursor(double x) {
        MapRotationCursor.destination.y = (int) x;
    }

    public void setYGradientCursor(double x) {
        GradientCursor.destination.y = (int) x;
    }

    public void setYTreeLocationCursor(double x) {
        TreeLocationCursor.destination.y = (int) x;
    }


    // //////////////////////////////////////////////////////////////////////////
    // MOUSE LISTENER IMPLEMENTATION

    @Override
    public void mouseMoved(MouseEvent e) {
        percept.stimulate();
        if (active_cursors.size() <= 0) {
            return;
        }
        // resize listener for Perceptron JFrame. may slow things down?
        x_scaler = (float) percept.screen_width / (float) percept.canvas.width;
        y_scaler = (float) percept.screen_height / (float) percept.canvas.height;
        // without this line only the small circle would encircle the selected cursor and move it
        // If the small circle and the selected cursor are moving linearly across the desktop and
        // in the program window, the large circle will move this many times slower or faster, so
        // the scaler is here to match the motion of mouse, the small and large circle.
        mouse_location = bound(new Point((int) (x_scaler * (e.getX() - percept.window_insets.left)), (int) (y_scaler * (e.getY() - percept.window_insets.top))));
        // without this line the large circle would move and select cursors, but the cursors would not move
        current_cursor.mouseMoved(e);
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }


    @Override
    public void mouseEntered(MouseEvent e) {
        percept.stimulate();
    }


    @Override
    public void mousePressed(MouseEvent e) {
        percept.stimulate();
        if (e.getButton() == MouseEvent.BUTTON1) {
            cursorSelection(1);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            cursorSelection(-1);
        }
    }


    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }


    // //////////////////////////////////////////////////////////////////////////
    // KEY LISTENER IMPLEMENTATION

    /**
     * The keyboard layout/options
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (ENTRY_MODE) { // keyboard associations when typing equation
            switch (e.getKeyCode()) {
                case VK_LEFT:
                    percept.BUFFER_LEFT();
                    break;
                case VK_RIGHT:
                    percept.BUFFER_RIGHT();
                    break;
                case VK_UP:
                    percept.BUFFER_UP();
                    break;
                case VK_DOWN:
                    percept.BUFFER_DOWN();
                    break;
                case VK_BACK_SPACE:
                    percept.BUFFER_BACKSPACE();
                    break;
                case VK_ENTER:
                    percept.BUFFER_TO_MAP();
                    break;
                case VK_PAGE_UP:
                    percept.BUFFER_SCROLL_UP();
                    break;
                case VK_PAGE_DOWN:
                    percept.BUFFER_SCROLL_DOWN();
                    break;
                case VK_ESCAPE:
                    percept.window_closing_event();
                    break;
                case VK_PAUSE:
                    break;
                case VK_PRINTSCREEN: // windows took over
                    break;
                case VK_CAPS_LOCK:
                    percept.save_as_running = true;
                    percept.running = false; // The ability to perceptron.save_frame() while typing equation
                    break;
                case VK_SHIFT:
                    break;
                case VK_TAB: // windows took over
                    break;
                case VK_ALT: // windows took over
                    break;
                case VK_CONTROL:
                    ENTRY_MODE = percept.CURSOR_ON = false;
                    if (percept.sw != null) {
                        percept.sw.jtb_type_equation.setSelected(false);
                        percept.sw.jtb_type_equation.setText("Type equation");
                    }
                    break;
                default:
                    percept.append_to_buffer(e.getKeyChar());
                    return;
            }
        } else if (PRESETS_MODE) {
            /**
             * In the presets mode the entire keyboard is dedicated to preset
             * numbers. Unused.
             */
            switch (e.getKeyCode()) {
                case VK_ESCAPE:
                    percept.window_closing_event();
                    break;
                case VK_STOP:
                    System.exit(0);
                    break;
                case VK_0:
                    apply_preset(0);
                    break;
                case VK_1:
                    apply_preset(1);
                    break;
                case VK_2:
                    apply_preset(2);
                    break;
                case VK_3:
                    apply_preset(3);
                    break;
                case VK_4:
                    apply_preset(4);
                    break;
                case VK_5:
                    apply_preset(5);
                    break;
                case VK_6:
                    apply_preset(6);
                    break;
                case VK_7:
                    apply_preset(7);
                    break;
                case VK_8:
                    apply_preset(8);
                    break;
                case VK_9:
                    apply_preset(9);
                    break;
                case VK_A:
                    apply_preset(10);
                    break;
                case VK_B:
                    apply_preset(11);
                    break;
                case VK_C:
                    apply_preset(12);
                    break;
                case VK_D:
                    apply_preset(13);
                    break;
                case VK_E:
                    apply_preset(14);
                    break;
                case VK_F:
                    apply_preset(15);
                    break;
                case VK_G:
                    apply_preset(16);
                    break;
                case VK_H:
                    apply_preset(17);
                    break;
                case VK_I:
                    apply_preset(18);
                    break;
                case VK_J:
                    apply_preset(19);
                    break;
                case VK_K:
                    apply_preset(20);
                    break;
                case VK_L:
                    apply_preset(21);
                    break;
                case VK_M:
                    apply_preset(22);
                    break;
                case VK_N:
                    apply_preset(23);
                    break;
                case VK_O:
                    apply_preset(24);
                    break;
                case VK_P:
                    apply_preset(25);
                    break;
                case VK_Q:
                    apply_preset(26);
                    break;
                case VK_R:
                    apply_preset(27);
                    break;
                case VK_S:
                    apply_preset(28);
                    break;
                case VK_T:
                    apply_preset(29);
                    break;
                case VK_U:
                    apply_preset(30);
                    break;
                case VK_V:
                    apply_preset(31);
                    break;
                case VK_W:
                    apply_preset(32);
                    break;
                case VK_X:
                    apply_preset(33);
                    break;
                case VK_Y:
                    apply_preset(34);
                    break;
                case VK_Z:
                    apply_preset(35);
                    break;
                case VK_F1:
                    apply_preset(36);
                    break;
                case VK_F2:
                    apply_preset(37);
                    break;
                case VK_F3:
                    apply_preset(38);
                    break;
                case VK_F4:
                    apply_preset(39);
                    break;
                case VK_F5:
                    apply_preset(40);
                    break;
                case VK_F6:
                    apply_preset(41);
                    break;
                case VK_F7:
                    apply_preset(42);
                    break;
                case VK_F8:
                    apply_preset(43);
                    break;
                case VK_F9:
                    apply_preset(44);
                    break;
                case VK_F10:
                    apply_preset(45);
                    break;
                case VK_F11:
                    apply_preset(46);
                    break;
                case VK_F12:
                    apply_preset(47);
                    break;
                case VK_BACK_QUOTE:
                    apply_preset(48);
                    break;
                case VK_QUOTE:
                    apply_preset(49);
                    break;
                case VK_OPEN_BRACKET:
                    apply_preset(50);
                    break;
                case VK_CLOSE_BRACKET:
                    apply_preset(51);
                    break;
                case VK_EQUALS:
                    apply_preset(52);
                    break;
                case VK_MINUS:
                    apply_preset(53);
                    break;
                case VK_SLASH:
                    apply_preset(54);
                    break;
                case VK_BACK_SPACE:
                    apply_preset(55);
                    break;
                case VK_COMMA:
                    apply_preset(56);
                    break;
                case VK_PERIOD:
                    apply_preset(57);
                    break;
                case VK_SPACE:
                    apply_preset(58);
                    break;
                case VK_SEMICOLON:
                    apply_preset(59);
                    break;
                case VK_BACK_SLASH:
                    apply_preset(60);
                    break;
                case VK_ENTER:
                    PRESETS_MODE = false;
                    break;
                default:
                    return;
            }
        } else {
            /**
             * The standard key associations (options) - keyboard layout.
             */
            switch (e.getKeyCode()) {
                case VK_ESCAPE:
                    percept.window_closing_event();
                    break;
                case VK_TAB: // windows took over
                    break;
                case VK_PAUSE:  // KDE took over for screen grabbing
                    percept.running = !percept.running;
                    break;
                case VK_PRINTSCREEN: // windows took over
                    break;
                case VK_NUM_LOCK:
                    break;
                case VK_Q:
                    fractal.increment_map(-1);
                    break;
                case VK_W:
                    fractal.increment_map(1);
                    break;
                case VK_E:
                    fractal.increment_outside_coloring(1);
                    break;
                case VK_R:
                    fractal.increment_boundary_condition(1);
                    break;
                case VK_T:
                    setTree(!tree.is_active());
                    break;
                case VK_Y:
                    fractal.change_convolution();
                    break;
                case VK_U:
                    percept.slow_down();
                    break;
                case VK_I:
                    fractal.switch_reflection_map();
                    break;
                case VK_O:
                    draw_futures = !draw_futures;
                    if (percept.sw != null) {
                        if (draw_futures) percept.sw.jcb_cursor_trails.setSelectedIndex(0);
                        else percept.sw.jcb_cursor_trails.setSelectedIndex(1);
                    }
                    break;
                case VK_P:
                    toggle_screensaver_mode();
                    break;
                case VK_OPEN_BRACKET:
                    current_cursor.remove_dot();
                    break;
                case VK_CLOSE_BRACKET:
                    current_cursor.add_dot();
                    break;
                case VK_BACK_SLASH:
                    percept.open_image_running = true; // calls open_image() in Perceptron.java
                    percept.running = false;
                    break;
                case VK_CAPS_LOCK:
                    break;
                case VK_A:
                    increment_preset(-1);
                    break;
                case VK_S:
                    increment_preset(1);
                    break;
                case VK_D:
                    percept.toggle_xor();
                    break;
                case VK_F:
                    fractal.increment_fader(1);
                    break;
                case VK_G:
                    fractal.increment_gradient(1);
                    break;
                case VK_H:
                    fractal.increment_accent(1);
                    break;
                case VK_J:
                    fractal.toggle_color_inversion();
                    break;
                case VK_K:
                    fractal.toggle_gradient_switch();
                    break;
                case VK_L:
                    fractal.increment_renderer(1);
                    break;
                case VK_SEMICOLON:
                    percept.toggle_salvia_mode();
                    break;
                case VK_QUOTE:
                    fractal.increment_gradient_shape(1);
                    break;
                case VK_ENTER:
                    percept.window_mode();
                    break;
                case VK_Z:
                    percept.toggle_frame_rate_display();
                    break;
                case VK_X:
                    fractal.increment_colorfilter(1);
                    break;
                case VK_C:
                    draw_cursors = !draw_cursors;
                    if (percept.sw != null) {
                        if (draw_cursors) percept.sw.jcb_hide_show_cursors.setSelectedIndex(0);
                        else percept.sw.jcb_hide_show_cursors.setSelectedIndex(1);
                    }
                    break;
                case VK_V:
                    fractal.dampen_colors_during_update_color();
                    break;
                case VK_B:
                    percept.toggle_persistent_initial_set();
                    break;
                case VK_N:
                    percept.increment_image(-1);
                    break;
                case VK_M:
                    percept.increment_image(1);
                    break;
                case VK_COMMA:
                    fractal.inc_ortho();
                    break;
                case VK_PERIOD:
                    fractal.inc_polar();
                    break;
                case VK_SLASH:
                    percept.help_screen();
                    break;
                case VK_SHIFT:
                    break;
                case VK_BACK_QUOTE:
                    percept.open_preset_running = true; // calls open_preset() in Perceptron.java
                    percept.running = false;
                    break;
                case VK_MINUS:
                    current_cursor.increment_rate_constant(-1);
                    break;
                case VK_EQUALS:
                    current_cursor.increment_rate_constant(1);
                    break;
                case VK_DELETE:
                    current_cursor.toggle_wander();
                    break;
                case VK_END:
                    fractal.pullback_flag = !fractal.pullback_flag;
                    if (percept.sw != null) {
                        if (fractal.pullback_flag) percept.sw.jcb_pullback.setSelectedIndex(0);
                        else percept.sw.jcb_pullback.setSelectedIndex(1);
                    }
                    break;
                case VK_BACK_SPACE:
                    percept.rotateImages = !percept.rotateImages;
                    if (percept.sw != null) {
                        if (percept.rotateImages) percept.sw.jcb_shuffle_images.setSelectedIndex(1);
                        else percept.sw.jcb_shuffle_images.setSelectedIndex(0);
                    }
                    break;
                case VK_CONTROL:
                    ENTRY_MODE = percept.salvia_mode = percept.CURSOR_ON = true;
                    if (percept.sw != null) {
                        percept.sw.jtb_type_equation.setSelected(true);
                        percept.sw.jtb_type_equation.setText("now reading in equation...");
                    }
                    break;
                case VK_ALT: // windows took over. Releases the mouse pointer.
                    break;
                case VK_SPACE:
                    percept.save_as_running = true;
                    percept.running = false; // calls save_frame() in Perceptron.java
                    break;
                case VK_UP:
                    fractal.setMotionBlur(fractal.motionblurp - 32);
                    break;
                case VK_DOWN:
                    fractal.setMotionBlur(fractal.motionblurp + 32);
                    break;
                case VK_LEFT:
                    fractal.setColorFilterWeight(fractal.filterweight - 32);
                    break;
                case VK_RIGHT:
                    fractal.setColorFilterWeight(fractal.filterweight + 32);
                    break;
                case VK_HOME:
                    percept.toggle_animation();
                    break;
                case VK_INSERT:
                    fractal.toggle_partial_gradient_inversion();
                    break;
                case VK_PAGE_UP:
                    percept.toggle_grabber_window();
                    break;
                case VK_PAGE_DOWN:
                    percept.toggle_settings_window();
                    break;
                case VK_0:
                    fractal.setMapping("(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H);
                    break;
                case VK_1:
                    fractal.setMapping("1*ln(z)/p*(2.4*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H + ")");
                    break;
                case VK_2:
                    fractal.setMapping("2*ln(z)/p*(2.4*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H + ")");
                    break;
                case VK_3:
                    fractal.setMapping("3*ln(z)/p*(2.4*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H + ")");
                    break;
                case VK_4:
                    fractal.setMapping("4*ln(z)/p*(2.4*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H + ")");
                    break;
                case VK_5:
                    fractal.setMapping("z/abs(sqrt((absz)^2-1.5))");
                    break;
                case VK_6:
                    fractal.setMapping("z-(z^3-1)/(3*z^2)"); // The Newton's method for z^3 - 1 = 0
                    break;
                case VK_7:
                    fractal.setMapping("z^(1.5)");
                    break;
                case VK_8:
                    fractal.setMapping("(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H);
                    break;
                case VK_9:
                    fractal.setMapping("(imag(z)*i+ln(abs(real(z))))*.3/(abs(real(z)))*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H);
                    break;
                case VK_F1:
                    percept.f1_for_help();
                    break;
                case VK_F2:
                    fractal.setMapping("z*abs(z)");
                    break;
                case VK_F3:
                    fractal.setMapping("f/z+i*z");
                    break;
                case VK_F4:
                    fractal.setMapping("z");
                    break;
                case VK_F5:
                    fractal.setMapping("1/z");
                    break;
                case VK_F6:
                    fractal.setMapping("e^z+e^(iz)");
                    break;
                case VK_F7:
                    fractal.setMapping("conj(e^z+e^(z*e^(i*p/4)))");
                    break;
                case VK_F8:
                    fractal.setMapping("z*z*e^(i*abs(z))");
                    break;
                case VK_F9:
                    fractal.setMapping("abs(z)*e^(i*arg(z)*2)*2");
                    break;
                case VK_F10:
                    fractal.setMapping("z*e^(i*abs(z))*abs(z)/f");
                    break;
                case VK_F11:
                    fractal.setMapping("(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H);
                    break;
                case VK_F12:
                    fractal.setMapping("(imag(z)*i+ln(abs(real(z))))*.3/(abs(real(z)))*" + percept.double_buffer.output.W + "/" + percept.double_buffer.output.H);
                    break;
                default:
                    return;
            }
        }
        percept.show_help_screen();
    }


    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }


    /**
     * Apply preset. Press A or S to change the preloaded preset.
     */
    public void apply_preset(int preset) {
        try {
            active_cursors.add(TempCursor);
            current_cursor = TempCursor;
            percept.user_presets[preset].set(percept);
            active_cursors.remove(TempCursor);
            String presetname = percept.list_of_presets.get(preset).toString();
            System.out.println("______applied preset: " + preset + " : " + presetname + "______");
        } catch (Exception e) {
            System.err.println("Error applying preset: " + preset);
            e.printStackTrace();
        }
    }


    /**
     * Load the preset by cycling through a number of different presets in the
     * resource/presets folder.
     */
    public void increment_preset(int n) {
        if (fractal.is_fading()) {
            return;
        }
        preset_number = preset_number + n;
        int number_of_presets = percept.user_presets.length;
        if (preset_number == number_of_presets) {
            preset_number = 0;
        }
        if (preset_number == -1) {
            preset_number = number_of_presets - 1;
        }
        apply_preset(preset_number);
    }


    /**
     * Cellular automata - experimental.
     */
    public void setLife(boolean active) {
        if (active) {
            for (Cursor c : LifeCursors) {
                if (!active_cursors.contains(c)) {
                    active_cursors.add(c);
                }
            }
        } else {
            for (Cursor c : LifeCursors) {
                active_cursors.remove(c);
            }
        }
        check_cursor();
        percept.life.running = active;
    }


    /**
     * experimental audio support
     */
    public void setAudio(boolean active) {
        if (active) {
            for (Cursor c : AudioCursors) {
                if (!active_cursors.contains(c)) {
                    active_cursors.add(c);
                }
            }
        } else {
            for (Cursor c : AudioCursors) {
                active_cursors.remove(c);
            }
        }
        check_cursor();
    }

}
