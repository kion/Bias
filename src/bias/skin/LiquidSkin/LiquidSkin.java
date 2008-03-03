/**
 * Created on Jan 6, 2007
 */
package bias.skin.LiquidSkin;

import javax.swing.UIManager;

import bias.skin.Skin;

import com.birosoft.liquid.LiquidLookAndFeel;

/**
 * @author kion
 */

public class LiquidSkin extends Skin {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        LiquidLookAndFeel.setLiquidDecorations(true);
        LiquidLookAndFeel.setShowTableGrids(true);
        UIManager.setLookAndFeel(new LiquidLookAndFeel());
    }
    
}
