package rendered2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Random;
import util.ColorUtil;

/**
 *
 * @author mer49
 */
public class CellModel {
    
    /**
     *
     */
    public boolean running = true ;

    public boolean antialiased = true;
    
    Random rand = new Random(1500450271L);
    
    float AREA = 80;
    int SMOOTH_WALK = 3;
    static int TRAILS = 17;
    int NUM_ID = 0;
    float growthRate = .01f;
    float moveRate = .1f;
    float searchRate = 5;
    long iteration;
    int W = 700, H = 700;
    int Wmask = W - 1, Hmask = H - 1;
    
    Environment[][] environment;
    ArrayList cells;

    /**
     *
     */
    public BufferedImage display;

    /**
     *
     */
    public DataBuffer display_data;

    /**
     *
     */
    public Graphics display_graphics;
    
    float R;

    /**
     *
     */
    public int renderer = 0 ;

    /**
     *
     */
    public boolean colored ;
    
    /**
     *
     * @param n
     */
    public void setRenderer( int n ) {
        renderer = Math.abs(n) % drawers.length ;
    }

    /**
     *
     * @return
     */
    public boolean isAntiAliased() {
        return antialiased;
    }

    /**
     *
     * @param antialias
     */
    public void setAntiAliased(boolean antialias) {
        antialiased = antialias;
        resetGraphics();
    }

    /**
     *
     * @param f
     */
    public void setArea(float f) {
        AREA = f;
    }

    /**
     *
     * @param pow
     */
    public void setGrowth(float pow) {
        growthRate = pow;
    }

    /**
     *
     * @param f
     */
    public void setRate(float f) {
        moveRate = f;
    }

    /**
     *
     * @param f
     */
    public void setSigma(float f) {
        searchRate = f;
    }

    /**
     *
     */
    public class Environment {
        int ID;
    }

    /**
     *
     */
    public class Cell {
        int ID;
        float HUE;
        int color;
        short X;
        short Y;

        /** */
        protected float[] rx = new float[SMOOTH_WALK],

        /** */
        ry = new float[SMOOTH_WALK];

        /** */
        protected float[] dx = new float[TRAILS + 1],

        /** */
        dy = new float[TRAILS +  1];
        float   size;
        boolean dividing;
        Image   image;
        
        
        void setHue(float h) {
            HUE = h;
            color = Color.HSBtoRGB(h, 1.f, 1.f);
        }
        
        /** It was complaining about "set hue" in constructor. */
        private Cell(float h) {
            HUE = h;
            color = Color.HSBtoRGB(h, 1.f, 1.f);
        }

        /**
         *
         * @param nx
         * @param ny
         * @param h
         * @param s
         */
        public Cell(float nx, float ny, float h, float s) {
            this(h);
            for (int i = 0; i <= TRAILS; i++) {
                dx[i] = nx;
                dy[i] = ny;
            }
            for (int i = 0; i < SMOOTH_WALK; i++) {
                rx[i] = nx;
                ry[i] = ny;
            }
            X = (short) nx;
            Y = (short) ny;
            bound();
            size = s;
            ID = NUM_ID++;
            if (NUM_ID == 0) System.out.println("WARNING: Cell ID overflow");
            dividing = false;
            image = trailer;
        }

        /**
         *
         */
        public final void bound() {
            for (int i = 0; i < SMOOTH_WALK; i++) {
                if (rx[i] >= W) rx[i] = W - 1;
                else if (rx[i] < 0) rx[i] = 0;
                if (ry[i] >= H) ry[i] = H - 1;
                else if (ry[i] < 0) ry[i] = 0;
            }
            for (int i = 0; i <= TRAILS; i++) {
                if (dx[i] >= W) dx[i] = W - 1;
                else if (dx[i] < 0) dx[i] = 0;
                if (dy[i] >= H) dy[i] = H - 1;
                else if (dy[i] < 0) dy[i] = 0;
            }
        }

        /**
         *
         */
        public void grow() {
            size += growthRate;
            if (size > 2) dividing = true;
        }

