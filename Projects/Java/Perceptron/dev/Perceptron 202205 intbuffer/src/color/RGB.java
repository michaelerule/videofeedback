package color;

/**
 * A Compositor describes abstract methods for manipulating integer-packed
 * color data. 
 * 
 * We plan to implement 3 compositors: 
 *  - 24-bit intRGB packed as 0x00RRGGBB
 *  - 12-bit packed as 0x00R0G0B0
 *  - 15-bit packed as 0b00[00000][rrrrr][00000][ggggg][00000][bbbbb]
 * 
 * These color routines are wrapped inside instances to allow color 
 * datatypes to be hot-swapped at runtime. 
 * 
 * @author mer49
 */
public abstract class RGB {
    
    // List of implemented compositors
    public static final RGB15 c15 = new RGB15();
    public static final RGB12 c12 = new RGB12();
    public static final RGB24 c24 = new RGB24();
    
    // Color masks for intRGB format
    public static final int 
        R8 = 0xff0000,
        G8 = 0x00ff00,
        B8 = 0x0000ff,
        M8 = 0xff00ff,
        ME8= 0x00800080,
        GE8= 0x00008000;
    
    // Color masks for short555RGB format
    public static final int 
        R5 = 0b111110000000000,
        G5 = 0b000001111100000,
        B5 = 0b000000000011111;
    
    /**
     * mean(int, int) returns the arithmetic average of two colors
     * @param c1 int-packed color 1
     * @param c2 int-packed color 2
     * @return 
     */
    public abstract int mean(int c1, int c2);
    
    /**
     * blend(int, int, int) returns a weighted average, where the weight
     * controls the influence of the second color
     * @param c1 int-packed color 1
     * @param c2 int-packed color 2
     * @param w  blending weight, equivalent to alpha
     * @return 
     */
    public abstract int blend(int c1, int c2, int w);
    
    /**
     * Generate premultiplied alpha format from two transparent colors.
     * Color 2 is treated as drawn atop color 1
     * 
     * @param c1 int-packed color 1
     * @param w1 blending weight, equivalent to alpha for color 1
     * @param c2 int-packed color 2
     * @param w2 blending weight, equivalent to alpha for color 2
     * @param result
     */
    public abstract void composeBlend(int[] result, int c1, int w1, int c2, int w2);
    
    /**
     * Compose to premultiplied alpha from non-premultiplied, in-place.
     * @param c
     * @param w
     * @param inplace 
     */
    public final void composeBlend(int[] inplace, int c, int w) {
        composeBlend(inplace,inplace[0],inplace[1],c,w);
    }
    
    /**
     * Blend color c with premultiplied translucent color (q,w)
     * @param c
     * @param q
     * @param w 
     */
    public abstract int blendPremultiplied(int c, int q, int w);
    
    /**
     * Compose translucency with a base color is in alpha-premultiplied format.
     * @param c: base color with alpha premultiplied
     * @param q: overlay color
     * @param α: base translucency [0..256]
     * @param w: overlay translucency
     * @param result int[2] to return the {color*alpha,alpha} result
     */
    public abstract void composePremultiplied(int[] result, int c, int α, int q, int w);
    
    /**
     * Compose to premultiplied alpha in-place.
     * @param q
     * @param w
     * @param inplace 
     */
    public final void composePremultiplied(int[] inplace, int q, int w) {
        composePremultiplied(inplace,inplace[0],inplace[1],q,w);
    }
    
    /**
     * Linearly interpolate in 2D
     * @param c1: RGB color for 0,0 pixel
     * @param c2: RGB color for 0,1 pixel
     * @param c3: RGB color for 1,0 pixel
     * @param c4: RGB color for 1,1 pixel
     * @param wx: blending weight from 0 to 32
     * @param wy: blending weight from 0 to 32
     * @return 
     */
    public abstract int lirp(int c1, int c2, int c3, int c4, int wx, int wy);
    
    /**
     * Convert color to INT_RGB
     * @param c: 050505 -R-G-B packed color
     * @return 0888 RGB packed color
     */
    public abstract int toIntRGB(int c);
    
    /**
     * Convert IntRGB to color
     * @param c: 0888 RGB packed color
     * @return 050505 -R-G-B packed color
     */
    public abstract int fromIntRGB(int c);
    
    /**
     * Convert color to USHORT_555_RGB
     * @param c: 0555 short RGB packed color
     * @return 050505 -R-G-B packed color
     */
    public abstract int toShort555RGB(int c);
    
    /**
     * Convert USHORT_555_RGB to color
     * @param c
     * @return 
     */
    public abstract int fromShort555RGB(int c);

}