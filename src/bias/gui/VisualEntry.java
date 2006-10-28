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
    
    private VisualEntry() {
        // default constructor without parameters should not be visible
    }
    
    /**
     * The only allowed constructor that is aware of initialization data.
     * @param data data to be incapsulated by visual entry
     */
    public VisualEntry(byte[] data) {
        this.data = data;
    }

    /**
     * Data getter visible for extending classes only.
     * @return data to be used for visual entry representation
     */
    protected byte[] getData() {
        return data;
    }

    /**
     * Serializes visual entry's data to array of bytes.
     * @return array of bytes representing serialized data
     */
    abstract public byte[] serialize() throws Exception;

}
