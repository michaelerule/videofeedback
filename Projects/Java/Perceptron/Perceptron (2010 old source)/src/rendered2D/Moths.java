
package rendered2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import math.complex;

import static java.lang.Math.* ;

public class Moths {
    
    class Moth extends Point2D.Float {
        
        float vx ;
        float vy ;
        float px ;
        float py ;
        float flightspeed ;
        Color color ;
        
        public Moth( float fs, float x, float y, Color c ) {
            super( x, y );
            flightspeed = fs ;
            color = c ;
        }
        
        public void step( float dt ) {
            px = x ;
            py = y ;
            x += vx*dt ;
            y += vy*dt ;
            bound() ;
        }
        
        void bound() {
            if ( x < 0 ) {
                x = 0 ;
                vx = -vx ;
            } else if ( x >= w ) {
                x = w-1 ;
                vx = -vx ;
            }
            if ( y < 0 ) {
                y = 0 ;
                vy = -vy ;
            } else if ( y >= h ) {
                y = h-1 ;
                vy = -vy ;
            }
        }
        
        public void normalisevelocity() {
            float speed = (float)sqrt(vx*vx+vy*vy);
              if ( speed <= .0001f ) speed = .0001f;
            float scalar = flightspeed/speed ;
            vx = vx*scalar;
            vy = vy*scalar;
        }
        
        public void addnoise() {
            vx += (random()-.5)*2f ;
            vy += (random()-.5)*2f ;
        }
        
        public void paint( Graphics g ) {
            g.setColor(color);
            g.drawLine((int)px,(int)py,(int)x,(int)y);
        }
        
        public void paintspecial( Graphics2D g ) {
            /*
             [ ct st dx ]
             [-st ct dy ]
             [  0  0 1  ]
             */ 
            float r  = 1f/(float)sqrt(vx*vx+vy*vy);
            float st = -vx * r ;
            float ct = vy * r ;
            AffineTransform v = AffineTransform.getTranslateInstance(-25,-25);
            AffineTransform t = AffineTransform.getRotateInstance(.5*PI+atan2(vy,vx));
            AffineTransform u = AffineTransform.getTranslateInstance(x,y);
            t.concatenate(v);
            u.concatenate(t);
            g.drawImage(mothsprite, u, null);
        }
        
        public void bounce( float tx, float ty ) {
            if ( x == tx && y == ty ) {
                vx = -vx ;
                vy = -vy ;
            } else {
                
                float nx = x - tx ;
                float ny = y - ty ;
                float theta = -(float)atan2( ny , nx );
                
                float ct = (float)cos(theta);
                float st = (float)sin(theta);
                float rvx = ct*vx + st*vy ;
                float rvy = ct*vy - st*vx ;
                rvx = -rvx ;
                vx = ct*rvx - st*rvy ;
                vy = ct*rvy + st*rvx ;
            }
        }
    }
    
    final Color lightColor = new Color(0xffffff00,true);
    int splinepoints = 20 ;
    
    class Light extends Point2D.Float {
        
        float intensity ;
        
        float [] xspline = new float[splinepoints];
        float [] yspline = new float[splinepoints];
        
        public Light( float nx, float ny, float inten ) {
            super( nx, ny );
            intensity = inten ;
            resetspline();
        }
        
        void resetspline() {
            for ( int i = 0 ; i < splinepoints ; i ++ )
            {
                xspline[i] = x ;
                yspline[i] = y ;
            }
        }
        
        public void step() {
            xspline[0] += (random() - .5)*40 ;
            yspline[0] += (random() - .5)*40 ;
            
            for ( int i = 1 ; i < splinepoints ; i ++ )
            {
                xspline[i]+=(xspline[i-1] - xspline[i])*.5f ;
                yspline[i]+=(yspline[i-1] - yspline[i])*.5f ;
            }
            
            x+=(xspline[splinepoints-1]-x)*.5f;
            y+=(yspline[splinepoints-1]-y)*.5f;
            
            bound();
        }
          
