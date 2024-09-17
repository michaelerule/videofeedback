package video;
/**JWebCam.java
 * Created on August 7, 2007, 2:42 PM
 */

import java.io.*;
import javax.media.*;
import javax.media.datasink.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.util.*;
import javax.media.control.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import com.sun.media.protocol.vfw.VFWCapture;   // JMF 2.1.1e version

public class JWebCam
{
   public final static int MIN_WIDTH  = 320;
   public final static int MIN_HEIGHT = 240;
   public static int shotCounter = 1;
   
   public Component visualComponent = null;
   
   public Player player = null;
   public CaptureDeviceInfo webCamDeviceInfo = null;
   public MediaLocator ml = null;
   public Dimension imageSize = null;
   public FormatControl formatControl = null;
   
   public VideoFormat currentFormat = null;
   public Format[] videoFormats = null;
   public MyVideoFormat[] myFormatList = null;
   
   public boolean initialised = false;
   
   /** --------------------------------------------------------------
    * Initialise
    *
    * @returns true if web cam is detected */
   public boolean initialise( )
   throws Exception
   {
      return ( initialise( autoDetect() ) );
   }
   
   /**-------------------------------------------------------------------
    * Initialise
    *
    * @params _deviceInfo, specific web cam device if not autodetected
    * @returns true if web cam is detected */
   public boolean initialise( CaptureDeviceInfo _deviceInfo )
   throws Exception
   {
      webCamDeviceInfo = _deviceInfo;
      
      if ( webCamDeviceInfo != null )
      {
         try
         {
            ml = webCamDeviceInfo.getLocator();
            if ( ml != null )
            {
               player = Manager.createRealizedPlayer( ml );
               if ( player != null )
               {
                  player.start();
                  formatControl = (FormatControl)player.getControl(
                     "javax.media.control.FormatControl" );
                  videoFormats = webCamDeviceInfo.getFormats();
                  
                  visualComponent = player.getVisualComponent();
                  if ( visualComponent != null )
                  {
                     myFormatList = new MyVideoFormat[videoFormats.length];
                     for ( int i=0; i<videoFormats.length; i++ )
                        myFormatList[i] =
                           new MyVideoFormat( (VideoFormat)videoFormats[i] );
                     
                     Format currFormat = formatControl.getFormat();
                     if ( currFormat instanceof VideoFormat )
                     {
                        currentFormat = (VideoFormat)currFormat;
                        imageSize = currentFormat.getSize();
                     }
                     return ( true );
                  }
               }
            }
         }
         catch ( Exception e )
         {
         }
      }
      return ( false );
   }
   
   
   /** -------------------------------------------------------------------
    * Dynamically create menu items
    *
    * @returns the device info object if found, null otherwise */
   public void setFormat( VideoFormat selectedFormat )
   {
      if ( formatControl != null )
      {
         player.stop();
         
         imageSize = selectedFormat.getSize();
         formatControl.setFormat( selectedFormat );
         
         player.start();
         
         currentFormat = selectedFormat;
      }
   }
   
   public VideoFormat getFormat( )
   {
      return ( currentFormat );
   }
   
   /** -------------------------------------------------------------------
    * autoDetects the first web camera in the system
    * searches for video for windows ( vfw ) capture devices
    *
    * @returns the device info object if found, null otherwise */
   public CaptureDeviceInfo autoDetect( )
   {
      Vector list = CaptureDeviceManager.getDeviceList( null );
      CaptureDeviceInfo devInfo = null;
      System.out.println("Looking for cameras... ");
      
      if ( list != null )
      {
         String name;
         
         for ( int i=0; i < list.size(); i++ )
         {
            devInfo = (CaptureDeviceInfo)list.elementAt( i );
            System.out.println(": " + devInfo);
            name = devInfo.getName();
            
            if ( name.startsWith("vfw:") )  break;
         }
         
         if ( devInfo != null && devInfo.getName().startsWith("vfw:") )
            return ( devInfo );
         
         else for ( int i = 0; i  <  10; i++ ) try
         {
            name = VFWCapture.capGetDriverDescriptionName( i );
            if (name != null && name.length() > 1)
            {
               devInfo =
                  com.sun.media.protocol.vfw.VFWSourceStream.autoDetect( i );
               if ( devInfo != null ) return ( devInfo );
            }
         }
         catch ( Exception ioEx )
         {
         }
      }
      return ( null );
   }
   
   
   /**-------------------------------------------------------------------
    * deviceInfo
    *
    * @note outputs text information
    * ------------------------------------------------------------------- */
   
   public void deviceInfo( )
   {
      if ( webCamDeviceInfo != null )
      {
         Format[] formats = webCamDeviceInfo.getFormats();
         for ( int i=0; i<formats.length; i++ )
         {
            Format aFormat = formats[i];
            if ( aFormat instanceof VideoFormat )
            {
               Dimension dim = ((VideoFormat)aFormat).getSize();
            }
         }
      }
      else System.out.println("Error : No web cam detected");
   }
   
   /**-------------------------------------------------------------------
    * grabs a frame's buffer from the web cam / device
    *
    * @returns A frames buffer */
   FrameGrabbingControl fgc ;
   public Buffer grabFrameBuffer( )
   {
      if ( player != null )
      {
         if ( fgc == null )
            fgc = (FrameGrabbingControl)player.getControl(
               "javax.media.control.FrameGrabbingControl" );
         if ( fgc != null ) return ( fgc.grabFrame() );
      }
      
      return null ;
   }
   
   
   /**-------------------------------------------------------------------
    * grabs a frame's buffer, as an image, from the web cam / device
    *
    * @returns A frames buffer as an image */
   public Image grabFrameImage( )
   {
      Buffer buffer = grabFrameBuffer();
      if ( buffer != null )
      {
         BufferToImage btoi = new BufferToImage( (VideoFormat)buffer.getFormat() );
         if ( btoi != null )
            return btoi.createImage( buffer ) ;
      }
      return null ;
   }
   
   /** -------------------------------------------------------------------
    * Closes and cleans up the player */
   public void playerClose( )
   {
      if ( player == null ) return ;
      
      player.close();
      player.deallocate();
      player = null;
   }
   
   public void finalize( ) throws Throwable
   {
      playerClose();
      super.finalize();
   }
   
   class MyVideoFormat
   {
      public VideoFormat format;
      
      public MyVideoFormat( VideoFormat _format )
      {
         format = _format;
      }
   };
   
   public static JWebCam getWebCam()
   {
      try
      {
         final JWebCam myWebCam = new JWebCam(  );
         
         if ( !myWebCam.initialise() )
            System.out.println("Web Cam not detected / initialised");
         
         return myWebCam ;
      }
      catch ( Exception ex )
      {
         ex.printStackTrace();
      }
      return null ;
   }
}
