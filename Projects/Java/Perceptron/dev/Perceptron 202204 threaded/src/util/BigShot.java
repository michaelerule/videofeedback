/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import color.ColorUtil;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import static java.awt.Cursor.E_RESIZE_CURSOR;
import static java.awt.Cursor.MOVE_CURSOR;
import static java.awt.Cursor.SE_RESIZE_CURSOR;
import static java.awt.Cursor.S_RESIZE_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.awt.MouseInfo.getPointerInfo;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import javax.swing.ImageIcon;
import static javax.swing.SwingUtilities.invokeLater;
import static util.Misc.clip;
import static util.Sys.sout;
import static util.Sys.centerWindow;

/**
 *
 * @author mer49
 */
public class BigShot {
    
    private static final int   SPACE   = 45;
    private static final int   MINSIZE = 5*SPACE;
    private static final Color TINT_COLOR = new Color(48,0,255,80);
    private static final Color FILL_COLOR = new Color(103,88,168);
    
    public static BufferedImage blur(BufferedImage img) {
        float sigma = 4f;
        int K = (int)(sigma*3+.5f);
        int N = 2*K+1;
        float[] gaussian = new float[N];
        float sum = 0f;
        for (int i=0; i<N; i++) {
            float dx = (i-K)/sigma;
            gaussian[i] = (float)(exp(-.5f*dx*dx)/sqrt(2*PI)/sigma);
            sum += gaussian[i];
        }
        for (int i=0; i<N; i++) gaussian[i]/=sum;
        Kernel k1 = new Kernel(N, 1, gaussian);
        Kernel k2 = new Kernel(1, N, gaussian);
        img = (new ConvolveOp(k1)).filter(img, null);
        img = (new ConvolveOp(k2)).filter(img, null);
        return img;
    }
    
    private static Rectangle getBigScreen() {
        // Determine total area of all screens
        double minx,miny,maxx,maxy;
        minx=miny=Double.POSITIVE_INFINITY;
        maxx=maxy=Double.NEGATIVE_INFINITY;
        for (var g:getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle screen = g.getDefaultConfiguration().getBounds();
            minx = min(minx,screen.getMinX());
            miny = min(miny,screen.getMinY());
            maxx = max(maxx,screen.getMaxX());
            maxy = max(maxy,screen.getMaxY());
        }   
        int x = (int)minx,
            y = (int)miny,
            w = (int)(maxx-minx),
            h = (int)(maxy-miny);
        return new Rectangle(x,y,w,h);
    }
    
