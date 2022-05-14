package perceptron;
/* RenderingKernel.java
 * Created on March 9, 2007, 7:27 PM
 */

import image.DoubleBuffer;
import java.awt.BasicStroke;
import util.ColorUtility;
import math.MathToken;
import math.complex;
import math.Equation;
import math.ComplexVarList;
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.util.Vector;

import static util.ColorUtility.* ;

import static java.lang.Math.*;

/**
 * [[[[ This is the core ]]]]
 * <p>
 * <center><img src="./doc-files/flowchart.png"/></center>
 * <p>
 * @author Michael Everett Rule
 */
public class RenderingKernel {

    long identifier_count = 0L;

    abstract class Operator {

        String name;

        Operator(String s) {
            name = s == null ? ("Operator" + identifier_count) : s;
            identifier_count++;
        }

        @Override
        public String toString() {
            return name;
        }

        abstract void operate(RenderStateMachine m);
    }
    static final double PI_OVER_TWO = Math.PI * .5;
    // USER EDITABLE PARAMETERS ////////////////////////////////////////////////
    /** Weather or not to compute a fractal mapping */
    boolean active = true;
    /** Automatic rotation of the constant */
    public boolean auto_rotate = false;
    // Fading data /////////////////////////////////////////////////////////////
    /** A weight to determine how much to fade the background by*/
    int fade_weight = 128, anti_fade_weight;
    /** A default color to fade the background towards, if desired */
    int fade_color = 0x00000000;
    /** A color generated from the an external source, like audio input */
    int input_color = 0x00000000;
    // Customisation data //////////////////////////////////////////////////////
    int grad_invert = 0xffffff;
    int grad_switch = 0x0;
    int[] accent_colors = new int[]{0x0, 0xFFFFFF, 0xFFFF00, 0x8000FF};
    int accent_color_index;
    int accent_color = 0x00;
    /** Which visual function to use */
    int visu_num = 1;
    /** Which color functions to use when the index is out of bounds */
    int offs_num = 2;
    /** Which fader function to use for offscreen pixles */
    int fade_num = 4;
    /** Which fader function to use for offscreen pixles */
    int grad_num = 4;
    /** Which mapping to use */
    int map_num = 0;
    /** Which color filters to use */
    int colf_num = 0;
    /** External Buffer ( 0 for none ) to import */
    int rndr_num = 0;

    int filterweight = 128;

    int filter_param1 = 128;
    int filter_param2 = 128;

    int ortho_type = 0;
    int polar_type = 0;

    /**
     *
     */
    public void inc_ortho() {
        ortho_type = (ortho_type+1)%3;
    }

    /**
     *
     */
    public void inc_polar() {
        polar_type = (polar_type+1)%3;
    }

    int motionblurp = 256;
    int motionblurq = 0;
    // Other Member Data ///////////////////////////////////////////////////////
    /** true if the map is currently being updated. calls to run will have
     *  no effect as long as the Thread is already running */
    boolean running = false;
    boolean interpolate = true;
    boolean fade = true;
    //boolean gradient = true;
    boolean inversion = false;
    boolean dampcolor = true;
    boolean OMG = false;
    boolean mega = false;
    /** used for animating the fractal */
    int phase_shift;
    /** local map storage */
    int[] map, map_buffer;//, metafield;
    /** count off how many frames used between maps */
    float fade_1;
    /** The Screen data to which to apply the mapping */
    DoubleBuffer buffer;
    /** The variable constant of this map */
    complex constant, rotation_constant;
    float[] norm_c;
    float gradient_slope_constant = 1f;
    float gradient_offset = 0f;
    //BUFFERS AND ASSOCIATED INFORMATION
    /** The W and H of the mapping, and common things */
    private final int W;
    private final int H;
    private final int W2;
    private final int H2,  W7,  H7,  W8,  H8,  W9,  H9,  M_END;
    private final float W8n, H8n;
    private final int cc23,x1,x2,x3,y1,y2,y3;
    private final int[] trix,triy;
    private final int M_LEN,  M_MID,  HALF_W,  HALF_H,  TWO_W;
    private final int TWO_H,  W_M_ONE,  H_M_ONE,  M_ONE,  M_M_ROW;
    private final int W_P_ONE,  MAX_C = 65535,  BOUNDS;
    private final float overW7,  overH7;

    /** Precomputed scalars for converting between the complex plane, the
     *  receptive field, and the mapped output */
    private float complex_to_map_W_scalar;
    private float complex_to_map_H_scalar;
    private float complex_to_W_scalar;
    private float complex_to_H_scalar;

    Vector<Mapping> mappings;

    /**
     * The upper left, lower right, and size of the rectangle in the
     *  complex plane
     */
    public static final complex size = new complex(6.4f, 4f);
    // these are actually re-initialized so these values dont matter

    /**
     *
     */
    public static final complex upper_left = new complex();

    /**
     *
     */
    public static final complex lower_right = new complex();

    /**
     *
     */
    public static final int ROTATIONAL_RENDERER = 0;

    /**
     *
     */
    public static final int IRROTATIONAL_RENDERER = 2;

