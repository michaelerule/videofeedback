/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package perceptron;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.swing.JFrame;
import math.complex;

/**
 *
 * @author mer49
 */
public class Misc {
    

    /** Set up full-screen mode.
     * @param p
     */
    public static void makeFullscreen(Perceptron p) {
        GraphicsDevice g = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode    d = g.getDisplayMode();
        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window)p);
            DisplayMode[] possible_modes = g.getDisplayModes();
            DisplayMode   best_mode      = null;
            for (DisplayMode m : possible_modes) {
                System.out.println(m.getBitDepth() + " " + m.getHeight() + " " + m.getRefreshRate() + " " + m.getWidth());
                if (   (m.getWidth()    >= p.screen_width  && 
                        m.getHeight()   >= p.screen_height && 
                        m.getBitDepth() >= Math.min(32, d.getBitDepth()))
                        && (best_mode == null || m.getWidth()*m.getHeight() < best_mode.getWidth()*best_mode.getHeight()))
                    best_mode = m;
            }
            if (best_mode != null)
                try {g.setDisplayMode(best_mode);}
                catch (InternalError e) {} // Fail silently
        } else {
            DisplayMode m = g.getDisplayMode();
            p.setBounds(0, 0, m.getWidth(), m.getHeight());
            p.setVisible(true);
        }
    }
    
    
    /** Make windowed, "not fullscreen". 
     */
    public static void makeNotFullscreen() {
        GraphicsDevice g = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (g.isFullScreenSupported()) g.setFullScreenWindow((Window) null);
    }

    
    /** Make a JFrame the undecorated main application window
     * @param j
     */
    public static void makeUndecoratedMainFrame(JFrame j) {
        // Exit on window close
        try {
            j.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        } catch (Exception e) {}
        //ignore prompts for redrawing from the operating system
        try {
            j.setIgnoreRepaint(true);
        } catch (Exception e) {}
        //remove the frame decorations titlebar etc..
        //this frame is not to be re-sized
        try {
            j.dispose();
            j.setUndecorated(true);
            j.setResizable(false);
        } catch (Exception e) {
        }
    }
    
    
    /** Get the screen device's graphics configurations
     * 
     * @return 
     */
    public static GraphicsConfiguration getDeviceGraphicsConfig() {
        return (((GraphicsEnvironment.getLocalGraphicsEnvironment())
                .getDefaultScreenDevice())
                .getDefaultConfiguration());
    }
    
    
    /** Clip integer in range
     * 
     * @param x
     * @param low
     * @param hi
     * @return 
     */
    public static int clip(int x,int low, int hi) {
        return x<low? low : x>hi? hi : x;
    }
    public static float clip(float x,float low, float hi) {
        return x<low? low : x>hi? hi : x;
    }
    
    
    /** Periodically wrap integer to range
     * 
     * @param n
     * @param m
     * @return 
     */
    public static int wrap(int n, int m) {
        return n < 0 ? m - (-n % m) : n % m;
    }
    public static float wrap(float n, float m) {
        return n < 0 ? m - (-n % m) : n % m;
    }
    public static double wrap(double n, double m) {
        return n < 0 ? m - (-n % m) : n % m;
    }
    
    
    /** Hide the mouse pointer, if possible
     * @param c */
    public static void hideCursor(Component c) {
        try {
            c.setCursor(Toolkit.getDefaultToolkit().
                    createCustomCursor(Toolkit.getDefaultToolkit().
                    getImage("xparent.gif"), new Point(0, 0), null));
        } catch (HeadlessException | IndexOutOfBoundsException e) {
            System.err.println("Cursor modification is unsupported.");
        }
    }
    
    
    public static final Set<String> trueNames  = Set.of("TRUE", "YES", "Y", "T", "SI", "ON", "1");
    public static final Set<String> falseNames = Set.of("FALSE", "NO", "N", "F", "NON", "OFF", "0");
    
    /** There must be a better way to write a best-effort parser?
     * @param val
     * @return 
     */
    public static Object bestEffortParse(String val) {
        
        try {
            return new Byte(val);
        } catch (NumberFormatException e) {
            // ignore
        }
        try {
            return new Short(val);
        } catch (NumberFormatException e) {
            // ignore
        }
        try {
            return new Integer(val);
        } catch (NumberFormatException e) {
            // ignore
        }
        try {
            return new Float(val);
        } catch (NumberFormatException e) {
            // ignore
        }
        try {
            return new Double(val);
        } catch (NumberFormatException e) {
            // ignore
        }
        try {
            return new complex(val);
        } catch (Exception e) {
            // ignore
        }
        
        if (trueNames.contains(val.toUpperCase()))
            return true;
        
        if (falseNames.contains(val.toUpperCase()))
            return false;
        
        return val;
    }
    
    public static <T,U> void zip(Collection<T> A, Collection<U> B, BiConsumer<T, U> c) {
        Iterator<T> it = A.iterator();
        Iterator<U> iu = B.iterator();
        while (it.hasNext() && iu.hasNext()) {
            c.accept(it.next(), iu.next());
        }
    }
    
}