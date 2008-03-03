/**
 * Created on Jan 24, 2008
 */
package bias.laf.BiasPgsLAF;

import bias.laf.LookAndFeel;

import com.pagosoft.plaf.PlafOptions;

/**
 * @author kion
 */

public class BiasPgsLAF extends LookAndFeel {

    // TODO [P3] provide look-&-feel customization

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        PlafOptions.setVistaStyle(true);
        PlafOptions.setAsLookAndFeel();
    }

}
