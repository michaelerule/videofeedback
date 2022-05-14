package perceptron;
import math.complex;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author mrule
 */
public class Parser {
    
    /**
     *
     * @param o
     * @param in
     * @throws IOException
     */
    public static void parse( Preset o , BufferedReader in ) throws IOException {
        
        String current_line;
        
        while ( (current_line = in.readLine() ) != null &&
                current_line.indexOf('}') < 0 ) if (
                current_line.length() > 0 &&
                current_line.charAt(0) != '*' ) {
            
            StringTokenizer token = new StringTokenizer(current_line) ;
            
            if (token.countTokens() >= 2 ) {
                
                String var = token.nextToken();
                String val = token.nextToken().toLowerCase();
                
                if ( var.equals("fractal_map") ) {
                    String mapstring = "";
                    mapstring += val;
                    while (token.hasMoreTokens()) mapstring += token.nextToken().toLowerCase();
                    o.fractal_map = RenderingKernel.makeMap( mapstring ) ;
                //    System.out.println("map : " + val );
                } 
                
                else {
                    Object value = null;
                    
                    try {
                        value = new Byte(val);
                    } catch (Exception e1) { try {
                        value = new Short(val);
                    } catch (Exception e2) { try {
                        value = new Integer(val);
                    } catch (Exception e3) { try {
                        value = new Float(val);
                    } catch (Exception e4) { try {
                        value = new Double(val);
                    } catch (Exception e5) { try {
                        value = new complex(val);
                    } catch (Exception e6) { try {
                        value = new Boolean(val.equals("on") || val.equals("true") || val.equals("t"));
                    } catch (Exception e7) {
                    //    System.err.println("NO VALID FORM MATCHING " + val);
                    }}}}}}}
                    
                    try {
                        o.getClass().getField(var).set(o,value) ;
                     //   System.out.println("set " + var + " to " + value) ;
                    } catch (Exception e) {
                    //    System.out.println("failed to assign " + value + " to " + var);
                    //    e.printStackTrace();
                    }
                }
            }}
    }
    
}
