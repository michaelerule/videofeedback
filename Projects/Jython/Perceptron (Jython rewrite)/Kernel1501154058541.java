import java.awt.Color;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.concurrent.atomic.AtomicInteger;

public class Kernel1501154058541
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
  public int VARIATION;
  public int VALUE;
  public int CHARISMA;
  public int WEIGHT;
  public int INVERT;
  public int ENABLE;
 }

 static final public ColorFilterParameters Post = new ColorFilterParameters();
 static final public ColorFilterParameters Recurrent = new ColorFilterParameters();
 static final public ColorFilterParameters Homeostatic = new ColorFilterParameters();

 static final Random rng = new Random( 19580427 );


 public static int HTAU;
 public static int MOTIONBLUR;
 public static int BLUR;
 public static int SHARPEN;
 public static int NOISE;
 public static int GRADIENT_GAIN;
 public static int GRADIENT_BIAS;
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
 public static int HUE_MODE=0;
 public static int PRECISION=0;
 public static int DUPLICATION=0;
 public static int GRADIENT=0;


 static int [] din,dtmp,dout;
 public static BufferedImage out;
 public static BufferedImage disp;

 public static void prepare(){
  out = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
  disp = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
  din = new int[(500*500)];
  dtmp = new int[(500*500)];
  dout = new int[(500*500)];
 }

 public static void convolveStage1(int threadnumber, int nthreads)
 {
  if (0==BLUR&&0==SHARPEN) return;
  int[] bufferIn = din;
  int[] bufferTmp = dtmp;
  DataBuffer bufferOut = out.getRaster().getDataBuffer();
  int curr, next, prev, pair;
  int Cstart = 500/nthreads*threadnumber;
  int Cstop = threadnumber==nthreads-1? 500: 500/nthreads*(threadnumber+1);
  for (int C=Cstart; C<Cstop; C++){
   next = bufferOut.getElem(C+500) & 0xfefefe;
   curr = bufferOut.getElem(C ) & 0xfefefe;
   bufferTmp[C] = pair = prev = next+curr>>1 & 0xfefefe;
   for (int R=1; R<(500 -1); R++){
    curr = next; next = bufferOut.getElem(C+(R+1)*500) & 0xfefefe;
    prev = pair; pair = next+curr>>1 & 0xfefefe;
    bufferTmp[C+R*500]=prev+pair>>1;
   }
   bufferTmp[C+(500 -1)*500]=pair>>1;
  }
 }

 public static void convolveStage2(int threadnumber, int nthreads)
 {
  int MASK = PRECISION==0?0xf8f8f8:0xffffff;
  int[] bufferIn = din;
  int[] bufferTmp = dtmp;
  DataBuffer bufferOut = out.getRaster().getDataBuffer();
  int Rstart = 500/nthreads*threadnumber;
  int Rstop = threadnumber==nthreads-1? 500: 500/nthreads*(threadnumber+1);
  int Istart = Rstart*500;
  int Istop = Rstop*500;
  if (0!=BLUR||0!=SHARPEN){
   int curr, next, prev, pair;
   for (int R=Rstart; R<Rstop; R++){
    next = bufferTmp[R*500 +1] & 0xfefefe;
    curr = bufferTmp[R*500 ] & 0xfefefe;
    bufferIn[R*500] = pair = prev = next+curr>>1 & 0xfefefe;
    for (int C=1; C<(500 -1); C++){
     curr = next; next = bufferTmp[R*500 +C+1] & 0xfefefe;
     prev = pair; pair = next+curr>>1 & 0xfefefe;
     bufferIn[R*500 +C]= prev+pair>>1;
    }
    bufferIn[R*500 +(500 -1)]=pair;
   }
   if (BLUR!=0) for (int i=Istart;i<Istop;i++){
    int c1 = bufferIn[i];
    int c2 = bufferOut.getElem(i);
    bufferIn[i] = ((BLUR*(c1&0xff00ff)+(0x100 -BLUR)*(c2&0xff00ff))+0x800080>>8&0xff00ff|(BLUR*(c1&0x00ff00)+(0x100 -BLUR)*(c2&0x00ff00))+0x008000>>8&0x00ff00)&MASK;
   }
   if (SHARPEN!=0) for (int i=Istart;i<Istop;i++){
    int c2 = ~bufferIn[i]&0xffffff;
    int c1 = bufferOut.getElem(i);
    bufferIn[i] = ((SHARPEN*(c2&0xff00ff)+(0x100 -SHARPEN)*(c1&0xff00ff))+0x800080>>8&0xff00ff|(SHARPEN*(c2&0x00ff00)+(0x100 -SHARPEN)*(c1&0x00ff00))+0x008000>>8&0x00ff00)&MASK;
   }
  }
  else for (int i=Istart;i<Istop;i++) bufferIn[i] = bufferOut.getElem(i)&MASK;
 }


 static float RMEAN=0,GMEAN=0,BMEAN=0;
 static float RVAR=0,GVAR=0,BVAR=0;
 static float RMIN=0,RMAX=0,GMIN=0,GMAX=0,BMIN=0,BMAX=0;
 static float SMEAN=0,SVAR=0,VMEAN=0,VVAR=0;
 public static void stat(){
  int[] bufferIn = din;
  int[] bufferTmp = dtmp;
  DataBuffer bufferOut = out.getRaster().getDataBuffer();
  RMEAN=GMEAN=BMEAN=0;
  RVAR=GVAR=BVAR=0;
  RMIN=GMIN=BMIN=0xff;
  RMAX=GMAX=BMAX=0;
  for (int i=0;i<(500*500);i++){
   int c = bufferIn[i];
   float r = (c>>16&0xff);
   float g = (c>> 8&0xff);
   float b = (c &0xff);
   RMEAN += r;
   GMEAN += g;
   BMEAN += b;
   if (r<RMIN) RMIN=r;
   if (g<GMIN) GMIN=g;
   if (b<BMIN) BMIN=b;
   if (r>RMAX) RMAX=r;
   if (g>GMAX) GMAX=g;
   if (b>BMAX) BMAX=b;
   float max = r>g?r>b?r:b:g>b?g:b;
   float min = r<g?r<b?r:b:g<b?g:b;
   float saturation = max<=0?0:1-min/max;
   SMEAN += saturation;
   VMEAN += max;
  }
  RMEAN /= (500*500);
  GMEAN /= (500*500);
  BMEAN /= (500*500);
  SMEAN /= (500*500);
  VMEAN /= (500*500);
  for (int i=0;i<(500*500);i++){
   int c = bufferIn[i];
   float r = (c>>16&0xff);
   float g = (c>> 8&0xff);
   float b = (c &0xff);
   float max = r>g?r>b?r:b:g>b?g:b;
   float min = r<g?r<b?r:b:g<b?g:b;
   float saturation = max<=0?0:1-min/max;
   r -= RMEAN;
   g -= GMEAN;
   b -= BMEAN;
   RVAR += r*r;
   GVAR += g*g;
   BVAR += b*b;
   saturation -= SMEAN;
   max -= VMEAN;
   SVAR += saturation*saturation;
   VVAR += max*max;
  }
  RVAR /= (500*500);
  GVAR /= (500*500);
  BVAR /= (500*500);
  SVAR /= (500*500);
  VVAR /= (500*500);
 }

