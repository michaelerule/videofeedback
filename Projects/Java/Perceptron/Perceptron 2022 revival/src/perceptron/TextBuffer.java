/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package perceptron;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author mer49
 */
public class TextBuffer {
    
    // Configrable constants
    static final int COLUMNS = 48;
    static final int ROWS    = 18;
    static final int SIZE    = 24;
    static final int[] COLORS = {
        0x00ff00, 0xff0000, 0x0000ff, 0xffff00, 0x00ffff, 0xff00ff
    };
    
    //Text Editor Data
    char[]  buf = new char[ROWS * COLUMNS];
    int     idx = 0;
    public boolean on        = false;
    public boolean cursor_on = true;
    Perceptron perceptron;
    
    /**
     * 
     * @param p 
     */
    public TextBuffer(Perceptron p) {
        perceptron = p;
    }
    
    /** 
     * Press S for salvia mode, D for XOR mode. 
     * @param g
     */
    public void renderTextBuffer(Graphics2D g) {
        int screen_height = perceptron.screen_height;
        int screen_width  = perceptron.screen_width;
        
        if (!on) return;

        float YOFF = (float) screen_height / ROWS;
        float XOFF = (float) (.3 * screen_width / COLUMNS);

        int  ROWS1 = ROWS ;
        Font old = g.getFont();
        Font F = new java.awt.Font(Font.MONOSPACED, Font.PLAIN, SIZE);
        g.setFont(F);
        int k = 0;
        for (int j = 0; j < ROWS; j++) {
            for (int i = 0; i < COLUMNS; i++) {
                String CHAR = "" + buf[k];
                k = (k + 1) % buf.length;
                g.setColor(new Color(COLORS[(int) (Math.random() * COLORS.length)]));
                GlyphVector G = g.getFont().createGlyphVector(
                        g.getFontRenderContext(), CHAR);
                g.drawGlyphVector(G, 
                        (int) ((double) i * screen_width / COLUMNS + XOFF), 
                        (int) ((double) j * screen_height / ROWS1 + YOFF));
            }
        }
        g.setPaintMode();
        g.setColor(new Color(COLORS[(int) (Math.random() * COLORS.length)]));
        g.drawString("" + buf[idx], 
                (int) ((double) (idx % COLUMNS) * screen_width / COLUMNS + XOFF), 
                (int) ((idx / COLUMNS) * screen_height / ROWS1 + YOFF));
        g.setFont(old);
        if (cursor_on) {
            g.setColor(new Color(0x000000));
            int x = (int) ((double) screen_width * (idx % COLUMNS) / COLUMNS);
            int y = (int) ((double) screen_height * (idx / COLUMNS) / ROWS);
            g.setXORMode(new Color(0xffffff));
            g.fillRect((int) (x + XOFF), (int) (y), screen_width / COLUMNS, screen_height / ROWS);
            g.setPaintMode();
        }
        
        /** Consciousness is evolution on infinitely faster time scales */
        g.setPaintMode();
    }
    
    void clear() {
        for (int i = 0; i<buf.length; i++) buf[i] = ' ';
    }

    void loadString(String string) {
        clear();
        int bufferSourceIndex = 0;
        int read;
        try {
            BufferedReader infile = new BufferedReader(new FileReader(string));
            read = infile.read();
            while (read > 0 && bufferSourceIndex < buf.length) {
                buf[bufferSourceIndex % buf.length] = ' ';
                read = infile.read();
                bufferSourceIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /** Press CTRL and start typing... 
     */
    void append(char c) {
        buf[idx] = c;
        idx = (idx + 1) % buf.length;
    }

    void left() {
        idx = (idx + buf.length + -1) % buf.length;
    }

    void right() {
        idx = (idx + 1) % buf.length;
    }

    void up() {
        idx = (idx + buf.length - COLUMNS) % buf.length;
    }

    void down() {
        idx = (idx + buf.length + COLUMNS) % buf.length;
    }

    void backspace() {
        idx = (idx + buf.length + -1) % buf.length;
        buf[idx] = ' ';
    }

    void scrollUp() {
    }

    void scrollDown() {
    }

    /** Press S.    */
    boolean toggle() {
        return on = !on;
    }

    /** Press CTRL, type equation, press enter. */
    void toMap() {
        perceptron.fractal.setMap(new String(buf));
    }

    /** Press C to turn off/on cursors. */
    void toggleCursor() {
        cursor_on = !cursor_on;
    }
}
