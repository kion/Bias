/**
 * Created on Jan 6, 2007
 */
package bias.laf.LiquidLAF;

import javax.swing.UIManager;

import bias.laf.LookAndFeel;

import com.birosoft.liquid.LiquidLookAndFeel;

/**
 * @author kion
 */

public class LiquidLAF extends LookAndFeel {

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
