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
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bias.annotation.AddOnAnnotation;
import bias.core.Recognizable;
import bias.extension.EntryExtension;
import bias.extension.ExtensionFactory;
import bias.extension.ToolExtension;
import bias.extension.SimpleSearch.SearchEngine.HighLightMarker;
import bias.gui.FrontEnd;
import bias.gui.VisualEntryDescriptor;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.3.1",
        author="kion",
        description = "Simple search tool")
public class SimpleSearch extends ToolExtension {

    private static final ImageIcon ICON = new ImageIcon(SimpleSearch.class.getResource("/bias/res/SimpleSearch/icon.png"));
    
    private static final String PROP_SEARCH_EXPRESSION = "SEARCH_EXPRESSION";
    private static final String PROP_IS_CASE_SENSITIVE = "IS_CASE_SENSITIVE";
    private static final String PROP_IS_REGULAR_EXPRESSION = "IS_REGULAR_EXPRESSION";
    private static final String PROP_FILTER_TYPE = "FILTER_TYPE";

    private static class IconViewPanel extends JPanel {
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
    
    @SuppressWarnings("unchecked")
    public SimpleSearch(byte[] data, byte[] settings) {
        super(data, settings);
        Properties props = PropertiesUtils.deserializeProperties(getSettings());
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

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        Properties props = new Properties();
        if (lastSearchCriteria != null) {
            props.setProperty(PROP_SEARCH_EXPRESSION, lastSearchCriteria.getSearchExpression());
            props.setProperty(PROP_IS_CASE_SENSITIVE, "" + lastSearchCriteria.isCaseSensitive());
            props.setProperty(PROP_IS_REGULAR_EXPRESSION, "" + lastSearchCriteria.isRegularExpression());
        }
        if (filterClass != null) {
            props.setProperty(PROP_FILTER_TYPE, filterClass.getName());
        }
        return PropertiesUtils.serializeProperties(props);
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#action()
     */
    @Override
    public void action() throws Throwable {
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
        
        final Map<String, Class<? extends EntryExtension>> types = ExtensionFactory.getInstance().getAnnotatedEntryExtensions();
        
        final JLabel filterClassL = new JLabel("search for entries of this type only:");
        final JComboBox filterClassCB = new JComboBox();
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
        Thread searchThread = new Thread(new Runnable(){
            public void run() {
                JLabel processLabel = new JLabel();
                try {
                    int option = JOptionPane.showConfirmDialog(
                            null, 
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
                        
                        JPanel entryPathItemsPanel = new JPanel();
                        entryPathItemsPanel.setLayout(new BorderLayout());
                        processLabel.setText("searching...");
                        entryPathItemsPanel.add(processLabel, BorderLayout.CENTER);
                        JLabel title = new JLabel("Search results");
                        FrontEnd.displayBottomPanel(title, entryPathItemsPanel);

                        filterClass = types.get(filterClassCB.getSelectedItem());
                        
                        if (lastSearchCriteria == null) {
                            lastSearchCriteria = new SearchCriteria();
                        }
                        lastSearchCriteria.setSearchExpression(searchExpressionTF.getText()); 
                        lastSearchCriteria.setCaseSensitive(isCaseSensitiveCB.isSelected()); 
                        lastSearchCriteria.setRegularExpression(isRegularExpressionCB.isSelected());                    
                        SearchCriteria sc = new SearchCriteria(
                                searchExpressionTF.getText(),
                                isCaseSensitiveCB.isSelected(),
                                isRegularExpressionCB.isSelected());
                        Map<VisualEntryDescriptor, Map<String, HighLightMarker>> result = search(sc, filterClass);
                        if (result.isEmpty()) {
                            title.setText("Search results :: 0 entries found");
                            processLabel.setText("No items matching search criteria.");
                        } else {
                            title.setText("Search results :: " + result.size() + " entries found");
                            entryPathItemsPanel.setVisible(false);
                            entryPathItemsPanel.removeAll();
                            entryPathItemsPanel.setLayout(new GridLayout(result.size(), 1));
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
                                        }
                                    };
                                    JPanel entryPathItemIcon = new IconViewPanel(((ImageIcon) r.getIcon()).getImage());
                                    entryPathItemIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                    entryPathItemIcon.addMouseListener(ml);
                                    entryPathItemPanel.add(entryPathItemIcon);
                                    JLabel entryPathItemLabel = new JLabel("<html><u><font color=blue>" + r.getCaption() + "</font></u></html>");
                                    entryPathItemLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                    entryPathItemLabel.addMouseListener(ml);
                                    entryPathItemPanel.add(entryPathItemLabel);
                                    if (it.hasNext()) {
                                        entryPathItemPanel.add(new JLabel(">"));
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
                                            hlStr += "<font bgcolor=yellow>";
                                            hlStr += str.substring(beginIndex, endIndex);
                                            hlStr += "</font>";
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
                                entryPathItemPanel.add(new JLabel("<html> &gt;&gt; <i>" + matchedSnippets.toString() + "</i></html>"));
                                JPanel p = new JPanel(new BorderLayout());
                                p.add(entryPathItemPanel, BorderLayout.WEST);
                                entryPathItemsPanel.add(p);
                            }
                            entryPathItemsPanel.setVisible(true);
                        }
                    }
                } catch (Throwable t) {
                    processLabel.setText("<html><font color=red>Error while processing search!</font></html>");
                    t.printStackTrace();
                }
            }
        });
        searchThread.start();
    }

