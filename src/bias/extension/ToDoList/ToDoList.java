/**
 * Created on Aug 6, 2007
 */
package bias.extension.ToDoList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.annotation.AddOnAnnotation;
import bias.extension.EntryExtension;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author kion
 */

@AddOnAnnotation(
        version="0.1.5",
        author="kion",
        description = "ToDo List")
public class ToDoList extends EntryExtension {

    private static final long serialVersionUID = 1L;
    
    private static final String XML_ELEMENT_ROOT = "root";
    private static final String XML_ELEMENT_ENTRY = "entry";
    private static final String XML_ELEMENT_TITLE = "title";
    private static final String XML_ELEMENT_DESCRIPTION = "description";
    private static final String XML_ELEMENT_ATTRIBUTE_ID = "id";
    private static final String XML_ELEMENT_ATTRIBUTE_TIMESTAMP = "timestamp";
    private static final String XML_ELEMENT_ATTRIBUTE_PRIORITY = "priority";
    private static final String XML_ELEMENT_ATTRIBUTE_STATUS = "status";
    
    private static final String PROPERTY_PRIORITIES = "PRIORITIES";
    private static final String PROPERTY_STATUSES = "STATUSES";
    private static final String PROPERTY_DATE_TIME_FORMAT = "DATE_FORMAT";
    
    private static final String SEPARATOR_PATTERN = "\\s*,\\s*";
    
