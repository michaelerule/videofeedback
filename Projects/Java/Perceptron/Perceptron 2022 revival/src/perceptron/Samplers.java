/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package perceptron;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

/**
 * Routines for sampling from buffered images as textures under different
 * reflection, boundary, and interpolation schemes.
 * 
 * @author mer49
 */
public class Samplers {
    
    /** Bit-masked for AARRGGBB 8-bit color data */
    final static int 
            R  = 0xFF0000, 
            G  = 0x00FF00, 
            B  = 0x0000FF, 
            M  = 0xFF00FF,
            Mr = 0x800080,
            Gr = 0x008000;
        
    /** Grabber is an abstract class that retrieves pixel data from a 
     *  texture.
     */
    public static abstract class Grabber {
        /** Retrieve color data at given (x,y) coordinate.
         *
         * @param x
         * @param y
         * @return RRGGBB packed color data
         */
        public abstract int get(int x, int y);
    }
    
    /**
     *
     */
    public final int W, H, WONE, HONE, N;

    /**
     *
     */
    public final DataBuffer buffer;
    
    /** Samplers instantiates a collection of possible samplers for a buffer
     *  with a given size.
     * 
     * @param W_
     * @param H_
     * @param buffer_ 
     */
    public Samplers(int W_, int H_, DataBuffer buffer_) {
        W = W_;
        H = H_;
        N = W*H;
        WONE = W - 1;
        HONE = H - 1;
        buffer = buffer_;
    }
    
    /**
     *
     * @param b
     */
    public Samplers(BufferedImage b) {
        this(b.getWidth(), b.getHeight(), b.getRaster().getDataBuffer());
    }

    
    /** Nearest-neighbor interpolation with reflected boundaries.
     */
    public final Grabber get = new Grabber() {
        @Override
        public int get(int x, int y) {
            // The input is in 8-bit fixed point; Here, we round it to the
            // nearest integer pixel. 
            x = (x + 127) >> 8;
            y = (y + 127) >> 8;
            // The sign-preserving right shift will move the sign bit
            // into all bit-indecies if the number is negative. 
            // Flipping all the bits essentially negates the integer
            // Well actually, abs(x)-1, which is good enough.
            //x ^= x >> 31;
            //y ^= y >> 31;
            x = x<0? -x : x;
            y = y<0? -y : y;
            // If we're an even number of screens in the horizontal/vertical
            // direction, we just wrap via modulo. If we're at an odd number
            // of screens, we need reflect it.
            x = ((x / W & 1) == 0) ? x%W : WONE-x%W;
            y = ((y / H & 1) == 0) ? y%H : HONE-y%H;
            return buffer.getElem(x+y*W);
        }
    };

    
    /** Nearest-neighbor with periodic boundaries.
     *  This is not used at the moment; 
     */
    public final Grabber getNoReflect = new Grabber() {
        @Override
        public int get(int x, int y) {
            // The input is in 8-bit fixed point; Here, we round it to the
            // nearest integer pixel. 
            x = (x + 127) >> 8;
            y = (y + 127) >> 8;
            // Don't reflect at all: periodic.
            return buffer.getElem(((x+y*W)%N+N)%N);
        }
    };

    
    /** Linear interpolation with 8-bit fixed point and reflection.
     */
    public final Grabber getFixed8Bit = new Grabber() {
        @Override
        public int get(int dx, int dy) {
            int x1, x2, y1, y2;
            // Take absolute value (approximately)
            //dy ^= dy >> 31;
            //dx ^= dx >> 31;
            dx = dx<0? -dx : dx;
            dy = dy<0? -dy : dy;
            // Get the integer part of the pixel locations
            x1 = dx >> 8;
            y1 = dy >> 8;
            // Get the adjacent pixel
            x2 = x1 + 1;
            y2 = y1 + 1;
            // Reflect location as appropriate
            x1 = (x1/W & 1) == 0 ? x1%W : WONE - x1%W;
            y1 = (y1/H & 1) == 0 ? y1%H : HONE - y1%H;
            // Reflect location as appropriate
            x2 = (x2/W & 1) == 0 ? x2%W : WONE - x2%W;
            y2 = (y2/H & 1) == 0 ? y2%H : HONE - y2%H;
            // Multiply by W to prepare for linear indexing
            y2 *= W;
            y1 *= W;
            // Retrieve color data for 2x2 neighborhood
            int c1 = buffer.getElem(y1 + x1);
            int c2 = buffer.getElem(y1 + x2);
            int c3 = buffer.getElem(y2 + x1);
            int c4 = buffer.getElem(y2 + x2);
            // Get fractional part and 1-fractional part
            int Ax = dx & 0xFF;
            int Ay = dy & 0xFF;
            int Bx = 256 - Ax;
            int By = 256 - Ay;
            // Compute per-pixe weights
            int C1 = Bx*By + 127 >> 8;
            int C2 = Ax*By + 127 >> 8;
            int C3 = Bx*Ay + 127 >> 8;
            int C4 = Ax*Ay + 127 >> 8;
            assert C1+C2+C3+C4<=256;
            // Interpolated color averaging
            return M & ((M&c1)*C1 + (M&c2)*C2 + (M&c3)*C3 + (M&c4)*C4 + Mr >> 8)
                 | G & ((G&c1)*C1 + (G&c2)*C2 + (G&c3)*C3 + (G&c4)*C4 + Gr >> 8);
        }
    };

    
    /** Linear interpolation with fixed-point and no reflection
     * 
     */
    public final Grabber getFixed8BitNoReflect = new Grabber() {
        @Override
        public int get(int dx, int dy) {
            int x1, x2, y1, y2;
            // Integer part
            x1 = dx >> 8;
            y1 = dy >> 8;
            // Wrap location
            x1 = (x1%W+W)%W;
            y1 = (y1%H+H)%H;
            // Subsequent pixels, also wrapped
            x2 = (x1+1)%W;
            y2 = (y1+1)%H;
            // Rescale y to prepare for row-major indexing
            y2 *= W;
            y1 *= W;
            // Retrieve color data for 2x2 neighborhood
            int c1 = buffer.getElem((y1 + x1));
            int c2 = buffer.getElem((y1 + x2));
            int c3 = buffer.getElem((y2 + x1));
            int c4 = buffer.getElem((y2 + x2));
            // Get fractional part and 1-fractional part
            int Ax = dx & 0xFF;
            int Ay = dy & 0xFF;
            int Bx = 256 - Ax;
            int By = 256 - Ay;
            // Compute per-pixe weights
            int C1 = Bx*By + 127 >> 8;
            int C2 = Ax*By + 127 >> 8;
            int C3 = Bx*Ay + 127 >> 8;
            int C4 = Ax*Ay + 127 >> 8;
            assert C1+C2+C3+C4<=256;
            // Interpolated color averaging
            return M & ((M&c1)*C1 + (M&c2)*C2 + (M&c3)*C3 + (M&c4)*C4 + Mr >> 8)
                 | G & ((G&c1)*C1 + (G&c2)*C2 + (G&c3)*C3 + (G&c4)*C4 + Gr >> 8);
        }
    };
    
    /**
     *
     * @param original
     * @param scale
     * @return
     */
    public static Grabber makeScaledGrabber(final Grabber original, final float scale) {
        return new Grabber() {
            @Override
            public int get(int x, int y) {
                return original.get((int)(x*scale),(int)(y*scale));
            }
        };
    }

}
