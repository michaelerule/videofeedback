package perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.max;

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
    public int edge_extend_mode; // the outside coloring method
    public int bounding_region; // the boundary condition
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
    public double YBranchingCursor;
    public double YAlphaCursor;
    public double YBranchLengthCursor;
    public double YTreeOrientationCursor;
    public double YMapCursor;
    public double YMapRotationCursor;
    public double YGradintCursor;
    public double YMapAlphaCursor;
    public double YTreeLocationCursor;

    /** Read the preset.
     * @param in
     * @param fractal
     * @return
     * @throws java.io.IOException  */
    public static Preset parse(BufferedReader in) throws IOException {
        Preset p = new Preset();
        Parser.parse(p, in);
        return p;
    }

    /** Write the preset.
     * @param percept
     * @param file
     * @throws java.io.IOException */
    public static void write(Perceptron percept, File file) throws IOException {
        FileWriter out = new FileWriter(file);
        out.write("preset " + file.getName() + " {\n" + settings(percept) + "}\n");
        out.flush();
        out.close();
    }

    /** *  Save the current state into a file (or input the
     * settings in as they are given here) from the
     * preset.Check if there are new features in
 the Perceptron.
     * @param percept
     * @return  */
    public static String settings(Perceptron percept) {
        String GAP = "      ";
        String out = "";
        out += GAP + "anti_alias             " + percept.isFancy() + "\n";
        out += GAP + "show_cursors           " + percept.control.draw_cursors + "\n";
        out += GAP + "cursor_trails          " + percept.control.draw_futures + "\n";
        //out += GAP + "velocity_mode          " + percept.fractal.auto_rotate + "\n";      //  killed!
        out += GAP + "autopilot              " + percept.control.screensaver + "\n";
        out += GAP + "fractal_map            " + percept.fractal.mapping + "\n";
        out += GAP + "edge_extend_mode       " + percept.fractal.outside_i + "\n";
        out += GAP + "bounding_region        " + percept.fractal.bounds_i + "\n";
        out += GAP + "fade_color_mode        " + percept.fractal.fade_i + "\n";
        out += GAP + "gradient_mode          " + percept.fractal.grad_mode + "\n";
        out += GAP + "color_accent           " + percept.fractal.grad_accent_i + "\n";
        out += GAP + "color_filter           " + percept.fractal.color_i + "\n";
        out += GAP + "dampen_fade_colors     " + percept.fractal.dampen_colors + "\n";
        out += GAP + "invert_colors          " + percept.fractal.color_mask + "\n";
        out += GAP + "gradient_direction     " + percept.fractal.grad_switch + "\n";
        out += GAP + "draw_tree              " + percept.the_tree.isActive() + "\n";
        out += GAP + "background_objects     " + !percept.objects_on_top + "\n";
        out += GAP + "cap_frame_rate         " + percept.cap_frame_rate + "\n";
        out += GAP + "show_frame_rate        " + percept.frame_rate_display + "\n";
        out += GAP + "salvia_mode            " + percept.text.on + "\n";
        out += GAP + "text_entry             " + percept.control.entry_mode + "\n";
        out += GAP + "sketch                 " + percept.sketchNum + "\n";
        out += GAP + "rotateImages           " + percept.rotateImages + "\n";

        out += GAP + "XBranchingCursor       " + percept.control.XBranchingCursor() + "\n";
        out += GAP + "XAlphaCursor           " + percept.control.XAlphaCursor() + "\n";
        out += GAP + "XBranchLengthCursor    " + percept.control.XBranchLengthCursor() + "\n";
        out += GAP + "XTreeOrientationCursor " + percept.control.XTreeOrientationCursor() + "\n";
        out += GAP + "XMapCursor             " + percept.control.XMapCursor() + "\n";
        out += GAP + "XMapRotationCursor     " + percept.control.XMapRotationCursor() + "\n";
        out += GAP + "XGradintCursor         " + percept.control.XGradientCursor() + "\n";
        out += GAP + "XTreeLocationCursor    " + percept.control.XTreeLocationCursor() + "\n";
        out += GAP + "YBranchingCursor       " + percept.control.YBranchingCursor() + "\n";
        out += GAP + "YAlphaCursor           " + percept.control.YAlphaCursor() + "\n";
        out += GAP + "YBranchLengthCursor    " + percept.control.YBranchLengthCursor() + "\n";
        out += GAP + "YTreeOrientationCursor " + percept.control.YTreeOrientationCursor() + "\n";
        out += GAP + "YMapCursor             " + percept.control.YMapCursor() + "\n";
        out += GAP + "YMapRotationCursor     " + percept.control.YMapRotationCursor() + "\n";
        out += GAP + "YGradintCursor         " + percept.control.YGradientCursor() + "\n";
        out += GAP + "YTreeLocationCursor    " + percept.control.YTreeLocationCursor() + "\n";

        return out;
    }

    /** The help screen contents.
     *  Press / to show  the help screen with the current settings next to option names.
     * @param P
     * @return  */
    public static String helpString(Perceptron P) {
        String out = "";
        out += "/   @show help             @" + P.show_help + "\n";
        out += "Y   @reflect input image   @" + P.buff.reflect + "\n";
        out += "C   @show cursors?         @" + P.control.draw_cursors + "\n";
        out += "O   @cursor trails?        @" + P.control.draw_futures + "\n";
        out += "Q,W @fractal map           @" + P.fractal.mapping + "\n";
        out += "Ctl @type equation         @" + P.control.entry_mode + "\n";
        out += "E   @outside coloring      @" + P.fractal.outside_i + "\n";
        out += "R   @boundary condition    @" + P.fractal.bounds_i + "\n";
        out += "A   @anti-alias            @" + P.isFancy() + "\n";
        out += "S   @show frame rate       @" + P.frame_rate_display + "\n";
        out += "D   @flip divergence test  @" + P.fractal.bounds_invert + "\n";
        out += "F   @fade color mode       @" + P.fractal.fade_i + "\n";
        out += "G   @gradient mode         @" + P.fractal.grad_mode + "\n";
        out += "'   @gradient shape        @" + P.fractal.grad_index + "\n";
        out += "H   @color accent          @" + P.fractal.grad_accent_i + "\n";
        out += "N   @color filter          @" + P.fractal.color_i + "\n";
        out += "V   @dampen fade color     @" + P.fractal.dampen_colors + "\n";
        out += "I   @interpolation         @" + P.buff.interpolate + "\n";
        out += "J   @invert colors?        @" + P.fractal.color_mask + "\n";
        out += "K   @gradient direction    @" + P.fractal.grad_switch + "\n";
        out += ",   @autorotate ortho      @" + P.fractal.ortho_type + "\n";
        out += ".   @autorotate polar      @" + P.fractal.polar_type + "\n";
        out += "P   @autopilot             @" + P.control.screensaver + "\n";
        out += "Del @wander                @" + P.control.wanderer + "\n";
     // out += "\\   @auto-advance image   @" + percept.rotateImages + "\n";
        out += "T   @draw tree             @" + P.the_tree.isActive() + "\n";
        out += "B   @objects on top        @" + P.objects_on_top + "\n";
        out += "U   @cap frame rate        @" + P.cap_frame_rate + "\n";
        out += ";   @salvia mode           @" + P.text.on + "\n";
        out += "M   @draw moths            @" + P.draw_moths + "\n";
        out += "Z,X @select input image    @" + P.sketchNum + "\n";
        out += "+-  @add/remove cursor dot @" + (P.control.current==null? "(none)" : P.control.current.nDots()) + "\n";
        out += "_   @horizontal edge       @" + P.draw_top_bars + "\n";
        out += "|   @vertical edge         @" + P.draw_side_bars + "\n";
        out += "↑↓  @adjust motion blur    @" + P.fractal.motionblurq + '\n';
        out += "←   @sharper               @" + max(0,-P.fractal.filterweight) + '\n';
        out += "→   @blurrier              @" + max(0, P.fractal.filterweight) + '\n';
        out += "Home    @.... animate (!)  @" + P.write_animation + "\n";
        return out;
        
        //out += "A   @?         @" + percept.controls.preset_number + "\n";
        //out += "Alt     @pause         @" + percept.controls.fullscreen + "\n";
    }
    
    /**
     *
     * @param P
     */
    public void set(Perceptron P) {
        P.setFancy(anti_alias);
        P.control.draw_futures = cursor_trails;
        P.control.screensaver = autopilot;
        P.fractal.loadEquation(fractal_map);
        P.fractal.setOutsideColoring(edge_extend_mode);
        P.fractal.setBounds(bounding_region);
        P.fractal.setFader(fade_color_mode);
        P.fractal.setGradient(gradient_mode);
        P.fractal.setAccent(color_accent);
        P.fractal.setColorFilter(color_filter);
        P.fractal.toggleFadeColorSmoothing();
        P.fractal.color_mask = invert_colors;
        P.fractal.grad_switch = gradient_direction;
        P.control.setTree(draw_tree);
        P.objects_on_top = !background_objects;
        P.cap_frame_rate = cap_frame_rate;
        P.frame_rate_display = show_frame_rate;
        P.text.on = salvia_mode;
        P.control.entry_mode = text_entry;
        P.setSketch(sketch);
        //P.rotateImages = rotateImages;

        P.control.setXBranchingCursor(XBranchingCursor);
        P.control.setXAlphaCursor(XAlphaCursor);
        P.control.setXBranchLengthCursor(XBranchLengthCursor);
        P.control.setXTreeOrientationCursor(XTreeOrientationCursor);
        P.control.setXMapCursor(XMapCursor);
        P.control.setXMapRotationCursor(XMapRotationCursor);
        P.control.setXGradientCursor(XGradintCursor);
        P.control.setXTreeLocationCursor(XTreeLocationCursor);
        P.control.setYBranchingCursor(YBranchingCursor);
        P.control.setYAlphaCursor(YAlphaCursor);
        P.control.setYBranchLengthCursor(YBranchLengthCursor);
        P.control.setYTreeOrientationCursor(YTreeOrientationCursor);
        P.control.setYMapCursor(YMapCursor);
        P.control.setYMapRotationCursor(YMapRotationCursor);
        P.control.setYGradientCursor(YGradintCursor);
        P.control.setYTreeLocationCursor(YTreeLocationCursor);
    }
}
