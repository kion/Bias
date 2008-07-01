/**
 * Created on Oct 24, 2006
 */
package bias.extension.PlainText;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import bias.extension.EntryExtension;
import bias.gui.FrontEnd;
import bias.utils.CommonUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

public class PlainText extends EntryExtension {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_SWITCH_MODE = new ImageIcon(CommonUtils.getResourceURL(PlainText.class, "switch_mode.png"));
    
    private static final ImageIcon ICON_INCREASE_FONT_SIZE = new ImageIcon(CommonUtils.getResourceURL(PlainText.class, "increase.png"));
    
    private static final ImageIcon ICON_DECREASE_FONT_SIZE = new ImageIcon(CommonUtils.getResourceURL(PlainText.class, "decrease.png"));
    
    private static final String PROPERTY_FONT_SIZE = "FONT_SIZE";
    
    private static final int[] FONT_SIZES = new int[]{ 8, 10, 12, 14, 18, 24, 36 };

    private static final int DEFAULT_FONT_SIZE = FONT_SIZES[2];
    
    private static final String PROPERTY_SCROLLBAR_VERT = "SCROLLBAR_VERT";
    
    private static final String PROPERTY_SCROLLBAR_HORIZ = "SCROLLBAR_HORIZ";
    
    private static final String PROPERTY_CARET_POSITION = "CARET_POSITION";

    private int currentFontSize = DEFAULT_FONT_SIZE;

    private boolean dataChanged = false;
    
    private Properties settings;
    
    private byte[] data;
    
    private JScrollPane jScrollPane = null;
    private JTextPane jTextPane = null;
    private JToolBar jToolBar = null;
    private JToggleButton jToggleButton = null;
    private JButton jButton1 = null;
    private JButton jButton2 = null;

