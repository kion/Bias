/**
 * Created on Jan 5, 2007
 */
package bias.laf.BiasJGLooksLAF;

import java.awt.Component;
import java.awt.Font;
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
import bias.laf.LookAndFeel;
import bias.laf.UIIcons;
import bias.utils.PropertiesUtils;

import com.jgoodies.looks.FontPolicies;
import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.FontSet;
import com.jgoodies.looks.FontSets;
import com.jgoodies.looks.Fonts;
import com.jgoodies.looks.plastic.PlasticTheme;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.BrownSugar;
import com.jgoodies.looks.plastic.theme.DarkStar;
import com.jgoodies.looks.plastic.theme.DesertBlue;
import com.jgoodies.looks.plastic.theme.DesertBluer;
import com.jgoodies.looks.plastic.theme.DesertGreen;
import com.jgoodies.looks.plastic.theme.DesertRed;
import com.jgoodies.looks.plastic.theme.DesertYellow;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import com.jgoodies.looks.plastic.theme.ExperienceGreen;
import com.jgoodies.looks.plastic.theme.ExperienceRoyale;
import com.jgoodies.looks.plastic.theme.LightGray;
import com.jgoodies.looks.plastic.theme.Silver;
import com.jgoodies.looks.plastic.theme.SkyBlue;
import com.jgoodies.looks.plastic.theme.SkyBluer;
import com.jgoodies.looks.plastic.theme.SkyGreen;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import com.jgoodies.looks.plastic.theme.SkyPink;
import com.jgoodies.looks.plastic.theme.SkyRed;
import com.jgoodies.looks.plastic.theme.SkyYellow;

/**
 * @author kion
 */

public class BiasJGLooksLAF extends LookAndFeel {
    
    private static final String PROPERTY_THEME = "Theme";
    private static final String[] PROPERTIES_FONTS = new String[]{
        "Control-Font",
        "Menu-Font",
        "Title-Font",
        "Message-Font",
        "Small-Font",
        "Window-Title-Font"
    };
    
    private static final Map<String, Font> FONTS = fontsMap();
    
    private static Map<String, Font> fontsMap() {
        Map<String, Font> m = new LinkedHashMap<String, Font>();
        m.put("Segue 12pt", Fonts.SEGOE_UI_12PT);
        m.put("Segue 13pt", Fonts.SEGOE_UI_13PT);
        m.put("Segue 15pt", Fonts.SEGOE_UI_15PT);
        m.put("Tahoma 11pt", Fonts.TAHOMA_11PT);
        m.put("Tahoma 13pt", Fonts.TAHOMA_13PT);
        m.put("Tahoma 14pt", Fonts.TAHOMA_14PT);
        return m;
    };
    
    private static final Class<?>[] THEMES = new Class[]{
        Silver.class,
        LightGray.class,
        ExperienceGreen.class,
        ExperienceBlue.class,
        ExperienceRoyale.class,
        DesertBluer.class,
        DesertBlue.class,
        DesertGreen.class,
        DesertRed.class,
        DesertYellow.class,
        SkyBluer.class,
        SkyBlue.class,
        SkyGreen.class,
        SkyKrupp.class,
        SkyPink.class,
        SkyRed.class,
        SkyYellow.class,
        BrownSugar.class,
        DarkStar.class
    };
    
    private static final Font DEFAULT_FONT = Fonts.SEGOE_UI_12PT;
    
    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) {
        Properties newSettings = PropertiesUtils.deserializeProperties(settings);
        Collection<Component> components = new LinkedList<Component>();
        JLabel lTh = new JLabel("Theme:");
        components.add(lTh);
        JComboBox cb = new JComboBox();
        for (Class<?> themeClass : THEMES) {
            cb.addItem(themeClass.getSimpleName());
        }
        String themeName = newSettings.getProperty(PROPERTY_THEME);
        if (themeName == null) {
            themeName = PlasticXPLookAndFeel.getPlasticTheme().getClass().getSimpleName();
        }
        cb.setSelectedItem(themeName);
        components.add(cb);
        for (String fontProp : PROPERTIES_FONTS) {
            JLabel fl = new JLabel(fontProp);
            components.add(fl);
            JComboBox fcb = new JComboBox();
            for (String font : FONTS.keySet()) {
                fcb.addItem(font);
            }
            String selFont = newSettings.getProperty(fontProp);
            if (selFont != null) {
                fcb.setSelectedItem(selFont);
            }
            fcb.setName(fontProp);
            components.add(fcb);
        }
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                components.toArray(), 
                "Settings for JGoodies Looks Look-&-Feel", 
                JOptionPane.INFORMATION_MESSAGE);
        newSettings.setProperty(PROPERTY_THEME, (String) cb.getSelectedItem());
        for (Component c : components) {
            if (c instanceof JComboBox && c.getName() != null) {
                newSettings.setProperty(c.getName(), (String) ((JComboBox) c).getSelectedItem());
            }
        }
        return PropertiesUtils.serializeProperties(newSettings);
    }

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#activate(byte[])
     */
    @Override
    public void activate(byte[] settingsBytes) throws Throwable {
        Properties settings = PropertiesUtils.deserializeProperties(settingsBytes);
        Font[] fonts = new Font[6];
        for (int i = 0; i < fonts.length; i++) {
            String fontName = (String) settings.get(PROPERTIES_FONTS[i]);
            Font font = FONTS.get(fontName);
            if (font == null) {
                font = DEFAULT_FONT;
            }
            fonts[i] = font;
        }
        FontSet defaultFontSet = FontSets.createDefaultFontSet(fonts[0], fonts[1], fonts[2], fonts[3], fonts[4], fonts[5]);
        FontPolicy defaultFontPolicy = FontPolicies.createFixedPolicy(defaultFontSet);
        PlasticXPLookAndFeel.setFontPolicy(defaultFontPolicy);
        PlasticTheme theme = null;
        String themeName = settings.getProperty(PROPERTY_THEME);
        if (themeName != null) {
            for (Class<?> themeClass : THEMES) {
                if (themeClass.getSimpleName().equals(themeName)) {
                    theme = (PlasticTheme) themeClass.newInstance();
                }
            }
        }
        if (theme == null) {
            theme = new Silver();
        }
        PlasticXPLookAndFeel.setPlasticTheme(theme);
        UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
    }

    /* (non-Javadoc)
     * @see bias.laf.LookAndFeel#getControlIcons()
     */
    @Override
    public UIIcons getUIIcons() {
        // TODO [P3] try to use custom LAF icons
        return null;
    }

}
