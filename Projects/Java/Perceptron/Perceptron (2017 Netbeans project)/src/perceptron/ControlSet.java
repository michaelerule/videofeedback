package perceptron;

import util.Matrix;
import util.ColorUtility;
import rendered3D.Tree3D;
import math.complex;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.io.File;
import javax.imageio.ImageIO;
import static java.awt.event.KeyEvent.* ;

/** CursorSet.java
 * Created on January 18, 2007, 2:55 AM
 * @author Michael Everett Rule
 */

public class ControlSet implements MouseListener, MouseMotionListener, KeyListener
{
    //THESE OBJECTS ARE CONTROLLED AND MODIFIED BY THIS CLASS
    
    /** The Perceptron to which to bind Cursors */
    Perceptron percept;
    
    /** The RenderingKernel to which to bind Cursors */
    RenderingKernel fractal;
    
    /** The Tree3D to which to bind Cursors */
    Tree3D tree;
    
    /** The AudioInput to which to bind Cursors */
    //AudioInput audio;
    
    //MOUSE DATA
    
    //A robot to move the mouse to the location of selected cursors
    Robot robot;
    
    //known mouse location
    Point mouse_location = new Point(0, 0);
    
    //CURSOR DATA
    
    /** The rate at which the cursor position responds to mouse events. If
     *  this is set to one the cursor will follow the mouse exactly. If it
     *  is set to small values less than one, greater than zero the cursors
     *  will drift slowly to follow the mouse.
     */
    private float rate = 4f;
    
    //Scale from physical screen dimension to virtual screen dimensions
    float x_scalar, y_scalar;
    
    //CURSORS
    Cursor
            BranchingCursor,
            AlphaCursor,
            BranchLengthCursor,
            TreeOrientationCursor,
            MapCursor,
            MapRotationCursor,
            GradientCursor,
            ContrastCursor,
            TreeLocationCursor
            ;
    
    //active cursor
    Cursor current_cursor;
    
    //weather or not to draw the cursors
    boolean draw_cursors = true;
    
    //Specific sets of cursors
    Cursor [] MapCursors, TreeCursors, AudioCursors, LifeCursors ;
    
    //Set of active cursors (cursors become active, inactive based on the
    //activation state of the controlled objects
    ArrayList active_cursors;
    ArrayList all_cursors;
    
    //PRESETS
    Preset [] presets ;
    
    BufferedImage cursor_ring = create_cursor_ring() ;
    
    boolean draw_futures ;
    
    ///CONSTRUCTOR
    /**
     *
     * @param Cursor_percept
     * @param user_presets
     */
    public ControlSet(
            Perceptron Cursor_percept,
            Preset [] user_presets )
    {
        presets = user_presets ;
        
        percept = Cursor_percept;
        fractal = percept.fractal;
        tree    = percept.the_tree;
        
        x_scalar = (float)percept.screen_width()  / percept.physical_width()  ;
        y_scalar = (float)percept.screen_height() / percept.physical_height() ;
        
        //TRY TO INITIALIZE A ROBOT TO MOVE THE CURSOR
        try
        {
            robot = new Robot();
        }
        catch (Exception e)
        {
            System.err.println("Could not initialise a robot.");
        }
        initialise_cursors();
    }
    
    boolean fullscreen = true ;
    
