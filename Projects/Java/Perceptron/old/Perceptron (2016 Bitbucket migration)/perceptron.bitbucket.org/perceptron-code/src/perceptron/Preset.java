package perceptron;

import image.ImageCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

/**
 * Read the preset and load the parameters.
 */
public final class Preset {

    /**
     * These variables load in the values stored in preset. We do not wish to
     * leave anything null in case we load and old preset with missing
     * variables, or if resource files are missing.
     */
    public Mapping fractal_map = FractalMap.makeMap("z*z");
    public float parameter_c_real = .5f;
    public float parameter_c_imaginary = .4f;
    public boolean pullback_map = true;
    public int bounding_region = 1;     // the boundary condition
    public int edge_extend_mode = 2;    // the outside coloring method
    public int reflection_transformation = 0;
    public String original_image_file = "resource/images/clouds.jpg";
    public int gradient_mode = 2;
    public int gradient_shape = 0;
    public int gradient_direction = 0xff;
    public int fade_color_mode = 0;
    public boolean dampen_fade_colors = false;
    public int color_accent = 0;
    public int color_filter = 0;
    public boolean partial_gradient_inversion = false;
    public int invert_colors = 16777215;
    public boolean initial_set = true;
    public int convolution_layer = 0;
    public boolean text_entry = false;
    public boolean salvia_mode = false;
    public boolean XOR_salvia_mode = false;
    public int renderer = 0;
    public boolean rotate_images = false;
    public boolean draw_tree = false;
    public int slow_down = 0;
    public boolean show_frame_rate = false;
    public int filter_weight = 64;
    public int motion_blur_p = 128;
    public boolean life = false;
    public int cellDrawer = 0;
    public boolean coloredLife = false;
    public boolean autopilot = false;
    public int autorotate_ortho = 0;
    public int autorotate_polar = 0;
    public boolean wander = false;
    public boolean show_cursors = true;
    public boolean cursor_trails = true;
    public int Width = 600, Height = 600;
    public double XBranchingCursor;// = 286.0;
    public double XAlphaCursor;// = 424.0;
    public double XBranchLengthCursor;// = 142.0;
    public double XTreeOrientationCursor;// = 306.0;
    public double XMapCursor;// = 192.0;
    public double XMapRotationCursor;// = 321.0;
    public double XGradintCursor;// = 248.0;
    public double XTreeLocationCursor;// = 308.0;
    public double YBranchingCursor;// = 178.0;
    public double YAlphaCursor;// = 241.0;
    public double YBranchLengthCursor;// = 312.0;
    public double YTreeOrientationCursor;// = 13.0;
    public double YMapCursor;// = 220.0;
    public double YMapRotationCursor;// = 200.0;
    public double YGradintCursor;// = 202.0;
    public double YTreeLocationCursor;// = 31.0;

//    public double XMapAlphaCursor;
//    public double XCellSpeedCursor;
//    public double XCellCursor;   
//    public double YMapAlphaCursor;   
//    public double YCellSpeedCursor;
//    public double YCellCursor;

    /**
     * Read the preset.
     */
    public static Preset parse(BufferedReader in) throws IOException {
        Preset p = new Preset();
        Parser.parse(p, in);
        return p;
    }

    /**
     * Write the preset.
     */
    public static void write(Perceptron percept, File file) throws IOException {
        try (FileWriter out = new FileWriter(file)) {
            out.write("****  Preset " + file.getName() + "\n");
            out.write("****  Comments: " + "\n");
            out.write("****  " + "\n");
            out.write("preset " + file.getName() + " {\n" + settings(percept) + "}\n");
            out.flush();
        }
    }

