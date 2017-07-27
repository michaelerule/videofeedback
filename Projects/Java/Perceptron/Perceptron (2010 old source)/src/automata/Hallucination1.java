package automata;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import util.ColorUtility;
import static java.lang.Math.* ;

public class Hallucination1 {
    
    int kernelsize = 10 ;
    int halfsize = kernelsize / 2 ;
    int kernellength = kernelsize * kernelsize ;
    
    int gridsize = 32 ;
    int halfgridsize = gridsize / 2 ;
    int gridmask = gridsize - 1 ;
    int gridlength = gridsize * gridsize ;
    int gridlengthmask = gridlength - 1 ;
    
    int[] kernel = new int[kernellength] ;
    int[] offset = new int[kernellength];
    int[] cortex = new int[gridlength];
    int[] buffer = new int[gridlength];
    
    int gain = 7000 ; 
    int noise = ( 1100 * 6000 ) << 8 ;
    
    public int color1 = 0xff0000 ;
    public int color2 = 0x00ffff ;
    
    int toint( double d ) { return (int)( .5 + 255 * d ); }
    
    public Hallucination1() {
        double oversigma1 = 5. / kernelsize ;
        double oversigma2 = 7. / kernelsize ;
        int i = 0 ;
        for ( int y = - halfsize ; y < halfsize ; y++ ) {
            for ( int x = - halfsize ; x < halfsize ; x++ ) {
                float r = (float)hypot( x , y );
                double s1 = r * oversigma1 ;
                double s2 = r * oversigma2 ;
                if ( x*x+y*y < halfsize*halfsize-8 ) 
                    kernel[i] = toint( gain * ( 2 * exp( -.5*s2*s2 ) - exp( -.5*s1*s1 ) ) ) ;
                offset[i] = x + y * gridsize ;
                i ++ ;
            }
        }
    }
    
    public BufferedImage draw = new BufferedImage( gridsize, gridsize, BufferedImage.TYPE_INT_RGB ) ;
    public DataBuffer data = draw.getRaster().getDataBuffer() ;
    
    double sigmoid( double x ) {
        return tanh( x ) ;
    }
    
    public void step() {
        int i = 0 ;
        for ( int y = 0 ; y < gridsize ; y ++ ) {
            for ( int x = 0 ; x < gridsize ; x ++ ) {
                int newval = (int)((Math.random()-.5)*noise) ;
                for ( int j = 0 ; j < kernellength ; j ++ ) 
                    newval += kernel[j] * cortex[ i + offset[j] & gridlengthmask ] ;
                newval >>= 8 ;
                if ( newval > 127 ) newval = 127 ;
                else if ( newval < -128 ) newval = -128 ;
                buffer[i] = newval ;
                int color = newval + 128 & 0xff ;
                data.setElem(i, ColorUtility.average(
                  ColorUtility.average(color1,color,color2,0xff&~color),24,
                  data.getElem(i),232));
                //data.setElem(i, ColorUtility.average(color1,color,color2,0xff&~color));
                i++ ;
            }
        }
        int [] temp = cortex ;
        cortex = buffer ;
        buffer = temp ;
    }
}
