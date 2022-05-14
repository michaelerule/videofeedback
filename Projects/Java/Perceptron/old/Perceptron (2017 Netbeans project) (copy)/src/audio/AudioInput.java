package audio;
/* AudioInput.java
 * Created on March 19, 2007, 12:04 PM
 *
 * This class handles retrieval of audio data from the system,
 * as well as filtering and visualisation of that data.
 */

import image.DoubleBuffer;
import perceptron.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;

/**
 * @author Michael Everett Rule
 */
public class AudioInput  {
    
    /**
     *
     */
    public int audio_line = 0 ;
    //STATIC CONSTANTS
    
    /**
     *
     */
    public static final int SAMPLE_RATE = 44100;//8000,11025,16000,22050,44100
    
    /**
     *
     */
    public static final int FREQUENCY_CUTOFF = 4;
    static int START_NOTE = - (3 * 12), END_NOTE = (2 * 12);
    /**
     *
     */
    public static final int SUBSAMPLE = 4;
    
    // The format of the sampled audio 
    static final AudioFormat format = new AudioFormat(
            SAMPLE_RATE,
            8,     //sampleSizeInBits //8,16
            1,     //channels         //1,2
            true,  //signed           //true,false
            false  //bigEndian        //true,false
            );
    
    final float constant = (float)(2 * Math.PI * SAMPLE_RATE);//dont ask
    
    //MEMBER DATA
    
    /**weather or not this class is actively responding to Audio */
    boolean active;
    
    /** a byte [] to contain incoming audio data */
    byte [] temp_byte;
    
    /** Filter method for frequency transform */
    float [][] filter;
    
    /** Frequency histogram */
    float [] histogram, histogram_buffer;
    
    /** Color derived from sound input */
    int spectrum_color, pitch_color;
    
    /** somethingorotheriforget */
    float [] frequency;
    
    /** Root out the harmonics! */
    float [][] harmonic;
    
    /** writing indecies for the filters */
    float [] index;
    
    /** The audio data line to which to listen*/
    TargetDataLine targetDataLine;
    
    /** The audio capture thread */
    CaptureThread capture;
    
    /** Weather or not audio input will even work on this system */
    boolean functional;
    
    /** The weight to give to new incoming audio data when averaging */
    /** defaults to 0.8, 0.2 */
    float weight = .05f, w1, w2;
    
    /** A volume scalar to apply to input */
    float volume = 1, d_volume_high, d_volume_low;
    
    /** for signal normalisation */
    float low_volume_average = 0, high_volume_average = 0, pitch_average = 0;
    
    float interval;
    int max_pitch;
    
    DoubleBuffer buffer;
    int image_width, image_height, half_image_height, buffer_size;
    
    //WHICH VISUALISATION TO USE
    
    VisualisationFunction current_visualisation;
    ArrayList<VisualisationFunction> visualisations;
    private final int line;
    
    //CONSTRUCTOR
    
