package perceptron;
/* FractalMap.java
 *
 * Version 1.0 (2010)
 *
 * @author Michael Everett Rule
 *
 *  public class FractalMap with
 *  public FractalMap(DoubleBuffer b, Vector<Mapping> maps, Perceptron parent)
 *
 */

import math.MathToken;
import math.complex;
import math.Equation;
import math.ComplexContex;
import java.awt.Color;
import java.util.ArrayList;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static math.complex.arg;
import static math.complex.mod;
import static util.Misc.clip;
import static util.Misc.wrap;
import static math.complex.polar;
import color.RGB;
import color.RGB15;
import image555.DoubleBuffer555;
import static java.awt.Color.HSBtoRGB;
import java.awt.image.DataBuffer;
import static java.lang.Math.random;
import static java.lang.System.currentTimeMillis;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newFixedThreadPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import static util.Sys.sout;

/**
 * @author Michael Everett Rule
 */
public class Map {
    
    // Experimental: render scanlines in parallel
    // we limit this to two threads to avoid overloading the system
    //ExecutorService executor = newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    ExecutorService ex = newFixedThreadPool(8);
    
    // Status of 8 to 5 bit conversion: 
    // in circle PR AX AY in_circle all in 8bit
    // not sure what's happening in TranslucentLayer, maybe bad things
    // map fade in 8bit (ok since map in 8 bit)
    // offset in 8bit getOffset in cache
    // Some of the caching just sloppily converts 8bit -> 5bit, come back here
    // divergence weight cache now 5 bit
    // NEED TO MOVE fxy cache to 5 bit
    // tint_level now 5-bit
    // color_dampen now 5-bit
    // color registers now 5-bit (maybe some bugs)
    // motion blur and noise level now in 5-bit
    // outops now in 5-bit 
    // Map is in 8-bit fixed point
    // Bounds test in 8-bit
    // Color now in 5-bit with 5-bit alpha
    // Gradient now 5-bit weights 
    // gradops don't do anything besides provide gradient operation names
    // gradops now handled in cache translucent
    // TODO try to resume precomputing gradient
    // switching gradient weights back to 8bit; will dither+collapse at runtime
    
    /** Bit-masked for AARRGGBB 8-bit color data */
    final static int 
            R8  = 0xFF0000, 
            G8  = 0x00FF00, 
            B8  = 0x0000FF, 
            M8  = 0xFF00FF,
            ME8 = 0x800080,
            GE8 = 0x008000;

    // Constants for manipulating int050505 packed colors
    public final static int
        SHIFT = 5,
        UNIT  = 1 << SHIFT, // 32
        MASK  = UNIT -  1,  // 31
        ROUND = MASK >> 1,  // 16
        R5 = 0b0000001111100000000000000000000,
        G5 = 0b0000000000000000111110000000000,
        B5 = 0b0000000000000000000000000011111,
        W5 = 0b0000001111100000111110000011111,
        O5 = 0b0000000000100000000010000000001,
        E5 = O5*ROUND;
    
    // Colors for int050505 format
    public final static int
        BLACK5   = 0,
        RED5     = 0b0000001111100000000000000000000,
        GREEN5   = 0b0000000000000000111110000000000,
        BLUE5    = 0b0000000000000000000000000011111,
        YELLOW5  = RED5  |GREEN5,
        MAGENTA5 = RED5  |BLUE5,
        CYAN5    = GREEN5|BLUE5,
        WHITE5   = RED5  |GREEN5|BLUE5;
    
    // Desired rectangular span of complex plane on screen
    // Changing this changes everything! Presets break. 
    public static final float ZSCALE = 2.8f;//2.4f;
    
    // Color model in use (experimental, keep at c24 for now).
    final static RGB rgb = RGB.c15;
    
    Perceptron           P;
    DoubleBuffer555 B;
    ArrayList<Mapping> maps;
    boolean            running = false;
    
    //DataBuffer 
    int []
        out, // current image on the screen
        buf, // image buffer after mapping
        img, // external, loaded image
        dsp; // cursors, circles, dots

    DataBuffer db; // current output data buffer
    
    // Control interpretation of translation, rotation + mirroring modes
    public int 
        offset_mode = 0,
        rotate_mode = 2,
        mirror_mode = 0;
    public static final int 
        // Constants for identifying the translation & rotation modes
        POSITION = 0,
        VELOCITY = 1,
        LOCKED   = 2,
        // For identifying mirroring modes
        NOMIRROR        = 0,
        HORIZ = 1,
        VERT   = 2,
        TURN       = 3,
        QUAD   = 4;
    public final String [] 
        translate_modes = {"Position","Velocity","Locked"},
        mirror_modes = {"None","Horizontal","Vertical","Turn","Quadrant"};
    public void nextOrthoMode(int i) {offset_mode = wrap(offset_mode + i, 3);}
    public void nextPolarMode(int i) {rotate_mode = wrap(rotate_mode + 1, 3);}
    public void nexMirrorMode(int i) {mirror_mode = wrap(mirror_mode + 1, 5);}
    
