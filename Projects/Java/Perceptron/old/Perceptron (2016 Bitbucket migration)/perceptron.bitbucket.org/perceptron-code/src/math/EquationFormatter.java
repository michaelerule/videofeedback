package math;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 *
 */

import javax.swing.*;
import java.text.ParseException;

public class EquationFormatter extends JFormattedTextField.AbstractFormatter {

    private static final long serialVersionUID = 1047230397048269871L;

    public EquationFormatter() {
        super();
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        try {
            return MathToken.toEquation(text);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        try {
            return value.toString();
        } catch (Exception e) {
        }
        return null;
    }
}
