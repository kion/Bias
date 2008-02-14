/**
 * Created on Aug 14, 2007
 */
package bias.extension.ToDoList.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;

import bias.Constants;
import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.HTMLPage.HTMLPage;
import bias.gui.FrontEnd;
import bias.gui.ImageFileChooser;
import bias.gui.VisualEntryDescriptor;
import bias.utils.AppManager;
import bias.utils.FSUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class HTMLEditorPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private static final ImageIcon ICON_ENTRY_LINK = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/entry_link.png"));

    private static final ImageIcon ICON_URL_LINK = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/url_link.png"));

    private static final ImageIcon ICON_IMAGE = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/image.png"));

    private static final ImageIcon ICON_COLOR = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/color.png"));

    private static final ImageIcon ICON_TEXT_UNDERLINE = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/text_underlined.png"));

    private static final ImageIcon ICON_TEXT_ITALIC = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/text_italic.png"));

    private static final ImageIcon ICON_TEXT_BOLD = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/text_bold.png"));

    private static final ImageIcon ICON_SWITCH_MODE = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/switch_mode.png"));

    private static final ImageIcon ICON_SAVE = new ImageIcon(BackEnd.getInstance().getResourceURL(HTMLPage.class, "editor/save.png"));

    private static final String HTML_PAGE_FILE_NAME_PATTERN = "(?i).+\\.(htm|html)$";

    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);

    private static final String[] FONT_FAMILY_NAMES = new String[] { "SansSerif", "Serif", "Monospaced" };

    private static final int FONT_SIZE_XX_LARGE = 36;

    private static final int FONT_SIZE_X_LARGE = 24;

    private static final int FONT_SIZE_LARGE = 18;

    private static final int FONT_SIZE_MEDIUM = 14;

    private static final int FONT_SIZE_SMALL = 12;

    private static final int FONT_SIZE_X_SMALL = 10;

    private static final int FONT_SIZE_XX_SMALL = 8;

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
    
    private boolean dataChanged = false;
    
    private String code;
    
    private Collection<String> processedAttachmentNames = new ArrayList<String>();

    private UUID entryID = null;
    
    private File lastOutputDir = null;
    
    private JScrollPane jScrollPane = null;

    private JTextPane jTextPane = null;

    private JToolBar jToolBar = null;

    private JToolBar jToolBar1 = null;

    private JPanel jPanel = null;

    private JToggleButton jToggleButton = null;

    private JToggleButton jToggleButton1 = null;

    private JToggleButton jToggleButton2 = null;

    private JToggleButton jToggleButton3 = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    private JButton jButton2 = null;

    private JButton jButton5 = null;

    private JButton jButton8 = null;

    private JComboBox jComboBox = null;

    private JComboBox jComboBox1 = null;
    
    @SuppressWarnings("unused")
    private HTMLEditorPanel() {
        // hidden default empty constructor
    }

    public HTMLEditorPanel(UUID dataEntryID, String code) {
        super();
        this.entryID = dataEntryID;
        this.code = code;
        initialize(processOnLoad(code));
    }
    
    private void initialize(String code) {
        getJTextPane().setText(code);
        getJTextPane().getDocument().addUndoableEditListener(new UndoRedoManager(getJTextPane()));
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
                dataChanged = true;
            }
        });
        this.setLayout(new BorderLayout());
        this.add(getJScrollPane(), BorderLayout.CENTER); 
        this.add(getJPanel(), BorderLayout.SOUTH); 
    }
    
    public String getCode() {
        if (dataChanged) {
            return processOnSave(getJTextPane().getText());
        } else {
            return code;
        }
    }
    
    public String getText() {
        Document doc = getJTextPane().getDocument();
        String text = null;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            // ignore, shouldn't happen ever
        }
        return text;
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
            jTextPane.setEditable(false);
            jTextPane.setEditorKit(new HTMLEditorKit());

            // set default font for JTextPane instance
            MutableAttributeSet attrs = jTextPane.getInputAttributes();
            StyleConstants.setFontFamily(attrs, DEFAULT_FONT.getFamily());
            StyleConstants.setFontSize(attrs, DEFAULT_FONT.getSize());
            StyleConstants.setItalic(attrs, (DEFAULT_FONT.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(attrs, (DEFAULT_FONT.getStyle() & Font.BOLD) != 0);
            HTMLDocument doc = (HTMLDocument) jTextPane.getStyledDocument();
            doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);

            doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
            doc.setPreservesUnknownTags(false);

            jTextPane.addCaretListener(new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    JTextPane textPane = (JTextPane) e.getSource();
                    synchronizeEditNoteControlsStates(textPane);
                }
            });
            jTextPane.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                        if (e.getDescription().startsWith(Constants.ENTRY_PROTOCOL_PREFIX)) {
                            String idStr = e.getDescription().substring(Constants.ENTRY_PROTOCOL_PREFIX.length());
                            FrontEnd.switchToVisualEntry(UUID.fromString(idStr));
                        } else {
                            try {
                                AppManager.getInstance().handleAddress(e.getDescription());
                            } catch (Exception ex) {
                                FrontEnd.displayErrorMessage(ex);
                            }
                        }
                    }
                }
            });
        }
        return jTextPane;
    }
    
    public int getCaretPosition() {
        return getJTextPane().getCaretPosition();
    }

    public void setCaretPosition(int pos) {
        getJTextPane().setCaretPosition(pos);
    }

    private void synchronizeEditNoteControlsStates(JTextPane textPane) {
        if (textPane.isEditable()) {
            boolean boldSelected = false;
            boolean italicSelected = false;
            boolean underlineSelected = false;
            Integer fontSize = null;
            String fontFamily = null;
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
                fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
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
                    if (as.containsAttribute(StyleConstants.Bold, Boolean.TRUE)) {
                        boldSelected = true;
                    }
                    if (as.containsAttribute(StyleConstants.Italic, Boolean.TRUE)) {
                        italicSelected = true;
                    }
                    if (as.containsAttribute(StyleConstants.Underline, Boolean.TRUE)) {
                        underlineSelected = true;
                    }
                    // get the biggest font size value in selected text
                    if (as.isDefined(StyleConstants.FontSize)) {
                        Integer fs = (Integer) as.getAttribute(StyleConstants.FontSize);
                        if (fontSize == null || fs.intValue() > fontSize.intValue()) {
                            fontSize = new Integer(fs.intValue());
                        }
                    }
                    // get font family of last char of selected text which has font family set
                    fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
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
                fontSize = DEFAULT_FONT.getSize();
            }
            Iterator<Entry<String, Integer>> it = FONT_SIZES.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Integer> fontSizeEntry = it.next();
                Integer fontSizeValue = fontSizeEntry.getValue();
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
            if (fontFamily == null) {
                fontFamily = DEFAULT_FONT.getFamily();
            }
            getJComboBox1().setSelectedItem(fontFamily);
            for (int i = 0; i < ils.length; i++) {
                getJComboBox1().addItemListener(ils[i]);
            }
        }
    }

    private String processOnLoad(String htmlCode) {
        StringBuffer parsedHtmlCode = new StringBuffer();
        Pattern p = Pattern.compile("(<img(\\s+\\w+=\"[^\"]*\")*\\s+src=)\"att://([^\"]+)\"");
        Matcher m = p.matcher(htmlCode);
        while (m.find()) {
            File f = extractAttachmentImage(m.group(3));
            if (f != null) {
                m.appendReplacement(parsedHtmlCode, m.group(1) + "\"file://" + f.getAbsolutePath() + "\"");
            }
        }
        m.appendTail(parsedHtmlCode);
        return parsedHtmlCode.toString();
    }

    private String processOnSave(String htmlCode) {
        processedAttachmentNames.clear();
        StringBuffer parsedHtmlCode = new StringBuffer();
        Pattern p = Pattern.compile("(<img(\\s+\\w+=\"[^\"]*\")*\\s+src=)\"(file://[^\"]+)\"");
        Matcher m = p.matcher(htmlCode);
        while (m.find()) {
            String attName = m.group(3);
            attName = attName.substring(attName.lastIndexOf("/")+1);
            m.appendReplacement(parsedHtmlCode, m.group(1) + "\"att://" + attName + "\"");
            processedAttachmentNames.add(attName);
        }
        m.appendTail(parsedHtmlCode);
        return parsedHtmlCode.toString();
    }
    
    public Collection<String> getProcessedAttachmentNames() {
        return processedAttachmentNames;
    }

    private void saveToFile(File htmlFile, String htmlCode) throws Exception {
        StringBuffer parsedHtmlCode = new StringBuffer();
        Pattern p = Pattern.compile("(<img(\\s+\\w+=\"[^\"]*\")*\\s+src=)\"(file://[^\"]+)\"");
        Matcher m = p.matcher(htmlCode);
        while (m.find()) {
            String attName = m.group(3);
            attName = attName.substring(attName.lastIndexOf("/")+1);
            File attsDir = new File(htmlFile.getParentFile(), htmlFile.getName().substring(0, htmlFile.getName().indexOf(".")) + "_files/");
            File attFile = saveAttachmentExternally(attsDir, attName);
            if (attFile != null) {
                URI uri = attFile.getParentFile().getParentFile().toURI().relativize(attFile.toURI());
                m.appendReplacement(parsedHtmlCode, m.group(1) + "\"" + uri.toASCIIString() + "\"");
            }
        }
        m.appendTail(parsedHtmlCode);
        FSUtils.writeFile(htmlFile, parsedHtmlCode.toString().getBytes());
    }

    private File saveAttachmentExternally(File attsDir, String attName) {
        File attFile = null;
        try {
            // get attachments and store to external directory
            // (need to do so, because attachments are stored in ecrypted form,
            // so have to be decrypted before use)
            for (Attachment att : BackEnd.getInstance().getAttachments(entryID)) {
                if (!attsDir.exists()) {
                    attsDir.mkdir();
                }
                attFile = new File(attsDir, att.getName());
                FSUtils.writeFile(attFile, att.getData());
            }
        } catch (Exception ex) {
            // ignore, broken images on page will inform about missing image-attachments
        }
        return attFile;
    }

    private File extractAttachmentImage(String attName) {
        File f = null;
        try {
            // get attachment and store it to temporary directory
            // (need to do so, because attachments are stored in encrypted form,
            // so have to be decrypted before use)
            Attachment att = BackEnd.getInstance().getAttachment(entryID, attName);
            if (att != null) {
                File idDir = new File(Constants.TMP_DIR, entryID.toString());
                if (!idDir.exists()) {
                    idDir.mkdir();
                }
                f = new File(idDir, att.getName());
                FSUtils.writeFile(f, att.getData());
            }
        } catch (Exception ex) {
            // ignore, broken images on page will inform about missing image-attachments
        }
        return f;
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
            jToolBar.add(getJButton8()); 
            jToolBar.add(getJToggleButton3()); 
        }
        return jToolBar;
    }

    /**
     * This method initializes jToolBar1
     * 
     * @return javax.swing.JToolBar
     */
    private JToolBar getJToolBar1() {
        if (jToolBar1 == null) {
            jToolBar1 = new JToolBar();
            jToolBar1.setFloatable(false); 
            jToolBar1.setBorder(null); 
            jToolBar1.add(getJButton1()); 
            jToolBar1.add(getJButton()); 
            jToolBar1.add(getJButton2()); 
            jToolBar1.add(getJButton5()); 
            jToolBar1.add(getJToggleButton()); 
            jToolBar1.add(getJToggleButton1()); 
            jToolBar1.add(getJToggleButton2()); 
            jToolBar1.add(getJComboBox()); 
            jToolBar1.add(getJComboBox1()); 
            jToolBar1.setVisible(false); 
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
            jToggleButton.setToolTipText("bold"); 
            jToggleButton.setIcon(ICON_TEXT_BOLD); 
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
            jToggleButton1.setToolTipText("italic"); 
            jToggleButton1.setIcon(ICON_TEXT_ITALIC); 
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
            jToggleButton2.setToolTipText("underline"); 
            jToggleButton2.setIcon(ICON_TEXT_UNDERLINE); 
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
            jToggleButton3.setToolTipText("switch mode"); 
            jToggleButton3.setIcon(ICON_SWITCH_MODE);
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
            jButton5.setToolTipText("text color"); 
            jButton5.setIcon(ICON_COLOR); 
            jButton5.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Color color = JColorChooser.showDialog(HTMLEditorPanel.this, "select text color", Color.BLACK);
                    new StyledEditorKit.ForegroundAction("Color", color).actionPerformed(e);
                }
            });
        }
        return jButton5;
    }

    /**
     * This method initializes jButton8
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton8() {
        if (jButton8 == null) {
            jButton8 = new JButton();
            jButton8.setToolTipText("save to file"); 
            jButton8.setIcon(ICON_SAVE); 
            jButton8.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JFileChooser jfc;
                        if (lastOutputDir != null) {
                            jfc = new JFileChooser(lastOutputDir);
                        } else {
                            jfc = new JFileChooser();
                        }
                        jfc.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return f.isDirectory() || (f.isFile() && f.getName().matches(HTML_PAGE_FILE_NAME_PATTERN));
                            }

                            @Override
                            public String getDescription() {
                                return "HTML page (*.htm, *.html)";
                            }
                        });
                        jfc.setMultiSelectionEnabled(false);
                        File file;
                        String fileName = FrontEnd.getSelectedVisualEntryCaption();
                        if (!Validator.isNullOrBlank(fileName)) {
                            file = new File(fileName);
                            jfc.setSelectedFile(file);
                        }
                        if (jfc.showSaveDialog(HTMLEditorPanel.this) == JFileChooser.APPROVE_OPTION) {
                            file = jfc.getSelectedFile();
                            if (!file.getName().matches(HTML_PAGE_FILE_NAME_PATTERN)) {
                                file = new File(file.getParentFile(), file.getName() + ".html");
                            }
                            Integer option = null;
                            if (file.exists()) {
                                option = JOptionPane.showConfirmDialog(HTMLEditorPanel.this, "File already exists, overwrite?",
                                        "Overwrite existing file", JOptionPane.YES_NO_OPTION);
                            }
                            if (option == null || option == JOptionPane.YES_OPTION) {
                                saveToFile(file, getJTextPane().getText());
                                lastOutputDir = file.getParentFile();
                            }
                        }
                    } catch (Exception ex) {
                        FrontEnd.displayErrorMessage(ex);
                    }
                }
            });
        }
        return jButton8;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setToolTipText("entry link"); 
            jButton.setIcon(ICON_ENTRY_LINK);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String text = null;
                    String href = null;
                    boolean replaceSel;
                    if (getJTextPane().getSelectedText() == null) {
                        text = null;
                        href = null;
                        replaceSel = false;
                    } else {
                        text = getJTextPane().getSelectedText();
                        int pos;
                        if (getJTextPane().getCaret().getDot() > getJTextPane().getCaret().getMark()) {
                            pos = getJTextPane().getCaret().getMark();
                        } else {
                            pos = getJTextPane().getCaret().getDot();
                        }
                        HTMLDocument document = (HTMLDocument) getJTextPane().getDocument();
                        BranchElement pEl = (BranchElement) document.getParagraphElement(pos);
                        Element el = pEl.positionToElement(pos);
                        AttributeSet attrs = el.getAttributes();
                        for (Enumeration<?> en = attrs.getAttributeNames(); en.hasMoreElements();) {
                            Object attr = en.nextElement();
                            if (attr.toString().equalsIgnoreCase("a")) {
                                String[] param = attrs.getAttribute(attr).toString().split(" ");
                                for (int i = 0; i < param.length; i++) {
                                    if (param[i].startsWith("href=")) {
                                        if (param[i].split("=").length == 2) {
                                            href = param[i].split("=")[1];
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        replaceSel = true;
                    }
                    String id = null;
                    if (href != null) {
                        id = href.substring(Constants.ENTRY_PROTOCOL_PREFIX.length());
                    }
                    VisualEntryDescriptor currDescriptor = null;
                    JLabel entryLabel = new JLabel("entry:");
                    JComboBox hrefComboBox = new JComboBox();
                    for (VisualEntryDescriptor veDescriptor : FrontEnd.getVisualEntryDescriptors().values()) {
                        hrefComboBox.addItem(veDescriptor);
                        if (veDescriptor.getEntry().getId().toString().equals(id)) {
                            currDescriptor = veDescriptor;
                        }
                    }
                    if (currDescriptor != null) {
                        hrefComboBox.setSelectedItem(currDescriptor);
                    }
                    JLabel textLabel = new JLabel("text:");
                    text = JOptionPane.showInputDialog(HTMLEditorPanel.this, new Component[] { entryLabel, hrefComboBox, textLabel }, text);
                    if (text != null) {
                        try {
                            StringBuffer linkHTML = new StringBuffer("<a ");
                            linkHTML.append("href=\"" + Constants.ENTRY_PROTOCOL_PREFIX);
                            if (!Validator.isNullOrBlank(hrefComboBox.getSelectedItem())) {
                                linkHTML.append(((VisualEntryDescriptor) hrefComboBox.getSelectedItem()).getEntry().getId());
                            } else {
                                linkHTML.append("#");
                            }
                            linkHTML.append("\">");
                            linkHTML.append(text);
                            linkHTML.append("</a>");
                            if (replaceSel) {
                                getJTextPane().replaceSelection(Constants.EMPTY_STR);
                            }
                            linkHTML.append("&nbsp;");
                            HTMLEditor.insertHTML(getJTextPane(), linkHTML.toString(), HTML.Tag.A);
                        } catch (BadLocationException exception) {
                            FrontEnd.displayErrorMessage(exception);
                        } catch (IOException exception) {
                            FrontEnd.displayErrorMessage(exception);
                        }
                    }
                    getJTextPane().requestFocusInWindow();
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
            jButton1.setToolTipText("URL link"); 
            jButton1.setIcon(ICON_URL_LINK);
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String text = null;
                    String href = null;
                    if (getJTextPane().getSelectedText() == null) {
                        text = null;
                        href = null;
                    } else {
                        text = getJTextPane().getSelectedText();
                        int pos;
                        if (getJTextPane().getCaret().getDot() > getJTextPane().getCaret().getMark()) {
                            pos = getJTextPane().getCaret().getMark();
                        } else {
                            pos = getJTextPane().getCaret().getDot();
                        }
                        HTMLDocument document = (HTMLDocument) getJTextPane().getDocument();
                        BranchElement pEl = (BranchElement) document.getParagraphElement(pos);
                        Element el = pEl.positionToElement(pos);
                        AttributeSet attrs = el.getAttributes();
                        for (Enumeration<?> en = attrs.getAttributeNames(); en.hasMoreElements();) {
                            Object attr = en.nextElement();
                            if (attr.toString().equalsIgnoreCase("a")) {
                                String[] param = attrs.getAttribute(attr).toString().split(" ");
                                for (int i = 0; i < param.length; i++) {
                                    if (param[i].startsWith("href=")) {
                                        href = param[i].substring("href=".length(), param[i].length());
                                    }
                                }
                                break;
                            }
                        }
                    }
                    JLabel textLabel = new JLabel("text:");
                    JTextField textField = new JTextField();
                    if (text != null) {
                        textField.setText(text);
                    }
                    JLabel urlLabel = new JLabel("URL:");
                    if (Validator.isNullOrBlank(href)) {
                        href = text;
                    }
                    href = JOptionPane.showInputDialog(HTMLEditorPanel.this, new Component[] { textLabel, textField, urlLabel }, href);
                    if (href != null) {
                        try {
                            StringBuffer linkHTML = new StringBuffer("<a ");
                            linkHTML.append("href=\"");
                            linkHTML.append(href);
                            linkHTML.append("\">");
                            if (!Validator.isNullOrBlank(textField.getText())) {
                                linkHTML.append(textField.getText());
                            } else {
                                linkHTML.append(href);
                            }
                            linkHTML.append("</a>");
                            HTMLEditor.insertHTML(getJTextPane(), linkHTML.toString(), HTML.Tag.A);
                        } catch (BadLocationException exception) {
                            FrontEnd.displayErrorMessage(exception);
                        } catch (IOException exception) {
                            FrontEnd.displayErrorMessage(exception);
                        }
                    }
                    getJTextPane().requestFocusInWindow();
                }
            });
        }
        return jButton1;
    }

    /**
     * This method initializes jButton1
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setToolTipText("image"); 
            jButton2.setIcon(ICON_IMAGE);
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    final JTextField srcTF = new JTextField();
                    JTextField altTF = new JTextField();
                    JTextField hrefTF = new JTextField();
                    JTextField widthTF = new JTextField();
                    JTextField heightTF = new JTextField();
                    JTextField hSpaceTF = new JTextField();
                    JTextField vSpaceTF = new JTextField();
                    JTextField borderTF = new JTextField();
                    JComboBox alignCB = new JComboBox();
                    alignCB.addItem(Constants.EMPTY_STR);
                    alignCB.addItem("left");
                    alignCB.addItem("right");
                    alignCB.addItem("middle");
                    alignCB.addItem("absmiddle");
                    alignCB.addItem("top");
                    alignCB.addItem("texttop");
                    alignCB.addItem("bottom");
                    alignCB.addItem("absbottom");
                    alignCB.addItem("center");
                    alignCB.addItem("baseline");
                    int pos;
                    if (getJTextPane().getCaret().getDot() > getJTextPane().getCaret().getMark()) {
                        pos = getJTextPane().getCaret().getMark();
                    } else {
                        pos = getJTextPane().getCaret().getDot();
                    }
                    HTMLDocument document = (HTMLDocument) getJTextPane().getDocument();
                    BranchElement pEl = (BranchElement) document.getParagraphElement(pos);
                    Element el = pEl.positionToElement(pos);
                    AttributeSet attrs = el.getAttributes();
                    String elName = attrs.getAttribute(StyleConstants.NameAttribute).toString().toUpperCase();
                    if (elName.equalsIgnoreCase("IMG")) {
                        if (attrs.isDefined(HTML.Attribute.SRC)) {
                            srcTF.setText(attrs.getAttribute(HTML.Attribute.SRC).toString());
                        }
                        if (attrs.isDefined(HTML.Attribute.ALT)) {
                            altTF.setText(attrs.getAttribute(HTML.Attribute.ALT).toString());
                        }
                        if (attrs.isDefined(HTML.Attribute.WIDTH)) {
                            widthTF.setText(attrs.getAttribute(HTML.Attribute.WIDTH).toString());
                        }
                        if (attrs.isDefined(HTML.Attribute.HEIGHT)) {
                            heightTF.setText(attrs.getAttribute(HTML.Attribute.HEIGHT).toString());
                        }
                        if (attrs.isDefined(HTML.Attribute.HSPACE)) {
                            hSpaceTF.setText(attrs.getAttribute(HTML.Attribute.HSPACE).toString());
                        }
                        if (attrs.isDefined(HTML.Attribute.VSPACE)) {
                            vSpaceTF.setText(attrs.getAttribute(HTML.Attribute.VSPACE).toString());
                        }
                        if (attrs.isDefined(HTML.Attribute.BORDER)) {
                            borderTF.setText(attrs.getAttribute(HTML.Attribute.BORDER).toString());
                        }
                        if (attrs.isDefined(HTML.Attribute.ALIGN)) {
                            alignCB.setSelectedItem(attrs.getAttribute(HTML.Attribute.ALIGN).toString());
                        }
                        for (Enumeration<?> en = attrs.getAttributeNames(); en.hasMoreElements();) {
                            Object attr = en.nextElement();
                            if (attr.toString().equalsIgnoreCase("a")) {
                                Object attrValue = attrs.getAttribute(attr);
                                if (attrValue != null && attrValue.toString().startsWith("href=")) {
                                    hrefTF.setText(attrValue.toString().split("=")[1]);
                                    break;
                                }
                            }
                        }
                    }
                    JPanel srcPanel = new JPanel(new BorderLayout());
                    srcPanel.add(new JLabel("source: "), BorderLayout.WEST);
                    srcPanel.add(srcTF, BorderLayout.CENTER);
                    JButton browseButton = new JButton("...");
                    browseButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser jFileChooser = new ImageFileChooser(false);
                            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            int rVal = jFileChooser.showOpenDialog(HTMLEditorPanel.this);
                            if (rVal == JFileChooser.APPROVE_OPTION) {
                                srcTF.setText(jFileChooser.getSelectedFile().getAbsolutePath());
                            }
                        }
                    });
                    srcPanel.add(browseButton, BorderLayout.EAST);
                    JPanel altPanel = new JPanel(new BorderLayout());
                    altPanel.add(new JLabel("alt: "), BorderLayout.WEST);
                    altPanel.add(altTF, BorderLayout.CENTER);
                    JPanel hrefPanel = new JPanel(new BorderLayout());
                    hrefPanel.add(new JLabel("href: "), BorderLayout.WEST);
                    hrefPanel.add(hrefTF, BorderLayout.CENTER);
                    JPanel widthPanel = new JPanel(new BorderLayout());
                    widthPanel.add(new JLabel("width: "), BorderLayout.WEST);
                    widthPanel.add(widthTF, BorderLayout.CENTER);
                    JPanel heightPanel = new JPanel(new BorderLayout());
                    heightPanel.add(new JLabel("height: "), BorderLayout.WEST);
                    heightPanel.add(heightTF, BorderLayout.CENTER);
                    JPanel hSpacePanel = new JPanel(new BorderLayout());
                    hSpacePanel.add(new JLabel("h-space: "), BorderLayout.WEST);
                    hSpacePanel.add(hSpaceTF, BorderLayout.CENTER);
                    JPanel vSpacePanel = new JPanel(new BorderLayout());
                    vSpacePanel.add(new JLabel("v-space: "), BorderLayout.WEST);
                    vSpacePanel.add(vSpaceTF, BorderLayout.CENTER);
                    JPanel borderPanel = new JPanel(new BorderLayout());
                    borderPanel.add(new JLabel("border: "), BorderLayout.WEST);
                    borderPanel.add(borderTF, BorderLayout.CENTER);
                    JPanel alignPanel = new JPanel(new BorderLayout());
                    alignPanel.add(new JLabel("align: "), BorderLayout.WEST);
                    alignPanel.add(alignCB, BorderLayout.CENTER);
                    int opt = JOptionPane.showConfirmDialog(HTMLEditorPanel.this, new Component[] { srcPanel, altPanel, hrefPanel, widthPanel,
                            heightPanel, hSpacePanel, vSpacePanel, borderPanel, alignPanel }, "Image properties",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (opt == JOptionPane.OK_OPTION) {
                        try {
                            StringBuffer imgHTML = new StringBuffer();
                            boolean isLink = !Validator.isNullOrBlank(hrefTF.getText()) ? true : false;
                            if (isLink) {
                                imgHTML.append("<a href=\"");
                                imgHTML.append(hrefTF.getText());
                                imgHTML.append("\">");
                            }
                            imgHTML.append("<img src=\"");
                            if (srcTF.getText().startsWith("http://")) {
                                imgHTML.append(srcTF.getText());
                            } else {
                                File file = new File(srcTF.getText());
                                if (file.exists()) {
                                    try {
                                        // try to read file as image first
                                        ImageIO.read(new FileInputStream(file));
                                        // attach image-file (will be encrypted)
                                        Attachment att = new Attachment(file);
                                        BackEnd.getInstance().addAttachment(entryID, att);
                                        // now get it back (in decrypted form) and extract actual image-file
                                        File f = extractAttachmentImage(att.getName());
                                        imgHTML.append("file://" + f.getAbsolutePath());
                                    } catch (Exception ex) {
                                        FrontEnd.displayErrorMessage("Failed to attach image to data entry!\n" + ex.getMessage(), ex);
                                    }
                                }
                            }
                            imgHTML.append("\"");
                            if (!Validator.isNullOrBlank((String) alignCB.getSelectedItem())) {
                                imgHTML.append(" align=\"");
                                imgHTML.append((String) alignCB.getSelectedItem());
                                imgHTML.append("\"");
                            }
                            if (!Validator.isNullOrBlank(altTF.getText())) {
                                imgHTML.append(" alt=\"");
                                imgHTML.append(altTF.getText());
                                imgHTML.append("\"");
                            }
                            if (!Validator.isNullOrBlank(widthTF.getText())) {
                                imgHTML.append(" width=\"");
                                imgHTML.append(widthTF.getText());
                                imgHTML.append("\"");
                            }
                            if (!Validator.isNullOrBlank(heightTF.getText())) {
                                imgHTML.append(" height=\"");
                                imgHTML.append(heightTF.getText());
                                imgHTML.append("\"");
                            }
                            if (!Validator.isNullOrBlank(hSpaceTF.getText())) {
                                imgHTML.append(" hspace=\"");
                                imgHTML.append(hSpaceTF.getText());
                                imgHTML.append("\"");
                            }
                            if (!Validator.isNullOrBlank(vSpaceTF.getText())) {
                                imgHTML.append(" vspace=\"");
                                imgHTML.append(vSpaceTF.getText());
                                imgHTML.append("\"");
                            }
                            if (!Validator.isNullOrBlank(borderTF.getText())) {
                                imgHTML.append(" border=\"");
                                imgHTML.append(borderTF.getText());
                                imgHTML.append("\"");
                            }
                            imgHTML.append(">");
                            if (isLink) {
                                imgHTML.append("</a>");
                            }
                            Tag tag;
                            if (imgHTML.toString().startsWith("<a")) {
                                tag = HTML.Tag.A;
                            } else {
                                tag = HTML.Tag.IMG;
                            }
                            HTMLEditor.insertHTML(getJTextPane(), imgHTML.toString(), tag);
                        } catch (BadLocationException exception) {
                            FrontEnd.displayErrorMessage(exception);
                        } catch (IOException exception) {
                            FrontEnd.displayErrorMessage(exception);
                        }
                    }
                    getJTextPane().requestFocusInWindow();
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
            jComboBox.setMaximumSize(new Dimension(150, 20)); 
            jComboBox.setPreferredSize(new Dimension(150, 20)); 
            jComboBox.setToolTipText("font size"); 
            jComboBox.setMinimumSize(new Dimension(150, 20)); 
            Iterator<String> it = FONT_SIZES.keySet().iterator();
            while (it.hasNext()) {
                jComboBox.addItem(it.next());
            }
            jComboBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    String selectedFontSizeStr = (String) getJComboBox().getSelectedItem();
                    int selectedFontSize = FONT_SIZES.get(selectedFontSizeStr);
                    String actionName = "font size";
                    new StyledEditorKit.FontSizeAction(actionName, selectedFontSize).actionPerformed(new ActionEvent(e.getSource(), e
                            .getID(), actionName));
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
            jComboBox1.setMaximumSize(new Dimension(150, 20)); 
            jComboBox1.setPreferredSize(new Dimension(150, 20)); 
            jComboBox1.setToolTipText("font family"); 
            jComboBox1.setMinimumSize(new Dimension(150, 20)); 
            for (int i = 0; i < FONT_FAMILY_NAMES.length; i++) {
                jComboBox1.addItem(FONT_FAMILY_NAMES[i]);
            }
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
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout()); 
            jPanel.add(getJToolBar1(), BorderLayout.CENTER); 
            jPanel.add(getJToolBar(), BorderLayout.WEST); 
        }
        return jPanel;
    }

}
