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

public class IteratedEquationFormatter extends JFormattedTextField.AbstractFormatter {

    private static final long serialVersionUID = -2433638867644992768L;

    public IteratedEquationFormatter() {
        super();
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        try {
            Equation equ = MathToken.toEquation(text);
            if (!isValid(equ)) {
                throw new ParseException("illegal variables in " + text, 0);
            }
            return equ;
        } catch (Exception e) {
            throw new ParseException("error converting " + text + " to an equation.", -1);
        }
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        try {
            return value.toString();
        } catch (Exception e) {
            throw new ParseException("error converting " + value + " to text.", -1);
        }
    }

    /**
     * OMG here we allow some special letters to the formula parser
     * in a short list...
     *
     * @param equ
     * @return
     */
    public static boolean isValid(Equation equ) {
        String vars = equ.getVariables();
        String legals = "fpiezwh";
        for (int i = 0; i < vars.length(); i++) {
            if (legals.indexOf(vars.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }
}
