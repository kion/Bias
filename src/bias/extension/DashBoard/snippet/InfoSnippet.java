/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard.snippet;

import java.awt.Container;
import java.util.UUID;

import javax.swing.JInternalFrame;
import javax.swing.border.EtchedBorder;

/**
 * @author kion
 */
public abstract class InfoSnippet extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private byte[] content;
    
    public InfoSnippet(UUID id, byte[] content) {
        super(null, true, true, false, false);
        this.id = id;
        this.content = content;
        setContentPane(getRepresentation());
        setBorder(new EtchedBorder(EtchedBorder.RAISED));
    }

    protected UUID getId() {
        return id;
    }

    protected byte[] getContent() {
        return content;
    }
    
    protected abstract Container getRepresentation();
    
    public abstract byte[] serializeContent();
    
    public abstract void cleanUpUnUsedAttachments();
    
}
