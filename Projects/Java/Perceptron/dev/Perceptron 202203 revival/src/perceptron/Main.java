package perceptron;

import javax.swing.SwingUtilities;
import static javax.swing.SwingUtilities.invokeLater;
import static util.Fullscreen.printScreenInfo;
import static util.Fullscreen.setNiceLAF;
import static util.Sys.sout;

/* Main.java
 * Created on March 8, 2007, 4:15 PM
 */
public class Main {

    public static void main(String[] args) {
        sout("Welcome to Perceptron...");
        invokeLater(()->{
            sout("Starting GUI.");
            printScreenInfo();
            setNiceLAF();
            final Perceptron P = new Perceptron(
                "resource/Settings.txt",
                "resource/CrashReport.txt",
                "resource/presets/");
            (new Thread(P::go)).start();
        });
    }
    
    /**
     * Lockstep renderer. (i.e. frame-synchronous pipeline)
     * 
     * Address these issues: 
     * - Java is slow, most computers have 2 cores, can we use both? 
     * - Much rendering can be cached, but
     * - If we cached on settings-change, we work too hard; 
     *   - User input can change many times per frame, (e.g. cursors)
     * 
     * Lockstep
     * - On each frame
     *   - Thread A checks the current state and caches what it can
     *   - Thread B uses caches from previous frame to run this frame
     * 
     * Cons: 
     * - 1 frame delay in response to settings 
     * - Synchronization complexity
     * - Complicated error condition if one thread dies or locks
     * 
     * Pros:
     * - Thread A backgrounds some of the calculations 
     *   - and is a NOOP if nothing is changing
     * - Thread B just handles pixels, should be faster
     * 
     * This is a bit tricky to design. It will look something like this: 
     * 
     *  forever: 
     *      in thread A: P.cacheFrame();
     *      in thread B: P.renderFrame(); 
     *      wait for both of these things to finish
     *      in thread A: P.renderFrame();
     *      in thread B: P.cacheFrame()
     *      wait for both of these things to finish
     * 
     * This is hard to start up. The cache can't be stale (really must
     * be from the previous frame). We should keep track of flags to know when
     * things change (and we need to recompute caches).
     * 
     * We want to interleave two staged-rendering operations
     * 
     * A [prepare]→[render]  | [prepare] → [render]
     * B           [prepare] → [render]  | [prepare] → [render]
     * 
     * This way, every [prepare] is directly followed by [render], and the 
     * cached data is never stale. When starting up, thread B simply waits
     * half a frame length to start. 
     * 
     * We need to control the timing: Limiting the frame rate might not be 
     * enough. If one of [prepare] or [render] is much faster than the other,
     * then the threads could "bunch up"; This would cause jittery
     * step-step-jump; step-step-jump; rendering. 
     * 
     * We can pretend [prepare] and [render] to each take similar amounts of
     * time between successive frames. We will ask each thread to wait half a
     * frame in time between starting to render. If we aren't limiting the
     * frame rate, we might have to do something more clever.
     * 
     * Or: we could have the event thread request a re-cache when it 
     * changes something. And we have another thread that just runs a tight
     * spin loop where it re-caches whenever this flag is set, best-effort. 
     * The flag might be re-set while it's caching. 
     * 
     * caching thread:
     *      forever:
     *          if work-to-do:
     *              cache
     * 
     * 
     */
    static void lockstep() {
        
    }
}

