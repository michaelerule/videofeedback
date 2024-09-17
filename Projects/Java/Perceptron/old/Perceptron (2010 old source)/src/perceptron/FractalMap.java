package perceptron;
/* FractalMap.java
 *
 * Version 1.0 (2010)
 *
 * @author Michael Everett Rule
 *
 *  public class FractalMap with
 *  public FractalMap(DoubleBuffer b, Vector<Mapping> maps, Perceptron parent)
 *
 */

import util.ColorUtility;
import math.MathToken;
import math.complex;
import math.Equation;
import math.ComplexVarList;
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.util.Vector;
import static util.ColorUtility.*;
import static java.lang.Math.*;

/**
 * @author Michael Everett Rule
 */
public class FractalMap {

    /** The input variables for the FractalMap.class. */
    DoubleBuffer buffer;
    Vector<Mapping> mappings;
    Perceptron perceptron;
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
    /** Weather or not to compute a fractal mapping */
    boolean active = true;
    //////// USER EDITABLE PARAMETERS ////
    //////////////////// Fading data /////////////////////////////////
    /** A weight to determine how much to fade the background by*/
    int fade_weight = 128, anti_fade_weight;
    /** A default color to fade the background towards, if desired */
    int fade_color = 0x00000000;
    /** A color generated from the an external source, like audio input */
    int input_color = 0x00000000;
    /** Count off how many frames used between the
     * fade in and out.     */
    float fade_1;
    //////////////// Customisation data ////////////////////////////
    /** The gradient parameters. */
    float gradient_slope_constant = 1f;
    float gradient_offset = 0f;
    /** The default NO INVERSION value. The preset overrides
     * the value!   */
    int gradient_inversion = 0xFFFFFF;
    public boolean gradient_inversion_flag = false;
    int gradient_switch = 0x0;
    int[] accent_colors = new int[]{0x0, 0xFFFFFF, 0xFFFF00, 0x8000FF};
    int accent_color_index;
    int accent_color = 0x00;
    /** Which gradient (not gradient function) to use. */
    int gradient_selection = 0;
    /** The gradients themselves. */
    char[][] gradients;
    char[] radial_gradient;
    /** The default selection for the range of visual
     * functions. The preset overrides these values. */
    /** Which boundary condition to use */
    int boundary_condition_number = 1;
    /** Which color functions to use when the index is out of bounds */
    int outside_coloring_number = 2;
    /** Which "fader function" to use for out of limit circle pixels. */
    int fade_number = 3;
    /** Which gradient function to use for out of limit circle pixels. */
    int gradient_number = 2;
    /** Which mapping (equation f(z)) to use from the
     * list of equations in the settings file.  */
    int equation_number = 0;
    /** Which color filters to use */
    int color_filter_number = 0;
    /** Renderer function number.
     * Alternatively, the image mode. External Buffer
     * (0 for none) to import, a.k.a. load an image and
     * set the image mode.  */
    int renderer_number = 0;
    /** Color filter. Press LEFT or RIGHT arrows.     */
    int filterweight = 128;
    int filter_param1 = 128;
    int filter_param2 = 128;
    /** Coordinate system type in the complex plane. */
    int ortho_type = 0;
    int polar_type = 0;
    /** Motion blur. Press UP or DOWN arrows.     */
    int motionblurp = 0;
    int motionblurq = 256;
    ////////////////// Other Member Data //////////////////////
    /** True if the map is currently being updated. calls to run will have
     *  no effect as long as the Thread is already running. */
    boolean running = false;
    boolean interpolate = true;
    boolean fade = true;
    boolean inversion = false;
    boolean dampen_colors = true;
    boolean OMG = false;    //  ignore
    boolean mega = false;
    /** Phase shift used for animating the fractal. */
    int phase_shift;
    /** BUFFERS AND ASSOCIATED INFORMATION */
    /** These 1D arrays contain the values of z and f(z). */
    int[] map, map_buffer;
    /** The arrays of values that indicate each point's z
     * divergence or convergence. I am puzzled by the
     * fact that we determine this in a single step, per
     * map f(z) in the compute_lookup(), immediately
     * after calculating the value of f(z).     */
    float[] z_divergence;
    float[] z_convergence;
    /**  THE 4 DATA BUFFERS */
    DataBuffer output_buffer, // current image on the screen
            buffer_buffer, // image buffer after mapping
            sketch_buffer, // external, loaded image
            display_buffer;              // cursors, circles, dots
    /** The Screen data to which to apply the mapping */
    /** The Width and Height and other common things */
    final int W;
    final int H;
    final int W2;
    final int H2, W7, H7, W8, H8, W9, H9, M_END;
    final int M_LEN, M_MID, HALF_W, HALF_H, TWO_W, TWO_M_LEN;
    final int TWO_H, W_M_ONE, H_M_ONE, M_ONE, M_M_ROW;
    final int W_P_ONE, MAX_C = 65535, BOUNDS;
    final float overW7, overH7;
    /** Precomputed scalars for converting between the complex plane, the
     *  receptive field, and the mapped output */
    float complex_to_map_W_scalar;
    float complex_to_map_H_scalar;
    float complex_to_W_scalar;
    float complex_to_H_scalar;
    /** The upper left, the lower right, and the size of the rectangle in the
     *   complex plane. These are actually re-initialized so these values don't matter.     */
    public static final complex size = new complex(6f, 6f);
    public static final complex upper_left = new complex();
    public static final complex lower_right = new complex();
    public static final int ROTATIONAL_RENDERER = 0;
    public static final int IRROTATIONAL_RENDERER = 2;
    public static final int IMAGE_RENDERER = 1;
    /** For the elastic limit circle */
    double boundRadius = 1;
    /** The Operators are the 1D arrays of functions
     * counted by numbers, that can be inserted
     * dynamically during the execution to produce
     * different visual effects. That is, they mostly
     * operate with the individual pixels, points of
     * the final fractal.      */
    Operator boundary_condition_operator,
            outside_coloring_operator,
            fade_operator,
            renderer_operator,
            gradient_operator,
            color_filter_operator;
    /** The fractal mapping f(z), a.k.a. the "equation". */
    Mapping mapping;
    /** The list of complex variables. */
    final static ComplexVarList new_vars = ComplexVarList.standard();
    /** The constant c in f(z, c) and the rotation constant. */
    complex constant, rotation_constant;
    float[] norm_c;
    /** The rotation constants and the normalized
     * constant c tied to rotation.     */
    float rotation_constant_real, rotation_constant_imag, rotation_constant_coef;
    float c_r, c_i, g_r, g_i;
    int Cx, Cy;
    /** A few more constants... */
    static final double PI_OVER_TWO = Math.PI * .5;
    float d_r = 0, d_i = 0;
    float rotation_drift = .1f;
    float QBYUR = 1 / 255.f;

