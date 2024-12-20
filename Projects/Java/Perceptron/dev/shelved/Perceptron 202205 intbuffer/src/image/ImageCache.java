package image;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import javax.imageio.ImageIO;
import static util.Misc.wrap;
import static util.Sys.serr;
import static util.Sys.sout;

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
            for (var file : files) {
                String [] parts = file.getName().toLowerCase().split("\\.");
                String extension = parts[parts.length-1];
                if ("png jpg gif bmp".contains(extension)) {
                    images.add(file);
                    sout("loaded image " + file.getName());
                }
            }
        }
        size = images.size();
    }

    public BufferedImage next(int n) {return get(wrap(images.indexOf(current) + n, size));}
    public int current() {return images.indexOf(current);}
    public String name() {return current!=null? current.getName() : "(none)";}

    public BufferedImage get(int n) {
        if (images.isEmpty()) {current=null; return null;}
        current = images.get(current == null ? 0 : wrap(n, size));
        try {
            BufferedImage i1 = ImageIO.read(current);
            if (null==i1) {
                serr("I tried to get image "+n+" which should be "+current+" but encoutered null?");
                return null;
            }
            BufferedImage i2 = new BufferedImage(i1.getWidth(), i1.getHeight(), TYPE_INT_RGB);
            i2.getGraphics().drawImage(i1, 0, 0, null);
            sout("loaded image " + current.getName());
            return i2;
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
