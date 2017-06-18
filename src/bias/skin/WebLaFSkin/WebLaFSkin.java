/**
 * Created on Apr 26, 2017
 */
package bias.skin.WebLaFSkin;

import javax.swing.UIManager;

import com.alee.laf.WebLookAndFeel;

import bias.skin.Skin;

/**
 * @author kion
 */
public class WebLaFSkin extends Skin {

    /* (non-Javadoc)
     * @see bias.skin.Skin#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new WebLookAndFeel());
    }

}
