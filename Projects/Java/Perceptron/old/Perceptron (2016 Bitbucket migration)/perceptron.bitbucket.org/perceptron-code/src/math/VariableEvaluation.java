package math;

/**
 * Perceptron
 *
 * @author Michael Everett Rule
 * @URL <http://perceptron.sourceforge.net/>
 */
public class VariableEvaluation implements EvaluationOperation {

    private final int myVariableIndex;

    public VariableEvaluation(char variableChar) {
        myVariableIndex = variableChar - 97;
    }

    @Override
    public boolean equals(int id) {
        return id == 1;
    }

    @Override
    public complex operate(ComplexVarList variables) {
        return variables.get(myVariableIndex);
    }

    @Override
    public String toString() {
        return "" + (char) (myVariableIndex + 97);
    }

    @Override
    public boolean is_analytic() {
        return true;
    }
}
