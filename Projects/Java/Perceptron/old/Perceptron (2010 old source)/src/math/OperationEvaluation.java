package math;
import perceptron.*;
import math.ComplexVarList;
interface TwoNumberOperation
{
    public complex execute(complex a, complex b);
    
    boolean is_analytic();
}

public class OperationEvaluation implements EvaluationOperation {
	
	private final EvaluationOperation left,right;
	private final int myOperation;
	
	public OperationEvaluation(MathToken opr, EvaluationOperation left,EvaluationOperation right) {
		this.left = left;
		this.right = right;
		myOperation = opr.getToken()-1;
	}
	
	public boolean equals(int id) {
		return id == 3;
	}
	
	public complex operate(ComplexVarList variables) {
		return TwoNumberOperations[myOperation].execute(left.operate(variables),right.operate(variables));
	}
	
	public String toString() {
		return MathToken.masterTokens[myOperation][0];
	}

    public boolean is_analytic()
    {
        return TwoNumberOperations[myOperation].is_analytic();
    }
	
	/*This constant array gold 6 implementations of the TwoNumberOperation class,
		and allows the operation of a MathToken to be accessed with constant time 
		based on its myToken id number.*/
	private static final TwoNumberOperation[] TwoNumberOperations = {
		new TwoNumberOperation() {
			public complex execute(complex a, complex b) {
				return a.plus(b);}
                        public boolean is_analytic() {
                            return true ;}},	
		new TwoNumberOperation() {
			public complex execute(complex a, complex b) {
				return a.minus(b);}
                        public boolean is_analytic() {
                            return true ;}},	
		new TwoNumberOperation() {
			public complex execute(complex a, complex b) {
				return a.times(b);}
                        public boolean is_analytic() {
                            return true ;}},
		new TwoNumberOperation() {
			public complex execute(complex a, complex b) {
				return a.over(b);}
                        public boolean is_analytic() {
                            return true ;}},
		new TwoNumberOperation() {
			public complex execute(complex a, complex b) {
				return a.toThe(b);}
                        public boolean is_analytic() {
                            return true ;}},//ERRORERRORERROR
		new TwoNumberOperation() {
			public complex execute(complex a, complex b) {
				complex temp = a.over(b);
				return temp.minus(new complex((int)temp.real,(int)temp.imag));}
                        public boolean is_analytic() {
                            return false ;}}};	
}
