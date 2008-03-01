/**
 * Created on Jan 5, 2007
 */
package bias.laf;


/**
 * @author kion
 */

public abstract class LookAndFeel {
    
    /**
     * Performs needed actions to activate certain Look-&-Feel
     * 
     * @param settings settings to use while activating
     */
    public abstract void activate(byte[] settings) throws Throwable;
    
    /**
     * Configures Look-&-Feel.
     * Should be overridden to return settings for certain Look-&-Feel.
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
     * Should be overridden to return control icons for concrete Look-&-Feel.
     * By default returns null (no custom icons).

     * @return ControlIcons structure
     */
    public UIIcons getUIIcons() {
        return null;
    }

}
