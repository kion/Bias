package bias.skin.FlatLaFSkin;

import bias.gui.FrontEnd;
import bias.skin.Skin;
import bias.utils.PropertiesUtils;
import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public class FlatLaFSkin extends Skin {

    private static final String PROPERTY_THEME = "Theme";

    private static final String THEME_FLAT_LIGHT = "FlatLightLaf";

    private static final String THEME_FLAT_DARK = "FlatDarkLaf";

    private static final String THEME_FLAT_INTELLIJ_LIGHT = "FlatIntelliJLaf";

    private static final String THEME_FLAT_INTELLIJ_DARCULA = "FlatDarculaLaf";

    private static final String[] THEMES = { THEME_FLAT_LIGHT, THEME_FLAT_DARK, THEME_FLAT_INTELLIJ_LIGHT, THEME_FLAT_INTELLIJ_DARCULA };

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
                "Settings for FlatLaF",
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
        FlatLaf theme = null;
        String themeName = settings.getProperty(PROPERTY_THEME);
        if (themeName != null) {
            switch (themeName) {
                case THEME_FLAT_LIGHT:
                    theme = new FlatLightLaf();
                    break;
                case THEME_FLAT_DARK:
                    theme = new FlatDarkLaf();
                    break;
                case THEME_FLAT_INTELLIJ_LIGHT:
                    theme = new FlatIntelliJLaf();
                    break;
                case THEME_FLAT_INTELLIJ_DARCULA:
                    theme = new FlatDarculaLaf();
                    break;
            }
        }
        if (theme == null) {
            theme = new FlatLightLaf();
        }
        UIManager.setLookAndFeel(theme);
    }

}
