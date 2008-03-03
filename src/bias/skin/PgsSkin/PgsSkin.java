/**
 * Created on Jan 24, 2008
 */
package bias.skin.PgsSkin;

import bias.skin.Skin;

import com.pagosoft.plaf.PlafOptions;

/**
 * @author kion
 */

public class PgsSkin extends Skin {

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
