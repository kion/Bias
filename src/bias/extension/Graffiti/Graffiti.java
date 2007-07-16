/**
 * Created on Oct 28, 2006
 */
package bias.extension.Graffiti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
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

import bias.annotation.AddOnAnnotation;
import bias.extension.EntryExtension;
import bias.extension.Graffiti.brush.LiveBrush;
import bias.extension.Graffiti.brush.PaintBrush;

import com.sun.image.codec.jpeg.ImageFormatException;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1.1",
        author="kion",
        description = "Simple painting component")
public class Graffiti extends EntryExtension {

    private static final long serialVersionUID = 1L;
    
    private static final String IMG_FORMAT = "PNG";
    
    private static final ImageIcon ICON_PAINT_BRUSH = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/Graffiti/paint_brush.png"));

    private static final ImageIcon ICON_LIVE_BRUSH = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/Graffiti/live_brush.png"));

    private static final ImageIcon ICON_ERASER = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/Graffiti/eraser.png"));

    private static final ImageIcon ICON_COLOR = 
        new ImageIcon(Graffiti.class.getResource("/bias/res/Graffiti/color.png"));

    private PaintingPanel pp;
    
    private PaintBrush paintBrush;
    
    private LiveBrush liveBrush;
    
    public PaintBrush getSimpleBrushInstance() {
        if (paintBrush == null) {
            paintBrush = new PaintBrush();
        }
        return paintBrush;
    }
    
    public LiveBrush getLiveBrushInstance() {
        if (liveBrush == null) {
            liveBrush = new LiveBrush();
        }
        return liveBrush;
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
    public Graffiti(UUID id, byte[] data, byte[] settings) throws ImageFormatException, IOException {
        super(id, data, settings);
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
        pp = new PaintingPanel(d, getSimpleBrushInstance(), Color.BLACK);
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
     * @see bias.gui.Extension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
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
                    pp.setCurrentBrush(getSimpleBrushInstance());
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
                    pp.setCurrentBrush(getLiveBrushInstance());
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
                    pp.setCurrentColor(JColorChooser.showDialog(Graffiti.this, "select text color", Color.BLACK));
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
