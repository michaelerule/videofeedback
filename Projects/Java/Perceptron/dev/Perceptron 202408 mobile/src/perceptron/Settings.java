package perceptron;

import rendered.TextMatrix;
import image.DoubleBuffer;
import image.Samplers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

/**
 * Preset.java
 * Created on October 17, 2007, 11:02 PM
 * @author Michael Rule
 */
/** Read the preset and load the parameters. */
public class Settings {
    final static String [] gradient_names  = {
        "Circle","Horizontal","Vertical","Diagonal",
        "Diamond","Cross","Ring","Sharp Ring","Eye"};

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
    public boolean capture_text    = true;
    public boolean tree_leaves     = false;
    public boolean tree_symmetry   = true;
    public boolean draw_dino       = false;
    public boolean draw_grid       = false;
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
    private Settings(String name) {this.name = name;}
    public String name() {return name;}

    /** Read the preset.
     * @param name
     * @param in
     * @return
     */
    public static Settings parse(String name, BufferedReader in) {
        Settings p = new Settings(name);
        try {
            Parse.parse(p, in);
        } catch (IOException e) {
            System.err.println("Error parsing preset "+name);
        }
        return p;
    }
    public static Settings parse(String state) {
        return parse("(anonymous)",new BufferedReader(new StringReader(state)));
    }
    

    /** Write the preset.
     * @param percept
     * @param file
     * @throws java.io.IOException */
    public static void write(Perceptron percept, File file) throws IOException {
        try (FileWriter out = new FileWriter(file)) {
            out.write("preset " + file.getName() + " {\n" + settings(percept) + "}\n");
            out.flush();
        }
    }