    /**
     *
     */
    public static final int IMAGE_RENDERER = 1;
    Perceptron perceptron;

    
    ////////////////////////////////////////////////////////////////////////////
    // Constructor /////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /** Creates a new instance of RenderingKernel
     * @param b
     * @param maps
     * @param parent
     */
    public RenderingKernel(DoubleBuffer b, Vector<Mapping> maps, Perceptron parent) {

        perceptron = parent;

        new_vars.set(18, new complex(size));
        new_vars.set(22, new complex(size.real));
        new_vars.set(7, new complex(size.imag));

        mappings = maps;
        if (mappings == null) {
            mappings = new Vector<Mapping>();
        }

        buffer = b;

        W = b.output.image.getWidth();
        H = b.output.image.getHeight();
        W2 = W << 2;
        H2 = H << 2;
        W7 = W << 7;
        H7 = H << 7;
        overW7 = 1.f / W7;
        overH7 = 1.f / H7;
        W8 = W << 8;
        H8 = H << 8;
        W8n = 1.0f/W8;
        H8n = 1.0f/H8*(float)(2/sqrt(3));
        cc23 = (int)(0.5f+0.666667*(H8-W8*sin(PI/3)));
        W9 = W << 9;
        H9 = H << 9;
        W_P_ONE = W + 1;
        W_M_ONE = W - 1;
        H_M_ONE = H - 1;
        M_LEN = W * H;
        M_END = M_LEN - 1;
        M_ONE = M_END - 1;
        M_M_ROW = M_ONE - W;
        HALF_W = W / 2;
        HALF_H = H / 2;
        TWO_W = W << 1;
        TWO_H = H << 1;
        M_MID = HALF_W + W * HALF_H;
        BOUNDS = min(W7, H7);
        y1 = H8-cc23;
        x1 = 0;
        x2 = W8;
        y2 = y1;
        x3 = W8>>1;
        y3 = cc23>>1;
        trix = new int[]{x1,x2,x3};
        triy = new int[]{y1,y2,y3};

        if (W > H) {
            size.real = 2.4f * W / H;
            size.imag = 2.4f;
        } else {
            size.real = 2.4f;
            size.imag = 2.4f * H / W;
        }

        lower_right.real = size.real * .5f;
        lower_right.imag = size.imag * .5f;

        upper_left.real = -lower_right.real;
        upper_left.imag = -lower_right.imag;

        complex_to_map_W_scalar = size.real / W;
        complex_to_map_H_scalar = size.imag / H;

        complex_to_W_scalar =
                complex_to_H_scalar = (W / size.real + H / size.imag) * .5f;

        //select initial behavior
        initialise_functions();
        //precompute as much data as possible to accelerate rendering
        initialise_lookups();
        //allocate memory and compute initial state for map
        initialise_map();

    }
    // Initialisers ////////////////////////////////////////////////////////////

    /** This assigns for the first time the []_function variables,
     *  which represent adaptable code which may be switched in and out
     *  at any given time
     */
    void initialise_functions() {

        visu_num = wrap(visu_num, Blendings.length);
        offs_num = wrap(offs_num, Offscreen.length);
        fade_num = wrap(fade_num, ColorMode.length);
        grad_num = wrap(grad_num, Gradients.length);
        colf_num = wrap(colf_num, ColorFilters.length);

        visu_opr = Blendings[visu_num];
        offs_opr = Offscreen[offs_num];
        fade_opr = ColorMode[fade_num];
        grad_opr = Gradients[grad_num];
        colf_opr = ColorFilters[colf_num];
        renderer = Renderers[rndr_num];

        if (mappings.size() != 0) {
            map_num %= mappings.size();
            mapping = mappings.get(map_num);
        }
        gradients = init_gradients();
        gradient_number = 0;
        radial_gradient = gradients[gradient_number];
    }

    /** This computes all information that does not change with the variable
     *  constants, this dramatically accelerates future rendering */
    void initialise_lookups() {
        map = new int[M_LEN << 1];
        map_buffer = new int[M_LEN << 1];
        compute_lookup();
    }

    /** Allocates memory and computes default map */
    final void initialise_map() {
        constant = new complex();
        rotation_constant = new complex();
        norm_c = new float[]{0, 0};
        set_constant(0, 0);
    }

