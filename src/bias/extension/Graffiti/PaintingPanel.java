/**
 * Created on Feb 27, 2007
 */
package bias.extension.Graffiti;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import bias.extension.Graffiti.brush.Brush;

/**
 * @author kion
 */
public class PaintingPanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;
    
    private Dimension dimension;

    private BufferedImage backbuffer;

    private Graphics2D backg;
    
    private Color currentColor;
    
    private Brush currentBrush;
    
    public PaintingPanel(Dimension dimension, Brush brush, Color color) {
        super();
        this.dimension = dimension;
        this.currentBrush = brush;
        this.currentColor = color;
        init();
    }
    
    public Brush getCurrentBrush() {
        return currentBrush;
    }

    public void setCurrentBrush(Brush currentBrush) {
        this.currentBrush = currentBrush;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }
    
    public void init() {
        setPreferredSize(dimension);
        setMinimumSize(dimension);
        setMaximumSize(dimension);
        backbuffer = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        backg = (Graphics2D) backbuffer.getGraphics();
        clear();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void mouseClicked(MouseEvent e) {
        if (currentBrush != null && currentColor != null) {
            currentBrush.mousePaint(backg, e, currentColor);
        }
        repaint();
        e.consume();
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
        if (currentBrush != null && currentColor != null) {
            currentBrush.stopPaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (currentBrush != null && currentColor != null) {
            currentBrush.mousePaint(backg, e, currentColor);
        }
        repaint();
        e.consume();
    }

    public void update(Graphics g) {
        g.drawImage(backbuffer, 0, 0, this);
    }

    public void paint(Graphics g) {
        update(g);
    }
    
    public void clear() {
        backg.setColor(Color.white);
        backg.fillRect(0, 0, dimension.width, dimension.height);
        backg.setColor(currentColor);
        repaint();
    }

    public BufferedImage getImage() {
        return backbuffer;
    }
    
    public void setImage(BufferedImage image) {
        backbuffer = image;
        backg = (Graphics2D) backbuffer.getGraphics();
    }

}
