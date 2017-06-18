/**
 * Created on Feb 19, 2009
 */
package bias.skin.DefaultSkin;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import bias.gui.FrontEnd;
import bias.skin.Skin;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */

public class DefaultSkin extends Skin {

    public static String DEFAULT_LAF;
    
    public static final String PROPERTY_LAF = "LookAndFeel";

    public static final Map<String, String> SUPPORTED_LAFS = getSupportedLAFs();
    
    public static Map<String, String> getSupportedLAFs() {
        Map<String, String> lafs = new HashMap<>();
        for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            if (UIManager.getCrossPlatformLookAndFeelClassName().equals(lafInfo.getClassName())) {
                DEFAULT_LAF = lafInfo.getName();
            }
            lafs.put(lafInfo.getName(), lafInfo.getClassName());
        }
        return lafs;
    };
	
	@Override
	public void activate(byte[] configBytes) throws Throwable {
        Properties settings = PropertiesUtils.deserializeProperties(configBytes);
        String laf = settings.getProperty(PROPERTY_LAF);
        if (laf == null || !SUPPORTED_LAFS.containsKey(laf)) {
            laf = DEFAULT_LAF;
        }
        try {
            UIManager.setLookAndFeel(SUPPORTED_LAFS.get(laf));
        } catch (Throwable cause) {
            cause.printStackTrace(System.err);
            // ignore, default cross-platform look-&-feel will be used 
        }
	}
	
	@Override
	public byte[] configure(byte[] configBytes) throws Throwable {
        Properties settings = PropertiesUtils.deserializeProperties(configBytes);
        JLabel labelLAF = new JLabel("Look & Feel:");
        JComboBox<String> cbLAF = new JComboBox<>();
        for (String laf : SUPPORTED_LAFS.keySet()) {
            cbLAF.addItem(laf);
        }
        cbLAF.setSelectedItem(settings.getProperty(PROPERTY_LAF, DEFAULT_LAF));
        Component[] cmps = new Component[]{ labelLAF, cbLAF };
        
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                cmps, 
                "Default Skin Settings", 
                JOptionPane.INFORMATION_MESSAGE);
        settings.setProperty(PROPERTY_LAF, "" + cbLAF.getSelectedItem());
        return PropertiesUtils.serializeProperties(settings);
	}
	
}
