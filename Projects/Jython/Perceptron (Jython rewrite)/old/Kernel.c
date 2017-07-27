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
#define CLIP(c)  (c<0?0:c>0xff?0xff:c)
#define ROUND(x) CLIP(x+0x80>>8)

import java.awt.Color;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

#define G64M  0x00ff00000000L
#define R64M  0x000000ff0000L
#define B64M  0x0000000000ffL
#define V64M  0x000000ff00ffL
#define W64M  0x00ff00ff00ffL
#define D64M  0x008000800080L
#define I64M  0x000100010001L
#define B64(d)   (int)(d     &0xff)
#define R64(d)   (int)(d>>16 &0xff)
#define G64(d)   (int)(d>>32 &0xff)
#define C32TO64(c) (c&V32M|(long)c<<24&G64M)
#define C64TO32(d) (int)(d&V64M|d>>24&G32M)
#define PACKRGB64(r,g,b) (b|(r<<16)|((long)g<<32))
#define CLIRP64(a,x,y)  (LIRP(a,x,y)+D64M>>8&W64M)

public class Kernel 
{
	static final Random rng = new Random( 19580427 );

	// parameters
	public static int HUE;
	public static int SATURATION;
	public static int LIGHTEN;
	public static int DARKEN;
	public static int BRIGHTNESS;
	public static int CONTRAST;
	public static int NONLINEARCONTRAST;
	public static int MOTIONBLUR;
	public static int BLUR;
	public static int NOISE;
	public static int INVERT;
	public static int T1;
	public static int T2;
	public static int T3;
	public static int T4;
	public static int T5;
	public static int T6;
	public static int[] mapping;
	
	// configuration
	static int W;
	static int H;
	static int L;
	static int W1;
	static int H1;
	static int W8;
	static int H8;
	static int W82;
	static int H82;
	
	public static BufferedImage out;
	
	static BufferedImage in, tmp;
	static int [] din,dtmp,dout;
	
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
		
		out = new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
		in  = new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
		tmp = new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
		
		din  = new int[L];
		dtmp = new int[L];
		dout = new int[L];
	}
	
	/**
	Current kernel algorithm:
	-- Pull through the mapping using linear interpolation
	*/
	
	public static void swap()
	{
		BufferedImage swap = in;
		in = out;
		out = swap;
	}
	
	// we use a very small separable convolution
	// this is separate because convolution is a little more wordy to 
	// multithread
	public static void convolve() 
	{
		swap();
		DataBuffer bufferIn  = in .getRaster().getDataBuffer();
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		DataBuffer bufferTmp = tmp.getRaster().getDataBuffer();
		int curr, next, prev, pair;
		for (int C=0; C<W; C++) 
		{
			next = bufferIn.getElem(C+W) & O32M;
			curr = bufferIn.getElem(C  ) & O32M;
			pair = prev = next+curr>>1 & O32M;
			bufferTmp.setElem(C,prev);
			for (int R=1; R<H1; R++) 
			{
				curr = next; next = bufferIn.getElem(C+(R+1)*W) & O32M;
				prev = pair; pair = next+curr>>1 & O32M;
				bufferTmp.setElem(C+R*W,prev+pair>>1);
			}
			bufferTmp.setElem(C+H1*W,pair>>1);
		}
		int i=0;
		for (int R=0; R<H; R++) 
		{
			next = bufferTmp.getElem(R*W+1) & O32M;
			curr = bufferTmp.getElem(R*W  ) & O32M;
			pair = prev = next+curr>>1 & O32M;
			bufferOut.setElem(i,CLIRP32(BLUR,prev,bufferIn.getElem(i)));
			i++;
			for (int C=1; C<W1; C++) 
			{
				curr = next; next = bufferTmp.getElem(R*W+C+1) & O32M;
				prev = pair; pair = next+curr>>1 & O32M;
				bufferOut.setElem(i,CLIRP32(BLUR,prev+pair>>1,bufferIn.getElem(i)));
				i++;
			}
			bufferOut.setElem(i,CLIRP32(BLUR,pair,bufferIn.getElem(i)));
			i++;
		}
		swap();
	}
	
	public static void render(int istart, int istop)
	{
		DataBuffer bufferIn  = in .getRaster().getDataBuffer();
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		
		int  r,g,b;
		int  c,c1,c2,c3,c4;
		long d,d1,d2,d3,d4;
		for (int i=istart; i<istop; i++)
		{
			// look up map location. packing a 8-bit FIXED point decimal
			// representation of a complex number into a long. Real and
			// imaginary parts are 32 bits where the lowest 8 bits represent
			// the fractional part of the number.
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
            while (x2<0) x2+=W82;
            while (y2<0) y2+=H82;
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
			
			c1 = bufferIn.getElem(x +y);
			c2 = bufferIn.getElem(x2+y);
			c3 = CLIRP32(ax,c2,c1);
			c1 = bufferIn.getElem(x +y2);
			c2 = bufferIn.getElem(x2+y2);
			c4 = CLIRP32(ax,c2,c1);
			c  = CLIRP32(ay,c4,c3);
			
			/*
			c1 = bufferIn.getElem(x +y);
			c2 = bufferIn.getElem(x2+y);
			d1 = C32TO64(c1);
			d2 = C32TO64(c2);
			d3 = CLIRP64(ax,d2,d1);
			c1 = bufferIn.getElem(x +y2);
			c2 = bufferIn.getElem(x2+y2);
			d1 = C32TO64(c1);
			d2 = C32TO64(c2);
			d4 = CLIRP64(ax,d2,d1);
			d  = CLIRP64(ay,d4,d3);
			c  = C64TO32(d);
			*/
			
			/*
			// 64-bit color operations
			c = CLIRP32(LIGHTEN,W32M,c);                            // ligten by blending with white
			c = CLIRP32(DARKEN ,0   ,c);                  // darken by blending with black
			// per-channel color operations
			r = R32(c);
			g = G32(c);
			b = B32(c);
			// contrast and brightness adjustment
			r = ROUND(r*CONTRAST*2+BRIGHTNESS*(0x100-CONTRAST*2));
			g = ROUND(g*CONTRAST*2+BRIGHTNESS*(0x100-CONTRAST*2));
			b = ROUND(b*CONTRAST*2+BRIGHTNESS*(0x100-CONTRAST*2));
			// TODO: hue, saturation adjustment
			// 32-bit color operations
			c = PACKRGB32(r,g,b);
			*/

			c = c^INVERT;                                  //Apply inversion
			//c = CLIRP32(NOISE,rng.nextInt()&W32M,c);       //Fade with noise
			//c = CLIRP32(MOTIONBLUR,bufferIn.getElem(i),c); //Motion Blur
			
			bufferOut.setElem(i,c);
		}
	}
}



