/**
 * Created on Oct 23, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * @author kion
 */
public class HTMLPage extends VisualEntry {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_COLOR = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/color.png"));

    private static final ImageIcon ICON_TEXT_UNDERLINE = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/text_underlined.png"));

    private static final ImageIcon ICON_TEXT_ITALIC = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/text_italic.png"));

    private static final ImageIcon ICON_TEXT_BOLD = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/text_bold.png"));

    private static final ImageIcon ICON_SWITCH_MODE = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/switch_mode.png"));

    private static final Integer FONT_SIZE_XX_LARGE = new Integer(36);
    private static final Integer FONT_SIZE_X_LARGE = new Integer(24);
    private static final Integer FONT_SIZE_LARGE = new Integer(18);
    private static final Integer FONT_SIZE_MEDIUM = new Integer(14);
    private static final Integer FONT_SIZE_SMALL = new Integer(12);
    private static final Integer FONT_SIZE_X_SMALL = new Integer(10);
    private static final Integer FONT_SIZE_XX_SMALL = new Integer(8);

    private static final Map<String, Integer> FONT_SIZES = fontSizes();

    private static final Map<String, Integer> fontSizes() {
        Map<String, Integer> fontSizes = new LinkedHashMap<String, Integer>();
        fontSizes.put("xx-small", FONT_SIZE_XX_SMALL);
        fontSizes.put("x-small", FONT_SIZE_X_SMALL);
        fontSizes.put("small", FONT_SIZE_SMALL);
        fontSizes.put("medium", FONT_SIZE_MEDIUM);
        fontSizes.put("large", FONT_SIZE_LARGE);
        fontSizes.put("x-large", FONT_SIZE_X_LARGE);
        fontSizes.put("xx-large", FONT_SIZE_XX_LARGE);
        return fontSizes;
    }

    private JToolBar jToolBar1 = null;
    private JToggleButton jToggleButton = null;
    private JToggleButton jToggleButton1 = null;
    private JToggleButton jToggleButton2 = null;
    private JToggleButton jToggleButton3 = null;
    private JButton jButton5 = null;
    private JComboBox jComboBox = null;
    private JComboBox jComboBox1 = null;
    private JTextPane jTextPane = null;
    private JPanel jPanel = null;
    private JToolBar jToolBar = null;
    private JScrollPane jScrollPane = null;

    /**
     * This is the default constructor
     */
    public HTMLPage(byte[] data) {
        super(data);
        initialize();
    }

    /* (non-Javadoc)
     * @see bias.gui.VisualEntry#serialize()
     */
    @Override
    public byte[] serialize() {
        return getJTextPane().getText().getBytes();
    }

