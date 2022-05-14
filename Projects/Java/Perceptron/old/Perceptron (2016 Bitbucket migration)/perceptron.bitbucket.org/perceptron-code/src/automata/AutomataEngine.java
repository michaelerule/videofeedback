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

public interface AutomataEngine {

    public void step(float[][] state, BufferedImage text, ColorScheme color);
}
