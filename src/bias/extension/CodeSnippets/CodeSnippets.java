/**
 * Created on Jun 11, 2017
 */
package bias.extension.CodeSnippets;

import bias.extension.CodeSnippets.xmlb.ObjectFactory;
import bias.extension.CodeSnippets.xmlb.Snippet;
import bias.extension.CodeSnippets.xmlb.Snippets;
import bias.extension.EntryExtension;
import bias.gui.FrontEnd;
import bias.utils.CommonUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author kion
 */
public class CodeSnippets extends EntryExtension {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_ADD = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "add-snippet.png"));
    private static final ImageIcon ICON_RENAME = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "rename-snippet.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "delete-snippet.png"));
    private static final ImageIcon ICON_MOVE_UP = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "move-up.png"));
    private static final ImageIcon ICON_MOVE_DOWN = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "move-down.png"));
    private static final ImageIcon ICON_SWITCH_MODE = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "switch-mode.png"));
    
    private static final Color FG_COLOR = new Color(41, 49, 52);
    private static final Color BG_COLOR = new Color(127, 139, 151);
    private static final Color BG_COLOR_SELECTED = new Color(224, 226, 228);
    
    private static final Color DARK_THEME_SEARCH_HL_COLOR = new Color(170, 0, 0);
    
    private static final List<String> LANGUAGES = getLangs();
    private static final List<String> getLangs() {
        // TODO/FIXME this is a rather weird way to get list of supported languages,
        // but I wasn't able to find a better/official way to do this...
        List<String> langs = new ArrayList<>();
        Field[] fields = SyntaxConstants.class.getFields();
        for (Field field : fields) {
            if (field.getName().startsWith("SYNTAX_STYLE_")) {
                try {
                    langs.add((String) field.get(SyntaxConstants.class));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return langs;
    }
    
    private static final String DEFAULT_THEME = "default";

    private static final List<String> DARK_THEMES = Arrays.asList(new String[]{ "dark", "monokai" });

    private static final String[] THEMES = new String[]{
        DEFAULT_THEME,
        "default-alt",
        "eclipse",
        "idea",
        "vs",
        DARK_THEMES.get(0),
        DARK_THEMES.get(1)
    };
    
    private static final int[] FONT_SIZES = IntStream.range(12, 37).toArray();
    
    private static final Integer DEFAULT_FONT_SIZE = FONT_SIZES[0];
    
    private static final Map<String, Font> FONTS = initFonts();
    
    private static final Map<String, Font> initFonts() {
        Map<String, Font> fonts = new LinkedHashMap<>();
        initFont(fonts, "Hack", "hack");
        initFont(fonts, "Inconsolata", "inconsolata");
        initFont(fonts, "JetBrains Mono", "jetbrainsmono");
        initFont(fonts, "M+ 2m", "mplus-2m");
        initFont(fonts, "Roboto Mono", "roboto-mono");
        initFont(fonts, "SourceCode Pro", "sourcecode-pro");
        fonts.put(Font.MONOSPACED, new Font(Font.MONOSPACED, Font.PLAIN, DEFAULT_FONT_SIZE));
        return fonts;
    }
    
    private static final void initFont(Map<String, Font> fonts, String fontName, String fontID) {
        try {
            fonts.put(fontName, Font.createFont(Font.TRUETYPE_FONT, CommonUtils.getResourceAsStream(CodeSnippets.class, "font/" + fontID + ".ttf")));
        } catch (Throwable cause) {
            // ignore font
        }
    }

    private static final String DEFAULT_FONT_NAME = FONTS.keySet().iterator().next();
    
    private static final String PROPERTY_EDITOR_THEME = "EDITOR_THEME";

    private static final String PROPERTY_FONT_NAME = "FONT_NAME";

    private static final String PROPERTY_FONT_SIZE = "FONT_SIZE";
    
    private static final String PROPERTY_SELECTED_IDX = "SELECTED_IDX";
    
    private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/addons/CodeSnippetsSchema.xsd";
    
    private static Map<String, ImageIcon> LangIcons = new HashMap<>();

    private static JAXBContext context;

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    private static ObjectFactory objFactory = new ObjectFactory();

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), ObjectFactory.class.getClassLoader());
        }
        return context;
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        if (unmarshaller == null) {
            unmarshaller = getContext().createUnmarshaller();
        }
        return unmarshaller;
    }

    private static Marshaller getMarshaller() throws JAXBException {
        if (marshaller == null) {
            marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        }
        return marshaller;
    }

    private Map<String, String> descriptions = new HashMap<>();
    
    private Map<String, String> codeSnippetLangs = new HashMap<>();
    
    private Map<String, String> codeSnippets = new HashMap<>();
    
    private Map<String, Integer> verticalScrollbarPosMap = new HashMap<>();
    
    private Map<String, Integer> horizontalScrollbarPosMap = new HashMap<>();
    
    private Map<String, Integer> caretPosMap = new HashMap<>();
    
    private JList<String> list;
    
    private DefaultListModel<String> listModel;
    
    private JSplitPane splitPane;
    
    private JToolBar toolbarEdit;
    
    private JToggleButton editSnippetBtn;
    
    private Properties settings;

    private String themeName;
    
    private String fontName;
    
    private Float fontSize;
    
    public CodeSnippets(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        initUI();
        if (getData() != null && getData().length != 0) {
            Snippets snippets = (Snippets) getUnmarshaller().unmarshal(new ByteArrayInputStream(getData()));
            for (Snippet snippet : snippets.getSnippet()) {
                descriptions.put(snippet.getName(), snippet.getDescription());
                codeSnippetLangs.put(snippet.getName(), snippet.getLanguage());
                codeSnippets.put(snippet.getName(), snippet.getCode());
                if (snippet.getVerticalScrollbarPos() != null) {
                    verticalScrollbarPosMap.put(snippet.getName(), snippet.getVerticalScrollbarPos());
                }
                if (snippet.getHorizontalScrollbarPos() != null) {
                    horizontalScrollbarPosMap.put(snippet.getName(), snippet.getHorizontalScrollbarPos());
                }
                if (snippet.getCaretPos() != null) {
                    caretPosMap.put(snippet.getName(), snippet.getCaretPos());
                }
                listModel.addElement(snippet.getName());
            }
        }
        applySettings(getSettings());
        String csi = this.settings.getProperty(PROPERTY_SELECTED_IDX);
        if (csi != null) {
            list.setSelectedIndex(Integer.valueOf(csi));
        }
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        JLabel themeLabel = new JLabel("Editor Theme");
        JComboBox<String> themeComboBox = createThemeSelector();
        themeComboBox.setSelectedItem(themeName);
        JLabel fontFamilyLabel = new JLabel("Font Name");
        JComboBox<String> fontFamilyComboBox = createFontFamilySelector();
        fontFamilyComboBox.setSelectedItem(fontName);
        JLabel fontSizeLabel = new JLabel("Font Size");
        JComboBox<Integer> fontSizeComboBox = createFontSizeSelector();
        fontSizeComboBox.setSelectedItem(fontSize.intValue());
        int opt = JOptionPane.showConfirmDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{themeLabel, themeComboBox, fontFamilyLabel, fontFamilyComboBox, fontSizeLabel, fontSizeComboBox}, 
                "Settings for " + this.getClass().getSimpleName() + " extension", 
                JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            newSettings.setProperty(PROPERTY_EDITOR_THEME, (String) themeComboBox.getSelectedItem().toString());
            newSettings.setProperty(PROPERTY_FONT_NAME, (String) fontFamilyComboBox.getSelectedItem());
            newSettings.setProperty(PROPERTY_FONT_SIZE, fontSizeComboBox.getSelectedItem().toString());
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
        String cet = this.settings.getProperty(PROPERTY_EDITOR_THEME);
        if (cet != null) {
            themeName = cet;
        } else {
            themeName = DEFAULT_THEME;
        }
        String cfn = this.settings.getProperty(PROPERTY_FONT_NAME);
        if (cfn != null) {
            fontName = cfn;
        } else {
            fontName = DEFAULT_FONT_NAME;
        }
        String cfs = this.settings.getProperty(PROPERTY_FONT_SIZE);
        if (cfs != null) {
            fontSize = Float.valueOf(cfs);
        } else {
            fontSize = DEFAULT_FONT_SIZE.floatValue();
        }
        applySettings();
    }
    
    private void applySettings() {
        JSplitPane sp = splitPane.getRightComponent() != null ? (JSplitPane) splitPane.getRightComponent() : null;
        if (sp != null) {
            JTextArea descriptionEditor = (JTextArea) sp.getTopComponent();
            if (descriptionEditor != null) {
                handleEditorFont(descriptionEditor);
            }
            RTextScrollPane codeEditor = (RTextScrollPane) sp.getBottomComponent();
            if (codeEditor != null) {
                handleEditorTheme((RSyntaxTextArea) codeEditor.getTextArea());
                handleEditorFont(codeEditor.getTextArea());
            }
            sp.setTopComponent(descriptionEditor);
            sp.setBottomComponent(codeEditor);
        }
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        settings.setProperty(PROPERTY_EDITOR_THEME, themeName);
        settings.setProperty(PROPERTY_FONT_NAME, fontName);
        settings.setProperty(PROPERTY_FONT_SIZE, "" + fontSize);
        if (list.getSelectedIndex() != -1) {
            settings.setProperty(PROPERTY_SELECTED_IDX, "" + list.getSelectedIndex());
        } else {
            settings.remove(PROPERTY_SELECTED_IDX);
        }
        return PropertiesUtils.serializeProperties(settings);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        // make sure current editor's code is saved before proceeding further
        JSplitPane sp = splitPane.getRightComponent() != null ? (JSplitPane) splitPane.getRightComponent() : null;
        if (sp != null) {
            if (list.getSelectedIndex() != -1) {
                String name = listModel.get(list.getSelectedIndex());
                JTextArea descriptionEditor = (JTextArea) sp.getTopComponent();
                if (descriptionEditor != null) {
                    descriptions.put(name, descriptionEditor.getText());
                }
                RTextScrollPane scrollPane = (RTextScrollPane) sp.getBottomComponent();
                RSyntaxTextArea codeEditor = (RSyntaxTextArea) scrollPane.getTextArea();
                if (codeEditor != null) {
                    codeSnippets.put(name, codeEditor.getText());
                    JScrollBar vsb = scrollPane.getVerticalScrollBar();
                    if (vsb != null && vsb.getValue() != 0) {
                        verticalScrollbarPosMap.put(name, vsb.getValue());
                    } else {
                        verticalScrollbarPosMap.remove(name);
                    }
                    JScrollBar hsb = scrollPane.getHorizontalScrollBar();
                    if (hsb != null && hsb.getValue() != 0) {
                        horizontalScrollbarPosMap.put(name, hsb.getValue());
                    } else {
                        horizontalScrollbarPosMap.remove(name);
                    }
                    int cp = codeEditor.getCaretPosition();
                    caretPosMap.put(name, cp);
                }
            }
        }
        // build data structure to get serialized
        Snippets snippets = objFactory.createSnippets();
        for (int i = 0; i < listModel.getSize(); i++) {
            String name = listModel.get(i);
            String lang = codeSnippetLangs.get(name);
            String code = codeSnippets.get(name);
            String descr = descriptions.get(name);
            Snippet snippet = objFactory.createSnippet();
            snippet.setName(name);
            snippet.setDescription(descr);
            snippet.setLanguage(lang);
            snippet.setCode(code);
            snippet.setVerticalScrollbarPos(verticalScrollbarPosMap.get(name));
            snippet.setHorizontalScrollbarPos(horizontalScrollbarPosMap.get(name));
            snippet.setCaretPos(caretPosMap.get(name));
            snippets.getSnippet().add(snippet);
        }
        // serialize data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getMarshaller().marshal(snippets, baos);
        return baos.toByteArray();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        Collection<String> searchData = new ArrayList<String>();
        for (Entry<String, String> cs : codeSnippets.entrySet()) {
            searchData.add(cs.getKey());
            searchData.add(cs.getValue());
        }
        for (String d : descriptions.values()) {
            searchData.add(d);
        }
        return searchData;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#highlightSearchResults(java.lang.String, boolean, boolean)
     */
    @Override
    public void highlightSearchResults(String searchExpression, boolean isCaseSensitive, boolean isRegularExpression) throws Throwable {
        JSplitPane sp = splitPane.getRightComponent() != null ? (JSplitPane) splitPane.getRightComponent() : null;
        if (sp != null) {
            JTextArea descriptionEditor = (JTextArea) sp.getTopComponent();
            if (descriptionEditor != null) {
                highlightSearchResults(descriptionEditor, searchExpression, isCaseSensitive, isRegularExpression);
            }
            RSyntaxTextArea codeEditor = (RSyntaxTextArea) ((RTextScrollPane) sp.getBottomComponent()).getTextArea();
            if (codeEditor != null) {
                highlightSearchResults(codeEditor, searchExpression, isCaseSensitive, isRegularExpression);
            }
        }
    }
    
    private void highlightSearchResults(JTextArea editor, String searchExpression, boolean isCaseSensitive, boolean isRegularExpression) throws BadLocationException {
        Highlighter hl = editor.getHighlighter();
        hl.removeAllHighlights();
        String text = editor.getText();
        if (!Validator.isNullOrBlank(text)) {
            boolean isDarkEditorTheme = editor instanceof RSyntaxTextArea && DARK_THEMES.contains(themeName);
            HighlightPainter hlPainter = new DefaultHighlighter.DefaultHighlightPainter(isDarkEditorTheme ? DARK_THEME_SEARCH_HL_COLOR : Color.YELLOW);
            boolean first = true;
            Pattern pattern = isRegularExpression ? Pattern.compile(searchExpression) : null;
            if (pattern != null) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    hl.addHighlight(matcher.start(), matcher.end(), hlPainter);
                    if (first) {
                        Rectangle viewRect = editor.modelToView(matcher.start());
                        editor.scrollRectToVisible(viewRect);
                        first = false;
                    }
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
                        if (first) {
                            Rectangle viewRect = editor.modelToView(index);
                            editor.scrollRectToVisible(viewRect);
                            first = false;
                        }
                    }
                } while (index != -1);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#clearSearchResultsHighlight()
     */
    @Override
    public void clearSearchResultsHighlight() throws Throwable {
        JSplitPane sp = splitPane.getRightComponent() != null ? (JSplitPane) splitPane.getRightComponent() : null;
        if (sp != null) {
            JTextArea descriptionEditor = (JTextArea) sp.getTopComponent();
            if (descriptionEditor != null) {
                descriptionEditor.getHighlighter().removeAllHighlights();
            }
            RSyntaxTextArea codeEditor = (RSyntaxTextArea) ((RTextScrollPane) sp.getBottomComponent()).getTextArea();
            if (codeEditor != null) {
                codeEditor.getHighlighter().removeAllHighlights();
            }
        }
    }
    
    private void initUI() {
        this.setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.SOUTH);
        splitPane = new JSplitPane();
        splitPane.setLeftComponent(createCodeSnippetList());
        splitPane.setRightComponent(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private Component createToolbar() {
        JPanel toolbarPane = new JPanel(new BorderLayout());
        JToolBar toolbarManage = new JToolBar();
        toolbarManage.setFloatable(false);
        JButton addSnippetBtn = new JButton(ICON_ADD);
        addSnippetBtn.setToolTipText("add snippet");
        toolbarManage.add(addSnippetBtn);
        JButton renameSnippetBtn = new JButton(ICON_RENAME);
        renameSnippetBtn.setToolTipText("rename snippet");
        toolbarManage.add(renameSnippetBtn);
        JButton moveSnippetUpBtn = new JButton(ICON_MOVE_UP);
        moveSnippetUpBtn.setToolTipText("move snippet up");
        toolbarManage.add(moveSnippetUpBtn);
        JButton moveSnippetDownBtn = new JButton(ICON_MOVE_DOWN);
        moveSnippetDownBtn.setToolTipText("move snippet down");
        toolbarManage.add(moveSnippetDownBtn);
        JButton delSnippetBtn = new JButton(ICON_DELETE);
        delSnippetBtn.setToolTipText("delete snippet");
        toolbarManage.add(delSnippetBtn);
        toolbarPane.add(toolbarManage, BorderLayout.WEST);
        toolbarEdit = new JToolBar();
        toolbarEdit.setFloatable(false);
        editSnippetBtn = new JToggleButton(ICON_SWITCH_MODE);
        editSnippetBtn.setToolTipText("switch edit mode");
        toolbarEdit.add(editSnippetBtn);
        toolbarEdit.setVisible(false);
        toolbarPane.add(toolbarEdit, BorderLayout.EAST);
        editSnippetBtn.addActionListener($ -> {
            JSplitPane sp = splitPane.getRightComponent() != null ? (JSplitPane) splitPane.getRightComponent() : null;
            if (sp != null) {
                JTextArea descriptionEditor = (JTextArea) sp.getTopComponent();
                if (descriptionEditor != null) {
                    descriptionEditor.setEditable(!descriptionEditor.isEditable());
                }
                RSyntaxTextArea codeEditor = (RSyntaxTextArea) ((RTextScrollPane) sp.getBottomComponent()).getTextArea();
                if (codeEditor != null) {
                    codeEditor.setEditable(!codeEditor.isEditable());
                    if (codeEditor.isEditable()) {
                        codeEditor.requestFocusInWindow();
                    }
                }
            }
        });
        renameSnippetBtn.addActionListener($ -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                String name = listModel.get(idx);
                String newName = JOptionPane.showInputDialog(CodeSnippets.this, "Snippet Name", name);
                if (newName != null && !"".equals(newName.trim())) {
                    if (listModel.contains(newName)) {
                        FrontEnd.displayErrorMessage("Snippet with given name already exists. Snippet name must be unique.");
                    } else {
                        list.clearSelection();
                        listModel.remove(idx);
                        String code = codeSnippets.remove(name);
                        String language = codeSnippetLangs.remove(name);
                        String description = descriptions.remove(name);
                        codeSnippets.put(newName, code);
                        codeSnippetLangs.put(newName, language);
                        descriptions.put(newName, description);
                        listModel.add(idx, newName);
                        list.setSelectedIndex(idx);
                    }
                }
            }
        });
        addSnippetBtn.addActionListener($ -> {
            JLabel langLabel = new JLabel("Language:");
            JComboBox<String> langSelector = new JComboBox<>();
            for (String lang : LANGUAGES) {
                langSelector.addItem(lang);
            }
            langSelector.setSelectedItem(SyntaxConstants.SYNTAX_STYLE_NONE);
            JLabel nameLabel = new JLabel("Snippet Name:");
            String name = JOptionPane.showInputDialog(CodeSnippets.this, new Component[] { langLabel, langSelector, nameLabel });
            if (name != null && !"".equals(name.trim())) {
                if (listModel.contains(name)) {
                    FrontEnd.displayErrorMessage("Snippet with given name already exists. Snippet name must be unique.");
                } else {
                    String language = langSelector.getSelectedItem() != null ? langSelector.getSelectedItem().toString() : null;
                    if (language != null) {
                        codeSnippetLangs.put(name, language);
                        listModel.addElement(name);
                        list.setSelectedIndex(listModel.getSize() - 1);
                    }
                }
            }
        });
        delSnippetBtn.addActionListener($ -> {
            int selIdx = list.getSelectedIndex();
            if (selIdx != -1) {
                String name = listModel.get(selIdx);
                if (JOptionPane.showConfirmDialog(CodeSnippets.this, 
                        "<html>Are you sure you want to delete <strong>" + name + "</strong> snippet?</html>", 
                        "Delete Snippet", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    doWithListSelectionListenersDisabled(() -> {
                        listModel.remove(selIdx);
                        list.clearSelection();
                        splitPane.setRightComponent(null);
                        descriptions.remove(name);
                        codeSnippets.remove(name);
                        codeSnippetLangs.remove(name);
                    });
                }
            }
        });
        moveSnippetUpBtn.addActionListener($ -> {
            int selIdx = list.getSelectedIndex();
            if (selIdx != -1 && selIdx != 0) {
                doWithListSelectionListenersDisabled(() -> {
                    swapListItems(selIdx, selIdx - 1);
                    list.setSelectedIndex(selIdx - 1);
                    list.ensureIndexIsVisible(selIdx - 1);
                });
            }
        });
        moveSnippetDownBtn.addActionListener($ -> {
            int selIdx = list.getSelectedIndex();
            if (selIdx != -1 && selIdx != listModel.getSize() - 1) {
                doWithListSelectionListenersDisabled(() -> {
                    swapListItems(selIdx, selIdx + 1);
                    list.setSelectedIndex(selIdx + 1);
                    list.ensureIndexIsVisible(selIdx + 1);
                });
            }
        });
        return toolbarPane;
    }

    private void swapListItems(int item1, int item2) {
        String item1El = listModel.getElementAt(item1);
        String item2El = listModel.getElementAt(item2);
        listModel.set(item1, item2El);
        listModel.set(item2, item1El);
    }

    private void doWithListSelectionListenersDisabled(Runnable runnable) {
        ListSelectionListener[] selectionListeners = list.getListSelectionListeners();
        for (ListSelectionListener listener : selectionListeners) {
            list.removeListSelectionListener(listener);
        }
        runnable.run();
        for (ListSelectionListener listener : selectionListeners) {
            list.addListSelectionListener(listener);
        }
    }
    
    private JList<String> createCodeSnippetList() {
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setBackground(BG_COLOR);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel(value + "   ", getLangIcon(codeSnippetLangs.get(value)), SwingConstants.LEADING);
                label.setOpaque(true);
                label.setForeground(FG_COLOR);
                if (isSelected) {
                    JSplitPane sp = splitPane.getRightComponent() != null ? (JSplitPane) splitPane.getRightComponent() : null;
                    if (sp != null) {
                        RSyntaxTextArea codeEditor = (RSyntaxTextArea) ((RTextScrollPane) sp.getBottomComponent()).getTextArea();
                        if (codeEditor != null) {
                            label.setBackground(BG_COLOR_SELECTED);
                        }
                    }
                } else {
                    label.setBackground(BG_COLOR);
                }
                Font font = FONTS.get(fontName);
                if (font == null) {
                    font = FONTS.values().iterator().next();
                }
                label.setFont(font.deriveFont(fontSize));
                return label;
            }
        });
        list.addListSelectionListener($ -> {
            if (!$.getValueIsAdjusting()) {
                int current = list.getSelectedIndex();
                int previous = current == $.getFirstIndex() ? $.getLastIndex() : $.getFirstIndex();
                String curr = current != -1 ? listModel.get(current) : null;
                String prev = previous != -1 && !listModel.isEmpty() && listModel.getSize() > previous ? listModel.get(previous) : null;
                if (prev != null) {
                    JSplitPane sp = splitPane.getRightComponent() != null ? (JSplitPane) splitPane.getRightComponent() : null;
                    if (sp != null) {
                        JTextArea descriptionEditor = (JTextArea) sp.getTopComponent();
                        if (descriptionEditor != null) {
                            descriptions.put(prev, descriptionEditor.getText());
                        }
                        RTextScrollPane scrollPane = (RTextScrollPane) sp.getBottomComponent();
                        RSyntaxTextArea codeEditor = (RSyntaxTextArea) scrollPane.getTextArea();
                        if (codeEditor != null) {
                            codeSnippets.put(prev, codeEditor.getText());
                            JScrollBar vsb = scrollPane.getVerticalScrollBar();
                            if (vsb != null && vsb.getValue() != 0) {
                                verticalScrollbarPosMap.put(prev, vsb.getValue());
                            } else {
                                verticalScrollbarPosMap.remove(prev);
                            }
                            JScrollBar hsb = scrollPane.getHorizontalScrollBar();
                            if (hsb != null && hsb.getValue() != 0) {
                                horizontalScrollbarPosMap.put(prev, hsb.getValue());
                            } else {
                                horizontalScrollbarPosMap.remove(prev);
                            }
                            int cp = codeEditor.getCaretPosition();
                            caretPosMap.put(prev, cp);
                        }
                    }
                }
                if (curr != null) {
                    String description = descriptions.get(curr);
                    String code = codeSnippets.get(curr);
                    String language = codeSnippetLangs.get(curr);
                    RTextScrollPane codeEditor = createCodeEditor(curr, language, code);
                    JTextArea descriptionEditor = createDescriptionEditor(description);
                    editSnippetBtn.setSelected(false);
                    JSplitPane sp = new JSplitPane();
                    sp.setOrientation(JSplitPane.VERTICAL_SPLIT);
                    sp.setTopComponent(descriptionEditor);
                    sp.setBottomComponent(codeEditor);
                    splitPane.setRightComponent(sp);
                    applySettings();
                } else {
                    splitPane.setRightComponent(null);
                }
                toolbarEdit.setVisible(curr != null);
            }
        });
        return list;
    }

    private JTextArea createDescriptionEditor(String description) {
        JTextArea descriptionEditor = new JTextArea();
        descriptionEditor.setBackground(BG_COLOR);
        descriptionEditor.setForeground(FG_COLOR);
        descriptionEditor.setText(description);
        descriptionEditor.setEditable(false);
        return descriptionEditor;
    }
    
    private RTextScrollPane createCodeEditor(String name, String language, String code) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(code);
        textArea.setSyntaxEditingStyle(language);
        textArea.setCodeFoldingEnabled(false);
        textArea.setEditable(false);
        Integer cPos = caretPosMap.get(name);
        if (cPos != null) {
            try {
                textArea.setCaretPosition(cPos);
            } catch (IllegalArgumentException iae) {
                // ignore incorrect caret positioning
            }
        }
        RTextScrollPane sp = new RTextScrollPane(textArea);
        SwingUtilities.invokeLater(() -> {
            Integer vsbPos = verticalScrollbarPosMap.get(name);
            if (vsbPos != null) {
                JScrollBar vsb = sp.getVerticalScrollBar();
                vsb.setVisibleAmount(0);
                vsb.setValue(vsb.getMaximum());
                vsb.setValue(Integer.valueOf(vsbPos));
            }
            Integer hsbPos = horizontalScrollbarPosMap.get(name);
            if (hsbPos != null) {
                JScrollBar hsb = sp.getHorizontalScrollBar();
                hsb.setVisibleAmount(0);
                hsb.setValue(hsb.getMaximum());
                hsb.setValue(Integer.valueOf(vsbPos));
            }
        });
        return sp;
    }
    
    private JComboBox<String> createThemeSelector() {
        JComboBox<String> themeComboBox = new JComboBox<>();
        themeComboBox.setToolTipText("theme"); 
        themeComboBox.setRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(" " + value + " ");
            }
        });
        for (String theme : THEMES) {
            themeComboBox.addItem(theme);
        }
        themeComboBox.setSelectedItem(DEFAULT_THEME);
        return themeComboBox;
    }
    
    private JComboBox<String> createFontFamilySelector() {
        JComboBox<String> fontFamilyComboBox = new JComboBox<>();
        fontFamilyComboBox.setToolTipText("font"); 
        fontFamilyComboBox.setRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(" " + value + " ");
            }
        });
        for (String f : FONTS.keySet()) {
            fontFamilyComboBox.addItem(f);
        }
        return fontFamilyComboBox;
    }

    private JComboBox<Integer> createFontSizeSelector() {
        JComboBox<Integer> fontSizeComboBox = new JComboBox<>();
        fontSizeComboBox.setToolTipText("font family"); 
        fontSizeComboBox.setRenderer(new ListCellRenderer<Integer>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(" " + value + " ");
            }
        });
        for (Integer ff : FONT_SIZES) {
            fontSizeComboBox.addItem(ff);
        }
        return fontSizeComboBox;
    }
    
    private void handleEditorFont(JTextArea editor) {
        Font font = FONTS.get(fontName);
        if (font == null) {
            font = FONTS.values().iterator().next();
        }
        editor.setFont(font.deriveFont(fontSize)); 
    }
    
    private void handleEditorTheme(RSyntaxTextArea editor) {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + themeName + ".xml"));
            theme.apply(editor);
        } catch (IOException ioe) {
            FrontEnd.displayErrorMessage("Failed to apply code editor theme", ioe);
        }
    }
    
    private Icon getLangIcon(String lang) {
        int idx = lang.indexOf('/');
        if (idx != -1) {
            lang = lang.substring(idx + 1);
        }
        ImageIcon icon = LangIcons.get(lang);
        if (icon == null) {
            try {
                icon = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "lang/" + lang + ".png"));
            } catch (Throwable cause) {
                icon = new ImageIcon(CommonUtils.getResourceURL(CodeSnippets.class, "lang/code.png"));
            }
            LangIcons.put(lang, icon);
        }
        return icon;
    }
    
}
