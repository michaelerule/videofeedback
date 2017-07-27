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
#define H_1 (HEIGHT-1)
#define W_1 (WIDTH-1)
#define H_3 (HEIGHT-3)
#define W_3 (WIDTH-3)
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
#define CLIRP32(a,x,y) (FLIRP(a,x&V32M,y&V32M)+D32M>>8&V32M|FLIRP(a,x&G32M,y&G32M)+S32M>>8&G32M)

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
	
	public static int INTERPOLATION=0;
	public static int BOUNDARY=0;
	public static int TREE=0;
	public static int HSV_OPERATOR=0;
	public static int INTERNAL_PRECISION=0;
	
	// configuration variables and internal data buffers
	static int [] din,dtmp,dout;
	public static BufferedImage out;
	public static BufferedImage disp;

	// secret stuff
	static int gRMEAN,gGMEAN,gBMEAN,gRVAR,gGVAR,gBVAR,gRMIN,gRMAX,gBMIN,gBMAX,gGMIN,gGMAX,RSCALE,GSCALE,BSCALE;
	static int RMEAN=0,GMEAN=0,BMEAN=0;
	static int RVAR=0,GVAR=0,BVAR=0;
	static int RMIN=0xff, RMAX=0, GMIN=0xff, GMAX=0, BMIN=0xff, BMAX=0;
	
	public static void prepare(){	
		out  = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		disp = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		din  = new int[LENGTH];
		dtmp = new int[LENGTH];
		dout = new int[LENGTH];
	}

	public static void convolveStage1(int threadnumber, int nthreads)
	{
		if (0==BLUR&&0==SHARPEN) return;
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		int curr, next, prev, pair;
		int Cstart = WIDTH/nthreads*threadnumber;
		int Cstop  = threadnumber==nthreads-1? WIDTH: WIDTH/nthreads*(threadnumber+1);
		for (int C=Cstart; C<Cstop; C++){
			next = bufferOut.getElem(C+WIDTH) & O32M;
			curr = bufferOut.getElem(C  ) & O32M;
			bufferTmp[C] = pair = prev = next+curr>>1 & O32M;
			for (int R=1; R<H_1; R++){
				curr = next; next = bufferOut.getElem(C+(R+1)*WIDTH) & O32M;
				prev = pair; pair = next+curr>>1 & O32M;
				bufferTmp[C+R*WIDTH]=prev+pair>>1;
			}
			bufferTmp[C+H_1*WIDTH]=pair>>1;
		}
	}
		
	public static void convolveStage2(int threadnumber, int nthreads)
	{
		int MASK = INTERNAL_PRECISION==0?0xf8f8f8:0xffffff;
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		int Rstart = HEIGHT/nthreads*threadnumber;
		int Rstop  = threadnumber==nthreads-1? HEIGHT: HEIGHT/nthreads*(threadnumber+1);
		int Istart = Rstart*WIDTH;
		int Istop  = Rstop*WIDTH;
		if (0!=BLUR||0!=SHARPEN){
			int curr, next, prev, pair;
			for (int R=Rstart; R<Rstop; R++){
				next = bufferTmp[R*WIDTH+1] & O32M;
				curr = bufferTmp[R*WIDTH  ] & O32M;
				bufferIn[R*WIDTH] = pair = prev = next+curr>>1 & O32M;
				for (int C=1; C<W_1; C++){
					curr = next; next = bufferTmp[R*WIDTH+C+1] & O32M;
					prev = pair; pair = next+curr>>1 & O32M;
					bufferIn[R*WIDTH+C]= prev+pair>>1;
				}
				bufferIn[R*WIDTH+W_1]=pair;
			}
			if (BLUR!=0) for (int i=Istart;i<Istop;i++){
				int c1 = bufferIn[i];
				int c2 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(BLUR,c1,c2)&MASK;
			}
			if (SHARPEN!=0) for (int i=Istart;i<Istop;i++){
				int c2 = ~bufferIn[i]&W32M;
				int c1 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(SHARPEN,c2,c1)&MASK;
			}
		}
		else for (int i=Istart;i<Istop;i++) bufferIn[i] = bufferOut.getElem(i)&MASK;		
	}
		
	public static void stat(){ 
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		RMEAN=GMEAN=BMEAN=0;
		RVAR=GVAR=BVAR=0;
		RMIN=GMIN=BMIN=0xff; 
		RMAX=GMAX=BMAX=0;
		for (int i=0;i<LENGTH;i++){
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
		for (int i=0;i<LENGTH;i++){
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
	
	#include "Kernel18.h"
	#include "Kernel32.h"
	#include "KernelF32.h"
	
	public static void render(int threadnumber, int nthreads){
		switch(INTERNAL_PRECISION){
			case 0: shortRender(threadnumber,nthreads); break;
			case 1: intRender(threadnumber,nthreads); break;
			case 2: floatRender(threadnumber,nthreads); break;
		}
	}
}


