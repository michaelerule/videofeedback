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

// MACROS FOR EXTRACTING COLOR COMPONENTS FROM DIFFERENT FORMATS
#define R64(d)   ((int)((d    )&0xff))
#define G64(d)   ((int)((d>>21)&0xff))
#define B64(d)   ((int)((d>>42)&0xff))
#define B32(d)   ((int)((d    )&0xff))
#define G32(d)   ((int)((d>> 8)&0xff))
#define R32(d)   ((int)((d>>16)&0xff))

#define R32mask      0xff0000
#define G32mask      0x00ff00
#define B32mask      0x0000ff
#define V32mask      0xff00ff

// define global FIXED point settings
#define FIXED    0x100
#define FIXEDP(x)              ((int)(x*FIXED))
#define maxv     (1<<scale)
#define mask     (maxv-1)
#define offset   (1<<(scale-1))
#define maxf                   (maxv/FIXED)
#define baseMultiplier         0x40000200001L
#define whiteMask              (0xff*baseMultiplier)
#define roundingCorrection     (0x80*baseMultiplier)
#define roundColor(x)          ((((x)+roundingCorrection)>>8)&whiteMask)
#define LIRP(a,x1,x2)          ((a)*(x1)+(FIXED-(a))*(x2))
#define colorLIRP64(a,x,y)     (roundColor(LIRP(a,x,y)))
#define colorLIRP32(a,x,y)     (LIRP(a,x&V32mask,y&V32mask)+0x100010>>8&V32mask|LIRP(a,x&G32mask,y&G32mask)+0x1000>>8&G32mask)
#define colorMUL(c,x)          (roundColor((c)*(x)))
#define RGB32TO64(c)           ((c)&0xffL|((c)&0xff00L)<<13|((c)&0xff0000L)<<26)
#define RGB64TO32(d)           ((int)((d)&0xff|(d)>>13&0xff00|(d)>>26&0xff0000))
#define PACKRGB32(r,g,b)       ((int)(((r)<<16)|((g)<<8)|(b)))
#define CLIP(c)                ((c)<0?0:(c)>0xff?0xff:(c))
#define roundValue(x)          CLIP((x)+0x80>>8)

#define OVF 0x00fefefe

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.util.Random;
public class Kernel 
{
	public static final String version = "0.0";
	public static final Random rng = new Random( 19580427 );
	/* The main rendering loop must be as lean as possible. 
	 * The basic operation of perceptron is:
	 * -- iterate over pixels
	 * -- look up map table(s)
	 * -- compute pull point(s)
	 * -- pull pixel data
	 * -- -- could be linear, nearest, cubic, off-screen
	 */
	public static int RMEAN=0x80,GMEAN=0x80,BMEAN=0x80;
	public static int RVAR=0x100,GVAR=0x100,BVAR=0x100;
	 
