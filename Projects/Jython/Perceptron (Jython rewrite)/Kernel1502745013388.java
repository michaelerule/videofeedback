


static void intRender(int thn, int nthreads){
int [] bufferIn = din;
DataBuffer bufferOut = out .getRaster().getDataBuffer();
DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
int random = rng.nextInt();
int rstart = 740/nthreads*thn;
int rstop = thn==nthreads-1? 740: 740/nthreads*(thn+1);
int cstart = 0;
int cstop = 960;
int halfway = 960/2;
switch(DUPLICATION) {
 case 1:
  rstart = 740/nthreads*thn;
  rstop = thn==nthreads-1? 740: 740/nthreads*(thn+1);
  cstart = 0;
  cstop = 960/2;
 break;
 case 2:
  rstart = 740/2/nthreads*thn;
  rstop = thn==nthreads-1? 740/2+1: (740/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 960;
 break;
 case 3:
  rstart = 740/2/nthreads*thn;
  rstop = thn==nthreads-1? 740/2+1: (740/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 960/2;
 break;
 case 4:

 break;
}


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

 for (int row=rstart; row<rstop; row++)
 for (int col=cstart; col<cstop; col++) {
  int i = row*960 +col;
int x1 = mapping[2*i];
int y1 = mapping[2*i+1];
int x = T1*x1+T2*y1+T3+0x80>>8;
int y = T4*x1+T5*y1+T6+0x80>>8;
switch (BOUNDARY)
{
 case 0:
     while (x<W_82) x+=W_82;
     while (y<H_82) y+=H_82;
     x=(x/W8&1)==1?x%W8:(W8-1)-x%W8;
     y=(y/H8&1)==1?y%H8:(H8-1)-y%H8;
     break;
 case 1:
     while (x<W8)x+=W8;
     while (y<H8)y+=H8;
  x%=W8;
  y%=H8;
  break;
 case 2:
  x = x<0?0:x>W8?W8:x;
  y = y<0?0:y>H8?H8:y;
  break;
 case 3:
 case 4:
}
  x1 = x>>8;
  y1 = y>>8;
  y1 *= 960;
  int c=0,r=0,g=0,b=0;
  switch (INTERPOLATION) {
   case 0:
    c = bufferIn[x1+y1];
    r = R32(c);
    g = G32(c);
    b = B32(c);
    break;
   case 1: {
    int x2 = x1+1;
    int y2 = y1+1*960;
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
   case 2: {
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
    int y2 = y1+1*960;
    int y3 = y1+2*960;
    int y4 = y1+3*960;
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
    int min = r<b?g<r?g:r:g<b?g:b;
    int value = r>b?g>r?g:r:g>b?g:b;
    int chroma = (value - min);
    if (chroma!=0) {
     int hue;
     if (value==r) hue=6*341+(g-b)*341/chroma;
     else if (value==g) hue=2*341+(b-r)*341/chroma;
     else hue=4*341+(r-g)*341/chroma;
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
    int Z = (Q2*(bg-rb)+Q1*gr>>8);
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
    int Z = (Q2*(bg-rb)+Q1*gr>>8);
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
  c = CLIRP32(MOTIONBLUR,bufferIn[i],c);
  random^=random>>19^random<<11^random<<3;
        c = CLIRP32(NOISE,random,c);
switch(GRADIENT) {
 case 0:
  break;
 case 1: {
  int alpha = col*256/960*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = CLIRP32(alpha,0,c);
  }
  break;
 case 2: {
  int alpha = row*256/740*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = CLIRP32(alpha,0,c);
  }
  break;
 case 3: {
  int alpha = ((row-740/2)*256/740*GRADIENT_BIAS+(col-960/2)*256/960*GRADIENT_GAIN>>7);
  alpha = alpha*alpha>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = CLIRP32(alpha,0,c);
  }
  break;
 case 4: {
  int alpha = (row-740/2)*512/740*GRADIENT_BIAS >> 6;
  int beta = (col-960 /2)*512/960 *GRADIENT_GAIN >> 6;
  alpha = alpha*alpha + beta*beta >> 8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = CLIRP32(alpha,0,c);
  }
  break;
 case 5: {
  if (abs((row-740/2)*512/740)>abs(GRADIENT_BIAS)) c=0;
  if (abs((col-960 /2)*512/960 )>abs(GRADIENT_GAIN)) c=0;
  }
  break;
 default:
  break;
}


if (Recurrent.INVERT==1) c=~c;
switch(DUPLICATION) {
 case 0:
  bufferOut.setElem(i,c);
 break;
 case 1:
  bufferOut.setElem(i,c);
  bufferOut.setElem(row*960 +960 -1-col,c);
 break;
 case 2:
  bufferOut.setElem(i,c);
  if (row>0)
  bufferOut.setElem((740 -row)*960 +960 -1-col,c);
 break;
 case 3:
  bufferOut.setElem(i,c);
  int ty1 = (740 -1)*960 -row*960;
  int tx1 = 960 -1-col;
  bufferOut.setElem(row*960 +tx1,c);
  bufferOut.setElem(ty1+tx1,c);
  bufferOut.setElem(ty1+col,c);
 break;
 case 4:
  c = (c>>1)&0x7f7f7f;
  int tx = row*960 +960 -1-col;
  if (col>halfway) c += bufferOut.getElem(i);
  bufferOut.setElem(i,c);
  bufferOut.setElem(tx,c);
 break;
}
 }
}
