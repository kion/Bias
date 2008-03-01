/**
 * Created on Jul 16, 2007
 */
package bias.extension;

/**
 * @author kion
 */
public interface Extension {

    /**
     * Serializes extension's settings to array of bytes
     * Should be implemented to return settings for certain extension's instance.
     * 
     * @return array of bytes representing serialized settings of extension's instance
     */
    public byte[] serializeSettings() throws Throwable;

    /**
     * Serializes extension's data to array of bytes.
     * Should be implemented to return data for certain extension's instance.
     * 
     * @return array of bytes representing serialized data of extension's instance
     */
    public byte[] serializeData() throws Throwable;

}
