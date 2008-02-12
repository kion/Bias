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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import bias.annotation.AddOnAnnotation;
import bias.annotation.IgnoreDataOnExport;
import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.extension.ToolExtension;
import bias.gui.FrontEnd;

/**
 * @author kion
 */

@IgnoreDataOnExport
@AddOnAnnotation(
        version="0.2.3",
        author="R. Kasianenko",
        description = "Simple statistics tool",
        details = "<i>SimpleStats</i> extension for Bias is a part<br>" +
                  "of standard \"all-inclusive-delivery-set\" of Bias application.<br>" +
                  "It is provided by <a href=\"http://kion.name/\">R. Kasianenko</a>,<br>" +
                  "an author of Bias application.")
public class SimpleStats extends ToolExtension {

    private static final ImageIcon ICON = new ImageIcon(BackEnd.getInstance().getResourceURL(SimpleStats.class, "icon.png"));
    
    private static final String SEPARATOR = "_";
    
    private static Map<String, Integer> typesCounts;
    
    private static int catCnt;

    private String[] dates;
    
    private Date startDate;
    
    public SimpleStats(byte[] data, byte[] settings) {
        super(data, settings);
        startDate = new Date();
        if (getData() != null) {
            dates = new String(getData()).split(SEPARATOR);
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#action()
     */
    @Override
    public void action() throws Throwable {
        JPanel statsPanel = new JPanel(new BorderLayout());
        JPanel entriesStatsPanel = new JPanel(new BorderLayout());
        entriesStatsPanel.add(new JLabel("<html><font color=\"#0000B5\">Entries statistics</font></html>"), BorderLayout.NORTH);
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
        sessionsStatsPanel.add(new JLabel("<html><font color=\"#0000B5\">Sessions statistics</font></html>"), BorderLayout.NORTH);
        if (dates != null && dates.length != 0) {
            final DefaultTableModel model = new DefaultTableModel() {
                private static final long serialVersionUID = 1L;
                public boolean isCellEditable(int rowIndex, int mColIndex) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            model.addColumn("Started at");
            model.addColumn("Stopped at");
            model.addColumn("Session length");
            for (int i = 0; i < dates.length; i += 2) {
                long startDate = Long.parseLong(dates[i]);
                long endDate = Long.parseLong(dates[i+1]);
                String sl = calculateSessionLength(startDate, endDate);
                model.addRow(new Object[]{new Date(startDate).toString(), new Date(endDate).toString(), sl});
            }
            sessionsStatsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        } else {
            sessionsStatsPanel.add(new JLabel("No sessions statistics gathered yet."), BorderLayout.CENTER);
        }
        statsPanel.add(entriesStatsPanel, BorderLayout.NORTH);
        statsPanel.add(sessionsStatsPanel, BorderLayout.CENTER);
        FrontEnd.displayBottomPanel(new JLabel("Statistics"), statsPanel);
    }
    
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
     * @see bias.extension.ToolExtension#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        JButton clearButt = new JButton("clear statistics");
        clearButt.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dates = null;
            }
        });
        JOptionPane.showMessageDialog(FrontEnd.getActiveWindow(), new Component[]{ clearButt }, "Configuration", JOptionPane.PLAIN_MESSAGE);
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.ToolExtension#getIcon()
     */
    @Override
    public Icon getIcon() {
        return ICON;
    }

}
