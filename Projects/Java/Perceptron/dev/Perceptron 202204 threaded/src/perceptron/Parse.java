package perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import math.complex;

/**
 *
 * @author mer49
 */
public class Parse {

    /**
     * @param ps
     * @param in
     * @throws IOException
     */
    public static void parse(Settings ps, BufferedReader in) throws IOException {
        String line;
        while ((line=in.readLine())!=null && line.indexOf('}')<0) {
            line = line.strip();
            if (line.length()<=0 || line.charAt(0)=='*') continue;
            String [] tokens = line.split("\\s+");
            if (tokens.length < 2) continue;
            String var = tokens[0];
            String val = tokens[1].toLowerCase();
            if (var.equals("fractal_map")) {
                ps.fractal_map = Map.makeMapStatic(val).toString();
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
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    /** Read in the settings file.
     * @param P
     * @param settings_path
     * @param presets_path */
    @SuppressWarnings({"ConvertToStringSwitch", "unchecked"})
    public static void parseSettings(Perceptron P, String settings_path, String presets_path) 
    {
        ArrayList<Settings> presets = new ArrayList<>();
        //Create exc FileReader for reading the file
        try (BufferedReader in = new BufferedReader(new FileReader(settings_path))) {
            String thisLine;
            while ((thisLine = in.readLine()) != null) {
                if (thisLine.length() > 0 && thisLine.charAt(0) != '*') {
                    StringTokenizer token = new StringTokenizer(thisLine);
                    if (token.countTokens() >= 2) {
                        String var = token.nextToken();
                        String val = token.nextToken();
                        if (var.equals("preset")) {
                            System.out.println("parsing preset "+val+":");
                            presets.add(Settings.parse(val,in));
                        } else if (var.equals("map")) {
                            try {
                                P.maps.add(Map.makeMapStatic(val));
                                System.out.println("map : "+val);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            //parse primitive
                            Object value = Parse.bestEffortParse(val);
                            if (null != value)
                                try {
                                    P.getClass().getField(var).set(P,value);
                                } catch (NoSuchFieldException
                                        | SecurityException
                                        | IllegalArgumentException
                                        | IllegalAccessException ex) {
                                    System.err.println("I could not set "+var+" to "+val+"; parsed as "+value);
                                    Logger.getLogger(Perceptron.class.getName()).log(Level.WARNING,null,ex);
                                }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading settings file");
            Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
        }
        File f = new File(presets_path);
        List<String> fileList = asList(f.list());
        sort(fileList);
        for (String name : fileList) {
            if (name.endsWith(".state")) {
                try (BufferedReader in = new BufferedReader(new FileReader(new File(presets_path+name)))) {
                    System.out.println("parsing preset "+name+":");
                    presets.add(Settings.parse(name, in));
                } catch (FileNotFoundException ex) {
                    System.err.println("Could not find preset file "+name);
                    Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
                } catch (IOException ex) {
                    System.err.println("Error loading preset "+name);
                    Logger.getLogger(Perceptron.class.getName()).log(Level.SEVERE,null,ex);
                }
            }
        }
        P.presets = (Settings[])(presets.toArray(Settings[]::new));
    }
    
}
