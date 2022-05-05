package rendered;

import image.DoubleBuffer;
import util.Matrix;
import util.ColorUtil;
import perceptron.*;
import math.complex;
import java.awt.Color;
import java.awt.Graphics;
import static util.Matrix.change_basis_3x3;
import static util.Matrix.distance;
import static util.Matrix.multiply;
import static util.Matrix.multiply_3x3_point;
import static util.Matrix.rotation;
import static util.Matrix.scale_point;
import static util.Misc.clip;

/* FastTree.java
 * Created on March 7, 2007, 3:43 PM
 */
/**
 * @author Michael Everett Rule
 */
public class Tree3D {

    private static final int RADIUS_CUTOFF = 5;
    
    //RENDERING DATA
    /** recursion depth of this tree */
    private final int max_depth;
    
    /** Array for storing and buffer for computing the branches of this tree */
    private float[][] branch, branch_buffer;
    
    /** Array and buffer for the Color indecies */
    private byte[] color, color_buffer;
    
    /** Array and buffer for branch radii */
    private float[] radius, radius_buffer;
    
    //GENERATING INFORMATION
    /** {float[] start, float[] end} points of the root */
    public float[][] root;
    
    /** The initial color of the tree */
    public int initial_color = 0;
    
    /** Pattern for generating the tree */
    public TreeForm[] form;
    
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
    public float[][] X, Y;
    float theta;
    
    /** The BufferedImage, Graphics, and DataBuffer to render to */
    DoubleBuffer buffer;
    
    /** information about the rendering raster */
    int width;
    int rasterLength;
    int rasterX;
    
    /** */
    public Color leafColor = new Color(127,32,255,120);
    
    // Rendering option flags
    public boolean draw_leaves    = true;
    public boolean draw_symmetric = true;
    public void    toggleLeaves()         {draw_leaves   =!draw_leaves   ;}
    public void    toggleSymmetry()       {draw_symmetric=!draw_symmetric;}
    public void    setLeaves  (boolean b) {draw_leaves   =b;}
    public void    setSymmetry(boolean b) {draw_symmetric=b;}
    public boolean getLeaves  ()          {return draw_leaves   ;}
    public boolean getSymmetry()          {return draw_symmetric;}
    
