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
     * Should be implemented to return settings for certain tool-extension.
     * 
     * @return settings byte array containing serialized configuration settings
     */
    public abstract byte[] configure() throws Throwable;

    /**
     * Returns tool representation.
     * Should be implemented by certain extending tool-class to define corresponding tool's representation.
     * 
     * @return ToolRepresentation instance containing corresponding tool representation 
     */
    public abstract ToolRepresentation getRepresentation();

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
