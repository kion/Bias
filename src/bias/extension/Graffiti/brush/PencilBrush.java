/**
 * Created on Jul 4, 2008
 */
package bias.extension.Graffiti.brush;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * @author kion
 */
public class PencilBrush extends Brush {
    
    private int x = -1;
    private int y = -1;

    /* (non-Javadoc)
     * @see bias.extension.Graffiti.brush.Brush#mousePaint(java.awt.Graphics2D, java.awt.event.MouseEvent, java.awt.Color)
     */
    public void mousePaint(Graphics2D graphics, MouseEvent event, Color color) {
        if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
            int x = event.getX();
            int y = event.getY();
            if (this.x != -1 && this.y != -1) {
                graphics.setStroke(new BasicStroke(1));
                graphics.setColor(color);
                graphics.drawLine(this.x, this.y, x, y);
            }
            this.x = x;
            this.y = y;
        } else {
            this.x = -1;
            this.y = -1;
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.Graffiti.brush.Brush#stopPaint()
     */
    public void stopPaint() {
        this.x = -1;
        this.y = -1;
    }

}
