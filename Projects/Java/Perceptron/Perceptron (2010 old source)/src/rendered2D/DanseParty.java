package rendered2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Random;

/**
 *
 * @author mrule
 */
public class DanseParty {

    int SIZE = 8 ;
    int MASK = ( 1 << SIZE ) - 1 ;
    int W = 1 << SIZE ;
    int H = 1 << SIZE ;
    
    // make sure SQUARESIZE divides SIZE
    int SQUARESIZE = 1 << ( SIZE - 3 ) ;
    int SQUARESTEP = SIZE / SQUARESIZE ;
      
    public BufferedImage draw ;
    public DataBuffer    data ;
    public Graphics      graph ;
    
    Random rand = new Random() ;
     
    public DanseParty() {
        draw = new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
        data = draw.getRaster().getDataBuffer() ;
        graph = draw.getGraphics();
    }
    
    public void step() {
        for ( int i = 0 ; i < H ; i += SQUARESIZE ) {
            for ( int j = 0 ; j < W ; j += SQUARESIZE ) {
                graph.setColor(Color.getHSBColor(rand.nextFloat(), 1f, rand.nextFloat()));
                graph.fillRect(i, j, SQUARESIZE, SQUARESIZE);
            }
        }
    }
}
