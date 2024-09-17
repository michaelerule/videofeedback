package perceptron;

import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;

/**
 *
 * @author mer49
 */
public class FullScreenTroubleshoot {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        JFrame test = new JFrame("test");
        int screen_width = 640;
        int screen_height = 640;

        GraphicsDevice g = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode current = g.getDisplayMode();

        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window) test);

            DisplayMode[] possible_modes = g.getDisplayModes();

            DisplayMode best_mode = null;

            for (DisplayMode m : possible_modes) {
                System.out.println(m.getBitDepth() + " " + m.getHeight() + " " + m.getRefreshRate() + " " + m.getWidth());
                if ((m.getWidth() >= screen_width && m.getHeight() >= screen_height && m.getBitDepth() >= Math.min(32, current.getBitDepth()))
                        && (best_mode == null || m.getWidth() * m.getHeight() < best_mode.getWidth() * best_mode.getHeight())) {
                    best_mode = m;
                }
            }
            if (best_mode != null) {
                g.setDisplayMode(best_mode);
            }

        } else {
            DisplayMode m = g.getDisplayMode();
            test.setBounds(0, 0, m.getWidth(), m.getHeight());
            test.setVisible(true);
        }

        test.createBufferStrategy(2);
        BufferStrategy bufferStrategy = test.getBufferStrategy();
        Graphics2D graph2D = (Graphics2D) bufferStrategy.getDrawGraphics();

    }
}