    /** Creates a new instance of AudioInput
     * @param b
     * @param line
     */
    public AudioInput( DoubleBuffer b , int line ) {
        /*
        audio_line = line ;
        Vector<TargetDataLine> valid_lines = new Vector<TargetDataLine>();
        try {
            
            System.out.println("AUDIO MIXERS { ");
            Mixer.Info[] mixers = AudioSystem.getMixerInfo() ;
            for ( Mixer.Info i : mixers ) {
                System.out.println( "  " + i.toString() );
                
                Mixer m = AudioSystem.getMixer( i ) ;
                
                try {
                    m.open();
                    
                    Line[] lines = m.getTargetLines();
                    if ( lines.length <= 0 ) System.out.println("  { no target lines }" );
                    else {
                        System.out.println("  TargetLines {");
                        for ( Line l : lines ) {
                            System.out.println( "    " + l.getLineInfo() );
                        }
                        System.out.println("  }");
                    }
                    
                    lines = m.getSourceLines();
                    if ( lines.length <= 0 ) System.out.println("  { no source lines }" );
                    else {
                        System.out.println("  SourceLines {");
                        for ( Line l : lines ) {
                            System.out.println( "    " + l.getLineInfo() );
                        }
                        System.out.println("  }");
                    }
                    
                    TargetDataLine.Info info = new TargetDataLine.Info(DataLine.class,format);
                    if ( m.isLineSupported( info ) ) {
                        Line t = m.getLine( info );
                        System.out.println("  " + t );
                        valid_lines.add((TargetDataLine)t);
                    }
                    m.close();
                } catch( Exception e ) {
                    System.out.println("Error opening mixer");
                    e.printStackTrace();
                    if ( m.isOpen() ) m.close() ;
                }
            }
            System.out.println("}");
            
            System.out.println("AUDIO LINES { ");
            Line.Info[] all_lines = AudioSystem.getTargetLineInfo(
                    new DataLine.Info(TargetDataLine.class,format)) ;
            for ( Line.Info i : all_lines ) {
                System.out.println( "  " + i.getLineClass() + " {" );
                System.out.println( "    " + i.toString() );
                Line l = AudioSystem.getLine( i );
                System.out.println( "    " + l );
                System.out.println("  }");
            }
            System.out.println("}");
            
            
            System.out.println("AUDIO FORMATS { ");
            AudioFileFormat.Type[] formats = AudioSystem.getAudioFileTypes();
            for ( AudioFileFormat.Type i : formats ) {
                System.out.println( "  " + i.toString() );
            }
            System.out.println("}");
            
            
            
        } catch( Exception e ) {
            System.out.println("Error grabbing audio streams");
            e.printStackTrace();
        }
        */
        try {
            
            //if ( valid_lines._size() <= 0 || line < 0 ) {
                //obtain and start a new incoming sound line
                targetDataLine =
                        (TargetDataLine)AudioSystem.getLine(new DataLine.Info(TargetDataLine.class,format));
            /*} else {
                targetDataLine = valid_lines.get(audio_line%valid_lines._size());
                System.out.println("LISTENING ON " + targetDataLine );
            }
             */
            targetDataLine.open(format);
            targetDataLine.start();
            //initialise filters to process the sound
            //try to hit actual notes.
            
            int counter = 0;
            
            START_NOTE *= SUBSAMPLE;
            END_NOTE   *= SUBSAMPLE;
            index = new float[END_NOTE - START_NOTE];
            
            interval = SUBSAMPLE * 12.f;
            for (int i = START_NOTE; i < END_NOTE ; i++) {
                float size = (int)(SAMPLE_RATE / (440 * Math.pow(2, i / interval)));
                if (size > 1) {
                    index[counter] = size;
                    counter++;
                }
            }
            
            filter = new float [index.length][];
            for (int i = 0; i < counter; i++) filter[i] = new float[(int)index[i]];
            
            histogram = new float[filter.length];
            histogram_buffer = new float[filter.length];
            
            harmonic = new float[filter.length][filter.length];
            for (int i = 0; i < harmonic.length; i++) {
                for (int j = 0; j < harmonic.length; j++) {
                    float quot = (float)StrictMath.abs(2 * ((index[j] / index[i]) % 1 - .5f));
                    if (quot > .9)
                        harmonic[i][j] = quot;
                }
            }
            
            
            //OTHER INIT
            
            temp_byte = new byte[(int)index[counter-1] * 4];
            
            buffer = b;
            image_width  = buffer.output.image.getWidth();
            image_height = buffer.output.image.getHeight();
            half_image_height = image_height / 2;
            
            buffer_size = buffer.output.buffer.getSize();
            
            update_weights();
            
            visualisations = new ArrayList<VisualisationFunction>();
            visualisations.add(piano);
            visualisations.add(bars);
            visualisations.add(waveform);
            visualisations.add(none);
            visualisations.add(transform);
            current_visualisation = none;
            
            functional = true;
            active = false;
            
        } catch (Exception E) {
            functional = active = false;
        }
        this.line = line;
    }
    
    
    //MEMBER FUNCTIONS
    
    //MUTATORS
    
    /** Sets the incoming data weights
     * @param f
     */
    public void setDataWeight(float f) {
        weight = f;
        update_weights();
    }
    
    /** Sets the volume
     * @param v
     */
    public void setVolume(float v) {
        volume = v;
        update_weights();
    }
    
    /**
     *
     * @return
     */
    public int visualisation_index() {
        int _size = visualisations.size();
        if (_size <= 0) return -1 ;
        return visualisations.indexOf(current_visualisation);
    }
    
    /**
     *
     * @param n
     */
    public void increment_visualisation(int n){
        int size = visualisations.size();
        if (size <= 0) return;
        int _index = visualisations.indexOf(current_visualisation);
        if (_index < 0) {
            current_visualisation = visualisations.get(0);
        } else {
            _index = _index + n;
            if (_index < 0) _index = size - (-_index % size);
            else if (_index >= size) _index %= size;
            current_visualisation = visualisations.get(_index);
        }
    }
    
    /**
     *
     * @param i
     */
    public void set_visualisation(int i){
        int size = visualisations.size();
        if (size <= 0) return;
        int _index = visualisations.indexOf(current_visualisation);
        if (_index < 0) {
            current_visualisation = visualisations.get(0);
        } else {
            _index = i;
            if (_index < 0) _index = size - (-_index % size);
            else if (_index >= size) _index %= size;
            current_visualisation = visualisations.get(_index);
        }
    }
    
