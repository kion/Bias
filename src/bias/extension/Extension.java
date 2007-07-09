/**
 * Created on Oct 23, 2006
 */
package bias.extension;

import java.util.Collection;
import java.util.UUID;

import javax.swing.JPanel;

/**
 * @author kion
 */

public abstract class Extension extends JPanel {

    private UUID id;
    
    private byte[] data;
    
    private byte[] settings;
    
    @SuppressWarnings("unused")
    private Extension() {
        // default constructor without parameters is not visible
    }

    /**
     * The only allowed constructor that is aware of initialization data and settings.
     * 
     * @param id id to be assigned to extension instance
     * @param data data to be encapsulated by extension instance
     * @param settings extension instance settings
     */
    public Extension(UUID id, byte[] data, byte[] settings) {
        if (id == null) {
        	id = UUID.randomUUID();
        }
    	this.id = id;
        this.data = data;
        this.settings = settings;
    }

	/**
	 * @return extension instance unique identifier
	 */
	public UUID getId() {
		return id;
	}

    /**
     * @return data to be used for extension instance representation
     */
    protected byte[] getData() {
        return data;
    }

    /**
     * @return settings to be used for extension instance representation
     */
    protected byte[] getSettings() {
        return settings;
    }

    /**
     * Configures extension.
     * By default returns null (no settings).
     * Should be overriden to return settings for certain extension.
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure(byte[] settings) throws Throwable {
        return null;
    }

    /**
     * Serializes extension's settings to array of bytes
     * By default returns null (no settings).
     * Should be overriden to return settings for certain extension's instance.
     * 
     * @return array of bytes representing serialized settings of extension's instance
     */
    public byte[] serializeSettings() throws Throwable {
        return null;
    }

    /**
     * Serializes extension's data to array of bytes.
     * By default returns null (no data).
     * Should be overriden to return data for certain extension's instance.
     * 
     * @return array of bytes representing serialized data of extension's instance
     */
    public byte[] serializeData() throws Throwable {
        return null;
    }

    /**
     * Returns extension's search data.
     * By default returns null (no data for search provided).
     * Should be overriden to return search data for certain extension's instance.
     * 
     * @return data for search provided by extension's instance
     */
    public Collection<String> getSearchData() throws Throwable {
        return null;
    }

}
