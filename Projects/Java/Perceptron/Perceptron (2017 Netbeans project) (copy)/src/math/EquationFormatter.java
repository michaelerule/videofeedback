package math;
import javax.swing.*;
import java.text.*;
import java.util.*;
/** Text formatter for basic equations */
public class EquationFormatter extends JFormattedTextField.AbstractFormatter {

    /**
     *
     */
    public EquationFormatter() {
        super();
    }
    
    /**
     *
     * @param text
     * @return
     * @throws ParseException
     */
    public Object stringToValue(String text)
    throws ParseException {
        try {
        return MathToken.toEquation(text);
    }catch (Exception e) {}
        return null;
    }
    
    /**
     *
     * @param value
     * @return
     * @throws ParseException
     */
    public String valueToString(Object value)
    throws ParseException {
        try {
        return value.toString();
        }catch (Exception e) {}
        return null;
    }
}