    /**
     *
     * @param n
     */
    public void increment_volume(int n){
        float v = volume + n/5.f;
        if (v < 0) v = 0;
        volume = v;
        update_weights();
    }
    
    /** Recalculates the data weighting to incorperate volume */
    void update_weights() {
        w2 = weight * volume;
        w1 = 1 - weight;
    }
    
    
    //ACTIVATION STATUS
    
    /** Turns this AudioInput on or off
     * @param a
     */
    public void set_active(boolean a) {
        if (!functional) {
            active = false;
            return;
        }
        if (active == a) return;
        if (active) stop();
        else start();
    }
    
    /** returns true if this AudioInput is active
     * @return
     */
    public boolean is_active() {
        return active;
    }
    
    /** Starts the capture thread */
    public void start() {
        if (!functional) {
            active = false;
            return;
        }
        //initialise a thread to capture the sound
        if (capture == null || !capture.running) {
            capture = new CaptureThread();
            capture.start();
        }
        active = true;
    }
    
    /** stops listening */
    public void stop() {
        capture.end();
        active = false;
    }
    
    
    //RENDERING FUNCTION
    
    /** Draws the current sound data to the given graphics */
    public void render() {
        if (!(active && ((capture != null) && capture.running))) return;
        
        compute_histogram();
        current_visualisation.render( buffer.output.graphics );
    }
    
    /**
     *
     * @param i
     */
    public void simulate_tone(int i) {
        for (int j = 0; j < histogram.length; j++) {
            //float quot = (float)StrictMath.abs(2 * ((index[j] / index[i]) % 1 - .5f));
            //quot *= quot;
            //quot = (float)((Math.exp(quot) - 1)/(Math.E - 1));//quot*quot;
            //quot= (float)Math.exp(1 - quot) - 1;
            //buffer.output.buffer.setElem((j*image_width/harmonic.length + image_width * (int)((2 - quot) * half_image_height)) % buffer_size,
            //       (quot > .9)? 0x00FF0000 : 0);
            //harmonic[i][j] = quot;
            
            float quot = (float)StrictMath.abs(2 * ((index[j] / index[i]) % 1 - .5f));
            //if (quot > .9)
            //harmonic[i][j] = quot;
            
            float temp = (float)(constant * Math.sqrt(1 / (index[i] * index[i]) + 1 / (index[j] * index[j])));
            float reso = 1;
            if (temp >= 1) {
                reso = 1 / temp;
            }
            
            //if (quot > .9)
            quot = reso;
            buffer.output.buffer.setElem((j*image_width/harmonic.length + image_width * (int)((2 - quot) * half_image_height)) % buffer_size,
                    (quot > .9)? 0x00FFFF00 : 0x00FFFFFF);
        }
    }
    float loudness = 1 ;
    /** compute the frequency histogram */
    void compute_histogram() {
        
        for (int i = 0; i < filter.length; i++) {
            frequency = filter[i];
            
            float sum = 0;
            for (int j = 0; j < frequency.length; j++)
                sum += frequency[j] * frequency[j];
            
            histogram[i] = sum / index[i];//.length;
        }
        
       /*
        for (int i = 0; i < filter.length; i++) {
            for (int j = 0; j < i; j++) {
                histogram[j] -= harmonic[i][j] * histogram[i];
                if (histogram[j] < 0) histogram[j] = 0;
            }
        }
        */
        
        float R = 0, G = 0, B = 0;
        
        float biggest = histogram[0];
        max_pitch = 0;
        float low_average = 0, high_average = 0;
        
        for (int i = 1; i < histogram.length/2; i++) {
            
            if (histogram[i] > biggest) {
                max_pitch = i;
                biggest = histogram[i];
            }
            
            float _weight = histogram[i] / low_volume_average;
            int RGB = Color.HSBtoRGB(i%interval/interval,1.f,_weight);
            R += (0x000000FF & (RGB >> 16));
            G += (0x000000FF & (RGB >>  8));
            B += (0x000000FF & RGB);
            
            low_average += histogram[i];
        }
        for (int i = histogram.length/2; i < histogram.length; i++) {
            
            if (histogram[i] > biggest) {
                max_pitch = i;
                biggest = histogram[i];
            }
            
            float _weight = histogram[i] / high_volume_average;
            int RGB = Color.HSBtoRGB(i%interval/interval,1.f,_weight);
            R += (0x000000FF & (RGB >> 16));
            G += (0x000000FF & (RGB >>  8));
            B += (0x000000FF & RGB);
            
            high_average += histogram[i];
        }
        
        d_volume_low = low_average / low_volume_average;
        d_volume_high = high_average / high_volume_average;
        
        pitch_average  = pitch_average * .99f + biggest * .01f;
        low_volume_average = low_volume_average * .99f + low_average * .01f;
        high_volume_average = high_volume_average * .99f + high_average * .01f;
        
        float normalise = (low_average + high_average) / (low_volume_average + high_volume_average) / Math.max(R,Math.max(G,B));
        R *= normalise;
        G *= normalise;
        B *= normalise;
        
        spectrum_color =
                (((int)(R * 255) & 0x000000FF) << 16) |
                (((int)(G * 255) & 0x000000FF) <<  8) |
                (((int)(B * 255) & 0x000000FF)      ) ;
        
        pitch_color = Color.HSBtoRGB((float)max_pitch / histogram.length,1.f,1.f) & 0x00FFFFFF;
        
        loudness = .999f * loudness + .001f * histogram[max_pitch] ;
    }
    
