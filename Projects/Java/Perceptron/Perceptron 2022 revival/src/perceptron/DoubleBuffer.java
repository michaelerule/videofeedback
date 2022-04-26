package perceptron;
//
//  DoubleBuffer.java
//
//
//  Created by Michael Rule on 6/1/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import util.ColorUtil;
import java.awt.image.*;
import perceptron.Samplers.Grabber;
import static perceptron.Misc.clip;

/**
 *
 * @author mer49
 */
public class DoubleBuffer {

    /** Image storage buffers. */
    public ImageRenderContext 
        out,    // Feedback output; Same as display if no post-processing.
        buf,    // Temporary buffer for rendering
        img,     // Buffer to store the current input image
        fde, // Used to fade between two inpiut images
        dsp;   // On-screen display; Contains post-processing effects.
    
    /**
     *
     */
    public boolean reflect     = true;
    public boolean interpolate = true;
    public boolean fancy       = true;
    
    final Perceptron P;

    /**
     *
     * @param P
     * @param a
     * @param b
     * @param s
     * @param d
     */
    public DoubleBuffer(Perceptron P, BufferedImage a, BufferedImage b, 
            BufferedImage s, BufferedImage d) {        
        if (a != null) out = new ImageRenderContext(a, interpolate, reflect);
        if (b != null) buf = new ImageRenderContext(b, interpolate, reflect);
        if (d != null) dsp = new ImageRenderContext(d, interpolate, reflect);
        this.P = P;
    }

    /**
     *
     * @param b
     */
    public synchronized void setFancy(boolean b) {
        fancy = b;
        if (out != null) out.setFancy(fancy);
        if (buf != null) buf.setFancy(fancy);
        if (img != null) img.setFancy(fancy);
        if (dsp != null) dsp.setFancy(fancy);
    }

    /**
     *
     */
    public synchronized void toggleFancy() {
        fancy = !fancy;
        if (out != null) out.setFancy(fancy);
        if (buf != null) buf.setFancy(fancy);
        if (img != null) img.setFancy(fancy);
        if (dsp != null) dsp.setFancy(fancy);
    }

    /**
     *
     */
    public synchronized void flip() {
        ImageRenderContext temp = out;
        out = buf;
        buf = temp;
    }

    /**
     * 
     * @param s
     * @param W
     * @param rr
     * @param P
     */
    public synchronized void loadImage(BufferedImage s) {
        if (s == null) return;
        final float scale = (s.getWidth()-0.5f) / (float)(out.W);
        fde = img;
        img = new ImageRenderContext(s, interpolate, reflect, scale);
    }

    public void toggleInterpolation() {
        setInterpolatedAndReflected(!interpolate,reflect);
    }
    
    public void toggleReflection() {
        setInterpolatedAndReflected(interpolate,!reflect);
    }
    
    void setInterpolatedAndReflected(boolean interp, boolean reflect) {
        this.reflect = reflect;
        this.interpolate = interp;
        if (out != null) out.setInterpolatedAndReflected(interp,reflect);
        if (buf != null) buf.setInterpolatedAndReflected(interp,reflect);
        if (img != null) img.setInterpolatedAndReflected(interp,reflect);
        if (dsp != null) dsp.setInterpolatedAndReflected(interp,reflect);
    }

}
