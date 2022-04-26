package perceptron;
import java.awt.image.DataBuffer;
import static java.lang.Math.*;
import image.DoubleBuffer.ImageRenderContext;
import util.ColorUtility;

import image.DoubleBuffer;

/**
 *
 * @author mrule
 */
public class Convolution {

    DoubleBuffer buffer;

    int [] d ;
    int s,h,e ;
    int [] temp;

    static double gaussian( double x, double sigma ) {
        return exp(-.5*pow(x/sigma,2))/(sigma*sqrt(2*PI));
    }

    /**
     *
     * @param std
     * @param b
     */
    public Convolution(float std,DoubleBuffer b){
        s = (int)(4*std);
        h = s/2;
        e = 256/h;
        d = new int[s];
        buffer = b;
        for (int i=0; i<s; i++ )
            d[i] = (int)(256*gaussian(i-s/2,std));
        temp = new int[b.buffer.W*b.buffer.H];
    }

    /**
     *
     * @param amount
     */
    public void operate(int amount) {
        ImageRenderContext source = buffer.output;
        ImageRenderContext dest   = buffer.buffer;
        DataBuffer sourcebuffer   = buffer.output.buffer;
        DataBuffer destbuffer     = buffer.buffer.buffer;

        int W = buffer.output.W;
        int H = buffer.output.H;
        int E = e;
        int Hp = H - h;
        int Wp = W - h;
        int U = H;
        //Do X blur
        int i = 0;
        for (int y=0; y<H; y++) {
            for (int x=0; x<W; x++) {
                int Y=0,G=0;
                for (int k=0; k<s; k++) {
                    int c = source.get.get(x+k-h<<8,y<<8);
                    int w = d[k];
                    Y += w*(c&0xff00ff);
                    G += w*(c&0x00ff00);
                }
                temp[i++]=(0xff00ff00&Y|0x00ff0000&G)>>8;
            }
        }
        //Do Y blur
        i = 0;
        int notamount = 256 - amount;
        for (int y=0; y<h; y++) {
            for (int x=0; x<W; x++) {
                int Y=0,G=0;
                for (int k=0; k<s; k++) {
                    int y2 = (y-h+k+H)%H;
                    int c = temp[x+W*(y2)];
                    int w = d[k];
                    Y += w*(c&0xff00ff);
                    G += w*(c&0x00ff00);
                }
                int c1 = (0xff00ff00&Y|0x00ff0000&G)>>8 ;
                int c2 = sourcebuffer.getElem(i)<<1;
                int r  = ((c2>>16)&0x1fe)-((c1>>16)&0xff);
                int g  = ((c2>>8 )&0x1fe)-((c1>>8 )&0xff);
                int b  = ((c2    )&0x1fe)-((c1    )&0xff);
                r = r<0?0:r>0xff?0xff:r;
                g = g<0?0:g>0xff?0xff:g;
                b = b<0?0:b>0xff?0xff:b;
                c2 = (r<<16)|(g<<8)|(b);
                c2 = ColorUtility.average(c1,amount,c2,notamount);
                destbuffer.setElem(i++,c2);
            }
        }
        for (int y=h; y<Hp; y++) {
            for (int x=0; x<W; x++) {
                int Y=0,G=0;
                for (int k=0; k<s; k++) {
                    int c = temp[x+W*(y-h+k)];
                    int w = d[k];
                    Y += w*(c&0xff00ff);
                    G += w*(c&0x00ff00);
                }
                int c1 = (0xff00ff00&Y|0x00ff0000&G)>>8 ;
                int c2 = sourcebuffer.getElem(i)<<1;
                int r  = ((c2>>16)&0x1fe)-((c1>>16)&0xff);
                int g  = ((c2>>8 )&0x1fe)-((c1>>8 )&0xff);
                int b  = ((c2    )&0x1fe)-((c1    )&0xff);
                r = r<0?0:r>0xff?0xff:r;
                g = g<0?0:g>0xff?0xff:g;
                b = b<0?0:b>0xff?0xff:b;
                c2 = (r<<16)|(g<<8)|(b);
                c2 = ColorUtility.average(c1,amount,c2,notamount);
                destbuffer.setElem(i++,c2);
            }
        }
        for (int y=Hp; y<H; y++) {
            for (int x=0; x<W; x++) {
                int Y=0,G=0;
                for (int k=0; k<s; k++) {
                    int y2 = y-h+k;
                    if (y2<0) y2=0;
                    else if (y2>=H) y2=H-1;
                    int c = temp[x+W*(y2)];
                    int w = d[k];
                    Y += w*(c&0xff00ff);
                    G += w*(c&0x00ff00);
                }
                int c1 = (0xff00ff00&Y|0x00ff0000&G)>>8 ;
                int c2 = sourcebuffer.getElem(i)<<1;
                int r  = ((c2>>16)&0x1fe)-((c1>>16)&0xff);
                int g  = ((c2>>8 )&0x1fe)-((c1>>8 )&0xff);
                int b  = ((c2    )&0x1fe)-((c1    )&0xff);
                r = r<0?0:r>0xff?0xff:r;
                g = g<0?0:g>0xff?0xff:g;
                b = b<0?0:b>0xff?0xff:b;
                c2 = (r<<16)|(g<<8)|(b);
                c2 = ColorUtility.average(c1,amount,c2,notamount);
                destbuffer.setElem(i++,c2);
            }
        }
    }
}
