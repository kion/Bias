/**
 * Created on Oct 23, 2006
 */
package bias.gui;

import java.util.UUID;

import javax.swing.JPanel;

/**
 * @author kion
 */
public abstract class VisualEntry extends JPanel {
    
    private UUID id;
    
    private byte[] data;
    
    private VisualEntry() {
        // default constructor without parameters is not visible
    }
    
    /**
     * The only allowed constructor that is aware of initialization data.
     * @param data data to be incapsulated by visual entry
     */
    public VisualEntry(UUID id, byte[] data) {
        if (id == null) {
        	id = UUID.randomUUID();
        }
    	this.id = id;
        this.data = data;
    }

	/**
	 * Concrete visual entry instance's unique identifier getter.
	 * @return
	 */
	public UUID getId() {
		return id;
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
