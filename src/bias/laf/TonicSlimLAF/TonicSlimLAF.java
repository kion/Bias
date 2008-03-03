/**
 * Created on Jan 6, 2007
 */
package bias.laf.TonicSlimLAF;

import java.awt.Component;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import bias.gui.FrontEnd;
import bias.laf.LookAndFeel;
import bias.utils.PropertiesUtils;

import com.digitprop.tonic.TonicLookAndFeel;

/**
 * @author kion
 */

public class TonicSlimLAF extends LookAndFeel {

    private static final String PROPERTY_THICK_BORDERS = "THICK_BORDERS";
    
    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        JLabel tbL = new JLabel("Thick borders:");
        JCheckBox tb = new JCheckBox();
        String selTB = newSettings.getProperty(PROPERTY_THICK_BORDERS);
        if (selTB != null && Boolean.parseBoolean(selTB)) {
            tb.setSelected(true);
        } else {
            tb.setSelected(false);
        }
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{tbL, tb}, 
                "Settings for Tonic Slim Look-&-Feel", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_THICK_BORDERS, "" + tb.isSelected());
        return PropertiesUtils.serializeProperties(newSettings);
    }

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settingsBytes) throws Throwable {
        Properties settings = PropertiesUtils.deserializeProperties(settingsBytes);
        UIManager.setLookAndFeel(new TonicLookAndFeel());
        if (settings != null) {
            String tbS = settings.getProperty(PROPERTY_THICK_BORDERS);
            if (tbS != null) {
                Boolean tb = Boolean.parseBoolean(tbS);
                UIManager.getDefaults().put("TabbedPane.thickBorders", tb);
            }
        }
    }

}
