/**
 * Created on Oct 23, 2006
 */
package bias.extension.HTMLPage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import bias.Constants;
import bias.extension.Extension;
import bias.gui.FrontEnd;
import bias.gui.VisualEntryDescriptor;
import bias.utils.BrowserLauncher;
import bias.utils.Validator;

/**
 * @author kion
 */

@Extension.Annotation(
        name = "HTML Page", 
        version="0.3",
        description = "WYSIWYG HTML page editor",
        author="kion")
public class HTMLPage extends Extension {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_ENTRY_LINK = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/HTMLPage/entry_link.png"));

    private static final ImageIcon ICON_URL_LINK = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/HTMLPage/url_link.png"));

    private static final ImageIcon ICON_COLOR = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/HTMLPage/color.png"));

    private static final ImageIcon ICON_TEXT_UNDERLINE = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/HTMLPage/text_underlined.png"));

    private static final ImageIcon ICON_TEXT_ITALIC = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/HTMLPage/text_italic.png"));

    private static final ImageIcon ICON_TEXT_BOLD = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/HTMLPage/text_bold.png"));

    private static final ImageIcon ICON_SWITCH_MODE = 
        new ImageIcon(HTMLPage.class.getResource("/bias/res/HTMLPage/switch_mode.png"));  //  @jve:decl-index=0:
    
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);
    
    private static final String[] FONT_FAMILY_NAMES = new String[] { "SansSerif", "Serif", "Monospaced" };

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
    
    private static class HTMLPageEditor {

        public static void insertHTML(JTextPane editor, String htmlText, Tag tag) throws BadLocationException, IOException {
            if (editor.getEditorKit() instanceof HTMLEditorKit && editor.getDocument() instanceof HTMLDocument) {
                
                // remove editor's selected text if any 
                if (editor.getSelectedText() != null) {
                    editor.replaceSelection(Constants.EMPTY_STR);
                }

                HTMLEditorKit editorKit = (HTMLEditorKit) editor.getEditorKit();
                HTMLDocument document = (HTMLDocument) editor.getDocument();

                int caret = editor.getCaretPosition();
                Element pEl = document.getParagraphElement(caret);
                
                // insert space after inserted html if needed
                boolean insertSpace = true;
                if (caret < document.getLength()) {
                    if (document.getText(caret, 1).matches("\\s+")) {
                        insertSpace = false;
                    }
                }
                if (insertSpace) {
                    htmlText += "&nbsp;";
                }
                
                boolean isBreakingTag = tag.breaksFlow() || tag.isBlock();
                boolean isParagraphBegining = caret == pEl.getStartOffset();

                if (isBreakingTag && !isParagraphBegining) {
                    editorKit.insertHTML(document, caret, htmlText, 1, 0, tag);
                } else if (!isBreakingTag && !isParagraphBegining) {
                    editorKit.insertHTML(document, caret, htmlText, 0, 0, tag);
                } else if (isBreakingTag && isParagraphBegining) {
                    // insert blank char to avoid incorrect caret positioning
                    // after html text is inserted in the begining of the paragraph 
                    document.insertBeforeStart(pEl, "&nbsp;");
                    // insert html text actually
                    editorKit.insertHTML(document, caret + 1, htmlText, 1, 0, tag);
                    // remove blank char added before
                    document.remove(caret, 1);
                } else if (!isBreakingTag && isParagraphBegining) {
                    // insert blank char to avoid incorrect caret positioning
                    // after html text is inserted in the begining of the paragraph 
                    document.insertAfterStart(pEl, "&nbsp;");
                    // insert html text actually
                    editorKit.insertHTML(document, caret + 1, htmlText, 0, 0, tag);
                    // remove blank char added before
                    document.remove(caret, 1);
                }
                
            }
        }
        
        /**
         * inserts HTML-line-break as response to user's Enter-key pressing;
         * should be called immediately after Enter-key has been released
         * 
         * @param editor editor to insert line break to
         * @throws BadLocationException
         * @throws IOException
         */
        public static void insertLineBreakOnEnter(JTextPane editor) throws BadLocationException, IOException {
            if (editor.getEditorKit() instanceof HTMLEditorKit && editor.getDocument() instanceof HTMLDocument) {
                HTMLEditorKit editorKit = (HTMLEditorKit) editor.getEditorKit();
                HTMLDocument document = (HTMLDocument) editor.getDocument();

                int caret = editor.getCaretPosition();
                Element pEl = document.getParagraphElement(caret-1);
                boolean isParagraphBegining = caret-1 == pEl.getStartOffset();

                // insert HTML-line-break
                editorKit.insertHTML(document, caret-1, "<br>", 0, 0, HTML.Tag.BR);

                if (!isParagraphBegining) {
                    // remove plain line break
                    document.remove(caret-1, 1);
                }
                
            }
        }

    }

    private JToolBar jToolBar1 = null;
    private JToggleButton jToggleButton = null;
    private JToggleButton jToggleButton1 = null;
    private JToggleButton jToggleButton2 = null;
    private JToggleButton jToggleButton3 = null;
    private JButton jButton = null;
    private JButton jButton1 = null;
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
    public HTMLPage(UUID id, byte[] data) {
        super(id, data);
        initialize();
    }

    /* (non-Javadoc)
     * @see bias.gui.Extension#serialize()
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
                fontSize = DEFAULT_FONT.getSize();
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
                getJComboBox1().setSelectedItem(DEFAULT_FONT.getAttributes().get(StyleConstants.FontFamily));
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
        getJTextPane().getDocument().addUndoableEditListener(new UndoRedoManager(jTextPane));
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
            jToolBar1.add(getJButton1());  // Generated
            jToolBar1.add(getJButton());  // Generated
            jToolBar1.add(getJButton5());  // Generated
            jToolBar1.add(getJToggleButton());  // Generated
            jToolBar1.add(getJToggleButton1());  // Generated
            jToolBar1.add(getJToggleButton2());  // Generated
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
     * This method initializes jButton 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setToolTipText("entry link");  // Generated
            jButton.setIcon(HTMLPage.ICON_ENTRY_LINK);
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
                        for (Enumeration en = attrs.getAttributeNames(); en.hasMoreElements();) {
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
                    for (VisualEntryDescriptor veDescriptor : FrontEnd.getInstance().getVisualEntryDescriptors()) {
                        hrefComboBox.addItem(veDescriptor);
                        if (veDescriptor.getId().toString().equals(id)) {
                            currDescriptor = veDescriptor;
                        }
                    }
                    if (currDescriptor != null) {
                        hrefComboBox.setSelectedItem(currDescriptor);
                    }
                    JLabel textLabel = new JLabel("text:");
                    text = JOptionPane.showInputDialog(FrontEnd.getInstance(), 
                            new Component[]{entryLabel, hrefComboBox, textLabel}, text);
                    if (text != null) {
                        try {
                            StringBuffer linkHTML = new StringBuffer("<a ");
                            linkHTML.append("href=\"" + Constants.ENTRY_PROTOCOL_PREFIX);
                            if (!Validator.isNullOrBlank(hrefComboBox.getSelectedItem())) {
                                linkHTML.append(((VisualEntryDescriptor)hrefComboBox.getSelectedItem()).getId());
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
                            HTMLPageEditor.insertHTML(getJTextPane(), linkHTML.toString(), HTML.Tag.A);
                        } catch (BadLocationException exception) {
                            FrontEnd.getInstance().displayErrorMessage(exception);
                        } catch (IOException exception) {
                            FrontEnd.getInstance().displayErrorMessage(exception);
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
            jButton1.setToolTipText("URL link");  // Generated
            jButton1.setIcon(HTMLPage.ICON_URL_LINK);
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
                        for (Enumeration en = attrs.getAttributeNames(); en.hasMoreElements();) {
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
                    href = JOptionPane.showInputDialog(FrontEnd.getInstance(), 
                            new Component[]{textLabel, textField, urlLabel}, href);
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
                            HTMLPageEditor.insertHTML(getJTextPane(), linkHTML.toString(), HTML.Tag.A);
                        } catch (BadLocationException exception) {
                            FrontEnd.getInstance().displayErrorMessage(exception);
                        } catch (IOException exception) {
                            FrontEnd.getInstance().displayErrorMessage(exception);
                        }
                    }
                    getJTextPane().requestFocusInWindow();
                }
            });    
        }
        return jButton1;
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
     * This method initializes jTextPane    
     *  
     * @return javax.swing.JTextPane    
     */
    private JTextPane getJTextPane() {
        if (jTextPane == null) {
            jTextPane = new JTextPane(){
                private static final long serialVersionUID = 1L;
                @Override
                public void paint(Graphics g) {
                    // enable font anti-aliasing
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    super.paint(g);
                }
            };
            jTextPane.setEditable(false);
            jTextPane.setEditorKit(new HTMLEditorKit());

            // set default font for JTextPane instance
            MutableAttributeSet attrs = jTextPane.getInputAttributes();
            StyleConstants.setFontFamily(attrs, DEFAULT_FONT.getFamily());
            StyleConstants.setFontSize(attrs, DEFAULT_FONT.getSize());
            StyleConstants.setItalic(attrs, (DEFAULT_FONT.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(attrs, (DEFAULT_FONT.getStyle() & Font.BOLD) != 0);
            StyledDocument doc = jTextPane.getStyledDocument();
            doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);

            jTextPane.addCaretListener(new CaretListener(){
                public void caretUpdate(CaretEvent e) {
                    JTextPane textPane = (JTextPane) e.getSource();
                    synchronizeEditNoteControlsStates(textPane);
                }
            });
            jTextPane.addKeyListener(new KeyListener(){
                public void keyTyped(KeyEvent e) {
                }
                public void keyPressed(KeyEvent e) {
                }
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        try {
                            HTMLPageEditor.insertLineBreakOnEnter(getJTextPane());
                        } catch (Exception exception) {
                            FrontEnd.getInstance().displayErrorMessage(exception);
                        }
                    }
                }
            });
            jTextPane.addHyperlinkListener(new HyperlinkListener(){
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                        if (e.getDescription().startsWith(Constants.ENTRY_PROTOCOL_PREFIX)) {
                            String idStr = e.getDescription().substring(Constants.ENTRY_PROTOCOL_PREFIX.length());
                            FrontEnd.getInstance().switchToVisualEntry(UUID.fromString(idStr));
                        } else {
                            try {
                                BrowserLauncher.openURL(e.getDescription());
                            } catch (Exception ex) {
                                FrontEnd.getInstance().displayErrorMessage(ex);
                            }
                        }
                    }
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