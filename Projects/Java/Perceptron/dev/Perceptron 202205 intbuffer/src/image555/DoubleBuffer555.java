package image555;
//  DoubleBuffer.java
//  Created by Michael Rule on 6/1/07.

import java.awt.image.BufferedImage;
import static util.Misc.wrap;

public class DoubleBuffer555 {

    /** Image storage buffers. */
    public Image555 
        out, // Feedback output; Same as display if no post-processing.
        buf, // Temporary buffer for rendering
        img, // Buffer to store the current input image
        fde, // Used to fade between two inpiut images
        dsp; // On-screen display; Contains post-processing effects.
    
    public int     reflect     = 0;
    public boolean interpolate = true;
    public boolean antialiased = true;
    
    public DoubleBuffer555(int W, int H) {
        out = new Image555(W, H, interpolate, reflect);
        buf = new Image555(W, H, interpolate, reflect);
        img = new Image555(W, H, interpolate, reflect);
        dsp = new Image555(W, H, interpolate, reflect);
    }

    public synchronized void setFancy(boolean b) {
        antialiased = b;
        if (out != null) out.setFancy(antialiased);
        if (buf != null) buf.setFancy(antialiased);
        if (img != null) img.setFancy(antialiased);
        if (dsp != null) dsp.setFancy(antialiased);
    }

    public synchronized void toggleAntialias() {
        antialiased = !antialiased;
        if (out != null) out.setFancy(antialiased);
        if (buf != null) buf.setFancy(antialiased);
        if (img != null) img.setFancy(antialiased);
        if (dsp != null) dsp.setFancy(antialiased);
    }

    public synchronized void flip() {
        Image555 temp = out;
        out = buf;
        buf = temp;
    }

    public synchronized void set(BufferedImage s) {
        if (s == null) return;
        float scale = (s.getWidth()-.5f) / (float)(out.w);
        fde = img;
        img = new Image555(s, interpolate, reflect, true, scale);
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

    public void thawAll() {
        if (out != null) out.thaw();
        if (buf != null) buf.thaw();
        if (img != null) img.thaw();
        if (dsp != null) dsp.thaw();
    }

    public void freezeAll() {
        if (out != null) out.freeze();
        if (buf != null) buf.freeze();
        if (img != null) img.freeze();
        if (dsp != null) dsp.freeze();
    }

}
