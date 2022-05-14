package perceptron;

/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 * @author Predrag Bokšić (junkerade@gmail.com)
 *         <p/>
 *          Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *          It evolves colored geometric patterns and visual images into the realm of infinite details
 *          and deepens the thought. </p>
 *
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 */


import perceptron.DoubleBuffer.ImageRenderContext;
import util.ColorUtility;

import java.awt.image.DataBuffer;

import static java.lang.StrictMath.*;


/**
 * Convolution for image softening using kernel (matrix) image processing.
 * https://en.wikipedia.org/wiki/Kernel_%28image_processing%29
 */
public final class Convolution {

    final DoubleBuffer buffer; // visual data storage
    int[] gaussian; // stores the Gaussian "distribution"
    int s, h, e, W, H, Hp, i, Y, G, c, w, c1, c2, r, g, b, y2, notamount;
    int[] pixel_color_storage; // one dimensional array of screen pixels


    /**
     * Constructs new Convolution using the convolution degree and DoubleBuffer as input parameters.
     */
    public Convolution(int standard_deviation, DoubleBuffer b) {
        s = 2 * standard_deviation;
        h = s / 2;
        e = 256 / h;
        gaussian = new int[s]; // just a few elements will be in the Gaussian distribution
        buffer = b;
        for (int i = 0; i < s; i++) {
            gaussian[i] = (int) (256d * gaussian(i - standard_deviation, standard_deviation));
            //System.err.println("GAUSSIAN " + gaussian[i]);
        }
        // pixel colors are stored as integers in a one-dimensional array; they are multiplied by 256 (256 <=> 1.0) for interpolation purpose
        // the color is encoded in the form of RGB values that are within a single number contained at triple-digit RGB positions, e.g. 0xff00ff
        // buffered image type that we are working with is not transparent and ends color number with 0, e.g. 0xff00ff00
        pixel_color_storage = new int[b.buffer.W * b.buffer.H]; // size of array is screen Width * Height
    }


    /**
     * Calculate the Gaussian for blurring.
     */
    public static double gaussian(final double x, final double sigma) {
        return exp(-.5 * pow(x / sigma, 2)) / (sigma * sqrt(2 * StrictMath.PI)); // actual equation
    }


    /**
     * Optimized power function.
     */
    public static double power(final double a, final double b) {
        final long tmp = (Double.doubleToLongBits(a) >> 32);
        final long tmp2 = (long) (b * (tmp - 1072632447) + 1072632447);
        return Double.longBitsToDouble(tmp2 << 32);
    }


    /**
     * Optimized exponent function with little benefit.
     */
    public static double exponent(final double val) {
        final long tmp = (long) (1512775 * val + (1072693248 - 60801));
        return Double.longBitsToDouble(tmp << 32);
    }


    /**
     * Process the loaded buffer (image). Convolution is required for B option to work, to enable or disable persistent
     * initial set. Parameter amount goes from 0 to 255 and relates to filter weight or the amount of effect applied.
     */
    public void operate(final int amount) {

        ImageRenderContext source = buffer.output;
        DataBuffer sourcebuffer = buffer.output.data_buffer;
        DataBuffer destbuffer = buffer.buffer.data_buffer;


        ////// option 0 convolution pass-through. we need persistent initial set to be inserted into the flow,
        // which is determined in Perceptron.go(); cursors then behave as persistent initial set (press B to disable).
        if (buffer.convolution == 0) {

            W = buffer.output.W;
            H = buffer.output.H;

            for (int iterator = 0; iterator < W * H; iterator++) {
                c2 = sourcebuffer.getElem(iterator) << 1;
                r = ((c2 >> 16) & 0x1fe);
                g = ((c2 >> 8) & 0x1fe);
                b = (c2 & 0x1fe);
                r = r < 0 ? 0 : r > 0xff ? 0xff : r;
                g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                c2 = (r << 16) | (g << 8) | b;
                destbuffer.setElem(iterator, c2);
            }


            /// option 1 ///////////////////////////////////
        } else if (buffer.convolution == 1) {

            W = buffer.output.W;
            H = buffer.output.H;

            i = 0; // read in the screen image
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    c = source.get_color_for_convolution.getColor(x << 8, y << 8);
                    //c = source.get_color_simple.getColor(x, y);
                    pixel_color_storage[i++] = c;
                }
            }

            notamount = 256 - amount;
            for (int iterator = 0; iterator < W * H; iterator++) {
                c2 = sourcebuffer.getElem(iterator) << 1;
                r = ((c2 >> 16) & 0x1fe);
                g = ((c2 >> 8) & 0x1fe);
                b = (c2 & 0x1fe);
                r = r < 0 ? 0 : r > 0xff ? 0xff : r;
                g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                c2 = (r << 16) | (g << 8) | b;
                c2 = ColorUtility.average(pixel_color_storage[iterator], amount, c2, notamount);
                destbuffer.setElem(iterator, c2);
            }

