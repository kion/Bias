/**
 * Created on Oct 15, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import bias.core.BackEnd;
import bias.global.Constants;
import bias.utils.Validator;

/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private Collection<String> notesCaptions;
    
    private JPanel jContentPane = null;

    private JTabbedPane jTabbedPane = null;

    private JToolBar jToolBar = null;

    private JButton jButton = null;

    private JButton jButton1 = null;
    
    private JButton jButton3 = null;

    private JButton jButton4 = null;

    private JToolBar jToolBar1 = null;

    private JToggleButton jToggleButton = null;

    private JToggleButton jToggleButton1 = null;

    private JToggleButton jToggleButton2 = null;

    private JComboBox jComboBox = null;
    
    private JComboBox jComboBox1 = null;

    private JButton jButton2 = null;

    private JButton jButton5 = null;

    private JPanel jPanel = null;

    private JToolBar jToolBar2 = null;

    private JButton jButton6 = null;

    /**
     * This is the default constructor
     */
    public FrontEnd() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(new Dimension(796, 558));  // Generated
        try {
            this.setTitle("Bias");
            this.setIconImage(Constants.ICON_APP.getImage());
            this.setContentPane(getJContentPane());

            BackEnd.load();
            Properties properties = BackEnd.getProperties();
            
            int wpxValue;
            int wpyValue;
            int wwValue;
            int whValue;
            String wpx = properties.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_X);
            if (wpx == null) {
                wpxValue = getToolkit().getScreenSize().width/4;
            } else {
                getToolkit().getScreenSize().getWidth();
                Double.valueOf(wpx);
                wpxValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
            }
            String wpy = properties.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
            if (wpy == null) {
                wpyValue = getToolkit().getScreenSize().height/4;
            } else {
                wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
            }
            String ww = properties.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
            if (ww == null) {
                wwValue = (getToolkit().getScreenSize().width/4)*2;
            } else {
                wwValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
            }
            String wh = properties.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
            if (wh == null) {
                whValue = (getToolkit().getScreenSize().height/4)*2;
            } else {
                whValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
            }

            this.setLocation(wpxValue, wpyValue);
            this.setSize(wwValue, whValue);
            
            initNotes(properties);

            int lstiValue;
            String lstiStr = properties.getProperty(Constants.PROPERTY_LAST_SELECTED_TAB_INDEX);
            if (lstiStr == null) {
                lstiValue = 0;
            } else {
                lstiValue = Integer.valueOf(lstiStr);
            }
            if (getJTabbedPane().getTabCount() > 0) {
                getJTabbedPane().setSelectedIndex(lstiValue);
                setNotesManagementToolbalEnabledState(true);
            } else {
                setNotesManagementToolbalEnabledState(false);
            }

            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.addWindowListener(new java.awt.event.WindowAdapter() {   
                public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        gatherCurrentData();
                        BackEnd.store();
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        } catch (Exception ex) {
            displayErrorMessage(ex);
        }
    }
    
    private void initNotes(Properties properties) {
        notesCaptions = new ArrayList<String>();
        for (Object noteCaption : properties.values()) {
            notesCaptions.add((String) noteCaption);
        }
        getJTabbedPane().removeAll();
        Map<String, String> notes = BackEnd.getNotes();
        for (Entry<String, String> note : notes.entrySet()) {
            String key = note.getKey();
            String caption = properties.getProperty(key);
            String value = note.getValue();
            JTextPane textPane = new JTextPane();
            textPane.setEditorKit(new HTMLEditorKit());
            textPane.setText(value);
            textPane.setEditable(false);
            textPane.addCaretListener(togglesStatesListener);
            getJTabbedPane().addTab(caption, new JScrollPane(textPane));
        }
    }
    
    private void gatherCurrentData() {

        Properties properties = new Properties();
        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_X, Constants.EMPTY_STR+getLocation().getX()/getToolkit().getScreenSize().getWidth());
        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_Y, Constants.EMPTY_STR+getLocation().getY()/getToolkit().getScreenSize().getHeight());
        properties.put(Constants.PROPERTY_WINDOW_WIDTH, Constants.EMPTY_STR+getSize().getWidth()/getToolkit().getScreenSize().getHeight());
        properties.put(Constants.PROPERTY_WINDOW_HEIGHT, Constants.EMPTY_STR+getSize().getHeight()/getToolkit().getScreenSize().getHeight());
        properties.put(Constants.PROPERTY_LAST_SELECTED_TAB_INDEX, Constants.EMPTY_STR+getJTabbedPane().getSelectedIndex());
        BackEnd.setProperties(properties);
        
        Map<String, String> notes = new LinkedHashMap<String, String>();
        for (int i = 0; i < getJTabbedPane().getTabCount(); i++) {
            JTextPane textPane = (JTextPane) 
                                    ((JViewport) 
                                         ((JScrollPane) 
                                                 getJTabbedPane().getComponent(i)).getComponent(0)).getComponent(0);
            String caption = getJTabbedPane().getTitleAt(i);
            String value = textPane.getText();
            properties.put(Constants.EMPTY_STR+(i+1), caption);
            notes.put(Constants.EMPTY_STR+(i+1), value);
        }
        BackEnd.setNotes(notes);
        
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
                fontSize = Constants.FONT_SIZE_MEDIUM;
            }
            Iterator it = Constants.FONT_SIZES.entrySet().iterator();
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
                getJComboBox1().setSelectedIndex(0);
            }
            for (int i = 0; i < ils.length; i++) {
                getJComboBox1().addItemListener(ils[i]);
            }
        }
    }

    private void setNotesManagementToolbalEnabledState(boolean enabled) {
        for (Component button : getJToolBar().getComponents()) {
            if (!button.equals(getJButton()) && !button.equals(getJButton2())) {
                button.setEnabled(enabled);
            }
        }
    }

    private void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void displayErrorMessage(Exception ex) {
        JOptionPane.showMessageDialog(this, "Details: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
            jContentPane.add(getJToolBar1(), BorderLayout.SOUTH);  // Generated
            jContentPane.add(getJPanel(), BorderLayout.NORTH);  // Generated
        }
        return jContentPane;
    }

    /**
     * This method initializes jTabbedPane	
     * 	
     * @return javax.swing.JTabbedPane	
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setBackground(null);  // Generated
            jTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    if (jTabbedPane.getSelectedIndex() != -1) {
                        JTextPane textPane = (JTextPane) 
                        ((JViewport) 
                             ((JScrollPane) 
                                     getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                     .getComponent(0)).getComponent(0);
                        if (textPane.isEditable()) {
                            getJToolBar1().setVisible(true);
                            textPane.requestFocusInWindow();
                            synchronizeEditNoteControlsStates(textPane);
                        } else {
                            getJToolBar1().setVisible(false);
                        }
                    } else {
                        getJToolBar1().setVisible(false);
                    }
                }
            });
        }
        return jTabbedPane;
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
            jToolBar.add(getJButton2());  // Generated
            jToolBar.add(getJButton());  // Generated
            jToolBar.add(getJButton3());  // Generated
            jToolBar.add(getJButton1());  // Generated
            jToolBar.add(getJButton4());  // Generated
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
            jButton.setToolTipText("add note");  // Generated
            jButton.setIcon(Constants.ICON_ADD);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JTextPane textPane = new JTextPane();
                        textPane.setEditorKit(new HTMLEditorKit());
                        textPane.setEditable(false);
                        textPane.addCaretListener(togglesStatesListener);
                        String noteCaption = JOptionPane.showInputDialog("Note caption:");
                        if (!Validator.isNullOrBlank(noteCaption)) {
                            if (!notesCaptions.contains(noteCaption)) {
                                getJTabbedPane().addTab(noteCaption, new JScrollPane(textPane));
                                getJTabbedPane().setSelectedIndex(getJTabbedPane().getTabCount()-1);
                                getJTabbedPane().setTitleAt(getJTabbedPane().getSelectedIndex(), noteCaption);
                                notesCaptions.add(noteCaption);
                                if (getJTabbedPane().getTabCount() == 1) {
                                    setNotesManagementToolbalEnabledState(true);
                                }
                            } else {
                                displayErrorMessage("Note caption must be unique!");
                            }
                        } else {
                            displayErrorMessage("Note caption can not be empty!");
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jButton3 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton();
            jButton3.setToolTipText("rename note");  // Generated
            jButton3.setIcon(Constants.ICON_RENAME);
            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        int index = getJTabbedPane().getSelectedIndex();
                        String currentNoteCaption = getJTabbedPane().getTitleAt(index);
                        String noteCaption = JOptionPane.showInputDialog("Note caption:");
                        if (!Validator.isNullOrBlank(noteCaption)) {
                            if (!currentNoteCaption.equals(noteCaption) && !notesCaptions.contains(noteCaption)) {
                                notesCaptions.remove(currentNoteCaption);
                                getJTabbedPane().setTitleAt(index, noteCaption);
                                notesCaptions.add(noteCaption);
                            } else {
                                displayErrorMessage("Note caption must be unique!");
                            }
                        } else {
                            displayErrorMessage("Note caption can not be empty!");
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jButton3;
    }

    /**
     * This method initializes jButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setToolTipText("delete note");  // Generated
            jButton1.setIcon(Constants.ICON_DELETE);
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        int index = getJTabbedPane().getSelectedIndex();
                        String noteCaption = getJTabbedPane().getTitleAt(index);
                        getJTabbedPane().remove(index);
                        notesCaptions.remove(noteCaption);
                        if (getJTabbedPane().getTabCount() == 0) {
                            setNotesManagementToolbalEnabledState(false);
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
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
            jButton4.setToolTipText("switch mode");  // Generated
            jButton4.setIcon(Constants.ICON_SWITCH_MODE);
            jButton4.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JTextPane textPane = (JTextPane) 
                        ((JViewport) 
                             ((JScrollPane) 
                                     getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                     .getComponent(0)).getComponent(0);
                        textPane.setEditable(!textPane.isEditable());
                        if (textPane.isEditable()) {
                            textPane.requestFocusInWindow();
                        }
                        if (!getJToolBar1().isVisible()) {
                            getJToolBar1().setVisible(true);
                            textPane.requestFocusInWindow();
                            synchronizeEditNoteControlsStates(textPane);
                        } else {
                            getJToolBar1().setVisible(false);
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jButton4;
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
            jToolBar1.add(getJToggleButton());  // Generated
            jToolBar1.add(getJToggleButton1());  // Generated
            jToolBar1.add(getJToggleButton2());  // Generated
            jToolBar1.add(getJButton5());  // Generated
            jToolBar1.add(getJComboBox());  // Generated
            jToolBar1.add(getJComboBox1());  // Generated
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
            jToggleButton.setIcon(Constants.ICON_TEXT_BOLD);
            jToggleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        new StyledEditorKit.BoldAction().actionPerformed(e);
                        JTextPane textPane = (JTextPane) 
                        ((JViewport) 
                             ((JScrollPane) 
                                     getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                     .getComponent(0)).getComponent(0);
                        textPane.requestFocusInWindow();
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jToggleButton;
    }

    /**
     * This method initializes jToggleButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JToggleButton getJToggleButton1() {
        if (jToggleButton1 == null) {
            jToggleButton1 = new JToggleButton();
            jToggleButton1.setToolTipText("italic");  // Generated
            jToggleButton1.setIcon(Constants.ICON_TEXT_ITALIC);
            jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        new StyledEditorKit.ItalicAction().actionPerformed(e);
                        JTextPane textPane = (JTextPane) 
                        ((JViewport) 
                             ((JScrollPane) 
                                     getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                     .getComponent(0)).getComponent(0);
                        textPane.requestFocusInWindow();
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jToggleButton1;
    }

    /**
     * This method initializes jToggleButton2	
     * 	
     * @return javax.swing.JButton	
     */
    private JToggleButton getJToggleButton2() {
        if (jToggleButton2 == null) {
            jToggleButton2 = new JToggleButton();
            jToggleButton2.setToolTipText("underline");  // Generated
            jToggleButton2.setIcon(Constants.ICON_TEXT_UNDERLINE);
            jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        new StyledEditorKit.UnderlineAction().actionPerformed(e);
                        JTextPane textPane = (JTextPane) 
                        ((JViewport) 
                             ((JScrollPane) 
                                     getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                     .getComponent(0)).getComponent(0);
                        textPane.requestFocusInWindow();
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jToggleButton2;
    }
    
    private CaretListener togglesStatesListener = new CaretListener() {
        public void caretUpdate(CaretEvent e) {
            JTextPane textPane = (JTextPane) e.getSource();  //  @jve:decl-index=0:
            synchronizeEditNoteControlsStates(textPane);
        }
    };

    /**
     * This method initializes jComboBox	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getJComboBox() {
        if (jComboBox == null) {
            jComboBox = new JComboBox();
            jComboBox.setMinimumSize(new Dimension(150, 20));
            jComboBox.setMaximumSize(new Dimension(150, 20));
            jComboBox.setPreferredSize(new Dimension(150, 20));
            jComboBox.setToolTipText("font size");
            Iterator it = Constants.FONT_SIZES.keySet().iterator();
            while (it.hasNext()) {
                jComboBox.addItem((String) it.next());
            }
            jComboBox.setSelectedItem(Constants.FONT_SIZES.get(Constants.FONT_SIZE_MEDIUM));
            jComboBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    JTextPane textPane = (JTextPane) 
                    ((JViewport) 
                         ((JScrollPane) 
                                 getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                 .getComponent(0)).getComponent(0);
                    String selectedFontSizeStr = (String) getJComboBox().getSelectedItem();
                    int selectedFontSize = ((Integer) Constants.FONT_SIZES.get(selectedFontSizeStr)).intValue();
                    String actionName = "font size";
                    new StyledEditorKit.FontSizeAction(actionName, selectedFontSize).actionPerformed(
                            new ActionEvent(e.getSource(), e.getID(), actionName));
                    textPane.requestFocusInWindow();
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
            jComboBox1.setMinimumSize(new Dimension(150, 20));
            jComboBox1.setMaximumSize(new Dimension(150, 20));
            jComboBox1.setPreferredSize(new Dimension(150, 20));
            jComboBox1.setToolTipText("font family");
            jComboBox1.addItem(Constants.EMPTY_STR);
            String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (int i = 0; i < fontFamilyNames.length; i++) {
                jComboBox1.addItem(fontFamilyNames[i]);
            }
            jComboBox1.setSelectedIndex(0);
            jComboBox1.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    JTextPane textPane = (JTextPane) 
                    ((JViewport) 
                         ((JScrollPane) 
                                 getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                 .getComponent(0)).getComponent(0);
                    String selectedFontFamilyStr = (String) getJComboBox1().getSelectedItem();
                    MutableAttributeSet ffAS = new SimpleAttributeSet();
                    ffAS.addAttribute(StyleConstants.FontFamily, selectedFontFamilyStr);
                    textPane.setCharacterAttributes(ffAS, false);
                    textPane.requestFocusInWindow();
                }
            });
        }
        return jComboBox1;
    }

    /**
     * This method initializes jButton2	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setToolTipText("import data from another Bias JAR");  // Generated
            jButton2.setIcon(Constants.ICON_IMPORT_DATA);
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JFileChooser jfc = new JFileChooser();
                        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        jfc.setFileFilter(new FileFilter(){
                            @Override
                            public boolean accept(File file) {
                                if (file.getName().endsWith(".jar")) {
                                    return true;
                                }
                                return false;
                            }
                            @Override
                            public String getDescription() {
                                return "Java Archive File (*.jar)";
                            }
                        });
                        File jarFile = null;
                        int rVal = jfc.showOpenDialog(FrontEnd.this);
                        if (rVal == JFileChooser.APPROVE_OPTION) {
                            jarFile = jfc.getSelectedFile();
                            gatherCurrentData();
                            BackEnd.importData(jarFile);
                            initNotes(BackEnd.getProperties());
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jButton2;
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
            jButton5.setIcon(Constants.ICON_TEXT_COLOR);
            jButton5.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        Color color = JColorChooser.showDialog(FrontEnd.this, "select text color", Color.BLACK);
                        new StyledEditorKit.ForegroundAction("Color", color).actionPerformed(e);
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });    
        }
        return jButton5;
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
            jPanel.add(getJToolBar(), BorderLayout.CENTER);  // Generated
            jPanel.add(getJToolBar2(), BorderLayout.EAST);  // Generated
        }
        return jPanel;
    }

    /**
     * This method initializes jToolBar2	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar2() {
        if (jToolBar2 == null) {
            jToolBar2 = new JToolBar();
            jToolBar2.setFloatable(false);  // Generated
            jToolBar2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);  // Generated
            jToolBar2.add(getJButton6());  // Generated
        }
        return jToolBar2;
    }

    /**
     * This method initializes jButton6	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton6() {
        if (jButton6 == null) {
            jButton6 = new JButton();
            jButton6.setToolTipText("about Bias");  // Generated
            jButton6.setIcon(Constants.ICON_ABOUT);
            jButton6.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JOptionPane.showMessageDialog(FrontEnd.this, 
                            "<html>Bias Personal Information Manager, version 0.1-beta" +
                            "<br>(c) ki0n, 2006" +
                            "<br>http://bias.sourceforge.net");
                }
            });
        }
        return jButton6;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
