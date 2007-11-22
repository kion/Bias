/**
 * Created on Oct 23, 2006
 */
package bias.extension.HTMLPage;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import bias.annotation.AddOnAnnotation;
import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.EntryExtension;
import bias.extension.HTMLPage.editor.HTMLEditorPanel;

/**
 * @author kion
 */

@AddOnAnnotation(version = "0.4.5", author = "kion", description = "WYSIWYG HTML page editor")
public class HTMLPage extends EntryExtension {

    private static final long serialVersionUID = 1L;
    
    private HTMLEditorPanel editorPanel = null;

    /**
     * This is the default constructor
     */
    public HTMLPage(UUID id, byte[] data, byte[] settings) {
        super(id, data, settings);
        initialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.Extension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        String data  = getHTMLEditorPanel().getCode();
        Collection<String> usedAttachmentNames = getHTMLEditorPanel().getProcessedAttachmentNames();
        cleanUpUnUsedAttachments(usedAttachmentNames);
        return data.getBytes();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.Extension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        Collection<String> searchData = new ArrayList<String>();
        searchData.add(getHTMLEditorPanel().getText());
        return searchData;
    }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.add(getHTMLEditorPanel(), BorderLayout.CENTER); 
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
