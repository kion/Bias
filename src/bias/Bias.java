/**
 * Created on Oct 15, 2006
 */
package bias;

import bias.core.BackEnd;
import bias.gui.FrontEnd;

/**
 * @author kion
 */
public class Bias {
    
    public static void launchApp(String password) throws Throwable {
        // pass password to back-end
        BackEnd.setPassword(null, password);
        // display front-end
        FrontEnd.startup();
    }
    
}