static void shortRender(int thn, int nthreads){
int [] bufferIn = din;
DataBuffer bufferOut = out .getRaster().getDataBuffer();
DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
int random = rng.nextInt();
int rstart = 500/nthreads*thn;
int rstop = thn==nthreads-1? 500: 500/nthreads*(thn+1);
int cstart = 0;
int cstop = 500;
int halfway = 500/2;
switch(DUPLICATION) {
 case 1:
  rstart = 500/nthreads*thn;
  rstop = thn==nthreads-1? 500: 500/nthreads*(thn+1);
  cstart = 0;
  cstop = 500/2;
 break;
 case 2:
  rstart = 500/2/nthreads*thn;
  rstop = thn==nthreads-1? 500/2+1: (500/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 500;
 break;
 case 3:
  rstart = 500/2/nthreads*thn;
  rstop = thn==nthreads-1? 500/2+1: (500/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 500/2;
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
  int i = row*500 +col;
int x1 = mapping[2*i];
int y1 = mapping[2*i+1];
int x = T1*x1+T2*y1+T3+0x80>>8;
int y = T4*x1+T5*y1+T6+0x80>>8;
switch (BOUNDARY)
{
 case 0:
     while (x<(((500 -3)*256)*2)) x+=(((500 -3)*256)*2);
     while (y<(((500 -3)*256)*2)) y+=(((500 -3)*256)*2);
     x=(x/((500 -3)*256)&1)==1?x%((500 -3)*256):(((500 -3)*256)-1)-x%((500 -3)*256);
     y=(y/((500 -3)*256)&1)==1?y%((500 -3)*256):(((500 -3)*256)-1)-y%((500 -3)*256);
     break;
 case 1:
     while (x<((500 -3)*256))x+=((500 -3)*256);
     while (y<((500 -3)*256))y+=((500 -3)*256);
  x%=((500 -3)*256);
  y%=((500 -3)*256);
  break;
 case 2:
  x = x<0?0:x>((500 -3)*256)?((500 -3)*256):x;
  y = y<0?0:y>((500 -3)*256)?((500 -3)*256):y;
  break;
 case 3:
 case 4:
}
  x1 = x>>8;
  y1 = y>>8;
  y1 *= 500;
  int c=0,r=0,g=0,b=0;
  switch (INTERPOLATION) {
   case 0:
    c = bufferIn[x1+y1];
    r = (c>>16&0xff);
    g = (c>> 8&0xff);
    b = (c &0xff);
    break;
   case 1: {
    int x2 = x1+1;
    int y2 = y1+1*500;
    int ax = (x&0xff)>>5;
    int ay = (y&0xff)>>5;
    int bx = 8-ax;
    int by = 8-ay;
    int c1,c2,c3,c4;
    c1 = bufferIn[x1+y1];
    c2 = bufferIn[x2+y1];
    c3 = c1*bx + c2*ax;
    c1 = bufferIn[x1+y2];
    c2 = bufferIn[x2+y2];
    c4 = c1*bx + c2*ax;
    c4 = (c3&0x7c7c7c0)*by + (c4&0x7c7c7c0)*ay >> 6;
    r = (c4>>16&0xff);
    g = (c4>> 8&0xff);
    b = (c4 &0xff);
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
    int y2 = y1+1*500;
    int y3 = y1+2*500;
    int y4 = y1+3*500;
    int c1,c2,c3,c4;
    c1 = bufferIn[x1+y1];
    c2 = bufferIn[x2+y1];
    c3 = bufferIn[x3+y1];
    c4 = bufferIn[x4+y1];
    int d1r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d1g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d1b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y2];
    c2 = bufferIn[x2+y2];
    c3 = bufferIn[x3+y2];
    c4 = bufferIn[x4+y2];
    int d2r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d2g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d2b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y3];
    c2 = bufferIn[x2+y3];
    c3 = bufferIn[x3+y3];
    c4 = bufferIn[x4+y3];
    int d3r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d3g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d3b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y4];
    c2 = bufferIn[x2+y4];
    c3 = bufferIn[x3+y4];
    c4 = bufferIn[x4+y4];
    int d4r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d4g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d4b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
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
    int RB = r-b;
    int GR = g-r;
    int BG = b-g;
    int r1 = (Q2*(GR-RB)-Q1*BG>>8)+r;
    int Z = (Q2*(BG-RB)+Q1*GR>>8);
    int Bs = BG&~((BG)>>31);
    int GRBs = GR+Bs;
    max = r+(GRBs)&~((GRBs)>>31);
    g += Z + (r-r1);
    b -= Z;
    GR = g-r1;
    BG = b-g;
    Bs = BG&~((BG)>>31);
    GRBs = GR+Bs;
    int adjust = r1+(GRBs)&~((GRBs)>>31);
    adjust = (256*max) / (1+adjust);
    r = r1*adjust >> 8;
    g = g*adjust >> 8;
    b = b*adjust >> 8;
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
  c = (int)((r)<<16|(g)<<8|(b));
  c = ((MOTIONBLUR*(bufferIn[i]&0xff00ff)+(0x100 -MOTIONBLUR)*(c&0xff00ff))+0x800080>>8&0xff00ff|(MOTIONBLUR*(bufferIn[i]&0x00ff00)+(0x100 -MOTIONBLUR)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  random^=random>>19^random<<11^random<<3;
        c = ((NOISE*(random&0xff00ff)+(0x100 -NOISE)*(c&0xff00ff))+0x800080>>8&0xff00ff|(NOISE*(random&0x00ff00)+(0x100 -NOISE)*(c&0x00ff00))+0x008000>>8&0x00ff00);
switch(GRADIENT) {
 case 0:
  break;
 case 1: {
  int alpha = col*256/500*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 2: {
  int alpha = row*256/500*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 3: {
  int alpha = ((row-500/2)*256/500*GRADIENT_BIAS+(col-500/2)*256/500*GRADIENT_GAIN>>7);
  alpha = alpha*alpha>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 4: {
  int alpha = (row-500/2)*512/500*GRADIENT_BIAS >> 6;
  int beta = (col-500 /2)*512/500 *GRADIENT_GAIN >> 6;
  alpha = alpha*alpha + beta*beta >> 8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 5: {
  if ((((row-500/2)*512/500)^(((row-500/2)*512/500)>>31))>((GRADIENT_BIAS)^((GRADIENT_BIAS)>>31))) c=0;
  if ((((col-500 /2)*512/500)^(((col-500 /2)*512/500)>>31))>((GRADIENT_GAIN)^((GRADIENT_GAIN)>>31))) c=0;
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
  bufferOut.setElem(row*500 +500 -1-col,c);
 break;
 case 2:
  bufferOut.setElem(i,c);
  if (row>0)
  bufferOut.setElem((500 -row)*500 +500 -1-col,c);
 break;
 case 3:
  bufferOut.setElem(i,c);
  int ty1 = (500 -1)*500 -row*500;
  int tx1 = 500 -1-col;
  bufferOut.setElem(row*500 +tx1,c);
  bufferOut.setElem(ty1+tx1,c);
  bufferOut.setElem(ty1+col,c);
 break;
 case 4:
  c = (c>>1)&0x7f7f7f;
  int tx = row*500 +500 -1-col;
  if (col>halfway) c += bufferOut.getElem(i);
  bufferOut.setElem(i,c);
  bufferOut.setElem(tx,c);
 break;
}
 }
}
static void intRender(int thn, int nthreads){
int [] bufferIn = din;
DataBuffer bufferOut = out .getRaster().getDataBuffer();
DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
int random = rng.nextInt();
int rstart = 500/nthreads*thn;
int rstop = thn==nthreads-1? 500: 500/nthreads*(thn+1);
int cstart = 0;
int cstop = 500;
int halfway = 500/2;
switch(DUPLICATION) {
 case 1:
  rstart = 500/nthreads*thn;
  rstop = thn==nthreads-1? 500: 500/nthreads*(thn+1);
  cstart = 0;
  cstop = 500/2;
 break;
 case 2:
  rstart = 500/2/nthreads*thn;
  rstop = thn==nthreads-1? 500/2+1: (500/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 500;
 break;
 case 3:
  rstart = 500/2/nthreads*thn;
  rstop = thn==nthreads-1? 500/2+1: (500/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 500/2;
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
  int i = row*500 +col;
int x1 = mapping[2*i];
int y1 = mapping[2*i+1];
int x = T1*x1+T2*y1+T3+0x80>>8;
int y = T4*x1+T5*y1+T6+0x80>>8;
switch (BOUNDARY)
{
 case 0:
     while (x<(((500 -3)*256)*2)) x+=(((500 -3)*256)*2);
     while (y<(((500 -3)*256)*2)) y+=(((500 -3)*256)*2);
     x=(x/((500 -3)*256)&1)==1?x%((500 -3)*256):(((500 -3)*256)-1)-x%((500 -3)*256);
     y=(y/((500 -3)*256)&1)==1?y%((500 -3)*256):(((500 -3)*256)-1)-y%((500 -3)*256);
     break;
 case 1:
     while (x<((500 -3)*256))x+=((500 -3)*256);
     while (y<((500 -3)*256))y+=((500 -3)*256);
  x%=((500 -3)*256);
  y%=((500 -3)*256);
  break;
 case 2:
  x = x<0?0:x>((500 -3)*256)?((500 -3)*256):x;
  y = y<0?0:y>((500 -3)*256)?((500 -3)*256):y;
  break;
 case 3:
 case 4:
}
  x1 = x>>8;
  y1 = y>>8;
  y1 *= 500;
  int c=0,r=0,g=0,b=0;
  switch (INTERPOLATION) {
   case 0:
    c = bufferIn[x1+y1];
    r = (c>>16&0xff);
    g = (c>> 8&0xff);
    b = (c &0xff);
    break;
   case 1: {
    int x2 = x1+1;
    int y2 = y1+1*500;
    int ax = x&0xff;
    int ay = y&0xff;
    int bx = 256-ax;
    int by = 256-ay;
    int c1,c2,c3,c4;
    c1 = bufferIn[x1+y1];
    c2 = bufferIn[x2+y1];
    c3 = ((ax*(c2&0xff00ff)+(0x100 -ax)*(c1&0xff00ff))+0x800080>>8&0xff00ff|(ax*(c2&0x00ff00)+(0x100 -ax)*(c1&0x00ff00))+0x008000>>8&0x00ff00);
    c1 = bufferIn[x1+y2];
    c2 = bufferIn[x2+y2];
    c4 = ((ax*(c2&0xff00ff)+(0x100 -ax)*(c1&0xff00ff))+0x800080>>8&0xff00ff|(ax*(c2&0x00ff00)+(0x100 -ax)*(c1&0x00ff00))+0x008000>>8&0x00ff00);
    c4 = ((ay*(c4&0xff00ff)+(0x100 -ay)*(c3&0xff00ff))+0x800080>>8&0xff00ff|(ay*(c4&0x00ff00)+(0x100 -ay)*(c3&0x00ff00))+0x008000>>8&0x00ff00);
    r = (c4>>16&0xff);
    g = (c4>> 8&0xff);
    b = (c4 &0xff);
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
    int y2 = y1+1*500;
    int y3 = y1+2*500;
    int y4 = y1+3*500;
    int c1,c2,c3,c4;
    c1 = bufferIn[x1+y1];
    c2 = bufferIn[x2+y1];
    c3 = bufferIn[x3+y1];
    c4 = bufferIn[x4+y1];
    int d1r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d1g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d1b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y2];
    c2 = bufferIn[x2+y2];
    c3 = bufferIn[x3+y2];
    c4 = bufferIn[x4+y2];
    int d2r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d2g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d2b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y3];
    c2 = bufferIn[x2+y3];
    c3 = bufferIn[x3+y3];
    c4 = bufferIn[x4+y3];
    int d3r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d3g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d3b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y4];
    c2 = bufferIn[x2+y4];
    c3 = bufferIn[x3+y4];
    c4 = bufferIn[x4+y4];
    int d4r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    int d4g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    int d4b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
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
  c = (int)((r)<<16|(g)<<8|(b));
  c = ((MOTIONBLUR*(bufferIn[i]&0xff00ff)+(0x100 -MOTIONBLUR)*(c&0xff00ff))+0x800080>>8&0xff00ff|(MOTIONBLUR*(bufferIn[i]&0x00ff00)+(0x100 -MOTIONBLUR)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  random^=random>>19^random<<11^random<<3;
        c = ((NOISE*(random&0xff00ff)+(0x100 -NOISE)*(c&0xff00ff))+0x800080>>8&0xff00ff|(NOISE*(random&0x00ff00)+(0x100 -NOISE)*(c&0x00ff00))+0x008000>>8&0x00ff00);
switch(GRADIENT) {
 case 0:
  break;
 case 1: {
  int alpha = col*256/500*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 2: {
  int alpha = row*256/500*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 3: {
  int alpha = ((row-500/2)*256/500*GRADIENT_BIAS+(col-500/2)*256/500*GRADIENT_GAIN>>7);
  alpha = alpha*alpha>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 4: {
  int alpha = (row-500/2)*512/500*GRADIENT_BIAS >> 6;
  int beta = (col-500 /2)*512/500 *GRADIENT_GAIN >> 6;
  alpha = alpha*alpha + beta*beta >> 8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 5: {
  if ((((row-500/2)*512/500)^(((row-500/2)*512/500)>>31))>((GRADIENT_BIAS)^((GRADIENT_BIAS)>>31))) c=0;
  if ((((col-500 /2)*512/500)^(((col-500 /2)*512/500)>>31))>((GRADIENT_GAIN)^((GRADIENT_GAIN)>>31))) c=0;
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
  bufferOut.setElem(row*500 +500 -1-col,c);
 break;
 case 2:
  bufferOut.setElem(i,c);
  if (row>0)
  bufferOut.setElem((500 -row)*500 +500 -1-col,c);
 break;
 case 3:
  bufferOut.setElem(i,c);
  int ty1 = (500 -1)*500 -row*500;
  int tx1 = 500 -1-col;
  bufferOut.setElem(row*500 +tx1,c);
  bufferOut.setElem(ty1+tx1,c);
  bufferOut.setElem(ty1+col,c);
 break;
 case 4:
  c = (c>>1)&0x7f7f7f;
  int tx = row*500 +500 -1-col;
  if (col>halfway) c += bufferOut.getElem(i);
  bufferOut.setElem(i,c);
  bufferOut.setElem(tx,c);
 break;
}
 }
}
static void floatRender(int thn, int nthreads){
int [] bufferIn = din;
DataBuffer bufferOut = out .getRaster().getDataBuffer();
DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
int random = rng.nextInt();
int rstart = 500/nthreads*thn;
int rstop = thn==nthreads-1? 500: 500/nthreads*(thn+1);
int cstart = 0;
int cstop = 500;
int halfway = 500/2;
switch(DUPLICATION) {
 case 1:
  rstart = 500/nthreads*thn;
  rstop = thn==nthreads-1? 500: 500/nthreads*(thn+1);
  cstart = 0;
  cstop = 500/2;
 break;
 case 2:
  rstart = 500/2/nthreads*thn;
  rstop = thn==nthreads-1? 500/2+1: (500/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 500;
 break;
 case 3:
  rstart = 500/2/nthreads*thn;
  rstop = thn==nthreads-1? 500/2+1: (500/2+1)/nthreads*(thn+1);
  cstart = 0;
  cstop = 500/2;
 break;
 case 4:

 break;
}

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


 for (int row=rstart; row<rstop; row++)
 for (int col=cstart; col<cstop; col++) {
  int i = row*500 +col;
int x1 = mapping[2*i];
int y1 = mapping[2*i+1];
int x = T1*x1+T2*y1+T3+0x80>>8;
int y = T4*x1+T5*y1+T6+0x80>>8;
switch (BOUNDARY)
{
 case 0:
     while (x<(((500 -3)*256)*2)) x+=(((500 -3)*256)*2);
     while (y<(((500 -3)*256)*2)) y+=(((500 -3)*256)*2);
     x=(x/((500 -3)*256)&1)==1?x%((500 -3)*256):(((500 -3)*256)-1)-x%((500 -3)*256);
     y=(y/((500 -3)*256)&1)==1?y%((500 -3)*256):(((500 -3)*256)-1)-y%((500 -3)*256);
     break;
 case 1:
     while (x<((500 -3)*256))x+=((500 -3)*256);
     while (y<((500 -3)*256))y+=((500 -3)*256);
  x%=((500 -3)*256);
  y%=((500 -3)*256);
  break;
 case 2:
  x = x<0?0:x>((500 -3)*256)?((500 -3)*256):x;
  y = y<0?0:y>((500 -3)*256)?((500 -3)*256):y;
  break;
 case 3:
 case 4:
}
  x1 = x>>8;
  y1 = y>>8;
  y1 *= 500;
  float r=0f,g=0f,b=0f;
  switch (INTERPOLATION) {
   case 0: {
    int c1 = bufferIn[x1+y1];
    r = (c1>>16&0xff);
    g = (c1>> 8&0xff);
    b = (c1 &0xff);
   break;}
   case 1: {
    float ax1 = (x&0xff)/256.f;
    float ay1 = (y&0xff)/256.f;
    int x2 = x1+1;
    int y2 = y1+1*500;
    int c1,c2,c3,c4;
    float bx1 = 1.0f-ax1;
    float by1 = 1.0f-ay1;
    c1 = bufferIn[x1+y1];
    c2 = bufferIn[x2+y1];
    float d1r = ax1*(c2>>16&0xff)+bx1*(c1>>16&0xff);
    float d1g = ax1*(c2>> 8&0xff)+bx1*(c1>> 8&0xff);
    float d1b = ax1*(c2 &0xff)+bx1*(c1 &0xff);
    c1 = bufferIn[x1+y2];
    c2 = bufferIn[x2+y2];
    float d2r = ax1*(c2>>16&0xff)+bx1*(c1>>16&0xff);
    float d2g = ax1*(c2>> 8&0xff)+bx1*(c1>> 8&0xff);
    float d2b = ax1*(c2 &0xff)+bx1*(c1 &0xff);
    r = ay1*d2r+by1*d1r;
    g = ay1*d2g+by1*d1g;
    b = ay1*d2b+by1*d1b;
   break;}
   case 2: {
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
    int y2 = y1+1*500;
    int y3 = y1+2*500;
    int y4 = y1+3*500;
    int c1,c2,c3,c4;
    c1 = bufferIn[x1+y1];
    c2 = bufferIn[x2+y1];
    c3 = bufferIn[x3+y1];
    c4 = bufferIn[x4+y1];
    float d1r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    float d1g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    float d1b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y2];
    c2 = bufferIn[x2+y2];
    c3 = bufferIn[x3+y2];
    c4 = bufferIn[x4+y2];
    float d2r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    float d2g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    float d2b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y3];
    c2 = bufferIn[x2+y3];
    c3 = bufferIn[x3+y3];
    c4 = bufferIn[x4+y3];
    float d3r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    float d3g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    float d3b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
    c1 = bufferIn[x1+y4];
    c2 = bufferIn[x2+y4];
    c3 = bufferIn[x3+y4];
    c4 = bufferIn[x4+y4];
    float d4r = bx1*(c1>>16&0xff)+bx2*(c2>>16&0xff)+bx3*(c3>>16&0xff)+bx4*(c4>>16&0xff);
    float d4g = bx1*(c1>> 8&0xff)+bx2*(c2>> 8&0xff)+bx3*(c3>> 8&0xff)+bx4*(c4>> 8&0xff);
    float d4b = bx1*(c1 &0xff)+bx2*(c2 &0xff)+bx3*(c3 &0xff)+bx4*(c4 &0xff);
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
    float min = r<b?g<r?g:r:g<b?g:b;
    float value = r>b?g>r?g:r:g>b?g:b;
    float C= value - min;
    if (C!=0.f) {
     float hue;
     if (value==r) hue = 6+(g-b)/C;
     else if (value==g) hue = 2+(b-r)/C;
     else hue = 4+(r-g)/C;
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
    float Z = Q2*(bg-rb)+Q1*gr;
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
    float Z = Q2*(bg-rb)+Q1*gr;
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
  int G = g<0.f?0:g>=256.f?0xff:(int)g;
  int B = b<0.f?0:b>=256.f?0xff:(int)b;
  c = (int)((R)<<16|(G)<<8|(B));
  c = ((MOTIONBLUR*(bufferIn[i]&0xff00ff)+(0x100 -MOTIONBLUR)*(c&0xff00ff))+0x800080>>8&0xff00ff|(MOTIONBLUR*(bufferIn[i]&0x00ff00)+(0x100 -MOTIONBLUR)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  random^=random>>19^random<<11^random<<3;
        c = ((NOISE*(random&0xff00ff)+(0x100 -NOISE)*(c&0xff00ff))+0x800080>>8&0xff00ff|(NOISE*(random&0x00ff00)+(0x100 -NOISE)*(c&0x00ff00))+0x008000>>8&0x00ff00);
switch(GRADIENT) {
 case 0:
  break;
 case 1: {
  int alpha = col*256/500*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 2: {
  int alpha = row*256/500*GRADIENT_GAIN+GRADIENT_BIAS*256>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 3: {
  int alpha = ((row-500/2)*256/500*GRADIENT_BIAS+(col-500/2)*256/500*GRADIENT_GAIN>>7);
  alpha = alpha*alpha>>8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 4: {
  int alpha = (row-500/2)*512/500*GRADIENT_BIAS >> 6;
  int beta = (col-500 /2)*512/500 *GRADIENT_GAIN >> 6;
  alpha = alpha*alpha + beta*beta >> 8;
  alpha = alpha<0?0:alpha>0xff?0xff:alpha;
  c = ((alpha*(0&0xff00ff)+(0x100 -alpha)*(c&0xff00ff))+0x800080>>8&0xff00ff|(alpha*(0&0x00ff00)+(0x100 -alpha)*(c&0x00ff00))+0x008000>>8&0x00ff00);
  }
  break;
 case 5: {
  if ((((row-500/2)*512/500)^(((row-500/2)*512/500)>>31))>((GRADIENT_BIAS)^((GRADIENT_BIAS)>>31))) c=0;
  if ((((col-500 /2)*512/500)^(((col-500 /2)*512/500)>>31))>((GRADIENT_GAIN)^((GRADIENT_GAIN)>>31))) c=0;
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
  bufferOut.setElem(row*500 +500 -1-col,c);
 break;
 case 2:
  bufferOut.setElem(i,c);
  if (row>0)
  bufferOut.setElem((500 -row)*500 +500 -1-col,c);
 break;
 case 3:
  bufferOut.setElem(i,c);
  int ty1 = (500 -1)*500 -row*500;
  int tx1 = 500 -1-col;
  bufferOut.setElem(row*500 +tx1,c);
  bufferOut.setElem(ty1+tx1,c);
  bufferOut.setElem(ty1+col,c);
 break;
 case 4:
  c = (c>>1)&0x7f7f7f;
  int tx = row*500 +500 -1-col;
  if (col>halfway) c += bufferOut.getElem(i);
  bufferOut.setElem(i,c);
  bufferOut.setElem(tx,c);
 break;
}
 }
}

 public static void render(int threadnumber, int nthreads){
  switch(PRECISION)
  {
   case 0: shortRender(threadnumber,nthreads); break;
   case 1: intRender(threadnumber,nthreads); break;
   case 2: floatRender(threadnumber,nthreads); break;
  }
 }
}