	public static void 
	render(
		int istart,
		int istop,
		int[] mapping, 
		int[] T, 
		BufferedImage in, 
		BufferedImage out, 
		int HUE, 
		int LIGHTEN, 
		int DARKEN, 
		int BRIGHTNESS,
		int CONTRAST, 
		int SATURATION,
		int BLUR)
	{
	    int U = FIXEDP(cos(HUE*0.02454369260617026f));
	    int V = FIXEDP(sin(HUE*0.02454369260617026f));
		int W  = in.getWidth();
		int H = in.getHeight();
		int length = W*H;
		int lastx  = W-1;
		int lasty  = H-1;
		// pull out the raw data buffers for the images. This code will assume
		// that the images are int RGB format.
		DataBuffer inputBuffer  = in.getRaster().getDataBuffer();
		DataBuffer outputBuffer = out.getRaster().getDataBuffer();
		// it is unclear how much this sort of unpacking speeds things up
		int T1 = T[0];
		int T2 = T[1];
		int T3 = T[2];
		int T4 = T[3];
		int T5 = T[4];
		int T6 = T[5];
		for (int i=istart; i<istop; i++)
		{
			// look up map location. packing a 8-bit FIXED point decimal
			// representation of a complex number into a long. Real and
			// imaginary parts are 32 bits where the lowest 8 bits represent
			// the fractional part of the number.
			int x = mapping[2*i];
			int y = mapping[2*i+1];
			// apply affine Tation ( FIXED point arithmetic )
			int x2 = T1*x+T2*y+T3 >> 8;
			int y2 = T4*x+T5*y+T6 >> 8;
			
			int W8 = FIXED*(W-1);
			int H8 = FIXED*(H-1);
            while (x2<=2*W8) x2+=2*W8;
            while (y2<=2*H8) y2+=2*H8;
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
			
			int c1 = inputBuffer.getElem( x  + y  );
			int c2 = inputBuffer.getElem( x2 + y  );
			int c3 = inputBuffer.getElem( x  + y2 );
			int c4 = inputBuffer.getElem( x2 + y2 );
			long d1 = RGB32TO64(c1);
			long d2 = RGB32TO64(c2);
			long d3 = RGB32TO64(c3);
			long d4 = RGB32TO64(c4);
			
			// linear interpolation of colors
			long d5 = colorLIRP64(ax,d2,d1);
			long d6 = colorLIRP64(ax,d4,d3);
			long d7 = colorLIRP64(ay,d6,d5);
			
			// ligten by blending with white
			// darken by blending with black
			d7 = colorLIRP64(DARKEN,0L,colorLIRP64(LIGHTEN,whiteMask,d7));

			/*
			d6 = ((d7>>7)&baseMultiplier)*0xFF;
			d7 = colorLIRP64(CONTRAST,d6,d7);
			*/

			// contrast should streach values away from 0x80
			int r = R64(d7);
			int g = G64(d7);
			int b = B64(d7);
			//r = roundValue((r-RMEAN)*(CONTRAST+0x100)+RMEAN*0x100);
			//g = roundValue((g-GMEAN)*(CONTRAST+0x100)+GMEAN*0x100);
			//b = roundValue((b-BMEAN)*(CONTRAST+0x100)+BMEAN*0x100);
			r = roundValue((r-RMEAN)*RVAR+0x8000);
			g = roundValue((g-GMEAN)*GVAR+0x8000);
			b = roundValue((b-BMEAN)*BVAR+0x8000);
			/*
			int INVERT = 1;
			if (INVERT==1) 
			{
				r = 0xff-r;
				g = 0xff-g;
				b = 0xff-b;
				//value = 0x1000000-value;
			}
			*/
			int value = (r<<16)|(g<<8)|b;// RGB64TO32(d7);//(r<<16)|(g<<8)|b;
			int blurvalue = inputBuffer.getElem(i);
			value = colorLIRP32(BLUR,blurvalue,value);
			value = ~value;
			
			outputBuffer.setElem(i,value);
		}
	}
	
	
	public static void convolution(BufferedImage in, BufferedImage out, BufferedImage temp, int BLUR, int NOISE, int CONTRAST)
	{
		int W  = in.getWidth();
		int H = in.getHeight();
		int L = W*H;
		int lastx  = W-1;
		int lasty  = H-1;

		DataBuffer inputBuffer  = in.getRaster().getDataBuffer();
		DataBuffer outputBuffer = out.getRaster().getDataBuffer();
		DataBuffer tempBuffer   = temp.getRaster().getDataBuffer();
		
		RMEAN=GMEAN=BMEAN=0;
		// two step 3-pixel running implmentation
		int curr, next, prev, pair;
		for (int row=0; row<H; row++) {
			next = inputBuffer.getElem(row*W+1)&OVF;
			curr = inputBuffer.getElem(row*W  )&OVF;
			pair = prev = (next+curr>>1)&OVF;
			tempBuffer.setElem(row*W,prev);
			for (int col=1; col<lastx; col++) {
				curr = next; next = inputBuffer.getElem(row*W+col+1)&OVF;
				prev = pair; pair = (next+curr>>1)&OVF;
				tempBuffer.setElem(row*W+col,prev+pair>>1);
			}
			tempBuffer.setElem(row*W+lastx,pair);
		}
		for (int col=0; col<W; col++) {
			next = tempBuffer.getElem(col+W)&OVF;
			curr = tempBuffer.getElem(col  )&OVF;
			pair = prev = (next+curr>>1)&OVF;
			outputBuffer.setElem(col,prev);
			for (int row=1; row<lasty; row++) {
				curr = next; next = tempBuffer.getElem(col+(row+1)*W)&OVF;
				prev = pair; pair = (next+curr>>1)&OVF;
				outputBuffer.setElem(col+row*W,prev+pair>>1);
			}
			outputBuffer.setElem(col+lasty*W,pair);
			
		}
		for (int i=0;i<L;i++)
		{
			int RGB1 = outputBuffer.getElem(i); // blurred  pixel
			int RGB2 = inputBuffer.getElem(i); // original pixel
			RGB1 = colorLIRP32(BLUR,RGB1,RGB2);
			RGB1 = colorLIRP32(NOISE,rng.nextInt()&0xffffff,RGB1);
			outputBuffer.setElem(i,RGB1);
			
			RMEAN += R32(RGB1);
			GMEAN += G32(RGB1);
			BMEAN += B32(RGB1);
			// for a sharpened image the blur is subtracted
			// for a blurred image the blue is averaged in
			//int R = CLIP(R32(RGB2)-R32(RGB1)*SHARPEN/256);
			//int G = CLIP(G32(RGB2)-G32(RGB1)*SHARPEN/256);
			//int B = CLIP(B32(RGB2)-B32(RGB1)*SHARPEN/256);
			//outputBuffer.setElem(i,PACKRGB32(R,G,B) ^ (rng.nextInt()&0x00020202) );
		}
		RMEAN/=L;
		GMEAN/=L;
		BMEAN/=L;
		
		RVAR=GVAR=BVAR=0;
		for (int i=0;i<L;i++)
		{
			int RGB = outputBuffer.getElem(i); // blurred  pixel
			int R = R32(RGB)-RMEAN;
			int G = G32(RGB)-GMEAN;
			int B = B32(RGB)-BMEAN;
			RVAR += R*R;
			GVAR += G*G;
			BVAR += B*B;
		}
		RVAR/=L;
		GVAR/=L;
		BVAR/=L;
		RVAR = (int)(sqrt(RVAR));
		GVAR = (int)(sqrt(GVAR));
		BVAR = (int)(sqrt(BVAR));
		// 0x100 is "1"
		// target variance is 0x40
		// if variance is too small we need to increase contrast
		// if variance is too large we need to decrease contrast
		// for example, 
		// to double the variance we would use the value 512
		// to halve the variance  we would use the value 128
		RVAR = CONTRAST*0x100/(32+RVAR);
		GVAR = CONTRAST*0x100/(32+GVAR);
		BVAR = CONTRAST*0x100/(32+BVAR);
	}
}



