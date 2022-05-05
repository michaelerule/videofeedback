package rendered3D;
import image.DoubleBuffer;
import rendered.TreeForm;
import util.Matrix;
import util.ColorUtil;
import perceptron.*;
import math.complex;
import java.awt.Color;
import java.awt.Graphics;

/* FastTree.java
 * Created on March 7, 2007, 3:43 PM
 */

/**
 * @author Michael Everett Rule
 */
public class Tree3D11
{
    
    private static final int RADIUS_CUTOFF = 2;
    
    //MEMBER DATA
    
    /** weather or not the three should actively render and respond to
     *  user input
     */
    private boolean active;
    
    //RENDERING DATA
    
    /** recursion depth of this tree */
    private final int min_depth, max_depth;
    
    /** Array for storing and buffer for computing the branches of this tree */
    private short[][] branch, branch_buffer;
    
    /** Array and buffer for the Color indecies */
    private byte[] color, color_buffer;
    
    /** Array and buffer for branch radii */
    private short[] radius, radius_buffer;
    
    
    //GENERATING INFORMATION
    
    /** {short[] start, short[] end} points of the root */
    public short [][] root;
    
    /** The initial color of the tree */
    public int initial_color = 0;
    
    /** Pattern for generating the tree */
    public TreeForm[] form;
    
    /** current depth (bound by maximum and minimum depths */
    private int current_depth;
    
    /** Integer representing the first index of the branch array that
     *  has no children (the start of the last layer)
     */
    private final int SPLIT;
    
    /** The x,y location of the base of the trunk of the tree */
    public java.awt.Point location;
    
    /** The Post-rendering transformations applied during drawing,
     *  transformations applied like : [Y][X][Z][S],
     *  Y should be a Y axis rotation
     *  X ""
     *  Z ""
     *  S should be a scaling transformation
     */
    public float [][] X, Y;
    float theta;
    
    /** The BufferedImage, Graphics, and DataBuffer to render to */
    DoubleBuffer buffer;
    
    /** information about the rendering raster */
    int width;
    int raster_length;
    int raster_x;
    
    /** fancy ( slow ) draw or fast ( ugly ) draw ? */
    public boolean fancy_graphics ;
    
    ////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR
    
    /** Creates a new instance of FastTree
     * @param min_tree_depth
     * @param max_tree_depth
     * @param tree_form
     * @param tree_color
     * @param tree_root
     * @param b
     * @param tree_location */
    public Tree3D11(
            int min_tree_depth,
            int max_tree_depth,
            short[][] tree_root,
            int tree_color,
            TreeForm[] tree_form,
            java.awt.Point tree_location,
            DoubleBuffer b)
    {
        buffer = b;
        
        width = buffer.out.img.getWidth();
        raster_length = buffer.out.buf.getSize();
        
        if (max_tree_depth > 29) max_tree_depth = 29;
        else if (max_tree_depth < 1) max_tree_depth = 1;
        
        if (min_tree_depth > 29) min_tree_depth = 29;
        else if (min_tree_depth < 1) min_tree_depth = 1;
        
        if (min_tree_depth > max_tree_depth)
        {
            max_depth = (byte)min_tree_depth;
            min_depth = (byte)max_tree_depth;
        }
        else
        {
            min_depth = (byte)min_tree_depth;
            max_depth = (byte)max_tree_depth;
        }
        
        current_depth = max_tree_depth;
        
        //Store the root, initial color, location and form.
        root          = tree_root     ;
        form          = tree_form     ;
        initial_color = tree_color    ;
        location      = tree_location ;
        
        //Generate default transform.
        X = Y = new float[][]{{1,0,0},{0,1,0},{0,0,1}};
        
        //initialise arrays to store the point location of branches as short
        //three vectors
        branch = new short[(2 << max_depth)][3];
        branch_buffer = new short[branch.length][3];
        
        //initialise color arrays
        color = new byte[branch.length];
        color_buffer = new byte[branch.length];
        
        //initialise radius arrays
        radius = new short[branch.length];
        radius_buffer = new short[branch.length];
        
        SPLIT = 1 << max_depth;
        
        fancy_graphics = true ;
        
        active = true;
    }
    
