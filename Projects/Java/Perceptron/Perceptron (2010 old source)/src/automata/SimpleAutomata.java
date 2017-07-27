package automata;



import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Random;
import static java.lang.Math.* ;

public class SimpleAutomata {

    final int timeout = 7000 ;
    final int size = 32 ;
    final int halfsize = size / 2 ;
    final int twogridsize = size * 2 - 1;
    final int mask = size - 1 ;
    final int kernelsize = 21 ;
    final int kernelcenter = 11 ;
    
    float [][] u = new float[size][size];
    float [][] ubuff = new float[size][size];
    float [][] v = new float[size][size];
    
    final float [][] we = new float[kernelsize][kernelsize];
    final float [][] wi = new float[kernelsize][kernelsize];
    final float [][] wa = new float[kernelsize][kernelsize];
    
    final float dt  = 1f ;
    final float c1  = 1f - dt ;
    final float amp = 1 ;
    final float w   = 0.25f * dt ;
    final float thr = 0.8f ;
    final float c   = 2f ;
    final float tau = 0.1f * dt ;
    final float b   = 0.2f ;
    final float a   = 6.2f ;
    final float g   = 4f ;
    final float te  = 0.25f ;
    
    int t = 0 ;
    
    Random rand = new Random() ;
    
    public SimpleAutomata() {
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ ) 
                u[i][j] = noise();
        
        float wesum = 0 ;
        float wisum = 0 ;
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
            {
                int y = i - kernelcenter ;
                int x = j - kernelcenter ;
                int rr = x*x+y*y ;
                wesum += we[i][j] = heav(4 -rr) ;
                wisum += wi[i][j] = heav(64-rr) ;
            }
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
            {
                we[i][j]/=wesum ;
                wi[i][j]/=wisum ;
                wa[i][j] = a * we[i][j] - c * wi[i][j] ;
            }
    }
    
    public BufferedImage draw = new BufferedImage( 
      size, size, BufferedImage.TYPE_INT_RGB  ) ;
    public DataBuffer data = draw.getRaster().getDataBuffer() ;
    
    float f( float x ) {
        return 1/(1+(float)exp(-x)) ;
    }
    
    float heav( float x ) {
        return x < 0f ? 0f : x == 0f ? .5f : 1f ; 
    }
    
    float ff( int t ) {
        return amp*heav( (float)cos(w*t) - thr );
    }
    
    float noise() {
        return (float)rand.nextGaussian() ;
    }
    
    final int bound = halfsize*halfsize;
    boolean inbounds( int x, int y ) {
       x-=halfsize;
       y-=halfsize;
       return (x*x+y*y<bound);
    }
    
    void step() {
        for ( int i = 0 ; i < size ; i ++ ) {
            for ( int j = 0 ; j < size ; j ++ ) {
                
                    float ka = 0 ;
                    
                    int cy = i - kernelcenter ;
                    int cx = j - kernelcenter ;
                    for ( int ii = 0 ; ii < kernelsize ; ii++ )
                        for ( int ij = 0 ; ij < kernelsize ; ij++ )
                            ka += wa[ii][ij]*u[ii+cy&mask][ij+cx&mask] ;
                    
                    float f = u[i][j] ;
                    ubuff[i][j] = c1*f + dt*f(ka-g*v[i][j]-te+ff(t)
                      //+.05f*noise()
                      );
                    v[i][j] += (f - b*v[i][j])*tau ;

                    int C = (int)(255*f);
                    C = (C<<16)|(C<<8)|C ;
                    draw.setRGB(j,i,C);
            }
        }
        t++ ;
        float[][] temp = u ;
        u = ubuff ;
        ubuff = temp ;
    }
}