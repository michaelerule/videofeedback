package math;

/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 *         <p/>
 *         <p/>Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *         It evolves colored geometric patterns and visual images into the realm of infinite details
 *         and deepens the thought.</p>
 *         <p/>
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 *
 */
interface TwoNumberOperation {

    public complex execute(complex a, complex b);

    boolean is_analytic();
}

public class OperationEvaluation implements EvaluationOperation {

    private final EvaluationOperation left, right;
    private final int myOperation;

    public OperationEvaluation(MathToken opr, EvaluationOperation left, EvaluationOperation right) {
        this.left = left;
        this.right = right;
        myOperation = opr.getToken() - 1;
    }

    @Override
    public boolean equals(int id) {
        return id == 3;
    }

    @Override
    public complex operate(ComplexVarList variables) {
        return TwoNumberOperations[myOperation].execute(left.operate(variables), right.operate(variables));
    }

    @Override
    public String toString() {
        return MathToken.masterTokens[myOperation][0];
    }

    @Override
    public boolean is_analytic() {
        return TwoNumberOperations[myOperation].is_analytic();
    }

    /*
     * This constant array holds 6 implementations of the TwoNumberOperation
     * class, and allows the operation of a MathToken to be accessed with
     * constant time based on its myToken id number.
     */
    private static final TwoNumberOperation[] TwoNumberOperations = {new TwoNumberOperation() {
        @Override
        public complex execute(complex a, complex b) {
            return a.plus(b);
        }

        @Override
        public boolean is_analytic() {
            return true;
        }
    }, new TwoNumberOperation() {
        @Override
        public complex execute(complex a, complex b) {
            return a.minus(b);
        }

        @Override
        public boolean is_analytic() {
            return true;
        }
    }, new TwoNumberOperation() {
        @Override
        public complex execute(complex a, complex b) {
            return a.times(b);
        }

        @Override
        public boolean is_analytic() {
            return true;
        }
    }, new TwoNumberOperation() {
        @Override
        public complex execute(complex a, complex b) {
            return a.over(b);
        }

        @Override
        public boolean is_analytic() {
            return true;
        }
    }, new TwoNumberOperation() {
        @Override
        public complex execute(complex a, complex b) {
            return a.toThe(b);
        }

        @Override
        public boolean is_analytic() {
            return true;
        }
    }, // ERRORERRORERROR
            new TwoNumberOperation() {
                @Override
                public complex execute(complex a, complex b) {
                    complex temp = a.over(b);
                    return temp.minus(new complex((int) temp.real, (int) temp.imag));
                }

                @Override
                public boolean is_analytic() {
                    return false;
                }
            }};
}
