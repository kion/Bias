/**
 * Created on Feb 2, 2008
 */
package bias.extension.FinancialFlows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.RectangleInsets;

import bias.annotation.AddOnAnnotation;
import bias.core.BackEnd;
import bias.extension.EntryExtension;
import bias.extension.FinancialFlows.xmlb.Direction;
import bias.extension.FinancialFlows.xmlb.ObjectFactory;
import bias.extension.FinancialFlows.xmlb.Parts;
import bias.extension.FinancialFlows.xmlb.RegularFlow;
import bias.extension.FinancialFlows.xmlb.RegularFlows;
import bias.extension.FinancialFlows.xmlb.SingleFlow;
import bias.extension.FinancialFlows.xmlb.SingleFlows;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooserCellEditor;

/**
 * @author kion
 */

@AddOnAnnotation(
        version = "0.1.1", 
        author = "R. Kasianenko", 
        description = "Financial Flows Management & Statistics", 
        details = "<i>FinancialFlows</i> extension for Bias is a part of standard \"all-inclusive-delivery-set\" of Bias application.<br>" + 
                  "It is provided by <a href=\"http://kion.name/\">R. Kasianenko</a>, an author of Bias application.<br><br>" + 
                  "Extension uses:<br><ul>" + 
                  "<li><a href=\"http://www.jfree.org/jfreechart/\">JFreeChart</a> - a free Java chart library that makes it easy<br>" + 
                  "to display professional quality charts in Java applications,<br>" + 
                  "provided by <a href=\"http://www.jfree.org/\">JFree.org</a> - site of free software projects targetting the Java platform,<br>" + 
                  "owned and operated by <a href=\"http://jroller.com/page/dgilbert/\">David Gilbert</a> and <a href=\"http://www.sherito.org/\">Thomas Morgner</a></li>" + 
                  "<li><a href=\"http://www.toedter.com/en/jcalendar/\">JCalendar</a> - a Java date chooser bean for graphically picking a date provided by <a href=\"http://www.toedter.com/en/contact.html\">Kai Toedter</a>.</li></ul>")
public class FinancialFlows extends EntryExtension {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_ADD = new ImageIcon(BackEnd.getInstance().getResourceURL(FinancialFlows.class, "add.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(BackEnd.getInstance().getResourceURL(FinancialFlows.class, "delete.png"));
    private static final ImageIcon ICON_CHART1 = new ImageIcon(BackEnd.getInstance().getResourceURL(FinancialFlows.class, "chart1.png"));
    private static final ImageIcon ICON_CHART2 = new ImageIcon(BackEnd.getInstance().getResourceURL(FinancialFlows.class, "chart2.png"));
    
    private static enum DIRECTION {
        INCOME, OUTGO
    }

    private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/schema.xsd";

    private static final int COLUMN_DIRECTION_IDX = 0;

    private static final int COLUMN_AMOUNT_IDX = 1;

    private static final int COLUMN_TYPE_IDX = 2;

    private static final int COLUMN_DATE_IDX = 3;

    private static final int COLUMN_END_DATE_IDX = 4;

    private static JAXBContext context;

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    private static ObjectFactory objFactory = new ObjectFactory();

    private TableRowSorter<TableModel> singleSorter;

    private TableRowSorter<TableModel> regularSorter;

    private String currency;

    private List<String> incomeTypes = new LinkedList<String>();

    private List<String> outgoTypes = new LinkedList<String>();

    private JToolBar jToolBar1;

    private JButton jButton2;

    private JButton jButton3;

    private JButton jButton4;

    private JButton jButton5;

    private JPanel jPanel1;

    private JTabbedPane jTabbedPane1;

    private JTable jTable1;

    private JTable jTable2;

    private JButton jButton1;

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        }
        return context;
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        if (unmarshaller == null) {
            unmarshaller = getContext().createUnmarshaller();
        }
        return unmarshaller;
    }

    private static Marshaller getMarshaller() throws JAXBException {
        if (marshaller == null) {
            marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        }
        return marshaller;
    }

    public FinancialFlows(UUID id, byte[] data, byte[] settings) {
        super(id, data, settings);
        initialize();
    }

