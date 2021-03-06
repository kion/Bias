/**
 * Created on Jul 16, 2007
 */
package bias.extension.SimpleStats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import bias.Constants;
import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.extension.ToolExtension;
import bias.extension.ToolRepresentation;
import bias.gui.FrontEnd;
import bias.utils.CommonUtils;
import bias.utils.FSUtils;
import bias.utils.FormatUtils;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */

public class SimpleStats extends ToolExtension {

    private static final ImageIcon ICON = new ImageIcon(CommonUtils.getResourceURL(SimpleStats.class, "icon.png"));
    
    private static final String PROPERTY_SHOW_UPTIME = "SHOW_UPTIME";
    
    private static final String SEPARATOR = "_";
    
    private static Map<String, Integer> typesCounts;
    
    private static int catCnt;

    private String[] dates;
    
    private Date startDate;
    
    private Properties properties;
    
    private byte[] settings;
    
    private boolean showUpTime = false;
    
    private JLabel label = null;
    
    public SimpleStats(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        startDate = new Date();
        if (getData() != null) {
            dates = new String(getData()).split(SEPARATOR);
        }
        initSettings();
    }

    private void initSettings() {
        if (getSettings() != null && !Arrays.equals(getSettings(), settings)) {
            settings = getSettings();
            properties = PropertiesUtils.deserializeProperties(settings);
            String showUpTimeStr = properties.getProperty(PROPERTY_SHOW_UPTIME);
            showUpTime = Boolean.valueOf(showUpTimeStr);
        }
        applySettings();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#getRepresentation()
     */
    @Override
    public ToolRepresentation getRepresentation() {
        return new ToolRepresentation(getButton(), getIndicator());
    }
    
    private JButton getButton() {
        JButton button = new JButton(ICON);
        button.addActionListener(al);
        return button;
    }
    
    private JLabel getIndicator() {
        initSettings();
        return label;
    }
    
    private void showUpTime() {
        new Thread(new Runnable() {
            public void run() {
                while (showUpTime) {
                    try {
                        Thread.sleep(5000);
                        if (label != null) {
                            label.setText("UpTime: " + calculateSessionLength(startDate.getTime(), new Date().getTime()));
                        }
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }).start();
    }
    
    private ActionListener al = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
            try {
                JPanel statsPanel = new JPanel(new BorderLayout());

                JPanel diskStatsPanel = new JPanel(new BorderLayout());
                JLabel l = new JLabel("Disk Usage");
                l.setForeground(new Color(0, 100, 0));
                diskStatsPanel.add(l, BorderLayout.NORTH);
                long size = FSUtils.getFileSize(Constants.ROOT_DIR, Collections.singletonList(Constants.TMP_DIR.getName()));
                String sizeStr = FormatUtils.formatByteSize(size);
                JLabel ls = new JLabel(sizeStr);
                diskStatsPanel.add(ls, BorderLayout.CENTER);
                
                JPanel entriesStatsPanel = new JPanel(new BorderLayout());
                JLabel l1 = new JLabel("Entries");
                l1.setForeground(new Color(0, 100, 0));
                entriesStatsPanel.add(l1, BorderLayout.NORTH);
                catCnt = 0;
                getTypesCountsMap().clear();
                parseTypes(getTypesCountsMap(), BackEnd.getInstance().getData());
                if (catCnt != 0) {
                    getTypesCountsMap().put("Categories", catCnt);
                }
                if (!getTypesCountsMap().isEmpty()) {
                    JPanel p = new JPanel(new GridLayout(getTypesCountsMap().size(), 1));
                    for (Entry<String, Integer> entry : getTypesCountsMap().entrySet()) {
                        JPanel pp = new JPanel(new BorderLayout());
                        String type = entry.getKey();
                        JLabel typeL = new JLabel(type);
                        typeL.setForeground(Color.BLUE);
                        pp.add(typeL, BorderLayout.WEST);
                        Integer count = entry.getValue();
                        JLabel countL = new JLabel("" + count);
                        countL.setForeground(Color.RED);
                        pp.add(countL, BorderLayout.EAST);
                        pp.setBorder(new BevelBorder(BevelBorder.LOWERED));
                        p.add(pp);
                    }
                    entriesStatsPanel.add(p, BorderLayout.CENTER);
                } else {
                    entriesStatsPanel.add(new JLabel("No entries statistics gathered yet."), BorderLayout.CENTER);
                }
                
                JPanel sessionsStatsPanel = new JPanel(new BorderLayout());
                JLabel l2 = new JLabel("Sessions");
                l2.setForeground(new Color(0, 100, 0));
                sessionsStatsPanel.add(l2, BorderLayout.NORTH);
                if (dates != null && dates.length != 0) {
                    final DefaultTableModel model = new DefaultTableModel() {
                        private static final long serialVersionUID = 1L;
                        public boolean isCellEditable(int rowIndex, int mColIndex) {
                            return false;
                        }
                    };
                    JTable table = new JTable(model);
                    model.addColumn("");
                    model.addColumn("Started at");
                    model.addColumn("Stopped at");
                    model.addColumn("Session length");
                    for (int i = 0; i < dates.length; i += 2) {
                        long startDate = Long.parseLong(dates[i]);
                        long endDate = Long.parseLong(dates[i+1]);
                        String sl = calculateSessionLength(startDate, endDate);
                        model.addRow(new Object[]{dates[i+1], new Date(startDate).toString(), new Date(endDate).toString(), sl});
                    }
                    table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
                    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
                    sorter.setSortsOnUpdates(true);
                    sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));
                    sorter.setSortable(1, false);
                    sorter.setSortable(2, false);
                    sorter.setSortable(3, false);
                    table.setRowSorter(sorter);
                    JPanel tablePanel = new JPanel(new BorderLayout());
                    tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
                    tablePanel.add(table, BorderLayout.CENTER);
                    sessionsStatsPanel.add(tablePanel, BorderLayout.CENTER);
                } else {
                    sessionsStatsPanel.add(new JLabel("No sessions statistics gathered yet."), BorderLayout.CENTER);
                }
                
                statsPanel.add(diskStatsPanel, BorderLayout.NORTH);
                statsPanel.add(entriesStatsPanel, BorderLayout.CENTER);
                statsPanel.add(sessionsStatsPanel, BorderLayout.SOUTH);
                
                FrontEnd.displayBottomPanel(new JLabel("Statistics"), statsPanel);
            } catch (Throwable t) {
                FrontEnd.displayErrorMessage("Failed to gather stats!", t);
            }
        }
    };
    
