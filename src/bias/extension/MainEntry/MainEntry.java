/**
 * Created on Feb 23, 2008
 */
package bias.extension.MainEntry;

import java.awt.Component;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import bias.Constants;
import bias.annotation.AddOnAnnotation;
import bias.event.BeforeSaveEventListener;
import bias.event.SaveEvent;
import bias.extension.ToolExtension;
import bias.extension.ToolRepresentation;
import bias.gui.FrontEnd;
import bias.gui.VisualEntryDescriptor;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.3.1",
        author="R. Kasianenko",
        description = "Allows to select certain entry as main",
        details = "<i>FixedMainEntry</i> extension for Bias is a part<br>" +
                  "of standard \"all-inclusive-delivery-set\" of Bias application.<br>" +
                  "It is provided by <a href=\"http://kion.name/\">R. Kasianenko</a>,<br>" +
                  "an author of Bias application.")
public class MainEntry extends ToolExtension implements BeforeSaveEventListener {
    
    private static final String PROPERTY_MAIN_ENTRY_UUID = "MAIN_ENTRY_UUID";
    private static final String PROPERTY_SWITCH_BEFORE_EXIT_ONLY = "SWITCH_BEFORE_EXIT_ONLY";
    
    private UUID mainEntryId;
    
    private boolean switchOnlyBeforeExit;
    
    public MainEntry(byte[] data, byte[] settings) {
        super(data, settings);
        String idStr = PropertiesUtils.deserializeProperties(getSettings()).getProperty(PROPERTY_MAIN_ENTRY_UUID);
        if (!Validator.isNullOrBlank(idStr)) {
            mainEntryId = UUID.fromString(idStr);
        }
        String sbeoStr = PropertiesUtils.deserializeProperties(getSettings()).getProperty(PROPERTY_SWITCH_BEFORE_EXIT_ONLY);
        if (!Validator.isNullOrBlank(sbeoStr)) {
            switchOnlyBeforeExit = Boolean.valueOf(sbeoStr);
        } else {
            switchOnlyBeforeExit = false;
        }
        FrontEnd.addBeforeSaveEventListener(this);
    }
    
    /* (non-Javadoc)
     * @see bias.event.BeforeSaveEventListener#onEvent(bias.event.SaveEvent)
     */
    public void onEvent(SaveEvent e) throws Throwable {
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
        Properties props = PropertiesUtils.deserializeProperties(getSettings());
        VisualEntryDescriptor currDescriptor = null;
        JLabel meLabel = new JLabel("Main entry (to switch to before save):");
        JComboBox meCB = new JComboBox();
        meCB.addItem(Constants.EMPTY_STR);
        for (VisualEntryDescriptor veDescriptor : FrontEnd.getVisualEntryDescriptors().values()) {
            meCB.addItem(veDescriptor);
            if (veDescriptor.getEntry().getId().equals(mainEntryId)) {
                currDescriptor = veDescriptor;
            }
        }
        if (currDescriptor != null) {
            meCB.setSelectedItem(currDescriptor);
        }
        JCheckBox sCB = new JCheckBox("Switch to main entry only before exit");
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
     * @see bias.extension.ToolExtension#getRepresentation()
     */
    @Override
    public ToolRepresentation getRepresentation() {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#skipConfigExport()
     */
    @Override
    public boolean skipConfigExport() {
        return true;
    }

}