    private Map<VisualEntryDescriptor, Map<String, HighLightMarker>> search(SearchCriteria sc, Class<? extends EntryExtension> filterClass) throws Throwable {
        Map<VisualEntryDescriptor, Map<String, HighLightMarker>> result = new LinkedHashMap<VisualEntryDescriptor, Map<String, HighLightMarker>>();
        Map<UUID, Collection<String>> entries = getSearchEntries(FrontEnd.getVisualEntries(filterClass));
        Map<UUID, Map<String, HighLightMarker>> matchesFound = SearchEngine.search(sc, entries);
        if (!matchesFound.isEmpty()) {
            Map<UUID, VisualEntryDescriptor> veMap = FrontEnd.getVisualEntriesMap();
            for (Entry<UUID, Map<String, HighLightMarker>> matchesFoundEntry : matchesFound.entrySet()) {
                UUID id = matchesFoundEntry.getKey();
                VisualEntryDescriptor ved = veMap.get(id);
                if (ved != null) {
                    result.put(ved, matchesFoundEntry.getValue());
                }
            }
        }
        return result;
    }
    
    private Map<UUID, Collection<String>> getSearchEntries(Map<VisualEntryDescriptor, JComponent> visualEntries) throws Throwable {
        Map<UUID, Collection<String>> entries = new LinkedHashMap<UUID, Collection<String>>();
        for (Entry<VisualEntryDescriptor, JComponent> visualEntry : visualEntries.entrySet()) {
            if (visualEntry.getValue() instanceof EntryExtension) {
                EntryExtension entry = (EntryExtension) visualEntry.getValue();
                Collection<String> searchStrings = null;
                String caption = visualEntry.getKey().getEntry().getCaption();
                if (!Validator.isNullOrBlank(caption)) {
                    // add visual entry caption to entry search data,
                    // so it will be considered while searching
                    searchStrings = new LinkedList<String>();
                    searchStrings.add(caption);
                }
                Collection<String> entrySearchStrings = entry.getSearchData();
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
            } else if (visualEntry.getValue() instanceof JTabbedPane) {
                String caption = visualEntry.getKey().getEntry().getCaption();
                if (!Validator.isNullOrBlank(caption)) {
                    // add visual entry caption to entry search data,
                    // so it will be considered while searching
                    Collection<String> searchStrings = new ArrayList<String>();
                    searchStrings.add(caption);
                    entries.put(UUID.fromString(((JTabbedPane) visualEntry.getValue()).getName()), searchStrings);
                }
            }
        }
        return entries;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#getIcon()
     */
    @Override
    public Icon getIcon() {
        return ICON;
    }

}
