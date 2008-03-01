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
    
    /**
     * Configures tool-extension.
     * Should be overridden to return settings for certain tool-extension.
     * By default returns null (no configuration).
     * 
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure() throws Throwable {
        return null;
    }

    /**
     * Returns tool representation.
     * Should be overridden by certain extending tool-class to define corresponding tool's representation.
     * By default returns null (no representation provided).
     * 
     * @return ToolRepresentation instance containing corresponding tool representation 
     */
    public ToolRepresentation getRepresentation() {
        return null;
    }

    /**
     * Defines whether extension's data should be skipped on export
     * By default returns false (data will be exported).
     */
    public boolean skipDataExport() {
        return false;
    }

    /**
     * Defines whether extension's configuration should be skipped on export
     * By default returns false (configuration will be exported).
     */
    public boolean skipConfigExport() {
        return false;
    }

}
