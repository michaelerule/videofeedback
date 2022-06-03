/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package color;

import image.AbstractSampler;
import static java.awt.Color.CYAN;
import static java.awt.Color.RED;
import static java.awt.Color.YELLOW;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static color.ColorUtil.fancy;
import static color.ColorUtil.fast;

/**
 * An experimental image format for 555RGB color modes.
 * 
 * This is a Short555 image datatype which keeps a second pixel-data buffer
 * packed as: 
 * 
 *      0b00-----RRRRR-----GGGGG-----BBBBB
 * 
 * This uses an int32 to store 15-bit color in a way that allows faster 5-bit 
 * fixed-point interpolation operations.
 * 
 * Note: we could probably get 665 RGB with very little extra cost TODO.
 * 
 * Drawing commands to `img` are not immediately reflected in `data` and vice-
 * versa. You must call `.freeze()` to copy the 050505 `data` buffer into the 
 * backing `img`, and `.thaw()` to copy pixels to the int050505 data buffer. 
 * 
 * The int050505 buffer and the backing BufferedImage TYPE_SHORT_555 are 
 * usually out of sync. Wrap in locking structures to ensure thread- re-entrant
 * safety. Manually sync as need, as seldom as possible. Use with caution.
 * 
 * @author mer49
 */
public class ImageRGB15 extends RGB15 {
    
    
    // Image dimensions and int050505 data buffer
    public final int w, h, w1, h1, length;
    public final int[] data;
    
    // These are public but must be manually synced wih the buffer[] state
    // in order for drawing commands to have a lasting effect.
    public final BufferedImage img;
    public final Graphics2D    g1;
    public final Graphics2D    g2;
    public final DataBuffer    buf;
    
    public abstract class Sampler5Bit extends AbstractSampler {
        /** 
         * Retrieve color data at given (x,y) coordinate.
         * @param x
         * @param y
         * @return 050505 packed RGB color data
         */
        public abstract int it(int x, int y);
    }
        
    // Analagously to ImageRenderContext
    public final Sampler5Bit 
            getReflect, 
            getWrap, 
            getReflect8Bit,
            getWrap8Bit;
    
    public ImageRGB15(int W, int H) {
        w = W; h = H; length = w*h; w1 = w-1; h1 = h-1; 
        data = new int[length];
        
        // BufferedImage to support drawing commands
        img = new BufferedImage(w,h,BufferedImage.TYPE_USHORT_555_RGB);
        img.setAccelerationPriority(1.f);
        g1  = fast(img.createGraphics());
        g2  = fancy(img.createGraphics());
        buf = img.getRaster().getDataBuffer();
        
        // Nearest-neighbor with periodic boundaries.
        getWrap = new Sampler5Bit() {public int it(int x, int y) {
            x = x>>8; y = y>>8;
            return data[((x+y*w)%length+length)%length];
        }};
        // Nearest-neighbor interpolation with reflected boundaries.
        getReflect = new Sampler5Bit() {public int it(int x, int y) {
            x = x + 127 >> 8;   y = y + 127 >> 8;
            if (x<0) x=-x;      if (y<0) y=-y;
            x = ((x / w & 1) == 0) ? x%w : w1-x%w;
            y = ((y / h & 1) == 0) ? y%h : h1-y%h;
            // try (1-b*2)*(x%W) + b*W1
            return data[x+y*w];
        }};
        // Linear interpolation with 8-bit fixed point and reflection.
        getReflect8Bit = new Sampler5Bit() {public int it(int x, int y) {
            int x1, x2, y1, y2, wx, wy;
            x = x+0b100>>3;     y = y+0b100>>3;
            x = x<0? -x : x;    y = y<0? -y : y;
            x1 = x >> 5;        y1 = y >> 5;
            x2 = x1 + 1;        y2 = y1 + 1;
            x1 = (x1/w & 1) == 0 ? x1%w : w1-x1%w;
            y1 = (y1/h & 1) == 0 ? y1%h : h1-y1%h;
            x2 = (x2/w & 1) == 0 ? x2%w : w1-x2%w;
            y2 = (y2/h & 1) == 0 ? y2%h : h1-y2%h;
            y2 *= w;        y1 *= w;
            wx = x&0x1F;    wy = y&0x1F;
            return lirp(data[y1+x1],data[y1+x2],data[y2+x1],data[y2+x2],wx,wy);
        }};
        // Linear interpolation with fixed-point and no reflection
        getWrap8Bit = new Sampler5Bit() {public int it(int x, int y) {
            int x1, x2, y1, y2, wx, wy;
            x = x+0b100>>3;     y = y+0b100>>3;
            x1 = x >> 5;        y1 = y >> 5;
            x1 = (x1%w+w)%w;    y1 = (y1%h+h)%h;
            x2 = x1+1;          y2 = y1+1;
            if (x2>=w) x2-=w;   if (y2>=h) y2-=h;
            y2 *= w;            y1 *= w;
            wx = x&0x1F;        wy = y&0x1F;
            return lirp(data[y1+x1],data[y1+x2],data[y2+x1],data[y2+x2],wx,wy);
        }};
    }
    
