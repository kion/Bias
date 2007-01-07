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
     * By default returns null (no settings).
     * Should be overriden to return settings for certain Look-&-Feel.
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure(byte[] settings) {
        return null;
    }

}