    /** Calculated lookup table values, store them in the buffer. */
    private void compute_lookup() {
        if (active) {
            try {
                if (fade_1 > 0) {
                    return;
                }
                fade_1 = 1.f;
                complex z = new complex();
                int i = 0;
                float upper_left_real = upper_left.real;
                float upper_left_imag = upper_left.imag;
                for (int y = 0; y < H; y++) {
                    for (int x = 0; x < W; x++) {
                        z.real = x * complex_to_map_W_scalar + upper_left_real;
                        z.imag = y * complex_to_map_H_scalar + upper_left_imag;
                        complex f_z = mapping.operate(z);
                        map_buffer[i++] = (int) (0x100 * (complex_to_W_scalar *
                                (f_z.real - upper_left_real))) - W7;
                        map_buffer[i++] = (int) (0x100 * (complex_to_H_scalar *
                                (f_z.imag - upper_left_imag))) - H7;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private boolean is_fading() {
        return fade_1 > 0;
    }

    /**
     *
     * @param map
     */
    public void load_map(Mapping map) {
        if (is_fading()) {
            return;
        }
        mapping = map;
        compute_lookup();
    }

    private void swap_map() {
        int[] temp = map;
        map = map_buffer;
        map_buffer = temp;
    }

    /**
     *
     */
    public void toggle_color_inversion() {
        grad_invert ^= 0xFFFFFF;
    }

    /**
     *
     */
    public void toggle_gradient_switch() {
        grad_switch ^= 0xFF;
    }

    /**
     *
     */
    public void toggle_interpolation() {
        interpolate = !interpolate;
    }
    ////////////////////////////////////////////////////////////////////////////
    //MAPPING FUNCTION
    DataBuffer output_buffer,
            buffer_buffer,
            sketch_buffer,
            disply_buffer;
    float d_r = 0, d_i = 0;
    float rotation_drift = .1f;
    /** Applies the current mapping to the screen_raster, storing the
     *  result in the output_raster. */
    void operate() {
        if (!active) {
            buffer.flip();
            buffer.output.graphics.drawImage(buffer.buffer.image, 0, 0, null);
            return;
        }


        //if somethinethings
        //buffer.buffer.graphics2D.setColor(Color.WHITE);
        //buffer.buffer.graphics2D.setStroke(new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        //buffer.buffer.graphics2D.drawPolygon(new int[]{x1>>8,x2>>8,x3>>8}, new int[]{y1>>8,y2>>8,y3>>8}, 3);

        buffer.flip();

        output_buffer = buffer.output.buffer;
        buffer_buffer = buffer.buffer.buffer;
        disply_buffer = buffer.disply.buffer;

        if (buffer.image != null) {
            sketch_buffer = buffer.image.buffer;
        }

        update_color();

        if (OMG) {
            grad_invert^=~0;
        }

        long time = System.currentTimeMillis();


        switch (ortho_type) {
            case 0:
                Cx = (int) (.5f + 0x100 * ((norm_c[0]))) + W7;
                Cy = (int) (.5f + 0x100 * ((norm_c[1]))) + H7;
                break;
            case 1:
                Cx = (int) (.5f + 0x100 * ((d_r=(abs(d_r)>MAX_C)?0:d_r+.1f*norm_c[0])))+W7;
                Cy = (int) (.5f + 0x100 * ((d_i=(abs(d_i)>MAX_C)?0:d_i+.1f*norm_c[1])))+H7;
                break;
            case 2:
                Cx = W7;
                Cy = H7;
                break;
            default:
                break;
        }
        complex local_rotation = new complex() ;
        switch (polar_type) {
            case 0:
                local_rotation = rotation_constant;
                break;
            case 1:
                rotation_drift += complex.arg(rotation_constant)*0.01f;
                local_rotation = complex.fromPolar(complex.mod(rotation_constant),rotation_drift);
                break;
            case 2:
                local_rotation = complex.fromPolar(1.0f,(float)(Math.PI*0.5));
                break;
            default:
                break;
        }




        /*
        Cx = (int) (.5f + 0x100 * ((auto_rotate
                ? (d_r = (abs(d_r) > MAX_C) ? 0 : d_r + .1f * norm_c[0])
                : norm_c[0]))) + W7;

        Cy = (int) (.5f + 0x100 * ((auto_rotate
                ? (d_i = (abs(d_i) > MAX_C) ? 0 : d_i + .1f * norm_c[1])
                : norm_c[1]))) + H7;
        */
        /*
        complex local_rotation = rotation_constant;
        if (auto_rotate) {
            rotation_drift += complex.arg(rotation_constant)*0.01f;
            local_rotation = complex.fromPolar(complex.mod(rotation_constant),rotation_drift);
        }
        */

        rotation_constant_real = mega ? (float) Math.pow(10, local_rotation.real) : local_rotation.real;
        //(int)( .5 + rotation_constant.real * 0x10000 ) ;
        rotation_constant_imag = mega ? (float) Math.pow(10, local_rotation.imag) : local_rotation.imag;

        //(int)( .5 + rotation_constant.imag * 0x10000 ) ;
        rotation_constant_coef = rotation_constant_real + rotation_constant_imag;

        buffer.output.set_interpolated(interpolate);
        //buffer.setFancy(interpolate);

        buffer.image.grabber.nextFrame();

        renderer.operate(new RenderStateMachine());

        buffer.flip();
    }

    /*
    void threadedOperate(final int cores) {
        if (!active) {
            buffer.flip();
            buffer.output.graphics.drawImage(buffer.buffer.image, 0, 0, null);
            return;
        }
        buffer.flip();
        output_buffer = buffer.output.buffer;
        buffer_buffer = buffer.buffer.buffer;
        disply_buffer = buffer.disply.buffer;
        if (buffer.image != null) {
            sketch_buffer = buffer.image.buffer;
        }
        update_color();
        if (OMG) {
            grad_invert ^=~0;
        }
        long time = System.currentTimeMillis();
        Cx = (int) (.5f + 0x100 * ((auto_rotate
                ? (d_r = (abs(d_r) > MAX_C) ? 0 : d_r + .1f * norm_c[0])
                : norm_c[0]))) + W7;
        Cy = (int) (.5f + 0x100 * ((auto_rotate
                ? (d_i = (abs(d_i) > MAX_C) ? 0 : d_i + .1f * norm_c[1])
                : norm_c[1]))) + H7;
        rotation_constant_real = mega ? (float) Math.pow(10, rotation_constant.real) : rotation_constant.real;
        rotation_constant_imag = mega ? (float) Math.pow(10, rotation_constant.imag) : rotation_constant.imag;
        rotation_constant_coef = rotation_constant_real + rotation_constant_imag;
        buffer.output.set_interpolated(interpolate);
        buffer.image.grabber.nextFrame();

        Thread[] threads = new Thread[cores];
        for (int i = 0; i < cores; i++) {
            final int I = i;
            threads[i] = new Thread() {

                @Override
                public void run() {
                    RenderStateMachine m = new RenderStateMachine();
                    //m.m_start = I*M_LEN/cores ;
                    //m.m_end   = (I+1)*M_LEN/cores ;

                    m.m_start = I;
                    m.m_end = M_LEN;
                    m.stride = cores;

                    renderer.operate(m);
                }
            };
            threads[i].start();
        }
        for (int i = 0; i < cores; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                threads[i].destroy();
            }
        }

        buffer.flip();
    }*/

    ////////////////////////////////////////////////////////////////////////////
    // Member Functions ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // Getters /////////////////////////////////////////////////////////////////
    /** Applies the current mapping to the given parameter
     * @param z
     * @return
     */
    public complex get_map(complex z) {
        return mapping.operate(z);
    }

    /** Returns a copy of the constant
     * @return
     */
    public complex get_constant() {
        return new complex(constant);
    }

    /** Returns a copy of the size of the complex plane area covered
     * @return
     */
    public complex get_size() {
        return new complex(constant);
    }

    /** Returns weather this RenderingKernel is active.
     * @return
     */
    public boolean is_active() {
        return active;
    }

    // Setters /////////////////////////////////////////////////////////////////
    /** Toggles active computation of the fractal map on and off
     * @param active
     */
    public void set_active(boolean active) {
        this.active = active;
    }

    /** Sets the constant value
     * @param real
     * @param imag
     */
    public void set_constant(float real, float imag) {
        constant.real = real;
        constant.imag = imag;
    }

    /** Sets the constant value given an x and y location on the screen
     * @param x
     * @param y
     */
    public void set_norm_c(float x, float y) {
        constant.real = (W - x) / complex_to_W_scalar + upper_left.real;
        constant.imag = (H - y) / complex_to_H_scalar + upper_left.imag;
        compute_norm_c();
    }

    /** Computes the constant displacement value scaled for the dimensions of
     *  the screen */
    void compute_norm_c() {
        if (norm_c == null) {
            norm_c = new float[]{0, 0};//new Point(0,0);
        }
        if (constant == null) {
            constant = new complex(0, 0);
        }
        norm_c[0] = (float) (constant.real * complex_to_W_scalar);
        norm_c[1] = (float) (constant.imag * complex_to_H_scalar);
    }

    /** sets rotation parameter of map
     * @param x
     * @param y
     */
    public void set_normalized_rotation(float x, float y) {
        rotation_constant.real = ((W - x) / complex_to_W_scalar +
                upper_left.real);
        rotation_constant.imag = ((H - y) / complex_to_H_scalar +
                upper_left.imag);
        float theta = complex.arg(rotation_constant);
        float r = complex.mod(rotation_constant)*2;
        rotation_constant = complex.fromPolar(1 / r, theta);
        boundRadius = 1 / (r * r);
    }
    double boundRadius = 1;

    /** Set the input color
     * @param col
     */
    public void set_input_color(int col) {
        input_color = col;
    }

    private static int wrap(int n, int m) {
        return n < 0 ? m - (-n % m) : n % m;
    }

    /**
     *
     * @param n
     */
    public void increment_renderer(int n) {
        set_renderer(rndr_num + n);
    }

    /**
     *
     * @param index
     */
    public void set_renderer(int index) {
        if (!active)
            return;

        renderer = Renderers[rndr_num = wrap(index, Renderers.length)];
        
        /*
        if (rndr_num == IRROTATIONAL_RENDERER)
            perceptron.controls.active_cursors.remove(perceptron.controls.MapRotationCursor);
        else if (!perceptron.controls.active_cursors.contains(perceptron.controls.MapRotationCursor))
            perceptron.controls.active_cursors.add(perceptron.controls.MapRotationCursor);
        */
    }

    /**
     *
     * @param n
     */
    public void increment_visualiser(int n) {
        set_visualiser(visu_num + n);
    }

    /**
     *
     * @param index
     */
    public void set_visualiser(int index) {
        if (!active) {
            return;
        }
        visu_opr = Blendings[visu_num = wrap(index, Blendings.length)];
    }

    /**
     *
     * @param n
     */
    public void increment_offscreen(int n) {
        set_offscreen(offs_num + n);
    }

    /**
     *
     * @param index
     */
    public void set_offscreen(int index) {
        if (!active) {
            return;
        }
        offs_opr = Offscreen[offs_num = wrap(index, Offscreen.length)];
    }

    /**
     *
     * @param n
     */
    public void increment_fader(int n) {
        set_fader(fade_num + n);
    }

    /**
     *
     * @param index
     */
    public void set_fader(int index) {
        if (!active) {
            return;
        }
        fade_opr = ColorMode[fade_num = wrap(index, ColorMode.length)];
    }

    /**
     *
     * @param amount
     */
    public void increment_gradient(int amount) {
        set_gradient(grad_num + amount);
    }

    /**
     *
     * @param index
     */
    public void set_gradient(int index) {
        if (!active) {
            return;
        }
        grad_opr = Gradients[grad_num = wrap(index, Gradients.length)];
    }

    /**
     *
     * @param amount
     */
    public void increment_colorfilter(int amount) {
        set_colorfilter(colf_num + amount);
    }

    /**
     *
     * @param index
     */
    public void set_colorfilter(int index) {
        if (!active) {
            return;
        }
        colf_opr = ColorFilters[colf_num = wrap(index, ColorFilters.length)];
    }

    /**
     *
     * @param n
     */
    public void increment_map(int n) {
        set_map(map_num + n);
    }

    /**
     *
     * @param index
     */
    public void set_map(int index) {
        if (!active || is_fading()) {
            return;
        }
        //state_changed = true ;
        load_map(mappings.get(map_num = wrap(index, mappings.size())));
    }

    /**
     *
     * @param n
     */
    public void increment_fade_weight(int n) {
        fade_weight = wrap(fade_weight + n, 256);
    }

    /**
     *
     * @param n
     */
    public void increment_accent(int n) {
        set_accent(accent_color_index + n);
    }

    /**
     *
     * @param x
     * @param y
     */
    public void setContrastParameters(int x, int y)
    {
        this.filter_param1 = max(min(0xff,x),0);
        this.filter_param2 = max(min(0xff,y),0);
    }

    /**
     *
     * @param color_accent
     */
    public void set_accent(int color_accent) {
        accent_color = accent_colors[accent_color_index =
                wrap(color_accent, accent_colors.length)];
    }

    /**
     *
     * @param s
     */
    public void set_interpolation(boolean s) {
        interpolate = s;
    }

    /**
     *
     * @param slope
     * @param offset
     */
    public void set_gradient_param(float slope, float offset) {
        gradient_slope_constant = slope;
        gradient_offset = offset;
    }

    // Other Member Functions //////////////////////////////////////////////////
    /** update the default color */
    public void update_color() {
        //state_changed = true ;
        RenderStateMachine m = new RenderStateMachine();
        m.old_color = fade_color;
        fade_opr.operate(m);
        if (dampcolor) {
            fade_color = average(fade_color, 56, m.old_color, 200);
        }
        anti_fade_weight = 256 - fade_weight;
    }

    boolean noise_on = false;
    int noise = 1; //number of noise color bits, 0 .. 8
    int noise_mask = 0x00010101;
    /**
     *
     * @param amt
     * @return
     */
    public synchronized int increment_noise(int amt) {
        amt+=noise;
        if (amt<0) amt=0;
        if (amt>8) amt=8;
        switch (amt) {
            case 0: noise_mask = 0; break;
            case 1: noise_mask = 0x00010101; break;
            case 2: noise_mask = 0x00030303; break;
            case 3: noise_mask = 0x00070707; break;
            case 4: noise_mask = 0x000f0f0f; break;
            case 5: noise_mask = 0x001f1f1f; break;
            case 6: noise_mask = 0x003f3f3f; break;
            case 7: noise_mask = 0x007f7f7f; break;
            case 8: noise_mask = 0x00ffffff; break;
        }
        return noise = amt;
    }
    /**
     *
     * @return
     */
    public synchronized boolean toggle_noise() {
        noise_on = !noise_on;
        return noise_on;
    }

    final static ComplexVarList new_vars = ComplexVarList.standard();

    /**
     *
     * @param equ
     * @return
     */
    public static Mapping makeMap(final String equ) {
        final Equation new_mapping = MathToken.toEquation(equ);
        new_vars.set(18, size);
        new_vars.set(22, new complex(size.real));
        new_vars.set(7, new complex(size.imag));
        return new Mapping() {

            public complex operate(complex z) {
                new_vars.set(25, z);
                return new_mapping.evaluate(new_vars);
            }

            @Override
            public String toString() {
                return equ;
            }
        };
    }

    /**
     *
     * @param s
     */
    public void setMapping(final String s) {
        Mapping old = mapping;
        try {
            final Equation equ = MathToken.toEquation(s);
            mapping = new Mapping() {

                public complex operate(complex z) {
                    new_vars.set(25, z);
                    return equ.evaluate(new_vars);
                }

                @Override
                public String toString() {
                    return s;
                }
            };
            compute_lookup();
        //qmappings.add(mapping);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // INTERCHANGEABLE FUNCTIONS ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /** These can be switched in and out to create different visual effects.
     * These functions access information about the mapping that
     * ordinarily would be confined to the scope of a single function.
     * To prevent passing and returning a large number of parameters,
     * or an array, to these functions, these function variables have been
     * made accessable to the entire class. This is not secure and
     * modifications to this class should be made with caution.*/
    //these can stay global
    Operator visu_opr,
            offs_opr,
            fade_opr,
            renderer,
            grad_opr,
            colf_opr;
    Mapping mapping;
    int gradient_number;
    char[][] gradients;
    char[] radial_gradient;
    float rotation_constant_real, rotation_constant_imag, rotation_constant_coef;
    float c_r, c_i, g_r, g_i;
    int Cx, Cy;

    /** These instance variables may be modified by the Operators. */
    /** The map buffer is iterated over, and the mapping queried to find the
     *  field index for each element of the map buffer. Various effect functions
     *  are then called to calculate the color of the map element */
    //these need to be encapsulated
    class RenderStateMachine {
        int color, old_color; //the new map pixle color
        int f_x, f_y;
        int m_i;
        int m_start = 0, m_end = M_LEN;
        int stride = 1;
    }

    /**
     *
     * @param n
     */
    public void increment_gradient_shape(int n) {
        set_gradient_shape(gradient_number + n);
    }

    /**
     *
     * @param n
     */
    public void set_gradient_shape(int n) {
        radial_gradient = gradients[gradient_number = wrap(n, gradients.length)];
    }

    char[][] init_gradients() {
        char[][] result = new char[10][M_LEN];
        int index = 0;
        for (int y = -HALF_H; y < HALF_H; y++) {
            for (int x = -HALF_W; x < HALF_W; x++) {
                result[0][index] = (char) (255 - 512 *
                        ((float) x * x / (W * W) + (float) y * y / (H * H)));
                result[1][index] = (char) (255 - (x + HALF_W) * 255 / W);
                result[2][index] = (char) (255 - (y + HALF_H) * 255 / H);
                result[3][index] = (char) (255 - (y + HALF_H) * (x + HALF_W) *
                        255 / (W * H));
                result[4][index] = (char) (255 - (abs(x) + abs(y)) * 255 /
                        (HALF_W + HALF_H));
                result[5][index] = (char) (255 - (abs(x) * abs(y)) * 255 /
                        (HALF_W * HALF_H));
                result[6][index] = (char) (255 * (1 - min(1, sqrt(x * x + y * y) / min(HALF_H, HALF_W))));
                result[7][index] = (char) (255 * (1 - pow(min(1, sqrt(x * x + y * y) / min(HALF_H, HALF_W)), 9)));
                result[8][index] = (char) (255 * (1 - pow(max(0, min(1, 1.3 - (sqrt(x * x + y * y)) / (min(HALF_H, HALF_W)))), 9)));
                result[9][index] = (char) (result[7][index]);//(char)Math.min(result[7][index],result[8][index])  ;
                //result[9][index] = x*x+y*y<55*55 ? (char)0xff : 0 ;
                index++;
            }
        }
        return result;
    }

    /*
    void set_gradient(boolean b) {
        gradient = b;
    }*/

    void set_fading(boolean b) {
        fade = b;
    }

    void set_inversion(boolean b) {
        inversion = b;
    }

    void setMotionBlur(int k) {
        if (k < 0) {
            k = 0;
        } else if (k > 256) {
            k = 256;
        }
        motionblurp = k;
        motionblurq = 256 - k;
    }

    void setColorFilterWeight(int k) {
        if (k < 0) {
            k = 0;
        } else if (k > 256) {
            k = 256;
        }
        this.filterweight = k;
    }

    boolean RectangularBounds(RenderStateMachine m) {
        return m.f_x < W8 && m.f_y < H8 && m.f_x >= 0 && m.f_y >= 0;
    }

    boolean OvalBounds(RenderStateMachine m) {
        float x = (float) m.f_x * overW7 - 1;
        float y = (float) m.f_y * overH7 - 1;
        return x * x + y * y < 1;
    }

    boolean ElasticOvalBounds(RenderStateMachine m) {
        float x = (float) m.f_x * overW7 - 1;
        float y = (float) m.f_y * overH7 - 1;
        return x * x + y * y < this.boundRadius;
    }

    int projectionMask(RenderStateMachine m) {
        int grad = gradients[9][m.m_i];
        return ColorUtility.average(
                m.color, grad,
                0, 256 - grad);
    }
    Operator[] Renderers = {
        new Operator("Primary Renderer") {

            void operate(RenderStateMachine m) {
                if (fade_1 > 0) {
                    int f1 = (int) (256 * fade_1);
                    int f2 = 256 - f1;
                    for (m.m_i = m.m_start; m.m_i < m.m_end; m.m_i += m.stride) {
                        int i = m.m_i << 1;
                        set_pixel(m,(map_buffer[i]*f2+map[i]*f1)>>8,(map_buffer[i|1]*f2+map[i|1]*f1)>>8);
                    }
                    fade_1 -= .05f;
                    if (fade_1 <= 0)
                        swap_map();
                } else for (m.m_i = m.m_start; m.m_i < m.m_end; m.m_i += m.stride) {
                    int i = m.m_i << 1;
                    set_pixel(m, map[i], map[i|1]);
                }
            }
            void set_pixel(RenderStateMachine m, int imag, int real) {
                float x4 = real * rotation_constant_real;
                float x5 = imag * rotation_constant_imag;
                m.f_x = (int) (x4 + x5) + Cx;
                m.f_y = (int) (rotation_constant_coef * (real - imag) - x4 + x5) + Cy;

                visu_opr.operate(m);
                m.color ^= grad_invert;

                //buffer_buffer.setElem(m.m_i,m.color);
                //disply_buffer.setElem(m.m_i,m.color);
                if (noise_on) m.color = ColorUtility.noise(noise_mask,m.color);
                buffer_buffer.setElem(m.m_i, m.color = ColorUtility.average(m.color, motionblurp,
                        buffer.disply.buffer.getElem(m.m_i), motionblurq));

                disply_buffer.setElem(m.m_i, m.color);
            }
        },
        new Operator("Image Mode") {

            DoubleBuffer.ImageRenderContext sketch;

            void operate(RenderStateMachine m) {
                if (buffer.image == null) {
                    Renderers[0].operate(m);
                    return;
                }
                sketch = buffer.image;
                m = new RenderStateMachine();
                if (fade_1 > 0) {
                    int f1 = (int) (256 * fade_1);
                    int f2 = 256 - f1;
                    for (m.m_i = m.m_start; m.m_i < m.m_end; m.m_i += m.stride) {
                        int i = m.m_i << 1;
                        set_pixel(m, (map_buffer[i] * f2 + map[i] * f1) >> 8,
                                (map_buffer[i | 1] * f2 + map[i | 1] * f1) >>
                                8);
                    }
                    fade_1 -= .05f;
                    if (fade_1 <= 0) {
                        swap_map();
                    }
                } else {
                    for (m.m_i = m.m_start; m.m_i < m.m_end; m.m_i += m.stride) {
                        int i = m.m_i << 1;
                        set_pixel(m, map[i], map[i | 1]);
                    }
                }
            }
            void set_pixel(RenderStateMachine m, int imag, int real) {
                buffer_buffer.setElem(m.m_i, ColorUtility.average(
                        m.color = grad_invert ^ sketch.get(real + Cx, imag + Cy), motionblurp,
                        buffer.disply.buffer.getElem(m.m_i), motionblurq));
                //disply_buffer.setElem(m.m_i, projectionMask(m));
                disply_buffer.setElem(m.m_i, m.color);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////
    Operator[] Blendings = {
        new Operator("Rectangular Window") {

            void operate(RenderStateMachine rsm) {
                if (RectangularBounds(rsm)) {
                    rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    offs_opr.operate(rsm);
                }
                colf_opr.operate(rsm);
            }
        },
        new Operator("Oval Window") {

            void operate(RenderStateMachine rsm) {
                if (OvalBounds(rsm)) {
                    rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    offs_opr.operate(rsm);
                }
                colf_opr.operate(rsm);
            }
        },
        new Operator("Elastic Oval") {

            void operate(RenderStateMachine rsm) {
                if (ElasticOvalBounds(rsm)) {
                    rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    offs_opr.operate(rsm);
                }
                colf_opr.operate(rsm);
            }
        },
        new Operator("Horozontal Window") {

            void operate(RenderStateMachine rsm) {
                if (rsm.f_y > 0 && rsm.f_y < H8) {
                    rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    offs_opr.operate(rsm);
                }
                colf_opr.operate(rsm);
            }
        },
        new Operator("Vertical Window") {

            void operate(RenderStateMachine rsm) {
                if (rsm.f_x > 0 && rsm.f_x < W8) {
                    rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    offs_opr.operate(rsm);
                }
                colf_opr.operate(rsm);
            }
        },/*
        new Operator("Chequre Window") {
        void operate(RenderStateMachine rsm) {
        long w8 = W8 ;
        long h8 = H8 ;
        long fx = rsm.f_x ;
        long fy = rsm.f_y ;
        double ffx = fx<0 ? 2.0+(double)fx/H7 : (double)fx/H7 ;
        double ffy = fy<0 ? 2.0+(double)fy/H7 : (double)fy/H7 ;
        double fffx = ffx%2.0;
        double fffy = ffy%2.0;
        if ((fffx<1.0)==(fffy<1.0))
        offs_opr.operate(rsm);
        else
        rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
        }
        },*/
        new Operator("Inverse Oval Window") {

            void operate(RenderStateMachine rsm) {
                if (OvalBounds(rsm)) {
                    offs_opr.operate(rsm);
                } else {
                    rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                }
                colf_opr.operate(rsm);
            }
        },
        new Operator("No Window") {

            void operate(RenderStateMachine rsm) {
                offs_opr.operate(rsm);
                colf_opr.operate(rsm);
            }
        },
        new Operator("Framed Window") {

            void operate(RenderStateMachine rsm) {
                rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                if (((rsm.m_i + (W << 2)) % M_LEN) <= (W << 3) || ((rsm.m_i + 4) % W) <=
                        8) {
                    rsm.color = fade_color;
                } else {
                    rsm.color = rsm.old_color;
                }
                grad_opr.operate(rsm);
                colf_opr.operate(rsm);
            }
        },
        new Operator("Kaliedescope") {
            void operate(RenderStateMachine rsm) {

                int k,i,j,l;
                float x,y,temp,l3;
                x = (rsm.f_x)*W8n;
                y = (rsm.f_y - cc23)*H8n;
                x -= 0.5f*y ;
                if (x<0) x+=3-3*((int)(x*0.333333333333333333f));
                if (y<0) y+=3-3*((int)(y*0.333333333333333333f));
                i = ((int)y)%3;
                k = ((int)x)%3;
                x -= (int)x;
                y -= (int)y;
                l = ((k-i)+3)%3;
                if ((x+y)>1.0f) {
                    temp = 1-y;
                    y = 1-x;
                    x = temp;
                }
                l3 = 1-x-y;
                //rsm.f_x = (int)(.5+x*trix[l]+y*trix[(l+1)%3]+l3*trix[(l+2)%3]);
                //rsm.f_y = (int)(.5+x*triy[l]+y*triy[(l+1)%3]+l3*triy[(l+2)%3]);
                switch(l){
                    case 0:
                        rsm.f_x = (int)(y*x2+l3*x3);
                        rsm.f_y = (int)(x*y1+y*y2+l3*y3);
                        break;
                    case 1:
                        rsm.f_x = (int)(x*x2+y*x3);
                        rsm.f_y = (int)(x*y2+y*y3+l3*y1);
                        break;
                    case 2:
                        rsm.f_x = (int)(x*x3+l3*x2);
                        rsm.f_y = (int)(x*y3+y*y1+l3*y2);
                        break;
                }
                offs_opr.operate(rsm);
                colf_opr.operate(rsm);
            }
        },
        new Operator("Kaliedescope") {
            void operate(RenderStateMachine rsm) {

                int k,i,j,l;
                float x,y,temp,l3;
                x = (rsm.f_x)*W8n;
                y = (rsm.f_y - cc23)*H8n;
                x -= 0.5f*y ;
                if (x<0) x+=3-3*((int)(x*0.333333333333333333f));
                if (y<0) y+=3-3*((int)(y*0.333333333333333333f));
                i = ((int)y)%3;
                k = ((int)x)%3;
                x -= (int)x;
                y -= (int)y;
                l = ((k-i)+3)%3;
                if ((x+y)>1.0f) {
                    temp = 1-y;
                    y = 1-x;
                    x = temp;
                }
                l3 = 1-x-y;
                //rsm.f_x = (int)(.5+x*trix[l]+y*trix[(l+1)%3]+l3*trix[(l+2)%3]);
                //rsm.f_y = (int)(.5+x*triy[l]+y*triy[(l+1)%3]+l3*triy[(l+2)%3]);
                switch(l){
                    case 0:
                        rsm.f_x = (int)(y*x2+l3*x3);
                        rsm.f_y = (int)(x*y1+y*y2+l3*y3);
                        break;
                    case 1:
                        rsm.f_x = (int)(x*x2+y*x3);
                        rsm.f_y = (int)(x*y2+y*y3+l3*y1);
                        break;
                    case 2:
                        rsm.f_x = (int)(x*x3+l3*x2);
                        rsm.f_y = (int)(x*y3+y*y1+l3*y2);
                        break;
                }
                offs_opr.operate(rsm);
                colf_opr.operate(rsm);
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    /** These functions determine what to do when the mapping goes offscreen */
    Operator[] Offscreen = {
        new Operator("Color Fill") {

            void operate(RenderStateMachine rsm) {
                rsm.color = rsm.old_color = fade_color;
                grad_opr.operate(rsm);
            }
        },
        new Operator("Edge Extend") {

            void operate(RenderStateMachine rsm) {
                rsm.color = rsm.old_color = buffer.output.get(
                        (rsm.f_x < 0) ? 0 : (rsm.f_x > W8) ? W8 : rsm.f_x,
                        (rsm.f_y < 0) ? 0 : (rsm.f_y > H8) ? H8 : rsm.f_y);
                grad_opr.operate(rsm);
            }
        },
        new Operator("Reflect") {
            void operate(RenderStateMachine rsm) {
                rsm.color = rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                grad_opr.operate(rsm);
            }
        },
        new Operator("Sketch") {
            void operate(RenderStateMachine rsm) {
                rsm.color = rsm.old_color = buffer.image.get(rsm.f_x, rsm.f_y);
                grad_opr.operate(rsm);
            }
        },
        new Operator("SketchII") {
            void operate(RenderStateMachine rsm) {
                float x = (float) rsm.f_x * overW7 - 1;
                float y = (float) rsm.f_y * overH7 - 1;
                float R = x * x + y * y;
                int K = max(0, min(256, (int) ((2 - R) * 256)));
                rsm.color = rsm.old_color = ColorUtility.average(
                        buffer.output.get(rsm.f_x, rsm.f_y), K,
                        buffer.image.get(rsm.f_x, rsm.f_y), 256 - K);
                grad_opr.operate(rsm);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////
    /** These functions determine which gradient to apply*/
    Operator[] Gradients = {
        new Operator("No Gradient") {

            void operate(RenderStateMachine rsm) {
            }
        },
        new Operator("Simple Gradient") {

            void operate(RenderStateMachine rsm) {
                int grad = 0xFF - (char) min(0xFF, max(0, radial_gradient[rsm.m_i] *
                        gradient_slope_constant -
                        gradient_offset));
                rsm.color = ColorUtility.average(rsm.color, 0xFF ^ grad,
                        fade_color, grad);
            }
        },
        new Operator("Accented Gradient") {

            void operate(RenderStateMachine rsm) {
                int grad = 0xFF - (char) min(0xFF, max(0, radial_gradient[rsm.m_i] *
                        gradient_slope_constant -
                        gradient_offset));
                rsm.color =
                        ColorUtility.average(rsm.color, 0xFF ^ grad,
                        ColorUtility.average(accent_color, 0xFF ^
                        grad ^ grad_switch, fade_color, grad ^ grad_switch), grad);
            }
        }
    };
    Operator[] ColorFilters = {
        new Operator("None") {
            void operate(RenderStateMachine rsm) {
            }
        },
        new Operator("RGB") {
            void operate(RenderStateMachine rsm) {
                int c1 = rsm.color;
                int c2 = RGB_contrast(rsm.color);
                int c3 = contrast(rsm.color);
                c3 = average(c3,filter_param1,c2,256-filter_param1);
                rsm.color = average(c1,filter_param2,c3,256-filter_param2);
            }
        },
        new Operator("mush !") {
            void operate(RenderStateMachine rsm) {
                int c1 = rsm.color;
                int c2 = contrast(rsm.color);
                int c3 = mush(rsm.color);
                c3 = average(c3,filter_param1,c2,256-filter_param1);
                rsm.color = average(c1,filter_param2,c3,256-filter_param2);
            }
        }/********,
        new Operator("RGB") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(RGB_contrast(rsm.color),filterweight,rsm.color,256-filterweight);
            }
        },*//*
        new Operator("RGB") {
            void operate(RenderStateMachine rsm) {
                rsm.color = RGB_contrast(rsm.color,filterweight);
            }
        },
        new Operator("SAT") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(saturate(rsm.color),filterweight,rsm.color,256-filterweight);
            }
        },*/
        /******
        new Operator("SAT3") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(average(saturate(rsm.color),~saturate(~rsm.color)),filterweight,rsm.color,256-filterweight);
            }
        },*//*
        new Operator("dSAT") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(dark_saturate(rsm.color),filterweight,rsm.color,256-filterweight);
            }
        },
        new Operator("lSAT") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(~dark_saturate(~rsm.color),filterweight,rsm.color,256-filterweight);
            }
        },*/
        /******
        new Operator("Contrast") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(contrast(rsm.color),filterweight,rsm.color,256-filterweight);
            }
        },
        new Operator("Contrast") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(average(contrast(rsm.color),RGB_contrast(rsm.color)),filterweight,rsm.color,256-filterweight);
            }
        },
        new Operator("Contrast") {
            void operate(RenderStateMachine rsm) {
                rsm.color = average(average(contrast(rsm.color),average(saturate(rsm.color),~saturate(~rsm.color))),filterweight,rsm.color,256-filterweight);
            }
        }*/
    };

    Operator[] ColorPermutations = {
        new Operator("None") {
            void operate(RenderStateMachine rsm) {
            }
        },
        new Operator("invert") {
            void operate(RenderStateMachine rsm) {
                rsm.color ^= 0xffffff;
            }
        },
        new Operator("permute") {
            void operate(RenderStateMachine rsm) {
                rsm.color = 0xffff&(rsm.color>>8)|(0xff0000&(rsm.color<<8));
            }
        },
        new Operator("ok") {
            void operate(RenderStateMachine rsm) {
                rsm.color = 0xffff&(rsm.color>>8)|(0xff0000&(rsm.color<<8));
                rsm.color = 0xffff&(rsm.color>>8)|(0xff0000&(rsm.color<<8));
            }
        },
        new Operator("just weird") {
            void operate(RenderStateMachine rsm) {
                int displaced = 0xffff&(rsm.color>>8)|(0xff0000&(rsm.color<<8));
                rsm.color = average(displaced,rsm.color);
            }
        }
    };

    float QBYUR = 1 / 255.f;
    ////////////////////////////////////////////////////////////////////////////
    /** These functions set the fade color for the mapping*/
    Operator[] ColorMode = {
        new Operator("Black") {

            void operate(RenderStateMachine rsm) {
                fade_color = 0x00000000;
            }
        },
        new Operator("White") {

            void operate(RenderStateMachine rsm) {
                fade_color = 0x00FFFFFF;
            }
        },
        new Operator("Middle Pixel Hue") {

            void operate(RenderStateMachine rsm) {
                int temp_color = buffer_buffer.getElem(M_MID);

                float[] HSV = java.awt.Color.RGBtoHSB(
                        (temp_color >> 16) & 0x000000FF,
                        (temp_color >> 8) & 0x000000FF,
                        (temp_color) & 0x000000FF,
                        new float[]{0, 0, 0});

                fade_color = java.awt.Color.HSBtoRGB(HSV[0], 1.f, 1.f);
            }
        },
        new Operator("not Middle Pixel Hue") {

            void operate(RenderStateMachine rsm) {
                fade_color = ~buffer_buffer.getElem(M_MID);
            }
        },
        new Operator("Middle Pixel Hue Rotate") {

            float hue = 0;

            void operate(RenderStateMachine rsm) {
                int temp_color = buffer_buffer.getElem(M_MID);

                float[] HSV = java.awt.Color.RGBtoHSB(
                        (temp_color >> 16) & 0xFF,
                        (temp_color >> 8) & 0xFF,
                        (temp_color) & 0xFF,
                        new float[]{0, 0, 0});

                HSV[0] += .2f;

                fade_color = java.awt.Color.HSBtoRGB(HSV[0], 1.f, 1.f);
            }
        },/*
        new Operator("External Input") {

            void operate(RenderStateMachine rsm) {
                fade_color = input_color;
            }
        },*/
        new Operator("Hue Rotate") {

            int HUE = 0;

            void operate(RenderStateMachine rsm) {
                fade_color = Color.HSBtoRGB((float) HUE * .01f, 1.f, 1.f);
                HUE++;
            }
        }
    };
}
