package perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Preset.java
 *
 * Created on October 17, 2007, 11:02 PM
 *
 * @author Michael Rule
 */
/**Check if  there are any new features in the Perceptron. */
/** Read the preset and load the parameters. */
public class Preset {

    public boolean anti_alias;
    public boolean show_cursors;
    public boolean cursor_trails;
    public boolean autopilot;
    public Mapping fractal_map;
    public int edge_extend_mode;      // the outside coloring method
    public int bounding_region;         // the boundary condition
    public int fade_color_mode;
    public int gradient_mode;
    public int color_accent;
    public int color_filter;
    public boolean dampen_fade_colors;
    public boolean interpolation;
    public int invert_colors;
    public int gradient_direction;
    public int mainRenderer;
    public int sketch; // load the sketch 0 by default
    public boolean draw_tree;
    public boolean anti_alias_tree;
    public boolean background_objects;
    public boolean cap_frame_rate;
    public boolean show_frame_rate;
    public boolean salvia_mode;
    public boolean XOR_salvia_mode;
    public boolean text_entry;
    public boolean life;
    public int cellDrawer;
    public boolean coloredLife;
    public boolean rotateImages;
    public double XBranchingCursor;
    public double XAlphaCursor;
    public double XBranchLengthCursor;
    public double XTreeOrientationCursor;
    public double XMapCursor;
    public double XMapRotationCursor;
    public double XGradintCursor;
    public double XMapAlphaCursor;
    public double XTreeLocationCursor;
    public double XCellSpeedCursor;
    public double XCellCursor;
    public double YBranchingCursor;
    public double YAlphaCursor;
    public double YBranchLengthCursor;
    public double YTreeOrientationCursor;
    public double YMapCursor;
    public double YMapRotationCursor;
    public double YGradintCursor;
    public double YMapAlphaCursor;
    public double YTreeLocationCursor;
    public double YCellSpeedCursor;
    public double YCellCursor;

    /** Read the preset. */
    public static Preset parse(BufferedReader in) throws IOException {
        Preset p = new Preset();
        Parser.parse(p, in);
        return p;
    }

    /** Write the preset. */
    public static void write(Perceptron percept, File file) throws IOException {
        FileWriter out = new FileWriter(file);
        out.write("preset " + file.getName() + " {\n" + settings(percept) + "}\n");
        out.flush();
        out.close();
    }