    private void initialize() {
        try {
            if (getData() != null && getData().length != 0) {
                Parts parts = (Parts) getUnmarshaller().unmarshal(new ByteArrayInputStream(getData()));
                populateSingleTable(parts.getSingle());
                populateRegularTable(parts.getRegular());
            }
            Properties props = PropertiesUtils.deserializeProperties(getSettings());
            currency = props.getProperty("CURRENCY");
        } catch (JAXBException e) {
            FrontEnd.displayErrorMessage("Failed to initialize data/settings!", e);
        }
        initGUI();
    }

    private void populateSingleTable(SingleFlows flows) {
        for (SingleFlow f : flows.getFlow()) {
            DIRECTION direction = DIRECTION.valueOf(f.getDirection().name());
            addRow(direction, f.getType(), f.getDate().toGregorianCalendar().getTime(), f.getAmount());
            populateTypes(direction, f.getType());
        }
    }

    private void populateRegularTable(RegularFlows flows) {
        for (RegularFlow f : flows.getFlow()) {
            DIRECTION direction = DIRECTION.valueOf(f.getDirection().name());
            addRow(direction, f.getType(), f.getDate().toGregorianCalendar().getTime(), f.getEndDate().toGregorianCalendar().getTime(), f
                    .getAmount());
            populateTypes(direction, f.getType());
        }
    }

    private void populateTypes(DIRECTION direction, String type) {
        if (direction == DIRECTION.INCOME) {
            if (!incomeTypes.contains(type)) {
                incomeTypes.add(type);
            }
        } else if (direction == DIRECTION.OUTGO) {
            if (!outgoTypes.contains(type)) {
                outgoTypes.add(type);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.Extension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        Parts parts = objFactory.createParts();
        parts.setSingle(serializeSingleFlows());
        parts.setRegular(serializeRegularFlows());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getMarshaller().marshal(parts, baos);
        return baos.toByteArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.EntryExtension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        // TODO [P1] table-sorting options should be stored as well
        Properties p = new Properties();
        if (currency != null) {
            p.setProperty("CURRENCY", currency);
        }
        return PropertiesUtils.serializeProperties(p);
    }

    private SingleFlows serializeSingleFlows() {
        SingleFlows flows = objFactory.createSingleFlows();
        for (int i = 0; i < getJTableSingle().getModel().getRowCount(); i++) {
            SingleFlow flow = objFactory.createSingleFlow();
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime((Date) getJTableSingle().getModel().getValueAt(i, COLUMN_DATE_IDX));
            flow.setDate(new XMLGregorianCalendarImpl(cal));
            flow.setAmount((Float) getJTableSingle().getModel().getValueAt(i, COLUMN_AMOUNT_IDX));
            flow.setType((String) getJTableSingle().getModel().getValueAt(i, COLUMN_TYPE_IDX));
            DIRECTION direction = (DIRECTION) getJTableSingle().getModel().getValueAt(i, COLUMN_DIRECTION_IDX);
            flow.setDirection(Direction.fromValue(direction.name()));
            flows.getFlow().add(flow);
        }
        return flows;
    }

    private RegularFlows serializeRegularFlows() {
        RegularFlows flows = objFactory.createRegularFlows();
        for (int i = 0; i < getJTableRegular().getModel().getRowCount(); i++) {
            RegularFlow flow = objFactory.createRegularFlow();
            GregorianCalendar cal1 = new GregorianCalendar();
            cal1.setTime((Date) getJTableRegular().getModel().getValueAt(i, COLUMN_DATE_IDX));
            flow.setDate(new XMLGregorianCalendarImpl(cal1));
            GregorianCalendar cal2 = new GregorianCalendar();
            cal2.setTime((Date) getJTableRegular().getModel().getValueAt(i, COLUMN_END_DATE_IDX));
            flow.setEndDate(new XMLGregorianCalendarImpl(cal2));
            flow.setAmount((Float) getJTableRegular().getModel().getValueAt(i, COLUMN_AMOUNT_IDX));
            flow.setType((String) getJTableRegular().getModel().getValueAt(i, COLUMN_TYPE_IDX));
            DIRECTION direction = (DIRECTION) getJTableRegular().getModel().getValueAt(i, COLUMN_DIRECTION_IDX);
            flow.setDirection(Direction.fromValue(direction.name()));
            flows.getFlow().add(flow);
        }
        return flows;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.Extension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        // TODO [P1] add purpose-data to search data
        Collection<String> searchData = new ArrayList<String>();
        searchData.addAll(incomeTypes);
        searchData.addAll(outgoTypes);
        populateSearchData(searchData, getJTableSingle());
        populateSearchData(searchData, getJTableRegular());
        return searchData;
    }
    
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        // TODO [P1] implement (income/outcome types management, currency settings and so on should be here)
        return super.configure(settings);
    }

    private void populateSearchData(Collection<String> searchData, JTable table) {
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            searchData.add(table.getModel().getValueAt(i, COLUMN_AMOUNT_IDX).toString());
        }
    }

