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
     * @param ps
     * @param in
     * @param fractal
     * @throws IOException
     */
    public static void parse(Preset ps, BufferedReader in) throws IOException {

        String line;

        while ((line = in.readLine()) != null
                && line.indexOf('}') < 0) {
            if (line.length() > 0  && line.charAt(0) != '*') {

                StringTokenizer T = new StringTokenizer(line);
                if (T.countTokens() >= 2) {

                    String var = T.nextToken();
                    String val = T.nextToken().toLowerCase();

                    if (var.equals("fractal_map")) {
                        ps.fractal_map = FractalMap.makeMapStatic(val).toString();
                        System.out.println("set fractal_map to "+val);
                    }
                    else {
                        Object value = Misc.bestEffortParse(val);
                        try {
                            ps.getClass().getField(var).set(ps, value);
                            System.out.println("set "+var+" to "+value);
                        } catch (IllegalAccessException 
                                | IllegalArgumentException 
                                | NoSuchFieldException 
                                | SecurityException e) {
                            System.err.println("failed to assign " + value + " to " + var);
                            //e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
