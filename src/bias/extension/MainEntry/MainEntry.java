/**
 * Created on Feb 23, 2008
 */
package bias.extension.MainEntry;

import java.awt.Component;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import bias.Constants;
import bias.annotation.AddOnAnnotation;
import bias.event.EventListener;
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
        version="0.2.1",
        author="R. Kasianenko",
        description = "Allows to select certain entry as main",
        details = "<i>FixedMainEntry</i> extension for Bias is a part<br>" +
                  "of standard \"all-inclusive-delivery-set\" of Bias application.<br>" +
                  "It is provided by <a href=\"http://kion.name/\">R. Kasianenko</a>,<br>" +
                  "an author of Bias application.")
public class MainEntry extends ToolExtension implements EventListener {
    
    private static final String PROPERTY_MAIN_ENTRY_UUID = "MAIN_ENTRY_UUID";
    
    private UUID mainEntryId;
    
    public MainEntry(byte[] data, byte[] settings) {
        super(data, settings);
        String idStr = PropertiesUtils.deserializeProperties(getSettings()).getProperty(PROPERTY_MAIN_ENTRY_UUID);
        if (!Validator.isNullOrBlank(idStr)) {
            mainEntryId = UUID.fromString(idStr);
        }
        FrontEnd.addBeforeSaveEventListener(this);
    }
    
    /* (non-Javadoc)
     * @see bias.event.EventListener#onEvent()
     */
    public void onEvent() throws Throwable {
        if (mainEntryId != null) {
            FrontEnd.switchToVisualEntry(mainEntryId);
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#configure()
     */
    public byte[] configure() throws Throwable {
        Properties props = PropertiesUtils.deserializeProperties(getSettings());
        VisualEntryDescriptor currDescriptor = null;
        JLabel label = new JLabel("Main Entry:");
        JComboBox comboBox = new JComboBox();
        comboBox.addItem(Constants.EMPTY_STR);
        for (VisualEntryDescriptor veDescriptor : FrontEnd.getVisualEntryDescriptors().values()) {
            comboBox.addItem(veDescriptor);
            if (veDescriptor.getEntry().getId().equals(mainEntryId)) {
                currDescriptor = veDescriptor;
            }
        }
        if (currDescriptor != null) {
            comboBox.setSelectedItem(currDescriptor);
        }
        JOptionPane.showMessageDialog(FrontEnd.getActiveWindow(), new Component[]{ label, comboBox }, "Define main entry", JOptionPane.QUESTION_MESSAGE);
        if (!Validator.isNullOrBlank(comboBox.getSelectedItem())) {
            VisualEntryDescriptor ve = (VisualEntryDescriptor) comboBox.getSelectedItem();
            mainEntryId = ve.getEntry().getId();
            props.setProperty(PROPERTY_MAIN_ENTRY_UUID, mainEntryId.toString());
        } else {
            mainEntryId = null;
            props.remove(PROPERTY_MAIN_ENTRY_UUID);
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
