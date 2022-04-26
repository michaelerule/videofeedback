package automata;

import java.awt.image.BufferedImage;

import static util.ColorUtil.* ;
import static java.lang.Math.* ;


/* class Automata represents the state data, image output, and behavior of a
 * cellular automata. The behavior of the automata can be changed by swapping 
 * the AutomataEngine operator. However, all AutomataEngines should use float
 * values between 0 and 1, for correct behavior when transitioning. The size 
 * of the data array must be a power of two ut support modulo by bitmasking.
 * the ColorScheme determines how the automata values are mapped to colors.
 * AutomataEngines may require additional state, and must be initialized for
 * this Automata. Presently, errors will occur if an AutomataEngine instance is
 * shared across more than one Automata.
 */

/**
 *
 * @author mer49
 */

public class Automata {

    private float [][] state ;
    private float [][] buffer ;
    private final BufferedImage texture ;
    
    private AutomataEngine operator ;
    private ColorScheme    colorer ;
    
    /**
     *
     * @param size
     */
    public Automata( int size )
    {
        if (((int)(log(size)/log(2))<<1)!=size)
          System.err.println("non power of two size detected, might crash");
        
        state   = new float[size][size];
        buffer  = new float[size][size];
        texture = new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB);
    }
    
    /**
     *
     * @return
     */
    public BufferedImage getTexture() {
        return texture ;
    }
    
    private void swapBuffers() {
        float [][] temp = state ;
        state = buffer ;
        buffer = temp ;
    }
    
    /**
     *
     */
    public void step() {
        operator.step(state,texture,colorer);
        swapBuffers();
    }
    
    /**
     *
     * @param o
     */
    public void setOperator( AutomataEngine o ) {
        operator = o ;
    }
    
    /**
     *
     * @param c
     */
    public void setColorScheme( ColorScheme c ) {
        colorer = c ;
    }
    
    /**
     *
     * @param c
     */
    public void crossfadeColorScheme( ColorScheme c ) {
        
        final long fadeout = (1<<12) + System.currentTimeMillis() ;
        final ColorScheme oldc = colorer ;
        final ColorScheme newc = c ;
        
        colorer = new ColorScheme() {
            int fade ;
            @Override
            public int toRGB(float f) {
                return average( oldc.toRGB(f), fade, newc.toRGB(f), 256-fade );
            }
            @Override
            public float fromRGB(int RGB) {
                float ff = fade * 0.00390625f ;
                return oldc.fromRGB(RGB)*ff + newc.fromRGB(RGB)*(1-ff) ;
            }
            @Override
            public void step() {
                int f = (int)(fadeout - System.currentTimeMillis())>>4 ;
                if (f<0) colorer = newc ;
                else fade = clip(f) ;
            }
        };
    }
}
