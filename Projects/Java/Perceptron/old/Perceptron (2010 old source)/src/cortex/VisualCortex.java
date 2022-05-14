package cortex;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Random;
import perceptron.Controller;
import util.MT;
import util.R250_521;
import static java.lang.Math.* ;

public class VisualCortex {
    
    private int shift       = 6 ;
    private int size        = 1 << shift ;
    private int mask        = size - 1 ;
    private int kernelsize = 16 ;
    private int kc = kernelsize >> 1 ;
        
    float [][] u     = new float[size][size]; //synaptic activity
    float [][] ubuff = new float[size][size];
    float [][] v     = new float[size][size]; //attenuation
    
    int   [][] cost  = new int  [size][size];
    int   [][] sint  = new int  [size][size];
    int   [][] xbuff = new int  [size][size];
    int   [][] ybuff = new int  [size][size];
    float [][] theta = new float[size][size];
    
    int   [][] kcos  = new int  [kernelsize][kernelsize];
    int   [][] ksin  = new int  [kernelsize][kernelsize]; 
    int   [][] kani  = new int  [kernelsize][kernelsize]; 
    float [][] wa    = new float[kernelsize][kernelsize];
    
    private float dt  = .1f ;
    private float c1  = 1f - dt ;
    private float w   = .2f ;
    private float wdt = w * dt ;
    private float tau = 10f ;
    private float dtovertau = dt / tau ;
    private float amp   = 1     ;
    private float ai    = 2f    ;
    private float b     = .2f   ;
    private float ae    = 6.23f ;
    private float g     = 4f    ;
    private float te    = .25f  ;
    private float ac    = .4f ;
    private float sigmae = 2 ;
    private float sigmai = 8 ;
    private float sigmaan =   10 ;
    private float aaniso  =  .1f ;
    private float brightness = 0f ;
    private float ai_orient   = 2f ;
    private float ae_orient   = 6.23f ;
    private float sigmae_orient = 2 ;
    private float sigmai_orient = 8 ;
    
    int t = 0 ;
    
    float [][] wa_orient  = new float[kernelsize][kernelsize];
    
    public int size() {return size;}
    private synchronized void setShift( int i ) {
        if ( shift < 1 || shift > 30 ) return ;        
        shift = i ;
        size  = 1 << shift ;
        mask  = size - 1 ;}
    public int kernelsize() {
        return kernelsize;}
    public synchronized void setKernelSize( int i ) {
        kernelsize = 16 ;
        kc         = kernelsize >> 1 ;}
    public synchronized void setDt(float f) {
        dt = f ;
        c1 = 1 - dt ;
        wdt = w * dt ;
        dtovertau = dt / tau ;}
    public synchronized void setW(float f) {
        w = f ;
        wdt = dt * w ;}
    public synchronized void setTau(float f) {
        //hack to prevent introducing NaN into the system
        if ( f < .001f ) return ;
        tau = f ;
        dtovertau = dt / tau ;}
    public synchronized void setAmp(float f) {amp = f ;}
    public synchronized void setAi(float f) {ai = f ;}
    public synchronized void setB(float f) {b = f ;}
    public synchronized void setAe(float f) {ae = f ;}
    public synchronized void setG(float f) {g = f;}
    public synchronized void setTe(float f) {te = f ;}
    public synchronized void setAc(float f) {ac = f ;}
    public synchronized void setSigmae(float f) {
        sigmae = f ;
        estimateKernelSize();
        form_kernel();}
    public synchronized void setSigmai(float f) {
        sigmai = f ;
        estimateKernelSize();
        form_kernel();}
    public synchronized void setSigmaan(float f) {
        sigmaan = f ;
        estimateKernelSize();
        form_kernel();}
    public synchronized void setAaniso(float f) {
        aaniso = f ;
        form_kernel();}
    public synchronized void setBrightness(float f) {brightness = f ;}
    public synchronized void setAiOrient(float f) {ai_orient = f ;}
    public synchronized void setAeOrient(float f) {ae_orient = f ;}
    public synchronized void setSigmaeOrient(float f) {
        sigmae_orient = f ;
        estimateKernelSize();
        form_kernel_orient();}
    public synchronized void setSigmaiOrient(float f) {
        sigmai_orient = f ;
        estimateKernelSize();
        form_kernel_orient();}
    
