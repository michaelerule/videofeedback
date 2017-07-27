/* Kernel.java
 *
 * The plan is to re-implement perceptron with most of the fuzzy stuff in
 * Jython. I'm not sure about this plan. Certainly the main rendering must
 * be handled in Java proper.
 * 
 * This includes: 
 * -- map lookups
 * -- image lookups
 * -- compositing
 * 
 * The place in my heart for psychedelic rock will never harden.
 * 
 * Vector graphics may be handeled by Jython. User interface configuration as
 * well. The goal is to reduce the size and complexity of the code.
 * 
 * ----------------------------------------------------------------------------
 * 
 * 
 */

#define LENGTH (WIDTH*HEIGHT)
#define W_1 (HEIGHT-1)
#define H_1 (WIDTH-1)
#define W_3 (HEIGHT-3)
#define H_3 (WIDTH-3)
#define W8 (W_3*256)
#define H8 (H_3*256)
#define W_82 (W8*2)
#define H_82 (H8*2)

#define FIXED 0x100
#define FLIRP(a,x1,x2)  (a*(x1)+(FIXED-a)*(x2))

// Java likes the int ARGB format. We can do some color operations on this
// format efficiently, but adding and combining the colors requires some
// separation of channels to avoid overflow issues. Linear interpolation can
// still do some tricks but is more verbose.
#define R32M  0xff0000
#define G32M  0x00ff00
#define B32M  0x0000ff
#define V32M  0xff00ff
#define D32M  0x800080
#define S32M  0x008000
#define W32M  0xffffff
#define I32M  0x010101
#define O32M  0xfefefe

#define B32(c)   (c    &0xff)
#define G32(c)   (c>> 8&0xff)
#define R32(c)   (c>>16&0xff)

#define PACKRGB32(r,g,b) (int)((r)<<16|(g)<<8|(b))
#define CLIRP32(a,x,y) FLIRP(a,x&V32M,y&V32M)+D32M>>8&V32M|FLIRP(a,x&G32M,y&G32M)+S32M>>8&G32M

