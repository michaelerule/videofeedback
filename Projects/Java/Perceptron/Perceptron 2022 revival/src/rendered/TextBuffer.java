/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rendered;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import perceptron.Perceptron;

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
    int     I = 0;
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
        g.drawString("" + buf[I], 
                (int) ((double) (I % COLUMNS) * screen_width / COLUMNS + XOFF), 
                (int) ((I / COLUMNS) * screen_height / ROWS1 + YOFF));
        g.setFont(old);
        if (cursor_on) {
            g.setColor(new Color(0x000000));
            int x = (int) ((double) screen_width * (I % COLUMNS) / COLUMNS);
            int y = (int) ((double) screen_height * (I / COLUMNS) / ROWS);
            g.setXORMode(new Color(0xffffff));
            g.fillRect((int) (x + XOFF), (int) (y), screen_width / COLUMNS, screen_height / ROWS);
            g.setPaintMode();
        }
        
        /** Consciousness is evolution on infinitely faster time scales */
        g.setPaintMode();
    }

    public void loadString(String string) {
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
    
    public void    clear()        {for (int i=0; i<buf.length; i++) buf[i]=' ';}
    public void    append(char c) {buf[I]=c;I=(I+1)%buf.length;}
    public void    left()         {I=(I+buf.length-1) % buf.length;}
    public void    right()        {I=(I+1) % buf.length;}
    public void    up()           {I=(I+buf.length-COLUMNS) % buf.length;}
    public void    down()         {I=(I+buf.length+COLUMNS) % buf.length;}
    public void    home()         {I=(I/COLUMNS)*COLUMNS;}
    public void    backspace()    {I=(I+buf.length-1)%buf.length;buf[I]=' ';}
    public void    scrollUp()     {}
    public void    scrollDown()   {}
    public boolean toggle()       {return on = !on;}
    public void    toggleCursor() {cursor_on = !cursor_on;}
    public String  buffer()       {return new String(buf);}
}
