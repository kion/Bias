/**
 * Created on Oct 24, 2006
 */
package bias.extension.PlainText;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import bias.annotation.AddOnAnnotation;
import bias.extension.Extension;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="1.3",
        author="kion",
        description = "Simple plain text editor")
public class PlainText extends Extension {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_SWITCH_MODE = 
        new ImageIcon(PlainText.class.getResource("/bias/res/PlainText/switch_mode.png"));
    
    private static final String PROPERTY_FONT_FAMILY = "FONT_FAMILY";
    
    private static final String PROPERTY_FONT_SIZE = "FONT_SIZE";
    
    private static final int DEFAULT_FONT_SIZE = 12;
    
    private static final String DEFAULT_FONT_FAMILY = "SansSerif";
    
    private static final String[] FONT_FAMILY_NAMES = new String[] { "SansSerif", "Serif", "Monospaced" };

    private static class UndoRedoManager extends UndoManager implements DocumentListener {

        private static final long serialVersionUID = 1L;

        public CompoundEdit compoundEdit;

        private JTextComponent editor;

        private int lastOffset;

        private int lastLength;

        public UndoRedoManager(JTextComponent editor) {
            this.editor = editor;
            this.editor.addKeyListener(new UndoRedoKeyListener());
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.UndoManager#undo()
         */
        public void undo() {
            super.undo();
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.UndoManager#redo()
         */
        public void redo() {
            super.redo();
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.UndoManager#undoableEditHappened(javax.swing.event.UndoableEditEvent)
         */
        public void undoableEditHappened(UndoableEditEvent e) {
            if (compoundEdit == null) {
                compoundEdit = startCompoundEdit(e.getEdit());
                lastLength = editor.getDocument().getLength();
                return;
            }

            AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) e.getEdit();

            if (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
                compoundEdit.addEdit(e.getEdit());
                return;
            }

            int offsetChange = editor.getCaretPosition() - lastOffset;
            int lengthChange = editor.getDocument().getLength() - lastLength;

            if (Math.abs(offsetChange) == 1 && Math.abs(lengthChange) == 1) {
                compoundEdit.addEdit(e.getEdit());
                lastOffset = editor.getCaretPosition();
                lastLength = editor.getDocument().getLength();
                return;
            }

            compoundEdit.end();
            compoundEdit = startCompoundEdit(e.getEdit());
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(final DocumentEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    int offset = e.getOffset() + e.getLength();
                    offset = Math.min(offset, editor.getDocument().getLength());
                    editor.setCaretPosition(offset);
                }
            });
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e) {
            editor.setCaretPosition(e.getOffset());
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e) {}

        private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
            lastOffset = editor.getCaretPosition();
            lastLength = editor.getDocument().getLength();

            compoundEdit = new CustomCompoundEdit();
            compoundEdit.addEdit(anEdit);

            addEdit(compoundEdit);
            return compoundEdit;
        }

        private class CustomCompoundEdit extends CompoundEdit {

            private static final long serialVersionUID = 1L;
            
            /* (non-Javadoc)
             * @see javax.swing.undo.CompoundEdit#isInProgress()
             */
            public boolean isInProgress() {
                return false;
            }
            
            /* (non-Javadoc)
             * @see javax.swing.undo.CompoundEdit#undo()
             */
            public void undo() throws CannotUndoException {
                if (compoundEdit != null) {
                    compoundEdit.end();
                }
                super.undo();
                compoundEdit = null;
            }
            
        }

        private class UndoRedoKeyListener extends KeyAdapter {
            
