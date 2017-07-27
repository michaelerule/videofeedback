package math;

//
//  MathToken.java
//
//  Created by Michael Rule on Mon Aug 09 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//
/* this class describes a single object that may be encountered in a mathematical equations,
which may be a constant, a variable, a function, or an operation on teo numbers.
This class contains static methods that convert a user inputted Equation into an instance
of Equation, a special binary tree of MathTokens that allws the computer to evaluate custom
equations. The conversion methods are not efficient, but they are not used excessivly either.
This class also contains methods for performing any possible opertaion stored in a MathToken,
with reasonable efficiency.
 */
import math.ComplexVarList;
import java.lang.*;
import java.util.*;
import java.io.*;

public class MathToken {

    int OPERATOR = 0;
    int VARIABLE = -1;
    int CONSTANT = -2;
    private int type;
    private int myToken;
    private float myNumber;
    private char myVar;
    private MathToken[] myObject;

    public MathToken(char Char) {
        myToken = -1;
        myVar = Char;
    }

    public MathToken(int num) {
        if (num > 0 && num <= masterTokens.length) {
            myToken = num;
        } else {
            myToken = -2;
            myNumber = num;
        }
    }

    public MathToken(float num) {
        myNumber = num;
        myToken = -2;
    }

    //Creates a new MathToken storing the same information as token
    public MathToken(MathToken token) {
        myToken = token.getToken();
        myObject = token.getEquation();
        myVar = token.getVar();
        myNumber = token.getNumber();
        type = token.getOperationTypeId();
    }

    /*Creates a new MathToken holding another array of MathTokens*/
    public MathToken(MathToken[] otherTokens) {
        myToken = 0;
        myObject = new MathToken[otherTokens.length];
        System.arraycopy(otherTokens, 0, myObject, 0, otherTokens.length);
    }

    /*Creates a new MathToken based on the function name passed in data*/
    public MathToken(String data) {
        this(extractToken(data));
    }

    //Returns the ID of this MathToken
    public int getToken() {
        return myToken;
    }

    public boolean isVariable() {
        return myToken == -1;
    }

    //Returns the OperationTypeID of this MathToken
    public int getOperationTypeId() {
        return type;
    }

    //Returns the variable being held in this MathTOken, if any
    public char getVar() {
        return myVar;
    }
    //Returns the number stored in this MathToken, if any

    public float getNumber() {
        return myNumber;
    }
    //Returns the array of MathTOkens stored in this MathTOken, if any

    public MathToken[] getEquation() {
        if (myObject != null) {
            return myObject;
        }
        return (new MathToken[]{this});
    }

    //Returns true if all data fields in this MathTOken match those in otherToken
    public boolean equals(MathToken otherToken) {
        return (otherToken.getToken() == myToken
                && otherToken.getVar() == myVar
                && otherToken.getNumber() == myNumber);
    }

    //Returns true if the number passed corresponds to the myToken ID
    public boolean equals(int num) {
        return (myToken == num);
    }

    /*This returns true if the tokens id is -1 or -2 (a number or variable)*/
    public boolean isOperator() {
        return (myToken > 0);
    }

    /* returns true if the passed token represents a function*/
    public boolean isFunction() {
        return (myToken > 8);
    }

    /***************************************************************************
     * The Following Methods are all static and are used inconverting a String into an Equation.
     *
     ***********************************************************************/
    //This method can extract a double form the beginning of a string
    private static float toFloat(String data) throws NumberFormatException {
        double result = 0;
        int i, j;
        if (data.charAt(0) == '-') {
            j = i = 2;
        } else {
            j = i = 1;
        }
        for (; i <= data.length(); i++) {
            try {
                result = Double.parseDouble(data.substring(0, i));
            } catch (NumberFormatException e) {
                break;
            }
        }
        if (i == j) {
            throw new NumberFormatException("no number here");
        }
        return (float) result;
    }

