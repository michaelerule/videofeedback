package perceptron;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.swing.SwingUtilities.invokeAndWait;

/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 * @author Predrag Bokšić (junkerade@gmail.com)
 *         <p/>
 *         Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *         It evolves colored geometric patterns and visual images into the realm of infinite details
 *         and deepens the thought. </p>
 *         <p/>
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 */

// extends the charm of event dispatch thread to solve issues with dialogues
final class Main extends JFrame implements Runnable {

    /**
     * Establish a pool of threads based on the number of CPUs, which defaults to the number of CPUs, CPU cores, or
     * CPU hyper threads. Number of threads in reality is unlimited.
     */
    final static int cpu_num = Runtime.getRuntime().availableProcessors();
    final static ExecutorService executor = Executors.newWorkStealingPool();
    final static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final static GraphicsDevice gd = ge.getDefaultScreenDevice();
    final static GraphicsConfiguration gc = gd.getDefaultConfiguration();
    static Perceptron perceptron;
    long memory = Runtime.getRuntime().freeMemory();


    public static void main(String[] argv) {

        Thread.currentThread().setName("Perceptron : initial thread");

        /** Do not accept any arguments. */
        if (argv.length > 0) {
            System.out.println("Consider running without any arguments after the Program Name.");
            return;
        }

        /** Welcome message */
        System.out.println("Welcome to Perceptron...");

        /** Run the program. */
        new Main();

    }


    /**
     * Configuration of Perceptron.
     */
    Main() {

        super(gc);  // we give some memory to god

        System.out.println("number of CPU's available at startup: " + cpu_num);
        System.out.println("free memory: " + memory);

        /**   Resources check     */
        try {
            File resource_folder = new File("resource");
            File settings_file = new File("resource/Settings.txt");
            File crash_log_file = new File("resource/CrashReport.txt");
            File presets_folder = new File("resource/presets");
            File cursors_folder = new File("resource/cursors");
            File data_folder = new File("resource/data");
            File ip_webcams_file = new File("resource/cameras.xml");

            if (!resource_folder.exists() || !resource_folder.canRead()
                    || !settings_file.exists() || !settings_file.canRead()
                    || !crash_log_file.exists() || !crash_log_file.canRead()
                    || !presets_folder.exists() || !presets_folder.canRead()
                    || !cursors_folder.exists() || !cursors_folder.canRead()
                    || !data_folder.exists() || !data_folder.canRead()
                    || !ip_webcams_file.exists() || !ip_webcams_file.canRead()) {
                System.err.println("Could not load the resources from the resource folder.");
                System.err.println("If you are running from the command line, try to be\n in the Perceptron folder.");
                exit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not check the resources in the resource folder.");
            exit();
        }

        /**
         * Perceptron is a Swing JFrame, so it runs on EDT, but the graphics rendering loop is in a separate thread.
         * The loop found in Perceptron.go() sometimes responds to the menu commands (dialogues), which run on EDT.     */
        try {
            invokeAndWait(this); // run on EDT
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println();
            System.err.println("Perceptron could not be made a reality.");
        }

    }


    @Override
    public void run() {
        perceptron = new Perceptron("resource/Settings.txt", "resource/CrashReport.txt", "resource/presets");
    }


    /**
     * Terminates the program
     */
    public static void exit() {

        System.out.println();
        System.out.println("....goodbye from Perceptron....");

        //perceptron.write_animation = false;
        perceptron.infinity = false;
        perceptron.running = false;

        try {
            /*
            if (perceptron.movie_writer_qt != null) {
                perceptron.movie_writer_qt.close();
            }
            */

            if (perceptron.screenRecorderMain != null) {
                perceptron.screenRecorderMain.stop();
                perceptron.screenRecorderMain.setVisible(false);
                perceptron.screenRecorderMain.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not shutdown Movie recording system.");
        }

        try {
            if (perceptron.webcam != null) {
                if (perceptron.webcam.isOpen()) {
                    perceptron.webcam.close();
                }
            }
        } catch (Exception c) {
            c.printStackTrace();
            System.err.println("Could not shutdown Webcam system.");
        }


        try {
            perceptron.setVisible(false);
            executor.shutdown();
            //perceptron.dispose();
        } catch (SecurityException s) {
            s.printStackTrace();
            System.err.println("Could not shutdown Perceptron process.");
        }
        System.exit(0);
    }


}
