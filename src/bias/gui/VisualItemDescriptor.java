/**
 * Created on Nov 1, 2006
 */
package bias.gui;

import java.util.UUID;

/**
 * @author kion
 */
public class VisualItemDescriptor {
    
    private UUID id;
    
    private String[] captionsPath;
    
    public VisualItemDescriptor() {
        // default constructor
    }
    
    public VisualItemDescriptor(UUID id, String[] captionsPath) {
        this.id = id;
        this.captionsPath = captionsPath;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String[] getCaptionsPath() {
        return captionsPath;
    }

    public void setCaptionsPath(String[] captionsPath) {
        this.captionsPath = captionsPath;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        int i = 1;
        for (String captionPathElement : captionsPath) {
            result.append(captionPathElement);
            if (i++ < captionsPath.length) {
                result.append(" > ");
            }
        }
        return result.toString();
    }

}
