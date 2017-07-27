package math;
//
//  equationFormatter.java
import javax.swing.*;
import java.text.*;
import java.util.*;

public class EquationFormatter extends JFormattedTextField.AbstractFormatter {

    public EquationFormatter() {
        super();
    }
    
    public Object stringToValue(String text)
    throws ParseException {
        try {
        return MathToken.toEquation(text);
    }catch (Exception e) {}
        return null;
    }
    
    public String valueToString(Object value)
    throws ParseException {
        try {
        return value.toString();
        }catch (Exception e) {}
        return null;
    }
}
