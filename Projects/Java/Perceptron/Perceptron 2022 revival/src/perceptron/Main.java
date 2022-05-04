package perceptron;

import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

/* Main.java
 * Created on March 8, 2007, 4:15 PM
 */

/**
 * @author Michael Everett Rule
 */
public class Main {

    /** Terminates the program */
    public static void exit() {
        System.exit(0);
    }

/////////////////////// MAIN ///////////////////////////
/////
    /** @param args the command line arguments*/
    public static void main(String[] args) {

        System.out.println("Welcome to Perceptron...");

        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Starting GUI.");
                try {
                    javax.swing.UIManager.setLookAndFeel(
                            javax.swing.UIManager.
                                    getSystemLookAndFeelClassName());
                    System.out.println("Set system L&F");
                } catch (ClassNotFoundException
                        | IllegalAccessException
                        | InstantiationException
                        | UnsupportedLookAndFeelException e) {
                }
                final Perceptron m = new Perceptron(
                        "resource/Settings.txt",
                        "resource/CrashReport.txt",
                        "resource/presets/");
                Thread go = new Thread() {
                    @Override
                    public void run() {
                        m.go();
                    }
                };
                go.setPriority(Thread.MIN_PRIORITY);
                go.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
    }
}