    private static abstract class WPanel extends JPanel implements MouseListener, MouseMotionListener {
        Point     mp=null;
        Point     lp=null;
        Rectangle wb=null;
        Rectangle lb=null;
        Rectangle ll=null;
        JFrame    W;
        int ex,ey;
        public WPanel(JFrame toMove, int CURSOR) {
            super();
            this.W = toMove;
            setOpaque(false);
            setCursor(getPredefinedCursor(CURSOR));
            setMinimumSize(  new Dimension(SPACE,SPACE));
            setPreferredSize(new Dimension(SPACE,SPACE));
            invokeLater(()->{
                addMouseListener(this);
                addMouseMotionListener(this);
            });
        }
        abstract void act(Point p);
        abstract void draw(Graphics g, int w, int h);
        private void clear() {mp=lp=null; wb=lb=ll=null; ex=ey=0;}
        public void mouseDragged (MouseEvent e) {act(e.getLocationOnScreen());}
        public void mouseClicked (MouseEvent e) {clear();}
        public void mouseReleased(MouseEvent e) {clear();}
        public void mouseMoved   (MouseEvent e) {clear();}
        public void mouseEntered (MouseEvent e) {}
        public void mouseExited  (MouseEvent e) {}
        public void mousePressed (MouseEvent e) {
            mp = e.getLocationOnScreen();
            wb = W.getBounds();
        }
        public void paintComponent(Graphics g) {
            draw(g,this.getWidth(),this.getHeight());
        }
        void to(int x, int y, int w, int h) {    
            lb = new Rectangle(x,y,w,h);
            W.setBounds(lb);
        }
    }
    private static class MovePanel extends WPanel {
        public MovePanel(JFrame toMove) {super(toMove,MOVE_CURSOR);}
        void act(Point p) {
            int x = wb.x+p.x-mp.x,
                y = wb.y+p.y-mp.y,
                w = wb.width,
                h = wb.height;
            Rectangle s = getBigScreen();
            int sx0 = s.x;
            int sy0 = s.y;
            int sx1 = s.x+s.width;
            int sy1 = s.y+s.height;
            x = clip(x,sx0,sx1-w);
            y = clip(y,sy0,sy1-h);
            to(x,y,w,h);
        }
        void draw(Graphics g, int w, int h){
            g.setColor(Color.white);
            g.drawRect(0,0,w-1,h-1);
            int w2=w/2, h2=h/2, d=min(w,h)/4;
            g.drawLine(w2-d,h2,w2+d,h2);
            g.drawLine(w2,h2-d,w2,h2+d);
            g.drawLine(w2-d,h2,w2,h2-d);
            g.drawLine(w2-d,h2,w2,h2+d);
            g.drawLine(w2+d,h2,w2,h2-d);
            g.drawLine(w2+d,h2,w2,h2+d);
        }
    }
    private static class ResizeSEPanel extends WPanel {
        public ResizeSEPanel(JFrame toMove) {super(toMove,SE_RESIZE_CURSOR);}
        public void act(Point p) {
            int x = wb.x,
                y = wb.y,
                w = wb.width+p.x-mp.x,
                h = wb.height+p.y-mp.y;
            Rectangle s = getBigScreen();
            int sx0 = s.x;
            int sy0 = s.y;
            int sx1 = s.x+s.width;
            int sy1 = s.y+s.height;
            if (x+w>sx1) w = max(MINSIZE, w - (x+w) + sx1 );
            if (y+h>sy1) h = max(MINSIZE, h - (y+h) + sy1 );
            /*
            Sometimes the region where windows are allowed to be is not the
            same as the screen area. Clipping won't save us here. 
            We can assume that if we're in this block of code, we're in the
            middle of a resize mouseDragged operation. The only reason the
            (x,y) position of the window should change is if we've made the
            window too large and the window manager has moved it to bring it
            back in-screen. We don't want this. Interpret x and y discrepency
            as an error that we need to accomodate. Problem: once we fix this
            error, it won't appear on the next move. The error will need to be
            persistent and integrate over time. Yes, this works!
            */
            Rectangle cb = W.getBounds();
            ex += wb.x - cb.x;
            ey += wb.y - cb.y;
            w -= ex;
            h -= ey;
            to(x,y,w,h);
        }
        public void draw(Graphics g, int w, int h){
            g.setColor(Color.white);
            g.drawLine(0,0,0,h);
            g.drawLine(0,0,w,0);
            int w2=w/2, h2=h/2, d=(int)(.5+min(w,h)/4/sqrt(2)), D=(int)(.5+min(w,h)/9*sqrt(2));
            g.drawLine(w2-d,h2-d,w2+d,h2+d);
            g.drawLine(w2+d,h2+d,w2+d,h2+d-D);
            g.drawLine(w2+d,h2+d,w2+d-D,h2+d);
        }
    }
    private static class ResizeEPanel extends WPanel {
        public ResizeEPanel(JFrame toMove) {super(toMove,E_RESIZE_CURSOR);}
        public void act(Point p) {
            int x = wb.x,
                y = wb.y,
                w = wb.width+p.x-mp.x,
                h = wb.height;
            Rectangle s = getBigScreen();
            int sx0 = s.x;
            int sy0 = s.y;
            int sx1 = s.x+s.width;
            int sy1 = s.y+s.height;
            if (x+w>sx1) w = max(MINSIZE, w - (x+w) + sx1 );
            if (y+h>sy1) h = max(MINSIZE, h - (y+h) + sy1 );
            Rectangle cb = W.getBounds();
            ex += wb.x - cb.x;
            ey += wb.y - cb.y;
            w -= ex;
            h -= ey;
            to(x,y,w,h);
        }
        public void draw(Graphics g, int w, int h){
            g.setColor(Color.white);
            g.drawLine(0,0,0,h);
            int w2=w/2, h2=h/2, d=min(w,h)/4, D=min(w,h)/9;
            g.drawLine(w2-d,h2,w2+d,h2);
            g.drawLine(w2+d,h2,w2+d-D,h2+D);
            g.drawLine(w2+d,h2,w2+d-D,h2-D);
        }
    }
    private static class ResizeSPanel extends WPanel {
        public ResizeSPanel(JFrame toMove) {super(toMove,S_RESIZE_CURSOR);}
        public void act(Point p) {
            int x = wb.x,
                y = wb.y,
                w = wb.width,
                h = wb.height+p.y-mp.y;
            Rectangle s = getBigScreen();
            int sx0 = s.x;
            int sy0 = s.y;
            int sx1 = s.x+s.width;
            int sy1 = s.y+s.height;
            if (x+w>sx1) w = max(MINSIZE, w - (x+w) + sx1 );
            if (y+h>sy1) h = max(MINSIZE, h - (y+h) + sy1 );
            Rectangle cb = W.getBounds();
            ex += wb.x - cb.x;
            ey += wb.y - cb.y;
            w -= ex;
            h -= ey;
            to(x,y,w,h);
        }
        public void draw(Graphics g, int w, int h){
            g.setColor(Color.white);
            g.drawLine(0,0,w,0);
            int w2=w/2, h2=h/2, d=min(w,h)/4, D=min(w,h)/9;
            g.drawLine(w2,h2-d,w2,h2+d);
            g.drawLine(w2-D,h2+d-D,w2,h2+d);
            g.drawLine(w2+D,h2+d-D,w2,h2+d);
        }
    }
    
        
    JFrame s = null;
    JFrame z = null;
    BufferedImage img = null;
                