            /* (non-Javadoc)
             * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
             */
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (editor.isEditable()) {
                    if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_Z) {
                        if (canUndo()) {
                            undo();
                        }
                    } else if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_Y) {
                        if (canRedo()) {
                            redo();
                        }
                    }
                }
            };
            
        }

    }
    
    private String currentFontFamily = DEFAULT_FONT_FAMILY;
    private int currentFontSize = DEFAULT_FONT_SIZE;

    private JScrollPane jScrollPane = null;
    private JTextArea jTextArea = null;
    private JToolBar jToolBar = null;
    private JToggleButton jToggleButton = null;
    private JButton jButton1 = null;
    private JButton jButton2 = null;
    private JComboBox jComboBox = null;

    /**
     * Default constructor
     */
    public PlainText(UUID id, byte[] data, byte[] settings) {
        super(id, data, settings);
        initialize();
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        JLabel ffLb = new JLabel("Default font family:");
        JComboBox ffCb = new JComboBox();
        for (String ff : FONT_FAMILY_NAMES) {
            ffCb.addItem(ff);
        }
        String selValue = newSettings.getProperty(PROPERTY_FONT_FAMILY);
        if (selValue == null) {
            selValue = DEFAULT_FONT_FAMILY;
        }
        ffCb.setSelectedItem(selValue);
        JLabel fsLb = new JLabel("Default font size:");
        selValue = newSettings.getProperty(PROPERTY_FONT_SIZE);
        int sel = DEFAULT_FONT_SIZE;
        if (selValue != null) {
            sel = Integer.valueOf(selValue);
        }
        final JLabel cfsLb = new JLabel("" + sel);
        final JSlider fsSl = new JSlider(1, 48, sel);
        fsSl.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                cfsLb.setText("" + fsSl.getValue());
            }
        });
        JOptionPane.showMessageDialog(
                FrontEnd.getInstance(), 
                new Component[]{ffLb, ffCb, fsLb, fsSl, cfsLb}, 
                "Settings for " + this.getClass().getSimpleName() + " extension", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_FONT_FAMILY, (String) ffCb.getSelectedItem());
        newSettings.setProperty(PROPERTY_FONT_SIZE, "" + fsSl.getValue());
        return PropertiesUtils.serializeProperties(newSettings);
    }

    /* (non-Javadoc)
     * @see bias.gui.Extension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        Properties settings = new Properties();
        settings.setProperty(PROPERTY_FONT_FAMILY, currentFontFamily);
        settings.setProperty(PROPERTY_FONT_SIZE, "" + currentFontSize);
        return PropertiesUtils.serializeProperties(settings);
    }
    
    /* (non-Javadoc)
     * @see bias.gui.Extension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        return getJTextArea().getText().getBytes();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(733, 515);
        this.setLayout(new BorderLayout());
        Properties settings = PropertiesUtils.deserializeProperties(getSettings());
        String cff = settings.getProperty(PROPERTY_FONT_FAMILY);
        if (cff != null) {
            currentFontFamily = cff;
        } else {
            currentFontFamily = DEFAULT_FONT_FAMILY;
        }
        String cfs = settings.getProperty(PROPERTY_FONT_SIZE);
        if (cfs != null) {
            currentFontSize = Integer.valueOf(cfs);
        } else {
            currentFontSize = DEFAULT_FONT_SIZE;
        }
        if (getData() != null) {
            getJTextArea().setText(new String(getData()));
        }
        Font font = new Font(currentFontFamily, Font.PLAIN, currentFontSize);
        getJTextArea().setFont(font);
        getJTextArea().getDocument().addUndoableEditListener(new UndoRedoManager(jTextArea));
        this.add(getJScrollPane(), BorderLayout.CENTER);  // Generated
        this.add(getJToolBar(), BorderLayout.SOUTH);  // Generated
    }

    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());  // Generated
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea    
     *  
     * @return javax.swing.JTextArea    
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea(){
                private static final long serialVersionUID = 1L;
                @Override
                public void paint(Graphics g) {
                    // enable font anti-aliasing
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    super.paint(g);
                }
            };
            jTextArea.setLineWrap(true);
            jTextArea.setWrapStyleWord(true);
            jTextArea.setEditable(false);
        }
        return jTextArea;
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
            jToolBar.add(getJToggleButton());  // Generated
            jToolBar.add(getJButton1());  // Generated
            jToolBar.add(getJButton2());  // Generated
            jToolBar.add(getJComboBox());  // Generated
        }
        return jToolBar;
    }

    /**
     * This method initializes jToggleButton    
     *  
     * @return javax.swing.JToggleButton    
     */
    private JToggleButton getJToggleButton() {
        if (jToggleButton == null) {
            jToggleButton = new JToggleButton();
            jToggleButton.setToolTipText("switch mode");  // Generated
            jToggleButton.setIcon(PlainText.ICON_SWITCH_MODE);
            jToggleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJTextArea().setEditable(!getJTextArea().isEditable());
                    if (getJTextArea().isEditable()) {
                        getJTextArea().requestFocusInWindow();
                    }
                }
            });
        }
        return jToggleButton;
    }

    /**
     * This method initializes jButton1    
     *  
     * @return javax.swing.JButton    
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setToolTipText("increase font size");  // Generated
            jButton1.setText("+");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    currentFontSize += 1;
                    Font font = new Font(currentFontFamily, Font.PLAIN, currentFontSize);
                    getJTextArea().setFont(font);
                    if (getJTextArea().isEditable()) {
                        getJTextArea().requestFocusInWindow();
                    }
                }
            });
        }
        return jButton1;
    }

    /**
     * This method initializes jButton2    
     *  
     * @return javax.swing.JButton    
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setToolTipText("decrease font size");  // Generated
            jButton2.setText("-");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    currentFontSize -= 1;
                    Font font = new Font(currentFontFamily, Font.PLAIN, currentFontSize);
                    getJTextArea().setFont(font);
                    if (getJTextArea().isEditable()) {
                        getJTextArea().requestFocusInWindow();
                    }
                }
            });
        }
        return jButton2;
    }

    /**
     * This method initializes jComboBox   
     *  
     * @return javax.swing.JComboBox    
     */
    private JComboBox getJComboBox() {
        if (jComboBox == null) {
            jComboBox = new JComboBox();
            jComboBox.setMaximumSize(new Dimension(150, 20));  // Generated
            jComboBox.setPreferredSize(new Dimension(150, 20));  // Generated
            jComboBox.setToolTipText("font family");  // Generated
            jComboBox.setMinimumSize(new Dimension(150, 20));  // Generated
            for (int i = 0; i < FONT_FAMILY_NAMES.length; i++) {
                jComboBox.addItem(FONT_FAMILY_NAMES[i]);
            }
            jComboBox.setSelectedItem(currentFontFamily);
            jComboBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    currentFontFamily = (String) getJComboBox().getSelectedItem();
                    Font font = new Font(currentFontFamily, Font.PLAIN, currentFontSize);
                    getJTextArea().setFont(font);
                    if (getJTextArea().isEditable()) {
                        getJTextArea().requestFocusInWindow();
                    }
                }
            });
        }
        return jComboBox;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
