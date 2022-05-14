package rendered3D;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 *
 * @author mrule
 */
public interface Shape3D
{
    /**
     *
     * @param i
     * @return
     */
    public int setDepth(int i);
    /**
     *
     * @return
     */
    public int setDepth();
    /**
     *
     * @return
     */
    public int depth();
        /**
         *
         * @param G
         * @param image_buffer
         */
        public void draw(Graphics G, BufferedImage image_buffer);
        /**
         *
         * @param oldpoints
         * @param newpoints
         * @return
         */
        public Shape3D cloneto(Point3D [] oldpoints, ArrayList newpoints);
}