    // Mistakes were made in this design
    final int W, H, W7, H7, W8, H8, MLEN, M2, MAXC=65535;
    final int w5, h5;
    final float cW, cH, oW2, oH2, oW7, oH7, z2mapW, z2mapH, z2W, z2H;
    
    /** The upper left, the lower right, and the size of the rectangle in the
     * complex plane. These are re-initialized so these values don't
     * matter.     */
    public static final complex size = new complex(6f, 6f);
    public static final complex UL   = new complex();
    public static final complex LR   = new complex();
    public static final double  PI_OVER_TWO = Math.PI * .5;
    
    // Accumulators for velocity mode
    private float dr = 0, di = 0, theta = .1f;
    
    // Coordinate lookup tables
    float   [] PR,AX,AY,FW;
    boolean [] in_circle;
    
    /** Create new FractalMap
     * @param b
     * @param maps
     * @param parent */
    //public Map(DoubleBuffer b, ArrayList<Mapping> maps, Perceptron parent) {
    public Map(DoubleBuffer555 b, ArrayList<Mapping> maps, Perceptron parent) {
        P = parent;
        this.maps = null==maps? new ArrayList<>() : maps;
        // Screen width and height, and related constants.
        // We need to remove most of these
        B   = b;
        W   = b.out.img.getWidth();
        H   = b.out.img.getHeight();
        w5  = W<<5;  h5  = H<<5;
        W7  = W<<7;  H7  = H<<7;
        W8  = W<<8;  H8  = H<<8;
        oW2 = 2f/W;  oH2 = 2f/H;
        oW7 = 1f/W7; oH7 = 1f/H7;
        cW  = oW7*oW7;  cH  = oH7*oH7;
        MLEN= W*H;   M2  = MLEN<<1;
        if (W >= H) {size.real=ZSCALE*W/H; size.imag=ZSCALE;    }
        else        {size.real=ZSCALE;     size.imag=ZSCALE*H/W;}
        vars.set('s'-'a',new complex(size));
        vars.set('w'-'a',new complex(size.real));
        vars.set('h'-'a',new complex(size.imag));
        LR.real = size.real/2f; LR.imag = size.imag/2f;
        UL.real = -LR.real;     UL.imag = -LR.imag;
        z2mapW  = size.real/W;  z2mapH = size.imag/H;
        z2W = z2H = (W/size.real+H/size.imag) * .5f;
        // Initialize modular rendering functions
        bounds_i  = wrap(bounds_i, bounds.length);
        outi      = wrap(outi, outops.length);
        grad_mode = wrap(grad_mode, grad_modes.length);
        syncOps();
        if (!maps.isEmpty()) mapping = maps.get(map_i %= maps.size());
        // Prepare the gradient maps
        int hW = W/2, hH=H/2;
        grads = new char[9][MLEN];
        int i = 0;
        
        for (int y=-hH; y<hH; y++) for (int x=-hW;x<hW; x++) {
            grads[0][i]=dither(2*(x*x/(float)(W*W)+y*y/(float)(H*H)));
            grads[1][i]=dither((x+hW)/(float)W);
            grads[2][i]=dither((y+hH)/(float)H);
            grads[3][i]=dither((y+hH)*(x+hW)/(float)(W*H));
            grads[4][i]=dither((abs(x)+abs(y))/(float)(hW+hH));
            grads[5][i]=dither((abs(x)*abs(y))/(float)(hW*hH));
            grads[6][i]=dither(min(1,sqrt(x*x+y*y)/min(hH,hW)));
            grads[7][i]=dither(pow(min(1,sqrt(x*x+y*y)/min(hH,hW)),9));
            grads[8][i]=dither(pow(max(0,min(1,1.3-(sqrt(x*x+y*y))/(min(hH,hW)))),9));
            i++;
        }
        grad_i = 0;
        grad   = grads[grad_i];
        // Prepare the buffers for the mapping f(z). 
        map     = new int[M2];
        map_buf = new int[M2];
        zconv   = new boolean[M2];
        computeLookup();
        // Prepare the mapping f(z). 
        offset   = new complex();
        rotation = new complex();
        normc    = new float[]{0, 0};
        offset.real=offset.imag=0;
        // Prepare radius lookup tables
        i=0;
        PR = new float[MLEN];
        AX = new float[MLEN];
        AY = new float[MLEN];
        FW = new float[MLEN];
        in_circle = new boolean[MLEN];
        for (int y=0; y<H; y++) for (int x=0; x<W; x++) {
            float px = x/(float)W*2-1;
            float py = y/(float)H*2-1;
            PR[i] = (px*px+py*py);
            AX[i] = abs(px);
            AY[i] = abs(py);
            in_circle[i] = PR[i]<0.995;
            
            int fx = x*256+128;
            int fy = y*256+128;
            float qx = fx*oW7-1;
            float qy = fy*oH7-1;
            float qr = (float)sqrt(qx*qx+qy*qy);
            FW[i] = qr;
            i++;
        }
        cache = new Cache();
    }

