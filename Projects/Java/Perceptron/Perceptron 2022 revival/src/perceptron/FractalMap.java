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

import math.MathToken;
import math.complex;
import math.Equation;
import math.ComplexVarList;
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import static util.ColorUtil.average;
import static util.ColorUtil.contrast;
import static util.ColorUtil.RGB_contrast;
import static util.ColorUtil.mush;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static perceptron.Misc.clip;
import static perceptron.Misc.wrap;

/**
 * @author Michael Everett Rule
 */
public class FractalMap {
       
    DoubleBuffer       buf;
    ArrayList<Mapping> mappings;
    Perceptron         percept;
    
    boolean            active  = true;
    boolean            running = false;
    
    public boolean isActive() {return active;}
    public void    setActive(boolean active) {this.active = active;}
    
    DataBuffer 
        output_buffer,  // current image on the screen
        buffer_buffer,  // image buffer after mapping
        sketch_buffer,  // external, loaded image
        display_buffer; // cursors, circles, dots

    // Control interpretation of translation, rotation cursors
    int ortho_type = 0; // Incremented by pressing comma
    int polar_type = 0; // Incremented by pressing period
    public static final int 
        // Constants for identifying the translation modes
        TRANSLATION_NORMAL   = 0,
        TRANSLATION_VELOCITY = 1,
        TRANSLATION_LOCKED   = 2,
        // Constants for identifying the rotation modes
        ROTATION_NORMAL      = 0,
        ROTATION_VELOCITY    = 1,
        ROTATION_LOCKED      = 2;
    
    // Mistakes were made in this design
    final int W, H, W2, H2, W7, H7, W8, H8, W9, H9, MEND, MLEN, MMID, HALFW, 
        HALFH, TWOW, TWOMLEN, TWOH, WMONE, HMONE, MONE, MMROW, WPONE, BOUNDS,
        MAXC=65535;
    final float overW7, overH7, z_to_map_W_scalar, 
        z_to_map_H_scalar, z_to_W_scalar, z_to_H_scalar;
    
    /** The upper left, the lower right, and the size of the rectangle in the
     *   complex plane. These are actually re-initialized so these values don't matter.     */
    public static final complex size = new complex(6f, 6f);
    public static final complex upper_left = new complex();
    public static final complex lower_right = new complex();
        

    

    
    /** A few more constants... */
    static final double PI_OVER_TWO = Math.PI * .5;
    float d_r = 0, d_i = 0;
    float rotation_drift = .1f;
    
    /** Create new FractalMap
     * @param b
     * @param maps
     * @param parent */
    public FractalMap(DoubleBuffer b, ArrayList<Mapping> maps, Perceptron parent) {

        percept = parent;

        vars.set(18, new complex(size));
        vars.set(22, new complex(size.real));
        vars.set(7, new complex(size.imag));

        mappings = maps;
        if (mappings == null) 
            mappings = new ArrayList<>();

        // FractalMap loads the buffer.
        buf = b;

        // Screen width and height, and related constants.
        // We convert the 2D screen to a 1D array length M_LEN
        W         = b.out.img.getWidth();
        H         = b.out.img.getHeight();
        W2        = W << 2;
        H2        = H << 2;
        W7        = W << 7;
        H7        = H << 7;
        overW7    = 1.f / W7;
        overH7    = 1.f / H7;
        W8        = W << 8;
        H8        = H << 8;
        W9        = W << 9;
        H9        = H << 9;
        WPONE   = W + 1;
        WMONE   = W - 1;
        HMONE   = H - 1;
        MLEN     = W * H;
        TWOMLEN = MLEN << 1;
        MEND     = MLEN - 1;
        MONE     = MEND - 1;
        MMROW   = MONE - W;
        HALFW    = W / 2;
        HALFH    = H / 2;
        TWOW     = W << 1;
        TWOH     = H << 1;
        MMID     = HALFW + W * HALFH;
        BOUNDS    = min(W7, H7);
        if (W >= H) {
            size.real = 2.4f * W / H;
            size.imag = 2.4f;
        } else {
            size.real = 2.4f;
            size.imag = 2.4f * H / W;
        }
        lower_right.real = size.real * .5f;
        lower_right.imag = size.imag * .5f;
        upper_left.real  = -lower_right.real;
        upper_left.imag  = -lower_right.imag;
        z_to_map_W_scalar = size.real / W;
        z_to_map_H_scalar = size.imag / H;
        z_to_W_scalar =
                z_to_H_scalar = (W / size.real + H / size.imag) * .5f;

        initialize_functions();
        // Prepare the buffers for the mapping f(z). 
        initMapLookup();
        // Prepare the mapping f(z). 
        initMapEquation();
    }

