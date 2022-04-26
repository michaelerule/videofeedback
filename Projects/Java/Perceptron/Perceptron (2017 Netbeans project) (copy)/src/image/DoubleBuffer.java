package image;

import util.ColorUtility;
import java.awt.image.* ;
import java.awt.Graphics ;
import java.awt.Graphics2D ;
import perceptron.Perceptron;

import static java.awt.RenderingHints.* ;

/**
 * Re-implementation of basic double-buffered texture. Supports
 * fast reflected/periodic bounday conditions for texturing. This
 * system is built on glorious bitwise operations.
 *
 * DoubleBuffer.java
 * Created by Michael Rule on 6/1/07.
 * @author mrule
 */
public class DoubleBuffer
{
    public interface Grabber
    {
        public int get( int x , int y ) ;
        public void nextFrame() ;
    }
    
    /**
     *
     */
    public static class ImageRenderContext
    {
        /**
         *
         */
        public BufferedImage image ;
        /**
         *
         */
        public Graphics      basic_graphics ;
        /**
         *
         */
        public Graphics2D    graphics2D ;
        /**
         *
         */
        public Graphics      graphics ;
        /**
         *
         */
        public DataBuffer    buffer ;
        /**
         *
         */
        public final int W ;
        /**
         *
         */
        public final int H ;
        /**
         *
         */
        public final int W_ONE ;
        /**
         *
         */
        public final int H_ONE ;
        
        /**
         *
         * @param b
         */
        public ImageRenderContext(BufferedImage b)
        {
            image = b;
            basic_graphics = b.getGraphics() ;
            graphics2D     = ColorUtility.fancy(b.createGraphics()) ;
            graphics       = graphics2D ;
            buffer         = b.getRaster().getDataBuffer() ;
            
            image.setAccelerationPriority(1.f) ;
            
            W = b.getWidth() ;
            H = b.getHeight() ;
            W_ONE = W - 1 ;
            H_ONE = H - 1 ;
        }
        
        final static int R = 0xFF0000 , G = 0x00FF00 , B = 0x0000FF , K = 0xFF00FF ;
        
        public Grabber getFixed8Bit = new Grabber()
        {
            public int get( int dx , int dy )
            {
                int x_1, x_2, y_1, y_2 ;
                
                dx ^= dx >> 31 ;
                x_1 = dx >> 8 ;
                x_2 = x_1 + 1 ;
                x_1 = ( x_1 / W & 1 ) == 0 ? x_1 % W : W_ONE - x_1 % W ;
                x_2 = ( x_2 / W & 1 ) == 0 ? x_2 % W : W_ONE - x_2 % W ;
                
                dy ^= dy >> 31 ;
                y_1 = dy >> 8 ;
                y_2 = y_1 + 1 ;
                y_1 = ( y_1 / H & 1 ) == 0 ? y_1 % H : H_ONE - y_1 % H ;
                y_2 = ( y_2 / H & 1 ) == 0 ? y_2 % H : H_ONE - y_2 % H ;
                y_2 *= W ;
                y_1 *= W ;
                
                int c11 = buffer.getElem( y_1 + x_1 ) ;
                int c21 = buffer.getElem( y_1 + x_2 ) ;
                int c12 = buffer.getElem( y_2 + x_1 ) ;
                int c22 = buffer.getElem( y_2 + x_2 ) ;
                
                int A_x = dx & 0xFF ;
                int A_y = dy & 0xFF ;
                int B_x = 0xFF & ~A_x ;
                int B_y = 0xFF & ~A_y ;
                int C_11 = B_x * B_y >> 8 ;
                int C_12 = A_x * B_y >> 8 ;
                int C_21 = B_x * A_y >> 8 ;
                int C_22 = A_x * A_y >> 8 ;
                
                return K & (( K & c11 ) * C_11 + ( K & c21 ) * C_12 + ( K & c12 ) * C_21 + ( K & c22 ) * C_22 >> 8 ) |
                       G & (( G & c11 ) * C_11 + ( G & c21 ) * C_12 + ( G & c12 ) * C_21 + ( G & c22 ) * C_22 >> 8 ) ;
            }
            public void nextFrame() {
            }
        };
        
