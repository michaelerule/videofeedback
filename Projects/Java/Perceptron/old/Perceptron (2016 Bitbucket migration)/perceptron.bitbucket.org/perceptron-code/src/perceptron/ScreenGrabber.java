package perceptron;

/**
 * Modified from MonteMedia package, part of ScreenRecorder application, JRecordingAreaFrame made by...
 *
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland. All rights reserved. You may not use, copy or modify
 * this file, except in compliance with the license agreement you entered into with Werner Randelshofer. For details
 * see accompanying license terms. Use of the Monte Media Library is free for all uses (non-commercial, commercial and
 * educational) under the terms of Creative Commons Attribution 3.0 (CC BY 3.0).
 * https://www.randelshofer.ch/monte/
 * https://creativecommons.org/licenses/by/3.0/
 */


import com.sun.awt.AWTUtilities;
import org.apache.commons.configuration.ConfigurationException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * ScreenGrabber.
 *
 * @author Werner Randelshofer
 * @version 1.0 2012-05-03 Created.
 */
public class ScreenGrabber extends javax.swing.JFrame {
    // define a transparent, draggable window with edges only
    // zero opacity means that a window cannot be grabbed and dragged across the screen, hence 1
    private final static Color backgroundColor = new Color(1, 1, 1, 1);
    private final Perceptron percept;
    private final Insets dragInsets = new Insets(6, 6, 6, 6);
    private final Dimension minSize = new Dimension(240, 240);
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel infoPanel;


    /**
     * Creates new form ScreenGrabber
     */
    public ScreenGrabber(Perceptron perceptron) {

        Thread.currentThread().setName("Perceptron : grabber window thread");

        percept = perceptron;

        // The following two lines must be executed before the window heavyweight component is created.
        setAlwaysOnTop(false);
        setUndecorated(true);
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);
        setAutoRequestFocus(false);
        requestFocus(false);
        setName("grabber");
        setIconImage(percept.icon.getImage());

        setBackground(backgroundColor); // windows transparency set here
        JContentPane cp = new JContentPane();
        setContentPane(cp);
        getRootPane().setOpaque(true);
        cp.setOpaque(false);
        AWTUtilities.setWindowOpaque(this, false);

        initComponents();

        Handler handler = new Handler();
        infoLabel.addMouseListener(handler);
        infoLabel.addMouseMotionListener(handler);
        cp.addMouseListener(handler);
        cp.addMouseMotionListener(handler);

        infoLabel.setOpaque(false);
        infoPanel.setBorder(new EraseBorder(new Insets(8, 10, 8, 10)));
        infoLabel.setForeground(new Color(0xFFFFFF));
        closeButton.setOpaque(false);

        // default size is perceptron's screen size (size of drawing canvas within the Perceptron JFrame)
        //setSize(perceptron.screen_width, perceptron.screen_height);
        // this has been overridden and it is safe for perceptron to load images of varying sizes
        //setResizable(false);

