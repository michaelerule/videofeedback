package perceptron;

import java.awt.image.DataBuffer;
import static java.lang.Math.*;
import java.util.function.IntBinaryOperator;
import static perceptron.Misc.clip;
import util.ColorUtil;

/**
 *
 * @author mer49
 */
public class BlurSharpen {

    private final DoubleBuffer buffer;
    private final int[] temp;
    
    /**
     *
     * @param b
     */
    public BlurSharpen(DoubleBuffer b) {
        buffer = b;
        temp   = new int[b.buf.W * b.buf.H];
    }
    
    // Constants for performing 3-point blur convolution
    // int yt = Y & ((yp*W1+yc*W2+yn*W3+YROUNDING) >> BLURSHIFT);
    // int gt = G & ((gp*W1+gc*W2+gn*W3+GROUNDING) >> BLURSHIFT);
    private static final int 
            Y = 0xff00ff, 
            K = 0x00ff00,
            Q1 = 19,
            Q2 = 26,
            Q3 = 19,
            BLURSHIFT = 6,
            YROUNDING = 0x100010,
            GROUNDING = 0x001000,
            UNSHARPSTRENGTH = 4,
            UNSHARPBASELINE = 32;
    
    private void horizontalBlur() {
        DataBuffer data = buffer.out.buf;
        int W = buffer.out.W;
        int H = buffer.out.H;
        int W1 = W-1;
        int W2 = W-2;
        // Blur rows, saving in the temp buffer
        for (int y=0; y<H; y++) {
            // We access the source buffer in row-major order but save the
            // output in transposed column-major format. This makes it
            int N0 = y*W;
            // Start by grabbing the first three piexls in this row
            int cprev = data.getElem(N0+0);
            int ccurr = data.getElem(N0+1);
            int cnext = data.getElem(N0+2);
            // Extract their color componnents
            int yp = Y & cprev, gp = K & cprev;
            int yc = Y & ccurr, gc = K & ccurr;
            int yn = Y & cnext, gn = K & cnext;
            // Don't blur the first pixel
            temp[0*H+y] = cprev;
            // Iterate over columns within each row
            for (int i=1; i<W2; i++) {
                // Compute a 1 2 1 weighted convolution
                int yt = Y & ((yp*Q1+yc*Q2+yn*Q3+YROUNDING) >> BLURSHIFT);
                int gt = K & ((gp*Q1+gc*Q2+gn*Q3+GROUNDING) >> BLURSHIFT);
                temp[i*H+y] = yt | gt;
                // Obtain the next pixel and shift forward
                yp = yc; yc = yn; gp = gc; gc = gn;
                cnext = data.getElem(N0+i+2);
                yn = Y & cnext; gn = K & cnext;
            }
            // Handle the penultimate pixel
            int yt = Y & ((yp*Q1+yc*Q2+yn*Q3+YROUNDING) >> BLURSHIFT);
            int gt = K & ((gp*Q1+gc*Q2+gn*Q3+GROUNDING) >> BLURSHIFT);
            temp[W2*H+y] = yt | gt;
            // Don't blur the final pixel
            temp[W1*H+y] = cnext;
        }
    }


    /**
     * Handle blurring only.
     * @param amount : value in range 0..256
     */
    private void blur(int amount) {
        DataBuffer data = buffer.out.buf;
        int W = buffer.out.W;
        int H = buffer.out.H;
        int H1 = H-1;
        horizontalBlur();
        int notamount = 256 - amount;
        // Blur columns, saving in the output buffer
        int H2 = H-2;
        for (int x=0; x<W; x++) {
            int N0 = x*H;
            // Start by grabbing the first three piexls in this row
            int cprev = temp[N0+0];
            int ccurr = temp[N0+1];
            int cnext = temp[N0+2];
            // Extract their color componnents
            int yp = Y & cprev, gp = K & cprev;
            int yc = Y & ccurr, gc = K & ccurr;
            int yn = Y & cnext, gn = K & cnext;
            // Don't blur the first pixel
            data.setElem(x, 
                ColorUtil.average(cprev, amount, 
                data.getElem(x), notamount));
            // Iterate over rows within each column
            for (int i=1; i<H2; i++) {
                // Compute a 1 2 1 weighted convolution
                int yt = Y & ((yp*Q1+yc*Q2+yn*Q3+YROUNDING) >> BLURSHIFT);
                int gt = K & ((gp*Q1+gc*Q2+gn*Q3+GROUNDING) >> BLURSHIFT);
                data.setElem(x + i*W, 
                    ColorUtil.average(yt|gt, amount, 
                    data.getElem(x + i*W), 
                    notamount));
                // Obtain the next pixel and shift forward
                yp = yc; yc = yn;
                gp = gc; gc = gn;
                cnext = temp[N0+i+2];
                yn = Y & cnext;
                gn = K & cnext;
            }
            // Handle the penultimate pixel
            int yt = Y & ((yp*Q1+yc*Q2+yn*Q3+YROUNDING) >> BLURSHIFT);
            int gt = K & ((gp*Q1+gc*Q2+gn*Q3+GROUNDING) >> BLURSHIFT);
            data.setElem(x + H2*W, 
                ColorUtil.average(yt | gt, amount, 
                data.getElem(x + H2*W), notamount));
            // Don't blur the final pixel
            data.setElem(x + H1*W, 
                ColorUtil.average(cnext, amount, 
                data.getElem(x + H1*W), notamount));

        }
    }

