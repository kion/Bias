/**
 * Created on Jan 5, 2007
 */
package bias.laf;

import bias.annotation.AddOnAnnotation;

/**
 * @author kion
 */

@AddOnAnnotation(
        name = "LookAndFeelActivator Interface", 
        version="1.0",
        description = "Look-&-Feel activator interface",
        author="kion")
public interface LookAndFeelActivator {
    
    /**
     * Performs actions to activate certain LookAndFeel
     */
    public void activate() throws Exception;

}
