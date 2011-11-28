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
import java.util.Arrays;
import java.util.Calendar;
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
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

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

import bias.extension.EntryExtension;
import bias.extension.FinancialFlows.xmlb.Direction;
import bias.extension.FinancialFlows.xmlb.ObjectFactory;
import bias.extension.FinancialFlows.xmlb.Parts;
import bias.extension.FinancialFlows.xmlb.RegularFlow;
import bias.extension.FinancialFlows.xmlb.RegularFlows;
import bias.extension.FinancialFlows.xmlb.SingleFlow;
import bias.extension.FinancialFlows.xmlb.SingleFlows;
import bias.gui.FrontEnd;
import bias.utils.CommonUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooserCellEditor;

/**
 * @author kion
 */

public class FinancialFlows extends EntryExtension {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_ADD = new ImageIcon(CommonUtils.getResourceURL(FinancialFlows.class, "add.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(CommonUtils.getResourceURL(FinancialFlows.class, "delete.png"));
    private static final ImageIcon ICON_CHART1 = new ImageIcon(CommonUtils.getResourceURL(FinancialFlows.class, "chart1.png"));
    private static final ImageIcon ICON_CHART2 = new ImageIcon(CommonUtils.getResourceURL(FinancialFlows.class, "chart2.png"));
    private static final ImageIcon ICON_SINGLE = new ImageIcon(CommonUtils.getResourceURL(FinancialFlows.class, "single.png"));
    private static final ImageIcon ICON_REGULAR = new ImageIcon(CommonUtils.getResourceURL(FinancialFlows.class, "regular.png"));
    private static final ImageIcon ICON_BALANCE = new ImageIcon(CommonUtils.getResourceURL(FinancialFlows.class, "balance.png"));
    
    private static enum DIRECTION {
        INCOME, OUTGO
    }

    private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/addons/FinancialFlowsSchema.xsd";

    private static final String PROPERTY_ACTIVE_TAB = "ACTIVE_TAB";

    private static final String PROPERTY_COLUMNS_WIDTHS_SINGLE = "COLUMNS_WIDTHS_SINGLE";

    private static final String PROPERTY_COLUMNS_WIDTHS_REGULAR = "COLUMNS_WIDTHS_REGULAR";

    private static final int MAX_SORT_KEYS_NUMBER = 4;
    
    private static final String PROPERTY_SORT_BY_COLUMN_SINGLE = "SORT_BY_COLUMN_SINGLE";
    
    private static final String PROPERTY_SORT_ORDER_SINGLE = "SORT_BY_ORDER_SINGLE";

    private static final String PROPERTY_SORT_BY_COLUMN_REGULAR = "SORT_BY_COLUMN_REGULAR";
    
    private static final String PROPERTY_SORT_ORDER_REGULAR = "SORT_BY_ORDER_REGULAR";

    private static final String PROPERTY_INCOME_TYPES = "INCOME_TYPES";

    private static final String PROPERTY_OUTGO_TYPES = "OUTGO_TYPES";

    private static final String PROPERTY_BALANCE_CORRECTION = "BALANCE_CORRECTION";
    
    private static final String SEPARATOR_PATTERN = "\\s*,\\s*";

    private static final int COLUMN_DIRECTION_IDX = 0;

    private static final int COLUMN_AMOUNT_IDX = 1;

    private static final int COLUMN_TYPE_IDX = 2;

    private static final int COLUMN_PURPOSE_IDX = 3;

    private static final int COLUMN_DATE_IDX = 4;

    private static final int COLUMN_END_DATE_IDX = 5;

    private static JAXBContext context;

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    private static ObjectFactory objFactory = new ObjectFactory();

    private TableRowSorter<TableModel> singleSorter;

    private TableRowSorter<TableModel> regularSorter;

    private int[] sortByColumnSingle = new int[MAX_SORT_KEYS_NUMBER];
    
    private SortOrder[] sortOrderSingle = new SortOrder[MAX_SORT_KEYS_NUMBER];

    private int[] sortByColumnRegular = new int[MAX_SORT_KEYS_NUMBER];
    
    private SortOrder[] sortOrderRegular = new SortOrder[MAX_SORT_KEYS_NUMBER];
    
    private double balanceCorrection = 0;

    private String currency;
    
    private Properties s;

    private String[] incomeTypes;

    private String[] oldIncomeTypes;

    private String[] outgoTypes;

    private String[] oldOutgoTypes;

    private JToolBar jToolBar1;

    private JButton jButton2;

    private JButton jButton3;

    private JButton jButton4;

    private JButton jButton5;

    private JButton jButton6;

    private JButton jButton7;

    private JPanel jPanel1;
    
    private JTextField filterText;

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
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        }
        return marshaller;
    }

