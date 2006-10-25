/**
 * Created on Oct 23, 2006
 */
package bias.gui;

import javax.swing.JPanel;

/**
 * @author kion
 */
public abstract class VisualEntry extends JPanel {
    
    private String data;
    
    public VisualEntry() {
        // default constructor
    }
    
    public VisualEntry(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    abstract public String serialize();

}
