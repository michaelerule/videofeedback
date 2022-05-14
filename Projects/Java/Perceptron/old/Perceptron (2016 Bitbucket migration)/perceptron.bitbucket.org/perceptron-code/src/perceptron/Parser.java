package perceptron;

import math.complex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 * @author Predrag Bokšić (junkerade@gmail.com)
 *         <p/>
 *         Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *         It evolves colored geometric patterns and visual images into the realm of infinite details
 *         and deepens the thought. </p>
 *         <p/>
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 */

/**
 * Parser is used to statically parse a preset file. Its relation to the world are input preset and BufferedReader.
 */
public final class Parser {

    public static void parse(Preset preset, BufferedReader in) throws IOException {
        String current_line;
        while ((current_line = in.readLine()) != null && current_line.indexOf('}') < 0) {
            if (current_line.length() > 0 && current_line.charAt(0) != '*' && !current_line.startsWith("preset")) {
                StringTokenizer token = new StringTokenizer(current_line);
                if (token.countTokens() >= 2) {
                    String var = token.nextToken();
                    String string_val = token.nextToken();
                    String val = string_val.toLowerCase();
                    if (var.equals("fractal_map")) {
                        try {
                            preset.fractal_map = FractalMap.makeMap(val);
                            System.out.println("map: " + val);
                        } catch (Exception e) {
                            System.out.println("Invalid map? : " + val);
                            e.printStackTrace();
                        }
                    } else if (var.equals("original_image_file") && string_val.startsWith("<")) {
                        try {
                            int i = current_line.indexOf("<") + 1;
                            StringBuilder s = new StringBuilder();
                            while (current_line.charAt(i) != '>') {
                                char file_name_letter = current_line.charAt(i);
                                s.append(file_name_letter);
                                i++;
                            }
                            String image_file = s.toString();
                            if (File.separatorChar == '\\') {
                                image_file = image_file.replace('/', File.separatorChar);
                                image_file = image_file.replace('\\', File.separatorChar);
                            }
                            if (File.separatorChar == '/') {
                                image_file = image_file.replace('\\', File.separatorChar);
                                image_file = image_file.replace('/', File.separatorChar);
                            }
                            System.out.println("image denoted in preset: \"" + image_file + "\"");
                            File f = new File(image_file);
                            if (!f.exists() || !f.canRead()) {
                                System.out.println("CANNOT READ THE IMAGE DENOTED IN PRESET: \"" + f.toString() + "\"");
                            } else {
                                preset.original_image_file = image_file;
                            }
                        } catch (Exception e) {
                            System.err.println("Invalid image denoted in preset.");
                            e.printStackTrace();
                        }
                        // compatibility workaround for old presets that had true and false values for convolution
                    } else if (var.equals("convolution_layer") && string_val.equals("true")) {
                        preset.convolution_layer = 1;
                    } else if (var.equals("convolution_layer") && string_val.equals("false")) {
                        preset.convolution_layer = 2;
                    } else { // try to see which type is the val and load it
                        Object value = new Object();
                        try {
                            value = new Byte(val);
                        } catch (Exception e1) {
                            try {
                                value = new Short(val);
                            } catch (Exception e2) {
                                try {
                                    value = new Integer(val);
                                } catch (Exception e3) {
                                    try {
                                        value = new Float(val);
                                    } catch (Exception e4) {
                                        try {
                                            value = new Double(val);
                                        } catch (Exception e5) {
                                            try {
                                                value = new complex(val);
                                            } catch (Exception e6) {
                                                try {
                                                    if (val.equals("true")) {
                                                        value = true;
                                                    } else if (val.equals("false")) {
                                                        value = false;
                                                    }
                                                } catch (Exception e7) {
                                                    System.err.println("NO VALID VARIABLE MATCHES: " + val);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        try { // load the value into variable (public field found in preset class)
                            preset.getClass().getField(var).set(preset, value);
                            // Print long list of loaded values.
                            System.out.println("set " + var + " to " + value);
                        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                            System.err.println("failed to assign value " + value + " to " + var);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