    public synchronized float getDt() {return dt;}
    public synchronized float getW() {return w;}
    public synchronized float getTau() {return tau;}
    public synchronized float getAmp() {return amp;}
    public synchronized float getAi() {return ai;}
    public synchronized float getB() {return b;}
    public synchronized float getAe() {return ae;}
    public synchronized float getG() {return g;}
    public synchronized float getTe() {return te;}
    public synchronized float getAc() {return ac;}
    public synchronized float getSigmae() {return sigmae;}
    public synchronized float getSigmai() {return sigmai;}
    public synchronized float getSigmaan() {return sigmaan;}
    public synchronized float getAaniso() {return aaniso;}
    public synchronized float getBrightness() {return brightness;}
    public synchronized float getAiOrient() {return ai_orient;}
    public synchronized float getAeOrient() {return ae_orient;}
    public synchronized float getSigmaeOrient() {return sigmae_orient;}
    public synchronized float getSigmaiOrient() {return sigmai_orient;}
    
    synchronized private void estimateKernelSize()
    {
        float sigma = max(max(max(sigmae,sigmai),sigmaan),max(sigmai_orient,sigmae_orient));
        int ksize = min(max(4,(int)(sigma*6)),64) ;
        System.out.println("kernel size : " + ksize );
        setKernelSize( ksize );
    }
    
    Random   rand   = new Random() ;
    MT       mtrand = new MT() ;
    R250_521 rsrand = new R250_521() ;
    
    public BufferedImage activationmap = new BufferedImage( 
      size, size, BufferedImage.TYPE_INT_RGB ) ;
    public BufferedImage orientationmap = new BufferedImage( 
      size, size, BufferedImage.TYPE_INT_RGB ) ;
    DataBuffer data = activationmap.getRaster().getDataBuffer() ;
    
    public VisualCortex() {
        seed_initial();
        seed_initial_orientation();
        form_kernel();
    }
    
    /* represent a float as a fixed point with 8 bits decimal */
    int toint( float f ) {
        return ( int )( .5 + f * 256 );
    }
    
    float tofloat( int i ) {
        return i * 0.00390625f ;
    }
    
