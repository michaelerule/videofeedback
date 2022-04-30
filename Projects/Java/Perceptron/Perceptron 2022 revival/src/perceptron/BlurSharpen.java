package perceptron;

import java.awt.image.DataBuffer;
import static perceptron.Misc.clip;
import static util.ColorUtil.blend;
import static util.ColorUtil.blend;

/**
 * Notes for calculating unsharp mask:
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
 */
public class BlurSharpen {

    private final DoubleBuffer buffer;
    private final int[] temp;
    
    public BlurSharpen(DoubleBuffer b) {
        buffer = b;
        temp   = new int[b.buf.W * b.buf.H];
    }
    
    // Constants for performing 3-point blur convolution
    // int yt = Y & ((yp*W1+yc*W2+yn*W3+YROUNDING) >> BLURSHIFT);
    // int gt = G & ((gp*W1+gc*W2+gn*W3+GROUNDING) >> BLURSHIFT);
    private static final int 
            M = 0xff00ff, 
            G = 0x00ff00,
            Q1 = 5,
            Q2 = 6,
            Q3 = 5,
            S = 4,
            MASK12 = 0xf0f0f0,
            R  = 0x0101010*8,
            YR = 0x010001*8,
            GR = 0x000100*8,
            UNSHARPSTRENGTH = 4,
            UNSHARPBASELINE = 32;
    
    private void horizontalBlur12() {
        DataBuffer data = buffer.out.buf;
        int W = buffer.out.W;
        int H = buffer.out.H;
        int W2 = W-2;
        // Blur rows, saving in the temp buffer
        for (int y=0; y<H; y++) {
            // We access the source buffer in row-major order but save the
            // output in transposed column-major format. This makes it
            int N0 = y*W;
            // Start by grabbing the first three piexls in this row
            int p = data.getElem(N0++) & MASK12;
            int c = data.getElem(N0++) & MASK12;
            int n = data.getElem(N0++) & MASK12;
            // Don't blur the first pixel
            temp[0*H+y] = p;
            // Iterate over columns within each row
            int index = y+H;
            for (int i=1; i<W2; i++) {
                // Obtain the next pixel and shift forward
                temp[index] = p*Q1 + c*Q2 + n*Q3 + R >> S;
                p = c; 
                c = n;
                n = data.getElem(N0++) & MASK12;
                index += H;
            }
            // Handle the penultimate pixel
            temp[index] = p*Q1 + c*Q2 + n*Q3 + R >> S;
            index += H;
            // Don't blur the final pixel
            temp[index] = n;
        }
    }

    private void blur12(int A) {
        DataBuffer b = buffer.out.buf;
        int W = buffer.out.W;
        int H = buffer.out.H;
        horizontalBlur12();
        int B = 256 - A;
        // Blur columns, saving in the output buffer
        int H2 = H-2;
        for (int x=0; x<W; x++) {
            int N0 = x*H;
            // Start by grabbing the first three piexls in this row
            int p = temp[N0++] & MASK12;
            int c = temp[N0++] & MASK12;
            int n = temp[N0++] & MASK12;
            // Don't blur the first pixel
            b.setElem(x, blend(p, b.getElem(x), A));
            // Iterate over rows within each column
            int k = x + W;
            for (int y=1; y<H2; y++) {
                // Obtain the next pixel and shift forward
                b.setElem(k, blend(p*Q1+c*Q2+n*Q3+R>>S, b.getElem(k), A));
                p = c; 
                c = n;
                n = temp[N0++] & MASK12;
                k+= W;
            }
            b.setElem(k, blend(p*Q1+c*Q2+n*Q3+R>>S, b.getElem(k), A));
            k += W;
            // Don't blur the final pixel
            b.setElem(k, blend(n, b.getElem(k), A));
        }
    }
    
