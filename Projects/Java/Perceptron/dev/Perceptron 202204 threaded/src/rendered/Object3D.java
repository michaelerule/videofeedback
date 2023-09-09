package rendered;
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
import static util.Sys.serr;
public class Object3D {
    public static interface Shape3D {
        public int setDepth(int i);
        public int setDepth();
        public int depth();
        public void draw(Graphics G, BufferedImage image_buffer);
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints);	
    }
    public static interface Point3D {
        public double getx();
        public double gety();
        public double getz();
        public Point3D clone2();
        public String toString();
    }
    public static final double RADIXPRECISION = 100;
    public static class RotateablePoint3D implements Point3D {
        public double x,y,z;
        public RotateablePoint3D(double X, double Y, double Z) {x = X;y = Y;z = Z;}
        public RotateablePoint3D(StringTokenizer k)  {
            this(Double.parseDouble(k.nextToken()),
                Double.parseDouble(k.nextToken()),
                Double.parseDouble(k.nextToken()));
        }
        public RotateablePoint3D(Point3D p) {x = p.getx();y = p.gety();z = p.getz();}
        public RotateablePoint3D clone2() {return new RotateablePoint3D(this);}
        public double getx() {return x;}
        public double gety() {return y;}
        public double getz() {return z;}
        public void setx(double n) {x = n;}
        public void sety(double n) {y = n;}
        public void setz(double n) {z = n;}
        public String toString() {return "R: <"+super.toString().substring(27)+" "+x+", "+y+", "+z+">";}
    }                                
    public static class InterpolatedPoint3D implements Point3D {
        private Point3D [] points;
        private double  [] dd;
        public  double x, y, z;
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
        public InterpolatedPoint3D(Point3D p1, Point3D p2) {
            points = new Point3D[2];
            points[0] = p1;
            points[1] = p2;initd();
            update();
        }
        public InterpolatedPoint3D(StringTokenizer k, ArrayList<Point3D> allpoints) {
            ArrayList<Point3D> newpoints = new ArrayList<>();
            while (k.hasMoreTokens()) 
                    newpoints.add(allpoints.get(Integer.parseInt(k.nextToken())-1));
            points = new Point3D[newpoints.size()];
            for (int i = 0; i < points.length; i++)
                    points[i] = (Point3D)(newpoints.get(i));
            initd();
        }
        public InterpolatedPoint3D clone2() {
            return new InterpolatedPoint3D(points);
        }
        public InterpolatedPoint3D createclone(ArrayList newpoints, Point3D[] point) {//clonedrotatedpoints, ArrayList clonedinterpolatedpoints) {
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
        
        public double getx() {return x;}
        public double gety() {return y;}
        public double getz() {return z;}
        
        @Override
        public String toString() {
            String s = "I: <"+super.toString().substring(29);
            for (Point3D point1 : points) s += " " + point1.toString();
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
            double surface = Math.PI*R*R;///40;
            for (int x = 0; x < surface; x++)
            {
                theta = 2*Math.PI*Math.random();
                phi   = Math.acos(2*Math.random()-1)-pi2;
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
                if (s.charAt(0)=='c'){super.setColor(parseColor(k));break;}
                A.add(Integer.valueOf(s));
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
            for (int i = 0; i < p.length; i++)  {
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
        public Cylinder3D(Point3D a, Point3D b, double r)  {
            super(a,b);
            radius = r;
            initPoints();
        }
        public Cylinder3D(Point3D a, Point3D b, Color c, double r)  {
            super(a,b,c);
            radius = r;
            initPoints();
        }
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
        public void draw(Graphics g, BufferedImage image_buffer)  {
            applyColor(g);
            int i   = (int)((Math.atan2(B.gety()-A.gety(),B.getx()-A.getx())+piovertwo)*radius+perimeter.length);
            int j   = i+perimeter.length;
            int end = i + perimeter.length/2;
            while (i < end) {
                int 
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
    
    public Color parseColor(StringTokenizer k) {
        return new Color(
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):127,
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):127,
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):127,
            (k.hasMoreTokens())?Integer.parseInt(k.nextToken()):255);
    }
    
    Point3D            [] point;          //all points
    RotateablePoint3D  [] p_rotateable;   //rotateable points, clone of m_p_rotateable
    RotateablePoint3D  [] mp_rotateable; //subset of m_p that can be rotated
    InterpolatedPoint3D[] p_interpolated; //subset of m_p that are interpolated
    Shape3D[] shape;
    static int max_d, min_d;
        
    
    public Object3D(BufferedReader b) {
        ArrayList<Point3D>             p   = new ArrayList<>();
        ArrayList<Shape3D>             s   = new ArrayList<>();
        ArrayList<RotateablePoint3D>   pr  = new ArrayList<>();
        ArrayList<RotateablePoint3D>   prm = new ArrayList<>();
        ArrayList<InterpolatedPoint3D> pi  = new ArrayList<>();
        StringTokenizer k;
        try {
            String line = b.readLine();
            while (line != null)  {
                try {
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
        
    private Object3D(ArrayList<Point3D> p,ArrayList pr,ArrayList prm,ArrayList pi,ArrayList s) {
        finalize(p, pr, prm, pi, s);
    }
    
    private int indexOf(Point3D p) {
        for (int i=0; i<point.length; i++) if (p==point[i]) return i;
        return -1;
    }
    
    final void finalize(ArrayList<Point3D> p,ArrayList pr,ArrayList prm,ArrayList pi,ArrayList s) {
        point = new Point3D[p.size()];
        p_rotateable   = new RotateablePoint3D[pr.size()];
        mp_rotateable = new RotateablePoint3D[prm.size()];
        p_interpolated = new InterpolatedPoint3D[pi.size()];
        
        for (int i = 0; i < point.length; i++) 
            point[i] = (Point3D)( p.get(i));
        for (int i = 0; i < mp_rotateable.length; i++) 
            mp_rotateable[i] = (RotateablePoint3D)(prm.get(i));
        for (int i = 0; i < p_rotateable.length; i++) 
            p_rotateable[i] = (RotateablePoint3D)(pr.get(i));
        for (int i = 0; i < p_interpolated.length; i++)
            p_interpolated[i] = (InterpolatedPoint3D)(pi.get(i));
        shape = new Shape3D[s.size()];
        for (int i = 0; i < shape.length; i++)
            shape[i] = (Shape3D)(s.get(i));
    }
    
    int point_counter = 0;
    final boolean parse(
        String                         t, 
        StringTokenizer                k, 
        ArrayList<Shape3D>             shapes, 
        ArrayList<Point3D>             points, 
        ArrayList<RotateablePoint3D>   master_rotateable_points,
        ArrayList<RotateablePoint3D>   rotate_points,
        ArrayList<InterpolatedPoint3D> interpoints) {
        switch (t) {
            case "p", "v", "point" -> {
                RotateablePoint3D p = new RotateablePoint3D(k);
                points.add(point_counter,p);
                rotate_points.add(p);
                master_rotateable_points.add(p.clone2());
                point_counter++;
            }
            case "i", "vr", "interpolatedpoint", "intepoint" -> {
                InterpolatedPoint3D p = new InterpolatedPoint3D(k,points);
                points.add(point_counter, p);
                interpoints.add(p);
                point_counter++;
            }
            case "c", "circle"                -> shapes.add(new Circle3D         (k,points));
            case "s", "sphere"                -> shapes.add(new Sphere3D         (k,points));
            case "l", "line"                  -> shapes.add(new Line3D           (k,points));
            case "t", "thickline"             -> shapes.add(new ThickLine3D      (k,points));
            case "h", "shadedline"            -> shapes.add(new ShadedLine3D     (k,points));
            case "j", "shadedcylinder"        -> shapes.add(new ShadedCylinder3D (k,points));
            case "m", "image"                 -> shapes.add(new Image3D          (k,points));
            case "a", "stationaryimage"       -> shapes.add(new StationaryImage3D(k,points));
            case "y", "cylinder"              -> shapes.add(new Cylinder3D       (k,points));
            case "n", "cone"                  -> shapes.add(new Cone3D           (k,points));
            case "o", "outline"               -> shapes.add(new Outline3D        (k,points));
            case "x", "text"                  -> shapes.add(new RotateableText3D (k,points));
            case "r", "pointsphere"           -> shapes.add(new PointSphere3D    (k,points,shapes));
            case "f", "fo", "form"            -> shapes.add(new Form3D           (k,points));
            case "e", "color", "coloredpoint" -> shapes.add(new ColoredPoint3D   (k,points));
            default -> {
                try {
                    RotateablePoint3D p = new RotateablePoint3D(
                        Double.parseDouble(t),
                        Double.parseDouble(k.nextToken()),
                        Double.parseDouble(k.nextToken()));
                    points.add(point_counter,p);
                    rotate_points.add(p);
                    master_rotateable_points.add(p.clone2());
                    point_counter++;
                }
                catch ( NumberFormatException e ) { return false; }
            }



        }
        
        return true;            
    }
    
    public void draw(Graphics g, BufferedImage img, int xo, int yo, int zo)  {
        if (shape.length <= 0) { serr("object is empty"); return; }
        for (int i = 0; i < p_rotateable.length; i++) {
            p_rotateable[i].setx( 2*mp_rotateable[i].getx()+xo);
            p_rotateable[i].sety(-2*mp_rotateable[i].gety()+yo);
            p_rotateable[i].setz( 2*mp_rotateable[i].getz()+zo);
        }        
        for (InterpolatedPoint3D p_interpolated1 : p_interpolated) 
            p_interpolated1.update();

        max_d = shape[0].setDepth();
        min_d = max_d;

        for (int i = 1; i < shape.length; i++) {
            int depth = shape[i].setDepth();
            max_d = Math.max(max_d,depth);
            min_d = Math.min(min_d,depth);
        }

        Shape3D [][] s1,s2 = new Shape3D [10][shape.length];
        int     []   w1,w2 = new int [10];

        w2[0] = shape.length;
        for (int i = 0; i < shape.length; i++) 
            (s2[0][i] = shape[i]).setDepth(shape[i].depth() - min_d);
        max_d -= min_d;
        int maximum_radix = (int)(Math.pow(10,(int)(Math.log(max_d)/Math.log(10))))*10;
        int q;
        for (int r=10; r<=maximum_radix; r*=10) {    
            w1 = w2; 
            w2 = new int [10];
            s1 = s2; 
            s2 = new Shape3D [10][shape.length];
            for (int i = 9; i >= 0; i--) while (w1[i]-- > 0) {
                q = s1[i][w1[i]].depth()%r*10/r;
                try {
                    s2[q][w2[q]] = s1[i][w1[i]];
                    w2[q]++;
                } catch (Exception e) {}
            }
        }
        for (int i=0; i<10; i++) while (w2[i]-->0) s2[i][w2[i]].draw(g,img);
    }
    
    public void rotatex(double t) {
        double 
            s = Math.sin(t),
            c = Math.cos(t);
        double temp;
        for (var m_p_rotateable1 : mp_rotateable) {
            temp = m_p_rotateable1.y;
            m_p_rotateable1.y = c * m_p_rotateable1.y + s * m_p_rotateable1.z;
            m_p_rotateable1.z = c * m_p_rotateable1.z - s * temp;
        }
    }
    
    public void rotatey(double t) {
        double 
            s = Math.sin(t),
            c = Math.cos(t);
        double temp;
        for (var m_p_rotateable1 : mp_rotateable) {
            temp = m_p_rotateable1.x;
            m_p_rotateable1.x = c * m_p_rotateable1.x + s * m_p_rotateable1.z;
            m_p_rotateable1.z = c * m_p_rotateable1.z - s * temp;
        }
    }
    
    public void rotatez(double t) {
        double 
            s = Math.sin(t),
            c = Math.cos(t);
        double temp;
        for (var m_p_rotateable1 : mp_rotateable) {
            temp = m_p_rotateable1.x;
            m_p_rotateable1.x = c * m_p_rotateable1.x + s * m_p_rotateable1.y;
            m_p_rotateable1.y = c * m_p_rotateable1.y - s * temp;
        }
    }
    
    public void shiftx(double dx) {for (var p: mp_rotateable) p.x+=dx;}
    public void shifty(double dy) {for (var p: mp_rotateable) p.y+=dy;}
    public void shiftz(double dz) {for (var p: mp_rotateable) p.z+=dz;}
    public void scalex(double dx) {for (var p: mp_rotateable) p.x*=dx;}
    public void scaley(double dy) {for (var p: mp_rotateable) p.y*=dy;}
    public void scalez(double dz) {for (var p: mp_rotateable) p.z*=dz;}
    public void scale( double a ) {for (var p: mp_rotateable) {p.x*=a;p.y*=a;p.z*=a;}}
    public Point3D [] getPoints() {return point;}
    public RotateablePoint3D [] getMasterRotateablePoints() {return mp_rotateable;}
    public InterpolatedPoint3D [] getMasterInterpolatedPoints() {return p_interpolated;}
    public Shape3D [] getShapes() {return shape;    }
    private static Point3D correspondingpoint(Point3D query, Point3D [] oldpoints, ArrayList newpoints)  {
        for (int i = 0; i < oldpoints.length; i++)
            if (query == oldpoints[i]) return (Point3D)(newpoints.get(i));
        return null;
    }
    public void recenter(int scale) {
        double minx = Double.MAX_VALUE ;
        double miny = Double.MAX_VALUE ;
        double minz = Double.MAX_VALUE ;
        double maxx = Double.MIN_VALUE ;
        double maxy = Double.MIN_VALUE ;
        double maxz = Double.MIN_VALUE ;
        for (var p : this.point ) {
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
        double a = scale / Math.max( maxz - minz , Math.max( maxx - minx , maxy - miny ));
        for ( var p : mp_rotateable ) {
            p.x = ( p.x - dx ) * a ;
            p.y = ( p.y - dy ) * a ;
            p.z = ( p.z - dz ) * a ;
        }
    }
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
        for (; i < shape.length; i++) newshape[i] = shape[i];
        for ( Shape3D s : shapes ) newshape[i++] = s;
        shape = newshape ;
    }
    
    public Object3D hilightpoints(int color) {
        ArrayList<Shape3D> s = new ArrayList<>() ;
        s.addAll(Arrays.asList(shape));
        Color c = new Color( color, true );
        for ( Point3D p : point ) s.add( new Circle3D(p,2,c));
        shape = s.toArray(Shape3D[]::new);
        return this ;
    }   
    
    public Object3D merge(Object3D o) {
        ArrayList<Point3D>             p   = new ArrayList<>();
        ArrayList<RotateablePoint3D>   pr  = new ArrayList<>();
        ArrayList<RotateablePoint3D>   prm = new ArrayList<>();
        ArrayList<InterpolatedPoint3D> pi  = new ArrayList<>();
        ArrayList<Shape3D>             s   = new ArrayList<>();
        
        prm.addAll(Arrays.asList(mp_rotateable));
        pi.addAll(Arrays.asList(p_interpolated));
        pr.addAll(Arrays.asList(p_rotateable));
        p.addAll(Arrays.asList(point));
        s.addAll(Arrays.asList(shape));
        prm.addAll(Arrays.asList(o.mp_rotateable));
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
