/**
 * Created on Jul 29, 2008
 */
package bias.skin.JTattooSkin;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import bias.gui.FrontEnd;
import bias.skin.Skin;
import bias.utils.PropertiesUtils;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aero.AeroLookAndFeel;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import com.jtattoo.plaf.fast.FastLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.luna.LunaLookAndFeel;
import com.jtattoo.plaf.mint.MintLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;
import com.jtattoo.plaf.smart.SmartLookAndFeel;

/**
 * @author kion
 */
public class JTattooSkin extends Skin {

    private static final String PROPERTY_LAF = "Look-&-Feel";
    private static final String PROPERTY_THEME = "Theme";

    private static enum LAF {
        Aluminium,
        Aero,
        Luna,
        Mint,
        Acryl,
        HiFi,
        Noire,
        Smart,
        Fast
    };
    
    private static final String DEFAULT_THEME = "Small-Font";
    
    private static final String[] SHARED_THEMES = new String[]{
        DEFAULT_THEME,
        "Large-Font",
        "Giant-Font"
    };
    
    private static final Map<LAF, String[]> LAF_THEMES = buildLAFThemes();
    
    private static final Map<LAF, String[]> buildLAFThemes() {
        Map<LAF, String[]> m = new LinkedHashMap<LAF, String[]>();
        m.put(LAF.Acryl, new String[]{
                "Green-Small-Font",
                "Green-Large-Font",
                "Green-Giant-Font",
                "Red-Small-Font",
                "Red-Large-Font",
                "Red-Giant-Font",
                "Lemmon-Small-Font",
                "Lemmon-Large-Font",
                "Lemmon-Giant-Font" 
        });
        m.put(LAF.Aero, new String[]{
                "Gold-Small-Font",
                "Gold-Large-Font",
                "Gold-Giant-Font", 
                "Green-Small-Font",
                "Green-Large-Font",
                "Green-Giant-Font"
        });
        m.put(LAF.Smart, new String[]{
                "Gold-Small-Font",
                "Gold-Large-Font",
                "Gold-Giant-Font",
                "Green-Small-Font",
                "Green-Large-Font",
                "Green-Giant-Font",
                "Brown-Small-Font",
                "Brown-Large-Font",
                "Brown-Giant-Font", 
                "Gray-Small-Font",
                "Gray-Large-Font",
                "Gray-Giant-Font",
                "Lemmon-Small-Font",
                "Lemmon-Large-Font",
                "Lemmon-Giant-Font"
        });
        m.put(LAF.Fast, new String[]{
                "Blue-Small-Font",
                "Blue-Large-Font",
                "Blue-Giant-Font",
                "Green-Small-Font",
                "Green-Large-Font",
                "Green-Giant-Font"
        });
        return m;
    }
    
    /* (non-Javadoc)
     * @see bias.skin.Skin#activate(byte[])
     */
    @Override
    public void activate(byte[] settingsBytes) throws Throwable {
        Properties settings = PropertiesUtils.deserializeProperties(settingsBytes);
        String themeName = settings.getProperty(PROPERTY_THEME);
        if (themeName == null) {
            themeName = DEFAULT_THEME;
        }
        AbstractLookAndFeel laf = null;
        String lafName = settings.getProperty(PROPERTY_LAF);
        if (lafName != null) {
            LAF l = LAF.valueOf(lafName);
            switch(l) {
            case Aluminium:
                laf = new AluminiumLookAndFeel();
                AluminiumLookAndFeel.setTheme(themeName);
                break;
            case Aero:
                laf = new AeroLookAndFeel();
                AeroLookAndFeel.setTheme(themeName);
                break;
            case HiFi:
                laf = new HiFiLookAndFeel();
                HiFiLookAndFeel.setTheme(themeName);
                break;
            case Noire:
                laf = new NoireLookAndFeel();
                NoireLookAndFeel.setTheme(themeName);
                break;
            case Acryl:
                laf = new AcrylLookAndFeel();
                AcrylLookAndFeel.setTheme(themeName);
                break;
            case Luna:
                laf = new LunaLookAndFeel();
                LunaLookAndFeel.setTheme(themeName);
                break;
            case Mint:
                laf = new MintLookAndFeel();
                MintLookAndFeel.setTheme(themeName);
                break;
            case Smart:
                laf = new SmartLookAndFeel();
                SmartLookAndFeel.setTheme(themeName);
                break;
            case Fast:
                laf = new FastLookAndFeel();
                FastLookAndFeel.setTheme(themeName);
                break;
            }
        } else {
            laf = new AluminiumLookAndFeel();
            AluminiumLookAndFeel.setTheme(themeName);
        }
        UIManager.setLookAndFeel(laf);
    }
    
    /* (non-Javadoc)
     * @see bias.skin.Skin#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        Collection<Component> components = new LinkedList<Component>();
        JLabel lTh = new JLabel(PROPERTY_LAF);
        components.add(lTh);
        final JComboBox cb = new JComboBox();
        for (LAF l : LAF.values()) {
            cb.addItem(l);
        }
        LAF laf;
        String lafName = newSettings.getProperty(PROPERTY_LAF);
        if (lafName == null) {
            laf = LAF.Aluminium;
        } else {
            laf = LAF.valueOf(lafName);
        }
        cb.setSelectedItem(laf);
        components.add(cb);
        JLabel fl = new JLabel(PROPERTY_THEME);
        components.add(fl);
        final JComboBox tcb = new JComboBox();
        for (String theme : SHARED_THEMES) {
            tcb.addItem(theme);
        }
        String[] themes = LAF_THEMES.get(laf);
        if (themes != null) {
            for (String theme : themes) {
                tcb.addItem(theme);
            }
        }
        String selTheme = newSettings.getProperty(PROPERTY_THEME);
        if (selTheme != null) {
            tcb.setSelectedItem(selTheme);
        }
        components.add(tcb);
        cb.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tcb.removeAllItems();
                    for (String theme : SHARED_THEMES) {
                        tcb.addItem(theme);
                    }
                    String[] themes = LAF_THEMES.get(e.getItem());
                    if (themes != null) {
                        for (String theme : themes) {
                            tcb.addItem(theme);
                        }
                    }
                }
            }
        });
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                components.toArray(), 
                "JTatoo Skin Configuration", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_LAF, ((LAF) cb.getSelectedItem()).name());
        newSettings.setProperty(PROPERTY_THEME, (String) tcb.getSelectedItem());
        return PropertiesUtils.serializeProperties(newSettings);
    }

}