    void seed_initial() {
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ ) 
                u[i][j] = noise();
    }
    
    void seed_initial_orientation() {
         
        int s = 3 ;
        int k = 1 << s ;
        int m = k - 1;
        int h = k / 2 ;
        
        float [][] column = new float[k][k];
        
        float dtheta = (float)(random()*PI);
        for ( int i = 0 ; i < k ; i ++ ) {
            for ( int j = 0 ; j < k ; j ++ ) {
                float dx = j-h ;
                float dy = i-h ;
                column[i][j] = (float)((atan2(dy,dx))%PI);
            }
        }
         
        final BufferedImage im = new BufferedImage( size, size, BufferedImage.TYPE_INT_RGB );
        int T = 0 ;
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ ) 
            {
                int ii = (((i>>s)&1)==1)?(i&m):m-(i&m) ;
                int jj = (((j>>s)&1)==1)?m-(j&m):(j&m) ;
                theta[i][j] = (column[ii][jj]) ;
                im.setRGB(j,i,Color.HSBtoRGB((float)(theta[i][j]/PI), 1, 1));
                xbuff[i][j] = cost[i][j] = toint((float)cos(theta[i][j]));//*weight[ii][jj]) ;
                ybuff[i][j] = sint[i][j] = toint((float)sin(theta[i][j]));//*weight[ii][jj]) ;
                T += 6608099 ;
            }
        
        final int S = 400 ;
    }
    
    float gaussian( float x, float mu, float sigma ) {
        //hack to prevent introducing NaN into the system
        if ( sigma < .1f ) return 0 ;
        return (float)(exp(-.5*pow((x-mu)/sigma,2))/(sigma*sqrt(2*PI)));
    }
    
    void form_kernel() {
        float [][] we = new float[kernelsize][kernelsize];
        float [][] wi = new float[kernelsize][kernelsize];
        float wesum = 0 ;
        float wisum = 0 ;
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
            {
                int iy = i - kc ;
                int jx = j - kc ;
                float rr = (float)sqrt(jx*jx+iy*iy) ;
                wesum += we[i][j] = gaussian( rr, 0, sigmae ) ;
                wisum += wi[i][j] = gaussian( rr, 0, sigmai ) ;
                kani[i][j] = toint(aaniso * gaussian( rr, 0, sigmaan ));
            }
        
        //hack to prevent introducing NaN into the system
        wesum = wesum == 0 ? 0 : ae / wesum ;
        wisum = wisum == 0 ? 0 : ai / wisum ;
        
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
                 wa[i][j] = we[i][j] * wesum - wi[i][j] * wisum ;
        
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ )  
            {
                int ii = i - kc ;
                int ij = j - kc ;
                float th = (float)atan2( ii , ij );
                kcos[i][j] = toint(-(float)cos(th));
                ksin[i][j] = toint(-(float)sin(th));
            }
        form_kernel_orient();
    }
    
    void form_kernel_orient() {
        float [][] we = new float[kernelsize][kernelsize];
        float [][] wi = new float[kernelsize][kernelsize];
        float wesum = 0 ;
        float wisum = 0 ;
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
            {
                int iy = i - kc ;
                int jx = j - kc ;
                float rr = (float)sqrt(jx*jx+iy*iy) ;
                wesum += we[i][j] = gaussian( rr, 0, sigmae_orient  ) ;
                wisum += wi[i][j] = gaussian( rr, 0, sigmai_orient  ) ;
            }
        //hack to prevent introducing NaN into the system
        wesum = wesum == 0 ? 0 : ae_orient / wesum ;
        wisum = wisum == 0 ? 0 : ai_orient / wisum ;
        for ( int i = 0 ; i < kernelsize ; i ++ )
            for ( int j = 0 ; j < kernelsize ; j ++ ) 
                 wa_orient[i][j] = we[i][j] * wesum - wi[i][j] * wisum ;
    }
    
    float f( float x ) {
        return 1/(1+(float)exp(-x)) ;
    }
    
    float heav( float x ) {
        return x < 0f ? 0f : x == 0f ? .5f : 1f ; 
    }
    
    float ff( int t ) {
        //return amp*heav( (float)cos(wdt*t) - thr );
        return amp *( 1 + (float)cos(wdt*t) );
    }
    
    float noise() {
        return (rsrand.randomr()&0xffff)*.0000152590219f;
    }
    
    /* cos( 2 * ( t0 - t1 ) ) in fixed point with 0x100~1.f */
    int costwodtheta( int c0, int s0, int c1, int s1 )
    {
        int cosdtheta = c0*c1 + s0*s1 >> 8;
        return ( cosdtheta * cosdtheta >> 7 ) - 0x100 ;
    }
    
    float coupling3( int ki, int kj, int i2, int j2, int io, int jo ) {
        int ct0 = cost[io][jo];
        int st0 = sint[io][jo];
        return tofloat( kani[ki][kj] * 
           costwodtheta(ct0,st0,cost[i2][j2],sint[i2][j2]) * 
           costwodtheta(ct0,st0,kcos[ki][kj],ksin[ki][kj]) >> 16 ) ;
    }
    
    float hue = 0f ;
    float HALFPI = (float)(.5*PI);   
    
    public synchronized void step()
    {
        stepRange(0,size,0,size);
        /*
        Thread t1 = new Thread() {
            public void run(){
                stepRange(0,size/4,0,size);
            }
        };
        Thread t2 = new Thread() {
            public void run(){
                stepRange(size/4,size/2,0,size);
            }
        };
        Thread t3 = new Thread() {
            public void run(){
                stepRange(size/2,size/4*3,0,size);
            }
        };
        Thread t4 = new Thread() {
            public void run(){
                stepRange(size/4*3,size,0,size);
            }
        };
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        
        try {
            t1.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(VisualCortex.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            t2.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(VisualCortex.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            t3.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(VisualCortex.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            t4.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(VisualCortex.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        t++ ;
        hue += .01f;
        swapbuffers();
    }
    
    public void stepRange( int i1, int i2, int j1, int j2) {
        float ffmte = ff(t)-te ;
        for ( int i = i1 ; i < i2 ; i ++ ) {
            int ioff = i - kc ;
            for ( int j = j1 ; j < j2 ; j ++ ) {
                int joff = j - kc ;
                float ka = ffmte ;
                float a = 0 ;
                for ( int ii = 0 ; ii < kernelsize ; ii++ )
                    for ( int ij = 0 ; ij < kernelsize ; ij++ ) 
                    {
                        int I = ii+ioff&mask ;
                        int J = ij+joff&mask ;
                        ka += (wa[ii][ij]+coupling3(ii,ij,I,J,i,j))*u[I][J];
                        a += wa_orient[ii][ij] * u[I][J];
                    }
                float U = u[i][j] ;
                float V = v[i][j] ;
                ubuff[i][j] = c1*U+dt*f(ka-g*V+ffmte+ac*noise());
                v[i][j]    += (U - b*V)*dtovertau ;
                a = f(a+brightness);
                activationmap.setRGB(j,i,Color.HSBtoRGB(hue + a*HALFPI,1f-a,a));
            }
        }
    }
    
    
    private void swapbuffers() {
        float[][] temp = u ;
        u = ubuff ;
        ubuff = temp ;
        swaporientbuffers();
    }
    
    void swaporientbuffers() {
        int[][] temp = cost ;
        cost = xbuff ;
        xbuff = temp ;
        temp = sint ;
        sint = ybuff ;
        ybuff = temp ;
    }
    
    
    public Controller makeController(Point C, double R) {
        Controller c = new Controller(this,C);
        int S = 20 ; 
        c.addControl("w"            ,0  ,10 ,.2f    ,2*R/S);
        c.addControl("tau"          ,0  ,30 ,10f    ,3*R/S);
        c.addControl("amp"          ,0  ,10 ,1      ,4*R/S);
        c.addControl("ai"           ,0  ,10 ,2f     ,5*R/S);
        c.addControl("b"            ,0  ,1, .2f     ,6*R/S);
        c.addControl("ae"           ,0  ,10 ,6.23f  ,7*R/S);
        c.addControl("g"            ,0  ,10 ,4f     ,8*R/S);
        c.addControl("te"           ,0  ,10 ,.25f   ,9*R/S);
        c.addControl("ac"           ,0  ,50 ,.4f    ,10*R/S);
        c.addControl("sigmae"       ,.8 ,32 ,2      ,11*R/S);
        c.addControl("sigmai"       ,.8 ,32 ,8      ,12*R/S);
        c.addControl("sigmaan"      ,.8 ,32 ,10     ,13*R/S);
        c.addControl("aaniso"       ,0  ,10 ,.1f    ,14*R/S);
        c.addControl("brightness"   ,0  ,10 ,0f     ,15*R/S);
        c.addControl("ai_orient"    ,0  ,50 ,2f     ,16*R/S);
        c.addControl("ae_orient"    ,0  ,50 ,6.23f  ,17*R/S);
        c.addControl("sigmae_orient",0  ,50 ,2      ,18*R/S);
        c.addControl("sigmai_orient",0  ,50 ,8      ,19*R/S);
        c.addControl("dt"           ,0  ,1  ,.1f    ,20*R/S);
        return c;
    }
}