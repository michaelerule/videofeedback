package rtf_editor;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class SmallButton extends JButton implements MouseListener {

    protected Border raised;
    protected Border lowered;
    protected Border inactive;

    public SmallButton(Action act, String tip) {

        super((Icon) act.getValue(Action.SMALL_ICON));
        raised = new BevelBorder(BevelBorder.RAISED);
        lowered = new BevelBorder(BevelBorder.LOWERED);
        inactive = new EmptyBorder(2, 2, 2, 2);
        setBorder(inactive);
        setMargin(new Insets(1, 1, 1, 1));
        setToolTipText(tip);
        addActionListener(act);
        addMouseListener(this);
        setRequestFocusEnabled(false);
    }

    public float getAlignmentY() {
        return 0.5f;
    }

    public void mousePressed(MouseEvent e) {
        setBorder(lowered);
    }

    public void mouseReleased(MouseEvent e) {
        setBorder(inactive);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        setBorder(raised);
    }

    public void mouseExited(MouseEvent e) {
        setBorder(inactive);
    }
}

class SmallToggleButton extends JToggleButton implements ItemListener {

    protected Border m_raised;
    protected Border m_lowered;

    public SmallToggleButton(boolean selected, ImageIcon imgUnselected, ImageIcon imgSelected, String tip) {

        super(imgUnselected, selected);
        setHorizontalAlignment(CENTER);
        setBorderPainted(true);
        m_raised = new BevelBorder(BevelBorder.RAISED);
        m_lowered = new BevelBorder(BevelBorder.LOWERED);
        setBorder(selected ? m_lowered : m_raised);
        setMargin(new Insets(1, 1, 1, 1));
        setToolTipText(tip);
        setRequestFocusEnabled(false);
        setSelectedIcon(imgSelected);
        addItemListener(this);
    }

    public float getAlignmentY() {
        return 0.5f;
    }

    public void itemStateChanged(ItemEvent e) {
        setBorder(isSelected() ? m_lowered : m_raised);
    }
}
