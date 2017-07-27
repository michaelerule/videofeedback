package math;

/**
 *  VariableEvaluation.java
 *  ResFracti
 *
 *  Created by Michael Rule on Mon Apr 04 2005.
 *  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
 */

public class VariableEvaluation implements Evaluation {
    private final int myVariableIndex;
    /** Creates a variable
     * @param variableChar
     */
    public VariableEvaluation(char variableChar) {
            myVariableIndex = (int)variableChar-97;
    }
    /**
     * @param id
     * @return
     */
    public boolean equals(int id) {
            return id == 1;
    }
    /** De-references variable
     * @param variables
     * @return
     */
    public complex operate(ComplexVarList variables) {
            return variables.get(myVariableIndex);
    }
    /** tostring override
     * @return
     */
    @Override
    public String toString() {
            return new String(""+(char)(myVariableIndex+97));
	}
    /** Variable references are analytic
     * @return
     */
    public boolean is_analytic()
    {
        return true ;
    }
}
