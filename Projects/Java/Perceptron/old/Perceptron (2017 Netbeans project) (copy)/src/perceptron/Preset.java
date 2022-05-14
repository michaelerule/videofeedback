package perceptron;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Preset.java
 *
 * handles loading and saving of presets : the closest thing
 * perceptron has to saved files.
 * 
 *
 * Created on October 17, 2007, 11:02 PM
 *
 * @author Michael Rule
 */
public class Preset
{
    /**
     *
     */
    public boolean anti_alias         ;
    /**
     *
     */
    public boolean show_cursors       ;
    /**
     *
     */
    public boolean cursor_trails      ;
    /**
     *
     */
    public boolean velocity_mode      ;
    /**
     *
     */
    public boolean autopilot          ;
    /**
     *
     */
    public Mapping fractal_map        ;
    /**
     *
     */
    public int     edge_extend_mode   ;
    /**
     *
     */
    public int     bounding_region    ;
    /**
     *
     */
    public int     fade_color_mode    ;
    /**
     *
     */
    public int     gradient_mode      ;
    /**
     *
     */
    public int     color_accent       ;
    /**
     *
     */
    public boolean interpolation      ;
    /**
     *
     */
    public int     invert_colors      ;
    /**
     *
     */
    public int     gradient_direction ;
    /**
     *
     */
    public int     mainRenderer       ;
    /**
     *
     */
    public int     sketch             ;
    /**
     *
     */
    public boolean draw_tree          ;
    /**
     *
     */
    public boolean anti_alias_tree    ;
    /**
     *
     */
    public boolean background_objects ;
    /**
     *
     */
    public boolean cap_frame_rate     ;
    /**
     *
     */
    public boolean show_frame_rate    ;
    /**
     *
     */
    public boolean salvia_mode        ;
    /**
     *
     */
    public boolean XOR_salvia_mode    ;
    /**
     *
     */
    public boolean text_entry         ;
    /**
     *
     */
    public boolean fullscreen         ;
    /**
     *
     */
    public boolean life               ;
    /**
     *
     */
    public int     cellDrawer         ;
    /**
     *
     */
    public boolean coloredLife        ;
    /**
     *
     */
    public boolean rotateImages       ;
    /**
     *
     */
    public boolean noisy              ;
    /**
     *
     */
    public int     noise              ;
    /**
     *
     */
    public boolean folded             ;
    /**
     *
     */
    public int     folder             ;
    /**
     *
     */
    public int     blur               ;
    /**
     *
     */
    public int     ortho_type         ;
    /**
     *
     */
    public int     polar_type         ;
    /**
     *
     */
    public int     radial_gradient    ;
    /**
     *
     */
    public int     colf_num           ;
   
    /**
     *
     */
    public double XBranchingCursor       ;
    /**
     *
     */
    public double XAlphaCursor           ;
    /**
     *
     */
    public double XBranchLengthCursor    ;
    /**
     *
     */
    public double XTreeOrientationCursor ;
    /**
     *
     */
    public double XMapCursor             ;
    /**
     *
     */
    public double XMapRotationCursor     ;
    /**
     *
     */
    public double XGradintCursor         ;
    /**
     *
     */
    public double XMapAlphaCursor        ;
    /**
     *
     */
    public double XTreeLocationCursor    ;
    /**
     *
     */
    public double XCellSpeedCursor       ;
    /**
     *
     */
    public double XCellCursor            ;
    /**
     *
     */
    public double XContrastCursor        ;
    /**
     *
     */
    public double YBranchingCursor       ;
    /**
     *
     */
    public double YAlphaCursor           ;
    /**
     *
     */
    public double YBranchLengthCursor    ;
    /**
     *
     */
    public double YTreeOrientationCursor ;
    /**
     *
     */
    public double YMapCursor             ;
    /**
     *
     */
    public double YMapRotationCursor     ;
    /**
     *
     */
    public double YGradintCursor         ;
    /**
     *
     */
    public double YMapAlphaCursor        ;
    /**
     *
     */
    public double YTreeLocationCursor    ;
    /**
     *
     */
    public double YCellSpeedCursor       ;
    /**
     *
     */
    public double YCellCursor            ;
    /**
     *
     */
    public double YContrastCursor        ;
    