    //This method is able to extract a MathToken form the front of a String
    public static MathToken extractToken(String data) {
        int j, k;
        try {
            return new MathToken(toFloat(data));
        } catch (Exception e) {
            boolean done = false;
            j = 0;
            while (j < masterTokens.length && !done) {
                k = 0;
                while (k < masterTokens[j].length && !done) {
                    int checkingTokenLength = masterTokens[j][k].length();
                    if (checkingTokenLength <= data.length()) {
                        if (isFirst(data, masterTokens[j][k].toLowerCase())) {
                            return new MathToken(j - 1);
                        }
                    }
                    k++;
                }
                j++;
            }
        }
        return new MathToken(data.charAt(0));
    }

    /*return true if token begins data*/
    public static boolean isFirst(String data, String token) {
        if (data.length() < token.length()) {
            return false;
        }
        return (data.substring(0, token.length()).equals(token));
    }

    //This method removes any extra spaces from a user inputted equation
    public static String removeWhiteSpace(String data) {
        StringTokenizer token = new StringTokenizer(data);
        String result = "";
        while (token.hasMoreTokens()) {
            result += token.nextToken();
        }
        return result;
    }

    /** Calls the methods that convert  a String into an Equation */
    public static Equation toEquation(String data) {
        data = removeWhiteSpace(data);
        MathToken[] token = toTokenArray(data);
        token = toBranchedTokenArray(token);
        return toEquation(token);
    }

    /** converts an appropriatly branched array of math tokens into an Equation */
    private static Equation toEquation(MathToken[] token) {
        if (token != null) {
            if (token.length == 1) {
                if (token[0].equals(0)) {
                    return new Equation(toEquation(token[0].getEquation()));
                } else {
                    return new Equation(token[0]);
                }
            } else if (token.length == 2) {
                if (token[0].isOperator()) {
                    return new Equation(toEquation(token[1].getEquation()), token[0]);
                } else {
                    return new Equation(toEquation(token[0].getEquation()), token[1]);
                }
            } else if (token.length == 3) {
                Equation left, right;
                if (token[0].equals(0)) {
                    left = toEquation(token[0].getEquation());
                } else {
                    left = new Equation(token[0]);
                }
                if (token[2].equals(0)) {
                    right = toEquation(token[2].getEquation());
                } else {
                    right = new Equation(token[2]);
                }
                return new Equation(left, token[1], right);
            }
        }
        return new Equation();
    }

    /** converts an array of math tokens into a branched form based on the order of operations*/
    private static MathToken[] toBranchedTokenArray(MathToken[] token) {
        MathToken[] result = new MathToken[token.length];
        System.arraycopy(token, 0, result, 0, token.length);
        int j;
        j = 0;
        while (j < result.length) {
            if (result[j].equals(7)) {
                int end = getEndParenthesis(result, j);
                result = replace(result, j, end, new MathToken(toBranchedTokenArray(substring(result, j + 1, end - 1))));
            } else {
                j++;
            }
        }

        j = 0;
        while (j < result.length) {
            if (result[j].isFunction()) {
                if (result[j].equals(9)) {
                    result = replace(result, j - 1, j + 1, new MathToken(substring(result, j - 1, j + 1)));
                } else {
                    result = replace(result, j, j + 2, new MathToken(substring(result, j, j + 2)));
                }
            } else {
                j++;
            }
        }

        j = 0;
        while (j < result.length) {
            if (result[j].equals(5)) {
                result = replace(result, j - 1, j + 2, new MathToken(substring(result, j - 1, j + 2)));
            } else {
                j++;
            }
        }
        j = 0;
        while (j < result.length) {
            if (result[j].equals(3) || result[j].equals(4) || token[j].equals(6)) {
                result = replace(result, j - 1, j + 2, new MathToken(substring(result, j - 1, j + 2)));
            } else {
                j++;
            }
        }
        j = 0;
        while (j < result.length) {
            if (result[j].equals(1) || result[j].equals(2)) {
                result = replace(result, j - 1, j + 2, new MathToken(substring(result, j - 1, j + 2)));
            } else {
                j++;
            }
        }
        return result;
    }

