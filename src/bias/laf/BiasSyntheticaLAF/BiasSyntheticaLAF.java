/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasSyntheticaLAF;

import javax.swing.UIManager;

import bias.annotation.AddOnAnnotation;
import bias.laf.LookAndFeel;
import de.javasoft.plaf.synthetica.SyntheticaBlackMoonLookAndFeel;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1",
        author="kion",
        description = "Bias Synthetica Look-&-Feel")
        // TODO [P1] add LAF details to add-on annotation
public class BiasSyntheticaLAF extends LookAndFeel {

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settings) throws Throwable {
        javax.swing.LookAndFeel laf;
//      laf = new SyntheticaStandardLookAndFeel();
//      laf = new SyntheticaSilverMoonLookAndFeel();
      laf = new SyntheticaBlackMoonLookAndFeel();
      UIManager.setLookAndFeel(laf);
//      SyntheticaLookAndFeel.setWindowsDecorated(false);
//      SyntheticaLookAndFeel.setFont("Dialog", 12);
      SyntheticaLookAndFeel.setAntiAliasEnabled(true);
//      SyntheticaLookAndFeel.setToolbarSeparatorDimension(new Dimension(1,32));
//      SyntheticaLookAndFeel.setExtendedFileChooserEnabled(false);
//      SyntheticaLookAndFeel.setRememberFileChooserPreferences(false);
    }
    
}
