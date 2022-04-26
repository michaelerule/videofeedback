package rendered2D;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
/*
 * GlyphLibrary.java
 *
 * Created on January 13, 2008, 3:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Michael Rule
 */
public class GlyphLibrary
{
    
    GlyphVector [] glyphs ;
    
    /** Creates a new instance of GlyphLibrary
     * @param G
     * @param SIZE */
    public GlyphLibrary( Graphics2D G , float SIZE )
    {
        ArrayList<GlyphVector> aglyphs = new ArrayList<>();
        
        FontRenderContext frc = G.getFontRenderContext() ;
        
        for ( Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts() )
        {
            System.out.println("font : " + f );
            f = f.deriveFont( SIZE );
            
            int N = f.getNumGlyphs() ;
            for ( int i = 0 ; i < N ; i ++ ) aglyphs.add( f.createGlyphVector( frc , new int[]{i}) );
        }
        glyphs = new GlyphVector[ aglyphs.size() ] ;
        aglyphs.toArray( glyphs ) ;
    }
    
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts() ;
    
    AffineTransform[] transform =
    {
        new AffineTransform() ,
        AffineTransform.getRotateInstance( Math.PI * .5 ),
        AffineTransform.getRotateInstance( Math.PI ),
        AffineTransform.getRotateInstance( - Math.PI * .5 ),
    };
    
    /**
     *
     * @param g
     * @param x
     * @param y
     * @param size
     */
    public void drawRandom( Graphics2D g , int x , int y , float size )
    {
        GlyphVector G = glyphs[(int)(Math.random()*glyphs.length)] ;
        G.setGlyphTransform( 0 , AffineTransform.getRotateInstance( Math.random()*2*Math.PI )) ;//transform[(int)(4*Math.random())]) ;
        g.drawGlyphVector( G , x , y );
    }
    
}
