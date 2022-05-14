package rendered3D;

/**
 *
 * @author mrule
 */
public interface Point3D {
		
    /**
     *
     * @return
     */
    public double getx();
    /**
     *
     * @return
     */
    public double gety();
                /**
                 *
                 * @return
                 */
                public double getz();
                /**
                 *
                 * @return
                 */
                public Point3D clone2();
                /**
                 *
                 * @return
                 */
    @Override
                public String toString();
		
}
