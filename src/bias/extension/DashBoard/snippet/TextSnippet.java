/**
 * Created on Apr 15, 2008
 */
package bias.extension.DashBoard.snippet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import bias.Constants;
import bias.extension.DashBoard.DashBoard;
import bias.gui.FrontEnd;
import bias.gui.editor.UndoRedoManager;
import bias.utils.CommonUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class TextSnippet extends InfoSnippet {
    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_SWITCH_MODE = new ImageIcon(CommonUtils.getResourceURL(DashBoard.class, "editor/switch_mode.png"));
    
    private static final ImageIcon ICON_INCREASE_FONT_SIZE = new ImageIcon(CommonUtils.getResourceURL(DashBoard.class, "editor/increase.png"));
    
    private static final ImageIcon ICON_DECREASE_FONT_SIZE = new ImageIcon(CommonUtils.getResourceURL(DashBoard.class, "editor/decrease.png"));
    
    private static final String PROPERTY_FONT_SIZE = "FONT_SIZE";
    
    private static final int[] FONT_SIZES = new int[]{ 8, 10, 12, 14, 18, 24, 36 };

    private static final int DEFAULT_FONT_SIZE = FONT_SIZES[2];
    
    private static final String PROPERTY_SCROLLBAR_VERT = "SCROLLBAR_VERT";
    
    private static final String PROPERTY_SCROLLBAR_HORIZ = "SCROLLBAR_HORIZ";
    
    private static final String PROPERTY_CARET_POSITION = "CARET_POSITION";

    private static final String HTML_STYLES_PATTERN = "(?i)(?s)<style.*</style>";

    private int currentFontSize = DEFAULT_FONT_SIZE;

    private boolean contentChanged = false;
    
    private Properties settings;
    
    private byte[] content;

    private JScrollPane jScrollPane = null;
    private JTextPane jTextPane = null;
    private JToolBar jToolBar = null;
    private JToggleButton jToggleButton = null;
    private JButton jButton1 = null;
    private JButton jButton2 = null;

    public TextSnippet(UUID dataEntryID, UUID id, byte[] content, byte[] settings) {
        super(dataEntryID, id, content, settings, true, true);
        initialize();
    }

    private void initialize() {
        this.setSize(733, 515);
        this.setLayout(new BorderLayout());
        if (getContent() != null) {
            content = getContent();
            String code = new String(content);
            // ===============================================================
            // some skins might provide (and rely on) styling 
            // for default HTMLEditorKit, in which case custom styles 
            // that could have been added to the document earlier, 
            // would break that styling; 
            // thus, need to remove custom styles for such skins;
            // NOTE: this would not turn into problem after switching to 
            // another skin / CustomHTMLEditorKit, as styles needed 
            // by the latter would get automatically re-added to document upon 
            // first attempt to initialize it using CustomHTMLEditorKit
            // ===============================================================
            if (FrontEnd.isDefaultHTMLEditorKitRequired()) {
                code = code.replaceAll(HTML_STYLES_PATTERN, Constants.EMPTY_STR);
            }
            // ===============================================================
            getJTextPane().setText(code);
        }
        settings = PropertiesUtils.deserializeProperties(getSettings());
        applySettings();
        String caretPos = settings.getProperty(PROPERTY_CARET_POSITION);
        if (!Validator.isNullOrBlank(caretPos)) {
            try {
                getJTextPane().setCaretPosition(Integer.valueOf(caretPos));
            } catch (IllegalArgumentException iae) {
                // ignore incorrect caret positioning
            }
        }
        SwingUtilities.invokeLater(() -> {
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
        });
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
                    contentChanged = true;
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
            if (!FrontEnd.isDefaultHTMLEditorKitRequired()) {
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
            }
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

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getRepresentation()
     */
    @Override
    protected Container getRepresentation() {
        return getJTextPane();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#configure()
     */
    @Override
    public void configure() throws Throwable {
        JLabel fsLb = new JLabel("Font size:");
        JComboBox<String> fsCb = new JComboBox<>();
        for (Integer fs : FONT_SIZES) {
            fsCb.addItem("" + fs);
        }
        String selValue = "" + currentFontSize;
        fsCb.setSelectedItem(selValue);
        int opt = JOptionPane.showConfirmDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{fsLb, fsCb}, 
                "Settings", 
                JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            currentFontSize = Integer.valueOf((String) fsCb.getSelectedItem());
            settings.setProperty(PROPERTY_FONT_SIZE, "" + currentFontSize);
            applySettings();
        }
    }
    
    public void applySettings() {
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
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() {
        Collection<String> searchData = new ArrayList<String>();
        searchData.add(getText());
        return searchData;
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#highlightSearchResults(java.lang.String, boolean, boolean)
     */
    @Override
    public void highlightSearchResults(String searchExpression, boolean isCaseSensitive, boolean isRegularExpression) throws Throwable {
        Highlighter hl = getJTextPane().getHighlighter();
        hl.removeAllHighlights();
        String text = getText();
        if (!Validator.isNullOrBlank(text)) {
            HighlightPainter hlPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
            Pattern pattern = isRegularExpression ? Pattern.compile(searchExpression) : null;
            if (pattern != null) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    hl.addHighlight(matcher.start(), matcher.end(), hlPainter);
                }
            } else {
                int index = -1;
                do {
                    int fromIdx = index != -1 ? index + searchExpression.length() : 0;
                    if (isCaseSensitive) {
                        index = text.indexOf(searchExpression, fromIdx);
                    } else {
                        index = text.toLowerCase().indexOf(searchExpression.toLowerCase(), fromIdx);
                    }
                    if (index != -1) {
                        hl.addHighlight(index, index + searchExpression.length(), hlPainter);
                    }
                } while (index != -1);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#clearSearchResultsHighlight()
     */
    @Override
    public void clearSearchResultsHighlight() throws Throwable {
        getJTextPane().getHighlighter().removeAllHighlights();
    }
    
    private String getText() {
        Document doc = getJTextPane().getDocument();
        String text = null;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            // ignore, shouldn't happen ever
        }
        return text;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#serializeContent()
     */
    @Override
    public byte[] serializeContent() {
        if (contentChanged) {
            content = getJTextPane().getText().getBytes();
            contentChanged = false;
        }
        return content;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() {
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
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(240, 120);
    }

}