#define CLIPCOLOR(v) ((v)>0xff?0xff:(v)<0?0:(v))
#define CLIPROUNDED(v) v>0xffff?0xff:v<0?0:v>>8

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

	// secret stuff
	static int gRMEAN,gGMEAN,gBMEAN,gRVAR,gGVAR,gBVAR,gRMIN,gRMAX,gBMIN,gBMAX,gGMIN,gGMAX,RSCALE,GSCALE,BSCALE;
	static int RMEAN=0,GMEAN=0,BMEAN=0;
	static int RVAR=0,GVAR=0,BVAR=0;
	static int RMIN=0xff, RMAX=0, GMIN=0xff, GMAX=0, BMIN=0xff, BMAX=0;
	
	public static void prepare()//(int w, int h)
	{	
		out  = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		disp = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		din  = new int[LENGTH];
		dtmp = new int[LENGTH];
		dout = new int[LENGTH];
	}
	/*
	public static void convolve(int nthreads)
		final AtomicInteger finished = AtomicInteger(0);
		
		Kernel.convolveStage1(a,b)
		finished.incrementAndGet()
		finished.notify()
		while (finished.get()<NTHREADS) synchronized(finisehd) finished.wait()
		Kernel.convolveStage2(a,b)
	*/
	public static void convolveStage1(int threadnumber, int nthreads)
	{
		if (0==BLUR&&0==SHARPEN) return;
		
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		int curr, next, prev, pair;
		
		int Cstart = WIDTH/nthreads*threadnumber;
		int Cstop  = threadnumber==nthreads-1? WIDTH: WIDTH/nthreads*(threadnumber+1);
		
		for (int C=Cstart; C<Cstop; C++) 
		{
			next = bufferOut.getElem(C+WIDTH) & O32M;
			curr = bufferOut.getElem(C  ) & O32M;
			bufferTmp[C] = pair = prev = next+curr>>1 & O32M;
			for (int R=1; R<H_1; R++) 
			{
				curr = next; next = bufferOut.getElem(C+(R+1)*WIDTH) & O32M;
				prev = pair; pair = next+curr>>1 & O32M;
				bufferTmp[C+R*WIDTH]=prev+pair>>1;
			}
			bufferTmp[C+H_1*WIDTH]=pair>>1;
		}
	}
		
	public static void convolveStage2(int threadnumber, int nthreads)
	{
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		
		int Rstart = HEIGHT/nthreads*threadnumber;
		int Rstop  = threadnumber==nthreads-1? HEIGHT: HEIGHT/nthreads*(threadnumber+1);
		int Istart = Rstart*WIDTH;
		int Istop  = Rstop*WIDTH;
		
		if (0!=BLUR||0!=SHARPEN)
		{
			int curr, next, prev, pair;
			for (int R=Rstart; R<Rstop; R++) 
			{
				next = bufferTmp[R*WIDTH+1] & O32M;
				curr = bufferTmp[R*WIDTH  ] & O32M;
				bufferIn[R*WIDTH] = pair = prev = next+curr>>1 & O32M;
				for (int C=1; C<W_1; C++) 
				{
					curr = next; next = bufferTmp[R*WIDTH+C+1] & O32M;
					prev = pair; pair = next+curr>>1 & O32M;
					bufferIn[R*WIDTH+C]= prev+pair>>1;
				}
				bufferIn[R*WIDTH+W_1]=pair;
			}
			
			if (BLUR!=0) for (int i=Istart;i<Istop;i++)
			{
				int c1 = bufferIn[i];
				int c2 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(BLUR,c1,c2);
			}
			if (SHARPEN!=0) for (int i=Istart;i<Istop;i++)
			{
				int c2 = ~bufferIn[i]&W32M;
				int c1 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(SHARPEN,c2,c1);
			}
		}
		else for (int i=Istart;i<Istop;i++) bufferIn[i] = bufferOut.getElem(i);		
	}
		
	public static void stat()
	{ 
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		
		RMEAN=GMEAN=BMEAN=0;
		RVAR=GVAR=BVAR=0;
		RMIN=GMIN=BMIN=0xff; 
		RMAX=GMAX=BMAX=0;
		for (int i=0;i<LENGTH;i++)
		{
			int c = bufferIn[i];
			int r = R32(c);
			int g = G32(c);
			int b = B32(c);
			RMEAN += r;
			GMEAN += g;
			BMEAN += b;
			if (r<RMIN) RMIN=r;
			if (g<GMIN) GMIN=g;
			if (b<BMIN) BMIN=b;
			if (r>RMAX) RMAX=r;
			if (g>GMAX) GMAX=g;
			if (b>BMAX) BMAX=b;
		}
		RMEAN = (RMEAN+LENGTH/2)/LENGTH;
		GMEAN = (GMEAN+LENGTH/2)/LENGTH;
		BMEAN = (BMEAN+LENGTH/2)/LENGTH;
		for (int i=0;i<LENGTH;i++)
		{
			int c = bufferIn[i];
			int r = R32(c)-RMEAN;
			int g = G32(c)-GMEAN;
			int b = B32(c)-BMEAN;
			RVAR += r*r+0x10>>5;
			GVAR += g*g+0x10>>5;
			BVAR += b*b+0x10>>5;
		}
		RVAR = (RVAR+LENGTH/2)/LENGTH+4>>3;
		GVAR = (GVAR+LENGTH/2)/LENGTH+4>>3;
		BVAR = (BVAR+LENGTH/2)/LENGTH+4>>3;
		RVAR = (int)(sqrt(RVAR)*16+0.5);
		GVAR = (int)(sqrt(GVAR)*16+0.5);
		BVAR = (int)(sqrt(BVAR)*16+0.5);
		RVAR = Homeostatic.CONTRAST*256/(RVAR+2);
		GVAR = Homeostatic.CONTRAST*256/(GVAR+2);
		BVAR = Homeostatic.CONTRAST*256/(BVAR+2);
	}
	
	public static void render(int threadnumber, int nthreads)
	{
		int [] bufferIn = din;
		DataBuffer bufferOut  = out .getRaster().getDataBuffer();
		DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
		
		int  random = rng.nextInt();
		int  r,g,b,R,G,B;
		int  c,c1,c2,c3,c4,c5,c6,c7,c8,c9;
		
		float[] HSV = {0f,0f,0f};
		
		int Rstart = HEIGHT/nthreads*threadnumber;
		int Rstop  = threadnumber==nthreads-1? HEIGHT: HEIGHT/nthreads*(threadnumber+1);
		int istart = Rstart*WIDTH;
		int istop  = Rstop*WIDTH;
		
		for (int i=istart; i<istop; i++)
		{
			int X,Y,X2,Y2,X3,Y3,AX,AY,x1,y1,x2,y2,x3,y3,ax,ay,x4,y4;
			x1 = mapping[2*i];
			y1 = mapping[2*i+1];
			
			//int x = i%WIDTH * 512;
			//int y = i/WIDTH * 512;
			
			// apply affine transform using 8-bit fixed point arithemetic
			x2 = T1*x1+T2*y1+T3 + 0x200 >> 10;
			y2 = T4*x1+T5*y1+T6 + 0x200 >> 10;
			// boundary wrapping and separating integer and fractional components
            //x2%=W8;
            //y2%=W8;
            while (x2<W_82) x2+=W_82;
            while (y2<H_82) y2+=H_82;
            x2  = (x2/W8&1)==1? x2%W8 : (W8-1)-x2%W8;
            y2  = (y2/H8&1)==1? y2%H8 : (H8-1)-y2%H8;
            
			ax = (x2&0xff);
			ay = (y2&0xff);
			
			int axx  = ax*ax+0x80>>8;
			int ayy  = ay*ay+0x80>>8;
			int axxx = ax*ax*ax+0x8000>>16;
			int ayyy = ay*ay*ay+0x8000>>16;
			
			x1 = x2>>8;
			x2 = x1+1;
			x3 = x1+2;
			x4 = x1+3;
			
			y1 = y2>>8;
			y2 = y1+1;
			y3 = y1+2;
			y4 = y1+3;

			y1*= WIDTH;
			y2*= WIDTH;
			y3*= WIDTH;
			y4*= WIDTH;
			
			c1 = bufferIn[x1+y1];
			c2 = bufferIn[x2+y1];
			c3 = bufferIn[x3+y1];
			c4 = bufferIn[x4+y1];
			int d1r = R32(c2) + (ax*(R32(c3)-R32(c1)) + axx*(2*R32(c1)-5*R32(c2)+4*R32(c3)-R32(c4)) + axxx*(3*(R32(c2)-R32(c3))-R32(c1)+R32(c4)) +0x100>>9);
			int d1g = G32(c2) + (ax*(G32(c3)-G32(c1)) + axx*(2*G32(c1)-5*G32(c2)+4*G32(c3)-G32(c4)) + axxx*(3*(G32(c2)-G32(c3))-G32(c1)+G32(c4)) +0x100>>9);
			int d1b = B32(c2) + (ax*(B32(c3)-B32(c1)) + axx*(2*B32(c1)-5*B32(c2)+4*B32(c3)-B32(c4)) + axxx*(3*(B32(c2)-B32(c3))-B32(c1)+B32(c4)) +0x100>>9);
			c1 = bufferIn[x1+y2];
			c2 = bufferIn[x2+y2];
			c3 = bufferIn[x3+y2];
			c4 = bufferIn[x4+y2];
			int d2r = R32(c2) + (ax*(R32(c3)-R32(c1)) + axx*(2*R32(c1)-5*R32(c2)+4*R32(c3)-R32(c4)) + axxx*(3*(R32(c2)-R32(c3))-R32(c1)+R32(c4)) +0x100>>9);
			int d2g = G32(c2) + (ax*(G32(c3)-G32(c1)) + axx*(2*G32(c1)-5*G32(c2)+4*G32(c3)-G32(c4)) + axxx*(3*(G32(c2)-G32(c3))-G32(c1)+G32(c4)) +0x100>>9);
			int d2b = B32(c2) + (ax*(B32(c3)-B32(c1)) + axx*(2*B32(c1)-5*B32(c2)+4*B32(c3)-B32(c4)) + axxx*(3*(B32(c2)-B32(c3))-B32(c1)+B32(c4)) +0x100>>9);
			c1 = bufferIn[x1+y3];
			c2 = bufferIn[x2+y3];
			c3 = bufferIn[x3+y3];
			c4 = bufferIn[x4+y3];
			int d3r = R32(c2) + (ax*(R32(c3)-R32(c1)) + axx*(2*R32(c1)-5*R32(c2)+4*R32(c3)-R32(c4)) + axxx*(3*(R32(c2)-R32(c3))-R32(c1)+R32(c4)) +0x100>>9);
			int d3g = G32(c2) + (ax*(G32(c3)-G32(c1)) + axx*(2*G32(c1)-5*G32(c2)+4*G32(c3)-G32(c4)) + axxx*(3*(G32(c2)-G32(c3))-G32(c1)+G32(c4)) +0x100>>9);
			int d3b = B32(c2) + (ax*(B32(c3)-B32(c1)) + axx*(2*B32(c1)-5*B32(c2)+4*B32(c3)-B32(c4)) + axxx*(3*(B32(c2)-B32(c3))-B32(c1)+B32(c4)) +0x100>>9);
			c1 = bufferIn[x1+y4];
			c2 = bufferIn[x2+y4];
			c3 = bufferIn[x3+y4];
			c4 = bufferIn[x4+y4];
			int d4r = R32(c2) + (ax*(R32(c3)-R32(c1)) + axx*(2*R32(c1)-5*R32(c2)+4*R32(c3)-R32(c4)) + axxx*(3*(R32(c2)-R32(c3))-R32(c1)+R32(c4)) +0x100>>9);
			int d4g = G32(c2) + (ax*(G32(c3)-G32(c1)) + axx*(2*G32(c1)-5*G32(c2)+4*G32(c3)-G32(c4)) + axxx*(3*(G32(c2)-G32(c3))-G32(c1)+G32(c4)) +0x100>>9);
			int d4b = B32(c2) + (ax*(B32(c3)-B32(c1)) + axx*(2*B32(c1)-5*B32(c2)+4*B32(c3)-B32(c4)) + axxx*(3*(B32(c2)-B32(c3))-B32(c1)+B32(c4)) +0x100>>9);
			
			r = d2r + (ay*(d3r-d1r) + ayy*(2*d1r-5*d2r+4*d3r-d4r) + ayyy*(3*d2r-d1r+d4r-3*d3r) +0x100>> 9);
			g = d2g + (ay*(d3g-d1g) + ayy*(2*d1g-5*d2g+4*d3g-d4g) + ayyy*(3*d2g-d1g+d4g-3*d3g) +0x100>> 9);
			b = d2b + (ay*(d3b-d1b) + ayy*(2*d1b-5*d2b+4*d3b-d4b) + ayyy*(3*d2b-d1b+d4b-3*d3b) +0x100>> 9);
			
			//r = er<0?0:er>0xff?0xff:er;
			//g = eg<0?0:eg>0xff?0xff:eg;
			//b = eb<0?0:eb>0xff?0xff:eb;			

			// homeostatic color effects
			if (Homeostatic.WEIGHT!=0)
			{
				r = (r-RMEAN)*RVAR+Homeostatic.BRIGHTNESS+0x80>>8;
				g = (g-GMEAN)*GVAR+Homeostatic.BRIGHTNESS+0x80>>8;
				b = (b-BMEAN)*BVAR+Homeostatic.BRIGHTNESS+0x80>>8;
				r=CLIPCOLOR(r);
				g=CLIPCOLOR(g);
				b=CLIPCOLOR(b);
			}
				
			// recurrent color effects
			if (Recurrent.WEIGHT>0)
			{
				if (0!=Recurrent.HUE || 128!=Recurrent.SATURATION)
				{
					Color.RGBtoHSB(r,g,b,HSV);
					float s = HSV[1]*Recurrent.SATURATION*0.0078125f;
					s = s<0f?0f:s>1f?1f:s;
					c=Color.HSBtoRGB(HSV[0]+Recurrent.HUE*0.00390625f,s,HSV[2]);
					r = R32(c);
					g = G32(c);
					b = B32(c);
				}
			}
			
			r=CLIPCOLOR(r);
			g=CLIPCOLOR(g);
			b=CLIPCOLOR(b);
			c = PACKRGB32(r,g,b);
			// Additional effects
			if (0!=MOTIONBLUR) 
			{
				c2 = bufferIn[i];
				//R = R32(c2);
				//G = G32(c2);
				//B = B32(c2);
				//r = MOTIONBLUR*R+(256-MOTIONBLUR)*r;
				//g = MOTIONBLUR*G+(256-MOTIONBLUR)*g;
				//b = MOTIONBLUR*B+(256-MOTIONBLUR)*b;
				c  = CLIRP32(MOTIONBLUR,c2,c); //Motion Blur
			}
			if (0!=NOISE)
			{
				random^=random>>19^random<<11^random<<3;
				//R = R32(random);
				//G = G32(random);
				//B = B32(random);
				//r = NOISE*R+(256-NOISE)*r;
				//g = NOISE*G+(256-NOISE)*g;
				//b = NOISE*B+(256-NOISE)*b;
                c=CLIRP32(NOISE,random,c); //Fade with noise
			}
			bufferOut.setElem(i,c);
			
			/*
			if (Post.WEIGHT>0)
			{
				// post-processing effects
				if (Post.CONTRAST!=0x80)
				{
					r = (r-0x80)*Post.CONTRAST+Post.BRIGHTNESS>>8;
					g = (g-0x80)*Post.CONTRAST+Post.BRIGHTNESS>>8;
					b = (b-0x80)*Post.CONTRAST+Post.BRIGHTNESS>>8;
				}
				if (0!=Post.HUE || 128!=Post.SATURATION)
				{
					Color.RGBtoHSB(r,g,b,HSV);
					float s = HSV[1]*Post.SATURATION*0.0078125f;
					s = s<0f?0f:s>1f?1f:s;
					c=Color.HSBtoRGB(HSV[0]+Post.HUE*0.00390625f,s,HSV[2]);
					r = R32(c);
					g = G32(c);
					b = B32(c);
				}
			}
			bufferDisp.setElem(i,PACKRGB32(CLIPCOLOR(r),CLIPCOLOR(g),CLIPCOLOR(b)));
			*/
		}
	}
	
	/*	
	void RgbToHsb()
	{
		max = r>b?r>g?r:g:b>g?g:b;
		min = r<b?r<g?r:g:b<g?g:b;
		delta = max - min;
		brightness = max;
		saturation = max==0?0:delta/max;
		if (delta == 0) hue = 0;
		else {
			if (r==max)      hue = 1*43+(g-b)*43/delta;
			else if (g==max) hue = 2*43+(b-r)*43/delta;
			else             hue = 4*43+(r-g)*43/delta;
			if (hue < 0) hue += 256;
			outHsb->hue = hue;
		}
	}
	*/
}










