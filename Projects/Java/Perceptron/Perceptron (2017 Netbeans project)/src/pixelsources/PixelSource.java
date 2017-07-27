package pixelsources;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**Abstract interface for modules that can deliver pixes to
 * Perceptron.
 *
 * Note : it may be good to have all PixelSources share a buffer.
 * This might be implemented as either an interface method that
 * provides a drawing buffer,
 *
 * Or an enforced constructor or generator that takes a buffer.
 *
 * @author mrule
 */
public interface PixelSource {

    /** Returns a BufferedImage
     * @return
     */
    public BufferedImage getSource();

    /** Informs the module to advance frames */
    public void step();

    /** Tells the modules to process a keystroke
     * @param e a KeyEvent
     */
    public void keyPressed(KeyEvent e);
    
}
