/**
 * Created on Jul 16, 2007
 */
package bias.extension;




/**
 * @author kion
 */

public abstract class ToolExtension implements Extension {

    private byte[] data;
    
    private byte[] settings;
    
    @SuppressWarnings("unused")
    private ToolExtension() {
        // default constructor without parameters is not visible
    }

    /**
     * The only allowed constructor that is aware of initialization data and settings.
     * 
     * @param id id to be assigned to extension instance
     * @param data data to be encapsulated by extension instance
     * @param settings extension instance settings
     */
    public ToolExtension(byte[] data, byte[] settings) {
        this.data = data;
        this.settings = settings;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the settings
     */
    public byte[] getSettings() {
        return settings;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#configure(byte[])
     */
    public byte[] configure(byte[] settings) throws Throwable {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        return null;
    }
    
    /**
     * @return tool representation
     */
    public abstract ToolRepresentation getRepresentation();

}
