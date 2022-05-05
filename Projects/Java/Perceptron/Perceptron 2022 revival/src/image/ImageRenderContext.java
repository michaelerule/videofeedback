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
            boolean interpolate, boolean reflect) {
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
            boolean interpolate, boolean reflect, float scale) {
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
    public final void setInterpolatedAndReflected(boolean inter, boolean reflect) {
        get = inter 
            ? (reflect? samplers.getFixed8Bit : samplers.getFixed8BitNoReflect)
            : (reflect? samplers.get          : samplers.getNoReflect);
        if (is_scaled)    
            get = samplers.makeScaledGrabber(get, scale);
    }
    
}