    public FinancialFlows(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        initialize();
    }

    private void initialize() throws Throwable {
        int idx = -1;
        try {
            s = PropertiesUtils.deserializeProperties(getSettings());
            for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
                int sortByColumn = -1;
                String sortByColumnStr = s.getProperty(PROPERTY_SORT_BY_COLUMN_SINGLE + i);
                if (!Validator.isNullOrBlank(sortByColumnStr)) {
                    sortByColumn = Integer.valueOf(sortByColumnStr);
                }
                this.sortByColumnSingle[i] = sortByColumn;
                SortOrder sortOrder = null;
                String sortOrderStr = s.getProperty(PROPERTY_SORT_ORDER_SINGLE + i);
                if (!Validator.isNullOrBlank(sortOrderStr)) {
                    sortOrder = SortOrder.valueOf(sortOrderStr);
                }
                this.sortOrderSingle[i] = sortOrder;
                sortByColumn = -1;
                sortByColumnStr = s.getProperty(PROPERTY_SORT_BY_COLUMN_REGULAR + i);
                if (!Validator.isNullOrBlank(sortByColumnStr)) {
                    sortByColumn = Integer.valueOf(sortByColumnStr);
                }
                this.sortByColumnRegular[i] = sortByColumn;
                sortOrder = null;
                sortOrderStr = s.getProperty(PROPERTY_SORT_ORDER_REGULAR + i);
                if (!Validator.isNullOrBlank(sortOrderStr)) {
                    sortOrder = SortOrder.valueOf(sortOrderStr);
                }
                this.sortOrderRegular[i] = sortOrder;
            }
            if (getData() != null && getData().length != 0) {
                Parts parts = (Parts) getUnmarshaller().unmarshal(new ByteArrayInputStream(getData()));
                populateSingleTable(parts.getSingle());
                populateRegularTable(parts.getRegular());
            }
            String colW = s.getProperty(PROPERTY_COLUMNS_WIDTHS_SINGLE);
            if (!Validator.isNullOrBlank(colW)) {
                String[] colsWs = colW.split(":");
                int cc = getJTableSingle().getColumnModel().getColumnCount();
                for (int i = 0; i < cc; i++) {
                    getJTableSingle().getColumnModel().getColumn(i).setPreferredWidth(Integer.valueOf(colsWs[i]));
                }
            }
            colW = s.getProperty(PROPERTY_COLUMNS_WIDTHS_REGULAR);
            if (!Validator.isNullOrBlank(colW)) {
                String[] colsWs = colW.split(":");
                int cc = getJTableRegular().getColumnModel().getColumnCount();
                for (int i = 0; i < cc; i++) {
                    getJTableRegular().getColumnModel().getColumn(i).setPreferredWidth(Integer.valueOf(colsWs[i]));
                }
            }
            String atStr = s.getProperty(PROPERTY_ACTIVE_TAB);
            if (!Validator.isNullOrBlank(atStr)) {
                idx = Integer.valueOf(atStr);
            }
            String bSubtr = s.getProperty(PROPERTY_BALANCE_CORRECTION);
            if (!Validator.isNullOrBlank(bSubtr)) {
                balanceCorrection = Double.valueOf(bSubtr);
            }
        } catch (Throwable t) {
            FrontEnd.displayErrorMessage("Failed to initialize data/settings!", t);
            throw t;
        }
        initGUI();
        applySettings(getSettings());
        if (idx != -1) {
            jTabbedPane1.setSelectedIndex(idx);
        }
    }

    private void populateSingleTable(SingleFlows flows) {
        for (SingleFlow f : flows.getFlow()) {
            DIRECTION direction = DIRECTION.valueOf(f.getDirection().name());
            addRow(direction, f.getType(), f.getPurpose(), f.getDate() == null ? null : f.getDate().toGregorianCalendar().getTime(), f.getAmount());
        }
    }

    private void populateRegularTable(RegularFlows flows) {
        for (RegularFlow f : flows.getFlow()) {
            DIRECTION direction = DIRECTION.valueOf(f.getDirection().name());
            addRow(direction, f.getType(), f.getPurpose(), 
                    f.getDate() == null ? null : f.getDate().toGregorianCalendar().getTime(), 
                            f.getEndDate() == null ? null : f.getEndDate().toGregorianCalendar().getTime(), f.getAmount());
        }
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        Parts parts = objFactory.createParts();
        parts.setSingle(serializeSingleFlows());
        parts.setRegular(serializeRegularFlows());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getMarshaller().marshal(parts, baos);
        return baos.toByteArray();
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        if (currency != null) {
            s.setProperty("CURRENCY", currency);
        }
        for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
            if (sortByColumnSingle[i] != -1 && sortOrderSingle[i] != null) {
                s.setProperty(PROPERTY_SORT_BY_COLUMN_SINGLE + i, "" + sortByColumnSingle[i]);
                s.setProperty(PROPERTY_SORT_ORDER_SINGLE + i, sortOrderSingle[i].name());
            } else {
                s.remove(PROPERTY_SORT_BY_COLUMN_SINGLE + i);
                s.remove(PROPERTY_SORT_ORDER_SINGLE + i);
            }
            if (sortByColumnRegular[i] != -1 && sortOrderRegular[i] != null) {
                s.setProperty(PROPERTY_SORT_BY_COLUMN_REGULAR + i, "" + sortByColumnRegular[i]);
                s.setProperty(PROPERTY_SORT_ORDER_REGULAR + i, sortOrderRegular[i].name());
            } else {
                s.remove(PROPERTY_SORT_BY_COLUMN_REGULAR + i);
                s.remove(PROPERTY_SORT_ORDER_REGULAR + i);
            }
        }
        StringBuffer colW = new StringBuffer();
        int cc = getJTableSingle().getColumnModel().getColumnCount();
        for (int i = 0; i < cc; i++) {
            colW.append(getJTableSingle().getColumnModel().getColumn(i).getWidth());
            if (i < cc - 1) {
                colW.append(":");
            }
        }
        s.setProperty(PROPERTY_COLUMNS_WIDTHS_SINGLE, colW.toString());
        colW = new StringBuffer();
        cc = getJTableRegular().getColumnModel().getColumnCount();
        for (int i = 0; i < cc; i++) {
            colW.append(getJTableRegular().getColumnModel().getColumn(i).getWidth());
            if (i < cc - 1) {
                colW.append(":");
            }
        }
        int idx = jTabbedPane1.getSelectedIndex();
        if (idx != -1) {
            s.setProperty(PROPERTY_ACTIVE_TAB, "" + idx);
        }
        s.setProperty(PROPERTY_COLUMNS_WIDTHS_REGULAR, colW.toString());
        s.setProperty(PROPERTY_BALANCE_CORRECTION, "" + balanceCorrection);
        return PropertiesUtils.serializeProperties(s);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#configure(byte[])
     */
    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        s = PropertiesUtils.deserializeProperties(settings);
        String incomeTypes = s.getProperty(PROPERTY_INCOME_TYPES);
        JTextField incomeTypesTF = new JTextField();
        if (!Validator.isNullOrBlank(incomeTypes)) {
            incomeTypesTF.setText(incomeTypes);
        }
        String outgoTypes = s.getProperty(PROPERTY_OUTGO_TYPES);
        JTextField outgoTypesTF = new JTextField();
        if (!Validator.isNullOrBlank(outgoTypes)) {
            outgoTypesTF.setText(outgoTypes);
        }
        int opt = JOptionPane.showConfirmDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{
                        new JLabel("Comma-separated list of income types"),
                        incomeTypesTF,
                        new JLabel("Comma-separated list of outgo types"),
                        outgoTypesTF
                }, 
                "Configuration", 
                JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            incomeTypes = incomeTypesTF.getText().trim();
            s.setProperty(PROPERTY_INCOME_TYPES, incomeTypes);
            outgoTypes = outgoTypesTF.getText().trim();
            s.setProperty(PROPERTY_OUTGO_TYPES, outgoTypes);
            byte[] bytes = PropertiesUtils.serializeProperties(s);
            setSettings(bytes);
            return bytes;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#applySettings(byte[])
     */
    @Override
    public void applySettings(byte[] settings) {
        Properties s = PropertiesUtils.deserializeProperties(settings);
        currency = s.getProperty("CURRENCY");
        String types = s.getProperty(PROPERTY_INCOME_TYPES);
        this.oldIncomeTypes = this.incomeTypes;
        if (!Validator.isNullOrBlank(types)) {
            this.incomeTypes = types.split(SEPARATOR_PATTERN);
        } else {
            this.incomeTypes = new String[]{};
        }
        types = s.getProperty(PROPERTY_OUTGO_TYPES);
        this.oldOutgoTypes = this.outgoTypes;
        if (!Validator.isNullOrBlank(types)) {
            this.outgoTypes = types.split(SEPARATOR_PATTERN);
        } else {
            this.outgoTypes = new String[]{};
        }
        initTablesCells();
    }

    private SingleFlows serializeSingleFlows() throws Exception {
        SingleFlows flows = objFactory.createSingleFlows();
        for (int i = 0; i < getJTableSingle().getModel().getRowCount(); i++) {
            SingleFlow flow = objFactory.createSingleFlow();
            Date date = (Date) getJTableSingle().getModel().getValueAt(i, COLUMN_DATE_IDX);
            if (date != null) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(date);
                flow.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
            } else {
                flow.setDate(null);
            }
            flow.setAmount((Float) getJTableSingle().getModel().getValueAt(i, COLUMN_AMOUNT_IDX));
            flow.setType((String) getJTableSingle().getModel().getValueAt(i, COLUMN_TYPE_IDX));
            flow.setPurpose((String) getJTableSingle().getModel().getValueAt(i, COLUMN_PURPOSE_IDX));
            DIRECTION direction = (DIRECTION) getJTableSingle().getModel().getValueAt(i, COLUMN_DIRECTION_IDX);
            flow.setDirection(Direction.fromValue(direction.name()));
            flows.getFlow().add(flow);
        }
        return flows;
    }

    private RegularFlows serializeRegularFlows() throws Exception {
        RegularFlows flows = objFactory.createRegularFlows();
        for (int i = 0; i < getJTableRegular().getModel().getRowCount(); i++) {
            RegularFlow flow = objFactory.createRegularFlow();
            Date date = (Date) getJTableRegular().getModel().getValueAt(i, COLUMN_DATE_IDX);
            if (date != null) {
                GregorianCalendar cal1 = new GregorianCalendar();
                cal1.setTime(date);
                flow.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal1));
            } else {
                flow.setDate(null);
            }
            date = (Date) getJTableRegular().getModel().getValueAt(i, COLUMN_END_DATE_IDX);
            if (date != null) {
                GregorianCalendar cal2 = new GregorianCalendar();
                cal2.setTime(date);
                flow.setEndDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal2));
            } else {
                flow.setEndDate(null);
            }
            flow.setAmount((Float) getJTableRegular().getModel().getValueAt(i, COLUMN_AMOUNT_IDX));
            flow.setType((String) getJTableRegular().getModel().getValueAt(i, COLUMN_TYPE_IDX));
            flow.setPurpose((String) getJTableRegular().getModel().getValueAt(i, COLUMN_PURPOSE_IDX));
            DIRECTION direction = (DIRECTION) getJTableRegular().getModel().getValueAt(i, COLUMN_DIRECTION_IDX);
            flow.setDirection(Direction.fromValue(direction.name()));
            flows.getFlow().add(flow);
        }
        return flows;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.EntryExtension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        Collection<String> searchData = new ArrayList<String>();
        searchData.addAll(Arrays.asList(incomeTypes));
        searchData.addAll(Arrays.asList(outgoTypes));
        populateSearchData(searchData, getJTableSingle());
        populateSearchData(searchData, getJTableRegular());
        return searchData;
    }
    
    private void populateSearchData(Collection<String> searchData, JTable table) {
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            searchData.add(table.getModel().getValueAt(i, COLUMN_AMOUNT_IDX).toString());
            searchData.add(table.getModel().getValueAt(i, COLUMN_PURPOSE_IDX).toString());
        }
    }

    private void initGUI() throws Throwable {
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
                                JPanel p = new JPanel(new GridLayout(ir ? 11 : 9, 1));
                                p.add(new JLabel("Direction"));
                                final JComboBox directionCombo = new JComboBox();
                                directionCombo.addItem(DIRECTION.INCOME);
                                directionCombo.addItem(DIRECTION.OUTGO);
                                directionCombo.setSelectedItem(DIRECTION.INCOME);
                                p.add(directionCombo);
                                p.add(new JLabel("Type"));
                                final JComboBox typeCombo = new JComboBox(incomeTypes);
                                typeCombo.setEditable(false);
                                p.add(typeCombo);
                                directionCombo.addItemListener(new ItemListener(){
                                    @Override
                                    public void itemStateChanged(ItemEvent e) {
                                        if (e.getStateChange() == ItemEvent.SELECTED) {
                                            typeCombo.removeAllItems();
                                            for (String type : (DIRECTION.INCOME.equals(directionCombo.getSelectedItem()) ? incomeTypes : outgoTypes)) {
                                                typeCombo.addItem(type);
                                            }
                                        }
                                    }
                                });
                                p.add(new JLabel(ir ? "Start date" : "Date"));
                                JDateChooser dateChooser = new JDateChooser(new Date());
                                p.add(dateChooser);
                                JDateChooser endDateChooser = null;
                                if (ir) {
                                    p.add(new JLabel("End date (leave blank for ongoing flows)"));
                                    endDateChooser = new JDateChooser();
                                    p.add(endDateChooser);
                                }
                                p.add(new JLabel("Purpose"));
                                JTextField purposeTF = new JTextField();
                                p.add(purposeTF);
                                p.add(new JLabel("Amount"));
                                String amountStr = JOptionPane.showInputDialog(getParent(), p);
                                if (amountStr != null) {
                                    try {
                                        Float amount = Float.valueOf(amountStr);
                                        DIRECTION direction = (DIRECTION) directionCombo.getSelectedItem();
                                        String purpose = purposeTF.getText();
                                        String type = (String) typeCombo.getSelectedItem();
                                        if (ir) {
                                            addRow(direction, type, purpose, dateChooser.getDate(), endDateChooser.getDate(), amount);
                                        } else {
                                            addRow(direction, type, purpose, dateChooser.getDate(), amount);
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
                    {
                        jButton6 = new JButton();
                        jToolBar1.add(jButton6);
                        jButton6.setIcon(ICON_BALANCE);
                        jButton6.setToolTipText("Balance");
                        jButton6.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                try {
                                    DefaultPieDataset iDataset = new DefaultPieDataset();
                                    populatePieDataset(DIRECTION.INCOME, iDataset);
                                    DefaultPieDataset oDataset = new DefaultPieDataset();
                                    populatePieDataset(DIRECTION.OUTGO, oDataset);
                                    double income = 0D;
                                    for (int i = 0; i < iDataset.getItemCount(); i++) {
                                        income += iDataset.getValue(i).doubleValue();
                                    }
                                    double outgo = 0D;
                                    for (int i = 0; i < oDataset.getItemCount(); i++) {
                                        outgo += oDataset.getValue(i).doubleValue();
                                    }
                                    Component[] cmp = new Component[]{
                                            new JLabel("Total balance: " + (income - outgo)),
                                            new JLabel("Corrected balance: " + (income - outgo + balanceCorrection))
                                    };
                                    JOptionPane.showMessageDialog(FinancialFlows.this, cmp);
                                } catch (Exception e) {
                                    FrontEnd.displayErrorMessage(e);
                                }
                            }
                        });
                    }
                    {
                        jButton7 = new JButton("*$*");
                        jToolBar1.add(jButton7);
                        jButton7.setToolTipText("Balance correction");
                        jButton7.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                try {
                                    String str = JOptionPane.showInputDialog(FinancialFlows.this, new JLabel("Input balance correction: "));
                                    if (!Validator.isNullOrBlank(str)) {
                                        balanceCorrection += Double.valueOf(str);
                                    }
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
                    filterText = new JTextField();
                    filterText.addCaretListener(new CaretListener(){
                        @SuppressWarnings("unchecked")
                        public void caretUpdate(CaretEvent e) {
                            TableRowSorter<TableModel> sorterSingle = (TableRowSorter<TableModel>) getJTableSingle().getRowSorter();
                            sorterSingle.setRowFilter(RowFilter.regexFilter("(?i)" + filterText.getText()));
                            TableRowSorter<TableModel> sorterRegular = (TableRowSorter<TableModel>) getJTableRegular().getRowSorter();
                            sorterRegular.setRowFilter(RowFilter.regexFilter("(?i)" + filterText.getText()));
                        }
                    });
                    JPanel filterPanel = new JPanel(new BorderLayout());
                    filterPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
                    filterPanel.add(filterText, BorderLayout.CENTER);
                    jPanel1.add(filterPanel, BorderLayout.NORTH);
                    jTabbedPane1 = new JTabbedPane();
                    jPanel1.add(jTabbedPane1, BorderLayout.CENTER);
                    {
                        jTabbedPane1.addTab("Single Flows", ICON_SINGLE, new JScrollPane(getJTableSingle()));
                        jTabbedPane1.addTab("Regular Flows", ICON_REGULAR, new JScrollPane(getJTableRegular()));
                    }
                }
                initTablesCells();
            }
        } catch (Throwable t) {
            FrontEnd.displayErrorMessage("Failed to initialize GUI!", t);
            throw t;
        }
    }

    private void initTablesCells() {
        initTableCells(getJTableSingle());
        initTableCells(getJTableRegular());
    }
    
    private void initTableCells(JTable table) {
        if (table != null) {
            // refresh combo-boxes in table cells
            String[] types = null;
            String[] oldTypes = null;
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = 0; i < model.getRowCount(); i++){
                DIRECTION direction = (DIRECTION) table.getModel().getValueAt(i, COLUMN_DIRECTION_IDX);
                if (direction == DIRECTION.INCOME) {
                    types = incomeTypes;
                    oldTypes = oldIncomeTypes;
                } else if (direction == DIRECTION.OUTGO) {
                    types = outgoTypes;
                    oldTypes = oldOutgoTypes;
                }
                if (types != null) {
                    String type = (String) model.getValueAt(i, COLUMN_TYPE_IDX);
                    if (!Arrays.asList(types).contains(type)) {
                        String value = "";
                        if (types.length != 0) {
                            int idx = getElementIndex(oldTypes, type);
                            if (idx != -1 && idx < types.length) {
                                value = types[idx];
                            } else {
                                value = types[0];
                            }
                        }
                        model.setValueAt(value, i, COLUMN_TYPE_IDX);
                    }
                }
            }
        }
    }

    private int getElementIndex(String[] elements, String element) {
        if (elements != null) {
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals(element)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private boolean isRegularTableActive() {
        int idx = jTabbedPane1.getSelectedIndex();
        return idx == 1 ? true : false;
    }

    private void populateCombo(JComboBox combo, String[] values) {
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
        model.addColumn("Purpose");
        model.addColumn("Date");
        getJTableSingle().setModel(model);
        getJTableSingle().setDefaultRenderer(Float.class, new CurrencyRenderer());
        getJTableSingle().getColumnModel().getColumn(COLUMN_DATE_IDX).setCellEditor(new JDateChooserCellEditor());
        getJTableSingle().getColumnModel().getColumn(COLUMN_TYPE_IDX).setCellEditor(new ComboBoxCellEditor());
        getJTableSingle().getColumnModel().removeColumn(getJTableSingle().getColumnModel().getColumn(COLUMN_DIRECTION_IDX));
        singleSorter = new TableRowSorter<TableModel>(model);
        singleSorter.setSortsOnUpdates(true);
        singleSorter.setMaxSortKeys(MAX_SORT_KEYS_NUMBER);
        List<SortKey> sortKeys = new LinkedList<SortKey>();
        for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
            if (sortByColumnSingle[i] != -1 && sortOrderSingle[i] != null) {
                SortKey sortKey = new SortKey(sortByColumnSingle[i], sortOrderSingle[i]);
                sortKeys.add(sortKey);
            }
        }
        singleSorter.setSortKeys(sortKeys);
        singleSorter.addRowSorterListener(new RowSorterListener(){
            public void sorterChanged(RowSorterEvent e) {
                if (e.getType().equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
                    List<? extends SortKey> sortKeys = singleSorter.getSortKeys();
                    for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
                        if (i < sortKeys.size()) {
                            SortKey sortKey = sortKeys.get(i);
                            sortByColumnSingle[i] = sortKey.getColumn();
                            sortOrderSingle[i] = sortKey.getSortOrder();
                        } else {
                            sortByColumnSingle[i] = -1;
                            sortOrderSingle[i] = null;
                        }
                    }
                }
            }
        });
        getJTableSingle().setRowSorter(singleSorter);
    }

    private void initJTableRegular() {
        DefaultTableModel model = getJTableModel();
        model.addColumn("Direction");
        model.addColumn("Amount");
        model.addColumn("Type");
        model.addColumn("Purpose");
        model.addColumn("Start date");
        model.addColumn("End date");
        getJTableRegular().setModel(model);
        getJTableRegular().setDefaultRenderer(Float.class, new CurrencyRenderer());
        getJTableRegular().getColumnModel().getColumn(COLUMN_DATE_IDX).setCellEditor(new JDateChooserCellEditor());
        getJTableRegular().getColumnModel().getColumn(COLUMN_END_DATE_IDX).setCellEditor(new JDateChooserCellEditor());
        getJTableRegular().getColumnModel().getColumn(COLUMN_TYPE_IDX).setCellEditor(new ComboBoxCellEditor());
        getJTableRegular().getColumnModel().removeColumn(getJTableRegular().getColumnModel().getColumn(COLUMN_DIRECTION_IDX));
        regularSorter = new TableRowSorter<TableModel>(model);
        regularSorter.setSortsOnUpdates(true);
        regularSorter.setMaxSortKeys(MAX_SORT_KEYS_NUMBER);
        List<SortKey> sortKeys = new LinkedList<SortKey>();
        for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
            if (sortByColumnRegular[i] != -1 && sortOrderRegular[i] != null) {
                SortKey sortKey = new SortKey(sortByColumnRegular[i], sortOrderRegular[i]);
                sortKeys.add(sortKey);
            }
        }
        regularSorter.setSortKeys(sortKeys);
        regularSorter.addRowSorterListener(new RowSorterListener(){
            public void sorterChanged(RowSorterEvent e) {
                if (e.getType().equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
                    List<? extends SortKey> sortKeys = regularSorter.getSortKeys();
                    for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
                        if (i < sortKeys.size()) {
                            SortKey sortKey = sortKeys.get(i);
                            sortByColumnRegular[i] = sortKey.getColumn();
                            sortOrderRegular[i] = sortKey.getSortOrder();
                        } else {
                            sortByColumnRegular[i] = -1;
                            sortOrderRegular[i] = null;
                        }
                    }
                }
            }
        });
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
            row = table.getRowSorter().convertRowIndexToModel(row);
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

    private void addRow(DIRECTION direction, String type, String purpose, Date date, Float amount) {
        DefaultTableModel model = (DefaultTableModel) getJTableSingle().getModel();
        model.addRow(new Object[] { direction, amount, type, purpose, date });
    }

    private void addRow(DIRECTION direction, String type, String purpose, Date date, Date endDate, Float amount) {
        DefaultTableModel model = (DefaultTableModel) getJTableRegular().getModel();
        model.addRow(new Object[] { direction, amount, type, purpose, date, endDate });
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
            periodHigh = new Month(endDate == null ? new Date() : endDate);
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
        if (getJTableSingle().getRowCount() > 0 || getJTableRegular().getRowCount() > 0) {
            Date dateLow = null;
            Date dateHigh = null;
            TableModel model = getJTableSingle().getModel();
            for (int i = 0; i < getJTableSingle().getRowCount(); i++) {
                int idx = getJTableSingle().getRowSorter().convertRowIndexToModel(i);
                DIRECTION d = (DIRECTION) model.getValueAt(idx, COLUMN_DIRECTION_IDX);
                if (d == direction) {
                    Date date = (Date) model.getValueAt(idx, COLUMN_DATE_IDX);
                    if (dateLow == null || date.before(dateLow)) {
                        dateLow = date;
                    }
                    if (dateHigh == null || date.after(dateHigh)) {
                        dateHigh = date;
                    }
                    String type = (String) model.getValueAt(idx, COLUMN_TYPE_IDX);
                    Number num = 0;
                    if (dataset.getKeys().contains(type)) {
                        num = dataset.getValue(type);
                    }
                    num = num.floatValue() + (Float) model.getValueAt(idx, COLUMN_AMOUNT_IDX);
                    dataset.setValue(type, num);
                }
            }
            model = getJTableRegular().getModel();
            Calendar dateLowCal = new GregorianCalendar();
            dateLowCal.setTime(dateLow);
            Calendar dateHighCal = new GregorianCalendar();
            dateHighCal.setTime(dateHigh);
            boolean filteredBySingleMonth = dateLowCal.get(Calendar.YEAR) == dateHighCal.get(Calendar.YEAR) && dateLowCal.get(Calendar.MONTH) == dateHighCal.get(Calendar.MONTH);
            if (!Validator.isNullOrBlank(filterText.getText()) && filteredBySingleMonth) {
                for (int i = 0; i < getJTableRegular().getRowCount(); i++) {
                    int idx = getJTableRegular().getRowSorter().convertRowIndexToModel(i);
                    DIRECTION d = (DIRECTION) model.getValueAt(idx, COLUMN_DIRECTION_IDX);
                    if (d == direction) {
                        String type = (String) model.getValueAt(idx, COLUMN_TYPE_IDX);
                        Number num = 0;
                        if (dataset.getKeys().contains(type)) {
                            num = dataset.getValue(type);
                        }
                        num = num.floatValue() + (Float) model.getValueAt(idx, COLUMN_AMOUNT_IDX);
                        dataset.setValue(type, num);
                    }
                }
            } else {
                for (int i = 0; i < getJTableRegular().getRowCount(); i++) {
                    int idx = getJTableRegular().getRowSorter().convertRowIndexToModel(i);
                    DIRECTION d = (DIRECTION) model.getValueAt(idx, COLUMN_DIRECTION_IDX);
                    if (d == direction) {
                        Date date = (Date) model.getValueAt(idx, COLUMN_DATE_IDX);
                        if (date != null && (dateLow == null || date.before(dateLow))) {
                            dateLow = date;
                        }
                        Date endDate = (Date) model.getValueAt(idx, COLUMN_END_DATE_IDX);
                        if (endDate != null && (dateHigh == null || endDate.after(dateHigh))) {
                            dateHigh = endDate;
                        }
                    }
                }
                if (dateHigh == null) {
                    dateHigh = new Date();
                }
                if (dateLow != null && dateHigh != null) {
                    RegularTimePeriod month = new Month(dateLow);
                    while (month.getStart().getTime() <= dateHigh.getTime()) {
                        for (int i = 0; i < getJTableRegular().getRowCount(); i++) {
                            int idx = getJTableRegular().getRowSorter().convertRowIndexToModel(i);
                            DIRECTION d = (DIRECTION) model.getValueAt(idx, COLUMN_DIRECTION_IDX);
                            Date date = (Date) model.getValueAt(idx, COLUMN_DATE_IDX);
                            Date endDate = (Date) model.getValueAt(idx, COLUMN_END_DATE_IDX);
                            if (d == direction && (date != null && date.getTime() >= dateLow.getTime() && date.getTime() <= month.getStart().getTime()) && (endDate == null || (endDate.getTime() <= dateHigh.getTime() && endDate.getTime() >= month.getStart().getTime()))) {
                                String type = (String) model.getValueAt(idx, COLUMN_TYPE_IDX);
                                Number num = 0;
                                if (dataset.getKeys().contains(type)) {
                                    num = dataset.getValue(type);
                                }
                                num = num.floatValue() + (Float) model.getValueAt(idx, COLUMN_AMOUNT_IDX);
                                dataset.setValue(type, num);
                            }
                        }
                        month = month.next();
                    }
                }
            }
        }
    }

    private Image buildPieChart(String title, PieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart3D(title, dataset, false, true, false);

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {1}"));
        
        int w = FrontEnd.getMainWindowSize().width/10*8;
        int h = w/5*3;
        BufferedImage image = chart.createBufferedImage(w, h);
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

        int w = FrontEnd.getMainWindowSize().width/10*8;
        int h = w/5*3;
        BufferedImage image = chart.createBufferedImage(w, h);
        return image;
    }

}
