/**
 * Created on Apr 26, 2017
 */
package bias.skin.DarculaSkin;

import javax.swing.UIManager;

import com.bulenkov.darcula.DarculaLaf;

import bias.skin.Skin;

/**
 * @author kion
 */
public class DarculaSkin extends Skin {

    /* (non-Javadoc)
     * @see bias.skin.Skin#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new DarculaLaf());
    }
    
    /* (non-Javadoc)
     * @see bias.skin.Skin#isDefaultHTMLEditorKitRequired()
     */
    @Override
    public boolean isDefaultHTMLEditorKitRequired() {
        return true;
    }
    
}
