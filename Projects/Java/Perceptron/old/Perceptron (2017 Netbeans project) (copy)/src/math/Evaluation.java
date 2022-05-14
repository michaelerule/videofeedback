package math;

/** Abstract operator for complex equation model
<p>
This interface describes a basic function performed on a node of the Equation class.
The Equation class uses 4 implementations of this interface, one that can retrieve that value of a
variable, another that returns a stored constant, another that performs a function on one number,
and a fourth that performs an operation on two numbers 
<p>OperationType.java
<p>ResFracti
<p>Created by Michael Rule on Sat Apr 02 2005.
*/

public interface Evaluation
{
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    public final int
            CONSTANT  = 0,
            VARIABLE  = 1,
            FUNCTION  = 2,
            OPERATION = 3;
    
    /**
     *
     * @param id
     * @return
     */
    public boolean equals(int id);
    
    /**
     *
     * @param variables
     * @return
     */
    public complex operate(ComplexVarList variables);
    
    /**
     *
     * @return
     */
    boolean is_analytic();

}
