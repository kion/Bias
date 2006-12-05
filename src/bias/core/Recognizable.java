/**
 * Created on Nov 5, 2006
 */
package bias.core;

import java.util.UUID;

import javax.swing.Icon;

/**
 * @author kion
 */
public abstract class Recognizable {
    
    protected UUID id;
    
    protected String caption;
    
    protected Icon icon;
    
    public Recognizable() {
        // default constructor
    }
    
    public Recognizable(UUID id, String caption, Icon icon) {
        this.id = id;
        this.caption = caption;
        this.icon = icon;
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

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

}
