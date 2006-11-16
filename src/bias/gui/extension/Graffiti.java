/**
 * Created on Oct 28, 2006
 */
package bias.gui.extension;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;


import com.sun.image.codec.jpeg.ImageFormatException;

/**
 * @author kion
 */

@Extension.Annotation(
        name = "Graffiti",
        version="0.1.1",
        description = "Simple painting component with basic features",
        author="kion")
public class Graffiti extends Extension {

    private static final long serialVersionUID = 1L;
    
    private static final String IMG_FORMAT = "PNG";
    
    private static final ImageIcon ICON_PAINT_BRUSH = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/paint_brush.png"));

    private static final ImageIcon ICON_LIVE_BRUSH = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/live_brush.png"));

    private static final ImageIcon ICON_ERASER = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/eraser.png"));

    private static final ImageIcon ICON_COLOR = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/color.png"));

    private class PaintingPanel extends JPanel implements MouseListener, MouseMotionListener {

        private static final long serialVersionUID = 1L;
        
        Dimension dimension;

        BufferedImage backbuffer;

        Graphics backg;
        
        public PaintingPanel(Dimension dimension) {
            super();
            this.dimension = dimension;
            init();
        }
        
        public void init() {
            setPreferredSize(dimension);
            setMinimumSize(dimension);
            setMaximumSize(dimension);
            backbuffer = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
            backg = backbuffer.getGraphics();
            clear();
            currentBrush = getSimpleBrushInstance();
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void mouseClicked(MouseEvent e) {
            if (currentBrush != null) {
                currentBrush.mousePaint(backg, e);
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
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            if (currentBrush != null) {
                currentBrush.mousePaint(backg, e);
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
            backg = backbuffer.getGraphics();
        }
        
    }
    
    private PaintingPanel pp;
    
    private Color currentColor = Color.black;
    
    private static interface Brush {
        void mousePaint(Graphics graphics, MouseEvent event);
    }
    
    private Brush currentBrush;
    
    private PaintBrush paintBrush;
    
    private PaintBrush getSimpleBrushInstance() {
        if (paintBrush == null) {
            paintBrush = new PaintBrush();
        }
        return paintBrush;
    }
    
    private class PaintBrush implements Brush {
        
        /* (non-Javadoc)
         * @see drafts.Painting.PaintingTool#paint(java.awt.Image, java.awt.event.MouseEvent)
         */
        public void mousePaint(Graphics graphics, MouseEvent event) {
            int x = event.getX();
            int y = event.getY();
            graphics.setColor(currentColor);
            graphics.fillOval(x - 5, y - 5, 10, 10);
        }

    }
    
    private LiveBrush liveBrush;
    
    private LiveBrush getLiveBrushInstance() {
        if (liveBrush == null) {
            liveBrush = new LiveBrush();
        }
        return liveBrush;
    }
    
    private class LiveBrush implements Brush {

        private int mx, my;

        private double t = 0;
        
        /* (non-Javadoc)
         * @see drafts.Painting.PaintingTool#paint(java.awt.Image, java.awt.event.MouseEvent)
         */
        public void mousePaint(Graphics graphics, MouseEvent event) {
            int x = event.getX();
            int y = event.getY();
            int dx = x - mx;
            int dy = y - my;
            t += Math.sqrt(dx * dx + dy * dy) / 20;
            if (t > 2 * Math.PI) {
                t -= 2 * Math.PI;
            }
            graphics.setColor(currentColor);
            graphics.drawLine(x, y, x + (int) (15 * Math.cos(t)), y + (int) (15 * Math.sin(t)));
            mx = x;
            my = y;
        }

    }
    
    private JToolBar jToolBar = null;
    private JButton jButton = null;
    private JButton jButton1 = null;
    private JButton jButton4 = null;

    private JButton jButton2 = null;

    /**
     * This is the default constructor
     * @throws IOException 
     * @throws ImageFormatException 
     * @throws IOException 
     * @throws ImageFormatException 
     */
    public Graffiti(UUID id, byte[] data) throws ImageFormatException, IOException {
        super(id, data);
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     * @throws IOException 
     * @throws ImageFormatException 
     */
    private void initialize() throws ImageFormatException, IOException {
        this.setLayout(new BorderLayout());  // Generated
        this.add(getJToolBar(), BorderLayout.SOUTH);  // Generated
        JPanel panel = new JPanel(new GridBagLayout());
        Dimension d = new Dimension(300,300);
        JPanel cp = new JPanel();
        pp = new PaintingPanel(d);
        cp.add(pp);
        cp.setBorder(new LineBorder(Color.black));
        panel.add(cp);
        this.add(new JScrollPane(panel), BorderLayout.CENTER);  // Generated
                
        if (getData() != null && getData().length > 0) {
            ByteArrayInputStream bais = new ByteArrayInputStream(getData());
            BufferedImage image = ImageIO.read(bais);
            pp.setImage(image);
        }
        
    }

    /* (non-Javadoc)
     * @see bias.gui.Extension#serialize()
     */
    @Override
    public byte[] serialize() throws ImageFormatException, IOException {
        BufferedImage image = pp.getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, IMG_FORMAT, baos);
        return baos.toByteArray();
    }

    /**
     * This method initializes jToolBar 
     *  
     * @return javax.swing.JToolBar 
     */
    private JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new JToolBar();
            jToolBar.setFloatable(false);  // Generated
            jToolBar.add(getJButton());  // Generated
            jToolBar.add(getJButton1());  // Generated
            jToolBar.add(getJButton4());  // Generated
            jToolBar.add(getJButton2());  // Generated
        }
        return jToolBar;
    }

    /**
     * This method initializes jButton  
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setToolTipText("paint brush");  // Generated
            jButton.setIcon(ICON_PAINT_BRUSH);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    currentBrush = getSimpleBrushInstance();
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jButton1 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setToolTipText("live brush");  // Generated
            jButton1.setIcon(ICON_LIVE_BRUSH);
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    currentBrush = getLiveBrushInstance();
                }
            });
        }
        return jButton1;
    }

    /**
     * This method initializes jButton4 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton();
            jButton4.setToolTipText("choose color");  // Generated
            jButton4.setIcon(ICON_COLOR);
            jButton4.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    currentColor = JColorChooser.showDialog(Graffiti.this, "select text color", Color.BLACK);
                }
            });
        }
        return jButton4;
    }

    /**
     * This method initializes jButton2	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setToolTipText("clear canvas");  // Generated
            jButton2.setIcon(ICON_ERASER);
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    pp.clear();
                }
            });
        }
        return jButton2;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"