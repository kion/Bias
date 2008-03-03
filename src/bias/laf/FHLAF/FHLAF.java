/**
 * Created on Jan 6, 2007
 */
package bias.laf.FHLAF;

import javax.swing.UIManager;

import bias.laf.LookAndFeel;

import com.shfarr.ui.plaf.fh.FhLookAndFeel;

/**
 * @author kion
 */

public class FHLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new FhLookAndFeel());
    }

}