        public Grabber getFixed8BitNoReflect = new Grabber()
        {
            public int get( int dx , int dy )
            {
                int x_1, x_2, y_1, y_2 ;
                
                dx ^= dx >> 31 ;
                x_1 = dx >> 8 ;
                x_2 = x_1 + 1 ;
                x_1 = dx < 0 ? x_1 % W : W_ONE - x_1 % W ;
                x_2 = dx < 0 ? x_2 % W : W_ONE - x_2 % W ;
                
                dy ^= dy >> 31 ;
                y_1 = dy >> 8 ;
                y_2 = y_1 + 1 ;
                y_1 = dy < 0 ? y_1 % H : H_ONE - y_1 % H ;
                y_2 = dy < 0 ? y_2 % H : H_ONE - y_2 % H ;
                y_2 *= W ;
                y_1 *= W ;
                
                int c11 = buffer.getElem( y_1 + x_1 ) ;
                int c21 = buffer.getElem( y_1 + x_2 ) ;
                int c12 = buffer.getElem( y_2 + x_1 ) ;
                int c22 = buffer.getElem( y_2 + x_2 ) ;
                
                int A_x = dx & 0xFF ;
                int A_y = dy & 0xFF ;
                int B_x = 0xFF & ~A_x ;
                int B_y = 0xFF & ~A_y ;
                int C_11 = B_x * B_y >> 8 ;
                int C_12 = A_x * B_y >> 8 ;
                int C_21 = B_x * A_y >> 8 ;
                int C_22 = A_x * A_y >> 8 ;
                
                return K & (( K & c11 ) * C_11 + ( K & c21 ) * C_12 + ( K & c12 ) * C_21 + ( K & c22 ) * C_22 >> 8 ) |
                        G & (( G & c11 ) * C_11 + ( G & c21 ) * C_12 + ( G & c12 ) * C_21 + ( G & c22 ) * C_22 >> 8 ) ;
            }
            public void nextFrame() {
            }
        };
        
        /**
         *
         */
        public Grabber get = new Grabber()
        {
            public int get( int x , int y )
            {
                x += 127 ;
                y += 127 ;
                x >>= 8 ;
                y >>= 8 ;
                x ^= x >> 31 ;
                y ^= y >> 31 ;
                x = ( ( x / W & 1 ) == 0 ) ? x % W : W_ONE - x % W ;
                y = ( ( y / H & 1 ) == 0 ) ? y % H : H_ONE - y % H ;
                return buffer.getElem( x + y * W );
            }
            public void nextFrame() {
            }
        };
        
        /**
         *
         */
        public Grabber getNoReflect = new Grabber()
        {
            public int get( int x , int y )
            {
                x += 127 ;
                y += 127 ;
                x >>= 8 ;
                y >>= 8 ;
                //x ^= x >> 31 ;
                //y ^= y >> 31 ;
                x = ( x > 0 ) ? x % W : (- x) % W ;
                y = ( y > 0 ) ? y % H : (- y) % H ;
                return buffer.getElem( x + y * W );
            }
            public void nextFrame() {
            }
        };
        