    /**
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static Preset parse( BufferedReader in ) throws IOException
    {
        Preset p = new Preset();
        Parser.parse( p, in );
        return p ;
    }
    
    /**
     *
     * @param percept
     * @param file
     * @throws IOException
     */
    public static void write( Perceptron percept, File file ) throws IOException
    {
        FileWriter out = new FileWriter(file);
        out.write("preset " + file.getName() + " {\n" + settings( percept ) + "}\n" );
        out.flush();
        out.close();
    }
    
    /**
     *
     * @param percept
     * @return
     */
    public static String settings( Perceptron percept )
    {
        String GAP = "     " ;
        String out = "" ;
        
        out += GAP + "anti_alias             " + percept.is_fancy()                 + "\n"  ;
        out += GAP + "show_cursors           " + percept.controls.draw_cursors      + "\n"  ;
        out += GAP + "cursor_trails          " + percept.controls.draw_futures      + "\n"  ;
        out += GAP + "velocity_mode          " + percept.fractal.auto_rotate        + "\n"  ;
        out += GAP + "autopilot              " + percept.controls.screensave        + "\n"  ;
        out += GAP + "fractal_map            " + percept.fractal.mapping            + "\n"  ;
        out += GAP + "edge_extend_mode       " + percept.fractal.offs_num           + "\n"  ;
        out += GAP + "bounding_region        " + percept.fractal.visu_num           + "\n"  ;
        out += GAP + "fade_color_mode        " + percept.fractal.fade_num           + "\n"  ;
        out += GAP + "gradient_mode          " + percept.fractal.grad_num           + "\n"  ;
        out += GAP + "color_accent           " + percept.fractal.accent_color_index + "\n"  ;
        out += GAP + "interpolation          " + percept.fractal.interpolate        + "\n"  ;
        out += GAP + "invert_colors          " + percept.fractal.grad_invert        + "\n"  ;
        out += GAP + "gradient_direction     " + percept.fractal.grad_switch        + "\n"  ;
        out += GAP + "draw_tree              " + percept.the_tree.is_active()       + "\n"  ;
        out += GAP + "anti_alias_tree        " + percept.the_tree.fancy_graphics    + "\n"  ;
        out += GAP + "background_objects     " + !percept.objects_on_top            + "\n"  ;
        out += GAP + "cap_frame_rate         " + percept.cap_frame_rate             + "\n"  ;
        out += GAP + "show_frame_rate        " + percept.frame_rate_display         + "\n"  ;
        out += GAP + "salvia_mode            " + percept.salvia_mode                + "\n"  ;
        out += GAP + "XOR_salvia_mode        " + percept.XOR_MODE                   + "\n"  ;
        out += GAP + "text_entry             " + percept.controls.ENTRY_MODE        + "\n"  ;
        //out += GAP + "life                   " + percept.life.running               + "\n"  ;
        //out += GAP + "cellDrawer             " + percept.life.renderer              + "\n"  ;
        //out += GAP + "coloredLife            " + percept.life.colored               + "\n"  ;
        out += GAP + "mainRenderer           " + percept.fractal.rndr_num           + "\n"  ;
        out += GAP + "sketch                 " + percept.sketchNum                  + "\n"  ;
        out += GAP + "rotateImages           " + percept.rotateImages               + "\n"  ;
        out += GAP + "noisy                  " + percept.fractal.noise_on           + "\n"  ;
        out += GAP + "noise                  " + percept.fractal.noise              + "\n"  ;
        out += GAP + "folded                 " + percept.convolution_on             + "\n"  ;
        out += GAP + "folder                 " + percept.fractal.filterweight       + "\n"  ;
        out += GAP + "blur                   " + percept.fractal.motionblurp        + "\n"  ;
        out += GAP + "ortho_type             " + percept.fractal.ortho_type         + "\n"  ;
        out += GAP + "colf_num               " + percept.fractal.colf_num           + "\n"  ;
        out += GAP + "polar_type             " + percept.fractal.polar_type                + "\n"  ;
        out += GAP + "radial_gradient        " + percept.fractal.gradient_number           + "\n"  ;
        out += GAP + "XBranchingCursor       " + percept.controls.XBranchingCursor()       + "\n" ;
        out += GAP + "XAlphaCursor           " + percept.controls.XAlphaCursor()           + "\n" ;
        out += GAP + "XBranchLengthCursor    " + percept.controls.XBranchLengthCursor()    + "\n" ;
        out += GAP + "XTreeOrientationCursor " + percept.controls.XTreeOrientationCursor() + "\n" ;
        out += GAP + "XMapCursor             " + percept.controls.XMapCursor()             + "\n" ;
        out += GAP + "XContrastCursor        " + percept.controls.XContrastCursor()       + "\n" ;
        out += GAP + "XMapRotationCursor     " + percept.controls.XMapRotationCursor()     + "\n" ;
        out += GAP + "XGradintCursor         " + percept.controls.XGradintCursor()         + "\n" ;
        out += GAP + "XTreeLocationCursor    " + percept.controls.XTreeLocationCursor()    + "\n" ;
        out += GAP + "YBranchingCursor       " + percept.controls.YBranchingCursor()       + "\n" ;
        out += GAP + "YAlphaCursor           " + percept.controls.YAlphaCursor()           + "\n" ;
        out += GAP + "YBranchLengthCursor    " + percept.controls.YBranchLengthCursor()    + "\n" ;
        out += GAP + "YTreeOrientationCursor " + percept.controls.YTreeOrientationCursor() + "\n" ;
        out += GAP + "YMapCursor             " + percept.controls.YMapCursor()             + "\n" ;
        out += GAP + "YContrastCursor        " + percept.controls.YContrastCursor()        + "\n" ;
        out += GAP + "YMapRotationCursor     " + percept.controls.YMapRotationCursor()     + "\n" ;
        out += GAP + "YGradintCursor         " + percept.controls.YGradintCursor()         + "\n" ;
        out += GAP + "YTreeLocationCursor    " + percept.controls.YTreeLocationCursor()    + "\n" ;
        
        return out ;
    }
    
