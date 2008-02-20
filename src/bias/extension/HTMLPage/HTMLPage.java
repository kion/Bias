/**
 * Created on Oct 23, 2006
 */
package bias.extension.HTMLPage;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import bias.annotation.AddOnAnnotation;
import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.EntryExtension;
import bias.extension.HTMLPage.editor.HTMLEditorPanel;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

@AddOnAnnotation(
        version = "0.5.7", 
        author = "R. Kasianenko", 
        description = "WYSIWYG HTML page editor",
        details = "<i>HTMLPage</i> extension for Bias is a part<br>" +
                  "of standard \"all-inclusive-delivery-set\" of Bias application.<br>" +
                  "It is provided by <a href=\"http://kion.name/\">R. Kasianenko</a>,<br>" +
                  "an author of Bias application.")
public class HTMLPage extends EntryExtension {
    private static final long serialVersionUID = 1L;
    
    private static final String PROPERTY_SCROLLBAR_VERT = "SCROLLBAR_VERT";
    
    private static final String PROPERTY_SCROLLBAR_HORIZ = "SCROLLBAR_HORIZ";
    
    private static final String PROPERTY_CARET_POSITION = "CARET_POSITION";

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
     * @see bias.extension.EntryExtension#serializeSettings()
     */
    @Override
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
        Properties props = PropertiesUtils.deserializeProperties(getSettings());
        this.setLayout(new BorderLayout());
        this.add(getHTMLEditorPanel(), BorderLayout.CENTER);
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
        String caretPos = props.getProperty(PROPERTY_CARET_POSITION);
        if (!Validator.isNullOrBlank(caretPos)) {
            getHTMLEditorPanel().setCaretPosition(Integer.valueOf(caretPos));
        }
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