        /**
         *
         */
        public Grabber getBicubic = new Grabber() {
            public int get( int dx , int dy ) {
                /*double CubicInterpolate(
   double y0,double y1,
   double y2,double y3,
   double mu)
{
   double a0,a1,a2,a3,mu2;

   mu2 = mu*mu;
   a0 = y3 - y2 - y0 + y1;
   a1 = y0 - y1 - a0;
   a2 = y2 - y0;
   a3 = y1;

   return(a0*mu*mu2+a1*mu2+a2*mu+a3);
}*/
                int x_1, x_2, x_3, x_4 ,
                    y_1, y_2, y_3, y_4 ;
                
                dx ^= dx >> 31 ;
                x_2 = dx >> 8 ;
                x_1 = x_2 - 1 ;
                x_3 = x_2 + 1 ;
                x_4 = x_2 + 2 ;
                x_1 = ( x_1 / W & 1 ) == 0 ? x_1 % W : W_ONE - x_1 % W ;
                x_2 = ( x_2 / W & 1 ) == 0 ? x_2 % W : W_ONE - x_2 % W ;
                x_3 = ( x_1 / W & 1 ) == 0 ? x_3 % W : W_ONE - x_3 % W ;
                x_4 = ( x_2 / W & 1 ) == 0 ? x_4 % W : W_ONE - x_4 % W ;
                
                dy ^= dy >> 31 ;
                y_2 = dy >> 8 ;
                y_1 = y_2 - 1 ;
                y_3 = y_2 + 1 ;
                y_4 = y_2 + 2 ;
                y_1 = ( y_1 / H & 1 ) == 0 ? y_1 % H : H_ONE - y_1 % H ;
                y_2 = ( y_2 / H & 1 ) == 0 ? y_2 % H : H_ONE - y_2 % H ;
                y_3 = ( y_1 / H & 1 ) == 0 ? y_3 % H : H_ONE - y_3 % H ;
                y_4 = ( y_2 / H & 1 ) == 0 ? y_4 % H : H_ONE - y_4 % H ;
                y_4 *= W ;
                y_3 *= W ;
                y_2 *= W ;
                y_1 *= W ;
                
                int c11 = buffer.getElem( y_1 + x_1 ) ;
                int c21 = buffer.getElem( y_1 + x_2 ) ;
                int c31 = buffer.getElem( y_1 + x_3 ) ;
                int c41 = buffer.getElem( y_1 + x_4 ) ;
                int c12 = buffer.getElem( y_2 + x_1 ) ;
                int c22 = buffer.getElem( y_2 + x_2 ) ;
                int c32 = buffer.getElem( y_2 + x_3 ) ;
                int c42 = buffer.getElem( y_2 + x_4 ) ;
                int c13 = buffer.getElem( y_3 + x_1 ) ;
                int c23 = buffer.getElem( y_3 + x_2 ) ;
                int c33 = buffer.getElem( y_3 + x_3 ) ;
                int c43 = buffer.getElem( y_3 + x_4 ) ;
                int c14 = buffer.getElem( y_4 + x_1 ) ;
                int c24 = buffer.getElem( y_4 + x_2 ) ;
                int c34 = buffer.getElem( y_4 + x_3 ) ;
                int c44 = buffer.getElem( y_4 + x_4 ) ;
                
                int mx_0 = (x_3 - x_1) >> 1 ;
                int mx_1 = (x_4 - x_2) >> 1 ;
                int my_0 = (y_3 - y_1) >> 1 ;
                int my_1 = (y_4 - y_2) >> 1 ;
                
                int A_x = dx & 0xFF ;
                int A_y = dy & 0xFF ;
                int B_x = 255 - A_x ;
                int B_y = 255 - A_y ;
                int C_11 = B_x * B_y >> 8 ;
                int C_12 = A_x * B_y >> 8 ;
                int C_21 = B_x * A_y >> 8 ;
                int C_22 = A_x * A_y >> 8 ;
                
                return K & (( K & c11 ) * C_11 + ( K & c21 ) * C_12 + ( K & c12 ) * C_21 + ( K & c22 ) * C_22 >> 8 ) |
                       G & (( G & c11 ) * C_11 + ( G & c21 ) * C_12 + ( G & c12 ) * C_21 + ( G & c22 ) * C_22 >> 8 ) ;
            }
            public void nextFrame() {
            }
        };
        
        public Grabber grabber = getFixed8Bit ;
        
        /**
         *
         * @param x
         * @param y
         * @return
         */
        public synchronized int get( int x , int y )
        {
            return grabber.get( x , y ) ;
        }
        
        /**
         *
         * @param fancy
         */
        public synchronized void set_fancy( boolean fancy )
        {
            graphics = fancy ? graphics2D : basic_graphics ;
        }


