package rendered3D;
import image.DoubleBuffer;
import util.Matrix;
import util.ColorUtility;
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
public class Tree3D
{
    // all units below in pixels
    private static final int RADIUS_CUTOFF = 0;//7;
    // stems shorter than this terminate early in leaves
    private static final int LEAF_CUTOFF = 4;
    private static final float leafsize = 15f; 
    
    //MEMBER DATA
    
    /** weather or not the three should actively render and respond to
     *  user input
     */
    private boolean active;
    
    //RENDERING DATA
    
    /** recursion depth of this tree */
    private final int min_depth, max_depth;
    
    /** Array for storing and buffer for computing the branches of this tree */
    private float[][] branch, branch_buffer;
    
    /** Array and buffer for the Color indecies */
    private byte[] color, color_buffer;
    
    /** Array and buffer for branch radii */
    private float[] radius, radius_buffer;
    
    
    //GENERATING INFORMATION
    
    /** {float[] start, float[] end} points of the root */
    public float [][] root;
    
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
    public boolean fancy_graphics = false;
    
    ////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR
    
    /** Creates a new instance of FastTree
     * @param min_tree_depth
     * @param max_tree_depth
     * @param b
     * @param tree_root
     * @param tree_form
     * @param tree_color
     * @param tree_location
     */
    public Tree3D(
            int min_tree_depth,
            int max_tree_depth,
            float[][] tree_root,
            int tree_color,
            TreeForm[] tree_form,
            java.awt.Point tree_location,
            DoubleBuffer b)
    {
        buffer = b;
        
        width = buffer.output.image.getWidth();
        raster_length = buffer.output.buffer.getSize();
        
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
        
        //initialise arrays to store the point location of branches as float
        //three vectors
        branch = new float[(2 << max_depth)][3];
        branch_buffer = new float[branch.length][3];
        
        //initialise color arrays
        color = new byte[branch.length];
        color_buffer = new byte[branch.length];
        
        //initialise radius arrays
        radius = new float[branch.length];
        radius_buffer = new float[branch.length];
        
        SPLIT = 1 << max_depth;
        
        fancy_graphics = true ;
        
        active = true;
    }
    
    //MEMBER FUNCTIONS
    
    /** This sets weather the tree is active or not. An inactive tree
     *  does not draw to the screen or respond to mouse events
     * @param a
     */
    public void set_active(boolean a)
    {
        active = a;
    }
    
    /** Returns weather the tree is active
     * @return
     */
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
        
        Graphics output_Graphics = buffer.output.graphics ;
        
        theta = (float)((theta + .01) % complex.two_pi);
        
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
        float [] L = branch[1];
        
        //Rotations
        float [][] T1 = Matrix.change_basis_3x3(form[0].T, LT);
        float [][] T2 = Matrix.change_basis_3x3(form[1].T, LT);
        
        //Temporary point arrays
        float[] p, q;
        
        // Start at the deepest level and work inwards
        int parent_index = SPLIT - 1;
        int depth = max_depth;
        int level = 1;

        float  x, y;
        int child_index, writing_index;
        
        raster_x = location.x + raster_length;
        
