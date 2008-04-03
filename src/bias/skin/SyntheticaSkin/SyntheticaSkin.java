/**
 * Created on Jan 6, 2007
 */
package bias.skin.SyntheticaSkin;

import javax.swing.UIManager;

import bias.skin.Skin;
import de.javasoft.plaf.synthetica.SyntheticaBlackMoonLookAndFeel;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;

/**
 * @author kion
 */

public class SyntheticaSkin extends Skin {

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
      SyntheticaLookAndFeel.setFont("SansSerif", 12);
      SyntheticaLookAndFeel.setAntiAliasEnabled(true);
//      SyntheticaLookAndFeel.setToolbarSeparatorDimension(new Dimension(1,32));
//      SyntheticaLookAndFeel.setExtendedFileChooserEnabled(false);
//      SyntheticaLookAndFeel.setRememberFileChooserPreferences(false);
    }
    
}
