package util;
/* ColorUtility.java
 * Created on March 13, 2007, 8:45 PM
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static util.Misc.clip;
import static util.Matrix.diag;
import static util.Matrix.invert_3x3;
import static util.Matrix.multiply;
import static util.Matrix.multiply_3x3_Laderman;
import static util.Matrix.multiply_3x3_point;
import static util.Matrix.rotation;

/**
 * @author Michael Everett Rule
 */
public class ColorUtil {

    /** The size of lookup arrays */
    public static final int LUTSIZE = 128;
    /** Largest lookup index */
    public static final int MAXLUTINDEX = LUTSIZE - 1;

    /** RGB bit masks */
    public static final int 
        R = 0x00FF0000,
        G = 0x0000FF00,
        B = 0x000000FF,
        M = 0x00FF00FF,
        MR= 0x00800080,
        GR= 0x00008000;
    
    /** A standard lookup table for converting HSV to Java.awt.Color*/
    public static Color[][] HSVtoRGB_color;
    
    /** A standard lookup table for converting HSV to RGB color data */
    public static int[][] HSVtoRGB_lookup;
    
    /** A standard lookup table for converting HSV to RGB color data */
    public static int[][][] RGBtoHSV_lookup;

    /** Static initializer generates the lookup tables */
    static {

        /** generate the HSV->RGB table*/
        /** generate the HSV->Color table*/
        HSVtoRGB_lookup = new int[LUTSIZE][LUTSIZE];
        HSVtoRGB_color  = new Color[LUTSIZE][LUTSIZE];
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
    public static int hue(int RGB) {
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
    public static int hueColor(int RGB) {
        int HSV = RGBtoHSV(RGB);
        return HSVtoRGB_lookup[(HSV >> 19) & 0x0000001F][MAXLUTINDEX];
    }

    //OPERATIONS
    /** fade the color by the given value (out of 256)
     * @param color
     * @param fader
     * @return  */
    public static int fade(int color, int fader) {
        return M & (fader * (color & M) >> 8)
             | G & (fader * (color & G) >> 8);
    }

    /** simple average of two colors
     * @param c1
     * @param c2
     * @return  */
    public static int blend(int c1, int c2) {
        return M & (((c1 & M) + (c2 & M) + 0x00010001) >> 1)
             | G & (((c1 & G) + (c2 & G) + 0x00000100) >> 1);
    }
    /** average two colors based on two given weights
     * @param p
     * @param q
     * @param w
     * @return  */
    public static int blend(int p, int q, int w) {
        // Faster 12-bit version
        //w1>>=4;
        //if (w1>16) w1=16;
        //c1&=0xf0f0f0;
        //c2&=0xf0f0f0;
        //return w1*(c1-c2) + (c2<<4) + 0x808080 >> 4;
        // 24 bit version
        int m1 = p&M;
        int m2 = q&M;
        int g1 = p&G;
        int g2 = q&G;
        return M&(m2+((m1-m2)*w+MR>>8))|G&(g2+((g1-g2)*w+GR>>8));
    }

    /**
     *
     * @param color
     * @return
     */
    public static int RGBContrast(int color) {
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
    public static int RGBContrast(int color, int degree) {
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
    public static int darkSaturate(int color) {
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

    /** Quick and dirty increase contrast by fixed amount.
     * @param color
     * @return
     */
    public static int contrast(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        r = (r << 1) - 128;
        g = (g << 1) - 128;
        b = (b << 1) - 128;
        return (clip(r,0,255) << 16) 
             | (clip(g,0,255) << 8) 
             |  clip(b,0,255);
    }

    /** This averages RGB components to uniform gray. There may be a slight
     *  bias toward darkening the color (255/256 rounding issue). 
     * @param color as intRRGGBB
     * @return Gray color intRRGGBB
     */
    public static int toGray(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int mush = ((r+g+b) * 85 + 0x8000) & 0xff00;
        return (mush << 8) | (mush) | (mush >> 8);
    }

    /**
     * @param color as intRRGGBB
     * @return intRRGGBB
     */
    public static int foobar(int color) {
        //return average(~RGB_contrast(color),RGB_contrast(~color));
        //return average(color,220,saturate(color),36);
        //return average(color,128,saturate(color),128);
        //return average(color,contrast(color));
        return contrast(color);
    }

    /** Take component-wise maximum of two colors.
     * @param c1
     * @param c2
     * @return
     */
    public static int colormax(int c1, int c2) {
        return (int) max(c1 & R, c2 & R)
             | (int) max(c1 & G, c2 & G)
             | (int) max(c1 & B, c2 & B);
    }


    /** Rotate hue using system HSV model.
     * @param RGB
     * @param d
     * @return
     */
    public static synchronized int rotate_hue(int RGB, float d) {/*
        int HSV = RGBtoHSV(RGB);
        return HSVtoRGB_lookup[ ( (HSV >> 19) + (int)( d * HSVtoRGB_lookup.length )) % HSVtoRGB_lookup.length ][max];
         */
        final float[] temp = new float[]{0, 0, 0};
        Color.RGBtoHSB(
                (RGB >> 16) & 0xFF,
                (RGB >> 8) & 0xFF,
                (RGB) & 0xFF,
                temp);
        return Color.HSBtoRGB(temp[0] + d, 1f, 1f);
    }

    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // ABY  chroma*exp(i hue) = A + iB; Y is LUMA //////////////////////////////
    

    // See https://en.wikipedia.org/wiki/HSL_and_HSV#Hue_and_chroma 
    public static final float [] 
            LUMA_SDTV  = {.2989f, .5871f, .1140f},
            LUMA_ADOBE = {.2120f, .7010f, .0870f},
            LUMA_HDTV  = {.2126f, .7152f, .0722f},
            LUMA_HDR   = {.2627f, .6780f, .0593f},
            LUMA_NAIVE = {1/3f, 1/3f, 1/3f};
    public static final float [] LUMA = LUMA_NAIVE;
    public static final float
            LUMA_R = LUMA[0],
            LUMA_G = LUMA[1],
            LUMA_B = LUMA[2];
    public static final float SIN60 = 0.8660254037844386f;
    // Overall the matrix form of this equation is
    // [ alpha     ]   [ 1   -1/2   -1/2 ][ r ]
    // [ beta      ] = [ 0   √3/2  -√3/2 ][ g ]
    // [ lightness ]   [ 1/3  1/3    1/3 ][ b ]
    public static final float [][] 
        ABY_MATRIX = {
            {1f, -.5f, -.5f},
            {0f, SIN60, -SIN60},
            {LUMA[0], LUMA[1], LUMA[2]}    
        };
    
    public static final float [][] ABY_INVERSE = invert_3x3(ABY_MATRIX);
    
    /**
     * @param color
     * @return 
     */
    public static final float [] intRGBtoFloatRGB(int color) {
        return new float[]{
            (color>>16)&0xff,
            (color>>8)&0xff,
            color&0xff
        };
    }
    
    /**
     * @param color
     * @return 
     */
    public static final int floatRGBtoIntRGB(float [] color) {
        int r = clip((int)(color[0]+0.5f),0,255);
        int g = clip((int)(color[1]+0.5f),0,255);
        int b = clip((int)(color[2]+0.5f),0,255);
        return (r<<16)|(g<<8)|b;
    }
    
    /**
     * @param color
     * @param result
     * @return 
     */
    public static final float[] floatRGBtoABY(float[] color, float[] result) {
        return multiply_3x3_point(ABY_MATRIX, color, result);
    }
    
    /**
     * @param color
     * @param result
     * @return 
     */
    public static final float[] intRGBtoABY(int color, float[] result) {
        return floatRGBtoABY(intRGBtoFloatRGB(color), result);
    }
    
    /**
     * 
     * @param hue Hue shift in 0..2pi
     * @param sat Saturation scaling, positive, 1=no change. 
     * @param lum Luma adjustment, positive, 1=no change
     * @return 
     */
    public static final float[][] makeHueSatLumaOperator(float hue, float sat, float lum) {
        return 
            multiply(ABY_INVERSE, 
            multiply(rotation(3,0,1,hue), // hue rotate
            multiply(diag(sat, sat, lum), // saturation and luma
                ABY_MATRIX
            )));
    }
    
    public static final int applyColorMatrix(float [][] M, int color) {
        return floatRGBtoIntRGB( multiply_3x3_point(M,intRGBtoFloatRGB(color)));
    }
    
    public static final int applyColorAffine(float [][] M, float []D, int color) {
        float [] c = multiply_3x3_point(M,intRGBtoFloatRGB(color));
        c[0] += D[0];
        c[1] += D[1];
        c[2] += D[2];
        return floatRGBtoIntRGB(c);
    }
    
    public static final int applyColorAffine8bit(int [][] M, int []D, int c) {
        int r0 = (c & 0xff0000) >> 16;
        int g0 = (c & 0xff00) >> 8;
        int b0 = (c & 0xff);
        int r1 = M[0][0] * r0 + M[0][1] * g0 + M[0][2] * b0 + D[0];
        int g1 = M[1][0] * r0 + M[1][1] * g0 + M[1][2] * b0 + D[1];
        int b1 = M[2][0] * r0 + M[2][1] * g0 + M[2][2] * b0 + D[2];
        r1 = clip((r1 + 127)>>8 , 0, 255);
        g1 = clip((g1 + 127)>>8 , 0, 255);
        b1 = clip((b1 + 127)>>8 , 0, 255);
        return(r1<<16)|(g1<<8)|b1;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // CONVOLUTIONS ////////////////////////////////////////////////////////////
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
        return M & ((W11 * (c11 & M) + W12 * (c12 & M) + W13 * (c13 & M)
            + W21 * (c21 & M) + W22 * (c22 & M) + W23 * (c23 & M)
            + W31 * (c31 & M) + W32 * (c32 & M) + W33 * (c33 & M) + MR) >> 8)
            | G & ((W11 * (c11 & G) + W12 * (c12 & G) + W13 * (c13 & G)
            + W21 * (c21 & G) + W22 * (c22 & G) + W23 * (c23 & G)
            + W31 * (c31 & G) + W32 * (c32 & G) + W33 * (c33 & G) + GR) >> 8);
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
        return M & ((R12 * (c12 & M)
                + R21 * (c21 & M) + R22 * (c22 & M) + R23 * (c23 & M)
                + R32 * (c32 & M) + MR) >> 8)
                | G & ((R12 * (c12 & G)
                + R21 * (c21 & G) + R22 * (c22 & G) + R23 * (c23 & G)
                + R32 * (c32 & G) + GR) >> 8);
    }

    /** performs a convolution kernel on the cross neighborhood,
     *  indexed starting at the upper left by rows then columns
     * @param c1
     * @param c3
     * @param c2
     * @return  */
    public static int convolve_3x1(int c1, int c2, int c3) {
        return M & ((W1 * (c1 & M) + W2 * (c2 & M) + W3 * (c3 & M) + MR) >> 8)
             | G & ((W1 * (c1 & G) + W2 * (c2 & G) + W3 * (c3 & G) + GR) >> 8);
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Rendering option control
    /** Turn on top-quality rendering options for Graphics2D.
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
    /** Turn on top speed rendering options for Graphics2D.
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
