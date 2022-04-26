package util;
/* ColorUtility.java
 * Created on March 13, 2007, 8:45 PM
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * @author Michael Everett Rule
 */
public class ColorUtil {

    /** The size of lookup arrays */
    public static final int LUTSIZE = 128;
    /** Largest lookup index */
    public static final int MAXLUTINDEX = LUTSIZE - 1;

    /** RGB bitmasks */
    public static final int 
        R = 0x00FF0000,
        G = 0x0000FF00,
        B = 0x000000FF,
        T = 0x00FF00FF,
        AT= 0x00800080,
        AG= 0x00008000;
    
    /** A standard lookuptable for converting HSV to Java.awt.Color*/
    public static Color[][] HSVtoRGB_color;
    
    /** A standard lookuptable for converting HSV to RGB color data */
    public static int[][] HSVtoRGB_lookup;
    
    /** A standard lookuptable for converting HSV to RGB color data */
    public static int[][][] RGBtoHSV_lookup;

    /** Static initialiser generates the lookup tables */
    static {

        /** generate the HSV->RGB table*/
        /** generate the HSV->Color table*/
        HSVtoRGB_lookup = new int[LUTSIZE][LUTSIZE];
        HSVtoRGB_color = new Color[LUTSIZE][LUTSIZE];
        float scalar = 1.f / LUTSIZE;
        for (int h = 0; h < LUTSIZE; h++) {
            for (int v = 0; v < LUTSIZE; v++) {
                int c = Color.HSBtoRGB(h * scalar, 1.f, v * scalar);
                HSVtoRGB_lookup[h][v] = c;
                HSVtoRGB_color[h][v]  = new Color(c);
            }
        }

        /** generate the RGB->HSV table */
        RGBtoHSV_lookup = new int[LUTSIZE][LUTSIZE][LUTSIZE];
        scalar = 255.f / MAXLUTINDEX;
        float[] hsv = {0, 0, 0};
        for (int r = 0; r < LUTSIZE; r++) {
            for (int g = 0; g < LUTSIZE; g++) {
                for (int b = 0; b < LUTSIZE; b++) {
                    hsv = Color.RGBtoHSB((int) (r * scalar), (int) (g * scalar), (int) (b * scalar), hsv);
                    int h = (int) (hsv[0] * 255);
                    int s = (int) (hsv[1] * 255);
                    int v = (int) (hsv[2] * 255);
                    RGBtoHSV_lookup[r][g][b] = (h << 16) | (s << 8) | (v);
                }
            }
        }
    }

    //STATIC FUNCTIONS
    //CONVERSIONS
    /** Converts float HSB to appropriate color model
     * @param h
     * @param v
     * @return  */
    public static int HSVtoRGB(float h, float v) {
        return HSVtoRGB_lookup[(int) (h * LUTSIZE) % LUTSIZE][(int) (v * LUTSIZE) % LUTSIZE];
    }

    /** Converts int HSB to appropriate color model
     * @param h
     * @param v
     * @return  */
    public static int HSVtoRGB(int h, int v) {
        return HSVtoRGB_lookup[h][v];
    }

    /** Converts int hue to appropriate color model
     * @param h
     * @return  */
    public static int HSVtoRGB(int h) {
        return HSVtoRGB_lookup[h][MAXLUTINDEX];
    }

    /** Converts int R,G,B to 0x00HHSSVV
     * @param R
     * @param G
     * @param B
     * @return  */
    public static int RGBtoHSV(int R, int G, int B) {
        return RGBtoHSV_lookup[R][G][B];
    }

    /** Converts int 0x00RRGGBB to 0x00HHSSVV
     * @param RGB
     * @return  */
    public static int RGBtoHSV(int RGB) {
        return RGBtoHSV_lookup[(RGB >> 19) & 0x0000001F][(RGB >> 11) & 0x0000001F][(RGB >> 3) & 0x0000001F];
    }

    /** This extracts the HUE of an intRGB color and returns a color with
     *  the same HUE but full saturation and value
     * @param RGB
     * @return  */
    public static int Hue(int RGB) {
        return (int) (Color.RGBtoHSB(
                (RGB >> 16) & 0x000000FF,
                (RGB >> 8) & 0x000000FF,
                (RGB) & 0x000000FF,
                new float[]{0, 0, 0})[0] * MAXLUTINDEX);
        //int HSV = RGBtoHSV(RGB);
        //return (HSV >> 19) & 0x0000001F;
    }

    /** This extracts the HUE of an intRGB color and returns a color with
     *  the same HUE but full saturation and value
     * @param RGB
     * @return  */
    public static int Hue_color(int RGB) {
        int HSV = RGBtoHSV(RGB);
        return HSVtoRGB_lookup[(HSV >> 19) & 0x0000001F][MAXLUTINDEX];
    }

