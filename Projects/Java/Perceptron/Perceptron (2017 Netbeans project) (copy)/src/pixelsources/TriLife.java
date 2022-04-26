package pixelsources;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

import static java.lang.Math.* ;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import perceptron.Perceptron;
import image.DoubleBuffer;
/**
 *
 * @author mrule
 */
public class TriLife extends JPanel implements PixelSource
{
    static final float HALF_PI = (float)( PI * .5 ) ;
    static final float TWO_THIRDS_PI = (float)( PI * 2 / 3. ) ;
    static final float RADIUS = 4f ;
    
    static final float X_SCAN = .5f * ( float )( RADIUS / tan( PI / 6. ));
    static final float Y_SCAN = ( float )( RADIUS + sin( PI / 6. ) * RADIUS );
    
    static final int DEAD =  0 ;
    static final int STAY =  2 ;
    static final int LIVE =  1 ;
    static final int NULL =  4 ;
    
    static final int W = 100 ;
    static final int H = 50 ;
      
    static final int [] color = { 0x700000 , 0x00FF00 , 0xFFFF00 , 0xFFFFFF } ;
    
    static final int [] rule  = { DEAD , DEAD , DEAD , DEAD , DEAD , DEAD , STAY , LIVE , LIVE , LIVE } ;
    
    static final float [] X_POINT = new float[3] ;
    static final float [] Y_POINT = new float[3] ;
    
    static final int [][] neighborhood =
    {
        { -1,  1 , 2 },
        {  0,  1 , 1 },
        {  1,  1 , 2 },
        
        { -2,  0 , 2 },
        { -1,  0 , 3 },
        {  1,  0 , 3 },
        {  2,  0 , 2 },
        
        { -2, -1 , 1 },
        { -1, -1 , 2 },
        {  0, -1 , 3 },
        {  1, -1 , 2 },
        {  2, -1 , 1 }
    };
    
    static
    {
        float theta = HALF_PI ;
        float y_min = 0 ;
        for ( int i = 0 ; i < 3 ; i ++ )
        {
            X_POINT[i] = (float)( RADIUS * cos( theta ) ) ;
            Y_POINT[i] = (float)( RADIUS * sin( theta ) ) ;
            if ( Y_POINT[i] < y_min ) y_min = Y_POINT[i] ;
            theta += TWO_THIRDS_PI ;
        }
        for ( int i = 0 ; i < 3 ; i ++ )
        {
            Y_POINT[i] -= y_min ;
        }
    }
    
    static final int [] rule_lookup = new int[1<<( neighborhood.length + 1 )] ;
    
    static
    {
        init_lookup();
    }
    
    static void init_lookup()
    {
        double K = random();
        for ( int i = 0 ; i < rule_lookup.length ; i ++ )
        {
            int sum  = 0 ;
            int temp = i ;
            for ( int j = neighborhood.length - 1 ; j >= 0 ; j -- )
            {
                int [] p = neighborhood[j];
                if ( ( temp & 1 ) == 1 ) sum += p[2] ;
                temp >>= 1 ;
            }
            int code ;
            if ( sum >= rule.length ) code = DEAD ;
            else
            {
                if ( rule[sum] != STAY )
                {
                    code = rule[sum] ;
                }
                else
                {
                    if ( ( temp & 1 ) == 1 ) code = LIVE ;
                    else code = DEAD ;
                }
            }
            rule_lookup[i] = code ;
        }
    }
    
    int [] grid , grid_buffer ;
    int [] lookup ;
    
    BufferedImage sketch ;
    DataBuffer    sketch_buffer ;
    Graphics      sketch_graphics ;
   
    final int imageW ;
    final int imageH ;
    