    /**
     *
     * @param percept
     * @return
     */
    public static String display_settings( Perceptron percept )
    {
        String out = "" ;
        
        out += "?,/ @show this menu     @" + percept.show_state                 + "\n"  ;
        out += "A   @anti-alias         @" + percept.is_fancy()                 + "\n"  ;
        out += "C   @show cursors       @" + percept.controls.draw_cursors      + "\n"  ;
        out += "O   @cursor trails      @" + percept.controls.draw_futures      + "\n"  ;
        out += "V   @velocity mode      @" + percept.fractal.auto_rotate        + "\n"  ;
        out += "P   @autopilot          @" + percept.controls.screensave        + "\n"  ;
        out += "Q,W @fractal map        @" + percept.fractal.mapping            + "\n"  ;
        out += "E   @edge extend mode   @" + percept.fractal.offs_num           + "\n"  ;
        out += "R   @bounding region    @" + percept.fractal.visu_num           + "\n"  ;
        out += "F   @fade color mode    @" + percept.fractal.fade_num           + "\n"  ;
        out += "G   @gradient mode      @" + percept.fractal.grad_num           + "\n"  ;
        out += "H   @color accent       @" + percept.fractal.accent_color_index + "\n"  ;
        out += "I   @interpolation      @" + percept.fractal.interpolate        + "\n"  ;
        out += "J   @invert colors      @" + percept.fractal.grad_invert        + "\n"  ;
        out += "K   @gradient direction @" + percept.fractal.grad_switch        + "\n"  ;
        out += "T   @draw tree          @" + percept.the_tree.is_active()       + "\n"  ;
        out += "Y   @anti-alias tree    @" + percept.the_tree.fancy_graphics    + "\n"  ;
        out += "B   @background objects @" + !percept.objects_on_top            + "\n"  ;
        out += "X   @cap frame rate     @" + percept.cap_frame_rate             + "\n"  ;
        out += "Z   @show frame rate    @" + percept.frame_rate_display         + "\n"  ;
        out += "S   @salvia mode        @" + percept.salvia_mode                + "\n"  ;
        out += "D   @XOR salvia mode    @" + percept.XOR_MODE                   + "\n"  ;
        out += "Ctrl@text entry         @" + percept.controls.ENTRY_MODE        + "\n"  ;
        out += "Alt @fullscreen         @" + percept.controls.fullscreen        + "\n"  ;
         
         
        return out ;
    }
    
