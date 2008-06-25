/**
 * Created on Jul 16, 2007
 */
package bias.extension;

import bias.i18n.I18nService;




/**
 * @author kion
 */

public abstract class ToolExtension implements Extension {

    /**
     * Internationalization support
     */
    protected String getMessage(String key) {
        return I18nService.getInstance().getMessages(getClass()).get(key);
    }
    protected String getMessage(String key, String...vars) {
        String modifiedMsg = getMessage(key);
        for (String var : vars) {
            modifiedMsg = modifiedMsg.replaceFirst("\\$", var);
        }
        return modifiedMsg;
    }
    
    private byte[] data;
    
    private byte[] settings;
    
    @SuppressWarnings("unused")
    private ToolExtension() {
        // default constructor without parameters is not visible
    }

    /**
     * The only allowed constructor that is aware of initialization data and settings.
     * 
     * @param data data to be encapsulated by extension instance
     * @param settings extension instance settings
     */
    public ToolExtension(byte[] data, byte[] settings) throws Throwable {
        this.data = data;
        this.settings = settings;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getSettings() {
        return settings;
    }

    public void setSettings(byte[] settings) {
        this.settings = settings;
    }

    /**
     * Serializes tool-extension's settings to array of bytes
     * Should be overridden to return settings for certain tool-extension's instance.
     * By default returns null (no settings).
     * 
     * @return array of bytes representing serialized settings of tool-extension's instance
     */
    public byte[] serializeSettings() throws Throwable {
        return null;
    }

    /**
     * Serializes tool-extension's data to array of bytes.
     * Should be overridden to return data for certain tool-extension's instance.
     * By default returns null (no data).
     * 
     * @return array of bytes representing serialized data of tool-extension's instance
     */
    public byte[] serializeData() throws Throwable {
        return null;
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