    /**
     * Freeze and thaw: Convert state to and from drawable image.
     */
    public boolean frozen = false;
    public void freeze() {
        if (frozen) return;
        for (int i=0; i<length; i++) buf.setElem(i,toShort555RGB(data[i]));
        frozen = true;
    }
    public void thaw() {
        if (!frozen) return;
        for (int i=0; i<length; i++) data[i] = fromShort555RGB(buf.getElem(i));
        frozen = false;
    }
    
    private static final String PLEASE_THAW = "please .thaw() image before writing";
    
    /**
     * Draw an image over this one with given alpha layer
     * @param other: Image555 to draw over this one
     * @param alpha: blending weights in 0..32 (unchecked!)
     */
    public void blend(ImageRGB15 other, byte [] alpha) {
        if (alpha.length!=length) throw new IllegalArgumentException(
                "Alpha buffer must match image buffer size");
        if (other.length!=length) throw new IllegalArgumentException(
                "Overdrawn buffer must match image buffer size");
        if (frozen) throw new RuntimeException(PLEASE_THAW);
        for (int i=0; i<length; i++)
            data[i] = blend(data[i],other.data[i],alpha[i]);
    }
    
    /**
     * Tint the image by color with given transparency
     * @param color: int050505 color
     * @param alpha: weight between 0 and 32
     */
    public void tint(int color, int alpha) {
        if (alpha<0 || alpha>32) throw new IllegalArgumentException(
                "alpha must be between >=0 and <=32, is "+alpha);
        if (frozen) throw new RuntimeException(PLEASE_THAW);
        for (int i=0; i<length; i++) 
            data[i] = blend(data[i],color,alpha);
    }
    
    /**
     * Set pixel value in buffer
     * @param x int in 0..w-1
     * @param y int in 0..h-1
     * @param c int050505 color
     */
    public void set(int x, int y, int c) {
        if (x<0 || x>=w || y<0 || y>=h) throw new IllegalArgumentException(
                "point ("+x+","+y+") out of range for "+w+"x"+h+" image");
        if (frozen) throw new RuntimeException(PLEASE_THAW);
        data[x+y*w]=c&W05;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    public static void main(String [] args) {
        int w = 480, h = 320;
        
        ImageRGB15 img = new ImageRGB15(w,h);
        
        JPanel see_me = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(img.img,0,0,w,h,null);
            }
        };
        
        // Freeze the image so we can draw on it, and draw a red rectangle.
        img.freeze();
        img.g1.setColor(RED);
        img.g1.fillRect(w/4, h/4, w/2, h/2);
        
        // Test 050505 blending: this should auto-thaw the image
        img.thaw();
        img.tint(RGB15.BLUE5, 16);
        
        // Test linear interpolation. Draw something worth interpolating.
        // Freeze the image to enable drawing then thaw it to enable direct
        // manipulation of int050505-format data.
        img.freeze();
        img.g1.setColor(YELLOW);
        img.g1.drawRect(w/4, h/4, w/2, h/2);
        img.g1.setColor(CYAN);
        img.g1.drawLine(w-1, 0, w-1, h-1);
        img.thaw();

        Sampler5Bit get = img.getReflect8Bit;
        int qx,qy,x0,y0;
        qx = (w/4-2)*256;
        qy = (h/4-2)*256;
        x0 =  w/4+w/2-64; 
        y0 =  h/4+h/2-64;
        for (int dy=0; dy<=128; dy++) {
            for (int dx=0; dx<=128; dx++) {
                img.set(x0+dx, y0+dy, get.it(qx+dx*8,qy+dy*8));
            }
        }
        get = img.getWrap8Bit;
        qx = (w-2)*256;
        qy = (h/4-2)*256;
        x0 =  w/4-64; 
        y0 =  h/4+h/2-64;
        for (int dy=0; dy<=128; dy++) {
            for (int dx=0; dx<=128; dx++) {
                img.set(x0+dx, y0+dy, get.it(qx+dx*8,qy+dy*8));
            }
        }
        
        JFrame test = new JFrame("test");
        test.setDefaultCloseOperation(EXIT_ON_CLOSE);
        test.setPreferredSize(new Dimension(w,h));
        test.setResizable(false);
        test.setContentPane(see_me);
        test.pack();
        test.setVisible(true);
        
        img.freeze();
        
        
        // Benchmark wrapping solutions
        /*
            x = ((x / w & 1) == 0) ? x%w : w1-x%w;
            y = ((y / h & 1) == 0) ? y%h : h1-y%h;
            // try (1-b*2)*(x%W) + b*W1
            x2 = (x2/w & 1) == 0 ? x2%w : w1-x2%w;
            y2 = (y2/h & 1) == 0 ? y2%h : h1-y2%h;
            x1 = (x1%w+w)%w;    y1 = (y1%h+h)%h;
        */
    }
    
}
