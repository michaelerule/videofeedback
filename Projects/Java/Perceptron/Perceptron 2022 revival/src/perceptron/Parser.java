package perceptron;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import math.complex;

/**
 *
 * @author mer49
 */
public class Parser {

    /**
     * @param ps
     * @param in
     * @throws IOException
     */
    public static void parse(Preset ps, BufferedReader in) throws IOException {
        String line;
        while ((line=in.readLine())!=null && line.indexOf('}')<0) {
            line = line.strip();
            if (line.length()<=0 || line.charAt(0)=='*') continue;
            String [] tokens = line.split("\\s+");
            if (tokens.length < 2) continue;
            String var = tokens[0];
            String val = tokens[1].toLowerCase();
            if (var.equals("fractal_map")) {
                ps.fractal_map = Fractal.makeMapStatic(val).toString();
                System.out.println("set fractal_map to "+val);
                continue;
            } 
            Object value = bestEffortParse(val);
            try {
                ps.getClass().getField(var).set(ps, value);
                System.out.println("set "+var+" to "+value);
            } catch (IllegalAccessException 
                    | IllegalArgumentException 
                    | NoSuchFieldException 
                    | SecurityException e) {
                System.err.println("failed to assign " + value + " to " + var);
            }
        }
    }

    /** There must be a better way to write a best-effort parser?
     * @param val
     * @return
     */
    public static Object bestEffortParse(String val) {
        try {return Byte   .parseByte  (val);}catch(NumberFormatException e){}
        try {return Short  .parseShort (val);}catch(NumberFormatException e){}
        try {return Integer.parseInt   (val);}catch(NumberFormatException e){}
        try {return Float  .parseFloat (val);}catch(NumberFormatException e){}
        try {return Double .parseDouble(val);}catch(NumberFormatException e){}
        try {return new     complex    (val);}catch(Exception e) {}
        if (trueNames .contains(val.toUpperCase())) return true;
        if (falseNames.contains(val.toUpperCase())) return false;
        return val;
    }
    public static final Set<String> trueNames  = Set.of("TRUE", "YES", "Y", "T", "SI", "ON", "1");
    public static final Set<String> falseNames = Set.of("FALSE", "NO", "N", "F", "NON", "OFF", "0");
    
    
}
