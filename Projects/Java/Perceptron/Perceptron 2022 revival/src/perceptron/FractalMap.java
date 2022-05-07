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

import image.DoubleBuffer;
import math.MathToken;
import math.complex;
import math.Equation;
import math.ComplexVarList;
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import static java.lang.Math.min;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.hypot;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static math.complex.arg;
import static math.complex.fromPolar;
import static math.complex.mod;
import static util.Misc.clip;
import static util.Misc.wrap;
import static util.ColorUtil.blend;

/**
 * @author Michael Everett Rule
 */
public class FractalMap {
       
    // Desired rectangular span of complex plane on screen
    // Changing this changes everything! Presets break. 
    public static final float ZSCALE = 2.8f;//2.4f;
    
    Perceptron         P;
    DoubleBuffer       B;
    ArrayList<Mapping> maps;
    boolean            running = false;
    
    DataBuffer 
        out, // current image on the screen
        buf, // image buffer after mapping
        img, // external, loaded image
        dsp; // cursors, circles, dots

    // Control interpretation of translation, rotation + mirroring modes
    public int 
        offset_mode = 0,
        rotate_mode = 2,
        mirror_mode = 0;
    public final int 
        // Constants for identifying the translation modes
        TRANSLATION_NORMAL   = 0,
        TRANSLATION_VELOCITY = 1,
        TRANSLATION_LOCKED   = 2,
        // Constants for identifying the rotation modes
        ROTATION_NORMAL      = 0,
        ROTATION_VELOCITY    = 1,
        ROTATION_LOCKED      = 2,
        // For identifying mirroring modes
        MIRROR_OFF           = 0,
        MIRROR_HORIZONTAL    = 1,
        MIRROR_VERTICAL      = 2,
        MIRROR_TURN          = 3,
        MIRROR_QUADRANT      = 4;
    public final String [] 
        translate_modes = {"Position","Velocity","Locked"},
        mirror_modes = {"None","Horizontal","Vertical","Turn","Quadrant"};
    public void nextOrthoMode(int i) {offset_mode = wrap(offset_mode + i, 3);}
    public void nextPolarMode(int i) {rotate_mode = wrap(rotate_mode + 1, 3);}
    public void nexMirrorMode(int i) {mirror_mode = wrap(mirror_mode + 1, 5);}
    
    // Mistakes were made in this design
    final int W, H, W7, H7, W8, H8, MLEN, M2, MAXC=65535;
    final float cW, cH, oW2, oH2, oW7, oH7, z2mapW, z2mapH, z2W, z2H;
    
    /** The upper left, the lower right, and the size of the rectangle in the
     * complex plane. These are re-initialized so these values don't
     * matter.     */
    public static final complex size = new complex(6f, 6f);
    public static final complex UL   = new complex();
    public static final complex LR   = new complex();
    public static final double  PI_OVER_TWO = Math.PI * .5;
    
    // Accumulators for velocity mode
    private float dr = 0, di = 0, dtheta = .1f;
    
    // Coordinate lookup tables
    float [] PR,AX,AY,FW;
    boolean [] in_circle;
    
