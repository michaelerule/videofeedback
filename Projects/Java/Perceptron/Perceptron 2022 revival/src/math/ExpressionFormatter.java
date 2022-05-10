package math;
//
//  ExpressionFormatter.java

import javax.swing.*;
import java.text.*;

/**
 *
 * @author mer49
 */
public class ExpressionFormatter extends JFormattedTextField.AbstractFormatter {
    
    /**
     *
     */
    public ExpressionFormatter() {
        super();}
    
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
            complex number = (MathToken.toEquation(text)).eval(ComplexContex.standard());
            if (number.imag == 0)
                return new Double(number.real);
            else throw new ParseException("",0);
        }catch (ParseException e) {
            throw new ParseException("",0);}}
    
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
        }catch (Exception e) {
            throw new ParseException("",0);}}
}
