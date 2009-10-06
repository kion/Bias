/**
 * Created on Feb 19, 2009
 */
package bias.skin.DefaultSkin;

import java.awt.Component;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bias.gui.FrontEnd;
import bias.skin.Skin;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */

public class DefaultSkin extends Skin {

    private static final String PROPERTY_USE_OS_NATIVE_LAF = "UseOSNativeLAF";

    private static final String PROPERTY_USE_NIMBUS_LAF = "UseNimbusLAF";

    @Override
	public void activate(byte[] settings) throws Throwable {
        Properties config = PropertiesUtils.deserializeProperties(settings);
        boolean useOSNativeLAF = Boolean.valueOf(config.getProperty(PROPERTY_USE_OS_NATIVE_LAF));
        boolean useNimbusLAF = Boolean.valueOf(config.getProperty(PROPERTY_USE_NIMBUS_LAF));
        if (useOSNativeLAF) {
        	try { // try to set system look-&-feel
    			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    		} catch (Throwable cause) {
    			// ignore, default cross-platform look-&-feel will be used automatically 
    		}
        } else if (useNimbusLAF) {
        	try { // try to set Nimbus look-&-feel
    			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    		} catch (Throwable cause) {
    			// ignore, default cross-platform look-&-feel will be used automatically 
    		}
        } else {
        	try { // try to set cross-platform look-&-feel
    			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    		} catch (Throwable cause) {
    			// ignore
    		}
        }
	}
	
	@Override
	public byte[] configure(byte[] settings) throws Throwable {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        final JCheckBox cb = new JCheckBox(getMessage("use.os.native.laf"));
        cb.setSelected(Boolean.valueOf(newSettings.getProperty(PROPERTY_USE_OS_NATIVE_LAF)));
        final JCheckBox cb2 = new JCheckBox(getMessage("use.nimbus.laf"));
        cb2.setSelected(Boolean.valueOf(newSettings.getProperty(PROPERTY_USE_NIMBUS_LAF)));

        cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (cb.isSelected()) {
					cb2.setSelected(false);
				}
			}
		});
        cb2.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (cb2.isSelected()) {
					cb.setSelected(false);
				}
			}
		});
        
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{cb, cb2}, 
                "Default Skin Settings", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_USE_OS_NATIVE_LAF, "" + cb.isSelected());
        newSettings.setProperty(PROPERTY_USE_NIMBUS_LAF, "" + cb2.isSelected());
        return PropertiesUtils.serializeProperties(newSettings);
	}

}
