/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package color;

/**
 *
 * @author mer49
 */
public class RGB15 extends RGB {

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
    
    /**
     * @param c1: 050505 -R-G-B packed color
     * @param c2: 050505 -R-G-B packed color
     * @param w: blending weight from 0 to 32
     * @return 
     */
    public final int blend(int c1, int c2, int w) {
        return W5 & (c2 + ( (c1-c2)*w + E5 >> SHIFT ));
    }
    

    @Override
    public int blend(int c1, int c2, int w, int q) {
        // c = W5 & ( c+((q-c)*tint_level + eq>>SHIFT) );
        return W5 & (c2 + ( (c1-c2)*w + (q&E5) >> SHIFT ));
        //throw new UnsupportedOperationException("Not supported yet.");
        // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
        throw new UnsupportedOperationException("Not supported yet."); 
        // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    /**
     * @param c: 050505 -R-G-B packed color
     * @return 0888 RGB packed color
     */
    public final int toIntRGB(int c) {
        //c |= c<<5;
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
    
    
}
