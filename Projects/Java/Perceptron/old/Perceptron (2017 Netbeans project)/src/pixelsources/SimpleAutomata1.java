package pixelsources;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Random;
import static java.lang.Math.* ;

// converted to 8 bit fixed point in integers, 0xFF = 1.f ;

/**
 *
 * @author mrule
 */
public class SimpleAutomata1 {
    
    int timeout = 7000 ;
    int size = 32 ;
    int halfsize = size / 2 ;
    int twogridsize = size * 2 - 1;
    int mask = size - 1 ;
    int kernelsize = 21 ;
    int kc = 11 ;
    
    
    int [][] u = new int[size][size];
    int [][] ubuff = new int[size][size];
    float [][] v = new float[size][size];
    
    float [][] we = new float[kernelsize][kernelsize];
    float [][] wi = new float[kernelsize][kernelsize];
    int [][] wa = new int[kernelsize][kernelsize];
    
    float dt  = 1f ;
    float c1  = 1f - dt ;
    float amp = 1 ;
    float w   = .25f * dt ;
    float thr = .8f ;
    float c   = 2f ;
    float tau = .1f * dt ;
    float b   = .2f ;
    float a   = 6.4f ;
    float g   = 4f ;
    float te  = .25f ;
    float noise = 1f ;
    float an  = .05f ;
    
    int idt  = toint( dt );
    int ic1  = toint( c1 );
    int iamp = toint( amp );
    int iw   = toint( w );
    int ithr = toint( thr );
    int ic   = toint( c );
    int itau = toint( tau );
    int ib   = toint( b );
    int ia   = toint( a );
    int ig   = toint( g );
    int ite  = toint( te );
    int ian  = toint( an );
    
    int t = 0 ;
    
    Random rand = new Random() ;
    
    /**
     *
     */
    public BufferedImage draw ;
    /**
     *
     */
    public DataBuffer data ;
    
    /**
     *
     */
    public SimpleAutomata1() {
        seed_noise();
        form_kernel();
        init_image();
        check_init();
    }
    
    void init_image() {
        draw = new BufferedImage( size, size, BufferedImage.TYPE_INT_RGB ) ;
        data = draw.getRaster().getDataBuffer() ;
    }
    
    void seed_noise() {
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ ) 
                u[i][j] = toint(noise());
    }
    
    void form_kernel() {
        float wesum = 0 ;
        float wisum = 0 ;
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
            {
                int y = i - kc ;
                int x = j - kc ;
                int rr = x*x+y*y ;
                wesum += we[i][j] = heav(4  - rr) ;
                wisum += wi[i][j] = heav(64 - rr) ;
            }
        wesum = a/wesum ;
        wisum = c/wisum ;
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
                 wa[i][j] = toint(we[i][j]*wesum - wi[i][j]*wisum) ;
    }
    
    void check_init() {
        
        System.out.println( "timeout = " + ( timeout ) ); 
        System.out.println( "size = " + ( size ) ); 
        System.out.println( "halfsize = " + ( halfsize ) ); 
        System.out.println( "twogridsize = " + ( twogridsize ) ); 
        System.out.println( "mask = " + ( mask ) ); 
        System.out.println( "kernelsize = " + ( kernelsize ) ); 
        System.out.println( "kc  = " + ( kc ) ); 
/*
        System.out.println( "dt  = " + tofloat(dt) ); 
        System.out.println( "c1  = " + tofloat(c1) ); 
        System.out.println( "amp = " + tofloat( amp ) ); 
        System.out.println( "w   = " + tofloat( w ) ); 
        System.out.println( "thr = " + tofloat( thr ) ); 
        System.out.println( "c   = " + tofloat( c ) ); 
        System.out.println( "tau = " + tofloat( tau ) ); 
        System.out.println( "b   = " + tofloat( b ) ); 
        System.out.println( "a   = " + tofloat( a ) ); 
        System.out.println( "g   = " + tofloat( g ) ); 
        System.out.println( "te  = " + tofloat( te ) ); 
        System.out.println( "an  = " + tofloat( an ) ); 
 */
    }
    
    int precision = 12 ;
    int multiplier = 1 << precision ;
    float demultiplier = 1f / multiplier ;
    
    int mul( int a, int b ) {
        return a * b >> precision ;
    }
    
    int div( int n, int d ) {
        return ( n << precision ) / d ;
    }
    
    int toint( double f ) {
        return (int)(f*multiplier+.5f);
    }
    
    float tofloat( int i ) {
        return (float)i*demultiplier;
    }
    
    /*
    int iheav( double x ) {
        return x < 0 ? 0 : x == 0 ? 128 : 256 ;
    }
    
    int inoise() {
        return toint(tofloat(amp)*(float)rand.nextGaussian()) ;
    }*/
    
    float f( float x ) {
        if ( x < -4 ) return 0 ;
        if ( x > 4 ) return 1 ;
        return
 .5f +
x* .25f +
x*x*x* -0.0625f/6 +
x*x*x*x*x* 0.0104166667f/120 +
x*x*x*x*x*x*x* -0.00147569444f/5040 ;
        
        //return 1/(1+(float)exp(-x)) ;
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
    
    /**
     *
     */
    public void step() {
        
        float const1 = ff(t) - te;
        
        for ( int i = 0 ; i < size ; i ++ ) {
            int ioff = i - kc ;
            for ( int j = 0 ; j < size ; j ++ ) {
                    int joff = j - kc ;
                
                    int ika = 0 ;
                    for ( int ii = 0 ; ii < kernelsize ; ii++ )
                        for ( int ij = 0 ; ij < kernelsize ; ij++ )
                            ika += wa[ii][ij]*u[ioff+ii&mask][joff+ij&mask] ;
                    ika >>= precision;
                    
                    
                    ubuff[i][j] = 
                      mul( ic1 , u[i][j] ) + 
                      toint(
                        dt * f(
                            tofloat(ika) - 
                            g*v[i][j] +
                            const1 + 
                            an*noise()
                        )
                    );
                    
                    float uij = tofloat(u[i][j]);
                    
                    v[i][j] += (uij-b*v[i][j])*tau ;

                    draw.setRGB(j,i,Color.HSBtoRGB(uij*3, 1f, uij));
            }
        }
        t++ ;
        
        int[][] temp = u ;
        u = ubuff ;
        ubuff = temp ;
    }
}