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
        version="0.1.1",
        author="R. Kasianenko",
        description = "Bias Substance Look-&-Feel",
        details = "<i>BiasSubstanceLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
                  "It uses cross-platform <a href=\"http://substance.dev.java.net/\">Substance Look-&-Feel</a> for Java/Swing applications.")
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
