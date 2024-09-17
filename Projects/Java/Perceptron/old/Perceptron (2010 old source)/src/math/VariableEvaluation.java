package math;

//
import math.ComplexVarList;
//  VariableEvaluation.java
//  ResFracti
//
//  Created by Michael Rule on Mon Apr 04 2005.
//  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
//

public class VariableEvaluation implements EvaluationOperation {

    private final int myVariableIndex;

    public VariableEvaluation(char variableChar) {
        myVariableIndex = (int) variableChar - 97;
    }

    public boolean equals(int id) {
        return id == 1;
    }

    public complex operate(ComplexVarList variables) {
        return variables.get(myVariableIndex);
    }

    public String toString() {
        return new String("" + (char) (myVariableIndex + 97));
    }

    public boolean is_analytic() {
        return true;
    }
}
