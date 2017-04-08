/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard.snippet;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import bias.extension.DashBoard.editor.HTMLEditorPanel;
import bias.utils.Validator;

/**
 * @author kion
 */
public class HTMLSnippet extends InfoSnippet {
    private static final long serialVersionUID = 1L;

    private HTMLEditorPanel editorPanel;

    public HTMLSnippet(UUID dataEntryID, UUID id, byte[] content, byte[] settings) {
        super(dataEntryID, id, content, settings, true, true);
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.frame.InternalFrame#getRepresentation()
     */
    @Override
    protected Container getRepresentation() {
        return getEditorPanel();
    }

    public HTMLEditorPanel getEditorPanel() {
        if (editorPanel == null) {
            editorPanel = new HTMLEditorPanel(getDataEntryID(), new String(getContent()));
        }
        return editorPanel;
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getReferencedAttachmentNames()
     */
    @Override
    public Collection<String> getReferencedAttachmentNames() {
    	return getEditorPanel().getProcessedAttachmentNames();
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.frame.InternalFrame#serializeContent()
     */
    @Override
    public byte[] serializeContent() {
        return getEditorPanel().getCode().getBytes();
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() {
        Collection<String> searchData = new ArrayList<String>();
        searchData.add(getEditorPanel().getText());
        return searchData;
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#highlightSearchResults(java.lang.String, boolean, boolean)
     */
    @Override
    public void highlightSearchResults(String searchExpression, boolean isCaseSensitive, boolean isRegularExpression) throws Throwable {
        Highlighter hl = getEditorPanel().getHighlighter();
        hl.removeAllHighlights();
        String text = getEditorPanel().getText();
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
        Highlighter hl = getEditorPanel().getHighlighter();
        hl.removeAllHighlights();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(290, 120);
    }

}
