/**
 * Created on Jan 6, 2007
 */
package bias.skin.FHSkin;

import javax.swing.UIManager;

import bias.skin.Skin;

import com.shfarr.ui.plaf.fh.FhLookAndFeel;

/**
 * @author kion
 */

public class FHSkin extends Skin {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new FhLookAndFeel());
    }

}