    private void synchronizeEditNoteControlsStates(JTextPane textPane) {
        if (textPane.isEditable()) {
            boolean boldSelected = false;
            boolean italicSelected = false;
            boolean underlineSelected = false;
            Integer fontSize = null;
            AttributeSet fontFamilyAS = new SimpleAttributeSet();
            if (textPane.getSelectedText() == null) {
                int pos = textPane.getCaretPosition() == 0 ? textPane.getCaretPosition() : textPane.getCaretPosition() - 1;
                AttributeSet as = ((HTMLDocument) textPane.getDocument()).getCharacterElement(pos).getAttributes();
                if (as.containsAttribute(StyleConstants.Bold, Boolean.TRUE)) {
                    boldSelected = true;
                }
                if (as.containsAttribute(StyleConstants.Italic, Boolean.TRUE)) {
                    italicSelected = true;
                }
                if (as.containsAttribute(StyleConstants.Underline, Boolean.TRUE)) {
                    underlineSelected = true;
                }
                // get font size at current position
                fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
                // get font family at the current position
                fontFamilyAS = ((HTMLDocument) textPane.getDocument()).getCharacterElement(pos).getAttributes();
            } else {
                AttributeSet as = null;
                int selStart;
                int selEnd;
                if (textPane.getCaret().getDot() > textPane.getCaret().getMark()) {
                    selStart = textPane.getCaret().getMark();
                    selEnd = textPane.getCaret().getDot();
                } else {
                    selStart = textPane.getCaret().getDot();
                    selEnd = textPane.getCaret().getMark();
                }
                for (int i = selStart; i < selEnd; i++) {
                    as = ((HTMLDocument) textPane.getDocument()).getCharacterElement(i).getAttributes();
                    if (!as.containsAttribute(StyleConstants.Bold, new Boolean(true))) {
                        boldSelected = false;
                    }
                    if (!as.containsAttribute(StyleConstants.Italic, new Boolean(true))) {
                        italicSelected = false;
                    }
                    if (!as.containsAttribute(StyleConstants.Underline, new Boolean(true))) {
                        underlineSelected = false;
                    }
                    // get the biggest font size value in selected text
                    if (as.isDefined(StyleConstants.FontSize)) {
                        Integer fs = (Integer) as.getAttribute(StyleConstants.FontSize);
                        if (fontSize == null || fs.intValue() > fontSize.intValue()) {
                            fontSize = new Integer(fs.intValue());
                        }
                    }
                    // get font family of last char of selected text which has font family set
                    AttributeSet ffAS = ((HTMLDocument) textPane.getDocument()).getCharacterElement(i).getAttributes();
                    if (ffAS.isDefined(StyleConstants.FontFamily)) {
                        fontFamilyAS = ffAS.copyAttributes();
                    }
                }
            }
            getJToggleButton().setSelected(boldSelected);
            getJToggleButton1().setSelected(italicSelected);
            getJToggleButton2().setSelected(underlineSelected);
            // set current font size in font size chooser
            ItemListener[] ils = getJComboBox().getItemListeners();
            for (int i = 0; i < ils.length; i++) {
                getJComboBox().removeItemListener(ils[i]);
            }
            if (fontSize == null) {
                fontSize = HTMLPage.FONT_SIZE_MEDIUM;
            }
            Iterator it = HTMLPage.FONT_SIZES.entrySet().iterator();
            while (it.hasNext()) {
                Entry fontSizeEntry = (Entry) it.next();
                Integer fontSizeValue = (Integer) fontSizeEntry.getValue();
                if (fontSizeValue.equals(fontSize)) {
                    getJComboBox().setSelectedItem(fontSizeEntry.getKey());
                    break;
                }
            }
            for (int i = 0; i < ils.length; i++) {
                getJComboBox().addItemListener(ils[i]);
            }
            // set current font family in font family chooser
            ils = getJComboBox1().getItemListeners();
            for (int i = 0; i < ils.length; i++) {
                getJComboBox1().removeItemListener(ils[i]);
            }
            if (fontFamilyAS.isDefined(StyleConstants.FontFamily)) {
                getJComboBox1().setSelectedItem(fontFamilyAS.getAttribute(StyleConstants.FontFamily));
            } else {
                getJComboBox1().setSelectedIndex(-1);
            }
            for (int i = 0; i < ils.length; i++) {
                getJComboBox1().addItemListener(ils[i]);
            }
        }
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(776, 532);
        this.setLayout(new BorderLayout());
        this.add(getJScrollPane(), BorderLayout.CENTER);  // Generated
        this.add(getJPanel(), BorderLayout.SOUTH);  // Generated
        getJTextPane().setText(new String(getData()));
    }

