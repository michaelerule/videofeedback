package rendered3D;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 *
 */

import util.ColorUtility;
import util.Matrix;

public class TreeForm {

    // change in length
    public float d_r;
    // change in HSV lookupu table color index
    public byte d_color;
    // branching axial rotation
    protected float alpha;
    // horizontal branching angle
    protected float beta;
    // precomputed transformation matrix
    protected float[][] T;

    // Construct
    public TreeForm(float new_alpha, float new_beta, float new_d_r, int new_d_color) {
        d_r = new_d_r;
        d_color = (byte) new_d_color;

        update_matrix();
    }

    /**
     * Update the rotation matrix
     */
    private void update_matrix() {
        T = Matrix.scale(Matrix.multiply(Matrix.rotation(3, 0, 2, alpha), Matrix.rotation(3, 0, 1, beta)), d_r);
    }

    /**
     * Set the axial rotation angle
     */
    public void set_alpha(float a) {
        alpha = a;
        update_matrix();
    }

    /**
     * Set the branching angle
     */
    public void set_beta(float b) {
        beta = b;
        update_matrix();
    }

    /**
     * get the axial rotation angle
     */
    public float get_alpha() {
        return alpha;
    }

    /**
     * get the branching angle
     */
    public float get_beta() {
        return beta;
    }

    /**
     * Safely increment of decrement the color lookup table index, wrapping
     * around from beginning to end if the index goes out of bounds
     */
    public int mutate_color(byte color_index) {
        return ((color_index + d_color + ColorUtility.size) % ColorUtility.size);
    }
}
