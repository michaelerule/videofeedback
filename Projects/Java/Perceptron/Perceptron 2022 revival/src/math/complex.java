package math;
//
//  complex.java
//  
//
//  Created by Michael Rule on Mon Jul 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. No rights reserved.
//

/**
 *
 * @author mer49
 */
public class complex {

    // STATIC
    // STATIC CONSTANTS

    /**
     *
     */
    public static float e = (float) Math.E;

    /**
     *
     */
    public static float pi = (float) Math.PI;

    /**
     *
     */
    public static final float TWOPI = 2 * pi;
    // Common complex constants

    /**
     *
     */
    public static final complex I = new complex(0, 1);

    /**
     *
     */
    public static final complex E = new complex(e);

    /**
     *
     */
    public static final complex PI = new complex(pi);

    /**
     *
     */
    public static final complex PHI = new complex((float) ((Math.sqrt(5) - 1) / 2));

    /**
     *
     * @param r
     * @param theta
     * @return
     */
    public static complex polar(float r, float theta) {
        return new complex(
                r * (float) Math.cos(theta),
                r * (float) Math.sin(theta));
    }

    /**
     *
     * @param n
     * @return
     */
    public static int sign(float n) {
        return n == 0 ? 0 : n < 0 ? -1 : 1;
    }

    /**
     *
     * @param n
     * @return
     */
    public static int sign2(float n) {
        return n <= 0 ? -1 : 1;
    }

    /**
     * This finds the first square root of the complex number
     * @param num
     * @return 
     */
    public static complex sqrt(complex num) {
        return polar((float) Math.pow(num.rSquared(), .25),
                arg(num) * .5f);
    }

    /**
     * this takes the recipricol of a complex number
     * @param num
     * @return 
     */
    public static complex oneOver(complex num) {
        return (new complex(num.real, -num.imag)).scale(num.rSquared());
    }

    //begin complex trigonomentry
    /**
     * This returns sine of a complex number, also complex
     * @param num
     * @return 
     */
    public static complex sin(complex num) {
        float a = (float) Math.exp(num.imag) * .5f;
        float b = .25f / a;
        return new complex((a + b) * (float) Math.sin(num.real),
                (a - b) * (float) Math.cos(num.real));
    }

    /**
     * this returns the cosine of a complex number, also complex
     * @param num
     * @return 
     */
    public static complex cos(complex num) {
        float a = (float) Math.exp(num.imag) * .5f;
        float b = .25f / a;
        return new complex((a + b) * (float) Math.cos(num.real),
                (b - a) * (float) Math.sin(num.real));
    }

    /**
     * This returns the tangent of a complex number, also complex
     * @param num
     * @return 
     */
    public static complex tan(complex num) {
        return sin(num).devidedBy(cos(num));
    }

    /**
     * This returns the cosecant of a complex number, also complex
     * @param num
     * @return 
     */
    public static complex csc(complex num) {
        return oneOver(sin(num));
    }

    /**
     * This returns the secant of a complex number, also complex
     * @param num
     * @return 
     */
    public static complex sec(complex num) {
        return oneOver(cos(num));
    }

    /**
     * This returns the cotangent of a complex number, also complex
     * @param num
     * @return 
     */
    public static complex cot(complex num) {
        return cos(num).devidedBy(sin(num));
    }

    /**
     * the arctangent of a complex number
     * @param num
     * @return 
     */
    public static complex atan(complex num) {
        float b = num.imag;
        float a = num.real;
        float c = a * a;
        return new complex((sign(a) * pi - (float) Math.atan2(b + 1, a) + (float) Math.atan2(b - 1, a)) * .5f,
                (float) Math.log((c + (float) Math.pow(b + 1, 2)) / (c + (float) Math.pow(b - 1, 2))) * .5f);
    }

    /**
     * the arcsin of a complex number
     * @param num
     * @return 
     */
    public static complex asin(complex num) {
        float a, b, c, d, g;
        a = num.real;
        c = a * a;
        a *= 2;
        b = num.imag;
        d = b * b;
        g = c + d + 1;
        c = (float) Math.sqrt(g + a);
        d = (float) Math.sqrt(g - a);
        return new complex((float) Math.asin((c - d) * .5f),
                (float) Math.log(((float) Math.sqrt(2 * (d * c + g - 2)) + c + d) * .5f) * sign2(b));
    }

