package util;

import perceptron.Perceptron;

import java.awt.*;

/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 * @author Predrag Bokšić (junkerade@gmail.com)
 *         <p/>
 *         Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *         It evolves colored geometric patterns and visual images into the realm of infinite details
 *         and deepens the thought. </p>
 *         <p/>
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 */

public final class ColorUtility {

    // The size of lookup arrays
    public static final int size = 128;
    // Largest lookup index
    public static final int max = size - 1;
    // RGB bitmasks
    public static final int R = 0x00FF0000, G = 0x0000FF00, B = 0x000000FF, T = 0x00FF00FF;
    public static final int A_T = 0x00800080, A_G = 0x00008000;
    /**
     * A standard lookup table for converting HSV to Java.awt.Color
     */
    public static final Color[][] HSVtoRGB_color;
    /**
     * A standard lookup table for converting HSV to RGB color data
     */
    public static final int[][] HSVtoRGB_lookup;
    /**
     * A standard lookup table for converting HSV to RGB color data
     */
    public static final int[][][] RGBtoHSV_lookup;

    /**
     * Static initializer generates the lookup tables
     */
    static {

        /**
         * generate the HSV->RGB table
         */
        /**
         * generate the HSV->Color table
         */
        HSVtoRGB_lookup = new int[size][size];
        HSVtoRGB_color = new Color[size][size];
        float scalar = 1.f / size;
        for (int h = 0; h < size; h++) {
            for (int v = 0; v < size; v++) {
                HSVtoRGB_lookup[h][v] = Color.HSBtoRGB(h * scalar, 1.f, v * scalar);
                HSVtoRGB_color[h][v] = new Color(HSVtoRGB_lookup[h][v]);
            }
        }

        /**
         * generate the RGB->HSV table
         */
        RGBtoHSV_lookup = new int[size][size][size];
        scalar = 255.f / max;
        float[] hsv = {0, 0, 0};
        for (int r = 0; r < size; r++) {
            for (int g = 0; g < size; g++) {
                for (int b = 0; b < size; b++) {
                    hsv = Color.RGBtoHSB((int) (r * scalar), (int) (g * scalar), (int) (b * scalar), hsv);
                    int h = (int) (hsv[0] * 255);
                    int s = (int) (hsv[1] * 255);
                    int v = (int) (hsv[2] * 255);
                    RGBtoHSV_lookup[r][g][b] = (h << 16) | (s << 8) | (v);
                }
            }
        }
    }

    // STATIC FUNCTIONS
    // CONVERSIONS

    /**
     * Converts float HSB to appropriate color model
     */
    public static int HSVtoRGB(float h, float v) {
        return HSVtoRGB_lookup[(int) (h * size) % size][(int) (v * size) % size];
    }

    /**
     * Converts int HSB to appropriate color model
     */
    public static int HSVtoRGB(int h, int v) {
        return HSVtoRGB_lookup[h][v];
    }

    /**
     * Converts int hue to appropriate color model
     */
    public static int HSVtoRGB(int h) {
        return HSVtoRGB_lookup[h][max];
    }

    /**
     * Converts int R,G,B to 0x00HHSSVV
     */
    public static int RGBtoHSV(int R, int G, int B) {
        return RGBtoHSV_lookup[R][G][B];
    }

    /**
     * Converts int 0x00RRGGBB to 0x00HHSSVV
     */
    public static int RGBtoHSV(int RGB) {
        return RGBtoHSV_lookup[(RGB >> 19) & 0x0000001F][(RGB >> 11) & 0x0000001F][(RGB >> 3) & 0x0000001F];
    }

    /**
     * This extracts the HUE of an intRGB color and returns a color with the
     * same HUE but full saturation and value
     */
    public static int Hue(int RGB) {
        return (int) (Color.RGBtoHSB((RGB >> 16) & 0x000000FF, (RGB >> 8) & 0x000000FF, (RGB) & 0x000000FF, new float[]{0, 0, 0})[0] * max);
    }

    /**
     * This extracts the HUE of an intRGB color and returns a color with the
     * same HUE but full saturation and value
     */
    public static int Hue_color(int RGB) {
        int HSV = RGBtoHSV(RGB);
        return HSVtoRGB_lookup[(HSV >> 19) & 0x0000001F][max];
    }

    // OPERATIONS

    /**
     * fade the color by the given value (out of 256)
     */
    public static int fade(int color, int fader) {
        return T & (fader * (color & T) >> 8) | G & (fader * (color & G) >> 8);
    }

    /**
     * simple average of two colors
     */
    public static int average(int c1, int c2) {
        return T & (((c1 & T) + (c2 & T) + 0x00010001) >> 1) | G & (((c1 & G) + (c2 & G) + 0x00000100) >> 1);
    }

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

