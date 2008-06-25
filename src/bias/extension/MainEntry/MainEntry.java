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
import bias.event.SaveEvent;
import bias.extension.ToolExtension;
import bias.gui.FrontEnd;
import bias.gui.VisualEntryDescriptor;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

public class MainEntry extends ToolExtension implements BeforeSaveEventListener {
    
    private static final String PROPERTY_MAIN_ENTRY_UUID = "MAIN_ENTRY_UUID";
    private static final String PROPERTY_SWITCH_BEFORE_EXIT_ONLY = "SWITCH_BEFORE_EXIT_ONLY";
    
    private UUID mainEntryId;
    
    private boolean switchOnlyBeforeExit;
    
    private byte[] settings;
    
    public MainEntry(byte[] data, byte[] settings) throws Throwable {
        super(data, settings);
        initSettings();
        FrontEnd.addBeforeSaveEventListener(this);
    }
    
    private void initSettings() {
        if (getSettings() != null && !Arrays.equals(getSettings(), settings)) {
            settings = getSettings();
            String idStr = PropertiesUtils.deserializeProperties(settings).getProperty(PROPERTY_MAIN_ENTRY_UUID);
            if (!Validator.isNullOrBlank(idStr)) {
                mainEntryId = UUID.fromString(idStr);
            }
            String sbeoStr = PropertiesUtils.deserializeProperties(settings).getProperty(PROPERTY_SWITCH_BEFORE_EXIT_ONLY);
            if (!Validator.isNullOrBlank(sbeoStr)) {
                switchOnlyBeforeExit = Boolean.valueOf(sbeoStr);
            } else {
                switchOnlyBeforeExit = false;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see bias.event.BeforeSaveEventListener#onEvent(bias.event.SaveEvent)
     */
    public void onEvent(SaveEvent e) throws Throwable {
        initSettings();
        if (mainEntryId != null) {
            if (switchOnlyBeforeExit && !e.isBeforeExit()) {
                return;
            }
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
        JCheckBox sCB = new JCheckBox(getMessage("switch.before.exit.only"));
        sCB.setSelected(switchOnlyBeforeExit);
        JOptionPane.showMessageDialog(FrontEnd.getActiveWindow(), new Component[]{ meLabel, meCB, sCB }, "Configuration", JOptionPane.QUESTION_MESSAGE);
        if (!Validator.isNullOrBlank(meCB.getSelectedItem())) {
            VisualEntryDescriptor ve = (VisualEntryDescriptor) meCB.getSelectedItem();
            mainEntryId = ve.getEntry().getId();
            props.setProperty(PROPERTY_MAIN_ENTRY_UUID, mainEntryId.toString());
        } else {
            mainEntryId = null;
            props.remove(PROPERTY_MAIN_ENTRY_UUID);
        }
        if (sCB.isSelected()) {
            switchOnlyBeforeExit = true;
            props.setProperty(PROPERTY_SWITCH_BEFORE_EXIT_ONLY, "" + true);
        } else {
            switchOnlyBeforeExit = false;
            props.remove(PROPERTY_SWITCH_BEFORE_EXIT_ONLY);
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