    /** parses a sring into a useable array of math tokens*/
    private static MathToken[] toTokenArray(String data) {
        MathToken[] token = new MathToken[data.length()];
        data = data.toLowerCase();
        int i, j, k; //counters
        i = 0;
        while (data.length() > 0 && i < token.length) {
            while ((int) data.charAt(0) > 255) {
                data = data.substring(1); //if it finds a non ascii character it pulls if off and ignores it
            }
            float num = 0;
            j = 1;
            while (j <= data.length()) {
                try {
                    if (data.charAt(j - 1) != '.') {
                        num = (float) (Double.parseDouble(data.substring(0, j)));
                    }
                } catch (NumberFormatException e) {
                    break;
                }
                j++;
            }
            if (j != 1) {
                data = data.substring(j - 1);
                token[i] = new MathToken(num);
            } else {//if it wasn't a number, figure out what it was by scanning masterTokens
                boolean done = false;
                int tokenLength = 0;
                j = 0;
                while (j < masterTokens.length && !done) {
                    k = 0;
                    while (k < masterTokens[j].length && !done) {
                        int checkingTokenLength = masterTokens[j][k].length();
                        if (checkingTokenLength <= data.length()) {
                            if (isFirst(data, masterTokens[j][k]) && (checkingTokenLength > tokenLength)) {
                                token[i] = new MathToken(j + 1);
                                tokenLength = checkingTokenLength;
                                if (j < 20) {
                                    done = true;
                                }
                            }
                        }
                        k++;
                    }
                    j++;
                }
                if (token[i] == null && data.length() > 0) {
                    token[i] = new MathToken(data.charAt(0));
                    data = data.substring(1);
                } else {
                    data = data.substring(tokenLength);
                }
            }
            i++;
        }
        return scrub(token);
    }

    // scrubs tokens, placing any implied * and turning any lone - signs into  signs
    private static MathToken[] scrub(MathToken[] token) {
        if (token.length > 0) {
            /*If there's anything to look at*/
            //make a large array to hold the edited token[]
            MathToken[] scrubber = new MathToken[token.length * 2];
            int placePosition = 1;
            int i = 1;
            if (token[0].equals(2)) {/*special case for the first token, if its a - sign make it a _ sign*/
                if (token[1].equals(-2)) {/*if its followed by a number, make that number negetive*/
                    scrubber[0] = new MathToken(-1.0f * (token[1].getNumber()));
                    i++;
                } else {
                    scrubber[0] = new MathToken(11);
                }
            } else //keep the first token, it must be good
            {
                scrubber[0] = token[0];
            }
            while (i < token.length) {
                if (token[i] != null) {
                    boolean b = !token[i].isOperator();

                    if ((b || token[i].equals(7)) && (!token[i - 1].isOperator()) || b && token[i - 1].equals(8)) {//insert multiplication sign
                        scrubber[placePosition] = new MathToken(3);
                        placePosition++;
                    }
                    //if (b || token[i].equals(8) || token[i].equals(9))
                    //{/*If it is not at the last token (or the last token is a !,),#,v*/
                    if (token[i].equals(2) && token[i - 1].isOperator()) {/*if it finds a - sign following another operator, it investigates for:*/
                        if (token[i + 1].equals(-2)) {
                            /*if it found a -sign in front of a number that is definatly not an subtractaion, then it makes the number negetive*/
                            scrubber[placePosition] = new MathToken(-1.0f * (token[i + 1].getNumber()));
                            i++;
                        } else if (token[i - 1].equals(8) || token[i].equals(9)) {
                            scrubber[placePosition] = new MathToken(3);
                            i--;
                        } else /*otherwise, it turns the - sign into a _() function, which will negate the contents of the following expression or variable*/ {
                            scrubber[placePosition] = new MathToken(11);
                        }
                    } else {
                        scrubber[placePosition] = token[i];
                    }
                    placePosition++;
                }
                i++;
            }
            MathToken[] result = new MathToken[placePosition];
            System.arraycopy(scrubber, 0, result, 0, placePosition);
//System.out.println(stringOf(result));
            return result;
        }
        return token;
    }

    /*replaces indecies start through end-1 with the new MathToken[]*/
    public static MathToken[] replace(MathToken[] data, int start, int end, MathToken[] replacement) {
        MathToken[] result = new MathToken[data.length - end + start];
        System.arraycopy(data, 0, result, 0, start);
        System.arraycopy(replacement, 0, result, start, replacement.length);
        System.arraycopy(data, end, result, start + replacement.length, data.length - end);
        return result;
    }

