package perceptron;

import java.awt.Color;
import java.awt.Graphics;
/*
 * AppletTest.java
 *
 * Created on April 25, 2007, 5:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Michael Everett Rule
 */
public class AppletTest extends javax.swing.JApplet {

    /** Creates a new instance of AppletTest */
    public AppletTest() {
    }

    /**
     *
     */
    @Override
    public void init() {
        getGraphics().setColor(Color.RED);
        getGraphics().fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     *
     * @param G
     */
    @Override
    public void paint(Graphics G) {
        G.drawString("Testing Applets...", 10, 10);
    }
}
