package perceptron;

import rendered.TextBuffer;
import image.DoubleBuffer;
import image.Samplers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Preset.java
 * Created on October 17, 2007, 11:02 PM
 * @author Michael Rule
 */
/** Read the preset and load the parameters. */
public class Preset {
    final static String [] gradient_names  = {"Circle","Horizontal","Vertical","Diagonal","Diamond","Cross","Ring","Sharp Ring","Eye"};

    ////////////////////////////////////////////////////////////////////////////
    // Mirror of every state in perceptron
    // Perceptron
    public boolean objects_on_top  = true;
    public boolean rotate_images   = false;
    public boolean fore_grad       = false;
    public boolean draw_moths      = false; 
    public boolean draw_top_bars   = true;
    public boolean draw_side_bars  = true;
    public boolean do_hue_rotation = true;
    public boolean tree_active     = false;
    public boolean on              = true;
    public boolean cursor_on       = true;
    public boolean tree_leaves     = false;
    public boolean tree_symmetry   = true;
    public boolean draw_dino       = false;
    public boolean mic_active      = false;
    public boolean dampen_colors   = true;
    public boolean bounds_invert   = false;
    public boolean interpolate     = true;
    public boolean anti_alias      = true;
    public boolean draw_futures    = true;
    public String  fractal_map;
    public int     reflect       = 0;
    public int     hue_rate      = 0;
    public int     sat_rate      = 0;
    public int     lum_rate      = 0;
    public int     bri_rate      = 0;
    public int     con_rate      = 0;
    public int     blursharp_rate= 0;
    public int     offset_mode   = 0;
    public int     rotate_mode   = 0;
    public int     mirror_mode   = 0;
    public int     motion_blur   = 0;
    public int     bounds_i      = 0; 
    public int     outside_i     = 0; 
    public int     gradcolor1_i  = 0; // Gradient color index used in modes 1 and 2
    public int     gradcolor2_i  = 0; // Accent color index if in gradient mode 2
    public int     barcolor_i    = 0; // H/V bar colors (if present)
    public int     tintcolor_i   = 0; // Global (uniform) tinting
    public int     outcolor_i    = 0; // Color used to fill in for boundary mode 0
    public int     tint_level    = 0;
    public int     noise_level   = 0;
    public int     color_dampen  = 0;
    public int     color_mask    = 0;  
    public int     feedback_mask = 0;
    public int     grad_accent   = 0;
    public int     grad_i        = 0;
    public int     grad_mode     = 0;
    public int     mic_visualization = 0;
    // Controls
    public float  grad_slope    = 1f; 
    public float  grad_offset   = 0f;
    public float  mic_speed     = .05f;
    public float  mic_volume    = 1f;
    public float  XBranchingCursor;
    public float  XAlphaCursor;
    public float  XBranchLengthCursor;
    public float  XTreeOrientationCursor;
    public float  XMapCursor;
    public float  XMapRotationCursor;
    public float  XGradintCursor;
    public float  XMapAlphaCursor;
    public float  XTreeLocationCursor;
    public float  YBranchingCursor;
    public float  YAlphaCursor;
    public float  YBranchLengthCursor;
    public float  YTreeOrientationCursor;
    public float  YMapCursor;
    public float  YMapRotationCursor;
    public float  YGradintCursor;
    public float  YMapAlphaCursor;
    public float  YTreeLocationCursor;
    
    public String  image_file = "";
    
    // Name is the only state that's not a stored perceptron state variable
    private final String name;
    private Preset(String name) {this.name = name;}
    public String name() {return name;}

