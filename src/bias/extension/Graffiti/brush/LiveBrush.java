/**
 * Created on Feb 27, 2007
 */
package bias.extension.Graffiti.brush;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * @author kion
 */
public class LiveBrush extends Brush {

    private int mx, my;

    private double t = 0;
    
    /* (non-Javadoc)
     * @see bias.extension.Graffiti.brush.Brush#mousePaint(java.awt.Graphics2D, java.awt.event.MouseEvent, java.awt.Color)
     */
    public void mousePaint(Graphics2D graphics, MouseEvent event, Color color) {
        int x = event.getX();
        int y = event.getY();
        int dx = x - mx;
        int dy = y - my;
        t += Math.sqrt(dx * dx + dy * dy) / 20;
        if (t > 2 * Math.PI) {
            t -= 2 * Math.PI;
        }
        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(color);
        graphics.drawLine(x, y, x + (int) (15 * Math.cos(t)), y + (int) (15 * Math.sin(t)));
        mx = x;
        my = y;
    }

}