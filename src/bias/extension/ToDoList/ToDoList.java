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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.Constants;
import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.EntryExtension;
import bias.extension.ToDoList.editor.HTMLEditorPanel;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author kion
 */

public class ToDoList extends EntryExtension {
    private static final long serialVersionUID = 1L;
    
    // TODO [P2] column widths should be stored as relative (% of whole table width) values
    
    private static final ImageIcon ICON_ADD = new ImageIcon(BackEnd.getInstance().getResourceURL(ToDoList.class, "add.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(BackEnd.getInstance().getResourceURL(ToDoList.class, "del.png"));

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
    private static final String PROPERTY_SORT_BY_COLUMN = "SORT_BY_COLUMN";
    private static final String PROPERTY_SORT_ORDER = "SORT_BY_ORDER";
    private static final String PROPERTY_SELECTED_ROW = "SELECTED_ROW";
    private static final String PROPERTY_DIVIDER_LOCATION = "DIVIDER_LOCATION";
    private static final String PROPERTY_COLUMNS_WIDTHS = "COLUMNS_WIDTHS";
    private static final String PROPERTY_SCROLLBAR_VERT = "SCROLLBAR_VERT";
    private static final String PROPERTY_SCROLLBAR_HORIZ = "SCROLLBAR_HORIZ";
    private static final String PROPERTY_CARET_POSITION = "CARET_POSITION";
    
    private static final String SEPARATOR_PATTERN = "\\s*,\\s*";
    
    private static final int MAX_SORT_KEYS_NUMBER = 4;
    
    private static class ComboBoxEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        public ComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }
    
    private String dateTimeFormat = "yyyy-MM-dd HH:mm";
    
    private int[] sortByColumn = new int[MAX_SORT_KEYS_NUMBER];
    
    private SortOrder[] sortOrder = new SortOrder[MAX_SORT_KEYS_NUMBER];
    
    private SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
    
    private String[] priorities = null;
    
    private String[] statuses = null;
    
    private String[] oldPriorities = null;
    
    private String[] oldStatuses = null;
    
    private Map<UUID, Date> originalTimestamps = new HashMap<UUID, Date>();
    
    private Map<UUID, HTMLEditorPanel> editorPanels = new HashMap<UUID, HTMLEditorPanel>();

    private JPanel mainPanel = null;
    
    private JTable todoEntriesTable = null;
    
    private JSplitPane splitPane = null;
    
    private TableRowSorter<TableModel> sorter;
    
    private Properties props;
    
    public ToDoList(UUID id, byte[] data, byte[] settings) {
        super(id, data, settings);
        initialize();
        revalidate();
    }
    
    private void initialize() {
        applySettings(getSettings());
        initGUI();
        parseData();
        String selRow = props.getProperty(PROPERTY_SELECTED_ROW);
        if (!Validator.isNullOrBlank(selRow) && todoEntriesTable.getRowCount() > 0 && todoEntriesTable.getRowCount() > Integer.valueOf(selRow)) {
            todoEntriesTable.setRowSelectionInterval(Integer.valueOf(selRow), Integer.valueOf(selRow));
        }
        String divLoc = props.getProperty(PROPERTY_DIVIDER_LOCATION);
        if (!Validator.isNullOrBlank(divLoc)) {
            splitPane.setDividerLocation(Integer.valueOf(divLoc));
        }
        String colW = props.getProperty(PROPERTY_COLUMNS_WIDTHS);
        if (!Validator.isNullOrBlank(colW)) {
            String[] colsWs = colW.split(":");
            int cc = todoEntriesTable.getColumnModel().getColumnCount();
            for (int i = 0; i < cc; i++) {
                todoEntriesTable.getColumnModel().getColumn(i).setPreferredWidth(Integer.valueOf(colsWs[i]));
            }
        }
        if (splitPane.getBottomComponent() != null) {
            HTMLEditorPanel htmlEditorPanel = ((HTMLEditorPanel) splitPane.getBottomComponent());
            JScrollPane sc = ((JScrollPane) htmlEditorPanel.getComponent(0));
            JScrollBar sb = sc.getVerticalScrollBar();
            if (sb != null) {
                String val = props.getProperty(PROPERTY_SCROLLBAR_VERT);
                if (val != null) {
                    sb.setVisibleAmount(0);
                    sb.setValue(sb.getMaximum());
                    sb.setValue(Integer.valueOf(val));
                }
            }
            sb = sc.getHorizontalScrollBar();
            if (sb != null) {
                String val = props.getProperty(PROPERTY_SCROLLBAR_HORIZ);
                if (val != null) {
                    sb.setVisibleAmount(0);
                    sb.setValue(sb.getMaximum());
                    sb.setValue(Integer.valueOf(val));
                }
            }
            String caretPos = props.getProperty(PROPERTY_CARET_POSITION);
            if (!Validator.isNullOrBlank(caretPos)) {
                htmlEditorPanel.setCaretPosition(Integer.valueOf(caretPos));
            }
        }
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#applySettings(byte[])
     */
    @Override
    public void applySettings(byte[] settings) {
        props = PropertiesUtils.deserializeProperties(settings);
        String priorities = props.getProperty(PROPERTY_PRIORITIES);
        String statuses = props.getProperty(PROPERTY_STATUSES);
        String dateTimeFormat = props.getProperty(PROPERTY_DATE_TIME_FORMAT);
        this.oldPriorities = this.priorities;
        if (!Validator.isNullOrBlank(priorities)) {
            this.priorities = priorities.split(SEPARATOR_PATTERN);
        } else {
            this.priorities = new String[]{};
        }
        this.oldStatuses = this.statuses;
        if (!Validator.isNullOrBlank(statuses)) {
            this.statuses = statuses.split(SEPARATOR_PATTERN);
        } else {
            this.statuses = new String[]{};
        }
        if (!Validator.isNullOrBlank(dateTimeFormat)) {
            this.dateTimeFormat = dateTimeFormat;
        }
        for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
            int sortByColumn = -1;
            String sortByColumnStr = props.getProperty(PROPERTY_SORT_BY_COLUMN + i);
            if (!Validator.isNullOrBlank(sortByColumnStr)) {
                sortByColumn = Integer.valueOf(sortByColumnStr);
            }
            this.sortByColumn[i] = sortByColumn;
            SortOrder sortOrder = null;
            String sortOrderStr = props.getProperty(PROPERTY_SORT_ORDER + i);
            if (!Validator.isNullOrBlank(sortOrderStr)) {
                sortOrder = SortOrder.valueOf(sortOrderStr);
            }
            this.sortOrder[i] = sortOrder;
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
            sdf = new SimpleDateFormat(dateTimeFormat);
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
                        int idx = getElementIndex(oldPriorities, priority);
                        if (idx != -1 && idx < this.priorities.length) {
                            value = this.priorities[idx];
                        } else {
                            value = this.priorities[0];
                        }
                    }
                    model.setValueAt(value, i, 3);
                }
                String status = (String) model.getValueAt(i, 4);
                if (!Arrays.asList(this.statuses).contains(status)) {
                    String value = "";
                    if (this.statuses.length != 0) {
                        int idx = getElementIndex(oldStatuses, status);
                        if (idx != -1 && idx < this.statuses.length) {
                            value = this.statuses[idx];
                        } else {
                            value = this.statuses[0];
                        }
                    }
                    model.setValueAt(value, i, 4);
                }
            }
        }
    }
    
    private int getElementIndex(String[] elements, String element) {
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }
    
    private void parseData() {
        Document doc = null;
        try {
            if (getData() != null && getData().length != 0) {
                doc = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(getData()));
            }
        } catch (Exception e) {
            FrontEnd.displayErrorMessage("Failed to parse todo-list XML data file!", e);
        }
        try {
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
                                String decodedText = URLDecoder.decode(entryChildNode.getTextContent(), Constants.UNICODE_ENCODING);
                                todoEntry.setTitle(decodedText);
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
                            String decodedText = URLDecoder.decode(attPriority.getNodeValue(), Constants.UNICODE_ENCODING);
                            todoEntry.setPriority(decodedText);
                        }
                        Node attStatus = attributes.getNamedItem(XML_ELEMENT_ATTRIBUTE_STATUS);
                        if (attStatus != null) {
                            String decodedText = URLDecoder.decode(attStatus.getNodeValue(), Constants.UNICODE_ENCODING);
                            todoEntry.setStatus(decodedText);
                        }
                        addToDoEntry(todoEntry);
                    }
                }
            }
        } catch (Exception e) {
            FrontEnd.displayErrorMessage("Failed to parse todo-list data!", e);
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
            
            model.addColumn("ID");
            model.addColumn("Timestamp");
            model.addColumn("Title");
            model.addColumn("Priority");
            model.addColumn("Status");
            
            // hide ID column
            TableColumn idCol = todoEntriesTable.getColumnModel().getColumn(0);
            todoEntriesTable.getColumnModel().removeColumn(idCol);
            
            sorter = new TableRowSorter<TableModel>(model);
            sorter.setSortsOnUpdates(true);
            sorter.setMaxSortKeys(MAX_SORT_KEYS_NUMBER);
            List<SortKey> sortKeys = new LinkedList<SortKey>();
            for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
                if (sortByColumn[i] != -1 && sortOrder[i] != null) {
                    SortKey sortKey = new SortKey(sortByColumn[i], sortOrder[i]);
                    sortKeys.add(sortKey);
                }
            }
            sorter.setSortKeys(sortKeys);
            sorter.addRowSorterListener(new RowSorterListener(){
                public void sorterChanged(RowSorterEvent e) {
                    if (e.getType().equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
                        List<? extends SortKey> sortKeys = sorter.getSortKeys();
                        for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
                            if (i < sortKeys.size()) {
                                SortKey sortKey = sortKeys.get(i);
                                sortByColumn[i] = sortKey.getColumn();
                                sortOrder[i] = sortKey.getSortOrder();
                            } else {
                                sortByColumn[i] = -1;
                                sortOrder[i] = null;
                            }
                        }
                    }
                }
            });
            todoEntriesTable.setRowSorter(sorter);
            
            initTableCells();

            JPanel entriesPanel = new JPanel(new BorderLayout());
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setDividerSize(3);
            splitPane.setTopComponent(new JScrollPane(todoEntriesTable));
            entriesPanel.add(splitPane, BorderLayout.CENTER);
            
            todoEntriesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
                        int rn = todoEntriesTable.getSelectedRow();
                        if (rn == -1) {
                            splitPane.setBottomComponent(null);
                        } else {
                            int dl = -1;
                            if (splitPane.getBottomComponent() != null) {
                                dl = splitPane.getDividerLocation();
                            }
                            rn = todoEntriesTable.convertRowIndexToModel(rn);
                            UUID id = UUID.fromString((String) model.getValueAt(rn, 0));
                            splitPane.setBottomComponent(editorPanels.get(id));
                            if (dl != -1) {
                                splitPane.setDividerLocation(dl);
                            } else {
                                splitPane.setDividerLocation(0.5);
                            }
                        }
                    }
                }
            });

            mainPanel.add(entriesPanel, BorderLayout.CENTER);
            final JTextField filterText = new JTextField();
            filterText.addCaretListener(new CaretListener(){
                @SuppressWarnings("unchecked")
                public void caretUpdate(CaretEvent e) {
                    TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) todoEntriesTable.getRowSorter();
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText.getText()));
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
    
    private JToolBar initToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton buttAdd = new JButton(ICON_ADD);
        buttAdd.setToolTipText("add entry");
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
        JButton buttDel = new JButton(ICON_DELETE);
        buttDel.setToolTipText("delete entry");
        buttDel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
                if (todoEntriesTable.getSelectedRow() != -1) {
                    int idx = sorter.convertRowIndexToModel(todoEntriesTable.getSelectedRow());
                    UUID id = UUID.fromString((String) model.getValueAt(idx, 0));
                    model.removeRow(idx);
                    cleanUpUnUsedAttachments(id);
                }
            }
        });
        toolbar.add(buttAdd);
        toolbar.add(buttDel);
        return toolbar;
    }
    
    private void addToDoEntry(ToDoEntry todoEntry) {
        DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
        int idx = todoEntriesTable.getSelectedRow();
        if (idx != -1) {
            todoEntriesTable.getSelectionModel().removeSelectionInterval(0, idx);
        }
        splitPane.setBottomComponent(null);
        todoEntriesTable.setVisible(false);
        model.addRow(new Object[]{
                todoEntry.getId().toString(),
                sdf.format(todoEntry.getTimestamp()), 
                todoEntry.getTitle(),
                todoEntry.getPriority() != null ? todoEntry.getPriority().trim() : Constants.EMPTY_STR,
                todoEntry.getStatus() != null ? todoEntry.getStatus().trim() : Constants.EMPTY_STR
                });
        if (todoEntry.getId() != null && todoEntry.getTimestamp() != null) {
            originalTimestamps.put(todoEntry.getId(), todoEntry.getTimestamp());
        }
        if (todoEntry.getId() != null && todoEntry.getDescription() != null) {
            editorPanels.put(todoEntry.getId(), new HTMLEditorPanel(getId(), todoEntry.getDescription()));
        }
        todoEntriesTable.setVisible(true);
        
    }
    
    private Collection<ToDoEntry> getToDoEntries() {
        Collection<ToDoEntry> entries = new LinkedList<ToDoEntry>();
        DefaultTableModel model = (DefaultTableModel) todoEntriesTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            ToDoEntry entry = new ToDoEntry();
            UUID id = UUID.fromString((String) model.getValueAt(i, 0));
            entry.setId(id);
            entry.setTimestamp(originalTimestamps.get(id));
            entry.setTitle((String) model.getValueAt(i, 2));
            entry.setPriority((String) model.getValueAt(i, 3));
            entry.setStatus((String) model.getValueAt(i, 4));
            HTMLEditorPanel editorPanel = editorPanels.get(id);
            entry.setDescription(editorPanel.getCode());
            entries.add(entry);
        }
        return entries;
    }

    private void cleanUpUnUsedAttachments(UUID id) {
        try {
            HTMLEditorPanel editorPanel = editorPanels.get(id);
            Collection<String> usedAttachmentNames = editorPanel.getProcessedAttachmentNames();
            Collection<Attachment> atts = BackEnd.getInstance().getAttachments(getId());
            for (Attachment att : atts) {
                if (!usedAttachmentNames.contains(att.getName())) {
                    BackEnd.getInstance().removeAttachment(getId(), att.getName());
                }
            }
        } catch (Exception ex) {
            // if some error occurred while cleaning up unused attachments,
            // ignore it, these attachments will be removed next time Bias persists data
        }
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        Collection<String> searchSnippets = new ArrayList<String>();
        for (ToDoEntry entry : getToDoEntries()) {
            searchSnippets.add(entry.getTitle());
            searchSnippets.add(editorPanels.get(entry.getId()).getText());
            searchSnippets.add(entry.getPriority());
            searchSnippets.add(entry.getStatus());
        }
        return searchSnippets;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        Document doc = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = doc.createElement(XML_ELEMENT_ROOT);
        doc.appendChild(rootNode);
        for (ToDoEntry entry : getToDoEntries()) {
            Element entryNode = doc.createElement(XML_ELEMENT_ENTRY);
            Element titleNode = doc.createElement(XML_ELEMENT_TITLE);
            String encodedText = Validator.isNullOrBlank(entry.getTitle()) ? Constants.EMPTY_STR : URLEncoder.encode(entry.getTitle(), Constants.UNICODE_ENCODING);
            titleNode.setTextContent(encodedText);
            entryNode.appendChild(titleNode);
            Element descriptionNode = doc.createElement(XML_ELEMENT_DESCRIPTION);
            descriptionNode.setTextContent(entry.getDescription());
            entryNode.appendChild(descriptionNode);
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_ID, entry.getId().toString());
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_TIMESTAMP, "" + entry.getTimestamp().getTime());
            encodedText = Validator.isNullOrBlank(entry.getPriority()) ? Constants.EMPTY_STR : URLEncoder.encode(entry.getPriority(), Constants.UNICODE_ENCODING);
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_PRIORITY, encodedText);
            encodedText = Validator.isNullOrBlank(entry.getStatus()) ? Constants.EMPTY_STR : URLEncoder.encode(entry.getStatus(), Constants.UNICODE_ENCODING);
            entryNode.setAttribute(XML_ELEMENT_ATTRIBUTE_STATUS, encodedText);
            rootNode.appendChild(entryNode);
        }
        OutputFormat of = new OutputFormat();
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(doc);
        return sw.getBuffer().toString().getBytes();
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
            if (sortByColumn[i] != -1 && sortOrder[i] != null) {
                props.setProperty(PROPERTY_SORT_BY_COLUMN + i, "" + sortByColumn[i]);
                props.setProperty(PROPERTY_SORT_ORDER + i, sortOrder[i].name());
            } else {
                props.remove(PROPERTY_SORT_BY_COLUMN + i);
                props.remove(PROPERTY_SORT_ORDER + i);
            }
        }
        int idx = todoEntriesTable.getSelectedRow();
        if (idx != -1) {
            props.setProperty(PROPERTY_SELECTED_ROW, "" + todoEntriesTable.getSelectedRow());
        } else {
            props.remove(PROPERTY_SELECTED_ROW);
        }
        int dl = splitPane.getDividerLocation();
        if (dl != -1) {
            props.setProperty(PROPERTY_DIVIDER_LOCATION, "" + dl);
        } else {
            props.remove(PROPERTY_DIVIDER_LOCATION);
        }
        StringBuffer colW = new StringBuffer();
        int cc = todoEntriesTable.getColumnModel().getColumnCount();
        for (int i = 0; i < cc; i++) {
            colW.append(todoEntriesTable.getColumnModel().getColumn(i).getWidth());
            if (i < cc - 1) {
                colW.append(":");
            }
        }
        props.setProperty(PROPERTY_COLUMNS_WIDTHS, colW.toString());
        if (splitPane.getBottomComponent() != null) {
            HTMLEditorPanel htmlEditorPanel = ((HTMLEditorPanel) splitPane.getBottomComponent());
            JScrollPane sc = ((JScrollPane) htmlEditorPanel.getComponent(0));
            JScrollBar sb = sc.getVerticalScrollBar();
            if (sb != null && sb.getValue() != 0) {
                props.setProperty(PROPERTY_SCROLLBAR_VERT, "" + sb.getValue());
            } else {
                props.remove(PROPERTY_SCROLLBAR_VERT);
            }
            sb = sc.getHorizontalScrollBar();
            if (sb != null && sb.getValue() != 0) {
                props.setProperty(PROPERTY_SCROLLBAR_HORIZ, "" + sb.getValue());
            } else {
                props.remove(PROPERTY_SCROLLBAR_HORIZ);
            }
            int cp = htmlEditorPanel.getCaretPosition();
            props.setProperty(PROPERTY_CARET_POSITION, "" + cp);
        } else {
            props.remove(PROPERTY_SCROLLBAR_VERT);
            props.remove(PROPERTY_SCROLLBAR_HORIZ);
        }
        return PropertiesUtils.serializeProperties(props);
    }

    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#configure(byte[])
     */
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
        FormatChangeListener formatChangeListener = new FormatChangeListener(dateTimeFormatTF);
        int opt = JOptionPane.showConfirmDialog(
                FrontEnd.getActiveWindow(), 
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
            priorities = prioritiesTF.getText().trim();
            props.setProperty(PROPERTY_PRIORITIES, priorities);
            statuses = statusesTF.getText().trim();
            props.setProperty(PROPERTY_STATUSES, statuses);
            if (!Validator.isNullOrBlank(dateTimeFormatTF.getText()) && formatChangeListener.isFormatCorrect) {
                dateTimeFormat = dateTimeFormatTF.getText().trim();
                props.setProperty(PROPERTY_DATE_TIME_FORMAT, dateTimeFormat);
            }
            return PropertiesUtils.serializeProperties(props);
        }
        return null;
    }
    
    private static class FormatChangeListener implements CaretListener {
        private JTextField dateTimeFormatTF;
        Color normal;
        private boolean isFormatCorrect = true;
        public FormatChangeListener(JTextField dateTimeFormatTF) {
            this.dateTimeFormatTF = dateTimeFormatTF;
            this.normal = dateTimeFormatTF.getForeground();
            this.dateTimeFormatTF.addCaretListener(this);
        }
        public void caretUpdate(CaretEvent e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormatTF.getText());
                dateTimeFormatTF.setForeground(normal);
                dateTimeFormatTF.setToolTipText(sdf.format(new Date()));
                isFormatCorrect = true;
            } catch (IllegalArgumentException iae) {
                dateTimeFormatTF.setForeground(Color.RED);
                dateTimeFormatTF.setToolTipText("Invalid date-time format!");
                isFormatCorrect = false;
            }
        }
    }
    
}
