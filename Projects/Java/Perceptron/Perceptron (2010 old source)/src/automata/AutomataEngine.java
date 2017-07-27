/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automata;
import java.awt.image.BufferedImage;
/**
 *
 * @author mrule
 */
public interface AutomataEngine {

    public void step(float [][] state, BufferedImage text, ColorScheme color);
    
}