    ////////////////////////////////////////////////////////////////////////////
    //////////////// Constructor ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /** Creates a new instance of FractalMap */
    public FractalMap(DoubleBuffer b, Vector<Mapping> maps, Perceptron parent) {

        perceptron = parent;

        new_vars.set(18, new complex(size));
        new_vars.set(22, new complex(size.real));
        new_vars.set(7, new complex(size.imag));

        mappings = maps;
        if (mappings == null) {
            mappings = new Vector<Mapping>();
        }

        /** FractalMap loads the buffer. */
        buffer = b;

        /** The screen width and height, and all the
         * related constants.         */
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
        W9 = W << 9;
        H9 = H << 9;
        W_P_ONE = W + 1;
        W_M_ONE = W - 1;
        H_M_ONE = H - 1;
        /** We later convert the two dimensional screen into
         * a one dimensional array the size of M_LEN.         */
        M_LEN = W * H;
        TWO_M_LEN = M_LEN << 1;
        M_END = M_LEN - 1;
        M_ONE = M_END - 1;
        M_M_ROW = M_ONE - W;
        HALF_W = W / 2;
        HALF_H = H / 2;
        TWO_W = W << 1;
        TWO_H = H << 1;
        M_MID = HALF_W + W * HALF_H;
        BOUNDS = min(W7, H7);

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

        /** Select the set of functions that will be used
         * at a given moment for drawing the fractal. */
        initialize_functions();
        /** Prepare the buffers for the mapping f(z). */
        initialize_lookups();
        /** Prepare the mapping f(z). */
        initialize_equation();
    }

    /** These respond to keyboard options "," and
     * ".".     */
    public void inc_ortho() {
        ortho_type = (ortho_type + 1) % 3;
    }

    public void inc_polar() {
        polar_type = (polar_type + 1) % 3;
    }

    /////////////////////// Initializers //////////////////////////////////
    /** This assigns for the first time the []_function 
     * variables, which represent adaptable code
     * which may be switched in and out at any given
     * time. The selection of operators.     */
    void initialize_functions() {

        /** The selection of any of the per-point functions
         * (operators) each contained in an array of its own,
         * occurs via the keyboard option (R, E, F, G, X)
         * that cycles through all the function index numbers.         */
        boundary_condition_number = wrap(boundary_condition_number, Boundary_conditions.length);
        outside_coloring_number = wrap(outside_coloring_number, Outside_coloring.length);
        fade_number = wrap(fade_number, FadeColor.length);
        gradient_number = wrap(gradient_number, Gradients.length);
        color_filter_number = wrap(color_filter_number, ColorFilters.length);

        /** The selected functions are initialized, that is,
         * selected. */
        boundary_condition_operator = Boundary_conditions[boundary_condition_number];
        outside_coloring_operator = Outside_coloring[outside_coloring_number];
        fade_operator = FadeColor[fade_number];
        gradient_operator = Gradients[gradient_number];
        color_filter_operator = ColorFilters[color_filter_number];
        /** ...and the key function, renderer.   */
        renderer_operator = Renderers[renderer_number];

        /** Select the fractal map f(z), a.k.a. the "equation"
         *  from the list of equations in the settings file.
         * Later, switch them by pressing Q or W.          */
        if (mappings.size() != 0) {
            equation_number %= mappings.size();
            mapping = mappings.get(equation_number);
        }

        /** Call the initialization of gradients.
         * gradients is a large 2D array of all the gradients
         * (gradient shapes). radial_gradient is a 1D array 
         * that represents one of the 10 gradients (gradient 
         * shapes) that are M_LEN deep.          */
        gradients = init_gradients();
        gradient_selection = 0;    // select the gradient, zero works
        /** Change the selection of gradient shape by pressing '. */
        radial_gradient = gradients[gradient_selection];
    }

    /** Initialize 1D array for the f(z) to transform and
     * load the result into another 1D array.     */
    void initialize_lookups() {
        /** The length of 1D array is W*H of screen. 
         * It will contain the scaled-to-screen points z. */
        map = new int[TWO_M_LEN];
        map_buffer = new int[TWO_M_LEN];   // the output array after f(z), scaled to screen
        /** For the convergent and divergent bailout
         * tests.         */
        //z_divergence = new float[TWO_M_LEN];
        z_convergence = new float[TWO_M_LEN];
        compute_lookup();
    }

    /** Prepare the fractal map, i.e. equation. */
    final void initialize_equation() {
        constant = new complex();
        rotation_constant = new complex();
        norm_c = new float[]{0, 0};
        set_constant(0, 0);
    }