    /** These respond to keyboard options `,` and `.`.
     */
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
    final void initialize_functions() {

        bounds_i  = wrap(bounds_i, boundary_conditions.length);
        outside_i = wrap(outside_i, outside_ops.length);
        fade_i    = wrap(fade_i, fade_colors.length);
        grad_mode = wrap(grad_mode, gradient_modes.length);
        color_i   = wrap(color_i, color_filters.length);

        bounds_op   = boundary_conditions[bounds_i];
        outside_op  = outside_ops[outside_i];
        fade_op     = fade_colors[fade_i];
        gradient_op = gradient_modes[grad_mode];
        color_op    = color_filters[color_i];

        /** Select the fractal map f(z), a.k.a. the "equation"
         *  from the list of equations in the settings file.
         * Later, switch them by pressing Q or W.          */
        if (!mappings.isEmpty()) {
            equation_i %= mappings.size();
            mapping = mappings.get(equation_i);
        }

        /** Call the initialization of gradients.
         * gradients is a large 2D array of all the gradients
         * (gradient shapes). radial_gradient is a 1D array 
         * that represents one of the 10 gradients (gradient 
         * shapes) that are M_LEN deep.          */
        gradients = initGradients();

        gradient = gradients[grad_index];
    }

    /** The gradient inversion. Press J. 
     * The gradient inverts when gradient_inversion = 0.
     * When it is 16777215, the gradient is normal.
     * In hexadecimal, that is 0x0 and 0xFFFFFF. 
     * This sucks inward all the contour colors and
     * leaves only the recursive conformal mapping
     * bending the help screen and the cursors.     */
    public void toggleInversion() {
        color_mask = color_mask>0? 0xff : 0;
    }
    
    /** Motion blur. Press UP or DOWN arrows. */
    int motionblurp = 255;
    int motionblurq = 0;
    void setMotionBlur(int k) {
        k = Misc.clip(k,1,255);
        motionblurp = k;
        motionblurq = 256 - k;
    }

    /** Spatial blur weight. */
    int filterweight = 0;
    void setColorFilterWeight(int k) {
        this.filterweight = Misc.clip(k,-256,256);
    }