            /// option 2 ///////////////////////////////////
        } else if (buffer.convolution == 2) {

            W = buffer.output.W; // screen width, height
            H = buffer.output.H; // obtained from the buffer
            Hp = H - h;
            // Do X blur
            i = 0;
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        c = source.get_color_for_convolution.getColor(x + k - h << 8, y << 8); // << 8 means * 256. compensate for particular getColor.
                        w = gaussian[k];  // gaussian affects the pixel color
                        // convolution matrix (kernel) summation
                        Y += w * (c & 0xff00ff); // make c = 0, 1, 2, 3... 255, 0, 1, 2... 255, 0, 1, 2...
                        G += w * (c & 0x00ff00); // make c = 0, 0,... (256 times), then c = 256, 256, 256... (256 times),
                        // then c = 512, 512... (256 times), then c = 768, 768, 768... (256 times), and so on...
                    }
                    pixel_color_storage[i++] = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8; // >> 8 means integer division by 256
                }
            }
            // Do Y blur
            i = 0;
            notamount = 256 - amount; // amount and notamount are both from 0 to 255
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        y2 = (y - h + k + H) % H;
                        c = pixel_color_storage[x + W * y2];
                        w = gaussian[k];
                        Y += w * (c & 0xff00ff);
                        G += w * (c & 0x00ff00);
                    }
                    c1 = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8;
                    c2 = sourcebuffer.getElem(i) << 1; // 0, 1, 2... << 1 = 0, 4, 8, 12, 16...
                    r = ((c2 >> 16) & 0x1fe) - ((c1 >> 16) & 0xff); // 0, 1, 2... >> 16 = 0, 0, 0... up to 1 at 65536, then 1, 1, 1...
                    // 0x1fe = 510; 0, 1, 2... & 01fe = 0, 0, 2, 2, 4, 4, 8, 8... up to 512th and then it repeats
                    g = ((c2 >> 8) & 0x1fe) - ((c1 >> 8) & 0xff);
                    b = (c2 & 0x1fe) - (c1 & 0xff);
                    r = r < 0 ? 0 : r > 0xff ? 0xff : r; // if r < 0 then r = 0; if r > 255 then r = 255 otherwise r = r
                    g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                    b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                    c2 = (r << 16) | (g << 8) | b; // 0, 1, 2... << 16 = 0, 65536, 131072, 196608... up to 2147418112 at 32767, then -2147483648, -2147418112, -2147352576...
                    c2 = ColorUtility.average(c1, amount, c2, notamount);
                    destbuffer.setElem(i++, c2);
                }
            }
            for (int y = h; y < Hp; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        c = pixel_color_storage[x + W * (y - h + k)];
                        w = gaussian[k];
                        Y += w * (c & 0xff00ff);
                        G += w * (c & 0x00ff00);
                    }
                    c1 = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8;
                    c2 = sourcebuffer.getElem(i) << 1;
                    r = ((c2 >> 16) & 0x1fe) - ((c1 >> 16) & 0xff);
                    g = ((c2 >> 8) & 0x1fe) - ((c1 >> 8) & 0xff);
                    b = (c2 & 0x1fe) - (c1 & 0xff);
                    r = r < 0 ? 0 : r > 0xff ? 0xff : r;
                    g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                    b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                    c2 = (r << 16) | (g << 8) | b;
                    c2 = ColorUtility.average(c1, amount, c2, notamount);
                    destbuffer.setElem(i++, c2);
                }
            }
            for (int y = Hp; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        y2 = y - h + k;
                        if (y2 < 0) {
                            y2 = 0;
                        } else if (y2 >= H) {
                            y2 = H - 1;
                        }
                        c = pixel_color_storage[x + W * y2];
                        w = gaussian[k];
                        Y += w * (c & 0xff00ff);
                        G += w * (c & 0x00ff00);
                    }
                    c1 = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8;
                    c2 = sourcebuffer.getElem(i) << 1;
                    r = ((c2 >> 16) & 0x1fe) - ((c1 >> 16) & 0xff);
                    g = ((c2 >> 8) & 0x1fe) - ((c1 >> 8) & 0xff);
                    b = (c2 & 0x1fe) - (c1 & 0xff);
                    r = r < 0 ? 0 : r > 0xff ? 0xff : r;
                    g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                    b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                    c2 = (r << 16) | (g << 8) | b;
                    c2 = ColorUtility.average(c1, amount, c2, notamount);
                    destbuffer.setElem(i++, c2);
                }
            }

            /// option 3 ///////////////////////////////////
        } else if (buffer.convolution == 3) {

            W = buffer.output.W; // screen width, height
            H = buffer.output.H; // obtained from the buffer
            Hp = H - h;
            // Do X blur
            i = 0;
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        c = source.get_color_for_convolution.getColor(x + k - h << 8, y << 8); // << 8 means * 256. compensate for particular getColor.
                        w = gaussian[k];  // gaussian affects the pixel color
                        // convolution matrix (kernel) summation
                        Y += w * (c & 0xff00ff); // make c = 0, 1, 2, 3... 255, 0, 1, 2... 255, 0, 1, 2...
                        G += w * (c & 0x00ff00); // make c = 0, 0,... (256 times), then c = 256, 256, 256... (256 times),
                        // then c = 512, 512... (256 times), then c = 768, 768, 768... (256 times), and so on...
                    }
                    pixel_color_storage[i++] = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8; // >> 8 means integer division by 256
                }
            }
            // Do Y blur
            i = 0;
            notamount = 256 - amount; // amount and notamount are both from 0 to 255
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        y2 = (y - h + k + H) % H;
                        c = pixel_color_storage[x + W * y2];
                        w = gaussian[k];
                        Y += w * (c & 0xff00ff);
                        G += w * (c & 0x00ff00);
                    }
                    c1 = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8;
                    c2 = sourcebuffer.getElem(i) << 1; // 0, 1, 2... << 1 = 0, 4, 8, 12, 16...
                    r = ((c2 >> 16) & 0x1fe) - ((c1 >> 16) & 0xff); // 0, 1, 2... >> 16 = 0, 0, 0... up to 1 at 65536, then 1, 1, 1...
                    g = ((c2 >> 8) & 0x1fe) - ((c1 >> 8) & 0xff);
                    b = (c2 & 0x1fe) - (c1 & 0xff);
                    r = r < 0 ? 0 : r > 0xff ? 0xff : r; // if r < 0 then r = 0; if r > 255 then r = 255 otherwise r = r
                    // 0x1fe = 510; 0, 1, 2... & 01fe = 0, 0, 2, 2, 4, 4, 8, 8... up to 512th and then it repeats
                    g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                    b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                    c2 = (r << 16) | (g << 8) | b; // 0, 1, 2... << 16 = 0, 65536, 131072, 196608... up to 2147418112 at 32767, then -2147483648, -2147418112, -2147352576...
                    c2 = ColorUtility.average(c1, amount, c2, notamount);
                    destbuffer.setElem(i++, c2);
                }
            }
            for (int y = h; y < Hp; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        c = pixel_color_storage[x + W * (y - h + k)];
                        w = gaussian[k];
                        Y += w * (c & 0xff00ff);
                        G += w * (c & 0x00ff00);
                    }
                    c1 = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8;
                    c2 = sourcebuffer.getElem(i) << 1;
                    r = ((c2 >> 16) & 0x1fe) - ((c1 >> 16) & 0xff);
                    g = ((c2 >> 8) & 0x1fe) - ((c1 >> 8) & 0xff);
                    b = (c2 & 0x1fe) - (c1 & 0xff);
                    r = r < 0 ? 0 : r > 0xff ? 0xff : r;
                    g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                    b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                    c2 = (r << 16) | (g << 8) | b;
                    c2 = ColorUtility.average(c1, amount, c2, notamount);
                    destbuffer.setElem(i++, c2);
                }
            }
            for (int y = Hp; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    Y = 0;
                    G = 0;
                    for (int k = 0; k < s; k++) {
                        y2 = y - h + k;
                        if (y2 < 0) {
                            y2 = 0;
                        } else if (y2 >= H) {
                            y2 = H - 1;
                        }
                        c = pixel_color_storage[x + W * y2];
                        w = gaussian[k];
                        Y += w * (c & 0xff00ff);
                        G += w * (c & 0x00ff00);
                    }
                    c1 = (0xff00ff00 & Y | 0x00ff0000 & G) >> 8;
                    c2 = sourcebuffer.getElem(i) << 1;
                    r = ((c2 >> 16) & 0x1fe) - ((c1 >> 16) & 0xff);
                    g = ((c2 >> 8) & 0x1fe) - ((c1 >> 8) & 0xff);
                    b = (c2 & 0x1fe) - (c1 & 0xff);
                    r = r < 0 ? 0 : r > 0xff ? 0xff : r;
                    g = g < 0 ? 0 : g > 0xff ? 0xff : g;
                    b = b < 0 ? 0 : b > 0xff ? 0xff : b;
                    c2 = (r << 16) | (g << 8) | b;
                    c2 = ColorUtility.average(c1, amount, c2, notamount);
                    destbuffer.setElem(i++, c2);
                }
            }


        }

    }

}



