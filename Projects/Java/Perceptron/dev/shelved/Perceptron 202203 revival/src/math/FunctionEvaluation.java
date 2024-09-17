package math;

import static java.lang.Math.*;
//
//  FunctionEvaluation.java
//  ResFracti
//
//  Created by Michael Rule on Mon Apr 04 2005.
//  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
//

/**
 *
 * @author mer49
 */
public class FunctionEvaluation implements EvaluationOperation {

    private final EvaluationOperation left;
    private final int myOperation;

    /**
     *
     * @param opr
     * @param left
     */
    public FunctionEvaluation(MathToken opr, EvaluationOperation left) {
        this.left = left;
        myOperation = opr.getToken() - 9;
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public boolean equals(int id) {
        return id == 2;
    }

    /**
     *
     * @param variables
     * @return
     */
    @Override
    public complex operate(ComplexContex variables) {
        return operations[myOperation].execute(left.operate(variables));
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return MathToken.masterTokens[myOperation + 8][0];
    }

    /**
     *
     * @return
     */
    @Override
    public boolean is_analytic() {
        return operations[myOperation].is_analytic();
    }
    /*This constant array holds 38 implementations of the Operation interface,
    and allow a mathtokens function to be accessed with constant time based on its
    myToken id*/
    private static final Operation[] operations = {
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.factorial(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.sqrt(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return num.scale(-1);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.ln(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.round(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return new complex(num.real);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return new complex(num.imag);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.abs(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.integer(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.sign(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return new complex(complex.arg(num));
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.conj(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.sin(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.cos(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.tan(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.csc(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.sec(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.cot(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.asin(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.acos(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.atan(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.acsc(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.asec(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.acot(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.sinh(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.cosh(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.tanh(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.csch(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.sech(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.coth(num);
            }

            @Override
            public boolean is_analytic() {
                return true;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.asinh(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.acosh(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.atanh(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.acsch(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.asech(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.acoth(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return complex.zeta(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        },
        new Operation() {

            @Override
            public complex execute(complex num) {
                return (new complex((float) random(), (float) random())).times(num);
            }

            @Override
            public boolean is_analytic() {
                return false;
            }
        }
    };
}
