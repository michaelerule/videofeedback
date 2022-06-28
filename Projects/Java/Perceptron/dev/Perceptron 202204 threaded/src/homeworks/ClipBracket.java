/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package homeworks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.lang.Math.min;
import static java.lang.Math.max;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import static javax.swing.SwingUtilities.invokeAndWait;
import static util.Misc.clip;
import static util.Sys.sout;

/**
 *
 * @author mer49
 */
public class ClipBracket {
    
    public static int getBarWidth() {
        Dimension r = getDefaultToolkit().getScreenSize();
        int size = min(r.width, r.height);
        return clip(size/40,10,50);
    }
    
    static Point     where_down = null;
    static Rectangle bounds_ft  = null;
    static Rectangle bounds_fl  = null;
    static boolean   resize_ft  = false;

    public static void main(String []args) throws InterruptedException, InvocationTargetException {
    
        invokeAndWait(()->{
            
            int barwidth = getBarWidth();
            sout(barwidth);
            
            JFrame ft = new JFrame("Top");
            ft.setLayout(new BorderLayout());
            ft.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ft.setAlwaysOnTop(true);
            JPanel pt = new JPanel() {
                public void paint(Graphics g) {
                    super.paint(g);
                    int w = getWidth();
                    int h = getHeight();
                    g.setColor(Color.white);
                    // Draw top left
                    g.drawRect(0,0,h-1,h-1);
                    g.fillPolygon(new int[]{2,h-2,2,2},new int[]{2,2,h-2,2}, 4);
                    // Draw top right
                    int x0 = w-h;
                    g.drawRect(x0,0,h-1,h-1);
                    g.fillPolygon(new int[]{x0+2,x0+h-2,x0+2,x0+2},new int[]{2,2,h-2,2}, 4);
                }
            };
            pt.setBackground(Color.black);
            pt.setPreferredSize(new Dimension(480,barwidth));
            ft.add(pt,BorderLayout.CENTER);
            ft.setUndecorated(true);
            ft.pack();
            ft.setVisible(true);
            
            
            JFrame fl = new JFrame("Left");
            fl.setLayout(new BorderLayout());
            fl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fl.setAlwaysOnTop(true);
            JPanel pl = new JPanel() {
                public void paint(Graphics g) {
                    super.paint(g);
                    int w = getWidth();
                    int h = getHeight();
                    g.setColor(Color.white);
                    // Draw left bottom
                    int y0 = h-w;
                    g.drawRect(0,y0,w-1,w-1);
                    g.fillPolygon(new int[]{2,w-2,2,2},new int[]{y0+2,y0+2,y0+w-2,y0+2}, 4);
                }
            };
            pl.setBackground(Color.black);
            pl.setPreferredSize(new Dimension(barwidth,320));
            fl.add(pl,BorderLayout.CENTER);
            fl.setUndecorated(true);
            fl.pack();
            Point txy = ft.getLocationOnScreen();
            fl.setLocation(txy.x, txy.y + barwidth);
            fl.setVisible(true);
            
            
            
            MouseAdapter tadapter = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    where_down = e.getLocationOnScreen();
                    bounds_ft  = ft.getBounds();
                    bounds_fl  = fl.getBounds();
                    resize_ft  = e.getX() > pt.getWidth()-pt.getHeight();
                }
                public void mouseReleased(MouseEvent e) {
                    bounds_ft  = null;
                    bounds_fl  = null;
                    where_down = null;
                    resize_ft  = false;
                }
                public void mouseDragged(MouseEvent e) {
                    if (null==where_down||null==bounds_ft||null==bounds_fl) return;
                    Point where_now = e.getLocationOnScreen();
                    int dx = where_now.x - where_down.x;
                    int dy = where_now.y - where_down.y;
                    
                    Dimension r = getDefaultToolkit().getScreenSize();
                    
                    if (resize_ft) {

                        if (bounds_ft.x + bounds_ft.width + dx > r.width) {
                            dx = r.width-bounds_ft.width-bounds_ft.x;
                        }
                        ft.setBounds(bounds_ft.x, bounds_ft.y, max(bounds_ft.height*2-1,bounds_ft.width + dx), bounds_ft.height);
                    } else {

                        if (bounds_ft.x + bounds_ft.width + dx > r.width) {
                            dx = r.width-bounds_ft.width-bounds_ft.x;
                        }
                        if (bounds_ft.y + dy + barwidth + bounds_fl.height>r.height) {
                            dy = r.height - barwidth - bounds_fl.height - bounds_ft.y;
                        }
                        ft.setLocation(bounds_ft.x + dx, bounds_ft.y + dy);
                        
                        
                        Point pos = ft.getContentPane().getLocationOnScreen();
                        Rectangle r2 = ft.getContentPane().getBounds(); 
                        r2.x = pos.x;
                        r2.y = pos.y;
   
   
                        fl.setBounds(r2.x, r2.y+barwidth, barwidth, bounds_fl.height);
                        sout(r2 + ": " + dx);
                    }
                }
            };
            pt.addMouseListener(tadapter);
            pt.addMouseMotionListener(tadapter);
            //pl.addMouseListener(tadapter);
            //pl.addMouseMotionListener(tadapter);
        });
    }
}
