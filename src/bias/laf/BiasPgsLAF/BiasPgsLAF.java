/**
 * Created on Jan 24, 2008
 */
package bias.laf.BiasPgsLAF;

import bias.laf.LookAndFeel;

import com.pagosoft.plaf.PlafOptions;

/**
 * @author kion
 */
//@AddOnAnnotation(
//        version="0.1.1",
//        author="R. Kasianenko",
//        description = "Bias Pgs Look-&-Feel",
//        details = "<i>BiasPgsLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
//                  "It uses <a href=\"http://pgslookandfeel.dev.java.net/\">Pgs Look-&-Feel</a> for Java/Swing applications.")
public class BiasPgsLAF extends LookAndFeel {

    // TODO [P3] provide look-&-feel customization

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        PlafOptions.setVistaStyle(true);
        PlafOptions.setAsLookAndFeel();
    }

}
