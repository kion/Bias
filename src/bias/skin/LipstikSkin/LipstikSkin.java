/**
 * Created on Jan 6, 2007
 */
package bias.skin.LipstikSkin;

import javax.swing.UIManager;

import bias.skin.Skin;

import com.lipstikLF.LipstikLookAndFeel;

/**
 * @author kion
 */

public class LipstikSkin extends Skin {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new LipstikLookAndFeel());
    }

}
