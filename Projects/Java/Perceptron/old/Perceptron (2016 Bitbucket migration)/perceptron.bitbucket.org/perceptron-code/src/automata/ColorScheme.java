package automata;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 *
 */

import util.ColorUtility;

import java.awt.*;

import static java.lang.Math.*;

/**
 * A ColorScheme should represent as close as possible a bijection between RGB 8
 * bit colorspace and float values in [0,1]
 */
public abstract class ColorScheme {

    public int clip(int i) {
        return min(255, max(0, i));
    }

    public int toRGB(int R, int G, int B) {
        return (R << 16) | (G << 8) | B;
    }

    public abstract int toRGB(float f);

    public abstract float fromRGB(int RGB);

    public abstract void step();

    public static final ColorScheme[] colorschemes = {new GreyscaleColorScheme(), new HueColorScheme(), new HVColorScheme(), new HSVColorScheme(),
            new HueRotatingColorScheme(), new HVRotatingColorScheme(), new HSVRotatingColorScheme(), new TwoToneColorScheme()};

    public static class GreyscaleColorScheme extends ColorScheme {

        @Override
        public int toRGB(float f) {
            int C = clip((int) (.5f + f * 255f));
            return toRGB(C, C, C);
        }

        @Override
        public float fromRGB(int RGB) {
            int R = (RGB >> 16) & 0xff;
            int G = (RGB >> 8) & 0xff;
            int B = (RGB) & 0xff;
            return (R + G + B) * 0.00130718954f;
        }

        @Override
        public void step() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class HueColorScheme extends ColorScheme {

        @Override
        public int toRGB(float f) {
            return Color.HSBtoRGB(f, 1f, 1f);
        }

        @Override
        public float fromRGB(int RGB) {
            int R = (RGB >> 16) & 0xff;
            int G = (RGB >> 8) & 0xff;
            int B = (RGB) & 0xff;
            return Color.RGBtoHSB(R, G, B, new float[3])[0];
        }

        @Override
        public void step() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class HVColorScheme extends ColorScheme {

        @Override
        public int toRGB(float f) {
            return Color.HSBtoRGB(f, 1f, f);
        }

        @Override
        public float fromRGB(int RGB) {
            int R = (RGB >> 16) & 0xff;
            int G = (RGB >> 8) & 0xff;
            int B = (RGB) & 0xff;
            float[] HSV = Color.RGBtoHSB(R, G, B, new float[3]);
            return (HSV[0] + HSV[2]) * .5f;
        }

        @Override
        public void step() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class HSVColorScheme extends ColorScheme {

        @Override
        public int toRGB(float f) {
            return Color.HSBtoRGB(f, 1f - f, f);
        }

        @Override
        public float fromRGB(int RGB) {
            int R = (RGB >> 16) & 0xff;
            int G = (RGB >> 8) & 0xff;
            int B = (RGB) & 0xff;
            float[] HSV = Color.RGBtoHSB(R, G, B, new float[3]);
            return (HSV[0] + HSV[2] + 1f - HSV[1]) * .5f;
        }

        @Override
        public void step() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class HueRotatingColorScheme extends ColorScheme {

        float hue = 0;

        @Override
        public int toRGB(float f) {
            return Color.HSBtoRGB(f + hue, 1f, 1f);
        }

        @Override
        public float fromRGB(int RGB) {
            int R = (RGB >> 16) & 0xff;
            int G = (RGB >> 8) & 0xff;
            int B = (RGB) & 0xff;
            return Color.RGBtoHSB(R, G, B, new float[3])[0];
        }

        @Override
        public void step() {
            hue = (hue + .02f) % 1f;
        }
    }

    public static class HVRotatingColorScheme extends ColorScheme {

        float hue = 0;

        @Override
        public int toRGB(float f) {
            return Color.HSBtoRGB(f + hue, 1f, f);
        }

        @Override
        public float fromRGB(int RGB) {
            int R = (RGB >> 16) & 0xff;
            int G = (RGB >> 8) & 0xff;
            int B = (RGB) & 0xff;
            float[] HSV = Color.RGBtoHSB(R, G, B, new float[3]);
            return (HSV[0] + HSV[2]) * .5f;
        }

        @Override
        public void step() {
            hue = (hue + .02f) % 1f;
        }
    }

    public static class HSVRotatingColorScheme extends ColorScheme {

        float hue = 0;

        @Override
        public int toRGB(float f) {
            return Color.HSBtoRGB(f + hue, 1f - f, f);
        }

        @Override
        public float fromRGB(int RGB) {
            int R = (RGB >> 16) & 0xff;
            int G = (RGB >> 8) & 0xff;
            int B = (RGB) & 0xff;
            float[] HSV = Color.RGBtoHSB(R, G, B, new float[3]);
            return (HSV[0] + HSV[2] + 1f - HSV[1]) * .5f;
        }

        @Override
        public void step() {
            hue = (hue + .02f) % 1f;
        }
    }

    public static class TwoToneColorScheme extends ColorScheme {

        public int color1 = 0xff00ff;
        public int color2 = 0x00ff00;

        @Override
        public int toRGB(float f) {
            int weight = clip((int) (.5f + f * 255f));
            return ColorUtility.average(color1, weight, color2, 256 - weight);
        }

        @Override
        public float fromRGB(int RGB) {
            return (float) random();
        }

        @Override
        public void step() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class MaskingColorScheme extends ColorScheme {

        public int mask = 0xff00ff;
        public ColorScheme colorer;

        @Override
        public int toRGB(float f) {
            return colorer.toRGB(f) ^ mask;
        }

        @Override
        public float fromRGB(int RGB) {
            return colorer.fromRGB(RGB ^ mask);
        }

        @Override
        public void step() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
