/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard;

import java.util.UUID;

import javax.swing.JInternalFrame;
import javax.swing.border.EtchedBorder;

import bias.extension.DashBoard.editor.HTMLEditorPanel;

/**
 * @author kion
 */
public class InternalFrame extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String content;
    private HTMLEditorPanel editorPanel;
    
    public InternalFrame(UUID id, String content) {
        super(null, true, true, false, false);
        this.id = id;
        this.content = content;
        setContentPane(getEditorPanel());
        setBorder(new EtchedBorder(EtchedBorder.RAISED));
    }

    public HTMLEditorPanel getEditorPanel() {
        if (editorPanel == null) {
            editorPanel = new HTMLEditorPanel(id, content);
        }
        return editorPanel;
    }

}
