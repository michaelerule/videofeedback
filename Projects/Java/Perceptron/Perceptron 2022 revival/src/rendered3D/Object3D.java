package rendered3D;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author mer49
 */
public class Object3D {

    /**
     *
     */
    public static final double RADIXPRECISION = 100;
                                    
    /**
     *
     */
    public static class RotateablePoint3D implements Point3D {
        
        /**  */
        public double x,y,z;
        
        /**
         * @param X
         * @param Y
         * @param Z
         */
        public RotateablePoint3D(double X, double Y, double Z) {
            x = X;
            y = Y;
            z = Z;
        }

        /**
         * @param k
         */
        public RotateablePoint3D(StringTokenizer k)  {
            this(Double.parseDouble(k.nextToken()),
                Double.parseDouble(k.nextToken()),
                Double.parseDouble(k.nextToken()));
        }
        
        /**
         * @param p
         */
        public RotateablePoint3D(Point3D p) {
            x = p.getx();
            y = p.gety();
            z = p.getz();
        }
        
        /**
         * @return
         */
        @Override
        public Point3D clone2() {
            return new RotateablePoint3D(this);
        }
        
        /**
         * @return
         */
        @Override
        public double getx() {
            return x;
        }

        /**
         * @return
         */
        @Override
        public double gety() {
            return y;
        }

        /**
         * @return
         */
        @Override
        public double getz() {
            return z;
        }

        /**
         * @param n
         */
        public void setx(double n) {
            x = n;
        }

        /**
         * @param n
         */
        public void sety(double n) {
            y = n;
        }

        /**
         * @param n
         */
        public void setz(double n) {
            z = n;
        }
        
        /**
         * @return
         */
        @Override
        public String toString() {
            return "R: <"+super.toString().substring(27)+" "+x+", "+y+", "+z+">";
        }
    }                                
    
    /**
     *
     */
    public static class InterpolatedPoint3D implements Point3D {
        private Point3D [] points;
        private double [] dd;

        /** 3D point coordinates */
        public double x, y, z;
        
        /**
         * @param p
         */
        public InterpolatedPoint3D(Point3D [] p) {
            points = p;
            initd();
            update();
        }
        
        final void initd() {
            dd = new double[points.length];
            double sum = 0;
            for (int i = 0; i < dd.length; i++) {
                dd[i] = 1;//Math.random();
                sum += dd[i];
            }
            sum = dd.length / sum;
            for (int i = 0; i < dd.length; i++) dd[i] *= sum;
        }
        
        /**
         * @param p1
         * @param p2
         */
        public InterpolatedPoint3D(Point3D p1, Point3D p2) {
            points = new Point3D[2];
            points[0] = p1;
            points[1] = p2;initd();
            update();
        }
        
        /**
         * @param k
         * @param allpoints
         */
        @SuppressWarnings("unchecked")
        public InterpolatedPoint3D(StringTokenizer k, ArrayList allpoints) {
            ArrayList newpoints = new ArrayList();
            while (k.hasMoreTokens()) 
                    newpoints.add(allpoints.get(Integer.parseInt(k.nextToken())-1));
            points = new Point3D[newpoints.size()];
            for (int i = 0; i < points.length; i++)
                    points[i] = (Point3D)(newpoints.get(i));
            initd();
        }
        
        /**
         * @return
         */
        @Override
        public Point3D clone2() {
            return new InterpolatedPoint3D(points);
        }
        
        /**
         * @param newpoints
         * @param point
         * @return
         */
        public Point3D createclone(ArrayList newpoints, Point3D[] point) {//clonedrotatedpoints, ArrayList clonedinterpolatedpoints) {
            Point3D [] newclonedpoints = new Point3D[points.length];
            for (int i = 0; i < points.length; i++) {
                for (int j = 0; j < point.length; j++) {
                    if (point[j] == points[i]) {
                        newclonedpoints[i] = (Point3D)(newpoints.get(j));//clonedrotatedpoints.get(j));
                        break;
                    }
                }
            }
            return new InterpolatedPoint3D(newclonedpoints);
        }
        
        /**  */
        public final void update() {
            x = 0; y = 0; z = 0;
            for (int i = 0; i < points.length; i++) {
                //Sxystem.out.println(points[i]);
                x += dd[i]*points[i].getx();
                y += dd[i]*points[i].gety();
                z += dd[i]*points[i].getz();
            }
            x /= points.length;
            y /= points.length;
            z /= points.length;
            //Sxystem.out.println(x+" "+y+" "+z);
        }
        
        /**
         * @return
         */
        @Override
        public double getx() {
            return x;
        }

        /**
         * @return
         */
        @Override
        public double gety() {
            return y;
        }

        /**
         * @return
         */
        @Override
        public double getz() {
            return z;
        }
        
        /**
         * @return
         */
        @Override
        public String toString() {
            String s = "I: <"+super.toString().substring(29);
            for (Point3D point1 : points) 
                s += " " + point1.toString();
            return s+">";
        }
    }
    
    /**
     *
     */
    public class Generic3D {

        int depth;
        int r,g,b,a,rgb;
        
        public int setDepth(int d) { return depth = d; }
        public int depth() { return depth; }
        public Color getColor() { return new Color(r,g,b,a); }
        public int getUnshadedRGBA() { return rgb; }
        
        /**
         * @param c
         */
        public void setColor(Color c) {
            r = c.getRed();
            g = c.getGreen();
            b = c.getBlue();
            a = c.getAlpha();
            rgb = 65536*r+256*g+b;
        }
        
        /**
         * @param r
         * @param g
         * @param b
         * @param a
         */
        public void setColor(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            rgb = 65536*r+256*g+b;
        }
        
        /**
         * @return
         */
        public int[] getRGBA() {
            int [] temp = {r,g,b,a};
            return temp;
        }
        
        /**
         * @return
         */
        public int getComponentRGBA() {
            double s = (double)depth/max_d;
            return (new Color(
                    (int)(s*r),
                    (int)(s*g),
                    (int)(s*b),
                    a)).getRGB();
        }
        
        /**
         * @param G
         */
        public void applyColor(Graphics G) {            
        try{
            double s = Math.max(0,(double)depth/max_d);
            G.setColor(
                new Color(
                    (int)(s*r),
                    (int)(s*g),
                    (int)(s*b),
                    a));}catch(Exception e){}
        }
    }
    
    /**
     *
     */
    public class StationaryImage3D extends Generic3D implements Shape3D {
        Point3D p;
        Image img;
        
        /**
         * @param p
         * @param b
         */
        public StationaryImage3D(Point3D p, Image b) {
            this.p = p;
            this.img = b;
        }
        
        /**
         * @param k
         * @param a
         */
        public StationaryImage3D(StringTokenizer k, ArrayList a) {
            p = (Point3D)(a.get(Integer.parseInt(k.nextToken())-1));
            img = (new ImageIcon(Object3D.class.getResource(k.nextToken()))).getImage();
        }
        
        @Override
        public int setDepth() { return depth = (int)(p.getz()*RADIXPRECISION); }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return new StationaryImage3D(correspondingpoint(p,oldpoints,newpoints),img);
        }
        
