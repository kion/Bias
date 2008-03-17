/**
 * Created on Mar 12, 2008
 */
package bias.extension.Synchronizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import bias.core.BackEnd;
import bias.event.AfterSaveEventListener;
import bias.event.SaveEvent;
import bias.event.StartUpEvent;
import bias.event.StartUpEventListener;
import bias.extension.ToolExtension;
import bias.gui.FrontEnd;
import bias.utils.FormatUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class Synchronizer extends ToolExtension implements AfterSaveEventListener, StartUpEventListener {
    
    private static final String PROPERTY_IMPORT_VERBOSE_MODE = "IMPORT_VERBOSE_MODE";
    private static final String PROPERTY_EXPORT_VERBOSE_MODE = "EXPORT_VERBOSE_MODE";
    private static final String PROPERTY_IMPORT_REQUEST_CONFIRMATIONS = "IMPORT_REQUEST_CONFIRMATIONS";
    private static final String PROPERTY_EXPORT_REQUEST_CONFIRMATIONS = "EXPORT_REQUEST_CONFIRMATIONS";
    private static final String PROPERTY_EXPORT_CONFIGS = "EXPORT_CONFIGS";
    private static final String PROPERTY_IMPORT_CONFIGS = "IMPORT_CONFIGS";
    private static final String SCHEDULE_VALUE_SEPARATOR = "_";
    private static final String PROPERTY_VALUES_SEPARATOR = ",";
    private static final String INVOKATION_TYPE_ON_SAVE = "On Save";
    private static final String INVOKATION_TYPE_ON_STARTUP = "On StartUp";
    private static final String INVOKATION_TYPE_SCHEDULED = "Scheduled";
    
    private static enum TIME_UNIT {
        Minute,
        Hour,
        Day,
        Week
    }
    
    private Properties props;
    
    private boolean verboseExport;
    
    private boolean requestConfirmationsOnExport;
    
    private boolean verboseImport;
    
    private boolean requestConfirmationsOnImport;
    
    private Map<String, Long> exportConfigs;
    
    private Map<String, Long> importConfigs;
    
    private DefaultTableModel exportConfigsModel;
    
    private DefaultTableModel importConfigsModel;
    
    private JTable exportActionsTable;
    
    private JTable importActionsTable;
    
    private ScheduledExecutorService executor;
    
    public Synchronizer(byte[] data, byte[] settings) throws Exception {
        super(data, settings);
        props = PropertiesUtils.deserializeProperties(getSettings());
        String verboseStr = props.getProperty(PROPERTY_EXPORT_VERBOSE_MODE);
        if (!Validator.isNullOrBlank(verboseStr)) {
            verboseExport = Boolean.valueOf(verboseStr);
        } else {
            verboseExport = false;
        }
        String rcStr = props.getProperty(PROPERTY_EXPORT_REQUEST_CONFIRMATIONS);
        if (!Validator.isNullOrBlank(rcStr)) {
            requestConfirmationsOnExport = Boolean.valueOf(rcStr);
        } else {
            requestConfirmationsOnExport = false;
        }
        verboseStr = props.getProperty(PROPERTY_IMPORT_VERBOSE_MODE);
        if (!Validator.isNullOrBlank(verboseStr)) {
            verboseImport = Boolean.valueOf(verboseStr);
        } else {
            verboseImport = false;
        }
        rcStr = props.getProperty(PROPERTY_IMPORT_REQUEST_CONFIRMATIONS);
        if (!Validator.isNullOrBlank(rcStr)) {
            requestConfirmationsOnImport = Boolean.valueOf(rcStr);
        } else {
            requestConfirmationsOnImport = false;
        }
        initActionConfigs(props);
        initSchedules();
        FrontEnd.addAfterSaveEventListener(this);
        FrontEnd.addStartUpEventListener(this);
    }
    
    private void initSchedules() {
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = new ScheduledThreadPoolExecutor(1);
        for (final Entry<String, Long> entry : getExportConfigs().entrySet()) {
            if (entry.getValue() > 0) {
                executor.scheduleAtFixedRate(new Runnable(){
                    public void run() {
                        boolean export = !requestConfirmationsOnExport;
                        if (requestConfirmationsOnExport) {
                            export = JOptionPane.showConfirmDialog(
                                    FrontEnd.getActiveWindow(), 
                                    "Do you want to perform sheduled export '" + entry.getKey() + "' now?", 
                                    "Scheduled export confirmation", 
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                        }
                        if (export) FrontEnd.autoExport(entry.getKey(), false, verboseExport);
                    }
                }, entry.getValue(), entry.getValue(), TimeUnit.MINUTES);
            }
        }
        for (final Entry<String, Long> entry : getImportConfigs().entrySet()) {
            if (entry.getValue() > 0) {
                executor.scheduleAtFixedRate(new Runnable(){
                    public void run() {
                        boolean importt = !requestConfirmationsOnImport;
                        if (requestConfirmationsOnImport) {
                            importt = JOptionPane.showConfirmDialog(
                                    FrontEnd.getActiveWindow(), 
                                    "Do you want to perform sheduled import '" + entry.getKey() + "' now?", 
                                    "Scheduled import confirmation", 
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                        }
                        if (importt) FrontEnd.autoImport(entry.getKey(), false, verboseImport);
                    }
                }, entry.getValue(), entry.getValue(), TimeUnit.MINUTES);
            }
        }
    }

    /* (non-Javadoc)
     * @see bias.event.StartUpEventListener#onEvent(bias.event.StartUpEvent)
     */
    public void onEvent(StartUpEvent e) throws Throwable {
        // TODO [P1] implement
    }

    /* (non-Javadoc)
     * @see bias.event.AfterSaveEventListener#onEvent(bias.event.SaveEvent)
     */
    public void onEvent(final SaveEvent e) throws Throwable {
        // TODO [P1] implement
//        FrontEnd.syncExecute(new Runnable(){
//            public void run() {
//                try {
//                    if (exportConfigs != null && !exportConfigs.isEmpty()) {
//                        Collection<String> exportConfigsToInvoke = null;
//                        if (requestConfirmations) {
//                            int opt = JOptionPane.showConfirmDialog(
//                                    FrontEnd.getActiveWindow(), 
//                                    new Component[] {
//                                        new JLabel("Choose export configurations to invoke now:"),
//                                        new JScrollPane(new JTable(getExportConfigsModel()))
//                                    }, 
//                                    "Invoke export configurations", 
//                                    JOptionPane.OK_CANCEL_OPTION);
//                            if (opt == JOptionPane.OK_OPTION) {
//                                exportConfigsToInvoke = new ArrayList<String>();
//                                int cnt = getExportConfigsModel().getRowCount();
//                                for (int i = 0; i < cnt; i++) {
//                                    if ((Boolean) getExportConfigsModel().getValueAt(i, 0)) {
//                                        String configName = (String) getExportConfigsModel().getValueAt(i, 0);
//                                        exportConfigsToInvoke.add(configName);
//                                    }
//                                }
//                            }
//                        } else {
//                            exportConfigsToInvoke = getExportConfigs().keySet();
//                        }
//                        if (exportConfigsToInvoke != null) {
//                            for (final String configName : exportConfigsToInvoke) {
//                                FrontEnd.autoExport(configName, false, verbose);
//                            }
//                        }
//                    }
//                } catch (Exception ex) {
//                    FrontEnd.displayErrorMessage("Failed to invoke export configuration(s)!", ex);
//                }
//            }
//        });
    }
    
    private Map<String, Long> getExportConfigs() {
        if (exportConfigs == null) {
            exportConfigs = new HashMap<String, Long>();
        }
        return exportConfigs;             
    }
    
    private Map<String, Long> getImportConfigs() {
        if (importConfigs == null) {
            importConfigs = new HashMap<String, Long>();
        }
        return importConfigs;             
    }
    
    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#configure()
     */
    @Override
    public byte[] configure() throws Throwable {
        // TODO [P1] optimization: initialize configuration screen once, then just reuse it
        String oldExportConfigsStr = props.getProperty(PROPERTY_EXPORT_CONFIGS);
        String oldImportConfigsStr = props.getProperty(PROPERTY_IMPORT_CONFIGS);
        
        final JLabel tuL = new JLabel("Choose time unit:");
        final JComboBox tuCB = new JComboBox();
        for (TIME_UNIT tu : TIME_UNIT.values()) {
            tuCB.addItem(tu);
        }
        tuCB.setSelectedIndex(1);
        SpinnerNumberModel sm = new SpinnerNumberModel();
        sm.setMinimum(1);
        sm.setStepSize(1);
        sm.setValue(Integer.valueOf(1));
        final JLabel pL = new JLabel("Choose period:");
        final JSpinner pS = new JSpinner(sm);
        
        final JPanel schedulePanel = new JPanel(new GridLayout(4, 1));
        schedulePanel.add(tuL);
        schedulePanel.add(tuCB);
        schedulePanel.add(pL);
        schedulePanel.add(pS);
        
        final JLabel typeL = new JLabel("Choose invokation type:");
        
        final JComboBox typeCB = new JComboBox();
        typeCB.addItem(INVOKATION_TYPE_SCHEDULED);
        typeCB.setSelectedIndex(0);
        typeCB.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if (typeCB.getSelectedItem().equals(INVOKATION_TYPE_SCHEDULED)) {
                    tuL.setEnabled(true);
                    tuCB.setEnabled(true);
                    pL.setEnabled(true);
                    pS.setEnabled(true);
                } else {
                    tuL.setEnabled(false);
                    tuCB.setEnabled(false);
                    pL.setEnabled(false);
                    pS.setEnabled(false);
                }
            }
        });
        
        final JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.add(typeL, BorderLayout.NORTH);
        configPanel.add(typeCB, BorderLayout.CENTER);
        configPanel.add(schedulePanel, BorderLayout.SOUTH);
        
        JButton addExportConfigButt = new JButton("Add export action...");
        addExportConfigButt.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    JComboBox configsCB = getConfigChooser(BackEnd.getInstance().getExportConfigurations(), getExportConfigs().keySet());
                    if (configsCB.getItemCount() > 0) {
                        typeCB.removeItem(INVOKATION_TYPE_ON_STARTUP);
                        typeCB.addItem(INVOKATION_TYPE_ON_SAVE);
                        int opt = JOptionPane.showConfirmDialog(
                                FrontEnd.getActiveWindow(), 
                                new Component[]{
                                    new JLabel("Choose export configuration:"),
                                    configsCB,
                                    configPanel
                                },
                                "Add export action",
                                JOptionPane.OK_CANCEL_OPTION);
                        if (opt == JOptionPane.OK_OPTION) {
                            String config = (String) configsCB.getSelectedItem();
                            long period = typeCB.getSelectedItem().equals(INVOKATION_TYPE_ON_SAVE) ? 0 : ((Number) pS.getValue()).longValue();
                            if (period != 0) {
                                switch ((TIME_UNIT) tuCB.getSelectedItem()) {
                                case Minute:
                                    break;
                                case Hour:
                                    period *= 60;
                                    break;
                                case Day:
                                    period *= 60 * 24;
                                    break;
                                case Week:
                                    period *= 60 * 24 * 7;
                                    break;
                                }
                            }
                            getExportConfigs().put(config, period);
                            getExportConfigsModel().addRow(new Object[] { config, period == 0 ? INVOKATION_TYPE_ON_SAVE : "Every " + FormatUtils.formatTimeDuration(period * 60 * 1000) });
                        }
                    }
                } catch (Exception ex) {
                    FrontEnd.displayErrorMessage("Failed to add export configuration!", ex);
                }
            }
        });
        JButton removeExportConfigButt = new JButton("Remove export action...");
        removeExportConfigButt.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int idx = getExportActionsTable().getSelectedRow();
                if (idx != -1) {
                    getExportConfigs().remove(getExportActionsTable().getValueAt(idx, 0));
                    getExportConfigsModel().removeRow(idx);
                }
            }
        });
        
        JButton addImportConfigButt = new JButton("Add import action...");
        addImportConfigButt.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    JComboBox configsCB = getConfigChooser(BackEnd.getInstance().getImportConfigurations(), getImportConfigs().keySet());
                    if (configsCB.getItemCount() > 0) {
                        typeCB.removeItem(INVOKATION_TYPE_ON_SAVE);
                        typeCB.addItem(INVOKATION_TYPE_ON_STARTUP);
                        int opt = JOptionPane.showConfirmDialog(
                                FrontEnd.getActiveWindow(), 
                                new Component[]{
                                    new JLabel("Choose import configuration:"),
                                    configsCB,
                                    configPanel
                                },
                                "Add import action",
                                JOptionPane.OK_CANCEL_OPTION);
                        if (opt == JOptionPane.OK_OPTION) {
                            String config = (String) configsCB.getSelectedItem();
                            long period = typeCB.getSelectedItem().equals(INVOKATION_TYPE_ON_STARTUP) ? 0 : ((Number) pS.getValue()).longValue();
                            if (period != 0) {
                                switch ((TIME_UNIT) tuCB.getSelectedItem()) {
                                case Minute:
                                    break;
                                case Hour:
                                    period *= 60;
                                    break;
                                case Day:
                                    period *= 60 * 24;
                                    break;
                                case Week:
                                    period *= 60 * 24 * 7;
                                    break;
                                }
                            }
                            getImportConfigs().put(config, period);
                            getImportConfigsModel().addRow(new Object[] { config, period == 0 ? INVOKATION_TYPE_ON_STARTUP : "Every " + FormatUtils.formatTimeDuration(period * 60 * 1000) });
                        }
                    }
                } catch (Exception ex) {
                    FrontEnd.displayErrorMessage("Failed to add import configuration!", ex);
                }
            }
        });
        JButton removeImportConfigButt = new JButton("Remove import action...");
        removeImportConfigButt.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int idx = getImportActionsTable().getSelectedRow();
                if (idx != -1) {
                    getImportConfigs().remove(getImportActionsTable().getValueAt(idx, 0));
                    getImportConfigsModel().removeRow(idx);
                }
            }
        });
        
        JCheckBox exportRcCB = new JCheckBox("Request action-confirmations from user");
        exportRcCB.setSelected(requestConfirmationsOnExport);
        JCheckBox exportVCB = new JCheckBox("Verbose mode");
        exportVCB.setSelected(verboseExport);
        
        JCheckBox importRcCB = new JCheckBox("Request action-confirmations from user");
        importRcCB.setSelected(requestConfirmationsOnImport);
        JCheckBox importVCB = new JCheckBox("Verbose mode");
        importVCB.setSelected(verboseImport);
        
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(new JLabel("Currently configured exports:"), BorderLayout.NORTH);
        p1.add(new JScrollPane(getExportActionsTable()), BorderLayout.CENTER);
        JPanel pb1 = new JPanel(new GridLayout(1, 2));
        pb1.add(addExportConfigButt);
        pb1.add(removeExportConfigButt);
        JPanel pcb1 = new JPanel(new GridLayout(2, 1));
        pcb1.add(exportVCB);
        pcb1.add(exportRcCB);
        JPanel pp1 = new JPanel(new BorderLayout());
        pp1.add(pb1, BorderLayout.NORTH);
        pp1.add(pcb1, BorderLayout.CENTER);
        p1.add(pp1, BorderLayout.SOUTH);
        
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JLabel("Currently configured imports:"), BorderLayout.NORTH);
        p2.add(new JScrollPane(getImportActionsTable()), BorderLayout.CENTER);
        JPanel pb2 = new JPanel(new GridLayout(1, 2));
        pb2.add(addImportConfigButt);
        pb2.add(removeImportConfigButt);
        JPanel pcb2 = new JPanel(new GridLayout(2, 1));
        pcb2.add(importVCB);
        pcb2.add(importRcCB);
        JPanel pp2 = new JPanel(new BorderLayout());
        pp2.add(pb2, BorderLayout.NORTH);
        pp2.add(pcb2, BorderLayout.CENTER);
        p2.add(pp2, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(p1, BorderLayout.WEST);
        panel.add(p2, BorderLayout.EAST);
        
        JOptionPane.showMessageDialog(
                FrontEnd.getActiveWindow(),
                panel,
                "Configuration", 
                JOptionPane.QUESTION_MESSAGE);

        if (getExportConfigs().isEmpty()) {
            props.remove(PROPERTY_EXPORT_CONFIGS);
        } else {
            StringBuffer sb = new StringBuffer();
            Iterator<Entry<String, Long>> it = getExportConfigs().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Long> entry = it.next();
                sb.append(entry.getKey() + SCHEDULE_VALUE_SEPARATOR + entry.getValue());
                if (it.hasNext()) {
                    sb.append(PROPERTY_VALUES_SEPARATOR);
                }
            }
            props.setProperty(PROPERTY_EXPORT_CONFIGS, sb.toString());
        }
        
        if (getImportConfigs().isEmpty()) {
            props.remove(PROPERTY_IMPORT_CONFIGS);
        } else {
            StringBuffer sb = new StringBuffer();
            Iterator<Entry<String, Long>> it = getImportConfigs().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Long> entry = it.next();
                sb.append(entry.getKey() + SCHEDULE_VALUE_SEPARATOR + entry.getValue());
                if (it.hasNext()) {
                    sb.append(PROPERTY_VALUES_SEPARATOR);
                }
            }
            props.setProperty(PROPERTY_IMPORT_CONFIGS, sb.toString());
        }
        
        if (exportRcCB.isSelected()) {
            requestConfirmationsOnExport = true;
            props.setProperty(PROPERTY_EXPORT_REQUEST_CONFIRMATIONS, "" + true);
        } else {
            requestConfirmationsOnExport = false;
            props.remove(PROPERTY_EXPORT_REQUEST_CONFIRMATIONS);
        }
        if (exportVCB.isSelected()) {
            verboseExport = true;
            props.setProperty(PROPERTY_EXPORT_VERBOSE_MODE, "" + true);
        } else {
            verboseExport = false;
            props.remove(PROPERTY_EXPORT_VERBOSE_MODE);
        }
        if (importRcCB.isSelected()) {
            requestConfirmationsOnImport = true;
            props.setProperty(PROPERTY_IMPORT_REQUEST_CONFIRMATIONS, "" + true);
        } else {
            requestConfirmationsOnImport = false;
            props.remove(PROPERTY_IMPORT_REQUEST_CONFIRMATIONS);
        }
        if (importVCB.isSelected()) {
            verboseImport = true;
            props.setProperty(PROPERTY_IMPORT_VERBOSE_MODE, "" + true);
        } else {
            verboseImport = false;
            props.remove(PROPERTY_IMPORT_VERBOSE_MODE);
        }

        String exportConfigsStr = props.getProperty(PROPERTY_EXPORT_CONFIGS);
        String importConfigsStr = props.getProperty(PROPERTY_IMPORT_CONFIGS);
        boolean unchanged = oldExportConfigsStr == null ? exportConfigsStr == null : oldExportConfigsStr.equals(exportConfigsStr);
        if (unchanged) unchanged = oldImportConfigsStr == null ? importConfigsStr == null : oldImportConfigsStr.equals(importConfigsStr);
        if (!unchanged) {
            initSchedules();
        }
        
        return PropertiesUtils.serializeProperties(props);
    }
    
    private void initActionConfigs(Properties props) throws Exception {
        String exportConfigsStr = props.getProperty(PROPERTY_EXPORT_CONFIGS);
        if (!Validator.isNullOrBlank(exportConfigsStr)) {
            String[] configs = exportConfigsStr.split(PROPERTY_VALUES_SEPARATOR);
            for (String config : configs) {
                String[] cfg = config.split(SCHEDULE_VALUE_SEPARATOR);
                getExportConfigs().put(cfg[0], Long.valueOf(cfg[1]));
            }
        }
        populateConfigsModel(getExportConfigsModel(), getExportConfigs());

        String importConfigsStr = props.getProperty(PROPERTY_IMPORT_CONFIGS);
        if (!Validator.isNullOrBlank(importConfigsStr)) {
            String[] configs = importConfigsStr.split(PROPERTY_VALUES_SEPARATOR);
            for (String config : configs) {
                String[] cfg = config.split(SCHEDULE_VALUE_SEPARATOR);
                getImportConfigs().put(cfg[0], Long.valueOf(cfg[1]));
            }
        }
        populateConfigsModel(getImportConfigsModel(), getImportConfigs());
    }
    
    private JComboBox getConfigChooser(Collection<String> configs, Collection<String> excludes) {
        JComboBox cb = new JComboBox();
        for (String config : configs) {
            if (!excludes.contains(config)) {
                cb.addItem(config);
            }
        }
        if (cb.getItemCount() > 0) cb.setSelectedIndex(0);
        return cb;
    }
    
    private void populateConfigsModel(DefaultTableModel model, Map<String, Long> configs) throws Exception {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        for (Entry<String, Long> config : configs.entrySet()) {
            String name = config.getKey();
            String period = config.getValue() == 0 ? INVOKATION_TYPE_ON_SAVE : "Every " + FormatUtils.formatTimeDuration(config.getValue() * 60 * 1000);
            model.addRow(new Object[]{ name, period });
        }
    }
    
    private JTable getExportActionsTable() {
        if (exportActionsTable == null) {
            exportActionsTable = new JTable(getExportConfigsModel());;
            exportActionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        return exportActionsTable;
    }
    
    private JTable getImportActionsTable() {
        if (importActionsTable == null) {
            importActionsTable = new JTable(getImportConfigsModel());;
            importActionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        return importActionsTable;
    }
    
    private DefaultTableModel getExportConfigsModel() {
        if (exportConfigsModel == null) {
            exportConfigsModel = createConfigsModel();
        }
        return exportConfigsModel;
    }
    
    private DefaultTableModel getImportConfigsModel() {
        if (importConfigsModel == null) {
            importConfigsModel = createConfigsModel();
        }
        return importConfigsModel;
    }
    
    private DefaultTableModel createConfigsModel() {
        DefaultTableModel model = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        model.addColumn("Configuration");
        model.addColumn("Schedule");
        return model;
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