    //MEMBER FUNCTIONS
    
    /** *  This sets weather the tree is active or not.An inactive tree
  does not draw to the screen or respond to mouse events
     * @param a */
    public void set_active(boolean a)
    {
        active = a;
    }
    
    /** Returns weather the tree is active
     * @return  */
    public boolean is_active()
    {
        return active;
    }
    
    /**
     *
     * @return
     */
    public synchronized boolean toggle_fancy_graphics()
    {
        return fancy_graphics = ! fancy_graphics ;
    }

    /**
     *
     * @param anti_alias_tree
     */
    public synchronized void set_fancy_graphics( boolean anti_alias_tree )
    {
        fancy_graphics = anti_alias_tree ;
    }
    
    //RENDER FUNCTIONS
    
    /** re-render the tree with a new tree form by applying the tree form
     *  transformations to this data set, rather than recursing through the
     *  tree and recalculating all the branching angles */
    public synchronized void render()
    {
        if (!active) return;
        
        Graphics output_Graphics = buffer.out.g ;
        
        theta = (float)((theta + .01) % complex.TWOPI);
        
        //Combine rotations into a single transform
        float [][] LT = Matrix.multiply(Matrix.multiply(Y,X),Matrix.rotation(3,0,2,theta));
        
        //transform root into the first branches
        multiply_3x3_point(LT, root[0], branch[0]);
        multiply_3x3_point(LT, root[1], branch[1]);
        scale_point( branch[0] , 1/form[0].d_r );
        scale_point( branch[1] , 1/form[1].d_r );
        
        //draw root
        output_Graphics.setColor(Color.BLACK);
        //output_Graphics.setColor(ColorUtility.HSVtoRGB_color[initial_color][ColorUtility.max]);
        output_Graphics.drawLine(
                (int)(location.x + branch[0][0]),
                (int)(location.y + branch[0][1]),
                (int)(location.x + branch[1][0]),
                (int)(location.y + branch[1][1]));
        output_Graphics.drawLine(
                (int)(location.x - branch[0][0]),
                (int)(location.y - branch[0][1]),
                (int)(location.x - branch[1][0]),
                (int)(location.y - branch[1][1]));
        
        //store the root color
        color[1] = color_buffer[1] = (byte)initial_color;
        
        //store the root length
        radius[1] = radius_buffer[1] = distance(root[0], root[1]);
        
        //Translation
        short [] L = branch[1];
        
        //Rotations
        float [][] T1 = Matrix.change_basis_3x3(form[0].T, LT);
        float [][] T2 = Matrix.change_basis_3x3(form[1].T, LT);
        
        //Temporary point arrays
        short[] p, q;
        
        int parent_index = SPLIT - 1;
        int depth = max_depth;
        int level = 1;
        
        int child_index, writing_index, x, y, x2, y2, paint_color, value;
        int value_scalar = Color.MAXLUTINDEX / max_depth;
        int alpha_scalar = 255 / max_depth;
        
        raster_x = location.x + raster_length;
        
        //iterate over branches
        for (int n = 1; n < SPLIT; n++)
        {
            //read old data and draw to screen
            
            if (parent_index < (1 << depth)) depth -- ;
            child_index = parent_index << 1 ;
            value       = Color.MAXLUTINDEX - depth * value_scalar ;
            int alpha   = (0xff&( 255 - depth * alpha_scalar)) << 24 ;
            
            if (radius[parent_index] > RADIUS_CUTOFF)
            {
                x  = location.x + branch[parent_index][0];
                y  = location.y + branch[parent_index][1];
                x2 = location.x - branch[parent_index][0];
                y2 = location.y - branch[parent_index][1];
                
                //if ( fancy_graphics )
                //    output_Graphics.setColor( new Color(ColorUtility.HSVtoRGB(color[parent_index],value)&0xffffff|alpha , true ));
                //else
                //    output_Graphics.setColor( ColorUtility.HSVtoRGB_color[color[parent_index]][value] );
                
                if (radius[child_index] > RADIUS_CUTOFF)
                {
                    
                    output_Graphics.drawLine(
                            x,y,
                            location.x + branch[child_index][0],
                            location.y + branch[child_index][1]);
                    /*
                    output_Graphics.drawLine(
                            x2,y2,
                            location.x - branch[child_index][0],
                            location.y - branch[child_index][1]);*/
                    
                }
                child_index ++;
                if (radius[child_index] > RADIUS_CUTOFF)
                {

                    output_Graphics.drawLine(
                            x,y,
                            location.x + branch[child_index][0],
                            location.y + branch[child_index][1]);
                    /*
                    output_Graphics.drawLine(
                            x2,y2,
                            location.x - branch[child_index][0],
                            location.y - branch[child_index][1]);*/
                }
            }
            parent_index --;
            
            //compute next frame
            
            q = branch[n];
            
            if (level * 2 <= n) level <<= 1;
            
            if (radius[n] > 0)
            {
                writing_index = level + n;
                
                color_buffer [writing_index] = (byte)((color[n]  + form[0].d_color + 128) % 128);
                radius_buffer[writing_index] = (short)(radius[n] * form[0].d_r + .5);
                
                p = branch_buffer[writing_index];
                multiply_3x3_point(T1, q, p);
                p[0] += L[0];
                p[1] += L[1];
                p[2] += L[2];
                
                writing_index += level;
                
                color_buffer [writing_index] = (byte)((color[n] + form[1].d_color + 128) % 128);
                radius_buffer[writing_index] = (short)(radius[n] * form[1].d_r + .5);
                
                p = branch_buffer[writing_index];
                multiply_3x3_point(T2, q, p);
                p[0] += L[0];
                p[1] += L[1];
                p[2] += L[2];
                
            }
            else radius_buffer[level + n] = radius_buffer[(level << 1) + n] = 0;
            
        }
        
        short[][] temp_branch = branch;
        branch = branch_buffer;
        branch_buffer = temp_branch;
        
        short[] temp_radius = radius;
        radius = radius_buffer;
        radius_buffer = temp_radius;
        
        byte[] temp_color = color;
        color = color_buffer;
        color_buffer = temp_color;
    }
    
