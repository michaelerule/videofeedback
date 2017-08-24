package perceptron;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;


/**
 * Extension of the image opener jfilechooser to show image preview with thumbnails based on docs from Oracle and other
 * ideas.
 */
public class ImagePreview extends JComponent implements PropertyChangeListener {
    ImageIcon tmpIcon, thumbnail = null;
    File file = null;

    public ImagePreview(JFileChooser fc) {
        setPreferredSize(new Dimension(280, 240));
        fc.addPropertyChangeListener(this);
    }

    public void loadImage() {
        if (file == null) {
            thumbnail = null;
            return;
        }

        //Don't use createImageIcon (which is a wrapper for getResource)
        //because the image we're trying to load is probably not one
        //of this program's own resources.
        tmpIcon = new ImageIcon(file.getPath());
        if (tmpIcon != null) {
            if (tmpIcon.getIconWidth() > 200) {
                thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(200, -1, Image.SCALE_DEFAULT));
            } else { //no need to miniaturize
                thumbnail = tmpIcon;
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();

        //If the directory changed, don't show an image.
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
            update = true;

            //If a file became selected, find out which one.
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            update = true;
        }

        //Update the preview accordingly.
        if (update) {
            thumbnail = null;
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (thumbnail == null) {
            loadImage();
        }
        if (thumbnail != null) {
            int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
            int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

            if (y < 0) {
                y = 0;
            }

            if (x < 5) {
                x = 5;
            }

            if (tmpIcon != null) {
                int w = tmpIcon.getIconWidth();
                int h = tmpIcon.getIconHeight();
                if (w < 0 || h < 0) {
                    g.setColor(Color.white);
                    g.drawString("not an image", x, y);
                } else {
                    String dim = w + " x " + h;
                    g.setColor(Color.black);
                    g.drawString(dim, x, y);
                    g.setColor(Color.white);
                    g.drawString(dim, x + 1, y + 1);
                }
            } else {
                g.setColor(Color.white);
                g.drawString("not an image", x, y);
            }

            thumbnail.paintIcon(this, g, x, y);
        }
    }
}
