package rendered3D;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.util.ArrayList;

public class Object3D {

    public static final double radix_percision_constant = 100;
									
	public static class RotateablePoint3D implements Point3D {
		
		public double x,y,z;
		
		public RotateablePoint3D(double X, double Y, double Z) {
			x = X;
			y = Y;
			z = Z;
		}

		public RotateablePoint3D(StringTokenizer k) 
		{
			this(	Double.parseDouble(k.nextToken()),
					Double.parseDouble(k.nextToken()),
					Double.parseDouble(k.nextToken()));
		}
		
		public RotateablePoint3D(Point3D p) {
			x = p.getx();
			y = p.gety();
			z = p.getz();
		}
		
		public Point3D clone2() {
			return new RotateablePoint3D(this);
		}
		
		public double getx() {
			return x;
		}
		public double gety() {
			return y;
		}
		public double getz() {
			return z;
		}
		public void setx(double n) {
			x = n;
		}
		public void sety(double n) {
			y = n;
		}
		public void setz(double n) {
			z = n;
		}
		
		public String toString() {
			return "R: <"+super.toString().substring(27)+" "+x+", "+y+", "+z+">";
		}
	}								
	
	public static class InterpolatedPoint3D implements Point3D {
		
		private Point3D [] points;
		private double [] dd;
		public double x,y,z;
		
		public InterpolatedPoint3D(Point3D [] p) {
			points = p;
			initd();
			update();
		}
		
