/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image555;

import image.AbstractSampler;
import static java.lang.Math.sqrt;
import static util.Misc.wrap;
import static util.Sys.sout;

/**
 * Routines for sampling from buffered images as textures under different
 * reflection, boundary, and interpolation schemes.
 * 
 * @author mer49
 */
public class Samplers555 {
    
    public static final String [] reflection_mode_names = {
        "Mirror",
        "Repeat",
        "Triangle"
    };
    
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
    
    public final int w, h, w1, h1, n;
    public final int [] buffer;
    
    public abstract class Sampler5Bit extends AbstractSampler {}
    public final Sampler5Bit 
            getWrap,  
            getWrap5Bit,
            getReflect, 
            getReflect5Bit,
            getTriangle,
            getTriangle5Bit;
    
    /** Samplers instantiates a collection of possible samplers for a buffer
     *  with a given size.
     * 
     * @param W_
     * @param H_
     * @param buffer_ 
     */
    public Samplers555(int W_, int H_, int [] buffer_) {
        w = W_;
        h = H_;
        n = w*h;
        w1 = w - 1;
        h1 = h - 1;
        buffer = buffer_;
    
        // Nearest-neighbor interpolation with reflected boundaries.
        getReflect = new Sampler5Bit() {
            @Override
            public int it(int x, int y) {
                // The input is in 8-bit fixed point; Here, we round it to the
                // nearest integer pixel. 
                x = x >> SHIFT;
                y = y >> SHIFT;
                if (x<0) x=-x;
                if (y<0) y=-y;
                // If we're an even number of screens in the horizontal/vertical
                // direction, we just wrap via modulo. If we're at an odd number
                // of screens, we need reflect it.
                x = ((x / w & 1) == 0) ? x%w : w1-x%w;
                y = ((y / h & 1) == 0) ? y%h : h1-y%h;
                return buffer[x+y*w];
            }
        };
        
        // Nearest-neighbor with periodic boundaries.
        getWrap = new Sampler5Bit() {
            @Override
            public int it(int x, int y) {
                // The input is in 8-bit fixed point; Here, we round it to the
                // nearest integer pixel. 
                x = x >> SHIFT;
                y = y >> SHIFT;
                // Don't reflect at all: periodic.
                return buffer[((x+y*w)%n+n)%n];
            }
        };

        // Linear interpolation with 8-bit fixed point and reflection.
        getReflect5Bit = new Sampler5Bit() {
            @Override
            public int it(int x, int y) {
                int x1, x2, y1, y2;
                // Take absolute value (approximately)
                x = x<0? -x : x;
                y = y<0? -y : y;
                // Get the integer part of the pixel locations
                x1 = x >> SHIFT;
                y1 = y >> SHIFT;
                // Get the adjacent pixel
                x2 = x1 + 1;
                y2 = y1 + 1;
                // Reflect location as appropriate
                x1 = (x1/w&1)==0 ? x1%w : w1 - x1%w;
                y1 = (y1/h&1)==0 ? y1%h : h1 - y1%h;
                // Reflect location as appropriate
                x2 = (x2/w&1)==0 ? x2%w : w1 - x2%w;
                y2 = (y2/h&1)==0 ? y2%h : h1 - y2%h;
                // Multiply by W to prepare for linear indexing
                y2 *= w;
                y1 *= w;
                // Retrieve color data for 2x2 neighborhood
                int c1 = buffer[y1 + x1];
                int c2 = buffer[y1 + x2];
                int c3 = buffer[y2 + x1];
                int c4 = buffer[y2 + x2];
                // Get fractional part and 1-fractional part
                int Ax = x & MASK;
                int Ay = y & MASK;
                // Compute per-pixe weight
                //int w4 = Ax*Ay >> SHIFT;
                //return W5 & (c1 + ((c2-c1)*Ax + (c3-c1)*Ay + (c1-c2-c3+c4)*w4 + ROUND >> SHIFT));
                int w4  = Ax*Ay >> SHIFT;
                int d31 = c3 - c1;
                return W5 & (c1 + ((c2-c1)*Ax + d31*Ay + (c4-c2-d31)*w4 + ROUND >> SHIFT));
            }
        };

        // Linear interpolation with fixed-point and no reflection
        getWrap5Bit = new Sampler5Bit() {
            @Override
            public int it(int x, int y) {
                int x1, x2, y1, y2;
                // Integer part
                x1 = x >> SHIFT;
                y1 = y >> SHIFT;
                // Wrap location
                x1 = (x1%w+w)%w;
                y1 = (y1%h+h)%h;
                // Subsequent pixels, also wrapped
                x2 = x1<w-1? x1+1 : 0;
                y2 = y1<h-1? y1+1 : 0;
                // Rescale y to prepare for row-major indexing
                y2 *= w;
                y1 *= w;
                // Retrieve color data for 2x2 neighborhood
                int c1 = buffer[y1 + x1];
                int c2 = buffer[y1 + x2];
                int c3 = buffer[y2 + x1];
                int c4 = buffer[y2 + x2];
                // Get fractional part and 1-fractional part
                int Ax = x & MASK;
                int Ay = y & MASK;
                //int Bx = UNIT - Ax;
                //int By = UNIT - Ay;
                // Compute per-pixel weight
                //int C1 = Bx*By >> SHIFT;
                //int C2 = Ax*By >> SHIFT;
                //int C3 = Bx*Ay >> SHIFT;
                //return W5 & (c4 + ((c1-c4)*C1 + (c2-c4)*C2 + (c3-c4)*C3 + ROUND >> SHIFT));
                int w4  = Ax*Ay >> SHIFT;
                int d31 = c3 - c1;
                return W5 & (c1 + ((c2-c1)*Ax + d31*Ay + (c4-c2-d31)*w4 + ROUND >> SHIFT));
            }
        };
        getTriangle = new Barysampler(getReflect);
        getTriangle5Bit = new Barysampler(getReflect5Bit);
    }
    