    /**
     * the inverse cosine of a complex number
     * @param num
     * @return 
     */
    public static complex acos(complex num) {
        float b = num.imag;
        float a = num.real;
        float c, d, h, f, g;
        h = a * a;
        f = b * b;
        a *= 2;
        g = h + f + 1;
        c = (float) Math.sqrt(g + a);
        d = (float) Math.sqrt(g - a);
        return new complex(
                (float) Math.acos((c - d) * .5f),
                -1 * (float) Math.log(
                ((float) Math.sqrt(2 * (d * c + g - 2)) + c + d) * .5f) * sign2(b));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex acsc(complex num) {
        return (new complex(0, -1)).times(
                ln((sqrt((new complex(1)).minus(
                oneOver(num.squared())))).plus(I.over(num))));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex asec(complex num) {
        return (new complex(pi / 2)).plus(I.times(ln((sqrt((new complex(1)).minus(oneOver(num.squared())))).plus(I.over(num)))));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex acot(complex num) {
        return new complex();
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex sinh(complex num) {
        return ((E.toThe(num)).minus(E.toThe(
                num.scale(-1)))).over(new complex(2));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex cosh(complex num) {
        return ((E.toThe(num)).plus(E.toThe(
                num.scale(-1)))).over(new complex(2));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex tanh(complex num) {
        complex temp = E.toThe(num.scale(2));
        complex temp2 = new complex(1);
        return (temp.minus(temp2)).over(temp.plus(temp2));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex csch(complex num) {
        return (new complex(2)).over((E.toThe(num)).minus(E.toThe(num.scale(-1))));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex sech(complex num) {
        return (new complex(2)).over((E.toThe(num)).plus(E.toThe(num.scale(-1))));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex coth(complex num) {
        complex temp = E.toThe(num.scale(2));
        complex temp2 = new complex(1);
        return (temp.plus(temp2)).over(temp.minus(temp2));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex asinh(complex num) {
        return ln(num.plus(sqrt(num.squared().plus(new complex(1)))));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex acosh(complex num) {
        complex temp = new complex(1);
        return ln(num.plus(sqrt(num.minus(temp)).times(sqrt(num.plus(temp)))));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex atanh(complex num) {
        complex temp = new complex(1);
        return (ln(temp.plus(num)).minus(ln(temp.minus(num)))).scale(.5f);
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex acsch(complex num) {
        complex temp = oneOver(num);
        return ln(sqrt((new complex(1)).plus(temp.squared())).plus(temp));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex asech(complex num) {
        complex temp = oneOver(num);
        complex temp2 = new complex(1);
        return ln((sqrt(temp.minus(temp2)).
                times(sqrt(temp2.plus(temp)))).plus(temp));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex acoth(complex num) {
        complex temp = oneOver(num);
        complex temp2 = new complex(1);
        return ((ln(temp2.plus(temp))).minus(ln(temp2.plus(temp)))).scale(.5f);
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex conj(complex num) {
        return new complex(num.real, -1 * num.imag);
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex sign(complex num) {
        return new complex(sign(num.real), sign(num.imag));
    }

    /**
     *
     * @param num
     * @return
     */
    public static complex abs(complex num) {
        if (num.imag == 0) {
            return new complex((float) Math.abs(num.real));
        }
        return new complex(complex.mod(num));
    }

    /*rounds real and complex parts to the nearest integer*/

    /**
     *
     * @param num
     * @return
     */

    public static complex round(complex num) {
        return new complex((float) Math.round(num.real),
                (float) Math.round((int) num.imag));
    }

    /**
     *
     * @param degrees
     * @return
     */
    public complex rotate(float degrees) {
        return times(polar(1f, degrees));
    }

    /**grabs the integer part of the real an imagenary part
     * @param num
     * @return */
    public static complex integer(complex num) {
        return new complex((int) num.real, (int) num.imag);
    }

    /** the hyperbolic sin of a floa
     * @param n
     * @return t*/
    public static float sinh(float n) {
        return ((float) Math.exp(n) - (float) Math.exp(-n)) * .5f;
    }

    /** the hyperbolic cos of a floa
     * @param n
     * @return t*/
    public static float cosh(float n) {
        return ((float) Math.exp(n) + (float) Math.exp(-n)) * .5f;
    }

    /** the hyperbolic tan of a floa
     * @param n
     * @return t*/
    public static float tanh(float n) {
        return ((float) (float) Math.exp(2 * n) - 1) / ((float) (float) Math.exp(2 * n) + 1);
    }

    /**
     * This takes e to the complex power
     * @param num
     * @return 
     */
    public static complex eToThe(complex num) {
        return polar((float) (float) Math.exp(num.real), num.imag);
    }

    /**
     * This takes the natural log of a complex number
     * @param num
     * @return 
     */
    public static complex ln(complex num) {
        return new complex((float) (float) Math.log(mod(num)), arg(num));
    }

    /**
     * this computes n! in complex terms..crazy
     * @param num
     * @return 
     */
    public static complex factorial(complex num) {
        return null;
    }

    /** Strips annoying trailing zeroes from floating points */
    private static String toSTring(float number) {
        if ((int) number == number) {
            return "" + (int) number;
        }
        return "" + number;
    }

    /** attempts to parse a complex number from a strin
     * @param data
     * @return */
    public static complex parseComplex(String data) {
        int breakIndex = data.indexOf('+', 1);
        if (breakIndex < 0) {
            breakIndex = data.indexOf('-', 1);
        }
        String iPart;
        if (data.indexOf('+', 1) > 0) {
            iPart = data.substring(
                    breakIndex + 1, data.length());
        } else {
            iPart = data.substring(breakIndex, data.length());
        }
        return new complex(Float.parseFloat(data.substring(0, breakIndex)),
                Float.parseFloat(iPart));
    }
    //NON-STATIC
    //INSTANCE VARIABLES

    /**
     *
     */
    public float real,

    /**
     *
     */
    imag;

    /**
     * Initializes a new complex number with real part Real and imagenary part Imag
     * @param Real
     * @param Imag
     */
    public complex(float Real, float Imag) {
        real = Real;
        imag = Imag;
    }

    /**
     * Initializes a new complex number as complex number n
     * @param n
     */
    public complex(complex n) {
        real = n.real;
        imag = n.imag;
    }

    /**
     * initializes a duoble as a complex number
     * @param Real
     */
    public complex(float Real) {
        real = Real;
        imag = 0;
    }

    /**
     * initializes zero as a complex number
     */
    public complex() {
        real = 0;
        imag = 0;
    }

    /** attempts to parse a new complex number from a string
     * @param data */
    public complex(String data) {
        int breakIndex = data.indexOf('+', 1);
        if (breakIndex < 0) {
            breakIndex = data.indexOf('-', 1);
        }
        String iPart;
        if (data.indexOf('+', 1) > 0) {
            iPart = data.substring(breakIndex + 1, data.length());
        } else {
            iPart = data.substring(breakIndex, data.length());
        }

        real = (float) (Double.parseDouble(data.substring(0, breakIndex)));
        imag = (float) (Double.parseDouble(iPart));
    }

    /**
     * creates a string representation of the complex number
     * @return 
     */
    @Override
    public String toString() {
        //System.out.println(real+" "+imag);
        if (real == 0) {
            if (imag == 0) {
                return "0";
            }
            if (imag == 1) {
                return "i";
            }
            return toSTring(imag) + "i";
        } else if (imag > 0) {
            if (imag == 1) {
                return toSTring(real) + "+" + "i";
            }
            return toSTring(real) + "+" + toSTring(imag) + "i";
        } else if (imag == 0) {
            return "" + toSTring(real);
        } else if (imag < 0) {
            if (imag == 1) {
                return toSTring(real) + "+" + "i";
            }
            return toSTring(real) + toSTring(imag) + "i";
        }
        return "NaN";
    }

    /**
     * this will reset both parts of the complex number at once
     * @param Real
     * @param Imag
     */
    public void setComplex(float Real, float Imag) {
        real = Real;
        imag = Imag;
    }

    //INSTANCE METHODS
    /**
     * this returns the complex modulus,
     *        the distance between complex number real,imag and the origin
     * @param num
     * @return 
     */
    public static float mod(complex num) {
        return (float) Math.sqrt(num.real * num.real + num.imag * num.imag);
    }

    /**
     * this returns the angle made between the +real axis and
     *        the line drawn from the complex number real,imag to the origin
     *  A.k.a.the atan2 implementation similar to Ultra Fractal.
     * @param num
     * @return 
     */
    public static float arg(complex num) {
        return (float) Math.atan2(num.imag, num.real);
    }

    /** This returns the i value squared plus the r value squared, 
     *  a very common calculatio
     * @return n*/
    public float rSquared() {
        return real * real + imag * imag;
    }

    /**
     * This multiplies the complex number by a scalar
     * @param scalar
     * @return 
     */
    public complex scale(float scalar) {
        return new complex(real * scalar, imag * scalar);
    }

    /**
     * This devides two complex numbers and returns a complex quotient
     * @param n
     * @return 
     */
    public complex devidedBy(complex n) {
        float num = 1 / n.rSquared();
        return new complex((real * n.real + imag * n.imag) * num,
                -((real * n.imag - imag * n.real) * num));
    }

    /** another name for devidedB
     * @param n
     * @return */
    public complex over(complex n) {
        return this.devidedBy(n);
    }

    /**
     * This multiplies two complex numbers and returns a complex product
     * @param n
     * @return 
     */
    public complex times(complex n) {
        float x4 = real * n.real;
        float x5 = imag * n.imag;
        return new complex(x4 - x5, (real + imag) * (n.real + n.imag) - x4 - x5);
    }

    /**
     * This subtracts two complex numbers and returns a complex difference
     * @param n
     * @return 
     */
    public complex minus(complex n) {
        return new complex(real - n.real, imag - n.imag);
    }

    /**
     * This adds two complex numbers and returns a complex sum
     * @param n
     * @return 
     */
    public complex plus(complex n) {
        return new complex(real + n.real, imag + n.imag);
    }

    /**
     * This takes one complex number to a complex power and returns a complex
     * @param n
     * @return 
     */
    public complex toThe(complex n) {
        float expReal = n.real;
        float expImag = n.imag;
        float baseArg = (float) Math.atan2(imag, real);
        float intrCalc1 = real * real + imag * imag;
        return polar(
                (float) Math.pow(intrCalc1, expReal * .5f)
                * (float) Math.exp(-expImag * baseArg),
                expReal * baseArg
                + .5f * expImag * (float) Math.log(intrCalc1));
    }

    /**
     * This takes the complex base to the given real power (a+bi)^#
     * @param n
     * @return 
     */
    public complex toThe(float n) {
        return polar(
                (float) Math.pow(real * real + imag * imag, n * .5f), n * arg(this));
    }

    /**
     * This squares a complex number (faster than this.toThe(2))
     * @return 
     */
    public complex squared() {
        return new complex(real * real - imag * imag, 2 * real * imag);
    }

    /**
     * This returns a log of given real base of the complex number
     * @param base
     * @return 
     */
    public complex log(float base) {
        return ln(this).scale((float) Math.log(base));
    }

    /**
     * This returns the log of given complex base of the complex number
     * @param base
     * @return 
     */
    public complex log(complex base) {
        return ln(this).devidedBy(ln(base));
    }

    /**
     * This multiplis the complex number by +i
     * @return 
     */
    public complex timesI() {
        return new complex(-imag, real);
    }

    /**
     *
     * @param z
     * @return
     */
    public static complex zeta(complex z) {
        complex zed = new complex(-z.real, -z.imag);
        float r = 0, i = 0;
        for (complex k = new complex(1); k.real < 40; k.real++) {
            complex K = k.toThe(zed);
            r += K.real;
            i += K.imag;
            if (Float.isNaN(r) || Float.isInfinite(r)
                    || Float.isNaN(i) || Float.isInfinite(i)) {
                break;
            }
        }
        return new complex(r, i);
    }
}
