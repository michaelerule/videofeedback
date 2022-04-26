/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package perceptron;

import java.awt.RenderingHints;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Contains code for blur/sharpen
 * @author mer49
 */
public class Unsharp {
    

    /**
     * 
     * @param x
     * @param sigma
     * @return 
     */
    static double gaussian(double x, double sigma) {
        return exp(-.5 * pow(x / sigma, 2)) / (sigma * sqrt(2 * PI));
    }

    /**
     * 
     * @param std
     * @return 
     */
    static Kernel makeGaussian(float std) {
        int s = (int) (4 * std);
        float sum = 0f;
        float[] d = new float[s * s];
        for (int i = 0; i < s; i++) 
            for (int j = 0; j < s; j++) 
                sum += d[i * s + j] = (float) gaussian(i - s / 2, std) * (float) gaussian(j - s / 2, std);
        sum = 1.0f / sum;
        for (int i = 0; i < s * s; i++) {
            d[i] *= sum;
        }
        return new Kernel(s, s, d);
    }

    /**
     * 
     * @param std
     * @return 
     */
    static Kernel makeUnsharp(float std) {
        int s = (int) (4 * std);
        float sum = 0f;
        float[] d = new float[s * s];
        for (int i = 0; i < s; i++) 
            for (int j = 0; j < s; j++) 
                sum += d[i * s + j] = -(float) gaussian(i - s / 2, std) * (float) gaussian(j - s / 2, std);
        d[s * (s / 2) + (s / 2)] += 1.0f - sum;
        return new Kernel(s, s, d);
    }
    static final ConvolveOp gconv = new ConvolveOp(makeUnsharp(4), ConvolveOp.EDGE_NO_OP, new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));
    
    BlurSharpen gtest;
}
