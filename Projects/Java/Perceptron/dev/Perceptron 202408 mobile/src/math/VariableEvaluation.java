package math;

//
//  ResFracti
//
//  Created by Michael Rule on Mon Apr 04 2005.
//  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
//

/**
 *
 * @author mer49
 */
public class VariableEvaluation implements EvaluationOperation {

    private final int myVariableIndex;

    /**
     *
     * @param variableChar
     */
    public VariableEvaluation(char variableChar) {
        myVariableIndex = (int) variableChar - 97;
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public boolean equals(int id) {
        return id == 1;
    }

    /**
     *
     * @param variables
     * @return
     */
    @Override
    public complex operate(ComplexContex variables) {
        return variables.get(myVariableIndex);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "" + (char) (myVariableIndex + 97);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean is_analytic() {
        return true;
    }
}
