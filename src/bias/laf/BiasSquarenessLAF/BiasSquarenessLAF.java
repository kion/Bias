/**
 * Created on Jan 24, 2008
 */
package bias.laf.BiasSquarenessLAF;

import javax.swing.UIManager;

import net.beeger.squareness.SquarenessLookAndFeel;
import bias.laf.LookAndFeel;

/**
 * @author kion
 */

public class BiasSquarenessLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new SquarenessLookAndFeel());
    }

}