    /**
     *
     * @param original
     * @param scale
     * @return
     */
    public Sampler5Bit makeScaledGrabber(final AbstractSampler original, final float scale) {
        return new Sampler5Bit() {
            @Override
            public int it(int x, int y) {
                return original.it((int)(x*scale),(int)(y*scale));
            }
        };
    }

    //Triangular reflected tiling
    public class Barysampler extends Sampler5Bit {
        private final int height,base,h8,b8,h81,b81,h83,b83,xshift;
        private final float b2h, h2b; 
        private final AbstractSampler orthosampler;
        public Barysampler(AbstractSampler orthosampler) {
            assert (w>=h);
            this.orthosampler = orthosampler;
            // Triangular region of screen
            height = h;
            base   = (int)(height*(float)(2f/sqrt(3))+0.5);
            // In 8-bit fixedpoint
            h8  = height<<8; b8  = base<<8;
            h81 = h8-1;      b81 = b8-1;
            h83 = h8*3;      b83 = b8*3;
            xshift = (w*256-b8)/2;
            b2h = (float)height/(float)base;
            h2b = (float)base/(float)height;
            System.out.println(height+" "+base+" "+
                h8+" "+b8+" "+
                h81+" "+b81+" "+
                h83+" "+b83);
        }
        int [] triangleize(int x, int y) {
            x = w*256-1-x-xshift; y = h*256-y;
            // x shifts by 1.5 triangles widths (half a unit cell
            // for every integer multiple of the unit cell in the y 
            // direction
            //int iy = y / h83;
            // Slide x coordinate to the RIGHT by height/2
            x = x + (y*148+127>>8);
            // Wrap vertically to 3x3 unit cell
            // Wrap horizontally to 3x3 unit cell
            y = wrap(y, h83); x = wrap(x, b83);
            // Figure out which row, col of 3x3 unit cell
            // Wrap to 1x1 unit cell
            // Flip x coordinate
            int row = y/h8, col = x/b8;
            y = y % h8; x = x % b8;
            // Check whether we're in lower or upper triangle
            // Flip down if in upper
            boolean upper = base*y>height*x;
            if (upper) y = h81-y; else x = b81-x;
            int qx = (int)(x*b2h), qy = (int)(y*h2b);
            int px=0, py=0;
            // Triangle code
            // 0: take from screen as normal
            // 1: rotate screen anticlockwise pi/3
            // 2: rotate screen clockwise pi/3
            int code = (row+col)%3;
            if (upper) code = 5-code;
            switch (code) {
                case 0: case 4: px = x; break;
                case 1: case 5: px = qy; break;
                case 2: case 3: px = b81-x-qy; break;
            }
            switch (code) {
                case 0: case 3: py = h81-y; break;
                case 2: case 5: py = h81-qx; break;
                case 1: case 4: py = y+qx; break;
            }
            px += ((h81-py)*148+127>>8)+xshift;
            return new int[]{px,py};
        }
        
        public int it(int x, int y) {
            int [] p = triangleize(x,y);
            return orthosampler.it(p[0],p[1]);
        }
    }
}
