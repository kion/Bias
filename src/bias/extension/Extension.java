/**
 * Created on Jul 16, 2007
 */
package bias.extension;

/**
 * @author kion
 */
public interface Extension {

    /**
     * Configures extension.
     * By default returns null (no settings).
     * Should be overridden to return settings for certain extension.
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure(byte[] settings) throws Throwable;

    /**
     * Serializes extension's settings to array of bytes
     * By default returns null (no settings).
     * Should be overridden to return settings for certain extension's instance.
     * 
     * @return array of bytes representing serialized settings of extension's instance
     */
    public byte[] serializeSettings() throws Throwable;

    /**
     * Serializes extension's data to array of bytes.
     * By default returns null (no data).
     * Should be overridden to return data for certain extension's instance.
     * 
     * @return array of bytes representing serialized data of extension's instance
     */
    public byte[] serializeData() throws Throwable;

}
