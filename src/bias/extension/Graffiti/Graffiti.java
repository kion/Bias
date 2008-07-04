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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;

import bias.Constants;
import bias.extension.EntryExtension;
import bias.extension.Graffiti.brush.LiveBrush;
import bias.extension.Graffiti.brush.PaintBrush;
import bias.extension.Graffiti.brush.PencilBrush;
import bias.gui.FrontEnd;
import bias.gui.ImageFileChooser;
import bias.utils.CommonUtils;

import com.sun.image.codec.jpeg.ImageFormatException;

/**
 * @author kion
 */

public class Graffiti extends EntryExtension {

    // TODO [P2] implement existing image loading

    private static final long serialVersionUID = 1L;
    
    private static final String IMG_FORMAT = "PNG";
    
    private static final ImageIcon ICON_PENCIL_BRUSH = new ImageIcon(CommonUtils.getResourceURL(Graffiti.class, "pencil_brush.png"));

    private static final ImageIcon ICON_PAINT_BRUSH = new ImageIcon(CommonUtils.getResourceURL(Graffiti.class, "paint_brush.png"));

    private static final ImageIcon ICON_LIVE_BRUSH = new ImageIcon(CommonUtils.getResourceURL(Graffiti.class, "live_brush.png"));

    private static final ImageIcon ICON_ERASER = new ImageIcon(CommonUtils.getResourceURL(Graffiti.class, "eraser.png"));

    private static final ImageIcon ICON_COLOR = new ImageIcon(CommonUtils.getResourceURL(Graffiti.class, "color.png"));
    
    private static final ImageIcon ICON_LOAD_IMAGE = new ImageIcon(CommonUtils.getResourceURL(Graffiti.class, "load_image.png"));

    private static final ImageIcon ICON_SAVE_IMAGE = new ImageIcon(CommonUtils.getResourceURL(Graffiti.class, "save_image.png"));

    // TODO [P2] default dimension should be customizable
    private static final Dimension DEFAULT_DIMENSION = new Dimension(500, 500);

    private static final ImageFileChooser ifc = new ImageFileChooser(false);

    private PaintingPanel pp;
    
    private PencilBrush pencilBrush;
    
    private PaintBrush paintBrush;
    
    private LiveBrush liveBrush;
    
    public PencilBrush getPencilBrushInstance() {
        if (pencilBrush == null) {
            pencilBrush = new PencilBrush();
        }
        return pencilBrush;
    }
    
    public PaintBrush getPaintBrushInstance() {
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

    private JPanel panel = null;
    private JPanel panel2 = null;
    private JToolBar jToolBar = null;
    private JButton jButton = null;
    private JButton jButton3 = null;
    private JButton jButton1 = null;
    private JButton jButton4 = null;
    private JButton jButton2 = null;
    private JButton jButton5 = null;
    private JButton jButton6 = null;

    /**
     * This is the default constructor
     * @throws IOException 
     * @throws ImageFormatException 
     */
    public Graffiti(UUID id, byte[] data, byte[] settings) throws Throwable {
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
        this.setLayout(new BorderLayout());  
        this.add(getJToolBar(), BorderLayout.SOUTH);  

        BufferedImage image = null;
        Dimension dim = DEFAULT_DIMENSION;
        if (getData() != null && getData().length > 0) {
            ByteArrayInputStream bais = new ByteArrayInputStream(getData());
            image = ImageIO.read(bais);
            dim = new Dimension(image.getWidth(), image.getHeight());
        }

        pp = new PaintingPanel(dim, getPencilBrushInstance(), Color.BLACK);
        if (image != null) { 
            pp.setImage(image);
        }
        this.add(new JScrollPane(initPaintingPanelContainer()), BorderLayout.CENTER);  
        
    }
    
    private JPanel initPaintingPanelContainer() {
        if (panel == null || panel2 == null) {
            panel = new JPanel(new GridBagLayout());
            panel2 = new JPanel();
            panel2.setBorder(new LineBorder(Color.black));
        }
        panel.removeAll();
        panel2.removeAll();
        panel2.add(pp);
        panel.add(panel2);
        return panel;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
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
            jToolBar.setFloatable(false);  
            jToolBar.add(getJButton5());  
            jToolBar.add(getJButton6());
            jToolBar.addSeparator();
            jToolBar.add(getJButton3());  
            jToolBar.add(getJButton());  
            jToolBar.add(getJButton1());  
            jToolBar.add(getJButton4());  
            jToolBar.add(getJButton2());  
        }
        return jToolBar;
    }

    /**
     * This method initializes jButton3  
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton();
            jButton3.setToolTipText("pencil brush");  
            jButton3.setIcon(ICON_PENCIL_BRUSH);
            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    pp.setCurrentBrush(getPencilBrushInstance());
                }
            });
        }
        return jButton3;
    }

    /**
     * This method initializes jButton  
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setToolTipText("paint brush");  
            jButton.setIcon(ICON_PAINT_BRUSH);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    pp.setCurrentBrush(getPaintBrushInstance());
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jButton5  
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton5() {
        if (jButton5 == null) {
            jButton5 = new JButton();
            jButton5.setToolTipText("load image from file");  
            jButton5.setIcon(ICON_LOAD_IMAGE);
            jButton5.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (JFileChooser.APPROVE_OPTION == ifc.showOpenDialog(FrontEnd.getActiveWindow())) {
                            BufferedImage image = ImageIO.read(new FileInputStream(ifc.getSelectedFile()));
                            pp.init(new Dimension(image.getWidth(), image.getHeight()));
                            pp.setImage(image);
                            initPaintingPanelContainer();
                        }
                    } catch (Throwable t) {
                        FrontEnd.displayErrorMessage("Failed to load image!", t);
                    }
                }
            });
        }
        return jButton5;
    }

    /**
     * This method initializes jButton6  
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton6() {
        if (jButton6 == null) {
            jButton6 = new JButton();
            jButton6.setToolTipText("save image to file");  
            jButton6.setIcon(ICON_SAVE_IMAGE);
            jButton6.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        if (JFileChooser.APPROVE_OPTION == ifc.showSaveDialog(FrontEnd.getActiveWindow())) {
                            ImageIO.write(pp.getImage(), getFileFormat(ifc.getSelectedFile()), new FileOutputStream(ifc.getSelectedFile()));
                        }
                    } catch (Throwable t) {
                        FrontEnd.displayErrorMessage("Failed to save image!", t);
                    }
                }
            });
        }
        return jButton6;
    }
    
    private String getFileFormat(File f) {
        String format = ifc.getSelectedFile().getName();
        int idx = format.lastIndexOf('.');
        if (idx != -1) {
            format = format.substring(idx+1);
        } else {
            format = Constants.DEFAULT_IMAGE_FORMAT;
        }
        return format;
    }

    /**
     * This method initializes jButton1 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setToolTipText("live brush");  
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
            jButton4.setToolTipText("choose color");  
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
            jButton2.setToolTipText("clear canvas");  
            jButton2.setIcon(ICON_ERASER);
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    pp.clear();
                }
            });
        }
        return jButton2;
    }

}
