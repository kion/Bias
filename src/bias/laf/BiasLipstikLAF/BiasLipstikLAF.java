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
        author="kion",
        description = "Bias Lipstik Look-&-Feel")
public class BiasLipstikLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        UIManager.setLookAndFeel(new LipstikLookAndFeel());
    }

}
