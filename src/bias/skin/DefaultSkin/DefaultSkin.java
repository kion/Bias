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
    
    private static final String NIMBUS_LAF_CLASS = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
    
    private static final boolean isNimbusLAFAvailable = isNimbusLAFAvailable();

	private static boolean isNimbusLAFAvailable() {
		try {
			Class.forName(NIMBUS_LAF_CLASS);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

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
        } else if (useNimbusLAF && isNimbusLAFAvailable) {
        	try { // try to set Nimbus look-&-feel
    			UIManager.setLookAndFeel(NIMBUS_LAF_CLASS);
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
        Component[] cmps = new Component[isNimbusLAFAvailable ? 2 : 1];
        final JCheckBox cb = new JCheckBox(getMessage("use.os.native.laf"));
        cb.setSelected(Boolean.valueOf(newSettings.getProperty(PROPERTY_USE_OS_NATIVE_LAF)));
        cmps[0] = cb;
        final JCheckBox cb2;
        if (isNimbusLAFAvailable) {
            cb2 = new JCheckBox(getMessage("use.nimbus.laf"));
            cb2.setSelected(Boolean.valueOf(newSettings.getProperty(PROPERTY_USE_NIMBUS_LAF)));
            cmps[1] = cb2;
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
        } else {
        	cb2 = null;
        }
        
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                cmps, 
                "Default Skin Settings", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_USE_OS_NATIVE_LAF, "" + cb.isSelected());
    	newSettings.setProperty(PROPERTY_USE_NIMBUS_LAF, "" + (isNimbusLAFAvailable && cb2.isSelected()));
        return PropertiesUtils.serializeProperties(newSettings);
	}
	
}
