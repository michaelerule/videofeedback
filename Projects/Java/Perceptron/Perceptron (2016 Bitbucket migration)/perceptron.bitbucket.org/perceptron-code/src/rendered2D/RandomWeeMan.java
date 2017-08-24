package rendered2D;

/**
 * Perceptron
 *
 * @URL <http://perceptron.sourceforge.net/>
 *
 * @author Michael Everett Rule
 * @author Ben McMillan
 *
 * Random Wee Man courtesy of Ben McMillan.
 *
 */

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class RandomWeeMan // extends JPanel
{

    int frameWidth = 600;
    int frameHeight = 700;
    Point2D.Double[] points1;
    Point2D.Double[] points2;
    Point2D.Double[] points3;
    Point2D.Double[] points;
    double radius1;
    double radius2;
    double radius3;
    double radius;

    public RandomWeeMan(int W, int H) {

        frameWidth = W;
        frameHeight = H;

        points1 = new Point2D.Double[5];
        points2 = new Point2D.Double[5];
        points3 = new Point2D.Double[5];
        points = new Point2D.Double[5];

        for (int i = 0; i < 5; i++) {
            points[i] = new Point2D.Double(W * .5, W * .5);
            points1[i] = new Point2D.Double(W * .5, W * .5);
            points2[i] = new Point2D.Double(W * .5, W * .5);
            points3[i] = new Point2D.Double(W * .5, W * .5);
        }
        radius = radius1 = radius2 = radius3 = 0;
    }

    public void paintComponent(Graphics g) {
        for (int i = 0; i < 4; i++) {
            points1[i] = new Point2D.Double((Math.random() * .25 + (double) i / 4) * frameWidth, (Math.random() * .5 + .5) * frameHeight);
        }
        points1[4] = new Point2D.Double((Math.random() * .5 + .25) * frameWidth, (Math.random() * .25 + .125) * frameHeight);

        radius1 = Math.random() * 50 + 50;

        for (int i = 0; i < 5; i++) {
            points2[i].x = points2[i].x * .9 + .1 * points1[i].x;
            points2[i].y = points2[i].y * .9 + .1 * points1[i].y;
        }
        for (int i = 0; i < 5; i++) {
            points3[i].x = points2[i].x * .9 + .1 * points2[i].x;
            points3[i].y = points2[i].y * .9 + .1 * points2[i].y;
        }
        for (int i = 0; i < 5; i++) {
            points[i].x = points2[i].x * .9 + .1 * points3[i].x;
            points[i].y = points2[i].y * .9 + .1 * points3[i].y;
        }
        radius2 = radius2 * .9 + .1 * radius1;
        radius3 = radius3 * .9 + .1 * radius2;
        radius = radius * .9 + .1 * radius3;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255), 255));
        // g2d.fill( new Rectangle2D.Double( 0 , 0 , frameWidth , frameHeight )
        // );
        for (int i = 0; i < 3; i++) {
            g2d.draw(new Line2D.Double(points[i], points[i + 1]));
        }
        Point2D.Double midpoint = new Point2D.Double((points[1].getX() + points[2].getX()) / 2, (points[1].getY() + points[2].getY()) / 2);
        g2d.draw(new Line2D.Double(midpoint, points[4]));
        g2d.draw(new Ellipse2D.Double(points[4].getX() - radius, points[4].getY() - 2 * radius, 2 * radius, 2 * radius));
    }
}
