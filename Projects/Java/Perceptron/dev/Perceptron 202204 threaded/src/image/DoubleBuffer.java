package image;
//  DoubleBuffer.java
//  Created by Michael Rule on 6/1/07.

import java.awt.image.*;
import static util.Misc.wrap;

public class DoubleBuffer {

    /** Image storage buffers. */
    public ImageRenderContext 
        out, // Feedback output; Same as display if no post-processing.
        buf, // Temporary buffer for rendering
        img, // Buffer to store the current input image
        fde, // Used to fade between two inpiut images
        dsp; // On-screen display; Contains post-processing effects.
    
    public int     reflect     = 0;
    public boolean interpolate = true;
    public boolean antialiased       = true;
    
    public DoubleBuffer(int W, int H) {
        out = new ImageRenderContext(W, H, interpolate, reflect);
        buf = new ImageRenderContext(W, H, interpolate, reflect);
        img = new ImageRenderContext(W, H, interpolate, reflect);
        dsp = new ImageRenderContext(W, H, interpolate, reflect);
    }

    /**
     *
     * @param b
     */
    public synchronized void setFancy(boolean b) {
        antialiased = b;
        if (out != null) out.setFancy(antialiased);
        if (buf != null) buf.setFancy(antialiased);
        if (img != null) img.setFancy(antialiased);
        if (dsp != null) dsp.setFancy(antialiased);
    }

    /**
     *
     */
    public synchronized void toggleAntialias() {
        antialiased = !antialiased;
        if (out != null) out.setFancy(antialiased);
        if (buf != null) buf.setFancy(antialiased);
        if (img != null) img.setFancy(antialiased);
        if (dsp != null) dsp.setFancy(antialiased);
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
     * @param s
     */
    public synchronized void set(BufferedImage s) {
        if (s == null) return;
        final float scale = (s.getWidth()-0.5f) / (float)(out.w);
        fde = img;
        img = new ImageRenderContext(s, interpolate, reflect, scale);
    }

    public void toggleInterpolation() {
        setInterpolatedAndReflected(!interpolate,reflect);
    }
    
    public void nextReflection(int n) {
        setInterpolatedAndReflected(interpolate,wrap(n+reflect,3));
    }
    
    void setInterpolatedAndReflected(boolean interp, int reflect) {
        this.reflect = reflect;
        this.interpolate = interp;
        if (out != null) out.setInterpolatedAndReflected(interp,reflect);
        if (buf != null) buf.setInterpolatedAndReflected(interp,reflect);
        if (img != null) img.setInterpolatedAndReflected(interp,reflect);
        if (dsp != null) dsp.setInterpolatedAndReflected(interp,reflect);
    }

}