    /** Read the preset.
     * @param name
     * @param in
     * @return
     * @throws java.io.IOException  */
    public static Preset parse(String name, BufferedReader in) throws IOException {
        Preset p = new Preset(name);
        try {
            Parser.parse(p, in);
        } catch (Exception e) {
            System.err.println("Error parsing preset "+name);
            throw e;
        }
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
        FractalMap   F = P.fractal;
        DoubleBuffer B = P.buf;
        TextBuffer   T = P.text;
        Controls   C = P.control;
        String GAP = "      ";
        String out = "";
        out += GAP + "objects_on_top         " + P.objects_on_top + "\n";
        //out += GAP + "cap_frame_rate         " + P.cap_frame_rate + "\n";
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
        
        out += GAP + "offset_mode            " + F.offset_mode + "\n";
        out += GAP + "rotate_mode            " + F.rotate_mode + "\n";
        out += GAP + "mirror_mode            " + F.mirror_mode + "\n";
        out += GAP + "motionblurp            " + F.motion_blur + "\n";
        out += GAP + "gradcolor1_i           " + F.gcolor1_i + "\n";
        out += GAP + "gradcolor2_i           " + F.gcolor2_i + "\n";
        out += GAP + "barcolor_i             " + F.barcolor_i + "\n";
        out += GAP + "tintcolor_i            " + F.tintcolor_i + "\n";
        out += GAP + "outcolor_i             " + F.outcolor_i + "\n";
        out += GAP + "noise_level            " + F.noise_level + "\n";
        out += GAP + "color_dampen           " + F.color_dampen + "\n";
        out += GAP + "fractal_map            " + F.mapping.toString().strip().replaceAll(" ","") + "\n";
        out += GAP + "bounds_i               " + F.bounds_i + "\n";
        out += GAP + "bounds_invert          " + F.invert_bound + "\n";
        out += GAP + "outside_i              " + F.outi + "\n";
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
        out += GAP + "anti_alias             " + B.fancy + "\n";
        out += GAP + "tree_active            " + P.draw_tree + "\n";
        
        out += GAP + "on                     " + T.on + "\n";
        out += GAP + "cursor_on              " + T.cursor_on + "\n";
        out += GAP + "draw_cursors           " + C.draw_cursors + "\n";
        out += GAP + "draw_futures           " + C.draw_futures + "\n";
        
        // New features
        out += GAP + "tree_leaves            " + P.tree.getLeaves() + "\n";
        out += GAP + "tree_symmetry          " + P.tree.getSymmetry() + "\n";
        out += GAP + "draw_dino              " + P.draw_dino + "\n";
        out += GAP + "mic_active             " + P.mic.isActive() + "\n";
        out += GAP + "mic_visualization      " + P.mic.getVis() + "\n";
        out += GAP + "mic_speed              " + P.mic.getSpeed() + "\n";
        out += GAP + "mic_volume             " + P.mic.getVolume() + "\n";
        
        out += GAP + "image_file             " + P.images.name() + "\n";
        
        out += GAP + "XBranchingCursor       " + C.branching.x() + "\n";
        out += GAP + "XAlphaCursor           " + C.alpha_cursor.x() + "\n";
        out += GAP + "XBranchLengthCursor    " + C.branch_length.x() + "\n";
        out += GAP + "XTreeOrientationCursor " + C.tree_orientation.x() + "\n";
        out += GAP + "XMapCursor             " + C.map_offset.x() + "\n";
        out += GAP + "XMapRotationCursor     " + C.map_rotation.x() + "\n";
        out += GAP + "XGradintCursor         " + C.gradient.x() + "\n";
        out += GAP + "XTreeLocationCursor    " + C.tree_location.x() + "\n";
        
        out += GAP + "YBranchingCursor       " + C.branching.y() + "\n";
        out += GAP + "YAlphaCursor           " + C.alpha_cursor.y() + "\n";
        out += GAP + "YBranchLengthCursor    " + C.branch_length.y() + "\n";
        out += GAP + "YTreeOrientationCursor " + C.tree_orientation.y() + "\n";
        out += GAP + "YMapCursor             " + C.map_offset.y() + "\n";
        out += GAP + "YMapRotationCursor     " + C.map_rotation.y() + "\n";
        out += GAP + "YGradintCursor         " + C.gradient.y() + "\n";
        out += GAP + "YTreeLocationCursor    " + C.tree_location.y() + "\n";
        return out;
    }

