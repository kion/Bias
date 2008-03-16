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
    
    // TODO [P1] implement import-handling
    
    // TODO [P1] implement schedule-based events firing (for both import and export)
    
    private static final String PROPERTY_VERBOSE_MODE = "VERBOSE_MODE";
    private static final String PROPERTY_EXPORT_BEFORE_EXIT_ONLY = "EXPORT_BEFORE_EXIT_ONLY";
    private static final String PROPERTY_REQUEST_CONFIRMATIONS = "REQUEST_CONFIRMATIONS";
    private static final String PROPERTY_EXPORT_CONFIGS = "EXPORT_CONFIGS";
    private static final String PROPERTY_VALUES_SEPARATOR = ",";
    
    private boolean verbose;
    
    private boolean exportBeforeExitOnly;
    
    private boolean requestConfirmations;
    
    private Collection<String> exportConfigs;
    
    private DefaultTableModel exportConfigsModel;
    
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
        String rcStr = props.getProperty(PROPERTY_REQUEST_CONFIRMATIONS);
        if (!Validator.isNullOrBlank(rcStr)) {
            requestConfirmations = Boolean.valueOf(rcStr);
        } else {
            requestConfirmations = false;
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
    public void onEvent(final SaveEvent e) throws Throwable {
        FrontEnd.syncExecute(new Runnable(){
            public void run() {
                try {
                    if (exportConfigs != null && !exportConfigs.isEmpty()) {
                        if (exportBeforeExitOnly && !e.isBeforeExit()) {
                            return;
                        }
                    }
                    Collection<String> exportConfigsToInvoke = null;
                    if (requestConfirmations) {
                        populateExportConfigsModel();
                        int opt = JOptionPane.showConfirmDialog(
                                FrontEnd.getActiveWindow(), 
                                new Component[] {
                                    new JLabel("Choose export configurations to invoke now:"),
                                    new JScrollPane(new JTable(getExportConfigsModel()))
                                }, 
                                "Invoke export configurations", 
                                JOptionPane.OK_CANCEL_OPTION);
                        if (opt == JOptionPane.OK_OPTION) {
                            exportConfigsToInvoke = new ArrayList<String>();
                            int cnt = getExportConfigsModel().getRowCount();
                            for (int i = 0; i < cnt; i++) {
                                if ((Boolean) getExportConfigsModel().getValueAt(i, 0)) {
                                    String configName = (String) getExportConfigsModel().getValueAt(i, 1);
                                    exportConfigsToInvoke.add(configName);
                                }
                            }
                        }
                    } else {
                        exportConfigsToInvoke = getExportConfigs();
                    }
                    if (exportConfigsToInvoke != null) {
                        for (final String configName : exportConfigsToInvoke) {
                            FrontEnd.autoExport(configName, false, verbose);
                        }
                    }
                } catch (Exception ex) {
                    FrontEnd.displayErrorMessage("Failed to invoke export configuration(s)!", ex);
                }
            }
        });
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
        populateExportConfigsModel();
        JCheckBox sbeoCB = new JCheckBox("Export only before exit");
        sbeoCB.setSelected(exportBeforeExitOnly);
        JCheckBox rcCB = new JCheckBox("Request action-confirmations from user");
        rcCB.setSelected(requestConfirmations);
        JCheckBox vCB = new JCheckBox("Verbose mode");
        vCB.setSelected(verbose);
        
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(), 
                new Component[] {
                    new JLabel("Choose export configurations to invoke on save:"),
                    new JScrollPane(new JTable(getExportConfigsModel())),
                    sbeoCB,
                    rcCB,
                    vCB
                }, 
                "Configuration", 
                JOptionPane.QUESTION_MESSAGE);

        getExportConfigs().clear();
        StringBuffer sb = new StringBuffer();
        int cnt = getExportConfigsModel().getRowCount();
        for (int i = 0; i < cnt; i++) {
            if ((Boolean) getExportConfigsModel().getValueAt(i, 0)) {
                String configName = (String) getExportConfigsModel().getValueAt(i, 1);
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
        if (rcCB.isSelected()) {
            requestConfirmations = true;
            props.setProperty(PROPERTY_REQUEST_CONFIRMATIONS, "" + true);
        } else {
            requestConfirmations = false;
            props.remove(PROPERTY_REQUEST_CONFIRMATIONS);
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
    
    private void populateExportConfigsModel() throws Exception {
        while (getExportConfigsModel().getRowCount() > 0) {
            getExportConfigsModel().removeRow(0);
        }
        for (String configName : BackEnd.getInstance().getExportConfigurations()) {
            getExportConfigsModel().addRow(new Object[]{ getExportConfigs().contains(configName), configName });
        }
    }
    
    private DefaultTableModel getExportConfigsModel() {
        if (exportConfigsModel == null) {
            exportConfigsModel = new DefaultTableModel() {
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
        }
        return exportConfigsModel;
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
