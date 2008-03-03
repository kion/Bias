/**
 * Created on Jan 6, 2007
 */
package bias.skin.TinySkin;

import javax.swing.UIManager;

import bias.skin.Skin;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;

/**
 * @author kion
 */

public class TinySkin extends Skin {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new TinyLookAndFeel());
    }

}
