/* Kernel.java
 *
 * The plan is to re-implement perceptron with most of the fuzzy stuff in
 * Jython. I'm not sure about this plan. Certainly the main rendering must
 * be handled in Java proper.
 */

#define LENGTH (WIDTH*HEIGHT)
#define W_1    (HEIGHT-1)
#define H_1    (WIDTH-1)
#define W5     (W_1<<5)
#define H5     (H_1<<5)
#define W_52   (W5<<1)
#define H_52   (H5<<1)
	
#define R32M  0x00ff0000
#define G32M  0x0000ff00
#define B32M  0x000000ff
#define V32M  0x00ff00ff
#define D32M  0x00800080
#define S32M  0x00008000
#define W32M  0x00ffffff
#define I32M  0x00010101
#define O32M  0x00fefefe

#define R15M  0x0000001f
#define G15M  0x00007C00
#define B15M  0x01f00000
#define W15M  0x01f07C1f
#define D15M  0x00804008
#define I15M  0x00100401
#define W1532M 0x00f8f8f8

#define B32(c) (c     & 0xff)
#define G32(c) (c>> 8 & 0xff)
#define R32(c) (c>>16 & 0xff)

#define B15(c) (c     & 0x1f)
#define G15(c) (c>>10 & 0x1f)
#define R15(c) (c>>20 & 0x1f)

#define PACKRGB32(r,g,b) (r<<16|g<<8|b)
#define COLOR32TO15(c) (c>>3&R15M|c>>1&G15M|c<<1&B15M)
#define COLOR15TO32(c) PACKRGB32((R15(c)<<3),(G15(c)<<3),(B15(c)<<3))

#define CLIRP15(a,c1,c2)  a*c1+(32-a)*c2+D15M>>5&W15M

import java.awt.Color;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.concurrent.atomic.AtomicInteger;

public class KERNELVERSION 
{
	public static class ColorFilterParameters
	{
		public int HUE;
		public int SATURATION;
		public int LIGHTEN;
		public int DARKEN;
		public int BRIGHTNESS;
		public int NONLINEARCONTRAST;
		public int CONTRAST;
		public int WEIGHT;
		public int INVERT;
		public int ENABLE;
	}
	
	static final public ColorFilterParameters Post        = new ColorFilterParameters();
	static final public ColorFilterParameters Recurrent   = new ColorFilterParameters();
	static final public ColorFilterParameters Homeostatic = new ColorFilterParameters();

	static final Random rng = new Random( 19580427 );
	
	// parameters
	public static int HTAU;
	public static int MOTIONBLUR;
	public static int BLUR;
	public static int SHARPEN;
	public static int NOISE;
	public static int T1;
	public static int T2;
	public static int T3;
	public static int T4;
	public static int T5;
	public static int T6;
	public static int[] mapping;
	
	// configuration variables and internal data buffers
	static int [] din,dtmp,dout;
	public static BufferedImage out;
	public static BufferedImage disp;

	public static void prepare()
	{	
		out  = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		disp = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		din  = new int[LENGTH];
		dtmp = new int[LENGTH];
		dout = new int[LENGTH];
	}
	
	public static void stat()
	{
		DataBuffer bufferOut  = out .getRaster().getDataBuffer();
		int [] bufferIn = din;
		for (int i=0; i<LENGTH; i++)
		{
			int c = bufferOut.getElem(i);
			bufferIn[i] = COLOR32TO15(c);
		}
	}

	public static void render(int threadnumber, int nthreads)
	{
		int [] bufferIn  = din;
		
		DataBuffer bufferOut  = out .getRaster().getDataBuffer();
		DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
		
		int random = rng.nextInt();
		
		float[] HSV = {0f,0f,0f};
		
		int Rstart = HEIGHT/nthreads*threadnumber;
		int Rstop  = threadnumber==nthreads-1? HEIGHT: HEIGHT/nthreads*(threadnumber+1);
		int istart = Rstart*WIDTH;
		int istop  = Rstop*WIDTH;
		
		int m = Recurrent.BRIGHTNESS*Recurrent.CONTRAST + (I15M<<2) >> 3 & I15M*0x7f;
		
		for (int i=istart; i<istop; i++)
		{
			// look up mapping
			int x = mapping[2*i];
			int y = mapping[2*i+1];
			
			// apply affine transform using fixed point arithemetic
			int x2 = T1*x+T2*y+T3 + (1<<12) >> 13;
			int y2 = T4*x+T5*y+T6 + (1<<12) >> 13;
			
			// boundary wrapping and separating integer and fractional components
            while (x2<W_52) x2+=W_52;
            while (y2<H_52) y2+=H_52;
            x2  = (x2/W5&1)==1? x2%W5 : (W5-1)-x2%W5;
            y2  = (y2/H5&1)==1? y2%H5 : (H5-1)-y2%H5;
			int ax = x2&0x1f;
			int ay = y2&0x1f;
			x = x2>>5;
			y = y2>>5;
			x2 = x+1;
			y2 = y+1;
			y *= WIDTH;
			y2*= WIDTH;
			
			// linear interpolation of colors
			int c1 = bufferIn[x +y];
			int c2 = bufferIn[x2+y];
			int c3 = CLIRP15(ax,c2,c1);
			int c4 = bufferIn[x +y2];
			int c5 = bufferIn[x2+y2];
			int c6 = CLIRP15(ax,c5,c4);
			int c  = CLIRP15(ay,c6,c3);
			
			// recurrent color effects
			if (Recurrent.WEIGHT>0)
			{
				/*
				HUE
				SATURATION
				LIGHTEN
				DARKEN
				BRIGHTNESS
				NONLINEARCONTRAST
				CONTRAST
				WEIGHT
				INVERT
				ENABLE
				*/
				c = (c*Recurrent.CONTRAST + (I15M<<2) >> 3 & I15M*0x7f) + (Recurrent.BRIGHTNESS<<2) + (I15M*0x100-m);
				int mask = c>>8&I15M|c>>9&I15M;
				c = (c&mask*0x3ff)|(I15M*256&(mask^I15M)*0x3FF);
				c = c-256*I15M;
				c = (c | (c>>7&I15M|c>>8&I15M)*0x7f) & W15M;

				if (0!=Recurrent.INVERT)
				{
					int inverse = ~c&W15M;
					c = CLIRP15(Recurrent.INVERT,inverse,c);
				}
			}
			// Additional effects
			if (0!=MOTIONBLUR) 
			{
				int previous = bufferIn[i];
				c = CLIRP15(MOTIONBLUR,previous,c);
			}
			if (0!=NOISE)
			{
				random^=random>>19^random<<11^random<<3;
				int noise = random & W15M;
                c = CLIRP15(NOISE,noise,c);
			}
			
			c = COLOR15TO32(c);
			bufferOut.setElem(i,c);
			//bufferDisp.setElem(i,c);
		}
	}
}










