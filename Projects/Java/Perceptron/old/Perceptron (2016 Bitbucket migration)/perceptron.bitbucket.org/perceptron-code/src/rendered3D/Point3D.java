package rendered3D;

/**
 * Perceptron
 *
 * @author Michael Everett Rule
 * @URL <http://perceptron.sourceforge.net/>
 */
public interface Point3D {

    public double getx();

    public double gety();

    public double getz();

    public Point3D clone2();

    @Override
    public String toString();
}
