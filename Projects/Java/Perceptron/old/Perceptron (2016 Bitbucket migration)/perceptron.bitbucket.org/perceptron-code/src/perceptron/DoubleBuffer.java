package perceptron;

import util.ColorUtility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

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


public final class DoubleBuffer {


    public Perceptron percept;
    public ImageRenderContext output, buffer, sketch, image, image_fade, various_displays; // ultimate drawing buffers
    public final int number_of_grabbers = 9; // number of linear transformations, "reflections"
    public int reflection = 0;  // default IFS transformation or "reflection" given in selected grabber (getColorDefault)
    public final int num_of_convolution_modes = 4; // number of implemented convolution modes
    public int convolution = 0; // disable, enable convolution, or prevent grabber used for convolution to get pixel color


    /**
     * Interface Grabber offers two functions: to get the color from the buffer at a given location and to move to the
     * next frame.
     */
    public interface Grabber {
        public int getColor(int x, int y);

        public void interface_nextFrame();
    }


    /**
     * Class ImageRenderContext inputs a buffered image and creates Graphics2D object and DataBuffer to work with.
     */
    public class ImageRenderContext {


        public BufferedImage image;// = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB); // non null image for safety
        public Graphics2D graphics2D;
        public DataBuffer data_buffer;
        final int W, H, W_ONE, H_ONE, HALF_W, HALF_H, TWO_W, TWO_H, HALF_W_ONE, HALF_H_ONE, TWO_W_ONE, TWO_H_ONE;
        final static int R = 0xFF0000, G = 0x00FF00, B = 0x0000FF, K = 0xFF00FF;


        public ImageRenderContext(BufferedImage b) {
            image = b;
            image.setAccelerationPriority(1.0f);
            graphics2D = ColorUtility.fancy(b.createGraphics());
            data_buffer = b.getRaster().getDataBuffer();
            W = b.getWidth();
            H = b.getHeight();
            W_ONE = W - 1;
            H_ONE = H - 1;
            TWO_W = W << 1;
            TWO_H = H << 1;
            HALF_W = W >> 1;
            HALF_H = H >> 1;
            TWO_W_ONE = W_ONE << 1;
            TWO_H_ONE = H_ONE << 1;
            HALF_W_ONE = W_ONE >> 1;
            HALF_H_ONE = H_ONE >> 1;
        }


        /**
         * The simplest grabber - experimental.
         */
        Grabber getColorSimple = new Grabber() {

            public int getColor(int x, int y) {
                x >>= 8; // divide by 256, because z_new was multiplied by 256 in compute_lookup();
                y >>= 8;
                return data_buffer.getElem(x + W * y);
            }

            @Override
            public void interface_nextFrame() {
            }
        };


        /**
         * The ugly grabber (former "get") without interpolation. It is always in use by Convolution.java.
         */
        Grabber getColorBasic = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                /**
                 * Only positive x and y at the screen can be read to obtain
                 * the color.
                 */
                // if (x < 0) return(0);
                // if (y < 0) return(0);
                /**
                 * However, we use another more interesting function,
                 * absolute(x, y).
                 */
                x ^= x >> 31; // absolute values only; no choice but to disregard negative z_new
                y ^= y >> 31;
                x >>= 8; // divide by 256, because z_new was multiplied by 256 in compute_lookup();
                y >>= 8;
                /**
                 * The reflection transformation to put the off-screen z_new
                 * points back within the screen. Although x = x % W and y =
                 * y % H would suffice, it is more interesting like this...
                 */
                x = (x / W & 1) == 0 ? x % W : W_ONE - x % W; // if x/W is even then x =... else x=...
                y = (y / H & 1) == 0 ? y % H : H_ONE - y % H; // has eyes

                // x = W_ONE - x % W; // no eyes
                // y = H_ONE - y % H;

                // x = x % W; // has eyes
                // y = y % H;

                /**
                 * Since the screen is a one-dimensional array of length
                 * W*H, the index of any element is i(x,y) = x + W * y.
                 */
                return data_buffer.getElem(x + W * y);
            }