    /**
     * This method initializes jToolBar1	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar1() {
        if (jToolBar1 == null) {
            jToolBar1 = new JToolBar();
            jToolBar1.setFloatable(false);  // Generated
            jToolBar1.setBorder(null);  // Generated
            jToolBar1.add(getJToggleButton());  // Generated
            jToolBar1.add(getJToggleButton1());  // Generated
            jToolBar1.add(getJToggleButton2());  // Generated
            jToolBar1.add(getJButton5());  // Generated
            jToolBar1.add(getJComboBox());  // Generated
            jToolBar1.add(getJComboBox1());  // Generated
            jToolBar1.setVisible(false);  // Generated
        }
        return jToolBar1;
    }

    /**
     * This method initializes jToggleButton	
     * 	
     * @return javax.swing.JToggleButton	
     */
    private JToggleButton getJToggleButton() {
        if (jToggleButton == null) {
            jToggleButton = new JToggleButton();
            jToggleButton.setToolTipText("bold");  // Generated
            jToggleButton.setIcon(HTMLPage.ICON_TEXT_BOLD);  // Generated
            jToggleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new StyledEditorKit.BoldAction().actionPerformed(e);
                    getJTextPane().requestFocusInWindow();
                }
            });
        }
        return jToggleButton;
    }

    /**
     * This method initializes jToggleButton1	
     * 	
     * @return javax.swing.JToggleButton	
     */
    private JToggleButton getJToggleButton1() {
        if (jToggleButton1 == null) {
            jToggleButton1 = new JToggleButton();
            jToggleButton1.setToolTipText("italic");  // Generated
            jToggleButton1.setIcon(HTMLPage.ICON_TEXT_ITALIC);  // Generated
            jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new StyledEditorKit.ItalicAction().actionPerformed(e);
                    getJTextPane().requestFocusInWindow();
                }
            });
        }
        return jToggleButton1;
    }

    /**
     * This method initializes jToggleButton2	
     * 	
     * @return javax.swing.JToggleButton	
     */
    private JToggleButton getJToggleButton2() {
        if (jToggleButton2 == null) {
            jToggleButton2 = new JToggleButton();
            jToggleButton2.setToolTipText("underline");  // Generated
            jToggleButton2.setIcon(HTMLPage.ICON_TEXT_UNDERLINE);  // Generated
            jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new StyledEditorKit.UnderlineAction().actionPerformed(e);
                    getJTextPane().requestFocusInWindow();
                }
            });
        }
        return jToggleButton2;
    }

    /**
     * This method initializes jToggleButton3
     *  
     * @return javax.swing.JToggleButton  
     */
    private JToggleButton getJToggleButton3() {
        if (jToggleButton3 == null) {
            jToggleButton3 = new JToggleButton();
            jToggleButton3.setToolTipText("switch mode");  // Generated
            jToggleButton3.setIcon(HTMLPage.ICON_SWITCH_MODE);
            jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJTextPane().setEditable(!getJTextPane().isEditable());
                    if (getJTextPane().isEditable()) {
                        getJTextPane().requestFocusInWindow();
                    }
                    if (!getJToolBar1().isVisible()) {
                        getJToolBar1().setVisible(true);
                        synchronizeEditNoteControlsStates(getJTextPane());
                    } else {
                        getJToolBar1().setVisible(false);
                    }
                }
            });
        }
        return jToggleButton3;
    }

    /**
     * This method initializes jButton5	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton5() {
        if (jButton5 == null) {
            jButton5 = new JButton();
            jButton5.setToolTipText("text color");  // Generated
            jButton5.setIcon(HTMLPage.ICON_COLOR);  // Generated
            jButton5.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Color color = JColorChooser.showDialog(HTMLPage.this, "select text color", Color.BLACK);
                    new StyledEditorKit.ForegroundAction("Color", color).actionPerformed(e);
                }
            });    
        }
        return jButton5;
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
            jComboBox.setToolTipText("font size");  // Generated
            jComboBox.setMinimumSize(new Dimension(150, 20));  // Generated
            Iterator it = HTMLPage.FONT_SIZES.keySet().iterator();
            while (it.hasNext()) {
                jComboBox.addItem((String) it.next());
            }
            jComboBox.setSelectedItem("medium");
            jComboBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    String selectedFontSizeStr = (String) getJComboBox().getSelectedItem();
                    int selectedFontSize = ((Integer) HTMLPage.FONT_SIZES.get(selectedFontSizeStr)).intValue();
                    String actionName = "font size";
                    new StyledEditorKit.FontSizeAction(actionName, selectedFontSize).actionPerformed(
                            new ActionEvent(e.getSource(), e.getID(), actionName));
                    getJTextPane().requestFocusInWindow();
                }
            });
        }
        return jComboBox;
    }

    /**
     * This method initializes jComboBox1	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getJComboBox1() {
        if (jComboBox1 == null) {
            jComboBox1 = new JComboBox();
            jComboBox1.setMaximumSize(new Dimension(150, 20));  // Generated
            jComboBox1.setPreferredSize(new Dimension(150, 20));  // Generated
            jComboBox1.setToolTipText("font family");  // Generated
            jComboBox1.setMinimumSize(new Dimension(150, 20));  // Generated
            String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (int i = 0; i < fontFamilyNames.length; i++) {
                jComboBox1.addItem(fontFamilyNames[i]);
            }
            jComboBox1.setSelectedIndex(-1);
            jComboBox1.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    String selectedFontFamilyStr = (String) getJComboBox1().getSelectedItem();
                    MutableAttributeSet ffAS = new SimpleAttributeSet();
                    ffAS.addAttribute(StyleConstants.FontFamily, selectedFontFamilyStr);
                    getJTextPane().setCharacterAttributes(ffAS, false);
                    getJTextPane().requestFocusInWindow();
                }
            });
        }
        return jComboBox1;
    }

    /**
     * This method initializes jTextPane	
     * 	
     * @return javax.swing.JTextPane	
     */
    private JTextPane getJTextPane() {
        if (jTextPane == null) {
            jTextPane = new JTextPane();
            jTextPane.setEditable(false);
            jTextPane.setEditorKit(new HTMLEditorKit());
            jTextPane.addCaretListener(new CaretListener(){
                public void caretUpdate(CaretEvent e) {
                    JTextPane textPane = (JTextPane) e.getSource();  //  @jve:decl-index=0:
                    synchronizeEditNoteControlsStates(textPane);
                }
            });
        }
        return jTextPane;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());  // Generated
            jPanel.add(getJToolBar1(), BorderLayout.CENTER);  // Generated
            jPanel.add(getJToolBar(), BorderLayout.WEST);  // Generated
        }
        return jPanel;
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
            jToolBar.add(getJToggleButton3());  // Generated
        }
        return jToolBar;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextPane());  // Generated
        }
        return jScrollPane;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"