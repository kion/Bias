/**
 * Created on Jan 5, 2007
 */
package bias.skin;


/**
 * @author kion
 */

public abstract class Skin {
    
    /**
     * Performs needed actions to activate certain Skin
     * 
     * @param settings settings to use while activating
     */
    public abstract void activate(byte[] settings) throws Throwable;
    
    /**
     * Configures Skin.
     * Should be overridden to return settings for certain Skin.
     * By default returns null (no configuration).
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure(byte[] settings) {
        return null;
    }
    
    /**
     * Defines icons of controls.
     * Should be overridden to return control icons for concrete Skin.
     * By default returns null (no custom icons).

     * @return ControlIcons structure
     */
    public UIIcons getUIIcons() {
        return null;
    }

}