    //OPERATIONS
    /** fade the color by the given value (out of 256)
     * @param color
     * @param fader
     * @return  */
    public static int fade(int color, int fader) {
        return T & (fader * (color & T) >> 8)
                | G & (fader * (color & G) >> 8);
    }

    /** simple average of two colors
     * @param c1
     * @param c2
     * @return  */
    public static int average(int c1, int c2) {
        return T & (((c1 & T) + (c2 & T) + 0x00010001) >> 1)
                | G & (((c1 & G) + (c2 & G) + 0x00000100) >> 1);
    }

    /**
     *
     * @param color
     * @return
     */
    public static int RGB_contrast(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int r1 = (r * 512 - (g + b) * 128);
        int g1 = (g * 512 - (r + b) * 128);
        int b1 = (b * 512 - (g + r) * 128);
        r1 &= ~((r1 & 0x80000000) >> 31);
        g1 &= ~((g1 & 0x80000000) >> 31);
        b1 &= ~((b1 & 0x80000000) >> 31);
        r1 >>= 8;
        g1 >>= 8;
        b1 >>= 8;
        int t1 = (r1 << 16) | b1;
        int M1 = (t1 >> 1) & 0x800080 | (g1 << 7) & 0x8000;
        M1 |= M1 >> 1;
        M1 |= M1 >> 2;
        M1 |= M1 >> 4;
        return (t1 & 0xff00ff | ((g1 & 0xff) << 8)) | M1 & 0xffffff;
    }

    /**
     *
     * @param color
     * @param degree
     * @return
     */
    public static int RGB_contrast(int color, int degree) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int y = degree << 1;
        int x = 2 * (128 + y);
        int r1 = (r * x - (g + b) * y);
        int g1 = (g * x - (r + b) * y);
        /*
        r1 &= ~((r1&0x80000000)>>31);
        g1 &= ~((g1&0x80000000)>>31);
        b1 &= ~((b1&0x80000000)>>31);
         */
        if (r1 < 0) r1 = 0;
        r1 >>= 8;
        if (r1 > 0xff) r1 = 0xff;
        
