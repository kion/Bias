/**
 * Created on Feb 23, 2008
 */
package bias.extension.MainEntry;

import java.awt.Component;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import bias.Constants;
import bias.event.BeforeSaveEventListener;
import bias.event.HideAppWindowEvent;
import bias.event.HideAppWindowEventListener;
import bias.event.SaveEvent;
import bias.extension.ToolExtension;
import bias.gui.FrontEnd;
import bias.gui.VisualEntryDescriptor;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

public class MainEntry extends ToolExtension implements BeforeSaveEventListener, HideAppWindowEventListener {
    
    private static final String PROPERTY_MAIN_ENTRY_UUID = "MAIN_ENTRY_UUID";
    private static final String PROPERTY_SWITCH_UPON_DATA_SAVE = "SWITCH_UPON_DATA_SAVE";
    private static final String PROPERTY_SWITCH_UPON_DATA_SAVE_BEFORE_APP_EXIT_ONLY = "_SWITCH_UPON_DATA_SAVE_BEFORE_APP_EXIT_ONLY";
    private static final String PROPERTY_SWITCH_UPON_APP_WINDOW_HIDE = "SWITCH_UPON_APP_WINDOW_HIDE";
    
    private UUID mainEntryId;
    
    private boolean switchUponDataSave;
    
    private boolean switchUponDataSaveBeforeAppExitOnly;
    
    private boolean switchUponAppWindowHide;
    
    private byte[] settings;
    
    public MainEntry(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        initSettings();
        FrontEnd.addBeforeSaveEventListener(this);
        FrontEnd.addHideAppWindowEventListener(this);
    }
    
    private void initSettings() {
        if (getSettings() != null && !Arrays.equals(getSettings(), settings)) {
            settings = getSettings();
            String idStr = PropertiesUtils.deserializeProperties(settings).getProperty(PROPERTY_MAIN_ENTRY_UUID);
            if (!Validator.isNullOrBlank(idStr)) {
                mainEntryId = UUID.fromString(idStr);
            }
            String sudsStr = PropertiesUtils.deserializeProperties(settings).getProperty(PROPERTY_SWITCH_UPON_DATA_SAVE);
            switchUponDataSave = Boolean.valueOf(sudsStr); 
            String sudsbaeoStr = PropertiesUtils.deserializeProperties(settings).getProperty(PROPERTY_SWITCH_UPON_DATA_SAVE_BEFORE_APP_EXIT_ONLY);
            switchUponDataSaveBeforeAppExitOnly = Boolean.valueOf(sudsbaeoStr);
            String suawhStr = PropertiesUtils.deserializeProperties(settings).getProperty(PROPERTY_SWITCH_UPON_APP_WINDOW_HIDE);
            switchUponAppWindowHide = Boolean.valueOf(suawhStr);
        }
    }
    
    /* (non-Javadoc)
     * @see bias.event.BeforeSaveEventListener#onEvent(bias.event.SaveEvent)
     */
    public void onEvent(SaveEvent e) throws Throwable {
        initSettings();
        if (mainEntryId != null && switchUponDataSave && (!switchUponDataSaveBeforeAppExitOnly || e.isBeforeExit())) {
            FrontEnd.switchToVisualEntry(mainEntryId);
        }
    }
    
    /* (non-Javadoc)
     * @see bias.event.BeforeHideAppWindowEventListener#onEvent(bias.event.HideAppWindowEvent)
     */
    @Override
    public void onEvent(HideAppWindowEvent e) throws Throwable {
        initSettings();
        if (mainEntryId != null && switchUponAppWindowHide) {
            FrontEnd.switchToVisualEntry(mainEntryId);
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#configure()
     */
    public byte[] configure() throws Throwable {
        initSettings();
        Properties props = PropertiesUtils.deserializeProperties(settings);
        JLabel meLabel = new JLabel(getMessage("main.entry"));
        JComboBox meCB = new JComboBox();
        meCB.addItem(Constants.EMPTY_STR);
        Map<UUID, VisualEntryDescriptor> veds = FrontEnd.getVisualEntryDescriptors();
        for (VisualEntryDescriptor veDescriptor : veds.values()) {
            meCB.addItem(veDescriptor);
        }
        if (mainEntryId != null) {
            meCB.setSelectedItem(veds.get(mainEntryId));
        }
        JCheckBox sudsCB = new JCheckBox(getMessage("switch.upon.data.save"));
        sudsCB.setSelected(switchUponDataSave);
        JCheckBox sudsbaeoCB = new JCheckBox(getMessage("switch.upon.data.save.before.app.exit.only"));
        sudsbaeoCB.setSelected(switchUponDataSave && switchUponDataSaveBeforeAppExitOnly);
        sudsbaeoCB.setEnabled(switchUponDataSave);
        JCheckBox suawhCB = new JCheckBox(getMessage("switch.upon.app.window.hide"));
        suawhCB.setSelected(switchUponAppWindowHide);
        sudsCB.addChangeListener($ -> {
            if (!sudsCB.isSelected()) {
                sudsbaeoCB.setSelected(false);
                sudsbaeoCB.setEnabled(false);
            } else {
                sudsbaeoCB.setEnabled(true);
            }
        });
        JOptionPane.showMessageDialog(FrontEnd.getActiveWindow(), 
            new Component[]{ meLabel, meCB, sudsCB, sudsbaeoCB, suawhCB }, 
            "Configuration", JOptionPane.QUESTION_MESSAGE);
        if (!Validator.isNullOrBlank(meCB.getSelectedItem())) {
            VisualEntryDescriptor ve = (VisualEntryDescriptor) meCB.getSelectedItem();
            mainEntryId = ve.getEntry().getId();
            props.setProperty(PROPERTY_MAIN_ENTRY_UUID, mainEntryId.toString());
        } else {
            mainEntryId = null;
            props.remove(PROPERTY_MAIN_ENTRY_UUID);
        }
        switchUponDataSave = sudsCB.isSelected();
        if (switchUponDataSave) {
            props.setProperty(PROPERTY_SWITCH_UPON_DATA_SAVE, "" + true);
        } else {
            props.remove(PROPERTY_SWITCH_UPON_DATA_SAVE);
        }
        switchUponDataSaveBeforeAppExitOnly = sudsbaeoCB.isSelected();
        if (switchUponDataSaveBeforeAppExitOnly) {
            props.setProperty(PROPERTY_SWITCH_UPON_DATA_SAVE_BEFORE_APP_EXIT_ONLY, "" + true);
        } else {
            props.remove(PROPERTY_SWITCH_UPON_DATA_SAVE_BEFORE_APP_EXIT_ONLY);
        }
        switchUponAppWindowHide = suawhCB.isSelected();
        if (switchUponAppWindowHide) {
            props.setProperty(PROPERTY_SWITCH_UPON_APP_WINDOW_HIDE, "" + true);
        } else {
            props.remove(PROPERTY_SWITCH_UPON_APP_WINDOW_HIDE);
        }
        return PropertiesUtils.serializeProperties(props);
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#skipConfigExport()
     */
    @Override
    public boolean skipConfigExport() {
        return true;
    }

}