    /**
     * Save the current state into a preset file.
     */
    public static String settings(Perceptron percept) {
        String GAP = "      ";
        String out = "";
        out += GAP + "fractal_map      " + percept.fractal.mapping + "\n";
        out += GAP + "parameter_c_real      " + percept.fractal.norm_c[0] / 0x100 + "\n";
        out += GAP + "parameter_c_imaginary      " + percept.fractal.norm_c[1] / 0x100 + "\n";
        out += GAP + "pullback_map      " + percept.fractal.pullback_flag + "\n";
        out += GAP + "bounding_region      " + percept.fractal.boundary_condition_number + "\n";
        out += GAP + "edge_extend_mode      " + percept.fractal.outside_coloring_number + "\n";
        out += GAP + "reflection_transformation      " + percept.double_buffer.reflection + "\n";
        out += GAP + "original_image_file      " + "<" + ImageCache.current + ">" + "\n";
        out += GAP + "gradient_mode      " + percept.fractal.gradient_number + "\n";
        out += GAP + "gradient_direction      " + percept.fractal.gradient_switch + "\n";
        out += GAP + "gradient_shape      " + percept.fractal.gradient_selection + "\n";
        out += GAP + "fade_color_mode      " + percept.fractal.fade_number + "\n";
        out += GAP + "dampen_fade_colors      " + percept.fractal.dampen_colors + "\n";
        out += GAP + "color_accent      " + percept.fractal.accent_color_index + "\n";
        out += GAP + "color_filter      " + percept.fractal.color_filter_number + "\n";
        out += GAP + "partial_gradient_inversion      " + percept.fractal.partial_gradient_inversion_flag + "\n";
        out += GAP + "invert_colors      " + percept.fractal.gradient_inversion + "\n";
        out += GAP + "initial_set      " + percept.persistent_initial_set + "\n";
        out += GAP + "convolution_layer      " + percept.double_buffer.convolution + "\n";
        out += GAP + "text_entry      " + percept.controls.ENTRY_MODE + "\n";
        out += GAP + "salvia_mode      " + percept.salvia_mode + "\n";
        out += GAP + "XOR_salvia_mode      " + percept.XOR_MODE + "\n";
        out += GAP + "renderer      " + percept.fractal.renderer_number + "\n";
        out += GAP + "rotate_images      " + percept.rotateImages + "\n";
        out += GAP + "draw_tree      " + percept.the_tree.is_active() + "\n";
        out += GAP + "slow_down      " + percept.max_frame_time_length + "\n";
        out += GAP + "show_frame_rate      " + percept.frame_rate_display + "\n";
        out += GAP + "filter_weight      " + percept.fractal.filterweight + "\n";
        out += GAP + "motion_blur_p      " + percept.fractal.motionblurp + "\n";
        out += GAP + "life      " + percept.life.running + "\n";
        out += GAP + "cellDrawer      " + percept.life.renderer + "\n";
        out += GAP + "coloredLife      " + percept.life.colored + "\n";
        out += GAP + "autopilot      " + percept.controls.screensaver + "\n";
        out += GAP + "autorotate_ortho      " + percept.fractal.ortho_type + "\n";
        out += GAP + "autorotate_polar      " + percept.fractal.polar_type + "\n";
        out += GAP + "wander      " + percept.controls.wanderer + "\n";
        out += GAP + "show_cursors      " + percept.controls.draw_cursors + "\n";
        out += GAP + "cursor_trails      " + percept.controls.draw_futures + "\n";
        out += GAP + "Width      " + percept.fractal.W + "\n";
        out += GAP + "Height      " + percept.fractal.H + "\n";
        out += GAP + "XBranchingCursor      " + percept.controls.XBranchingCursor() + "\n";
        out += GAP + "XAlphaCursor      " + percept.controls.XAlphaCursor() + "\n";
        out += GAP + "XBranchLengthCursor      " + percept.controls.XBranchLengthCursor() + "\n";
        out += GAP + "XTreeOrientationCursor      " + percept.controls.XTreeOrientationCursor() + "\n";
        out += GAP + "XMapCursor      " + percept.controls.XMapCursor() + "\n";
        out += GAP + "XMapRotationCursor      " + percept.controls.XMapRotationCursor() + "\n";
        out += GAP + "XGradintCursor      " + percept.controls.XGradientCursor() + "\n";
        out += GAP + "XTreeLocationCursor      " + percept.controls.XTreeLocationCursor() + "\n";
        out += GAP + "YBranchingCursor      " + percept.controls.YBranchingCursor() + "\n";
        out += GAP + "YAlphaCursor      " + percept.controls.YAlphaCursor() + "\n";
        out += GAP + "YBranchLengthCursor      " + percept.controls.YBranchLengthCursor() + "\n";
        out += GAP + "YTreeOrientationCursor      " + percept.controls.YTreeOrientationCursor() + "\n";
        out += GAP + "YMapCursor      " + percept.controls.YMapCursor() + "\n";
        out += GAP + "YMapRotationCursor      " + percept.controls.YMapRotationCursor() + "\n";
        out += GAP + "YGradintCursor      " + percept.controls.YGradientCursor() + "\n";
        out += GAP + "YTreeLocationCursor      " + percept.controls.YTreeLocationCursor() + "\n";

        // out += GAP + "XMapAlphaCursor      " + perceptron.controls.XMapAlphaCursor() + "\n" ;
        // out += GAP + "XCellSpeedCursor      " + perceptron.controls.XCellSpeedCursor() + "\n" ;
        // out += GAP + "XCellCursor      " + perceptron.controls.XCellCursor() + "\n" ;
        // out += GAP + "YMapAlphaCursor      " + perceptron.controls.YMapAlphaCursor() + "\n" ;
        // out += GAP + "YCellSpeedCursor      " + perceptron.controls.YCellSpeedCursor() + "\n" ;
        // out += GAP + "YCellCursor      " + perceptron.controls.YCellCursor() + "\n" ;

        return out;
    }

