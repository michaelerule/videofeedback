/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pixelsources;

import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** automata.Main should text automata engines
 *
 * @author mrule
 */
public class Main {
    public static void main(String [] args) {
        final PixelSource ps = new TriLife();

        JFrame view = new JFrame("Automata");
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel jp = new JPanel(){
            public void paint(Graphics g){
                g.drawImage(ps.getSource(),0,0,getWidth(),getHeight(),null);
            }
        };

        while (true) {
            ps.step();
            jp.paint(jp.getGraphics());
        }
    }
}
