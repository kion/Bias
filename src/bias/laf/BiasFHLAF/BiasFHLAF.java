/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasFHLAF;

import javax.swing.UIManager;

import bias.annotation.AddOnAnnotation;
import bias.laf.ControlIcons;
import bias.laf.LookAndFeel;

import com.shfarr.ui.plaf.fh.FhLookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1.1",
        author="R. Kasianenko",
        description="Bias FH Look-&-Feel",
        details = "<i>BiasFHLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
                  "It uses FH Look-&-Feel for Java/Swing applications provided by <a href=\"http://www.geocities.com/shfarr/\">Stefan Harsan Fárr</a>.")
public class BiasFHLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new FhLookAndFeel());
    }

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#getControlIcons()
     */
    @Override
    public ControlIcons getControlIcons() {
        return null;
    }

}
