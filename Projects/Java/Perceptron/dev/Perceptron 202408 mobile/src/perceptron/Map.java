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

import color.ColorUtil;
import image.DoubleBuffer;
import math.MathToken;
import math.complex;
import math.Equation;
import math.ComplexContex;
import java.awt.image.DataBuffer;
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
import static util.Misc.dither;
import static math.complex.polar;
import color.RGB;
import static java.awt.Color.HSBtoRGB;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newFixedThreadPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import static util.Sys.sout;

/**
 * @author Michael Everett Rule
 */
public final class Map {
       
    // Desired rectangular span of complex plane on screen
    // Changing this will probably break all saved presets!
    public static final float  ZSCALE      = 2.8f;//2.4f;
    public static final double PI_OVER_TWO = 1.57079632679489661923132169163975;
    
    // Color model in use (experimental, keep at c24 for now).
    final static RGB rgb = RGB.c24;
    
    private final Perceptron         P;
    private final DoubleBuffer       B;
    private final ArrayList<Mapping> maps;
    private DataBuffer buf; // image buffer after mapping
    
    // Thread pool to  render scanlines in parallel
    // we limit this to two threads to avoid overloading the system
    //ExecutorService executor = newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final static ExecutorService ex = newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
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
        MIRROR_OFF        = 0,
        MIRROR_HORIZONTAL = 1,
        MIRROR_VERTICAL   = 2,
        MIRROR_TURN       = 3,
        MIRROR_QUADRANT   = 4;
    public final String [] 
        translate_modes = {"Position","Velocity","Locked"},
        mirror_modes    = {"None","Horizontal","Vertical","Turn","Quadrant"};
    public void nextOrthoMode(int i) {offset_mode = wrap(offset_mode + i, 3);}
    public void nextPolarMode(int i) {rotate_mode = wrap(rotate_mode + 1, 3);}
    public void nexMirrorMode(int i) {mirror_mode = wrap(mirror_mode + 1, 5);}
    
    final int 
        W,    // Width in pixels
        H,    // Height in pixels 
        W7,   // Width in pixels  *128
        H7,   // Height in pixels *128
        W8,   // Width in pixels  *256
        H8,   // Height in pixels *256
        MLEN, // W*H
        M2,   // 2*W*H
        MAXC=65535;
    final float 
        cW, 
        cH, 
        oW7, 
        oH7, 
        z2mapW, 
        z2mapH, 
        z2W, 
        z2H;
    
    /** The upper left, the lower right, and the size of the rectangle in the
     * complex plane. Contents are set in the constructor */
    public final complex size = new complex(6f, 6f);
    public final complex UL   = new complex();
    public final complex LR   = new complex();
    
    // Accumulators for velocity mode
    private float dr = 0, di = 0, theta = .1f;

    // Coordinate lookup tables
    float [] 
        PR, // Pixel radius squared
        AX, // absolute value of x coordinate
        AY, // absolute value of y coordinate
        FW; // Pixel radius squared for 8-bit fixed point coordinates
    boolean [] in_circle;
    
