package rendered;
/* AudioInput.java
 * Created on March 19, 2007, 12:04 PM
 *
 * This class handles retrieval of audio data from the system,
 * as well as filtering and visualisation of that data.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import image.DoubleBuffer;
import static java.lang.Math.max;
import java.util.List;
import static javax.sound.sampled.AudioSystem.getLine;
import static javax.sound.sampled.AudioSystem.getTargetLineInfo;
import static util.Misc.clip;
import static util.Misc.wrap;

public class Microphone {

    public static final int 
            SAMPLE_RATE      = 44100,//8000,11025,16000,22050,44100
            FREQUENCY_CUTOFF = 4,
            SUBSAMPLE        = 4;
    // The desired format of the sampled audio 
    static final AudioFormat format = new AudioFormat(
            SAMPLE_RATE,
            8,    //sampleSizeInBits //8,16
            1,    //channels         //1,2
            true, //signed           //true,false
            false //bigEndian        //true,false
            );
    static final float V          = (float) (2 * Math.PI * SAMPLE_RATE);
    static final int   START_NOTE = -(3 * 12)*SUBSAMPLE, 
                       END_NOTE   =  (2 * 12)*SUBSAMPLE;
    
    DoubleBuffer   buffer;
    TargetDataLine line;
    CaptureThread  capture;
    public int     audio_line = 0;
    boolean        active=false, 
                   working=false;
    
    byte []   temp_byte;
    float[][] filter;
    float[]   hist, histbuf;
    float[]   frequency;
    float[][] harmonic;
    float[]   index;
    
    float speed=.05f, w1, w2;
    float vol=1, d_volume_high, d_volume_low;
    float low_volume_average=0, 
          high_volume_average=0, 
          pitch_average=0,
          loudness = 1;
    float interval;
    int   spect_color, pitch_color;
    int   max_pitch;
    int   image_width, image_height, half_image_height, buffer_size;
    
    public Microphone(DoubleBuffer b, int iline) {
        audio_line = iline ;
        try {            
            //obtain and start a new incoming sound line
            ArrayList<TargetDataLine> valid_lines = new ArrayList<>();
            Line.Info[] all_lines = getTargetLineInfo(new TargetDataLine.Info(TargetDataLine.class,format)) ;
            for (var i : all_lines)
                valid_lines.add((TargetDataLine)getLine(i));
            if (valid_lines.size()<=0 || iline<0 || iline>=valid_lines.size()) {
                active = working = false; return;
            }
            line = (TargetDataLine)valid_lines.get(iline);
            line.open(format);
            line.start();
            System.out.println("LISTENING ON " + line );

            int counter = 0;

            index = new float[END_NOTE - START_NOTE];

            interval = SUBSAMPLE * 12.f;
            for (int i = START_NOTE; i < END_NOTE; i++) {
                float size = (int) (SAMPLE_RATE / (440 * Math.pow(2, i / interval)));
                if (size > 1) {
                    index[counter] = size;
                    counter++;
                }
            }

            filter = new float[index.length][];
            for (int i = 0; i < counter; i++) {
                filter[i] = new float[(int) index[i]];
            }

            hist = new float[filter.length];
            histbuf = new float[filter.length];

            harmonic = new float[filter.length][filter.length];
            for (int i = 0; i < harmonic.length; i++) {
                for (int j = 0; j < harmonic.length; j++) {
                    float quot = (float) StrictMath.abs(2 * ((index[j] / index[i]) % 1 - .5f));
                    if (quot > .9) harmonic[i][j] = quot;
                }
            }

            temp_byte = new byte[(int) index[counter - 1] * 4];
            buffer = b;
            image_width  = buffer.out.img.getWidth();
            image_height = buffer.out.img.getHeight();
            half_image_height = image_height / 2;
            buffer_size = buffer.out.buf.getSize();
            updateWeights();
            vis = visuals.get(0);
            working = true;
            active = false;
        } catch (LineUnavailableException E) {
            System.err.println("Could not get audio line "+iline+", audio input won\'t work.");
            E.printStackTrace();
            working = active = false;
        }
    }

    public void setActive(boolean a) {
        if (!working) {active=false;return;}
        if (active == a) return;
        if (active) stop();
        else start();
    }

    public void start() {
        if (!working) {active=false;return;}
        if (capture==null||!capture.running) {(capture=new CaptureThread()).start();}
        active = true;
    }
    
    public void    stop() {capture.end();active=false;}
    public void    setSpeed(float f)    {speed=f;updateWeights();}
    public void    setVolume(float v)   {vol  =v;updateWeights();}
    public void    adjustSpeed(float n) {setSpeed(clip(speed+n,0.01f,0.99f));}
    public void    adjustVolume(float n){setVolume(clip(vol+n,0,10));}
    public float   getSpeed()           {return speed;}
    public float   getVolume()          {return vol;}
    final  void    updateWeights()      {w2=speed*vol; w1=1-speed;}
    public boolean isActive()           {return active;}
    public int     soundColor()         {return spect_color;}
    public int     pitchColor()         {return pitch_color;}
    
    public void render() {
        if (!(active && ((capture != null) && capture.running))) return;
        computeHistogram();
        vis.draw(buffer.out.g);
    }
    
    /** compute the frequency histogram */
    void computeHistogram() {
        for (int i = 0; i < filter.length; i++) {
            frequency = filter[i];
            float sum = 0;
            for (int j = 0; j < frequency.length; j++) sum += frequency[j] * frequency[j];
            hist[i] = sum / index[i];//.length;
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
        float biggest = hist[0];
        max_pitch = 0;
        float low_average = 0, high_average = 0;
        for (int i = 1; i < hist.length / 2; i++) {
            if (hist[i] > biggest) {
                max_pitch = i;
                biggest = hist[i];
            }
            float weight_ = hist[i] / low_volume_average;
            int RGB = Color.HSBtoRGB(i % interval / interval, 1.f, weight_);
            R += (0x000000FF & (RGB >> 16));
            G += (0x000000FF & (RGB >> 8));
            B += (0x000000FF & RGB);
            low_average += hist[i];
        }
        for (int i = hist.length / 2; i < hist.length; i++) {
            if (hist[i] > biggest) {
                max_pitch = i;
                biggest = hist[i];
            }
            float weight_ = hist[i] / high_volume_average;
            int RGB = Color.HSBtoRGB(i % interval / interval, 1.f, weight_);
            R += (0x000000FF & (RGB >> 16));
            G += (0x000000FF & (RGB >> 8));
            B += (0x000000FF & RGB);
            high_average += hist[i];
        }
        d_volume_low = low_average / low_volume_average;
        d_volume_high = high_average / high_volume_average;
        pitch_average = pitch_average * .99f + biggest * .01f;
        low_volume_average = low_volume_average * .99f + low_average * .01f;
        high_volume_average = high_volume_average * .99f + high_average * .01f;
        float normalise = (low_average + high_average) / (low_volume_average + high_volume_average) / Math.max(R, Math.max(G, B));
        R *= normalise;
        G *= normalise;
        B *= normalise;
        spect_color =
                (((int) (R * 255) & 0x000000FF) << 16)
                | (((int) (G * 255) & 0x000000FF) << 8)
                | (((int) (B * 255) & 0x000000FF));
        pitch_color = Color.HSBtoRGB((float) max_pitch / hist.length, 1.f, 1.f) & 0x00FFFFFF;
        loudness = .999f * loudness + .001f * hist[max_pitch];
    }

    class CaptureThread extends Thread {
        boolean running;
        long frame_position = 0;
        public CaptureThread() { running = false; }
        public void run() {
            if (running || !working)  return;
            running = true;
            try {
                float[] filt;
                while (running) {
                    int new_data_length = line.read(temp_byte, 0, temp_byte.length);
                    for (int j = 0; j < new_data_length; j++) {
                        float new_data = w2 * temp_byte[j];
                        for (int i = 0; i < filter.length; i++) {
                            filt = filter[i];
                            int ii = (int) (frame_position % index[i]);
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
        public void end() {running = false;}
    }

    ////////////////////////////////////////////////////////////////////////////
    // Audio visualization drawing routines
    public VisualisationFunction vis;
    public int  getVis()       {return visuals.isEmpty()?-1:visuals.indexOf(vis);}
    public void setVis(int i)  {if (!visuals.isEmpty()) vis=visuals.get(wrap(i,visuals.size()));}
    public void nextVis(int n) {setVis(max(0,visuals.indexOf(vis))+n);}
    public abstract class VisualisationFunction {
        public final String name;
        public VisualisationFunction(String s) {name=s;}
        public abstract void draw(Graphics output_Graphics);
    }
    private final List<VisualisationFunction> visuals = List.of(
        new VisualisationFunction("piano") {public void draw(Graphics g) {
            final int[] white = {0, 2, 3, 5, 7, 8, 10}, black = {1, 4, 6, 9, 11};
            float xs = (float) (image_width - 1) / ((filter.length / SUBSAMPLE) * 7 / 12.f);
            int keywidth = (int) Math.max(2, xs - 2);
            int keyheight = 6 * keywidth;
            int note = 0;
            int octave = 0;
            int key = octave * 12 + black[note];
            int counter = 1;
            float color_scalar = 255 / hist[max_pitch];/// 77.f * 256 / histogram[max_pitch];;
            int px = 0;
            int END = filter.length / SUBSAMPLE;
            while (key < END) {
                float sum = hist[key * SUBSAMPLE];
                for (int i = 1; i < SUBSAMPLE; i++) sum += hist[key * SUBSAMPLE - i];
                int color = 0x000000FF & (int) (255 - Math.min(255, sum * color_scalar));
                color = (color << 16) | (color << 8) | color;
                g.setColor(new Color(color));
                int x = (int) (counter * xs);
                g.fillRect(px, 0, (x - px) - 1, keyheight);
                px = x;
                note++;
                if (note >= white.length) {
                    note = 0;
                    octave++;
                }
                key = octave * 12 + white[note];
                counter++;
            }
            xs = (float) (image_width - 1) / (filter.length / SUBSAMPLE);
            keywidth = keywidth * 7 / 8;
            keyheight = keyheight * 3 / 5;
            int offset = keywidth / 3;
            note = 0;
            octave = 0;
            key = octave * 12 + black[note];
            while (key < END) {
                float sum = hist[key * SUBSAMPLE];
                for (int i = 1; i < SUBSAMPLE; i++) sum += hist[key * SUBSAMPLE - i];
                int c = 0x000000FF & (int) (Math.min(255, sum * color_scalar));
                c = (c << 16) | (c << 8) | c;
                g.setColor(new Color(c));
                g.fillRect((int) (key * xs) + offset, 0, keywidth, keyheight);
                note++;
                if (note >= black.length) {
                    note = 0;
                    octave++;
                }
                key = octave * 12 + black[note];
            }
        }},
        new VisualisationFunction("bars") {public void draw(Graphics g){
            g.setColor(new Color(spect_color));
            float xs = (float) (image_width - 1) / (filter.length - 1);
            float ys = image_height / hist[max_pitch];
            int x = 0;
            int y = image_height - (int) (hist[0] * ys);
            for (int i = 1; i < filter.length; i++) {
                int nx = (int) (i * xs);
                int ny = image_height - (int) (hist[i] * ys);
                g.drawRect(x, y, nx - x, image_height - y);
                x = nx;
                y = ny;
            }
        }},
        new VisualisationFunction("transform") {public void draw(Graphics g){
            g.setColor(new Color(spect_color));
            float xs = (float) (image_width - 1) / (filter.length - 1);
            float ys = image_height / hist[max_pitch];
            int x = 0;
            int y = image_height - (int) (hist[0] * ys);
            for (int i = 1; i < filter.length; i++) {
                int nx = (int) (i * xs);
                int ny = image_height - (int) (hist[i] * ys);
                int tx = (nx + x) / 2;
                g.drawLine(x, y, tx, ny);
                g.drawLine(tx, ny, nx, ny);
                x = nx;
                y = ny;
            }
        }},
        new VisualisationFunction("waveform") {public void draw(Graphics g) {
            g.setColor(new Color(spect_color));
            float[] waveform = filter[filter.length - 1];
            float xs = (float) (image_width - 1) / (waveform.length - 1);
            float ys = 4 * image_height / 128.f;
            int xp = 0;
            int yp = (int) (half_image_height + waveform[0] * ys);
            for (int i = 1; i < waveform.length; i++) {
                int x = (int) (i * xs);
                int y = (int) (waveform[i] * ys + half_image_height);
                g.drawLine(xp, yp, x, y);
                xp = x;
                yp = y;
            }
        }});
    
    
    public static void main(String [] args) {
        
        TargetDataLine.Info info = new TargetDataLine.Info(TargetDataLine.class,format);
        
        try {

            /*
            ArrayList<Line> valid_lines = new ArrayList<>();
            System.out.println("AUDIO MIXERS ");
            Mixer.Info[] mixers = AudioSystem.getMixerInfo() ;
            for (var i: mixers) {
                System.out.println("  Mixer: "+i);
                Mixer m = AudioSystem.getMixer(i) ;
                try {
                    m.open();
                    //Line.Info[] lines = m.getSourceLineInfo();
                    //if (lines.length<=0) System.out.println("    ( no source lines )" );
                    //else {
                    //    System.out.println("    SourceLines");
                    //    for (var l:lines) System.out.println( "      " + l );
                    //}
                    Line.Info[] lines = m.getTargetLineInfo();
                    if (lines.length<=0) System.out.println("    ( no target lines )" );
                    else {
                        System.out.println("    TargetLines");
                        for (var l:lines) {
                            System.out.println("      " + l);
                            
                            Class c = l.getLineClass();
                            System.out.println("        " + c);
                            for (var f: c.getDeclaredMethods())
                                System.out.println("          " + f);
                            
                            try {
                                DataLine.Info L = (DataLine.Info)l;
                                System.out.println("      MaxBufferSize " + L.getMaxBufferSize());
                                System.out.println("      MinBufferSize " + L.getMinBufferSize());
                                System.out.println("      Formats:");
                                for (var f:L.getFormats()) {
                                    System.out.println("        " + f);
                                }
                                System.out.println("        (this is a DataLine)");
                            } catch (Exception e) {
                            }
                            try {
                                Port.Info L = (Port.Info)l;
                                System.out.println("        To/From: " + L.getName());
                                System.out.println("        Is Source (audio input)? " + L.isSource());
                                System.out.println("        (this is a Port)");
                            } catch (Exception e) {
                            }
                        }
                        if ( m.isLineSupported( info ) ) {
                            valid_lines.add(m.getLine( info ));
                        } else {
                            System.out.println("      ( desired format isn't supported )");
                        }
                    }

                } catch( LineUnavailableException e ) {
                    System.out.println("Error opening mixer");
                    e.printStackTrace();
                } finally {
                    if (m.isOpen()) m.close();
                }
                for (var l:valid_lines) System.out.println("- "+l);
            }
            */

            System.out.println("AUDIO LINES");
            ArrayList<TargetDataLine.Info> valid_lines = new ArrayList<>();
            Line.Info[] all_lines = getTargetLineInfo(info) ;
            for ( Line.Info i : all_lines ) {
                System.out.println( "  " + i.getLineClass());
                System.out.println( "    " + i.toString() );
                Line l = AudioSystem.getLine( i );
                System.out.println( "    " + l );
                valid_lines.add((TargetDataLine.Info)i);
            }
            System.out.println(valid_lines);

            //System.out.println("AUDIO FORMATS");
            //AudioFileFormat.Type[] formats = AudioSystem.getAudioFileTypes();
            //for ( var i : formats )
            //    System.out.println( "  " + i.toString() );

        } catch( LineUnavailableException e ) {
            System.out.println("Error grabbing audio streams");
            e.printStackTrace();
        }
    }

}
