package math;
interface TwoNumberOperation
{
    public complex execute(complex a, complex b);
    
    boolean is_analytic();
}

/**
 *
 * @author mer49
 */
public class OperationEvaluation implements EvaluationOperation {
	
    private final EvaluationOperation left,right;
    private final int op;
	
    /**
     *
     * @param opr
     * @param left
     * @param right
     */
    public OperationEvaluation(MathToken opr, EvaluationOperation left,EvaluationOperation right) {
        this.left = left;
        this.right = right;
        op = opr.getToken()-1;
    }
    @Override
    public boolean equals(int id) {return id == 3;}
    @Override
    public String toString() {return MathToken.masterTokens[op][0];}
	
    /**
     *
     * @param variables
     * @return
     */
    @Override
    public complex operate(ComplexContex variables) {
        return binary_ops[op].execute(left.operate(variables),right.operate(variables));
    }
    

    /**
     *
     * @return
     */
        @Override
    public boolean is_analytic()
    {
        return binary_ops[op].is_analytic();
    }
	
	/*This constant array gold 6 implementations of the TwoNumberOperation class,
		and allows the operation of a MathToken to be accessed with constant time 
		based on its myToken id number.*/
	private static final TwoNumberOperation[] binary_ops = {
		new TwoNumberOperation() {
                        @Override
			public complex execute(complex a, complex b) {
				return a.plus(b);}
                        @Override
                        public boolean is_analytic() {
                            return true ;}},	
		new TwoNumberOperation() {
                        @Override
			public complex execute(complex a, complex b) {
				return a.minus(b);}
                        @Override
                        public boolean is_analytic() {
                            return true ;}},	
		new TwoNumberOperation() {
                        @Override
			public complex execute(complex a, complex b) {
				return a.times(b);}
                        @Override
                        public boolean is_analytic() {
                            return true ;}},
		new TwoNumberOperation() {
                        @Override
			public complex execute(complex a, complex b) {
				return a.over(b);}
                        @Override
                        public boolean is_analytic() {
                            return true ;}},
		new TwoNumberOperation() {
                        @Override
			public complex execute(complex a, complex b) {
				return a.toThe(b);}
                        @Override
                        public boolean is_analytic() {
                            return true ;}},//ERRORERRORERROR
		new TwoNumberOperation() {
                        @Override
			public complex execute(complex a, complex b) {
				complex temp = a.over(b);
				return temp.minus(new complex((int)temp.real,(int)temp.imag));}
                        @Override
                        public boolean is_analytic() {
                            return false ;}}};	
}
