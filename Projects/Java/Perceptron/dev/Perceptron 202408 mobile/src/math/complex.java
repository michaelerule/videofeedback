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

    // STATIC CONSTANTS
    public static final float   e     = (float) Math.E;
    public static final float   pi    = (float) Math.PI;
    public static final float   TWOPI = 2 * complex.pi;
    public static final complex I     = new complex(0, 1);
    public static final complex E     = new complex(complex.e);
    public static final complex PI    = new complex(complex.pi);
    public static final complex PHI   = new complex((float) ((Math.sqrt(5) - 1) / 2));
    
    public static complex polar(float r, float theta) {
        return new complex(r*(float)Math.cos(theta),r*(float)Math.sin(theta));
    }
    public static complex sqrt(complex num) {
        return polar((float) Math.pow(num.rSquared(), .25),
                arg(num) * .5f);
    }
    public static complex oneOver(complex num) {
        return (new complex(num.real, -num.imag)).scale(num.rSquared());
    }
    public static complex sin(complex num) {
        float a = (float) Math.exp(num.imag) * .5f;
        float b = .25f / a;
        return new complex((a + b) * (float) Math.sin(num.real),(a - b) * (float) Math.cos(num.real));
    }
    public static complex cos(complex num) {
        float a = (float) Math.exp(num.imag) * .5f;
        float b = .25f / a;
        return new complex((a + b) * (float) Math.cos(num.real),(b - a) * (float) Math.sin(num.real));
    }
    public static complex tan(complex num) {return sin(num).devidedBy(cos(num));}
    public static complex csc(complex num) {return oneOver(sin(num));}
    public static complex sec(complex num) {return oneOver(cos(num));}
    public static complex cot(complex num) {return cos(num).devidedBy(sin(num));}
    public static complex atan(complex num) {
        float b = num.imag;
        float a = num.real;
        float c = a * a;
        return new complex((sign(a) * pi - (float) Math.atan2(b + 1, a) + (float) Math.atan2(b - 1, a)) * .5f,
                (float) Math.log((c + (float) Math.pow(b + 1, 2)) / (c + (float) Math.pow(b - 1, 2))) * .5f);
    }
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
    public static complex acsc(complex num) {
        return (new complex(0, -1)).times(ln((sqrt((new complex(1)).minus(oneOver(num.squared())))).plus(I.over(num))));
    }
    public static complex asec(complex num) {
        return (new complex(pi / 2)).plus(I.times(ln((sqrt((new complex(1)).minus(oneOver(num.squared())))).plus(I.over(num)))));
    }
    public static complex acot(complex num) {
        return atan(new complex(num.imag,num.real));
    }
    public static float sinh(float n) {return ((float) Math.exp(n) - (float) Math.exp(-n)) * .5f; }
    public static float cosh(float n) {return ((float) Math.exp(n) + (float) Math.exp(-n)) * .5f; }
    public static float tanh(float n) {return ((float) (float) Math.exp(2 * n) - 1) / ((float) (float) Math.exp(2 * n) + 1); }
    public static complex sinh(complex num) {return ((E.toThe(num)).minus(E.toThe(num.scale(-1)))).over(new complex(2));}
    public static complex cosh(complex num) {return ((E.toThe(num)).plus(E.toThe(num.scale(-1)))).over(new complex(2));}
    public static complex tanh(complex num) {
        complex temp = E.toThe(num.scale(2));
        complex temp2 = new complex(1);
        return (temp.minus(temp2)).over(temp.plus(temp2));
    }
    public static complex csch(complex num) {
        return (new complex(2)).over((E.toThe(num)).minus(E.toThe(num.scale(-1))));
    }
    public static complex sech(complex num) {
        return (new complex(2)).over((E.toThe(num)).plus(E.toThe(num.scale(-1))));
    }
    public static complex coth(complex num) {
        complex temp = E.toThe(num.scale(2));
        complex temp2 = new complex(1);
        return (temp.plus(temp2)).over(temp.minus(temp2));
    }
    public static complex asinh(complex num) {
        return ln(num.plus(sqrt(num.squared().plus(new complex(1)))));
    }
    public static complex acosh(complex num) {
        complex temp = new complex(1);
        return ln(num.plus(sqrt(num.minus(temp)).times(sqrt(num.plus(temp)))));
    }
    public static complex atanh(complex num) {
        complex temp = new complex(1);
        return (ln(temp.plus(num)).minus(ln(temp.minus(num)))).scale(.5f);
    }
    public static complex acsch(complex num) {
        complex temp = oneOver(num);
        return ln(sqrt((new complex(1)).plus(temp.squared())).plus(temp));
    }
    public static complex asech(complex num) {
        complex temp = oneOver(num);
        complex temp2 = new complex(1);
        return ln((sqrt(temp.minus(temp2)).
                times(sqrt(temp2.plus(temp)))).plus(temp));
    }
    public static complex acoth(complex num) {
        complex temp = oneOver(num);
        complex temp2 = new complex(1);
        return ((ln(temp2.plus(temp))).minus(ln(temp2.plus(temp)))).scale(.5f);
    }

    public static complex conj(complex num) {return new complex(num.real, -1 * num.imag);}
    public static int sign (float n) {return n == 0 ? 0 : n < 0 ? -1 : 1;}
    public static int sign2(float n) {return n <= 0 ? -1 : 1;}
    public static complex sign(complex num) {return new complex(sign(num.real), sign(num.imag));}
    public static complex abs(complex num) {
        return new complex(num.imag==0? (float)Math.abs(num.real): complex.mod(num));
    }
    public static complex round(complex num) {
        return new complex((float) Math.round(num.real),
                (float) Math.round((int) num.imag));
    }
    public complex rotate(float degrees) {return times(polar(1f, degrees));}
    public static complex integer(complex num) {
        return new complex((int) num.real, (int) num.imag);
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

    /** attempts to parse a complex number from a string
     * @param data
     * @return */
    public static complex parseComplex(String data) {
        int breakIndex = data.indexOf('+', 1);
        if (breakIndex < 0) breakIndex = data.indexOf('-', 1);
        String iPart = data.indexOf('+',1) > 0
                ? data.substring(breakIndex + 1, data.length())
                : data.substring(breakIndex    , data.length());
        return new complex(Float.parseFloat(data.substring(0, breakIndex)),Float.parseFloat(iPart));
    }

    public float real, imag;
    public complex(float Real, float Imag) {real = Real  ; imag = Imag;  }
    public complex(complex n)              {real = n.real; imag = n.imag;}
    public complex(float Real)             {real = Real  ; imag = 0;     }
    public complex()                       {real = 0     ; imag = 0;     }
    public void setComplex(float Real, float Imag) {real = Real;imag = Imag;}

    /** attempts to parse a new complex number from a string
     * @param data */
    public complex(String data) {
        int breakIndex = data.indexOf('+', 1);
        if (breakIndex < 0) breakIndex = data.indexOf('-', 1);
        String iPart = data.indexOf('+', 1) > 0
            ? data.substring(breakIndex + 1, data.length())
            : data.substring(breakIndex    , data.length());
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
            if (imag == 0) return "0";
            if (imag == 1) return "i";
            return toSTring(imag) + "i";
        } else if (imag > 0) {
            if (imag == 1) return toSTring(real) + "+" + "i";
            return toSTring(real) + "+" + toSTring(imag) + "i";
        } else if (imag == 0) {
            return "" + toSTring(real);
        } else if (imag < 0) {
            if (imag == 1) return toSTring(real) + "+" + "i";
            return toSTring(real) + toSTring(imag) + "i";
        }
        return "NaN";
    }

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
     *  a very common calculation
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
     * This divides two complex numbers and returns a complex quotient
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
    
    public boolean equals(complex o) {return (this.real==o.real && this.imag==o.imag);}
    public float length() {return complex.mod(this);}
    public float angle() {return complex.arg(this);}

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