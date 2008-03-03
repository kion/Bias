/**
 * Created on Jan 6, 2007
 */
package bias.laf.BiasInfoNodeLAF;

import javax.swing.UIManager;

import net.infonode.gui.laf.InfoNodeLookAndFeel;
import bias.laf.LookAndFeel;

/**
 * @author kion
 */

public class BiasInfoNodeLAF extends LookAndFeel {

    @Override
    public void activate(byte[] settings) throws Throwable {
//      InfoNodeLookAndFeelTheme theme =
//      new InfoNodeLookAndFeelTheme("My Theme",
//                                   new Color(110, 120, 150),
//                                   new Color(0, 170, 0),
//                                   new Color(80, 80, 80),
//                                   Color.WHITE,
//                                   new Color(0, 170, 0),
//                                   Color.WHITE,
//                                   0.8);
//      UIManager.setLookAndFeel(new InfoNodeLookAndFeel(theme));
        UIManager.setLookAndFeel(new InfoNodeLookAndFeel());
    }
    
}
