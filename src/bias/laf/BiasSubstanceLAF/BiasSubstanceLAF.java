/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasSubstanceLAF;

import javax.swing.UIManager;

import org.jvnet.substance.fonts.FontPolicies;
import org.jvnet.substance.skin.SubstanceBusinessLookAndFeel;

import bias.laf.LookAndFeel;

/**
 * @author kion
 */

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
      SubstanceBusinessLookAndFeel.setFontPolicy(FontPolicies.getTransitionalPlasticPolicy());
      UIManager.setLookAndFeel(laf);
    }
    
}
