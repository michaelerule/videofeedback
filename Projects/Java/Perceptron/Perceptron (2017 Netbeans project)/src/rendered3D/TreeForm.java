package rendered3D;
import perceptron.*;
/* TreeForm.java
 * Created on March 7, 2007, 12:16 PM
 */
import util.ColorUtility;
import util.Matrix;

/**
 * @author Michael Everett Rule
 */
public class TreeForm {
    
    //change in length 
    /**
     *
     */
    public float d_r;

    //change in HSV lookupu table color index 
    /**
     *
     */
    public byte d_color;

    //branching axial rotation
    /**
     *
     */
    protected float alpha;

    //horizontal branching angle
    /**
     *
     */
    protected float beta;

    //precomputed transformation matrix
    /**
     *
     */
    protected float [][] T;
    
    
    //Construct
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
        T = Matrix.scale(Matrix.multiply(Matrix.rotation(3,0,2,alpha),Matrix.rotation(3,0,1,beta)), d_r);
    }
    
    /** Set the axial rotation angle
     * @param a
     */
    public void set_alpha(float a) {
        alpha = a;
        update_matrix();
    }
    
    /** Set the branching angle
     * @param b
     */
    public void set_beta(float b) {
        beta = b;
        update_matrix();
    }
    
    /** get the axial rotation angle
     * @return
     */
    public float get_alpha() {return alpha;}
    
    /** get the branching angle
     * @return
     */
    public float get_beta() {return beta;}
    
    /** Safely increment of decrement the color lookup table index, wrapping
     *  around from beginning to end if the index goes out of bounds
     * @param color_index
     * @return
     */
    public int mutate_color(byte color_index) {
        return ((color_index + d_color + ColorUtility.size) % ColorUtility.size);
    }
}
