/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static util.ColorUtil.fancy;
import static util.ColorUtil.fast;

/**
 *
 * @author mer49
 */
public class ImageRenderContext 
{
    public static final int 
        REFLECT_MIRROR   = 0,
        REFLECT_REPEAT   = 0,
        REFLECT_TRIANGLE = 0;
    
    public BufferedImage img;
    public Graphics2D    g0;
    public Graphics2D    g2D;
    public Graphics2D    g;
    public DataBuffer    buf;
    public Samplers      samplers;
    public Samplers.Sampler get;
    int W, H;
    
    boolean is_scaled = false;
    float   scale = 1f;

    /**
     *
     * @param b
     * @param interpolate
     * @param reflect
     */
    public ImageRenderContext(BufferedImage b, 
            boolean interpolate, int reflect) {
        img  = b;
        img.setAccelerationPriority(1.f);
        
        g0   = fast(b.createGraphics());
        g2D  = fancy(b.createGraphics());
        g    = g2D;
        buf  = b.getRaster().getDataBuffer();
        samplers = new Samplers(b);
        
        setInterpolatedAndReflected(interpolate, reflect);
        
        get = samplers.getFixed8Bit;
        W = b.getWidth();
        H = b.getHeight();
    }
    
    public ImageRenderContext(BufferedImage b, 
            boolean interpolate, int reflect, float scale) {
        this(b, interpolate, reflect);
        this.is_scaled = true;
        this.scale = scale;
        get = samplers.makeScaledGrabber(get, scale);
    }
    
    /** The following return statement decides which
     * grabber will ultimately be used as a default one.
     * @param x
     * @param y
     * @return
     */
    /*
    public int get(int x, int y) {
        return is_scaled
                ? grab.get((int)(x*scale),(int)(y*scale))
                : grab.get(x, y);
    }
    */

    /**
     *
     * @param fancy
     */
    public void setFancy(boolean fancy) {
        g = fancy ? g2D : g0;
    }

    /**
     *
     * @param inter
     * @param reflect
     */
    public final void setInterpolatedAndReflected(boolean inter, int reflect) {
        switch (reflect) {
            case 0:
                get = inter? samplers.getFixed8Bit : samplers.get;
                break;
            case 1:
                get = inter? samplers.getFixed8BitNoReflect : samplers.getNoReflect;
                break;
            case 2:
                get = inter? samplers.getTriangleFixed8Bit : samplers.getTriangle;
                break;
        }
        if (is_scaled)    
            get = samplers.makeScaledGrabber(get, scale);
    }
    
}
