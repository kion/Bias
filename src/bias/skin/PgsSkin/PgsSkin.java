/**
 * Created on Jan 24, 2008
 */
package bias.skin.PgsSkin;

import java.awt.Component;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.pagosoft.plaf.PgsTheme;
import com.pagosoft.plaf.PlafOptions;
import com.pagosoft.plaf.themes.ElegantGrayTheme;
import com.pagosoft.plaf.themes.SilverTheme;
import com.pagosoft.plaf.themes.VistaTheme;

import bias.gui.FrontEnd;
import bias.skin.Skin;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */

public class PgsSkin extends Skin {

    private static final String PROPERTY_THEME = "Theme";
    
    private static final String THEME_SILVER = "Silver";

    private static final String THEME_VISTA = "Vista";

    private static final String THEME_ELEGANT_GRAY = "ElegantGray";

    private static final String[] THEMES = { THEME_SILVER, THEME_VISTA, THEME_ELEGANT_GRAY };
    
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
            themeName = THEMES[0];
        }
        cb.setSelectedItem(themeName);
        components.add(cb);
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                components.toArray(), 
                "Settings for Pgs Look-&-Feel", 
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
        PgsTheme theme = null;
        String themeName = settings.getProperty(PROPERTY_THEME);
        if (themeName != null) {
            switch (themeName) {
                case THEME_SILVER:
                    theme = new SilverTheme();
                    break;
                case THEME_VISTA:
                    theme = new VistaTheme();
                    break;
                case THEME_ELEGANT_GRAY:
                    theme = ElegantGrayTheme.getInstance();
                    break;
            }
        }
        if (theme == null) {
            theme = new SilverTheme();
        }
        PlafOptions.setCurrentTheme(theme);
        PlafOptions.setAsLookAndFeel();
    }

}