    public static int RGB_contrast(int color, int degree) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int y = degree << 1;
        int x = 2 * (128 + y);
        int r1 = (r * x - (g + b) * y);
        int g1 = (g * x - (r + b) * y);
        int b1 = (b * x - (g + r) * y);
        if (r1 < 0) {
            r1 = 0;
        }
        if (g1 < 0) {
            g1 = 0;
        }
        if (b1 < 0) {
            b1 = 0;
        }
        r1 >>= 8;
        g1 >>= 8;
        b1 >>= 8;
        if (r1 > 0xff) {
            r1 = 0xff;
        }
        if (g1 > 0xff) {
            g1 = 0xff;
        }
        if (b1 > 0xff) {
            b1 = 0xff;
        }
        return (r1 << 16) | (g1 << 8) | b1;
    }

    public static int saturate(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int min = r < g ? b < r ? 0 : 16 : b < g ? 0 : 8;
        int maxx = r > g ? b > r ? 0 : 16 : b > g ? 0 : 8;
        if (min == maxx) {
            return color;
        }
        int mid = 24 - min - maxx;
        return (0xff << maxx) | ((((color >> mid) & 0xff) * 0xff / ((color >> maxx) & 0xff)) << mid);
    }

    public static int dark_saturate(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int min = r < g ? b < r ? 0 : 16 : b < g ? 0 : 8;
        int maxx = r > g ? b > r ? 0 : 16 : b > g ? 0 : 8;
        if (min == maxx) {
            return color;
        }
        int mid = 24 - min - maxx;
        int minc = ((color >> min) & 0xff);
        int midc = ((color >> mid) & 0xff);
        int maxc = ((color >> maxx) & 0xff);
        int dark = minc * maxc / (maxc + midc);
        maxc += dark;
        midc += minc - dark;
        return ((maxc > 0xff ? 0xff : maxc) << maxx) | ((midc > 0xff ? 0xff : midc) << mid);
    }

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

    public static int mush(int color) {
        int mush = (((0xff & (color >> 16)) + (0xff & (color >> 8)) + (0xff & (color))) * 85) & 0xff00;
        return (mush << 8) | (mush) | (mush >> 8);
    }

    public static int colormax(int c1, int c2) {
        return R & ((Math.max((c1 & R), (c2 & R)))) | G & ((Math.max((c1 & G), (c2 & G)))) | B & ((Math.max((c1 & B), (c2 & B))));
    }

    /**
     * average two colors based on two given weights
     */
    public static int average(int c1, int w1, int c2, int w2) {
        return T & (((c1 & T) * w1 + (c2 & T) * w2 + A_T) >> 8) | G & (((c1 & G) * w1 + (c2 & G) * w2 + A_G) >> 8);
    }

    /** CONVOLUTIONS
     *
     */
    /** 3x3 convolution weights
     *
     */
    private static int w11 = 20, w12 = 32, w13 = 20, w21 = 32, w22 = 48, w23 = 32, w31 = 20, w32 = 32, w33 = 20;
    /** cross convolution weights
     *
     */
    private static int r12 = 47, r21 = 47, r22 = 68, r23 = 47, r32 = 47;
    /** 3x1 convolution weights
     *
     */
    private static int w1 = 64, w2 = 128, w3 = 64;

    // CONVOLUTION FUNCTIONS

    /**
     * performs a convolution kernel on 3x3 neighborhood, indexed starting at
     * the upper left by rows then columns
     */
    public static int convolve_3x3(int c11, int c12, int c13, int c21, int c22, int c23, int c31, int c32, int c33) {
        return T
                & ((w11 * (c11 & T) + w12 * (c12 & T) + w13 * (c13 & T) + w21 * (c21 & T) + w22 * (c22 & T) + w23 * (c23 & T) + w31 * (c31 & T) + w32
                * (c32 & T) + w33 * (c33 & T) + A_T) >> 8)
                | G
                & ((w11 * (c11 & G) + w12 * (c12 & G) + w13 * (c13 & G) + w21 * (c21 & G) + w22 * (c22 & G) + w23 * (c23 & G) + w31 * (c31 & G) + w32
                * (c32 & G) + w33 * (c33 & G) + A_G) >> 8);
    }

    /**
     * performs a convolution kernel on the cross neighborhood, indexed starting
     * at the upper left by rows then columns
     */
    public static int convolve_cross(int c12, int c21, int c22, int c23, int c32) {
        return T & ((r12 * (c12 & T) + r21 * (c21 & T) + r22 * (c22 & T) + r23 * (c23 & T) + r32 * (c32 & T) + A_T) >> 8) | G
                & ((r12 * (c12 & G) + r21 * (c21 & G) + r22 * (c22 & G) + r23 * (c23 & G) + r32 * (c32 & G) + A_G) >> 8);
    }

    /**
     * performs a convolution kernel on the cross neighborhood, indexed starting
     * at the upper left by rows then columns
     */
    public static int convolve_3x1(int c1, int c2, int c3) {
        return T & ((w1 * (c1 & T) + w2 * (c2 & T) + w3 * (c3 & T) + A_T) >> 8) | G & ((w1 * (c1 & G) + w2 * (c2 & G) + w3 * (c3 & G) + A_G) >> 8);
    }

    static float[] temp = new float[]{0, 0, 0};

    public static int rotate_hue(int RGB, float d) {
        Color.RGBtoHSB((RGB >> 16) & 0xFF, (RGB >> 8) & 0xFF, (RGB) & 0xFF, temp);
        return Color.HSBtoRGB(temp[0] + d, 1f, 1f);
    }

    /**
     * Fancy graphics setting applies to all drawn objects such as circle around the cursor and the 3D tree. This is
     * a bottleneck for speed.
     */
    public static Graphics2D fancy(Graphics2D image_graphics) {

        if (Perceptron.fancy_graphics == 0)   {

            image_graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            image_graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            image_graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            image_graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            image_graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            image_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        }   else if   (Perceptron.fancy_graphics == 1) {

            image_graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smoothing of circle
            image_graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            image_graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            image_graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // 2nd degree interpolation
            image_graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            image_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        }   else if   (Perceptron.fancy_graphics == 2) {

            image_graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);  //
            image_graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);  //
            image_graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            image_graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);  //
            image_graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            image_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   //

        }   else if   (Perceptron.fancy_graphics == 3) {

            image_graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);       //
            image_graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);     //
            image_graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            image_graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);  //
            image_graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            image_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);    //
            image_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   //

        }   else if   (Perceptron.fancy_graphics == 4) {

            image_graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);   //
            image_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);       //
            image_graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);    //
            image_graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);        //
            image_graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);  //
            image_graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);   //
            image_graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);    //
            image_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);    //
            image_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   //

        }

        return image_graphics;

    }

}
