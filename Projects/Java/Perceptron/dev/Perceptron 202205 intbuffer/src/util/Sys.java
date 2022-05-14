/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import static java.lang.Math.min;
import javax.swing.JFrame;

/**
 *
 * @author mer49
 */
public class Sys {
    
    public static final Cursor 
        CROSS = new Cursor(Cursor.CROSSHAIR_CURSOR),
        NONE  = getDefaultToolkit().createCustomCursor(
                    getDefaultToolkit().getImage("xparent.gif"), 
                    new Point(0, 0), 
                    null);

    /** 
     * Get the screen device's graphics configurations
     * @return
     */
    public static GraphicsConfiguration getDeviceGraphicsConfig() {
        return ((getLocalGraphicsEnvironment()).getDefaultScreenDevice()).getDefaultConfiguration();
    }


    /** 
     * Make windowed, "not fullscreen".
     */
    public static void makeNotFullscreen(JFrame j) {
        GraphicsDevice g = getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (g.isFullScreenSupported()) g.setFullScreenWindow((Window)null);
    }
    
    /** Set up full-screen mode.
     * @param j
     * @param w
     * @param h
     * @return 
     */
    public static boolean makeFullscreen(JFrame j, int w, int h) {
        GraphicsEnvironment v = getLocalGraphicsEnvironment();
        GraphicsDevice      g = v.getDefaultScreenDevice();
        DisplayMode         d = g.getDisplayMode();
        if (g.isFullScreenSupported()) {
            j.dispose();
            j.setUndecorated(true);
            j.pack();
            j.setVisible(true);
            g.setFullScreenWindow((Window)j);
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
                if (best != null) g.setDisplayMode(best);
            }
            return true;
        } 
        //fakeFullscreen(j);
        return false;
    }
    
    /**
     * Fake it til you make it. Or just fake it, if you can't make it.
     * @param j 
     */
    public static void fakeFullscreen(JFrame j) {
        GraphicsDevice g = getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode    d = g.getDisplayMode();
        j.setBounds(0, 0, d.getWidth(), d.getHeight());
        j.setVisible(true);
    }
    
    /**
     * Send text to system clipboard.
     * @param text 
     */
    public static void setClip(String text) {
        getDefaultToolkit().getSystemClipboard()
            .setContents(new java.awt.datatransfer.StringSelection(text), null);
    }
    
    /**
     * Try to get text from system clipboard.
     * @return 
     */
    public static String getClip() {
        try { 
            String s = (String)getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            return s.strip();
        } catch (UnsupportedFlavorException | IOException ex) {
            return "";
        }
    }
    
    public static boolean mouseIn(Component c) {
        Point     m = MouseInfo.getPointerInfo().getLocation();
        Rectangle b = c.getBounds();
        b.setLocation(c.getLocationOnScreen());
        return b.contains(m);
    }
    
    /**
     * In 2022, System.out.println is entirely unacceptable.
     * @param o 
     */
    public static void sout(Object o) {
        java.lang.System.out.println(o.toString());
    }
    
    /**
     * In 2022, System.err.println is entirely unacceptable.
     * @param o 
     */
    public static void serr(Object o) {
        java.lang.System.err.println(o.toString());
    }
    
}