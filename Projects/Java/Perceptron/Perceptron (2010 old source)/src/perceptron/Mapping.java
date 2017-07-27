package perceptron;

import math.complex;

/** A generic analytic mapping */
public interface Mapping {

    /** Applies and returns some mapping of complex z */
    public complex operate(complex z);
}
