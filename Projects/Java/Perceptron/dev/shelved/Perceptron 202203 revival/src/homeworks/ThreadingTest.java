/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package homeworks;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newFixedThreadPool;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import static util.Sys.sout;

/**
 * Threading example from stackoverflow.com/a/28632378/900749 .
 * 
 * This approach might allow us to synchronously update caches while the
 * current frame is rendering. 
 * 
 * @author mer49
 */
public class ThreadingTest {
    
    
    public static void main(String [] args) {
        
        class MyCallable implements Callable<String> {
            int how_long;
            MyCallable(int how_long) {
                this.how_long = how_long;
            }
            public String call() {
                try {
                    Thread.sleep(how_long);
                    return "Slept "+how_long+" ms.";
                } catch (InterruptedException ex) {
                    return "Tried to sleep for "+how_long
                          +" ms, but was interrupted.";
                }
            }
        }

        // Create two threads for running two things at once
        ExecutorService executor = newFixedThreadPool(2);
        while (true) {
            Future<String> aFuture = executor.submit(new MyCallable(2000));
            Future<String> bFuture = executor.submit(new MyCallable(1000));
            try {
                // this will block until all calculations are finished:
                sout("Thread a says: "+aFuture.get());
            } catch (InterruptedException ex) {
                sout("Thread a was interrupted");
            } catch (ExecutionException ex) {
                sout("Thread a had a problem");
            }
            try {
                // this will block until all calculations are finished:
                sout("Thread b says: "+bFuture.get());
            } catch (InterruptedException ex) {
                sout("Thread b was interrupted");
            } catch (ExecutionException ex) {
                sout("Thread b had a problem");
            }
        }

    }
    
}
