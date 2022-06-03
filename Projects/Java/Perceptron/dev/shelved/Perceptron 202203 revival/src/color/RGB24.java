/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package color;

/**
 *
 * @author mer49
 */
public class RGB24 extends RGB {


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
     * @param c1 intRGB bottom color 
     * @param c2 intRGB top color 
     * @param α1 alpha weight for bottom layer in 0..256
     * @param α2 alpha weight for top layer in 0..256
     * @param result int[2] to return the {color*alpha,alpha} result
     */
    public final void composeBlend(int[] result, int c1, int α1, int c2, int α2) {
        // Version for aplha in 0..256 (would need a new way to return alpha)
        // Composition weights for c1 and c2; 0 <= α <= 256
        // 8-bit fixed point derivation of w1 = (1-α2) α1 
        //      (256 - α2) α1  >> 8 
        //      256 α1 - α2 α1 >> 8 
        //       α1 - (α2 α1 >> 8)
        int w1 = α1 - (α2 * α1 >> 8);
        int w2 = α2;
        // Version limiting resulting alpha to 0..255
        //int w1 = α1 - ((α1*α2 + 255>>8)) ;
        //int w2 = α2;
        // Alpha is the sum of the weights
        int α  = w1 + w2;
        // Extract 0x0000GG00 and 0x00RR00BB components
        int m1 = M8 & c1;
        int m2 = M8 & c2;
        int g1 = G8 & c1;
        int g2 = G8 & c2;
        int m  = M8 & (m1*w1 + m2*w2 + ME8 >> 8);
        int g  = G8 & (g1*w1 + g2*w2 + GE8 >> 8);
        result[0] = m | g;
        result[1] = α;
    }
    
    /**
     * Compose translucency with a base color is in alpha-premultiplied format.
     * Everything is the same as composeBlend() except base color c1 is provided
     * as a fixed-point multiplication of the color value and α1
     *      c1 = c0 * alpha + 127 >> 8
     * In the non-premultiplied case the weight of the first color is
     *      α1 - (α2 * α1 >> 8)
     * In floating point that is
     *      α1*(1 - α2)
     * With α1 premultiplied we instead use a weight of 1-α2. In fixed point
     *      256 - α2
     * This reduces the composition to an ordinary linear interpolation.
     * @param c: base color with alpha premultiplied
     * @param q: overlay color
     * @param a: base translucency [0..256]
     * @param w: overlay translucency
     * @param result int[2] to return the {color*alpha,alpha} result
     */
    public final void composePremultiplied(int[] result, int c, int a, int q, int w) {
        int ma = M8 & c;
        int ga = G8 & c;
        int m2 = M8 & q;
        int g2 = G8 & q;
        int m  = M8 & (ma + ((m2 - ma)*w + ME8 >> 8));
        int g  = G8 & (ga + ((g2 - ga)*w + GE8 >> 8));
        result[0] = m | g;
        result[1] = w + a - (w * a >> 8);
    }

    public final int lirp(int c1, int c2, int c3, int c4, int Ax, int Ay) {
        int Bx = 256 - Ax;
        int By = 256 - Ay;
        int C1 = Bx*By >> 8;
        int C2 = Ax*By >> 8;
        int C3 = Bx*Ay >> 8;
        int M1 = M8&c1;
        int M2 = M8&c2;
        int M3 = M8&c3;
        int M4 = M8&c4;
        int G1 = G8&c1;
        int G2 = G8&c2;
        int G3 = G8&c3;
        int G4 = G8&c4;
        return M8 & (M4 + ((M1-M4)*C1 + (M2-M4)*C2 + (M3-M4)*C3 + ME8 >> 8))
             | G8 & (G4 + ((G1-G4)*C1 + (G2-G4)*C2 + (G3-G4)*C3 + GE8 >> 8));
    }

    public final int toIntRGB(int c) {
        return c;
    }
    
    public final int fromIntRGB(int c) {
        return c;
    }

    public final int toShort555RGB(int c) {
        return 
            c & 0xf80000 >> 9 |
            c & 0x00f800 >> 6 |
            c & 0x0000f8 >> 3;  
    }

    public final int fromShort555RGB(int c) {
        c = c & 0x001f << 3 |
            c & 0x03e0 << 6 |
            c & 0x7c00 << 9 ;
        c |= c & 0x739c;
        return c;
    }
    
}