    /** The help screen contents.
     *  Press / to show  the help screen with the current settings next to option names.
     * @param P
     * @return  */
    public static String helpString(Perceptron P) {
        Controls C = P.control;
        FractalMap F = P.fractal;
        String out = "";
        out += "Ctl @type equation         @" + C.entry_mode + "\n";
        out += "?/  @show help             @" + P.show_help + "\n";
        out += "qQ  @± fractal map         @" + F.mapping + "\n";
        out += "wW  @± outside coloring    @" + F.outi + " " + F.outop.name+"\n";
        out += "eE  @± boundary test       @" + F.bounds_i  + " " + F.bound_op.name+"\n";
        out += "r   @reflection            @" + P.buf.reflect + Samplers.reflection_mode_names[P.buf.reflect]+"\n";
        out += "R   @reverse bounds test   @" + F.invert_bound + "\n";
        out += "t   @draw tree             @" + P.draw_tree + "\n";
        out += "T   @objects on top        @" + P.objects_on_top + "\n";
        out += "yY  @± outer color (w=0)   @" + F.outcolor_i + " " + F.color_register_names[F.outcolor_i] + "\n";
        out += "u   @show frame rate       @" + P.draw_framerate + "\n";
        out += "U   @cap frame rate        @" + P.cap_frame_rate + "\n";
        out += "iI  @± input image         @" + P.image_i + " " + P.images.name() + "\n";
        out += "oO  @± translate mode      @" + F.offset_mode + " " + F.translate_modes[F.offset_mode] + "\n";
        out += "pP  @± rotate mode         @" + F.rotate_mode + " " + F.translate_modes[F.rotate_mode] + "\n";
        out += "&   @tree leaves (t=true)  @" + P.tree.getLeaves() + "\n";
        out += "*   @tree symmetry (t=true)@" + P.tree.getSymmetry() + "\n";
        out += "a   @+ audio visualizer    @" + P.mic.getVis() + " " + P.mic.vis.name + "\n";
        out += "A   @Mic enabled?          @" + P.mic.isActive() + "\n";
        out += "s   @Stegosaurus?          @" + P.draw_dino + "\n";
        out += "S   @save (shift+s)        @ \n";
        out += "dD  @± fade color damping  @" + F.color_dampen + "\n";
        out += "f   @+ grad color 1 (g>0)  @" + F.gcolor1_i + " " + F.color_register_names[F.gcolor1_i] + "\n";
        out += "F   @+ grad color 2 (g=2)  @" + F.gcolor2_i + " " + F.color_register_names[F.gcolor2_i] + "\n";
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
        out += "xX  @± tint amount         @" + F.tint_level + "\n";
        out += "C   @cursor futures        @" + C.draw_futures + "\n";
        out += "c   @show cursors          @" + C.draw_cursors + "\n";
        out += "v   @wander                @" + (C.current==null?"(none)":C.current.name+": "+C.current.wander) + "\n";
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
        out += "←→  @sharpen/blur          @" + P.blursharp_rate + '\n';
        out += "PgUp/Dn @± mic volume      @" + P.mic.getVolume() + "\n";
        out += "Home/End@± mic speed       @" + P.mic.getSpeed() + "\n";
        out += "Del   @write animation     @" + P.write_animation + "\n";
        out += "Enter @presets mode        @" + (
                C.presets_mode? "true ("+C.preset_i+"; "
                    + (C.presets.length<=0
                        ? "NONE LOADED" 
                        : ((C.preset_i>=0 && C.preset_i<C.presets.length)
                            ? C.presets[C.preset_i].name() 
                            : "OUT OF BOUNDS")
                    )
                    +")" : "false") + "\n";
        
        return out;
    }
    