    private void sharp12(int A) {
        DataBuffer d = buffer.out.buf;
        int W  = buffer.out.W;
        int H  = buffer.out.H;
        horizontalBlur12();
        // Blur columns, saving in the output buffer
        int H2 = H-2;
        for (int x=0; x<W; x++) {
            int N0 = x*H;
            // Start by grabbing the first three piexls in this row
            int p = temp[N0++] & MASK12;
            int c = temp[N0++] & MASK12;
            int n = temp[N0++] & MASK12;
            // Don't blur the first pixel
            d.setElem(x, unsharp(d.getElem(x), p, A));
            // Iterate over rows within each column
            int k = x + W;
            for (int i=1; i<H2; i++) {
                // Compute a 1 2 1 weighted convolution
                d.setElem(k, unsharp(d.getElem(k), p*Q1+c*Q2+n*Q3+R>>S, A));
                // Obtain the next pixel and shift forward
                p = c; 
                c = n;
                n = temp[N0++] & MASK12;
                k+= W;
            }
            // Handle the penultimate pixel
            d.setElem(k, unsharp(d.getElem(k), p*Q1+c*Q2+n*Q3+R>>S, A));
            k += W;
            // Don't blur the final pixel
            d.setElem(k, unsharp(d.getElem(k), n, A));
        }
    }
    
    private void horizontalBlur24() {
        DataBuffer data = buffer.out.buf;
        int W = buffer.out.W;
        int H = buffer.out.H;
        int W2 = W-2;
        // Blur rows, saving in the temp buffer
        for (int y=0; y<H; y++) {
            // We access the source buffer in row-major order but save the
            // output in transposed column-major format. This makes it
            int N0 = y*W;
            // Start by grabbing the first three piexls in this row
            int p = data.getElem(N0++) ;
            int c = data.getElem(N0++);
            int n = data.getElem(N0++);
            // Extract their color componnents
            int yp = M&p, gp = G&p;
            int yc = M&c, gc = G&c;
            int yn = M&n, gn = G&n;
            // Don't blur the first pixel
            temp[0*H+y] = p;
            // Iterate over columns within each row
            int index = y+H;
            for (int i=1; i<W2; i++) {
                // Compute a 1 2 1 weighted convolution
                //int yt = M & ((yp*Q1+yc*Q2+yn*Q3+YR) >> S);
                //int gt = G & ((gp*Q1+gc*Q2+gn*Q3+GR) >> S);
                int yt = M & (((yp-yn)*Q1+(yc-yn)*Q2+(yn<<S)+YR) >> S);
                int gt = G & (((gp-gn)*Q1+(gc-gn)*Q2+(gn<<S)+GR) >> S);
                temp[index] = yt | gt;
                // Obtain the next pixel and shift forward
                yp = yc; yc = yn; gp = gc; gc = gn;
                n = data.getElem(N0++);
                yn = M & n; gn = G & n;
                index += H;
            }
            // Handle the penultimate pixel
            //int yt = M & ((yp*Q1+yc*Q2+yn*Q3+YR) >> S);
            //int gt = G & ((gp*Q1+gc*Q2+gn*Q3+GR) >> S);
            int yt = M & (((yp-yn)*Q1+(yc-yn)*Q2+(yn<<S)+YR) >> S);
            int gt = G & (((gp-gn)*Q1+(gc-gn)*Q2+(gn<<S)+GR) >> S);
            temp[index] = yt | gt;
            index += H;
            // Don't blur the final pixel
            temp[index] = n;
        }
    }
    
