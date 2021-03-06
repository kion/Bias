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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
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

import bias.Constants;
import bias.core.BackEnd;
import bias.event.AfterSaveEventListener;
import bias.event.SaveEvent;
import bias.event.StartUpEvent;
import bias.event.StartUpEventListener;
import bias.extension.ToolExtension;
import bias.gui.FrontEnd;
import bias.utils.CommonUtils;
import bias.utils.FormatUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class Synchronizer extends ToolExtension implements AfterSaveEventListener, StartUpEventListener {
    
    // TODO [P2] implement scheduled actions performed periodically at fixed day time (e.g. every day at 18:00)
    
    // TODO [P2] implement import/export configs linking feature (to avoid redundant transfers)
    //           it should be checksum based: 
    //           if export config gave the same checksum as it's linked import config - do nothing, and vice versa
    
    // TODO [P2] optionally, show pop-up messages about import that just have been performed 
    //           (useful for background imports in 'minimized to system tray' mode) 
    
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
    
    private byte[] settings;
    
    private boolean verboseExport;
    
    private boolean requestConfirmationsOnExport;
    
    private boolean verboseImport;
    
    private boolean requestConfirmationsOnImport;
    
    private Map<String, Collection<Long>> exportConfigs;
    
    private Map<String, Collection<Long>> importConfigs;
    
    private Map<String, Long> exportConfigsValues;
    
    private Map<String, Long> importConfigsValues;
    
    private DefaultTableModel exportConfigsModel;
    
    private DefaultTableModel importConfigsModel;
    
    private JTable exportActionsTable;
    
    private JTable importActionsTable;
    
    private ScheduledExecutorService executor;
    
    public Synchronizer(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        initSettings();
        FrontEnd.addAfterSaveEventListener(this);
        FrontEnd.addStartUpEventListener(this);
    }
    
    private void initSettings() throws Exception {
        props = new Properties();
        if (getSettings() != null && !Arrays.equals(getSettings(), settings)) {
            settings = getSettings();
            props.putAll(PropertiesUtils.deserializeProperties(settings));
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
        }
    }

    private void initSchedules() {
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = new ScheduledThreadPoolExecutor(1);
        for (final Entry<String, Collection<Long>> entry : getExportConfigs().entrySet()) {
            for (Long period : entry.getValue()) {
                if (period > 0) {
                    executor.scheduleAtFixedRate(new Runnable(){
                        public void run() {
                            try {
                                boolean export = !requestConfirmationsOnExport;
                                if (requestConfirmationsOnExport) {
                                    export = JOptionPane.showConfirmDialog(
                                            FrontEnd.getActiveWindow(), 
                                            "Do you want to perform sheduled export '" + entry.getKey() + "' now?", 
                                            "Scheduled export confirmation", 
                                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                                }
                                if (!BackEnd.getInstance().getExportConfigurations().contains(entry.getKey())) {
                                    throw new Exception("Export configuration does not exist anymore!");
                                }
                                if (export) FrontEnd.autoExport(entry.getKey(), false, verboseExport);
                            } catch (Throwable t) {
                                FrontEnd.displayStatusBarErrorMessage("Failed to perform export ('" + entry.getKey() + "')! " + CommonUtils.getFailureDetails(t));
                            }
                        }
                    }, period, period, TimeUnit.MINUTES);
                }
            }
        }
        for (final Entry<String, Collection<Long>> entry : getImportConfigs().entrySet()) {
            for (Long period : entry.getValue()) {
                if (period > 0) {
                    executor.scheduleAtFixedRate(new Runnable(){
                        public void run() {
                            try {
                                boolean importt = !requestConfirmationsOnImport;
                                if (requestConfirmationsOnImport) {
                                    importt = JOptionPane.showConfirmDialog(
                                            FrontEnd.getActiveWindow(), 
                                            "Do you want to perform sheduled import '" + entry.getKey() + "' now?", 
                                            "Scheduled import confirmation", 
                                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                                }
                                if (!BackEnd.getInstance().getImportConfigurations().contains(entry.getKey())) { 
                                    throw new Exception("Import configuration does not exist anymore!");
                                }
                                if (importt) FrontEnd.autoImport(entry.getKey(), false, verboseImport);
                            } catch (Throwable t) {
                                FrontEnd.displayStatusBarErrorMessage("Failed to perform import ('" + entry.getKey() + "')! " + CommonUtils.getFailureDetails(t));
                            }
                        }
                    }, period, period, TimeUnit.MINUTES);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see bias.event.StartUpEventListener#onEvent(bias.event.StartUpEvent)
     */
    public void onEvent(StartUpEvent e) throws Throwable {
        FrontEnd.syncExecute(new Runnable(){
            public void run() {
                try {
                    initSettings();
                    if (importConfigs != null && !importConfigs.isEmpty()) {
                        DefaultTableModel model = getConfirmImportPopulatedConfigsModel();
                        if (model.getRowCount() > 0) {
                            boolean importt = !requestConfirmationsOnImport;
                            if (requestConfirmationsOnImport) {
                                int opt = JOptionPane.showConfirmDialog(
                                        FrontEnd.getActiveWindow(), 
                                        new Component[] {
                                            new JLabel("Choose import actions to invoke now:"),
                                            new JScrollPane(new JTable(model))
                                        }, 
                                        "Invoke import actions", 
                                        JOptionPane.OK_CANCEL_OPTION);
                                if (opt == JOptionPane.OK_OPTION) {
                                    importt = true;
                                }
                            }
                            if (importt) {
                                int cnt = model.getRowCount();
                                for (int i = 0; i < cnt; i++) {
                                    if ((Boolean) model.getValueAt(i, 0)) {
                                        String configName = (String) model.getValueAt(i, 1);
                                        if (!BackEnd.getInstance().getImportConfigurations().contains(configName)) { 
                                            FrontEnd.displayStatusBarErrorMessage("Failed to invoke import action '" + configName + "': non-existing configuration!"); // FIXME [P1] should be fixed, not ignored
                                        } else {
                                            FrontEnd.autoImport(configName, false, verboseImport);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    FrontEnd.displayErrorMessage("Failed to invoke import action(s): " + CommonUtils.getFailureDetails(ex), ex);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see bias.event.AfterSaveEventListener#onEvent(bias.event.SaveEvent)
     */
    public void onEvent(final SaveEvent e) throws Throwable {
        FrontEnd.syncExecute(new Runnable(){
            public void run() {
                try {
                    initSettings();
                    if (exportConfigs != null && !exportConfigs.isEmpty()) {
                        DefaultTableModel model = getConfirmExportPopulatedConfigsModel();
                        if (model.getRowCount() > 0) {
                            boolean export = !requestConfirmationsOnExport;
                            if (requestConfirmationsOnExport) {
                                int opt = JOptionPane.showConfirmDialog(
                                        FrontEnd.getActiveWindow(), 
                                        new Component[] {
                                            new JLabel("Choose export actions to invoke now:"),
                                            new JScrollPane(new JTable(model))
                                        }, 
                                        "Invoke export actions", 
                                        JOptionPane.OK_CANCEL_OPTION);
                                if (opt == JOptionPane.OK_OPTION) {
                                    export = true;
                                }
                            }
                            if (export) {
                                int cnt = model.getRowCount();
                                for (int i = 0; i < cnt; i++) {
                                    if ((Boolean) model.getValueAt(i, 0)) {
                                        String configName = (String) model.getValueAt(i, 1);
                                        if (!BackEnd.getInstance().getExportConfigurations().contains(configName)) { 
                                            FrontEnd.displayStatusBarErrorMessage("Failed to invoke export action '" + configName + "': non-existing configuration!"); // FIXME [P1] should be fixed, not ignored
                                        } else {
                                            FrontEnd.autoExport(configName, false, verboseExport);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    FrontEnd.displayErrorMessage("Failed to invoke export action(s): " + CommonUtils.getFailureDetails(ex), ex);
                }
            }
        });
    }
    
    private Map<String, Collection<Long>> getExportConfigs() {
        if (exportConfigs == null) {
            exportConfigs = new HashMap<String, Collection<Long>>();
        }
        return exportConfigs;             
    }
    
    private Map<String, Collection<Long>> getImportConfigs() {
        if (importConfigs == null) {
            importConfigs = new HashMap<String, Collection<Long>>();
        }
        return importConfigs;             
    }
    
    private Map<String, Long> getExportConfigsValues() {
        if (exportConfigsValues == null) {
            exportConfigsValues = new HashMap<String, Long>();
        }
        return exportConfigsValues;             
    }
    
    private Map<String, Long> getImportConfigsValues() {
        if (importConfigsValues == null) {
            importConfigsValues = new HashMap<String, Long>();
        }
        return importConfigsValues;             
    }
    
    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#configure()
     */
    @Override
    public byte[] configure() throws Throwable {
        // TODO [P2] optimization: initialize configuration screen once, then just reuse it
        initSettings();
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
                    JComboBox configsCB = getConfigChooser(BackEnd.getInstance().getExportConfigurations());
                    if (configsCB.getItemCount() > 0) {
                        typeCB.removeItem(INVOKATION_TYPE_ON_STARTUP);
                        if (typeCB.getItemCount() == 1) {
                            typeCB.addItem(INVOKATION_TYPE_ON_SAVE);
                        }
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
                            Collection<Long> periods = getExportConfigs().get(config);
                            if (periods == null) {
                                periods = new ArrayList<Long>();
                                getExportConfigs().put(config, periods);
                            }
                            periods.add(period);
                            String formatted = "Every " + FormatUtils.formatTimeDuration(period * 60 * 1000);
                            getExportConfigsModel().addRow(new Object[] { config, period == 0 ? INVOKATION_TYPE_ON_SAVE : formatted });
                            if (period != 0) {
                                getExportConfigsValues().put(config+formatted, period);
                            }
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
                    String config = (String) getExportActionsTable().getValueAt(idx, 0);
                    Object obj = getExportActionsTable().getValueAt(idx, 1);
                    if (INVOKATION_TYPE_ON_SAVE.equals(obj)) {
                        obj = 0L;
                    } else {
                        obj = getExportConfigsValues().get(config + obj);
                    }
                    getExportConfigs().get(config).remove(obj);
                    getExportConfigsModel().removeRow(idx);
                }
            }
        });
        
        JButton addImportConfigButt = new JButton("Add import action...");
        addImportConfigButt.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    JComboBox configsCB = getConfigChooser(BackEnd.getInstance().getImportConfigurations());
                    if (configsCB.getItemCount() > 0) {
                        typeCB.removeItem(INVOKATION_TYPE_ON_SAVE);
                        if (typeCB.getItemCount() == 1) {
                            typeCB.addItem(INVOKATION_TYPE_ON_STARTUP);
                        }
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
                            Collection<Long> periods = getImportConfigs().get(config);
                            if (periods == null) {
                                periods = new ArrayList<Long>();
                                getImportConfigs().put(config, periods);
                            }
                            periods.add(period);
                            String formatted = "Every " + FormatUtils.formatTimeDuration(period * 60 * 1000);
                            getImportConfigsModel().addRow(new Object[] { config, period == 0 ? INVOKATION_TYPE_ON_STARTUP : formatted });
                            if (period != 0) {
                                getImportConfigsValues().put(config+formatted, period);
                            }
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
                    String config = (String) getImportActionsTable().getValueAt(idx, 0);
                    Object obj = getImportActionsTable().getValueAt(idx, 1);
                    if (INVOKATION_TYPE_ON_STARTUP.equals(obj)) {
                        obj = 0L;
                    } else {
                        obj = getImportConfigsValues().get(config + obj);
                    }
                    getImportConfigs().get(config).remove(obj);
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
            Iterator<Entry<String, Collection<Long>>> it1 = getExportConfigs().entrySet().iterator();
            while (it1.hasNext()) {
                Entry<String, Collection<Long>> entry = it1.next();
                Iterator<Long> it2 = entry.getValue().iterator();
                while (it2.hasNext()) {
                    Long period = it2.next();
                    sb.append(entry.getKey() + SCHEDULE_VALUE_SEPARATOR + period);
                    if (it2.hasNext()) {
                        sb.append(PROPERTY_VALUES_SEPARATOR);
                    }
                }
                if (it1.hasNext()) {
                    sb.append(PROPERTY_VALUES_SEPARATOR);
                }
            }
            props.setProperty(PROPERTY_EXPORT_CONFIGS, sb.toString());
        }
        
        if (getImportConfigs().isEmpty()) {
            props.remove(PROPERTY_IMPORT_CONFIGS);
        } else {
            StringBuffer sb = new StringBuffer();
            Iterator<Entry<String, Collection<Long>>> it1 = getImportConfigs().entrySet().iterator();
            while (it1.hasNext()) {
                Entry<String, Collection<Long>> entry = it1.next();
                Iterator<Long> it2 = entry.getValue().iterator();
                while (it2.hasNext()) {
                    Long period = it2.next();
                    sb.append(entry.getKey() + SCHEDULE_VALUE_SEPARATOR + period);
                    if (it2.hasNext()) {
                        sb.append(PROPERTY_VALUES_SEPARATOR);
                    }
                }
                if (it1.hasNext()) {
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
            	if (!Validator.isNullOrBlank(config)) {
                    String[] cfg = config.split(SCHEDULE_VALUE_SEPARATOR);
                    Collection<Long> periods = getExportConfigs().get(cfg[0]);
                    if (periods == null) {
                        periods = new ArrayList<Long>();
                        getExportConfigs().put(cfg[0], periods);
                    }
                    Long period = Long.valueOf(cfg[1]);
                    if (!periods.contains(period)) {
                        periods.add(period);
                    }
            	}
            }
        }
        populateConfigsModel(getExportConfigsModel(), getExportConfigs(), getExportConfigsValues(), INVOKATION_TYPE_ON_SAVE);

        String importConfigsStr = props.getProperty(PROPERTY_IMPORT_CONFIGS);
        if (!Validator.isNullOrBlank(importConfigsStr)) {
            String[] configs = importConfigsStr.split(PROPERTY_VALUES_SEPARATOR);
            for (String config : configs) {
                String[] cfg = config.split(SCHEDULE_VALUE_SEPARATOR);
                Collection<Long> periods = getImportConfigs().get(cfg[0]);
                if (periods == null) {
                    periods = new ArrayList<Long>();
                    getImportConfigs().put(cfg[0], periods);
                }
                Long period = Long.valueOf(cfg[1]);
                if (!periods.contains(period)) {
                    periods.add(period);
                }
            }
        }
        populateConfigsModel(getImportConfigsModel(), getImportConfigs(), getImportConfigsValues(), INVOKATION_TYPE_ON_STARTUP);
    }
    
    private JComboBox getConfigChooser(Collection<String> configs) {
        JComboBox cb = new JComboBox();
        for (String config : configs) {
            cb.addItem(config);
        }
        if (cb.getItemCount() > 0) cb.setSelectedIndex(0);
        return cb;
    }
    
    private void populateConfigsModel(DefaultTableModel model, Map<String, Collection<Long>> configs, Map<String, Long> configsValues, String eventStr) throws Exception {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        for (Entry<String, Collection<Long>> config : configs.entrySet()) {
            String name = config.getKey();
            for (Long period : config.getValue()) {
                String formatted = "Every " + FormatUtils.formatTimeDuration(period * 60 * 1000);
                String periodStr = period == 0 ? eventStr : formatted;
                model.addRow(new Object[]{ name, periodStr });
                if (period != 0) {
                    configsValues.put(name+formatted, period);
                }
            }
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
    
    private DefaultTableModel getConfirmExportPopulatedConfigsModel() {
        return createConfirmPopulatedConfigsModel(getExportConfigs());
    }
    
    private DefaultTableModel getConfirmImportPopulatedConfigsModel() {
        return createConfirmPopulatedConfigsModel(getImportConfigs());
    }
    
    private DefaultTableModel createConfirmPopulatedConfigsModel(Map<String, Collection<Long>> configs) {
        DefaultTableModel model = new DefaultTableModel() {
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
        model.addColumn(Constants.EMPTY_STR);
        model.addColumn("Configuration");
        for (Entry<String, Collection<Long>> config : configs.entrySet()) {
            for (Long period : config.getValue()) {
                if (period == 0) {
                    model.addRow(new Object[]{ Boolean.TRUE, config.getKey() });
                }
            }
        }
        return model;
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
