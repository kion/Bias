/**
 * Created on Nov 5, 2006
 */
package bias.core;

import java.util.UUID;

import javax.swing.Icon;

/**
 * @author kion
 */
public class Recognizable {
    
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Recognizable)) {
            return false;
        } else {
            Recognizable r = (Recognizable) obj;
            if (r.getId() == null) {
                return false;
            } else {
                return r.getId().equals(getId()) 
                        && (r.getIcon() == null ? getIcon() == null : r.getIcon().equals(getIcon())) 
                        && (r.getCaption() == null ? getCaption() == null : r.getCaption().equals(getCaption()));
            }
        }
    }

}
