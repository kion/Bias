/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasTinyLAF;

import javax.swing.UIManager;

import bias.annotation.AddOnAnnotation;
import bias.laf.LookAndFeel;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1",
        author="kion",
        description = "Bias Tiny Look-&-Feel")
        // TODO [P1] add LAF details to add-on annotation
public class BiasTinyLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new TinyLookAndFeel());
    }

}
