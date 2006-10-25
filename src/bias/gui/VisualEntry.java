/**
 * Created on Oct 23, 2006
 */
package bias.gui;

import javax.swing.JPanel;

/**
 * @author kion
 */
public abstract class VisualEntry extends JPanel {
    
    private byte[] data;
    
    public VisualEntry() {
        // default constructor
    }
    
    public VisualEntry(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    abstract public String serialize();

}
