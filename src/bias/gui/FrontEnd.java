/**
 * Created on Oct 15, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.TabbedPaneUI;

import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.global.Constants;
import bias.gui.extension.Extension;
import bias.utils.BrowserLauncher;

/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final ImageIcon ICON_APP = new ImageIcon(Constants.class.getResource("/bias/res/app_icon.png"));

    public static final ImageIcon ICON_ABOUT = new ImageIcon(Constants.class.getResource("/bias/res/about.png"));

    public static final ImageIcon ICON_IMPORT_DATA = new ImageIcon(Constants.class.getResource("/bias/res/import_data.png"));

    public static final ImageIcon ICON_DELETE = new ImageIcon(Constants.class.getResource("/bias/res/delete.png"));

    public static final ImageIcon ICON_ADD_CATEGORY = new ImageIcon(Constants.class.getResource("/bias/res/add_category.png"));

    public static final ImageIcon ICON_ADD_ROOT_CATEGORY = new ImageIcon(Constants.class.getResource("/bias/res/add_root_category.png"));

    public static final ImageIcon ICON_ADD_ENTRY = new ImageIcon(Constants.class.getResource("/bias/res/add_entry.png"));

    public static final ImageIcon ICON_ADD_ROOT_ENTRY = new ImageIcon(Constants.class.getResource("/bias/res/add_root_entry.png"));

    public static final ImageIcon ICON_SAVE = new ImageIcon(Constants.class.getResource("/bias/res/save.png"));

    public static final ImageIcon ICON_EXTENSIONS = new ImageIcon(Constants.class.getResource("/bias/res/extensions.png"));

    private static final Placement[] PLACEMENTS = new Placement[] { new Placement(JTabbedPane.TOP),
            new Placement(JTabbedPane.LEFT), new Placement(JTabbedPane.RIGHT),
            new Placement(JTabbedPane.BOTTOM) };

    private static class Placement {
        private String string;
        private Integer integer;
        public Placement(int placementType) {
        	this.integer = placementType;
        	switch (placementType) {
        	case JTabbedPane.TOP: this.string = "Top"; break;
        	case JTabbedPane.LEFT: this.string = "Left"; break;
        	case JTabbedPane.RIGHT: this.string = "Right"; break;
        	case JTabbedPane.BOTTOM: this.string = "Bottom"; break;
        	default: this.string = "Top";
        	}
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
    
    private static class ExtensionFileChooser extends JFileChooser {
        private static final long serialVersionUID = 1L;

        public ExtensionFileChooser() {
            super();
            setFileFilter(new FileFilter(){
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().matches(Constants.EXTENSION_FILE_PATTHERN);
                }
                @Override
                public String getDescription() {
                    return Constants.EXTENSION_FILE_PATTHERN_DESCRIPTION;
                }
            });            
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

    private JButton jButton8 = null;

    private JButton jButton3 = null;

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(new Dimension(772, 535)); // Generated
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
                wpxValue = getToolkit().getScreenSize().width / 4;
            } else {
                getToolkit().getScreenSize().getWidth();
                Double.valueOf(wpx);
                wpxValue = Math.round(Float
                        .valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
            }
            String wpy = properties.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
            if (wpy == null) {
                wpyValue = getToolkit().getScreenSize().height / 4;
            } else {
                wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR
                        + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
            }
            String ww = properties.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
            if (ww == null) {
                wwValue = (getToolkit().getScreenSize().width / 4) * 2;
            } else {
                wwValue = Math
                        .round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
            }
            String wh = properties.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
            if (wh == null) {
                whValue = (getToolkit().getScreenSize().height / 4) * 2;
            } else {
                whValue = Math
                        .round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
            }

            this.setLocation(wpxValue, wpyValue);
            this.setSize(wwValue, whValue);

            representData(BackEnd.getInstance().getData());

            String lsid = properties.getProperty(Constants.PROPERTY_LAST_SELECTED_ID);
            if (lsid != null) {
                switchToVisualEntry(UUID.fromString(lsid));
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
                    Extension extension = ExtensionFactory.getInstance().newExtension((DataEntry) item);
                    tabbedPane.addTab(caption, extension);
                } else if (item instanceof DataCategory) {
                    String caption = item.getCaption();
                    JTabbedPane categoryTabPane = new JTabbedPane();
                    if (item.getId() != null) {
                        categoryTabPane.setName(item.getId().toString());
                    }
                    DataCategory dc = (DataCategory) item;
                    categoryTabPane.setTabPlacement(dc.getPlacement());
                    addTabPaneListeners(categoryTabPane);
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
        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_X, Constants.EMPTY_STR + getLocation().getX()
                / getToolkit().getScreenSize().getWidth());
        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_Y, Constants.EMPTY_STR + getLocation().getY()
                / getToolkit().getScreenSize().getHeight());
        properties.put(Constants.PROPERTY_WINDOW_WIDTH, Constants.EMPTY_STR + getSize().getWidth()
                / getToolkit().getScreenSize().getHeight());
        properties.put(Constants.PROPERTY_WINDOW_HEIGHT, Constants.EMPTY_STR + getSize().getHeight()
                / getToolkit().getScreenSize().getHeight());
        UUID lsid = getSelectedVisualEntryID();
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
                Extension extension = (Extension) c;
                byte[] serializedData = extension.serialize();
                DataEntry dataEntry = new DataEntry(extension.getId(), caption, extension.getClass().getSimpleName(), serializedData);
                data.addDataItem(dataEntry);
            }
        }
        return data;
    }

    private UUID getSelectedVisualEntryID() {
        return getSelectedVisualEntryID(getJTabbedPane());
    }

    private UUID getSelectedVisualEntryID(JTabbedPane tabPane) {
        if (tabPane.getTabCount() > 0) {
            if (tabPane.getSelectedIndex() != -1) {
                Component c = tabPane.getSelectedComponent();
                if (c instanceof JTabbedPane) {
                    return getSelectedVisualEntryID((JTabbedPane) c);
                } else if (c instanceof Extension) {
                    return ((Extension) c).getId();
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

    private Collection<UUID> getVisualEntriesIDs() {
        return getVisualEntriesIDs(getJTabbedPane());
    }

    private Collection<UUID> getVisualEntriesIDs(JTabbedPane rootTabPane) {
        Collection<UUID> ids = new LinkedList<UUID>();
        String idStr = rootTabPane.getName();
        if (idStr != null) {
            ids.add(UUID.fromString(idStr));
        }
        for (Component c : rootTabPane.getComponents()) {
            if (c instanceof JTabbedPane) {
                ids.addAll(getVisualEntriesIDs((JTabbedPane) c));
            } else if (c instanceof Extension) {
                Extension ve = (Extension) c;
                if (ve.getId() != null) {
                    ids.add(ve.getId());
                }
            }
        }
        return ids;
    }

    public Collection<VisualEntryDescriptor> getVisualEntryDescriptors() {
        return getVisualEntryDescriptors(getJTabbedPane(), new LinkedList<String>());
    }

    private Collection<VisualEntryDescriptor> getVisualEntryDescriptors(JTabbedPane rootTabPane, LinkedList<String> captionsPath) {
        Collection<VisualEntryDescriptor> vDescriptors = new LinkedList<VisualEntryDescriptor>();
        String idStr = rootTabPane.getName();
        if (idStr != null) {
            vDescriptors.add(new VisualEntryDescriptor(UUID.fromString(idStr), captionsPath.toArray(new String[] {})));
        }
        for (int i = 0; i < rootTabPane.getTabCount(); i++) {
            Component c = rootTabPane.getComponent(i);
            String caption = rootTabPane.getTitleAt(i);
            captionsPath.addLast(caption);
            if (c instanceof JTabbedPane) {
                vDescriptors.addAll(getVisualEntryDescriptors((JTabbedPane) c, captionsPath));
                captionsPath.removeLast();
            } else if (c instanceof Extension) {
                Extension ve = (Extension) c;
                if (ve.getId() != null) {
                    vDescriptors.add(new VisualEntryDescriptor(ve.getId(), captionsPath.toArray(new String[] {})));
                }
                captionsPath.removeLast();
            }
        }
        return vDescriptors;
    }

    public boolean switchToVisualEntry(UUID id) {
        return switchToVisualEntry(getJTabbedPane(), id, new LinkedList<Component>());
    }

    private boolean switchToVisualEntry(JTabbedPane rootTabPane, UUID id, LinkedList<Component> path) {
        String idStr = rootTabPane.getName();
        if (idStr != null && UUID.fromString(idStr).equals(id)) {
            switchToVisualEntry(getJTabbedPane(), path.iterator());
            return true;
        }
        for (Component c : rootTabPane.getComponents()) {
            path.addLast(c);
            if (c instanceof JTabbedPane) {
                JTabbedPane tabPane = (JTabbedPane) c;
                if (switchToVisualEntry(tabPane, id, path)) {
                    return true;
                } else {
                    path.removeLast();
                }
            } else if (c instanceof Extension) {
                Extension ve = (Extension) c;
                if (ve.getId().equals(id)) {
                    switchToVisualEntry(getJTabbedPane(), path.iterator());
                    return true;
                } else {
                    path.removeLast();
                }
            }
        }
        return false;
    }

    private void switchToVisualEntry(JTabbedPane tabPane, Iterator<Component> pathIterator) {
        if (pathIterator.hasNext()) {
            Component selComp = pathIterator.next();
            tabPane.setSelectedComponent(selComp);
            if (selComp instanceof JTabbedPane) {
                switchToVisualEntry((JTabbedPane) selComp, pathIterator);
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

    private void addTabPaneListeners(JTabbedPane tabPane) {
        tabPane.addMouseListener(tabClickListener);
        tabPane.addChangeListener(tabChangeListener);
        tabPane.addMouseListener(tabMoveListener);
        tabPane.addMouseMotionListener(tabMoveListener);
    }

    private MouseListener tabClickListener = new MouseAdapter() {
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
                JLabel pLabel = new JLabel("Item's category placement:");
                JComboBox placementsChooser = new JComboBox();
                for (Placement placement : PLACEMENTS) {
                    placementsChooser.addItem(placement);
                }
                for (int i = 0; i < placementsChooser.getItemCount(); i++) {
                	if (((Placement) placementsChooser.getItemAt(i)).getInteger().equals(tabbedPane.getTabPlacement())) {
                		placementsChooser.setSelectedIndex(i);
                		break;
                	}
                }
                JLabel cLabel = new JLabel("Item's caption:");
                caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser, cLabel },
                        caption);
                if (caption != null) {
                	tabbedPane.setTitleAt(index, caption);
                	tabbedPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
                }
                
            }
        }
    };

    private ChangeListener tabChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            currentTabPane = getActiveTabPane((JTabbedPane) e.getSource());
        }
    };
    
    private TabMoveListener tabMoveListener = new TabMoveListener();

    public class TabMoveListener extends MouseAdapter implements MouseMotionListener {

        private int srcIndex = -1;

        private int currIndex = -1;

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                srcIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
            }
            currIndex = -1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            if (!e.isPopupTrigger()) {
                int dstIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                if (srcIndex != -1 && dstIndex != -1 && srcIndex != dstIndex) {
                    moveTab(tabbedPane, srcIndex, dstIndex);
                }
            }
            deHighLight(tabbedPane);
            setCursor(Cursor.getDefaultCursor());
            srcIndex = -1;
            currIndex = -1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
         */
        public void mouseDragged(MouseEvent e) {
            if (srcIndex != -1) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
                if (index != -1) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                if (index != -1 && index != currIndex) { // moved over another tab
                    deHighLight(tabbedPane);
                    currIndex = index;
                }
                if (currIndex != -1 && currIndex != srcIndex) {
                    highLight(tabbedPane);
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
         */
        public void mouseMoved(MouseEvent e) {}

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent e) {
            deHighLight((JTabbedPane) e.getSource());
            currIndex = -1;
        }

        /**
         * As far as internal structure of JTabbedPane data model does not correspond to 
         * its visual representation, that is, component located on tab with index X 
         * is <b>not</b> located in the internal components array using the same index, 
         * we have to rearrange this array each time tab has been moved and 
         * repopulate/repaint JTabbedPane instance after that.
         * 
         */
        private void moveTab(JTabbedPane tabPane, int srcIndex, int dstIndex) {
            
            int cnt = tabPane.getTabCount();

            // get tabpane's components/captions
            Component[] components = new Component[cnt];
            for (int i = 0; i< cnt; i++) {
                components[i] = tabPane.getComponent(i);
            }
            String[] captions = new String[cnt];
            for (int i = 0; i < cnt; i++) {
                captions[i] = tabPane.getTitleAt(i);
            }

            // remember component/caption that has to be moved
            Component srcComp = components[srcIndex];
            String srcCap = captions[srcIndex];
            
            // rearrange components/captions using shifting
            if (srcIndex > dstIndex) {
                for (int i = srcIndex; i > dstIndex; i--) {
                    components[i] = components[i-1];
                    captions[i] = captions[i-1];
                }
            } else {
                for (int i = srcIndex; i < dstIndex; i++) {
                    components[i] = components[i+1];
                    captions[i] = captions[i+1];
                }
            }

            // set moved component/caption to its new position
            components[dstIndex] = srcComp;
            captions[dstIndex] = srcCap;
            
            // remove everything from tabpane before repopulating it
            tabPane.removeAll();
            
            // repopulate tabpane with resulting components/captions
            for (int i = 0; i < cnt; i++) {
                tabPane.addTab(captions[i], components[i]);
            }
            
            // set moved component as selected
            tabPane.setSelectedIndex(dstIndex);
            
            // repaint tabpane
            tabPane.repaint();
            
        }

        private void deHighLight(JTabbedPane tabbedPane) {
            if (currIndex == -1) {
                return;
            }
            TabbedPaneUI ui = tabbedPane.getUI();
            Rectangle rect = ui.getTabBounds(tabbedPane, currIndex);
            tabbedPane.repaint(rect);
        }

        private void highLight(JTabbedPane tabbedPane) {
            TabbedPaneUI ui = tabbedPane.getUI();
            Rectangle rect = ui.getTabBounds(tabbedPane, currIndex);
            Graphics graphics = tabbedPane.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
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
            jContentPane.add(getJPanel(), BorderLayout.NORTH); // Generated
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
            jTabbedPane.setBackground(null); // Generated
            jTabbedPane.setTabPlacement(JTabbedPane.LEFT);
            addTabPaneListeners(jTabbedPane);
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
            jToolBar.setFloatable(false); // Generated
            jToolBar.add(getJButton7()); // Generated
            jToolBar.add(getJButton2()); // Generated
            jToolBar.add(getJButton3()); // Generated
            jToolBar.add(getJButton4());
            jToolBar.add(getJButton()); // Generated
            jToolBar.add(getJButton5()); // Generated
            jToolBar.add(getJButton1()); // Generated
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
            jButton.setToolTipText("add root entry"); // Generated
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
            jButton5.setToolTipText("add entry"); // Generated
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
            jButton1.setToolTipText("delete active item"); // Generated
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
            jButton2.setToolTipText("import data from another Bias JAR"); // Generated
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
            jPanel.setLayout(new BorderLayout()); // Generated
            jPanel.add(getJToolBar(), BorderLayout.CENTER); // Generated
            jPanel.add(getJToolBar2(), BorderLayout.EAST); // Generated
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
            jToolBar2.setFloatable(false); // Generated
            jToolBar2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // Generated
            jToolBar2.add(getJButton6()); // Generated
            jToolBar2.add(getJButton8()); // Generated
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
            jButton6.setToolTipText("about Bias"); // Generated
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
            jButton7.setToolTipText("save"); // Generated
            jButton7.setIcon(ICON_SAVE);
        }
        return jButton7;
    }

    /**
     * This method initializes jButton8
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton8() {
        if (jButton8 == null) {
            jButton8 = new JButton(manageExtensionsAction);
            jButton8.setToolTipText("manage extensions"); // Generated
            jButton8.setIcon(ICON_EXTENSIONS);
        }
        return jButton8;
    }

    /**
     * This method initializes jButton3
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton(addRootCategoryAction);
            jButton3.setToolTipText("add root category"); // Generated
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
        Placement placement = (Placement) JOptionPane.showInputDialog(FrontEnd.this, "Choose placement:",
                "Choose placement for root container", JOptionPane.QUESTION_MESSAGE, null, PLACEMENTS, PLACEMENTS[0]);
        if (placement != null) {
            getJTabbedPane().setTabPlacement(placement.getInteger());
            result = true;
        }
        return result;
    }

    private Action addRootCategoryAction = new AbstractAction() {
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
        String categoryCaption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser },
                "New root category:", JOptionPane.QUESTION_MESSAGE);
        if (categoryCaption != null) {
            JTabbedPane categoryTabPane = new JTabbedPane();
            categoryTabPane.setName(UUID.randomUUID().toString());
            categoryTabPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
            addTabPaneListeners(categoryTabPane);
            getJTabbedPane().addTab(categoryCaption, categoryTabPane);
            getJTabbedPane().setSelectedComponent(categoryTabPane);
        }
    }

    private Action addRootEntryAction = new AbstractAction() {
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
            for (String entryType : ExtensionFactory.getInstance().getExtensions().keySet()) {
                entryTypeComboBox.addItem(entryType);
            }
            if (lastAddedEntryType != null) {
                entryTypeComboBox.setSelectedItem(lastAddedEntryType);
            }
            entryTypeComboBox.setEditable(false);
            String caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { entryTypeLabel, entryTypeComboBox },
                    "New entry:", JOptionPane.QUESTION_MESSAGE);
            if (caption != null) {
                String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                lastAddedEntryType = typeDescription;
                Class type = ExtensionFactory.getInstance().getExtensions().get(typeDescription);
                Extension extension = ExtensionFactory.getInstance().newExtension(type);
                if (extension != null) {
                    getJTabbedPane().addTab(caption, extension);
                    getJTabbedPane().setSelectedComponent(extension);
                }
            }
        } catch (Exception ex) {
            displayErrorMessage(ex);
        }
    }

    private Action addEntryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
            if (getJTabbedPane().getTabCount() > 0) {
                try {
                    if (getJTabbedPane().getSelectedIndex() == -1) {
                        currentTabPane = getJTabbedPane();
                    }
                    JLabel entryTypeLabel = new JLabel("Type:");
                    JComboBox entryTypeComboBox = new JComboBox();
                    for (String entryType : ExtensionFactory.getInstance().getExtensions().keySet()) {
                        entryTypeComboBox.addItem(entryType);
                    }
                    if (lastAddedEntryType != null) {
                        entryTypeComboBox.setSelectedItem(lastAddedEntryType);
                    }
                    entryTypeComboBox.setEditable(false);
                    String caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { entryTypeLabel, entryTypeComboBox },
                            "New entry:", JOptionPane.QUESTION_MESSAGE);
                    if (caption != null) {
                        String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                        lastAddedEntryType = typeDescription;
                        Class type = ExtensionFactory.getInstance().getExtensions().get(typeDescription);
                        Extension extension = ExtensionFactory.getInstance().newExtension(type);
                        if (extension != null) {
                            currentTabPane.addTab(caption, extension);
                            currentTabPane.setSelectedComponent(extension);
                        }
                    }
                } catch (Exception ex) {
                    displayErrorMessage(ex);
                }
            }
        }
    };

    private Action deleteEntryOrCategoryAction = new AbstractAction() {
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

    private Action importDataAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
            try {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.isDirectory() || file.getName().endsWith(".jar")) {
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
                    representData(BackEnd.getInstance().importData(jarFile, getVisualEntriesIDs()));
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };

    private Action addCategoryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
            try {
                if (getJTabbedPane().getTabCount() > 0) {
                    JLabel pLabel = new JLabel("Choose placement:");
                    JComboBox placementsChooser = new JComboBox();
                    for (Placement placement : PLACEMENTS) {
                        placementsChooser.addItem(placement);
                    }
                    String categoryCaption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser },
                            "New category:", JOptionPane.QUESTION_MESSAGE);
                    if (categoryCaption != null) {
                        JTabbedPane categoryTabPane = new JTabbedPane();
                        UUID id = UUID.randomUUID();
                        categoryTabPane.setName(id.toString());
                        categoryTabPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
                        addTabPaneListeners(categoryTabPane);
                        currentTabPane.addTab(categoryCaption, categoryTabPane);
                        ((JTabbedPane) categoryTabPane.getParent()).setSelectedComponent(categoryTabPane);
                        currentTabPane = (JTabbedPane) categoryTabPane.getParent();
                    }
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };

    private Action saveAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
            try {
                store();
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    private Action manageExtensionsAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        private Map<String, String> components;
        private String selectedComponent;
        DefaultListModel model;
        private boolean modified = false;
        
        public void actionPerformed(ActionEvent e) {
            try {
                JLabel vcsLabel = new JLabel("Visual Components Management");
                JList vcsList = new JList(new DefaultListModel());
                model = (DefaultListModel) vcsList.getModel();
                components = new HashMap<String, String>();
                for (String component : BackEnd.getInstance().getExtensions()) {
                    Class<?> vcClass = Class.forName(component);
                    Extension.Annotation vcAnn = 
                        (Extension.Annotation) vcClass.getAnnotation(Extension.Annotation.class);
                    String annotationStr = vcAnn.name() + " [ " + vcAnn.description() + " ]";
                    model.addElement(annotationStr);
                    components.put(annotationStr, vcClass.getName());
                }
                vcsList.addListSelectionListener(new ListSelectionListener(){
                    public void valueChanged(ListSelectionEvent e) {
                        selectedComponent = 
                            ((String) ((JList) e.getSource()).getSelectedValue());
                    }
                });
                JButton instButt = new JButton("Install new");
                instButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fc = new ExtensionFileChooser();
                        if (fc.showOpenDialog(FrontEnd.getInstance()) == JFileChooser.APPROVE_OPTION) {
                            try {
                                BackEnd.getInstance().installExtension(fc.getSelectedFile());
                                model.addElement("Extension installed from: " + fc.getSelectedFile());
                                setTitle("Will be restarted");
                                modified = true;
                            } catch (Exception ex) {
                                FrontEnd.getInstance().displayErrorMessage(ex);
                            }
                        }
                    }
                });
                JButton uninstButt = new JButton("Uninstall selected");
                uninstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            BackEnd.getInstance().uninstallExtension(components.get(selectedComponent));
                            model.removeElement(selectedComponent);
                            setTitle("Will be restarted");
                            modified = true;
                        } catch (Exception ex) {
                            FrontEnd.getInstance().displayErrorMessage(ex);
                        }
                    }
                });
                JOptionPane.showMessageDialog(
                    FrontEnd.this, 
                    new Component[]{
                            vcsLabel,
                            new JScrollPane(vcsList),
                            instButt,
                            uninstButt
                    },
                    "Manage Extensions",
                    JOptionPane.INFORMATION_MESSAGE,
                    null
                );
                if (modified) {
                    store();
                    System.exit(0);
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };

    private Action displayAboutInfoAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
        	JLabel aboutLabel = new JLabel(
        							"<html>Bias Personal Information Manager, version 0.1-beta<br>" +
        							"(c) kion, 2006<br>"
        						);
        	JLabel linkLabel = new JLabel(
        							"<html><u><font color=blue>http://bias.sourceforge.net</font></u>"
        						);
        	linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	linkLabel.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						BrowserLauncher.openURL("http://bias.sourceforge.net");
					} catch (Exception ex) {
						// do nothing
					}
				}
        	});
            JOptionPane.showMessageDialog(FrontEnd.this, new Component[]{aboutLabel, linkLabel} );
        }
    };

} // @jve:decl-index=0:visual-constraint="10,10"
