/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image555;

import image555.Samplers555;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.awt.image.DataBuffer;
import static color.ColorUtil.fancy;
import static color.ColorUtil.fast;
import color.RGB;
import color.RGB15;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author mer49
 */
public class Image555 extends RGB {
    
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
    
    public static final int 
        MIRROR   = 0,
        WRAP     = 0,
        TRIANGLE = 0;
    
    public final BufferedImage img;
    public final int w, h, n;

    final boolean is_scaled;
    final float   scale;
    
    public  Graphics2D    g0;
    public  Graphics2D    g2D;
    public  Graphics2D    g;
    private Graphics2D   _g;
    
    public  int [] buf;
    private final int [] _buf;
    
    public  Samplers555 samplers;
    public  Samplers555.Sampler5Bit get;
    private Samplers555.Sampler5Bit _get;
    
    private final AtomicBoolean frozen = new AtomicBoolean(true);
    
    public Image555(BufferedImage b, boolean interp, int reflect, boolean is_scaled, float scale) {

        img  = b;
        img.setAccelerationPriority(1.f);
        
        w = b.getWidth();
        h = b.getHeight();
        n = w*h;
        
        g0  = fast(b.createGraphics());
        g2D = fancy(b.createGraphics());
        g   = g2D;
        
        _buf     = new int[n];
        buf      = _buf;
        samplers = new Samplers555(w,h,buf);
        
        this.is_scaled = is_scaled;
        this.scale     = scale;
        
        setInterpolatedAndReflected(interp, reflect);
        thaw();
    }
    
    public Image555(BufferedImage b, boolean interp, int reflect) {
        this(b,interp,reflect,false,1.0f);
    }
    
    public Image555(int w, int h, boolean interpolate, int reflect) {
        this(new BufferedImage(w,h,TYPE_INT_RGB), interpolate, reflect);
    }

    public final void setInterpolatedAndReflected(boolean inter, int reflect) {
        synchronized (frozen) {
            int i = (inter?1:0)*3 + reflect;
            _get = (new Samplers555.Sampler5Bit[]{
                samplers.getReflect,
                samplers.getWrap,
                samplers.getTriangle,
                samplers.getReflect5Bit,
                samplers.getWrap5Bit,
                samplers.getTriangle5Bit})[i];
            if (is_scaled)    
                _get = samplers.makeScaledGrabber(_get, scale);
            get = frozen.get()? null : _get;
        }
    }
    
    public void setFancy(boolean fancy) {
        synchronized (frozen) {
            if (frozen.get()) 
                g = fancy? g2D : g0;
            else 
                _g = fancy? g2D : g0;
        }
    }

    /**
     * Move the image into the buffered image.
     */
    public void freeze() {
        synchronized (frozen) {
            if (frozen.get()) 
                throw new RuntimeException("Image is already frozen.");
            // Linear Congruential RNG seeded with stronger RNG at start of scan
            //int rn = (int)((long)(Math.random()*4294967296L)&0xffffffff)|1,
            //rA = 0x343FD, rC = 0x269EC3;
            // Copy image data to intRGB
            DataBuffer b = img.getRaster().getDataBuffer();
            for (int i=0; i<n; i++) {
                //int q  = ((rn=rn*rA+rC)>>8) & 0b000001110000011100000111;
                int c  = this._buf[i];
                //c |= c<<5;
                //b.setElem(i, c );
                b.setElem(i, RGB.c15.toIntRGB(c) );
            }
            //img.getData().createCompatibleWritableRaster().setPixels(0, 0, w, h, buf);
            // Make graphics visible, make int array hidden
            this.g   = _g;
            this.buf = null;
            this.get = null;
            frozen.set(true);
        }
    }
    
    /**
     * Freeze assuming that the image data buffer is not stale.
     */
    public void coldFreeze() {
        synchronized (frozen) {
            if (frozen.get()) 
                throw new RuntimeException("Image is already frozen.");
            // Make graphics visible, make int array hidden
            this.g   = _g;
            this.buf = null;
            this.get = null;
            frozen.set(true);
        }
    }
    

    /**
     * Move the image into the integer array.
     */
    public final void thaw() {
        synchronized (frozen) {
            if (!frozen.get()) 
                throw new RuntimeException("Image is already thawed.");
            
            // Linear Congruential RNG seeded with stronger RNG at start of scan
            //int rn = (int)((long)(Math.random()*4294967296L)&0xffffffff)|1,
            //rA = 0x343FD, rC = 0x269EC3;
            
            // Copy from image to integer buffer
            DataBuffer b = img.getRaster().getDataBuffer();
            for (int i=0; i<n; i++) {
                int c = b.getElem(i);
                /*
                int q  = ((rn=rn*rA+rC)>>8) & 0b000001110000011100000111;
                int r,s;
                // dithering code to reduce noticeability of 5-bit color
                // first, clear lowest bit to make room for a carry
                // (clear the blue for consistency even though we don't need to)
                // then, add a number that will make each color overflow
                // _if_ adding noise could make it overflow.
                // remember thes 'saturated' channels; add noise; then
                // set saturated channels to white.
                c &= 0b0111111101111111011111110;
                r  = 0b0000010000000100000001000 + c;
                s  = 0b1000000010000000100000000 & r;
                c += 0b0000001110000011100000111 & q;
                c &= 0b0111110001111100011111000;
                c |= (s>>8)*0b11111000;
                c = 
                    ((c&0b00000000111110000000000000000000)<<1)|
                    ((c&0b00000000000000001111100000000000)>>1)|
                    ((c&0b00000000000000000000000011111000)>>3);
                this._buf[i] = c;
                */
                this._buf[i] = RGB.c15.fromIntRGB(c);
            }
            //img.getData().getPixels(0, 0, w, h, buf);

            // Hide the graphics, expose the integer buffer
            this._g  = this.g;
            this.g   = null;
            this.buf = _buf;
            this.get = _get;
            
            frozen.set(false);
        }
    }

