package word;


/**
 * Adapted from http://www.javafaq.nu/java-bookpage-31-1.html
 */

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

// RTF editor that displays titles, normal, bold and italic fonts, and saves them, but without any controls.
public class WordProcessor extends JFrame {

    protected JTextPane editor_text_pane;
    protected StyleContext style_context;
    protected DefaultStyledDocument styled_document;
    protected RTFEditorKit rtf_kit;
    protected JFileChooser file_chooser;
    protected SimpleFilter rtf_file_filter;
    protected JToolBar toolBar;

    File default_help_file = new File("help.rtf");

    public WordProcessor() {

        super("Perceptron Help Window");
        setLocation(680, 330);
        setSize(640, 640);

        editor_text_pane = new JTextPane();
        rtf_kit = new RTFEditorKit();
        editor_text_pane.setEditorKit(rtf_kit);
        style_context = new StyleContext();
        styled_document = new DefaultStyledDocument(style_context);
        editor_text_pane.setDocument(styled_document);
        JScrollPane scroll_pane = new JScrollPane(editor_text_pane);
        getContentPane().add(scroll_pane, BorderLayout.CENTER);
        JMenuBar menu_bar = createMenuBar();
        setJMenuBar(menu_bar);
        file_chooser = new JFileChooser();
        file_chooser.setCurrentDirectory(new File("."));
        rtf_file_filter = new SimpleFilter("rtf", "RTF Documents");
        file_chooser.setFileFilter(rtf_file_filter);


        /*
        // standalone app exits this way
        WindowListener wndCloser = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        addWindowListener(wndCloser);
        */

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Load default help file immediately
        try {
            InputStream in = new FileInputStream(default_help_file);
            styled_document = new DefaultStyledDocument(style_context);
            rtf_kit.read(in, styled_document, 0);
            editor_text_pane.setDocument(styled_document);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //setVisible(true);
    }

    protected JMenuBar createMenuBar() {

        JMenuBar menu_bar = new JMenuBar();
        JMenu file_menu = new JMenu("File");
        file_menu.setMnemonic('f');

        ImageIcon iconNew = new ImageIcon("resource/icons/word/new.png");

        javax.swing.Action actionNew = new AbstractAction("New", iconNew) {
            public void actionPerformed(ActionEvent e) {
                styled_document = new DefaultStyledDocument(style_context);
                editor_text_pane.setDocument(styled_document);
            }
        };

        JMenuItem item = file_menu.add(actionNew);
        item.setMnemonic('n');

        ImageIcon iconOpen = new ImageIcon("resource/icons/word/open.png");

        javax.swing.Action actionOpen = new AbstractAction("Open...", iconOpen) {
            public void actionPerformed(ActionEvent e) {
                WordProcessor.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Thread runner = new Thread() {
                    public void run() {
                        if (file_chooser.showOpenDialog(WordProcessor.this) != JFileChooser.APPROVE_OPTION) return;
                        WordProcessor.this.repaint();
                        File file = file_chooser.getSelectedFile();
                        try {
                            InputStream in = new FileInputStream(file);
                            styled_document = new DefaultStyledDocument(style_context);
                            rtf_kit.read(in, styled_document, 0);
                            editor_text_pane.setDocument(styled_document);
                            in.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        WordProcessor.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                };
                runner.start();
            }
        };

        item = file_menu.add(actionOpen);
        item.setMnemonic('o');

        ImageIcon iconSave = new ImageIcon("resource/icons/word/save.png");

        javax.swing.Action actionSave = new AbstractAction("Save!", iconSave) {
            public void actionPerformed(ActionEvent e) {
                WordProcessor.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Thread runner = new Thread() {
                    public void run() {
                        //if (file_chooser.showSaveDialog(WordProcessor.this) != JFileChooser.APPROVE_OPTION) return;
                        WordProcessor.this.repaint();
                        //File fChoosen = file_chooser.getSelectedFile();
                        try {
                            //OutputStream out = new FileOutputStream(fChoosen);
                            OutputStream out = new FileOutputStream(default_help_file);
                            rtf_kit.write(out, styled_document, 0, styled_document.getLength());
                            out.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        file_chooser.rescanCurrentDirectory();
                        WordProcessor.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                };
                runner.start();
            }
        };

        ImageIcon iconSaveAs = new ImageIcon("resource/icons/word/saveas.png");

        javax.swing.Action actionSaveAs = new AbstractAction("Save As...", iconSaveAs) {
            public void actionPerformed(ActionEvent e) {
                WordProcessor.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Thread runner = new Thread() {
                    public void run() {
                        if (file_chooser.showSaveDialog(WordProcessor.this) != JFileChooser.APPROVE_OPTION) return;
                        WordProcessor.this.repaint();
                        File file = file_chooser.getSelectedFile();
                        file = new File(file.getAbsolutePath());
                        if (file.exists()) {
                            ShortFilename name = new ShortFilename(file_chooser.getSelectedFile().getName());
                            int answer = JOptionPane.showConfirmDialog(null, "File " + name.getShortName(30) + " already exists!\nOverwrite??", "Yes or No?",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
                                return;
                            }
                        }
                        try {
                            OutputStream out = new FileOutputStream(file);
                            rtf_kit.write(out, styled_document, 0, styled_document.getLength());
                            out.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        file_chooser.rescanCurrentDirectory();
                        WordProcessor.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                };
                runner.start();
            }
        };

        item = file_menu.add(actionSave);
        item.setMnemonic('s');

        item = file_menu.add(actionSaveAs);
        item.setMnemonic('w');

        file_menu.addSeparator();

        javax.swing.Action actionExit = new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                //System.exit(0);
                setVisible(false);
                dispose();
            }
        };

        item = file_menu.add(actionExit);
        item.setMnemonic('x');
        menu_bar.add(file_menu);
        toolBar = new JToolBar();
        JButton buttonNew = new SmallButton(actionNew, "New document");
        toolBar.add(buttonNew);
        JButton buttonOpen = new SmallButton(actionOpen, "Open RTF document");
        toolBar.add(buttonOpen);
        JButton buttonSave = new SmallButton(actionSave, "Save");
        toolBar.add(buttonSave);
        JButton buttonSaveAs = new SmallButton(actionSaveAs, "Save As");
        toolBar.add(buttonSaveAs);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        return menu_bar;

    }
}