    public BigShot() {
        System.setProperty("sun.awt.noerasebackground", "true");
        prepareWatcher();
        prepareSelector();
        s.pack();
        z.pack();        
        // Move selection window to center of screen 0 and show
        Rectangle b = getPointerInfo().getDevice().getDefaultConfiguration().getBounds();
        z.setLocation(b.x,b.y);
        Rectangle zb = z.getBounds();
        s.setLocation(b.x,b.y+zb.height+5);
        s.setVisible(false);
        z.setVisible(true);
    }
    
    private void prepareWatcher() {
        ////////////////////////////////////////////////////////////////////////
        // Prepare a button-window to re-select capture region
        z = new JFrame("watcher");
        z.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        z.setUndecorated(true);
        z.setAlwaysOnTop(true);
        z.setIconImage(new ImageIcon("resource/data/icon2.png").getImage());
        // Button to change capture region
        JButton b = new JButton(new AbstractAction("Click to change capture region") {
            public void actionPerformed(ActionEvent e) {
                z.setVisible(false);
                try {locate();} catch (AWTException ex) {z.setVisible(true);}
            }
        });
        b.setBackground(Color.black);
        b.setForeground(Color.white);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(320,SPACE));
        // Move panel
        MovePanel m = new MovePanel(z);
        // Message to user
        var l = new JLabel("Screen view: set w←{3,4} and press n",JLabel.CENTER);
        l.setBackground(Color.black);
        l.setForeground(Color.white);
        l.addMouseListener(m);
        l.addMouseMotionListener(m);
        // Put it all together
        Container cp = z.getContentPane();
        cp.setBackground(Color.black);
        cp.setForeground(Color.white);        
        cp.setLayout(new BorderLayout());
        cp.add(b,BorderLayout.CENTER);
        cp.add(l,BorderLayout.NORTH);
        cp.add(m,BorderLayout.WEST);
    }
    
    private void prepareSelector() {
        ////////////////////////////////////////////////////////////////////////
        // Make a selection window
        s = new JFrame("selector");
        s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        s.setUndecorated(true);
        s.setAlwaysOnTop(true);
        s.setResizable(false);
        s.setMinimumSize(new Dimension(MINSIZE,MINSIZE));
        s.setBackground(FILL_COLOR);
        s.setIconImage(new ImageIcon("resource/data/icon2.png").getImage());
        // Make JPanel that shines through without using transparency
        JPanel c = new JPanel(new BorderLayout()) {
            public void paintComponent(Graphics g) {
                // Draw see-through image
                int w = this.getWidth(), h = this.getHeight();
                Point pwin = s.getLocationOnScreen();
                Point ppan = s.getRootPane().getLocation();
                int x0 = pwin.x+ppan.x, y0 = pwin.y+ppan.y;
                g.drawImage(img,0,0,w,h,x0,y0,x0+w,y0+h,null);
                // Draw instructions to user
                g = (Graphics)ColorUtil.fancy((Graphics2D)g);
                g.setColor(Color.white);
                String s1 = "Move this over area to capture";
                String s2 = "Then click here";
                var fm = g.getFontMetrics();
                int sw1 = (int)fm.getStringBounds(s1,g).getWidth();
                int sh1 = (int)fm.getStringBounds(s1,g).getHeight();
                int sw2 = (int)fm.getStringBounds(s2,g).getWidth();
                int sh2 = (int)fm.getStringBounds(s2,g).getHeight();
                int sh  = sh1+sh2;
                int ty  = (h-sh)/2;
                g.drawString(s1, (w-sw1)/2, ty);
                g.drawString(s2, (w-sw2)/2, ty+sh1);
            }
        };
        // Prepare screen capture region selection window
        Dimension size = new Dimension(320,240);
        c.setBackground(FILL_COLOR);
        c.setPreferredSize(size);
        c.setOpaque(false);
        s.setContentPane(c);
        addSelectorListeners(c);
        addSelectorHandles(c);
    }
    
    private void addSelectorListeners(JPanel c) {
        // Redraw window for anything that could change transparency
        // The system won't redraw when we move the window
        // We need to do it ourselves
        s.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {}//invokeLater(c::repaint);}
            public void componentMoved  (ComponentEvent e) {invokeLater(c::repaint);}
            public void componentShown  (ComponentEvent e) {invokeLater(c::repaint);}
            public void componentHidden (ComponentEvent e) {invokeLater(c::repaint);}
        });
        s.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                sout("click "+e);
                float x = (float)e.getX()/c.getWidth();
                float y = (float)e.getY()/c.getHeight();
                if (x>.25f&&x<.75f&&y>.25f&&y<.75f) {
                    s.setVisible(false);
                    z.setVisible(true);
                }
            }
        });
    }
    
    private void addSelectorHandles(JPanel c) {
        // Button to drag the window
        JPanel centr = new JPanel(new BorderLayout());
        c.add(centr,BorderLayout.CENTER);
        JPanel north = new JPanel(new BorderLayout());
        north.add(new MovePanel(s),BorderLayout.WEST);
        centr.add(north,BorderLayout.NORTH);
        // Button to resize the window south east
        JPanel south = new JPanel(new BorderLayout());
        south.add(new ResizeSEPanel(s),BorderLayout.EAST);
        // Button to resize the window south
        south.add(new ResizeSPanel(s),BorderLayout.CENTER);
        c.add(south,BorderLayout.SOUTH);
        // Button to resize the window south east
        c.add(new ResizeEPanel(s),BorderLayout.EAST);
        centr.setOpaque(false);
        north.setOpaque(false);
        south.setOpaque(false);
    }
    
    public void locate() throws AWTException {
        // Capture an image of everything, blur, tint it
        img = blur(new Robot().createScreenCapture(getBigScreen()));
        var g = img.createGraphics();
        g.setColor(TINT_COLOR);
        g.fillRect(0,0,img.getWidth(),img.getHeight());
        s.setVisible(true);
    }
    
    public Rectangle getBounds() {
        return s.getBounds();
    }
    
    public static void main(String [] args) throws AWTException {
        BigShot b = new BigShot();//.locate();
    }
    
}
