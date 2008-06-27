/**
 * Created on Nov 5, 2006
 */
package bias.core;

import java.util.UUID;

import javax.swing.Icon;

/**
 * @author kion
 */
public class Recognizable extends Identifiable {
    
    protected String caption;
    
    protected Icon icon;
    
    public Recognizable() {
        super();
    }
    
    public Recognizable(UUID id, String caption, Icon icon) {
        super(id);
        this.caption = caption;
        this.icon = icon;
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