    /** Cursor initialisation */
    @SuppressWarnings("static-access")
    public void initialise_cursors()
    {
        BranchingCursor = new Cursor("c1.png")
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location(rate);
                float a1 = (float)(2 * x / percept.screen_width()  - 1) * complex.two_pi ;
                float a2 = (float)(2 * y / percept.screen_height() - 1) * complex.two_pi ;
                tree.form[0].set_beta( a1 + a2/2 );
                tree.form[1].set_beta( a1 - a2/2 );
            }
        };
        
        MapRotationCursor =  new Cursor("c2.jpg" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location( rate );
                fractal.set_normalized_rotation( x , y );
            }
        };
        MapRotationCursor.destination.x = percept.screen_width()/2 ;//(int)(fractal.W - fractal.complex_to_W_scalar - RenderingKernel.upper_left.real);
        MapRotationCursor.destination.y = percept.screen_height()/2 ;
                
        MapCursor =  new Cursor("c7.png" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location( rate ) ;
                fractal.set_norm_c(x,y);
            }
        };
        
        TreeLocationCursor =  new Cursor("c5.png" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location(rate);
                tree.location.x = (int)( .5 + x ) ;
                tree.location.y = (int)( .5 + y ) ;
            }
            //public void drawTo( Graphics G ) {
            //    if ( current_cursor == this )  super.drawTo(G) ;
            //}
        };
        
        AlphaCursor = new Cursor("c4.png" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location(rate);
                
                float x_o = x - percept.screen_width()/2 ;
                float y_o = y - percept.screen_height()/2;
                
                if (x_o * x_o + y_o * y_o < 5)
                {
                    tree.form[0].set_alpha(0);
                    tree.form[1].set_alpha(0);
                }
                else
                {
                    float a1 = (float)(x / percept.screen_width()/2  - 1) * complex.two_pi ;
                    float a2 = (float)(y / percept.screen_height()/2 - 1) * complex.two_pi ;
                    tree.form[0].set_alpha( a1 + a2/2 );
                    tree.form[1].set_alpha( a1 - a2/2 );
                }
            }
        };
        
        BranchLengthCursor = new Cursor("c3.png" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location(rate);
                tree.form[0].d_r = (x / percept.screen_width()  - .5f) * .5f + .7f;
                tree.form[1].d_r = (y / percept.screen_height() - .5f) * .5f + .7f;
            }
        };
        
        TreeOrientationCursor = new Cursor("iupiter.jpg" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location(rate);
                tree.Y = Matrix.rotation(3,0,2,(float)(x / percept.screen_width()/2  - 1) * complex.pi);
                tree.X = Matrix.rotation(3,1,2,(float)(y / percept.screen_height()/2 - 1) * complex.pi);
            }
        };

        GradientCursor = new Cursor("c8.jpg" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location(rate);
                fractal.set_gradient_param(
                        (float) Math.pow( 2. * x / percept.screen_width , 2 ),
                        (float) y / percept.screen_height * 256 - 128 );
            }
        };

        ContrastCursor = new Cursor("dragonfly.jpg" )
        {
            @Override
            public void advance_location(float rate)
            {
                super.advance_location(rate);
                int X = 0xff&(int)(0xff * x / percept.screen_width ) ;
                int Y = 0xff&(int)(0xff * y / percept.screen_height) ;
                fractal.setContrastParameters(X,Y) ;
            }
        };
        
        MapCursors   = new Cursor[] { MapCursor, GradientCursor, MapRotationCursor, ContrastCursor };
        TreeCursors  = new Cursor[] { TreeOrientationCursor , BranchingCursor , AlphaCursor , BranchLengthCursor , TreeLocationCursor };
        AudioCursors = new Cursor[] {};
        
        active_cursors = new ArrayList();
        all_cursors = new ArrayList();
        if (fractal.is_active()) for (Cursor c : MapCursors  ) active_cursors.add(c);
        System.out.println(tree);
        System.out.println(TreeCursors);
        System.out.println(active_cursors);
        for (Cursor c : TreeCursors ) System.out.println(c);
        if (tree!=null) if (tree   .is_active()) for (Cursor c : TreeCursors ) active_cursors.add(c);
        //if (audio  .is_active()) for (Cursor c : AudioCursors) active_cursors.add(c);
        
        for (Cursor [] set : new Cursor[][]{ MapCursors,TreeCursors,AudioCursors})
            for (Cursor c : set  ) all_cursors.add(c);
            
        //Start with the map cursor
        current_cursor = MapCursor;
        draw_futures   = true ;
    }
    
    
    /** Tests to see if a point is offscreen, and makes a correction if so */
    private Point bound(Point p)
    {
        if (p.x < 0) p.x = 0;
        else if (p.x >= percept.screen_width()) p.x = percept.screen_width();
        if (p.y < 0) p.y = 0;
        else if (p.y >= percept.screen_height()) p.y = percept.screen_height();
        return p;
    }
    
    /** Tells all the cursors to safely update to the same mouseEvent.
     * @param e
     */
    public void moveAll(MouseEvent e)
    {
        for (int i = 0; i < active_cursors.size(); i++)
        {
            Cursor c = (Cursor)(active_cursors.get(i));
            c.destination = bound(e.getPoint());
        }
    }
    
    /** increment cursors toward targets */
    int[] CODES = new int[]{ VK_Q, VK_W };
                
    /**
     *
     * @param framerate
     */
    public void advance(int framerate)
    {
        if (screensave) {
            for (int i = 0; i < active_cursors.size(); i++) ((Cursor)(active_cursors.get(i))).walk();
        }
        if ( active_cursors.size() <= 0 ) return ;
        if ( framerate < 1 ) framerate = 1 ;
        float focus_drift   = (float)Math.min( 1 , 1 - 1 / ( 1 + rate / framerate ) );
        float unfocus_drift = (float)Math.min( 1 , 1 - 1 / ( 1 + .4 * rate / framerate ) );
        
        if (current_cursor != null && !screensave) {
            current_cursor.advance_location(focus_drift);
        }
        
        for (int i = 0; i < all_cursors.size(); i++)
        {
            Cursor c = (Cursor)(all_cursors.get(i));
            c.advance_location(unfocus_drift);
        }
    }
    
    /** Handle a cursor activation event : move to next or previous cursor*/
    void cursorSelection(int step)
    {
        //if there are no active cursors do nothing
        if (active_cursors.size() <= 0)
        {
            current_cursor = null;
            return;
        }
        
        int index = active_cursors.indexOf(current_cursor);
        if (index < 0) index = 0;
        else
        {
            int size = active_cursors.size();
            index = index + step + size;
            if (index < 0) index = size - 1 - (-index % size);
            else if (index >= size) index %= size;
        }
        
        current_cursor = (Cursor)(active_cursors.get(index));
        
        //update mouse location to that of the new cursor, if possible
        if (robot != null)
        {
            int x = (int)(current_cursor.destination.x / x_scalar);
            if (x < 0) x = 0;
            if (x > percept.physical_width()) x = percept.physical_width();
            int y = (int)(current_cursor.destination.y / y_scalar);
            if (y < 0) y = 0;
            if (y > percept.physical_height()) y = percept.physical_height();
            robot.mouseMove(x,y);
        }
    }
    
    private void check_cursor()
    {
        if (!active_cursors.contains(current_cursor))
        {
            if (active_cursors.size() <= 0)
            {
                current_cursor = null;
            }
            else
            {
                current_cursor = (Cursor)(active_cursors.get(0));
            }
        }
    }
    
    /** Draw all active controls
     * @param G graphics drawing context
     */
    public void drawAll(Graphics G)
    {
        //if there are no active cursors do nothing
        if (active_cursors.size() <= 0 || !draw_cursors) return;
        for (int i = 0; i < active_cursors.size(); i++) ((Cursor)(active_cursors.get(i))).drawTo(G);
        
        if ( !screensave ) {
            G.drawImage( cursor_ring , (int)(mouse_location.x) - 7, (int)(mouse_location.y) - 7 , null ) ;
            if (current_cursor != null ) {
                G.drawImage( focus , (int)current_cursor.x -11, (int)current_cursor.y -11, null ) ;
            }
        }
    }
    
    BufferedImage create_cursor_ring()
    {
        BufferedImage b = new BufferedImage(14,14,BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
        image_graphics.setColor(Color.WHITE);
        image_graphics.drawOval(2, 2, 10, 10);
        return b;
    }
    
    /** Toggles screensaver mode */
    boolean screensave = false;
    /**
     *
     */
    public void toggle_screensaver_mode()
    {
        screensave = !screensave;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //MOUSE LISTENER IMPLEMENTATION
    
    /**
     *
     * @param e
     */
    public void mouseEntered(MouseEvent e)
    {
        if (current_cursor==null) return;
        //update mouse location to that of the cursor, if possible
        if (robot != null)
        {
            int x = (int)(current_cursor.x / x_scalar);
            if (x < 0) x = 0;
            if (x > percept.physical_width()) x = percept.physical_width();
            int y = (int)(current_cursor.y / y_scalar);
            if (y < 0) y = 0;
            if (y > percept.physical_height()) y = percept.physical_height();
            robot.mouseMove(x,y);
        }
    }

    /**
     *
     * @param e
     */
    public void mouseExited(MouseEvent e)
    {
    }

    /**
     *
     * @param e
     */
    public void mousePressed(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1)
            cursorSelection(1);
        else if (e.getButton() == MouseEvent.BUTTON3)
            cursorSelection(-1);
    }
    /**
     *
     * @param e
     */
    public void mouseReleased(MouseEvent e)
    {}
    /**
     *
     * @param e
     */
    public void mouseClicked(MouseEvent e)
    {}

    static final int MOUSE_STOP = 1;
    /**
     *
     * @param e
     */
    public void mouseMoved(MouseEvent e)
    {
        if (e.getY()<MOUSE_STOP)
            robot.mouseMove(e.getX(),MOUSE_STOP);
        if (active_cursors.size() <= 0) return;
        mouse_location = bound(new Point(
                (int)(x_scalar*e.getX()) ,
                (int)(y_scalar*e.getY()) ));
        if (current_cursor!=null)
        current_cursor.mouseMoved(e);
    }
    
    /**
     *
     * @param e
     */
    public void mouseDragged(MouseEvent e)
    {
        mouseMoved(e);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //KEY LISTENER IMPLEMENTATION
    
    boolean ENTRY_MODE = false ;
    boolean PRESETS_MODE = false ;
    /**
     *
     * @param e
     */
    public void keyPressed(KeyEvent e)
    {
        /////////percept.stimulate();
        //System.out.println("WHAT THE HELL?");
        //percept.textlayer.keyPressed(e);
        //if (true) return;
        
        if ( ENTRY_MODE ) switch( e.getKeyCode() )
        {
/*          case VK_LEFT       : percept.BUFFER_LEFT()        ; break ;
            case VK_RIGHT      : percept.BUFFER_RIGHT()       ; break ;
            case VK_UP         : percept.BUFFER_UP()          ; break ;
            case VK_DOWN       : percept.BUFFER_DOWN()        ; break ;
            case VK_BACK_SPACE : percept.BUFFER_BACKSPACE()   ; break ;
            case VK_ENTER      : percept.BUFFER_TO_MAP()      ; break ;
            case VK_PAGE_UP    : percept.BUFFER_SCROLL_UP()   ; break ;
            case VK_PAGE_DOWN  : percept.BUFFER_SCROLL_DOWN() ; break ;*/
            case VK_ESCAPE     : System.exit(0)               ; break ;
            case VK_CAPS_LOCK  :                              ; break ;
            case VK_SHIFT      :                              ; break ;
            //case VK_TAB        : percept.toggle_cursor()      ; break ;
            case VK_ALT        :      ; break ;
            //case VK_CONTROL    : ENTRY_MODE = percept.CURSOR_ON = false; break ;
            //default : percept.append_to_buffer( e.getKeyChar() )      ; return ;
        }
        else if ( PRESETS_MODE ) {
            System.out.println("Interpreting keypress in presets mode");
            switch( e.getKeyCode() )
        {
            case VK_ESCAPE : System.exit(0)                            ; break ;
            case VK_STOP   : System.exit(0)                            ; break ;
            
            case VK_0 : preset(0)                                ; break ;
            case VK_1 : preset(1)                                ; break ;
            case VK_2 : preset(2)                                ; break ;
            case VK_3 : preset(3)                                ; break ;
            case VK_4 : preset(4)                                ; break ;
            case VK_5 : preset(5)                                ; break ;
            case VK_6 : preset(6)                                ; break ;
            case VK_7 : preset(7)                                ; break ;
            case VK_8 : preset(8)                                ; break ;//#
            case VK_9 : preset(9)                                ; break ;
            case VK_A : preset(10)                               ; break ;
            case VK_B : preset(11)                               ; break ;
            case VK_C : preset(12)                               ; break ;
            case VK_D : preset(13)                               ; break ;
            case VK_E : preset(14)                               ; break ;
            case VK_F : preset(15)                               ; break ;
            case VK_G : preset(16)                               ; break ;//#
            case VK_H : preset(17)                               ; break ;
            case VK_I : preset(18)                               ; break ;
            case VK_J : preset(19)                               ; break ;
            case VK_K : preset(20)                               ; break ;
            case VK_L : preset(21)                               ; break ;
            case VK_M : preset(22)                               ; break ;//
            case VK_N : preset(23)                               ; break ;
            case VK_O : preset(24)                               ; break ;
            case VK_P : preset(25)                               ; break ;
            case VK_Q : preset(26)                               ; break ;
            case VK_R : preset(27)                               ; break ;
            case VK_S : preset(28)                               ; break ;
            case VK_T : preset(29)                               ; break ;
            case VK_U : preset(30)                               ; break ;
            case VK_V : preset(31)                               ; break ;
            case VK_W : preset(32)                               ; break ;
            case VK_X : preset(33)                               ; break ;
            case VK_Y : preset(34)                               ; break ;
            case VK_Z : preset(35)                               ; break ;
            case VK_F1: preset(36)                               ; break ;
            case VK_F2: preset(37)                               ; break ;
            case VK_F3: preset(38)                               ; break ;
            case VK_F4: preset(39)                               ; break ;
            case VK_F5: preset(40)                               ; break ;
            case VK_F6: preset(41)                               ; break ;
            case VK_F7: preset(42)                               ; break ;
            case VK_F8: preset(43)                               ; break ;
            case VK_F9: preset(44)                               ; break ;
            case VK_F10: preset(45)                              ; break ;
            case VK_F11: preset(46)                              ; break ;
            case VK_F12: preset(47)                              ; break ;
            
            case VK_BACK_QUOTE    : preset(48)                   ; break ;
            case VK_QUOTE         : preset(49)                   ; break ;//#
            case VK_OPEN_BRACKET  : preset(50)                   ; break ;
            case VK_CLOSE_BRACKET : preset(51)                   ; break ;
            case VK_EQUALS        : preset(52)                   ; break ;
            case VK_MINUS         : preset(53)                   ; break ;
            case VK_SLASH         : preset(54)                   ; break ;
            case VK_BACK_SPACE    : preset(55)                   ; break ;
            case VK_COMMA         : preset(56)                   ; break ;
            case VK_PERIOD        : preset(57)                   ; break ;
            case VK_SPACE         : preset(58)                   ; break ;//
            case VK_SEMICOLON     : preset(59)                   ; break ;
            case VK_BACK_SLASH    : preset(60)                   ; break ;
            
            case VK_ENTER      : PRESETS_MODE = false  ; break ;
            
            default :                                                   return ;
        }
        }
        else 
        switch( e.getKeyCode() )
        {
            //DISABLED FOR PRESENTATION : RE-ENABLE LATER
            case VK_ESCAPE : System.exit(0)                            ; break ;
            case VK_BACK_QUOTE : percept.save()                        ; break ;
  //          case VK_SPACE : percept.running = !percept.running                         ; break ;

//            case VK_TAB : percept.toggle_cursor()                      ; break ;
            case VK_Q : fractal.increment_map(1)                       ; break ;
            case VK_W : fractal.increment_map(-1)                      ; break ;
            case VK_E : fractal.increment_offscreen(1)                 ; break ;
            case VK_R : fractal.increment_visualiser(1)                ; break ;
            case VK_T : setTree(!tree.is_active())                     ; break ;
            case VK_Y : tree.toggle_fancy_graphics()                   ; break ;
            case VK_U : percept.buffer.toggleReflect()                 ; break ;
            case VK_I : fractal.toggle_interpolation()                 ; break ;
            case VK_O : draw_futures = !draw_futures                   ; break ;
            case VK_P : percept.increment_sketch(1)                    ; break ;
            case VK_OPEN_BRACKET : current_cursor.add_dot()            ; break ;
            case VK_CLOSE_BRACKET : current_cursor.remove_dot()        ; break ;
            case VK_BACK_SLASH : fractal.toggle_noise()                ; break ;

            case VK_CAPS_LOCK : percept.toggle_convolution()           ; break ;
            //case VK_A :                                                ; break ;
            //case VK_S : percept.toggle_salvia_mode()                   ; break ;
            //case VK_D : percept.rotate_automota()                      ; break ;
            case VK_F : fractal.increment_fader(1)                     ; break ;
            case VK_G : fractal.increment_gradient(1)                  ; break ;
            case VK_H : fractal.increment_accent(1)                    ; break ;
            case VK_J : fractal.toggle_color_inversion()               ; break ;
            case VK_K : fractal.toggle_gradient_switch()               ; break ;
            case VK_L : fractal.increment_renderer(1)                  ; break ;
            case VK_SEMICOLON : fractal.dampcolor =!fractal.dampcolor  ; break ;
            case VK_QUOTE : fractal.increment_gradient_shape(1)        ; break ;
            case VK_ENTER : PRESETS_MODE = true                        ; break ;
            case VK_SPACE : PRESETS_MODE = true                        ; break ;

            case VK_Z : percept.toggle_frame_rate_display()            ; break ;
            case VK_X : fractal.increment_colorfilter(1)               ; break ;
            case VK_C : draw_cursors = !draw_cursors                   ; break ;
            case VK_V :                                                ; break ;
            case VK_B :                                                ; break ;
            case VK_N : fractal.increment_noise(-1)                    ; break ;
            case VK_M : fractal.increment_noise(1)                     ; break ;
            case VK_COMMA : fractal.inc_ortho()                        ; break ;
            case VK_PERIOD : fractal.inc_polar()                       ; break ;
            case VK_SLASH : percept.help_screen()                      ; break ;
            case VK_SHIFT :                                            ; break ;

            case VK_MINUS : current_cursor.increment_rate_constant(-1) ; break ;
            case VK_EQUALS : current_cursor.increment_rate_constant(1) ; break ;
            case VK_DELETE : current_cursor.toggle_wander()            ; break ;
            case VK_A : percept.rotateImages = !percept.rotateImages          ; break ;

//            case VK_CONTROL : ENTRY_MODE=percept.salvia_mode=percept.CURSOR_ON = true  ; break ;
            case VK_ALT :                                                              ; break ;
            case VK_UP         :                                                       ; ;
            case VK_PAGE_UP    : fractal.setMotionBlur(fractal.motionblurp-32)         ; break ;
            case VK_DOWN       :                                                       ; ;
            case VK_PAGE_DOWN  : fractal.setMotionBlur(fractal.motionblurp+32)         ; break ;
            case VK_LEFT       : fractal.setColorFilterWeight(fractal.filterweight-32) ; break ;
            case VK_RIGHT      : fractal.setColorFilterWeight(fractal.filterweight+32) ; break ;

            case VK_1 : fractal.setMapping("(1+i)*100+1*ln(z)/p*(2.4*"+percept.buffer.output.W+"/"+percept.buffer.output.H+")"); break ;
            case VK_2 : fractal.setMapping("(1+i)*100+2*ln(z)/p*(2.4*"+percept.buffer.output.W+"/"+percept.buffer.output.H+")"); break ;
            case VK_3 : fractal.setMapping("(1+i)*100+3*ln(z)/p*(2.4*"+percept.buffer.output.W+"/"+percept.buffer.output.H+")"); break ;
            case VK_4 : fractal.setMapping("(1+i)*100+4*ln(z)/p*(2.4*"+percept.buffer.output.W+"/"+percept.buffer.output.H+")"); break ;
            case VK_5 : fractal.setMapping("(1+i)*100+0.5*ln(z)/p*(2.4*"+percept.buffer.output.W+"/"+percept.buffer.output.H+")"); break ;
            case VK_6 : fractal.setMapping("(real(z)+i*ln(abs(imag(z))))*.3/(abs(imag(z)))*" +  percept.buffer.output.W + "/" + percept.buffer.output.H ); break ;
            case VK_7 : fractal.setMapping("(imag(z)*i+ln(abs(real(z))))*.3/(abs(real(z)))*" +  percept.buffer.output.W + "/" + percept.buffer.output.H ); break ;
            case VK_8 : fractal.setMapping("1*ln(z)/p*(2.4*" +  percept.buffer.output.W + "/" + percept.buffer.output.H + ")")                                 ; break ;
            case VK_9 : /*reserved for comicated map*/ ; break ;
            case VK_0 : /*reserved for comicated map*/ ; break ;

            //TODO make these load from config file.
            case VK_F1 : fractal.setMapping("z*z")                         ; break ;
            case VK_F2 : fractal.setMapping("z*abs(z)")                    ; break ;
            case VK_F3 : fractal.setMapping("f/z+i*z")                     ; break ;
            case VK_F4 : fractal.setMapping("z")                           ; break ;
            case VK_F5 : fractal.setMapping("1/z")                         ; break ;
            case VK_F6 : fractal.setMapping("e^z+e^(iz)")                  ; break ;
            case VK_F7 : fractal.setMapping("conj(e^z+e^(z*e^(i*p/4)))")   ; break ;
            case VK_F8 : fractal.setMapping("z*z*e^(i*abs(z))")            ; break ;
            case VK_F9 : fractal.setMapping("abs(z)*e^(i*arg(z)*2)*2")     ; break ;
            case VK_F10: fractal.setMapping("z*e^(i*abs(z))*abs(z)/f")     ; break ;
            case VK_F11: fractal.setMapping("z/abs(sqrt((absz)^2-1.5))")   ; break ;
            case VK_F12:  ; break ;
            default :                                                   return ;
        }
//        percept.show_state();
        mouseEntered(null);
    }
    
    /**
     *
     * @param preset
     */
    public void preset( int preset )
    {
        try
        {
             presets[ presets.length>1?preset % presets.length:0 ].set( percept );
             System.out.println( "Applied preset " + preset );
        }
        catch ( Exception e )
        {
            System.out.println( "Error applying preset " + preset + ":" );
            e.printStackTrace() ;
        }
    }
    
    /**
     *
     * @param active
     */
    public void setAudio( boolean active )
    {
        if ( active )
        {
            for (Cursor c : AudioCursors)
                if (!active_cursors.contains(c)) active_cursors.add(c);
        }
        else for (Cursor c : AudioCursors) active_cursors.remove(c);
        check_cursor();
        //audio.set_active( active );
    }
    
    /**
     *
     * @param active
     */
    public void setFractal( boolean active )
    {
        if ( active )
        {
            for (Cursor c : MapCursors)
                if (!active_cursors.contains(c)) active_cursors.add(c);
        }
        else for (Cursor c : MapCursors) active_cursors.remove(c);
        check_cursor();
        fractal.set_active( active );
    }
    
    /**
     *
     * @param active
     */
    public void setTree( boolean active )
    {
        if ( active )
        {
            for (Cursor c : TreeCursors)
                if (!active_cursors.contains(c)) active_cursors.add(c);
        }
        else for (Cursor c : TreeCursors) active_cursors.remove(c);
        check_cursor();
        tree.set_active( active );
    }
    
    /**
     *
     * @param active
     */
    public void setLife( boolean active )
    {
        if ( active )
        {
            for (Cursor c : LifeCursors)
                if (!active_cursors.contains(c)) active_cursors.add(c);
        }
        else for (Cursor c : LifeCursors) active_cursors.remove(c);
        check_cursor();
//        percept.life.running = active ;
    }

    /*
    public void setCortex( boolean active )
    {
        if ( active )
        {
            for (Cursor c : CortexCursors)
                if (!active_cursors.contains(c)) active_cursors.add(c);
        }
        else for (Cursor c : CortexCursors) active_cursors.remove(c);
        check_cursor();
        //percept.life.running = active ;
    }*/
    
    /**
     *
     * @param e
     */
    public void keyTyped(KeyEvent e)
    {
        //percept.textlayer.keyTyped(e);
    }
    /**
     *
     * @param e
     */
    public void keyReleased(KeyEvent e)
    {
        //percept.textlayer.keyReleased(e);
    }

////////////////////////////////////////////////////////////////////////////
//INNER CLASSES
    
    /** A class representing an object that responds to mouse envents in ways
     *  similar to the normal mouse curosor */
    abstract class Cursor
    {
        
        /** The point on the screen that the virtual cursor should finally come
         *  to rest at. */
        protected Point destination;
        
        /** The point where the cursor is currently located */
        protected float x, y;
        
        /** err... thingy */
        protected float[] dx = new float[2], dy = new float[2];
        
        protected double local_rate = 3. ;
        protected int    rate_constant = 0 ;
        
        protected boolean wanderer = false ;
        
        public synchronized void add_dot()
        {
            if ( dx.length > 250 ) return ;
            float [] new_dx = new float[ dx.length + 1 ] ;
            float [] new_dy = new float[ dy.length + 1 ] ;
            for ( int i = 0 ; i < dx.length ; i ++ ) new_dx[i] = dx[i] ;
            for ( int i = 0 ; i < dy.length ; i ++ ) new_dy[i] = dy[i] ;
            new_dx[dx.length] = dx[dx.length - 1];
            new_dy[dy.length] = dy[dy.length - 1];
            dx = new_dx ; dy = new_dy ;
        }
        
        public synchronized void remove_dot()
        {
            if ( dx.length <= 1 ) return ;
            float [] new_dx = new float[ dx.length - 1 ] ;
            float [] new_dy = new float[ dy.length - 1 ] ;
            for ( int i = 0 ; i < new_dx.length ; i ++ ) new_dx[i] = dx[i] ;
            for ( int i = 0 ; i < new_dy.length ; i ++ ) new_dy[i] = dy[i] ;
            dx = new_dx ; dy = new_dy ;
        }
        
        public void increment_rate_constant( int amt )
        {
            rate_constant += amt ;
            if ( rate_constant > 32 )       rate_constant =  32 ;
            else if ( rate_constant < -32 ) rate_constant = -32 ;
            
            local_rate = Math.pow( 2 , rate_constant / 8. ) ;
        }
        
        /** A BufferedImage representing this cursor when drawn on screen */
        protected Image image;
        
        /** The center of the cursor */
        protected Point offset;
        
        public Cursor( String imagename )
        {
            // Cursor begins by heading toward the center of screen
            destination = new Point(percept.screen_width()/2,percept.screen_height()/2);
            // Load cursor sprite
            image = default_image(imagename);
            // Set cursor location to center of sprite
            offset = new Point(image.getWidth(null) / 2, image.getHeight(null) / 2);
        }
        
        public void mouseMoved(MouseEvent e)
        {
            destination.x = (int)(x_scalar*e.getX()) ;
            destination.y = (int)(y_scalar*e.getY()) ;
            bound(destination);
        }
        
        public void mouseDragged(MouseEvent e)
        {
            mouseMoved(e);
        }
        
        public void toggle_wander() {
            wanderer = ! wanderer ;
        }
        
        //OTHER MEMBER FUNCTIONS
        
        /** Random walker */
        public synchronized void walk()
        {
            destination.x += (int)((Math.random()-.5) * 44 );
            if (destination.x < 0) destination.x = 0;
            else if (destination.x >= percept.physical_width()) destination.x = percept.physical_width() - 1;
            destination.y += (int)((Math.random()-.5) * 44 );
            if (destination.y < 0) destination.y = 0;
            else if (destination.y >= percept.physical_height()) destination.y = percept.physical_height() - 1;
        }
        
        /** Advances this cursor towards its destination at a
         *  custom drift_rate */
        public synchronized void advance_location(float drift_rate)
        {
            if ( wanderer ) walk() ;
            
            drift_rate *= local_rate ;
            if ( drift_rate > 1 ) drift_rate = 1 ;
            dx[0] += (destination.x - dx[0]) * drift_rate;
            dy[0] += (destination.y - dy[0]) * drift_rate;
            int i = 1;
            while ( i < dx.length )
            {
                dx[i] += (dx[i-1] - dx[i]) * drift_rate  ;
                dy[i] += (dy[i-1] - dy[i]) * drift_rate  ;
                i ++ ;
            }
            i -- ;
            x += (dx[i] - x) * drift_rate ;
            y += (dy[i] - y) * drift_rate ;
        }
        
        /** Draws this cursor to the given Graphics in the given Dimension */
        public synchronized void drawTo(Graphics G)
        {
            if ( draw_futures ) for ( int i = 0 ; i < dx.length ; i ++ )
            {
                G.drawImage(trailer, (int)(dx[i]) - offset.x, (int)(dy[i]) - offset.y, null);
            }
            G.drawImage(image, (int)(x) - offset.x, (int)(y) - offset.y, null);
        }
    }
    
    static BufferedImage trailer = trailer();
    static BufferedImage trailer()
    {
        BufferedImage b = new BufferedImage(40,40,BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
        image_graphics.setColor(new Color( 255 , 255 , 255 , 128 ));
        image_graphics.drawOval(19,19,3,3);
        return b;
    }
    
    static BufferedImage focus = focus();
    static BufferedImage focus()
    {
        BufferedImage b = new BufferedImage(22,22,BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
        image_graphics.setColor(Color.WHITE);
        image_graphics.drawOval(1,1,19,19);
        return b;
    }
    
    /**
     * Default cursor image
     */
    private static BufferedImage default_image( String imagename)
    {
        BufferedImage b = new BufferedImage(40,40,BufferedImage.TYPE_INT_ARGB);
        Graphics2D image_graphics = ColorUtility.fancy(b.createGraphics());
        
        // restricts all sprites to a small circular window?
        
        image_graphics.fillOval(13,13,13,13);
        
        try
        {
            BufferedImage image1 = ImageIO.read( new File("resource/cursors/"+imagename) ) ;
            for ( int y = 0 ; y < 40 ; y ++ )
            {
                for ( int x = 0 ; x < 40 ; x ++ )
                {
                    int R , G , B , C ;
                    C = b.getRGB( x , y ) ;
                    R = C & 0xff ;
                    G = ( C >> 8 ) & 0xFF ;
                    B = ( C >> 16 ) & 0xFF ;
                    float[] col1 = Color.RGBtoHSB( R , G , B , new float[3] ) ;
                    C = image1.getRGB( x , y ) ;
                    R = C & 0xff ;
                    G = ( C >> 8 ) & 0xFF ;
                    B = ( C >> 16 ) & 0xFF ;
                    float[] col2 = Color.RGBtoHSB( R , G , B , new float[3] ) ;
                    b.setRGB( x , y , b.getRGB( x , y ) & 0xff000000 | 0x00ffffff & C );
                }
            }
        }
        catch( Exception e)
        {}
        
        //image_graphics.setColor(Color.WHITE);
        //image_graphics.drawOval(10,10,19,19);
        return b;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     *
     * @return
     */
    public double XBranchingCursor       (){
        return BranchingCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double XAlphaCursor           (){
        return AlphaCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double XBranchLengthCursor    (){
        return BranchLengthCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double XTreeOrientationCursor(){
        return TreeOrientationCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double XMapCursor             (){
        return MapCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double XMapRotationCursor     (){
        return MapRotationCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double XGradintCursor         (){
        return GradientCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double XTreeLocationCursor    (){
        return TreeLocationCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double YBranchingCursor       (){
        return BranchingCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double YAlphaCursor           (){
        return AlphaCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double YBranchLengthCursor    (){
        return BranchLengthCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double YTreeOrientationCursor(){
        return TreeOrientationCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double YMapCursor             (){
        return MapCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double XContrastCursor             (){
        return this.ContrastCursor.destination.x ;
    }
    /**
     *
     * @return
     */
    public double YContrastCursor             (){
        return this.ContrastCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double YMapRotationCursor     (){
        return MapRotationCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double YGradintCursor         (){
        return GradientCursor.destination.y ;
    }
    /**
     *
     * @return
     */
    public double YTreeLocationCursor    (){
        return TreeLocationCursor.destination.y ;
    }
    
    
    /**
     *
     * @param x
     * @param y
     */
    public void setBranchingCursor(double x,double y){
        setCursor(BranchingCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setAlphaCursor(double x,double y){
        setCursor(AlphaCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setBranchLengthCursor(double x,double y){
        setCursor(BranchLengthCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setTreeOrientationCursor(double x,double y){
        setCursor(TreeOrientationCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setMapCursor(double x,double y){
        setCursor(MapCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setContrastCursor(double x,double y){
        setCursor(ContrastCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setMapRotationCursor(double x,double y){
        setCursor(MapRotationCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setGradientCursor(double x,double y){
        setCursor(GradientCursor,x,y);
    }
    /**
     *
     * @param x
     * @param y
     */
    public void setTreeLocationCursor(double x,double y){
        setCursor(TreeLocationCursor,x,y);
    }
    void setCursor(Cursor c, double xx, double yy) {
        c.destination.x = (int)xx;
        c.destination.y = (int)yy;
    }




    //public void setYMapAlphaCursor        (double x){
    //    MapAlphaCursor.destination.y = (int) x ;
    //}
    //public void setYCellSpeedCursor       (double x){
    //    CellSpeedCursor.destination.y = (int) x ;
    //}
    //public void setYCellCursor            (double x){
    //    CellCursor.destination.y = (int) x ;
    //}
    //public double XMapAlphaCursor        (){
    //    return MapAlphaCursor.destination.x ;
    //}
    //public double XCellSpeedCursor       (){
    //    return CellSpeedCursor.destination.x ;
    //}
    //public double XCellCursor            (){
    //    return CellCursor.destination.x ;
    //}
    //public double YMapAlphaCursor        (){
    //    return MapAlphaCursor.destination.y ;
    //}
    //public double YCellSpeedCursor       (){
    //    return CellSpeedCursor.destination.y ;
    //}
    //public double YCellCursor            (){
    //    return CellCursor.destination.y ;
    //}
    //public void setXMapAlphaCursor        (double x){
    //    MapAlphaCursor.destination.x = (int) x ;
    //}
    //public void setXCellSpeedCursor       (double x){
    //    CellSpeedCursor.destination.x = (int) x ;
    //}
    //public void setXCellCursor            (double x){
    //    CellCursor.destination.x = (int) x ;
    //}
}
