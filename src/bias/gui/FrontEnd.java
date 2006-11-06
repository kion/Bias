/**
 * Created on Oct 15, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.global.Constants;



/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final ImageIcon ICON_APP = 
        new ImageIcon(Constants.class.getResource("/bias/res/app_icon.png"));
    
    public static final ImageIcon ICON_ABOUT = 
        new ImageIcon(Constants.class.getResource("/bias/res/about.png"));

    public static final ImageIcon ICON_IMPORT_DATA = 
        new ImageIcon(Constants.class.getResource("/bias/res/import_data.png"));

    public static final ImageIcon ICON_DELETE = 
        new ImageIcon(Constants.class.getResource("/bias/res/delete.png"));

    public static final ImageIcon ICON_ADD_CATEGORY = 
        new ImageIcon(Constants.class.getResource("/bias/res/add_category.png"));

    public static final ImageIcon ICON_ADD_ROOT_CATEGORY = 
        new ImageIcon(Constants.class.getResource("/bias/res/add_root_category.png"));

    public static final ImageIcon ICON_ADD_ENTRY = 
        new ImageIcon(Constants.class.getResource("/bias/res/add_entry.png"));

    public static final ImageIcon ICON_ADD_ROOT_ENTRY = 
        new ImageIcon(Constants.class.getResource("/bias/res/add_root_entry.png"));

    public static final ImageIcon ICON_SAVE = 
        new ImageIcon(Constants.class.getResource("/bias/res/save.png"));

    private static final Placement[] PLACEMENTS = new Placement[]{
        new Placement("Top", JTabbedPane.TOP),
        new Placement("Left", JTabbedPane.LEFT),
        new Placement("Right", JTabbedPane.RIGHT),
        new Placement("Bottom", JTabbedPane.BOTTOM)
    };

    private static class Placement {
        private String string;
        private Integer integer;
        public Placement() {
        }
        public Placement(String string, Integer integer) {
            this.string = string;
            this.integer = integer;
        }
        public Integer getInteger() {
            return integer;
        }
        public void setInteger(Integer integer) {
            this.integer = integer;
        }
        public String getString() {
            return string;
        }
        public void setString(String string) {
            this.string = string;
        }
        @Override
        public String toString() {
            return string;
        }
    }
    
	private static FrontEnd instance;
	
    /**
     * Default singleton's hidden constructor without parameters
     */
    private FrontEnd() {
        super();
        initialize();
    }

	public static FrontEnd getInstance() {
		if (instance == null) {
			instance = new FrontEnd();
		}
		return instance;
	}
    
    private String lastAddedEntryType = null;
    
    private JTabbedPane currentTabPane = null;
    
    private JPanel jContentPane = null;

    private JTabbedPane jTabbedPane = null;

    private JToolBar jToolBar = null;

    private JButton jButton = null;

    private JButton jButton1 = null;
    
    private JButton jButton2 = null;
    
    private JButton jButton4 = null;

    private JButton jButton5 = null;

    private JPanel jPanel = null;

    private JToolBar jToolBar2 = null;

    private JButton jButton6 = null;

    private JButton jButton7 = null;

    private JButton jButton3 = null;

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(new Dimension(772, 535));  // Generated
        try {
            this.setTitle("Bias");
            this.setIconImage(ICON_APP.getImage());
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setContentPane(getJContentPane());

            BackEnd.getInstance().load();
            Properties properties = BackEnd.getInstance().getProperties();
            
            int wpxValue;
            int wpyValue;
            int wwValue;
            int whValue;
            String wpx = properties.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_X);
            if (wpx == null) {
                wpxValue = getToolkit().getScreenSize().width/4;
            } else {
                getToolkit().getScreenSize().getWidth();
                Double.valueOf(wpx);
                wpxValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
            }
            String wpy = properties.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
            if (wpy == null) {
                wpyValue = getToolkit().getScreenSize().height/4;
            } else {
                wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
            }
            String ww = properties.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
            if (ww == null) {
                wwValue = (getToolkit().getScreenSize().width/4)*2;
            } else {
                wwValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
            }
            String wh = properties.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
            if (wh == null) {
                whValue = (getToolkit().getScreenSize().height/4)*2;
            } else {
                whValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
            }
            
            this.setLocation(wpxValue, wpyValue);
            this.setSize(wwValue, whValue);
            
            representData(BackEnd.getInstance().getData());
            
            String lsid = properties.getProperty(Constants.PROPERTY_LAST_SELECTED_ID);
            if (lsid != null) {
                switchToVisualItem(UUID.fromString(lsid));
            }

            this.addWindowListener(new java.awt.event.WindowAdapter() {   
                public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        store();
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
            
        } catch (Exception ex) {
            displayErrorMessage(ex);
        }
    }
    
    private void representData(DataCategory data) {
        if (data.getPlacement() != null) {
            getJTabbedPane().setTabPlacement(data.getPlacement());
        }
        representData(getJTabbedPane(), data);
    }
    
    private void representData(JTabbedPane tabbedPane, DataCategory data) {
        try {
            for (Recognizable item : data.getData()) {
                if (item instanceof DataEntry) {
                    String caption = item.getCaption();
                    VisualEntry visualEntry = VisualEntryFactory.getInstance().newVisualEntry((DataEntry) item);
                    tabbedPane.addTab(caption, visualEntry);
                } else if (item instanceof DataCategory) {
                    String caption = item.getCaption();
                    JTabbedPane categoryTabPane = new JTabbedPane();
                    if (item.getId() != null) {
                        categoryTabPane.setName(item.getId().toString());
                    }
                    DataCategory dc = (DataCategory) item;
                    categoryTabPane.setTabPlacement(dc.getPlacement());
                    categoryTabPane.addMouseListener(tabPaneClickListener);
                    categoryTabPane.addChangeListener(tabPaneChangeListener);
                    tabbedPane.addTab(caption, categoryTabPane);
                    currentTabPane = categoryTabPane;
                    representData(categoryTabPane, dc);
                    if (dc.getActiveIndex() != null) {
                        categoryTabPane.setSelectedIndex(dc.getActiveIndex());
                    }
                }
            }
        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }
    
    private void store() throws Exception {
        collectProperties();
        collectData();
        BackEnd.getInstance().store();
    }
    
    private void collectProperties() {
        Properties properties = new Properties();
        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_X, Constants.EMPTY_STR+getLocation().getX()/getToolkit().getScreenSize().getWidth());
        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_Y, Constants.EMPTY_STR+getLocation().getY()/getToolkit().getScreenSize().getHeight());
        properties.put(Constants.PROPERTY_WINDOW_WIDTH, Constants.EMPTY_STR+getSize().getWidth()/getToolkit().getScreenSize().getHeight());
        properties.put(Constants.PROPERTY_WINDOW_HEIGHT, Constants.EMPTY_STR+getSize().getHeight()/getToolkit().getScreenSize().getHeight());
        UUID lsid = getSelectedVisualItemID();
        if (lsid != null) {
            properties.put(Constants.PROPERTY_LAST_SELECTED_ID, lsid.toString());
        }
        BackEnd.getInstance().setProperties(properties);
    }
    
    private void collectData() throws Exception {
        DataCategory data = collectData("root", getJTabbedPane());
        data.setPlacement(getJTabbedPane().getTabPlacement());
        BackEnd.getInstance().setData(data);
    }
    
    private DataCategory collectData(String caption, JTabbedPane tabPane) throws Exception {
        DataCategory data = new DataCategory();
        data.setCaption(caption);
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            caption = tabPane.getTitleAt(i);
            Component c = tabPane.getComponent(i);
            if (c instanceof JTabbedPane) {
                JTabbedPane tp = (JTabbedPane) c;
                DataCategory dc = collectData(caption, tp);
                if (tp.getName() != null) {
                    dc.setId(UUID.fromString(tp.getName()));
                    data.addDataItem(dc);
                    if (tp.getSelectedIndex() != -1) {
                        dc.setActiveIndex(tp.getSelectedIndex());
                    }
                }
                dc.setPlacement(tp.getTabPlacement());
            } else {
                VisualEntry visualEntry = (VisualEntry) c;
                byte[] serializedData = visualEntry.serialize();
                DataEntry dataEntry = new DataEntry(visualEntry.getId(), caption, visualEntry.getClass().getSimpleName(), serializedData);
                data.addDataItem(dataEntry);
            }
        }
        return data;
    }
    
    private UUID getSelectedVisualItemID() {
        return getSelectedVisualItemID(getJTabbedPane());
    }
    
    private UUID getSelectedVisualItemID(JTabbedPane tabPane) {
        if (tabPane.getTabCount() > 0) {
            if (tabPane.getSelectedIndex() != -1) {
                Component c = tabPane.getSelectedComponent();
                if (c instanceof JTabbedPane) {
                    return getSelectedVisualItemID((JTabbedPane) c);
                } else if (c instanceof VisualEntry) {
                    return ((VisualEntry) c).getId();
                }
            } else {
                String idStr = tabPane.getName();
                if (idStr != null) {
                    return UUID.fromString(idStr);
                } else {
                    return null;
                }
            }
        }
        return null;
    }
    
    private Collection<UUID> getVisualItemsIDs() {
        return getVisualItemsIDs(getJTabbedPane());        
    }
    
    private Collection<UUID> getVisualItemsIDs(JTabbedPane rootTabPane) {
        Collection<UUID> ids = new LinkedList<UUID>();
        String idStr = rootTabPane.getName();
        if (idStr != null) {
            ids.add(UUID.fromString(idStr));
        }
        for (Component c : rootTabPane.getComponents()) {
            if (c instanceof JTabbedPane) {
                ids.addAll(getVisualItemsIDs((JTabbedPane) c));
            } else if (c instanceof VisualEntry) {
                VisualEntry ve = (VisualEntry) c;
                if (ve.getId() != null) {
                    ids.add(ve.getId());
                }
            }
        }
        return ids;
    }
    
    public Collection<VisualItemDescriptor> getVisualItemDescriptors() {
        return getVisualItemDescriptors(getJTabbedPane(), new LinkedList<String>());
    }
    
    private Collection<VisualItemDescriptor> getVisualItemDescriptors(JTabbedPane rootTabPane, LinkedList<String> captionsPath) {
        Collection<VisualItemDescriptor> vDescriptors = new LinkedList<VisualItemDescriptor>();
        String idStr = rootTabPane.getName();
        if (idStr != null) {
            vDescriptors.add(new VisualItemDescriptor(UUID.fromString(idStr), captionsPath.toArray(new String[]{})));
        }
    	for (int i = 0; i < rootTabPane.getTabCount(); i++) {
            Component c = rootTabPane.getComponent(i);
            String caption = rootTabPane.getTitleAt(i);
            captionsPath.addLast(caption);
            if (c instanceof JTabbedPane) {
                vDescriptors.addAll(getVisualItemDescriptors((JTabbedPane) c, captionsPath));
                captionsPath.removeLast();
            } else if (c instanceof VisualEntry) {
                VisualEntry ve = (VisualEntry) c;
                if (ve.getId() != null) {
                    vDescriptors.add(new VisualItemDescriptor(ve.getId(), captionsPath.toArray(new String[]{})));
                }
                captionsPath.removeLast();
            }
    	}
    	return vDescriptors;
    }
    
    public boolean switchToVisualItem(UUID id) {
        return switchToVisualItem(getJTabbedPane(), id, new LinkedList<Component>());
    }
    
    private boolean switchToVisualItem(JTabbedPane rootTabPane, UUID id, LinkedList<Component> path) {
        String idStr = rootTabPane.getName(); 
        if (idStr != null && UUID.fromString(idStr).equals(id)) {
            switchToVisualItem(getJTabbedPane(), path.iterator());
            return true;
        }
        for (Component c : rootTabPane.getComponents()) {
            path.addLast(c);
            if (c instanceof JTabbedPane) {
                JTabbedPane tabPane = (JTabbedPane) c;
                if (switchToVisualItem(tabPane, id, path)) {
                    return true;
                } else {
                    path.removeLast();
                }
            } else if (c instanceof VisualEntry) {
                VisualEntry ve = (VisualEntry) c;
                if (ve.getId().equals(id)) {
                    switchToVisualItem(getJTabbedPane(), path.iterator());
                    return true;
                } else {
                    path.removeLast();
                }
            }
        }
        return false;
    }
    
    private void switchToVisualItem(JTabbedPane tabPane, Iterator<Component> pathIterator) {
        if (pathIterator.hasNext()) {
            Component selComp = pathIterator.next();
            tabPane.setSelectedComponent(selComp);
            if (selComp instanceof JTabbedPane) {
                switchToVisualItem((JTabbedPane) selComp, pathIterator);
            }
        }
    }
    
    private JTabbedPane getActiveTabPane(JTabbedPane rootTabPane) {
        if (rootTabPane.getTabCount() > 0) {
            if (rootTabPane.getSelectedIndex() != -1) {
                Component c = rootTabPane.getSelectedComponent();
                if (c instanceof JTabbedPane) {
                    return getActiveTabPane((JTabbedPane) c);
                } else {
                    return rootTabPane;
                }
            } else {
                return rootTabPane;
            }
        }
        return rootTabPane;
    }
    
    public void displayErrorMessage(Exception ex) {
        JOptionPane.showMessageDialog(FrontEnd.this, "Details: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
    
    MouseListener tabPaneClickListener = new MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            currentTabPane = (JTabbedPane) e.getSource();
            if (currentTabPane.getSelectedIndex() != -1) {
                if (currentTabPane.getSelectedComponent() instanceof JTabbedPane) {
                    currentTabPane = (JTabbedPane) currentTabPane.getSelectedComponent();
                }
            }
            if (e.getClickCount() == 2) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                int index = tabbedPane.getSelectedIndex();
                String caption = tabbedPane.getTitleAt(index);
                caption = JOptionPane.showInputDialog(FrontEnd.this, "Entry caption:", caption);
                if (caption != null) { 
                    tabbedPane.setTitleAt(index, caption);
                }
            }
        }
    };
    
    ChangeListener tabPaneChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            currentTabPane = getActiveTabPane((JTabbedPane) e.getSource());
        }
    };
    
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
            jContentPane.add(getJPanel(), BorderLayout.NORTH);  // Generated
        }
        return jContentPane;
    }

    /**
     * This method initializes jTabbedPane	
     * 	
     * @return javax.swing.JTabbedPane	
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setBackground(null);  // Generated
            jTabbedPane.addMouseListener(tabPaneClickListener);
            jTabbedPane.addChangeListener(tabPaneChangeListener);
            jTabbedPane.setTabPlacement(JTabbedPane.LEFT);
        }
        return jTabbedPane;
    }

    /**
     * This method initializes jToolBar	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new JToolBar();
            jToolBar.setFloatable(false);  // Generated
            jToolBar.add(getJButton7());  // Generated
            jToolBar.add(getJButton2());  // Generated
            jToolBar.add(getJButton3());  // Generated
            jToolBar.add(getJButton4());
            jToolBar.add(getJButton());  // Generated
            jToolBar.add(getJButton5());  // Generated
            jToolBar.add(getJButton1());  // Generated
        }
        return jToolBar;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton(addRootEntryAction);
            jButton.setToolTipText("add root entry");  // Generated
            jButton.setIcon(ICON_ADD_ROOT_ENTRY);
        }
        return jButton;
    }

    /**
     * This method initializes jButton5  
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton5() {
        if (jButton5 == null) {
            jButton5 = new JButton(addEntryAction);
            jButton5.setToolTipText("add entry");  // Generated
            jButton5.setIcon(ICON_ADD_ENTRY);
        }
        return jButton5;
    }

    /**
     * This method initializes jButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton(deleteEntryOrCategoryAction);
            jButton1.setToolTipText("delete active item");  // Generated
            jButton1.setIcon(ICON_DELETE);
        }
        return jButton1;
    }

    /**
     * This method initializes jButton2	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton(importDataAction);
            jButton2.setToolTipText("import data from another Bias JAR");  // Generated
            jButton2.setIcon(ICON_IMPORT_DATA);
        }
        return jButton2;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());  // Generated
            jPanel.add(getJToolBar(), BorderLayout.CENTER);  // Generated
            jPanel.add(getJToolBar2(), BorderLayout.EAST);  // Generated
        }
        return jPanel;
    }

    /**
     * This method initializes jToolBar2	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar2() {
        if (jToolBar2 == null) {
            jToolBar2 = new JToolBar();
            jToolBar2.setFloatable(false);  // Generated
            jToolBar2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);  // Generated
            jToolBar2.add(getJButton6());  // Generated
        }
        return jToolBar2;
    }

    /**
     * This method initializes jButton6	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton6() {
        if (jButton6 == null) {
            jButton6 = new JButton(displayAboutInfoAction);
            jButton6.setToolTipText("about Bias");  // Generated
            jButton6.setIcon(ICON_ABOUT);
        }
        return jButton6;
    }

    /**
     * This method initializes jButton7 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton7() {
        if (jButton7 == null) {
            jButton7 = new JButton(saveAction);
            jButton7.setToolTipText("save");  // Generated
            jButton7.setIcon(ICON_SAVE);
        }
        return jButton7;
    }

    /**
     * This method initializes jButton3	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton(addRootCategoryAction);
            jButton3.setToolTipText("add root category");  // Generated
            jButton3.setIcon(ICON_ADD_ROOT_CATEGORY);
        }
        return jButton3;
    }
    
    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton(addCategoryAction);
            jButton4.setIcon(ICON_ADD_CATEGORY);
            jButton4.setToolTipText("add category");
        }
        return jButton4;
    }
    
    private boolean defineRootPlacement() {
        boolean result = false;
        Placement placement = (Placement) JOptionPane.showInputDialog(
                FrontEnd.this, 
                "Choose placement:", 
                "Choose placement for root container", 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                PLACEMENTS, 
                PLACEMENTS[0]);
        if (placement != null) {
            getJTabbedPane().setTabPlacement(placement.getInteger());
            result = true;
        }
        return result;
    }
    
    Action addRootCategoryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            try {
                if (getJTabbedPane().getTabCount() == 0) {
                    if (defineRootPlacement()) {
                        addRootCategoryAction();
                    }
                } else {
                    addRootCategoryAction();
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    private void addRootCategoryAction() {
        JLabel pLabel = new JLabel("Choose placement:");
        JComboBox placementsChooser = new JComboBox();
        for (Placement placement : PLACEMENTS) {
            placementsChooser.addItem(placement);
        }
        String categoryCaption = JOptionPane.showInputDialog(
                FrontEnd.this, 
                new Component[]{
                        pLabel, 
                        placementsChooser}, 
                "New root category:",
                JOptionPane.QUESTION_MESSAGE);
        if (categoryCaption != null) {
            JTabbedPane categoryTabPane = new JTabbedPane();
            categoryTabPane.setName(UUID.randomUUID().toString());
            categoryTabPane.setTabPlacement(((Placement)placementsChooser.getSelectedItem()).getInteger());
            categoryTabPane.addMouseListener(tabPaneClickListener);
            categoryTabPane.addChangeListener(tabPaneChangeListener);
            getJTabbedPane().addTab(categoryCaption, categoryTabPane);
            getJTabbedPane().setSelectedComponent(categoryTabPane);
        }
    }
    
    Action addRootEntryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            if (getJTabbedPane().getTabCount() == 0) {
                if (defineRootPlacement()) {
                    addRootEntryAction();
                }
            } else {
                addRootEntryAction();
            }
        }
    };
    
    private void addRootEntryAction() {
        try {
            JLabel entryTypeLabel = new JLabel("Type:");
            JComboBox entryTypeComboBox = new JComboBox();
            for (String entryType : VisualEntryFactory.getInstance().getEntryTypes().keySet()) {
                entryTypeComboBox.addItem(entryType);
            }
            if (lastAddedEntryType != null) {
                entryTypeComboBox.setSelectedItem(lastAddedEntryType);
            }
            entryTypeComboBox.setEditable(false);
            String caption = JOptionPane.showInputDialog(
                    FrontEnd.this, 
                    new Component[]{
                            entryTypeLabel,
                            entryTypeComboBox}, 
                    "New entry:", 
                    JOptionPane.QUESTION_MESSAGE);
            if (caption != null) {
                String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                lastAddedEntryType = typeDescription;
                Class type = VisualEntryFactory.getInstance().getEntryTypes().get(typeDescription);
                VisualEntry visualEntry = VisualEntryFactory.getInstance().newVisualEntry(type);
                if (visualEntry != null) {
                    getJTabbedPane().addTab(caption, visualEntry);
                    getJTabbedPane().setSelectedComponent(visualEntry);
                }
            }
        } catch (Exception ex) {
            displayErrorMessage(ex);
        }
    }
    
    Action addEntryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            if (getJTabbedPane().getTabCount() > 0) {
                try {
                    if (getJTabbedPane().getSelectedIndex() == -1) {
                        currentTabPane = getJTabbedPane();
                    }
                    JLabel entryTypeLabel = new JLabel("Type:");
                    JComboBox entryTypeComboBox = new JComboBox();
                    for (String entryType : VisualEntryFactory.getInstance().getEntryTypes().keySet()) {
                        entryTypeComboBox.addItem(entryType);
                    }
                    if (lastAddedEntryType != null) {
                        entryTypeComboBox.setSelectedItem(lastAddedEntryType);
                    }
                entryTypeComboBox.setEditable(false);
                    String caption = JOptionPane.showInputDialog(
                            FrontEnd.this, 
                            new Component[]{
                                    entryTypeLabel,
                                    entryTypeComboBox}, 
                            "New entry:", 
                            JOptionPane.QUESTION_MESSAGE);
                    if (caption != null) {
                        String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                        lastAddedEntryType = typeDescription;
                        Class type = VisualEntryFactory.getInstance().getEntryTypes().get(typeDescription);
                        VisualEntry visualEntry = VisualEntryFactory.getInstance().newVisualEntry(type);
                        if (visualEntry != null) {
                            currentTabPane.addTab(caption, visualEntry);
                            currentTabPane.setSelectedComponent(visualEntry);
                        }
                    }
                } catch (Exception ex) {
                    displayErrorMessage(ex);
                }
            }
        }
    };
    
    Action deleteEntryOrCategoryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            if (getJTabbedPane().getTabCount() > 0) {
                try {
                    if (currentTabPane.getTabCount() > 0) {
                        currentTabPane.remove(currentTabPane.getSelectedIndex());
                        currentTabPane = getActiveTabPane(currentTabPane);
                    } else {
                        JTabbedPane parentTabPane = (JTabbedPane) currentTabPane.getParent();
                        if (parentTabPane != null) {
                            parentTabPane.remove(currentTabPane);
                            currentTabPane = getActiveTabPane(parentTabPane);
                        }
                    }
                } catch (Exception ex) {
                    displayErrorMessage(ex);
                }
            }
        }
    };
    
    Action importDataAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            try {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setFileFilter(new FileFilter(){
                    @Override
                    public boolean accept(File file) {
                        if (file.getName().endsWith(".jar")) {
                            return true;
                        }
                        return false;
                    }
                    @Override
                    public String getDescription() {
                        return "Java Archive File (*.jar)";
                    }
                });
                File jarFile = null;
                int rVal = jfc.showOpenDialog(FrontEnd.this);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    jarFile = jfc.getSelectedFile();
                    representData(BackEnd.getInstance().importData(jarFile, getVisualItemsIDs()));
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    Action addCategoryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            try {
                if (getJTabbedPane().getTabCount() > 0) {
                    JLabel pLabel = new JLabel("Choose placement:");
                    JComboBox placementsChooser = new JComboBox();
                    for (Placement placement : PLACEMENTS) {
                        placementsChooser.addItem(placement);
                    }
                    String categoryCaption = JOptionPane.showInputDialog(
                            FrontEnd.this, 
                            new Component[]{
                                    pLabel, 
                                    placementsChooser}, 
                            "New category:",
                            JOptionPane.QUESTION_MESSAGE);
                    if (categoryCaption != null) {
                        JTabbedPane categoryTabPane = new JTabbedPane();
                        UUID id = UUID.randomUUID();
                        categoryTabPane.setName(id.toString());
                        categoryTabPane.setTabPlacement(((Placement)placementsChooser.getSelectedItem()).getInteger());
                        categoryTabPane.addMouseListener(tabPaneClickListener);
                        categoryTabPane.addChangeListener(tabPaneChangeListener);
                        currentTabPane.addTab(categoryCaption, categoryTabPane);
                        ((JTabbedPane)categoryTabPane.getParent()).setSelectedComponent(categoryTabPane);
                        currentTabPane = (JTabbedPane)categoryTabPane.getParent();
                    }
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    Action saveAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            try {
                store();
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    Action displayAboutInfoAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
            JOptionPane.showMessageDialog(FrontEnd.this, 
                    "<html>Bias Personal Information Manager, version 0.1-beta"
                    + "<br>(c) kion, 2006"
                    + "<br>http://bias.sourceforge.net");
        }
    };
    
}  //  @jve:decl-index=0:visual-constraint="10,10"
