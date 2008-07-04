/**
 * Created on Feb 27, 2007
 */
package bias.extension.Graffiti.brush;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * @author kion
 */
public abstract class Brush {
    
    public abstract void mousePaint(Graphics2D graphics, MouseEvent event, Color color);
    
    public void stopPaint() {};
    
}
