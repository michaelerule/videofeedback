package image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import javax.imageio.ImageIO;

/**
 *
 * @author mer49
 */
public class ImageCache {

    private ArrayList<File> images;
    private final int size;
    File current;

    /**
     *
     * @param folder_name
     */
    public ImageCache(String folder_name) {
        assert null != folder_name;
        assert nonNull(folder_name);
        System.err.println("Really? "+folder_name);
        folder_name = requireNonNull(folder_name);
        assert !("null".equals(folder_name));
        images = new ArrayList<>();
        File f = new File(requireNonNull(folder_name));
        if (null != f.listFiles()) {
            File[] files = f.listFiles();
            Arrays.sort(files);
            for (var file : files) {
                try {
                    images.add(file);
                    System.out.println("loaded image " + file.getName());
                } catch (Exception e) {
                    System.err.println("could not open image " + file.getName());
                }
            }
        }
        size = images.size();
    }

    private static int wrap(int n, int m) {
        return n < 0 ? m - 1 - (-n % m) : n % m;
    }

    /**
     *
     * @param n
     * @return
     */
    public BufferedImage advance(int n) {
        int number_of_images = images.size();
        int current_image_selection = images.indexOf(current);
        current_image_selection = current_image_selection + n;
        if (current_image_selection == number_of_images) {
            current_image_selection = 0;
        }
        if (current_image_selection == -1) {
            current_image_selection = number_of_images - 1;
        }
        return get(current_image_selection);
    }

    /**
     *
     * @return
     */
    public int current() {
        return images.indexOf(current);
    }
    
    public String name() {
        return current!=null? current.getName() : "(none)";
    }

    /**
     *
     * @param n
     * @return
     */
    public BufferedImage get(int n) {
        if (images.isEmpty()) {
            current = null;
            return null;
        }
        current = images.get(current == null ? 0 : wrap(n, size));
        try {
            BufferedImage image1 = ImageIO.read(current);
            BufferedImage image2 = new BufferedImage(
                    image1.getWidth(),
                    image1.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            image2.getGraphics().drawImage(image1, 0, 0, null);
            System.out.println("loaded image " + current.getName());
            return image2;
        } catch (IOException e) {
            System.err.println("could not open image " + current.getName());
        }
        return null;
    }
}