        /**
         *
         * @param inter
         */
        public synchronized void set_interpolated( boolean inter )
        {
            grabber = inter ? getFixed8Bit : get ;
        }
        
    }
    
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    public ImageRenderContext output, buffer, sketch, image, imagefade, disply ;
    boolean fancy = false ;

    public boolean fancy() { return fancy; }
    /**
     *
     * @param a
     * @param b
     * @param s
     * @param d
     */
    public DoubleBuffer(
      BufferedImage a, BufferedImage b, BufferedImage s, BufferedImage d)
    {
        if ( a != null ) output = new ImageRenderContext(a);
        if ( b != null ) buffer = new ImageRenderContext(b);
        if ( s != null ) sketch = new ImageRenderContext(s);
        if ( d != null ) disply = new ImageRenderContext(d);
    }
    /**
     *
     * @param b
     */
    public synchronized void setFancy( boolean b )
    {
        fancy = b ;
        if ( output != null ) output.set_fancy( fancy );
        if ( buffer != null ) buffer.set_fancy( fancy );
        if ( image  != null ) image.set_fancy( fancy );
        if ( disply != null ) disply.set_fancy( fancy );
    }
    /**
     *
     */
    public synchronized void toggle_fancy()
    {
        fancy = ! fancy ;
        if ( output != null ) output.set_fancy( fancy );
        if ( buffer != null ) buffer.set_fancy( fancy );
        if ( image != null ) image.set_fancy( fancy );
        if ( disply != null ) disply.set_fancy( fancy );
    }
    
    /**
     *
     */
    public synchronized void flip()
    {
        ImageRenderContext temp = output;
        output = buffer;
        buffer = temp;
    }
    
    /**
     *
     * @param s
     * @param W
     * @param rr
     * @param P
     */
    public synchronized void load_sketch( BufferedImage s , int W, boolean rr, final Perceptron P )
    {
        try {
        if ( s != null ) {
            
            if ( image != null ) {
                imagefade = image ;
                image = new ImageRenderContext(s);
            }
            else 
                imagefade = image = new ImageRenderContext(s);
           
            final float SCALE1 = (float) image.W / W ;
            final float SCALE2 = (float) imagefade.W / W ;
            
            final ImageRenderContext thisSketch = image ;
            final Grabber g1 = reflect ? image.getFixed8Bit : image.getFixed8BitNoReflect ;
            final Grabber g2 = reflect ? imagefade.getFixed8Bit : imagefade.getFixed8BitNoReflect ;
            //final Grabber g2 = imagefade.get ;
           
            //thats right, its a scary awful way of compositing functions
            //in java, on the fly.
            final Grabber endfade = 
            new Grabber() {
                public int get(int x, int y) {
                    return g1.get( x*=SCALE1 , y*=SCALE1 );
                    //return g1.get( x , y );
                }
                public void nextFrame() {
                }
            };
            
            final long fadeout = (1<<12) + System.currentTimeMillis() ;
            thisSketch.grabber = new Grabber() {
                int fade ;
                public int get(int x, int y) {
                    return ColorUtility.average(
                      g2.get( (int)(x*SCALE2) , (int)(y*SCALE2) ), fade, 
                      g1.get( (int)(x*SCALE1) , (int)(y*SCALE1) ), 256 - fade );
                }
                public void nextFrame() {
                    int f = (int)(fadeout - System.currentTimeMillis())>>4 ;
                    if ( f < 0 ) {
                        //if (P.rotateImages) {
                        //    P.increment_sketch(1);
                        //} else {
                            thisSketch.grabber = endfade ;
                        //}
                    }
                    else fade = Math.min( 256 , Math.max( 0 , f ) ) ;
                }
            };
            thisSketch.grabber.nextFrame();
        }
        } catch ( Exception e ) {
            System.out.println("SOMETHING BROKE!");
            e.printStackTrace();
        }
    }
    
    boolean reflect = true ;

    public void toggleReflect() {
        reflect = !reflect ;
    }    
    
    
    /**
     *
     * @param s
     */
    public synchronized void fast_load_sketch( BufferedImage s )
    {
        if ( s != null ) {
            image = new ImageRenderContext(s);
        }
    }
}