    private void initGUI() {
        try {
            {
                BorderLayout thisLayout = new BorderLayout();
                this.setLayout(thisLayout);
                this.setPreferredSize(new java.awt.Dimension(743, 453));
                {
                    jToolBar1 = new JToolBar();
                    jToolBar1.setFloatable(false);
                    this.add(jToolBar1, BorderLayout.SOUTH);
                    jToolBar1.setPreferredSize(new java.awt.Dimension(743, 26));
                    {
                        jButton1 = new JButton();
                        jToolBar1.add(jButton1);
                        jButton1.setIcon(ICON_ADD);
                        jButton1.setToolTipText("add flow");
                        jButton1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                boolean ir = isRegularTableActive();
                                JPanel p = new JPanel(new GridLayout(ir ? 9 : 7, 1));
                                p.add(new JLabel("Direction:"));
                                final JComboBox directionCombo = new JComboBox();
                                directionCombo.addItem(DIRECTION.INCOME);
                                directionCombo.addItem(DIRECTION.OUTGO);
                                directionCombo.setSelectedItem(DIRECTION.INCOME);
                                p.add(directionCombo);
                                p.add(new JLabel("Type:"));
                                final JComboBox typeCombo = new JComboBox();
                                populateCombo(typeCombo, incomeTypes);
                                typeCombo.setEditable(true);
                                p.add(typeCombo);
                                directionCombo.addItemListener(new ItemListener() {
                                    public void itemStateChanged(ItemEvent e) {
                                        DIRECTION direction = (DIRECTION) directionCombo.getSelectedItem();
                                        if (direction == DIRECTION.INCOME) {
                                            populateCombo(typeCombo, incomeTypes);
                                        } else if (direction == DIRECTION.OUTGO) {
                                            populateCombo(typeCombo, outgoTypes);
                                        }
                                    }
                                });
                                p.add(new JLabel(ir ? "Start date:" : "Date:"));
                                JDateChooser dateChooser = new JDateChooser(new Date());
                                p.add(dateChooser);
                                JDateChooser endDateChooser = null;
                                if (ir) {
                                    p.add(new JLabel("End date:"));
                                    endDateChooser = new JDateChooser(new Date());
                                    p.add(endDateChooser);
                                }
                                p.add(new JLabel("Amount:"));
                                String amountStr = JOptionPane.showInputDialog(p);
                                if (amountStr != null) {
                                    try {
                                        Float amount = Float.valueOf(amountStr);
                                        DIRECTION direction = (DIRECTION) directionCombo.getSelectedItem();
                                        String type = (String) typeCombo.getSelectedItem();
                                        if (ir) {
                                            addRow(direction, type, dateChooser.getDate(), endDateChooser.getDate(), amount);
                                        } else {
                                            addRow(direction, type, dateChooser.getDate(), amount);
                                        }
                                        if (direction == DIRECTION.INCOME) {
                                            if (!incomeTypes.contains(type)) {
                                                incomeTypes.add(type);
                                            }
                                        } else if (direction == DIRECTION.OUTGO) {
                                            if (!outgoTypes.contains(type)) {
                                                outgoTypes.add(type);
                                            }
                                        }
                                    } catch (NumberFormatException nfe) {
                                        FrontEnd.displayErrorMessage("Invalid amount format!");
                                    }
                                }
                            }
                        });
                    }
                    {
                        jButton2 = new JButton();
                        jToolBar1.add(jButton2);
                        jButton2.setIcon(ICON_DELETE);
                        jButton2.setToolTipText("delete flow");
                        jButton2.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                try {
                                    JTable table = isRegularTableActive() ? getJTableRegular() : getJTableSingle();
                                    if (table.getSelectedRow() != -1) {
                                        removeRow(table, table.getSelectedRow());
                                    }
                                } catch (Exception e) {
                                    FrontEnd.displayErrorMessage(e);
                                }
                            }
                        });
                    }
                    {
                        jButton3 = new JButton();
                        jToolBar1.add(jButton3);
                        jButton3.setIcon(ICON_CHART1);
                        jButton3.setToolTipText("Income Chart");
                        jButton3.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                try {
                                    DefaultPieDataset dataset = new DefaultPieDataset();
                                    populatePieDataset(DIRECTION.INCOME, dataset);
                                    Image image = buildPieChart("Income Chart", dataset);
                                    JLabel label = new JLabel();
                                    label.setIcon(new ImageIcon(image));
                                    JOptionPane.showMessageDialog(FinancialFlows.this, new JScrollPane(label));
                                } catch (Exception e) {
                                    FrontEnd.displayErrorMessage(e);
                                }
                            }
                        });
                    }
                    {
                        jButton4 = new JButton();
                        jToolBar1.add(jButton4);
                        jButton4.setIcon(ICON_CHART1);
                        jButton4.setToolTipText("Outgo Chart");
                        jButton4.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                try {
                                    DefaultPieDataset dataset = new DefaultPieDataset();
                                    populatePieDataset(DIRECTION.OUTGO, dataset);
                                    Image image = buildPieChart("Outgo Chart", dataset);
                                    JLabel label = new JLabel();
                                    label.setIcon(new ImageIcon(image));
                                    JOptionPane.showMessageDialog(FinancialFlows.this, new JScrollPane(label));
                                } catch (Exception e) {
                                    FrontEnd.displayErrorMessage(e);
                                }
                            }
                        });
                    }
                    {
                        jButton5 = new JButton();
                        jToolBar1.add(jButton5);
                        jButton5.setIcon(ICON_CHART2);
                        jButton5.setToolTipText("Income/Outgo Chart");
                        jButton5.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                try {
                                    TimeSeriesCollection dataset = new TimeSeriesCollection();
                                    TimeSeries incomeSeries = new TimeSeries("Income", Month.class);
                                    TimeSeries outgoSeries = new TimeSeries("Outgo", Month.class);
                                    populateTimeSeries(getJTableSingle(), incomeSeries, outgoSeries, false);
                                    populateTimeSeries(getJTableRegular(), incomeSeries, outgoSeries, true);
                                    dataset.addSeries(incomeSeries);
                                    dataset.addSeries(outgoSeries);
                                    Image image = buildTimeSeriesChart("Income/Outgo Chart", "Amount", dataset);
                                    JLabel label = new JLabel();
                                    label.setIcon(new ImageIcon(image));
                                    JOptionPane.showMessageDialog(FinancialFlows.this, new JScrollPane(label));
                                } catch (Exception e) {
                                    FrontEnd.displayErrorMessage(e);
                                }
                            }
                        });
                    }
                }
                {
                    jPanel1 = new JPanel();
                    jPanel1.setLayout(new BorderLayout());
                    this.add(jPanel1, BorderLayout.CENTER);
                    jTabbedPane1 = new JTabbedPane();
                    jPanel1.add(jTabbedPane1, BorderLayout.CENTER);
                    {
                        jTabbedPane1.addTab("Single Flows", new JScrollPane(getJTableSingle()));
                        jTabbedPane1.addTab("Regular Flows", new JScrollPane(getJTableRegular()));
                    }
                }
            }
        } catch (Exception e) {
            FrontEnd.displayErrorMessage(e);
        }
    }

    private boolean isRegularTableActive() {
        int idx = jTabbedPane1.getSelectedIndex();
        return idx == 1 ? true : false;
    }

    private void populateCombo(JComboBox combo, List<String> values) {
        combo.removeAllItems();
        for (String s : values) {
            combo.addItem(s);
        }
    }

    private JTable getJTableSingle() {
        if (jTable1 == null) {
            jTable1 = new JTable();
            initJTableSingle();
        }
        return jTable1;
    }

    private JTable getJTableRegular() {
        if (jTable2 == null) {
            jTable2 = new JTable();
            initJTableRegular();
        }
        return jTable2;
    }

    private DefaultTableModel getJTableModel() {
        DefaultTableModel jTableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return true;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                case COLUMN_DATE_IDX:
                    return Date.class;
                case COLUMN_END_DATE_IDX:
                    return Date.class;
                case COLUMN_AMOUNT_IDX:
                    return Float.class;
                default:
                    return super.getColumnClass(columnIndex);
                }
            }
        };
        return jTableModel;
    }

    private void initJTableSingle() {
        DefaultTableModel model = getJTableModel();
        model.addColumn("Direction");
        model.addColumn("Amount");
        model.addColumn("Type");
        model.addColumn("Date");
        getJTableSingle().setModel(model);
        getJTableSingle().setDefaultRenderer(Float.class, new CurrencyRenderer());
        getJTableSingle().getColumnModel().getColumn(COLUMN_DATE_IDX).setCellEditor(new JDateChooserCellEditor());
        getJTableSingle().getColumnModel().getColumn(COLUMN_TYPE_IDX).setCellEditor(new ComboBoxCellEditor());
        getJTableSingle().getColumnModel().removeColumn(getJTableSingle().getColumnModel().getColumn(COLUMN_DIRECTION_IDX));
        singleSorter = new TableRowSorter<TableModel>(model);
        // TODO [P1] table-sorting options should be restored from settings
        List<SortKey> sortKeys = new LinkedList<SortKey>();
        sortKeys.add(new SortKey(COLUMN_DATE_IDX, SortOrder.ASCENDING));
        singleSorter.setSortKeys(sortKeys);
        getJTableSingle().setRowSorter(singleSorter);
    }

    private void initJTableRegular() {
        DefaultTableModel model = getJTableModel();
        model.addColumn("Direction");
        model.addColumn("Amount");
        model.addColumn("Type");
        model.addColumn("Start date");
        model.addColumn("End date");
        getJTableRegular().setModel(model);
        getJTableRegular().setDefaultRenderer(Float.class, new CurrencyRenderer());
        getJTableRegular().getColumnModel().getColumn(COLUMN_DATE_IDX).setCellEditor(new JDateChooserCellEditor());
        getJTableRegular().getColumnModel().getColumn(COLUMN_END_DATE_IDX).setCellEditor(new JDateChooserCellEditor());
        getJTableRegular().getColumnModel().getColumn(COLUMN_TYPE_IDX).setCellEditor(new ComboBoxCellEditor());
        getJTableRegular().getColumnModel().removeColumn(getJTableRegular().getColumnModel().getColumn(COLUMN_DIRECTION_IDX));
        regularSorter = new TableRowSorter<TableModel>(model);
        // TODO [P1] table-sorting options should be restored from settings
        List<SortKey> sortKeys = new LinkedList<SortKey>();
        sortKeys.add(new SortKey(COLUMN_DIRECTION_IDX, SortOrder.ASCENDING));
        regularSorter.setSortKeys(sortKeys);
        getJTableRegular().setRowSorter(regularSorter);
    }

    public class ComboBoxCellEditor extends JComboBox implements TableCellEditor {

        private static final long serialVersionUID = 1L;

        protected EventListenerList listenerList = new EventListenerList();

        protected ChangeEvent changeEvent = new ChangeEvent(this);

        public ComboBoxCellEditor() {
            super();
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    fireEditingStopped();
                }
            });
        }

        public void addCellEditorListener(CellEditorListener listener) {
            listenerList.add(CellEditorListener.class, listener);
        }

        public void removeCellEditorListener(CellEditorListener listener) {
            listenerList.remove(CellEditorListener.class, listener);
        }

        protected void fireEditingStopped() {
            CellEditorListener listener;
            Object[] listeners = listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingStopped(changeEvent);
                }
            }
        }

        protected void fireEditingCanceled() {
            CellEditorListener listener;
            Object[] listeners = listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingCanceled(changeEvent);
                }
            }
        }

        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        public boolean isCellEditable(EventObject event) {
            return true;
        }

        public boolean shouldSelectCell(EventObject event) {
            return true;
        }

        public Object getCellEditorValue() {
            return getSelectedItem();
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            DIRECTION direction = (DIRECTION) table.getModel().getValueAt(row, COLUMN_DIRECTION_IDX);
            if (direction == DIRECTION.INCOME) {
                populateCombo(this, incomeTypes);
            } else if (direction == DIRECTION.OUTGO) {
                populateCombo(this, outgoTypes);
            }
            setSelectedItem(value);
            setEditable(true);
            return this;
        }
    }

    private class CurrencyRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        private final Color COLOR_INCOME = new Color(195, 255, 195);

        private final Color COLOR_OUTGO = new Color(255, 195, 195);

        public CurrencyRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        public void setValue(Object value) {
            if ((value != null) && (value instanceof Number)) {
                Number numberValue = (Number) value;
                NumberFormat formatter = NumberFormat.getInstance();
                value = (currency != null ? currency + " " : "") + formatter.format(numberValue.floatValue());
            }
            super.setValue(value);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = table.getRowSorter().convertRowIndexToModel(row);
            DIRECTION direction = (DIRECTION) table.getModel().getValueAt(row, COLUMN_DIRECTION_IDX);
            if (direction == DIRECTION.INCOME) {
                c.setBackground(COLOR_INCOME);
            } else if (direction == DIRECTION.OUTGO) {
                c.setBackground(COLOR_OUTGO);
            }
            return c;
        }

    }

    private void addRow(DIRECTION direction, String type, Date date, Float amount) {
        DefaultTableModel model = (DefaultTableModel) getJTableSingle().getModel();
        model.addRow(new Object[] { direction, amount, type, date });
    }

    private void addRow(DIRECTION direction, String type, Date date, Date endDate, Float amount) {
        DefaultTableModel model = (DefaultTableModel) getJTableRegular().getModel();
        model.addRow(new Object[] { direction, amount, type, date, endDate });
    }

    private void removeRow(JTable jTable, int rowIdx) {
        DefaultTableModel model = (DefaultTableModel) jTable.getModel();
        rowIdx = jTable.getRowSorter().convertRowIndexToModel(rowIdx);
        model.removeRow(rowIdx);
    }

    private void populateTimeSeries(JTable jTable, TimeSeries incomeSeries, TimeSeries outgoSeries, boolean regular) {
        TableModel model = jTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            DIRECTION d = (DIRECTION) model.getValueAt(i, COLUMN_DIRECTION_IDX);
            if (d == DIRECTION.INCOME) {
                if (regular) {
                    populateTimeSeries(incomeSeries, outgoSeries, model, i);
                } else {
                    populateTimeSeries(incomeSeries, model, i);
                }
            } else if (d == DIRECTION.OUTGO) {
                if (regular) {
                    populateTimeSeries(outgoSeries, incomeSeries, model, i);
                } else {
                    populateTimeSeries(outgoSeries, model, i);
                }
            }
        }
    }

    private void populateTimeSeries(TimeSeries series, TableModel model, int rowIdx) {
        Date date = (Date) model.getValueAt(rowIdx, COLUMN_DATE_IDX);
        Float amount = (Float) model.getValueAt(rowIdx, COLUMN_AMOUNT_IDX);
        Month month = new Month(date);
        TimeSeriesDataItem item = series.getDataItem(month);
        if (item != null) {
            amount += item.getValue().floatValue();
        }
        series.addOrUpdate(new Month(date), amount);
    }

    private void populateTimeSeries(TimeSeries readWriteSeries, TimeSeries readOnlySeries, TableModel model, int rowIdx) {
        Date date = (Date) model.getValueAt(rowIdx, COLUMN_DATE_IDX);
        Date endDate = model.getColumnCount() > COLUMN_END_DATE_IDX ? (Date) model.getValueAt(rowIdx, COLUMN_END_DATE_IDX) : null;
        Float amount = (Float) model.getValueAt(rowIdx, COLUMN_AMOUNT_IDX);
        // find out lowest/highest periods
        RegularTimePeriod periodLow = null;
        RegularTimePeriod periodHigh = null;
        if (!readWriteSeries.isEmpty()) {
            periodLow = readWriteSeries.getDataItem(0).getPeriod();
            periodHigh = readWriteSeries.getDataItem(readWriteSeries.getItemCount() - 1).getPeriod();
            if (!readOnlySeries.isEmpty()) {
                RegularTimePeriod period1 = readOnlySeries.getDataItem(0).getPeriod();
                if (period1.getStart().before(periodLow.getStart())) {
                    periodLow = period1;
                }
                RegularTimePeriod period2 = readOnlySeries.getDataItem(readOnlySeries.getItemCount() - 1).getPeriod();
                if (period2.getEnd().after(periodHigh.getEnd())) {
                    periodHigh = period2;
                }
            }
        } else if (!readOnlySeries.isEmpty()) {
            periodLow = readOnlySeries.getDataItem(0).getPeriod();
            periodHigh = readOnlySeries.getDataItem(readOnlySeries.getItemCount() - 1).getPeriod();
        }
        if (periodLow == null || date.before(periodLow.getEnd())) {
            periodLow = new Month(date);
        }
        if (periodHigh == null || (endDate != null && endDate.after(periodHigh.getStart()))) {
            periodHigh = new Month(endDate);
        }
        // update all periods visible on chart with regular amount
        while (periodLow.getStart().before(periodHigh.getEnd()) && (endDate == null || periodLow.getStart().before(endDate))) {
            if (periodLow.getEnd().after(date)) {
                TimeSeriesDataItem item = readWriteSeries.getDataItem(periodLow);
                Float corrAmount = amount;
                if (item != null) {
                    corrAmount += item.getValue().floatValue();
                }
                readWriteSeries.addOrUpdate(periodLow, corrAmount);
            }
            periodLow = periodLow.next();
        }
    }

    private void populatePieDataset(DIRECTION direction, DefaultPieDataset dataset) {
        if (getJTableSingle().getModel().getRowCount() > 0 || getJTableRegular().getModel().getRowCount() > 0) {
            Date dateLow = null;
            Date dateHigh = null;
            TableModel model = getJTableSingle().getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                DIRECTION d = (DIRECTION) model.getValueAt(i, COLUMN_DIRECTION_IDX);
                if (d == direction) {
                    Date date = (Date) model.getValueAt(i, COLUMN_DATE_IDX);
                    if (dateLow == null || date.before(dateLow)) {
                        dateLow = date;
                    }
                    if (dateHigh == null || date.after(dateHigh)) {
                        dateHigh = date;
                    }
                    String type = (String) model.getValueAt(i, COLUMN_TYPE_IDX);
                    Number num = 0;
                    if (dataset.getKeys().contains(type)) {
                        num = dataset.getValue(type);
                    }
                    num = num.floatValue() + (Float) model.getValueAt(i, COLUMN_AMOUNT_IDX);
                    dataset.setValue(type, num);
                }
            }
            model = getJTableRegular().getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                DIRECTION d = (DIRECTION) model.getValueAt(i, COLUMN_DIRECTION_IDX);
                if (d == direction) {
                    Date date = (Date) model.getValueAt(i, COLUMN_DATE_IDX);
                    if (dateLow == null || date.before(dateLow)) {
                        dateLow = date;
                    }
                    Date endDate = (Date) model.getValueAt(i, COLUMN_END_DATE_IDX);
                    if (dateHigh == null || endDate.after(dateHigh)) {
                        dateHigh = endDate;
                    }
                }
            }
            RegularTimePeriod month = new Month(dateLow);
            while (month.getStart().getTime() <= dateHigh.getTime()) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    DIRECTION d = (DIRECTION) model.getValueAt(i, COLUMN_DIRECTION_IDX);
                    Date date = (Date) model.getValueAt(i, COLUMN_DATE_IDX);
                    Date endDate = (Date) model.getValueAt(i, COLUMN_END_DATE_IDX);
                    if (d == direction && date.getTime() >= dateLow.getTime() && endDate.getTime() <= dateHigh.getTime()) {
                        String type = (String) model.getValueAt(i, COLUMN_TYPE_IDX);
                        Number num = 0;
                        if (dataset.getKeys().contains(type)) {
                            num = dataset.getValue(type);
                        }
                        num = num.floatValue() + (Float) model.getValueAt(i, COLUMN_AMOUNT_IDX);
                        dataset.setValue(type, num);
                    }
                }
                month = month.next();
            }
        }
    }

    private Image buildPieChart(String title, PieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart3D(title, dataset, false, true, false);

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {1}"));

        BufferedImage image = chart.createBufferedImage(500, 300);
        return image;
    }

    private Image buildTimeSeriesChart(String title, String valueTitle, TimeSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Date", valueTitle, dataset, true, true, false);

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDrawingSupplier(new DefaultDrawingSupplier(new Paint[] { Color.BLUE, Color.RED },
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            r.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
            r.setBaseItemLabelsVisible(true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

        BufferedImage image = chart.createBufferedImage(1000, 500);
        return image;
    }

}