        if (g1 < 0) g1 = 0;
        g1 >>= 8;
        if (g1 > 0xff) g1 = 0xff;
        /*
        int b1 = (b * x - (g + r) * y);
        if (b1 < 0) {
            b1 = 0;
        }
        b1 >>= 8;
        if (b1 > 0xff) {
            b1 = 0xff;
        }
        int t1 = (r1<<16)|b1;
        int M1 = (t1>>1)&0x800080|(g1<<7)&0x8000;
        M1|=M1>>1;
        M1|=M1>>2;
        M1|=M1>>4;
         */
        //return (t1&0xff00ff|((g1&0xff)<<8))|M1&0xffffff;
        return (r1 << 16) | (g1 << 8) | b;
    }

    /**
     *
     * @param color
     * @return
     */
    public static int saturate(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int min = r < g ? b < r ? 0 : 16 : b < g ? 0 : 8;
        int max = r > g ? b > r ? 0 : 16 : b > g ? 0 : 8;
        if (min == max) {
            return color;
        }
        int mid = 24 - min - max;
        return (0xff << max) | ((((color >> mid) & 0xff) * 0xff / ((color >> max) & 0xff)) << mid);
    }

    /**
     *
     * @param color
     * @return
     */
    public static int dark_saturate(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int min = r < g ? b < r ? 0 : 16 : b < g ? 0 : 8;
        int max = r > g ? b > r ? 0 : 16 : b > g ? 0 : 8;
        if (min == max) {
            return color;
        }
        int mid = 24 - min - max;
        int minc = ((color >> min) & 0xff);
        int midc = ((color >> mid) & 0xff);
        int maxc = ((color >> max) & 0xff);
        int dark = minc * maxc / (maxc + midc);
        maxc += dark;
        midc += minc - dark;
        return ((maxc > 0xff ? 0xff : maxc) << max) | ((midc > 0xff ? 0xff : midc) << mid);
    }

    /**
     *
     * @param color
     * @return
     */
    public static int contrast(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        r = ((r << 1) - 128);
        g = ((g << 1) - 128);
        b = ((b << 1) - 128);
        r = r < 0 ? 0 : r > 0xff ? 0xff : r;
        g = g < 0 ? 0 : g > 0xff ? 0xff : g;
        b = b < 0 ? 0 : b > 0xff ? 0xff : b;
        return (r << 16) | (g << 8) | b;
    }

    /**
     *
     * @param color
     * @return
     */
    public static int mush(int color) {
        int mush = (((0xff & (color >> 16)) + (0xff & (color >> 8)) + (0xff & (color))) * 85) & 0xff00;
        return (mush << 8) | (mush) | (mush >> 8);
    }

    /**
     *
     * @param color
     * @return
     */
    public static int foobar(int color) {
        //return average(~RGB_contrast(color),RGB_contrast(~color));
        //return average(color,220,saturate(color),36);
        //return average(color,128,saturate(color),128);
        //return average(color,contrast(color));
        return contrast(color);
    }

    /**
     *
     * @param c1
     * @param c2
     * @return
     */
    public static int colormax(int c1, int c2) {
        return R & ((int) (Math.max((c1 & R), (c2 & R))))
                | G & ((int) (Math.max((c1 & G), (c2 & G))))
                | B & ((int) (Math.max((c1 & B), (c2 & B))));
    }

    /** average two colors based on two given weights
     * @param c1
     * @param w1
     * @param c2
     * @param w2
     * @return  */
    public static int average(int c1, int w1, int c2, int w2) {
        return T & (((c1 & T) * w1 + (c2 & T) * w2 + AT) >> 8)
                | G & (((c1 & G) * w1 + (c2 & G) * w2 + AG) >> 8);
    }
    //CONVOLUTIONS
    //3x3 convoltion weights
    private static final int 
            W11 = 20, W12 = 32, W13 = 20,
            W21 = 32, W22 = 48, W23 = 32,
            W31 = 20, W32 = 32, W33 = 20;
    //cross convoltion weights
    private static final int 
            R12 = 47, R21 = 47, R22 = 68, 
            R23 = 47, R32 = 47;
    //3x1 convoltion weights
    private static final int 
            W1 = 64, W2 = 128, W3 = 64;

    //CONVOLUTION FUNCTIONS
    /** performs a convolution kernel on 3x3 neighborhood,
     *  indexed starting at the upper left by rows then columns
     * @param c11
     * @param c12
     * @param c23
     * @param c13
     * @param c33
     * @param c21
     * @param c22
     * @param c31
     * @param c32
     * @return  */
    public static int convolve_3x3(
            int c11, int c12, int c13,
            int c21, int c22, int c23,
            int c31, int c32, int c33) {
        return T & ((W11 * (c11 & T) + W12 * (c12 & T) + W13 * (c13 & T)
            + W21 * (c21 & T) + W22 * (c22 & T) + W23 * (c23 & T)
            + W31 * (c31 & T) + W32 * (c32 & T) + W33 * (c33 & T) + AT) >> 8)
            | G & ((W11 * (c11 & G) + W12 * (c12 & G) + W13 * (c13 & G)
            + W21 * (c21 & G) + W22 * (c22 & G) + W23 * (c23 & G)
            + W31 * (c31 & G) + W32 * (c32 & G) + W33 * (c33 & G) + AG) >> 8);
    }

    /** performs a convolution kernel on the cross neighborhood,
     *  indexed starting at the upper left by rows then columns
     * @param c12
     * @param c21
     * @param c23
     * @param c22
     * @param c32
     * @return  */
    public static int convolve_cross(int c12, int c21, int c22, int c23, int c32) {
        return T & ((R12 * (c12 & T)
                + R21 * (c21 & T) + R22 * (c22 & T) + R23 * (c23 & T)
                + R32 * (c32 & T) + AT) >> 8)
                | G & ((R12 * (c12 & G)
                + R21 * (c21 & G) + R22 * (c22 & G) + R23 * (c23 & G)
                + R32 * (c32 & G) + AG) >> 8);
    }

    /** performs a convolution kernel on the cross neighborhood,
     *  indexed starting at the upper left by rows then columns
     * @param c1
     * @param c3
     * @param c2
     * @return  */
    public static int convolve_3x1(int c1, int c2, int c3) {
        return T & ((W1 * (c1 & T) + W2 * (c2 & T) + W3 * (c3 & T) + AT) >> 8)
                | G & ((W1 * (c1 & G) + W2 * (c2 & G) + W3 * (c3 & G) + AG) >> 8);
    }
    static float[] temp = new float[]{0, 0, 0};

    /**
     *
     * @param RGB
     * @param d
     * @return
     */
    public static synchronized int rotate_hue(int RGB, float d) {/*
        int HSV = RGBtoHSV(RGB);
        return HSVtoRGB_lookup[ ( (HSV >> 19) + (int)( d * HSVtoRGB_lookup.length )) % HSVtoRGB_lookup.length ][max];
         */

        Color.RGBtoHSB(
                (RGB >> 16) & 0xFF,
                (RGB >> 8) & 0xFF,
                (RGB) & 0xFF,
                temp);
        return Color.HSBtoRGB(temp[0] + d, 1f, 1f);

    }

    /**
     *
     * @param image_graphics
     * @return
     */
    public static Graphics2D fancy(Graphics2D image_graphics) {
        image_graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        image_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        image_graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        image_graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        image_graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        image_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        return image_graphics;
    }
    
    /**
     * 
     * @param image_graphics
     * @return 
     */
    public static Graphics2D fast(Graphics2D image_graphics) {
        image_graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        image_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        image_graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        image_graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        image_graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        image_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        image_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints. 	VALUE_TEXT_ANTIALIAS_OFF);
        return image_graphics;
    }
}
