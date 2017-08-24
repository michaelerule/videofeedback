package perceptron;

import javax.swing.filechooser.FileFilter;
import java.io.File;


/**
 * Defines allowed image extensions.
 */
public class ImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            return extension.equals(Utils.tiff) ||
                    extension.equals(Utils.tif) ||
                    extension.equals(Utils.gif) ||
                    extension.equals(Utils.jpeg) ||
                    extension.equals(Utils.jpg) ||
                    extension.equals(Utils.png) ||
                    extension.equals(Utils.bmp) ||
                    extension.equals(Utils.tga) ||
                    extension.equals(Utils.ico);
        }

        return false;
    }

    @Override
    /** The description of this filter */
    public String getDescription() {
        return "Just Images";
    }
}
