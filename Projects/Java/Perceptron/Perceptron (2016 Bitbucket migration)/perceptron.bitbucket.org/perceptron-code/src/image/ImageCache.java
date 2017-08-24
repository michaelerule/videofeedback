package image;

/**
 * Perceptron
 *
 * @author Michael Everett Rule (mrule7404@gmail.com)
 * @author Predrag Bokšić (junkerade@gmail.com)
 *         <p/>
 *         Perceptron is a video feedback engine with a variety of extraordinary graphical effects.
 *         It evolves colored geometric patterns and visual images into the realm of infinite details
 *         and deepens the thought. </p>
 *         <p/>
 *         <p> Please visit the project Perceptron home page...</p>
 *         <p><a href="http://perceptron.sourceforge.net/">perceptron.sourceforge.net</a></p>
 */

import perceptron.Perceptron;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * ImageCache will store image files sorted by name on the input in memory, in an ArrayList.
 */
public final class ImageCache {


    public ArrayList<File> images; // stores the image file or files
    public static File current;
    int size = 0;
    Perceptron percept;


    /** Create image cache from all the phoptos in a given folder. */
    public ImageCache(Perceptron p, String folder_name) {
        if (folder_name == null) {
            System.out.println("Image folder name missing.");
            return;
        }
        percept = p;
        // platform compatibility check for pathnames
        if (File.separatorChar == '\\') {
            folder_name = folder_name.replace('/', File.separatorChar);
            folder_name = folder_name.replace('\\', File.separatorChar);
            System.out.println("image cache:  " + folder_name);
        }
        if (File.separatorChar == '/') {
            folder_name = folder_name.replace('\\', File.separatorChar);
            folder_name = folder_name.replace('/', File.separatorChar);
            System.out.println("image cache:  " + folder_name);
        }
        File f = new File(folder_name);
        if (!f.exists() || !f.canRead()) {
            System.out.println("Cannot read the image folder: \"" + f.toString() + "\"");
            return;
        }
        f = new File(folder_name);
        images = new ArrayList<>();
        if (f != null && f.listFiles() != null) {
            Arrays.sort(f.listFiles());
            for (File file : f.listFiles()) {
                try {
                    try {
                        if (ImageIO.read(file) == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        System.err.println("could not understand the image file: \"" + file.toString() + "\"");
                    }
                    images.add(file);
                    System.out.println("preloaded image: \"" + folder_name + File.separatorChar + file.getName() + "\"");
                } catch (Exception e) {
                    System.err.println("could not preload image: \"" + folder_name + File.separatorChar + file.getName() + "\"");
                }
            }
        }
    }


    public int current_image_index() {
        return images.indexOf(current);
    }


    int wrap(int n, int m) {
        return n < 0 ? m - 1 - (-n % m) : n % m;
    }


    /**
     * Move to the next or previous image in the ImageCache.
     */
    public BufferedImage advance_image(int image_index_increment) {
        int number_of_images = images.size();
        int current_image_index = images.indexOf(current);
        current_image_index = current_image_index + image_index_increment;
        if (current_image_index == number_of_images) {
            current_image_index = 0;
        }
        if (current_image_index == -1) {
            current_image_index = number_of_images - 1;
        }
        return load_image_to_cache(current_image_index);
    }


    /**
     * Load image into the ImageCache.
     */
    public BufferedImage load_image_to_cache(int current_image_index) {
        size = images.size();
        current = images.get(current_image_index = wrap(current_image_index, size));
        if (percept.sw != null) {
            percept.sw.jcb_select_image.setSelectedIndex(current_image_index);
        }
        try {
            BufferedImage image1 = ImageIO.read(current);
            if (image1 != null) {
                BufferedImage image2 = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_RGB);
                image2.getGraphics().drawImage(image1, 0, 0, null);
                System.out.println("loaded image: \"" + current.getName() + "\"");
                return image2;
            }
        } catch (Exception e) {
            System.err.println("could not open image: \"" + current.getName() + "\"");
        }
        return null;
    }

}
