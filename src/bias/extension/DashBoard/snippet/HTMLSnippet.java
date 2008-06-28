/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard.snippet;

import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.DashBoard.editor.HTMLEditorPanel;

/**
 * @author kion
 */
public class HTMLSnippet extends InfoSnippet {
    private static final long serialVersionUID = 1L;

    private HTMLEditorPanel editorPanel;

    public HTMLSnippet(UUID id, byte[] content, byte[] settings) {
        super(id, content, settings, true, true);
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
            editorPanel = new HTMLEditorPanel(getId(), new String(getContent()));
        }
        return editorPanel;
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.frame.InternalFrame#cleanUpUnUsedAttachments()
     */
    @Override
    public void cleanUpUnUsedAttachments() {
        try {
            Collection<String> usedAttachmentNames = getEditorPanel().getProcessedAttachmentNames();
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

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.frame.InternalFrame#serializeContent()
     */
    @Override
    public byte[] serializeContent() {
        String data  = getEditorPanel().getCode();
        Collection<String> usedAttachmentNames = getEditorPanel().getProcessedAttachmentNames();
        cleanUpUnUsedAttachments(usedAttachmentNames);
        return data.getBytes();
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