    /** Save the current state into a file (or input the
     * settings in as they are given here) from the
     * preset. Check if there are new features in
     * the Perceptron.     */
    public static String settings(Perceptron percept) {
        String GAP = "      ";
        String out = "";
        out += GAP + "anti_alias             " + percept.is_fancy() + "\n";
        out += GAP + "show_cursors           " + percept.controls.draw_cursors + "\n";
        out += GAP + "cursor_trails          " + percept.controls.draw_futures + "\n";
        //out += GAP + "velocity_mode          " + percept.fractal.auto_rotate + "\n";      //  killed!
        out += GAP + "autopilot              " + percept.controls.screensaver + "\n";
        out += GAP + "fractal_map            " + percept.fractal.mapping + "\n";
        out += GAP + "edge_extend_mode       " + percept.fractal.outside_coloring_number + "\n";
        out += GAP + "bounding_region        " + percept.fractal.boundary_condition_number + "\n";
        out += GAP + "fade_color_mode        " + percept.fractal.fade_number + "\n";
        out += GAP + "gradient_mode          " + percept.fractal.gradient_number + "\n";
        out += GAP + "color_accent           " + percept.fractal.accent_color_index + "\n";
        out += GAP + "color_filter           " + percept.fractal.color_filter_number + "\n";
        out += GAP + "dampen_fade_colors           " + percept.fractal.dampen_colors + "\n";
        out += GAP + "interpolation          " + percept.fractal.interpolate + "\n";
        out += GAP + "invert_colors          " + percept.fractal.gradient_inversion + "\n";
        out += GAP + "gradient_direction     " + percept.fractal.gradient_switch + "\n";
        out += GAP + "draw_tree              " + percept.the_tree.is_active() + "\n";
        out += GAP + "anti_alias_tree        " + percept.the_tree.fancy_graphics + "\n";
        out += GAP + "background_objects     " + !percept.objects_on_top + "\n";
        out += GAP + "cap_frame_rate         " + percept.cap_frame_rate + "\n";
        out += GAP + "show_frame_rate        " + percept.frame_rate_display + "\n";
        out += GAP + "salvia_mode            " + percept.salvia_mode + "\n";
        out += GAP + "XOR_salvia_mode        " + percept.XOR_MODE + "\n";
        out += GAP + "text_entry             " + percept.controls.ENTRY_MODE + "\n";
        out += GAP + "life                   " + percept.life.running + "\n";
        out += GAP + "cellDrawer             " + percept.life.renderer + "\n";
        out += GAP + "coloredLife            " + percept.life.colored + "\n";
        out += GAP + "mainRenderer           " + percept.fractal.renderer_number + "\n";
        out += GAP + "sketch                 " + percept.sketchNum + "\n";
        out += GAP + "rotateImages           " + percept.rotateImages + "\n";

        out += GAP + "XBranchingCursor       " + percept.controls.XBranchingCursor() + "\n";
        out += GAP + "XAlphaCursor           " + percept.controls.XAlphaCursor() + "\n";
        out += GAP + "XBranchLengthCursor    " + percept.controls.XBranchLengthCursor() + "\n";
        out += GAP + "XTreeOrientationCursor " + percept.controls.XTreeOrientationCursor() + "\n";
        out += GAP + "XMapCursor             " + percept.controls.XMapCursor() + "\n";
        out += GAP + "XMapRotationCursor     " + percept.controls.XMapRotationCursor() + "\n";
        out += GAP + "XGradintCursor         " + percept.controls.XGradientCursor() + "\n";
        //out += GAP + "XMapAlphaCursor        " + percept.controls.XMapAlphaCursor()        + "\n" ;
        out += GAP + "XTreeLocationCursor    " + percept.controls.XTreeLocationCursor() + "\n";
        //out += GAP + "XCellSpeedCursor       " + percept.controls.XCellSpeedCursor()       + "\n" ;
        //out += GAP + "XCellCursor            " + percept.controls.XCellCursor()            + "\n" ;
        out += GAP + "YBranchingCursor       " + percept.controls.YBranchingCursor() + "\n";
        out += GAP + "YAlphaCursor           " + percept.controls.YAlphaCursor() + "\n";
        out += GAP + "YBranchLengthCursor    " + percept.controls.YBranchLengthCursor() + "\n";
        out += GAP + "YTreeOrientationCursor " + percept.controls.YTreeOrientationCursor() + "\n";
        out += GAP + "YMapCursor             " + percept.controls.YMapCursor() + "\n";
        out += GAP + "YMapRotationCursor     " + percept.controls.YMapRotationCursor() + "\n";
        out += GAP + "YGradintCursor         " + percept.controls.YGradientCursor() + "\n";
        //out += GAP + "YMapAlphaCursor        " + percept.controls.YMapAlphaCursor()        + "\n" ;
        out += GAP + "YTreeLocationCursor    " + percept.controls.YTreeLocationCursor() + "\n";
        //out += GAP + "YCellSpeedCursor       " + percept.controls.YCellSpeedCursor()       + "\n" ;
        //out += GAP + "YCellCursor            " + percept.controls.YCellCursor()            + "\n" ;

        return out;
    }