    private String calculateSessionLength(long startTime, long endTime) {
        long duration = endTime - startTime;
        return FormatUtils.formatTimeDuration(duration);
    }
    
    private void parseTypes(Map<String, Integer> typesCounts, DataCategory dc) {
        for (Recognizable r : dc.getData()) {
            if (r instanceof DataCategory) {
                catCnt++;
                parseTypes(typesCounts, (DataCategory) r);
            } else if (r instanceof DataEntry) {
                increaseTypeCounter(((DataEntry) r).getType());
            }
        }
    }
    
    private void increaseTypeCounter(String type) {
        Integer count = getTypesCountsMap().get(type);
        if (count == null) {
            count = 1;
        } else {
            count++;
        }
        getTypesCountsMap().put(type, count);
    }
    
    private Map<String, Integer> getTypesCountsMap() {
        if (typesCounts == null) {
            typesCounts = new LinkedHashMap<String, Integer>();
        }
        return typesCounts;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        StringBuffer sb = new StringBuffer();
        if (dates != null) {
            int i = 0;
            for (String s : dates) {
                sb.append(s);
                if (i++ < dates.length) {
                    sb.append(SEPARATOR);
                }
            }
        }
        sb.append(startDate.getTime());
        sb.append(SEPARATOR);
        sb.append(new Date().getTime());
        return sb.toString().getBytes();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        if (properties == null) properties = new Properties();
        if (showUpTime) {
            properties.setProperty(PROPERTY_SHOW_UPTIME, "" + true);
        } else {
            properties.remove(PROPERTY_SHOW_UPTIME);
        }
        return PropertiesUtils.serializeProperties(properties);
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#configure()
     */
    public byte[] configure() throws Throwable {
        initSettings();
        final JCheckBox showUpTime = new JCheckBox("Show UpTime in statusbar", this.showUpTime);
        showUpTime.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                SimpleStats.this.showUpTime = showUpTime.isSelected();
                if (SimpleStats.this.showUpTime) {
                    showUpTime();
                }
            }
        });
        JButton clearButt = new JButton("Clear statistics");
        clearButt.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dates = null;
            }
        });
        JOptionPane.showMessageDialog(FrontEnd.getActiveWindow(), new Component[]{ showUpTime, clearButt }, "Configuration", JOptionPane.PLAIN_MESSAGE);
        if (this.showUpTime) {
            properties.setProperty(PROPERTY_SHOW_UPTIME, "" + true);
        } else {
            properties.remove(PROPERTY_SHOW_UPTIME);
        }
        settings = PropertiesUtils.serializeProperties(properties);
        return settings;
    }
    
    private void applySettings() {
        if (showUpTime) {
            if (label == null) {
                label = new JLabel("UpTime: ...");
                showUpTime();
            }
        } else {
            label = null;
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#skipDataExport()
     */
    @Override
    public boolean skipDataExport() {
        return true;
    }
    
}