    private static class ComboBoxEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        public ComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }
    
    private String dateTimeFormat = "yyyy-MM-dd HH:mm";
    
    private SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
    
    private byte[] settings = null;
    
    private String[] priorities = null;
    
    private String[] statuses = null;
    
    private Map<UUID, Date> originalTimestamps = new HashMap<UUID, Date>();
    
    private Map<UUID, String> descriptions = new HashMap<UUID, String>();
    
    private UUID currEntryID = null;
    
    private JPanel mainPanel = null;
    
    private JTable todoEntriesTable = null;
    
    private JTextPane textPane = null;

    public ToDoList(UUID id, byte[] data, byte[] settings) {
        super(id, data, settings);
        initialize();
    }
    
    private void initialize() {
        settings = getSettings();
        applySettings(settings);
        initGUI();
        parseData();
    }
    
    private void applySettings(byte[] settings) {
        Properties props = PropertiesUtils.deserializeProperties(settings);
        String priorities = props.getProperty(PROPERTY_PRIORITIES);
        String statuses = props.getProperty(PROPERTY_STATUSES);
        String dateTimeFormat = props.getProperty(PROPERTY_DATE_TIME_FORMAT);
        if (!Validator.isNullOrBlank(priorities)) {
            this.priorities = priorities.split(SEPARATOR_PATTERN);
        } else {
            this.priorities = new String[]{};
        }
        if (!Validator.isNullOrBlank(statuses)) {
            this.statuses = statuses.split(SEPARATOR_PATTERN);
        } else {
            this.statuses = new String[]{};
        }
        if (!Validator.isNullOrBlank(dateTimeFormat)) {
            this.dateTimeFormat = dateTimeFormat;
        }
        initTableCells();
    }
    
    private void initTableCells() {
        if (todoEntriesTable != null) {
            // refresh combo-boxes in table cells
            TableColumn col = todoEntriesTable.getColumnModel().getColumn(2);
            col.setCellEditor(new ComboBoxEditor(this.priorities));
            col = todoEntriesTable.getColumnModel().getColumn(3);
            col.setCellEditor(new ComboBoxEditor(this.statuses));
            // update table model:
            // * change date-time format
            // * change non-existing priority/status titles to first available
            SimpleDateFormat sdf = null;
            try {
                sdf = new SimpleDateFormat(dateTimeFormat);
            } catch (IllegalArgumentException iae) {
                FrontEnd.displayErrorMessage("Invalid Date-Time format!");
            }
            DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++){
                if (sdf != null) {
                    UUID id = UUID.fromString((String) model.getValueAt(i, 0));
                    Date timestamp = originalTimestamps.get(id);
                    model.setValueAt(sdf.format(timestamp), i, 1);
                }
                String priority = (String) model.getValueAt(i, 3);
                if (!Arrays.asList(this.priorities).contains(priority)) {
                    String value = "";
                    if (this.priorities.length != 0) {
                        value = this.priorities[0];
                    }
                    model.setValueAt(value, i, 3);
                }
                String status = (String) model.getValueAt(i, 4);
                if (!Arrays.asList(this.statuses).contains(status)) {
                    String value = "";
                    if (this.statuses.length != 0) {
                        value = this.statuses[0];
                    }
                    model.setValueAt(value, i, 4);
                }
            }
            this.sdf = sdf;
        }
    }
    
    private void parseData() {
        Document doc = null;
        try {
            if (getData() != null && getData().length != 0) {
                doc = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(getData()));
            }
        } catch (Exception e) {
            FrontEnd.displayErrorMessage("Failed to parse todo-list data!", e);
        }
        if (doc != null) {
            NodeList entryNodes = doc.getFirstChild().getChildNodes();
            for (int i = 0; i < entryNodes.getLength(); i++) {
                ToDoEntry todoEntry = new ToDoEntry();
                Node entryNode = entryNodes.item(i);
                if (entryNode.getNodeName().equals(XML_ELEMENT_ENTRY)) {
                    NodeList entryChildNodes = entryNode.getChildNodes();
                    for (int j = 0; j < entryChildNodes.getLength(); j++) {
                        Node entryChildNode = entryChildNodes.item(j);
                        if (entryChildNode.getNodeName().equals(XML_ELEMENT_TITLE)) {
                            String title = entryChildNode.getTextContent();
                            todoEntry.setTitle(title);
                        } else if (entryChildNode.getNodeName().equals(XML_ELEMENT_DESCRIPTION)) {
                            String description = entryChildNode.getTextContent();
                            todoEntry.setDescription(description);
                        }
                    }
                    NamedNodeMap attributes = entryNode.getAttributes();
                    Node attID = attributes.getNamedItem(XML_ELEMENT_ATTRIBUTE_ID);
                    if (attID != null) {
                        UUID id = UUID.fromString(attID.getNodeValue());
                        todoEntry.setId(id);
                    }
                    Node attTS = attributes.getNamedItem(XML_ELEMENT_ATTRIBUTE_TIMESTAMP);
                    if (attTS != null) {
                        long timestamp = Long.valueOf(attTS.getNodeValue());
                        todoEntry.setTimestamp(new Date(timestamp));
                    }
                    Node attPriority = attributes.getNamedItem(XML_ELEMENT_ATTRIBUTE_PRIORITY);
                    if (attPriority != null) {
                        String priority = attPriority.getNodeValue();
                        todoEntry.setPriority(priority);
                    }
                    Node attStatus = attributes.getNamedItem(XML_ELEMENT_ATTRIBUTE_STATUS);
                    if (attStatus != null) {
                        String status = attStatus.getNodeValue();
                        todoEntry.setStatus(status);
                    }
                    addToDoEntry(todoEntry);
                }
            }
        }
    }
    
    private void initGUI() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());
            JToolBar toolbar = initToolBar();
            DefaultTableModel model = new DefaultTableModel() {
                private static final long serialVersionUID = 1L;
                public boolean isCellEditable(int rowIndex, int mColIndex) {
                    if (mColIndex == 2 || mColIndex == 3 || mColIndex == 4) return true;
                    return false;
                }
            };
            todoEntriesTable = new JTable(model);
            todoEntriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
            todoEntriesTable.setRowSorter(sorter);
            model.addColumn("ID");
            model.addColumn("Timestamp");
            model.addColumn("Title");
            model.addColumn("Priority");
            model.addColumn("State");
            
            // hide ID column
            TableColumn idCol = todoEntriesTable.getColumnModel().getColumn(0);
            todoEntriesTable.getColumnModel().removeColumn(idCol);
            
            initTableCells();

            JPanel entriesPanel = new JPanel(new BorderLayout());
            final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setDividerSize(3);
            splitPane.setTopComponent(new JScrollPane(todoEntriesTable));
            splitPane.setBottomComponent(getTextPane());
            entriesPanel.add(splitPane, BorderLayout.CENTER);
            
            todoEntriesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                    DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
                    if (currEntryID != null) {
                        descriptions.put(currEntryID, getTextPane().getText());
                    }
                    int rn = todoEntriesTable.getSelectedRow();
                    if (rn == -1) {
                        getTextPane().setVisible(false);
                        currEntryID = null;
                    } else {
                        UUID id = UUID.fromString((String) model.getValueAt(rn, 0));
                        String text = descriptions.get(id);
                        if (text == null) {
                            text = "";
                        }
                        getTextPane().setText(text);
                        getTextPane().setVisible(true);
                        splitPane.setDividerLocation(0.5);
                        currEntryID = id;
                    }
                }
            });

            
            mainPanel.add(entriesPanel, BorderLayout.CENTER);
            final JTextField filterText = new JTextField();
            filterText.addCaretListener(new CaretListener(){
                @SuppressWarnings("unchecked")
                public void caretUpdate(CaretEvent e) {
                    TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) todoEntriesTable.getRowSorter();
                    sorter.setRowFilter(RowFilter.regexFilter(filterText.getText()));
                }
            });
            JPanel filterPanel = new JPanel(new BorderLayout());
            filterPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
            filterPanel.add(filterText, BorderLayout.CENTER);
            mainPanel.add(filterPanel, BorderLayout.NORTH);
            mainPanel.add(toolbar, BorderLayout.SOUTH);
            this.setLayout(new BorderLayout());
            this.add(mainPanel, BorderLayout.CENTER);
        }
    }
    
    private JTextPane getTextPane() {
        if (textPane == null) {
            textPane = new JTextPane();
            textPane.setVisible(false);
        }
        return textPane;
    }
    
    private JToolBar initToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton buttAdd = new JButton("add");
        buttAdd.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JComboBox priority = new JComboBox(priorities);
                JComboBox status = new JComboBox(statuses);
                String title = JOptionPane.showInputDialog(
                        ToDoList.this, 
                        new Component[]{priority, status},
                        "Entry title:",
                        JOptionPane.PLAIN_MESSAGE
                        );
                if (!Validator.isNullOrBlank(title)) {
                    ToDoEntry todoEntry = new ToDoEntry();
                    todoEntry.setId(UUID.randomUUID());
                    todoEntry.setTimestamp(new Date());
                    todoEntry.setTitle(title);
                    todoEntry.setDescription("");
                    todoEntry.setPriority((String) priority.getSelectedItem());
                    todoEntry.setStatus((String) status.getSelectedItem());
                    addToDoEntry(todoEntry);
                }
            }
        });
        JButton buttDel = new JButton("delete");
        buttDel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
                int i;
                while ((i = todoEntriesTable.getSelectedRow()) != -1) {
                    model.removeRow(i);
                }
            }
        });
        JButton buttConf = new JButton("configure");
        buttConf.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    byte[] oldSettings = settings;
                    settings = configure(settings);
                    if (!Arrays.equals(oldSettings, settings)) {
                        applySettings(settings);
                    }
                } catch (Throwable t) {
                    FrontEnd.displayErrorMessage(t);
                }
            }
        });
        toolbar.add(buttAdd);
        toolbar.add(buttDel);
        toolbar.add(buttConf);
        return toolbar;
    }
    
    private void addToDoEntry(ToDoEntry todoEntry) {
        
        DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();

        int idx = todoEntriesTable.getSelectedRow();
        if (idx != -1) {
            todoEntriesTable.getSelectionModel().removeSelectionInterval(0, idx);
        }
        if (currEntryID != null) {
            descriptions.put(currEntryID, getTextPane().getText());
            getTextPane().setVisible(false);
            currEntryID = null;
        }
        
        todoEntriesTable.setVisible(false);
        model.addRow(new Object[]{
                todoEntry.getId().toString(),
                sdf.format(todoEntry.getTimestamp()), 
                todoEntry.getTitle(),
                todoEntry.getPriority().trim(),
                todoEntry.getStatus().trim()
                });
        if (todoEntry.getId() != null && todoEntry.getTimestamp() != null) {
            originalTimestamps.put(todoEntry.getId(), todoEntry.getTimestamp());
        }
        if (todoEntry.getDescription() != null) {
            descriptions.put(todoEntry.getId(), todoEntry.getDescription());
        }
        todoEntriesTable.setVisible(true);
        
    }
    
    private Collection<ToDoEntry> getToDoEntries() {
        Collection<ToDoEntry> entries = new LinkedList<ToDoEntry>();
        DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                ToDoEntry entry = new ToDoEntry();
                UUID id = UUID.fromString((String) model.getValueAt(i, 0));
                entry.setId(id);
                entry.setTimestamp(originalTimestamps.get(id));
                entry.setTitle((String) model.getValueAt(i, 2));
                entry.setPriority((String) model.getValueAt(i, 3));
                entry.setStatus((String) model.getValueAt(i, 4));
                entry.setDescription(descriptions.get(id));
                entries.add(entry);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return entries;
    }

    @Override
    public byte[] serializeData() throws Throwable {
        Document doc = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = doc.createElement(XML_ELEMENT_ROOT);
        doc.appendChild(rootNode);
        for (ToDoEntry entry : getToDoEntries()) {
            Element entryNode = doc.createElement(XML_ELEMENT_ENTRY);
            Element titleNode = doc.createElement(XML_ELEMENT_TITLE);
            titleNode.setTextContent(entry.getTitle());
            entryNode.appendChild(titleNode);
            Element descriptionNode = doc.createElement(XML_ELEMENT_DESCRIPTION);
            descriptionNode.setTextContent(entry.getDescription());
            entryNode.appendChild(descriptionNode);
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_ID, entry.getId().toString());
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_TIMESTAMP, "" + entry.getTimestamp().getTime());
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_PRIORITY, entry.getPriority());
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_STATUS, entry.getStatus());
            rootNode.appendChild(entryNode);
        }
        OutputFormat of = new OutputFormat();
        of.setIndenting(true);
        of.setIndent(4);
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(doc);
        return sw.getBuffer().toString().getBytes();
    }

    @Override
    public byte[] serializeSettings() throws Throwable {
        return settings;
    }

    @Override
    public byte[] configure(byte[] settings) throws Throwable {
        Properties props = PropertiesUtils.deserializeProperties(settings);
        String priorities = props.getProperty(PROPERTY_PRIORITIES);
        String statuses = props.getProperty(PROPERTY_STATUSES);
        String dateTimeFormat = props.getProperty(PROPERTY_DATE_TIME_FORMAT);
        if (Validator.isNullOrBlank(dateTimeFormat)) {
            dateTimeFormat = this.dateTimeFormat;
        }
        JTextField prioritiesTF = new JTextField();
        if (!Validator.isNullOrBlank(priorities)) {
            prioritiesTF.setText(priorities);
        }
        JTextField statusesTF = new JTextField();
        if (!Validator.isNullOrBlank(statuses)) {
            statusesTF.setText(statuses);
        }
        final JTextField dateTimeFormatTF = new JTextField(dateTimeFormat);
        dateTimeFormatTF.setToolTipText(sdf.format(new Date()));
        final Color normal = dateTimeFormatTF.getForeground();
        dateTimeFormatTF.addCaretListener(new CaretListener(){
            public void caretUpdate(CaretEvent e) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormatTF.getText());
                    dateTimeFormatTF.setForeground(normal);
                    dateTimeFormatTF.setToolTipText(sdf.format(new Date()));
                } catch (IllegalArgumentException iae) {
                    dateTimeFormatTF.setForeground(Color.RED);
                    dateTimeFormatTF.setToolTipText("Invalid date-time format!");
                }
            }
        });
        int opt = JOptionPane.showConfirmDialog(
                ToDoList.this, 
                new Component[]{
                        new JLabel("Comma-separated list of priorities:"),
                        prioritiesTF,
                        new JLabel("Comma-separated list of statuses:"),
                        statusesTF,
                        new JLabel("Date-Time format:"),
                        dateTimeFormatTF
                }, 
                "Configuration", 
                JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            if (!Validator.isNullOrBlank(prioritiesTF.getText())) {
                priorities = prioritiesTF.getText().trim();
                props.setProperty(PROPERTY_PRIORITIES, priorities);
            }
            if (!Validator.isNullOrBlank(statusesTF.getText())) {
                statuses = statusesTF.getText().trim();
                props.setProperty(PROPERTY_STATUSES, statuses);
            }
            if (!Validator.isNullOrBlank(dateTimeFormatTF.getText())) {
                dateTimeFormat = dateTimeFormatTF.getText().trim();
                props.setProperty(PROPERTY_DATE_TIME_FORMAT, dateTimeFormat);
            }
        }
        return PropertiesUtils.serializeProperties(props);
    }
    
}
