/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard.snippet;

import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import bias.extension.DashBoard.editor.HTMLEditorPanel;

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
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(290, 120);
    }

}