    public void copy(Image555 o) {
        // Locking technically not quite right but does the job
        synchronized (frozen) {
            if (frozen.get()) {
                if (o.isFrozen())
                    img.getRaster().setDataElements(0,0,w,h,o.img.getRaster());
                else {
                    DataBuffer b = img.getRaster().getDataBuffer();
                    for (int i=0; i<n; i++) b.setElem(i, RGB.c15.toIntRGB(o.buf[i]));
                }
            } else {
                if (o.isFrozen()) {
                    DataBuffer b = o.img.getRaster().getDataBuffer();
                    for (int i=0; i<n; i++) this._buf[i] = RGB.c15.fromIntRGB(b.getElem(i));
                } else System.arraycopy(o.buf, 0, this.buf, 0, n);
            }
        }
    }
    
    public boolean isFrozen() {
        return frozen.get();
    }
    
    
    /**
     * @param c1: 050505 -R-G-B packed color
     * @param c2: 050505 -R-G-B packed color
     * @param w: blending weight from 0 to 32
     * @return 
     */
    public final int blend(int c1, int c2, int w) {
        return RGB.c15.blend(c1, c2, w);
    }
    
    /**
     * 
     * @param c1
     * @param c2
     * @return 
     */
    public int mean(int c1, int c2) {
        return W5 & (c1 + c2 >> 1);
    }
    
    /**
     * Linearly interpolate in 2D
     * @param c00: 050505 -R-G-B packed color
     * @param c01: 050505 -R-G-B packed color
     * @param c10: 050505 -R-G-B packed color
     * @param c11: 050505 -R-G-B packed color
     * @param wx: blending weight from 0 to 32
     * @param wy: blending weight from 0 to 32
     * @return 
     */
    public final int lirp(int c00, int c01, int c10, int c11, int wx, int wy) {
        int ux = UNIT - wx;
        int uy = UNIT - wy;
        // Compute per-pixel weights; Rounding might be optional here
        int p = ux*uy + ROUND >> SHIFT;
        int q = wx*uy + ROUND >> SHIFT;
        int r = ux*wy + ROUND >> SHIFT;
        return W5&(c11+((c00-c11)*p+(c01-c11)*q+(c10-c11)*r+E5>>SHIFT));
    }
    
    /**
     * Merge two translucent colors into one color with premultiplied alpha/
     * 
     * @param c1
     * @param c2
     * @param w1
     * @param w2
     * @param result 
     */
    public void composeBlend(int[] result, int c1, int w1, int c2, int w2) {
        w1 -= (w2 * w1 >> 5);
        result[0] = W5 & (c1*w1 + c2*w2 + E5 >> 5);
        result[1] = w1 + w2;
    }
    
    /**
     * 
     * @param result
     * @param c
     * @param a
     * @param q
     * @param w 
     */
    public void composePremultiplied(int[] result, int c, int a, int q, int w) {
        result[0] = W5 & (c + ((q-c)*w + E5 >> SHIFT));
        result[1] = w + a - (w * a >> SHIFT);
    }

    @Override
    public int blendPremultiplied(int c, int q, int w) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    /**
     * @param c: 050505 -R-G-B packed color
     * @return 0888 RGB packed color
     */
    public final int toIntRGB(int c) {
        //c |= c<<5;
        //return (R8&(c>>8))|(G8&(c>>4))|(B8&(c>>2));
        return 
            ((c & 0b000001111100000000000000000000)>>1)|
            ((c & 0b000000000000000111110000000000)<<1)|
            ((c & 0b000000000000000000000000011111)<<3);
    }
    
    /**
     * @param c: 0888 RGB packed color
     * @return 050505 -R-G-B packed color
     */
    public final int fromIntRGB(int c) {
        return 
            ((c&0b00000000111110000000000000000000)<<1)|
            ((c&0b00000000000000001111100000000000)>>1)|
            ((c&0b00000000000000000000000011111000)>>3);
    }
    
    /**
     * @param c: 0555 short RGB packed color
     * @return 050505 -R-G-B packed color
     */
    public final int toShort555RGB(int c) {
        return 
            (0b111110000000000&(c>>10))|
            (0b000001111100000&(c>>5 ))|
            (0b000000000011111&(c    ));
    }
    
    /**
     * 
     * @param c
     * @return 
     */
    public final int fromShort555RGB(int c) {
        return 
            ((0b111110000000000&c)<<10)|
            ((0b000001111100000&c)<<5)|
            ((0b000000000011111&c));
    }

    @Override
    public int blend(int c1, int c2, int w, int q) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
}