    /**
     * Default constructor
     */
    public PlainText(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        initialize();
    }

    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        JLabel fsLb = new JLabel("Font size:");
        JComboBox fsCb = new JComboBox();
        for (Integer fs : FONT_SIZES) {
            fsCb.addItem("" + fs);
        }
        String selValue = newSettings.getProperty(PROPERTY_FONT_SIZE);
        if (selValue == null) {
            selValue = "" + DEFAULT_FONT_SIZE;
        }
        fsCb.setSelectedItem(selValue);
        int opt = JOptionPane.showConfirmDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{fsLb, fsCb}, 
                "Settings for " + this.getClass().getSimpleName() + " extension", 
                JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            newSettings.setProperty(PROPERTY_FONT_SIZE, (String) fsCb.getSelectedItem());
            return PropertiesUtils.serializeProperties(newSettings);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#applySettings(byte[])
     */
    @Override
    public void applySettings(byte[] settings) {
        this.settings = PropertiesUtils.deserializeProperties(settings);
        String cfs = this.settings.getProperty(PROPERTY_FONT_SIZE);
        if (cfs != null) {
            currentFontSize = Integer.valueOf(cfs);
        } else {
            currentFontSize = DEFAULT_FONT_SIZE;
        }
        setFontSize(new ActionEvent(getJTextPane(), ActionEvent.ACTION_PERFORMED, "Font size"));
        if (currentFontSize == FONT_SIZES[FONT_SIZES.length-1]) {
            getJButton1().setEnabled(false);
        } else if (currentFontSize == FONT_SIZES[0]) {
            getJButton2().setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        settings.setProperty(PROPERTY_FONT_SIZE, "" + currentFontSize);
        JScrollBar sb = getJScrollPane().getVerticalScrollBar();
        if (sb != null && sb.getValue() != 0) {
            settings.setProperty(PROPERTY_SCROLLBAR_VERT, "" + sb.getValue());
        } else {
            settings.remove(PROPERTY_SCROLLBAR_VERT);
        }
        sb = getJScrollPane().getHorizontalScrollBar();
        if (sb != null && sb.getValue() != 0) {
            settings.setProperty(PROPERTY_SCROLLBAR_HORIZ, "" + sb.getValue());
        } else {
            settings.remove(PROPERTY_SCROLLBAR_HORIZ);
        }
        int cp = getJTextPane().getCaretPosition();
        settings.setProperty(PROPERTY_CARET_POSITION, "" + cp);
        return PropertiesUtils.serializeProperties(settings);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        if (dataChanged) {
            data = getJTextPane().getText().getBytes();
            dataChanged = false;
            return data;
        } else {
            return data;
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        Collection<String> searchData = new ArrayList<String>();
        Document doc = getJTextPane().getDocument();
        String text = null;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            // ignore, shouldn't happen ever
        }
        searchData.add(text);
        return searchData;
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(733, 515);
        this.setLayout(new BorderLayout());
        if (getData() != null) {
            data = getData();
            getJTextPane().setText(new String(data));
        }
        applySettings(getSettings());
        JScrollBar sb = getJScrollPane().getVerticalScrollBar();
        if (sb != null) {
            String val = settings.getProperty(PROPERTY_SCROLLBAR_VERT);
            if (val != null) {
                sb.setVisibleAmount(0);
                sb.setValue(sb.getMaximum());
                sb.setValue(Integer.valueOf(val));
            }
        }
        sb = getJScrollPane().getHorizontalScrollBar();
        if (sb != null) {
            String val = settings.getProperty(PROPERTY_SCROLLBAR_HORIZ);
            if (val != null) {
                sb.setVisibleAmount(0);
                sb.setValue(sb.getMaximum());
                sb.setValue(Integer.valueOf(val));
            }
        }
        String caretPos = settings.getProperty(PROPERTY_CARET_POSITION);
        if (!Validator.isNullOrBlank(caretPos)) {
            try {
                getJTextPane().setCaretPosition(Integer.valueOf(caretPos));
            } catch (IllegalArgumentException iae) {
                // ignore incorrect caret positioning
            }
        }
        getJTextPane().getDocument().addUndoableEditListener(new UndoRedoManager(jTextPane));
        getJTextPane().getDocument().addDocumentListener(new DocumentListener(){
            public void changedUpdate(DocumentEvent e) {
                dataChanged();
            }
            public void insertUpdate(DocumentEvent e) {
                dataChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                dataChanged();
            }
            private void dataChanged() {
                if (getJTextPane().isEditable()) {
                    dataChanged = true;
                }
            }
        });
        this.add(getJScrollPane(), BorderLayout.CENTER);  
        this.add(getJToolBar(), BorderLayout.SOUTH);  
    }

    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextPane());  
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextPane    
     *  
     * @return javax.swing.JTextPane    
     */
    private JTextPane getJTextPane() {
        if (jTextPane == null) {
            jTextPane = new JTextPane();
            jTextPane.setEditorKit(new HTMLEditorKit());
            HTMLDocument doc = new HTMLDocument() {
                private static final long serialVersionUID = 1L;
                @Override
                public Font getFont(AttributeSet attr) {
                    Object family = attr.getAttribute(StyleConstants.FontFamily);
                    Object size = attr.getAttribute(StyleConstants.FontSize);
                    if (family == null && size == null) {
                        return new Font("SansSerif", Font.PLAIN, currentFontSize);
                    }
                    return super.getFont(attr);
                }
            };
            doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
            doc.setPreservesUnknownTags(false);
            jTextPane.setStyledDocument(doc);
            jTextPane.setEditable(false);
        }
        return jTextPane;
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
            jToolBar.add(getJToggleButton());  
            jToolBar.add(getJButton1());  
            jToolBar.add(getJButton2());  
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
            jToggleButton.setToolTipText("switch mode");  
            jToggleButton.setIcon(ICON_SWITCH_MODE);
            jToggleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJTextPane().setEditable(!getJTextPane().isEditable());
                    if (getJTextPane().isEditable()) {
                        getJTextPane().requestFocusInWindow();
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
            jButton1.setToolTipText("increase font size");  
            jButton1.setIcon(ICON_INCREASE_FONT_SIZE);
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJButton2().setEnabled(true);
                    for (int i = 0; i < FONT_SIZES.length; i++) {
                        if (FONT_SIZES[i] == currentFontSize && i < FONT_SIZES.length - 1) {
                            currentFontSize = FONT_SIZES[i + 1];
                            if (currentFontSize == FONT_SIZES[FONT_SIZES.length-1]) {
                                getJButton1().setEnabled(false);
                            }
                            break;
                        }
                    }
                    setFontSize(e);
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
            jButton2.setToolTipText("decrease font size");  
            jButton2.setIcon(ICON_DECREASE_FONT_SIZE);
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJButton1().setEnabled(true);
                    for (int i = FONT_SIZES.length - 1; i >= 0; i--) {
                        if (FONT_SIZES[i] == currentFontSize && i > 0) {
                            currentFontSize = FONT_SIZES[i - 1];
                            if (currentFontSize == FONT_SIZES[0]) {
                                getJButton2().setEnabled(false);
                            }
                            break;
                        }
                    }
                    setFontSize(e);
                }
            });
        }
        return jButton2;
    }
    
    private void setFontSize(ActionEvent e) {
        String actionName = "Font size";
        getJTextPane().setSelectionStart(0);
        getJTextPane().setSelectionEnd(getJTextPane().getText().length());
        new StyledEditorKit.FontSizeAction(actionName, currentFontSize).actionPerformed(
                        new ActionEvent(e.getSource(), e.getID(), actionName));
        getJTextPane().requestFocusInWindow();
        if (getJTextPane().isEditable()) {
            getJTextPane().requestFocusInWindow();
        }
        getJTextPane().setSelectionStart(0);
        getJTextPane().setSelectionEnd(0);
    }

}
