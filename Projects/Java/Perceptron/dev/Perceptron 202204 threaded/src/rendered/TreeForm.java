package rendered;
/* TreeForm.java
 * Created on March 7, 2007, 12:16 PM
 */
import static java.lang.Math.max;
import color.ColorUtil;
import util.Matrix;
import static util.Misc.clip;

/**
 * Describes the geometric transformation associated with a single branch in
 * the tree. Each tree should have two of these, one for left and one for right
 * branches.
 * @author Michael Everett Rule
 */
public class TreeForm {
    
    /**
     * change in length.
     */
    public float d_r;

    /**
     * change in HSV lookup table color index .
     */
    public byte d_color;

    /**
     * branching axial rotation.
     */
    public float alpha;

    /**
     * horizontal branching angle.
     */
    public float beta;

    /**
     * precomputed transformation matrix.
     */
    public float [][] T;
    
    /**
     *
     * @param new_alpha
     * @param new_beta
     * @param new_d_r
     * @param new_d_color
     */
    public TreeForm(float new_alpha, float new_beta, float new_d_r, int new_d_color) 
    {
        d_r = new_d_r;
        d_color = (byte)new_d_color;
        update_matrix();
    }
    
    /** Update the rotation matrix */
    private void update_matrix() {
        T = Matrix.scale(
                Matrix.multiply(
                        Matrix.rotation(3,0,2,alpha),// about Y axis (the tricky one?)
                        Matrix.rotation(3,0,1,beta)),// about Z axis
                d_r);
    }
    
    /** Set the axial rotation angle
     * @param a */
    public void setAlpha(float a) {
        alpha = a;
        update_matrix();
    }
    
    /** Set the branching angle
     * @param b */
    public void setBeta(float b) {
        b = b % (float)(2*Math.PI);
        beta = clip(b,0,(float)(Math.PI/2));
        update_matrix();
    }
    
    /** get the axial rotation angle
     * @return  */
    public float getAlpha() {return alpha;}
    
    /** get the branching angle
     * @return  */
    public float getBeta() {return beta;}
    
    /** Safely increment of decrement the color lookup table index, wrapping
     *  around from beginning to end if the index goes out of bounds
     * @param color_index
     * @return s*/
    public int setColorIndex(byte color_index) {
        return ((color_index + d_color + ColorUtil.LUTSIZE) % ColorUtil.LUTSIZE);
    }
}