    /**
     * Handle unsharp mask only.
     * 
     * How to calculate this? 
     *   
     * To "Sharpen" we compute an unsharp mask by subtracting out the blurred component.
     * amount = 255 : return the blurred result;
     * amount = 128 : return original
     * amount = 0   : return original minus the blur
     *   
     * The subtraction is a bit funny. If I were working with [0,1] color
     * data then this is what I'd do:
     *     (color - 0.5) - g * blur( color - 0.5 ) + 0.5
     *   
     * The subtraction of 0.5 factors and cancels out: 
     *     color - g * blur( color ) - 0.5 * g 
     *   
     * What if instead we blended it with the INVERTED blur? Fast.
     * This looks terrible. Ok. Let's try this: first compute the unsharp
     * then fade it with the original to adjust strength? 
     * 
     * This is g=1 so the full unsharp would be:
     *     color - blur( color ) - 0.5 
     * 
     * No, I don't like that. color - blur( color ) can take on values ranging
     * from -1 to 1, maybe we can rescale this to [0,1]
     *     (color-blur(color)+1)/2
     * 
     * ok, with [0,255] colors we can get -255 .. 255; if we add 255 then 
     * we get values in the range 0..510; we can add 1 and divide by 2;
     * 
     * Then fade with the original. This will never go negative so perhaps
     * we can calculate it without component-wise subtraction
     *     (1-a) * c + a * ((c-blur(c))+1)/2
     *
     * OK, Let's do this in two steps. An unsharp and a fade. 
     *     ([0..255] - [0..255] + 256) >> 2 
     *
     * This works but does rapidly fade us to gray. Is there no clean way to 
     * increase the contrast? This is the contrast code, which does act 
     * componentwise.
     * 
     *      public static int contrast(int color) {
     *          int r = 0xff & (color >> 16);
     *          int g = 0xff & (color >> 8);
     *          int b = 0xff & (color);
     *          r = ((r << 1) - 128);
     *          g = ((g << 1) - 128);
     *          b = ((b << 1) - 128);
     *          r = r < 0 ? 0 : r > 0xff ? 0xff : r;
     *          g = g < 0 ? 0 : g > 0xff ? 0xff : g;
     *          b = b < 0 ? 0 : b > 0xff ? 0xff : b;
     *          return (r << 16) | (g << 8) | b;
     *      }
     * 
     * @param amount : value in range 0..256
     */
    
    private int unsharp(int color, int blur, int alpha) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int R = 0xff & (blur >> 16);
        int G = 0xff & (blur >> 8);
        int B = 0xff & (blur);
        // Make alpha range from 0..64 instead of 0..256
        int beta = 256 - alpha;
        // This will naturally fade toward a value of UNSHARPBASELINE
        r = clip( ( beta * r + alpha * (UNSHARPSTRENGTH*(r - R) + UNSHARPBASELINE) ) >> 8, 0, 255);
        g = clip( ( beta * g + alpha * (UNSHARPSTRENGTH*(g - G) + UNSHARPBASELINE) ) >> 8, 0, 255);
        b = clip( ( beta * b + alpha * (UNSHARPSTRENGTH*(b - B) + UNSHARPBASELINE) ) >> 8, 0, 255);
        return (r << 16) | (g << 8) | b;
    } 
    
    private void sharpen(int amount) {
        DataBuffer d = buffer.out.buf;
        int W  = buffer.out.W;
        int H  = buffer.out.H;
        int H1 = H-1;
        int j;
        horizontalBlur();
        // Blur columns, saving in the output buffer
        int H2 = H-2;
        for (int x=0; x<W; x++) {
            int N0 = x*H;
            // Start by grabbing the first three piexls in this row
            int cprev = temp[N0+0];
            int ccurr = temp[N0+1];
            int cnext = temp[N0+2];
            int yp = Y & cprev, gp = K & cprev;
            int yc = Y & ccurr, gc = K & ccurr;
            int yn = Y & cnext, gn = K & cnext;
            // Don't blur the first pixel
            // destbuffer.setElem(x, cprev);
            int color = d.getElem(x);
            d.setElem(x, unsharp(color, cprev, amount));
            // Iterate over rows within each column
            for (int i=1; i<H2; i++) {
                // Compute a 1 2 1 weighted convolution
                int yt = Y & ((yp*Q1+yc*Q2+yn*Q3+YROUNDING) >> BLURSHIFT);
                int gt = K & ((gp*Q1+gc*Q2+gn*Q3+GROUNDING) >> BLURSHIFT);
                j = x + i*W;
                d.setElem(j, unsharp(d.getElem(j), yt|gt, amount));
                // Obtain the next pixel and shift forward
                yp = yc; yc = yn; gp = gc; gc = gn;
                cnext = temp[N0+i+2];
                yn = Y & cnext; gn = K & cnext;
            }
            // Handle the penultimate pixel
            int yt = Y & ((yp*Q1+yc*Q2+yn*Q3+YROUNDING) >> BLURSHIFT);
            int gt = K & ((gp*Q1+gc*Q2+gn*Q3+GROUNDING) >> BLURSHIFT);
            j = x + H2*W;
            d.setElem(j, unsharp(d.getElem(j), yt|gt, amount));
            // Don't blur the final pixel
            j = x + H1*W;
            d.setElem(j, unsharp(d.getElem(j), cnext, amount));
        }
    }
    
    /**
     * Apply the blur/sharpen operation.
     * @param amount
     */
    public void operate(int amount) {
        if      (amount>0) blur   (clip( amount,0,256));
        else if (amount<0) sharpen(clip(-amount,0,256));
    }
}
