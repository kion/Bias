/**
 * Created on Feb 19, 2009
 */
package bias.skin.DefaultSkin;

import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import bias.gui.FrontEnd;
import bias.skin.Skin;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */

public class DefaultSkin extends Skin {

    private static final String PROPERTY_USE_OS_NATIVE_LAF = "UseOSNativeLAF";

    @Override
	public void activate(byte[] settings) throws Throwable {
        Properties config = PropertiesUtils.deserializeProperties(settings);
        boolean useOSNativeLAF = Boolean.valueOf(config.getProperty(PROPERTY_USE_OS_NATIVE_LAF));
        if (useOSNativeLAF) {
        	try { // try to set system look-&-feed
    			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    		} catch (Exception e) {
    			// ignore, default cross-platform look-&-feel will be used automatically 
    		}
        } else {
        	try { // try to set cross-platform look-&-feed
    			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    		} catch (Exception e2) {
    			// ignore
    		}
        }
	}
	
	@Override
	public byte[] configure(byte[] settings) throws Throwable {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        JCheckBox cb = new JCheckBox(getMessage("use.os.native.laf"));
        cb.setSelected(Boolean.valueOf(newSettings.getProperty(PROPERTY_USE_OS_NATIVE_LAF)));
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                cb, 
                "Default Skin Settings", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_USE_OS_NATIVE_LAF, "" + cb.isSelected());
        return PropertiesUtils.serializeProperties(newSettings);
	}

}
