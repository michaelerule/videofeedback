package perceptron;

import java.awt.Graphics;

/**
 *
 * @author mer49
 */
public interface AbstractController {

    /**
     *
     * @param i
     */
    public void stepControl(int i);

    /**
     *
     * @param dR
     */
    public void stepActive(double dR);

    /**
     *
     * @param g
     */
    public void paint(Graphics g);

    /**
     *
     * @param dt
     */
    public void stepFrame(double dt);
}
