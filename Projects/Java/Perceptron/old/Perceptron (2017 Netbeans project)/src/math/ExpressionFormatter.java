package math;
import javax.swing.*;
import java.text.*;
import java.util.*;
/** Text formatter for real-valued expressions
<p>
This text formatter rejects expressions containing variables or complex numbers
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
    public Object stringToValue(String text)
    throws ParseException {
        try {
            complex number = (MathToken.toEquation(text)).evaluate(ComplexVarList.standard());
            if (number.imag == 0)
                return new Double(number.real);
            else throw new ParseException("",0);
        }catch (Exception e) {
            throw new ParseException("",0);}}
    
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
        }catch (Exception e) {
            throw new ParseException("",0);}}
}
