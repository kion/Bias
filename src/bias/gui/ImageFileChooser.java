/**
 * Created on Jan 27, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;

/**
 * @author kion
 */
public class ImageFileChooser extends JFileChooser implements ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    
    private static final int PREVIEW_WIDTH = 150;

    private static final int PREVIEW_HEIGHT = 150;

    private JCheckBox previewCheckBox;

    private ImageViewPanel imageViewPanel = new ImageViewPanel();

    public ImageFileChooser(boolean multiselectionEnabled) {
        super();
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(imageViewPanel, BorderLayout.CENTER);
        previewCheckBox = new JCheckBox("Preview");
        previewCheckBox.setSelected(true);
        previewCheckBox.addActionListener(this);
        previewPanel.add(previewCheckBox, BorderLayout.SOUTH);
        previewPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        previewPanel.setBorder(new EtchedBorder());
        setFileSelectionMode(FILES_ONLY);
        setFileFilter(imageFileFilter);
        setAccessory(previewPanel);
        if (multiselectionEnabled) {
            setMultiSelectionEnabled(multiselectionEnabled);
            addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, this);
        } else {
            addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, this);
        }
    }

    /* 
     * (non-Javadoc)
     * 
     * @see javax.swing.JFileChooser#setMultiSelectionEnabled(boolean)
     */
    @Override
    public void setMultiSelectionEnabled(boolean b) {
        if (b) {
            removePropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, this);
            addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, this);
        } else {
            removePropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, this);
            addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, this);
        }
        super.setMultiSelectionEnabled(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        showPreview();
    }

    /* 
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        previewCheckBox.setSelected(!previewCheckBox.isSelected());
        if (previewCheckBox.isSelected()) {
            showPreview();
        } else {
            imageViewPanel.setImage(null);
        }
        
    }
    
    private void showPreview(){
        File file = null;
        if (isMultiSelectionEnabled()) {
            File[] selectedFiles = getSelectedFiles();
            if (selectedFiles.length > 0) {
                int idx = selectedFiles.length - 1;
                file = selectedFiles[idx];
            }
        } else {
            file = getSelectedFile();
        }
        if (previewCheckBox.isSelected() && file != null && !file.isDirectory()) {
            ImageIcon ii = new ImageIcon(file.getAbsolutePath());
            imageViewPanel.setImage(ii.getImage());
        } else {
            imageViewPanel.setImage(null);
        }
    }

    private FileFilter imageFileFilter = new FileFilter() {

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {
            return (f.isDirectory()
                    || (f.isFile() && f.getName().toLowerCase().endsWith(".jpg"))
                    || (f.isFile() && f.getName().toLowerCase().endsWith(".jpeg"))
                    || (f.isFile() && f.getName().toLowerCase().endsWith(".gif"))
                    || (f.isFile() && f.getName().toLowerCase().endsWith(".png")));
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        public String getDescription() {
            return "JPG/GIF/PNG image file";
        }

    };

    private class ImageViewPanel extends JPanel {

        private static final long serialVersionUID = 438944019406000615L;

        private Image image;

        /**
         * Sets new image to view and repaints the component
         * 
         * @param image
         *            image to view
         */
        public void setImage(Image image) {
            if (image != null) {
                int previewWidth;
                int previewHeight;
                int imWidth = image.getWidth(this);
                int imHeight = image.getHeight(this);
                if (imWidth > PREVIEW_WIDTH || imHeight > PREVIEW_HEIGHT) {
                    if (imWidth >= imHeight){
                        previewHeight = (int)(imHeight/((float)imWidth/PREVIEW_WIDTH));
                        previewWidth = PREVIEW_WIDTH;
                    } else {
                        previewWidth = (int)(imWidth/((float)imHeight/PREVIEW_HEIGHT));
                        previewHeight = PREVIEW_HEIGHT;
                    }
                } else {
                    previewWidth = imWidth;
                    previewHeight = imHeight;
                }
                this.image = image.getScaledInstance(previewWidth, previewHeight, Image.SCALE_SMOOTH);
            } else {
                this.image = null;
            }
            revalidate();
            repaint();
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(image, 0, 0, this);
        }

    }

}
