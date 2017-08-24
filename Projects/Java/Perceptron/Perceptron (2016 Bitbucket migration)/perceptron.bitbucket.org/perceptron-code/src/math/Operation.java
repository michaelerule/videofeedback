package math;

/**
 * Perceptron
 *
 * @author Michael Everett Rule
 * @URL <http://perceptron.sourceforge.net/>
 */
public interface Operation {

    public complex execute(complex num);

    boolean is_analytic();
}