    private void blur24(int A) {
        DataBuffer b = buffer.out.buf;
        int W = buffer.out.W;
        int H = buffer.out.H;
        horizontalBlur24();
        int B = 256 - A;
        // Blur columns, saving in the output buffer
        int H2 = H-2;
        for (int x=0; x<W; x++) {
            int N0 = x*H;
            // Start by grabbing the first three piexls in this row
            int p = temp[N0++];
            int c = temp[N0++];
            int n = temp[N0++];
            // Extract their color componnents
            int yp = M&p, gp = G&p;
            int yc = M&c, gc = G&c;
            int yn = M&n, gn = G&n;
            // Don't blur the first pixel
            b.setElem(x, blend(p, b.getElem(x), A));
            // Iterate over rows within each column
            int k = x + W;
            for (int y=1; y<H2; y++) {
                // Compute a 1 2 1 weighted convolution
                //int yt = M & ((yp*Q1+yc*Q2+yn*Q3+YR) >> S);
                //int gt = G & ((gp*Q1+gc*Q2+gn*Q3+GR) >> S);
                int yt = M & (((yp-yn)*Q1+(yc-yn)*Q2+(yn<<S)+YR) >> S);
                int gt = G & (((gp-gn)*Q1+(gc-gn)*Q2+(gn<<S)+GR) >> S);
                b.setElem(k, blend(yt|gt, b.getElem(k), A));
                // Obtain the next pixel and shift forward
                yp = yc; yc = yn; gp = gc; gc = gn;
                n = temp[N0++];
                yn = M & n; gn = G & n;
                k += W;
            }
            // Handle the penultimate pixel
            //int yt = M & ((yp*Q1+yc*Q2+yn*Q3+YR) >> S);
            //int gt = G & ((gp*Q1+gc*Q2+gn*Q3+GR) >> S);
            int yt = M & (((yp-yn)*Q1+(yc-yn)*Q2+(yn<<S)+YR) >> S);
            int gt = G & (((gp-gn)*Q1+(gc-gn)*Q2+(gn<<S)+GR) >> S);
            b.setElem(k, blend(yt|gt, b.getElem(k), A));
            k += W;
            // Don't blur the final pixel
            b.setElem(k, blend(n, b.getElem(k), A));

        }
    }
    
    private void sharp24(int A) {
        DataBuffer d = buffer.out.buf;
        int W  = buffer.out.W;
        int H  = buffer.out.H;
        horizontalBlur24();
        // Blur columns, saving in the output buffer
        int H2 = H-2;
        for (int x=0; x<W; x++) {
            int N0 = x*H;
            // Start by grabbing the first three piexls in this row
            int p = temp[N0++];
            int c = temp[N0++];
            int n = temp[N0++];
            int yp = M&p, gp = G&p;
            int yc = M&c, gc = G&c;
            int yn = M&n, gn = G&n;
            // Don't blur the first pixel
            int color = d.getElem(x);
            d.setElem(x, unsharp(color, p, A));
            // Iterate over rows within each column
            int k = x + W;
            for (int i=1; i<H2; i++) {
                // Compute a 1 2 1 weighted convolution
                //int yt = Y & ((yp*Q1+yc*Q2+yn*Q3+YR) >> S);
                //int gt = K & ((gp*Q1+gc*Q2+gn*Q3+GR) >> S);
                int yt = M & (((yp-yn)*Q1+(yc-yn)*Q2+(yn<<S)+YR) >> S);
                int gt = G & (((gp-gn)*Q1+(gc-gn)*Q2+(gn<<S)+GR) >> S);
                d.setElem(k, unsharp(d.getElem(k), yt|gt, A));
                // Obtain the next pixel and shift forward
                yp = yc; yc = yn; gp = gc; gc = gn;
                n = temp[N0++];
                yn = M & n; gn = G & n;
                k+=W;
            }
            // Handle the penultimate pixel
            int yt = M & ((yp*Q1+yc*Q2+yn*Q3+YR) >> S);
            int gt = G & ((gp*Q1+gc*Q2+gn*Q3+GR) >> S);
            d.setElem(k, unsharp(d.getElem(k), yt|gt, A));
            k += W;
            // Don't blur the final pixel
            d.setElem(k, unsharp(d.getElem(k), n, A));
        }
    }
    
    private int unsharp(int color, int blur, int A) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & (color);
        int r_ = 0xff & (blur >> 16);
        int g_ = 0xff & (blur >> 8);
        int b_ = 0xff & (blur);
        // Make alpha range from 0..64 instead of 0..256
        int B = 256 - A;
        // This will naturally fade toward a value of UNSHARPBASELINE
        r = clip( ( B * r + A * (UNSHARPSTRENGTH*(r - r_) + UNSHARPBASELINE) ) >> 8, 0, 255);
        g = clip( ( B * g + A * (UNSHARPSTRENGTH*(g - g_) + UNSHARPBASELINE) ) >> 8, 0, 255);
        b = clip( ( B * b + A * (UNSHARPSTRENGTH*(b - b_) + UNSHARPBASELINE) ) >> 8, 0, 255);
        return (r << 16) | (g << 8) | b;
    } 
        
    public void operate(int amount) {
        if      (amount>0) blur24 (clip( amount,0,256));
        else if (amount<0) sharp24(clip(-amount,0,256));
    }
}
