/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasLiquidLAF;

import javax.swing.UIManager;

import bias.annotation.AddOnAnnotation;
import bias.laf.LookAndFeel;

import com.birosoft.liquid.LiquidLookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1",
        author="R. Kasianenko",
        description = "Bias Liquid Look-&-Feel",
        details = "<i>BiasLiquidLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
                  "It uses <a href=\"https://liquidlnf.dev.java.net/\">Liquid Look-&-Feel</a> for Java/Swing applications.")
public class BiasLiquidLAF extends LookAndFeel {

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
