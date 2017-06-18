/**
 * Created on Jan 6, 2007
 */
package bias.skin.TinySkin;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import bias.gui.FrontEnd;
import bias.skin.Skin;
import bias.utils.PropertiesUtils;
import de.muntjak.tinylookandfeel.Theme;
import de.muntjak.tinylookandfeel.ThemeDescription;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;

/**
 * @author kion
 */

public class TinySkin extends Skin {
    
    private static final String PROPERTY_THEME = "Theme";

    private static final List<String> THEMES = 
        Arrays.stream(Theme.getAvailableThemes())
              .map($ -> $.getName())
              .collect(Collectors.toList());
    
    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        Collection<Component> components = new LinkedList<Component>();
        JLabel lTh = new JLabel("Theme:");
        components.add(lTh);
        JComboBox<String> cb = new JComboBox<>();
        for (String theme : THEMES) {
            cb.addItem(theme);
        }
        String themeName = newSettings.getProperty(PROPERTY_THEME);
        if (themeName == null) {
            themeName = Theme.getAvailableThemes()[0].getName();
        }
        cb.setSelectedItem(themeName);
        components.add(cb);
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                components.toArray(), 
                "Settings for Tiny Look-&-Feel", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_THEME, (String) cb.getSelectedItem());
        return PropertiesUtils.serializeProperties(newSettings);
    }
    
    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settingsBytes) throws Throwable {
        Properties settings = PropertiesUtils.deserializeProperties(settingsBytes);
        ThemeDescription themeDescr = null;
        String themeName = settings.getProperty(PROPERTY_THEME);
        if (themeName != null) {
            for (ThemeDescription td : Theme.getAvailableThemes()) {
                if (td.getName().equals(themeName)) {
                    themeDescr = td;
                    break;
                }
            }
        }
        if (themeDescr == null) {
            themeDescr = Theme.getAvailableThemes()[0];
        }
        Theme.loadTheme(themeDescr);
        UIManager.setLookAndFeel(new TinyLookAndFeel());
    }

}