    /** The help screen contents. Press / to show 
     * the help screen with the current settings next
     * to option names. */
    public static String display_help_screen(Perceptron percept) {
        String out = "";

        out += "/   @show help     @" + percept.show_help + "\n";
        //out += "NO LETTER   @anti-alias         @" + percept.is_fancy() + "\n";
        out += "C   @hide/show cursors       @" + percept.controls.draw_cursors + "\n";
        out += "O   @cursor trails      @" + percept.controls.draw_futures + "\n";
        out += "Q,W     @fractal map        @" + percept.fractal.mapping + "\n";
        out += "Ctrl    @type equation         @" + percept.controls.ENTRY_MODE + "\n";
        out += "E   @outside coloring   @" + percept.fractal.outside_coloring_number + "\n";
        out += "R   @boundary condition    @" + percept.fractal.boundary_condition_number + "\n";
        out += "F   @fade color mode    @" + percept.fractal.fade_number + "\n";
        out += "G   @gradient mode      @" + percept.fractal.gradient_number + "\n";
        out += "'   @gradient shape      @" + percept.fractal.gradient_selection + "\n";
        out += "H   @color accent       @" + percept.fractal.accent_color_index + "\n";
        out += "X   @color filter     @" + percept.fractal.color_filter_number + "\n";
        out += "V   @dampen fade color     @" + percept.fractal.dampen_colors + "\n";
        out += "I   @interpolation      @" + percept.fractal.interpolate + "\n";
        out += "J   @invert colors      @" + percept.fractal.gradient_inversion_flag + "\n";
        out += "K   @gradient direction @" + percept.fractal.gradient_switch + "\n";
        out += ",   @autorotate ortho         @" + percept.fractal.ortho_type + "\n";
        out += ".   @autorotate polar         @" + percept.fractal.polar_type + "\n";
        out += "P   @autopilot          @" + percept.controls.screensaver + "\n";
        out += "Del   @wander          @" + percept.controls.wanderer + "\n";
        out += "T   @draw tree          @" + percept.the_tree.is_active() + "\n";
        out += "Y   @anti-alias tree    @" + percept.the_tree.fancy_graphics + "\n";
        out += "B   @background objects @" + !percept.objects_on_top + "\n";
        out += "U   @cap frame rate     @" + percept.cap_frame_rate + "\n";
        out += "Z   @show frame rate    @" + percept.frame_rate_display + "\n";
        out += ";   @salvia mode        @" + percept.salvia_mode + "\n";
        out += "D   @XOR salvia mode    @" + percept.XOR_MODE + "\n";
        out += "A,S   @load preset         @" + percept.controls.preset_number + "\n";
        out += "L   @image mode         @" + percept.fractal.renderer_number + "\n";
        out += "M,N   @select image (L=1)       @" + percept.sketchNum + "\n";
        //out += "Alt     @pause         @" + percept.controls.fullscreen + "\n";
        out += "Home    @.... animate (!)         @" + percept.write_animation + "\n";
        return out;
    }

    public void set(Perceptron P) {
        P.set_fancy(anti_alias);
        //P.controls.draw_cursors =      show_cursors       ;
        P.controls.draw_futures = cursor_trails;
        P.controls.screensaver = autopilot;
        P.fractal.load_equation(fractal_map);
        P.fractal.set_outside_coloring(edge_extend_mode);
        P.fractal.set_boundary_condition(bounding_region);
        P.fractal.set_fader(fade_color_mode);
        P.fractal.set_gradient(gradient_mode);
        P.fractal.set_accent(color_accent);
        P.fractal.set_colorfilter(color_filter);
        P.fractal.dampen_colors_during_update_color();
        P.fractal.set_interpolation(interpolation);
        P.fractal.gradient_inversion = invert_colors;
        P.fractal.gradient_switch = gradient_direction;
        P.controls.setTree(draw_tree);
        P.the_tree.set_fancy_graphics(anti_alias_tree);
        P.objects_on_top = !background_objects;
        P.cap_frame_rate = cap_frame_rate;
        P.frame_rate_display = show_frame_rate;
        P.salvia_mode = salvia_mode;
        P.XOR_MODE = XOR_salvia_mode;
        P.life.running = life;
        P.controls.ENTRY_MODE = text_entry;
        P.life.setRenderer(cellDrawer);
        P.life.colored = coloredLife;
        P.set_sketch(sketch);
        P.fractal.set_renderer(mainRenderer);
        P.rotateImages = rotateImages;

        P.controls.setXBranchingCursor(XBranchingCursor);
        P.controls.setXAlphaCursor(XAlphaCursor);
        P.controls.setXBranchLengthCursor(XBranchLengthCursor);
        P.controls.setXTreeOrientationCursor(XTreeOrientationCursor);
        P.controls.setXMapCursor(XMapCursor);
        P.controls.setXMapRotationCursor(XMapRotationCursor);
        P.controls.setXGradientCursor(XGradintCursor);
        P.controls.setXTreeLocationCursor(XTreeLocationCursor);
        //P.controls.setXCellSpeedCursor       (XCellSpeedCursor)       ;
        //P.controls.setXCellCursor            (XCellCursor)            ;
        P.controls.setYBranchingCursor(YBranchingCursor);
        P.controls.setYAlphaCursor(YAlphaCursor);
        P.controls.setYBranchLengthCursor(YBranchLengthCursor);
        P.controls.setYTreeOrientationCursor(YTreeOrientationCursor);
        P.controls.setYMapCursor(YMapCursor);
        P.controls.setYMapRotationCursor(YMapRotationCursor);
        P.controls.setYGradientCursor(YGradintCursor);
        P.controls.setYTreeLocationCursor(YTreeLocationCursor);
        //P.controls.setYCellSpeedCursor       (YCellSpeedCursor)       ;
        //P.controls.setYCellCursor            (YCellCursor)            ;
    }
}