    /**
     *
     * @param col
     * @return
     */
    public int set_initial_color(int col)
    {
        if (col > 0)
        {
            initial_color = Color.Hue(col);
            return col;
        }
        else
        {
            return Color.HSVtoRGB_lookup[initial_color = (color[1] + 2)%Color.MAXLUTINDEX][Color.MAXLUTINDEX];
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //CONVIENIENT STATIC METHODS
    
    /** Calculate the short distance between two short points */
    private static short distance(short [] a, short [] b)
    {
        int x = a[0] - b[0],
                y = a[1] - b[1],
                z = a[2] - b[2];
        return (short)(StrictMath.sqrt(x * x + y * y + z * z));
    }
    
    
    /** Matrix multiplication simplified for linear transformation of low
     *  percision 3D points of small value
     * @param A
     * @param P
     * @return  */
    public static short[] multiply_3x3_point(float [][] A, short [] P)
    {
        return multiply_3x3_point(A, P, new short[3]);
    }
    
    
    /** Matrix multiplication simplified for linear transformation of low
     *  percision 3D points of small value
     * @param A
     * @param result
     * @param P
     * @return  */
    public static short[] multiply_3x3_point(float [][] A, short [] P, short [] result)
    {
        
        result[0] = (short)(A[0][0] * P[0] + A[0][1] * P[1] + A[0][2] * P[2]);
        result[1] = (short)(A[1][0] * P[0] + A[1][1] * P[1] + A[1][2] * P[2]);
        result[2] = (short)(A[2][0] * P[0] + A[2][1] * P[1] + A[2][2] * P[2]);
        
        return result;
    }

    /**
     *
     * @param P
     * @param dr
     * @return
     */
    public static short[] scale_point( short [] P, double dr )
    {
        P[0] *= dr ; P[1] *= dr ; P[2] *= dr ;
        return P;
    }
}
