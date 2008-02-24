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
     * Should be implemented to return settings for certain Look-&-Feel.
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public abstract byte[] configure(byte[] settings);
    
    /**
     * Defines icons of controls.
     * Should be implemented to return control icons for concrete Look-&-Feel.

     * @return ControlIcons structure
     */
    public abstract ControlIcons getControlIcons();

}
