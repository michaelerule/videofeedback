package perceptron;
/**@file Main.java
 * Created on March 8, 2007, 4:15 PM
 * 
 * Main entry point for Perceptron
 *
 * @author Michael Everett Rule
 */
public class Main
{
   
   /** Terminates the program */
   public static void exit()
   {
      System.exit(0);
   }
   
   /**
    * Performs basic sytem level initialization and launches
    * perceptron in a separate thread.
    * 
    * @param args the command line arguments AAAAAGH */
   public static void main(String[] args)
   {
      
      System.out.println("Running perceptron...");
      //achieve native L.A.F.
      try
      {
         javax.swing.UIManager.setLookAndFeel(
            javax.swing.UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      { }

      System.out.println("running...");
      final Perceptron m = new Perceptron("resource/Settings.txt","resource/presets/");


      System.out.println("running...");
      Thread go = new Thread()
      {     
        @Override
        public void run()
        {
           m.go();
        }
      };
      System.out.println("running...");
      go.setPriority( Thread.MIN_PRIORITY );
      System.out.println("running...");
      go.start();
   }
   
}
