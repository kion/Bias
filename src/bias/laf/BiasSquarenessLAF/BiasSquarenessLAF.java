/**
 * Created on Jan 24, 2008
 */
package bias.laf.BiasSquarenessLAF;

import javax.swing.UIManager;

import net.beeger.squareness.SquarenessLookAndFeel;
import bias.annotation.AddOnAnnotation;
import bias.laf.LookAndFeel;

/**
 * @author kion
 */
@AddOnAnnotation(
        version="0.1",
        author="R. Kasianenko",
        description = "Bias Squareness Look-&-Feel",
        details = "<i>BiasSquarenessLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
                  "It uses a minimalistic and flat looking <a href=\"http://squareness.beeger.net/\">Squareness Look-&-Feel</a> for Java/Swing applications<br>" +
                  "provided by <a href=\"http://beeger.net/\">Robert F. Beeger</a>.")
public class BiasSquarenessLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new SquarenessLookAndFeel());
    }

}
