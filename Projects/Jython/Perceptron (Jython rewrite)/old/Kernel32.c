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

#define FIXED 0x100
#define LIRP(a,x1,x2)  (a*(x1)+(FIXED-a)*(x2))

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

#define B32(c)   (c     & 0xff)
#define G32(c)   (c>> 8 & 0xff)
#define R32(c)   (c>>16 & 0xff)

#define PACKRGB32(r,g,b) (int)((r<<16)|(g<<8)|b)
#define CLIRP32(a,x,y) (LIRP(a,x&V32M,y&V32M)+D32M>>8&V32M|LIRP(a,x&G32M,y&G32M)+S32M>>8&G32M)

import java.awt.Color;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class Kernel32 
{
	static final Random rng = new Random( 19580427 );
	
	// parameters
	public static int HUE;
	public static int SATURATION;
	public static int LIGHTEN;
	public static int DARKEN;
	public static int BRIGHTNESS;
	public static int CONTRAST;
	public static int HCONTRAST;
	public static int HMEAN;
	public static int HTAU;
	public static int LCONTRAST;
	public static int YCONTRAST;
	public static int RCONTRAST;
	public static int NONLINEARCONTRAST;
	public static int MOTIONBLUR;
	public static int BLUR;
	public static int SHARPEN;
	public static int NOISE;
	public static int INVERT;
	public static int T1;
	public static int T2;
	public static int T3;
	public static int T4;
	public static int T5;
	public static int T6;
	public static int[] mapping;
	
	// configuration variables and internal data buffers
	static int W;
	static int H;
	static int L;
	static int W1;
	static int H1;
	static int W8;
	static int H8;
	static int W82;
	static int H82;
	static int [] din,dtmp;
	public static BufferedImage out;

	// secret stuff
	static int gRMEAN,gGMEAN,gBMEAN,gRVAR,gGVAR,gBVAR;
	
	
	public static void prepare(int w, int h)
	{
		W  = w;
		H  = h;
		L  = W*H;
		W1 = W-1;
		H1 = H-1;
		W8 = FIXED*(W-1);
		H8 = FIXED*(H-1);
		W82= 2*W8;
		H82= 2*W8;
		
		out  = new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
		din  = new int[L];
		dtmp = new int[L];
	}
	
	public static void convolve() 
	{
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		int curr, next, prev, pair;
		if (0!=BLUR||0!=SHARPEN)
		{
			for (int C=0; C<W; C++) 
			{
				next = bufferOut.getElem(C+W) & O32M;
				curr = bufferOut.getElem(C  ) & O32M;
				bufferTmp[C] = pair = prev = next+curr>>1 & O32M;
				for (int R=1; R<H1; R++) 
				{
					curr = next; next = bufferOut.getElem(C+(R+1)*W) & O32M;
					prev = pair; pair = next+curr>>1 & O32M;
					bufferTmp[C+R*W]=prev+pair>>1;
				}
				bufferTmp[C+H1*W]=pair>>1;
			}
			int j=0;
			for (int R=0; R<H; R++) 
			{
				next = bufferTmp[R*W+1] & O32M;
				curr = bufferTmp[R*W  ] & O32M;
				bufferIn[j++] = pair = prev = next+curr>>1 & O32M;
				for (int C=1; C<W1; C++) 
				{
					curr = next; next = bufferTmp[R*W+C+1] & O32M;
					prev = pair; pair = next+curr>>1 & O32M;
					bufferIn[j++]= prev+pair>>1;
				}
				bufferIn[j++]=pair;
			}
			if (BLUR!=0) for (int i=0;i<L;i++)
			{
				int c1 = bufferIn[i];
				int c2 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(BLUR,c1,c2);
			}
			if (SHARPEN!=0) for (int i=0;i<L;i++)
			{
				int c2 = ~bufferIn[i]&W32M;
				int c1 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(SHARPEN,c2,c1);
				
				/*
				int r = R32(c);
				int g = G32(c);
				int b = B32(c);
				r = r*SHARPEN*2+0x80*(0x100-SHARPEN*2) >>8;
				g = g*SHARPEN*2+0x80*(0x100-SHARPEN*2) >>8;
				b = b*SHARPEN*2+0x80*(0x100-SHARPEN*2) >>8;
				r = r<0?0:r>0xff?0xff:r;
				g = g<0?0:g>0xff?0xff:g;
				b = b<0?0:b>0xff?0xff:b;
				bufferIn[i] = PACKRGB32(r,g,b);
				*/
				//int r = R32(c1)*(256-SHARPEN)-R32(c2)*SHARPEN + SHARPEN*256>>8;
				//int g = G32(c1)*(256-SHARPEN)-G32(c2)*SHARPEN + SHARPEN*256>>8;
				//int b = B32(c1)*(256-SHARPEN)-B32(c2)*SHARPEN + SHARPEN*256>>8;
				//r = r<0?0:r>0xff?0xff:r;
				//g = g<0?0:g>0xff?0xff:g;
				//b = b<0?0:b>0xff?0xff:b;
				//PACKRGB32(r,g,b);
			}
		}
		else for (int i=0;i<L;i++) bufferIn[i] = bufferOut.getElem(i);		
		
		
		int RMEAN=0,GMEAN=0,BMEAN=0;
		for (int i=0;i<L;i++)
		{
			int c = bufferIn[i];
			RMEAN += R32(c);
			GMEAN += G32(c);
			BMEAN += B32(c);
		}
		RMEAN /= L;
		GMEAN /= L;
		BMEAN /= L;
		int RVAR=0,GVAR=0,BVAR=0;
		for (int i=0;i<L;i++)
		{
			int c = bufferIn[i];
			int r = R32(c)-RMEAN;
			int g = G32(c)-GMEAN;
			int b = B32(c)-BMEAN;
			RVAR += r*r;
			GVAR += g*g;
			BVAR += b*b;
		}
		RVAR /= L;
		GVAR /= L;
		BVAR /= L;
		
		RVAR = HCONTRAST*2*256/(8+RVAR);
		GVAR = HCONTRAST*2*256/(8+GVAR);
		BVAR = HCONTRAST*2*256/(8+BVAR);
		RMEAN *= 0x100-RVAR;
		GMEAN *= 0x100-GVAR;
		BMEAN *= 0x100-BVAR;
		
		gRMEAN = RMEAN;
		gGMEAN = GMEAN;
		gBMEAN = BMEAN;
		gRVAR = RVAR;
		gGVAR = GVAR;
		gBVAR = BVAR;
	}
	
	public static void render(int istart, int istop)
	{
		int [] bufferIn = din;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		
		int  random = rng.nextInt();
		int  r,g,b;
		int  c,c1,c2,c3,c4;
		long d,d1,d2,d3,d4;
		
		float[] HSV = {0f,0f,0f};
				
		for (int i=istart; i<istop; i++)
		{
			int x = mapping[2*i];
			int y = mapping[2*i+1];
			
			//int x = i%W * 512;
			//int y = i/W * 512;
			
			// apply affine transform using 8-bit fixed point arithemetic
			int x2 = T1*x+T2*y+T3 + 0x200 >> 10;
			int y2 = T4*x+T5*y+T6 + 0x200 >> 10;
			// boundary wrapping and separating integer and fractional components
            //x2%=W8;
            //y2%=W8;
            while (x2<W82) x2+=W82;
            while (y2<H82) y2+=H82;
            x2  = (x2/W8&1)==1? x2%W8 : (W8-1)-x2%W8;
            y2  = (y2/H8&1)==1? y2%H8 : (H8-1)-y2%H8;
			int ax = x2&0xff;
			int ay = y2&0xff;
			x = x2>>8;
			y = y2>>8;
    		
			x2 = x+1;
			y2 = y+1;
			y *= W;
			y2*= W;
			
			// linear interpolation of colors
			c1 = bufferIn[x +y];
			c2 = bufferIn[x2+y];
			c3 = CLIRP32(ax,c2,c1);
			c1 = bufferIn[x +y2];
			c2 = bufferIn[x2+y2];
			c4 = CLIRP32(ax,c2,c1);
			c  = CLIRP32(ay,c4,c3);
			
				
			r = R32(c);
			g = G32(c);
			b = B32(c);
			if (CONTRAST!=0x100)
			{
				r = r*CONTRAST+BRIGHTNESS*(0x100-CONTRAST) >>8;
				g = g*CONTRAST+BRIGHTNESS*(0x100-CONTRAST) >>8;
				b = b*CONTRAST+BRIGHTNESS*(0x100-CONTRAST) >>8;
				r = r<0?0:r>0xff?0xff:r;
				g = g<0?0:g>0xff?0xff:g;
				b = b<0?0:b>0xff?0xff:b;
				
				/*
				int R = R32(c);
				int G = G32(c);
				int B = B32(c);
				int L = R+G+B;
				int Y = R+G;
				L = L*CONTRAST*2+BRIGHTNESS*(0x100-CONTRAST*2) >>8;
				Y = Y*CONTRAST*2+BRIGHTNESS*(0x100-CONTRAST*2) >>8;
				R = R*CONTRAST*2+BRIGHTNESS*(0x100-CONTRAST*2) >>8;
				R = R<0?0:R>0xff?0xff:R;
				Y = Y<0?0:Y>0xff*2?0xff*2:Y;
				L = L<0?0:L>0xff*3?0xff*3:L;
				G = Y-R;
				B = L-Y;
				G = G<0?0:G>0xff?0xff:G;
				B = B<0?0:B>0xff?0xff:B;
				c = PACKRGB32(R,G,B);
				*/
			}
			
			c = PACKRGB32(r,g,b);
			r = r*gRVAR+gRMEAN >>8;
			g = g*gGVAR+gGMEAN >>8;
			b = b*gBVAR+gBMEAN >>8;
			r = r<0?0:r>0xff?0xff:r;
			g = g<0?0:g>0xff?0xff:g;
			b = b<0?0:b>0xff?0xff:b;
			c2 = PACKRGB32(r,g,b);
			c = CLIRP32(HTAU,c,c2);
			
			if (0!=LIGHTEN)    c=CLIRP32(LIGHTEN,W32M,c);  // ligten by blending with white
			if (0!=DARKEN)     c=CLIRP32(DARKEN ,0   ,c);  // darken by blending with black
			if (0!=INVERT)     c=c^INVERT;                   //Apply inversion
			if (0!=HUE||128!=SATURATION)
			{
				Color.RGBtoHSB(R32(c),G32(c),B32(c),HSV);
				float s = HSV[1]*SATURATION*0.0078125f;
				s = s<0f?0f:s>1f?1f:s;
				c=Color.HSBtoRGB(HSV[0]+HUE*0.00390625f,s,HSV[2]);
			}
			if (0!=MOTIONBLUR) 
			{
				c2 = bufferIn[i];
				c  = CLIRP32(MOTIONBLUR,c2,c); //Motion Blur
			}
			if (0!=NOISE)
			{
				random^=random>>19^random<<11^random<<3;
                c=CLIRP32(NOISE,random,c); //Fade with noise
			}
			bufferOut.setElem(i,c);
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










