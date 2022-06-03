package util;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.lang.Math.min;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static javax.swing.SwingUtilities.isEventDispatchThread;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static util.Sys.serr;
import static util.Sys.sout;

/**
 *
 * @author mer49
 */
public class Fullscreen {
    
    /**
     * 
     */
    public static void setNiceLAF() {
        sout("Current LAF is"+UIManager.getLookAndFeel());
        sout("System  LAF is"+UIManager.getSystemLookAndFeelClassName());
        String syslaf = UIManager.getSystemLookAndFeelClassName().toLowerCase();
        if (isJavaLAF(syslaf)) {
            for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                String cn = laf.getClassName();
                if (!isJavaLAF(cn)) {
                    syslaf = cn;
                    break;
                }
            }
        }
        try {
            UIManager.setLookAndFeel(syslaf);
        } catch (ClassNotFoundException 
                | InstantiationException 
                | IllegalAccessException 
                | UnsupportedLookAndFeelException ex) {
            serr(ex.getMessage());
        }
    }
    
    /**
     * 
     * @param laf
     * @return 
     */
    public static boolean isJavaLAF(String laf) {
        return laf.contains("motif") || laf.contains("metal") || laf.contains("nimbus");
    }
    
    /**
     * 
     */
    public static void printScreenInfo() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();
        for (var gd : screenDevices) {
            sout(gd);
            sout("  "+gd.getDisplayMode());
            sout("  isDisplayChangeSupported "+gd.isDisplayChangeSupported());
            sout("  isFullScreenSupported    "+gd.isFullScreenSupported());
            
            //sout("  "+gd.getDisplayModes());
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            sout("  "+gc);
            sout("    "+gc.getBounds());
            sout("    "+gc.getImageCapabilities());
        }
    }
    
    /**
     * Use location of center of window to identify screen device.
     * @param j
     * @return 
     */
    public static GraphicsDevice getScreen(JFrame j){
        Rectangle window = j.getBounds();
        float x = window.x + .5f*window.width;
        float y = window.y + .5f*window.height;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (var gd : ge.getScreenDevices()) {
            Rectangle screen = gd.getDefaultConfiguration().getBounds();
            if (screen.contains(x, y)) return gd;
        }
        throw new RuntimeException("Window "+j+" is not on any screen");
    }
    
    /**
     * If already in full-screen mode, attempt to change to a lower resolution.
     * This will be faster when possible. 
     * @param j
     * @param w
     * @param h 
     */
    public static void changeDisplayMode(JFrame j, int w, int h) {
        GraphicsEnvironment v = getLocalGraphicsEnvironment();
        GraphicsDevice      g = v.getDefaultScreenDevice();
        DisplayMode         d = g.getDisplayMode();
        if (g.isDisplayChangeSupported()) {
            // Locate the best display mode
            int depth = min(32,d.getBitDepth());
            DisplayMode best = null;
            int size = 0;
            for (DisplayMode m : g.getDisplayModes()) {
                int mw=m.getWidth(), mh=m.getHeight(), md=m.getBitDepth();
                if (mw>=w && mh>=h && md>=depth && (best==null||mw*mh<size)) {
                    best = m;
                    size = mw*mh;
                }
            }
            if (best != null) {
                g.setDisplayMode(best);
                j.requestFocus();
            }
        }
    }
    
    /**
     * Sets whether to draw the window borders on a JFrame.
     * @param j
     * @param frame_on 
     * @param preserve_size 
     */
    public static void setFrame(JFrame j, boolean frame_on, boolean preserve_size) {
        if (!isEventDispatchThread()) {
            serr("Expected to be on the event dispatch thread!");
            serr("(I'm refusing to change the window thread)");
            return;
        }
        if (frame_on != j.isUndecorated()) return;
        if (preserve_size) 
            j.getRootPane().setPreferredSize(j.getRootPane().getSize());
        j.dispose();
        j.setUndecorated(!frame_on);
        j.pack();
        j.setVisible(true);
        j.requestFocus();
    }
    
    /**
     * Get size of interior of JFrame
     * @param j
     * @return 
     */
    public static Dimension getInnerSize(JFrame j) {
        Dimension size = j.getSize();
        Insets insets  = j.getInsets();
        if (insets != null) {
            size.height -= insets.top + insets.bottom;
            size.width  -= insets.left + insets.right;
        }
        return size;
    }
    
    /**
     * Check if we're the full-screen window.
     * @param j
     * @return 
     */
    public static boolean isFullscreenWindow(JFrame j) {
        return j == j.getGraphicsConfiguration()
            .getDevice()
            .getFullScreenWindow();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    static void testFrame(String title) {

        setNiceLAF();
        printScreenInfo();    

        String[] data = new String[10];
        JFrame window = new JFrame(title);
        
        JPanel content = new JPanel(){
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                String all = "";
                for (String s : data) if (null!=s) all+="\n"+s; 
                
                GraphicsDevice gd = getScreen(window);
                all+="\n"+gd.toString();
                all+="\nisDisplayChangeSupported "+gd.isDisplayChangeSupported();
                all+="\nisFullScreenSupported "+gd.isFullScreenSupported();
                
                final Font TEXTFONT = new Font(Font.MONOSPACED,Font.PLAIN,14);
                final int LINEHEIGHT = 16;
                g.setFont(TEXTFONT);
                int y = 2;
                for (var l : all.split("\n")) {
                    g.drawString(l, 2, y += LINEHEIGHT );
                }
            }};
        content.setLayout(new BorderLayout());
        content.setPreferredSize(new Dimension(640,480));
        content.addMouseMotionListener(new MouseAdapter(){
            public void mouseMoved(MouseEvent e) {
                data[2] = e.getLocationOnScreen().toString();
                data[3] = e.getPoint().toString();
                content.repaint();
                e.consume();
            }
        });
        final AtomicBoolean am_fullscreen = new AtomicBoolean(false);
        content.addKeyListener(new KeyAdapter(){
            public void keyTyped(KeyEvent e) {
                
                data[4] = "Typed "+e.toString();
                am_fullscreen.set(!am_fullscreen.get());
                
                if (am_fullscreen.get()) {
                    GraphicsDevice gd = getScreen(window);
                    if (gd.isFullScreenSupported()) {
                        window.dispose();
                        window.setUndecorated(true);
                        window.pack();
                        window.setVisible(true);
                        gd.setFullScreenWindow((Window)window);
                        data[5] = "Am I full screen yet?";
                    }
                } else {
                    GraphicsDevice gd = getScreen(window);
                    if (gd.isFullScreenSupported()) {
                        window.dispose();
                        window.setUndecorated(false);
                        gd.setFullScreenWindow((Window)null);
                        window.pack();
                        window.setVisible(true);
                        data[5] = "Am I normal yet?";
                    }
                }
            }
        });
        data[4] = "Press any key to enter full screen";
        content.setFocusable(true);
        
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
        window.setContentPane(content);
        window.pack();
        window.setVisible(true);
        
        (new Thread(()->{
        while (true) {
            data[0] = window.getBounds().toString();
            data[1] = window.getRootPane().getBounds().toString();
            content.repaint();
            try {Thread.sleep(100);} catch (InterruptedException ex) {}
        }
        })).start();
    }
    
    public static void main(String [] args) {
        SwingUtilities.invokeLater(()->{testFrame("Frame 1");});
        SwingUtilities.invokeLater(()->{testFrame("Frame 2");});
    }
}