        void bound() {
            if ( x < 0 ) {
                x = 0 ;
                resetspline();
            } else if ( x > w ) {
                x = w ;
                resetspline();
            }
            if ( y < 0 ) {
                y = 0 ;
                resetspline();
            } else if ( y > h ) {
                y = h ;
                resetspline();
            }
        }
        
        public void paint( Graphics g ) {
            g.setColor(lightColor);
            g.fillOval((int)(x-9.5f), (int)(y-9.5f), 20, 20);
        }
    }
    
    float w ;
    float h ;
    
    ArrayList<Moth>  moths ;
    ArrayList<Light> lights ;
    
    BufferedImage mothsprite ;
    
    static BufferedImage grabImage() {
        try {
            File f = new File( "moth.png" ) ;
            BufferedImage image1 = ImageIO.read( f ) ;
            BufferedImage image2 = new BufferedImage( 
                    image1.getWidth() , 
                    image1.getHeight() , 
                    BufferedImage.TYPE_INT_ARGB ) ;
            image2.getGraphics().drawImage( image1 , 0 , 0 , null );
            
            DataBuffer d = image2.getRaster().getDataBuffer();
            for ( int i = 0 ; i < d.getSize() ; i++ ) {
                int c = d.getElem(i);
                if ( (c&0xffffff) == 0xff00ff )
                    d.setElem(i,0x00000000);
            }
            return image2 ;
        } catch ( Exception e ) {
            System.err.println("could not open image");
        }
        return null ;
    }
    
    public Moths( float w , float h ) {
        this.w = w ;
        this.h = h ;
        
        moths  = new ArrayList<Moth>();
        lights = new ArrayList<Light>();
        
        for ( int i = 0 ; i < 10 ; i ++ ) 
        {
            float x = (float)random()*w;
            float y = (float)random()*h;
            moths.add( new Moth(1,x,y,new Color(0xffffffff,true)));
        }
        for ( int i = 0 ; i < 1 ; i ++ ) 
        {
            float x = (float)random()*w;
            float y = (float)random()*h;
            lights.add(new Light(x,y,10));
        }
        
        mothsprite = grabImage() ;
    }
    
    public void light(float x, float y) {
        Light L = lights.get(0);
        L.x = x ;
        L.y = y ;
    }
    
    public void paint( Graphics2D g ) {
        //for ( Light l : lights ) l.paint( g );
        for ( Moth m : moths ) m.paintspecial( g );
    }
    
    public void step( float dt ) {
        
        //for ( Light L : lights ) L.step();
        
        for ( Moth M : moths ) {
            float fx = 0 ;
            float fy = 0 ;
            float px = M.x;
            float py = M.y;
            float normal = 0f ;
            M.addnoise();
            M.step(dt);
            
            complex 
              z1=new complex(),
              z2=new complex(),
              z3=new complex() ;
            for ( Light L : lights ) {
                float relativeintensity = L.intensity / (float)M.distanceSq(L);
                
                z1.setComplex( px  - L.x , py  - L.y);
                complex z5 = product3( M.x - L.x , M.y - L.y, M.x - px, M.y - py );
                complex z4 = z5.over(z1);
                z4.scale(1f/complex.mod(z4));
                fx += z4.real;
                fy += z4.imag;
                normal += 1;
            }
            M.vx = fx ;
            M.vy = fy ;
            M.normalisevelocity();
        }
    } 
    
    static complex product3( float real, float imag, float nreal, float nimag ) {
        float x4 = real * nreal ;
        float x5 = imag * nimag ;
        return new complex( x4 - x5 , ( real + imag ) * ( nreal + nimag ) - x4 - x5 ) ;
    }
    static complex oneOver( float real, float imag ) {
        return (new complex(real,-imag)).scale(real*real+imag*imag);
    }
    static complex divide( float real, float imag, float nreal, float nimag ) {
        float rr = real*nreal-nimag*nimag ;
        nimag /= -rr;
        nreal /=  rr;
        float x4 = real * nreal ;
        float x5 = imag * nimag ;
        return new complex( x4 - x5 , ( real + imag ) * ( nreal + nimag ) - x4 - x5 ) ;
    }
}
