package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

public class JDiceToggleButton extends JToggleButton implements YahtzeeGame.GameStateListener {
    private YahtzeeGame game;
    private int die;

    public JDiceToggleButton(YahtzeeGame game, int die) {
        super();
        this.game = game;
        this.die = die;
        game.addGameStateListener(this);
        setHideActionText(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var hints = new HashMap<>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).addRenderingHints(hints);
        new DicePainter().paintDice(g, new Rectangle2D.Double(0, 0, getWidth(), getHeight()), game.getDice().getDie(die));
    }

    @Override
    public void gameStateChanged() {
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 100);
    }
}
