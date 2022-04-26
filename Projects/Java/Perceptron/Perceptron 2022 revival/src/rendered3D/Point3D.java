package rendered3D;

/**
 *
 * @author mer49
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
    public String toString();
		
}
