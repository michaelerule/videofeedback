package automata;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 *
 */

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Random;

public class Waves {

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

    public Waves() {
        cell = new Oscillator[H][W];
        buff = new Oscillator[H][W];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                cell[i][j] = new Oscillator();
                buff[i][j] = new Oscillator();
                cell[i][j].Vprime = buff[i][j].Vprime = VMAX;
                float theta = (float) (2 * Math.PI * rand.nextFloat());
                float ct = (float) Math.cos(theta);
                float st = (float) Math.sin(theta);
                float V = cell[i][j].V * ct + st * cell[i][j].Vprime;
                float Vp = -cell[i][j].V * st + ct * cell[i][j].Vprime;
                cell[i][j].V = V;
                cell[i][j].Vprime = Vp;
            }
        }
        draw = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        data = draw.getRaster().getDataBuffer();
    }

    public void step() {
        float theta = .3f;
        float ct = (float) Math.cos(theta);
        float st = (float) Math.sin(theta);
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                float V = .33333333333333f * (.5f * buff[(i - 1) & MASK][j].V + .5f * buff[(i + 1) & MASK][j].V + buff[i][j].V + .5f
                        * buff[i][(j - 1) & MASK].V + .5f * buff[i][(j + 1) & MASK].V);
                float Vprime = buff[i][j].Vprime;
                float dr = (float) Math.sqrt((Vprime * Vprime + V * V));
                // if ( dr < .00001 ) {
                // cell[i][j].Vprime = buff[i][j].Vprime =
                // (float)rand.nextGaussian() ;
                // cell[i][j].V = buff[i][j].V = (float)rand.nextGaussian() ;
                // } else {
                dr = VMAX / dr;
                cell[i][j].V = dr * (V * ct + st * Vprime + 6 * (float) rand.nextGaussian());
                cell[i][j].Vprime = dr * (-V * st + ct * Vprime);
                // }
                int color = (int) (0xFF * Math.max(0, V + VMAX + 5) / (10 + VMAX * 2)) & 0xFF;
                int color2 = (int) (0xFF * Math.max(0, Vprime + VMAX + 5) / (10 + VMAX * 2)) & 0xFF;
                // draw.setRGB(j, i, 0xFF0000);
                data.setElem((i << SIZE) | j, (255 - color) | (color2 << 8) | (color << 16));
            }
        }

        Oscillator[][] temp = buff;
        buff = cell;
        cell = temp;
    }
}
