package perceptron;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author mer49
 */
public class Parser {

    /**
     *
     * @param o
     * @param in
     * @param fractal
     * @throws IOException
     */
    public static void parse(Preset o, BufferedReader in) throws IOException {

        String current_line;

        while ((current_line = in.readLine()) != null
                && current_line.indexOf('}') < 0) {
            if (current_line.length() > 0  && current_line.charAt(0) != '*') {

                StringTokenizer token = new StringTokenizer(current_line);
                if (token.countTokens() >= 2) {

                    String var = token.nextToken();
                    String val = token.nextToken().toLowerCase();

                    if (var.equals("fractal_map")) o.fractal_map = FractalMap.makeMapStatic(val);
                    else {
                        Object value = Misc.hackyParse(val);
                        try {
                            o.getClass().getField(var).set(o, value);
                        } catch (IllegalAccessException 
                                | IllegalArgumentException 
                                | NoSuchFieldException 
                                | SecurityException e) {
                            System.err.println("failed to assign " + value + " to " + var);
                        }
                    }
                }
            }
        }
    }
}
