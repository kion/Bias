/**
 * Created on Jul 16, 2007
 */
package bias.extension.SimpleStats;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import bias.annotation.AddOnAnnotation;
import bias.core.BackEnd;
import bias.extension.ToolExtension;
import bias.gui.FrontEnd;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1.1",
        author="kion",
        description = "Simple statistics tool",
        details = "SimpleStats extension for Bias is a part<br>" +
                  "of standard \"all-inclusive-delivery-set\" of Bias application.<br>" +
                  "It is provided by <a href=\"http://kion.name/\">R. Kasianenko</a>,<br>" +
                  "an author of Bias application.")
public class SimpleStats extends ToolExtension {

    private static final ImageIcon ICON = new ImageIcon(BackEnd.getInstance().getResourceURL(SimpleStats.class, "icon.png"));
    
    private static final String SEPARATOR = "_";
    
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
            for (int i = 0; i < dates.length; i += 2) {
                long startDate = Long.parseLong(dates[i]);
                long endDate = Long.parseLong(dates[i+1]);
                model.addRow(new Object[]{new Date(startDate).toString(), new Date(endDate).toString()});
            }
            statsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        } else {
            statsPanel.add(new JLabel("No statistics gathered yet."), BorderLayout.CENTER);
        }
        FrontEnd.displayBottomPanel(new JLabel("Statistics"), statsPanel);
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
        JOptionPane.showMessageDialog(null, new Component[]{ clearButt }, "Configuration", JOptionPane.PLAIN_MESSAGE);
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
