/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasSubstanceLAF;

import javax.swing.UIManager;

import org.jvnet.substance.skin.SubstanceBusinessLookAndFeel;

import bias.annotation.AddOnAnnotation;
import bias.laf.LookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1",
        author="kion",
        description = "Bias Substance Look-&-Feel")
        // TODO [P1] add LAF details to add-on annotation
public class BiasSubstanceLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        javax.swing.LookAndFeel laf;
//      laf = new SubstanceLookAndFeel();
//      laf = new SubstanceCremeLookAndFeel();
//      laf = new SubstanceSaharaLookAndFeel();
//      laf = new SubstanceModerateLookAndFeel();
//      laf = new SubstanceOfficeSilver2007LookAndFeel();
//      laf = new SubstanceRavenLookAndFeel();
//      laf = new SubstanceMagmaLookAndFeel();
//      laf = new SubstanceOfficeBlue2007LookAndFeel();
//      laf = new SubstanceGreenMagicLookAndFeel();
//      laf = new SubstanceMangoLookAndFeel();
//      laf = new SubstanceFieldOfWheatLookAndFeel();
      laf = new SubstanceBusinessLookAndFeel();
      UIManager.setLookAndFeel(laf);
    }
    
}