            @Override
            public void interface_nextFrame() {
            }
        };

        /**
         * The default grabber that uses interpolation and a standard linear geometric (reflection) transformation.
         */
        Grabber getColorDefault = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                int x_1, x_2, y_1, y_2;

                x ^= x >> 31;
                y ^= y >> 31;
                x_1 = x >> 8;
                x_2 = x_1 + 1;
                y_1 = y >> 8;
                y_2 = y_1 + 1;

                x_1 = W_ONE - x_1 % W;
                x_2 = W_ONE - x_2 % W;
                y_1 = H_ONE - y_1 % H;
                y_2 = H_ONE - y_2 % H;

                y_1 *= W;
                y_2 *= W;

                int c11 = data_buffer.getElem(y_1 + x_1);
                int c21 = data_buffer.getElem(y_1 + x_2);
                int c12 = data_buffer.getElem(y_2 + x_1);
                int c22 = data_buffer.getElem(y_2 + x_2);

                int A_x = x & 0xFF;
                int A_y = y & 0xFF;
                int B_x = ~A_x & 0xFF;
                int B_y = ~A_y & 0xFF;
                int C_11 = B_x * B_y >> 8;
                int C_12 = A_x * B_y >> 8;
                int C_21 = B_x * A_y >> 8;
                int C_22 = A_x * A_y >> 8;

                return K & ((K & c11) * C_11 + (K & c21) * C_12 + (K & c12) * C_21 + (K & c22) * C_22 >> 8) | G
                        & ((G & c11) * C_11 + (G & c21) * C_12 + (G & c12) * C_21 + (G & c22) * C_22 >> 8);
            }

            @Override
            public void interface_nextFrame() {
            }
        };

        /**
         * The eyeballs grabber uses interpolation and a creates an effect of magnified cursors staring at you.
         */
        Grabber getColor_Eyeballs = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                int x_1, x_2, y_1, y_2;

                x ^= x >> 31; // x = abs(dx); only absolute values come in
                y ^= y >> 31;
                x_1 = x >> 8; // x_1 = x / 256; pixels, that is, the z_new points were multiplied by 256 and scaled to screen
                x_2 = x_1 + 1; // add 1
                y_1 = y >> 8;
                y_2 = y_1 + 1;

                /**
                 * Reflections.
                 */
                x_1 = (x_1 / W & 1) == 0 ? x_1 % W : W_ONE - x_1 % W; // if x_1/W is even... or else if it is odd...
                x_2 = (x_2 / W & 1) == 0 ? x_2 % W : W_ONE - x_2 % W;
                y_1 = (y_1 / H & 1) == 0 ? y_1 % H : H_ONE - y_1 % H;
                y_2 = (y_2 / H & 1) == 0 ? y_2 % H : H_ONE - y_2 % H;

                y_1 *= W;
                y_2 *= W;

                /**
                 * Interpolation.
                 */
                int c11 = data_buffer.getElem(y_1 + x_1);
                int c21 = data_buffer.getElem(y_1 + x_2);
                int c12 = data_buffer.getElem(y_2 + x_1);
                int c22 = data_buffer.getElem(y_2 + x_2);

                int A_x = x & 0xFF;
                int A_y = y & 0xFF;
                int B_x = ~A_x & 0xFF;
                int B_y = ~A_y & 0xFF;
                int C_11 = B_x * B_y >> 8;
                int C_12 = A_x * B_y >> 8;
                int C_21 = B_x * A_y >> 8;
                int C_22 = A_x * A_y >> 8;

                /**
                 * Returns a large integer number that contains the pixel color
                 * data at the position (x, y) at the screen.
                 */
                return K & ((K & c11) * C_11 + (K & c21) * C_12 + (K & c12) * C_21 + (K & c22) * C_22 >> 8) | G
                        & ((G & c11) * C_11 + (G & c21) * C_12 + (G & c12) * C_21 + (G & c22) * C_22 >> 8);
            }

            @Override
            public void interface_nextFrame() {
            }
        };

        /**
         * The IFS1 grabber.
         */
        Grabber getColor_IFS1 = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                int x_1, x_2, y_1, y_2;

                x ^= x >> 31;
                y ^= y >> 31;
                x_1 = x >> 8;
                x_2 = x_1 + 1;
                y_1 = y >> 8;
                y_2 = y_1 + 1;

                x_1 = x_1 % HALF_W; // can be just W for a nicer symmetry
                x_2 = x_2 % HALF_W;
                y_1 = y_1 % HALF_H;
                y_2 = y_2 % HALF_H;

                y_1 *= W;
                y_2 *= W;

                /**
                 * Interpolation.
                 */
                int c11 = data_buffer.getElem(y_1 + x_1);
                int c21 = data_buffer.getElem(y_1 + x_2);
                int c12 = data_buffer.getElem(y_2 + x_1);
                int c22 = data_buffer.getElem(y_2 + x_2);

                int A_x = x & 0xFF;
                int A_y = y & 0xFF;
                int B_x = ~A_x & 0xFF;
                int B_y = ~A_y & 0xFF;
                int C_11 = B_x * B_y >> 8;
                int C_12 = A_x * B_y >> 8;
                int C_21 = B_x * A_y >> 8;
                int C_22 = A_x * A_y >> 8;

                /**
                 * Returns a large integer number that contains the color data.
                 */
                return K & ((K & c11) * C_11 + (K & c21) * C_12 + (K & c12) * C_21 + (K & c22) * C_22 >> 8) | G
                        & ((G & c11) * C_11 + (G & c21) * C_12 + (G & c12) * C_21 + (G & c22) * C_22 >> 8);
            }

            @Override
            public void interface_nextFrame() {
            }
        };

        /**
         * The IFS2 grabber.
         */
        Grabber getColor_IFS2 = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                int x_1, x_2, y_1, y_2;

                x ^= x >> 31;
                y ^= y >> 31;
                x_1 = x >> 8;
                y_1 = y >> 8;
                x_2 = x_1 + 1;
                y_2 = y_1 + 1;

                x_1 = (x_1 > W_ONE) ? x_1 % W : W_ONE - x_1 % HALF_W; // nice, no eyes
                x_2 = (x_2 > W_ONE) ? x_2 % W : W_ONE - x_2 % HALF_W;
                y_1 = (y_1 > H_ONE) ? y_1 % H : H_ONE - y_1 % HALF_H;
                y_2 = (y_2 > H_ONE) ? y_2 % H : H_ONE - y_2 % HALF_H;

                y_1 *= W;
                y_2 *= W;

                /**
                 * Interpolation.
                 */
                int c11 = data_buffer.getElem(y_1 + x_1);
                int c21 = data_buffer.getElem(y_1 + x_2);
                int c12 = data_buffer.getElem(y_2 + x_1);
                int c22 = data_buffer.getElem(y_2 + x_2);

                int A_x = x & 0xFF;
                int A_y = y & 0xFF;
                int B_x = ~A_x & 0xFF;
                int B_y = ~A_y & 0xFF;
                int C_11 = B_x * B_y >> 8;
                int C_12 = A_x * B_y >> 8;
                int C_21 = B_x * A_y >> 8;
                int C_22 = A_x * A_y >> 8;

                /**
                 * Returns a large integer number that contains the color data.
                 */
                return K & ((K & c11) * C_11 + (K & c21) * C_12 + (K & c12) * C_21 + (K & c22) * C_22 >> 8) | G
                        & ((G & c11) * C_11 + (G & c21) * C_12 + (G & c12) * C_21 + (G & c22) * C_22 >> 8);
            }

            @Override
            public void interface_nextFrame() {
            }
        };

        /**
         * The IFS3 grabber.
         */
        Grabber getColor_IFS3 = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                int x_1, x_2, y_1, y_2;

                x ^= x >> 31;
                y ^= y >> 31;
                x_1 = x >> 8;
                y_1 = y >> 8;
                x_2 = x_1 + 1;
                y_2 = y_1 + 1;

                x_1 = (x_1 < W) ? x_1 % HALF_W : W_ONE - x_1 % W;
                x_2 = (x_2 < W) ? x_2 % HALF_W : W_ONE - x_2 % W;
                y_1 = (y_1 < H) ? y_1 % HALF_H : H_ONE - y_1 % H;
                y_2 = (y_2 < H) ? y_2 % HALF_H : H_ONE - y_2 % H;

                y_1 *= W;
                y_2 *= W;

                /**
                 * Interpolation.
                 */
                int c11 = data_buffer.getElem(y_1 + x_1);
                int c21 = data_buffer.getElem(y_1 + x_2);
                int c12 = data_buffer.getElem(y_2 + x_1);
                int c22 = data_buffer.getElem(y_2 + x_2);

                int A_x = x & 0xFF;
                int A_y = y & 0xFF;
                int B_x = ~A_x & 0xFF;
                int B_y = ~A_y & 0xFF;
                int C_11 = B_x * B_y >> 8;
                int C_12 = A_x * B_y >> 8;
                int C_21 = B_x * A_y >> 8;
                int C_22 = A_x * A_y >> 8;

                /**
                 * Returns a large integer number that contains the color data.
                 */
                return K & ((K & c11) * C_11 + (K & c21) * C_12 + (K & c12) * C_21 + (K & c22) * C_22 >> 8) | G
                        & ((G & c11) * C_11 + (G & c21) * C_12 + (G & c12) * C_21 + (G & c22) * C_22 >> 8);
            }

            @Override
            public void interface_nextFrame() {
            }
        };

        /**
         * The IFS4 grabber.
         */
        Grabber getColor_IFS4 = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                int x_1, x_2, y_1, y_2;

                x ^= x >> 31;
                y ^= y >> 31;
                x_1 = x >> 8;
                x_2 = x_1 + 1;
                y_1 = y >> 8;
                y_2 = y_1 + 1;

                /**
                 * Reflections.
                 */
                x_1 = (x_1 / W & 1) == 0 ? x_1 % HALF_W : W_ONE - x_1 % HALF_W;
                x_2 = (x_2 / W & 1) == 0 ? x_2 % HALF_W : W_ONE - x_2 % HALF_W;
                y_1 = (y_1 / H & 1) == 0 ? y_1 % HALF_H : H_ONE - y_1 % HALF_H;
                y_2 = (y_2 / H & 1) == 0 ? y_2 % HALF_H : H_ONE - y_2 % HALF_H;

                y_1 *= W;
                y_2 *= W;

                /**
                 * Safety check, because very large numbers could overflow, wrap
                 * around and turn out as negative numbers.
                 */
                x_1 ^= x_1 >> 31;
                y_1 ^= y_1 >> 31;
                x_2 ^= x_2 >> 31;
                y_2 ^= y_2 >> 31;

                /**
                 * Interpolation.
                 */
                int c11 = data_buffer.getElem(y_1 + x_1);
                int c21 = data_buffer.getElem(y_1 + x_2);
                int c12 = data_buffer.getElem(y_2 + x_1);
                int c22 = data_buffer.getElem(y_2 + x_2);

                int A_x = x & 0xFF;
                int A_y = y & 0xFF;
                int B_x = ~A_x & 0xFF;
                int B_y = ~A_y & 0xFF;
                int C_11 = B_x * B_y >> 8;
                int C_12 = A_x * B_y >> 8;
                int C_21 = B_x * A_y >> 8;
                int C_22 = A_x * A_y >> 8;

                /**
                 * Returns a large integer number that contains the pixel color
                 * data at the position (x, y) at the screen.
                 */
                return K & ((K & c11) * C_11 + (K & c21) * C_12 + (K & c12) * C_21 + (K & c22) * C_22 >> 8) | G
                        & ((G & c11) * C_11 + (G & c21) * C_12 + (G & c12) * C_21 + (G & c22) * C_22 >> 8);
            }

            @Override
            public void interface_nextFrame() {
            }
        };

        /**
         * The IFS5 grabber.
         */
        Grabber getColor_IFS5 = new Grabber() {
            @Override
            public int getColor(int x, int y) {
                int x_1, x_2, y_1, y_2;

                x ^= x >> 31;
                y ^= y >> 31;
                x_1 = x >> 8;
                x_2 = x_1 + 1;
                y_1 = y >> 8;
                y_2 = y_1 + 1;

                /**
                 * Reflections.
                 */
                x_1 = (x_1 / W & 1) == 0 ? x_1 % HALF_W : HALF_W_ONE - x_1 % HALF_W;
                x_2 = (x_2 / W & 1) == 0 ? x_2 % HALF_W : HALF_W_ONE - x_2 % HALF_W;
                y_1 = (y_1 / H & 1) == 0 ? y_1 % HALF_H : HALF_H_ONE - y_1 % HALF_H;
                y_2 = (y_2 / H & 1) == 0 ? y_2 % HALF_H : HALF_H_ONE - y_2 % HALF_H;

                y_1 *= W;
                y_2 *= W;

                /**
                 * If we are to have a safety check, we might as well utilize
                 * the overflow with another remainder function.
                 */
                // x_1 = x_1 % W;
                // y_1 = y_1 % H ;
                // x_2 = x_2 % W;
                // y_2 = y_2 % H;
                /**
                 * Safety check, because very large numbers could overflow, wrap
                 * around and turn out as negative numbers.
                 */
                x_1 ^= x_1 >> 31;
                y_1 ^= y_1 >> 31;
                x_2 ^= x_2 >> 31;
                y_2 ^= y_2 >> 31;

                /**
                 * Interpolation.
                 */
                int c11 = data_buffer.getElem(y_1 + x_1);
                int c21 = data_buffer.getElem(y_1 + x_2);
                int c12 = data_buffer.getElem(y_2 + x_1);
                int c22 = data_buffer.getElem(y_2 + x_2);

                int A_x = x & 0xFF;
                int A_y = y & 0xFF;
                int B_x = ~A_x & 0xFF;
                int B_y = ~A_y & 0xFF;
                int C_11 = B_x * B_y >> 8;
                int C_12 = A_x * B_y >> 8;
                int C_21 = B_x * A_y >> 8;
                int C_22 = A_x * A_y >> 8;

                /**
                 * Returns a large integer number that contains the pixel color
                 * data at the position (x, y) at the screen.
                 */
                return K & ((K & c11) * C_11 + (K & c21) * C_12 + (K & c12) * C_21 + (K & c22) * C_22 >> 8) | G
                        & ((G & c11) * C_11 + (G & c21) * C_12 + (G & c12) * C_21 + (G & c22) * C_22 >> 8);
            }

            @Override
            public void interface_nextFrame() {
            }
        };


        // selection of grabbers that we are going to use by default
        Grabber get_color_basic = getColorBasic;
        Grabber get_color_for_convolution = getColorBasic;
        Grabber get_color_simple = getColorSimple;


        /**
         * Obtains the color of pixel at the given coordinates. Uses a Grabber.
         */
        public int getColor(int x, int y) {
            return get_color_basic.getColor(x, y);
        }


        /**
         * Select the grabber used during operation. AKA selection of reflection transformation, because of the included
         * linear geometric transformations that transform the inputted pixel coordinates into the coordinates that fit
         * into the allowed screen dimensions, thereby creating the geometric image transformation for some target
         * coordinates. Press i.
         */
        public void select_grabber() {
            if (reflection == 0) {
                get_color_basic = getColorDefault;
            }
            if (reflection == 1) {
                get_color_basic = getColorBasic;
            }
            if (reflection == 2) {
                get_color_basic = getColor_Eyeballs;
            }
            if (reflection == 3) {
                get_color_basic = getColor_IFS1;
            }
            if (reflection == 4) {
                get_color_basic = getColor_IFS2;
            }
            if (reflection == 5) {
                get_color_basic = getColor_IFS3;
            }
            if (reflection == 6) {
                get_color_basic = getColor_IFS4;
            }
            if (reflection == 7) {
                get_color_basic = getColor_IFS5;
            }
        }
    }


    /**
     * The DoubleBuffer class constructor inputs buffered images used for drawing (as drawing canvases).
     */
    public DoubleBuffer(Perceptron p, BufferedImage a, BufferedImage b, BufferedImage s, BufferedImage d) {
        percept = p;
        if (a != null) {
            output = new ImageRenderContext(a);
        }
        if (b != null) {
            buffer = new ImageRenderContext(b);
        }
        if (s != null) {
            sketch = new ImageRenderContext(s);
        }
        if (d != null) {
            various_displays = new ImageRenderContext(d);
        }
    }


    /**
     * Flip buffer(s).
     */
    public void flip() {
        ImageRenderContext temp = output;
        output = buffer;
        buffer = temp;
    }


    /**
     * Load buffered image.
     */
    public void load_image(BufferedImage s, int W, final Perceptron P) {
        try {
            if (s != null) {

                if (image != null) {
                    image_fade = new ImageRenderContext(image.image);
                    image = new ImageRenderContext(s);
                } else {
                    image = new ImageRenderContext(s);
                    image_fade = new ImageRenderContext(image.image);
                }

                final float SCALE1 = (float) image.W / W;
                final float SCALE2 = (float) image_fade.W / W;

                final ImageRenderContext thisSketch = image;
                final Grabber g1 = image.getColorDefault;
                final Grabber g2 = image_fade.getColorDefault;

                final Grabber endfade = new Grabber() {
                    @Override
                    public int getColor(int x, int y) {
                        return g1.getColor(x *= SCALE1, y *= SCALE1);
                    }

                    @Override
                    public void interface_nextFrame() {
                    }
                };

                final long fadeout = (1 << 12) + System.currentTimeMillis();
                thisSketch.get_color_basic = new Grabber() {
                    int fade = 0;

                    @Override
                    public int getColor(int x, int y) {
                        return ColorUtility.average(g2.getColor((int) (x * SCALE2), (int) (y * SCALE2)), fade,
                                g1.getColor((int) (x * SCALE1), (int) (y * SCALE1)), 256 - fade);
                    }

                    @Override
                    public void interface_nextFrame() {
                        int f = (int) (fadeout - System.currentTimeMillis()) >> 4;
                        if (f < 0) {
                            if (P.rotateImages) {
                                P.increment_image(1);
                            } else {
                                thisSketch.get_color_basic = endfade;
                            }
                        } else {
                            fade = Math.min(256, Math.max(0, f));
                        }
                    }
                };
                thisSketch.get_color_basic.interface_nextFrame();
            }
        } catch (Exception e) {
            System.err.println("SOMETHING BROKE in the DoubleBuffer/load_image!");
        }
    }


    /**
     * Load buffered image from the screen grabber framed by its window.
     */
    public void load_image_from_screen_grabber(BufferedImage s, int W) {
        if (s == null) return;
        try {

            if (image != null) {
                image_fade = new ImageRenderContext(image.image);
                image = new ImageRenderContext(s);
            } else {
                image = new ImageRenderContext(s);
                image_fade = new ImageRenderContext(image.image);
            }

            final ImageRenderContext thisSketch = image;
            final float SCALE1 = (float) image.W / W;
            final float SCALE2 = (float) image_fade.W / W;
            final Grabber g1 = image.getColorDefault;
            final Grabber g2 = image_fade.getColorDefault;

            final Grabber endfade = new Grabber() {
                @Override
                public int getColor(int x, int y) {
                    return g1.getColor(x *= SCALE1, y *= SCALE1);
                }

                @Override
                public void interface_nextFrame() {
                }
            };

            thisSketch.get_color_basic = new Grabber() {
                int fade = 0;

                @Override
                public int getColor(int x, int y) {
                    return ColorUtility.average(g2.getColor((int) (x * SCALE2), (int) (y * SCALE2)), fade,
                            g1.getColor((int) (x * SCALE1), (int) (y * SCALE1)), 256 - fade);
                }

                @Override
                public void interface_nextFrame() {
                    thisSketch.get_color_basic = endfade;
                }
            };
            thisSketch.get_color_basic.interface_nextFrame();

        } catch (Exception e) {
            System.err.println("SOMETHING BROKE in the DoubleBuffer/load_image_from_screen_grabber!");
        }
    }


    /**
     * Select the reflection transformation. Press i.
     */
    public void set_reflection(int r) {
        reflection = wrap(r, number_of_grabbers);
        if (percept.sw != null) {
            percept.sw.jcb_reflection_map.setSelectedIndex(reflection);
        }
    }


    /**
     * Wrapper.
     */
    int wrap(int n, int m) {
        return n < 0 ? m - (-n % m) : n % m;
    }

}
