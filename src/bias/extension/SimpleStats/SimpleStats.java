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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

import bias.Bias;
import bias.annotation.AddOnAnnotation;
import bias.annotation.IgnoreDataOnExport;
import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.extension.ToolExtension;
import bias.extension.ToolRepresentation;
import bias.gui.FrontEnd;
import bias.utils.FSUtils;
import bias.utils.FormatUtils;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */

@IgnoreDataOnExport
@AddOnAnnotation(
        version="0.3.9",
        author="R. Kasianenko",
        description = "Simple statistics tool",
        details = "<i>SimpleStats</i> extension for Bias is a part<br>" +
                  "of standard \"all-inclusive-delivery-set\" of Bias application.<br>" +
                  "It is provided by <a href=\"http://kion.name/\">R. Kasianenko</a>,<br>" +
                  "an author of Bias application.")
public class SimpleStats extends ToolExtension {

    private static final ImageIcon ICON = new ImageIcon(BackEnd.getInstance().getResourceURL(SimpleStats.class, "icon.png"));
    
    private static final String PROPERTY_SHOW_UPTIME = "SHOW_UPTIME";
    
    private static final String SEPARATOR = "_";
    
    private static Map<String, Integer> typesCounts;
    
    private static int catCnt;

    private String[] dates;
    
    private Date startDate;
    
    private Properties settings;
    
    private boolean showUpTime = false;
    
    private JLabel label = null;

    public SimpleStats(byte[] data, byte[] settings) {
        super(data, settings);
        startDate = new Date();
        if (getData() != null) {
            dates = new String(getData()).split(SEPARATOR);
        }
        this.settings = PropertiesUtils.deserializeProperties(settings);
        String showUpTimeStr = this.settings.getProperty(PROPERTY_SHOW_UPTIME);
        showUpTime = Boolean.valueOf(showUpTimeStr);
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
        if (showUpTime) {
            if (label == null) {
                label = new JLabel("UpTime: ...");
            }
        } else {
            label = null;
        }
        return label;
    }
    
    private void showUpTime() {
        new Thread(new Runnable() {
            public void run() {
                while (showUpTime) {
                    try {
                        Thread.sleep(5000);
                        if (getIndicator() != null) {
                            getIndicator().setText("UpTime: " + calculateSessionLength(startDate.getTime(), new Date().getTime()));
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
                long size = FSUtils.getFileSize(Bias.getJarFile().getParentFile());
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
                FrontEnd.displayErrorMessage("Failed to perform search!", t);
            }
        }
    };
    
    private String calculateSessionLength(long startDate, long endDate) {
        List<String> list = new LinkedList<String>();
        StringBuffer lenStr = new StringBuffer();
        long len = endDate - startDate;
        long sec = len/1000;
        if (sec > 0) {
            long min = sec / 60;
            if (min > 0) {
                sec = sec % 60;
                long hr = min / 60;
                if (hr > 0) {
                    min = min % 60;
                    long days = hr / 24;
                    if (days > 0) {
                        hr = hr % 24;
                        list.add(days + " d ");
                    }
                    list.add(hr + " hr ");
                }
                list.add(min + " min ");
            }
            list.add(sec + " sec ");
        }
        for (String s : list) {
            lenStr.append(s);
        }
        return lenStr.toString();
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
     * @see bias.extension.ToolExtension#serializeData()
     */
    @Override
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
     * @see bias.extension.ToolExtension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        if (showUpTime) {
            settings.setProperty(PROPERTY_SHOW_UPTIME, "" + true);
        } else {
            settings.remove(PROPERTY_SHOW_UPTIME);
        }
        return PropertiesUtils.serializeProperties(settings);
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
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
            this.settings.setProperty(PROPERTY_SHOW_UPTIME, "" + true);
        } else {
            this.settings.remove(PROPERTY_SHOW_UPTIME);
        }
        return PropertiesUtils.serializeProperties(this.settings);
    }
    
    private void applySettings() {
        if (SimpleStats.this.showUpTime) {
            showUpTime();
        }
    }

}