    /** Save the current state into a file (or input the settings in as they 
     * are given here) from the preset. 
     * @param P
     * @return  */
    public static String settings(Perceptron P) {
        Map          F = P.map;
        DoubleBuffer B = P.buf;
        TextMatrix   T = P.text;
        Control      C = P.control;
        String GAP = "      ";
        String out = "";
        out += GAP + "objects_on_top         " + P.objects_on_top + "\n";
        //out += GAP + "cap_frame_rate         " + P.cap_frame_rate + "\n";
        out += GAP + "rotate_images          " + P.rotate_images + "\n";
        out += GAP + "fore_grad              " + P.fore_tint + "\n";
        
        out += GAP + "draw_moths             " + P.draw_moths + "\n";
        out += GAP + "draw_top_bars          " + P.draw_top_bars + "\n";
        out += GAP + "draw_side_bars         " + P.draw_side_bars + "\n";
        out += GAP + "do_hue_rotation        " + P.do_color_transform + "\n";
        out += GAP + "capture_text           " + P.capture_text + "\n";
        
        out += GAP + "hue_rate               " + P.hue_rate + "\n";
        out += GAP + "sat_rate               " + P.sat_rate + "\n";
        out += GAP + "lum_rate               " + P.lum_rate + "\n";
        out += GAP + "bri_rate               " + P.bri_rate + "\n";
        out += GAP + "con_rate               " + P.con_rate + "\n";
        out += GAP + "blursharp_rate         " + P.blursharp_rate + "\n";
        
        out += GAP + "offset_mode            " + F.offset_mode + "\n";
        out += GAP + "rotate_mode            " + F.rotate_mode + "\n";
        out += GAP + "mirror_mode            " + F.mirror_mode + "\n";
        out += GAP + "motion_blur            " + F.motion_blur + "\n";
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
        out += GAP + "grad_offset            " + F.gbias + "\n";
        out += GAP + "color_mask             " + F.color_mask + "\n";
        out += GAP + "feedback_mask          " + F.feedback_mask + "\n";
        out += GAP + "grad_accent            " + F.gc2 + "\n";
        out += GAP + "grad_i                 " + F.grad_i + "\n";
        out += GAP + "grad_mode              " + F.grad_mode + "\n";
        out += GAP + "reflect                " + B.reflect + "\n";
        out += GAP + "interpolate            " + B.interpolate + "\n";
        out += GAP + "anti_alias             " + B.antialiased + "\n";
        out += GAP + "tree_active            " + P.draw_tree + "\n";
        
        out += GAP + "on                     " + T.on + "\n";
        out += GAP + "cursor_on              " + T.cursor_on + "\n";
        out += GAP + "draw_cursors           " + C.draw_cursors + "\n";
        out += GAP + "draw_futures           " + C.draw_futures + "\n";
        
        // New features
        out += GAP + "tree_leaves            " + P.tree.getLeaves() + "\n";
        out += GAP + "tree_symmetry          " + P.tree.getSymmetry() + "\n";
        out += GAP + "draw_dino              " + P.draw_dino + "\n";
        out += GAP + "draw_grid              " + P.draw_grid + "\n";
        out += GAP + "mic_active             " + P.mic.isActive() + "\n";
        out += GAP + "mic_visualization      " + P.mic.getVis() + "\n";
        out += GAP + "mic_speed              " + P.mic.getSpeed() + "\n";
        out += GAP + "mic_volume             " + P.mic.getVolume() + "\n";
        
        out += GAP + "image_file             " + P.images.name() + "\n";
        
        out += GAP + "XBranchingCursor       " + C.branching.x() + "\n";
        //out += GAP + "XAlphaCursor           " + C.alpha_cursor.x() + "\n";
        out += GAP + "XBranchLengthCursor    " + C.branch_length.x() + "\n";
        //out += GAP + "XTreeOrientationCursor " + C.tree_orientation.x() + "\n";
        out += GAP + "XMapCursor             " + C.map_offset.x() + "\n";
        out += GAP + "XMapRotationCursor     " + C.map_rotation.x() + "\n";
        out += GAP + "XGradintCursor         " + C.gradient.x() + "\n";
        out += GAP + "XTreeLocationCursor    " + C.tree_location.x() + "\n";
        
        out += GAP + "YBranchingCursor       " + C.branching.y() + "\n";
        //out += GAP + "YAlphaCursor           " + C.alpha_cursor.y() + "\n";
        out += GAP + "YBranchLengthCursor    " + C.branch_length.y() + "\n";
        //out += GAP + "YTreeOrientationCursor " + C.tree_orientation.y() + "\n";
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
        Control C = P.control;
        Map F = P.map;        
        /*
        ⌘ Command (Cmd) U+2318
        ⌥ Option (Opt or Alt) U+2325
        ⌃ Control (Ctrl) U+2303
        ⇧ Shift U+21E7
        ⇪ Caps Lock U+21EA
        ↩ Return U+21A9
        ⌤ Enter U+2324
        ⌫ Delete (Backspace) U+232B
        ⌦ Forward Delete U+2326
        ⎋ Escape (Esc) U+238B
        ⏏ Eject U+23CF
        ⌽ Power U+2333D
        ⇥ Tab U+21E5
        ⇞ Page Up U+21DE
        ⇟ Page Down U+21DF
        ↖ Home U+2196
        ↘ End U+2198
        */
        String s = C.current==null? "":C.current.name;
        
        return
        "─┤ PERCEPTRON ├────────────────"
        + "\n Left  Click   next cursor"
        + "\n Right Click   previous cursor"
        + "\n ⌃q            quit"
        + "\n ⌃s  or S      save"
        + "\n ` or Space    play/pause"
        + "\n alt+s         write video (" + P.write_animation + ")"
        + "\n"
        + "\n qQ  @±fractal map     @" + F.mapping
        + "\n wW  @±outside         @" + F.outi + " " + F.outop.name
        + "\n eE  @±boundary test   @" + F.bounds_i  + " " + F.bound_op.name
        + "\n rR  @reflection       @" + P.buf.reflect + " " + Samplers.reflection_mode_names[P.buf.reflect]
        + "\n ^R  @reverse bounds   @" + F.invert_bound
        + "\n yY  @±out color       @" + F.outcolor_i + " " + F.color_register_names[F.outcolor_i]
        + "\n iI  @±input image     @" + P.image_i + " " + P.images.name()
        + "\n \\  @auto image       @" + P.rotate_images
        + "\n j   @invert output    @" + F.color_mask
        + "\n J   @invert input     @" + F.feedback_mask
        + "\n oO  @±offset          @" + F.offset_mode + " " + F.translate_modes[F.offset_mode]
        + "\n pP  @±rotate          @" + F.rotate_mode + " " + F.translate_modes[F.rotate_mode]
        + "\n mM  @mirror           @" + F.mirror_mode + " " + F.mirror_modes[F.mirror_mode]
        + "\n ↩   @presets          @" + (
                C.presets_mode? "true ("+C.preset_i+"; "
                    + (C.presets.length<=0
                        ? "NONE LOADED" 
                        : ((C.preset_i>=0 && C.preset_i<C.presets.length)
                            ? C.presets[C.preset_i].name() 
                            : "OUT OF BOUNDS")
                    )
                    +")" : "false")
        + "\n 0-9 Fn 1-12: built-in presets"
                
        + "\n"//+ + "\n─┤ OBJECTS ├───────────────────"
        + "\n T   @objects atop     @" + P.objects_on_top
        + "\n t   @draw tree        @" + P.draw_tree
        + "\n &   @tree leaves      @" + P.tree.getLeaves()
        + "\n *   @tree down        @" + P.tree.getSymmetry()
        + "\n s   @stegosaurus      @" + P.draw_dino
        + "\n ^g  @coordinates      @" + P.draw_grid
        + "\n ⌃⌥M @moths            @" + P.draw_moths
        + "\n _   @horizon edge     @" + P.draw_top_bars
        + "\n |   @side edge        @" + P.draw_side_bars
        + "\n []  @±edge color      @" + F.barcolor_i + " " + F.color_register_names[F.barcolor_i]
                
        + "\n"//+ + "\n─┤ TINTING ├───────────────────"
        + "\n gG  @±grad            @" + F.grad_mode + " " + F.grad_op.name 
        + "\n f   @+color 1 (g1)    @" + F.gcolor1_i + " " + F.color_register_names[F.gcolor1_i]
        + "\n F   @+color 2 (g2)    @" + F.gcolor2_i + " " + F.color_register_names[F.gcolor2_i]
        + "\n dD  @±color dampen    @" + F.color_dampen
        + "\n hH  @±grad shape      @" + F.grad_i + " " + gradient_names[F.grad_i]
        + "\n k   @grad within      @" + P.fore_tint
        + "\n zZ  @±tint color      @" + F.tintcolor_i + " " + F.color_register_names[F.tintcolor_i]
        + "\n xX  @±tint            @" + F.tint_level
                
        + "\n"//+ "\n─┤ COLOR ├─────────────────────"
        + "\n K   @color adjust     @" + P.do_color_transform
        + "\n ;\' @±Δhue            @" + P.hue_rate
        + "\n :\" @±Δsaturate       @" + P.sat_rate
        + "\n ,.  @±Δcontrast       @" + P.con_rate
        + "\n <>  @±Δbright         @" + P.bri_rate
        + "\n ()  @±noise           @" + F.noise_level
        + "\n ↑↓  @±motionblur      @" + F.motion_blur
        + "\n ←→  @sharp/blur       @" + P.blursharp_rate
                
        + "\n"//+ + "\n─┤ CURSOR ├────────────────────" 🭼_
        + "\n ⌃m  @hide mouse       @" + P.hide_mouse
        + "\n c   @show cursors     @" + C.draw_cursors
        + "\n ^⇧C @feedback cursors @" + P.capture_cursors
        + "\n ^p  @park cursors     @" + C.parked
        + "\n ^v  @wander           @" + (C.current==null?"(no cursor)":s.substring(0,s.length()-4)+": "+C.current.wander)
        + "\n ^⇧V @autopilot        @" + C.screensaver
        + "\n +-  @±speed           @" + (C.current==null? "(no cursor)" : C.current.speed)
        + "\n C   @dots             @" + C.draw_futures
        + "\n {}  @±dots            @" + (C.current==null? "(no cursor)" : C.current.nDots())
                
        + "\n"//+ + "\n─┤ AUDIO ├─────────────────────"
        + "\n A   @Mic on           @" + P.mic.isActive()
        + "\n a   @+visualizer      @" + P.mic.getVis() + " " + P.mic.vis.name
        + "\n ⇞⇟  @±volume          @" + P.mic.getVolume()
        + "\n ↖↘  @±mic speed       @" + P.mic.getSpeed()
                
        + "\n"//+ + "\n─┤ TEXT ├──────────────────────"
        + "\n ⇥   @type equation    @" + C.text_mode
        + "\n b   @show text        @" + P.text.on
        + "\n B   @text cursor      @" + P.text.cursor_on
        + "\n ⌃⇧T @feedback text    @" + P.capture_text
        + "\n ⌃↩ or ⇧↩ @@text → equation"
                
        + "\n"//+ + "\n─┤ WINDOW ├────────────────────"
        + "\n ?/  @show help        @" + P.show_state
        + "\n n   @toggle screencap @" + P.capture_screen
        + "\n N   @show notices     @" + P.show_notices
        + "\n u   @show info        @" + P.show_monitor
        + "\n U   @limit speed      @" + P.cap_frame_rate
        + "\n l   @interpolate      @" + P.buf.interpolate
        + "\n L   @anti-alias       @" + P.isAntialiased()
        + "\n ⌃f  @fullscreen       @" + P.isFullscreen()
        + "\n ⌃e  @edges (¬f)       @" + !P.isUndecorated()
                ;
    }
    