    /** Computes a color from audio input
     * @return
     */
    public int sound_color() {
        return (active)? spectrum_color : -1;
        //return (active)? pitch_color : -1;
    }
    
    /** Computes a color from audio input
     * @return
     */
    public int pitch_color() {
        return (active)? pitch_color : -1;
    }
    
    /**
     *
     * @return
     */
    public float D_volume_low() {
        return d_volume_low;
    }
    /**
     *
     * @return
     */
    public float D_volume_high() {
        return d_volume_high;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    //INNER CLASSES
    
    //AUDIO CAPTURE THREAD
    class CaptureThread extends Thread {
        
        /** Weather or not audio input is running */
        boolean running;
        
        long frame_position = 0;
        
        //CONSTRUCTOR
        
        public CaptureThread() {
            running = false;
        }
        
        //RUN METHOD
        
        /** Opens up and starts reading from the Audi data line */
        @Override
        public void run() {
            if (running || !functional) return;
            running = true;
            
            try {
                float [] filt;
                
                while(running) {
                    int new_data_length = targetDataLine.read(temp_byte,0,temp_byte.length);
                    
                    for (int j = 0; j < new_data_length; j++) {
                        
                        float new_data = w2 * temp_byte[j];
                        
                        for (int i = 0; i < filter.length; i++) {
                            filt = filter[i];
                            int ii = (int)(frame_position % index[i]);
                            filt[ii] = filt[ii] * w1 + new_data;
                        }
                        
                        frame_position++;
                    }
                    
                }
                
                running = false;
                
            } catch (Exception E) {
                E.printStackTrace();
            }
        }
        
        
        //OTHER METHODS
        
        /** Terminates this thread */
        public void end() {
            running = false;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //ABSTRACT DRAWING METHOD
    interface VisualisationFunction {
        
        /** render to the drawing image */
        public void render( Graphics output_Graphics );
        
        /** return a string identifying this function */
        public String getInfo();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //Drawing methods for various visualisations
    
    VisualisationFunction none = new VisualisationFunction() {
        /** render to the drawing image */
        public void render( Graphics output_Graphics ){
        }
        /** return a string identifying this function */
        public String getInfo(){
            return "null";
        }
    };
    
    VisualisationFunction bars = new VisualisationFunction() {
        /** render to the drawing image */
        public void render( Graphics output_Graphics ){
            output_Graphics.setColor(new Color(spectrum_color));
            
            float xscalar = (float)(image_width-1) / (filter.length-1);
            float yscalar = image_height / histogram[max_pitch];
            
            int x = 0;
            int y = image_height - (int)(histogram[0] * yscalar);
            
            for (int i = 1; i < filter.length; i++) {
                int nx = (int)(i * xscalar);
                int ny = image_height - (int)(histogram[i] * yscalar);
                output_Graphics.drawRect(x, y, nx - x, image_height - y);
                x = nx;
                y = ny;
            }
        }
        /** return a string identifying this function */
        public String getInfo(){
            return "bars";
        }
    };
    
    VisualisationFunction transform = new VisualisationFunction() {
        /** render to the drawing image */
        public void render( Graphics output_Graphics ){
            output_Graphics.setColor(new Color(spectrum_color));
            
            float xscalar = (float)(image_width-1) / (filter.length-1);
            float yscalar = image_height / histogram[max_pitch] ;
            
            int x = 0;
            int y = image_height - (int)(histogram[0] * yscalar);
            
            for (int i = 1; i < filter.length; i++) {
                int nx = (int)(i * xscalar);
                int ny = image_height - (int)(histogram[i] * yscalar);
                int tx = (nx + x)/2;
                output_Graphics.drawLine(x, y, tx, ny);
                output_Graphics.drawLine(tx, ny, nx, ny);
                x = nx;
                y = ny;
            }
        }
        /** return a string identifying this function */
        public String getInfo(){
            return "transform";
        }
    };
    
    static int[] white = {0,2,3,5,7,8,10}, black = {1,4,6,9,11};
    
    VisualisationFunction piano = new VisualisationFunction() {
        /** render to the drawing image */
        public void render( Graphics output_Graphics ){
            
            float xscalar   = (float)(image_width-1) / ((filter.length  / SUBSAMPLE) * 7 / 12.f);
            int   keywidth  = (int)Math.max(2,xscalar - 2);
            int   keyheight = 6 * keywidth;
            
            int note = 0;
            int octave = 0;
            int key = octave * 12 + black[note];
            int counter = 1;
            float color_scalar = 255 / histogram[max_pitch];/// 77.f * 256 / histogram[max_pitch];;
            int px = 0;
            
            int END = filter.length / SUBSAMPLE;
            
            while (key < END) {
                
                float sum = histogram[key * SUBSAMPLE];
                for (int i = 1; i < SUBSAMPLE; i++) sum += histogram[key * SUBSAMPLE - i];
                
                int color = 0x000000FF & (int)(255 - Math.min(255, sum * color_scalar));
                color = (color << 16) | (color << 8) | color;
                
                output_Graphics.setColor(new Color(color));
                int x = (int)(counter * xscalar);
                output_Graphics.fillRect(px,0,(x-px)-1,keyheight);
                px = x;
                note ++;
                if (note >= white.length) {
                    note = 0;
                    octave ++;
                }
                key = octave * 12 + white[note];
                counter++;
            }
            
            xscalar  = (float)(image_width - 1) / (filter.length / SUBSAMPLE);
            keywidth =  keywidth * 7 / 8;
            keyheight = keyheight * 3 / 5;
            int offset = keywidth / 3;
            
            note = 0;
            octave = 0;
            key = octave * 12 + black[note];
            while (key < END) {
                
                float sum = histogram[key * SUBSAMPLE];
                for (int i = 1; i < SUBSAMPLE; i++) sum += histogram[key * SUBSAMPLE - i];
                
                int color = 0x000000FF & (int)(Math.min(255, sum * color_scalar));
                color = (color << 16) | (color << 8) | color;
                
                output_Graphics.setColor(new Color(color));
                output_Graphics.fillRect((int)(key * xscalar)+offset,0,keywidth,keyheight);
                
                note ++;
                if (note >= black.length) {
                    note = 0;
                    octave ++;
                }
                key = octave * 12 + black[note];
            }
            
        }
        /** return a string identifying this function */
        public String getInfo(){
            return "piano";
        }
    };
    
    VisualisationFunction waveform = new VisualisationFunction() {
        
        /** render to the drawing image */
        public void render( Graphics output_Graphics ) {
            
            output_Graphics.setColor(new Color(spectrum_color));
            
            float[] waveform = filter[filter.length-1];
            
            float xscalar = (float)(image_width-1) / (waveform.length - 1);
            float yscalar = 4 * image_height / 128.f;
            
            int xp = 0;
            int yp = (int)(half_image_height + waveform[0] * yscalar);
            
            for (int i = 1; i < waveform.length; i++) {
                int x = (int)(i * xscalar);
                int y = (int)(waveform[i] * yscalar + half_image_height);
                output_Graphics.drawLine(xp,yp,x,y);
                xp = x;
                yp = y;
            }
        }
        
        /** return a string identifying this function */
        public String getInfo() {
            return "waveform";
        }
    };
    
    /**
     *
     * @param args
     */
    public static void main(String [] args) {
     
        int W = 1000 , H = 200 ;
        final BufferedImage frameimage = new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
        final DoubleBuffer buffer = new DoubleBuffer( frameimage ,frameimage,null,null) ;
        final AudioInput audio = new AudioInput(buffer,1);
     
        audio.set_active(true);
        audio.set_visualisation(0);
     
        final JFrame frame = new JFrame("audio test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(W,H);
        frame.setVisible(true);
     
        Graphics g = frame.getGraphics();
        Graphics g2 = frameimage.getGraphics();
        g2.setColor(new Color(0x050000000,true));
     
        audio.start();
        System.out.println("audio started.");
        while (true) {
            g2.fillRect(0,0,W,H);
            audio.render();
            audio.simulate_tone(audio.max_pitch);
            g.drawImage(frameimage,0,0,null);
        }
    }
    
}
