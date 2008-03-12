/**
 * Created on Mar 12, 2008
 */
package bias.extension.Synchronizer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import bias.Constants;
import bias.core.BackEnd;
import bias.event.AfterSaveEventListener;
import bias.event.SaveEvent;
import bias.extension.ToolExtension;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class Synchronizer extends ToolExtension implements AfterSaveEventListener {

    private static final String PROPERTY_VERBOSE_MODE = "VERBOSE_MODE";
    private static final String PROPERTY_EXPORT_BEFORE_EXIT_ONLY = "EXPORT_BEFORE_EXIT_ONLY";
    private static final String PROPERTY_EXPORT_CONFIGS = "EXPORT_CONFIGS";
    private static final String PROPERTY_VALUES_SEPARATOR = ",";
    
    private boolean verbose;
    
    private boolean exportBeforeExitOnly;
    
    private Collection<String> exportConfigs;
    
    public Synchronizer(byte[] data, byte[] settings) {
        super(data, settings);
        Properties props = PropertiesUtils.deserializeProperties(getSettings());
        String sbeoStr = props.getProperty(PROPERTY_EXPORT_BEFORE_EXIT_ONLY);
        if (!Validator.isNullOrBlank(sbeoStr)) {
            exportBeforeExitOnly = Boolean.valueOf(sbeoStr);
        } else {
            exportBeforeExitOnly = false;
        }
        String verboseStr = props.getProperty(PROPERTY_VERBOSE_MODE);
        if (!Validator.isNullOrBlank(verboseStr)) {
            verbose = Boolean.valueOf(verboseStr);
        } else {
            verbose = false;
        }
        String exportConfigsStr = props.getProperty(PROPERTY_EXPORT_CONFIGS);
        if (!Validator.isNullOrBlank(exportConfigsStr)) {
            String[] configs = exportConfigsStr.split(PROPERTY_VALUES_SEPARATOR);
            for (String config : configs) {
                getExportConfigs().add(config);
            }
        }
        FrontEnd.addAfterSaveEventListener(this);
    }

    /* (non-Javadoc)
     * @see bias.event.AfterSaveEventListener#onEvent(bias.event.SaveEvent)
     */
    public void onEvent(SaveEvent e) throws Throwable {
        if (exportConfigs != null && !exportConfigs.isEmpty()) {
            if (exportBeforeExitOnly && !e.isBeforeExit()) {
                return;
            }
            for (String configName : getExportConfigs()) {
                FrontEnd.autoExport(configName, false, verbose);
            }
        }
    }
    
    private Collection<String> getExportConfigs() {
        if (exportConfigs == null) {
            exportConfigs = new ArrayList<String>();
        }
        return exportConfigs;             
    }
    
    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#configure()
     */
    @Override
    public byte[] configure() throws Throwable {
        Properties props = PropertiesUtils.deserializeProperties(getSettings());
        String exportConfigsStr = props.getProperty(PROPERTY_EXPORT_CONFIGS);
        if (!Validator.isNullOrBlank(exportConfigsStr)) {
            String[] configs = exportConfigsStr.split(PROPERTY_VALUES_SEPARATOR);
            for (String config : configs) {
                getExportConfigs().add(config);
            }
        }
        DefaultTableModel exportConfigsModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return mColIndex == 0 ? true : false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else {
                    return super.getColumnClass(columnIndex);
                }
            }
        };
        exportConfigsModel.addColumn(Constants.EMPTY_STR);
        exportConfigsModel.addColumn("Configuration");
        for (String configName : BackEnd.getInstance().getExportConfigurations()) {
            exportConfigsModel.addRow(new Object[]{ getExportConfigs().contains(configName), configName });
        }
        JCheckBox sbeoCB = new JCheckBox("Export only before exit");
        sbeoCB.setSelected(exportBeforeExitOnly);
        JCheckBox vCB = new JCheckBox("Verbose mode");
        vCB.setSelected(verbose);
        
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                new Component[] {
                    new JLabel("Choose export configurations to invoke on save:"),
                    new JScrollPane(new JTable(exportConfigsModel)),
                    sbeoCB,
                    vCB
                }, 
                "Configuration", 
                JOptionPane.QUESTION_MESSAGE);

        getExportConfigs().clear();
        StringBuffer sb = new StringBuffer();
        int cnt = exportConfigsModel.getRowCount();
        for (int i = 0; i < cnt; i++) {
            if ((Boolean) exportConfigsModel.getValueAt(i, 0)) {
                String configName = (String) exportConfigsModel.getValueAt(i, 1);
                getExportConfigs().add(configName);
                sb.append(configName);
                if (i < cnt - 1) {
                    sb.append(PROPERTY_VALUES_SEPARATOR);
                }
            }
        }
        if (!Validator.isNullOrBlank(sb)) {
            props.setProperty(PROPERTY_EXPORT_CONFIGS, sb.toString());
        } else {
            props.remove(PROPERTY_EXPORT_CONFIGS);
        }
        if (sbeoCB.isSelected()) {
            exportBeforeExitOnly = true;
            props.setProperty(PROPERTY_EXPORT_BEFORE_EXIT_ONLY, "" + true);
        } else {
            exportBeforeExitOnly = false;
            props.remove(PROPERTY_EXPORT_BEFORE_EXIT_ONLY);
        }
        if (vCB.isSelected()) {
            verbose = true;
            props.setProperty(PROPERTY_VERBOSE_MODE, "" + true);
        } else {
            verbose = false;
            props.remove(PROPERTY_VERBOSE_MODE);
        }
        return PropertiesUtils.serializeProperties(props);
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#skipDataExport()
     */
    @Override
    public boolean skipDataExport() {
        return true;
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#skipConfigExport()
     */
    @Override
    public boolean skipConfigExport() {
        return true;
    }

}
