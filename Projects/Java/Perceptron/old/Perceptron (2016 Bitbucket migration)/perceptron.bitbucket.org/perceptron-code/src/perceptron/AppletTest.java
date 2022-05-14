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

public class AppletTest extends javax.swing.JApplet {

    private static final long serialVersionUID = 274467137958072929L;

    /**
     * Creates a new instance of AppletTest
     */
    public AppletTest() {
    }

    @Override
    public void init() {
        getGraphics().setColor(Color.RED);
        getGraphics().fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void paint(Graphics G) {
        G.drawString("Testing Applets...", 10, 10);
    }
}
