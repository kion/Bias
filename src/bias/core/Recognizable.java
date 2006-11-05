/**
 * Created on Nov 5, 2006
 */
package bias.core;

import java.util.UUID;

/**
 * @author kion
 */
public abstract class Recognizable {
    
    protected UUID id;
    
    protected String caption;
    
    public Recognizable() {
        // default constructor
    }
    
    public Recognizable(UUID id, String caption) {
        this.id = id;
        this.caption = caption;
    }
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

}
