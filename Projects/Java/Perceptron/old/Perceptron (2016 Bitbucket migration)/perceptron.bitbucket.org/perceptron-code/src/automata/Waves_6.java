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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Random;

public class Waves_6 {

    int COLORB = 0xFFFF90;
    int COLORA = 0x800040;
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

    public Waves_6() {
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
                    V *= .9f;
                } else {
                    V = V * .9f + .1f * VMAX;
                }
                Vprime = Vprime * .95f + .05f * V;
                if (V > VMAX - 1) {
                    Vprime = 0;
                } else if (V < VMIN) {
                    Vprime = VMAX;
                }
                cell[i][j].V = V + (float) rand.nextGaussian() * .1f;
                cell[i][j].Vprime = Vprime;

                int color1 = (int) (0xFF * ((V - VMIN) / (VMAX - VMIN))) & 0xFF;

                data.setElem((i << SIZE) + j,
                        ColorUtility.average(rand.nextInt(0x1000000), 26, ColorUtility.average(COLORA, color1, COLORB, 256 - color1), 230));

                // int color = (int)(0xFF*Math.max(0,V+VMAX+5)/(10+VMAX*2)) &
                // 0xFF ;
                // int color2 =
                // (int)(0xFF*Math.max(0,Vprime+VMAX+5)/(10+VMAX*2)) & 0xFF ;
                // draw.setRGB(j, i, 0xFF0000);
                // data.setElem((i<<SIZE)|j, ( 255 - color ) | ( color2 << 8 ) |
                // ( color << 16 ));
            }
        }

        Oscillator[][] temp = buff;
        buff = cell;
        cell = temp;
    }
}