    /**
     * The help screen contents. Press / to show the help screen with the
     * current settings next to option names.
     */
    public static String display_help_screen(Perceptron percept) {
        String out = "";
        out += "/    @show help    @" + percept.show_help + "\n";
        out += "Q, W    @ fractal map    @" + percept.fractal.mapping + "\n";
        out += "Ctrl    @type equation    @" + percept.controls.ENTRY_MODE + "\n";
        out += "End    @pullback    @" + percept.fractal.pullback_flag + "\n";
        out += "R    @boundary condition    @ " + percept.fractal.boundary_condition_number + "\n";
        out += "E    @outside coloring    @" + percept.fractal.outside_coloring_number + "\n";
        out += "I    @reflection map    @" + percept.double_buffer.reflection + "\n";
        out += "G    @gradient mode    @" + percept.fractal.gradient_number + "\n";
        out += "'   @gradient shape    @" + percept.fractal.gradient_selection + "\n";
        out += "K    @gradient direction    @" + percept.fractal.gradient_switch + "\n";
        out += "F    @fade color mode    @" + percept.fractal.fade_number + "\n";
        out += "V    @dampen fade color    @ " + percept.fractal.dampen_colors + "\n";
        out += "H    @color accent    @" + percept.fractal.accent_color_index + "\n";
        out += "X    @color filter    @" + percept.fractal.color_filter_number + "\n";
        out += "Ins    @partial inversion    @" + percept.fractal.partial_gradient_inversion_flag + "\n";
        out += "J    @total inversion    @" + percept.fractal.gradient_inversion_flag + "\n";
        out += "B    @initial set    @" + percept.persistent_initial_set + "\n";
        out += ";    @salvia mode    @" + percept.salvia_mode + "\n";
        out += "D    @XOR salvia mode    @" + percept.XOR_MODE + "\n";
        out += "L    @image mode    @" + percept.fractal.renderer_number + "\n";
        out += "M, N    @ select image    @" + percept.images.current_image_index() + "\n";
        out += "BkSp    @  shuffle images    @" + percept.rotateImages + "\n";
        out += "T    @draw tree    @" + percept.the_tree.is_active() + "\n";
        out += "Y    @convolution    @" + percept.double_buffer.convolution + "\n";
        out += "C    @hide/show cursors    @" + percept.controls.draw_cursors + "\n";
        out += "O    @cursor trails    @" + percept.controls.draw_futures + "\n";
        out += "U    @slow mode    @" + percept.max_frame_time_length + "\n";
        out += "left/right    @       filter weight    @" + percept.fractal.filterweight + "\n";
        out += "up/down    @       motion blur    @" + percept.fractal.motionblurp + "\n";
        out += "Z    @show frame rate    @" + percept.frame_rate_display + "\n";
        out += "A, S    @ load preset    @" + percept.controls.preset_number + "\n";
        out += "Home    @   animate!    @" + percept.write_animation + "\n";
        out += ",    @autorotate ortho    @" + percept.fractal.ortho_type + "\n";
        out += ".    @autorotate polar    @" + percept.fractal.polar_type + "\n";
        out += "P    @autopilot    @" + percept.controls.screensaver + "\n";
        out += "Del    @wander    @" + percept.controls.wanderer + "\n";

        return out;
    }

