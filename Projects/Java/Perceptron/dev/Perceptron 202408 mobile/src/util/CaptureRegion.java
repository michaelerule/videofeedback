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

/**
 * GUI window used to select the screen capture region. 
 * @author mer49
 */
public class CaptureRegion {
    
    // Minimize width and height of capture region, in pixels
    private static final int   SPACE      = 45;
    private static final int   MINSIZE    = 5*SPACE;
    private static final Color TINT_COLOR = new Color(48,0,255,80);
    private static final Color FILL_COLOR = new Color(103,88,168);
    
    /**
     * We simulate translucency by hiding the window, capturing the screen,
     * and then displaying a blurred and tinted version of this capture in 
     * the background. Some JREs do support real transparency, but not JDK as
     * of 2023.
     * @param img
     * @return 
     */
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
    
    /**
     * Infer the screen capture rectangle required to capture everything,
     * even across a multi-monitor setup. 
     * @return 
     */
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
    
    /** A JPanel acting as a button to drag and reposition a window.
     */
    private static abstract class WPanel extends JPanel implements MouseListener, MouseMotionListener {
        JFrame    W; // Window this button acts upon
        // State required to correctly process mouse dragging
        Point     mp=null; // Location of last mouse down
        Point     lp=null;
        Rectangle wb=null; 
        Rectangle lb=null; // Bounds of window "W"
        Rectangle ll=null;
        int       ex;
        int       ey;
        public WPanel(JFrame toMove, int CURSOR) {
            super();
            this.W = toMove;
            // Show parent component through background
            setOpaque(false);
            // The mouse cursor will hint at the WPanel's action (move, resize, etc)
            setCursor(getPredefinedCursor(CURSOR));
            // This is the minimum size of the WPanel as a button
            setMinimumSize(  new Dimension(SPACE,SPACE));
            setPreferredSize(new Dimension(SPACE,SPACE));
            // Register event listeners
            invokeLater(()->{
                addMouseListener(this);
                addMouseMotionListener(this);
            });
        }
        abstract void act(Point p); // React to mouse-event location
        abstract void draw(Graphics g, int w, int h); // Render button
        // Clear the move/drag state memory
        private void clear() {mp=lp=null; wb=lb=ll=null; ex=ey=0;}
        // When dragged, act upon moving mouse location
        public void mouseDragged (MouseEvent e) {act(e.getLocationOnScreen());}
        // Detect all events signally mouse button release and stop tracking
        public void mouseClicked (MouseEvent e) {clear();}
        public void mouseReleased(MouseEvent e) {clear();}
        public void mouseMoved   (MouseEvent e) {clear();}
        // It's common for the mouse to momentarily leave the window when 
        // dragging or resizing, so we don't react to enter/exit events
        public void mouseEntered (MouseEvent e) {}
        public void mouseExited  (MouseEvent e) {}
        // A mouse press indicates a drag action has started. 
        // We store the mouse down location and current window bounds
        public void mousePressed (MouseEvent e) {
            mp = e.getLocationOnScreen();
            wb = W.getBounds();
        }
        // Paint calls the draw routine, providing the component size.
        public void paintComponent(Graphics g) {
            draw(g,this.getWidth(),this.getHeight());
        }
        // Explicitly set bounds of the window we control.
        void to(int x, int y, int w, int h) {    
            lb = new Rectangle(x,y,w,h);
            W.setBounds(lb);
        }
    }
    
