/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.sqrt;
import static util.Misc.wrap;

/**
 * Routines for sampling from buffered images as textures under different
 * reflection, boundary, and interpolation schemes.
 * 
 * @author mer49
 */
public class Samplers {
    
    public static final String [] reflection_mode_names = {
        "Mirror",
        "Repeat",
        "Triangle"
    };
    
    /** Bit-masked for AARRGGBB 8-bit color data */
    final static int 
            R  = 0xFF0000, 
            G  = 0x00FF00, 
            B  = 0x0000FF, 
            M  = 0xFF00FF,
            MR = 0x800080,
            GR = 0x008000;
    
    public final int w, h, w1, h1, n;
    public final DataBuffer buffer;
    
    /** Abstract class that retrieves pixel data from a texture.
     */
    public abstract class Sampler8Bit extends AbstractSampler {
        /** Retrieve color data at given (x,y) coordinate.
         *
         * @param x
         * @param y
         * @return RRGGBB packed color data
         */
        public abstract int it(int x, int y);
        
        public abstract void weight(int x, int y, int [] index, int [] weight);
    }
    
    public final Sampler8Bit 
            getWrap,  
            getWrap8Bit,
            getReflect, 
            getReflect8Bit,
            getTriangle,
            getTriangle8Bit;
    
    /** Construct a sampler collection matching given buffered image
     * 
     * @param b 
     */
    public Samplers(BufferedImage b) {
        this(b.getWidth(), b.getHeight(), b.getRaster().getDataBuffer());
    }
    