    /*replaces indecies start through end-1 with the new MathToken*/
    public static MathToken[] replace(MathToken[] data, int start, int end, MathToken replacement) {
        MathToken[] result = new MathToken[data.length - end + start + 1];
        System.arraycopy(data, 0, result, 0, start + 1);
        result[start] = replacement;
        System.arraycopy(data, end, result, start + 1, data.length - end);
        return result;
    }

    /* deletes indecies start through end-1*/
    public static MathToken[] remove(MathToken[] data, int start, int end) {
        MathToken[] result = new MathToken[data.length - end + start];
        System.arraycopy(data, 0, result, 0, start);
        System.arraycopy(data, end, result, start, data.length - end);
        return result;
    }

    /* copies indecies start through end-1*/
    public static MathToken[] substring(MathToken[] data, int start, int end) {
        MathToken[] result = new MathToken[end - start];
        System.arraycopy(data, start, result, 0, result.length);
        return result;
    }

    /* finds the index of the close parenthesis coreesponding to
    the open parenthesis at the passed index, used to break up an array of MathTokens
    into a group of smaller, linked arrays, an intermediate step to making an Equation*/
    public static int getEndParenthesis(MathToken[] token, int start) {
        //System.out.println(stringOf(token));
        int parenthesis = 1;
        int i = start + 1;
        while (parenthesis > 0 && i < token.length) {
            if (token[i].equals(7)) {
                parenthesis++;
            } else if (token[i].equals(8)) {
                parenthesis--;
            }
            i++;
        }
        return i;
    }

//Overrides toString in Object, converts the data of this MathToken to String format
    public String toString() {
        if (myToken == 0) {
            return "";
        }
        if (myToken == -1) {
            return "" + myVar;
        }
        if (myToken == -2) {
            return "" + myNumber;
        }
        return "" + masterTokens[myToken - 1][0];
    }

    /*****************************************************************************
     *Remaining methods are associated with the main block, which tests the functionality of this class   *
     *****************************************************************************/
    /*This method returns a String of all legal functions in MathToken,
    used by the main block testing method*/
    public static String functions() {
        String result = "";
        for (int i = 0; i < masterTokens.length - 1; i++) {
            result += masterTokens[i][0] + "\n";
        }
        result += masterTokens[masterTokens.length - 1][0];
        return result;
    }

