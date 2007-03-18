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
public interface Brush {
    
    void mousePaint(Graphics graphics, MouseEvent event, Color color);
    
}