        /**
         *
         */
        @SuppressWarnings("unchecked")
        public void advance_location() {
            if (!dividing) {
                float rate = searchRate * (2 - size);
                float Dx = rx[0] + (float) (rand.nextGaussian() * rate);
                float Dy = ry[0] + (float) (rand.nextGaussian() * rate);
                float x = (Dx - .5f * W);
                float y = (Dy - .5f * H);
                float r = x * x + y * y;
                if (r >= R) {
                    double delta = Math.sqrt(R / r);
                    x *= delta;
                    y *= delta;
                    Dx = x + .5f * W;
                    Dy = y + .5f * H;
                }
                /*
                if (Dx >= W) Dx = W - 1 ;
                else if (Dx < 0) Dx = 0 ;
                if (Dy >= H) Dy = H - 1 ;
                else if (Dy < 0) Dy = 0 ;*/
                rx[0] = Dx;
                ry[0] = Dy;
            }

            //EXPONENTIALLY DAMPED RANDOM WALK
            int i = 1;
            float min_length = (float) (4 * AREA / (Math.PI * TRAILS * TRAILS));
            while (i < rx.length) {
                rx[i] += (rx[i - 1] - rx[i]) * moveRate;
                ry[i] += (ry[i - 1] - ry[i]) * moveRate;
                i++;
            }
            dx[0] += (rx[rx.length - 1] - dx[0]) * moveRate;
            dy[0] += (ry[rx.length - 1] - dy[0]) * moveRate;
            i = 1;
            while (i < dx.length) {
                float deltax = dx[i - 1] - dx[i];
                float deltay = dy[i - 1] - dy[i];
                float r = deltax * deltax + deltay * deltay;
                if (r > min_length) {
                    dx[i] += deltax * moveRate;
                    dy[i] += deltay * moveRate;
                }
                i++;
            }

            if (dividing) {
                double deltax = dx[TRAILS] - dx[0];
                double deltay = dy[TRAILS] - dy[0];
                if (deltax * deltax + deltay * deltay < 2 * AREA) {
                    float nsize = (float) (size * .5 + .1 * rand.nextGaussian());
                    size -= nsize;
                    cells.add(new Cell(dx[TRAILS], dy[TRAILS], HUE + .05553643f,
                      nsize));
                    setHue(HUE - .0322560f);
                    dividing = false;
                    image = trailer;
                }
            }

            X = (short) dx[TRAILS];
            Y = (short) dy[TRAILS];
        }
    }

    /**
     *
     * @param w
     * @param h
     */
    public CellModel(int w, int h) {
        W = w;
        H = h;
        R = (float) Math.pow(Math.min(w, h) / 2, 2) - 10;
        environment = new Environment[H][W];
        cells = new ArrayList();
        display = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        display_data = display.getRaster().getDataBuffer();

        resetAll();
    }

    /**
     *
     */
    public final void resetAll() {
        resetGraphics();
        resetEnvironment();
        resetCellArray();
        resetCounter();
        resetImage();
        rand = new Random(1500450271L);
    }

    /**
     *
     */
    public void resetEnvironment() {
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                environment[y][x] = new Environment();
    }

    /**
     *
     */
    public void resetCellArray() {
        cells.clear();
        this.NUM_ID = 0;
    }

    /**
     *
     */
    public void resetCounter() {
        iteration = 0;
    }

    /**
     *
     */
    public void resetGraphics() {
        if (antialiased) display_graphics = ColorUtil.fancy(display.createGraphics());
        else display_graphics = display.getGraphics();
    }

    /**
     *
     * @param n
     */
    public void seedUniformRandom(int n) {
        for (int i = 0; i < n; i++)
            addCell((float) rand.nextFloat() * W, (float) rand.nextFloat() * H);
    }

    /**
     *
     * @param x
     * @param y
     */
    @SuppressWarnings("unchecked")
    public void addCell(float x, float y) {
        Cell C = new Cell(x, y, (float) (Math.random()), 1);
        cells.add(C);
        drawers[renderer].draw( C , colored ? C.color : 0x0 );
    }

    /**
     *
     * @param frames
     */
    public void iterate(int frames) {
        for (int i = 0; i < frames; i++) {
            updatePopulation();
            updateEnvironment();
            iteration++;
        }
    }

    /**
     *
     */
    public void updatePopulation() {
        int Size = cells.size();
        if (Size > 500) {
            cells.clear();
            this.seedUniformRandom(1);
        //for ( int i = 0 ; i < 495 ; i++ ) {
        //    cells.remove((int)(Math.random()*cells.size()));
        //}
        }
        for (int i = 0; i < Size; i++) {
            Cell C = (Cell) cells.get(i);
            C.grow();
            C.advance_location();
        }
    }

    /**
     *
     */
    public void updateEnvironment() {
    }

    /**
     *
     */
    public void updateRegions() {
    }

    /**
     *
     */
    public void updateImage() {
        int Size = cells.size();
        for (int i = 0; i < Size; i++) {
            Cell C = (Cell) cells.get(i);
            drawers[renderer].draw( C , colored ? C.color : 0x0 );
        }
    }

    /**
     *
     */
    public static BufferedImage trailer = trailer();

    /**
     *
     * @return
     */
    public static BufferedImage trailer() {
        BufferedImage b = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.setColor(Color.BLACK);
        g.fillOval(1, 1, 8, 8);
        return b;
    }
    
    //various rendering functions for the cells