    /**
     *
     * @param p
     */
    public TriLife()
    {
        grid = new int[H*W] ;
        grid_buffer = new int[H*W] ;
        sketch = new BufferedImage( (int)( .5f + ( W - .5f ) * X_SCAN ) - 2 , (int)( .5f + ( H - .5f ) * Y_SCAN ) , BufferedImage.TYPE_INT_RGB ) ;
        sketch_graphics = sketch.getGraphics();
        sketch_buffer = sketch.getRaster().getDataBuffer();
        
        imageW = sketch.getWidth() ;
        imageH = sketch.getHeight() ;
        
        draw_to( sketch_graphics ) ;
        
        lookup = new int[ sketch.getWidth() * sketch.getHeight() ] ;
        for ( int i = 0 ; i < lookup.length ; i ++ )
        {
            lookup[i] = 0xFFFFFF&(sketch_buffer.getElem(i));
            if ( lookup[i] < 0 || lookup[i] >= W*H ) lookup[i] = 0 ;
        }
        randomize() ;
        //scan();
        setPreferredSize( new Dimension( sketch.getWidth() , sketch.getHeight() ) ) ;
        
        addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                init_lookup();
                randomize() ;
            }
        });
    }
    
    /**
     *
     * @param g
     */
    @Override
    public void paint( Graphics g )
    {
        g.drawImage( sketch , 0 , 0 , null ) ;
    }
    
    /**
     *
     * @param g
     */
    public void draw_to( Graphics g )
    {
        boolean tri_flip = true ;
        boolean row_flip = true ;
        float y = 0 ;
        int cell_id = 0 ;
        for ( int i = 0 ; i < H ; i ++ )
        {
            float x = 0 ;
            for ( int j = 0 ; j < W ; j ++  )
            {
                g.setColor( new Color( cell_id ) ) ;
                draw_triangle( g , tri_flip == row_flip , x , y ) ;
                tri_flip = ! tri_flip ;
                x += X_SCAN ;
                cell_id ++ ;
            }
            row_flip = ! row_flip ;
            y += Y_SCAN ;
        }
    }
    
    public void scan()
    {
        //DoubleBuffer.ImageRenderContext [] image = {perceptron.buffer.output,perceptron.buffer.imagefade};
        //if ( image[1] == null || image[0] == null ) {
            for ( int i = 0 ; i < lookup.length ; i ++ )
            {
                sketch_buffer.setElem( i , color[grid[lookup[i]]] ) ;
            }
        //}
        /*
        else {
            int i = 0 ;
            for ( int y = 0 ; y < imageH ; y ++ ){
                for ( int x = 0 ; x < imageW ; x ++ ){
                    sketch_buffer.setElem( i , image[grid[lookup[i]]&1].get(x<<8, y<<8)) ;
                    i ++ ;
                }
            }
        }*/
    }
    
    private void draw_triangle( Graphics g , boolean flip , float x , float y )
    {
        int [] X = new int[3] ;
        int [] Y = new int[3] ;
        if ( flip ) for ( int i = 0 ; i < 3 ; i ++ )
        {
            X[i] = (int)( .5f + x + X_POINT[i] ) ;
            Y[i] = (int)( .5f + y + Y_POINT[i] ) ;
        }
        else for ( int i = 0 ; i < 3 ; i ++ )
        {
            X[i] = (int)( .5f + x + X_POINT[i] ) ;
            Y[i] = (int)( .5f + y + Y_SCAN - Y_POINT[i] ) ;
        }
        g.fillPolygon( X , Y , 3 ) ;
    }
    
    /**
     *
     */
    public void randomize()
    {
        for ( int i = 0 ; i < grid.length ; i ++ )
            grid[i] = random() < .1 ? LIVE : DEAD ;
    }
    
    private int next_state( int i , int j , boolean flip )
    {
        int key = grid[ i * W + j ] == LIVE ? 1 : 0 ;
        if ( ((i&1)^(j&1)) == 0 ) for ( int [] p : neighborhood )
            key = ( key << 1 ) | get( i + p[1] , j + p[0] ) ;
        else for ( int [] p : neighborhood )
            key = ( key << 1 ) | get( i - p[1] , j + p[0] ) ;
        return rule_lookup[ key ];
    }
    
    /**
     *
     */
    public void step()
    {
        boolean flip = true ;
        int index = 0 ;
        for ( int i = 0 ; i < H ; i ++ )
        {
            for ( int j = 0 ; j < W ; j ++ )
            {
                grid_buffer[index] = next_state( i , j , flip ) ;
                index ++ ;
                flip = ! flip ;
            }
        }
        int [] temp = grid ;
        grid = grid_buffer ;
        grid_buffer = temp ;
        
        //scan();
    }
    
    int get( int i , int j )
    {
        if ( i < 0 ) i += H ;
        if ( j < 0 ) j += W ;
        if ( i >= H ) i %= H ;
        if ( j >= W ) j %= W ;
        return grid[i*W + j] ;
    }
    
    void test_neighborhood( int i , int j )
    {
        grid[i*W+j] = NULL ;
        if ( ((i&1)^(j&1)) == 0 ) for ( int [] p : neighborhood )
            grid[ ( i + p[1] ) * W + j + p[0] ] = p[2] ;
        
        else for ( int [] p : neighborhood )
            grid[ ( i - p[1] ) * W +  j + p[0] ] = p[2] ;
        
    }

    public BufferedImage getSource() {
        return sketch;
    }

    public void keyPressed(KeyEvent e) {
        ;
    }
}
