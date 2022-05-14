package perceptron;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import static javax.swing.SwingUtilities.invokeLater;
import static util.Fullscreen.printScreenInfo;
import static util.Fullscreen.setNiceLAF;
import static util.Sys.sout;

/* Main.java
 * Created on March 8, 2007, 4:15 PM
 */
public class Main {

    public static void main(String[] args) {
        sout("Welcome to Perceptron...");
        invokeLater(()->{
            sout("Starting GUI.");
            printScreenInfo();
            setNiceLAF();
            final Perceptron P = new Perceptron(
                "resource/Settings.txt",
                "resource/CrashReport.txt",
                "resource/presets/");
            (new Thread(P::go)).start();
        });
    }
}

