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
            MR = 0x800080,
            GR = 0x008000;
        
    /** Grabber is an abstract class that retrieves pixel data from a 
     *  texture.
     */
    public static abstract class Sampler {
        /** Retrieve color data at given (x,y) coordinate.
         *
         * @param x
         * @param y
         * @return RRGGBB packed color data
         */
        public abstract int it(int x, int y);
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
    public final Sampler get = new Sampler() {
        @Override
        public int it(int x, int y) {
            // The input is in 8-bit fixed point; Here, we round it to the
            // nearest integer pixel. 
            x = (x + 127) >> 8;
            y = (y + 127) >> 8;
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
    public final Sampler getNoReflect = new Sampler() {
        @Override
        public int it(int x, int y) {
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
    public final Sampler getFixed8Bit = new Sampler() {
        @Override
        public int it(int dx, int dy) {
            int x1, x2, y1, y2;
            // Take absolute value (approximately)
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
    };

    
    /** Linear interpolation with fixed-point and no reflection
     * 
     */
    public final Sampler getFixed8BitNoReflect = new Sampler() {
        @Override
        public int it(int dx, int dy) {
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
            int c1 = buffer.getElem(y1 + x1);
            int c2 = buffer.getElem(y1 + x2);
            int c3 = buffer.getElem(y2 + x1);
            int c4 = buffer.getElem(y2 + x2);
            // Get fractional part and 1-fractional part
            int Ax = dx & 0xFF;
            int Ay = dy & 0xFF;
            int Bx = 256 - Ax;
            int By = 256 - Ay;
            // Compute per-pixel weights
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
    };
    
    /**
     *
     * @param original
     * @param scale
     * @return
     */
    public static Sampler makeScaledGrabber(final Sampler original, final float scale) {
        return new Sampler() {
            @Override
            public int it(int x, int y) {
                return original.it((int)(x*scale),(int)(y*scale));
            }
        };
    }

}
