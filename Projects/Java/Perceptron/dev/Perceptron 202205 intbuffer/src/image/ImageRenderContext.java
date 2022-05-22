/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.awt.image.DataBuffer;
import static color.ColorUtil.fancy;
import static color.ColorUtil.fast;

/**
 *
 * @author mer49
 */
public class ImageRenderContext 
{
    public static final int 
        MIRROR   = 0,
        WRAP     = 0,
        TRIANGLE = 0;
    
    public final BufferedImage img;
    public final int w, h;

    final boolean is_scaled;
    final float   scale;
    
    public Graphics2D    g0;
    public Graphics2D    g2D;
    public Graphics2D    g;
    
    public DataBuffer    buf;
    
    public Samplers      samplers;
    public Samplers.Sampler8Bit get;
    

    
    public ImageRenderContext(BufferedImage b, boolean interp, int reflect, boolean is_scaled, float scale) {

        img  = b;
        img.setAccelerationPriority(1.f);
        
        g0   = fast(b.createGraphics());
        g2D  = fancy(b.createGraphics());
        g    = g2D;
        buf  = b.getRaster().getDataBuffer();
        samplers = new Samplers(b);
        
        setInterpolatedAndReflected(interp, reflect);
        
        get = samplers.getReflect8Bit;
        w = b.getWidth();
        h = b.getHeight();
        
        this.is_scaled = is_scaled;
        this.scale     = scale;
        
        get = samplers.makeScaledGrabber(get, scale);
    }
    
    public ImageRenderContext(BufferedImage b, boolean interp, int reflect) {
        this(b,interp,reflect,false,1.0f);
    }
    
    public ImageRenderContext(int w, int h, boolean interpolate, int reflect) {
        this(new BufferedImage(w,h,TYPE_INT_RGB), interpolate, reflect);
    }
    
    public void setFancy(boolean fancy) {
        g = fancy ? g2D : g0;
    }

    public final void setInterpolatedAndReflected(boolean inter, int reflect) {
        int i = (inter?1:0)*3 + reflect;
        get = (new Samplers.Sampler8Bit[]{
            samplers.getReflect,
            samplers.getWrap,
            samplers.getTriangle,
            samplers.getReflect8Bit,
            samplers.getWrap8Bit,
            samplers.getTriangle8Bit})[i];
        if (is_scaled)    
            get = samplers.makeScaledGrabber(get, scale);
    }
    
}
