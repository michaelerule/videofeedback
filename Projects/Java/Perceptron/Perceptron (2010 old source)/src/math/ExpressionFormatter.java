package math;
//
//  ExpressionFormatter.java

import math.ComplexVarList;
import javax.swing.*;
import java.text.*;
import java.util.*;

public class ExpressionFormatter extends JFormattedTextField.AbstractFormatter {
    
    public ExpressionFormatter() {
        super();}
    
    public Object stringToValue(String text)
    throws ParseException {
        try {
            complex number = (MathToken.toEquation(text)).evaluate(ComplexVarList.standard());
            if (number.imag == 0)
                return new Double(number.real);
            else throw new ParseException("",0);
        }catch (Exception e) {
            throw new ParseException("",0);}}
    
    public String valueToString(Object value)
    throws ParseException {
        try {
            return value.toString();
        }catch (Exception e) {
            throw new ParseException("",0);}}
}