		void initd() {
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
		
		public InterpolatedPoint3D(StringTokenizer k, ArrayList allpoints) 
		{
			ArrayList newpoints = new ArrayList();
			while (k.hasMoreTokens()) 
				newpoints.add(allpoints.get(Integer.parseInt(k.nextToken())-1));
			points = new Point3D[newpoints.size()];
			for (int i = 0; i < points.length; i++)
				points[i] = (Point3D)(newpoints.get(i));
			initd();
		}
		
		public Point3D clone2() {
			return new InterpolatedPoint3D(points);
		}
		
		public Point3D createclone(ArrayList newpoints, Point3D[] point) {//clonedrotatedpoints, ArrayList clonedinterpolatedpoints) {
			Point3D [] newclonedpoints = new Point3D[points.length];
			for (int i = 0; i < points.length; i++)
			{
				//#TODO: convert to while
					for (int j = 0; j < point.length; j++) 
					{
						//Sxystem.out.println("\t\t"+points[i]+" :: "+point[j]);
						if (point[j] == points[i]) 
						{
							newclonedpoints[i] = (Point3D)(newpoints.get(j));//clonedrotatedpoints.get(j));
							break;
						}
					}
					//if (newclonedpoints[i] == null) {
					//	Sxystem.out.println("!!"+points[i]);
					//	newclonedpoints[i] = points[i];
					//}
			}
			return new InterpolatedPoint3D(newclonedpoints);
		}
		
		public void update() {
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
		
		public double getx() {
			return x;
		}
		public double gety() {
			return y;
		}
		public double getz() {
			return z;
		}
		
		public String toString() {
			String s = "I: <"+super.toString().substring(29);
			for (int i = 0; i < points.length; i++)
				s += " "+points[i].toString();
			return s+">";
		}
	}
	
	public class Generic3D {
		
		int depth;
		int r,g,b,a,rgb;
		
		public int setDepth(int d) {
			return depth = d;
		}

		public int depth() {
			return depth;
		}
		
		public void setColor(Color c) {
			r = c.getRed();
			g = c.getGreen();
			b = c.getBlue();
			a = c.getAlpha();
			rgb = 65536*r+256*g+b;
		}
		
		public void setColor(int r, int g, int b, int a) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
			rgb = 65536*r+256*g+b;
		}
		
		public Color getColor() 
		{
			return new Color(r,g,b,a);
		}
		
		public int[] getRGBA() 
		{
			int [] temp = {r,g,b,a};
			return temp;
		}
		
		public int getComponentRGBA()
		{
			double s = (double)depth/max_d;
			return (new Color(
					(int)(s*r),
					(int)(s*g),
					(int)(s*b),
					a)).getRGB();
		}
		
		public int getUnshadedRGBA()
		{
			return rgb;
		}
		
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
	
	public class StationaryImage3D extends Generic3D implements Shape3D
	{
		Point3D p;
		Image b;
		
		public StationaryImage3D(Point3D p, Image b)
		{
			this.p = p;
			this.b = b;
		}
		
		public StationaryImage3D(StringTokenizer k, ArrayList a)
		{
			p = (Point3D)(a.get(Integer.parseInt(k.nextToken())-1));
			b = (new ImageIcon(Object3D.class.getResource(k.nextToken()))).getImage();
		}
		
		public int setDepth()
		{
			return depth = (int)(p.getz()*radix_percision_constant);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return new StationaryImage3D(correspondingpoint(p,oldpoints,newpoints),b);
		}
		
		public void draw(Graphics g, BufferedImage i)
		{
			g.drawImage(b,(int)p.getx(),(int)p.gety(),null);
		}
	}
	
	public class RotateableText3D extends Generic3D implements Shape3D
	{
		Point3D p;
		String message;
		
		public RotateableText3D(Point3D n, int rgb, String s)
		{
			p = n;
			message = s;
			setColor(new Color(rgb));
		}
		
		public RotateableText3D(Point3D n, Color c, String s)
		{
			p = n;
			setColor(c);
		}
		
		public RotateableText3D(StringTokenizer k, ArrayList a)
		{
			p = (Point3D)(a.get(Integer.parseInt(k.nextToken())-1));
			message = k.nextToken();
			if (k.nextToken() != null)
				setColor(parseColor(k));
			else setColor(Color.WHITE);
		}
		
		public int setDepth()
		{
			return depth = (int)(p.getz()*radix_percision_constant);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return new RotateableText3D(correspondingpoint(p,oldpoints,newpoints),getUnshadedRGBA(),message);
		}
		
		public void draw(Graphics g, BufferedImage b)
		{
			applyColor(g);
			g.drawString(message,(int)p.getx(),(int)p.gety());
		}
	}
	
	public class ColoredPoint3D extends Generic3D implements Shape3D
	{
		Point3D p;
		
		public ColoredPoint3D(Point3D n, int rgb)
		{
			p = n;
			setColor(new Color(rgb));
		}
		public ColoredPoint3D(Point3D n, Color c)
		{
			p = n;
			setColor(c);
		}
		
		public ColoredPoint3D(StringTokenizer k, ArrayList a)
		{
			p = (Point3D)(a.get(Integer.parseInt(k.nextToken())-1));
			setColor(parseColor(k));
		}
		
		public int setDepth()
		{
			return depth = (int)(p.getz()*radix_percision_constant);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return new ColoredPoint3D(correspondingpoint(p,oldpoints,newpoints),getUnshadedRGBA());
		}
		
		public void draw(Graphics g, BufferedImage b)
		{
			try{b.setRGB((int)p.getx(),(int)p.gety(),getComponentRGBA());}catch(Exception e){}
		}
	}
	
	public class PointSphere3D extends ColoredPoint3D implements Shape3D
	{
		Point3D [] surfacePoint;
		
		public PointSphere3D(StringTokenizer k, ArrayList a, ArrayList s)
		{
			super(k,a);
			
			double pi2 = Math.PI/2;
			double phi = -pi2;
			double theta = 0;
			double r =  Double.parseDouble(k.nextToken());
			double f  = (Math.sqrt(5)-1)/2;
			double surface = Math.PI*r*r/40;
			for (int x = 0; x < surface; x++)
			{
				theta = 2*Math.PI*Math.random();
				phi = Math.acos(2*Math.random()-1)-pi2;
				Point3D temp = new RotateablePoint3D(
					p.getx()+r*Math.cos(theta)*Math.cos(phi),
					p.gety()+r*Math.sin(phi),
					p.getz()+r*Math.sin(theta)*Math.cos(phi));
				s.add(new ColoredPoint3D(temp,getUnshadedRGBA()));
				a.add(temp);
			}
		}
		
		public void draw(Graphics g, BufferedImage b){}
	}
	
	public class Image3D extends Generic3D implements Shape3D
	{
		int w,h;
		Point3D point0, point1, point2, point3;
		int[][] rgb;
		
		public Image3D(Point3D[] p, BufferedImage b)
		{
			point0 = p[0];
			point1 = p[1];
			point2 = p[2];
			point3 = p[3];
			w = b.getWidth(null);
			h = b.getHeight(null);
			initRGB(b);
		}
		
		public Image3D(Point3D[] p, String b)
		{
			point0 = p[0];
			point1 = p[1];
			point2 = p[2];
			point3 = p[3];
			
			Image i = (new ImageIcon(Object3D.class.getResource(b))).getImage();
			BufferedImage image = new BufferedImage(i.getWidth(null),i.getHeight(null),BufferedImage.TYPE_INT_ARGB);
			(image.getGraphics()).drawImage(i,0,0,null);
			w = image.getWidth(null);
			h = image.getHeight(null);
			initRGB(image);
		}
		
		public Image3D(Point3D p0,Point3D p1,Point3D p2,Point3D p3,int [][] newrgb)
		{
			point0 = p0;
			point1 = p1;
			point2 = p2;
			point3 = p3;
			rgb = newrgb;
		}
		
		public Image3D(StringTokenizer k, ArrayList p)
		{
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
		
		public void initRGB(BufferedImage image) {
			rgb = new int[w][h];
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					rgb[x][y] = image.getRGB(x,y);
				}
			}
		}
		
		public int setDepth()
		{
			return depth = (int)((point0.getz()+point1.getz()+point2.getz()+point3.getz())/4*radix_percision_constant);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return new Image3D(
				correspondingpoint(point0,oldpoints,newpoints),
				correspondingpoint(point1,oldpoints,newpoints),
				correspondingpoint(point2,oldpoints,newpoints),
				correspondingpoint(point3,oldpoints,newpoints),rgb);
		}
		
		Point3D p0,p1,p2,p3,p4,p5;
		double dy5, dy3;
		public void draw(Graphics g, BufferedImage image_buffer) 
		{	
			double x0 = point0.getx(), x1 = point1.getx(), x2 = point2.getx(), x3 = point3.getx();
			if (x0 < x2)
				if (x1 < x3)
					if (x0 < x1)		{p0 = point0; p1 = point1; p4 = point3;
						if (x2 < x3)	{p2 = point2; p3 = point3; p5 = point0;
											dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
											dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
						else 			{p2 = point3; p3 = point2; p5 = point1;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
					else				{p0 = point1; p1 = point0; p4 = point2;
						if (x2 < x3)	{p2 = point2; p3 = point3; p5 = point0;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
						else 			{p2 = point3; p3 = point2; p5 = point1;
											dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
											dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}}
				else if (x0 < x3)		{p0 = point0; p1 = point3; p4 = point1;
						if (x2 < x1)	{p2 = point2; p3 = point1; p5 = point0;
											dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
											dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
						else 			{p2 = point1; p3 = point2; p5 = point3;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
					else				{p0 = point3; p1 = point0; p4 = point2;
						if (x2 < x1)	{p2 = point2; p3 = point1; p5 = point0;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
						else 			{p2 = point1; p3 = point2; p5 = point3;
											dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
											dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}}
			else if (x1 < x3) 
					if (x2 < x1) 		{p0 = point2; p1 = point1; p4 = point3;
						if (x0 < x3)	{p2 = point0; p3 = point3; p5 = point2;
											dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
											dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
						else 			{p2 = point3; p3 = point0; p5 = point1;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
					else 				{p0 = point1; p1 = point2; p4 = point0;
						if (x0 < x3) 	{p2 = point0; p3 = point3; p5 = point2;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
						else 			{p2 = point3; p3 = point0; p5 = point1;
											dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
											dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}}
				else if (x2 < x3) 		{p0 = point2; p1 = point3; p4 = point1;
						if (x0 < x1) 	{p2 = point0; p3 = point1; p5 = point2;
											dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());
											dy3 = (p2.gety()-p1.gety())/(p2.getx()-p1.getx());}
						else 			{p2 = point1; p3 = point0; p5 = point3;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}}
					else 				{p0 = point3; p1 = point2; p4 = point0;
						if (x0 < x1)	{p2 = point0; p3 = point1; p5 = point2;
											dy3 = dy5 = (p3.gety()-p5.gety())/(p3.getx()-p5.getx());}
						else 			{p2 = point1; p3 = point0; p5 = point3;
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
			m0x = x*m0, m1x = x*m1, m2x = x*m2, m3x = x*m3,
			m0p1x = p1.getx()*m0, m1p1x = p1.getx()*m1, m2p1x = p1.getx()*m2, m3p1x = p1.getx()*m3;
			
			if (dy1<dy2) {
				for (; x < p1.getx()-1; x++) {
					for (int y = (int)y1; y <= y2; y++)
						try{image_buffer.setRGB(x,y,rgb	[(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
														[(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
						}catch(Exception e){}
					y1 += dy1;	y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
				
				y1 = p1.gety();
				for (; x < p2.getx()-1; x++) {
					for (int y = (int)y1; y <= y2; y++)
						try{image_buffer.setRGB(x,y,rgb	[(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
														[(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
						}catch(Exception e){}
					y1 += dy3;	y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
			}	
			else {
				for (; x < p1.getx()-1; x++) {
					for (int y = (int)y2; y <= y1; y++)
						try{image_buffer.setRGB(x,y,rgb	[(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
														[(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
						}catch(Exception e){}
					y1 += dy1;	y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
				y1 = p1.gety();
				for (; x < p2.getx()-1; x++) {
					for (int y = (int)y2; y <= y1; y++)
						try{image_buffer.setRGB(x,y,rgb	[(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
														[(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
						}catch(Exception e){}
					y1 += dy3;	y2 += dy2; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
			}		
			
			if (p4 == p3) y1 = y2; 
			y2 = p2.gety();
			if (y1<y2) 
				for (; x < p3.getx()-1; x++) {
					for (int y = (int)y1; y <= y2; y++)
						try{image_buffer.setRGB(x,y,rgb	[(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
														[(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
						}catch(Exception e){}
					y1 += dy5;	y2 += dy6; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
			else 
				for (; x < p3.getx()-1; x++) {
					for (int y = (int)y2; y <= y1; y++)
						try{image_buffer.setRGB(x,y,rgb	[(int)(wr0/(r0-(m2x-y+c2)/(m0x-y+c0)))]
														[(int)(hr1/(r1-(m3x-y+c3)/(m1x-y+c1)))]);
						}catch(Exception e){}
					y1 += dy5;	y2 += dy6; m0x += m0; m1x += m1; m2x += m2; m3x += m3;}
		}
	}

	//public class PictureCube3D extends Generic3D implements Shape3D {}

	public class Circle3D extends Generic3D implements Shape3D
	{
		Point3D a;
		double size;
		
		public Circle3D(Point3D A, double SIZE, Color c) {
			a = A;
			size = SIZE;
			setColor(c);
		}
		
		public Circle3D(StringTokenizer k, ArrayList p)
		{
			a = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			size = Integer.parseInt(k.nextToken());
			if (k.hasMoreTokens())
			{
				k.nextToken();
				setColor(parseColor(k));
			}
		}
		
		public Point3D getPoint()
		{
			return a;
		}
		
		public int setDepth() {
			return super.depth = (int)(a.getz() * radix_percision_constant);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return new Circle3D(correspondingpoint(a,oldpoints,newpoints),size,getColor());
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) {
			int s = (int)(2*size);
			applyColor(g);
			g.drawOval((int)(a.getx()-size),(int)(a.gety()-size),s,s);
		}
	}
	
	public class Sphere3D extends Circle3D
	{
		public Sphere3D(Point3D A, double SIZE, Color c) 
		{
			super(A,SIZE,c);
		}
		
		public Sphere3D(StringTokenizer k, ArrayList p)
		{
			super(k,p);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return new Sphere3D(correspondingpoint(a,oldpoints,newpoints),size,getColor());
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			int s = (int)(2*size);
			applyColor(g);
			g.fillOval((int)(a.getx()-size),(int)(a.gety()-size),s,s);
		}
	}
	
	public class Line3D extends Generic3D implements Shape3D {
		
		public Point3D a,b;
		
		public Line3D(){
		}
		
		public Line3D(Point3D A, Point3D B) 
		{
			a = A;
			b = B;
			setColor(Color.WHITE);
		}
		
		public Line3D(Point3D A, Point3D B, Color c) 
		{
			a = A;
			b = B;
			setColor(c);
		}
		
		public Line3D(StringTokenizer k, ArrayList p)
		{
			a = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			b = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			if (k.hasMoreTokens())
			{
				k.nextToken();
				setColor(parseColor(k));
			}
		}
		
		public int setDepth() {
			return super.depth = (int)((a.getz()+b.getz())/2.0 * radix_percision_constant);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return new Line3D(
				correspondingpoint(a,oldpoints,newpoints),
				correspondingpoint(b,oldpoints,newpoints),
				getColor());
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			//Sxystem.out.println("line "+(int)a.getx()+" "+(int)a.gety()+" "+(int)b.getx()+" "+(int)b.gety());
			applyColor(g);
			g.drawLine((int)a.getx(),(int)a.gety(),(int)b.getx(),(int)b.gety());
		}
		
	}
	
	public class ThickLine3D extends Line3D
	{
		int 
			thickness, 
			halfway, 
			n;
		
		public ThickLine3D(Point3D A, Point3D B, Color c, double t)
		{
			super(A,B,c);
			thickness = (int)(t*2);
			halfway = thickness/2;
			n = halfway-thickness+1;
		}
		
		public ThickLine3D(StringTokenizer k, ArrayList p)
		{
			a = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			b = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			thickness = Integer.parseInt(k.nextToken());
			halfway = thickness/2;
			n = halfway-thickness+1;
			if (k.hasMoreTokens())
			{
				k.nextToken();
				setColor(parseColor(k));
			}
			else setColor(Color.GRAY);
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			applyColor(g);
			for (int i = n; i <= halfway; i++)
					g.drawLine((int)a.getx()-n,(int)a.gety()-i,(int)b.getx()-n,(int)b.gety()-i);
			for (int i = n; i <= halfway; i++)
					g.drawLine((int)a.getx()-i,(int)a.gety()-n,(int)b.getx()-i,(int)b.gety()-n);
			for (int i = n; i <= halfway; i++)
					g.drawLine((int)a.getx()-halfway,(int)a.gety()-i,(int)b.getx()-halfway,(int)b.gety()-i);
			for (int i = n; i <= halfway; i++)
					g.drawLine((int)a.getx()-i,(int)a.gety()-halfway,(int)b.getx()-i,(int)b.gety()-halfway);
		}
	}
	
	public class ShadedLine3D extends ThickLine3D
	{		
		private int [][] rgba;
		
		private final int 
			color_shading = -100;
		
		public ShadedLine3D(Point3D A, Point3D B, Color c, int t)
		{
			super(A,B,c,t);
			setColor(c);
		}
		
		public ShadedLine3D(StringTokenizer k, ArrayList p)
		{
			super(k,p);
			if (k.hasMoreTokens())
			{
				k.nextToken();
				setColor(parseColor(k));
			}
			else setColor(Color.GRAY);
		}
		
		public void setColor(Color c)
		{	
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
		
		public void applyColor(Graphics G, int i) {
			
			double s = (double)depth/max_d;
			
			G.setColor(
				new Color(
					(int)(Math.max(0,s)*rgba[i][0]),
					(int)(Math.max(0,s)*rgba[i][1]),
					(int)(Math.max(0,s)*rgba[i][2]),
					rgba[i][3]));
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) 
		{	
			for (int i = n; i <= halfway; i++)
			{
				applyColor(g,(i-n)/2);
				g.drawLine((int)a.getx()-n,(int)a.gety()-i,(int)b.getx()-n,(int)b.gety()-i);
				g.drawLine((int)a.getx()-i,(int)a.gety()-n,(int)b.getx()-i,(int)b.gety()-n);
				g.drawLine((int)a.getx()-i,(int)a.gety()-halfway,(int)b.getx()-i,(int)b.gety()-halfway);
				g.drawLine((int)a.getx()-halfway,(int)a.gety()-i,(int)b.getx()-halfway,(int)b.gety()-i);
			}
		}
	}
	
	public class Outline3D extends Generic3D implements Shape3D 
	{
		public Point3D[] p;	
		
		public Outline3D(Point3D[ ] P, Color c) 
		{
			p = P;
			setColor(c);
		}
		
		public Outline3D (StringTokenizer k, ArrayList l) 
		{
			ArrayList a = new ArrayList();
			setColor(Color.WHITE);
			while (k.hasMoreTokens()) 
			{	
				String s = k.nextToken();
				if (s.charAt(0)=='c')
				{
					setColor(parseColor(k));
					break;
				}
				a.add(new Integer(s));
			}	
			p = new Point3D [a.size()];
			
			for (int i = 0; i < p.length; i++)
				p[i] = (Point3D)(l.get(((Integer)(a.get(i))).intValue()-1));
		}
		
		public int setDepth()
		{
			double sum = 0;
			for (int i = 0; i < p.length; i++) if ( p[i] != null ) sum += p[i].getz();
			return super.depth = (int)(sum*radix_percision_constant/p.length);
		}
		
		public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints) {
			return null;
		}
		
		public void draw(Graphics g, BufferedImage image_buffer)
		{
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
	
	public class Form3D extends Outline3D
	{	
		public Form3D(Point3D[ ] P, Color c) 
		{
			super(P,c);
		}
		
		public Form3D (StringTokenizer k, ArrayList l) 
		{
			super(k,l);
		}
	
		public void draw(Graphics g, BufferedImage image_buffer)
		{
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
		
		for (int i = 1; i <= result.length; i++) 
		{
			int ii = i-1;
			result[ii] = new double[(int)(Math.PI*2*i)][2];
			double theta = 0;
			double theta_increment = 1.0/i;
			for (int j = 0; j < result[ii].length; j++)
			{
				result[ii][j][0] = i*Math.cos(theta);
				result[ii][j][1] = i*Math.sin(theta);
				theta+=theta_increment;
			}
		}	
		return result;
	}
	
	public class Cylinder3D extends Line3D
	{
		double radius;
		double [][] perimeter;
		
		Cylinder3D(){}
		
		public Cylinder3D(Point3D a, Point3D b, double r) 
		{
			super(a,b);
			radius = r;
			initPoints();
		}
		
		public Cylinder3D(Point3D a, Point3D b, Color c, double r) 
		{
			super(a,b,c);
			radius = r;
			initPoints();
		}
		
		public Cylinder3D(StringTokenizer k, ArrayList p)
		{
			a = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			b = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			radius = Double.parseDouble(k.nextToken());
			if (k.hasMoreTokens())
			{
				k.nextToken();
				setColor(parseColor(k));
			}
			initPoints();
		}
		
		private void initPoints()
		{
			if (radius >= 0 && radius < allPerimeters.length)   {
				perimeter = allPerimeters[(int)radius];
			//	System.out.println(perimeter+"=="+allPerimeters[(int)radius]);
			}
			else {
				perimeter = new double[(int)(Math.PI*2*radius)][2];
				double theta = 0;
				double theta_increment = 1/radius;
				for (int i = 0; i < perimeter.length; i++)
				{
					perimeter[i][0] = radius*Math.cos(theta);
					perimeter[i][1] = radius*Math.sin(theta);
					theta+=theta_increment;
				}
			}
		}
		
		int p1,p2,p3,p4;
		double piovertwo = Math.PI/2;
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			applyColor(g);
			int i   = (int)((Math.atan2(b.gety()-a.gety(),b.getx()-a.getx())+piovertwo)*radius+perimeter.length);
			int j   = i+perimeter.length;
			int end = i + perimeter.length/2;
			while (i < end)
			{	int 
				index1 = i%perimeter.length,
				index2 = j%perimeter.length;
				p1 = (int)(perimeter[index1][0]+a.getx());
				p2 = (int)(perimeter[index1][1]+a.gety());
				p3 = (int)(perimeter[index2][0]+b.getx());
				p4 = (int)(perimeter[index2][1]+b.gety());
				g.drawLine(p1,p2,p3,p4);
				g.drawLine(p1++,p2++,p3+1,p4+1);
				g.drawLine(p1,p2,p3,p4);
				g.drawLine(p1,p2,p3+1,p4+1);
				i++;
				j--;
			}
		}
		
	}
	
	public class TaperedStem3D extends Cylinder3D {
		
		double radius2;
		double [][] perimeter2;
		
		TaperedStem3D(){}
		
		public TaperedStem3D(Point3D a, Point3D b, double r1, double r2) 
		{
			super(a,b,r1);
			radius2 = r2;
			initPoints();
		}
		
		public TaperedStem3D(Point3D a, Point3D b, Color c, double r1, double r2) 
		{
			super(a,b,c,r1);
			radius2 = r2;
			initPoints();
		}
		
		public TaperedStem3D(StringTokenizer k, ArrayList p)
		{
			a = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			b = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			radius = Double.parseDouble(k.nextToken());
			radius2 = Double.parseDouble(k.nextToken());
			if (k.hasMoreTokens())
			{
				k.nextToken();
				setColor(parseColor(k));
			}
			initPoints();
		}
		
		private void initPoints()
		{
			super.initPoints();
			if (radius2 >= 0 && radius2 < allPerimeters.length)   {
				perimeter2 = allPerimeters[(int)radius2];
			}
			else {
				perimeter2 = new double[(int)(Math.PI*2*radius2)][2];
				double theta = 0;
				double theta_increment = 1/radius2;
				for (int i = 0; i < perimeter2.length; i++)
				{
					perimeter2[i][0] = radius2*Math.cos(theta);
					perimeter2[i][1] = radius2*Math.sin(theta);
					theta+=theta_increment;
				}
			}
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			applyColor(g);
			
			int i1   = (int)((Math.atan2(b.gety()-a.gety(),b.getx()-a.getx())+piovertwo)*radius+perimeter.length);
			int i2   = (int)((Math.atan2(b.gety()-a.gety(),b.getx()-a.getx())+piovertwo)*radius2+perimeter2.length);
			
			int 
			index1 =  i1 % perimeter .length,
			index2 = (i2 + perimeter2.length) % perimeter2.length;
			g.drawLine(	(int)(perimeter[index1][0] + a.getx()),
						(int)(perimeter[index1][1] + a.gety()),
						(int)(perimeter2[index2][0]+ b.getx()),
						(int)(perimeter2[index2][1]+ b.gety()));
			
			index1 = (i1 + perimeter .length/2 - 1) % perimeter .length;
			index2 = (i2 + perimeter2.length/2 + 1) % perimeter2.length;
			g.drawLine(	(int)(perimeter[index1][0] + a.getx()),
						(int)(perimeter[index1][1] + a.gety()),
						(int)(perimeter2[index2][0]+ b.getx()),
						(int)(perimeter2[index2][1]+ b.gety()));
		}
		
		
		
	}
	
	public class HollowCylinder3D extends Cylinder3D {
				
		public HollowCylinder3D(Point3D a, Point3D b, double r) 
		{
			super(a,b,r);
		}
		
		public HollowCylinder3D(Point3D a, Point3D b, Color c, double r) 
		{
			super(a,b,c,r);
		}
		
		public HollowCylinder3D(StringTokenizer k, ArrayList p)
		{
			super(k,p);
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			applyColor(g);
			int color = getComponentRGBA();
			int i   = (int)((Math.atan2(b.gety()-a.gety(),b.getx()-a.getx())+piovertwo)*radius+perimeter.length);
			int j   = i+perimeter.length;
			int end = i + perimeter.length/2;
			
			int 
			index1 = i%perimeter.length,
			index2 = j%perimeter.length;
			g.drawLine(	(int)(perimeter[index1][0]+a.getx()),
						(int)(perimeter[index1][1]+a.gety()),
						(int)(perimeter[index2][0]+b.getx()),
						(int)(perimeter[index2][1]+b.gety()));
			i++;
			j--;
			
			while (i < end-1)
			{	 
				index1 = i%perimeter.length;
				index2 = j%perimeter.length;
				g.drawLine(p1,p2,p3,p4);
				image_buffer.setRGB((int)(perimeter[index1][0]+a.getx()),(int)(perimeter[index1][1]+a.gety()),color);
				image_buffer.setRGB((int)(perimeter[index2][0]+b.getx()),(int)(perimeter[index2][1]+b.gety()),color);
				i++;
				j--;
			}
			
			index1 = i%perimeter.length;
			index2 = j%perimeter.length;
			g.drawLine(	(int)(perimeter[index1][0]+a.getx()),
						(int)(perimeter[index1][1]+a.gety()),
						(int)(perimeter[index2][0]+b.getx()),
						(int)(perimeter[index2][1]+b.gety()));
		}
		
	}
	
	public class SparseCylinder3D extends Cylinder3D {
				
		int skip;
				
		public SparseCylinder3D(Point3D a, Point3D b, double r) 
		{
			super(a,b,r);
		}
		
		public SparseCylinder3D(Point3D a, Point3D b, Color c, double r) 
		{
			super(a,b,c,r);
		}
		
		public SparseCylinder3D(StringTokenizer k, ArrayList p)
		{
			super(k,p);
		}
		
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			applyColor(g);
			int i   = (int)((Math.atan2(b.gety()-a.gety(),b.getx()-a.getx())+piovertwo)*radius+perimeter.length);
			int j   = i+perimeter.length;
			int end = i + perimeter.length/2;
			while (i < end)
			{	int 
				index1 = i%perimeter.length,
				index2 = j%perimeter.length;
				p1 = (int)(perimeter[index1][0]+a.getx());
				p2 = (int)(perimeter[index1][1]+a.gety());
				p3 = (int)(perimeter[index2][0]+b.getx());
				p4 = (int)(perimeter[index2][1]+b.gety());
				g.drawLine(p1,p2,p3,p4);
				i+=2;
				j-=2;
			}
		}
	}
	
	public class Cone3D extends Cylinder3D 
	{
		public Cone3D(StringTokenizer k, ArrayList a) {
			super(k,a);
		}
		
		int p1,p2;
		double piovertwo = Math.PI/2;
		public void draw(Graphics g, BufferedImage image_buffer) 
		{
			applyColor(g);
			int i   = (int)((Math.atan2(b.gety()-a.gety(),b.getx()-a.getx())+piovertwo)*radius+perimeter.length);
			int j   = i+perimeter.length;
			int end = i + perimeter.length/2;
			while (i < end)
			{	int 
				index1 = i%perimeter.length,
				p1 = (int)(perimeter[index1][0]+a.getx());
				p2 = (int)(perimeter[index1][1]+a.gety());
				p3 = (int)(b.getx());
				p4 = (int)(b.gety());
				g.drawLine(p1,p2,p3,p4);
				g.drawLine(p1++,p2++,p3,p4);
				g.drawLine(p1,p2,p3,p4);
				g.drawLine(p1,p2,p3,p4);
				i++;
				j--;
			}
		}
	}
	
	public class ShadedCylinder3D extends Cylinder3D
	{
		private int [][] rgba;
		
		int alpha;
		
		private final int 
			color_shading = -128;
		
		public ShadedCylinder3D(Point3D a, Point3D b, double r) 
		{
			super(a,b,r);
			setColor(Color.gray);
		}
		
		public ShadedCylinder3D(Point3D a, Point3D b, Color c, double r) 
		{
			super(a,b,c,r);
			setColor(c);
		}
		
		public ShadedCylinder3D(Point3D a, Point3D b, int [][] rgba, double r) 
		{
			super(a,b,Color.WHITE,r);
			//super.setColor(c);
			this.rgba = rgba;
			alpha = 255;
		}
		
		public ShadedCylinder3D(StringTokenizer k, ArrayList p)
		{
			a = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			b = (Point3D)(p.get(Integer.parseInt(k.nextToken())-1));
			radius = Double.parseDouble(k.nextToken());
			if (k.hasMoreTokens())
			{
				k.nextToken();
				setColor(parseColor(k));
			}
			super.initPoints();
		}
		
		public void setColor(Color c)
		{	
			super.setColor(c);
			int [] color = super.getRGBA();
				alpha = color[3];
			rgba = new int[(int)(Math.PI * radius)][3];
			for (int i = 0; i < rgba.length; i++)
			{
				double shade = Math.abs((double)i/rgba.length*2-.5)*color_shading;
				
				for (int j = 0; j < 3; j++)
					rgba[i][j] = (int)(Math.min(255,Math.max(0,color[j]+shade)));
			}
		}
		
		int p1,p2,p3,p4;
		final double piovertwo = Math.PI/2;
		public void draw(Graphics g, BufferedImage image_buffer) 
		{	
			int l2 = perimeter.length/2;
			int i = (int)((Math.atan2(b.gety()-a.gety(),b.getx()-a.getx())+piovertwo)*radius)+perimeter.length;
			int j = i+perimeter.length;
			int end = i + l2;
			int c = 0;
			double s = Math.max(0,(double)depth/max_d);
			while (i < end)
			{
				g.setColor(new Color((int)(s*rgba[c][0]),(int)(s*rgba[c][1]),(int)(s*rgba[c][2]),alpha));
				int 
				index1 = i%perimeter.length,
				index2 = j%perimeter.length;
				g.drawLine(
					p1 = (int)(perimeter[index1][0]+a.getx()),
					p2 = (int)(perimeter[index1][1]+a.gety()),
					p3 = (int)(perimeter[index2][0]+b.getx()),
					p4 = (int)(perimeter[index2][1]+b.gety()));
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
	
	Point3D[] point; 						//all points, ever
	RotateablePoint3D  [] p_rotateable; 	//rotateable points, clone of m_p_rotateable
	RotateablePoint3D  [] m_p_rotateable; 	//subset of m_p that can be rotated
	InterpolatedPoint3D[] p_interpolated; 	//subset of m_p that are interpolated
	Shape3D[] shape;
	static int max_d, min_d;
		
	Object3D(){}	
	
	public Object3D(BufferedReader b) {
	
		ArrayList  
			p = new ArrayList(), 
			s = new ArrayList(), 
			pr = new ArrayList(), 
			prm = new ArrayList(), 
			pi = new ArrayList();
		
		StringTokenizer k;
		try 
		{
			String line = b.readLine();
			while (line != null) 
			{
				try 
				{
					k = new StringTokenizer(line);
					String t = k.nextToken().toLowerCase();
					parse(t,k,s,p,prm,pr,pi);
				} 
				catch (Exception e) {
					//Sxystem.out.println(line);
					}
				line = b.readLine();
			}
		} 
		catch (Exception e) {
			//Sxystem.out.println("Error creating 3D object");
			}
		finalize(p,pr,prm,pi,s);
	}
	
	public Object3D smooth() {
		
		ArrayList 
			outlines  = new ArrayList(),					//will store existing outlines
			newshapes = new ArrayList(),					//will store new outlines
			newpoints = new ArrayList(),					//will store clones of all points and new points
			allnewshapes = new ArrayList(),				
			newrotateablepoints = new ArrayList(), 			//will store clones of existing rotateable points
			newmasterrotatreablepoints = new ArrayList(), 	//will store clones of existing master rotateable points
			newinterpolatedpoints = new ArrayList();		//will store clones of existing as well as new interpolated points
			
		for (int i = 0; i < m_p_rotateable.length; i++)
			newmasterrotatreablepoints.add(m_p_rotateable[i].clone2());
			
		for (int i = 0; i < p_rotateable.length; i++)
		{
			RotateablePoint3D p = (RotateablePoint3D)(p_rotateable[i].clone2());
			newrotateablepoints.add(p);
			newpoints.add(p);
		}
		//clones existing interpolated points
		for (int i = 0; i < p_interpolated.length; i++) {
			InterpolatedPoint3D p = (InterpolatedPoint3D)( p_interpolated[i].createclone(newpoints,point));
			newinterpolatedpoints.add(p);
			newpoints.add(p);
		}
		//stores lines as points in an arraylist, one arraylist for each points
		ArrayList [] lines         = new ArrayList[point.length],
		//stores midpoints of the lines in ArrayList lines
					 linemidpoints = new ArrayList[point.length];
		
		//initialize the ArrayLists int lines and linemidpoints, good to go.
		for (int i = 0; i < point.length; i++) 
		{
			lines[i]         = new ArrayList();
			linemidpoints[i] = new ArrayList();
		}
		//look for existing outlines
		for (int i = 0; i < shape.length; i++)
		{
			try 
			{
				//if an outlines ins found:
				Outline3D o = (Outline3D)shape[i];
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
					
					InterpolatedPoint3D midpoint = null;
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
						
						linemidpoints[index        ].add(midpoint);	//store the new midpoint
						linemidpoints[previousindex].add(midpoint);	//crossreferenced.
						newpoints.add(midpoint);						//also to newpoints
						newinterpolatedpoints.add(midpoint);			//and newinterpolatedpoints
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
			} 
			catch (Exception e) 
			{//the shape is not a valid outline or form 3d
			//	e.printStackTrace();
				try{allnewshapes.add(shape[i].cloneto(point,newpoints));}catch (Exception e1){}
			}
		}
		
		//all midpoints have been calculted
		//now to locate and create ne faces/outlines created from interpolations
		for (int i = 0; i < point.length; i++) 
		{
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
					if (match_has_not_been_found) 
					{
						destinationindex++;
						for (int k = 0; k < p.length; k++)
							for (int j = 0; j < destinationindex; j++) 
								if (p2[j] != p[k]) 
									p2[destinationindex] = p[k];
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
	
	private Object3D(ArrayList p,ArrayList pr,ArrayList prm,ArrayList pi,ArrayList s)
	{
		finalize(p, pr, prm, pi, s);
	}
	
	private int indexOf(Point3D p) {
		for (int i = 0; i < point.length; i++)
			if (p == point[i]) return i;
		//Sxystem.out.println(p+"\t");
		return -1;
	}
	
	void finalize(ArrayList p,ArrayList pr,ArrayList prm,ArrayList pi,ArrayList s)
	{
		point = new Point3D[p.size()];
		p_rotateable     = new RotateablePoint3D[pr.size()];
		m_p_rotateable   = new RotateablePoint3D[prm.size()];
		p_interpolated = new InterpolatedPoint3D[pi.size()];
		
//		Sxystem.out.println("all points");
		for (int i = 0; i < point.length; i++)
		{
			point[i] = (Point3D)( p.get(i));
//			Sxystem.out.println(point[i]);
		}
//		Sxystem.out.println("master points");
		for (int i = 0; i < m_p_rotateable.length; i++)
		{
			m_p_rotateable[i] = (RotateablePoint3D)(prm.get(i));
//			Sxystem.out.println(m_p_rotateable[i]);
		}
//		Sxystem.out.println("rotateable points");
		for (int i = 0; i < p_rotateable.length; i++)
		{
			p_rotateable[i] = (RotateablePoint3D)(pr.get(i));
//			Sxystem.out.println(p_rotateable[i]);
		}
//		Sxystem.out.println("interpolated points");
		for (int i = 0; i < p_interpolated.length; i++)
		{
			p_interpolated[i] = (InterpolatedPoint3D)(pi.get(i));
//			Sxystem.out.println(p_interpolated[i]);
		}
		shape = new Shape3D[s.size()];
		for (int i = 0; i < shape.length; i++)
		{ 
			shape[i] = (Shape3D)(s.get(i));
		}
//			Sxystem.out.println("done");
	}
	
	int point_counter = 0;
	boolean parse(
		String t, 
		StringTokenizer k, 
		ArrayList shape_destination, 
		ArrayList point_destination, 
		ArrayList master_rotateable_point_destination,
		ArrayList rotateable_point_destination,
		ArrayList interpolated_point_destination)
	{
					if (t.equals("p") || t.equals("v") || t.equals("point"))
					{
						 RotateablePoint3D p = new RotateablePoint3D(k);
						 point_destination.add(point_counter,p);
						 rotateable_point_destination.add(p);
						 master_rotateable_point_destination.add(p.clone2());
						 point_counter++;
					}
					
					if (t.equals("r") || t.equals("vr") || t.equals("interpolatedpoint") || t.equals("intepoint"))
					{
						 InterpolatedPoint3D p = new InterpolatedPoint3D(k,point_destination);
						 point_destination.add(point_counter, p);
						 interpolated_point_destination.add(p);
						 point_counter++;
					}

					else if (t.equals("c") || t.equals("circle"))
						 shape_destination.add(new Circle3D			(k,point_destination));

					else if (t.equals("s") || t.equals("sphere"))
						 shape_destination.add(new Sphere3D			(k,point_destination));

					else if (t.equals("l") || t.equals("line"))
						 shape_destination.add(new Line3D			(k,point_destination));

					else if (t.equals("t") || t.equals("thickline"))
						 shape_destination.add(new ThickLine3D		(k,point_destination));

					else if (t.equals("h") || t.equals("shadedline"))
						 shape_destination.add(new ShadedLine3D		(k,point_destination));

					else if (t.equals("i") || t.equals("shadedcylinder"))
						 shape_destination.add(new ShadedCylinder3D	(k,point_destination));

					else if (t.equals("m") || t.equals("image"))
						 shape_destination.add(new Image3D			(k,point_destination));

					else if (t.equals("a") || t.equals("stationaryimage"))
						 shape_destination.add(new StationaryImage3D(k,point_destination));

					else if (t.equals("y") || t.equals("cylinder"))
						 shape_destination.add(new Cylinder3D		(k,point_destination));

					else if (t.equals("n") || t.equals("cone"))
						 shape_destination.add(new Cone3D			(k,point_destination));

					else if (t.equals("o") || t.equals("outline"))
						 shape_destination.add(new Outline3D		(k,point_destination));

					else if (t.equals("x") || t.equals("text"))
						 shape_destination.add(new RotateableText3D	(k,point_destination));

					else if (t.equals("r") || t.equals("pointsphere"))
						 shape_destination.add(new PointSphere3D	(k,point_destination,shape_destination));

					else if (t.equals("f") || t.equals("fo") || t.equals("form"))
						 shape_destination.add(new Form3D			(k,point_destination));

					else if (t.equals("e") || t.equals("color") || t.equals("coloredpoint"))
						 shape_destination.add(new ColoredPoint3D	(k,point_destination));
						
                    else try
					{
                        RotateablePoint3D p = new RotateablePoint3D(
                            Double.parseDouble(t),
                            Double.parseDouble(k.nextToken()),
            				Double.parseDouble(k.nextToken()));
						point_destination.add(point_counter,p);
						rotateable_point_destination.add(p);
						master_rotateable_point_destination.add(p.clone2());
						point_counter++;
					}
                    
                    catch ( Exception e ) {
                        return false;
                    }
                    
					return true;			
	}
	
	public void draw(Graphics g, BufferedImage image_buffer, int x_offset, int y_offset, int z_offset) 
	{
		if (shape.length > 0)
		{
			for (int i = 0; i < p_rotateable.length; i++) 
			{
				p_rotateable[i].setx( 2*m_p_rotateable[i].getx()+x_offset);
				p_rotateable[i].sety(-2*m_p_rotateable[i].gety()+y_offset);
				p_rotateable[i].setz( 2*m_p_rotateable[i].getz()+z_offset);
			}		
			for (int i = 0; i < p_interpolated.length; i++) 
				p_interpolated[i].update();

			max_d = shape[0].setDepth();
			min_d = max_d;
				
			for (int i = 1; i < shape.length; i++) 
			{
				int depth = shape[i].setDepth();
				max_d = Math.max(max_d,depth);
				min_d = Math.min(min_d,depth);
			}
			
			Shape3D [][] 
				s1, 
				s2 = new Shape3D [10][shape.length];
			
			int []  
				w1,	
				w2 = new int [10];
			
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
	
	public void rotatex(double t)
	{
		double 
			s = Math.sin(t),
			c = Math.cos(t);
		
		double temp;
		for (int i = 0; i < m_p_rotateable.length; i++) 
		{
			temp = m_p_rotateable[i].y;
			m_p_rotateable[i].y = c * m_p_rotateable[i].y + s * m_p_rotateable[i].z;
			m_p_rotateable[i].z = c * m_p_rotateable[i].z - s * temp;
		}
	}
	
	public void rotatey(double t)
	{
		double 
			s = Math.sin(t),
			c = Math.cos(t);
		
		double temp;
		for (int i = 0; i < m_p_rotateable.length; i++) 
		{
			temp = m_p_rotateable[i].x;
			m_p_rotateable[i].x = c * m_p_rotateable[i].x + s * m_p_rotateable[i].z;
			m_p_rotateable[i].z = c * m_p_rotateable[i].z - s * temp;
		}
	}
	
	public void rotatez(double t)
	{
		double 
			s = Math.sin(t),
			c = Math.cos(t);
		
		double temp;
		for (int i = 0; i < m_p_rotateable.length; i++) 
		{
			temp = m_p_rotateable[i].x;
			m_p_rotateable[i].x = c * m_p_rotateable[i].x + s * m_p_rotateable[i].y;
			m_p_rotateable[i].y = c * m_p_rotateable[i].y - s * temp;
		}
	}
	
	public void shiftx(double dx)
	{
		for (int i = 0; i < m_p_rotateable.length; i++)  
			m_p_rotateable[i].x+=dx;
	}

	public void shifty(double dy)
	{
		for (int i = 0; i < m_p_rotateable.length; i++)  
			m_p_rotateable[i].y+=dy;
	}
	
	public void shiftz(double dz)
	{
		for (int i = 0; i < m_p_rotateable.length; i++)  
			m_p_rotateable[i].z+=dz;
	}
    
	public void scalex(double dx)
	{
		for (int i = 0; i < m_p_rotateable.length; i++)  
			m_p_rotateable[i].x*=dx;
	}

	public void scaley(double dy)
	{
		for (int i = 0; i < m_p_rotateable.length; i++)  
			m_p_rotateable[i].y*=dy;
	}
	
	public void scalez(double dz)
	{
		for (int i = 0; i < m_p_rotateable.length; i++)  
			m_p_rotateable[i].z*=dz;
	}
    
    public void scale( double a ) {
        for ( RotateablePoint3D p : m_p_rotateable ) {
            p.x *= a ;
            p.y *= a ;
            p.z *= a ;
        }
    }
		
	public Point3D [] getPoints()
	{
		return point;
	}
	
	public RotateablePoint3D [] getMasterRotateablePoints()
	{
		return m_p_rotateable;
	}
	
	public InterpolatedPoint3D [] getMasterInterpolatedPoints()
	{
		return p_interpolated;
	}
	
	public Shape3D [] getShapes()
	{
		return shape;
	}
	
	private static Point3D correspondingpoint(Point3D query, Point3D [] oldpoints, ArrayList newpoints) 
	{
		for (int i = 0; i < oldpoints.length; i++)
			if (query == oldpoints[i]) return (Point3D)(newpoints.get(i));
		return null;
	}
       
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
    
    public void nearest( double thresh ) {
        ArrayList<Shape3D> shapes = new ArrayList<Shape3D>() ;
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
		for (; i < shape.length; i++)
		{ 
			newshape[i] = shape[i];//Shape3D)(shapes.get(i));
		}
        for ( Shape3D s : shapes ) {
            newshape[i] = s ;
            i ++ ;
        }
        shape = newshape ;
    }

    public Object3D convexHull() {
      
        
        
        return null;
    }
    
    public Object3D hilightpoints(int color) {
        ArrayList<Shape3D> s = new ArrayList<Shape3D>() ;
        for ( Shape3D b : shape ) s.add(b);
        
        Color c = new Color( color, true );
        for ( Point3D p : point ) {
            s.add( new Circle3D(p,2,c));
        }
        shape = s.toArray(new Shape3D[s.size()]);
        return this ;
    }   
    
	public Object3D merge(Object3D o)
	{
        ArrayList p = new ArrayList() ;
        ArrayList pr = new ArrayList() ;
        ArrayList prm = new ArrayList() ;
        ArrayList pi = new ArrayList() ;
        ArrayList s = new ArrayList() ;
        
        for ( Object b : m_p_rotateable ) prm.add(b);
        for ( Object b : p_interpolated ) pi.add(b);
        for ( Object b : p_rotateable   ) pr.add(b);
        for ( Object b : point          ) p.add(b);
        for ( Object b : shape          ) s.add(b);
        for ( Object b : o.m_p_rotateable ) prm.add(b);
        for ( Object b : o.p_interpolated ) pi.add(b);
        for ( Object b : o.p_rotateable   ) pr.add(b);
        for ( Object b : o.point          ) p.add(b);
        for ( Object b : o.shape          ) s.add(b);
        
		return new Object3D(
			p, 
			pr, 
			prm, 
			pi, 
			s);
	}
}