/**
 * Created on Jan 6, 2007
 */
package bias.laf.LipstikLAF;

import javax.swing.UIManager;

import bias.laf.LookAndFeel;

import com.lipstikLF.LipstikLookAndFeel;

/**
 * @author kion
 */

public class LipstikLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new LipstikLookAndFeel());
    }

}
