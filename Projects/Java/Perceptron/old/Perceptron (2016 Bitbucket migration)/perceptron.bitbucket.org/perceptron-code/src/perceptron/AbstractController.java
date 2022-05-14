package perceptron;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 *
 */

import java.awt.*;

public interface AbstractController {

    public void stepControl(int i);

    public void stepActive(double dR);

    public void paint(Graphics g);

    public void stepFrame(double dt);
}
