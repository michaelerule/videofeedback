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

#define B32(c)   (d     & 0xff)
#define G32(c)   (d>> 8 & 0xff)
#define R32(c)   (d>>16 & 0xff)

#define PACKRGB32(r,g,b) (int)((r<<16)|(g<<8)|b)

#define CLIRP32(a,x,y) (LIRP(a,x&V32M,y&V32M)+D32M>>8&V32M|LIRP(a,x&G32M,y&G32M)+S32M>>8&G32M)

// If we separate the RGB components enough we can operate on all colors at
// once without worrying ( too much ) about overflow. To allow faster 
// conversion between the expanded long format and the intRGB format, we
// byte-align the channels, with one byte padding for overflow. The channel
// order is GRB, this lets us just move the green channel to the left to 
// make our long intRGB.
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

#define CLIP(c)  (c<0?0:c>0xff?0xff:c)
#define ROUND(x) CLIP(x+0x80>>8)


import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.util.Random;
public class Kernel 
{
	static final Random rng = new Random( 19580427 );

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
	
	/**
	Current kernel algorithm:
	-- Pull through the mapping using linear interpolation
	*/
	
	// we use a very small separable convolution
	// this is separate because convolution is a little more wordy to 
	// multithread
	public static void
	convolve(
		int W, int H,
		long[] bufferIn,
		long[] bufferOut,
		long[] bufferTmp)
	{
		int L  = W*H;
		int W1 = W-1;
		int H1 = H-1;
		long curr, next, prev, pair;
		for (int col=0; col<W; col++) {
			next = bufferIn[col+W];
			curr = bufferIn[col  ];
			pair = prev = next+curr;
			bufferTmp[col]=prev>>1;
			for (int row=1; row<H1; row++) {
				curr = next; next = bufferIn[col+(row+1)*W];
				prev = pair; pair = next+curr;
				bufferTmp[col+row*W]=prev+pair+1>>2;
			}
			bufferTmp[col+H1*W]=pair>>1;
		}
		int i=0;
		for (int row=0; row<H; row++) {
			next = bufferTmp[row*W+1];
			curr = bufferTmp[row*W  ];
			pair = prev = next+curr;
			bufferOut[i] = CLIRP64(BLUR,prev>>1,bufferIn[i]);
			i++;
			for (int col=1; col<W1; col++) {
				curr = next; next = bufferTmp[row*W+col+1];
				prev = pair; pair = next+curr;
				bufferOut[i] = CLIRP64(BLUR,prev+pair+1>>2,bufferIn[i]);
				i++;
			}
			bufferOut[i] = CLIRP64(BLUR,pair>>1,bufferIn[i]);
			i++;
		}
	}
	
	public static void 
	render(
		int istart, 
		int istop, 
		int[] mapping, 
		long[] bufferIn,
		long[] bufferOut,
		BufferedImage out)
	{
		int W  = out.getWidth();
		int H  = out.getHeight();
		int L  = W*H;
		int W1 = W-1;
		int H1 = H-1;
		int W8 = FIXED*(W-1);
		int H8 = FIXED*(H-1);
		int W82= 2*W8;
		int H82= 2*W8;
		
		DataBuffer outputBuffer = out.getRaster().getDataBuffer();
		
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

			// apply affine transform using 8-bit fixed point arithemetic
			int x2 = T1*x+T2*y+T3 >> 8;
			int y2 = T4*x+T5*y+T6 >> 8;
			
			// boundary wrapping and separating integer and fractional components
            while (x2<=W82) x2+=W82;
            while (y2<=H82) y2+=H82;
            x2  = (x2/W8&1)==1? x2%W8 : (W8-1)-x2%W8 ;
            y2  = (y2/H8&1)==1? y2%H8 : (H8-1)-y2%H8 ;
			int ax = x2&0xff;
			int ay = y2&0xff;
			x = x2>>8;
			y = y2>>8;
    		
			x2 = x+1;
			y2 = y+1;
			y *= W;
			y2*= W;
			
			// linear interpolation of colors
			d1 = bufferIn[x +y];
			d2 = bufferIn[x2+y];
			d3 = CLIRP64(ax,d2,d1);
			d1 = bufferIn[x +y2];
			d2 = bufferIn[x2+y2];
			d4 = CLIRP64(ax,d2,d1);
			d  = CLIRP64(ay,d4,d3);
			
			// 64-bit color operations
			d = CLIRP64(LIGHTEN,W64M,d);                            // ligten by blending with white
			d = CLIRP64(DARKEN ,0L  ,d);                            // darken by blending with black
			d = CLIRP64(NONLINEARCONTRAST,(d>>7&I64M)*0xFF,d); // Soft contrast by blending with 3-bit color
			
			// per-channel color operations
			r = R64(d);
			g = G64(d);
			b = B64(d);
			// contrast and brightness adjustment
			r = ROUND(r*CONTRAST+BRIGHTNESS*(0x100-CONTRAST));
			g = ROUND(g*CONTRAST+BRIGHTNESS*(0x100-CONTRAST));
			b = ROUND(b*CONTRAST+BRIGHTNESS*(0x100-CONTRAST));
			// TODO: hue, saturation adjustment
			
			// 32-bit color operations
			c = PACKRGB32(r,g,b);
			c = c^INVERT;                                    //Apply inversion M
			c = CLIRP32(NOISE,rng.nextInt()&W32M,c); //Fade with noise
			
			outputBuffer.setElem(i,c);
			bufferOut[i] = C32TO64(c);
		}
	}
}



