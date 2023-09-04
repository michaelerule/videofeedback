/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.image.BufferedImage;
import static java.lang.System.currentTimeMillis;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newFixedThreadPool;
import java.util.concurrent.Future;
import javax.swing.JFrame;
import javax.swing.JPanel;
import static javax.swing.SwingUtilities.invokeAndWait;

/**
 *
 * @author mer49
 */
public class ScreenCap {
    
    // Screenshots are stale after...
    public static final int 
            STALE_MS   = 1000,
            TIMEOUT_MS = 200;
    
    private Robot robot = null;       // Robot does the capture
    public  Rectangle screenRect;     // Region to capture
    private final ExecutorService ex; // Executer makes capture async
            
    public ScreenCap() {
        ex         = newFixedThreadPool(1);
        screenRect = new Rectangle(getDefaultToolkit().getScreenSize());
        try { 
            robot = new Robot();
        } catch (AWTException ex) {
            // Fail silently for now
        }
    }
    
    /**
     * Take and encapsulate screenshot result.
     */
    private final class Screenshot {
        final BufferedImage image;
        final long time;
        public Screenshot() {
            image = robot.createScreenCapture(screenRect);
            time  = currentTimeMillis();
        }
    }

    /**
     * Return last screenshot if not stale and async call for next one.
     */
    private Screenshot done = null;
    private Future<Screenshot> pending = null;
    public synchronized BufferedImage getScreenshot() {
        // If there is no pending screenshot, or if the last queued screenshot
        // has completed (or failed), we need to start another one. 
        // Try to save the screenshot result in "done", if possible.
        if (null==pending || pending.isCancelled() || pending.isDone()) {
            if (null!=pending) try { 
                done = pending.get();
            } catch (InterruptedException | ExecutionException e) {}
            pending = ex.submit(()->{return new Screenshot();});
        }
        // If there is no completed screenshot (done is null), or if the
        // saved screenshot is too old (based on time stamps), take a new
        // screenshot now (synchronously). 
        if (null==done || currentTimeMillis()-done.time>STALE_MS) 
            done = new Screenshot();
        return done.image;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    static JPanel    p = null;
    static ScreenCap c = null;
    public static void main(String [] args) throws InterruptedException, InvocationTargetException, AWTException {
        
        c = new ScreenCap();
            
        invokeAndWait(()->{
            JFrame f = new JFrame("Capture Test");
            f.setLayout(new BorderLayout());
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            p = new JPanel() {
                public void paint(Graphics g) {
                    if (null==c) return;
                    BufferedImage image = c.getScreenshot();
                    if (null!=image) g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                }
            };
            p.setPreferredSize(new Dimension(640,480));

            f.add(p,BorderLayout.CENTER);
            f.pack();
            f.setVisible(true);
        });
        while (true) {
            long next = currentTimeMillis() + 50;
            //p.paint(g);
            //Graphics2D g = ColorUtil.fast((Graphics2D) p.getGraphics());
            //g.fillRect(0,0,p.getWidth(),p.getHeight());
            p.repaint();
            long delay = next - currentTimeMillis();
            if (delay>0) Thread.sleep(delay);
        }
    }
    
}