    /**
     * Initializations of functions or variables with values stored in a preset.
     */
    public void set(Perceptron percept) {
        System.out.println("~~~~~~~~~~~ Setting the current preset values ~~~~~~~~~~~~");
        percept.fractal.load_equation(fractal_map);
        // We do not really use the parameter c to store and load the parameter c. Instead, its value comes from the
        // saved cursor coordinates and the screen dimensions. It is written into preset file only for reference.
        //perceptron.fractal.norm_c[0] = parameter_c_real * 0x100;
        //perceptron.fractal.norm_c[1] = parameter_c_imaginary * 0x100;
        percept.fractal.set_pullback(pullback_map);
        percept.fractal.set_boundary_condition(bounding_region);
        percept.fractal.set_outside_coloring(edge_extend_mode);
        percept.set_image(original_image_file);
        percept.double_buffer.set_reflection(reflection_transformation);
        percept.fractal.set_gradient(gradient_mode);
        percept.fractal.gradient_switch = gradient_direction;
        if (percept.sw != null) {
            if (percept.fractal.gradient_switch == 0) {
                percept.sw.jcb_gradient_direction.setSelectedIndex(0);
            }
            if (percept.fractal.gradient_switch == 255) {
                percept.sw.jcb_gradient_direction.setSelectedIndex(1);
            }
        }
        percept.fractal.set_fader(fade_color_mode);
        percept.fractal.dampen_colors = dampen_fade_colors;
        if (percept.sw != null) {
            if (percept.fractal.dampen_colors) {
                percept.sw.jcb_dampen_fade_color.setSelectedIndex(0);
            } else {
                percept.sw.jcb_dampen_fade_color.setSelectedIndex(1);
            }
        }
        percept.fractal.set_accent(color_accent);
        percept.fractal.set_colorfilter(color_filter);
        percept.fractal.partial_gradient_inversion_flag = partial_gradient_inversion;
        if (percept.sw != null) {
            if (percept.fractal.partial_gradient_inversion_flag) {
                percept.sw.jcb_partial_inversion.setSelectedIndex(1);
            } else {
                percept.sw.jcb_partial_inversion.setSelectedIndex(0);
            }
        }
        percept.fractal.gradient_inversion = invert_colors;
        if (percept.sw != null) {
            if (percept.fractal.gradient_inversion == 0xFFFFFF) {
                percept.fractal.gradient_inversion_flag = false;
                percept.sw.jcb_total_inversion.setSelectedIndex(0);
            } else if (percept.fractal.gradient_inversion == 0x0) {
                percept.fractal.gradient_inversion_flag = true;
                percept.sw.jcb_total_inversion.setSelectedIndex(1);
            }
        }
        percept.fractal.set_gradient_shape(gradient_shape);
        percept.persistent_initial_set = initial_set;
        if (percept.sw != null) {
            if (percept.persistent_initial_set) {
                percept.sw.jcb_initial_set.setSelectedIndex(0);
            } else {
                percept.sw.jcb_initial_set.setSelectedIndex(1);
            }
        }
        percept.controls.ENTRY_MODE = text_entry;
        if (percept.sw != null) {
            if (percept.controls.ENTRY_MODE) {
                percept.sw.jtb_type_equation.setSelected(true);
                percept.sw.jtb_type_equation.setText("now reading in equation...");
            } else {
                percept.sw.jtb_type_equation.setSelected(false);
                percept.sw.jtb_type_equation.setText("Type equation");
            }
        }
        percept.salvia_mode = salvia_mode;
        if (percept.sw != null) {
            if (percept.salvia_mode) {
                percept.sw.jcb_salvia_mode.setSelectedIndex(1);
            } else {
                percept.sw.jcb_salvia_mode.setSelectedIndex(0);
            }
        }
        percept.XOR_MODE = XOR_salvia_mode;
        if (percept.sw != null) {
            if (percept.XOR_MODE) {
                percept.sw.jcb_XOR_salvia_mode.setSelectedIndex(1);
            } else {
                percept.sw.jcb_XOR_salvia_mode.setSelectedIndex(0);
            }
        }
        percept.controls.setTree(draw_tree);
        percept.fractal.set_convolution(convolution_layer);
        percept.max_frame_time_length = slow_down;
        if (percept.sw != null) {
            switch (percept.max_frame_time_length) {
                case 0:
                    percept.sw.jcb_slow_mode.setSelectedIndex(0);
                    break;
                case 400:
                    percept.sw.jcb_slow_mode.setSelectedIndex(1);
                    break;
                case 300:
                    percept.sw.jcb_slow_mode.setSelectedIndex(2);
                    break;
                case 200:
                    percept.sw.jcb_slow_mode.setSelectedIndex(3);
                    break;
                case 100:
                    percept.sw.jcb_slow_mode.setSelectedIndex(4);
                    break;
            }
        }
        percept.frame_rate_display = show_frame_rate;
        if (percept.sw != null) {
            if (percept.frame_rate_display) {
                percept.sw.jcb_show_frame_rate.setSelectedIndex(0);
            } else {
                percept.sw.jcb_show_help.setSelectedIndex(1);
            }
        }
        percept.fractal.set_renderer(renderer);
        percept.rotateImages = rotate_images;
        if (percept.sw != null) {
            if (percept.rotateImages) {
                percept.sw.jcb_shuffle_images.setSelectedIndex(1);
            } else {
                percept.sw.jcb_shuffle_images.setSelectedIndex(0);
            }
        }
        percept.fractal.setColorFilterWeight(filter_weight);
        percept.fractal.setMotionBlur(motion_blur_p);

        percept.life.running = life;
        percept.life.setRenderer(cellDrawer);
        percept.life.colored = coloredLife;

        percept.controls.draw_cursors = show_cursors;
        if (percept.sw != null) {
            if (percept.controls.draw_cursors) {
                percept.sw.jcb_hide_show_cursors.setSelectedIndex(0);
            } else {
                percept.sw.jcb_hide_show_cursors.setSelectedIndex(1);
            }
        }
        percept.controls.draw_futures = cursor_trails;
        if (percept.sw != null) {
            if (percept.controls.draw_futures) {
                percept.sw.jcb_cursor_trails.setSelectedIndex(0);
            } else {
                percept.sw.jcb_cursor_trails.setSelectedIndex(1);
            }
        }

        percept.controls.screensaver = autopilot;
        if (percept.sw != null) {
            if (percept.controls.screensaver) {
                percept.sw.jtb_autopilot.setSelected(true);
            } else {
                percept.sw.jtb_autopilot.setSelected(false);
            }
        }
        percept.fractal.ortho_type = autorotate_ortho;
        if (percept.sw != null) {
            percept.sw.jcb_autorotate_ortho.setSelectedIndex(percept.fractal.ortho_type);
        }
        percept.fractal.polar_type = autorotate_polar;
        if (percept.sw != null) {
            percept.sw.jcb_autorotate_polar.setSelectedIndex(percept.fractal.polar_type);
        }
        percept.controls.wanderer = wander;
        if (percept.sw != null) {
            if (percept.controls.wanderer) {
                percept.sw.jtb_wander.setSelected(true);
            } else {
                percept.sw.jtb_wander.setSelected(false);
            }
        }

        if ((percept.fractal.W != Width) || (percept.fractal.H != Height)) {
            float scale_x = (float) percept.fractal.W / Width;
            float scale_y = (float) percept.fractal.H / Height;
            percept.controls.setXBranchingCursor(scale_x * XBranchingCursor);
            percept.controls.setXAlphaCursor(scale_x * XAlphaCursor);
            percept.controls.setXBranchLengthCursor(scale_x * XBranchLengthCursor);
            percept.controls.setXTreeOrientationCursor(scale_x * XTreeOrientationCursor);
            percept.controls.setXMapCursor(scale_x * XMapCursor);
            percept.controls.setXMapRotationCursor(scale_x * XMapRotationCursor);
            percept.controls.setXGradientCursor(scale_x * XGradintCursor);
            percept.controls.setXTreeLocationCursor(scale_x * XTreeLocationCursor);
            // perceptron.controls.setXCellSpeedCursor (scale_x * XCellSpeedCursor) ;
            // perceptron.controls.setXCellCursor (scale_x * XCellCursor) ;
            percept.controls.setYBranchingCursor(scale_y * YBranchingCursor);
            percept.controls.setYAlphaCursor(scale_y * YAlphaCursor);
            percept.controls.setYBranchLengthCursor(scale_y * YBranchLengthCursor);
            percept.controls.setYTreeOrientationCursor(scale_y * YTreeOrientationCursor);
            percept.controls.setYMapCursor(scale_y * YMapCursor);
            percept.controls.setYMapRotationCursor(scale_y * YMapRotationCursor);
            percept.controls.setYGradientCursor(scale_y * YGradintCursor);
            percept.controls.setYTreeLocationCursor(scale_y * YTreeLocationCursor);
            // perceptron.controls.setYCellSpeedCursor (scale_y * YCellSpeedCursor) ;
            // perceptron.controls.setYCellCursor (scale_y * YCellCursor) ;
        } else {
            percept.controls.setXBranchingCursor(XBranchingCursor);
            percept.controls.setXAlphaCursor(XAlphaCursor);
            percept.controls.setXBranchLengthCursor(XBranchLengthCursor);
            percept.controls.setXTreeOrientationCursor(XTreeOrientationCursor);
            percept.controls.setXMapCursor(XMapCursor);
            percept.controls.setXMapRotationCursor(XMapRotationCursor);
            percept.controls.setXGradientCursor(XGradintCursor);
            percept.controls.setXTreeLocationCursor(XTreeLocationCursor);
            // perceptron.controls.setXCellSpeedCursor (XCellSpeedCursor) ;
            // perceptron.controls.setXCellCursor (XCellCursor) ;
            percept.controls.setYBranchingCursor(YBranchingCursor);
            percept.controls.setYAlphaCursor(YAlphaCursor);
            percept.controls.setYBranchLengthCursor(YBranchLengthCursor);
            percept.controls.setYTreeOrientationCursor(YTreeOrientationCursor);
            percept.controls.setYMapCursor(YMapCursor);
            percept.controls.setYMapRotationCursor(YMapRotationCursor);
            percept.controls.setYGradientCursor(YGradintCursor);
            percept.controls.setYTreeLocationCursor(YTreeLocationCursor);
            // perceptron.controls.setYCellSpeedCursor (YCellSpeedCursor) ;
            // perceptron.controls.setYCellCursor (YCellCursor) ;
        }
    }
}