    /** 
     * Apply given preset to perceptron.
     * @param P
     */
    public void set(Perceptron P) {
        Map          F = P.map;
        DoubleBuffer B = P.buf;
        TextMatrix   T = P.text;
        Control      C = P.control;
        
        F.setMap(fractal_map);
        
        P.objects_on_top        = objects_on_top;
        P.capture_text          = capture_text;
        P.rotate_images         = rotate_images;
        P.fore_tint             = fore_grad;
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
        F.gbias                 = grad_offset;
        F.color_mask            = color_mask;
        F.feedback_mask         = feedback_mask;
        F.gc2                   = grad_accent;
        //F.grad_i                = grad_i;
        F.grad_mode             = grad_mode;
        F.gcolor1_i             = gradcolor1_i;
        F.gcolor2_i             = gradcolor2_i;
        F.barcolor_i            = barcolor_i;
        F.tintcolor_i           = tintcolor_i;
        F.tint_level            = tint_level;
        F.outcolor_i            = outcolor_i;
        F.noise_level           = noise_level;
        F.color_dampen          = color_dampen;
        F.setGradientShape(grad_i);
        F.syncOps();

        B.reflect       = reflect;
        B.interpolate   = interpolate;
        P.draw_tree     = tree_active;
        T.on            = on;
        T.cursor_on     = cursor_on;
        
        C.draw_futures  = draw_futures;
        C.setTree(tree_active);
        C.syncCursors();
        try{C.branching.set(XBranchingCursor,YBranchingCursor);} catch (Exception e){}
        //try{C.alpha_cursor.set(XAlphaCursor,YAlphaCursor);} catch (Exception e){}
        try{C.branch_length.set(XBranchLengthCursor,YBranchLengthCursor);} catch (Exception e){}
        //try{C.tree_orientation.set(XTreeOrientationCursor,YTreeOrientationCursor);} catch (Exception e){}
        try{C.map_offset.set(XMapCursor,YMapCursor);} catch (Exception e){}
        try{C.map_rotation.set(XMapRotationCursor,YMapRotationCursor);} catch (Exception e){}
        try{C.gradient.set(XGradintCursor,YGradintCursor);} catch (Exception e){}
        try{C.tree_location.set(XTreeLocationCursor,YTreeLocationCursor);} catch (Exception e){}
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
        out += GAP + "capture_text           = " + capture_text + "\n";
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