    /**
     *
     * @param P
     */
    public void set( Perceptron P )
    {
        P.set_fancy(                   anti_alias )       ;
        //P.controls.draw_cursors =      show_cursors       ;
        
        P.controls.draw_futures =      cursor_trails      ;
        P.fractal.auto_rotate   =      velocity_mode      ;
        //P.controls.screensave   =      autopilot          ;
        P.fractal.load_map(            fractal_map )      ;
        P.fractal.set_offscreen(       edge_extend_mode ) ;
        P.fractal.set_visualiser(      bounding_region )  ;
        P.fractal.set_fader(           fade_color_mode )  ;
        P.fractal.set_gradient(        gradient_mode )    ;
        P.fractal.set_accent(          color_accent )     ;
        P.fractal.set_interpolation(   interpolation )    ;
        P.fractal.grad_invert =        invert_colors      ;
        P.fractal.grad_switch =        gradient_direction ;
        P.controls.setTree(            draw_tree )        ;
        P.the_tree.set_fancy_graphics( anti_alias_tree )  ;
        P.objects_on_top =           ! background_objects ;
        P.cap_frame_rate =             cap_frame_rate     ;
        P.frame_rate_display =         show_frame_rate    ;
        P.salvia_mode =                salvia_mode        ;
        P.XOR_MODE =                   XOR_salvia_mode    ;
        //P.life.running =               life               ;
        P.controls.ENTRY_MODE =        text_entry         ;
        //P.life.setRenderer(            cellDrawer )       ;
        //P.life.colored =               coloredLife        ;
        P.set_sketch(                  sketch )           ;
        P.fractal.set_renderer(        mainRenderer )     ;
        P.rotateImages =               rotateImages       ;
        
        
        P.fractal.noise_on = noisy;
        P.fractal.noise = noise;
        P.convolution_on = folded  ;
        P.fractal.filterweight = folder ;
        P.fractal.set_colorfilter(colf_num);
        P.fractal.setMotionBlur(blur) ;
        P.fractal.ortho_type = ortho_type        ;
        P.fractal.polar_type = polar_type        ;
        P.fractal.set_gradient_shape(radial_gradient);

        ControlSet.Cursor x = P.controls.current_cursor;
        P.controls.current_cursor = null;
        P.controls.setBranchingCursor      (XBranchingCursor,YBranchingCursor);
        P.controls.setAlphaCursor          (XAlphaCursor,YAlphaCursor);
        P.controls.setBranchLengthCursor   (XBranchLengthCursor,YBranchLengthCursor);
        P.controls.setTreeOrientationCursor(XTreeOrientationCursor,YTreeOrientationCursor);
        P.controls.setMapCursor            (XMapCursor,YMapCursor);
        P.controls.setMapRotationCursor    (XMapRotationCursor,YMapRotationCursor);
        P.controls.setGradientCursor       (XGradintCursor,YGradintCursor);
        P.controls.setTreeLocationCursor   (XTreeLocationCursor,YTreeLocationCursor);
        P.controls.setContrastCursor       (XContrastCursor,YContrastCursor);
        //P.controls.current_cursor = x;
        //P.controls.cursorSelection(0);
        
        //if ( fullscreen ) P.make_fullscreen() ;
        //else P.make_small() ;
    }
    
}
