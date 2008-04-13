/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard.frame;

import java.awt.Container;
import java.util.UUID;

import javax.swing.JInternalFrame;
import javax.swing.border.EtchedBorder;

/**
 * @author kion
 */
public abstract class InternalFrame extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String content;
    
    public InternalFrame(UUID id, String content) {
        super(null, true, true, false, false);
        this.id = id;
        this.content = content;
        setContentPane(getRepresentation());
        setBorder(new EtchedBorder(EtchedBorder.RAISED));
    }

    protected UUID getId() {
        return id;
    }

    protected String getContent() {
        return content;
    }
    
    protected abstract Container getRepresentation();
    
    public abstract String serializeContent();
    
    public abstract void cleanUpUnUsedAttachments();
    
}