        updateLabel();
    }


    void updateLabel() {
        Rectangle r = getBounds();
        infoLabel.setText("" + r.x + ", " + r.y + "; " + r.width + " x " + r.height);
    }


    /**
     * Code for dispatching events from components to event handlers.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        infoPanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        infoPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setText("screen grabber: dimensions : 0,0,600,600");
        infoPanel.add(infoLabel, new java.awt.GridBagConstraints());

        getContentPane().add(infoPanel, new java.awt.GridBagConstraints());

        closeButton.setText("capture");
        closeButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        getContentPane().add(closeButton, gridBagConstraints);

        pack();
    }


    private void closeButtonPerformed() {
        percept.grabber_rectangle = getBounds();
        if (percept.system_based_preferences) {
            percept.PREFS.putInt("grabber_location.x", percept.screen_grabber_frame.getLocation().x);
            percept.PREFS.putInt("grabber_location.y", percept.screen_grabber_frame.getLocation().y);
            percept.PREFS.putInt("grabber_size.width", percept.screen_grabber_frame.getSize().width);
            percept.PREFS.putInt("grabber_size.height", percept.screen_grabber_frame.getSize().height);
            percept.PREFS.putInt("grabber_rectangle_height", percept.grabber_rectangle.height);
            percept.PREFS.putInt("grabber_rectangle_width", percept.grabber_rectangle.width);
        } else {
            percept.config.setProperty("grabber_location.x", percept.screen_grabber_frame.getLocation().x);
            percept.config.setProperty("grabber_location.y", percept.screen_grabber_frame.getLocation().y);
            percept.config.setProperty("grabber_size.width", percept.screen_grabber_frame.getSize().width);
            percept.config.setProperty("grabber_size.height", percept.screen_grabber_frame.getSize().height);
            percept.config.setProperty("grabber_rectangle_width", percept.grabber_rectangle.width);
            percept.config.setProperty("grabber_rectangle_height", percept.grabber_rectangle.height);
            try {
                percept.config.save();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
        setVisible(false);
        percept.grab_screen(percept.grabber_rectangle);
    }


    private static class JContentPane extends JPanel {

        @Override
        protected void paintComponent(Graphics gr) {
            int w = getWidth(), h = getHeight();
            Graphics2D g = (Graphics2D) gr;
            g.setComposite(AlphaComposite.Src);
            g.setColor(backgroundColor);
            g.fillRect(0, 0, w, h);
            g.setColor(Color.BLACK);
            g.drawRect(1, 1, w - 3, h - 3);
            g.drawRect(2, 2, w - 5, h - 5);
            g.drawRect(3, 3, w - 7, h - 7);

            g.setColor(Color.WHITE);
            float dash_phase = (System.nanoTime() % 1000000000) / 10000000;
            BasicStroke s = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[]{5f, 5f}, dash_phase);
            g.setStroke(s);
            g.drawRect(2, 2, w - 5, h - 5);
            repaint(100);
        }
    }


    private static class EraseBorder implements Border {

        private final Insets insets;

        public EraseBorder(Insets insets) {
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) gr;
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(0x000000, true));
            g.fillRect(x, y, width, height);
            g.setComposite(AlphaComposite.SrcOver);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return (Insets) insets.clone();
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }


    private class Handler implements MouseListener, MouseMotionListener {

        private Point prevp;
        private int region;


        private int getRegion(MouseEvent e) {
            Point p = getLocationOnRootPane(e);

            int w = getWidth(), h = getHeight();
            if (p.x < dragInsets.left) {
                if (p.y < dragInsets.top) {
                    return SwingConstants.NORTH_WEST;
                } else if (p.y > h - dragInsets.bottom) {
                    return SwingConstants.SOUTH_WEST;
                } else {
                    return SwingConstants.WEST;
                }
            } else if (p.x > w - dragInsets.right) {
                if (p.y < dragInsets.top) {
                    return SwingConstants.NORTH_EAST;
                } else if (p.y > h - dragInsets.bottom) {
                    return SwingConstants.SOUTH_EAST;
                } else {
                    return SwingConstants.EAST;
                }
            } else if (p.y < dragInsets.top) {
                return SwingConstants.NORTH;
            } else if (p.y > h - dragInsets.bottom) {
                return SwingConstants.SOUTH;
            }
            return SwingConstants.CENTER;
        }

        private Point getLocationOnRootPane(MouseEvent e) {
            Point mp = e.getLocationOnScreen();
            Point rp = getRootPane().getLocationOnScreen();
            mp.x -= rp.x;
            mp.y -= rp.y;
            return mp;
        }


        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            prevp = getLocationOnRootPane(e);
            prevp = e.getLocationOnScreen();
            region = getRegion(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getLocationOnScreen();
            Point l = getLocation();
            Dimension s = getSize();
            Point d = new Point(p.x - prevp.x, p.y - prevp.y);

            switch (region) {
                case SwingConstants.NORTH:
                    setLocation(l.x, l.y + d.y);
                    setSize(s.width, max(minSize.height, s.height - d.y));
                    break;
                case SwingConstants.SOUTH:
                    setLocation(l.x, min(l.y + d.y + s.height - minSize.height, l.y));
                    setSize(s.width, max(minSize.height, s.height + d.y));
                    break;
                case SwingConstants.WEST:
                    setLocation(l.x + d.x, l.y);
                    setSize(max(minSize.width, s.width - d.x), s.height);
                    break;
                case SwingConstants.EAST:
                    setLocation(min(l.x + d.x + s.width - minSize.width, l.x), l.y);
                    setSize(max(minSize.width, s.width + d.x), s.height);
                    break;
                case SwingConstants.NORTH_EAST:
                    setLocation(min(l.x + d.x + s.width - minSize.width, l.x), l.y + d.y);
                    setSize(max(minSize.width, s.width + d.x), max(minSize.height, s.height - d.y));
                    break;
                case SwingConstants.SOUTH_EAST:
                    setLocation(min(l.x + d.x + s.width - minSize.width, l.x), min(l.y + d.y + s.height - minSize.height, l.y));
                    setSize(max(minSize.width, s.width + d.x), max(minSize.height, s.height + d.y));
                    break;
                case SwingConstants.NORTH_WEST:
                    setLocation(l.x + d.x, l.y + d.y);
                    setSize(max(minSize.width, s.width - d.x), max(minSize.height, s.height - d.y));
                    break;
                case SwingConstants.SOUTH_WEST:
                    setLocation(l.x + d.x, min(l.y + d.y + s.height - minSize.height, l.y));
                    setSize(max(minSize.width, s.width - d.x), max(minSize.height, s.height + d.y));
                    break;
                case SwingConstants.CENTER:
                    setLocation(l.x + d.x, l.y + d.y);
                    break;
                default:
                    break;
            }
            prevp = p;

            updateLabel();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int r = getRegion(e);
            switch (r) {
                case SwingConstants.NORTH:
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    break;
                case SwingConstants.SOUTH:
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                    break;
                case SwingConstants.WEST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                    break;
                case SwingConstants.EAST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    break;
                case SwingConstants.NORTH_EAST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    break;
                case SwingConstants.SOUTH_EAST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    break;
                case SwingConstants.NORTH_WEST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    break;
                case SwingConstants.SOUTH_WEST:
                    setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                    break;
                case SwingConstants.CENTER:
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    break;
                default:
                    setCursor(Cursor.getDefaultCursor());
            }
        }
    }


    private class FormListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == closeButton) {
                ScreenGrabber.this.closeButtonPerformed();
            }
        }

    }


}
