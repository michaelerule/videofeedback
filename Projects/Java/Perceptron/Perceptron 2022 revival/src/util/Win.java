/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import perceptron.Perceptron;

/**
 *
 * @author mer49
 */
public class Win {

    /** Hide the mouse pointer, if possible
     * @param c */
    public static void hideCursor(Component c) {
        try {
            c.setCursor(getDefaultToolkit().createCustomCursor(getDefaultToolkit().getImage("xparent.gif"), new Point(0, 0), null));
        } catch (HeadlessException | IndexOutOfBoundsException e) {
            System.err.println("Cursor modification is unsupported.");
        }
    }

    /** Get the screen device's graphics configurations
     *
     * @return
     */
    public static GraphicsConfiguration getDeviceGraphicsConfig() {
        return ((getLocalGraphicsEnvironment()).getDefaultScreenDevice()).getDefaultConfiguration();
    }

    /** Make a JFrame the undecorated main application window
     * @param j
     */
    public static void makeUndecoratedMainFrame(JFrame j) {
        // Exit on window close
        try {
            j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception e) {
        }
        //ignore prompts for redrawing from the operating system
        try {
            j.setIgnoreRepaint(true);
        } catch (Exception e) {
        }
        //remove the frame decorations titlebar etc..
        //this frame is not to be re-sized
        try {
            j.dispose();
            j.setUndecorated(true);
            j.setResizable(false);
        } catch (Exception e) {
        }
    }

    /** Make windowed, "not fullscreen".
     */
    public static void makeNotFullscreen() {
        GraphicsDevice g = getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window) null);
        }
    }

    /** Set up full-screen mode.
     * @param p
     */
    public static void makeFullscreen(Perceptron p) {
        GraphicsDevice g = getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode d = g.getDisplayMode();
        if (g.isFullScreenSupported()) {
            g.setFullScreenWindow((Window) p);
            DisplayMode[] possible_modes = g.getDisplayModes();
            DisplayMode best_mode = null;
            for (DisplayMode m : possible_modes) {
                System.out.println(m.getBitDepth() + 
                        " " + m.getHeight() + 
                        " " + m.getRefreshRate() + 
                        " " + m.getWidth());
                if ((m.getWidth() >= p.screen_width 
                        && m.getHeight() >= p.screen_height 
                        && m.getBitDepth() >= Math.min(32, d.getBitDepth())) 
                        && (best_mode == null || m.getWidth() * m.getHeight() < best_mode.getWidth() * best_mode.getHeight())) {
                    best_mode = m;
                }
            }
            if (best_mode != null) {
                try {
                    g.setDisplayMode(best_mode);
                } catch (InternalError e) {
                } // Fail silently
            }
        } else {
            DisplayMode m = g.getDisplayMode();
            p.setBounds(0, 0, m.getWidth(), m.getHeight());
            p.setVisible(true);
        }
    }
    
    
    public static void toClipboard(String text) {
        getDefaultToolkit().getSystemClipboard()
            .setContents(new java.awt.datatransfer.StringSelection(text), null);
    }
    
    public static String fromClipboard() {
        try { 
            String s = (String)getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            return s.strip();
        } catch (UnsupportedFlavorException | IOException ex) {
            return "";
        }
    }
    
}