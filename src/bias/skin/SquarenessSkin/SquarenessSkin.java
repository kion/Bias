/**
 * Created on Jan 24, 2008
 */
package bias.skin.SquarenessSkin;

import javax.swing.UIManager;

import net.beeger.squareness.SquarenessLookAndFeel;
import bias.skin.Skin;

/**
 * @author kion
 */

public class SquarenessSkin extends Skin {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new SquarenessLookAndFeel());
    }

}
