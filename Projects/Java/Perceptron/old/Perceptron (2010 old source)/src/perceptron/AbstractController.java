package perceptron;

import java.awt.Graphics;

public interface AbstractController {

    public void stepControl(int i);

    public void stepActive(double dR);

    public void paint(Graphics g);

    public void stepFrame(double dt);
}
