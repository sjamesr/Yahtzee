package org.example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class DicePainter {
    private static final double PADDING = 0.1;

    void paintDice(Graphics g, Rectangle2D rect, int value) {
        int sizeX = (int) (rect.getWidth() / 5.0);
        int sizeY = (int) (rect.getHeight() / 5.0);
        switch (value) {
            case 1:
                drawCenterSpot(g, rect, sizeX, sizeY);
                break;
            case 2:
                drawTopLeftSpot(g, rect, sizeX, sizeY);
                drawBottomRightSpot(g, rect, sizeX, sizeY);
                break;
            case 3:
                drawCenterSpot(g, rect, sizeX, sizeY);
                drawTopLeftSpot(g, rect, sizeX, sizeY);
                drawBottomRightSpot(g, rect, sizeX, sizeY);
                break;
            case 4:
                drawTopLeftSpot(g, rect, sizeX, sizeY);
                drawBottomLeftSpot(g, rect, sizeX, sizeY);
                drawBottomRightSpot(g, rect, sizeX, sizeY);
                drawTopRightSpot(g, rect, sizeX, sizeY);
                break;
            case 5:
                drawTopLeftSpot(g, rect, sizeX, sizeY);
                drawBottomLeftSpot(g, rect, sizeX, sizeY);
                drawBottomRightSpot(g, rect, sizeX, sizeY);
                drawTopRightSpot(g, rect, sizeX, sizeY);
                drawCenterSpot(g, rect, sizeX, sizeY);
                break;
            case 6:
                drawTopLeftSpot(g, rect, sizeX, sizeY);
                drawBottomLeftSpot(g, rect, sizeX, sizeY);
                drawBottomRightSpot(g, rect, sizeX, sizeY);
                drawTopRightSpot(g, rect, sizeX, sizeY);
                drawLeftMiddleSpot(g, rect, sizeX, sizeY);
                drawRightMiddleSpot(g, rect, sizeX, sizeY);
                break;
            default:
                g.drawString( value + " ????? ", 10, 10);
        }
    }

    private void drawCenterSpot(Graphics g, Rectangle2D rect, int sizeX, int sizeY) {
        drawSpot(g, new Point2D.Double(rect.getCenterX() - (sizeX / 2.0), rect.getCenterY() - (sizeY / 2.0)), sizeX, sizeY);
    }

    private void drawTopLeftSpot(Graphics g, Rectangle2D rect, int sizeX, int sizeY) {
        drawSpot(g, new Point2D.Double(rect.getWidth() * PADDING, rect.getHeight() * PADDING), sizeX, sizeY);
    }

    private void drawBottomRightSpot(Graphics g, Rectangle2D rect, int sizeX, int sizeY) {
        drawSpot(g, new Point2D.Double(7 * rect.getWidth() * PADDING, 7 * rect.getHeight() * PADDING), sizeX, sizeY);
    }

    private void drawBottomLeftSpot(Graphics g, Rectangle2D rect, int sizeX, int sizeY) {
        drawSpot(g, new Point2D.Double(rect.getWidth() * PADDING, 7 * rect.getHeight() * PADDING), sizeX, sizeY);
    }

    private void drawTopRightSpot(Graphics g, Rectangle2D rect, int sizeX, int sizeY) {
        drawSpot(g, new Point2D.Double(7 * rect.getWidth() * PADDING, rect.getHeight() * PADDING), sizeX, sizeY);
    }

    private void drawLeftMiddleSpot(Graphics g, Rectangle2D rect, int sizeX, int sizeY) {
        drawSpot(g, new Point2D.Double(rect.getWidth() * PADDING, 4 * rect.getHeight() * PADDING), sizeX, sizeY);
    }

    private void drawRightMiddleSpot(Graphics g, Rectangle2D rect, int sizeX, int sizeY) {
        drawSpot(g, new Point2D.Double(7 * rect.getWidth() * PADDING, 4 * rect.getHeight() * PADDING), sizeX, sizeY);
    }

    private void drawSpot(Graphics g, Point2D center, int sizeX, int sizeY) {
        g.fillOval((int) center.getX(), (int) center.getY(), sizeX, sizeY);
    }

}
