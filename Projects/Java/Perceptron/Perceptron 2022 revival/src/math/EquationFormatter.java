package math;
//
//  equationFormatter.java
import javax.swing.*;
import java.text.*;

/**
 *
 * @author mer49
 */
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
    @Override
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
    @Override
    public String valueToString(Object value)
    throws ParseException {
        try {
        return value.toString();
        }catch (Exception e) {}
        return null;
    }
}
