package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

public class JDiceToggleButton extends JToggleButton {
    private int val;

    public JDiceToggleButton(int val) {
        super();

        this.val = val;
    }

    public void setValue(int newValue) {
        this.val = newValue;
        repaint();
    }

    public int getValue() {
        return val;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var hints = new HashMap<>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).addRenderingHints(hints);
        new DicePainter().paintDice(g, new Rectangle2D.Double(0, 0, getWidth(), getHeight()), val);
    }
}
