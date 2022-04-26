package math;

/**
 *  Represents a constant value in an equation tree.
 *  <p>
 *  Originally of the ResFracti project
 *  <p>
 *  Created by Michael Rule on Mon Apr 04 2005.
 *  Copyright (c) 2005. All rights reserved.
 */
public class ConstantEvaluation extends complex implements Evaluation
{
    /** Forewards to complex(float) constructor
     * @param x
     */
    public ConstantEvaluation(float x) { super(x); }
    /** Forewards to complex(complex) constructor
     * @param x
     */
    public ConstantEvaluation(complex x) { super(x); }
    /** Node type is 0. Returns true on argument 0 else false.
     * @param id
     * @return
     */
    public boolean equals(int id) { return id == 0; }
    /** Returns the constant value stored here
     * @param variables
     * @return
     */
    public complex operate(ComplexVarList variables) { return this; }
    /** True : constand lookip is an analytic function
     * @return
     */
    public boolean is_analytic() { return true; }
}