    /** Create new FractalMap
     * @param b
     * @param maps
     * @param parent */
    public FractalMap(DoubleBuffer b, ArrayList<Mapping> maps, Perceptron parent) {
        P = parent;
        this.maps = null==maps? new ArrayList<>() : maps;
        // Screen width and height, and related constants.
        // We need to remove most of these
        B   = b;
        W   = b.out.img.getWidth();
        H   = b.out.img.getHeight();
        oW2 = 2f/W;  oH2 = 2f/H;
        W7  = W<<7;  H7  = H<<7;
        oW7 = 1f/W7; oH7 = 1f/H7;
        cW  = oW7*oW7;  cH  = oH7*oH7;
        W8  = W<<8;  H8  = H<<8;
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
        outi = wrap(outi, outops.length);
        grad_mode = wrap(grad_mode, grad_modes.length);
        syncOps();
        if (!maps.isEmpty()) mapping = maps.get(map_i %= maps.size());
        // Prepare the gradient maps
        int hW = W/2, hH=H/2;
        grads = new char[9][MLEN];
        int i = 0;
        for (int y=-hH; y<hH; y++) for (int x=-hW;x<hW; x++) {
            grads[0][i]=(char)(255-512*((float)x*x/(W*W)+(float)y*y/(H*H)));
            grads[1][i]=(char)(255-(x+hW)*255/W);
            grads[2][i]=(char)(255-(y+hH)*255/H);
            grads[3][i]=(char)(255-(y+hH)*(x+hW)*255/(W*H));
            grads[4][i]=(char)(255-(abs(x)+abs(y))*255/(hW+hH));
            grads[5][i]=(char)(255-(abs(x)*abs(y))*255/(hW*hH));
            grads[6][i]=(char)(255*(1-min(1,sqrt(x*x+y*y)/min(hH,hW))));
            grads[7][i]=(char)(255*(1-pow(min(1,sqrt(x*x+y*y)/min(hH,hW)),9)));
            grads[8][i]=(char)(255*(1-pow(max(0,min(1,1.3-(sqrt(x*x+y*y))/(min(hH,hW)))),9)));
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
    }

    public void syncOps() {
        bound_op   = bounds[bounds_i];
        outop = outops[outi];
        grad_op    = grad_modes[grad_mode];
    }

    //🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛
    // Apply current map to screen raster, storing result in output raster. 
    public synchronized void operate() {
        out = B.out.buf;
        buf = B.buf.buf;
        dsp = B.dsp.buf;
        if (B.img != null) img = B.img.buf;
        
        updateColorRegisters();

        // Mode 0: (normal) offset is converted to 8-bit fixed point. 
        // Mode 1: Velocity mode. 
        // Mode 2: Locked: the offset is clamped to zero. 
        int cx = W7, cy = H7;
        switch (offset_mode) {
            case TRANSLATION_NORMAL: 
                cx=(int)(.5f+256*normc[0])+W7; 
                cy=(int)(.5f+256*normc[1])+H7; 
                break;
            case TRANSLATION_VELOCITY:
                cx = (int)(.5f+256*((dr=(abs(dr)>MAXC)?0:dr+.1f*normc[0])))+W7;
                cy = (int)(.5f+256*((di=(abs(di)>MAXC)?0:di+.1f*normc[1])))+H7;
                break;
            case TRANSLATION_LOCKED: 
                break;
        }
        // Mode 0: Blue cursor controls scale and rotation
        // Mode 1: Blue cursor controls velocity
        // Mode 2: Scale and rotation are locked at 1, pi/2, respectively. 
        complex r = new complex();
        switch (rotate_mode) {
            case ROTATION_NORMAL:
                r = rotation;
                break;
            case ROTATION_VELOCITY:
                dtheta += arg(rotation)*.01f;
                r = fromPolar(mod(rotation),dtheta);
                break;
            case ROTATION_LOCKED:
                r = fromPolar(1f,(float)PI_OVER_TWO);
                break;
        }

        int hH = (H+1)/2, hW = (W+1)/2, hM = (MLEN+1)/2;
        switch (mirror_mode) {
            case MIRROR_OFF: {
                render(0,MLEN,cx,cy,r);
            } break;
            case MIRROR_HORIZONTAL: {
                for (int y=0; y<H; y++) {
                    render(y*W,y*W+hW,cx,cy,r);
                    int i=y*W+W-1, j=y*W;
                    for (int x=0; x<hW; x++) buf.setElem(i--,buf.getElem(j++));
                } 
            } break;
            case MIRROR_VERTICAL: {
                render(0,W*hH,cx,cy,r);
                for (int y=0; y<hH; y++) {
                    int i = W*(H-1-y), j = W*y;
                    for (int x=0; x<W; x++) buf.setElem(i++, buf.getElem(j++));
                }
            } break;
            case MIRROR_TURN: {
                render(0,hM,cx,cy,r);
                int i=MLEN-1;
                for (int j=0; j<hM; j++) buf.setElem(i--, buf.getElem(j));
            } break;
            case MIRROR_QUADRANT: {
                for (int y=0; y<hH; y++) {
                    render(y*W,y*W+hW,cx,cy,r);
                    int i=y*W+W-1, j=y*W;
                    for (int x=0; x<hW; x++) buf.setElem(i--,buf.getElem(j++));
                }
                for (int y=0; y<hH; y++) {
                    int i = W*(H-1-y), j = W*y;
                    for (int x=0; x<W; x++) buf.setElem(i++,buf.getElem(j++));
                }
            } break;
        }
        
        // Exchange output and "buffer" operators.
        // Perceptron will
        B.flip();
    }
    //🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛🮛

    ////////////////////////////////////////////////////////////////////////////
    // The main rendering operator /////////////////////////////////////////////
    void render(int start, int end, int cx, int cy, complex r) {
        
        float rx = r.real;
        float ry = r.imag;
        float rc = rx + ry;
        
        int noise = (int)((long)(Math.random()*4294967296L)&0xffffffff);
        int f1 = (int) (256 * map_fade);
        for (int i = start; i<end; i++) {
            int j = i<<1;
            int real, imag;
            if (f1 > 0) {
                real = map_buf[j] + (map[j]*f1 >> 8); j++;
                imag = map_buf[j] + (map[j]*f1 >> 8);
            } 
            else {real = map[j++]; imag = map[j]; }
            float x4 = imag * rx;
            float x5 = real * ry;
            int   fx = (int)(x4+x5)+cx;
            int   fy = (int)(rc*(imag-real)-x4+x5)+cy;
            int   c;
            if (bound_op.test(fx,fy,i) != invert_bound) {
                // Within bounds for feedback unless bounds_invert = true
                c = B.out.get.it(fx, fy);
                if (P.fore_grad) c = grad_op.go(i,c);
                c ^= feedback_mask;
            } else {
                c = outop.go(fx,fy,i);
                c = grad_op.go(i,c);
            }
            if (noise_level>0) {
                noise *= 0xFD43FD;
                noise += 0xC39EC3;
                c = blend(noise >> 8, c, noise_level);
            }
            c ^= color_mask;  
            if (tint_level >0) c = blend(tint_color, c, tint_level);
            if (motion_blur>0) c = blend(B.buf.buf.getElem(i), c, motion_blur);
            buf.setElem(i,c);
        }
        if (map_fade > 0) {
            map_fade -= .05f;
            if (map_fade <= 0) {int[] t=map;map=map_buf;map_buf=t;}
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
    public int gcolor1      = 0; 
    public int gcolor2      = 0;
    public int bar_color    = 0; 
    public int tint_color   = 0; 
    public int out_color    = 0; 
    public int pout_color   = 0; 
    public int tint_level   = 0;
    public int color_dampen = 128;
    //public void setGColor1(int i) {fade_op = fade_colors[gradcolor1_i = wrap(i, N_COLOR_REGISTERS)];}
    public void setGColor1   (int i) {gcolor1_i   =wrap(i,N_COLOR_REGISTERS);}
    public void setGColor2   (int i) {gcolor2_i   =wrap(i,N_COLOR_REGISTERS);}
    public void setBarColor  (int i) {barcolor_i  =wrap(i,N_COLOR_REGISTERS);}
    public void setTintColor (int i) {tintcolor_i =wrap(i,N_COLOR_REGISTERS);}
    public void setOutColor  (int i) {outcolor_i  =wrap(i,N_COLOR_REGISTERS);}
    public void setTintLevel (int i) {tint_level  =clip(i,0,255);}
    public void setColorDamp (int i) {color_dampen=clip(i,0,255);}
    public void nextGColor1  (int n) {setGColor1  (gcolor1_i   +n);}    
    public void nextGColor2  (int n) {setGColor2  (gcolor2_i   +n);}    
    public void nextBarColor (int n) {setBarColor (barcolor_i  +n);}    
    public void nextOutColor (int n) {setOutColor (outcolor_i  +n);}    
    public void nextTintColor(int n) {setTintColor(tintcolor_i +n);}    
    public void nextTintLevel(int n) {setTintLevel(tint_level  +n);}
    public void nextColorDamp(int n) {setColorDamp(color_dampen+n);}
    public final int N_COLOR_REGISTERS = 6;
    public final String [] color_register_names = {
        "Black",
        "White",
        "Middle Pixel Hue",
        "Middle Pixel Inverse",
        "Middle Pixel Hue Rotate",
        "Hue Rotate"
    };
    public final int[] color_registers = new int[N_COLOR_REGISTERS];
    private int rotation_hue = 0;
    public void updateColorRegisters() {
        color_registers[0] = blend(color_registers[0],0x00000000,color_dampen);
        color_registers[1] = blend(color_registers[1],0x00ffffff,color_dampen);
        int MMID   = (W/2) + W*(H/2);
        int middle = buf.getElem(MMID);
        float[] HSV = java.awt.Color.RGBtoHSB(
                (middle >> 16) & 0xff,
                (middle >> 8) & 0xff,
                (middle) & 0xff,
                new float[]{0, 0, 0});
        int color;
        int i = 2;
        color = Color.HSBtoRGB(HSV[0], 1.f, 1.f);
        color_registers[i] = blend(color_registers[i++],color,color_dampen);
        color = ~middle;
        color_registers[i] = blend(color_registers[i++],color,color_dampen);
        color = Color.HSBtoRGB(HSV[0]+0.2f, 1.f, 1.f);
        color_registers[i] = blend(color_registers[i++],color,color_dampen);
        rotation_hue++;
        color = Color.HSBtoRGB((float)rotation_hue*.01f, 1.f, 1.f);
        color_registers[i] = blend(color_registers[i++],color,color_dampen);
        // TODO: need to also apply color transform and tint from previous frame here?
        pout_color = out_color^feedback_mask^color_mask; // needed for special edge mode
        gcolor1    = color_registers[gcolor1_i];
        gcolor2    = color_registers[gcolor2_i];
        bar_color  = color_registers[barcolor_i  ]; 
        out_color  = color_registers[outcolor_i  ];
        tint_color = color_registers[tintcolor_i ];
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
    public PixelOp  outop;
    public int      outi = 4; // 0:color 1:edge  2:repeat  3:image  4:fade 5:special
    public void nextOutside(int n) {setOutside(outi + n);}
    public void setOutside(int index) {outop = outops[outi = wrap(index, outops.length)];}
    public abstract class PixelOp {
        public final String name;
        abstract int go(int fx, int fy, int i);
        public PixelOp(String s){this.name=s;}
    }    
    public abstract class SpecialOp extends PixelOp {
        public SpecialOp(String s){super(s);}
        final int weight(int fx,int fy,int i) {
            //if (fx<0) fx=-fx;
            //if (fy<0) fy=-fy;
            float pr,qr;
            switch (FractalMap.this.bounds_i) {
                case 2: {// Horizon
                    pr = AY[i];
                    qr = (float)abs((float)fy*oH7-1);
                } break;
                case 1: {// Oval
                    pr = PR[i];
                    fx-= W7; fy-= H7;
                    qr = (float)(fx*(fx*cW)+fy*(fy*cH));
                } break;
                case 3: {// Radius
                    pr = PR[i];
                    fx-= W7; fy-= H7;
                    qr = (float)(fx*(fx*cW)+fy*(fy*cH));
                    pr *= inverse_radius;
                    qr *= inverse_radius;
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
            return (int)(256f*w1+0.5f);
        }
    }
    public final PixelOp[] outops = {
        new PixelOp("Color"){int go(int x,int y,int i){return out_color; }},
        new PixelOp("Edge" ){int go(int x,int y,int i){return B.buf.get.it(clip(x,0,W8),clip(y,0,H8));}},
        new PixelOp("Tile" ){int go(int x,int y,int i){return B.buf.get.it(x,y);}},
        new PixelOp("Image"){int go(int x,int y,int i){return B.img.get.it(x,y);}},
        new SpecialOp("Fade"){int go(int x,int y,int i){
            return blend(B.buf.get.it(x,y),B.img.get.it(x,y),weight(x,y,i));
        }},
        new SpecialOp("Special1"){int go(int fx,int fy,int i){
            return blend(bar_color,out_color,weight(fx,fy,i));
        }},
        new SpecialOp("Special2"){int go(int fx,int fy,int i){
            int x = weight(fx,fy,i); x=(256-x)*x;
            return blend(bar_color,out_color,(x*x)>>20);
        }},
        new SpecialOp("Special3"){int go(int fx,int fy,int i){
            int x = weight(fx,fy,i);
            x = ((768 - 2*x)*x*x)>>16;
            //fx>>=8; fy>>=8;
            //B.buf.buf.getElem(clip(fx,0,W-1)+W*clip(fy,0,H-1))
            return blend(B.buf.get.it(fx,clip(fy,0,H8-1)),blend(out_color,bar_color,x),x);
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
    public void setMap(Mapping map) {mapping = map;computeLookup();}
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
        rotation.real = ((W - x) / z2W + UL.real);
        rotation.imag = ((H - y) / z2H + UL.imag);
        float theta = complex.arg(rotation);
        float r = complex.mod(rotation) * 2; 
        inverse_radius = complex.mod(rotation);
        bound_radius = 1 / inverse_radius;
        rotation = complex.fromPolar(1 / r, theta);   
    }
    final static ComplexVarList vars = ComplexVarList.standard();
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
            System.out.println("!!!! Error parsing map "+s);
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
                zconv[k++] = (Z.real-z.real)*(Z.real-z.real)+(Z.imag-z.imag)*(Z.imag-z.imag)>0.1f;
            }
        }
        map_fade = 1.f;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // The boundary conditions for the Julia set. "R" //////////////////////////
    public Bound   bound_op;
    public int     bounds_i       = 1;     // 0:screen 1:circle 2:elastic 3:horizon 4:Newton 5:none
    public boolean invert_bound   = false; // Invert boundary condition?
    public float   bound_radius   = 1;     // adjust radius with map scale (mode 2)
    public float   inverse_radius = 1;     // adjust radius with map scale (mode 2)
    public void nextBound(int n) {setBound(bounds_i + n);}
    public void setBound(int index) {bound_op = bounds[bounds_i = wrap(index, bounds.length)];}
    public abstract class Bound {
        public final String name;
        public Bound(String name) {this.name = name;}
        abstract boolean test(int fx, int fy, int i);
        float r1(int X, int Y) {float x_=(float)X*oW7-1, y_=(float)Y*oH7-1; return (float)sqrt(x_*x_+y_*y_);}}
    private final Bound[] bounds = {
        new Bound("Screen") {boolean test(int x,int y,int i) {return x<W8&&y<H8&&x>=0&&y>=0;}},
        new Bound("Oval")   {boolean test(int x,int y,int i) {return x<W8&&y<H8&&x>=0&&y>=0&&in_circle[wrap((x>>8)+W*(y>>8),MLEN)];}},
        new Bound("Horizon"){boolean test(int x,int y,int i) {return y>0&&y<H8;}},
        new Bound("Radius") {boolean test(int x,int y,int i) {return r1(x,y)<bound_radius;}},
        new Bound("Newton") {boolean test(int x,int y,int i) {return zconv[i];}},
        new Bound("None")   {boolean test(int x,int y,int i) {return false;}}};
    
    ////////////////////////////////////////////////////////////////////////////
    // Gradient ////////////////////////////////////////////////////////////////
    public GradOp   grad_op;
    public char[][] grads;              // All gradient tables; see initGradients()
    public char[]   grad;               // Active gradient lookup table
    public float    gslope        = 1f; // Cursor-controlled gradient rate
    public float    goffset       = 0f; // Cursor-controlled gradient size
    public int      color_mask    = 0;  // XOR this with output color
    public int      feedback_mask = 0;  // XOR this with feedback color
    public int      grad_i        = 0;  // In-use gradient lookup table
    public int      grad_mode     = 0;  // Modes; 0: off 1: one-color 2: two-color
    public void setGradientParam (float slope, float offset) {gslope=slope;goffset=offset;}
    public void setGradientShape (int n) {grad=grads[grad_i=wrap(n,grads.length)];}
    public void setGradient      (int i) {grad_op=grad_modes[grad_mode=wrap(i,grad_modes.length)];}
    public void nextGradientShape(int n) {setGradientShape(grad_i+n);}
    public void nextGradient     (int n) {setGradient(grad_mode+n);  }
    public void toggleInversion()      {color_mask    ^= 0xffffff;}
    public void toggleFeedbackInvert() {feedback_mask ^= 0xffffff;}
    public abstract class GradOp {
        public final String name; 
        public GradOp(String s) {name=s;}
        abstract int go(int i, int c);
        int w(int i){return clip((int)(grad[i]*gslope-goffset),0,255);}}
    public final GradOp[] grad_modes = {
        new GradOp("None")   {int go(int i,int c){return c;}},
        new GradOp("1-Color"){int go(int i,int c){return blend(c,gcolor1,w(i));}},
        new GradOp("2-Color"){int go(int i,int c){int g=w(i); return blend(c,blend(gcolor2,gcolor1,g),g);}}};

    
}
