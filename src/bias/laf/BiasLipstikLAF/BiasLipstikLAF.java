/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasLipstikLAF;

import javax.swing.UIManager;

import bias.annotation.AddOnAnnotation;
import bias.laf.LookAndFeel;

import com.lipstikLF.LipstikLookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1",
        author="R. Kasianenko",
        description = "Bias Lipstik Look-&-Feel",
        details = "<i>BiasLipstikLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
                  "It uses <a href=\"http://regis.risp.pl/\">Lipstik Look-&-Feel</a> for Java/Swing applications provided by Michal Buczko.")
public class BiasLipstikLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new LipstikLookAndFeel());
    }

}
