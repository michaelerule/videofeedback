package automata;

/**
 * Perceptron
 *
 * @author Michael Everett Rule
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 */

import perceptron.Perceptron;
import util.ColorUtility;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Random;

public class Waves_1 {

    int SIZE = 5;
    int MASK = (1 << SIZE) - 1;
    int W = 1 << SIZE;
    int H = 1 << SIZE;
    float Vo = 0;
    float VMAX = 10;
    float VMIN = 5;
    float VTIP = 9;

    class Oscillator {

        float V;
        float Vprime;
    }

    Oscillator[][] cell;
    Oscillator[][] buff;
    public BufferedImage draw;
    public DataBuffer data;
    Random rand = new Random();
    Perceptron perceptron;

    public Waves_1(Perceptron p) {
        perceptron = p;
        cell = new Oscillator[H][W];
        buff = new Oscillator[H][W];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                cell[i][j] = new Oscillator();
                buff[i][j] = new Oscillator();
                cell[i][j].Vprime = buff[i][j].Vprime = VMIN;
            }
        }
        draw = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        data = draw.getRaster().getDataBuffer();
    }

    public void step() {
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                float V = .2f * (buff[(i - 1) & MASK][j].V + buff[(i + 1) & MASK][j].V + buff[i][j].V + buff[i][(j - 1) & MASK].V + buff[i][(j + 1) & MASK].V);
                float Vprime = buff[i][j].Vprime;
                if (Vprime < VMIN) {
                    V *= .99f;
                } else {
                    V = V * .99f + .01f * VMAX;
                }
                Vprime = Vprime * .99f + .01f * V;
                if (V > VMAX - 1) {
                    Vprime = 0;
                } else if (V < VMIN) {
                    Vprime = VMAX;
                }
                cell[i][j].V = V + (float) rand.nextGaussian() * .01f;
                cell[i][j].Vprime = Vprime;

                int color2 = ((int) (0xFF * (1 - (V - VMIN) / (VMAX - VMIN))) & 0xFF);
                int weight = color2;
                color2 <<= 16;
                data.setElem(
                        (i << SIZE) + j,
                        ColorUtility.average(perceptron.double_buffer.output.getColor(j << 8, i << 8), 256 - weight,
                                perceptron.double_buffer.image_fade.getColor(j << 8, i << 8), weight));
            }
        }

        Oscillator[][] temp = buff;
        buff = cell;
        cell = temp;
    }
}