    /** Samplers instantiates a collection of possible samplers for a buffer
     *  with a given size.
     * 
     * @param W_
     * @param H_
     * @param buffer_ 
     */
    public Samplers(int W_, int H_, DataBuffer buffer_) {
        w = W_;
        h = H_;
        n = w*h;
        w1 = w - 1;
        h1 = h - 1;
        buffer = buffer_;
    
        // Nearest-neighbor interpolation with reflected boundaries.
        getReflect = new Sampler8Bit() {
            @Override
            public int it(int x, int y) {
                // The input is in 8-bit fixed point; Here, we round it to the
                // nearest integer pixel. 
                x = x >> 8;
                y = y >> 8;
                if (x<0) x=-x;
                if (y<0) y=-y;
                // If we're an even number of screens in the horizontal/vertical
                // direction, we just wrap via modulo. If we're at an odd number
                // of screens, we need reflect it.
                x = ((x / w & 1) == 0) ? x%w : w1-x%w;
                y = ((y / h & 1) == 0) ? y%h : h1-y%h;
                return buffer.getElem(x+y*w);
            }

            @Override
            public void weight(int x, int y, int[] index, int[] weight) {
                x = x + 127 >> 8;
                y = y + 127 >> 8;
                if (x<0) x=-x;
                if (y<0) y=-y;
                x = ((x / w & 1) == 0) ? x%w : w1-x%w;
                y = ((y / h & 1) == 0) ? y%h : h1-y%h;
                index [0] = x + y*w;
                weight[0] = 256;
            }
        };
        
        // Nearest-neighbor with periodic boundaries.
        getWrap = new Sampler8Bit() {
            @Override
            public int it(int x, int y) {
                // The input is in 8-bit fixed point; Here, we round it to the
                // nearest integer pixel. 
                x = x >> 8;
                y = y >> 8;
                // Don't reflect at all: periodic.
                return buffer.getElem(((x+y*w)%n+n)%n);
            }

            @Override
            public void weight(int x, int y, int[] index, int[] weight) {
                x = x + 127 >> 8;
                y = y + 127 >> 8;
                index [0] = ((x+y*w)%n+n)%n;
                weight[0] = 256;
            }
        };

        // Linear interpolation with 8-bit fixed point and reflection.
        getReflect8Bit = new Sampler8Bit() {
            @Override
            public int it(int x, int y) {
                int x1, x2, y1, y2;
                // Take absolute value (approximately)
                x = x<0? -x : x;
                y = y<0? -y : y;
                // Get the integer part of the pixel locations
                x1 = x >> 8;
                y1 = y >> 8;
                // Get the adjacent pixel
                x2 = x1 + 1;
                y2 = y1 + 1;
                // Reflect location as appropriate
                x1 = (x1/w & 1) == 0 ? x1%w : w1 - x1%w;
                y1 = (y1/h & 1) == 0 ? y1%h : h1 - y1%h;
                // Reflect location as appropriate
                x2 = (x2/w & 1) == 0 ? x2%w : w1 - x2%w;
                y2 = (y2/h & 1) == 0 ? y2%h : h1 - y2%h;
                // Multiply by W to prepare for linear indexing
                y2 *= w;
                y1 *= w;
                // Retrieve color data for 2x2 neighborhood
                int c1 = buffer.getElem(y1 + x1);
                int c2 = buffer.getElem(y1 + x2);
                int c3 = buffer.getElem(y2 + x1);
                int c4 = buffer.getElem(y2 + x2);
                // Get fractional part and 1-fractional part
                int Ax = x & 0xFF;
                int Ay = y & 0xFF;
                int Bx = 256 - Ax;
                int By = 256 - Ay;
                // Compute per-pixe weight
                int C1 = Bx*By >> 8;
                int C2 = Ax*By >> 8;
                int C3 = Bx*Ay >> 8;
                int M1 = M&c1;
                int M2 = M&c2;
                int M3 = M&c3;
                int M4 = M&c4;
                int G1 = G&c1;
                int G2 = G&c2;
                int G3 = G&c3;
                int G4 = G&c4;
                return M & (M4 + ((M1-M4)*C1 + (M2-M4)*C2 + (M3-M4)*C3 + MR >> 8))
                     | G & (G4 + ((G1-G4)*C1 + (G2-G4)*C2 + (G3-G4)*C3 + GR >> 8));
            }

            @Override
            public void weight(int x, int y, int[] index, int[] weight) {
                int x1, x2, y1, y2;
                x = x<0? -x : x;
                y = y<0? -y : y;
                x1 = x + 127 >> 8;
                y1 = y + 127 >> 8;
                x2 = x1 + 1;
                y2 = y1 + 1;
                x1 = (x1/w & 1) == 0 ? x1%w : w1 - x1%w;
                y1 = (y1/h & 1) == 0 ? y1%h : h1 - y1%h;
                x2 = (x2/w & 1) == 0 ? x2%w : w1 - x2%w;
                y2 = (y2/h & 1) == 0 ? y2%h : h1 - y2%h;
                y2 *= w;
                y1 *= w;
                index[0] = y1 + x1;
                index[1] = y1 + x2;
                index[2] = y2 + x1;
                index[3] = y2 + x2;
                int Ax = x & 0xFF, Ay = y & 0xFF;
                int Bx = 256 - Ax, By = 256 - Ay;
                weight[0] = Bx*By + 127 >> 8;
                weight[1] = Ax*By + 127 >> 8;
                weight[2] = Bx*Ay + 127 >> 8;
            }
        };

        // Linear interpolation with fixed-point and no reflection
        getWrap8Bit = new Sampler8Bit() {
            @Override
            public int it(int x, int y) {
                int x1, x2, y1, y2;
                // Integer part
                x1 = x >> 8;
                y1 = y >> 8;
                // Wrap location
                x1 = (x1%w+w)%w;
                y1 = (y1%h+h)%h;
                // Subsequent pixels, also wrapped
                x2 = (x1+1)%w;
                y2 = (y1+1)%h;
                // Rescale y to prepare for row-major indexing
                y2 *= w;
                y1 *= w;
                // Retrieve color data for 2x2 neighborhood
                int c1 = buffer.getElem(y1 + x1);
                int c2 = buffer.getElem(y1 + x2);
                int c3 = buffer.getElem(y2 + x1);
                int c4 = buffer.getElem(y2 + x2);
                // Get fractional part and 1-fractional part
                int Ax = x & 0xFF;
                int Ay = y & 0xFF;
                int Bx = 256 - Ax;
                int By = 256 - Ay;
                // Compute per-pixel weight
                int C1 = Bx*By >> 8;
                int C2 = Ax*By >> 8;
                int C3 = Bx*Ay >> 8;
                int M1 = M&c1;
                int M2 = M&c2;
                int M3 = M&c3;
                int M4 = M&c4;
                int G4 = G&c4;
                int G1 = G&c1;
                int G2 = G&c2;
                int G3 = G&c3;
                return M & (M4 + ((M1-M4)*C1 + (M2-M4)*C2 + (M3-M4)*C3 + MR >> 8))
                     | G & (G4 + ((G1-G4)*C1 + (G2-G4)*C2 + (G3-G4)*C3 + GR >> 8));
            }

            @Override
            public void weight(int x, int y, int[] index, int[] weight) {
                int x1, x2, y1, y2;
                // Integer part
                x1 = x >> 8;
                y1 = y >> 8;
                // Wrap location
                x1 = (x1%w+w)%w;
                y1 = (y1%h+h)%h;
                // Subsequent pixels, also wrapped
                x2 = (x1+1)%w;
                y2 = (y1+1)%h;
                // Rescale y to prepare for row-major indexing
                y2 *= w;
                y1 *= w;
                index[0] = y1 + x1;
                index[1] = y1 + x2;
                index[2] = y2 + x1;
                index[3] = y2 + x2;
                int Ax = x & 0xFF, Ay = y & 0xFF;
                int Bx = 256 - Ax, By = 256 - Ay;
                weight[0] = Bx*By + 127 >> 8;
                weight[1] = Ax*By + 127 >> 8;
                weight[2] = Bx*Ay + 127 >> 8;
            }
        };
        getTriangle = new Barysampler(getReflect);
        getTriangle8Bit = new Barysampler(getReflect8Bit);
    }
    
    /**
     *
     * @param original
     * @param scale
     * @return
     */
    public Sampler8Bit makeScaledGrabber(final Sampler8Bit original, final float scale) {
        return new Sampler8Bit() {
            @Override
            public int it(int x, int y) {
                return original.it((int)(x*scale),(int)(y*scale));
            }
            @Override
            public void weight(int x, int y, int[] index, int[] w) {
                original.weight((int)(x*scale),(int)(y*scale),index,w);
            }
        };
    }

    //Triangular reflected tiling
    public class Barysampler extends Sampler8Bit {
        private final int height,base,h8,b8,h81,b81,h83,b83,xshift;
        private final float b2h, h2b; 
        private final Sampler8Bit orthosampler;
        public Barysampler(Sampler8Bit orthosampler) {
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

        @Override
        public void weight(int x, int y, int[] index, int[] weight) {
            int [] p = triangleize(x,y);
            orthosampler.weight(p[0],p[1],index,weight);
        }
    }
}
