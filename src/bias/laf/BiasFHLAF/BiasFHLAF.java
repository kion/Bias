/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasFHLAF;

import javax.swing.UIManager;

import bias.annotation.AddOnAnnotation;
import bias.laf.LookAndFeel;

import com.shfarr.ui.plaf.fh.FhLookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1",
        author="kion",
        description="Bias FH Look-&-Feel")
        // TODO [P1] add LAF details to add-on annotation
public class BiasFHLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new FhLookAndFeel());
    }

}
