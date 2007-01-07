/**
 * Created on Jan 5, 2007
 */
package bias.laf;

import java.util.Properties;


/**
 * @author kion
 */

public abstract class LookAndFeelManager {
    
    /**
     * Performs needed actions to activate certain Look-&-Feel
     */
    public abstract void activate(Properties properties) throws Throwable;
    
    /**
     * Configures Look-&-Feel.
     * By default returns null (no settings).
     * Should be overriden to return settings for certain Look-&-Feel.
     * 
     * @param properties initial settings
     * @return Properties instance containing configuration settings
     */
    public Properties configure(Properties properties) {
        return null;
    }

}
