package perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.max;
import rendered3D.Tree3D;

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
    
    final static String [] gradient_names  = {"Circle","Horizontal","Vertical","Diagonal","Diamond","Cross","Ring","Sharp Ring","Eye"};
    final static String [] gradient_accent_names  = {"Black","White"};

    ////////////////////////////////////////////////////////////////////////////
    // Mirror of every state in perceptron
    // Perceptron
    public boolean objects_on_top     = true;
    public boolean cap_frame_rate     = true;
    public boolean rotate_images      = false;
    public boolean fore_grad          = false;
    public boolean draw_moths         = false; 
    public boolean draw_top_bars      = true;
    public boolean draw_side_bars     = true;
    public boolean do_hue_rotation    = true;
    public int     hue_rate = 0;
    public int     sat_rate = 0;
    public int     lum_rate = 0;
    public int     bri_rate = 0;
    public int     con_rate = 0;
    public int     blursharp_rate = 0;
    // FractalMap
    public int     offset_mode   = 0;
    public int     rotate_mode   = 0;
    public int     mirror_mode   = 0;
    public int     motion_blur   = 255;
    public boolean dampen_colors = true;
    public String  fractal_map;
    public boolean bounds_invert = false;
    public int     bounds_i      = 0; 
    public int     outside_i     = 0; 
    public int     gradcolor1_i  = 0; // Gradient color index used in modes 1 and 2
    public int     gradcolor2_i  = 0; // Accent color index if in gradient mode 2
    public int     barcolor_i    = 0; // H/V bar colors (if present)
    public int     tintcolor_i   = 0; // Global (uniform) tinting
    public int     outcolor_i    = 0; // Color used to fill in for boundary mode 0
    public int     noise_level   = 0;
    public int     color_dampen  = 0;
    public float   grad_slope    = 1f; 
    public float   grad_offset   = 0f;
    public int     color_mask    = 0;  
    public int     feedback_mask = 0;
    public int     grad_accent   = 0;
    public int     grad_i        = 0;
    public int     grad_mode     = 0;
    // DoubleBuffer
    public boolean reflect     = true;
    public boolean interpolate = true;
    public boolean fancy       = true;
    // ControlSet
    //public boolean draw_cursors = true;
    public boolean draw_futures = true;
    public double  XBranchingCursor;
    public double  XAlphaCursor;
    public double  XBranchLengthCursor;
    public double  XTreeOrientationCursor;
    public double  XMapCursor;
    public double  XMapRotationCursor;
    public double  XGradintCursor;
    public double  XMapAlphaCursor;
    public double  XTreeLocationCursor;
    public double  YBranchingCursor;
    public double  YAlphaCursor;
    public double  YBranchLengthCursor;
    public double  YTreeOrientationCursor;
    public double  YMapCursor;
    public double  YMapRotationCursor;
    public double  YGradintCursor;
    public double  YMapAlphaCursor;
    public double  YTreeLocationCursor;
    // Tree3D
    public boolean tree_active;
    // TextBuffer
    public boolean on   = true;
    public boolean cursor_on = true;
    

    /** Read the preset.
     * @param in
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

    /** Save the current state into a file (or input the settings in as they 
     * are given here) from the preset. 
     * @param P
     * @return  */
    public static String settings(Perceptron P) {
        FractalMap F = P.fractal;
        DoubleBuffer B = P.buf;
        Tree3D R = P.tree;
        TextBuffer T = P.text;
        ControlSet C = P.control;
        String GAP = "      ";
        String out = "";
        out += GAP + "objects_on_top         " + P.objects_on_top + "\n";
        out += GAP + "cap_frame_rate         " + P.cap_frame_rate + "\n";
        out += GAP + "rotate_images          " + P.rotate_images + "\n";
        out += GAP + "fore_grad              " + P.fore_grad + "\n";
        out += GAP + "draw_moths             " + P.draw_moths + "\n";
        out += GAP + "draw_top_bars          " + P.draw_top_bars + "\n";
        out += GAP + "draw_side_bars         " + P.draw_side_bars + "\n";
        out += GAP + "do_hue_rotation        " + P.do_color_transform + "\n";
        out += GAP + "hue_rate               " + P.hue_rate + "\n";
        out += GAP + "sat_rate               " + P.sat_rate + "\n";
        out += GAP + "lum_rate               " + P.lum_rate + "\n";
        out += GAP + "bri_rate               " + P.bri_rate + "\n";
        out += GAP + "con_rate               " + P.con_rate + "\n";
        out += GAP + "blursharp_rate         " + P.blursharp_rate + "\n";
        out += GAP + "offset_type            " + F.offset_mode + "\n";
        out += GAP + "rotate_type            " + F.rotate_mode + "\n";
        out += GAP + "mirror_type            " + F.rotate_mode + "\n";
        out += GAP + "motionblurp            " + F.motion_blur + "\n";
        out += GAP + "gradcolor1_i           " + F.gcolor1_i + "\n";
        out += GAP + "gradcolor2_i           " + F.gcolor2_i + "\n";
        out += GAP + "barcolor_i             " + F.barcolor_i + "\n";
        out += GAP + "tintcolor_i            " + F.tintcolor_i + "\n";
        out += GAP + "outcolor_i             " + F.outcolor_i + "\n";
        out += GAP + "noise_level            " + F.noise_level + "\n";
        out += GAP + "color_dampen           " + F.color_dampen + "\n";
        out += GAP + "fractal_map            " + F.mapping + "\n";
        out += GAP + "bounds_i               " + F.bounds_i + "\n";
        out += GAP + "bounds_invert          " + F.bounds_invert + "\n";
        out += GAP + "outside_i              " + F.outside_i + "\n";
        out += GAP + "fade_i                 " + F.gcolor1_i + "\n";
        out += GAP + "grad_accent_i          " + F.gcolor2_i + "\n";
        out += GAP + "grad_slope             " + F.gslope + "\n";
        out += GAP + "grad_offset            " + F.goffset + "\n";
        out += GAP + "color_mask             " + F.color_mask + "\n";
        out += GAP + "feedback_mask          " + F.feedback_mask + "\n";
        out += GAP + "grad_accent            " + F.gcolor2 + "\n";
        out += GAP + "grad_i                 " + F.grad_i + "\n";
        out += GAP + "grad_mode              " + F.grad_mode + "\n";
        out += GAP + "reflect                " + B.reflect + "\n";
        out += GAP + "interpolate            " + B.interpolate + "\n";
        out += GAP + "fancy                  " + B.fancy + "\n";
        out += GAP + "tree_active            " + R.active + "\n";
        out += GAP + "on                     " + T.on + "\n";
        out += GAP + "cursor_on              " + T.cursor_on + "\n";
        out += GAP + "draw_cursors           " + C.draw_cursors + "\n";
        out += GAP + "draw_futures           " + C.draw_futures + "\n";
        out += GAP + "XBranchingCursor       " + C.XBranchingCursor() + "\n";
        out += GAP + "XAlphaCursor           " + C.XAlphaCursor() + "\n";
        out += GAP + "XBranchLengthCursor    " + C.XBranchLengthCursor() + "\n";
        out += GAP + "XTreeOrientationCursor " + C.XTreeOrientationCursor() + "\n";
        out += GAP + "XMapCursor             " + C.XMapCursor() + "\n";
        out += GAP + "XMapRotationCursor     " + C.XMapRotationCursor() + "\n";
        out += GAP + "XGradintCursor         " + C.XGradientCursor() + "\n";
        out += GAP + "XTreeLocationCursor    " + C.XTreeLocationCursor() + "\n";
        out += GAP + "YBranchingCursor       " + C.YBranchingCursor() + "\n";
        out += GAP + "YAlphaCursor           " + C.YAlphaCursor() + "\n";
        out += GAP + "YBranchLengthCursor    " + C.YBranchLengthCursor() + "\n";
        out += GAP + "YTreeOrientationCursor " + C.YTreeOrientationCursor() + "\n";
        out += GAP + "YMapCursor             " + C.YMapCursor() + "\n";
        out += GAP + "YMapRotationCursor     " + C.YMapRotationCursor() + "\n";
        out += GAP + "YGradintCursor         " + C.YGradientCursor() + "\n";
        out += GAP + "YTreeLocationCursor    " + C.YTreeLocationCursor() + "\n";
        return out;
    }

    /** The help screen contents.
     *  Press / to show  the help screen with the current settings next to option names.
     * @param P
     * @return  */
    public static String helpString(Perceptron P) {
        ControlSet C = P.control;
        FractalMap F = P.fractal;
        String out = "";
        out += "Ctl @type equation         @" + C.entry_mode + "\n";
        out += "?/  @show help             @" + P.show_help + "\n";
        out += "qQ  @± fractal map         @" + F.mapping + "\n";
        out += "wW  @± outside coloring    @" + F.outside_i + " " + F.outside_op.name+"\n";
        out += "eE  @± boundary test       @" + F.bounds_i  + " " + F.bound_op.name+"\n";
        out += "r   @reflection            @" + P.buf.reflect + "\n";
        out += "R   @reverse bounds test   @" + F.bounds_invert + "\n";
        out += "t   @draw tree             @" + P.tree.isActive() + "\n";
        out += "T   @objects on top        @" + P.objects_on_top + "\n";
        out += "yY  @± outer color (w=0)   @" + F.outcolor_i + " " + F.color_register_names[F.outcolor_i] + "\n";
        out += "u   @show frame rate       @" + P.show_framerate + "\n";
        out += "U   @cap frame rate        @" + P.cap_frame_rate + "\n";
        out += "iI  @± input image         @" + P.image_i + " " + P.images.name() + "\n";
        out += "oO  @± translate mode      @" + F.offset_mode + " " + F.translate_modes[F.offset_mode] + "\n";
        out += "pP  @± rotate mode         @" + F.rotate_mode + " " + F.translate_modes[F.rotate_mode] + "\n";
        out += "S   @save (shift+s)        @ \n";
        out += "dD  @± fade color damping  @" + F.color_dampen + "\n";
        out += "f   @± grad color 1 (g>0)  @" + F.gcolor1_i + " " + F.color_register_names[F.gcolor1_i] + "\n";
        out += "F   @± grad color 2 (g=2)  @" + F.gcolor2_i + " " + F.color_register_names[F.gcolor2_i] + "\n";
        out += "gG  @± gradient mode       @" + F.grad_mode + " " + F.grad_op.name  + "\n";
        out += "hH  @± gradient shape      @" + F.grad_i + " " + gradient_names[F.grad_i] + "\n";
        out += "j   @invert output         @" + F.color_mask + "\n";
        out += "J   @invert feedback       @" + F.feedback_mask + "\n";
        out += "k   @foreground gradient   @" + P.fore_grad + "\n";
        out += "K   @color transform       @" + P.do_color_transform + '\n';
        out += "l   @linear interpolate    @" + P.buf.interpolate + "\n";
        out += "L   @anti-alias            @" + P.isFancy() + "\n";
        out += ";\' @± Δhue (n)            @" + P.hue_rate + '\n';
        out += ":\" @± Δsaturation (n)     @" + P.sat_rate + '\n';
        out += ",.  @± Δcontrast (n)       @" + P.con_rate + '\n';
        out += "<>  @± Δbrightness (n)     @" + P.bri_rate + '\n';
        out += "zZ  @± tint color          @" + F.tintcolor_i + " " + F.color_register_names[F.tintcolor_i] + "\n";
        out += "xX  @± tint level          @" + F.tint_level + "\n";
        out += "C   @cursor futures        @" + C.draw_futures + "\n";
        out += "c   @show cursors          @" + C.draw_cursors + "\n";
        out += "v   @wander                @" + (C.current==null?"(none)":C.current.wanderer) + "\n";
        out += "V   @autopilot             @" + C.screensaver + "\n";
        out += "b   @show text buffer      @" + P.text.on + "\n";
        out += "B   @text buffer cursor    @" + P.text.cursor_on + "\n";
        out += "n   @show notifications    @" + P.show_notifications + "\n";
        out += "m   @mirroring mode        @" + F.mirror_mode + " " + F.mirror_modes[F.mirror_mode] + "\n";
        out += "M   @draw moths            @" + P.draw_moths + "\n";
        out += "{}  @± cursor dots         @" + (C.current==null? "(none)" : C.current.nDots()) + "\n";
        out += "+-  @± cursor speed        @" + (C.current==null? "(none)" : C.current.speed) + '\n';
        out += "\\  @auto-advance image    @" + P.rotate_images + "\n";
        out += "_   @horizontal edge       @" + P.draw_top_bars + "\n";
        out += "|   @vertical edge         @" + P.draw_side_bars + "\n";
        out += "[]  @± HV edge color       @" + F.barcolor_i + " " + F.color_register_names[F.barcolor_i] + "\n";
        out += "()  @± noise level         @" + F.noise_level + '\n';
        out += "↑↓  @± motion blur         @" + F.motion_blur + '\n';
        out += "←   @sharpen               @" + max(0,-P.blursharp_rate) + '\n';
        out += "→   @blur                  @" + max(0, P.blursharp_rate) + '\n';
        out += "Home  @.... animate (!)  @" + P.write_animation + "\n";
        out += "Enter @presets mode        @" + (C.presets_mode? "true ("+C.preset_i+")" : "false") + "\n";
        return out;
    }
    
    /**
     *
     * @param P
     */
    public void set(Perceptron P) {
        FractalMap F = P.fractal;
        DoubleBuffer B = P.buf;
        Tree3D R = P.tree;
        TextBuffer T = P.text;
        ControlSet C = P.control;
        P.objects_on_top = objects_on_top;
        P.cap_frame_rate = cap_frame_rate;
        P.rotate_images = rotate_images;
        P.fore_grad = fore_grad;
        P.draw_moths = draw_moths;
        P.draw_top_bars = draw_top_bars;
        P.draw_side_bars = draw_side_bars;
        P.do_color_transform = do_hue_rotation;
        P.hue_rate = hue_rate;
        P.sat_rate = sat_rate;
        P.lum_rate = lum_rate;
        P.bri_rate = bri_rate;
        P.con_rate = con_rate;
        P.blursharp_rate = blursharp_rate;
        
        F.offset_mode = offset_mode;
        F.rotate_mode = rotate_mode;
        F.mirror_mode = mirror_mode;
        F.motion_blur = motion_blur;
        F.setMap(fractal_map);
        F.bounds_i = bounds_i;
        F.bounds_invert = bounds_invert;
        F.outside_i = outside_i;
        F.gslope = grad_slope;
        F.goffset = grad_offset;
        F.color_mask = color_mask;
        F.feedback_mask = feedback_mask;
        F.gcolor2 = grad_accent;
        F.grad_i = grad_i;
        F.grad_mode = grad_mode;
        
        F.gcolor1_i = gradcolor1_i;
        F.gcolor2_i = gradcolor2_i;
        F.barcolor_i   = barcolor_i;
        F.tintcolor_i  = tintcolor_i;
        F.outcolor_i   = outcolor_i;
        F.noise_level  = noise_level;
        F.color_dampen = color_dampen;
        
        F.syncOps();

        B.reflect = reflect;
        B.fancy = fancy;
        B.interpolate = interpolate;
        R.active = tree_active;
        T.on = on;
        T.cursor_on = cursor_on;

        C.draw_futures = draw_futures;
        C.setFractal(true);
        C.setTree(tree_active);
        C.setAudio(false);
        C.syncCursors();
        C.setBranchingCursor(XBranchingCursor,YBranchingCursor);
        C.setAlphaCursor(XAlphaCursor,YAlphaCursor);
        C.setBranchLengthCursor(XBranchLengthCursor,YBranchLengthCursor);
        C.setTreeOrientationCursor(XTreeOrientationCursor,YTreeOrientationCursor);
        C.setMapOffsetCursor(XMapCursor,YMapCursor);
        C.setMapRotationCursor(XMapRotationCursor,YMapRotationCursor);
        C.setGradientCursor(XGradintCursor,YGradintCursor);
        C.setTreeLocationCursor(XTreeLocationCursor,YTreeLocationCursor);
    }
    
    @Override
    public String toString() {
        String GAP = "      ";
        String out = "";
        out += GAP + "objects_on_top         = " + objects_on_top + "\n";
        out += GAP + "rotate_images          = " + rotate_images + "\n";
        out += GAP + "fore_grad              = " + fore_grad + "\n";
        out += GAP + "draw_moths             = " + draw_moths + "\n";
        out += GAP + "draw_top_bars          = " + draw_top_bars + "\n";
        out += GAP + "draw_side_bars         = " + draw_side_bars + "\n";
        out += GAP + "do_hue_rotation        = " + do_hue_rotation + "\n";
        out += GAP + "hue_rate               = " + hue_rate + "\n";
        out += GAP + "sat_rate               = " + sat_rate + "\n";
        out += GAP + "lum_rate               = " + lum_rate + "\n";
        out += GAP + "bri_rate               = " + bri_rate + "\n";
        out += GAP + "con_rate               = " + con_rate + "\n";
        out += GAP + "blursharp_rate         = " + blursharp_rate + "\n";
        out += GAP + "translate_type         = " + offset_mode + "\n";
        out += GAP + "rotate_type            = " + rotate_mode + "\n";
        out += GAP + "motionblurp            = " + motion_blur + "\n";
        out += GAP + "dampen_colors          = " + dampen_colors + "\n";
        out += GAP + "fractal_map            = " + fractal_map + "\n";
        out += GAP + "bounds_i               = " + bounds_i + "\n";
        out += GAP + "bounds_invert          = " + bounds_invert + "\n";
        out += GAP + "outside_i              = " + outside_i + "\n";
        out += GAP + "gradcolor1_i           = " + gradcolor1_i + "\n";
        out += GAP + "gradcolor2_i           = " + gradcolor2_i + "\n";
        out += GAP + "barcolor_i             = " + barcolor_i + "\n";
        out += GAP + "tintcolor_i            = " + tintcolor_i + "\n";
        out += GAP + "outcolor_i             = " + outcolor_i + "\n";
        out += GAP + "noise_level            = " + noise_level + "\n";
        out += GAP + "color_dampen           = " + color_dampen + "\n";
        out += GAP + "grad_slope             = " + grad_slope + "\n";
        out += GAP + "grad_offset            = " + grad_offset + "\n";
        out += GAP + "color_mask             = " + color_mask + "\n";
        out += GAP + "feedback_mask          = " + feedback_mask + "\n";
        out += GAP + "grad_accent            = " + grad_accent + "\n";
        out += GAP + "grad_i                 = " + grad_i + "\n";
        out += GAP + "grad_mode              = " + grad_mode + "\n";
        out += GAP + "reflect                = " + reflect + "\n";
        out += GAP + "interpolate            = " + interpolate + "\n";
        out += GAP + "fancy                  = " + fancy + "\n";
        out += GAP + "tree_active            = " + tree_active + "\n";
        out += GAP + "on                     = " + on + "\n";
        out += GAP + "cursor_on              = " + cursor_on + "\n";
        out += GAP + "draw_futures           = " + draw_futures + "\n";
        out += GAP + "XBranchingCursor       = " + XBranchingCursor + "\n";
        out += GAP + "XAlphaCursor           = " + XAlphaCursor + "\n";
        out += GAP + "XBranchLengthCursor    = " + XBranchLengthCursor + "\n";
        out += GAP + "XTreeOrientationCursor = " + XTreeOrientationCursor + "\n";
        out += GAP + "XMapCursor             = " + XMapCursor + "\n";
        out += GAP + "XMapRotationCursor     = " + XMapRotationCursor + "\n";
        out += GAP + "XGradintCursor         = " + XGradintCursor + "\n";
        out += GAP + "XTreeLocationCursor    = " + XTreeLocationCursor + "\n";
        out += GAP + "YBranchingCursor       = " + YBranchingCursor + "\n";
        out += GAP + "YAlphaCursor           = " + YAlphaCursor + "\n";
        out += GAP + "YBranchLengthCursor    = " + YBranchLengthCursor + "\n";
        out += GAP + "YTreeOrientationCursor = " + YTreeOrientationCursor + "\n";
        out += GAP + "YMapCursor             = " + YMapCursor + "\n";
        out += GAP + "YMapRotationCursor     = " + YMapRotationCursor + "\n";
        out += GAP + "YGradintCursor         = " + YGradintCursor + "\n";
        out += GAP + "YTreeLocationCursor    = " + YTreeLocationCursor + "\n";
        return out;
    }
} 