    /**
     *
     * @param P
     */
    public void set(Perceptron P) {
        FractalMap   F = P.fractal;
        DoubleBuffer B = P.buf;
        TextBuffer   T = P.text;
        Controls   C = P.control;
        
        F.setMap(fractal_map);
        P.objects_on_top        = objects_on_top;
        P.rotate_images         = rotate_images;
        P.fore_grad             = fore_grad;
        P.draw_moths            = draw_moths;
        P.draw_top_bars         = draw_top_bars;
        P.draw_side_bars        = draw_side_bars;
        P.do_color_transform    = do_hue_rotation;
        P.hue_rate              = hue_rate;
        P.sat_rate              = sat_rate;
        P.lum_rate              = lum_rate;
        P.bri_rate              = bri_rate;
        P.con_rate              = con_rate;
        P.blursharp_rate        = blursharp_rate;
        P.draw_dino             = draw_dino;
        P.tree.setLeaves(tree_leaves);
        P.tree.setSymmetry(tree_symmetry);
        P.mic.setActive(mic_active);
        P.mic.setVis(mic_visualization);
        P.mic.setSpeed(mic_speed);
        P.mic.setVolume(mic_volume);
        P.setImage(image_file);
        P.setAntialias(anti_alias);
        
        F.offset_mode           = offset_mode;
        F.rotate_mode           = rotate_mode;
        F.mirror_mode           = mirror_mode;
        F.motion_blur           = motion_blur;
        F.bounds_i              = bounds_i;
        F.invert_bound          = bounds_invert;
        F.outi                  = outside_i;
        F.gslope                = grad_slope;
        F.goffset               = grad_offset;
        F.color_mask            = color_mask;
        F.feedback_mask         = feedback_mask;
        F.gcolor2               = grad_accent;
        F.grad_i                = grad_i;
        F.grad_mode             = grad_mode;
        F.gcolor1_i             = gradcolor1_i;
        F.gcolor2_i             = gradcolor2_i;
        F.barcolor_i            = barcolor_i;
        F.tintcolor_i           = tintcolor_i;
        F.tint_level            = tint_level;
        F.outcolor_i            = outcolor_i;
        F.noise_level           = noise_level;
        F.color_dampen          = color_dampen;
        F.syncOps();

        B.reflect       = reflect;
        B.interpolate   = interpolate;
        P.draw_tree     = tree_active;
        T.on            = on;
        T.cursor_on     = cursor_on;
        
        C.draw_futures  = draw_futures;
        C.setFractal(true);
        C.setTree(tree_active);
        C.setAudio(false);
        C.syncCursors();
        C.branching.set(XBranchingCursor,YBranchingCursor);
        C.alpha_cursor.set(XAlphaCursor,YAlphaCursor);
        C.branch_length.set(XBranchLengthCursor,YBranchLengthCursor);
        C.tree_orientation.set(XTreeOrientationCursor,YTreeOrientationCursor);
        C.map_offset.set(XMapCursor,YMapCursor);
        C.map_rotation.set(XMapRotationCursor,YMapRotationCursor);
        C.gradient.set(XGradintCursor,YGradintCursor);
        C.tree_location.set(XTreeLocationCursor,YTreeLocationCursor);
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
        out += GAP + "offset_mode            = " + offset_mode + "\n";
        out += GAP + "rotate_mode            = " + rotate_mode + "\n";
        out += GAP + "motion_blur            = " + motion_blur + "\n";
        out += GAP + "dampen_colors          = " + dampen_colors + "\n";
        out += GAP + "fractal_map            = " + fractal_map + "\n";
        out += GAP + "bounds_i               = " + bounds_i + "\n";
        out += GAP + "bounds_invert          = " + bounds_invert + "\n";
        out += GAP + "outside_i              = " + outside_i + "\n";
        out += GAP + "gradcolor1_i           = " + gradcolor1_i + "\n";
        out += GAP + "gradcolor2_i           = " + gradcolor2_i + "\n";
        out += GAP + "barcolor_i             = " + barcolor_i + "\n";
        out += GAP + "tintcolor_i            = " + tintcolor_i + "\n";
        out += GAP + "tint_level             = " + tint_level + "\n";
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
        out += GAP + "anti_alias             = " + anti_alias + "\n";
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
        // New features
        out += GAP + "tree_leaves            = " + tree_leaves + "\n";
        out += GAP + "tree_symmetry          = " + tree_symmetry + "\n";
        out += GAP + "draw_dino              = " + draw_dino + "\n";
        out += GAP + "mic_active             = " + mic_active + "\n";
        out += GAP + "mic_visualization      = " + mic_visualization + "\n";
        out += GAP + "mic_speed              = " + mic_speed + "\n";
        out += GAP + "mic_volume             = " + mic_volume + "\n";
        out += GAP + "image_file             = " + image_file + "\n";
        return out;
    }
} 