    public void syncOps() {
        bound_op = bounds[bounds_i];
        outop    = outops[outi];
        grad_op  = grad_modes[grad_mode];
    }
    
    private static char dither(double w) {
        return (char)clip((int)(w*255 + random()),0,255);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Caching /////////////////////////////////////////////////////////////////
    // Work in progress. Move more of the rendering to be pre-computed. 
    // Cache gradient premultiplied with tint
    // initialize after setting MLEN to avoid weirdness
    final Cache cache;
    /**
     * The rendering cache. 
     * Compute values in preparation for the next frame in parallel. 
     *  - Compose the gradient and tint into a single translucent layer
     *  - If needed, update the recurrent map as well as the exterior image. 
     */
    public class Cache {
        public TranslucentLayer 
            trans  = new TranslucentLayer(),
            trans_ = new TranslucentLayer();
        public MapCache 
            map_cache  = new MapCache(),
            map_cache_ = new MapCache();
        AtomicBoolean 
            map_stale = new AtomicBoolean(true),
            trn_ready = new AtomicBoolean(false),
            map_ready = new AtomicBoolean(false);
        public synchronized void cache() {
            if (map_stale.getAndSet(false) || map_cache.looksStale()) {
                map_cache_.cache();
                map_ready.set(true);
            }
            if (trans.looksStale()) {
                sout(currentTimeMillis()+" Overlay is stale");
                trans_.cache();
                trn_ready.set(true);
            }
        }
        public synchronized void flip() {
            if (trn_ready.getAndSet(false)) {
                TranslucentLayer tempt = trans;
                trans = trans_;
                trans_ = tempt;
            }
            if (map_ready.getAndSet(false)) {
                MapCache tempm = map_cache;
                map_cache = map_cache_;
                map_cache_ = tempm;
            }
        }
        /**
         * Merge translucent effects into a single layer.
         */
        public class TranslucentLayer {
            final char[] A = new char[MLEN]; // alpha values in [0..256]
            boolean off = true;           // whether layer is enabled
            // Track these so we can tell if they change
            private int grad_mode, grad_i, mirror_mode;
            private float gslope, gbias;
            public synchronized void cache() {
                // Track these so we can tell if they change
                grad_mode  = Map.this.grad_mode;
                grad_i     = Map.this.grad_i;
                gslope     = Map.this.gslope;
                gbias      = Map.this.gbias;
                mirror_mode= Map.this.mirror_mode;
                scanMirror((a,b)->{
                    for (int i=a;i<b;i++) 
                        A[i]=(char)clip(grad_modes[grad_mode].w(i),0,255);
                });
            }
            public boolean looksStale() {
                return 
                    grad_mode != Map.this.grad_mode||
                    grad_i != Map.this.grad_i||
                    gslope != Map.this.gslope||
                    gbias  != Map.this.gbias||
                    mirror_mode!=Map.this.mirror_mode;
            }
        }
       
        /** 
         * Apply the current translation and rotation to the map.
         */
        public class MapCache {
            final int     [] fxy    = new     int[MLEN*2];
            final char    [] rate   = new    char[MLEN];
            final boolean [] bounds = new boolean[MLEN];
            final int     [] image  = new     int[MLEN];
            private int f1,mirror_mode;
            private int [] offset = new int[2];
            private complex oldr;
            private boolean did_image;
            public boolean looksStale() {
                if (mirror_mode !=Map.this.mirror_mode) return true;
                if ((outi==3||outi==4)!=this.did_image) return true;
                if ((int)(map_fade*256 + 0.5)!=this.f1) return true;
                int [] c = getOffset();
                if (c[0]!=this.offset[0] || c[1]!=this.offset[1]) return true;
                complex r = getRotation();
                return (this.oldr.minus(r).rSquared()>1e-5);
            }
            public synchronized void cache() {
                final int f1 = (int)(map_fade*256 + 0.5);
                this.mirror_mode = Map.this.mirror_mode;
                this.f1          = f1;
                this.offset      = getOffset();
                this.oldr        = getRotation();
                this.did_image   = outi==3||outi==4;
                final complex R  = oldr;
                final int CX = this.offset[0], CY = this.offset[1];
                scanMirror((a,b)->{scanline(a,b,CX,CY,R,f1);});
            }
            private int[] getOffset() {
                switch (offset_mode) {
                    case POSITION: return new int[]{
                        (int)(.5f+256*normc[0])+W7,
                        (int)(.5f+256*normc[1])+H7}; 
                    case VELOCITY: return new int[]{
                        (int)(.5f+256*((dr=(abs(dr)>MAXC)?0:dr+.1f*normc[0])))+W7,
                        (int)(.5f+256*((di=(abs(di)>MAXC)?0:di+.1f*normc[1])))+H7};
                    default: return new int[]{W7,H7};
                }
            }
            private complex getRotation() {
                switch (rotate_mode) {
                    case POSITION: return new complex(rotation);
                    case LOCKED:   return polar(1f,(float)PI_OVER_TWO); 
                    case VELOCITY: 
                        theta += arg(rotation)*.01f; 
                        return polar(mod(rotation),theta);
                }
                return null;
            }
            private int scanline(int start, int stop,int cx,int cy,complex r, int f1) {
                float RX = r.real, RY = r.imag, RC = r.real+r.imag;
                // Linear Congruential RNG seeded with stronger RNG at start of scan
                int rn = (int)((long)(Math.random()*4294967296L)&0xffffffff)|1,
                    rA = 0x343FD, rC = 0x269EC3;
                for (int i=start; i<stop; i++) {
                    int j = i<<1;
                    int real, imag;
                    if (f1 > 0) {
                        real = map_buf[j  ] + (map[j  ]*f1 >> 8);
                        imag = map_buf[j|1] + (map[j|1]*f1 >> 8);
                    } 
                    else {real = map[j]; imag = map[j|1]; } 
                    float x4 = imag * RX;
                    float x5 = real * RY;
                    int fx = cx + (int)(x5 + x4 + 0.5f);
                    int fy = cy + (int)(x5 - x4 + RC*(imag-real) + 0.5f);
                    fxy [j  ] = fx>>3;
                    fxy [j|1] = fy>>3;
                    bounds[i] = bound_op.test(fx,fy,i);
                    rate[i] = divergenceRate(fx,fy,i,((rn=rn*rA+rC)>>SHIFT));
                    if (did_image) image[i] = B.img.get.it(fx>>3,fy>>3);
                }
                return 0;
            }
            
            char divergenceRate(int fx,int fy,int i, int q) {
                float pr,qr;
                switch (Map.this.bounds_i) {
                    case 2: {// Horizon
                        pr = AY[i];
                        qr = abs(fy * oH7 - 1);
                    } break;
                    case 1: {// Oval
                        pr = PR[i];
                        fx-= W7; fy-= H7;
                        qr = (fx*(fx*cW)+fy*(fy*cH));
                        pr = (float)Math.sqrt(pr);
                        qr = (float)Math.sqrt(qr);
                    } break;
                    case 3: {// Radius
                        pr = PR[i];
                        fx-= W7; fy-= H7;
                        qr = (fx*(fx*cW)+fy*(fy*cH));
                        pr *= inverse_radius;
                        qr *= inverse_radius;
                        pr = (float)Math.sqrt(pr);
                        qr = (float)Math.sqrt(qr);
                    } break;
                    case 0: // Screen
                    case 4: // Newton
                    case 5: // None
                    default: {
                        pr = (float)max(AX[i],AY[i]);
                        qr = (float)max(abs((float)fx*oW7-1),abs((float)fy*oH7-1));
                    }}
                float w1 = clip(invert_bound?pr-1:1-pr,0f,1f);
                float w2 = clip(invert_bound?1-qr:qr-1,0f,1f);
                w1 /= w1+w2;
                //return dither(w1);
                
                return (char)clip((int)(w1*0xFFFF) + (q&0xFF) >> 8,0,255);
            }
        }
    }
    
    private void runAll(Runnable [] fn) {
        int N = fn.length;
        Future f[] = new Future[N];
        for (int i=0;i<N;i++) f[i]=ex.submit(fn[i]);
        for (int i=0;i<N;i++) try {
            f[i].get();
        } catch (InterruptedException | ExecutionException ex) {
            fn[i].run();
        }
    }
    private void scanAll(BiConsumer<Integer,Integer> fn) {
        Runnable [] f= new Runnable[H];
        for (int y=0; y<H; y++) {
            final int a=y*W, b=y*W+W;
            f[y]=()->fn.accept(a,b);
        }
        runAll(f);
    }
    private void scanLeft(BiConsumer<Integer,Integer> fn) {
        Runnable [] f= new Runnable[H];
        int hW=(W+1)/2;
        for (int y=0; y<H; y++) {
            final int a=y*W, b=y*W+hW;
            f[y]=()->fn.accept(a,b);
        }
        runAll(f);
    }
    private void scanTop(BiConsumer<Integer,Integer> fn) {
        int hH=(H+1)/2;
        Runnable [] f= new Runnable[hH];
        for (int y=0; y<hH; y++) {
            final int a=y*W, b=y*W+W;
            f[y]=()->fn.accept(a,b);
        }
        runAll(f);
    }
    private void scanCorner(BiConsumer<Integer,Integer> fn) {
        int hW=(W+1)/2, hH=(H+1)/2;
        Runnable [] f= new Runnable[hH];
        for (int y=0; y<hH; y++) {
            final int a=y*W, b=y*W+hW;
            f[y]=()->fn.accept(a,b);
        }
        runAll(f);
    }
    private void scanMirror(BiConsumer<Integer,Integer> fn) {
        switch (mirror_mode) {
            case NOMIRROR:        scanAll   (fn); break;
            case HORIZ:           scanLeft  (fn); break;
            case VERT: case TURN: scanTop   (fn); break;
            case QUAD:            scanCorner(fn); break;
            default: throw new RuntimeException("Mirror should be in 0..3");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Apply current map to screen raster, storing result in output raster. 
    public synchronized void operate() {
        this.out = B.out.buf;
        this.buf = B.buf.buf;
        this.dsp = B.dsp.buf;
        this.db  = B.buf.img.getRaster().getDataBuffer();
        if (B.img != null) this.img = B.img.buf;
        
        scanMirror(this::rend);
        if (mirror_mode==HORIZ|mirror_mode==QUAD) for (int y=0; y<H; y++) {
            int i=y*W+W-1,j=y*W; 
            for (int x=0;x<(W+1)/2;x++) 
                db.setElem(i--,db.getElem(j++));
        }
        if (mirror_mode==VERT |mirror_mode==QUAD) for (int y=0;y<(H+1)/2;y++) {
            int i=W*(H-1-y),j=W*y; 
            for (int x=0;x<W;x++) 
                db.setElem(i+x,db.getElem(j+x));
        }
        if (mirror_mode==TURN) {
            int i=MLEN-1; 
            for (int j=0; j<(MLEN+1)/2; j++) 
                db.setElem(i--,db.getElem(j));
        }
        
        // Update map cross-fader
        if (map_fade > 0) {
            map_fade -= .05f;
            if (map_fade <= 0) {int[] t=map;map=map_buf;map_buf=t;}
        } 
        
        // Update flavor color registers
        updateColorRegisters();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // The main rendering operator /////////////////////////////////////////////
    private void rend(int start, int end) {
            
        // Linear Congruential RNG seeded with stronger RNG at start of scan
        int rn = (int)((long)(Math.random()*4294967296L)&0xffffffff)|1,
            rA = 0x343FD, rC = 0x269EC3;
        
        int     [] fxy  = cache.map_cache.fxy;
        char    [] rate = cache.map_cache.rate;
        boolean [] in   = cache.map_cache.bounds;
        char    [] A    = cache.trans.A;
        
        int j = start*2;
        for (int i = start; i<end; i++) {
            // random noise: dithering or a useful addition
            int q  = ((rn=rn*rA+rC)>>SHIFT)&W5;
            int eq = q&E5;
            int fx = fxy[j++];
            int fy = fxy[j++];
            boolean isout = in[i]==invert_bound;
            int c = isout? outop.f(fx,fy,i,rate[i],q)^color_mask 
                         : B.out.get.it(fx, fy)^feedback_mask;
            if (isout || P.fore_tint) {
                if (grad_mode>0) {
                    int w = A[i]+(0b111&q)>>3;
                    int k = gc1;
                    if (grad_mode==2) k=W5&(k+((gc2-k)*w+eq>>SHIFT));
                    c=W5&(c+((k-c)*w+eq>>SHIFT) );
                }                         
                c=W5&(c+((tint_color-c)*tint_level+eq>>SHIFT));
            }
            c=W5&(c+((q-c)*noise_level+eq>>SHIFT));
            c=W5&(c+((buf[i]-c)*motion_blur+eq>>SHIFT));
            
            // The output buffer is immediately converted to intRGB
            // after we return from here. Can we speed things up by doing this
            // conversion in the reindering loop (below?)
            // In fact we NEVER use the raterized output
            // (we'll move the blur/color operations to immediately before)
            c = (c<<3) | (q&0b000000011100000001110000000111);
            db.setElem(i,
                ((c & 0b000001111111100000000000000000000)>>4)|
                ((c & 0b000000000000000111111110000000000)>>2)|
                ((c & 0b000000000000000000000000011111111))
            );
        }
    }
    
    
    /**
     * Apply gradient and tinting operations. 
     * These operations have been separated from the main rendering loop
     * so that we may optionally call them either before or after the mapping
     * operation. This allows us to flip whether the gradient is drawon on
     * top with the P.objects_on_top flag. 
     */
    void applyGradientAndTint() {
        // TODO
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Color registers /////////////////////////////////////////////////////////
    // we'll make these available to various things, including
    // - Gradient color 1
    // - Gradient color 2
    // - Boundary color
    // - Tint color
    // grad_accent: used as the 2nd gradient color
    // fade_color: used as the 1st gradient color
    // fade_color: use for outside coloring 
    // fade_color: used for bar coloring
    // Colors: 0black 1white 2midhue 3midinv 4midhuerot 5huerot
    public int gcolor1_i    = 4; // Gradient color index used in modes 1 and 2
    public int gcolor2_i    = 0; // Accent color index if in gradient mode 2
    public int barcolor_i   = 0; // H/V bar colors (if present)
    public int tintcolor_i  = 0; // Global (uniform) tinting
    public int outcolor_i   = 4; // Color used to fill in for boundary mode 0
    public int gc1 = 0, gc2 = 0; // Colors used for the gradient 
    public int bar_color    = 0; // Edge bar colors
    public int tint_color   = 0; 
    public int out_color    = 0; 
    public int pout_color   = 0; 
    public int tint_level   = 0;
    public int color_dampen = UNIT>>1;
    
    //public void setGColor1(int i) {fade_op = fade_colors[gradcolor1_i = wrap(i, N_COLOR_REGISTERS)];}
    public void setGColor1   (int i) {gcolor1_i   =wrap(i,N_COLOR_REGISTERS);}
    public void setGColor2   (int i) {gcolor2_i   =wrap(i,N_COLOR_REGISTERS);}
    public void setBarColor  (int i) {barcolor_i  =wrap(i,N_COLOR_REGISTERS);}
    public void setTintColor (int i) {tintcolor_i =wrap(i,N_COLOR_REGISTERS);}
    public void setOutColor  (int i) {outcolor_i  =wrap(i,N_COLOR_REGISTERS);}
    public void setTintLevel (int i) {tint_level  =clip(i,0,MASK);}
    public void setColorDamp (int i) {color_dampen=clip(i,0,MASK);}
    public void nextGColor1  (int n) {setGColor1  (gcolor1_i   +n);}    
    public void nextGColor2  (int n) {setGColor2  (gcolor2_i   +n);}    
    public void nextBarColor (int n) {setBarColor (barcolor_i  +n);}    
    public void nextOutColor (int n) {setOutColor (outcolor_i  +n);}    
    public void nextTintColor(int n) {setTintColor(tintcolor_i +n);}    
    public void nextTintLevel(int n) {setTintLevel(tint_level  +n);}
    public void nextColorDamp(int n) {setColorDamp(color_dampen+n);}
    public final int N_COLOR_REGISTERS = 7;
    public final String [] color_register_names = {
        "Black",
        "White",
        "Mid:Hue",
        "Mid:Invert",
        "Mid:Spin",
        "Hue Spin",
        "Previous Out"
    };
    public final int[] color_registers = new int[N_COLOR_REGISTERS];
    private int rotation_hue = 0;
    public void updateColorRegisters() {
        color_registers[0] = BLACK5;
        color_registers[1] = WHITE5;
        int MMID   = (W/2) + W*(H/2);
        //int middle = buf.getElem(MMID);
        int middle = out[MMID];
        
        int middleRGB = rgb.toIntRGB(middle);
        float[] HSV = java.awt.Color.RGBtoHSB(
                (middleRGB >> 16) & 0xff,
                (middleRGB >> 8 ) & 0xff,
                (middleRGB      ) & 0xff,
                new float[]{0, 0, 0});

        color_registers[2] = rgb.fromIntRGB(HSBtoRGB(HSV[0], 1f, 1f));
        color_registers[3] = W5&(~middle);
        color_registers[4] = rgb.fromIntRGB(HSBtoRGB(HSV[0]+0.2f, 1f, 1f));
        rotation_hue++;
        color_registers[5] = rgb.fromIntRGB(HSBtoRGB(rotation_hue*.01f, 1f, 1f));
        color_registers[6] = out_color;
        
        // TODO: need to also apply color transform and tint from previous frame here?
        pout_color = out_color^feedback_mask^color_mask; // needed for special edge mode
        
        if (color_dampen>0) {
            gc1        = rgb.blend(gc1,        color_registers[gcolor1_i  ],color_dampen);
            gc2        = rgb.blend(gc2,        color_registers[gcolor2_i  ],color_dampen);
            bar_color  = rgb.blend(bar_color,  color_registers[barcolor_i ],color_dampen);
            out_color  = rgb.blend(out_color,  color_registers[outcolor_i ],color_dampen);
            tint_color = rgb.blend(tint_color, color_registers[tintcolor_i],color_dampen);
        } else {
            gc1        = color_registers[gcolor1_i  ];
            gc2        = color_registers[gcolor2_i  ];
            bar_color  = color_registers[barcolor_i ]; 
            out_color  = color_registers[outcolor_i ];
            tint_color = color_registers[tintcolor_i];
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Noise and Motion blur. //////////////////////////////////////////////////
    public int motion_blur = 0;
    public int noise_level = 0;
    public void setMotionBlur(int k) {motion_blur = clip(k,0,UNIT);}
    public void setNoise     (int i) {noise_level = clip(i,0,UNIT);}
    public void nextNoise    (int n) {setNoise(noise_level+n);}
    
    ////////////////////////////////////////////////////////////////////////////
    // Describe what to do when a pixel is out of bounds ///////////////////////
    public PixOp outop;
    public int   outi = 4; // 0:color 1:edge  2:repeat  3:image  4:fade 5:special
    public void nextOutside(int n) {setOutside(outi + n);}
    public void setOutside(int index) {outop = outops[outi = wrap(index, outops.length)];}
    public abstract class PixOp {
        public final String name;
        public PixOp(String s){this.name=s;}
        abstract int f(int x, int y, int i, int w,int q);
    }
    public final PixOp[] outops = {
        new PixOp("Color"){int f(int x,int y,int i,int w,int q){return out_color; }},
        new PixOp("Edge" ){int f(int x,int y,int i,int w,int q){return B.buf.get.it(clip(x,0,w5-1),clip(y,0,h5-1));}},
        new PixOp("Tile" ){int f(int x,int y,int i,int w,int q){return B.buf.get.it(x,y);}},
        new PixOp("Image"){int f(int x,int y,int i,int w,int q){return cache.map_cache.image[i];}},
        new PixOp("Fade") {int f(int x,int y,int i,int w,int q){
            w = w+(q&7)>>3;
            return rgb.blend(B.buf.get.it(x,y), cache.map_cache.image[i], w, q);
        }},
        // TODO: broke these
        new PixOp("Rate1"){int f(int x,int y,int i,int w,int q){
            w = w+(q&7)>>3;
            return rgb.blend(out_color, bar_color, w, q);
        }},
        new PixOp("Rate2"){int f(int x,int y,int i,int w,int q){
            w=(256-w)*w;
            w=w*w+(((1<<23)-1)&q)>>23;
            return rgb.blend(out_color, bar_color,w, q);
        }},
        // Should probably just delete this one
        new PixOp("Rate3"){int f(int x,int y,int i,int w,int q){
            w=((3*256-2*w)*w*w);
            w=w + (q&0x7ffff)>>(2*8+3);
            int c=rgb.blend(
                    B.buf.get.it(x,clip(y,0,h5-1)),
                    out_color, 
                    w, q);
            return c;
        }},
    };
    
    ////////////////////////////////////////////////////////////////////////////
    // Fractal map control /////////////////////////////////////////////////////
    public int       map_i = 0;// Index into list of built-in maps
    public Mapping   mapping;  // The fractal mapping f(z)
    public int[]     map;      // Lookup table for current map
    public complex   offset;   // The "+c" in a Julia set iteration 
    public complex   rotation; // Rotation/scale of the complex map
    public float[]   normc;    // Offset constant in screen coordinates
    public boolean[] zconv;    // "converged" points LUT for Newton fractals
    public int[]     map_buf;  // Additional map LUT to use when transitioning
    public float     map_fade; // Frame counter used to transition maps smoothly
    public void setMap(Mapping map) {mapping = map; computeLookup();}
    public void setMap(int index) {setMap(maps.get(map_i=wrap(index,maps.size())));}
    public void setMap(final String s) {setMap(makeMap(s));}
    public void nextMap(int n) {setMap(map_i + n);}
    public void setNormalizedConstant(float x, float y) {
        if (normc  == null) normc  = new float[]{0, 0};
        if (offset == null) offset = new complex(0, 0);
        offset.real = (W - x) / z2W + UL.real;
        offset.imag = (H - y) / z2H + UL.imag;
        normc[0] = (offset.real * z2W);
        normc[1] = (offset.imag * z2H);
    }
    public void setNormalizedRotation(float x, float y) {
        rotation.real = ((W-x) / z2W + UL.real);
        rotation.imag = ((H-y) / z2H + UL.imag);
        float h = complex.arg(rotation);
        float r = complex.mod(rotation) * 2; 
        inverse_radius = complex.mod(rotation);
        radius = 1 / inverse_radius;
        rotation = complex.polar(1 / r, h);
    }
    final static ComplexContex vars = ComplexContex.standard();
    public static abstract class Mapping {
        public final String s;
        public Mapping(String S){s=S;}
        public String toString() {return s;}
        public abstract complex f(complex z);
    }
    public static Mapping makeMapStatic(final String s) {
        try {
            final Equation e = MathToken.toEquation(s);
            vars.set('s'-'a',new complex(ZSCALE,ZSCALE));
            vars.set('w'-'a',new complex(ZSCALE));
            vars.set('h'-'a',new complex(ZSCALE));
            return new Mapping(s) {public complex f(complex z) {vars.set(25,z);return e.eval(vars);}};
        } catch (Exception e) {
            sout("!!!! Error parsing map "+s);
            e.printStackTrace();
            throw e;
        }
    }
    public Mapping makeMap(final String s) {
        final Equation e = MathToken.toEquation(s);
        vars.set('s'-'a',size);
        vars.set('w'-'a',new complex(size.real));
        vars.set('h'-'a',new complex(size.imag));
        return new Mapping(s) {public complex f(complex z) {vars.set('z'-'a',z);return e.eval(vars);}};
    }
    private synchronized void computeLookup() {
        // If we're transition freeze this intermediate state
        // The itermediate map will be stored in the new "map" buffer
        // If currently interpolating: 
        //      map_buf: map we're interpolating TO
        //      map: FROM - TO
        // If not interpolating:
        //      map: current map
        //      map_buf and map_temp: ignored
        // When we're done here:
        //      map: current interpolated map
        //      map_buf: most recent map we transitions TO
        if (map_fade > 0) {
            int f1 = (int) (256 * map_fade + 0.5);
            for (int i=0; i<M2; i++) map[i] = map_buf[i] + (map[i]*f1 >> 8);
        }
        // Compute new map
        complex z = new complex();
        int i = 0, k = 0;
        float x0 = UL.real;
        float y0 = UL.imag;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                z.real = x*z2mapW + x0;
                z.imag = y*z2mapH + y0;
                complex Z = mapping.f(z); 
                map[i] -= (map_buf[i] = ((int)(0x100*(z2W*(Z.real-x0)))-W7)); // FROM-TO
                i++;
                map[i] -= (map_buf[i] = ((int)(0x100*(z2H*(Z.imag-y0)))-H7));
                i++;
                // Precomputed convergence test (prdrag's addition)
                // TODO: this needs to be moved to the cache so it can adapt to 
                zconv[k++] = (Z.real-z.real)*(Z.real-z.real)+(Z.imag-z.imag)*(Z.imag-z.imag)>0.1f;
            }
        }
        map_fade = 1.f;
        if (null!=cache) cache.map_stale.set(true);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // The boundary conditions for the Julia set. "R" //////////////////////////
    public Bound   bound_op;
    public int     bounds_i       = 1;     // 0:screen 1:circle 2:elastic 3:horizon 4:Newton 5:none
    public boolean invert_bound   = false; // Invert boundary condition?
    public float   radius         = 1;     // adjust radius with map scale (mode 2)
    public float   inverse_radius = 1;     // adjust radius with map scale (mode 2)
    public void nextBound(int n) {setBound(bounds_i + n);}
    public void setBound(int index) {
        bound_op = bounds[bounds_i = wrap(index, bounds.length)]; 
        cache.map_stale.set(true);
    }
    public abstract class Bound {
        public final String name;
        public Bound(String name) {this.name = name;}
        abstract boolean test(int fx, int fy, int i);
        float r1(int X, int Y) {float x_=X*oW7-1, y_=Y*oH7-1; return (float)sqrt(x_*x_+y_*y_);}}
    private final Bound[] bounds = {
        new Bound("Screen") {boolean test(int x,int y,int i) {return x<W8&&y<H8&&x>=0&&y>=0;}},
        new Bound("Oval")   {boolean test(int x,int y,int i) {return x<W8&&y<H8&&x>=0&&y>=0&&in_circle[wrap((x>>8)+W*(y>>8),MLEN)];}},
        new Bound("Horizon"){boolean test(int x,int y,int i) {return y>0&&y<H8;}},
        new Bound("Radius") {boolean test(int x,int y,int i) {return r1(x,y)<radius;}},
        new Bound("Newton") {boolean test(int x,int y,int i) {return zconv[i];}},
        new Bound("None")   {boolean test(int x,int y,int i) {return false;}}};
    
    ////////////////////////////////////////////////////////////////////////////
    // Gradient ////////////////////////////////////////////////////////////////
    public GradOp   grad_op;
    public char[][] grads;              // All gradient tables; see initGradients()
    public char[]   grad;               // Active gradient lookup table
    public float    gslope        = 1f; // Cursor-controlled gradient rate
    public float    gbias         = 0f; // Cursor-controlled gradient size
    public int      color_mask    = 0;  // XOR this with output color
    public int      feedback_mask = 0;  // XOR this with feedback color
    public int      grad_i        = 0;  // In-use gradient lookup table
    public int      grad_mode     = 0;  // Modes; 0: off 1: one-color 2: two-color
    public void setGradientParam (float slope, float offset) {gslope=slope;gbias=offset;}
    public void setGradientShape (int n) {grad=grads[grad_i=wrap(n,grads.length)];}
    public void setGradient      (int i) {grad_op=grad_modes[grad_mode=wrap(i,grad_modes.length)];}
    public void nextGradientShape(int n) {setGradientShape(grad_i+n);}
    public void nextGradient     (int n) {setGradient(grad_mode+n);  }
    public void toggleInversion()        {color_mask    ^= W5;}
    public void toggleFeedbackInvert()   {feedback_mask ^= W5;}
    public abstract class GradOp {
        public final String name; 
        public GradOp(String s) {name=s;}
        public int w(int i){return (int)((int)grad[i]*gslope-gbias+0.5f);}
        abstract int go(int i, int c);}
    // TODO: NO LONGER USED IN THE NEW DESIGN
    public final GradOp[] grad_modes = {
        new GradOp("None")   {int go(int i,int c){return c;}},
        new GradOp("1-Color"){int go(int i,int c){return rgb.blend(gc1, c,w(i));}},
        new GradOp("2-Color"){int go(int i,int c){int g=w(i); return rgb.blend(rgb.blend(gc1, gc2, g), c,g);}}};

    
}
