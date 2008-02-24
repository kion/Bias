/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasTinyLAF;

import javax.swing.UIManager;

import bias.annotation.AddOnAnnotation;
import bias.laf.ControlIcons;
import bias.laf.LookAndFeel;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1.1",
        author="R. Kasianenko",
        description = "Bias Tiny Look-&-Feel",
        details = "<i>BiasTinyLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
                  "It uses <a href=\"http://www.muntjak.de/hans/java/tinylaf/index.html\">Tiny Look-&-Feel</a> for Java/Swing applications<br>" +
                  "provided by <a href=\"http://www.muntjak.de/index.html\">Hans Bickel</a>.")
public class BiasTinyLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new TinyLookAndFeel());
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