        /**
         * @param g
         * @param i
         */
        @Override
        public void draw(Graphics g, BufferedImage i) {
            g.drawImage(img,(int)p.getx(),(int)p.gety(),null);
        }
    }
    
    /**
     *
     */
    public class RotateableText3D extends Generic3D implements Shape3D {
    
        Point3D p;
        String message;
        
        /**
         * @param n
         * @param rgb
         * @param s
         */
        public RotateableText3D(Point3D n, int rgb, String s) {
            p = n;
            message = s;
            super.setColor(new Color(rgb));
        }
        
        /**
         * @param n
         * @param c
         * @param s
         */
        public RotateableText3D(Point3D n, Color c, String s) {
            p = n;
            super.setColor(c);
        }
        
        /**
         * @param k
         * @param a
         */
        public RotateableText3D(StringTokenizer k, ArrayList a) {
            p = (Point3D)(a.get(Integer.parseInt(k.nextToken())-1));
            message = k.nextToken();
            super.setColor(k.nextToken() != null? parseColor(k) : Color.WHITE);
        }
        
        /**
         * @return
         */
        @Override
        public int setDepth() { return depth = (int)(p.getz()*RADIXPRECISION); }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return new RotateableText3D(correspondingpoint(p,oldpoints,newpoints),getUnshadedRGBA(),message);
        }
        
        /**
         * @param g
         * @param b
         */
        @Override
        public void draw(Graphics g, BufferedImage b) {
            applyColor(g);
            g.drawString(message,(int)p.getx(),(int)p.gety());
        }
    }
    
    /**
     *
     */
    public class ColoredPoint3D extends Generic3D implements Shape3D {
        Point3D p;
        
        /**
         * @param n
         * @param rgb
         */
        public ColoredPoint3D(Point3D n, int rgb) {
            p = n;
            super.setColor(new Color(rgb));
        }

        /**
         * @param n
         * @param c
         */
        public ColoredPoint3D(Point3D n, Color c) {
            p = n;
            super.setColor(c);
        }
        
        /**
         * @param k
         * @param a
         */
        public ColoredPoint3D(StringTokenizer k, ArrayList a) {
            p = (Point3D)(a.get(Integer.parseInt(k.nextToken())-1));
            super.setColor(parseColor(k));
        }
        
        /**
         * @return
         */
        @Override
        public int setDepth() { return depth = (int)(p.getz()*RADIXPRECISION); }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return new ColoredPoint3D(correspondingpoint(p,oldpoints,newpoints),getUnshadedRGBA());
        }
        
        /**
         * @param g
         * @param b
         */
        @Override
        public void draw(Graphics g, BufferedImage b) {
            try{b.setRGB((int)p.getx(),(int)p.gety(),getComponentRGBA());}catch(Exception e){}
        }
    }
    
    /**
     *
     */
    public class PointSphere3D extends ColoredPoint3D implements Shape3D {
        Point3D [] surfacePoint;
        
        /**
         * @param k
         * @param a
         * @param s
         */
        @SuppressWarnings("unchecked")
        public PointSphere3D(StringTokenizer k, ArrayList a, ArrayList s) {
            super(k,a);
            
            double pi2 = Math.PI/2;
            double phi;// = -pi2;
            double theta;// = 0;
            double R = Double.parseDouble(k.nextToken());
            //double f  = (Math.sqrt(5)-1)/2;
            double surface = Math.PI*R*R/40;
            for (int x = 0; x < surface; x++)
            {
                theta = 2*Math.PI*Math.random();
                phi = Math.acos(2*Math.random()-1)-pi2;
                Point3D temp = new RotateablePoint3D(
                    p.getx()+R*Math.cos(theta)*Math.cos(phi),
                    p.gety()+R*Math.sin(phi),
                    p.getz()+R*Math.sin(theta)*Math.cos(phi));
                s.add(new ColoredPoint3D(temp,super.getUnshadedRGBA()));
                a.add(temp);
            }
        }
        
        /**
         * @param g
         * @param b
         */
        @Override
        public void draw(Graphics g, BufferedImage b){}
    }
        
    /**
     * 
     * @param b
     * @return 
     */
    private static BufferedImage getImage(String b) {
        Image i = (new ImageIcon(Object3D.class.getResource(b))).getImage();
        BufferedImage image = new BufferedImage(
                i.getWidth(null),
                i.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(i,0,0,null);
        return image;
    }

    /**
     *
     */
    public class Image3D extends Generic3D implements Shape3D {
        int w,h;
        Point3D point0, point1, point2, point3;
        int[][] RGB;
        
        /**
         * @param p
         * @param b
         */
        public Image3D(Point3D[] p, BufferedImage b) {
            point0 = p[0];
            point1 = p[1];
            point2 = p[2];
            point3 = p[3];
            w = b.getWidth(null);
            h = b.getHeight(null);
            initRGB(b);
        }
        
        /**
         * @param p
         * @param b
         */
        public Image3D(Point3D[] p, String b) {
            this(p,getImage(b));
        }
        
        /**
         * @param p0
         * @param p1
         * @param p2
         * @param p3
         * @param newrgb
         */
        public Image3D(Point3D p0,Point3D p1,Point3D p2,Point3D p3,int [][] newrgb) {
            point0 = p0;
            point1 = p1;
            point2 = p2;
            point3 = p3;
            RGB = newrgb;
        }
        
        /**
         * @param k
         * @param p
         */
        public Image3D(StringTokenizer k, ArrayList p) {
            point0 = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            point1 = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            point2 = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            point3 = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            Image i = (new ImageIcon(Object3D.class.getResource(k.nextToken()))).getImage();
            BufferedImage image = new BufferedImage(i.getWidth(null),i.getHeight(null),BufferedImage.TYPE_INT_ARGB);
            (image.getGraphics()).drawImage(i,0,0,null);
            w = image.getWidth(null);
            h = image.getHeight(null);
            initRGB(image);
        }
        
        /**
         * @param image
         */
        public final void initRGB(BufferedImage image) {
            RGB = new int[w][h];
            for (int x = 0; x < w; x++) 
                for (int y = 0; y < h; y++) 
                    RGB[x][y] = image.getRGB(x,y);
        }
        
        /**
         * @return
         */
        @Override
        public int setDepth() {
            return depth = (int)((point0.getz()+point1.getz()+point2.getz()+point3.getz())/4*RADIXPRECISION);
        }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return new Image3D(
                correspondingpoint(point0,oldpoints,newpoints),
                correspondingpoint(point1,oldpoints,newpoints),
                correspondingpoint(point2,oldpoints,newpoints),
                correspondingpoint(point3,oldpoints,newpoints),RGB);
        }
        
        Point3D p0,p1,p2,p3,p4,p5;
        double dy5, dy3;
 
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {    
            double x0 = point0.getx(), x1 = point1.getx(), x2 = point2.getx(), x3 = point3.getx();
            if (x0 < x2)
                if (x1 < x3)
                    if (x0 < x1)        {p0 = point0; p1 = point1; p4 = point3;
                        if (x2 < x3)    {p2 = point2; p3 = point3; p5 = point0;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
                        else             {p2 = point3; p3 = point2; p5 = point1;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
                    else                {p0 = point1; p1 = point0; p4 = point2;
                        if (x2 < x3)    {p2 = point2; p3 = point3; p5 = point0;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
                        else             {p2 = point3; p3 = point2; p5 = point1;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}}
                else if (x0 < x3)        {p0 = point0; p1 = point3; p4 = point1;
                        if (x2 < x1)    {p2 = point2; p3 = point1; p5 = point0;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
                        else             {p2 = point1; p3 = point2; p5 = point3;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
                    else                {p0 = point3; p1 = point0; p4 = point2;
                        if (x2 < x1)    {p2 = point2; p3 = point1; p5 = point0;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
                        else             {p2 = point1; p3 = point2; p5 = point3;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}}
            else if (x1 < x3) 
                    if (x2 < x1)         {p0 = point2; p1 = point1; p4 = point3;
                        if (x0 < x3)    {p2 = point0; p3 = point3; p5 = point2;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
                        else             {p2 = point3; p3 = point0; p5 = point1;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
                    else                 {p0 = point1; p1 = point2; p4 = point0;
                        if (x0 < x3)     {p2 = point0; p3 = point3; p5 = point2;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
                        else             {p2 = point3; p3 = point0; p5 = point1;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}}
                else if (x2 < x3)         {p0 = point2; p1 = point3; p4 = point1;
                        if (x0 < x1)     {p2 = point0; p3 = point1; p5 = point2;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
                        else             {p2 = point1; p3 = point0; p5 = point3;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
                    else                 {p0 = point3; p1 = point2; p4 = point0;
                        if (x0 < x1)    {p2 = point0; p3 = point1; p5 = point2;
                                            dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
                        else             {p2 = point1; p3 = point0; p5 = point3;
                                            dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
                                            dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}}
            int x = (int)p0.getx();
            
            double
                m0 = (point0.gety() - point1.gety())/(x0 - x1),
                m1 = (point1.gety() - point2.gety())/(x1 - x2),
                m2 = (point2.gety() - point3.gety())/(x2 - x3),
                m3 = (point3.gety() - point0.gety())/(x3 - x0),
                c0 = point0.gety()-m0*x0,
                c1 = point1.gety()-m1*x1,
                c2 = point2.gety()-m2*x2,
                c3 = point3.gety()-m3*x3,
                r0 = Math.sqrt(m2*m2+1)/Math.sqrt(m0*m0+1),
                r1 = Math.sqrt(m3*m3+1)/Math.sqrt(m1*m1+1),
                wr0 = w*r0,
                hr1 = h*r1,
                dy1 = (p0.gety()-p1.gety())/(p0.getx()-p1.getx()),
                dy2 = (p0.gety()-p4.gety())/(p0.getx()-p4.getx()),
                dy6 = (p3.gety()-p2.gety())/(p3.getx()-p2.getx()),
                y1 = p0.gety(),
                y2 = y1,
                m0x = x*m0, 
                m1x = x*m1, 
                m2x = x*m2, 
                m3x = x*m3;
            //double m0p1x = p1.getx()*m0;
            //double m1p1x = p1.getx()*m1;
            //double m2p1x = p1.getx()*m2;
            //var m3p1x = p1.getx()*m3;
            
            if (dy1<dy2) {
                for (; x < p1.getx()-1; x++) {
                    for (int y = (int)y1; y <= y2; y++)
                        try{image_buffer.setRGB(x,y,RGB    [(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
                                                        [(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
                        }catch(Exception e){}
                    y1 += dy1;    y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
                
                y1 = p1.gety();
                for (; x < p2.getx()-1; x++) {
                    for (int y = (int)y1; y <= y2; y++)
                        try{image_buffer.setRGB(x,y,RGB    [(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
                                                        [(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
                        }catch(Exception e){}
                    y1 += dy3;    y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
            }    
            else {
                for (; x < p1.getx()-1; x++) {
                    for (int y = (int)y2; y <= y1; y++)
                        try{image_buffer.setRGB(x,y,RGB    [(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
                                                        [(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
                        }catch(Exception e){}
                    y1 += dy1;    y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
                y1 = p1.gety();
                for (; x < p2.getx()-1; x++) {
                    for (int y = (int)y2; y <= y1; y++)
                        try{image_buffer.setRGB(x,y,RGB    [(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
                                                        [(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
                        }catch(Exception e){}
                    y1 += dy3;    y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
            }        
            
            if (p4 == p3) y1 = y2; 
            y2 = p2.gety();
            if (y1<y2) 
                for (; x < p3.getx()-1; x++) {
                    for (int y = (int)y1; y <= y2; y++)
                        try{image_buffer.setRGB(x,y,RGB    [(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
                                                        [(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
                        }catch(Exception e){}
                    y1 += dy5;    y2 += dy6; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
            else 
                for (; x < p3.getx()-1; x++) {
                    for (int y = (int)y2; y <= y1; y++)
                        try{image_buffer.setRGB(x,y,RGB    [(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
                                                        [(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
                        }catch(Exception e){}
                    y1 += dy5;    y2 += dy6; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
        }
    }

    
    public class Circle3D extends Generic3D implements Shape3D {
        
        Point3D A;
        double size;
        
        /**
         * @param A
         * @param SIZE
         * @param c
         */
        public Circle3D(Point3D A, double SIZE, Color c) {
            this.A = A;
            size = SIZE;
            super.setColor(c);
        }
        
        /**
         * @param k
         * @param p
         */
        public Circle3D(StringTokenizer k, ArrayList p) {
            A = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            size = Integer.parseInt(k.nextToken());
            if (k.hasMoreTokens())
            {
                k.nextToken();
                super.setColor(parseColor(k));
            }
        }
        
        /**
         * @return
         */
        public Point3D getPoint() {
            return A;
        }
        
        /**
         * @return
         */
        @Override
        public int setDepth() {
            return super.depth = (int)(A.getz() * RADIXPRECISION);
        }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return new Circle3D(correspondingpoint(A,oldpoints,newpoints),size,getColor());
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer) {
            int s = (int)(2*size);
            applyColor(g);
            g.drawOval((int)(A.getx()-size),(int)(A.gety()-size),s,s);
        }
    }
    
    /**
     *
     */
    public class Sphere3D extends Circle3D {
 
        /**
         * @param A
         * @param SIZE
         * @param c
         */
        public Sphere3D(Point3D A, double SIZE, Color c)  {
            super(A,SIZE,c);
        }
        
        /**
         * @param k
         * @param p
         */
        public Sphere3D(StringTokenizer k, ArrayList p) {
            super(k,p);
        }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return new Sphere3D(correspondingpoint(A,oldpoints,newpoints),size,getColor());
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            int s = (int)(2*size);
            applyColor(g);
            g.fillOval((int)(A.getx()-size),(int)(A.gety()-size),s,s);
        }
    }
    
    /**
     *
     */
    public class Line3D extends Generic3D implements Shape3D {
        
        /**  */
        public Point3D A, B;
        
        /**  */
        public Line3D(){
        }
        
        /**
         * @param A
         * @param B
         */
        public Line3D(Point3D A, Point3D B)  {
            this.A = A;
            this.B = B;
            super.setColor(Color.WHITE);
        }
        
        /**
         * @param A
         * @param B
         * @param c
         */
        public Line3D(Point3D A, Point3D B, Color c)  {
            this.A = A;
            this.B = B;
            super.setColor(c);
        }
        
        /**
         * @param k
         * @param p
         */
        public Line3D(StringTokenizer k, ArrayList p) {
            A = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            B = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            if (k.hasMoreTokens())
            {
                k.nextToken();
                super.setColor(parseColor(k));
            }
        }
        
        /**
         * @return
         */
        @Override
        public int setDepth() {
            return super.depth = (int)((A.getz()+B.getz())/2.0 * RADIXPRECISION);
        }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return new Line3D(
                correspondingpoint(A,oldpoints,newpoints),
                correspondingpoint(B,oldpoints,newpoints),
                getColor());
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            //Sxystem.out.println("line "+(int)a.getx()+" "+(int)a.gety()+" "+(int)B.getx()+" "+(int)B.gety());
            applyColor(g);
            g.drawLine((int)A.getx(),(int)A.gety(),(int)B.getx(),(int)B.gety());
        }
        
    }
    
    /**
     *
     */
    public class ThickLine3D extends Line3D {
        
        int thickness, halfway, n;

        /**
         * @param A
         * @param B
         * @param c
         * @param t
         */
        public ThickLine3D(Point3D A, Point3D B, Color c, double t) {
            super(A,B,c);
            thickness = (int)(t*2);
            halfway = thickness/2;
            n = halfway-thickness+1;
        }
        
        /**
         * @param k
         * @param p
         */
        public ThickLine3D(StringTokenizer k, ArrayList p) {
            A = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            B = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            thickness = Integer.parseInt(k.nextToken());
            halfway = thickness/2;
            n = halfway-thickness+1;
            if (k.hasMoreTokens()) {
                k.nextToken();
                super.setColor(parseColor(k));
            }
            else super.setColor(Color.GRAY);
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            applyColor(g);
            for (int i = n; i <= halfway; i++)
                g.drawLine((int)A.getx()-n,(int)A.gety()-i,(int)B.getx()-n,(int)B.gety()-i);
            for (int i = n; i <= halfway; i++)
                g.drawLine((int)A.getx()-i,(int)A.gety()-n,(int)B.getx()-i,(int)B.gety()-n);
            for (int i = n; i <= halfway; i++)
                g.drawLine((int)A.getx()-halfway,(int)A.gety()-i,(int)B.getx()-halfway,(int)B.gety()-i);
            for (int i = n; i <= halfway; i++)
                g.drawLine((int)A.getx()-i,(int)A.gety()-halfway,(int)B.getx()-i,(int)B.gety()-halfway);
        }
    }
    
    /**
     *
     */
    public class ShadedLine3D extends ThickLine3D {        
        private int [][] rgba;
        
        private final int 
            color_shading = -100;
        
        /**
         * @param A
         * @param B
         * @param c
         * @param t
         */
        public ShadedLine3D(Point3D A, Point3D B, Color c, int t) {
            super(A,B,c,t);
            super.setColor(c);
        }
        
        /**
         * @param k
         * @param p
         */
        public ShadedLine3D(StringTokenizer k, ArrayList p) {
            super(k,p);
            if (k.hasMoreTokens()) {
                k.nextToken();
                super.setColor(parseColor(k));
            }
            else super.setColor(Color.GRAY);
        }
        
        /**
         * @param c
         */
        @Override
        public void setColor(Color c) {    
            super.setColor(c);
            int [] color = super.getRGBA();
            
            rgba = new int[thickness][4];
            int d = 0;
            
            for (int i = halfway;i >= n; i--)
            {
                double shade = (double)Math.abs(i)/n*color_shading;
                for (int j = 0; j < 3; j++)
                    rgba[d][j] = (int)(Math.min(255,Math.max(0,color[j]-shade)));
                rgba[d][3] = color[3];
                d++;
            }
        }
        
        /**
         * @param G
         * @param i
         */
        public void applyColor(Graphics G, int i) {
            
            double s = (double)depth/max_d;
            
            G.setColor(
                new Color(
                    (int)(Math.max(0,s)*rgba[i][0]),
                    (int)(Math.max(0,s)*rgba[i][1]),
                    (int)(Math.max(0,s)*rgba[i][2]),
                    rgba[i][3]));
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {    
            for (int i = n; i <= halfway; i++)
            {
                applyColor(g,(i-n)/2);
                g.drawLine((int)A.getx()-n,(int)A.gety()-i,(int)B.getx()-n,(int)B.gety()-i);
                g.drawLine((int)A.getx()-i,(int)A.gety()-n,(int)B.getx()-i,(int)B.gety()-n);
                g.drawLine((int)A.getx()-i,(int)A.gety()-halfway,(int)B.getx()-i,(int)B.gety()-halfway);
                g.drawLine((int)A.getx()-halfway,(int)A.gety()-i,(int)B.getx()-halfway,(int)B.gety()-i);
            }
        }
    }
    
    /**
     *
     */
    public class Outline3D extends Generic3D implements Shape3D  {
    
        /**  */
        public Point3D[] p;    
        
        /**
         * @param P
         * @param c
         */
        public Outline3D(Point3D[ ] P, Color c)  {
            p = P;
            super.setColor(c);
        }
        
        /**
         * @param k
         * @param l
         */
        @SuppressWarnings("unchecked")
        public Outline3D (StringTokenizer k, ArrayList l)  {
            ArrayList A = new ArrayList();
            super.setColor(Color.WHITE);
            while (k.hasMoreTokens()) 
            {    
                String s = k.nextToken();
                if (s.charAt(0)=='c')
                {
                    super.setColor(parseColor(k));
                    break;
                }
                A.add(new Integer(s));
            }    
            p = new Point3D [A.size()];
            
            for (int i = 0; i < p.length; i++)
                p[i] = (Point3D)(l.get(((Integer)(A.get(i)))-1));
        }
        
        /**
         * @return
         */
        @Override
        public int setDepth() {
            double sum = 0;
            for (Point3D p1 : p)
                if (p1 != null) 
                    sum += p1.getz();
            return super.depth = (int)(sum*RADIXPRECISION/p.length);
        }
        
        /**
         * @param oldpoints
         * @param newpoints
         * @return
         */
        @Override
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
            return null;
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer) {
            int [] x = new int [p.length], y = new int [p.length];
            for (int i = 0; i < p.length; i++)
            {
                x[i] = (int)p[i].getx();
                y[i] = (int)p[i].gety();
            }
            applyColor(g);
            g.drawPolygon(x,y,p.length);
        }
        
    }
    
    /**
     *
     */
    public class Form3D extends Outline3D {    
 
        /**
         * @param P
         * @param c
         */
        public Form3D(Point3D[ ] P, Color c)  {
            super(P,c);
        }
        
        /**
         * @param k
         * @param l
         */
        public Form3D (StringTokenizer k, ArrayList l)  {
            super(k,l);
        }
    
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer) {
            int [] x = new int [p.length], y = new int [p.length];
            for (int i = 0; i < p.length; i++)
            {
                x[i] = (int)p[i].getx();
                y[i] = (int)p[i].gety();
            }
            applyColor(g);
            g.fillPolygon(x,y,p.length);
        }    
    }
        
    static final double [][][] allPerimeters = getAllPerimeters();
        
    private static double [][][] getAllPerimeters() {
        double [][][] result = new double[20][][];
        
        for (int i = 1; i <= result.length; i++)  {
            int ii = i-1;
            result[ii] = new double[(int)(Math.PI*2*i)][2];
            double theta = 0;
            double theta_increment = 1.0/i;
            for (double[] item : result[ii]) {
                item[0] = i*Math.cos(theta);
                item[1] = i*Math.sin(theta);
                theta+=theta_increment;
            }
        }    
        return result;
    }
    
    /**
     *
     */
    public class Cylinder3D extends Line3D {
        double radius;
        double [][] perimeter;
        
        Cylinder3D(){}
        
        /**
         * @param a
         * @param b
         * @param r
         */
        public Cylinder3D(Point3D a, Point3D b, double r)  {
            super(a,b);
            radius = r;
            initPoints();
        }
        
        /**
         * @param a
         * @param b
         * @param c
         * @param r
         */
        public Cylinder3D(Point3D a, Point3D b, Color c, double r)  {
            super(a,b,c);
            radius = r;
            initPoints();
        }
        
        /**
         * @param k
         * @param p
         */
        public Cylinder3D(StringTokenizer k, ArrayList p) {
            A = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            B = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            radius = Double.parseDouble(k.nextToken());
            if (k.hasMoreTokens()) {
                k.nextToken();
                super.setColor(parseColor(k));
            }
            initPoints();
        }
        
        private void initPoints() {
            if (radius >= 0 && radius < allPerimeters.length)   {
                perimeter = allPerimeters[(int)radius];
            //    System.out.println(perimeter+"=="+allPerimeters[(int)radius]);
            }
            else {
                perimeter = new double[(int)(Math.PI*2*radius)][2];
                double theta = 0;
                double theta_increment = 1/radius;
                for (double[] perimeter1 : perimeter) {
                    perimeter1[0] = radius*Math.cos(theta);
                    perimeter1[1] = radius*Math.sin(theta);
                    theta+=theta_increment;
                }
            }
        }
        
        int p1,p2,Q3,Q4;
        double piovertwo = Math.PI/2;
 
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            applyColor(g);
            int i   = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius+perimeter.length);
            int j   = i+perimeter.length;
            int end = i + perimeter.length/2;
            while (i < end)
            {    int 
                index1 = i%perimeter.length,
                index2 = j%perimeter.length;
                p1 = (int)(perimeter[index1][0]+A.getx());
                p2 = (int)(perimeter[index1][1]+A.gety());
                Q3 = (int)(perimeter[index2][0]+B.getx());
                Q4 = (int)(perimeter[index2][1]+B.gety());
                g.drawLine(p1,p2,Q3,Q4);
                g.drawLine(p1++,p2++,Q3+1,Q4+1);
                g.drawLine(p1,p2,Q3,Q4);
                g.drawLine(p1,p2,Q3+1,Q4+1);
                i++;
                j--;
            }
        }
        
    }
    
    /**
     *
     */
    public class TaperedStem3D extends Cylinder3D {
        
        double radius2;
        double [][] perimeter2;
        
        TaperedStem3D(){}
        
        /**
         * @param a
         * @param b
         * @param r1
         * @param r2
         */
        public TaperedStem3D(Point3D a, Point3D b, double r1, double r2)  {
            super(a,b,r1);
            radius2 = r2;
            initPoints();
        }
        
        /**
         * @param a
         * @param b
         * @param c
         * @param r1
         * @param r2
         */
        public TaperedStem3D(Point3D a, Point3D b, Color c, double r1, double r2)  {
            super(a,b,c,r1);
            radius2 = r2;
            initPoints();
        }
        
        /**
         * @param k
         * @param p
         */
        public TaperedStem3D(StringTokenizer k, ArrayList p) {
            A = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            B = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            radius = Double.parseDouble(k.nextToken());
            radius2 = Double.parseDouble(k.nextToken());
            if (k.hasMoreTokens()) {
                k.nextToken();
                super.setColor(parseColor(k));
            }
            initPoints();
        }
        
        private void initPoints() {
            super.initPoints();
            if (radius2 >= 0 && radius2 < allPerimeters.length)   {
                perimeter2 = allPerimeters[(int)radius2];
            }
            else {
                perimeter2 = new double[(int)(Math.PI*2*radius2)][2];
                double theta = 0;
                double theta_increment = 1/radius2;
                for (double[] perimeter21 : perimeter2) {
                    perimeter21[0] = radius2*Math.cos(theta);
                    perimeter21[1] = radius2*Math.sin(theta);
                    theta+=theta_increment;
                }
            }
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            applyColor(g);
            
            int i1   = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius+perimeter.length);
            int i2   = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius2+perimeter2.length);
            
            int 
            index1 =  i1 % perimeter .length,
            index2 = (i2 + perimeter2.length) % perimeter2.length;
            g.drawLine(    (int)(perimeter[index1][0] + A.getx()),
                        (int)(perimeter[index1][1] + A.gety()),
                        (int)(perimeter2[index2][0]+ B.getx()),
                        (int)(perimeter2[index2][1]+ B.gety()));
            
            index1 = (i1 + perimeter .length/2 - 1) % perimeter .length;
            index2 = (i2 + perimeter2.length/2 + 1) % perimeter2.length;
            g.drawLine(    (int)(perimeter[index1][0] + A.getx()),
                        (int)(perimeter[index1][1] + A.gety()),
                        (int)(perimeter2[index2][0]+ B.getx()),
                        (int)(perimeter2[index2][1]+ B.gety()));
        }
        
        
        
    }
    
    /**
     *
     */
    public class HollowCylinder3D extends Cylinder3D {
                
        /**
         * @param a
         * @param b
         * @param r
         */
        public HollowCylinder3D(Point3D a, Point3D b, double r)  {
            super(a,b,r);
        }
        
        /**
         * @param a
         * @param b
         * @param c
         * @param r
         */
        public HollowCylinder3D(Point3D a, Point3D b, Color c, double r)  {
            super(a,b,c,r);
        }
        
        /**
         * @param k
         * @param p
         */
        public HollowCylinder3D(StringTokenizer k, ArrayList p) {
            super(k,p);
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            applyColor(g);
            int color = getComponentRGBA();
            int i   = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius+perimeter.length);
            int j   = i+perimeter.length;
            int end = i + perimeter.length/2;
            
            int 
            index1 = i%perimeter.length,
            index2 = j%perimeter.length;
            g.drawLine(    (int)(perimeter[index1][0]+A.getx()),
                        (int)(perimeter[index1][1]+A.gety()),
                        (int)(perimeter[index2][0]+B.getx()),
                        (int)(perimeter[index2][1]+B.gety()));
            i++;
            j--;
            
            while (i < end-1)
            {     
                index1 = i%perimeter.length;
                index2 = j%perimeter.length;
                g.drawLine(p1,p2,Q3,Q4);
                image_buffer.setRGB((int)(perimeter[index1][0]+A.getx()),(int)(perimeter[index1][1]+A.gety()),color);
                image_buffer.setRGB((int)(perimeter[index2][0]+B.getx()),(int)(perimeter[index2][1]+B.gety()),color);
                i++;
                j--;
            }
            
            index1 = i%perimeter.length;
            index2 = j%perimeter.length;
            g.drawLine(    (int)(perimeter[index1][0]+A.getx()),
                        (int)(perimeter[index1][1]+A.gety()),
                        (int)(perimeter[index2][0]+B.getx()),
                        (int)(perimeter[index2][1]+B.gety()));
        }
        
    }
    
    /**
     *
     */
    public class SparseCylinder3D extends Cylinder3D {
                
        int skip;
                
        /**
         * @param a
         * @param b
         * @param r
         */
        public SparseCylinder3D(Point3D a, Point3D b, double r)  {
            super(a,b,r);
        }
        
        /**
         * @param a
         * @param b
         * @param c
         * @param r
         */
        public SparseCylinder3D(Point3D a, Point3D b, Color c, double r)  {
            super(a,b,c,r);
        }
        
        /**
         * @param k
         * @param p
         */
        public SparseCylinder3D(StringTokenizer k, ArrayList p) {
            super(k,p);
        }
        
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            applyColor(g);
            int i   = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius+perimeter.length);
            int j   = i+perimeter.length;
            int end = i + perimeter.length/2;
            while (i < end)
            {    int 
                index1 = i%perimeter.length,
                index2 = j%perimeter.length;
                p1 = (int)(perimeter[index1][0]+A.getx());
                p2 = (int)(perimeter[index1][1]+A.gety());
                Q3 = (int)(perimeter[index2][0]+B.getx());
                Q4 = (int)(perimeter[index2][1]+B.gety());
                g.drawLine(p1,p2,Q3,Q4);
                i+=2;
                j-=2;
            }
        }
    }
    
    /**
     *
     */
    public class Cone3D extends Cylinder3D  {
        int P1,Q2;

        /**
         * @param k
         * @param a
         */
        public Cone3D(StringTokenizer k, ArrayList a) {
            super(k,a);
        }
 
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {
            applyColor(g);
            int i   = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius+perimeter.length);
            int j   = i+perimeter.length;
            int end = i + perimeter.length/2;
            while (i < end)
            {    int 
                index1 = i%perimeter.length,
                Q1 = (int)(perimeter[index1][0]+A.getx());
                Q2 = (int)(perimeter[index1][1]+A.gety());
                Q3 = (int)(B.getx());
                Q4 = (int)(B.gety());
                g.drawLine(Q1,Q2,Q3,Q4);
                g.drawLine(Q1++,Q2++,Q3,Q4);
                g.drawLine(Q1,Q2,Q3,Q4);
                g.drawLine(Q1,Q2,Q3,Q4);
                i++;
                j--;
            }
        }
    }
    
    /**
     *
     */
    public class ShadedCylinder3D extends Cylinder3D {
        private int [][] rgba;
        int alpha;
        private final int color_shading = -128;
        
        /**
         * @param a
         * @param b
         * @param r
         */
        public ShadedCylinder3D(Point3D a, Point3D b, double r)  {
            super(a,b,r);
            super.setColor(Color.gray);
        }
        
        /**
         * @param a
         * @param b
         * @param c
         * @param r
         */
        public ShadedCylinder3D(Point3D a, Point3D b, Color c, double r)  {
            super(a,b,c,r);
            super.setColor(c);
        }
        
        /**
         * @param a
         * @param b
         * @param rgba
         * @param r
         */
        public ShadedCylinder3D(Point3D a, Point3D b, int [][] rgba, double r)  {
            super(a,b,Color.WHITE,r);
            //super.setColor(c);
            this.rgba = rgba;
            alpha = 255;
        }
        
        /**
         * @param k
         * @param p
         */
        public ShadedCylinder3D(StringTokenizer k, ArrayList p) {
            A = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            B = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
            radius = Double.parseDouble(k.nextToken());
            if (k.hasMoreTokens()) {
                k.nextToken();
                super.setColor(parseColor(k));
            }
            super.initPoints();
        }
        
        /**
         * @param c
         */
        @Override
        public void setColor(Color c) {    
            super.setColor(c);
            int [] color = super.getRGBA();
                alpha = color[3];
            rgba = new int[(int)(Math.PI * radius)][3];
            for (int i = 0; i < rgba.length; i++) {
                double shade = Math.abs((double)i/rgba.length*2-.5)*color_shading;
                for (int j = 0; j < 3; j++)
                    rgba[i][j] = (int)(Math.min(255,Math.max(0,color[j]+shade)));
            }
        }
        
        int P1,P2,P3,P4;
 
        /**
         * @param g
         * @param image_buffer
         */
        @Override
        public void draw(Graphics g, BufferedImage image_buffer)  {    
            int l2 = perimeter.length/2;
            int i = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius)+perimeter.length;
            int j = i+perimeter.length;
            int end = i + l2;
            int c = 0;
            double s = Math.max(0,(double)depth/max_d);
            while (i < end) {
                g.setColor(new Color((int)(s*rgba[c][0]),(int)(s*rgba[c][1]),(int)(s*rgba[c][2]),alpha));
                int 
                index1 = i%perimeter.length,
                index2 = j%perimeter.length;
                g.drawLine(P1 = (int)(perimeter[index1][0]+A.getx()),
                    P2 = (int)(perimeter[index1][1]+A.gety()),
                    P3 = (int)(perimeter[index2][0]+B.getx()),
                    P4 = (int)(perimeter[index2][1]+B.gety()));
                i++;
                j--;
                c = (c+1)%rgba.length;
            }
        }
        
    }
    
    /**
     *
     * @param k
     * @return
     */
    public Color parseColor(StringTokenizer k) {
        return new Color(
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):127,
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):127,
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):127,
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):255);
    }
    
    Point3D[] point;                      //all points, ever
    RotateablePoint3D  [] p_rotateable;   //rotateable points, clone of m_p_rotateable
    RotateablePoint3D  [] m_p_rotateable; //subset of m_p that can be rotated
    InterpolatedPoint3D[] p_interpolated; //subset of m_p that are interpolated
    Shape3D[] shape;
    static int max_d, min_d;
        
    Object3D() {}
    
    /**
     * @param b
     */
    public Object3D(BufferedReader b) {
    
        ArrayList  
            p   = new ArrayList(), 
            s   = new ArrayList(), 
            pr  = new ArrayList(), 
            prm = new ArrayList(), 
            pi  = new ArrayList();
        
        StringTokenizer k;
        try  {
            String line = b.readLine();
            while (line != null)  {
                try  {
                    k = new StringTokenizer(line);
                    String t = k.nextToken().toLowerCase();
                    parse(t,k,s,p,prm,pr,pi);
                } 
                catch (Exception e) {}
                line = b.readLine();
            }
        } 
        catch (IOException e) {}
        finalize(p,pr,prm,pi,s);
    }
    
    /**
     *
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public Object3D smooth() {
        
        ArrayList 
            outlines  = new ArrayList(),                    //will store existing outlines
            newshapes = new ArrayList(),                    //will store new outlines
            newpoints = new ArrayList(),                    //will store clones of all points and new points
            allnewshapes = new ArrayList(),                
            newrotateablepoints = new ArrayList(),             //will store clones of existing rotateable points
            newmasterrotatreablepoints = new ArrayList(),     //will store clones of existing master rotateable points
            newinterpolatedpoints = new ArrayList();        //will store clones of existing as well as new interpolated points
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) {
            newmasterrotatreablepoints.add(m_p_rotateable1.clone2());
        }
            
        for (RotateablePoint3D p_rotateable1 : p_rotateable) {
            RotateablePoint3D p = (RotateablePoint3D) (p_rotateable1.clone2());
            newrotateablepoints.add(p);
            newpoints.add(p);
        }
        //clones existing interpolated points
        for (InterpolatedPoint3D p_interpolated1 : p_interpolated) {
            InterpolatedPoint3D p = (InterpolatedPoint3D) (p_interpolated1.createclone(newpoints, point));
            newinterpolatedpoints.add(p);
            newpoints.add(p);
        }
        //stores lines as points in an arraylist, one arraylist for each points
        ArrayList [] lines         = new ArrayList[point.length],
        //stores midpoints of the lines in ArrayList lines
        linemidpoints = new ArrayList[point.length];
        
        //initialize the ArrayLists int lines and linemidpoints, good to go.
        for (int i = 0; i < point.length; i++)  {
            lines[i]         = new ArrayList();
            linemidpoints[i] = new ArrayList();
        }
        //look for existing outlines
        for (var shape1 : shape) {
            try {
                //if an outlines ins found:
                Outline3D o = (Outline3D) shape1;
                outlines.add(o);
                //create an array to hold the correct clones of the outline's points
                Point3D [] outlinpointclone = new Point3D[o.p.length];
                //to store midpoints that will define a new outline based on interpolation
                InterpolatedPoint3D [] newobjectpoints = new InterpolatedPoint3D[o.p.length];
                //find the first correct cloned point for the outline
                Point3D unclonedprevious = o.p[o.p.length - 1];
                Point3D clonedprevious = null;
                if (unclonedprevious.getClass() == RotateablePoint3D.class) {
                    for (int j = 0; j < p_rotateable.length; j++)
                        if (p_rotateable[j] == unclonedprevious) 
                            clonedprevious = (Point3D)(newrotateablepoints.get(j));}
                else {
                    for (int j = 0; j < p_interpolated.length; j++)
                        if (p_interpolated[j] == unclonedprevious) 
                            clonedprevious = (Point3D)(newinterpolatedpoints.get(j));}
                //where the other points linking to cloned previous should be placed int the lines[]
                //hopefully the index of the cloned previous in line is the same as the index of uncloned previous
                //in points
                int previousindex = indexOf(unclonedprevious);
                //then go through the rest of the points and store the lines and the midpoints of the lines,
                //while completing the reconstruction of the clones outline
                for (int j = 0; j < o.p.length; j++) 
                {
                    //find the next correct cloned point for the outline
                    Point3D uncloned = o.p[j];
                    Point3D cloned = null;
                    if (uncloned.getClass() == RotateablePoint3D.class) {
                        for (int k = 0; k < p_rotateable.length; k++)
                            if (p_rotateable[k] == uncloned) 
                                cloned = (Point3D)(newrotateablepoints.get(k));}
                    else {
                        for (int k = 0; k < p_interpolated.length; k++)
                            if (p_interpolated[k] == uncloned) 
                                cloned = (Point3D)(newinterpolatedpoints.get(k));}
                    if (newpoints.get(previousindex) != cloned){
                        
                        //where the other points linking to cloned should be placed int the lines[]
                        //hopefully the index of the clone in lines[] is the same as the index of uncloned in points
                        int index = indexOf(uncloned);
                        //store the correct cloned point
                        outlinpointclone[j] = cloned;
                        
                        InterpolatedPoint3D midpoint;
                        //if the outline line segment has already been added to the lines[] ArrayList
                        if (lines[previousindex].contains(cloned))
                        {
                            midpoint = (InterpolatedPoint3D)(
                                    linemidpoints[previousindex].get(lines[previousindex].indexOf(cloned)));
                        }
                        else
                        {
                            lines[index].add(clonedprevious);//store the line,
                            lines[previousindex].add(cloned);//referenced twice.
                            
                            midpoint = //calculate the line's midpoint
                                    new InterpolatedPoint3D(cloned, clonedprevious);
                            
                            linemidpoints[index        ].add(midpoint);    //store the new midpoint
                            linemidpoints[previousindex].add(midpoint);    //crossreferenced.
                            newpoints.add(midpoint);                        //also to newpoints
                            newinterpolatedpoints.add(midpoint);            //and newinterpolatedpoints
                        }
                        
                        newobjectpoints[j] = midpoint; //the midpoint will become one of the points in the new outline
                        
                        previousindex  = index;  //set the current index  as previous
                        clonedprevious = cloned; //set the current cloned as previous
                        
                    }
                }
                //create a new outline3d by interpolation
                Outline3D newoutline;
                if (o.getClass() == Form3D.class)
                    newoutline = new Form3D(newobjectpoints,o.getColor());
                else
                    newoutline = new Outline3D(newobjectpoints,o.getColor());
                newshapes.add(newoutline);
                allnewshapes.add(newoutline);
            } catch (Exception e) {
                //the shape is not a valid outline or form 3d
                //    e.printStackTrace();
                try {
                    allnewshapes.add(shape1.cloneto(point, newpoints));
                }catch (Exception e1){}
            }
        }
        
        //all midpoints have been calculted
        //now to locate and create ne faces/outlines created from interpolations
        for (int i = 0; i < point.length; i++)  {
            //investigate every point that has at least three lines
            if (lines[i].size() > 2) 
            {
                //now comes the tricly part, correctly reconstructing an outline
                
                //store all the midpoints (to become points of an outline) an a new array
                Point3D [] p = new Point3D[linemidpoints[i].size()];
                for (int j = 0; j < linemidpoints[i].size(); j++) 
                    p[j] = (Point3D)(linemidpoints[i].get(j));
                
                //create a new array that will hold the outline points in correct order
                Point3D [] p2 = new Point3D[p.length];
                
                //store the first point
                p2[0] = p[0];
                
                //and the index of that point
                int destinationindex = 0;
                int formweight = 0, outlineweight = 0, friends = 0;
                double red = 0, green = 0, blue = 0, alpha = 0;
                //as long as not all points have been filled
                while (destinationindex < p.length-1) 
                {
                    boolean match_has_not_been_found = true;
                    int shapeindex = 0;
                    //look for a match in the shapes
                    while(match_has_not_been_found && shapeindex < newshapes.size()) 
                    {
                        Outline3D o   = (Outline3D)(newshapes.get(shapeindex));
                        int pointindex = 0;
                        while (match_has_not_been_found && pointindex < o.p.length)
                        {
                            if (o.p[pointindex] == p2[destinationindex]) 
                            {
                                int secondpointindex = 0; 
                                while (match_has_not_been_found && secondpointindex < o.p.length)
                                {
                                    int desiredpointindex = 0;
                                    while (match_has_not_been_found && desiredpointindex < p.length)
                                    {
                                        if (o.p[secondpointindex] == p[desiredpointindex]) 
                                        {
                                            match_has_not_been_found = false;
                                            for (int m = 0; m <= destinationindex; m++) 
                                                if (p2[m] == o.p[secondpointindex]) 
                                                    match_has_not_been_found = true;
                                            if (!match_has_not_been_found) 
                                            {
                                                destinationindex++;
                                                p2[destinationindex] = o.p[secondpointindex];
                                                if (o.getClass() == Form3D.class) formweight++;
                                                else outlineweight++;
                                                Color c = o.getColor();
                                                red += c.getRed();
                                                green += c.getGreen();
                                                blue += c.getBlue();
                                                alpha += c.getAlpha();
                                                friends ++;
                                            }
                                        }
                                        desiredpointindex++;
                                    }
                                    secondpointindex++;
                                }
                            }
                            pointindex++;
                        }
                        shapeindex ++;
                    }
                    if (match_has_not_been_found) {
                        destinationindex++;
                        for (Point3D p1 : p) 
                            for (int j = 0; j < destinationindex; j++) 
                                if (p2[j] != p1) p2[destinationindex] = p1;
                    }
                }
                int r = (int)(red  /friends);
                int g = (int)(green/friends);
                int b = (int)(blue /friends);
                int a = (int)(alpha/friends);
                Outline3D newoutline;
                if (formweight > outlineweight)
                    newoutline = new Form3D(p2,new Color(r,g,b,a));
                else 
                    newoutline = new Outline3D(p2,new Color(r,g,b));
                newshapes.add(newoutline);
                allnewshapes.add(newoutline);
            }
        }

        return new Object3D(
            newpoints, 
            newrotateablepoints, 
            newmasterrotatreablepoints, 
            newinterpolatedpoints, 
            allnewshapes);
    }
    
    private Object3D(ArrayList p,ArrayList pr,ArrayList prm,ArrayList pi,ArrayList s) {
        finalize(p, pr, prm, pi, s);
    }
    
    private int indexOf(Point3D p) {
        for (int i = 0; i < point.length; i++)
            if (p == point[i]) return i;
        //Sxystem.out.println(p+"\t");
        return -1;
    }
    
    void finalize(ArrayList p,ArrayList pr,ArrayList prm,ArrayList pi,ArrayList s) {
        point = new Point3D[p.size()];
        p_rotateable     = new RotateablePoint3D[pr.size()];
        m_p_rotateable   = new RotateablePoint3D[prm.size()];
        p_interpolated = new InterpolatedPoint3D[pi.size()];
        
        for (int i = 0; i < point.length; i++) 
            point[i] = (Point3D)( p.get(i));
        for (int i = 0; i < m_p_rotateable.length; i++) 
            m_p_rotateable[i] = (RotateablePoint3D)(prm.get(i));
        for (int i = 0; i < p_rotateable.length; i++) 
            p_rotateable[i] = (RotateablePoint3D)(pr.get(i));
        for (int i = 0; i < p_interpolated.length; i++)
            p_interpolated[i] = (InterpolatedPoint3D)(pi.get(i));
        shape = new Shape3D[s.size()];
        for (int i = 0; i < shape.length; i++)
            shape[i] = (Shape3D)(s.get(i));
    }
    
    int point_counter = 0;
    @SuppressWarnings("unchecked")
    boolean parse(
        String t, 
        StringTokenizer k, 
        ArrayList shape_destination, 
        ArrayList point_destination, 
        ArrayList master_rotateable_point_destination,
        ArrayList rotateable_point_destination,
        ArrayList interpolated_point_destination) 
    {
        switch (t) {
            case "p":
            case "v":
            case "point": {
                RotateablePoint3D p = new RotateablePoint3D(k);
                point_destination.add(point_counter,p);
                rotateable_point_destination.add(p);
                master_rotateable_point_destination.add(p.clone2());
                point_counter++;
                break;
            }
            case "i":
            case "vr":
            case "interpolatedpoint":
            case "intepoint": {
                InterpolatedPoint3D p = new InterpolatedPoint3D(k,point_destination);
                point_destination.add(point_counter, p);
                interpolated_point_destination.add(p);
                point_counter++;
                break;
            }
            case "c":
            case "circle":
                shape_destination.add(new Circle3D(k,point_destination));
                break;
            case "s":
            case "sphere":
                shape_destination.add(new Sphere3D(k,point_destination));
                break;
            case "l":
            case "line":
                shape_destination.add(new Line3D(k,point_destination));
                break;
            case "t":
            case "thickline":
                shape_destination.add(new ThickLine3D(k,point_destination));
                break;
            case "h":
            case "shadedline":
                shape_destination.add(new ShadedLine3D(k,point_destination));
                break;
            case "j":
            case "shadedcylinder":
                shape_destination.add(new ShadedCylinder3D(k,point_destination));
                break;
            case "m":
            case "image":
                shape_destination.add(new Image3D(k,point_destination));
                break;
            case "a":
            case "stationaryimage":
                shape_destination.add(new StationaryImage3D(k,point_destination));
                break;
            case "y":
            case "cylinder":
                shape_destination.add(new Cylinder3D(k,point_destination));
                break;
            case "n":
            case "cone":
                shape_destination.add(new Cone3D(k,point_destination));
                break;
            case "o":
            case "outline":
                shape_destination.add(new Outline3D(k,point_destination));
                break;
            case "x":
            case "text":
                shape_destination.add(new RotateableText3D(k,point_destination));
                break;
            case "r":
            case "pointsphere":
                shape_destination.add(new PointSphere3D(k,point_destination,shape_destination));
                break;
            case "f":
            case "fo":
            case "form":
                shape_destination.add(new Form3D(k,point_destination));
                break;
            case "e":
            case "color":
            case "coloredpoint":
                shape_destination.add(new ColoredPoint3D(k,point_destination));
                break;
            default:
                try {
                    RotateablePoint3D p = new RotateablePoint3D(
                        Double.parseDouble(t),
                        Double.parseDouble(k.nextToken()),
                        Double.parseDouble(k.nextToken()));
                        point_destination.add(point_counter,p);
                        rotateable_point_destination.add(p);
                        master_rotateable_point_destination.add(p.clone2());
                        point_counter++;
                }
                catch ( NumberFormatException e ) { return false; }   
                break;
        }
        
        return true;            
    }
    
    /**
     *
     * @param g
     * @param image_buffer
     * @param x_offset
     * @param y_offset
     * @param z_offset
     */
    public void draw(Graphics g, BufferedImage image_buffer, int x_offset, int y_offset, int z_offset)  {
        if (shape.length > 0) {
            for (int i = 0; i < p_rotateable.length; i++) {
                p_rotateable[i].setx( 2*m_p_rotateable[i].getx()+x_offset);
                p_rotateable[i].sety(-2*m_p_rotateable[i].gety()+y_offset);
                p_rotateable[i].setz( 2*m_p_rotateable[i].getz()+z_offset);
            }        
            for (InterpolatedPoint3D p_interpolated1 : p_interpolated) {
                p_interpolated1.update();
            }

            max_d = shape[0].setDepth();
            min_d = max_d;
                
            for (int i = 1; i < shape.length; i++) {
                int depth = shape[i].setDepth();
                max_d = Math.max(max_d,depth);
                min_d = Math.min(min_d,depth);
            }
            
            Shape3D [][] s1, s2 = new Shape3D [10][shape.length];
            
            int [] w1, w2 = new int [10];
            
            w2[0] = shape.length;
            
            for (int i = 0; i < shape.length; i++) 
                (s2[0][i] = shape[i]).setDepth(shape[i].depth() - min_d);
            
            max_d -= min_d;
            
            int maximum_radix = (int)(Math.pow(10,(int)(Math.log(max_d)/Math.log(10))))*10;
            
            int q;
            for (int r = 10; r <= maximum_radix; r *= 10) 
            {    
                w1 = w2; 
                w2 = new int [10];
                
                s1 = s2; 
                s2 = new Shape3D [10][shape.length];
                
                for (int i = 9; i >= 0; i--)
                    while (w1[i]-- > 0) 
                    {
                        q = s1[i][w1[i]].depth()%r*10/r;
                        
                        try {
                        s2[q][w2[q]] = s1[i][w1[i]];
                        w2[q]++;
                        } catch (Exception e) {
                            //System.out.println("error"+q);
                            }
                    }
            }
            
            for (int i = 0; i < 10; i++)
                while (w2[i]-- > 0)
                    s2[i][w2[i]].draw(g,image_buffer);
        }
        else System.out.println("object is empty");
    }
    
    /**
     *
     * @param t
     */
    public void rotatex(double t) {
        double 
            s = Math.sin(t),
            c = Math.cos(t);
        
        double temp;
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) {
            temp = m_p_rotateable1.y;
            m_p_rotateable1.y = c * m_p_rotateable1.y + s * m_p_rotateable1.z;
            m_p_rotateable1.z = c * m_p_rotateable1.z - s * temp;
        }
    }
    
    /**
     *
     * @param t
     */
    public void rotatey(double t) {
        double 
            s = Math.sin(t),
            c = Math.cos(t);
        
        double temp;
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) {
            temp = m_p_rotateable1.x;
            m_p_rotateable1.x = c * m_p_rotateable1.x + s * m_p_rotateable1.z;
            m_p_rotateable1.z = c * m_p_rotateable1.z - s * temp;
        }
    }
    
    /**
     *
     * @param t
     */
    public void rotatez(double t) {
        double 
            s = Math.sin(t),
            c = Math.cos(t);
        
        double temp;
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) {
            temp = m_p_rotateable1.x;
            m_p_rotateable1.x = c * m_p_rotateable1.x + s * m_p_rotateable1.y;
            m_p_rotateable1.y = c * m_p_rotateable1.y - s * temp;
        }
    }
    
    /**
     *
     * @param dx
     */
    public void shiftx(double dx) {
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) 
            m_p_rotateable1.x += dx;
    }

    /**
     *
     * @param dy
     */
    public void shifty(double dy) {
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) 
            m_p_rotateable1.y += dy;
    }
    
    /**
     *
     * @param dz
     */
    public void shiftz(double dz) {
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) 
            m_p_rotateable1.z += dz;
    }
    
    /**
     *
     * @param dx
     */
    public void scalex(double dx) {
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) 
            m_p_rotateable1.x *= dx;
    }

    /**
     *
     * @param dy
     */
    public void scaley(double dy) {
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) 
            m_p_rotateable1.y *= dy;
    }
    
    /**
     *
     * @param dz
     */
    public void scalez(double dz) {
        for (RotateablePoint3D m_p_rotateable1 : m_p_rotateable) 
            m_p_rotateable1.z *= dz;
    }
    
    /**
     *
     * @param a
     */
    public void scale( double a ) {
        for ( RotateablePoint3D p : m_p_rotateable ) {
            p.x *= a ;
            p.y *= a ;
            p.z *= a ;
        }
    }
        
    /**
     *
     * @return
     */
    public Point3D [] getPoints() {
        return point;
    }
    
    /**
     *
     * @return
     */
    public RotateablePoint3D [] getMasterRotateablePoints() {
        return m_p_rotateable;
    }
    
    /**
     *
     * @return
     */
    public InterpolatedPoint3D [] getMasterInterpolatedPoints() {
        return p_interpolated;
    }
    
    /**
     *
     * @return
     */
    public Shape3D [] getShapes() {
        return shape;
    }
    
    private static Point3D correspondingpoint(Point3D query, Point3D [] oldpoints, ArrayList newpoints)  {
        for (int i = 0; i < oldpoints.length; i++)
            if (query == oldpoints[i]) return (Point3D)(newpoints.get(i));
        return null;
    }
       
    /**
     *
     * @param i
     */
    public void recenter(int i) {
        double minx = Double.MAX_VALUE ;
        double miny = Double.MAX_VALUE ;
        double minz = Double.MAX_VALUE ;
        double maxx = Double.MIN_VALUE ;
        double maxy = Double.MIN_VALUE ;
        double maxz = Double.MIN_VALUE ;
        for ( Point3D p : this.point ) {
            double x = p.getx() ;
            double y = p.gety() ;
            double z = p.getz() ;
            if ( x < minx ) minx = x ;
            if ( y < miny ) miny = y ;
            if ( z < minz ) minz = z ;
            if ( x > maxx ) maxx = x ;
            if ( y > maxy ) maxy = y ;
            if ( z > maxz ) maxz = z ;
        }
        double dx = (maxx + minx)/2 ;
        double dy = (maxy + miny)/2 ;
        double dz = (maxz + minz)/2 ;
        //System.out.println("DX  :" + dx + "DY  :" + dy + "DZ  :" + dz);
        double a = i / Math.max( maxz - minz , Math.max( maxx - minx , maxy - miny ));
        
        for ( RotateablePoint3D p : m_p_rotateable ) {
            p.x = ( p.x - dx ) * a ;
            p.y = ( p.y - dy ) * a ;
            p.z = ( p.z - dz ) * a ;
        }
    }
    
    /**
     *
     * @param thresh
     */
    public void nearest( double thresh ) {
        ArrayList<Shape3D> shapes = new ArrayList<>() ;
        thresh *= thresh ;
        for ( Point3D p : this.point ) {
            for ( Point3D q : this.point ) {
                if ( p != q ) {
                    double dx = p.getx() - q.getx() ;
                    double dy = p.gety() - q.gety() ;
                    double dz = p.getz() - q.getz() ;
                    if ( dx*dx+dy*dy+dz*dz < thresh ) {
                        shapes.add( new Line3D(p,q) );
                    }
                }
            }
        }
        
        Shape3D[] newshape = new Shape3D[shape.length + shapes.size()];
        int i = 0 ;
        for (; i < shape.length; i++) { 
            newshape[i] = shape[i];//Shape3D)(shapes.get(i));
        }
        for ( Shape3D s : shapes ) {
            newshape[i] = s ;
            i ++ ;
        }
        shape = newshape ;
    }

    /**
     *
     * @return
     */
    public Object3D convexHull() {
      
        
        
        return null;
    }
    
    /**
     *
     * @param color
     * @return
     */
    public Object3D hilightpoints(int color) {
        ArrayList<Shape3D> s = new ArrayList<>() ;
        s.addAll(Arrays.asList(shape));
        
        Color c = new Color( color, true );
        for ( Point3D p : point ) {
            s.add( new Circle3D(p,2,c));
        }
        shape = s.toArray(Shape3D[]::new);
        return this ;
    }   
    
    /**
     *
     * @param o
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object3D merge(Object3D o) {
        ArrayList p = new ArrayList() ;
        ArrayList pr = new ArrayList() ;
        ArrayList prm = new ArrayList() ;
        ArrayList pi = new ArrayList() ;
        ArrayList s = new ArrayList() ;
        
        prm.addAll(Arrays.asList(m_p_rotateable));
        pi.addAll(Arrays.asList(p_interpolated));
        pr.addAll(Arrays.asList(p_rotateable));
        p.addAll(Arrays.asList(point));
        s.addAll(Arrays.asList(shape));
        prm.addAll(Arrays.asList(o.m_p_rotateable));
        pi.addAll(Arrays.asList(o.p_interpolated));
        pr.addAll(Arrays.asList(o.p_rotateable));
        p.addAll(Arrays.asList(o.point));
        s.addAll(Arrays.asList(o.shape));
        
        return new Object3D(
            p, 
            pr, 
            prm, 
            pi, 
            s);
    }
}