    /** Calculate lookup table values, store them in the buffer.
     * The fractal map f(z) is applied here to a raster grid,
     * part of the complex plane. The result is stored in
     * the map_buffer[] the size of M_LEN.
     * The compute_lookup executes once per map.     */
    private void compute_lookup() {
        if (active) {
            try {
                if (fade_1 > 0) {
                    return;
                }
                fade_1 = 1.f;
                complex z = new complex();  // we create complex z = 0 without affecting the buffer
                complex z_new = new complex();
                int i = 0;
                /** For the bailout tests, set k, k2. */
                //int k = 0;
                int k2 = 0;
                float upper_left_real = upper_left.real;
                float upper_left_imag = upper_left.imag;
                for (int y = 0; y < H; y++) {
                    for (int x = 0; x < W; x++) {
                        z.real = x * complex_to_map_W_scalar + upper_left_real;
                        z.imag = y * complex_to_map_H_scalar + upper_left_imag;
                        z_new = mapping.operate(z);      // f(z) = do_equation(z);
                        //z_divergence[k++] = z_new.real * z_new.real + z_new.imag * z_new.imag;
                        //k++;
                        z_convergence[k2++] = (z_new.real - z.real) * (z_new.real - z.real) + (z_new.imag - z.imag) * (z_new.imag - z.imag);
                        /** or, z_convergence could be an array of precalculated boolean values
                         * z_convergence[k2++] = ((z_new.real - z.real) * (z_new.real - z.real) + (z_new.imag - z.imag) * (z_new.imag - z.imag)) > 0.1;    */
                        k2++;
                        /** The map_buffer[i] array contains the interchanging real and imaginary
                         * values of z calculated and scaled from the complex plane to the screen.  */
                        map_buffer[i++] = (int) (0x100 * (complex_to_W_scalar
                                * (z_new.real - upper_left_real))) - W7;
                        map_buffer[i++] = (int) (0x100 * (complex_to_H_scalar
                                * (z_new.imag - upper_left_imag))) - H7;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    /** The fade out and fade into mode is between 
     * the map changes. This is not about the fade-to
     * color.     */
    private boolean is_fading() {
        return fade_1 > 0;
    }

    /** Load the fractal map f(z), a.k.a. the "mapping",
     * a.k.a. function f(z), a.k.a. "equation".
     *
     * @param map
     */
    public void load_equation(Mapping map) {
        if (is_fading()) {
            return;
        }
        mapping = map;
        compute_lookup();
    }

    /** Swap the two 1D arrays, the "maps" that contain the
     * values z before and after mapping f(z). Tricky language?
     * Used for fading.     */
    private void swap_map() {
        int[] temp = map;
        map = map_buffer;
        map_buffer = temp;
    }

    /** The gradient inversion. Press J. 
     * The gradient inverts when gradient_inversion = 0.
     * When it is 16777215, the gradient is normal.
     * In hexadecimal, that is 0x0 and 0xFFFFFF. 
     * This sucks inward all the contour colors and
     * leaves only the recursive conformal mapping
     * bending the help screen and the cursors.     */
    public void toggle_color_inversion() {
        gradient_inversion ^= 0xFFFFFF;     // ^ is the bitwise XOR operator
        gradient_inversion_flag = !gradient_inversion_flag;
    }

    /* The gradient direction. Press K.
     * The gradient is enumerated from 0 to 256 or from
     * 256 to 0. The default value on screen is 255.    */
    public void toggle_gradient_switch() {
        gradient_switch ^= 0xFF;
    }

    /** Press I for interpolation. On by default.   */
    public void toggle_interpolation() {
        interpolate = !interpolate;
    }

    /** Applies the current mapping to the screen raster, 
     * storing the result in the output raster. The screen
     * contents are called buffers.     */
    void operate() {
        /** If the calculation of the entire screen is over, display it.   */
        if (!active) {
            buffer.flip();
            /** Display buffer.output to the screen.    */
            buffer.output.graphics.drawImage(buffer.buffer.image, 0, 0, null);
            return;
        }

        /** Flip buffer. The first call out of two.*/
        buffer.flip();

        /** The current screen.  */
        output_buffer = buffer.output.buffer;
        /**The previous frame, or to say, the exchange frame.   */
        buffer_buffer = buffer.buffer.buffer;
        /** The cursors and such displays. These
         * behave as the persistent initial set entering
         * the recursive conformal mapping.     */
        display_buffer = buffer.display.buffer;

        if (buffer.image != null) {
            sketch_buffer = buffer.image.buffer;
        }

        /** Update the default color of the pixel.
         * New RenderStateMachine is created locally.
         * This is required for the fade color function to
         * work.         */
        update_color();

        if (OMG) {
            gradient_inversion ^= ~0;   // Clear a flag n in an integer set of flags (binary sequences).
        }

        /** Time count. 
         * Unused         */
        long time = System.currentTimeMillis();

        /** Constant c coupled to rotation and coordinate system related information...         */
        switch (ortho_type) {
            case 0:
                Cx = (int) (.5f + 0x100 * ((norm_c[0]))) + W7;
                Cy = (int) (.5f + 0x100 * ((norm_c[1]))) + H7;
                break;
            case 1:
                Cx = (int) (.5f + 0x100 * ((d_r = (abs(d_r) > MAX_C) ? 0 : d_r + .1f * norm_c[0]))) + W7;
                Cy = (int) (.5f + 0x100 * ((d_i = (abs(d_i) > MAX_C) ? 0 : d_i + .1f * norm_c[1]))) + H7;
                break;
            case 2:
                Cx = W7;
                Cy = H7;
                break;
            default:
                break;
        }
        complex local_rotation = new complex();
        switch (polar_type) {
            case 0:
                local_rotation = rotation_constant;
                break;
            case 1:
                rotation_drift += complex.arg(rotation_constant) * 0.01f;
                local_rotation = complex.fromPolar(complex.mod(rotation_constant), rotation_drift);
                break;
            case 2:
                local_rotation = complex.fromPolar(1.0f, (float) (PI_OVER_TWO));
                break;
            default:
                break;
        }

        rotation_constant_real = mega ? (float) Math.pow(10, local_rotation.real) : local_rotation.real;
        rotation_constant_imag = mega ? (float) Math.pow(10, local_rotation.imag) : local_rotation.imag;

        rotation_constant_coef = rotation_constant_real + rotation_constant_imag;

        /** Interpolation. */
        buffer.output.set_interpolated(interpolate);

        /** Move on to the next frame. */
        buffer.image.grabber.nextFrame();

        /********************************************************
         * THE PRIMARY RENDERING PROCEDURE.
         *
         * New RenderStateMachine is created, which
         * all other functions (operators) use later to
         * get data, orderly, until they update the color
         * of the point z. The RenderStateMachine stores
         * the data per point (pixel).
         ********************************************************/
        renderer_operator.operate(new RenderStateMachine());

        /** Flip buffer. The second call out or two.*/
        buffer.flip();
    }

    ////////////////////////////////////////////////////////////////////////////
    /////////////// Member Functions ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Getters ////////////////////////////////////
    /** Applies the current mapping to the given parameter
     * Unused.     */
    public complex do_equation(complex z) {
        return mapping.operate(z);
    }

    /** Returns a copy of the constant
     * Unused     */
    public complex get_constant() {
        return new complex(constant);
    }

    /** Returns a copy of the size of the complex plane area covered
     *   Unused     */
    public complex get_size() {
        return new complex(constant);
    }

    /** Returns weather this FractalMap is active. */
    public boolean is_active() {
        return active;
    }

    /////////////////////////// Setters ////////////////////////////////////
    /** Toggles active computation of the fractal map on and off */
    public void set_active(boolean active) {
        this.active = active;
    }

    /** Sets the constant c value in f(z, c). */
    public void set_constant(float real, float imag) {
        constant.real = real;
        constant.imag = imag;
    }

    /** Sets the "constant c" (parameter c of Julia fractal)
     * value given an x and y location of the cursor on
     * the screen.    */
    public void set_norm_c(float x, float y) {
        constant.real = (W - x) / complex_to_W_scalar + upper_left.real;
        constant.imag = (H - y) / complex_to_H_scalar + upper_left.imag;
        compute_norm_c();
    }

    /** Computes the constant displacement value scaled for the dimensions of
     *  the screen */
    void compute_norm_c() {
        if (norm_c == null) {
            norm_c = new float[]{0, 0};     // new Point(0,0);
        }
        if (constant == null) {
            constant = new complex(0, 0);
        }
        norm_c[0] = (float) (constant.real * complex_to_W_scalar);
        norm_c[1] = (float) (constant.imag * complex_to_H_scalar);
    }

    /** Sets rotation parameter of map. Also applies to
     * pullback function accessible via the blue cursor.     */
    public void set_normalized_rotation(float x, float y) {
        rotation_constant.real = ((W - x) / complex_to_W_scalar + upper_left.real);
        rotation_constant.imag = ((H - y) / complex_to_H_scalar + upper_left.imag);
        float theta = complex.arg(rotation_constant);
        float r = complex.mod(rotation_constant) * 2;
        rotation_constant = complex.fromPolar(1 / r, theta);    // this 1/r is used for pullback
        boundRadius = 1 / (r * r);      // not in use?
    }

    /** Set the input color.
     * Unused.     */
    public void set_input_color(int col) {
        input_color = col;
    }

    /** Wrapper for the number of the function in the
     * array of functions.      */
    private static int wrap(int n, int m) {
        return n < 0 ? m - (-n % m) : n % m;
    }

    /** Beyond the primary renderer for drawing fractals,
     * the other renderer is for image mode - to load an
     * image and recursively apply the conformal
     * mapping.   */
    public void increment_renderer(int n) {
        set_renderer(renderer_number + n);
    }

    public void set_renderer(int index) {
        if (!active) {
            return;
        }
        renderer_operator = Renderers[renderer_number = wrap(index, Renderers.length)];
        if (renderer_number == IRROTATIONAL_RENDERER) {
            perceptron.controls.active_cursors.remove(perceptron.controls.MapRotationCursor);
        } else if (!perceptron.controls.active_cursors.contains(perceptron.controls.MapRotationCursor)) {
            perceptron.controls.active_cursors.add(perceptron.controls.MapRotationCursor);
        }
    }

    /** Press R to change the boundary condition. */
    public void increment_boundary_condition(int n) {
        set_boundary_condition(boundary_condition_number + n);
    }

    public void set_boundary_condition(int index) {
        if (!active) {
            return;
        }
        boundary_condition_operator = Boundary_conditions[boundary_condition_number = wrap(index, Boundary_conditions.length)];
    }

    /** Press E to change the outside coloring. */
    public void increment_outside_coloring(int n) {
        set_outside_coloring(outside_coloring_number + n);
    }

    public void set_outside_coloring(int index) {
        if (!active) {
            return;
        }
        outside_coloring_operator = Outside_coloring[outside_coloring_number = wrap(index, Outside_coloring.length)];
    }

    /** Press F to change the fade color mode. */
    public void increment_fader(int n) {
        set_fader(fade_number + n);
    }

    public void set_fader(int index) {
        if (!active) {
            return;
        }
        fade_operator = FadeColor[fade_number = wrap(index, FadeColor.length)];
    }

    /** Press G to swith to another gradient function
     * (gradient operator).     */
    public void increment_gradient(int amount) {
        set_gradient(gradient_number + amount);
    }

    public void set_gradient(int index) {
        if (!active) {
            return;
        }
        gradient_operator = Gradients[gradient_number = wrap(index, Gradients.length)];
    }

    /** Press X to change the contrast enhancement.
     * (Preset default is 0.)   */
    public void increment_colorfilter(int amount) {
        set_colorfilter(color_filter_number + amount);
    }

    public void set_colorfilter(int index) {
        if (!active) {
            return;
        }
        color_filter_operator = ColorFilters[color_filter_number = wrap(index, ColorFilters.length)];
    }

    /** Press Q or W to change the equation f(z),
     * unrelated with the presets. The equations
     * are stored in the settings file. Q advances
     * on the list, and W cycles backwards.     */
    public void increment_map(int n) {
        set_map(equation_number + n);
    }

    public void set_map(int index) {
        if (!active || is_fading()) {
            return;
        }
        load_equation(mappings.get(equation_number = wrap(index, mappings.size())));
    }

    /** Unused. */
    public void increment_fade_weight(int n) {
        fade_weight = wrap(fade_weight + n, 256);
    }

    /** Color accent. Press H. */
    public void increment_accent(int n) {
        set_accent(accent_color_index + n);
    }

    public void set_accent(int color_accent) {
        accent_color = accent_colors[accent_color_index =
                wrap(color_accent, accent_colors.length)];
    }

    /** Press N to dampen fade color(s) during the
     * default update_color(). As a result, if you press
     * F for some of the fade modes, the colors will
     * cycle faster.    */
    public void dampen_colors_during_update_color() {
        dampen_colors = !dampen_colors;
    }

    /** The contrast (color filter) parameters tied with
     * the contrast cursor. */
    public void setContrastParameters(int x, int y) {
        this.filter_param1 = max(min(0xff, x), 0);
        this.filter_param2 = max(min(0xff, y), 0);
    }

    /** Press I. Always use interpolation. */
    public void set_interpolation(boolean s) {
        interpolate = s;
    }

    /** Set the gradient via the gradient cursor.   */
    public void set_gradient_param(float slope, float offset) {
        gradient_slope_constant = slope;
        gradient_offset = offset;
    }

    ///////////////// Other Member Functions //////////////
    /** Update the default color across the buffer with 
     * the fade-to color. This is required for the fade modes.
     * Otherwise, everything is black, white and gray
     * with full range of... grayness.
     *  The buffer is current when void operate() evokes the
     *  update_color() in the public class FractalMap.     */
    public void update_color() {
        RenderStateMachine m = new RenderStateMachine();
        m.old_color = fade_color; // could become obsolete
        m.color = fade_color;
        /** The fade modes depend on the following line.
         * No other function calls the fade_operator
         * anyway.          */
        fade_operator.operate(m);
        if (dampen_colors) {
            fade_color = average(fade_color, 56, m.color, 200);
        }
        anti_fade_weight = 256 - fade_weight;
    }

    /** Press CTRL, write an equation and press enter.
     * TODO let c be a protected symbol for the variable that should
     * change with the cursor movements.
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

    /** By calling setMapping, we can write new
     * equation f(z). In that case, the equation is
     * "hardcoded".
     * Keys 1, 2, 3... to 0 and F1 to F12 are assigned
     * to different hardcoded equations.
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    ///// INTERCHANGEABLE FUNCTIONS ////
    ////////////////////////////////////////////////////////////////////////////

    /** These can be switched in and out to create different visual effects.
     * These functions access the information about the mapping that
     * ordinarily would be confined to the scope of a single function.
     * To prevent passing and returning a large number of parameters,
     * or an array, to these functions, these function variables have been
     * made accessable to the entire class. This is not secure and
     * modifications to this class should be made with caution.*/
    /** The RenderState machine structure contains
     * the instance variables that the Operators can modify.
     *  The Renderer scans the map_buffer to set the pixel
     * color at the appropriate location in the screen buffer.
     * Various effect functions are then called to calculate
     * the color of the map element, and hence the pixel. */
    class RenderStateMachine {

        /** Pixel color */
        int color, old_color;   // old_color is used only in the operator("Framed Window")
        /** The integer representations of the coordinates
         * in the previous frame. They are integers multiplied 
         * by 256. The format uses 256=1.0, so the actual 
         * pixel value is X/256.The extra 8 bits allow the
         * interpolation without using floating point.         */
        int f_x, f_y;
        /** The target pixels are within a 1D array... read
         * more about int [] map, map_buffer. m_i is the
         * index of the target pixel at the given moment
         * of execution.  */
        int m_i;
        /** The size of map buffer. */
        int m_start = 0;
        int m_end = M_LEN;     // ...notice the M_LEN in init_gradient
        /** The step for walking across the map buffer. */
        int stride = 1;
    }

    /** Press quote.    */
    public void increment_gradient_shape(int n) {
        set_gradient_shape(gradient_selection + n);
    }

    /** Gradient shapes change the color, gradient (shades)
     * and transparency of the fractal contours.     */
    public void set_gradient_shape(int n) {
        radial_gradient = gradients[gradient_selection = wrap(n, gradients.length)];
    }

    /** The gradients store a char [0... 255] for each pixel on the screen,
     * in a 1D array of length W*H. At the moment, all the gradient arrays
     * are also contained in one BIG array called the "Gradients[][]". */
    char[][] init_gradients() {
        char[][] result = new char[10][M_LEN];
        int index = 0;
        for (int y = -HALF_H; y < HALF_H; y++) {
            for (int x = -HALF_W; x < HALF_W; x++) {
                /** These are the gradient shapes. */
                result[0][index] = (char) (255 - 512 * ((float) x * x / (W * W) + (float) y * y / (H * H)));
                result[1][index] = (char) (255 - (x + HALF_W) * 255 / W);
                result[2][index] = (char) (255 - (y + HALF_H) * 255 / H);
                result[3][index] = (char) (255 - (y + HALF_H) * (x + HALF_W) * 255 / (W * H));
                result[4][index] = (char) (255 - (abs(x) + abs(y)) * 255 / (HALF_W + HALF_H));
                result[5][index] = (char) (255 - (abs(x) * abs(y)) * 255 / (HALF_W * HALF_H));
                result[6][index] = (char) (255 * (1 - min(1, sqrt(x * x + y * y) / min(HALF_H, HALF_W))));
                result[7][index] = (char) (255 * (1 - pow(min(1, sqrt(x * x + y * y) / min(HALF_H, HALF_W)), 9)));
                result[8][index] = (char) (255 * (1 - pow(max(0, min(1, 1.3 - (sqrt(x * x + y * y)) / (min(HALF_H, HALF_W)))), 9)));
                result[9][index] = (char) (result[7][index]);
                index++;
            }
        }
        /** The init_gradients returns all the gradients
         * in a 2D array.         */
        return result;
    }

    /** Unused. */
    void set_fading(boolean b) {
        fade = b;
    }

    /** Unused. */
    void set_inversion(boolean b) {
        inversion = b;
    }

    /** Set the motion blur via page up or page down. */
    void setMotionBlur(int k) {
        if (k < 0) {
            k = 0;
        } else if (k > 256) {
            k = 256;
        }
        motionblurp = k;
        motionblurq = 256 - k;
    }

    /** Color filter weight (responsiveness of the
     * buffer. Press left or right arrow.     */
    void setColorFilterWeight(int k) {
        if (k < 0) {
            k = 0;
        } else if (k > 256) {
            k = 256;
        }
        this.filterweight = k;
    }

    /** A few types of boundary conditions (a.k.a.
     * Julia set bailout conditions). */
    boolean RectangularBounds(RenderStateMachine m) {
        return m.f_x < W8 && m.f_y < H8 && m.f_x >= 0 && m.f_y >= 0;
    }

    /** The divergent bailout condition.     */
    boolean WithinLimitCircle(RenderStateMachine m) {
        float x = (float) m.f_x * overW7 - 1;
        float y = (float) m.f_y * overH7 - 1;
        return (x * x + y * y < 1);
    }

    /** The convergent bailout condition.     */
    boolean ConvergentBailout(RenderStateMachine m) {

        int i = m.m_i << 1;

        /** The following return statement should test
         * the divergent bailout condition with accurate 
         * complex points z. It works, but it draws a
         * slight outline of a semi-transparent circle
         * permanently covering the central portion
         * of the screen. I don't know why.
         * TODO examine the origin of the circle and
         * apply the conclusion to the convergent bailout.  
         * 
         * return (z_divergence[i] < 0.8);
         *          
         */
        /** The test result of convergence. Required
         * for drawing Newton and Nova fractals.         */
        return (z_convergence[i] > 0.1);

        /** It is possible to combine the bailout conditions
         * when drawing ordinary Julia set. The divergent 
         * bailout gives it it's outside (coloring), and the 
         * convergent bailout may add structure to the 
         * inside (in some formulas).
         *
         * return (z_divergence[i] < 0.8) && (z_convergence[i] > 0.1);         */
    }

    /** The divergent bailout condition with variable limit circle.     */
    boolean WithinElasticLimitCircle(RenderStateMachine m) {
        float x = (float) m.f_x * overW7 - 1;
        float y = (float) m.f_y * overH7 - 1;
        return x * x + y * y < this.boundRadius;
    }

    /** Image mode-related projection mask. */
    int projectionMask(RenderStateMachine m) {
        int grad = gradients[9][m.m_i];
        return ColorUtility.average(
                m.color, grad,
                0, 256 - grad);
    }
    Operator[] Renderers = {
        /** The renderer is the most active routine that is
         * using the RenderStateMachine structure of data
         * most heavily, by changing m_i, walking across
         * the map_buffer (or map) and setting the pixel. */
        new Operator("Primary Renderer") {

            void operate(RenderStateMachine m) {
                /** The fade-into period, e.g. when switching equations. */
                if (fade_1 > 0) {
                    int f1 = (int) (256 * fade_1);
                    int f2 = 256 - f1;
                    for (m.m_i = m.m_start; m.m_i < m.m_end; m.m_i += m.stride) {
                        int i = m.m_i << 1;
                        /** The signed bitwise right shift operator >> is the same as integer division by 2^n if both sides are positive.   */
                        set_pixel(m, (map_buffer[i] * f2 + map[i] * f1) >> 8, (map_buffer[i | 1] * f2 + map[i | 1] * f1) >> 8);
                    }
                    fade_1 -= .05f;
                    if (fade_1 <= 0) {
                        swap_map();
                    }
                } /** The conventional "putpixel" for the current buffer.
                 * It walks across the map[] buffer, where the integer
                 * representations of points f(z) scaled to screen are.                 */
                else {
                    for (m.m_i = m.m_start; m.m_i < m.m_end; m.m_i += m.stride) {
                        /** The signed bitwise left shift operator << means multiply with 2^n. i = 0, 2, 4, 6, 8, 10... M_LEN. */
                        int i = m.m_i << 1;
                        set_pixel(m, map[i], map[i | 1]);   // The bitwise i OR 1 = 1, 3, 5, 7, 9...
                    }
                }
            }

            /** The set_pixel is computing the active pixel 
             * coordinates, calling the boundary check,
             * performing gradient inversion and writing the
             * pixel to the screen buffer.            */
            void set_pixel(RenderStateMachine m, int imag, int real) {
                float x4 = real * rotation_constant_real;
                float x5 = imag * rotation_constant_imag;
                m.f_x = (int) (x4 + x5) + Cx;
                m.f_y = (int) (rotation_constant_coef * (real - imag) - x4 + x5) + Cy;
                /** Check the boundary condition for the current point z (or pixel f_x, f_y).
                 * Eventually, every function stemming from the boundary
                 * condition check will specify m.color. Hence, wrtiting to the
                 * RenderStateMachine can get out of hand. This line is
                 * absolutely required.                 */
                boundary_condition_operator.operate(m);
                /**All the functions called via the boundary_condition_operator
                 * interact with the following line when they finish and if they wrote
                 * to the color variable in the RenderStateMachine. Practically,
                 * the following line is used for the gradient inversion. Pressing J
                 * inverts the gradient_inversion from 0x0 to 0xFFFFFF. If we
                 * exclude this line, the gradient inversion will occur and the perpetual,
                 * persistent flicker (exchange of two colors) will not occur in the
                 * Julia basin. If we set m.color to a fixed value, the entire screen will
                 * have the same (dead) color. Cursors will be unchanged.
                 * The bitwise xor operator ^ is responsible for the interchanging
                 * contour colors around the Julia basin. m.color ^= 0x0 by
                 * default, and it XORs the rsm.color value that came from the
                 * gradient function (operator) or elsewhere. rsm.color can have
                 * a fixed value for a mostly blue, classical Julia set. By default
                 * rsm.color is a function of (m_i, fade_color) depending from the
                 * gradient function.
                 * Since the m.color is int long, and gradient_inversion = 0xFFFFFF =
                 * all ones in binary, the m.color ^= gradient_inversion will produce a
                 * straight declining line of values from 0xFFFFFF down by m.color. If the
                 * gradient_inversion = 0, then m.color is unchanged (inversion enabled).                  */
                m.color ^= gradient_inversion;
                /** "putpixel" (at location m_i, put m_color). Includes the motionblur
                 * consideration.   */
                buffer_buffer.setElem(m.m_i, m.color = ColorUtility.average(m.color, motionblurp,
                        buffer.output.buffer.getElem(m.m_i), motionblurq));
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
                                (map_buffer[i | 1] * f2 + map[i | 1] * f1)
                                >> 8);
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
                        m.color = gradient_inversion ^ sketch.get(real + Cx, imag + Cy), motionblurp,
                        buffer.output.buffer.getElem(m.m_i), motionblurq));
                display_buffer.setElem(m.m_i, projectionMask(m));
            }
        },
        new Operator("Primary Renderer 2") {

            void operate(RenderStateMachine m) {
                if (fade_1 > 0) {
                    int f1 = (int) (256 * fade_1);
                    int f2 = 256 - f1;
                    for (m.m_i = m.m_start; m.m_i < m.m_end; m.m_i += m.stride) {
                        int i = m.m_i << 1;
                        set_pixel(m, (map_buffer[i] * f2 + map[i] * f1) >> 8,
                                (map_buffer[i | 1] * f2 + map[i | 1] * f1)
                                >> 8);
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
                m.f_x = real + Cx;
                m.f_y = imag + Cy;
                boundary_condition_operator.operate(m);
                m.color ^= gradient_inversion;
                buffer_buffer.setElem(m.m_i, ColorUtility.average(m.color, motionblurp,
                        buffer.output.buffer.getElem(m.m_i), motionblurq));
                display_buffer.setElem(m.m_i, projectionMask(m));
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////
    /** An array of functions accessible via the
     * keyboard options by numbers from 0 to ... */
    ////////////////////////////////////////////////////////////////////////////
    /** The boundary conditions for the Julia set. 
     * They respond to key R, increment_boundary_condition, 
     * then set_boundary_condition.
     * (Preset default is 2.)     */
    Operator[] Boundary_conditions = {
        /** This is the number R = 0 in the preset file */
        new Operator("Rectangular Window as the limit circle") {

            void operate(RenderStateMachine rsm) {
                if (RectangularBounds(rsm)) {
                    rsm.color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    outside_coloring_operator.operate(rsm);
                }
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 1 in the preset file */
        new Operator("Limit Circle") {

            void operate(RenderStateMachine rsm) {
                if (WithinLimitCircle(rsm)) {
                    /** Within the Julia basin. Without this line, we cannot 
                     * see the Julia basin. There are not any contours either,
                     * just the reflections (IFS fractals) and the gradient applied to it.
                     * If we set this to a fixed color, there won't be any contours either. 
                     * If we combine the fixed color here and in the gradient function,
                     * the fixed gradient color will prevent the pullback copies and 
                     * the reflections to appear. The default form causes the natural
                     * flicker, liveliness of the inner most Julia basin (lake).                      */
                    rsm.color = buffer.output.get(rsm.f_x, rsm.f_y); // this color returns to the set_pixel routine
                } else {
                    /** Outside of the Julia basin. Without this line,
                     * we can see only the most inner Julia basin.
                     * The basin is flickering. There is a hint of the
                     * contours, but they are barely visible. There
                     * are not any reflections and the pullback
                     * does not have any effect.                     */
                    outside_coloring_operator.operate(rsm);
                }
                /** If I disable update_color, set the gradient
                 * function to put a fixed rsm.color each time,
                 * and disable the m.color in set_pixel, we get
                 * a lonely Julia set with many contours in
                 * a single color tone, with a non-flickering
                 * basin. Disabling or enabling the color
                 * filter does not make any further difference.                  */
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 2 in the preset file */
        new Operator("Elastic Limit Circle") {

            void operate(RenderStateMachine rsm) {
                if (WithinElasticLimitCircle(rsm)) {
                    rsm.color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    outside_coloring_operator.operate(rsm);
                }
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 3 in the preset file */
        new Operator("Horizontal Window") {

            void operate(RenderStateMachine rsm) {
                if (rsm.f_y > 0 && rsm.f_y < H8) {
                    rsm.color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    outside_coloring_operator.operate(rsm);
                }
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 4 in the preset file */
        new Operator("Vertical Window") {

            void operate(RenderStateMachine rsm) {
                if (rsm.f_x > 0 && rsm.f_x < W8) {
                    rsm.color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    outside_coloring_operator.operate(rsm);
                }
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 5 in the preset file */
        new Operator("Inverse Oval Window") {

            void operate(RenderStateMachine rsm) {
                if (WithinLimitCircle(rsm)) {
                    outside_coloring_operator.operate(rsm);
                } else {
                    rsm.color = buffer.output.get(rsm.f_x, rsm.f_y);
                }
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 6 in the preset file */
        new Operator("No Window") {

            void operate(RenderStateMachine rsm) {
                outside_coloring_operator.operate(rsm);
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 8 in the preset file.     */
        new Operator("Framed Window") {

            void operate(RenderStateMachine rsm) {
                rsm.old_color = buffer.output.get(rsm.f_x, rsm.f_y);
                if (((rsm.m_i + (W << 2)) % M_LEN) <= (W << 3) || ((rsm.m_i + 4) % W)
                        <= 8) {
                    rsm.color = fade_color;
                } else {
                    rsm.color = rsm.old_color;
                }
                gradient_operator.operate(rsm);
                color_filter_operator.operate(rsm);
            }
        },
        /** This is the number R = 7 in the preset file */
        new Operator("Convergent bailout condition") {

            void operate(RenderStateMachine rsm) {
                /** For some reason, I changed places
                 * of the instructions after if and else to
                 * test the convergent bailout and it still
                 * worked. I restored them since.  */
                if (ConvergentBailout(rsm)) {
                    rsm.color = buffer.output.get(rsm.f_x, rsm.f_y);
                } else {
                    outside_coloring_operator.operate(rsm);
                }
                color_filter_operator.operate(rsm);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////
    /** These functions determine what to do when
     * the mapped point is outside of the limit circle
     * (oval, square, rectagon...), depending from
     * the boundary condition. Also known as the
     * offscreen coloring procedures. We set them by
     * using set_outside_coloring().
     * The presets load the choice into the variable
     * edge_extend_mode. (Preset default is 2.)
     * Press E to change option.   */
    Operator[] Outside_coloring = {
        /** This is the number E = 0 in the preset file */
        new Operator("Color Fill") {

            void operate(RenderStateMachine rsm) {
                rsm.color = fade_color;
                gradient_operator.operate(rsm);
            }
        },
        /** This is the number E = 1 in the preset file */
        new Operator("Edge Extend") {

            void operate(RenderStateMachine rsm) {
                rsm.color = buffer.output.get(
                        (rsm.f_x < 0) ? 0 : (rsm.f_x > W8) ? W8 : rsm.f_x,
                        (rsm.f_y < 0) ? 0 : (rsm.f_y > H8) ? H8 : rsm.f_y);
                gradient_operator.operate(rsm);
            }
        },
        /** This is the number E = 2 in the preset file */
        new Operator("Reflect") {

            void operate(RenderStateMachine rsm) {
                /** Outside of the Julia basin. Without this line, there is 
                 * the inner most basin, barely visible contours (helped
                 * by the gradient other than No Gradient), but not the
                 * reflections, or the copies of Julia set through pullback.
                 * If we set this to a fixed color value, and choose No
                 * Gradient, we obtain black and white contours
                 * (with other gradients, colored in a complex manner).                 */
                rsm.color = buffer.output.get(rsm.f_x, rsm.f_y); // this color returns to the set_pixel routine
                /** Call the gradient. Without this line, there is not any
                 * gradient function (e.g. gradient cursor is off.) All
                 * the fractal shapes are barely there, mostly gray, with
                 * flickering in-between.                  */
                gradient_operator.operate(rsm);
            }
        },
        /** This is the number E = 3 in the preset file. 
         * Here "sketch" image is used to color the outside.         */
        new Operator("Sketch") {

            void operate(RenderStateMachine rsm) {
                rsm.color = buffer.image.get(rsm.f_x, rsm.f_y);
                gradient_operator.operate(rsm);
            }
        },
        /** This is the number E = 4 in the preset file */
        new Operator("SketchII") {

            void operate(RenderStateMachine rsm) {
                float x = (float) rsm.f_x * overW7 - 1;
                float y = (float) rsm.f_y * overH7 - 1;
                float R = x * x + y * y;
                int K = max(0, min(256, (int) ((2 - R) * 256)));
                rsm.color = ColorUtility.average(
                        buffer.output.get(rsm.f_x, rsm.f_y), K,
                        buffer.image.get(rsm.f_x, rsm.f_y), 256 - K);
                gradient_operator.operate(rsm);
            }
        },
        /** This is the number E = 5 in the preset file */
        new Operator("Fuzzy") {

            void operate(RenderStateMachine rsm) {
                int c22 = buffer.output.get(rsm.f_x, rsm.f_y);
                int c12 = buffer.output.get(rsm.f_x - 1, rsm.f_y);
                int c21 = buffer.output.get(rsm.f_x, rsm.f_y - 1);
                int c23 = buffer.output.get(rsm.f_x, rsm.f_y + 1);
                int c32 = buffer.output.get(rsm.f_x + 1, rsm.f_y);
                rsm.color = convolve_cross(c12, c21, c22, c23, c32);
                gradient_operator.operate(rsm);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////
    /** These functions determine which gradient to apply.
     * (Preset default is 2.)
     * Press G to change option.      */
    Operator[] Gradients = {
        new Operator("No Gradient") {

            void operate(RenderStateMachine rsm) {
            }
        },
        new Operator("Simple Gradient") {

            void operate(RenderStateMachine rsm) {
                /** The gradient selection depends from
                 * the index of the gradient inputed as m_i
                 * to the radial_gradients[].
                 * int grad = 255 - anything larger than 0 and
                 * smaller than 255 in function of rsm.m_i. 
                 * The slope and the offset come from the
                 * gradient cursor. The fade color modes
                 * depend from update_color. 
                 * If the gradient is ugly or poorly designed,
                 * the colors may come from positioning the
                 * cursor at the particular point from where it 
                 * will be spread out due to the particular 
                 * mapping.                 */
                int grad = 0xFF - (char) min(0xFF, max(0, radial_gradient[rsm.m_i]
                        * gradient_slope_constant
                        - gradient_offset));
                /** Without the following line, the entire fractal is there,
                 * but it is all mostly gray and hard to distinguish at all.
                 * (In other words, in is not quite there.) Only the most
                 * inner Julia basin is alive with color and flicker.
                 * By setting rsm.color = 0..., we color the contours
                 * with two specific colors. The one we set is the first,
                 * and the second one comes from the set_pixel():
                 * m.color ^= gradient_inversion. By default, that is
                 * m.color ^= 0x0. (With inversion, 0xFFFFFF.)
                 * The basin remains alive, but there are not any
                 * reflections or pullback copies of Julia set. The
                 * fade modes do not function. The basin will flicker
                 * in colors that depend from how we play with the
                 * Julia set. It may insert the color from the cursors,
                 * the help screen or the contours. All of this does
                 * not depend on update_color(). Further,
                 * if I disable the line m.color ^= gradient_inversion
                 * in set_pixel, we obtain the classical Julia set with
                 * contours in consecutive shades of one color,
                 * ending with the non-flickering basin.
                 * However, with the convergent condition, we CAN
                 * see the pullback and the reflections (IFS fractals).                 */
                rsm.color = ColorUtility.average(rsm.color, 0xFF ^ grad, fade_color, grad); // this color returns to the set_pixel
            }
        },
        new Operator("Accented Gradient") {

            void operate(RenderStateMachine rsm) {
                int grad = 0xFF - (char) min(0xFF, max(0, radial_gradient[rsm.m_i]
                        * gradient_slope_constant
                        - gradient_offset));
                rsm.color =
                        ColorUtility.average(rsm.color, 0xFF ^ grad,
                        ColorUtility.average(accent_color, 0xFF
                        ^ grad ^ gradient_switch, fade_color, grad ^ gradient_switch), grad);
            }
        }
    };
    /** Color filters... They improve the contrast.
     * These respond to keyboard option X.
     * (Preset default is 0.)     */
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
                c3 = average(c3, filter_param1, c2, 256 - filter_param1);
                rsm.color = average(c1, filter_param2, c3, 256 - filter_param2);
            }
        },
        new Operator("mush !") {

            void operate(RenderStateMachine rsm) {
                int c1 = rsm.color;
                int c2 = contrast(rsm.color);
                int c3 = mush(rsm.color);
                c3 = average(c3, filter_param1, c2, 256 - filter_param1);
                rsm.color = average(c1, filter_param2, c3, 256 - filter_param2);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////
    /** These functions set the fade-to color for the 
     * mapping as a constant for other interchangable
     * functions to use. They respond to key F.
     * (Preset default is 3.)     */
    Operator[] FadeColor = {
        new Operator("Black") {

            /** Black fade-out color allows everything to
             * function fine. There are all the fractal shapes
             * and all the colors and the flicker.             */
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
        new Operator("Not Middle Pixel Hue") {

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
        },
        new Operator("Hue Rotate") {

            int HUE = 0;

            void operate(RenderStateMachine rsm) {
                fade_color = Color.HSBtoRGB((float) HUE * .01f, 1.f, 1.f);
                HUE++;
            }
        }
    };
    /** Color permutations.
     * Unused.     */
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
                rsm.color = 0xffff & (rsm.color >> 8) | (0xff0000 & (rsm.color << 8));
            }
        },
        new Operator("ok") {

            void operate(RenderStateMachine rsm) {
                rsm.color = 0xffff & (rsm.color >> 8) | (0xff0000 & (rsm.color << 8));
                rsm.color = 0xffff & (rsm.color >> 8) | (0xff0000 & (rsm.color << 8));
            }
        },
        new Operator("just weird") {

            void operate(RenderStateMachine rsm) {
                int displaced = 0xffff & (rsm.color >> 8) | (0xff0000 & (rsm.color << 8));
                rsm.color = average(displaced, rsm.color);
            }
        }
    };
}
