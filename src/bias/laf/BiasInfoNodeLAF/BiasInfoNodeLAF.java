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

//@AddOnAnnotation(
//        version="0.1.1",
//        author="R. Kasianenko",
//        description = "Bias InfoNode Look-&-Feel",
//        details = "<i>BiasInfoNodeLAF</i> add-on for Bias provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br>" +
//                  "It uses <a href=\"http://www.geocities.com/shfarr/\">InfoNode Look-&-Feel</a> for Java/Swing applications " +
//                  "provided by <a href=\"http://www.nnl.se/data/html/english/index.html\">NNL Technology AB</a>.")
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
