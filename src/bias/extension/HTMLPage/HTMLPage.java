/**
 * Created on Oct 23, 2006
 */
package bias.extension.HTMLPage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.EntryExtension;
import bias.gui.editor.HTMLEditorPanel;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

public class HTMLPage extends EntryExtension {
    private static final long serialVersionUID = 1L;
    
    private static final String PROPERTY_SCROLLBAR_VERT = "SCROLLBAR_VERT";
    
    private static final String PROPERTY_SCROLLBAR_HORIZ = "SCROLLBAR_HORIZ";
    
    private static final String PROPERTY_CARET_POSITION = "CARET_POSITION";

    private HTMLEditorPanel editorPanel = null;

    /**
     * This is the default constructor
     */
    public HTMLPage(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        initialize();
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        String data  = getHTMLEditorPanel().getCode();
        Collection<String> usedAttachmentNames = getHTMLEditorPanel().getProcessedAttachmentNames();
        cleanUpUnUsedAttachments(usedAttachmentNames);
        return data.getBytes();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        Properties props = new Properties();
        JScrollPane sc = ((JScrollPane) getHTMLEditorPanel().getComponent(0));
        JScrollBar sb = sc.getVerticalScrollBar();
        if (sb != null && sb.getValue() != 0) {
            props.setProperty(PROPERTY_SCROLLBAR_VERT, "" + sb.getValue());
        } else {
            props.remove(PROPERTY_SCROLLBAR_VERT);
        }
        sb = sc.getHorizontalScrollBar();
        if (sb != null && sb.getValue() != 0) {
            props.setProperty(PROPERTY_SCROLLBAR_HORIZ, "" + sb.getValue());
        } else {
            props.remove(PROPERTY_SCROLLBAR_HORIZ);
        }
        int cp = getHTMLEditorPanel().getCaretPosition();
        props.setProperty(PROPERTY_CARET_POSITION, "" + cp);
        return PropertiesUtils.serializeProperties(props);
    }

    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        Collection<String> searchData = new ArrayList<String>();
        searchData.add(getHTMLEditorPanel().getText());
        return searchData;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#highlightSearchResults(java.lang.String, boolean, boolean)
     */
    @Override
    public void highlightSearchResults(String searchExpression, boolean isCaseSensitive, boolean isRegularExpression) throws Throwable {
        Highlighter hl = getHTMLEditorPanel().getHighlighter();
        hl.removeAllHighlights();
        String text = getHTMLEditorPanel().getText();
        if (!Validator.isNullOrBlank(text)) {
            HighlightPainter hlPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
            boolean first = true;
            Pattern pattern = isRegularExpression ? Pattern.compile(searchExpression) : null;
            if (pattern != null) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    hl.addHighlight(matcher.start(), matcher.end(), hlPainter);
                    if (first) {
                        getHTMLEditorPanel().scrollToTextPos(matcher.start());
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
                            getHTMLEditorPanel().scrollToTextPos(index);
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
        getHTMLEditorPanel().getHighlighter().removeAllHighlights();
    }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        Properties props = PropertiesUtils.deserializeProperties(getSettings());
        this.setLayout(new BorderLayout());
        this.add(getHTMLEditorPanel(), BorderLayout.CENTER);
        String caretPos = props.getProperty(PROPERTY_CARET_POSITION);
        if (!Validator.isNullOrBlank(caretPos)) {
            getHTMLEditorPanel().setCaretPosition(Integer.valueOf(caretPos));
        }
        SwingUtilities.invokeLater(() -> {
            JScrollPane sc = ((JScrollPane) getHTMLEditorPanel().getComponent(0));
            JScrollBar sb = sc.getVerticalScrollBar();
            if (sb != null) {
                String val = props.getProperty(PROPERTY_SCROLLBAR_VERT);
                if (val != null) {
                    sb.setVisibleAmount(0);
                    sb.setValue(sb.getMaximum());
                    sb.setValue(Integer.valueOf(val));
                }
            }
            sb = sc.getHorizontalScrollBar();
            if (sb != null) {
                String val = props.getProperty(PROPERTY_SCROLLBAR_HORIZ);
                if (val != null) {
                    sb.setVisibleAmount(0);
                    sb.setValue(sb.getMaximum());
                    sb.setValue(Integer.valueOf(val));
                }
            }
        });
    }

    private HTMLEditorPanel getHTMLEditorPanel() {
        if (editorPanel == null) {
            editorPanel = new HTMLEditorPanel(getId(), new String(getData()));
        }
        return editorPanel;
    }

    private void cleanUpUnUsedAttachments(Collection<String> usedAttachmentNames) {
        try {
            Collection<Attachment> atts = BackEnd.getInstance().getAttachments(getId());
            for (Attachment att : atts) {
                if (!usedAttachmentNames.contains(att.getName())) {
                    BackEnd.getInstance().removeAttachment(getId(), att.getName());
                }
            }
        } catch (Exception ex) {
            // if some error occurred while cleaning up unused attachments,
            // ignore it, these attachments will be removed next time Bias persists data
        }
    }

}
