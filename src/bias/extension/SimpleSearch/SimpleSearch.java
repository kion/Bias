/**
 * Created on Jul 16, 2007
 */
package bias.extension.SimpleSearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import bias.core.Recognizable;
import bias.extension.EntryExtension;
import bias.extension.ExtensionFactory;
import bias.extension.ToolExtension;
import bias.extension.ToolRepresentation;
import bias.extension.SimpleSearch.SearchEngine.HighLightMarker;
import bias.gui.FrontEnd;
import bias.gui.VisualEntryDescriptor;
import bias.gui.VisualEntryDescriptor.ENTRY_TYPE;
import bias.utils.CommonUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

public class SimpleSearch extends ToolExtension {

    private static final ImageIcon ICON = new ImageIcon(CommonUtils.getResourceURL(SimpleSearch.class, "icon.png"));
    
    private static final String PROP_SEARCH_EXPRESSION = "SEARCH_EXPRESSION";
    private static final String PROP_IS_CASE_SENSITIVE = "IS_CASE_SENSITIVE";
    private static final String PROP_IS_REGULAR_EXPRESSION = "IS_REGULAR_EXPRESSION";
    private static final String PROP_FILTER_TYPE = "FILTER_TYPE";

    private class IconViewPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int MAX_ICON_WIDTH = 32;
        private static final int MAX_ICON_HEIGHT = 32;
        private Image image;
        public IconViewPanel(Image image) {
            if (image != null) {
                this.image = image;
                int previewWidth;
                int previewHeight;
                int imWidth = image.getWidth(this);
                int imHeight = image.getHeight(this);
                if (imWidth > MAX_ICON_WIDTH || imHeight > MAX_ICON_HEIGHT) {
                    if (imWidth >= imHeight){
                        previewHeight = (int)(imHeight/((float)imWidth/MAX_ICON_WIDTH));
                        previewWidth = MAX_ICON_WIDTH;
                    } else {
                        previewWidth = (int)(imWidth/((float)imHeight/MAX_ICON_HEIGHT));
                        previewHeight = MAX_ICON_HEIGHT;
                    }
                } else {
                    previewWidth = imWidth;
                    previewHeight = imHeight;
                }
                this.image = image.getScaledInstance(previewWidth, previewHeight, Image.SCALE_FAST);
                this.setPreferredSize(new Dimension(previewWidth, previewHeight));
            }
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

    private SearchCriteria lastSearchCriteria = null;
    
    private Class<? extends EntryExtension> filterClass = null;
    
    private byte[] settings;
    
    public SimpleSearch(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        initSettings();
    }
    
    @SuppressWarnings("unchecked")
    private void initSettings() {
        if (getSettings() != null && !Arrays.equals(getSettings(), settings)) {
            settings = getSettings();
            Properties props = PropertiesUtils.deserializeProperties(settings);
            if (!props.isEmpty()) {
                String searchExpression = props.getProperty(PROP_SEARCH_EXPRESSION);
                boolean isCaseSensitive = Boolean.parseBoolean(props.getProperty((PROP_IS_CASE_SENSITIVE)));
                boolean isRegularExpression = Boolean.parseBoolean(props.getProperty((PROP_IS_REGULAR_EXPRESSION)));
                lastSearchCriteria = new SearchCriteria(searchExpression, isCaseSensitive, isRegularExpression);
                try {
                    filterClass = (Class<? extends EntryExtension>) Class.forName(props.getProperty((PROP_FILTER_TYPE)));
                } catch (Exception ex) {
                    // ignore, last searched type may not exist any longer
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        Properties props = new Properties();
        if (lastSearchCriteria != null) {
            if (!Validator.isNullOrBlank(lastSearchCriteria.getSearchExpression())) {
                props.setProperty(PROP_SEARCH_EXPRESSION, lastSearchCriteria.getSearchExpression());
            }
            if (!Validator.isNullOrBlank(lastSearchCriteria.isCaseSensitive())) {
                props.setProperty(PROP_IS_CASE_SENSITIVE, "" + lastSearchCriteria.isCaseSensitive());
            }
            if (!Validator.isNullOrBlank(lastSearchCriteria.isRegularExpression())) {
                props.setProperty(PROP_IS_REGULAR_EXPRESSION, "" + lastSearchCriteria.isRegularExpression());
            }
        }
        if (filterClass != null) {
            props.setProperty(PROP_FILTER_TYPE, filterClass.getName());
        }
        return PropertiesUtils.serializeProperties(props);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#getRepresentation()
     */
    @Override
    public ToolRepresentation getRepresentation() {
        JButton button = new JButton(ICON);
        button.addActionListener(searchAction);
        return new ToolRepresentation(button, null);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#bindHotkeys(javax.swing.InputMap, javax.swing.ActionMap)
     */
    @Override
    public void bindHotkeys(InputMap inputMap, ActionMap actionMap) {
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), searchAction.getValue(Action.NAME));
        actionMap.put(searchAction.getValue(Action.NAME), searchAction);
    }
    
    @Override
    public String getHelpInfo() {
        return getMessage("help.info");
    }
    
    private SearchAction searchAction = new SearchAction();
    private class SearchAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public SearchAction(){
            putValue(Action.NAME, "search");            
        }
        public void actionPerformed(ActionEvent e) {
            try {
                initSettings();
                final JLabel searchExpressionL = new JLabel("search expression:");
                final JTextField searchExpressionTF = new JTextField();
                final Color normal = searchExpressionTF.getForeground();
                final JLabel isCaseSensitiveL = new JLabel("case sensitive:");
                final JCheckBox isCaseSensitiveCB = new JCheckBox();
                final JPanel caseSensitivePanel = new JPanel(new BorderLayout());
                caseSensitivePanel.add(isCaseSensitiveL, BorderLayout.CENTER);
                caseSensitivePanel.add(isCaseSensitiveCB, BorderLayout.EAST);
                final JLabel isRegularExpressionL = new JLabel("regular expression:");
                final JCheckBox isRegularExpressionCB = new JCheckBox();
                isRegularExpressionCB.addChangeListener(new ChangeListener(){
                    public void stateChanged(ChangeEvent e) {
                        if (isRegularExpressionCB.isSelected()) {
                            isCaseSensitiveL.setEnabled(false);
                            isCaseSensitiveCB.setEnabled(false);
                        } else {
                            isCaseSensitiveL.setEnabled(true);
                            isCaseSensitiveCB.setEnabled(true);
                        }
                    }
                });
                final JPanel regularExpressionPanel = new JPanel(new BorderLayout());
                regularExpressionPanel.add(isRegularExpressionL, BorderLayout.CENTER);
                regularExpressionPanel.add(isRegularExpressionCB, BorderLayout.EAST);
                
                final Map<String, Class<? extends EntryExtension>> types = ExtensionFactory.getAnnotatedEntryExtensionClasses();
                
                final JLabel filterClassL = new JLabel("search for entries of this type only:");
                final JComboBox<String> filterClassCB = new JComboBox<>();
                filterClassCB.addItem("Any type");
                final JPanel filterClassPanel = new JPanel(new BorderLayout());
                filterClassPanel.add(filterClassL, BorderLayout.NORTH);
                filterClassPanel.add(filterClassCB, BorderLayout.CENTER);
                String selectItem = null;
                for (Entry<String, Class<? extends EntryExtension>> entry : types.entrySet()) {
                    filterClassCB.addItem(entry.getKey());
                    if (filterClass != null 
                            && entry.getValue().getName().equals(filterClass.getName())) {
                        selectItem = entry.getKey();
                    }
                }
                if (selectItem != null) {
                    filterClassCB.setSelectedItem(selectItem);
                }

                if (lastSearchCriteria != null) {
                    searchExpressionTF.setText(lastSearchCriteria.getSearchExpression());
                    isCaseSensitiveCB.setSelected(lastSearchCriteria.isCaseSensitive());
                    isRegularExpressionCB.setSelected(lastSearchCriteria.isRegularExpression());
                }
                
                searchExpressionTF.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        String searchExpression = searchExpressionTF.getText();
                        if (isRegularExpressionCB.isSelected() && !Validator.isNullOrBlank(searchExpression)) {
                            try {
                                Pattern.compile(searchExpression);
                                searchExpressionTF.setForeground(normal);
                                searchExpressionTF.setToolTipText(null);
                            } catch (PatternSyntaxException pse) {
                                String errorMsg = "Pattern syntax error";
                                int idx = pse.getIndex();
                                if (idx != -1) {
                                    if (idx > 0) {
                                        idx--;
                                    }
                                    errorMsg +=  " at the position " + (idx + 1) + " - char '" + searchExpression.charAt(idx) + "'";
                                } else {
                                    errorMsg += "!";
                                }
                                searchExpressionTF.setForeground(Color.RED);
                                searchExpressionTF.setToolTipText(errorMsg);
                            }
                        } else {
                            searchExpressionTF.setForeground(normal);
                            searchExpressionTF.setToolTipText(null);
                        }
                    }
                });
                ActionListener al = new ActionListener(){
                    public void actionPerformed(ActionEvent ae){
                        searchExpressionTF.requestFocusInWindow();
                        searchExpressionTF.selectAll();
                    }
                };
                Timer timer = new Timer(100, al);
                timer.setRepeats(false);
                timer.start();
                int option = JOptionPane.showConfirmDialog(
                        FrontEnd.getActiveWindow(), 
                        new Component[]{
                            searchExpressionL,
                            searchExpressionTF,
                            caseSensitivePanel,
                            regularExpressionPanel,
                            filterClassPanel
                        }, 
                        "Search", 
                        JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION && !Validator.isNullOrBlank(searchExpressionTF.getText())) {
                    Thread searchThread = new Thread(new Runnable(){
                        public void run() {
                            JLabel processLabel = new JLabel();
                            try {
                                JPanel entryPathItemsPanel = new JPanel();
                                entryPathItemsPanel.setLayout(new BorderLayout());
                                processLabel.setText(" Searching... ");
                                entryPathItemsPanel.add(processLabel, BorderLayout.CENTER);
                                JLabel title = new JLabel("Search results");
                                FrontEnd.displayBottomPanel(title, entryPathItemsPanel, new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            for (EntryExtension extension : FrontEnd.getEntryExtensions().values()) {
                                                try {
                                                    extension.clearSearchResultsHighlight();
                                                } catch (Throwable t) {
                                                    t.printStackTrace(System.err);
                                                }
                                            }
                                        } catch (Throwable t) {
                                            t.printStackTrace(System.err);
                                        }
                                    }
                                });

                                filterClass = types.get(filterClassCB.getSelectedItem());
                                
                                if (lastSearchCriteria == null) {
                                    lastSearchCriteria = new SearchCriteria();
                                }
                                lastSearchCriteria.setSearchExpression(searchExpressionTF.getText()); 
                                lastSearchCriteria.setCaseSensitive(isCaseSensitiveCB.isSelected()); 
                                lastSearchCriteria.setRegularExpression(isRegularExpressionCB.isSelected());                    
                                final SearchCriteria sc = new SearchCriteria(
                                        searchExpressionTF.getText(),
                                        isCaseSensitiveCB.isSelected(),
                                        isRegularExpressionCB.isSelected());
                                Map<VisualEntryDescriptor, Map<String, HighLightMarker>> result = search(sc, filterClass);
                                if (result.isEmpty()) {
                                    title.setText(" Search results :: no entries found");
                                    processLabel.setText(" No items matching search criteria ");
                                } else {
                                    title.setText("<html>&nbsp;Search results :: <strong>" + result.size() + "</strong> entr" + (result.size() > 1 ? "ies" : "y") + " found</html>");
                                    entryPathItemsPanel.setVisible(false);
                                    entryPathItemsPanel.removeAll();
                                    BoxLayout layout = new BoxLayout(entryPathItemsPanel, BoxLayout.Y_AXIS);
                                    entryPathItemsPanel.setLayout(layout);
                                    for (Entry<VisualEntryDescriptor, Map<String, HighLightMarker>> resultEntry : result.entrySet()) {
                                        final VisualEntryDescriptor ved = resultEntry.getKey();
                                        Collection<Recognizable> entryPath = ved.getEntryPath();
                                        JPanel entryPathItemPanel = new JPanel(new FlowLayout());
                                        Iterator<Recognizable> it = entryPath.iterator();
                                        while (it.hasNext()) {
                                            final Recognizable r = it.next();
                                            MouseListener ml = new MouseAdapter(){
                                                @Override
                                                public void mouseClicked(MouseEvent e) {
                                                    FrontEnd.switchToVisualEntry(r.getId());
                                                    try {
                                                        EntryExtension entry = FrontEnd.getEntryExtensions().get(r.getId());
                                                        if (entry != null) {
                                                            entry.highlightSearchResults(sc.getSearchExpression(), sc.isCaseSensitive(), sc.isRegularExpression());
                                                        }
                                                    } catch (Throwable t) {
                                                        t.printStackTrace(System.err);
                                                    }
                                                }
                                            };
                                            if (r.getIcon() != null) {
                                                JPanel entryPathItemIcon = new IconViewPanel(((ImageIcon) r.getIcon()).getImage());
                                                entryPathItemIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                                entryPathItemIcon.addMouseListener(ml);
                                                entryPathItemPanel.add(entryPathItemIcon);
                                            }
                                            JLabel entryPathItemLabel = new JLabel("<html><u><font color=blue>" + r.getCaption() + "</font></u></html>");
                                            entryPathItemLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                            entryPathItemLabel.addMouseListener(ml);
                                            entryPathItemPanel.add(entryPathItemLabel);
                                            if (it.hasNext()) {
                                                entryPathItemPanel.add(new JLabel("<html> &raquo; </html>"));
                                            }
                                        }
                                        StringBuffer matchedSnippets = new StringBuffer();
                                        Iterator<Entry<String, HighLightMarker>> it2 = resultEntry.getValue().entrySet().iterator();
                                        while (it2.hasNext()) {
                                            Entry<String, HighLightMarker> entry = it2.next();
                                            String str = entry.getKey();
                                            String hlStr = null;
                                            HighLightMarker hlMarker = entry.getValue();
                                            if (hlMarker != null) {
                                                Integer beginIndex = entry.getValue().getBeginIndex();
                                                Integer endIndex = entry.getValue().getEndIndex();
                                                if (beginIndex != null && endIndex != null) {
                                                    hlStr = str.substring(0, beginIndex);
                                                    hlStr += str.substring(beginIndex, endIndex);
                                                    hlStr += str.substring(endIndex, str.length());
                                                }
                                            }
                                            if (hlStr != null) {
                                                matchedSnippets.append(hlStr);
                                            } else {
                                                matchedSnippets.append(str);
                                            }
                                            if (it2.hasNext()) {
                                                matchedSnippets.append(" :: ");
                                            }
                                        }
                                        
                                        JTextField tf = new JTextField(matchedSnippets.toString());
                                        tf.setBorder(null);
                                        tf.setEditable(false);
                                        tf.setSelectedTextColor(Color.LIGHT_GRAY);
                                        
                                        Highlighter hl = tf.getHighlighter();
                                        hl.removeAllHighlights();
                                        
                                        String text = tf.getText();
                                        if (!Validator.isNullOrBlank(text)) {
                                            HighlightPainter hlPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                                            Pattern pattern = sc.isRegularExpression() ? Pattern.compile(sc.getSearchExpression()) : null;
                                            if (pattern != null) {
                                                Matcher matcher = pattern.matcher(text);
                                                while (matcher.find()) {
                                                    hl.addHighlight(matcher.start(), matcher.end(), hlPainter);
                                                }
                                            } else {
                                                int index = -1;
                                                do {
                                                    int fromIdx = index != -1 ? index + sc.getSearchExpression().length() : 0;
                                                    if (sc.isCaseSensitive()) {
                                                        index = text.indexOf(sc.getSearchExpression(), fromIdx);
                                                    } else {
                                                        index = text.toLowerCase().indexOf(sc.getSearchExpression().toLowerCase(), fromIdx);
                                                    }
                                                    if (index != -1) {
                                                        hl.addHighlight(index, index + sc.getSearchExpression().length(), hlPainter);
                                                    }
                                                } while (index != -1);
                                            }
                                        }

                                        entryPathItemPanel.add(new JLabel("<html>&rarr;</html>"));
                                        entryPathItemPanel.add(tf);
                                        
                                        JPanel p = new JPanel(new BorderLayout());
                                        p.add(entryPathItemPanel, BorderLayout.WEST);
                                        entryPathItemsPanel.add(p);
                                    }
                                    entryPathItemsPanel.setVisible(true);
                                }
                                System.gc();
                            } catch (Throwable t) {
                                processLabel.setText("<html><font color=red>Error while processing search!</font></html>");
                                t.printStackTrace(System.err);
                            }
                        }
                    });
                    searchThread.start();
                }
            } catch (Throwable t) {
                FrontEnd.displayErrorMessage("Failed to perform search!", t);
            }
        }
    };

    private Map<VisualEntryDescriptor, Map<String, HighLightMarker>> search(SearchCriteria sc, Class<? extends EntryExtension> filterClass) throws Throwable {
        Map<VisualEntryDescriptor, Map<String, HighLightMarker>> result = new LinkedHashMap<VisualEntryDescriptor, Map<String, HighLightMarker>>();
        Map<UUID, VisualEntryDescriptor> vedMap = FrontEnd.getVisualEntryDescriptors(filterClass);
        Map<UUID, EntryExtension> extensions = FrontEnd.getEntryExtensions(filterClass);
        Map<UUID, Collection<String>> entries = getSearchEntries(vedMap.values(), extensions);
        Map<UUID, Map<String, HighLightMarker>> matchesFound = SearchEngine.search(sc, entries);
        if (!matchesFound.isEmpty()) {
            for (Entry<UUID, Map<String, HighLightMarker>> matchesFoundEntry : matchesFound.entrySet()) {
                UUID id = matchesFoundEntry.getKey();
                VisualEntryDescriptor ved = vedMap.get(id);
                if (ved != null) {
                    result.put(ved, matchesFoundEntry.getValue());
                }
            }
        }
        return result;
    }
    
    private Map<UUID, Collection<String>> getSearchEntries(Collection<VisualEntryDescriptor> visualEntryDescriptors, Map<UUID, EntryExtension> extensions) throws Throwable {
        Map<UUID, Collection<String>> entries = new LinkedHashMap<UUID, Collection<String>>();
        for (VisualEntryDescriptor visualEntryDescriptor : visualEntryDescriptors) {
            if (visualEntryDescriptor.getEntryType() == ENTRY_TYPE.ENTRY) {
                EntryExtension entry = extensions.get(visualEntryDescriptor.getEntry().getId());
                Collection<String> searchStrings = null;
                String caption = visualEntryDescriptor.getEntry().getCaption();
                if (!Validator.isNullOrBlank(caption)) {
                    // add visual entry caption to entry search data,
                    // so it will be considered while searching
                    searchStrings = new LinkedList<String>();
                    searchStrings.add(caption);
                }
                Collection<String> entrySearchStrings = null;
                try {
                    entrySearchStrings = entry.getSearchData();
                } catch (Throwable t) {
                    System.err.println("Failed to get search data from entry " + entry.getClass() + "/" + entry.getId().toString());
                    t.printStackTrace(System.err);
                }
                if (entrySearchStrings != null) {
                    if (searchStrings != null) {
                        searchStrings.addAll(entrySearchStrings);
                    } else {
                        searchStrings = entrySearchStrings;
                    }
                }
                if (searchStrings != null) {
                    entries.put(entry.getId(), searchStrings);
                }
            } else if (visualEntryDescriptor.getEntryType() == ENTRY_TYPE.CATEGORY) {
                String caption = visualEntryDescriptor.getEntry().getCaption();
                if (!Validator.isNullOrBlank(caption)) {
                    // add visual entry caption to entry search data,
                    // so it will be considered while searching
                    Collection<String> searchStrings = new ArrayList<String>();
                    searchStrings.add(caption);
                    entries.put(visualEntryDescriptor.getEntry().getId(), searchStrings);
                }
            }
        }
        return entries;
    }

}
