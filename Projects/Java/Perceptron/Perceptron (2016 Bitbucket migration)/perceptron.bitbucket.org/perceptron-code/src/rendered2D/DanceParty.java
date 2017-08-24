package rendered2D;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 *
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Random;

public class DanceParty {

    int SIZE = 8;
    int MASK = (1 << SIZE) - 1;
    int W = 1 << SIZE;
    int H = 1 << SIZE;
    // make sure SQUARESIZE divides SIZE
    int SQUARESIZE = 1 << (SIZE - 3);
    int SQUARESTEP = SIZE / SQUARESIZE;
    public BufferedImage draw;
    public DataBuffer data;
    public Graphics graph;
    Random rand = new Random();

    public DanceParty() {
        draw = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        data = draw.getRaster().getDataBuffer();
        graph = draw.getGraphics();
    }

    public void step() {
        for (int i = 0; i < H; i += SQUARESIZE) {
            for (int j = 0; j < W; j += SQUARESIZE) {
                graph.setColor(Color.getHSBColor(rand.nextFloat(), 1f, rand.nextFloat()));
                graph.fillRect(i, j, SQUARESIZE, SQUARESIZE);
            }
        }
    }
}
