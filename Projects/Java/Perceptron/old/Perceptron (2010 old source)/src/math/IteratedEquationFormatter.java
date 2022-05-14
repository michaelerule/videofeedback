package math;
//
//  equationFormatter.java
import javax.swing.*;
import java.text.*;
import java.util.*;

public class IteratedEquationFormatter extends JFormattedTextField.AbstractFormatter {

    public IteratedEquationFormatter() {
        super();
    }
    
    public Object stringToValue(String text)
    throws ParseException {
        try {
            Equation equ = MathToken.toEquation(text);
            if (!isValid(equ)) throw new ParseException("illegal variables in "+text,0);
            return equ;
        }
        catch (Exception e) {
            throw new ParseException("error converting "+text+" to an equation.",-1);}
    }
    
    public String valueToString(Object value)
    throws ParseException {
        try {
            return value.toString();
        }catch (Exception e) {
            throw new ParseException("error converting "+value+" to text.",-1);}
    }
    
    public static boolean isValid(Equation equ) {
        String vars  = equ.getVariables();
        String legals = "fpiezwh";
        for (int i = 0; i < vars.length(); i++)
            if (legals.indexOf(vars.charAt(i)) < 0)
                return false;
        return true;
    }
}
