package math;

//
//  resMedii
//
//  Created by Michael Rule on Sun Aug 08 2004.
/*This class uses a binary tree structure to store the relationship between MathTokens in a user
inputted equations. An instance of this class contains a MathToken and a reference to a left
and right sub equation.*/

/**
 *
 * @author mer49
 */
public class Equation
{
    //Stores the MathToken at this node
    private EvaluationOperation myEvaluationOperation;
    
    //Stores the right and left sub equations, if they exist
    private Equation left, right ;
    
    /** initializes an empty equation*/
    public Equation()
    {
        myEvaluationOperation = null;
        left = right = null;
    }
    
    /**This method looks at a math token and decides which operation it is associated with*/
    private void setOperation(MathToken opr)
    {
        if (opr.isOperator())
        {
            if (opr.isFunction())
                myEvaluationOperation = new FunctionEvaluation(opr,left.getOperation());
            else
                myEvaluationOperation = new OperationEvaluation(opr,left.getOperation(),right.getOperation());
        }
        else if (opr.isVariable())
            myEvaluationOperation = new VariableEvaluation(opr.getVar());
        else
            myEvaluationOperation = new ConstantEvaluation(opr.getNumber());
    }
    
    /** cobmines two equations with a perscribed operation
     * @param equ
     * @param opr
     * @param equ2 */
    public Equation(Equation equ, MathToken opr, Equation equ2)
    {
        left  = equ  ;
        right = equ2 ;
        setOperation(opr);
    }
    
    /**combines an equation and a single MathToke
     * @param equ
     * @param opr*/
    public Equation(Equation equ, MathToken opr)
    {
        left = equ ;
        setOperation(opr);
    }
    
    /*Makes an Equation out of a math token*/

    /**
     *
     * @param opr
     */

    public Equation(MathToken opr)
    {
        setOperation(opr);
    }
    
    /**
     *
     * @param equ
     */
    public Equation( Equation equ )
    {
        myEvaluationOperation = equ.getOperation();
        if (equ.getLeft() != null) left = new Equation(equ.getLeft());
        if (equ.getRight() != null) right = new Equation(equ.getRight());
    }
    
    /** getter
     * @return s*/
    public EvaluationOperation getOperation()
    {
        return myEvaluationOperation;
    }
    
    /**
     *
     * @return
     */
    public Equation getLeft()
    {
        return left;
    }
    
    /**
     *
     * @return
     */
    public Equation getRight()
    {
        return right;
    }
    
    /**Sets the MathToken of this Equation
     * @param token
     */
    public void setEvaluationOperation(MathToken token)
    {
        setOperation(token);
    }
    
    /**
     *
     * @return
     */
    public EvaluationOperation getEvaluationOperation()
    {
        return myEvaluationOperation;
    }
    
    //Sets the left subEquation of this Equation

    /**
     *
     * @param equ
     */
    public void setLeft(Equation equ)
    {
        left = new Equation(equ);
    }
    
    //Sets the right subEquation of this Equation

    /**
     *
     * @param equ
     */
    public void setRight(Equation equ)
    {
        right = new Equation(equ);
    }
    
    //Evaluates the equation for the given variables

    /**
     *
     * @param variables
     * @return
     */
    public complex eval(ComplexVarList variables)
    {
        return myEvaluationOperation.operate(variables);
    }
    
    /**
     *
     * @param Variable
     * @param number
     */
    public void substitute(char Variable, complex number)
    {
        if (
                myEvaluationOperation.equals(1) &&
                myEvaluationOperation.toString().equals("o"))
        {
            System.out.println("substituted");
            myEvaluationOperation = new ConstantEvaluation(number);
        }
        else
        {
            if (left  != null) left .substitute(Variable,number);
            if (right != null) right.substitute(Variable,number);
        }
    }
    
    /**
     *
     * @return
     */
    public boolean is_analytic()
    {
        return !(
                ( ! myEvaluationOperation.is_analytic() ) || 
                ( left != null  && ! left .is_analytic() ) || 
                ( right != null && ! right.is_analytic() )) ;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //returns a string of the chars of all variables contained within this Equation structure

    /**
     *
     * @return
     */
    public String getVariables()
    {
        String result = "";
        if (right == null)
        {
            if (left == null)
            {
                if (myEvaluationOperation.equals(EvaluationOperation.VARIABLE)) result += myEvaluationOperation;
            }
            else result += left.getVariables();
        }
        else result += right.getVariables();
        return result;
    }
    
    //A lazy equals method that compares the toStrings of both equations

    /**
     *
     * @param equ
     * @return
     */
    public boolean equals(Equation equ)
    {
        return toString().equals(equ.toString());
    }
    
    //This overrides toString in Object and converts an Equation into a fairly readable String

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        String result = "";
        try
        {
            if (myEvaluationOperation.equals(EvaluationOperation.FUNCTION))
            {
                result += myEvaluationOperation;
                if (left != null)
                    result += '('+left.toStringHelper()+')';
            }
            else
            {
                if (left != null) result += left.toStringHelper();
                result += myEvaluationOperation;
                if (right != null) result += right.toStringHelper();
            }
        }
        catch (Exception e)
        {}
        return result;
    }
    
    //assists toString, calling itself recursivly on all subEquatons

    /**
     *
     * @return
     */
    public String toStringHelper()
    {
        String result = "";
        try
        {
            if (myEvaluationOperation.equals(EvaluationOperation.FUNCTION))
            {
                if (myEvaluationOperation.equals(9))
                {
                    if (left != null) result += '('+left.toStringHelper()+')';
                    result += myEvaluationOperation;
                }
                else
                {
                    result += myEvaluationOperation;
                    if (left != null) result += '('+left.toStringHelper()+')';
                }
            }
            else
            {
                if (left != null)
                    result += "("+left.toStringHelper();
                result += myEvaluationOperation;
                if (right != null)
                    result += right.toStringHelper()+")";
            }
        }
        catch (Exception e)
        {}
        return result;
    }
}