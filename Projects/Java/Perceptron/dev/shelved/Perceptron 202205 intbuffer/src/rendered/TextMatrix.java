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
import static util.Misc.wrap;

/**
 *
 * @author mer49
 */
public class TextMatrix {
    
    /*
    This is a test of the emergentcy broadcast system. 
    Special programming to follow. 
    */
    
    // Configrable constants
    static final int COLUMNS = 80;
    static final int ROWS    = 24;
    static final int LENGTH  = ROWS*COLUMNS;
    static final int SIZE    = 18;
    static final int[] COLORS = {
        0x00ff00, 0xff0000, 0x0000ff, 0xffff00, 0x00ffff, 0xff00ff
    };
    
    //Text Editor Data
    final char[]  buf = new char[ROWS * COLUMNS];

    int     I = 0;
    public boolean on        = false;
    public boolean cursor_on = true;
    public boolean insert    = false;
    public final int W, H; 

    public TextMatrix(int W, int H) {
        this.W = W; 
        this.H = H;
        for (int i=0; i<LENGTH; i++) buf[i]=' ';
    }
    
    /** 
     * Press S for salvia mode, D for XOR mode. 
     * @param g
     */
    public void renderTextBuffer(Graphics2D g) {
        if (!on) return;
        float YOFF  = (float) H / ROWS;
        float XOFF  = (float) (.3 * W / COLUMNS);
        int   ROWS1 = ROWS ;
        Font old = g.getFont();
        Font F = new java.awt.Font(Font.MONOSPACED, Font.PLAIN, SIZE);
        g.setFont(F);
        int k = 0;
        for (int j = 0; j < ROWS; j++) {
            for (int i = 0; i < COLUMNS; i++) {
                String CHAR = "" + buf[k];
                k = (k + 1) % buf.length;
                g.setColor(new Color(COLORS[(int) (Math.random() * COLORS.length)]));
                GlyphVector G = g.getFont().createGlyphVector(g.getFontRenderContext(),CHAR);
                g.drawGlyphVector(G, 
                    (int) ((double) i * W / COLUMNS + XOFF), 
                    (int) ((double) j * H / ROWS1 + YOFF));
            }
        }
        g.setColor(new Color(COLORS[(int) (Math.random() * COLORS.length)]));
        g.drawString("" + buf[I], 
            (int) ((double) (I % COLUMNS) * W / COLUMNS + XOFF), 
            (int) ((I / COLUMNS) * H / ROWS1 + YOFF));
        g.setFont(old);
        if (cursor_on) {
            int x = (int) ((double) W * (I % COLUMNS) / COLUMNS);
            int y = (int) ((double) H * (I / COLUMNS) / ROWS);
            g.setXORMode(new Color(0xffffff));
            if (insert) g.fillRect((int) (x + XOFF), (int) (y), W / COLUMNS / 4, H / ROWS);
            else        g.fillRect((int) (x + XOFF), (int) (y), W / COLUMNS, H / ROWS);
            g.setPaintMode();
        }
        /** Consciousness is evolution on infinitely faster time scales */
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
    
    void shiftBack(int i0) {
        for (int i=i0; i<buf.length-1; i++) buf[i] = buf[i+1]; 
        buf[buf.length-1] = ' ';
    }
    
    void shiftForewards(int i0) {
        for (int i=buf.length-1; i>i0; i--) buf[i] = buf[i-1]; 
        buf[i0] = ' ';
    }
    
    void makeRoom() {
        for (int i=I; i<LENGTH-1; i++) {
            if (buf[i]==' ' && buf[i+1]==' ') {
                // There is room. 
                // Move text between here and cursor to the right. 
                for (int j=i+1; j>=I; j--) {
                    buf[j]=buf[j-1];
                }
                return;
            }
        }
    }
    
    public void append(String s) {
        s.chars().forEach((c)->{append((char)c);});
    }

    /**
     * 
     * @param c 
     */
    public void append(char c) {
        switch (c) {
            case 0: break; // null
            case 1: break; // start heading
            case 2: break; // start text
            case 3: break; // end text
            case 4: break; // end transmit
            case 5: break; // query
            case 6: break; // ack
            case 7: break; // bell
            case '\b': backspace(); break;
            // Ignore tab; this is used to switch focus
            case '\t': break; //append("    "); break;
            case '\n': down(); home(); break;
            case 0x0b: down(); break; // vertical tab
            case '\f': down(); home(); break;
            case '\r': home(); break;
            case 0x0e: break; // shift out
            case 0x0f: break; // shift in
            case 0x10: break; // datalink escape
            case 0x11: break; // device control 1
            case 0x12: break; // device control 2
            case 0x13: break; // device control 3
            case 0x14: break; // device control 4
            case 0x15: break; // negack
            case 0x16: break; // sync
            case 0x17: break; // end transmit
            case 0x18: break; // cancel
            case 0x19: break; // end medium
            case 0x1a: break; // substitute
            case 0x1b: break; // escape
            case 0x1c: break; // file sep
            case 0x1d: break; // grp sep
            case 0x1e: break; // rcd sep
            case 0x1f: break; // unit sep
            case 0x7f: delete(); break; // delete
            default: 
            if (insert) makeRoom();
            buf[I] = c;
            I = wrap(I+1,buf.length);
        }
    }

    public void delete() {
        if (insert) {
            // BUG
            // We end up one square to the right
            // We end up deleteing an extra character
            // It's almost as if it hears 'Type Space'
            shiftBack(I);
        } else {
            buf[I]=' '; 
            I = wrap(I+1,buf.length);
        }
    }
    
    public void backspace() {
        if (insert) {
            left();
            delete();
        } else {
            I=wrap(I-1,buf.length);
            buf[I]=' ';
        }
    }
    
    boolean lineEmpty() {
        int i = (I/COLUMNS)*COLUMNS;
        for (int j=0; j<COLUMNS; j++) if (buf[i+j]!=' ') return false;
        return true;
    }
    
    int lineStarts() {
        int i = (I/COLUMNS)*COLUMNS;
        for (int j=0; j<COLUMNS; j++) if (buf[i+j]!=' ') return j;
        return -1;
    }
    
    int lineEnds() {
        int i = (I/COLUMNS)*COLUMNS + COLUMNS-1;
        for (int j=0; j<COLUMNS; j++) if (buf[i-j]!=' ') return COLUMNS-j;
        return -1;
    }

    public void home() {
        int start = lineStarts();
        if (start>0 && start < wrap(I,COLUMNS)) {
            // Go to start of text
            I = (I/COLUMNS)*COLUMNS + start;    
        } else {
            // Go to beginning
            I=(I/COLUMNS)*COLUMNS;
        }
    }
    
    public void end() {
        int ends = lineEnds();
        if (ends>0 && wrap(I,COLUMNS)<ends) {
            // Go to end of text
            I = (I/COLUMNS)*COLUMNS + ends;    
        } else {
            // Go to end of lne
            I=(I/COLUMNS)*COLUMNS+COLUMNS-1;
        }
    } 
    
    public void prevWord() {
        int starts = lineStarts();
        if (starts<0) return;
        int start = (I/COLUMNS)*COLUMNS+starts-1;
        while (buf[I]!=' ' && I>=start) left();
        while (buf[I]==' ' && I>=start) left();
    }
    
    public void nextWord() {
        int ends = lineEnds();
        if (ends<0) return;
        int stop = (I/COLUMNS)*COLUMNS+ends;
        while (buf[I]!=' ' && I<=stop) right();
        while (buf[I]==' ' && I<=stop) right();
    }
    
    public void    clear()        {for (int i=0; i<buf.length; i++) buf[i]=' ';}
    public void    left()         {I=(I+buf.length-1) % buf.length;}
    public void    right()        {I=(I+1) % buf.length;}
    public void    up()           {I=(I+buf.length-COLUMNS) % buf.length;}
    public void    down()         {I=(I+buf.length+COLUMNS) % buf.length;}
    public void    pageUp()       {up(); home(); while (lineEmpty() && I>COLUMNS) up();}
    public void    pageDown()     {down(); home(); while (lineEmpty() && I<LENGTH-COLUMNS) down();}
    public boolean toggle()       {return on = !on;}
    public void    toggleCursor() {cursor_on = !cursor_on;}
    public void    toggleInsert() {insert = !insert;}
    public String  get()          {return new String(buf).strip().replaceAll(" +"," ");}
}