    //🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛
    // Apply current map to screen raster, storing result in output raster. 
    void operate() {
        
        //buffer.flip();
        
        if (!active) {
            buf.out.g.drawImage(buf.buf.img, 0, 0, null);
            return;
        }
        output_buffer  = buf.out.buf;
        buffer_buffer  = buf.buf.buf;
        display_buffer = buf.dsp.buf;
        if (buf.img != null)
            sketch_buffer = buf.img.buf;
        
        /** Update the default color of the pixel.
         * New RenderStateMachine is created locally.
         * This is required for the fade color function to
         * work.         */
        updateColor();

        //gradient_inversion ^= ~0;
        final RenderStateMachine m = new RenderStateMachine();

        // This option is contolled by pressing the COMMA key
        // It controls how the fractal map treats the 2D point offset.
        // Mode 0: (normal) offset is converted to 8-bit fixed point. 
        // Mode 1: Velocity mode. 
        // Mode 2: Locked: the offset is clamped to zero. 
        switch (ortho_type) {
            case 0:
                m.Cx = (int) (.5f + 0x100 * ((norm_c[0]))) + W7;
                m.Cy = (int) (.5f + 0x100 * ((norm_c[1]))) + H7;
                break;
            case 1:
                m.Cx = (int) (.5f + 0x100 * ((d_r = (abs(d_r) > MAXC) ? 0 : d_r + .1f * norm_c[0]))) + W7;
                m.Cy = (int) (.5f + 0x100 * ((d_i = (abs(d_i) > MAXC) ? 0 : d_i + .1f * norm_c[1]))) + H7;
                break;
            case 2:
                m.Cx = W7;
                m.Cy = H7;
                break;
        }
        
        // This option is controlled by pressing the PERIOD key.
        // It controls how the fractal map interprets the rotate/scale cursor.
        // Mode 0: Blue cursor controls scale and rotation
        // Mode 1: Blue cursor controls velocity
        // Mode 2: Scale and rotation are locked at 1, pi/2, respectively. 
        complex r = new complex();
        switch (polar_type) {
            case 0:
                r = rotation;
                break;
            case 1:
                rotation_drift += complex.arg(rotation) * 0.01f;
                r = complex.fromPolar(
                        complex.mod(rotation), 
                        rotation_drift);
                break;
            case 2:
                r = complex.fromPolar(1.0f, (float) (PI_OVER_TWO));
                break;
        }
        m.rotation_real = r.real;
        m.rotation_imag = r.imag;
        m.rotation_coef = m.rotation_real + m.rotation_imag;

        // The render state machine is more or less just a for-loop over 
        // pixels. By passing this state to various "operators" we compose
        // a modular rendering loop to add, remove, and change effects.
        render_op.operate(m);
        
        // Exchange output and "buffer" operators.
        // Perceptron will
        buf.flip();
    }
    //🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Updates the "fade" color used to tint/accent fractal ////////////////////
    boolean dampen_colors = true;
    public void toggleFadeColorSmoothing() {
        dampen_colors = !dampen_colors;
    }
    public void updateColor() {
        RenderStateMachine m = new RenderStateMachine();
        m.oldcolor = fade_color; 
        m.color = fade_color;
        fade_op.operate(m);
        if (dampen_colors)
            fade_color = average(fade_color, 56, m.color, 200);
        // why is this line here? 
        anti_fade_weight = 256 - fade_weight;
    }
    // The color mixing is adjustable but this isn't used presently.
    int fade_weight = 128, anti_fade_weight = 128;
    public void incrementFadeWeight(int n) {
        fade_weight = wrap(fade_weight + n, 256);
    }
    /** A color generated from the an external source, like audio input */
    // This isn't used presently
    //int input_color = 0x00000000;
    //public void setInputColor(int col) {
    //    input_color = col;
    //}
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // The main rendering operator /////////////////////////////////////////////
    private final Operator render_op = new Operator("Primary Renderer") {
        @Override
        void operate(RenderStateMachine m) {
            // To transition, interpolate-fade between two maps
            if (transition_map > 0) {
                int f1 = (int) (256 * transition_map);
                int f2 = 256 - f1;
                for (m.i=m.start; m.i<m.end; m.i+=m.stride) {
                    int i = m.i<<1;
                    setPixel(m, 
                        (mapBuffer[i  ]*f2 + map[i  ]*f1) >> 8, 
                        (mapBuffer[i|1]*f2 + map[i|1]*f1) >> 8);
                }
                // Swap to new map once done
                transition_map -= .05f;
                if (transition_map <= 0) {
                    int[] temp = map;
                    map = mapBuffer;
                    mapBuffer = temp;
                }
            } 
            // If not transitioning, use the current map
            else for (m.i = m.start; m.i<m.end; m.i+=m.stride) {
                int i = m.i<<1;
                setPixel(m, map[i], map[i|1]);
            }
        }
        void setPixel(RenderStateMachine m, int imag, int real) {
            // Apply rotation to map using Gauss's method of multiplying
            // complex numbers.
            // This conjugates the output for some reason. 
            float x4 = real * m.rotation_real;
            float x5 = imag * m.rotation_imag;
            // Set the source-pixel texture coordinates.
            m.fx = (int)(x4+x5)+m.Cx;
            m.fy = (int)(m.rotation_coef*(real-imag)-x4+x5)+m.Cy;
            // Store source pixel color in m.color, if pixel in-bounds.
            // Get a color from somewhere else otherwise.
            if (bounds_op.test(m) != bounds_invert)
                m.color = buf.out.grab.get(m.fx, m.fy);
            else 
                outside_op.operate(m);
            // Apply the gradient mask
            gradient_op.operate(m);
            // Update the gradient color
            color_op.operate(m);
            // Set, applying inversion and motion blur if applicable.               
            setWithMotionBlur(m);
        }
        void setWithMotionBlur(RenderStateMachine m) {
            m.color ^= color_mask;  
            // Set the output buffer pixel, applying motion-blur.
            // We need to handle background objects as a special case
            // We don't want to motion-blur with the over-drawn objects.
            // We will assume the main perceptron code has saved us a
            // copy of what we need in the buffer_buffer in this case. 
            if (motionblurp<255) {
                buffer_buffer.setElem(m.i, 
                    average(m.color, 
                        motionblurp,
                        (percept.objects_on_top
                                ? buf.out 
                                : buf.buf).buf.getElem(m.i), 
                        motionblurq));
            } else {
                buffer_buffer.setElem(m.i, m.color);
            }
            // If you want to re-enable this remember to set m.color to the
            // color used above to set the output buffer before calling.
            //display_buffer.setElem(m.i,projectionMask(m));
    }};
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Fractal map control /////////////////////////////////////////////////////
    int       equation_i = 0; // Index into list of built-in maps
    Mapping   mapping;        // The fractal mapping f(z)
    int[]     map;            // Lookup table for current map
    complex   constant;       // The "+c" in a Julia set iteration 
    complex   rotation;       // Rotation/scale of the complex map
    float[]   norm_c;         // Offset constant in screen coordinates
    boolean[] z_converged;    // "converged" points LUT for Newton fractals
    int[]     mapBuffer;      // Additional map LUT to use when transitioning
    float     transition_map; // Frame counter used to transition maps smoothly
    public complex getConstant() {
        return new complex(constant);
    }
    public void setConstant(float real, float imag) {
        constant.real=real; constant.imag=imag;
    }
    public void setNormalizedConstant(float x, float y) {
        constant.real = (W - x) / z_to_W_scalar + upper_left.real;
        constant.imag = (H - y) / z_to_H_scalar + upper_left.imag;
        if (norm_c   == null) norm_c   = new float[]{0, 0};
        if (constant == null) constant = new complex(0, 0);
        norm_c[0] = (float) (constant.real * z_to_W_scalar);
        norm_c[1] = (float) (constant.imag * z_to_H_scalar);
    }
    public void setNormalizedRotation(float x, float y) {
        rotation.real = ((W - x) / z_to_W_scalar + upper_left.real);
        rotation.imag = ((H - y) / z_to_H_scalar + upper_left.imag);
        float theta = complex.arg(rotation);
        float r = complex.mod(rotation) * 2;
        // this 1/r is used for pullback
        rotation = complex.fromPolar(1 / r, theta);    
        // Adjust radius for elastic circle boundary conditions
        bound_radius = 1 / (r * r);
    }
    public void nextMap(int n) {
        setMap(equation_i + n);
    }
    public void setMap(int index) {
        loadEquation(mappings.get(equation_i = wrap(index, mappings.size())));
    }
    public void loadEquation(Mapping map) {
        mapping = map;
        computeLookup();
    }
    public void setMap(final String s) {
        try {
            mapping = makeMap(s);
            computeLookup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    final void initMapLookup() {
        map         = new int[TWOMLEN];
        mapBuffer   = new int[TWOMLEN];
        z_converged = new boolean[TWOMLEN];
        computeLookup();
    }
    final void initMapEquation() {
        constant = new complex();
        rotation = new complex();
        norm_c = new float[]{0, 0};
        setConstant(0, 0);
    }
    final static ComplexVarList vars = ComplexVarList.standard();
    public static Mapping makeMapStatic(final String s) {
        final Equation e = MathToken.toEquation(s);
        return new Mapping() {
            @Override
            public complex operate(complex z) {vars.set(25, z); return e.eval(vars);}
            @Override
            public String toString() {return s;}
        };
    }
    public Mapping makeMap(final String s) {
        final Equation e = MathToken.toEquation(s);
        vars.set(18,size);
        vars.set(22,new complex(size.real));
        vars.set(7, new complex(size.imag));
        return new Mapping() {
            @Override
            public complex operate(complex z) {vars.set(25, z); return e.eval(vars);}
            @Override
            public String toString() {return s;}
        };
    }
    private void computeLookup() {
        if (!active || transition_map>0) return;
        try {
            transition_map = 1.f;
            complex z = new complex();
            complex Z;
            int i = 0;
            int k = 0;
            float upper_left_real = upper_left.real;
            float upper_left_imag = upper_left.imag;
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    z.real = x*z_to_map_W_scalar + upper_left_real;
                    z.imag = y*z_to_map_H_scalar + upper_left_imag;
                    Z = mapping.operate(z); 
                    mapBuffer[i++] = (int) (0x100 * (z_to_W_scalar
                            * (Z.real - upper_left_real))) - W7;
                    mapBuffer[i++] = (int) (0x100 * (z_to_H_scalar
                            * (Z.imag - upper_left_imag))) - H7;
                    //z_converged[k++] = Z.minus(z).rSquared() > 0.1;
                    z_converged[k++] = (Z.real - z.real) * (Z.real - z.real) 
                            + (Z.imag - z.imag) * (Z.imag - z.imag) > 0.1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
        
    ////////////////////////////////////////////////////////////////////////////
    // The boundary conditions for the Julia set. "R" //////////////////////////
    BoundsTest bounds_op;
    int bounds_i  = 0; // 0:screen 1:circle 2:elastic 3:horizon 4:Newton 5:none
    public void nextBounds(int n) {
        setBounds(bounds_i + n);
    }
    public void setBounds(int index) {
        if (!active) return;
        bounds_op = boundary_conditions[bounds_i = wrap(index, boundary_conditions.length)];
    }
    public boolean bounds_invert = false; // Invert boundary condition?
    double bound_radius = 1;      // adjust radius with map scale (mode 2)
    private abstract class BoundsTest {
        public final String name;
        public BoundsTest(String name) {this.name = name;}
        public abstract boolean test(RenderStateMachine m);
    }
    private final BoundsTest[] boundary_conditions = {
        new BoundsTest("Screen Edge") {
            @Override
            public boolean test(RenderStateMachine m) {
                return m.fx < W8 && m.fy < H8 && m.fx >= 0 && m.fy >= 0;
            }},
        new BoundsTest("Limit Circle") {
            @Override
            public boolean test(RenderStateMachine m) {
                float x = (float) m.fx * overW7 - 1;
                float y = (float) m.fy * overH7 - 1;
                return x*x + y*y < 1;
            }},
        new BoundsTest("Horizontal Window") {
            @Override
            public boolean test(RenderStateMachine m) {
                return m.fy > 0 && m.fy < H8;
            }},
        new BoundsTest("Elastic Circle") {
            // Adjustable limit circle
            @Override
            public boolean test(RenderStateMachine m) {
                float x = (float) m.fx * overW7 - 1;
                float y = (float) m.fy * overH7 - 1;
                return x*x + y*y < bound_radius;
            }},
        new BoundsTest("Newton Convergence") {
            @Override
            public boolean test(RenderStateMachine m) {
                // Convergence test for Newton and nova fractals
                return z_converged[m.i];
                // One can combine the bailout conditions when drawing ordinary
                // Julia set. The divergent bailout gives it it's outside 
                // coloring, and the convergent bailout may add structure to the 
                // inside in some formulas.
                // return (z_divergence[i] < 0.8) && (z_convergence[i] > 0.1);
            }},
        new BoundsTest("No Window") {
            @Override
            public boolean test(RenderStateMachine m) {return false;}}};
    
    

    ////////////////////////////////////////////////////////////////////////////
    // Describe what to do when a pixel is out of bounds ///////////////////////
    Operator outside_op;
    int      outside_i = 0; // 0:color 1:edge  2:repeat  3:image  4:fade
    public void nextOutsideColoring(int n) {
        setOutsideColoring(outside_i + n);
    }
    public void setOutsideColoring(int index) {
        if (!active) return;
        outside_op = outside_ops[outside_i = wrap(index, outside_ops.length)];
    }
    private final Operator[] outside_ops = {
        new Operator("Color Fill") {
            @Override
            void operate(RenderStateMachine m) {
                m.color = fade_color;
            }},
        new Operator("Edge Extend") {
            @Override
            void operate(RenderStateMachine m) {
                m.color = buf.out.grab.get(
                    (m.fx < 0) ? 0 : (m.fx > W8) ? W8 : m.fx,
                    (m.fy < 0) ? 0 : (m.fy > H8) ? H8 : m.fy);
            }
        },
        new Operator("Repeat") {
            @Override
            void operate(RenderStateMachine m) {
                m.color = buf.out.grab.get(m.fx, m.fy);
            }},
        new Operator("Image") {
            @Override
            void operate(RenderStateMachine m) {
                m.color = buf.img.grab.get(m.fx, m.fy);
            }},
        new Operator("Image Fade") {
            @Override
            void operate(RenderStateMachine m) {
                float x = (float) m.fx * overW7 - 1;
                float y = (float) m.fy * overH7 - 1;
                float R = x * x + y * y;
                int K = max(0, min(256, (int) ((2 - R) * 256)));
                m.color = average(buf.out.grab.get(m.fx, m.fy), K, buf.img.grab.get(m.fx, m.fy), 256 - K);
            }}};
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Color filters applied per-pixel during mapping operation ////////////////
    Operator color_op;
    int color_i = 0; // Which color filters to use
    /** These are controlled by a cursor but I don't remember what they do.
     *  I think we should replace this with an up/down control. 
     */
    int filter_param1 = 128;
    int filter_param2 = 128;
    public void setContrastParameters(int x, int y) {
        this.filter_param1 = clip(x,0,0xff);
        this.filter_param2 = clip(y,0,0xff);
    }
    public void nextColorFilter(int amount) {
        setColorFilter(color_i + amount);
    }
    public void setColorFilter(int index) {
        if (!active) return;
        color_op = color_filters[color_i = wrap(index, color_filters.length)];
    }
    private final Operator[] color_filters = {
        new Operator("None") {
            @Override
            void operate(RenderStateMachine m) {
            }},
        new Operator("RGB") {
            @Override
            void operate(RenderStateMachine m) {
                int c1 = m.color;
                int c2 = RGB_contrast(m.color);
                int c3 = contrast(m.color);
                c3 = average(c3, filter_param1, c2, 256 - filter_param1);
                m.color = average(c1, filter_param2, c3, 256 - filter_param2);
            }},
        new Operator("mush") {
            @Override
            void operate(RenderStateMachine m) {
                int c1 = m.color;
                int c2 = contrast(m.color);
                int c3 = mush(m.color);
                c3 = average(c3, filter_param1, c2, 256 - filter_param1);
                m.color = average(c1, filter_param2, c3, 256 - filter_param2);
            }}};
    
    

    ////////////////////////////////////////////////////////////////////////////
    // Fade Color: used to add dynamic color accents ///////////////////////////
    Operator fade_op;   // Current fade operator
    int fade_i;         // Index of current color fade operators
    int fade_color = 0; // Fade color to provide to renderer
    private final Operator[] fade_colors = {
        new Operator("Black") {
            @Override
            void operate(RenderStateMachine m) {
                fade_color = 0x00000000;
            }},
        new Operator("White") {
            @Override
            void operate(RenderStateMachine m) {
                fade_color = 0x00FFFFFF;
            }},
        new Operator("Middle Pixel Hue") {
            @Override
            void operate(RenderStateMachine m) {
                int temp_color = buffer_buffer.getElem(MMID);
                float[] HSV = java.awt.Color.RGBtoHSB(
                        (temp_color >> 16) & 0x000000FF,
                        (temp_color >> 8) & 0x000000FF,
                        (temp_color) & 0x000000FF,
                        new float[]{0, 0, 0});
                fade_color = java.awt.Color.HSBtoRGB(HSV[0], 1.f, 1.f);
            }},
        new Operator("Not Middle Pixel Hue") {
            @Override
            void operate(RenderStateMachine m) {
                fade_color = ~buffer_buffer.getElem(MMID);
            }},
        new Operator("Middle Pixel Hue Rotate") {
            float hue = 0;
            @Override
            void operate(RenderStateMachine m) {
                int temp_color = buffer_buffer.getElem(MMID);
                float[] HSV = java.awt.Color.RGBtoHSB(
                        (temp_color >> 16) & 0xFF,
                        (temp_color >> 8) & 0xFF,
                        (temp_color) & 0xFF,
                        new float[]{0, 0, 0});
                HSV[0] += .2f;
                fade_color = java.awt.Color.HSBtoRGB(HSV[0], 1.f, 1.f);
            }},
        new Operator("Hue Rotate") {
            int HUE = 0;
            @Override
            void operate(RenderStateMachine m) {
                fade_color = Color.HSBtoRGB((float) HUE * .01f, 1.f, 1.f);
                HUE++;
            }}};
    public void setFader(int index) {
        fade_op = fade_colors[fade_i = wrap(index, fade_colors.length)];
    }
    public void nextFader(int n) {
        setFader(fade_i + n);
    }

    
    
    ////////////////////////////////////////////////////////////////////////////
    // These are used to manipulate the accent color I think? //////////////////
    private final Operator[] ColorPermutations = {
        new Operator("None") {
            @Override
            void operate(RenderStateMachine m) {}},
        new Operator("invert") {
            @Override
            void operate(RenderStateMachine m) {
                m.color ^= 0xffffff;
            }},
        new Operator("permute") {
            @Override
            void operate(RenderStateMachine m) {
                m.color = 0xffff & (m.color >> 8) | (0xff0000 & (m.color << 8));
            }},
        new Operator("just weird") {
            @Override
            void operate(RenderStateMachine m) {
                int q = 0xffff & (m.color >> 8) | (0xff0000 & (m.color << 8));
                m.color = average(q, m.color);
            }}
    };
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Gradient ////////////////////////////////////////////////////////////////
    Operator gradient_op;
    int[] gradient_accents = new int[]{0x0, 0xFFFFFF}; // Accent colors (mode 2)
    char[][] gradients;          // All gradient tables; see initGradients()
    char[]   gradient;           // Active gradient lookup table
    int      grad_accent_i = 0;  // Accent color if in gradient mode 2
    float    grad_slope    = 1f; // Cursor-controlled gradient rate
    float    grad_offset   = 0f; // Cursor-controlled gradient size
    int      color_mask    = 0;  // Flip gradient transparency weight
    int      grad_switch   = 0;  // Flip color order if accented; "K"
    int      grad_accent   = 0;  // Usually accent_colors[accent_color_i]
    int      grad_index    = 0;  // In-use gradient lookup table
    int      grad_mode     = 0;  // Modes; 0: off 1: one-color 2: two-color
    public void setGradientParam(float slope, float offset) {
        grad_slope = slope;
        grad_offset = offset;
    }
    public void setGradientShape(int n) {
        gradient = gradients[grad_index = wrap(n, gradients.length)];
    }
    public void nextGradientShape(int n) {
        setGradientShape(grad_index + n);
    }
    public void setAccent(int color_accent) {
        grad_accent = gradient_accents[grad_accent_i=wrap(color_accent, gradient_accents.length)];
    }
    public void nextAccent(int n) {
        setAccent(grad_accent_i + n);
    }
    public void setGradient(int index) {
        if (!active) return;
        gradient_op = gradient_modes[grad_mode = wrap(index, gradient_modes.length)];
    }
    public void nextGradient(int amount) {
        setGradient(grad_mode + amount);
    }
    char[][] initGradients() {
        char[][] result = new char[10][MLEN];
        int index = 0;
        for (int y = -HALFH; y < HALFH; y++) {
            for (int x = -HALFW; x < HALFW; x++) {
                /** These are the gradient shapes. */
                result[0][index] = (char) (255 - 512 * ((float) x * x / (W * W) + (float) y * y / (H * H)));
                result[1][index] = (char) (255 - (x + HALFW) * 255 / W);
                result[2][index] = (char) (255 - (y + HALFH) * 255 / H);
                result[3][index] = (char) (255 - (y + HALFH) * (x + HALFW) * 255 / (W * H));
                result[4][index] = (char) (255 - (abs(x) + abs(y)) * 255 / (HALFW + HALFH));
                result[5][index] = (char) (255 - (abs(x) * abs(y)) * 255 / (HALFW * HALFH));
                result[6][index] = (char) (255 * (1 - min(1, sqrt(x * x + y * y) / min(HALFH, HALFW))));
                result[7][index] = (char) (255 * (1 - pow(min(1, sqrt(x * x + y * y) / min(HALFH, HALFW)), 9)));
                result[8][index] = (char) (255 * (1 - pow(max(0, min(1, 1.3 - (sqrt(x * x + y * y)) / (min(HALFH, HALFW)))), 9)));
                result[9][index] = (char) (result[7][index]);
                index++;
            }
        }
        grad_index = 0;
        return result;
    }
    private final Operator[] gradient_modes = {
        new Operator("None") {
            @Override
            void operate(RenderStateMachine m) {
            }},
        new Operator("1 Color") {
            @Override
            void operate(RenderStateMachine m) {
                int grad = 0xFF - (char) min(0xFF, max(0, gradient[m.i]
                        * grad_slope
                        - grad_offset));
                m.color = average(m.color, 0xFF ^ grad, fade_color, grad); 
            }},
        new Operator("2 Color") {
            @Override
            void operate(RenderStateMachine m) {
                int grad = 255 - (char)clip((int)(
                    gradient[m.i] * grad_slope - grad_offset
                        ), 0, 255);
                m.color =
                    average(m.color, 0xFF^grad,
                        average(grad_accent, 0xFF^grad^grad_switch, 
                                fade_color     , grad^grad_switch),
                        grad);
            }}};
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Abstract definition of an image-processing operation. 
    private long opcount = 0L;
    private abstract class Operator {
        final String name;
        Operator() {name = "Operator" + (opcount++);}
        Operator(String s) {name = s == null ? ("Operator" + (opcount++)) : s;}
        abstract void operate(RenderStateMachine m);
        @Override
        public String toString() {return name;}
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // RenderState machine structure contains the instance variables that 
    // Operators can modify. The Renderer scans the map_buffer to set the pixel
    // color at the appropriate location in the screen buffer.
    // Various effect functions are then called to calculate
    // the color of the map element, and hence the pixel.
    class RenderStateMachine {
        int color, oldcolor;   
        // 8-bit fixed point coordinates of mapped pixel
        int fx, fy;
        // index of target pixel in the output buffer
        int i;
        // Render indecies from start to end-1, skipping every `stride` pixels
        int start = 0;
        int end = MLEN;     
        int stride = 1;
        // Constant offset in 8-bit fixed point screen coordinates
        int Cx, Cy;
        // Precomputed rotation constants
        float rotation_real; // Rotate/scale normalized to screen coordinates
        float rotation_imag; // Rotate/scale normalized to screen coordinates
        float rotation_coef; // Intermediate term used for Gauss multiply
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Dead code. //////////////////////////////////////////////////////////////
    
    /** Image mode-related projection mask. */
    int projectionMask(RenderStateMachine m) {
        int grad = gradients[9][m.i];
        return average(m.color, grad, 0, 256 - grad);
    }
}