    /*The main block acts as a calcualator to test the math operations contained in MathToken
    (see the welcome message for details)*/
    public static void main(String args[]) throws IOException {
        System.out.println("\n___________________________________________________________________________________\n"
                + "This is the testing mode of the math classes to be used in a larger project\n"
                + "it can evaluate just about anything that can be expressed by one argument functions\n"
                + "and algebraic operators (no summations, power series, or calculus)\n"
                + '"' + "exit" + '"' + " to end testing\n"
                + '"' + "functions" + '"' + " to display a list of legal functions and operators.\n"
                + "___________________________________________________________________________________\n");
        BufferedReader keyBoard = new BufferedReader(new InputStreamReader(System.in));
        Equation equ;
        ComplexVarList variables = new ComplexVarList();
        variables.fillStandard();
        complex n = new complex();
        variables.add('a', n);
        variables.add('g', new complex((9.80655f)));
        variables.add('c', new complex(299792458));
        System.out.println("Legal variables: \n" + variables);
        System.out.println("(a is the previous answer)");
        String in;
        while (true) {
            System.out.print(">");
            try {
                in = (keyBoard.readLine()).toLowerCase();
                if (in.equals("exit")) {
                    System.exit(0);
                } else if (in.equals("functions")) {
                    for (int i = 0; i < masterTokens.length - 1; i++) {
                        System.out.print(masterTokens[i][0] + "\t");
                    }
                    System.out.println(masterTokens[masterTokens.length - 1][0]);
                } else {
                    equ = toEquation(in);
                    System.out.print(equ + "=");
                    n = equ.evaluate(variables);
                    System.out.println(n);
                    variables.setVal('a', n);
                }
            } catch (Exception e) {
                System.out.println("[bad command]");
                e.printStackTrace();
            }
        }
    }
    /*The following massive array contains all the legal text representations
    of all functions handled in MathToken*/
    public final static String[][] masterTokens = {
        /*1*/{"+", "plus", "and"},
        /*2*/ {"-", "minus"},
        /*3*/ {"*", "times", "multipliedby"},
        /*4*/ {"/", "over", "devidedby"},
        /*5*/ {"^", "tothe", "'", "**"},
        /*6*/ {"%", "mod", "modulo"},
        /*7*/ {"(", "{", "["},
        /*8*/ {")", "}", "]"},
        /*9*/ {"!", "fact", "factorial"},
        /*10*/ {"sqrt", "sqr"},
        /*11*/ {"_", "negetive"},
        /*12*/ {"ln", "naturallog", "naturalog"},
        /*13*/ {"round", "rnd"},
        /*14*/ {"rpart", "realpart", "real"},
        /*15*/ {"ipart", "imagpart", "imag", "imagenary", "imagenarypart"},
        /*16*/ {"abs", "absolute", "absolutevalue"},
        /*17*/ {"int", "integer", "intof", "integerof", "integerpart",
            "integerpartof"},
        /*18*/ {"sign", "signum", "sgn"},
        /*19*/ {"arg", "Complexarg", "argument", "Complexargument"},
        /*20*/ {"conj", "conjugate", "Complexconjugate", "Complexconj"},
        /*21*/ {"sin", "sine"},
        /*22*/ {"cos", "cosin", "cosine"},
        /*23*/ {"tan", "tangent"},
        /*24*/ {"csc", "cosecant", "csec", "cosec"},
        /*25*/ {"sec", "secant"},
        /*26*/ {"cot", "cotangent", "cotan"},
        /*27*/ {"asin", "arcsin", "asine", "arcsine", "inversesin", "inversesine"
        },
        /*28*/ {"acos", "arccos", "acosine", "arccosine", "acosin", "arccosin",
            "arcosine", "arcosin", "arcos", "inversecos", "inversecosine",
            "inversecosin"
        },
        /*29*/ {"atan", "arctan", "atangent", "arctangent", "inversetangent",
            "inversetan"
        },
        /*30*/ {"acsc", "acosecant", "acsec", "acosec", "arcsc", "arcosecant",
            "arcosec", "arccsc", "arccosecant", "arccsec", "arccosec", "inversecsc",
            "inversecosecant", "inversecsec", "inversecosec"
        },
        /*31*/ {"asec", "asecant", "arcsec", "arcsecant", "inversesec",
            "inversesecant"
        },
        /*32*/ {"acot", "acotangent", "acotan", "arccot", "arccotangent", "arccotan",
            "inversecot", "inversecotangent", "inversecotan"
        },
        /*33*/ {"sinh", "sh", "sineh", "hyperbolicsine", "hyperbolicsin", "hsin",
            "hsine"
        },
        /*34*/ {"cosh", "cosineh", "cosinh", "hyperboliccos", "hcos", "hcosin",
            "hcosine",
            "hyperboliccosine", "hyperboliccosin", "hyperbolicos",
            "hyperbolicosine", "hyperbolicosin"
        },
        /*35*/ {"tanh", "hyperbolictangent", "htan", "hyperbolictan", "htangent",
            "tangenth"
        },
        /*36*/ {"csch", "hyperbolicsc", "hyperbolicosecant", "hyperbolicosec",
            "hyperboliccsc", "hyperboliccosecant", "hyperboliccsec",
            "hyperboliccosec", "hcsc", "hcosecant", "hcsec", "hcosec", "cosecanth",
            "csech", "cosech"
        },
        /*37*/ {"sech", "hyperbolicsec", "hyperbolicsecant", "hsec", "hsecant",
            "secanth"
        },
        /*38*/ {"coth", "hyperbolicot", "hyperbolicotangent", "hyperbolicotan",
            "hyperboliccot", "hyperboliccotangent", "hyperboliccotan", "hcot",
            "hcotangent", "hcotan", "cotangenth", "cotanh"
        },
        /*39*/ {"asinh", "asineh", "ahsin", "archsin", "ahsine", "archsine", "arcsinh",
            "arcsineh", "ahyperbolicsine", "ahyperbolicsin", "archyperbolicsine",
            "archyperbolicsin", "inversesinh", "inversesineh",
            "inversehyperbolicsine", "inversehyperbolicsin", "inversehsin",
            "inversehsine"
        },
        /*40*/ {"acosh", "arcosh", "arcosineh", "arcosinh", "arccosh", "arccosineh",
            "arccosinh", "archyperboliccos", "archcos", "archcosin", "archcosine",
            "archyperboliccosine", "archyperboliccosin", "archyperbolicos",
            "archyperbolicosine", "archyperbolicosin", "acosineh", "acosinh",
            "ahyperboliccos", "ahcos", "ahcosin", "ahcosine", "ahyperboliccosine",
            "ahyperboliccosin", "ahyperbolicos", "ahyperbolicosine",
            "ahyperbolicosin", "inversecosh", "inversecosineh", "inversecosinh",
            "inversehyperboliccos", "inversehcos", "inversehcosin",
            "inversehcosine", "inversehyperboliccosine", "inversehyperboliccosin",
            "inversehyperbolicos", "inversehyperbolicosine",
            "inversehyperbolicosin"
        },
        /*41*/ {"atanh", "ahyperbolictangent", "ahtan", "ahyperbolictan",
            "ahtangent", "atangenth", "arctanh", "archyperbolictangent", "archtan",
            "archyperbolictan", "archtangent", "arctangenth", "inversetanh",
            "inversehyperbolictangent", "inversehtan", "inversehyperbolictan",
            "inversehtangent", "inversetangenth"
        },
        /*42*/ {"acsch", "ahyperbolicsc", "ahyperbolicosecant", "ahyperbolicosec",
            "ahyperboliccsc", "ahyperboliccosecant", "ahyperboliccsec",
            "ahyperboliccosec", "ahcsc", "ahcosecant", "ahcsec", "ahcosec",
            "acosecanth", "acsech", "acosech", "arcsch", "arcosecanth", "arcosech",
            "archyperbolicsc", "archyperbolicosecant", "archyperbolicosec",
            "archyperboliccsc", "archyperboliccosecant", "archyperboliccsec",
            "archyperboliccosec", "archcsc", "archcosecant", "archcsec",
            "archcosec", "arccsch", "arccosecanth", "arccsech", "arccosech",
            "inversehyperbolicsc", "inversehyperbolicosecant",
            "inversehyperbolicosec", "inversehyperboliccsc",
            "inversehyperboliccosecant", "inversehyperboliccsec",
            "inversehyperboliccosec", "inversehcsc", "inversehcosecant",
            "inversehcsec", "inversehcosec", "inversecsch",
            "inversecosecanth", "inversecsech", "inversecosech"
        },
        /*43*/ {"asech", "ahyperbolicsec", "ahyperbolicsecant", "ahsec",
            "ahsecant", "asecanth", "archyperbolicsec", "archyperbolicsecant",
            "archsec", "archsecant", "arcsech", "arcsecanth",
            "inversehyperbolicsec", "inversehyperbolicsecant", "inversehsec",
            "inversehsecant", "inversesech", "inversesecanth"
        },
        /*44*/ {"acoth", "ahyperbolicot", "ahyperbolicotangent",
            "ahyperbolcolicotan", "ahyperboliccot", "ahyperboliccotangent",
            "ahyperboliccotan", "ahcot", "ahcotangent", "ahcotan", "acotangenth",
            "acotanh", "arcoth", "arcotangenth", "arcotanh", "archyperbolicot",
            "archyperbolicotangent", "archyperbolicotan", "archyperboliccot",
            "archyperboliccotangent", "archyperboliccotan", "archcot",
            "archcotangent", "archcotan", "arccoth", "arccotangenth", "arccotanh",
            "inversehyperbolicot", "inversehyperbolicotangent",
            "inversehyperbolicotan", "inversehyperboliccot",
            "inversehyperboliccotangent", "inversehyperboliccotan", "inversehcot",
            "inversehcotangent", "inversehcotan", "inversecoth",
            "inversecotangenth", "inversecotanh"
        },
        /*45*/ {"zeta", "reimannzeta"},
        /*46*/ {"rand", "random"}
    };
}
