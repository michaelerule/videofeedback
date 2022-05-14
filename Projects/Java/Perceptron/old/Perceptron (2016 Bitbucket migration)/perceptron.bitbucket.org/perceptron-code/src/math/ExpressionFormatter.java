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

public class ExpressionFormatter extends JFormattedTextField.AbstractFormatter {

    private static final long serialVersionUID = -9190271193184245253L;

    public ExpressionFormatter() {
        super();
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        try {
            complex number = (MathToken.toEquation(text)).evaluate(ComplexVarList.standard());
            if (number.imag == 0) {
                return (double) number.real;
            } else {
                throw new ParseException("", 0);
            }
        } catch (Exception e) {
            throw new ParseException("", 0);
        }
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        try {
            return value.toString();
        } catch (Exception e) {
            throw new ParseException("", 0);
        }
    }
}
