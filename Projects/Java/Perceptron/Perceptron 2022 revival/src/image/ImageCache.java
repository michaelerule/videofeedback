package image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import javax.imageio.ImageIO;
import static util.Misc.wrap;

public class ImageCache {

    private ArrayList<File> images;
    private final int size;
    File current;

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
            for (var file : files) try {
                images.add(file);
                System.out.println("loaded image " + file.getName());
            } catch (Exception e) {
                System.err.println("could not open image \"" + file.getName() + "\"");
            }
        }
        size = images.size();
    }

    public BufferedImage next(int n) {return get(wrap(images.indexOf(current) + n, size));}
    public int current() {return images.indexOf(current);}
    public String name() {return current!=null? current.getName() : "(none)";}

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

    public int where(String image_file) {
        int i=0;
        for (var imf:images) {
            if (imf.getName().equals(image_file)) return i;
            i++;
        }
        return -1;
    }
}
