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
    public boolean equals(int id) {
        return id == 2;
    }
    /**
     *
     * @param variables
     * @return
     */
    public complex operate(ComplexContex variables) {
        return operations[myOperation].execute(left.operate(variables));
    }
    /**
     *
     * @return
     */
    public String toString() {
        return MathToken.masterTokens[myOperation + 8][0];
    }
    /**
     *
     * @return
     */
    public boolean is_analytic() {
        return operations[myOperation].is_analytic();
    }
    /*This constant array holds 38 implementations of the Operation interface,
    and allow a mathtokens function to be accessed with constant time based on its
    myToken id*/
    private static final Operation[] operations = {
        new Operation() {
            public complex execute(complex num) {
                return complex.factorial(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.sqrt(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return num.scale(-1);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.ln(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.round(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return new complex(num.real);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return new complex(num.imag);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.abs(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.integer(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.sign(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return new complex(complex.arg(num));
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.conj(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.sin(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.cos(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.tan(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.csc(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.sec(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.cot(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.asin(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.acos(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.atan(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.acsc(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.asec(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.acot(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.sinh(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.cosh(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.tanh(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.csch(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.sech(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.coth(num);
            }
            public boolean is_analytic() {return true;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.asinh(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.acosh(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.atanh(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.acsch(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.asech(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.acoth(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.zeta(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return (new complex((float) random(), (float) random())).times(num);
            }
            public boolean is_analytic() {return false;}
        },
        new Operation() {
            public complex execute(complex num) {
                return complex.E.toThe(num);
            }
            public boolean is_analytic() {return true;}
        }
    };
}
