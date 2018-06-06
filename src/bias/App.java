/**
 * Created on Oct 15, 2006
 */
package bias;

import javax.swing.SwingUtilities;

import bias.core.BackEnd;
import bias.gui.FrontEnd;

/**
 * @author kion
 */
public class App {
    
    public static void launch(String password) throws Throwable {
        // pass password to back-end
        BackEnd.getInstance().setPassword(password);
        // display front-end
        SwingUtilities.invokeLater(() -> {
            FrontEnd.startup();
        });
    }
    
}