    /**
     *
     */
    public abstract class CellDrawer {
        String name;

        /**
         *
         * @param s
         */
        public CellDrawer(String s) {
            name = s;
        }

        /**
         *
         * @param c
         * @param color
         */
        public abstract void draw(Cell c, int color);

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     *
     */
    public CellDrawer[] drawers = {
        new CellDrawer("Polygon") {
            @Override
            public void draw(Cell c, int color) {
                float v = 0;
                for (int i = 0; i < TRAILS; i++) {
                    float W = (float) i / TRAILS;// Math.pow(.8,10 - 10. * i / TRAILS);
                    W = W * (1 - W);
                    float x = c.dx[i + 1] - c.dx[i];
                    float y = c.dy[i + 1] - c.dy[i];
                    float l = (float) Math.sqrt(x * x + y * y);
                    v += W * l;
                }
                float volume = AREA / v;
                if (volume > AREA / 5) volume = AREA / 5;
                int R = (int) Math.sqrt(AREA);
                //if (v > 0 && volume < AREA) { 
                int POINTS = TRAILS * 2;
                int[] Px = new int[1 + POINTS];
                int[] Py = new int[1 + POINTS];
                for (int i = 0; i < TRAILS; i++) {
                    float W = (float) i / TRAILS;
                    //float W = (float) Math.pow(.8,10 - 10. * i / TRAILS);
                    W = volume * W * (1 - W);
                    if (W > R) W = R;
                    float xo = c.dx[i];
                    float yo = c.dy[i];
                    int j = i + 1;
                    float xf = c.dx[j];
                    float yf = c.dy[j];
                    float x = (xf - xo);
                    float y = (yf - yo);
                    float r = x * x + y * y;
                    if (r <= 0.f) {
                        x = y = 0;
                    } else {
                        W *= Math.pow(r, -.5);
                        x *= W;
                        y *= W;
                    }
                    Px[i] = (int) (y + xo);
                    Py[i] = (int) (-x + yo);
                    Px[POINTS - i] = (int) (-y + xo);
                    Py[POINTS - i] = (int) (x + yo);
                }
                Px[TRAILS] = (int) c.dx[TRAILS];
                Py[TRAILS] = (int) c.dy[TRAILS];
                //display_graphics.setColor(new Color(c.color));
                //display_graphics.setColor(Color.BLACK);
                display_graphics.setColor( new Color( ~color ));
                display_graphics.fillPolygon(Px, Py, POINTS + 1);
                //display_graphics.setColor(Color.WHITE);
                display_graphics.setColor(Color.BLACK);
                //display_graphics.setColor(new Color(c.color));
                display_graphics.drawPolygon(Px, Py, POINTS + 1);
            }
        },
        new CellDrawer("Line") {
            @Override
            public void draw(Cell c, int color) {
                int X1 = (int) c.dx[0];
                int Y1 = (int) c.dy[0];
                display_graphics.setColor( new Color( color ));
                for ( int i = 1 ; i < TRAILS ; i ++ ) {
                    int X = (int) c.dx[i];
                    int Y = (int) c.dy[i];
                    display_graphics.drawLine( X , Y , X1 , Y1 );
                    X1 = X ; Y1 = Y ;
                }
            }
        },
        new CellDrawer("Dot") {
            @Override
            public void draw(Cell c, int color) {
                int X = (int) c.X;
                int Y = (int) c.Y;
                if ((X | Y | W - X - 2 | H - Y - 2) >= 0) {
                    int i = X + Y * W;
                    display_data.setElem(i, color);
                    display_data.setElem(i + 1, color);
                    display_data.setElem(i + W, color);
                    display_data.setElem(i + W + 1, color);
                }
            }
        },
        new CellDrawer("Circles") {
            @Override
            public void draw(Cell c, int color) {
                int X = (int) (c.X - 5);
                int Y = (int) (c.Y - 5);
                display_graphics.drawImage(trailer, X, Y, null);
                display_graphics.setXORMode(new Color(color));
                display_graphics.drawImage(trailer, X, Y, null);
                display_graphics.setPaintMode();
            }
        },
        new CellDrawer("Circles") {
            @Override
            public void draw(Cell c, int color) {
                int X = (int) (c.X - 5);
                int Y = (int) (c.Y - 5);
                display_graphics.setColor( new Color( color ));
                display_graphics.fillOval( X , Y , 10 , 10 );
            }
        },
        new CellDrawer("Points") {
            @Override
            public void draw(Cell c, int color) {
                int X = (int) c.X;
                int Y = (int) c.Y;
                if ((X | Y | W - X - 1 | H - Y - 1) >= 0)
                    display_data.setElem(X + W * Y, color);
            }
        }
    };
}
