package word;

import javax.swing.filechooser.FileFilter;
import java.io.File;


/*
Adapted from http://www.javafaq.nu/java-bookpage-31-1.html for use with FFMpeg Drive.
 */

import javax.swing.filechooser.FileFilter;
import java.io.File;

class SimpleFilter extends FileFilter {

    private String m_description = null;
    private String m_extension = null;

    public SimpleFilter(String extension, String description) {
        m_description = description;
        m_extension = "." + extension.toLowerCase();
    }

    public String getDescription() {
        return m_description;
    }

    public boolean accept(File f) {
        if (f == null) return false;
        if (f.isDirectory()) return true;
        return f.getName().toLowerCase().endsWith(m_extension);
    }
}