        //iterate over branches
        // Iterate over... all parents? what?
        // Seems to draw from leaves to root?
        // ... yes, this is because leaves will be overwritten by the parent
        // .. er rather, smaller stems get overwritteb by bigger ones
        // 
        for (int n = 1; n < SPLIT; n++)
        {
            //read old data and draw to screen
            
            if (parent_index < (1 << depth)) depth -- ;
            child_index = parent_index << 1 ;
            
            // only draw lines that are big enough?
            // otherwise draw leaves
            // but... leave early if we're at the end?
            if (depth <= max_depth-2
                    && radius[parent_index] > LEAF_CUTOFF)
            {
                // get... root point?
                x  = location.x + branch[parent_index][0];
                y  = location.y + branch[parent_index][1];
                
                // draw first branch
                // only draw lines that are long enough?
                if (radius[child_index] > RADIUS_CUTOFF)
                {   
                    output_Graphics.drawLine(
                            (int)(x),(int)(y),
                            (int)(location.x + branch[child_index][0]),
                            (int)(location.y + branch[child_index][1]));
                    
                }
                // draw other branch (if long enough)
                if (radius[child_index+1] > RADIUS_CUTOFF)
                {
                    output_Graphics.drawLine(
                            (int)(x),(int)(y),
                            (int)(location.x + branch[child_index+1][0]),
                            (int)(location.y + branch[child_index+1][1]));
                }
            }
            else
            {
                // I guess we're at a terminal node
                // if not at the root
                // and not (somehow? how?) pas the end of the buffer
                // and if we drew the parent
                if (parent_index >= 0 
                    && child_index < radius.length 
                    && radius[parent_index] > RADIUS_CUTOFF)
                {
                    // Draw leaves!
                    double rr ;
                    // Get point of origin for this stem
                    float ax  = branch[parent_index][0];
                    float ay  = branch[parent_index][1];
                    // Get (relative) point for the end of the stem
                    float bx  = branch[child_index][0] - ax;
                    float by  = branch[child_index][1] - ay;
                    // Get (rescaled) inverse radius of the stem
                    rr = leafsize/Math.sqrt(bx*bx+by*by);
                    // Rescale (relative) stem endpoint by inverse radius
                    // to give a vector of length leafsize
                    bx *= rr;
                    by *= rr;

                    // Get "sister" stem and rescale accordingly
                    // (thest two stems will be used to define a basis for
                    //  the leaf polygon)
                    float cx = branch[child_index+1][0] - ax;
                    float cy = branch[child_index+1][1] - ay;
                    rr = leafsize/Math.sqrt(cx*cx+cy*cy);
                    cx *= rr;
                    cy *= rr;

                    float dx = bx + cx ;
                    float dy = by + cy ;
                    bx += ax;
                    by += ay;
                    cx += ax;
                    cy += ay;
                    dx += ax;
                    dy += ay;

                    x = location.x ;
                    y = location.y ;

                    // Prepare the leaf points
                    int [] X = new int[]{
                        (int)(x+ax),
                        (int)(x+(3*ax+bx)*.25f),
                        (int)(x+(3*ax+4*bx+dx)*.125f),
                        (int)(x+(ax+4*bx+3*dx)*.125f),
                        (int)(x+(3*dx+bx)*.25),
                        (int)(x+dx),
                        (int)(x+(3*dx+cx)*.25f),
                        (int)(x+(ax+4*cx+3*dx)*.125f),
                        (int)(x+(3*ax+4*cx+dx)*.125f),
                        (int)(x+(3*ax+cx)*.25f)};

                    int [] Y = new int[]{
                        (int)(y+ay),
                        (int)(y+(3*ay+by)*.25),
                        (int)(y+(3*ay+4*by+dy)*.125),
                        (int)(y+(ay+4*by+3*dy)*.125),
                        (int)(y+(3*dy+by)*.25),
                        (int)(y+dy),
                        (int)(y+(3*dy+cy)*.25),
                        (int)(y+(ay+4*cy+3*dy)*.125),
                        (int)(y+(3*ay+4*cy+dy)*.125),
                        (int)(y+(3*ay+cy)*.25)};

                    // draw the leaf
                    output_Graphics.setColor(new Color(70,20,170,120));
                    output_Graphics.fillPolygon(X,Y,10);
                    output_Graphics.setColor(new Color(0,0,0));
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
                radius_buffer[writing_index] = (float)(radius[n] * form[0].d_r + .5);
                
                p = branch_buffer[writing_index];
                multiply_3x3_point(T1, q, p);
                p[0] += L[0];
                p[1] += L[1];
                p[2] += L[2];
                
                writing_index += level;
                
                color_buffer [writing_index] = (byte)((color[n] + form[1].d_color + 128) % 128);
                radius_buffer[writing_index] = (float)(radius[n] * form[1].d_r + .5);
                
                p = branch_buffer[writing_index];
                multiply_3x3_point(T2, q, p);
                p[0] += L[0];
                p[1] += L[1];
                p[2] += L[2];
                
            }
            else radius_buffer[level + n] = radius_buffer[(level << 1) + n] = 0;
            
        }
        
        float[][] temp_branch = branch;
        branch = branch_buffer;
        branch_buffer = temp_branch;
        
        float[] temp_radius = radius;
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
            initial_color = ColorUtility.Hue(col);
            return col;
        }
        else
        {
            return ColorUtility.HSVtoRGB_lookup[initial_color = (color[1] + 2)%ColorUtility.max][ColorUtility.max];
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //CONVIENIENT STATIC METHODS
    
    /** Calculate the float distance between two float points */
    private static float distance(float [] a, float [] b)
    {
        float x = a[0] - b[0],
              y = a[1] - b[1],
              z = a[2] - b[2];
        return (float)(StrictMath.sqrt(x * x + y * y + z * z));
    }
    
    
    /** Matrix multiplication simplified for linear transformation of low
     *  percision 3D points of small value
     * @param A
     * @param P
     * @return
     */
    public static float[] multiply_3x3_point(float [][] A, float [] P)
    {
        return multiply_3x3_point(A, P, new float[3]);
    }
    
    
    /** Matrix multiplication simplified for linear transformation of low
     *  percision 3D points of small value
     * @param A
     * @param P
     * @param result
     * @return
     */
    public static float[] multiply_3x3_point(float [][] A, float [] P, float [] result)
    {   
        result[0] = (float)(A[0][0] * P[0] + A[0][1] * P[1] + A[0][2] * P[2]);
        result[1] = (float)(A[1][0] * P[0] + A[1][1] * P[1] + A[1][2] * P[2]);
        result[2] = (float)(A[2][0] * P[0] + A[2][1] * P[1] + A[2][2] * P[2]);
        
        return result;
    }
    /**
     *
     * @param P
     * @param dr
     * @return
     */
    public static float[] scale_point( float [] P, double dr )
    {
        P[0] *= dr ; P[1] *= dr ; P[2] *= dr ;
        return P;
    }
}
