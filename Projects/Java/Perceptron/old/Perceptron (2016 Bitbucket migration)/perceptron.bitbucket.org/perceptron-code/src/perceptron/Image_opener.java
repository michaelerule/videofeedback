package perceptron;


import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Image opener with thumbnails, image preview and memory problems...
 * See https://stackoverflow.com/questions/4096433/making-jfilechooser-show-image-thumbnails
 */
class Image_opener extends JFileChooser {

    /**
     * All preview icons will be this width and height
     */
    private static final int ICON_SIZE = 32;

    /**
     * This invisible icon will be used while previews are loading
     */
    private final Image LOADING_IMAGE = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
    /**
     * Use a weak hash map to cache images until the next garbage collection
     */
    private final WeakHashMap<String, Icon> imageCache = new WeakHashMap<>();
    /**
     * This thread pool is where the thumbnail icon loaders run
     */
    private final ThreadGroup threadGroup = new ThreadGroup("Perceptron : image opener group");
    final ExecutorService executor = Executors.newCachedThreadPool(r -> new Thread(threadGroup, r));
    /**
     * What file types will be previewed.
     */
    private final Pattern imageFilePattern = Pattern.compile(".+?\\.(png|jpe?g|gif|tiff?|tif|tga|bmp|ico)$", Pattern.CASE_INSENSITIVE);
    // static, conservative icons
    private final ImageIcon jpgIcon = Utils.createImageIcon("thumbs/jpgIcon.gif");
    private final ImageIcon gifIcon = Utils.createImageIcon("thumbs/gifIcon.gif");
    private final ImageIcon tiffIcon = Utils.createImageIcon("thumbs/tiffIcon.gif");
    private final ImageIcon pngIcon = Utils.createImageIcon("thumbs/pngIcon.png");

    Image_opener() {
        super();
    }

    {
        // This initializer block is always executed after any constructor call.
        setFileView(new ThumbnailView());
    }

    /**
     * Thumbnail preview with image cache.
     */
    private class ThumbnailView extends FileView {

        @Override
        public Icon getIcon(File file) {
            // avoid errors
            if (file == null) return null;
            // skip non-image files... ahem, by extension alone?
            if (!imageFilePattern.matcher(file.getName()).matches()) {
                return null;
            }

            String extension = Utils.getExtension(file);
            //System.out.println("icon found: " + file.toString());

            // Our cache overflows memory
            synchronized (imageCache) {

                String file_path = file.getAbsolutePath();
                Icon icon = imageCache.get(file_path);

                if (icon == null) {
                    // default empty icon prevents null pointer exception
                    icon = new ImageIcon(LOADING_IMAGE);
                    // for large files, use static icons, but don't put them into cache (for some reason)
                    if (file.length() > 5000000) {
                        if (extension != null) {
                            switch (extension) {
                                case Utils.jpeg:
                                case Utils.jpg:
                                    icon = jpgIcon;
                                    break;
                                case Utils.gif:
                                    icon = gifIcon;
                                    break;
                                case Utils.tiff:
                                case Utils.tif:
                                    icon = tiffIcon;
                                    break;
                                case Utils.png:
                                    icon = pngIcon;
                                    break;
                                default:
                                    // Assure that method is used
                                    icon = new ImageIcon(LOADING_IMAGE);
                                    break;
                            }
                        }
                    } else {
                        // Add to the cache
                        imageCache.put(file_path, icon);
                        // Submit a new task to load the image and update the icon
                        executor.submit(new ThumbnailIconLoader(file_path, icon));
                    }

                }

                return icon;
            }
        }


        @Override
        public String getTypeDescription(File f) {
            String extension = Utils.getExtension(f);
            String type = null;

            if (extension != null) {
                switch (extension) {
                    case Utils.jpeg:
                    case Utils.jpg:
                        type = "JPEG Image";
                        break;
                    case Utils.gif:
                        type = "GIF Image";
                        break;
                    case Utils.tiff:
                    case Utils.tif:
                        type = "TIFF Image";
                        break;
                    case Utils.png:
                        type = "PNG Image";
                        break;
                    case Utils.bmp:
                        type = "BMP Image";
                        break;
                    case Utils.tga:
                        type = "TGA Image";
                        break;
                    case Utils.ico:
                        type = "ICO Image";
                        break;
                }
            }
            return type;
        }

    }


    private class ThumbnailIconLoader implements Runnable {

        private final String file_path;
        private Icon icon;


        public ThumbnailIconLoader(String s, Icon i) {
            file_path = s;
            icon = i;
        }


        @Override
        public void run() {
            Thread.currentThread().setName("Perceptron : image opener thread (total " + threadGroup.activeCount() + ")");
            //System.out.println("Loading image: " + file);
            // Load and scale the image down, then replace the icon's old image with the new one.
            assert (icon) != null;

            ImageIcon newIcon = null;
            Image img, img_scaled = null;

            try {
                try {
                    newIcon = new ImageIcon(file_path);
                } catch (Exception ee) {
                    ee.printStackTrace();
                    return;
                }
                if (newIcon != null) {
                    img = newIcon.getImage();
                    img_scaled = img.getScaledInstance(ICON_SIZE, -1, Image.SCALE_SMOOTH);
                }
                if (img_scaled != null) {
                    newIcon.setImage(img_scaled);
                    icon = newIcon;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            imageCache.put(file_path, icon);

            // Repaint the dialog so we see the new icon.
            SwingUtilities.invokeLater(() -> repaint());

        }
    }
}