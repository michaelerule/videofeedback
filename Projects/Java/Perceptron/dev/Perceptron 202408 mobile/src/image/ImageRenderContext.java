/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static color.ColorUtil.fancy;
import static color.ColorUtil.fast;

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
    
    public BufferedImage        img;
    public Graphics2D           g0;
    public Graphics2D           g2D;
    public Graphics2D           g;
    public DataBuffer           buf;
    public Samplers             samplers;
    public Samplers.Sampler8Bit get;
    
    boolean is_scaled = false;
    float   scale = 1f;
    
    // These may need to be non-final to implement resolution changes?
    public final int w, h;

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
        
        get = samplers.getReflect8Bit;
        w = b.getWidth();
        h = b.getHeight();
    }
    
    public ImageRenderContext(BufferedImage b, 
            boolean interpolate, int reflect, float scale) {
        this(b, interpolate, reflect);
        this.is_scaled = true;
        this.scale = scale;
        get = samplers.makeScaledGrabber(get, scale);
    }
    
    public ImageRenderContext(int w, int h, boolean interpolate, int reflect) {
        this(new BufferedImage(w,h,TYPE_INT_RGB), interpolate, reflect);
    }

    /**
     *
     * @param fancy
     */
    public void setFancy(boolean fancy) {
        g = fancy ? g2D : g0;
    }

    /**
     *
     * @param ip
     * @param ref
     */
    public final void setInterpolatedAndReflected(boolean ip, int ref) {
        switch (ref) {
            case 0 -> get = ip? samplers.getReflect8Bit : samplers.getReflect;
            case 1 -> get = ip? samplers.getWrap8Bit : samplers.getWrap;
            case 2 -> get = ip? samplers.getTriangle8Bit : samplers.getTriangle;
        }
        if (is_scaled)    
            get = samplers.makeScaledGrabber(get, scale);
    }
    
}
