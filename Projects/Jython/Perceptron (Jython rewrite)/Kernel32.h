static void intRender(int thn, int nthreads){
	#include "kernStart.h"
	/////////////////////////////////////////////////////////////////////////
	
	int lRVAR = (int)(sqrt(RVAR)*16+0.5);
	int lGVAR = (int)(sqrt(GVAR)*16+0.5);
	int lBVAR = (int)(sqrt(BVAR)*16+0.5);
	lRVAR = Homeostatic.CONTRAST*3*256/(lRVAR+2);
	lGVAR = Homeostatic.CONTRAST*3*256/(lGVAR+2);
	lBVAR = Homeostatic.CONTRAST*3*256/(lBVAR+2);
	int rs = Recurrent.SATURATION*2;
	int rsc2 = (256-rs);
	int rBias = rs*(Homeostatic.BRIGHTNESS*256-(int)RMEAN*lRVAR)>>8;
	int gBias = rs*(Homeostatic.BRIGHTNESS*256-(int)GMEAN*lGVAR)>>8;
	int bBias = rs*(Homeostatic.BRIGHTNESS*256-(int)BMEAN*lBVAR)>>8;
	int rGain = rs*lRVAR>>8;
	int gGain = rs*lGVAR>>8;
	int bGain = rs*lBVAR>>8;
	int huerotate = Recurrent.HUE*6*341/256;
	
	float th = (float)(Recurrent.HUE*6.29/256.f);
	int Q1 = (int)(256*sin(-th)/sqrt(3));
	int Q2 = (int)(256*(1-cos(th))/3);
	/////////////////////////////////////////////////////////////////////////
	for (int row=rstart; row<rstop; row++)
	for (int col=cstart; col<cstop; col++) {
		int i = row*WIDTH+col;
		#include "mapLookup.h"
		x1 = x>>8;
		y1 = y>>8;
		y1 *= WIDTH;
		int c=0,r=0,g=0,b=0;
		switch (INTERPOLATION) {
			case 0: 
				c = bufferIn[x1+y1]; 
				r = R32(c);
				g = G32(c);
				b = B32(c);
				break; // nearest
			case 1: {// linear
				int x2 = x1+1;
				int y2 = y1+1*WIDTH;
				int ax = x&0xff;
				int ay = y&0xff;
				int bx = 256-ax;
				int by = 256-ay;
				int c1,c2,c3,c4;
				c1 = bufferIn[x1+y1];
				c2 = bufferIn[x2+y1];
				c3 = INTERP32(ax,bx,c1,c2);
				c1 = bufferIn[x1+y2];
				c2 = bufferIn[x2+y2];
				c4 = INTERP32(ax,bx,c1,c2);
				c4 = INTERP32(ay,by,c3,c4);
				r = R32(c4);
				g = G32(c4);
				b = B32(c4);
			break;}
			case 2: {// cubic
				int ax1 = x&0xff;
				int ay1 = y&0xff;
				int ax2 = ax1*ax1>>8;
				int ay2 = ay1*ay1>>8;
				int ax3 = ax1*ax2>>8;
				int ay3 = ay1*ay2>>8;
				int bx1 = (2*ax2-(ax1+ax3));
				int bx2 = (512-ax2*5+ax3*3);
				int bx3 = (ax1+ax2*4-ax3*3);
				int bx4 = ((ax3-ax2));
				int by1 = (2*ay2-(ay1+ay3));
				int by2 = (512-ay2*5+ay3*3);
				int by3 = (ay1+ay2*4-ay3*3);
				int by4 = ((ay3-ay2));
				int x2 = x1+1;
				int x3 = x1+2;
				int x4 = x1+3;
				int y2 = y1+1*WIDTH;
				int y3 = y1+2*WIDTH;
				int y4 = y1+3*WIDTH;
				int c1,c2,c3,c4;
				c1 = bufferIn[x1+y1];
				c2 = bufferIn[x2+y1];
				c3 = bufferIn[x3+y1];
				c4 = bufferIn[x4+y1];
				int d1r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				int d1g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
				int d1b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				c1 = bufferIn[x1+y2];
				c2 = bufferIn[x2+y2];
				c3 = bufferIn[x3+y2];
				c4 = bufferIn[x4+y2];
				int d2r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				int d2g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);	
				int d2b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				c1 = bufferIn[x1+y3];
				c2 = bufferIn[x2+y3];
				c3 = bufferIn[x3+y3];
				c4 = bufferIn[x4+y3];
				int d3r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				int d3g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
				int d3b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				c1 = bufferIn[x1+y4];
				c2 = bufferIn[x2+y4];
				c3 = bufferIn[x3+y4];
				c4 = bufferIn[x4+y4];
				int d4r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				int d4g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
				int d4b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				r = by1*d1r+by2*d2r+by3*d3r+by4*d4r>>18;
				g = by1*d1g+by2*d2g+by3*d3g+by4*d4g>>18;
				b = by1*d1b+by2*d2b+by3*d3b+by4*d4b>>18;
			break;}
		}
		int max = (r>b?g>r?g:r:g>b?g:b)*rsc2;
		r = r*rGain+rBias+max>>8;
		g = g*gGain+gBias+max>>8;
		b = b*bGain+bBias+max>>8;

		switch(HUE_MODE) {
			case 1: {
				int min    = r<b?g<r?g:r:g<b?g:b; 
				int value  = r>b?g>r?g:r:g>b?g:b; 
				int chroma = (value - min);
				if (chroma!=0) { //with no saturation, hue is undefined
					int hue;
					if      (value==r) hue=6*341+(g-b)*341/chroma;
					else if (value==g) hue=2*341+(b-r)*341/chroma;
					else               hue=4*341+(r-g)*341/chroma;
					r=g=b=min;
					hue = (hue+huerotate)&0x7ff;
					switch(hue*6>>11){
						case 0: r+=chroma;g+=chroma*(hue)*768>>18; break;
						case 1: r+=chroma*(2*341-hue)*768>>18;g+=chroma; break;
						case 2: g+=chroma;b+=chroma*(hue-2*341)*768>>18; break;
						case 3: g+=chroma*(4*341-hue)*768>>18;b+=chroma; break;
						case 4: r+=chroma*(hue-4*341)*768>>18;b+=chroma; break;
						case 5: r+=chroma;b+=chroma*(6*341-hue)*768>>18; break;
					}
				}
			}
			break;
			case 2:
			{
				max = r>b?g>r?g:r:g>b?g:b;
				int rb = r-b;
				int gr = g-r;
				int bg = b-g;
				int r1 = (Q2*(gr-rb)-Q1*bg>>8)+r;
				int Z  = (Q2*(bg-rb)+Q1*gr>>8);
				g += Z + (r-r1);
				b -= Z;
				r = r1;
				int adjust = ( r>b?g>r?g:r:g>b?g:b );
			}
			break;
			case 3:
			{
				max = r>b?g>r?g:r:g>b?g:b;
				int rb = r-b;
				int gr = g-r;
				int bg = b-g;
				int r1 = (Q2*(gr-rb)-Q1*bg>>8)+r;
				int Z  = (Q2*(bg-rb)+Q1*gr>>8);
				g += Z + (r-r1);
				b -= Z;
				r = r1;
				int adjust = ( r>b?g>r?g:r:g>b?g:b );
				adjust = adjust <= 0? 256 : 256 * max / adjust;
				r = r*adjust >> 8;
				g = g*adjust >> 8;
				b = b*adjust >> 8;
			}
			break;
		}
		r = r<0?0:r>0xff?0xff:r;
		g = g<0?0:g>0xff?0xff:g;
		b = b<0?0:b>0xff?0xff:b;
		c = PACKRGB32(r,g,b);
		c = CLIRP32(MOTIONBLUR,bufferIn[i],c); //Motion Blur
		random^=random>>19^random<<11^random<<3;
        c = CLIRP32(NOISE,random,c); //Fade with noise
		#include "kernWrite.h"
	}
}
