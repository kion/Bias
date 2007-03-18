/**
 * Created on Feb 27, 2007
 */
package bias.extension.Graffiti.brush;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

/**
 * @author kion
 */
public class PaintBrush implements Brush {
    
    /* (non-Javadoc)
     * @see bias.extension.Graffiti.brush.Brush#mousePaint(java.awt.Graphics, java.awt.event.MouseEvent, java.awt.Color)
     */
    public void mousePaint(Graphics graphics, MouseEvent event, Color color) {
        int x = event.getX();
        int y = event.getY();
        graphics.setColor(color);
        graphics.fillOval(x - 5, y - 5, 10, 10);
    }

}