/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package perceptron;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

import static java.lang.Math.*;

/**
 *
 * @author mrule
 */
public class TextLayer implements KeyListener {

    static final private Font my_font = Font.decode("Monospaced 12");

    Vector<String> scrollback = new Vector<String>();
    StringBuffer thisline = new StringBuffer("");
    int scroll_line = 0;
    int caret = 0;

    Vector<String> output = new Vector<String>();
    StringBuffer outputline = new StringBuffer("");

    public PrintStream me = new PrintStream(new OutputStream(){
        void bounceline() {
            output.add(outputline.toString());
            outputline = new StringBuffer();
        }
        @Override
        public void write(int b) throws IOException {
            char ch = (char)b;
            if (ch=='\n')
                bounceline();
            else if (Character.isDefined(ch) && !Character.isISOControl(ch))
            {
                if (outputline.length()==cols)
                    bounceline();
                outputline.append(ch);
            }
        }
    });

    Perceptron parent;

    int w = 400;
    int h = 400;
    int rows = 40;
    int cols = 80;
    int rowspacing = 15;
    int colspacing = 7;

    int [] cached_chars = new int[rowspacing*colspacing*256];
    BufferedImage [] temp;

    private Color background = new Color(0,true);

    public TextLayer(Perceptron p) {
        parent = p;
        h = p.screen_height();
        w = p.screen_width();
        rows = (int) round(floor((h-10-rowspacing)/rowspacing));
        cols = (int) round(floor(w/colspacing));
        me.println("rows : "+rows+" cols : "+cols);

        temp = new BufferedImage[256];
        for (int i=0; i<256; i++) {
            temp[i] = new BufferedImage(colspacing,rowspacing,BufferedImage.TYPE_INT_RGB);
            Graphics g = temp[i].getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0,0,temp[i].getWidth(),temp[i].getHeight());
            g.setColor(Color.WHITE);
            g.drawString(""+(char)i, i*colspacing, rowspacing);
        }

    }

    public void setBackground(int color)
    {
        this.background=new Color(color,true);
    }
    
    private void drawChar(Graphics g,char c,int x, int y)
    {
        g.drawImage(temp[c], x, y, null);
    }

    private void drawString(Graphics g, String s, int x, int y)
    {
        g.drawString(s, x, y);/*
        int k=0;
        for (char c : s.toCharArray())
            drawChar(g,c,x+(k++)*colspacing,y);*/
    }

    public void paint(Graphics g) {
        g.setFont(my_font);
        int div_y = h - round(rowspacing) - 5;
        g.setColor(background);
        g.fillRect(0,0,w,h);
        g.setColor(Color.WHITE);
        g.drawLine(0,div_y,w,div_y);
        drawString(g,thisline.toString(), 0, h-5);
        g.setXORMode(Color.BLACK);
        g.fillRect(colspacing*caret, div_y, colspacing, h-div_y);
        g.setPaintMode();
        drawString(g,outputline.toString(), 0, h-5-rowspacing-5);
        for (int i=1; i<rows; i++) {
            int y_pos = h-10-rowspacing*(rows-i);
            int nline = i+output.size()-rows;
            if (nline>=0 && nline<output.size())
                drawString(g,output.get(nline), 0, y_pos);
        }
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER || e.getKeyChar()=='\n') {
            me.print("> ");
            me.println(thisline);
            scrollback.add(thisline.toString());
            parent.eval(thisline.toString());
            thisline = new StringBuffer();
            caret = 0;
            scroll_line = scrollback.size();
        } else {
            char ch = e.getKeyChar();
            if (Character.isDefined(ch) && !Character.isISOControl(ch))
            {
                //thisline.setLength(max(thisline.length(),caret+1));
                thisline.insert(caret, e.getKeyChar());
                scroll_line = scrollback.size();
                caret++;
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE:
                if (caret>0) {
                    thisline.deleteCharAt(caret-1);
                    caret--;
                }
                break;
            case KeyEvent.VK_DELETE:
                if (thisline.length()>0) {
                    if (caret<thisline.length())
                        thisline.deleteCharAt(caret);
                }
                break;
            case KeyEvent.VK_UP:
                if (scroll_line>0) {
                    thisline = new StringBuffer(scrollback.get(--scroll_line));
                    caret=thisline.length()-1;
                    if (caret<0) caret=0;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (scroll_line>=-1 && scroll_line<scrollback.size()-1) {
                    thisline = new StringBuffer(scrollback.get(++scroll_line));
                    caret=thisline.length()-1;
                    if (caret<0) caret=0;
                }
                else if (scroll_line==scrollback.size()-1)
                {
                    scroll_line++;
                    thisline = new StringBuffer("");
                    caret=thisline.length()-1;
                    if (caret<0) caret=0;
                }
                break;
            case KeyEvent.VK_LEFT:
                if (caret>0) caret--;
                break;
            case KeyEvent.VK_RIGHT:
                if (caret<thisline.length()-1) caret++;
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
    }

}
