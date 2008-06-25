/**
 * Created on Jun 25, 2008
 */
package bias.extension.DashBoard.snippet;

import java.awt.Container;
import java.util.UUID;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import bias.extension.DashBoard.DashBoard;

/**
 * @author kion
 */
public class BrokenSnippet extends InfoSnippet {
    private static final long serialVersionUID = 1L;

    public BrokenSnippet(UUID id, byte[] content, byte[] settings) {
        super(id, content, settings, false, false);
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#getRepresentation()
     */
    @Override
    protected Container getRepresentation() {
        return getJTextPane();
    }

    private JTextPane getJTextPane() {
        JTextPane jTextPane = new JTextPane();
        jTextPane.setEditable(false);
        jTextPane.setEditorKit(new HTMLEditorKit());
        jTextPane.setText(buildInitFailureMessage());
        return jTextPane;
    }

    private String buildInitFailureMessage() {
        return
        "<html><body>" + 
        "<font size=\"3\" face=\"SansSerif\">" +
        "<b><font color=\"#FF0000\">BROKEN SNIPPET!</font></b><br><br>" + 
        "<b>To display this snippet you should (re)install/update <b><i><font color=\"#0000FF\">" + DashBoard.class.getSimpleName() + "</font></i></b> extension!</b>" + 
        "</font>" +
        "</body></html>"; 
    }

    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#serializeContent()
     */
    @Override
    public byte[] serializeContent() {
        return getContent();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.DashBoard.snippet.InfoSnippet#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() {
        return getSettings();
    }
    
}