    /** A button to drag/move/reposition a window (the diamond).
     */
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
            // Draw a diamond shape
            g.drawLine(w2-d,h2,w2+d,h2);
            g.drawLine(w2,h2-d,w2,h2+d);
            g.drawLine(w2-d,h2,w2,h2-d);
            g.drawLine(w2-d,h2,w2,h2+d);
            g.drawLine(w2+d,h2,w2,h2-d);
            g.drawLine(w2+d,h2,w2,h2+d);
        }
    }
    
    /** Button to resize window, south-east corner. */
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
            as an error that we need to accomodate.
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
    
    /** Button to resize window, east edge. */
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
    
    /** Button to resize window, south edge. */
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
    
        
    public JFrame selector = null;
    public JFrame watcher  = null;
    public BufferedImage img = null;
    public int width  = MINSIZE;
    public int height = MINSIZE;
                
    public CaptureRegion(int width, int height) {
        this.width  = width;
        this.height = height;
        System.setProperty("sun.awt.noerasebackground", "true");
        // Watcher: small window indicating active screen acpture
        // Selector: adjustable faux-translucent window to set capture region
        prepareWatcher();
        prepareSelector();
        selector.pack();
        watcher.pack();        
        // Discover which screen we're on and get its bounding box
        Rectangle b = getPointerInfo().getDevice().getDefaultConfiguration().getBounds();
        // Put the "watcher" window in the top left
        watcher.setLocation(b.x,b.y);
        // Intially, place the selector window immediately below the watcher
        Rectangle zb = watcher.getBounds();
        selector.setLocation(b.x,b.y+zb.height+5);
        selector.setVisible(false);
        watcher.setVisible(true);
    }
    public CaptureRegion() {this(MINSIZE,MINSIZE);}
    
    /**
     * Create a smaller floating window, indicating that screen capture is
     * active, and providing buttons to change the screen capture region.
     */
    private void prepareWatcher() {
        ////////////////////////////////////////////////////////////////////////
        // Prepare a button-window to re-select capture region
        watcher = new JFrame("watcher");
        watcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        watcher.setUndecorated(true);
        watcher.setAlwaysOnTop(true);
        watcher.setIconImage(new ImageIcon("resource/data/icon2.png").getImage());
        // Button to change capture region
        JButton b = new JButton(new AbstractAction("Click to change capture region") {
            public void actionPerformed(ActionEvent e) {
                watcher.setVisible(false);
                try {locate();} catch (AWTException ex) {watcher.setVisible(true);}
            }
        });
        b.setBackground(Color.black);
        b.setForeground(Color.white);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(320,SPACE));
        // Move panel
        MovePanel m = new MovePanel(watcher);
        // Message to user
        var l = new JLabel("Screen view: set w←{3,4} and press n",JLabel.CENTER);
        l.setBackground(Color.black);
        l.setForeground(Color.white);
        l.addMouseListener(m);
        l.addMouseMotionListener(m);
        // Put it all together
        Container cp = watcher.getContentPane();
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
        selector = new JFrame("selector");
        selector.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        selector.setUndecorated(true);
        selector.setAlwaysOnTop(true);
        selector.setResizable(false);
        selector.setMinimumSize(new Dimension(MINSIZE,MINSIZE));
        selector.setBackground(FILL_COLOR);
        selector.setIconImage(new ImageIcon("resource/data/icon2.png").getImage());
        // Make JPanel that shines through without using transparency
        JPanel c = new JPanel(new BorderLayout()) {
            public void paintComponent(Graphics g) {
                // Draw see-through image
                int w = this.getWidth(), h = this.getHeight();
                Point pwin = selector.getLocationOnScreen();
                Point ppan = selector.getRootPane().getLocation();
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
        Dimension size = new Dimension(width,height);
        c.setBackground(FILL_COLOR);
        c.setPreferredSize(size);
        c.setOpaque(false);
        selector.setContentPane(c);
        addSelectorListeners(c);
        addSelectorHandles(c);
    }
    
    private void addSelectorListeners(JPanel c) {
        // Redraw window for anything that could change transparency
        // The system won't redraw when we move the window
        // We need to do it ourselves
        selector.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {}//invokeLater(c::repaint);}
            public void componentMoved  (ComponentEvent e) {invokeLater(c::repaint);}
            public void componentShown  (ComponentEvent e) {invokeLater(c::repaint);}
            public void componentHidden (ComponentEvent e) {invokeLater(c::repaint);}
        });
        selector.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                sout("click "+e);
                float x = (float)e.getX()/c.getWidth();
                float y = (float)e.getY()/c.getHeight();
                if (x>.25f&&x<.75f&&y>.25f&&y<.75f) {
                    selector.setVisible(false);
                    watcher.setVisible(true);
                }
            }
        });
    }
    
    /** Add the buttons that move/resize to the selector window
     * @param c 
     */
    private void addSelectorHandles(JPanel c) {
        JPanel centr = new JPanel(new BorderLayout()); centr.setOpaque(false);
        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        JPanel south = new JPanel(new BorderLayout()); south.setOpaque(false);
        
        // Making the north (top) buttons par of the center panel
        // ensures that the east panel will extend to the top of the window.
        north.add(new MovePanel(selector),BorderLayout.WEST);
        
        // Button to match perceptron pixel size
        JButton matchSize = new JButton(
            new AbstractAction("Click to match Perceptron size") {
            public void actionPerformed(ActionEvent e) {
                selector.setBounds(selector.getX(), selector.getY(), width, height);
            }});
        matchSize.setOpaque(false);
        matchSize.setContentAreaFilled(false);
        
        //b.setBackground(Color.black);
        matchSize.setForeground(Color.white);
        
        north.add(matchSize);            
        
        centr.add(north,BorderLayout.NORTH);
        south.add(new ResizeSPanel(selector),BorderLayout.CENTER);
        south.add(new ResizeSEPanel(selector),BorderLayout.EAST);
        c.add(centr,BorderLayout.CENTER);
        c.add(south,BorderLayout.SOUTH);
        c.add(new ResizeEPanel(selector),BorderLayout.EAST);
    }
    
    public void locate() throws AWTException {
        // Capture an image of everything, blur, tint it
        img = blur(new Robot().createScreenCapture(getBigScreen()));
        var g = img.createGraphics();
        g.setColor(TINT_COLOR);
        g.fillRect(0,0,img.getWidth(),img.getHeight());
        selector.setVisible(true);
    }
    
    public Rectangle getBounds() {
        return selector.getBounds();
    }
    
    public static void main(String [] args) throws AWTException {
        CaptureRegion b = new CaptureRegion();//.locate();
    }
    
}
