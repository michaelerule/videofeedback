package image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class ImageCache {

    private ArrayList<File> images;
    private int size;
    File current;

    public ImageCache(String folder_name) {

        images = new ArrayList<File>();

        File f = new File(folder_name);

        if (f != null && f.listFiles() != null) {
            for (File file : f.listFiles()) {
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

    public int current() {
        return images.indexOf(current);
    }

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
        } catch (Exception e) {
            System.err.println("could not open image " + current.getName());
        }
        return null;
    }
}
