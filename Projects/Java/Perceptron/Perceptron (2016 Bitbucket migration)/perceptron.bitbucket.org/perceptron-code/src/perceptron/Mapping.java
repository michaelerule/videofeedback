package perceptron;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 *
 */

import math.complex;

/**
 * A generic analytic mapping
 */
public interface Mapping {

    /**
     * Applies and returns some mapping of complex z
     */
    public complex operate(complex z);
}