    ////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR
    /** Creates a new instance of FastTree
     * @param min_tree_depth
     * @param max_tree_depth
     * @param tree_root
     * @param tree_form
     * @param tree_color
     * @param tree_location
     * @param b */
    public Tree3D(
            int min_tree_depth,
            int max_tree_depth,
            float[][] tree_root,
            int tree_color,
            TreeForm[] tree_form,
            java.awt.Point tree_location,
            DoubleBuffer b) {
        
        buffer = b;
        width  = buffer.out.img.getWidth();
        rasterLength = buffer.out.buf.getSize();

        max_tree_depth = clip(max_tree_depth, 1, 13);
        max_depth = (byte) max_tree_depth;

        //Store the root, initial color, location and form.
        root = tree_root;
        form = tree_form;
        initial_color = tree_color;
        location = tree_location;

        //Generate default transform.
        X = Y = new float[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

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
    }

    //RENDER FUNCTIONS
    /** re-render the tree with a new tree form by applying the tree form
     *  transformations to this data set, rather than recursing through the
     *  tree and recalculating all the branching angles */
    public void render() {
        Graphics g = buffer.out.g;
        theta = (float) ((theta + .01) % complex.TWOPI);

        //Combine rotations into a single transform
        float[][] LT = multiply(multiply(Y, X), rotation(3, 0, 2, theta));

        //transform root into the first branches
        multiply_3x3_point(LT, root[0], branch[0]);
        multiply_3x3_point(LT, root[1], branch[1]);
        scale_point(branch[0], 1/form[0].d_r);
        scale_point(branch[1], 1/form[1].d_r);

        //draw root in both directions
        //output_Graphics.setColor(Color.BLACK);
        g.setColor(ColorUtil.HSVtoRGB_color[initial_color][ColorUtil.MAXLUTINDEX]);
        g.drawLine(
                (int) (location.x + branch[0][0]),
                (int) (location.y + branch[0][1]),
                (int) (location.x + branch[1][0]),
                (int) (location.y + branch[1][1]));
        if (draw_symmetric) g.drawLine(
                (int) (location.x - branch[0][0]),
                (int) (location.y - branch[0][1]),
                (int) (location.x - branch[1][0]),
                (int) (location.y - branch[1][1]));

        //store the root color
        color[1] = color_buffer[1] = (byte) initial_color;

        //store the root length
        radius[1] = radius_buffer[1] = distance(root[0], root[1]);

        //Translation
        float[] L = branch[1];

        //Rotations
        float[][] T1 = change_basis_3x3(form[0].T, LT);
        float[][] T2 = change_basis_3x3(form[1].T, LT);

        //Temporary point arrays
        float[] p, q;

        int parent_index = SPLIT - 1;
        int depth = max_depth;
        int level = 1;

        float x, y;
        int child_index, writing_index;

        rasterX = location.x + rasterLength;

        int value_scalar = ColorUtil.MAXLUTINDEX / max_depth;
        
        //iterate over branches
        for (int n = 1; n < SPLIT; n++) {
            //read old data and draw to screen

            if (parent_index < (1 << depth)) depth--;
            child_index = parent_index << 1;
            
            // Small branches are treated as terminal branches with leaves
            if (depth >= max_depth-1 || radius[parent_index] <= RADIUS_CUTOFF) {
                int parentparent_index = parent_index >> 1;
                if (draw_leaves
                        && parentparent_index >= 0
                        && parentparent_index < radius.length 
                        && radius[parentparent_index] > RADIUS_CUTOFF) {
                    double rr ;
                    float ax  = branch[parentparent_index][0];
                    float ay  = branch[parentparent_index][1];
                    float bx  = branch[parent_index][0] - ax;
                    float by  = branch[parent_index][1] - ay;
                    rr = 10.0/Math.sqrt(bx*bx+by*by);
                    bx *= rr;
                    by *= rr;
                    float cx = branch[parent_index+1][0] - ax;
                    float cy = branch[parent_index+1][1] - ay;
                    rr = 10.0/Math.sqrt(cx*cx+cy*cy);
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
                    // Too tired to opitmize; Let's hope jvm elimenates 
                    // common sub-expressions.
                    g.setColor(leafColor);
                    g.fillPolygon(
                        new int[]{
                            (int)(x+ax),
                            (int)(x+(3*ax+bx)*.25),
                            (int)(x+(3*ax+4*bx+dx)*.125),
                            (int)(x+(ax+4*bx+3*dx)*.125),
                            (int)(x+(3*dx+bx)*.25),
                            (int)(x+dx),
                            (int)(x+(3*dx+cx)*.25),
                            (int)(x+(ax+4*cx+3*dx)*.125),
                            (int)(x+(3*ax+4*cx+dx)*.125),
                            (int)(x+(3*ax+cx)*.25)},
                        new int[]{
                            (int)(y+ay),
                            (int)(y+(3*ay+by)*.25),
                            (int)(y+(3*ay+4*by+dy)*.125),
                            (int)(y+(ay+4*by+3*dy)*.125),
                            (int)(y+(3*dy+by)*.25),
                            (int)(y+dy),
                            (int)(y+(3*dy+cy)*.25),
                            (int)(y+(ay+4*cy+3*dy)*.125),
                            (int)(y+(3*ay+4*cy+dy)*.125),
                            (int)(y+(3*ay+cy)*.25)},10);
                    if (draw_symmetric) g.fillPolygon(
                        new int[]{
                            (int)(x-ax),
                            (int)(x-(3*ax+bx)*.25),
                            (int)(x-(3*ax+4*bx+dx)*.125),
                            (int)(x-(ax+4*bx+3*dx)*.125),
                            (int)(x-(3*dx+bx)*.25),
                            (int)(x-dx),
                            (int)(x-(3*dx+cx)*.25),
                            (int)(x-(ax+4*cx+3*dx)*.125),
                            (int)(x-(3*ax+4*cx+dx)*.125),
                            (int)(x-(3*ax+cx)*.25)},
                        new int[]{
                            (int)(y-ay),
                            (int)(y-(3*ay+by)*.25),
                            (int)(y-(3*ay+4*by+dy)*.125),
                            (int)(y-(ay+4*by+3*dy)*.125),
                            (int)(y-(3*dy+by)*.25),
                            (int)(y-dy),
                            (int)(y-(3*dy+cy)*.25),
                            (int)(y-(ay+4*cy+3*dy)*.125),
                            (int)(y-(3*ay+4*cy+dy)*.125),
                            (int)(y-(3*ay+cy)*.25)},10);
                }
            } 
            // Branches as sticks
            else {
                int value = ColorUtil.MAXLUTINDEX - depth * value_scalar ;
                g.setColor(ColorUtil.HSVtoRGB_color[color[parent_index]][value]);
                if (radius[child_index] > RADIUS_CUTOFF) {
                    g.drawLine(
                        (int) (location.x + branch[parent_index][0]), 
                        (int) (location.y + branch[parent_index][1]),
                        (int) (location.x + branch[child_index][0]),
                        (int) (location.y + branch[child_index][1]));
                    if (draw_symmetric) g.drawLine(
                        (int) (location.x - branch[parent_index][0]), 
                        (int) (location.y - branch[parent_index][1]),
                        (int) (location.x - branch[child_index][0]),
                        (int) (location.y - branch[child_index][1]));
                }
                child_index++;
                if (radius[child_index] > RADIUS_CUTOFF) {
                    g.drawLine(
                        (int) (location.x + branch[parent_index][0]), 
                        (int) (location.y + branch[parent_index][1]),
                        (int) (location.x + branch[child_index][0]),
                        (int) (location.y + branch[child_index][1]));
                    if (draw_symmetric) g.drawLine(
                        (int) (location.x - branch[parent_index][0]), 
                        (int) (location.y - branch[parent_index][1]),
                        (int) (location.x - branch[child_index][0]),
                        (int) (location.y - branch[child_index][1]));
                }
            }
            parent_index--;
            //compute next frame
            q = branch[n];
            if (level*2 <= n) level <<= 1;
            if (radius[n] > 0) {
                writing_index = level + n;
                color_buffer [writing_index] = (byte) ((color[n] + form[0].d_color + 128) % 128);
                radius_buffer[writing_index] = (float) (radius[n] * form[0].d_r + .5);
                p = branch_buffer[writing_index];
                multiply_3x3_point(T1, q, p);
                p[0] += L[0];
                p[1] += L[1];
                p[2] += L[2];
                writing_index += level;
                color_buffer[writing_index] = (byte) ((color[n] + form[1].d_color + 128) % 128);
                radius_buffer[writing_index] = (float) (radius[n] * form[1].d_r + .5);
                p = branch_buffer[writing_index];
                multiply_3x3_point(T2, q, p);
                p[0] += L[0];
                p[1] += L[1];
                p[2] += L[2];
            } else {
                radius_buffer[level + n] = radius_buffer[(level << 1) + n] = 0;
            }

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

    public int set_initial_color(int col) {
        if (col > 0) {
            initial_color = ColorUtil.hue(col);
            return col;
        } else return ColorUtil.HSVtoRGB_lookup[
            initial_color = (color[1] + 2) % ColorUtil.MAXLUTINDEX
            ][ColorUtil.MAXLUTINDEX];
    }

}
