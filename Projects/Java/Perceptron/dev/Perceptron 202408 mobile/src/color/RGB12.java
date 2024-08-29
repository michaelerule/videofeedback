/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package color;

/**
 *
 * @author mer49
 */
public class RGB12 extends RGB {

    public static final int
            SHIFT = 4,
            UNIT  = 1 << SHIFT, // 16
            MASK  = UNIT -  1,  // 15
            ROUND = MASK >> 1,  // 8
            W4 = 0x0404040,
            R4 = 0x0400000,
            G4 = 0x0004000,
            B4 = 0x0000040,
            M4 = 0x0400040,
            O4 = 0b0000000100000001000000010000,
            E4 = O4*ROUND;

    public final int mean(int c1, int c2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final int blend(int c1, int c2, int w) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Precompose two transparent colors.
     * The resulting color is returned with alpha premultiplied to avoid 
     * division by zero. The new alpha value is 
     *      alpha = (1-α2) α1 + α2
     * The new RGB color with alpha premultiplied is
     *      (1-α2) α1 c1 + α2 c2
     * 
     * @param result
     * @param c intRGB bottom color 
     * @param a alpha weight for bottom layer in 0..256
     * @param w intRGB top color 
     * @param q alpha weight for top layer in 0..256
     */
    public final void composeBlend(int[] result, int c, int a, int q, int w) {
        int w1 = a - (w * a >> 4);
        result[0] = W4 & (c*w1 + q*w + E4 >> 4);
        result[1] = w1 + w;
    }

    @Override
    public void composePremultiplied(int[] result, int c, int α, int q, int w) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int blendPremultiplied(int c, int q, int w) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int lirp(int c1, int c2, int c3, int c4, int wx, int wy) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int toIntRGB(int c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int fromIntRGB(int c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int toShort555RGB(int c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int fromShort555RGB(int c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
