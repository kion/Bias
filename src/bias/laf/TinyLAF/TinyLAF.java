/**
 * Created on Jan 6, 2007
 */
package bias.laf.TinyLAF;

import javax.swing.UIManager;

import bias.laf.LookAndFeel;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;

/**
 * @author kion
 */

public class TinyLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new TinyLookAndFeel());
    }

}
