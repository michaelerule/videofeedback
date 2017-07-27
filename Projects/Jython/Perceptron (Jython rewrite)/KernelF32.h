static void floatRender(int thn, int nthreads){
	#include "kernStart.h"
	/////////////////////////////////////////////////////////////////////////
	float fhuerotate = Recurrent.HUE*6/256.f;
	
	float lRVAR = (float)sqrt(RVAR)*16;
	float lGVAR = (float)sqrt(GVAR)*16;
	float lBVAR = (float)sqrt(BVAR)*16;
	lRVAR = Homeostatic.CONTRAST*3/(lRVAR+2);
	lGVAR = Homeostatic.CONTRAST*3/(lGVAR+2);
	lBVAR = Homeostatic.CONTRAST*3/(lBVAR+2);
	float rs = Recurrent.SATURATION/128.f;
	float rsc2 = (1.f-rs);
	float rBias = rs*(Homeostatic.BRIGHTNESS-RMEAN*lRVAR);
	float gBias = rs*(Homeostatic.BRIGHTNESS-GMEAN*lGVAR);
	float bBias = rs*(Homeostatic.BRIGHTNESS-BMEAN*lBVAR);

	float rGain = rs*lRVAR;
	float gGain = rs*lGVAR;
	float bGain = rs*lBVAR;
	
	float th = (float)(Recurrent.HUE*6.29/256.f);
	float Q1 = (float)(sin(-th)/sqrt(3));
	float Q2 = (float)((1-cos(th))/3);
	
	/////////////////////////////////////////////////////////////////////////
	for (int row=rstart; row<rstop; row++)
	for (int col=cstart; col<cstop; col++) {
		int i = row*WIDTH+col;
		#include "mapLookup.h"
		x1 = x>>8;
		y1 = y>>8;
		y1 *= WIDTH;
		float r=0f,g=0f,b=0f;
		switch (INTERPOLATION) {
			case 0: {// nearest
				int c1 = bufferIn[x1+y1];
				r = R32(c1);
				g = G32(c1);
				b = B32(c1);
			break;}
			case 1: {// linear
				float ax1 = (x&0xff)/256.f;
				float ay1 = (y&0xff)/256.f;
				int x2 = x1+1;
				int y2 = y1+1*WIDTH;
				int c1,c2,c3,c4;
				float bx1 = 1.0f-ax1;
				float by1 = 1.0f-ay1;
				c1 = bufferIn[x1+y1];
				c2 = bufferIn[x2+y1];
				float d1r = ax1*R32(c2)+bx1*R32(c1);
				float d1g = ax1*G32(c2)+bx1*G32(c1);
				float d1b = ax1*B32(c2)+bx1*B32(c1);
				c1 = bufferIn[x1+y2];
				c2 = bufferIn[x2+y2];
				float d2r = ax1*R32(c2)+bx1*R32(c1);
				float d2g = ax1*G32(c2)+bx1*G32(c1);
				float d2b = ax1*B32(c2)+bx1*B32(c1);
				r = ay1*d2r+by1*d1r;
				g = ay1*d2g+by1*d1g;
				b = ay1*d2b+by1*d1b;
			break;}









			case 2: {// cubic
				float ax1 = (x&0xff)*0.00390625f;
				float ay1 = (y&0xff)*0.00390625f;
				float ax2 = ax1*ax1;
				float ay2 = ay1*ay1;
				float ax3 = ax1*ax2;
				float ay3 = ay1*ay2;
				float bx1 = ax2-0.5f*(ax1+ax3);
				float bx2 = 1.0f-ax2*2.5f+ax3*1.5f;
				float bx3 = 0.5f*ax1+ax2*2.0f-ax3*1.5f;
				float bx4 = 0.5f*(ax3-ax2);
				float by1 = ay2-0.5f*(ay1+ay3);
				float by2 = 1.0f-ay2*2.5f+ay3*1.5f;
				float by3 = 0.5f*ay1+ay2*2.0f-ay3*1.5f;
				float by4 = 0.5f*(ay3-ay2);
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
				float d1r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				float d1g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
				float d1b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				c1 = bufferIn[x1+y2];
				c2 = bufferIn[x2+y2];
				c3 = bufferIn[x3+y2];
				c4 = bufferIn[x4+y2];
				float d2r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				float d2g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
				float d2b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				c1 = bufferIn[x1+y3];
				c2 = bufferIn[x2+y3];
				c3 = bufferIn[x3+y3];
				c4 = bufferIn[x4+y3];
				float d3r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				float d3g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
				float d3b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				c1 = bufferIn[x1+y4];
				c2 = bufferIn[x2+y4];
				c3 = bufferIn[x3+y4];
				c4 = bufferIn[x4+y4];
				float d4r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
				float d4g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
				float d4b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
				r = by1*d1r+by2*d2r+by3*d3r+by4*d4r;
				g = by1*d1g+by2*d2g+by3*d3g+by4*d4g;
				b = by1*d1b+by2*d2b+by3*d3b+by4*d4b;
			break;}
		}
		
		float max = (r>b?g>r?g:r:g>b?g:b)*rsc2;
		r = r*rGain+rBias+max;
		g = g*gGain+gBias+max;
		b = b*bGain+bBias+max;
		
		int c;
		switch(HUE_MODE) {
			case 1: {
				float min    = r<b?g<r?g:r:g<b?g:b; 
				float value  = r>b?g>r?g:r:g>b?g:b; 
				float C= value - min;
				if (C!=0.f) { //with no saturation, hue is undefined
					float hue;
					if      (value==r) hue = 6+(g-b)/C;
					else if (value==g) hue = 2+(b-r)/C;
					else               hue = 4+(r-g)/C;
					hue += fhuerotate;
					hue %= 6.f;
					r=g=b=min;
					switch((int)hue) {
						case 0: r+=C; g+=C*hue; break;
						case 1: r+=C*(2-hue); g+=C; break;
						case 2: g+=C; b+=C*(hue-2); break;
						case 3: g+=C*(4-hue); b+=C; break;
						case 4: r+=C*(hue-4); b+=C; break;
						case 5: r+=C; b+=C*(6-hue); break;
					}
				}
			}
			break;
			case 2:
			{
				max = r>b?g>r?g:r:g>b?g:b;
				float rb = r-b;
				float gr = g-r;
				float bg = b-g;
				float r1 = Q2*(gr-rb)-Q1*bg+r;
				float Z  = Q2*(bg-rb)+Q1*gr;
				g += Z + (r-r1);
				b -= Z;
				r = r1;
			}
			break;
			case 3:
			{
				max = r>b?g>r?g:r:g>b?g:b;
				float rb = r-b;
				float gr = g-r;
				float bg = b-g;
				float r1 = Q2*(gr-rb)-Q1*bg+r;
				float Z  = Q2*(bg-rb)+Q1*gr;
				g += Z + (r-r1);
				b -= Z;
				r = r1;
				float adjust = ( r>b?g>r?g:r:g>b?g:b );
				adjust = adjust <= 0.f? 1.f : max / adjust;
				r *= adjust;
				g *= adjust;
				b *= adjust;
			}
			break;
		}
		int R = r<0.f?0:r>=256.f?0xff:(int)r;
		int	G = g<0.f?0:g>=256.f?0xff:(int)g;
		int	B = b<0.f?0:b>=256.f?0xff:(int)b;
		c = PACKRGB32(R,G,B);
		c = CLIRP32(MOTIONBLUR,bufferIn[i],c); //Motion Blur
		random^=random>>19^random<<11^random<<3;
        c = CLIRP32(NOISE,random,c); //Fade with noise
		#include "kernWrite.h"
	}
}