    /** Create new FractalMap
     * @param b
     * @param maps
     * @param parent */
    public Map(DoubleBuffer b, ArrayList<Mapping> maps, Perceptron parent) {
        P = parent;
        this.maps = null==maps? new ArrayList<>() : maps;
        // Screen width and height, and related constants.
        // TODO: remove most of these
        B   = b;
        W   = b.out.img.getWidth();
        H   = b.out.img.getHeight();
        W7  = W<<7;     H7  = H<<7;
        oW7 = 1f/W7;    oH7 = 1f/H7;
        cW  = oW7*oW7;  cH  = oH7*oH7;
        W8  = W<<8;     H8  = H<<8;
        MLEN= W*H;      M2  = MLEN<<1;
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
        if (maps!=null && !maps.isEmpty()) mapping = maps.get(map_i %= maps.size());
        // Prepare the gradient maps
        int hW = W/2, hH=H/2;
        grads = new char[9][MLEN];
        int i = 0;
        for (int y=-hH; y<hH; y++) for (int x=-hW;x<hW; x++) {
            grads[0][i]=dither(2*(x*x/(float)(W*W)+y*y/(float)(H*H)));//circle
            grads[1][i]=dither((x+hW)/(float)W);//horizontal
            grads[2][i]=dither((y+hH)/(float)H);//vertical
            grads[3][i]=dither((y+hH)*(x+hW)/(float)(W*H));//diagonal
            grads[4][i]=dither((abs(x)+abs(y))/(float)(hW+hH));//diamond
            grads[5][i]=dither((abs(x)*abs(y))/(float)(hW*hH));//cross
            grads[6][i]=dither(min(1,sqrt(x*x+y*y)/min(hH,hW)));//ring
            grads[7][i]=dither(pow(min(1,sqrt(x*x+y*y)/min(hH,hW)),9));//sharp ring
            grads[8][i]=dither(pow(max(0,min(1,1.3-(sqrt(x*x+y*y))/(min(hH,hW)))),9));//eye
            i++;
        }
        
        grad_i = 0;
        grad   = grads[grad_i];
        // Prepare the buffers for the mapping f(z). 
        map     = new int[M2];
        map_buf = new int[M2];
        zconv   = new char[M2];
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
            float px = (float)x/(float)W*2-1;
            float py = (float)y/(float)H*2-1;
            PR[i] = (float)(px*px+py*py);
            AX[i] = abs(px);
            AY[i] = abs(py);
            in_circle[i] = PR[i]<0.995;
            
            int fx = x*256+128;
            int fy = y*256+128;
            float qx = (float)fx*oW7-1;
            float qy = (float)fy*oH7-1;
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

    
    ////////////////////////////////////////////////////////////////////////////
    // Caching /////////////////////////////////////////////////////////////////
    // Work in progress. Move more of the rendering to be pre-computed. 
    // Cache gradient premultiplied with tint
    // initialize after setting MLEN to avoid weirdness
    final Cache cache;
    /**
     * The rendering cache. 
     * 
     * Compute values in preparation for the next frame in parallel. 
     *  - Compose the gradient and tint into a single translucent layer
     *  - If needed, update the recurrent map as well as the exterior image. 
     * 
     */
    public class Cache {
        public TranslucentLayer 
            trnc = new TranslucentLayer(),
            trnb = new TranslucentLayer();
        public MapCache 
            mapc = new MapCache(),
            mapb = new MapCache();
        AtomicBoolean 
            map_stale = new AtomicBoolean(true),
            trn_ready = new AtomicBoolean(false),
            map_ready = new AtomicBoolean(false);
        public synchronized void cache() {
            if (map_stale.getAndSet(false) || mapc.stale()) {mapb.cache();map_ready.set(true);}
            if (trnc.stale()) {trnb.cache();trn_ready.set(true);}
        }
        public synchronized void flip() {
            if (trn_ready.getAndSet(false)) {TranslucentLayer t = trnc; trnc = trnb; trnb = t; }
            if (map_ready.getAndSet(false)) {MapCache t = mapc; mapc = mapb; mapb = t; }
        }
        /**
         * Merge translucent effects into a single layer.
         */
        public class TranslucentLayer {
            final int [] C = new  int[MLEN]; // premultiplied intRGB
            final char[] A = new char[MLEN]; // alpha values in [0..256]
            boolean off = true;              // whether layer is enabled
            // Track these so we can tell if they change
            private int grad_mode, gc1, gc2, tint_color, tint_level, grad_i, mirror_mode;
            private float gslope, gbias;
            public synchronized void cache() {
                // Track these so we can tell if they change
                grad_mode  = Map.this.grad_mode;
                gc1        = Map.this.gc1;
                gc2        = Map.this.gc2;
                tint_color = Map.this.tint_color;
                tint_level = Map.this.tint_level;
                grad_i     = Map.this.grad_i;
                gslope     = Map.this.gslope;
                gbias      = Map.this.gbias;
                mirror_mode= Map.this.mirror_mode;
                // Copy, so they don't change mid-frame
                int g=grad_mode, c1=gc1, c2=gc2, t=tint_level, q=tint_color;
                if (off=(g==0&&t==0)) return; // Do nothing if transparent
                int [] compose = new int[2];  // {premultiplied intRGB, alpha}
                for (int i=0; i<MLEN; i++) {  // Loop over pixels
                    compose[0]=compose[1]=0;  // Base: transparent black
                    if (g>0) {                // Add gradient if it is on
                        int w = grad_modes[g].w(i);
                        int c = g==1? c1 : rgb.blend(c1, c2,w);
                        rgb.composePremultiplied(compose, c, w);
                    }                         // Apply tint if it is on
                    if (t>0) rgb.composePremultiplied(compose, q, t);
                    C[i] = compose[0];        // Save
                    A[i] = (char)compose[1];
                }
            }
            public boolean stale() {
                if (grad_mode>0) {
                    if (gc1    != Map.this.gc1    ||
                        gc2    != Map.this.gc2    ||
                        grad_i != Map.this.grad_i ||
                        gslope != Map.this.gslope ||
                        gbias  != Map.this.gbias) return true;
                }
                return mirror_mode != Map.this.mirror_mode || 
                       grad_mode   != Map.this.grad_mode   ||
                       tint_level  != Map.this.tint_level  ||
                       tint_color  != Map.this.tint_color  && tint_level>0;
            }
        }
        
        /** 
         * Apply the current translation and rotation to the map.
         * 
         * `image`: Lookup for warped image input. This is updated if
         * `did_image=True` in the `scanline()` function, which is called
         * (possibly in parallel) by the `cache()` function.
         */
        public class MapCache {
            
            final int     [] fxy    = new     int[MLEN*2];
            final short   [] rate   = new   short[MLEN];
            final boolean [] bounds = new boolean[MLEN];
            final int     [] image  = new     int[MLEN]; 
            
            private int f1,mirror_mode;
            private int [] offset = new int[2];
            private complex oldr;
            private boolean did_image;
            
            // Check if cache is stale (if current parameters don't match 
            // the parameters of last cache update).
            public boolean stale() {
                if (mirror_mode!=Map.this.mirror_mode) return true;
                if ((outi==3||outi==4||outi==8)!=this.did_image) return true;
                if (f1!=this.f1) return true;
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
                this.did_image   = outi==3||outi==4||outi==8;
                final complex R  = oldr;
                final int CX = this.offset[0]+W7, CY = this.offset[1]+H7;
                scanMirror((a,b)->{scanline(a,b,CX,CY,R,f1);});
            }
            
            private int[] getOffset() {
                return switch (offset_mode) {
                    case POSITION -> new int[]{(int)(.5f+256*normc[0]), (int)(.5f+256*normc[1])};
                    case VELOCITY -> new int[]{
                        (int)(.5f+256*((dr=(abs(dr)>MAXC)?0:dr+.1f*normc[0]))),
                        (int)(.5f+256*((di=(abs(di)>MAXC)?0:di+.1f*normc[1])))};
                    default -> new int[]{0,0};
                };
            }
            
            private complex getRotation() {
                switch (rotate_mode) {
                    case POSITION -> {return new complex(rotation);}
                    case LOCKED   -> {return polar(1f,(float)PI_OVER_TWO);}
                    case VELOCITY -> {theta += arg(rotation)*.01f;return polar(mod(rotation),theta);}
                }
                return null;
            }            
            
            private void scanline(int a, int b,int cx,int cy,complex r, int f1) {
                // This caches information that is constant across frames                // 
                // see `operate()` for code that updates fade weights
                // see `scanline()` for code that caches crossfaded, shifted, rotated lookup
                // see `Map.computeLookup()` for code that interprets complex map function
                float RX = r.real, RY = r.imag, RC = r.real+r.imag;
                for (int i=a; i<b; i++) {
                    final int j = i<<1, k = j|1;
                    int real = map[j], imag = map[k];
                    // Cross fade map
                    if (f1 > 0) {
                        real += f1*(map_buf[j] - real) >> 8;
                        imag += f1*(map_buf[k] - imag) >> 8;
                    } 
                    // Gauss's trick for affine transform of map
                    float x4  = imag * RX;
                    float x5  = real * RY;
                    int fx    = cx + (int)(x5 + x4 + 0.5f);
                    int fy    = cy + (int)(x5 - x4 + RC*(imag-real) + 0.5f);
                    // Save transformed map
                    fxy[j] = fx;
                    fxy[k] = fy;
                    // Boolean test for if pixel source is off-screen
                    bounds[i] = bound_op.test(fx,fy,i);
                    
                    if (Map.this.bounds_i==4) { // Newton fractal convergence active
                        // Precomputed convergence test (prdrag's addition)
                        // This is used for the "newton" boundary conditions, 
                        // which assigns "off screen" to points that stay close 
                        // together under the inverse map. This allows us to render
                        // Newtons-method basin fractals. 
                        // TODO: this needs to be moved to the cache so it can adapt
                        //float delta2 = (Z.real-z.real)*(Z.real-z.real)+(Z.imag-z.imag)*(Z.imag-z.imag);
                        int dx = (fx+127>>8) - (i%W);
                        int dy = (fy+127>>8) - (i/W);
                        float delta2 = (dx*dx+dy*dy)/MLEN;
                        final float ratio = 0.001f;
                        zconv[i] = delta2<=ratio
                                ? (char)(255*(ratio-delta2)/ratio)
                                : 0;
                        rate[i] = (short)zconv[i];
                    } else {
                        // Integer in 0..256 related to rate of divergence
                        rate[i] = divergenceRate(fx,fy,i);
                    }
                    
                    if (did_image) image[i] = B.img.get.it(fx,fy);
                }
            }
            
            // Returns an integer in 0...256
            short divergenceRate(int fx,int fy,int i) {
                float pr,qr;
                switch (Map.this.bounds_i) {
                    case 2: {// Horizon
                        pr = AY[i];
                        qr = (float)abs((float)fy*oH7-1);
                    } break;
                    case 1: {// Oval
                        pr = PR[i];
                        fx-= W7; fy-= H7;
                        qr = (float)(fx*(fx*cW)+fy*(fy*cH));
                        pr = (float)Math.sqrt(pr);
                        qr = (float)Math.sqrt(qr);
                    } break;
                    case 3: {// Radius
                        pr = PR[i];
                        fx-= W7; fy-= H7;
                        qr = (float)(fx*(fx*cW)+fy*(fy*cH));
                        pr *= inverse_radius;
                        qr *= inverse_radius;
                        pr = (float)Math.sqrt(pr);
                        qr = (float)Math.sqrt(qr);
                    } break;
                    case 4: return (short)zconv[i]; // Newton TODO
                    case 0: // Screen
                    case 5: // None
                    default: {
                        pr = (float)max(AX[i],AY[i]);
                        qr = (float)max(abs((float)fx*oW7-1),abs((float)fy*oH7-1));
                    }}
                float w1 = clip(invert_bound?pr-1:1-pr,0f,1f);
                float w2 = clip(invert_bound?1-qr:qr-1,0f,1f);
                w1 /= w1+w2;
                return (short)(256f*w1+0.5f);
            }
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Threading helper functions
    private void runAll(Runnable [] fn) {
        int N = fn.length;
        Future f[] = new Future[N];
        for (int i=0;i<N;i++) f[i]=ex.submit(fn[i]);
        for (int i=0;i<N;i++) try {f[i].get();} catch (InterruptedException | ExecutionException ex2) {fn[i].run();}
    }
    private void scanAll(BiConsumer<Integer,Integer> fn) {
        Runnable [] f= new Runnable[H];
        for (int y=0;y<H;y++) {final int a=y*W;f[y]=()->fn.accept(a,a+W);}
        runAll(f);
    }
    private void scanLeft(BiConsumer<Integer,Integer> fn) {
        final int hW=(W+1)/2;
        Runnable [] f= new Runnable[H];
        for (int y=0;y<H;y++) {final int a=y*W;f[y]=()->fn.accept(a,a+hW);}
        runAll(f);
    }
    private void scanTop(BiConsumer<Integer,Integer> fn) {
        int hH=(H+1)/2;
        Runnable [] f= new Runnable[hH];
        for (int y=0;y<hH;y++) {final int a=y*W;f[y]=()->fn.accept(a,a+W);}
        runAll(f);
    }
    private void scanCorner(BiConsumer<Integer,Integer> fn) {
        final int hW=(W+1)/2, hH=(H+1)/2;
        Runnable [] f= new Runnable[hH];
        for (int y=0;y<hH;y++) {final int a=y*W;f[y]=()->fn.accept(a,a+hW);}
        runAll(f);
    }
    private void scanMirror(BiConsumer<Integer,Integer> fn) {
        switch (mirror_mode) {
            case MIRROR_OFF                   -> scanAll (fn);
            case MIRROR_HORIZONTAL            -> scanLeft(fn);
            case MIRROR_VERTICAL, MIRROR_TURN -> scanTop (fn);
            case MIRROR_QUADRANT              -> scanCorner(fn);
            default -> throw new RuntimeException("Mirror should be in 0..3");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Apply current map to screen raster, storing result in output raster. 
    public synchronized void operate() {
        // The target buffer is passed indirectly to render via the instance 
        // variable `buff`. This is the only place where `buf` is assigned and
        // it is behind a `synchronized` barrier.
        buf = B.buf.buf;
        scanMirror(this::render);
        // If a mirrored mode is active, copy the rendered result
        // to the mirrored halves/quadrants
        if (mirror_mode==MIRROR_HORIZONTAL|mirror_mode==MIRROR_QUADRANT) 
            for (int y=0; y<H; y++) {
                int i=y*W+W-1,j=y*W; 
                for (int x=0;x<(W+1)/2;x++) buf.setElem(i--,buf.getElem(j++));
            }
        if (mirror_mode==MIRROR_VERTICAL |mirror_mode==MIRROR_QUADRANT) 
            for (int y=0;y<(H+1)/2;y++) {
                int i=W*(H-1-y),j=W*y; 
                for (int x=0;x<W;x++) buf.setElem(i+x,buf.getElem(j+x));
            }
        if (mirror_mode==MIRROR_TURN) {
            int i=MLEN-1; 
            for (int j=0; j<(MLEN+1)/2; j++) buf.setElem(i--,buf.getElem(j));
        }
        
        // See private synchronized void computeLookup() for map calculation code
        // Update map cross-fader (flip to target map if done)
        if (map_fade > 0) {
            map_fade -= .05f;
            this.cache.map_stale.set(true);
        }
        
        updateColorRegisters(buf); // Update accent colors
    }

    ////////////////////////////////////////////////////////////////////////////
    // The main rendering operator /////////////////////////////////////////////
    void render(int start, int end) {
        
        // Linear Congruential RNG seeded with stronger RNG at start of scan
        int rn = (int)((long)(Math.random()*4294967296L)&0xffffffff)|1,
            rA = 0x343FD, rC = 0x269EC3;
        
        int     [] fxy  = cache.mapc.fxy;
        short   [] rate = cache.mapc.rate;
        boolean [] in   = cache.mapc.bounds;
        char    [] A    = cache.trnc.A;
        int     [] C    = cache.trnc.C;
        
        boolean translucentActive = (grad_mode!=0)||(tint_level>0);
        
        for (int i = start; i<end; i++) {
            final int j=i<<1, k=j|1;
            int fx = fxy[j], fy = fxy[k];
            int c;
            if (in[i]== invert_bound) {
                // out of bounds (unless boundary test is inverted)
                c = outop.f(fx,fy,i,rate[i]);
                c^= color_mask;
                if (translucentActive) c = rgb.blendPremultiplied(c, C[i], A[i]);
            } else {
                // in bounds (unless boundary test is inverted)
                c = B.out.get.it(fx, fy);
                c^= feedback_mask;
                if (translucentActive && P.fore_tint) c = rgb.blendPremultiplied(c, C[i], A[i]);
            }
            if (noise_level>0) c = rgb.blend(c, (rn=rn*rA+rC)>>8    , noise_level);
            if (motion_blur>0) c = rgb.blend(c, B.buf.buf.getElem(i), motion_blur);
            buf.setElem(i,c);
        }
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
    public int color_dampen = 128;
    //public void setGColor1(int i) {fade_op = fade_colors[gradcolor1_i = wrap(i, N_COLOR_REGISTERS)];}
    public void setGColor1   (int i) {gcolor1_i    = wrap(i,N_COLOR_REGISTERS);}
    public void setGColor2   (int i) {gcolor2_i    = wrap(i,N_COLOR_REGISTERS);}
    public void setBarColor  (int i) {barcolor_i   = wrap(i,N_COLOR_REGISTERS);}
    public void setTintColor (int i) {tintcolor_i  = wrap(i,N_COLOR_REGISTERS);}
    public void setOutColor  (int i) {outcolor_i   = wrap(i,N_COLOR_REGISTERS);}
    public void setTintLevel (int i) {tint_level   = clip(i,0,255);}
    public void setColorDamp (int i) {color_dampen = clip(i,0,255);}
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
    public void updateColorRegisters(DataBuffer buf) {
        color_registers[0] = 0x000000;
        color_registers[1] = 0xffffff;
        int mid = buf.getElem((W/2) + W*(H/2));
        float[] HSV = java.awt.Color.RGBtoHSB((mid>>16)&0xff,(mid>>8)&0xff,(mid)&0xff,new float[]{0,0,0});
        color_registers[2] = rgb.fromIntRGB(HSBtoRGB(HSV[0], 1f, 1f));
        color_registers[3] = ~mid;
        color_registers[4] = rgb.fromIntRGB(HSBtoRGB(HSV[0]+0.2f, 1f, 1f));
        color_registers[5] = rgb.fromIntRGB(HSBtoRGB((++rotation_hue)*.01f, 1f, 1f));
        color_registers[6] = out_color;
        // TODO: need to also apply color transform and tint from previous frame here?
        pout_color = out_color^feedback_mask^color_mask; // needed for special edge mode
        gc1        = rgb.blend(gc1,        color_registers[gcolor1_i  ],color_dampen);
        gc2        = rgb.blend(gc2,        color_registers[gcolor2_i  ],color_dampen);
        bar_color  = rgb.blend(bar_color,  color_registers[barcolor_i ],color_dampen);
        out_color  = rgb.blend(out_color,  color_registers[outcolor_i ],color_dampen);
        tint_color = rgb.blend(tint_color, color_registers[tintcolor_i],color_dampen);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Noise and Motion blur. //////////////////////////////////////////////////
    public int motion_blur = 0;
    public int noise_level = 0;
    public void setMotionBlur(int k) {motion_blur = clip(k,0,256);}
    public void setNoise     (int i) {noise_level = clip(i,0,256);}
    public void nextNoise    (int n) {setNoise(noise_level+n);}
    
    ////////////////////////////////////////////////////////////////////////////
    // Describe what to do when a pixel is out of bounds ///////////////////////
    public PixOp outop;
    public int   outi = 4; // See `outops` array for definitions
    public void nextOutside(int n) {setOutside(outi + n);}
    public void setOutside(int index) {outop = outops[outi = wrap(index, outops.length)];}
    public abstract class PixOp {
        public final String name;
        public PixOp(String s){this.name=s;}
        abstract int f(int x, int y, int i, int w);
    }
    public final PixOp[] outops = {
        new PixOp("Color"){int f(int x,int y,int i,int w){return out_color; }},
        new PixOp("Edge" ){int f(int x,int y,int i,int w){return B.buf.get.it(clip(x,0,W8),clip(y,0,H8));}},
        new PixOp("Tile" ){int f(int x,int y,int i,int w){return B.buf.get.it(x,y);}},
        new PixOp("Image"){int f(int x,int y,int i,int w){return cache.mapc.image[i];}},
        new PixOp("Fade" ){int f(int x,int y,int i,int w){return rgb.blend(cache.mapc.image[i], B.buf.get.it(x,y), w);}},
        new PixOp("Rate1"){int f(int x,int y,int i,int w){return rgb.blend(out_color, bar_color, w);}},
        new PixOp("Rate2"){int f(int x,int y,int i,int w){w=(256-w)*w;return rgb.blend(out_color, bar_color,(w*w)>>20);}},
        new PixOp("Rate3"){int f(int x,int y,int i,int w){w=((768-2*w)*w*w)>>16;return rgb.blend(rgb.blend(bar_color, out_color, w), B.buf.get.it(x,clip(y,0,H8-1)),w);}},
        new PixOp("Blend"){int f(int x,int y,int i,int w){return ColorUtil.mean(cache.mapc.image[i], B.buf.get.it(x,y));}},
    };
    
    ////////////////////////////////////////////////////////////////////////////
    // Fractal map control /////////////////////////////////////////////////////
    public int       map_i = 0;// Index into list of built-in maps
    public Mapping   mapping;  // The fractal mapping f(z)
    public int[]     map;      // Lookup table for current map
    public complex   offset;   // The "+c" in a Julia set iteration 
    public complex   rotation; // Rotation/scale of the complex map
    public float[]   normc;    // Offset constant in screen coordinates
    public char[]    zconv;    // "converged" points LUT for Newton fractals
    public int[]     map_buf;  // Additional map LUT to use when transitioning
    public float     map_fade; // Frame counter used to transition maps smoothly
    final static ComplexContex vars = ComplexContex.standard();
    public static abstract class Mapping {
        public final String s;
        public Mapping(String S){s=S;}
        public String toString() {return s;}
        public abstract complex f(complex z);
    }
    public void setMap(Mapping map) {mapping = map; computeLookup();}
    public void setMap(int index) {setMap(maps.get(map_i=wrap(index,maps.size())));}
    public void setMap(final String s) {setMap(makeMap(s));}
    public void nextMap(int n) {setMap(map_i + n);}
    public void setNormalizedConstant(float x, float y) {
        if (normc  == null) normc  = new float[]{0, 0};
        if (offset == null) offset = new complex(0, 0);
        offset.real = (W - x) / z2W + UL.real;
        offset.imag = (H - y) / z2H + UL.imag;
        normc[0] = (float)(offset.real * z2W);
        normc[1] = (float)(offset.imag * z2H);
    }
    public void setNormalizedRotation(float x, float y) {
        rotation.real  = (W - x) / z2W + UL.real;
        rotation.imag  = (H - y) / z2H + UL.imag;
        inverse_radius = rotation.length();
        radius         = 1 / inverse_radius;
        rotation       = complex.polar(.5f / rotation.length() , rotation.angle());   
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
        if (map_fade > 0) {
            // If crossfading, Freeze and save current interpolated map 
            int f1 = (int) (256 * map_fade + 0.5);
            for (int i=0; i<M2; i++) map[i] += f1*(map_buf[i]-map[i]) >> 8;
        }
        
        // see `operate()` for code that updates fade weights
        // see `scanline()` for code that caches crossfaded, shifted, rotated lookup
        // see `computeLookup()` for code that inteprets complex map expression
        // Compute new map
        complex z = new complex();
        int i = 0, k = 0;
        // The +0.5 shift keeps mirrored modes aligned
        float x0 = UL.real+.5f*z2mapW, y0 = UL.imag+.5f*z2mapH ;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                // conjugate before+after for cartesian, not image, coordinates.
                z.real =  x*z2mapW + x0;
                z.imag = -y*z2mapH - y0;
                // Rotate coordinates for tall screens 
                // (hacky mobile experiment)
                //if (P.is_sideways) z = z.timesI();
                complex Z = mapping.f(z); 
                //if (P.is_sideways) Z = Z.overI();
                Z = complex.conj(Z); 
                map_buf[i] = (int)(0x100*(z2W*(Z.real-x0)))-W7;
                //map[i] -= map_buf[i]; // FROM-TO
                i++;
                map_buf[i] = (int)(0x100*(z2H*(Z.imag-y0)))-H7;
                //map[i] -= map_buf[i];
                i++;
                // Precomputed convergence test (prdrag's addition)
                // This is used for the "newton" boundary conditions, 
                // which assigns "off screen" to points that stay close 
                // together under the inverse map. This allows us to render
                // Newtons-method basin fractals. 
                // TODO: this needs to be moved to the cache so it can adapt
                float delta2 = (Z.real-z.real)*(Z.real-z.real)+(Z.imag-z.imag)*(Z.imag-z.imag);
                zconv[k++] = delta2<=0.025f
                        ? (char)(255*(0.025f-delta2)/0.025f)
                        : 0;
            }
        }
        map_fade = 1.f;
        // Flip immediately for now
        // map contains target, map_buf contains previous map if any
        int[] t=map;map=map_buf;map_buf=t;
        
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
    public void setBound(int index) {bound_op = bounds[bounds_i = wrap(index, bounds.length)]; cache.map_stale.set(true);}
    public abstract class Bound {
        public final String name;
        public Bound(String name) {this.name = name;}
        abstract boolean test(int fx, int fy, int i);
        float r1(int X, int Y) {float x_=(float)X*oW7-1, y_=(float)Y*oH7-1; return (float)sqrt(x_*x_+y_*y_);}}
    private final Bound[] bounds = {
        new Bound("Screen") {boolean test(int x,int y,int i) {return x<W8&&y<H8&&x>=0&&y>=0;}},
        new Bound("Oval")   {boolean test(int x,int y,int i) {return x<W8&&y<H8&&x>=0&&y>=0&&in_circle[wrap((x>>8)+W*(y>>8),MLEN)];}},
        new Bound("Horizon"){boolean test(int x,int y,int i) {return y>0&&y<H8;}},
        new Bound("Radius") {boolean test(int x,int y,int i) {return r1(x,y)<radius;}},
        new Bound("Newton") {boolean test(int x,int y,int i) {return zconv[i]>0;}},
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
    public void toggleInversion()        {color_mask    ^= 0xffffff;}
    public void toggleFeedbackInvert()   {feedback_mask ^= 0xffffff;}
    public abstract class GradOp {
        public final String name; 
        public GradOp(String s) {name=s;}
        public int w(int i){return clip((int)(grad[i]*gslope-gbias),0,255);}
        abstract int go(int i, int c);}
    public final GradOp[] grad_modes = {
        new GradOp("None")   {int go(int i,int c){return c;}},
        new GradOp("1-Color"){int go(int i,int c){return rgb.blend(gc1, c,w(i));}},
        new GradOp("2-Color"){int go(int i,int c){int g=w(i); return rgb.blend(rgb.blend(gc1, gc2, g), c,g);}}};
    
    
}
