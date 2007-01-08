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
import java.util.Arrays;
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
import javax.swing.Icon;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.TabbedPaneUI;

import bias.Constants;
import bias.annotation.AddOnAnnotation;
import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.extension.Extension;
import bias.extension.ExtensionFactory;
import bias.extension.MissingExtensionInformer;
import bias.gui.utils.ImageFileChooser;
import bias.laf.LookAndFeel;
import bias.utils.BrowserLauncher;


/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_LOOK_AND_FEEL = "Default Look-&-Feel";

    public static final ImageIcon ICON_APP = new ImageIcon(Constants.class.getResource("/bias/res/app_icon.png"));

    public static final ImageIcon ICON_ABOUT = new ImageIcon(Constants.class.getResource("/bias/res/about.png"));

    public static final ImageIcon ICON_IMPORT_DATA = new ImageIcon(Constants.class.getResource("/bias/res/import_data.png"));

    public static final ImageIcon ICON_DELETE = new ImageIcon(Constants.class.getResource("/bias/res/delete.png"));

    public static final ImageIcon ICON_ADD_CATEGORY = new ImageIcon(Constants.class.getResource("/bias/res/add_category.png"));

    public static final ImageIcon ICON_ADD_ROOT_CATEGORY = new ImageIcon(Constants.class.getResource("/bias/res/add_root_category.png"));

    public static final ImageIcon ICON_ADD_ENTRY = new ImageIcon(Constants.class.getResource("/bias/res/add_entry.png"));

    public static final ImageIcon ICON_ADD_ROOT_ENTRY = new ImageIcon(Constants.class.getResource("/bias/res/add_root_entry.png"));

    public static final ImageIcon ICON_SAVE = new ImageIcon(Constants.class.getResource("/bias/res/save.png"));

    public static final ImageIcon ICON_DISCARD_UNSAVED_CHANGES = new ImageIcon(Constants.class.getResource("/bias/res/discard.png"));

    public static final ImageIcon ICON_EXTENSIONS = new ImageIcon(Constants.class.getResource("/bias/res/extensions.png"));

    public static final ImageIcon ICON_LAF = new ImageIcon(Constants.class.getResource("/bias/res/laf.png"));

    public static final ImageIcon ICON_ICONS = new ImageIcon(Constants.class.getResource("/bias/res/icons.png"));

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
    
    private static ExtensionFileChooser extensionFileChooser = new ExtensionFileChooser();
    private static class ExtensionFileChooser extends JFileChooser {
        private static final long serialVersionUID = 1L;

        public ExtensionFileChooser() {
            super();
            setMultiSelectionEnabled(true);
    		setFileSelectionMode(JFileChooser.FILES_ONLY);
            setFileFilter(new FileFilter(){
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().matches(Constants.ADDON_PACK_PATTERN);
                }
                @Override
                public String getDescription() {
                    return Constants.ADDON_FILE_PATTERN_DESCRIPTION;
                }
            });            
        }
    }

    private static IconsFileChooser iconsFileChooser = new IconsFileChooser();
    private static class IconsFileChooser extends ImageFileChooser {
        private static final long serialVersionUID = 1L;
        public IconsFileChooser() {
            super(true);
            final FileFilter imgFF = getFileFilter();
            setFileFilter(new FileFilter(){
                @Override
                public boolean accept(File f) {
                    return imgFF.accept(f) || f.getName().matches(Constants.ADDON_PACK_PATTERN);
                }
                @Override
                public String getDescription() {
                    return imgFF.getDescription() + ", " + Constants.ADDON_FILE_PATTERN_DESCRIPTION;
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
            boolean lafActivationSuccess;
            Throwable error = null;
            try {
                preInit();
                lafActivationSuccess = true;
            } catch (Throwable e) {
                error = e;
                lafActivationSuccess = false;
            }
            instance = new FrontEnd();
            if (!lafActivationSuccess) {
                String laf = settings.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
                instance.displayErrorMessage(
                        "Current Look-&-Feel '" + laf + "' is broken (failed to initialize)!\n" +
                        "It will be uninstalled.", 
                        error);
                try {
                    String lafFullClassName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                    BackEnd.getInstance().uninstallLAF(lafFullClassName);
                    settings.remove(Constants.PROPERTY_LOOK_AND_FEEL);
                    BackEnd.getInstance().store();
                    instance.displayMessage(
                            "Broken Look-&-Feel '" + laf + "' has been uninstalled ;)\n" +
                            "Bias needs to be restarted now...");
                    System.exit(0);
                } catch (Exception e) {
                    instance.displayErrorMessage("Broken Look-&-Feel '" + laf + "' failed to uninstall :(", e);
                }
            }
        }
        return instance;
    }
    
    private static void preInit() throws Throwable {
        BackEnd.getInstance().load();
        settings = BackEnd.getInstance().getSettings();
        String laf = settings.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
        activateLAF(laf);
    }
    
    private static void activateLAF(String laf) throws Throwable {
        if (laf != null) {
            String lafFullClassName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                        + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
            Class lafClass = Class.forName(lafFullClassName);
            LookAndFeel lafInstance = (LookAndFeel) lafClass.newInstance();
            byte[] lafSettings = BackEnd.getInstance().getLAFSettings(lafFullClassName);
            lafInstance.activate(lafSettings);
        }
    }
    
    private static Properties settings;
    
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

    private JButton jButton9 = null;

    private JButton jButton10 = null;

    private JButton jButton11 = null;

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

            int wpxValue;
            int wpyValue;
            int wwValue;
            int whValue;
            String wpx = settings.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_X);
            if (wpx == null) {
                wpxValue = getToolkit().getScreenSize().width / 4;
            } else {
                getToolkit().getScreenSize().getWidth();
                Double.valueOf(wpx);
                wpxValue = Math.round(Float
                        .valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
            }
            String wpy = settings.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
            if (wpy == null) {
                wpyValue = getToolkit().getScreenSize().height / 4;
            } else {
                wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR
                        + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
            }
            String ww = settings.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
            if (ww == null) {
                wwValue = (getToolkit().getScreenSize().width / 4) * 2;
            } else {
                wwValue = Math
                        .round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
            }
            String wh = settings.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
            if (wh == null) {
                whValue = (getToolkit().getScreenSize().height / 4) * 2;
            } else {
                whValue = Math
                        .round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
            }

            this.setLocation(wpxValue, wpyValue);
            this.setSize(wwValue, whValue);

            representData(BackEnd.getInstance().getData());

            String lsid = settings.getProperty(Constants.PROPERTY_LAST_SELECTED_ID);
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
    
    private void setActiveLAF(String laf) throws Exception {
        boolean changed = false;
        String currentLAF = settings.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
        if (laf != null) {
            String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            if (!lafName.equals(currentLAF)) {
                settings.put(Constants.PROPERTY_LOOK_AND_FEEL, lafName);
                changed = true;
            }
        } else if (currentLAF != null) {
            settings.remove(Constants.PROPERTY_LOOK_AND_FEEL);
            changed = true;
        }
        if (!changed) {
            FrontEnd.getInstance().displayMessage("Selected Look-&-Feel is already active");
        } else {
            configureLAF(laf);
            store();
            displayMessage("Changes will take effect after Bias restart");
            System.exit(0);
        }
    }
    
    private void configureLAF(String laf) throws Exception {
        if (laf != null) {
            Class lafClass = Class.forName(laf);
            LookAndFeel lafInstance = ((LookAndFeel)lafClass.newInstance());
            byte[] lafSettings = BackEnd.getInstance().getLAFSettings(laf);
            byte[] settings = lafInstance.configure(lafSettings);
            if (!Arrays.equals(settings,lafSettings)) {
                BackEnd.getInstance().storeLAFSettings(laf, settings);
                store();
                displayMessage("Changes will take effect after Bias restart");
                System.exit(0);
            }
        }
    }
    
    private void configureExtension(String extension) throws Exception {
        if (extension != null) {
            try {
                Class extensionClass = Class.forName(extension);
                Extension extensionInstance = ExtensionFactory.getInstance().newExtension(extensionClass);
                byte[] extensionSettings = BackEnd.getInstance().getExtensionSettings(extension);
                byte[] settings = extensionInstance.configure(extensionSettings);
                BackEnd.getInstance().storeExtensionSettings(extension, settings);
            } catch (Throwable t) {
                displayErrorMessage(
                        "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize just configured settings!\n" +
                        "Settings that are failed to serialize will be lost! :(\n" + 
                        "This the most likely is an extension's bug.\n" +
                        "You can either:\n" +
                            "* check for new version of extension (the bug may be fixed in new version)\n" +
                            "* uninstall extension to avoid further instability and data loss\n" + 
                            "* refer to extension's author for further help", t);
            }
        }
    }
    
    private void representData(DataCategory data) {
        if (data.getPlacement() != null) {
            getJTabbedPane().setTabPlacement(data.getPlacement());
        }
        int brokenExtensionsFound = representData(getJTabbedPane(), data);
        if (data.getActiveIndex() != null) {
            getJTabbedPane().setSelectedIndex(data.getActiveIndex());
        }
        if (brokenExtensionsFound > 0) {
            displayErrorMessage("Some entries (" + brokenExtensionsFound + ") have not been successfully represented.\n" +
                    "Corresponding extensions seem to be broken/missing.\n" +
                    "Try to open extensions management dialog, " +
                    "it will autodetect and remove broken extensions (if any).\n" +
                    "After that, try to (re)install broken/missing extensions.\n");
        }
    }

    private int representData(JTabbedPane tabbedPane, DataCategory data) {
        int brokenExtensionsFound = 0;
        try {
        	int idx = tabbedPane.getTabCount();
            for (Recognizable item : data.getData()) {
                if (item instanceof DataEntry) {
                    DataEntry de = (DataEntry) item;
                    String caption = de.getCaption();
                    Extension extension;
                    try {
                        extension = ExtensionFactory.getInstance().newExtension(de);
                    } catch (Throwable t) {
                    	t.printStackTrace();
                    	brokenExtensionsFound++;
                        extension = new MissingExtensionInformer(de);
                    }
                    tabbedPane.addTab(caption, extension);
                    tabbedPane.setIconAt(idx++, item.getIcon());
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
                    tabbedPane.setIconAt(idx++, item.getIcon());
                    currentTabPane = categoryTabPane;
                    brokenExtensionsFound += representData(categoryTabPane, dc);
                    if (dc.getActiveIndex() != null) {
                        categoryTabPane.setSelectedIndex(dc.getActiveIndex());
                    }
                }
            }
        } catch (Exception ex) {
            displayErrorMessage("Critical error! :( Bias can not proceed further...", ex);
            System.exit(1);
        }
        return brokenExtensionsFound;
    }

    private void store() throws Exception {
        collectProperties();
        collectData();
        BackEnd.getInstance().store();
    }

    private void collectProperties() {
        settings.put(Constants.PROPERTY_WINDOW_COORDINATE_X, Constants.EMPTY_STR + getLocation().getX()
                / getToolkit().getScreenSize().getWidth());
        settings.put(Constants.PROPERTY_WINDOW_COORDINATE_Y, Constants.EMPTY_STR + getLocation().getY()
                / getToolkit().getScreenSize().getHeight());
        settings.put(Constants.PROPERTY_WINDOW_WIDTH, Constants.EMPTY_STR + getSize().getWidth()
                / getToolkit().getScreenSize().getHeight());
        settings.put(Constants.PROPERTY_WINDOW_HEIGHT, Constants.EMPTY_STR + getSize().getHeight()
                / getToolkit().getScreenSize().getHeight());
        UUID lsid = getSelectedVisualEntryID();
        if (lsid != null) {
            settings.put(Constants.PROPERTY_LAST_SELECTED_ID, lsid.toString());
        }
        BackEnd.getInstance().setSettings(settings);
    }

    private void collectData() throws Exception {
        DataCategory data = collectData("root", getJTabbedPane());
        data.setPlacement(getJTabbedPane().getTabPlacement());
        if (getJTabbedPane().getSelectedIndex() != -1) {
            data.setActiveIndex(getJTabbedPane().getSelectedIndex());
        }
        BackEnd.getInstance().setData(data);
    }

    private DataCategory collectData(String caption, JTabbedPane tabPane) throws Exception {
        DataCategory data = new DataCategory();
        data.setCaption(caption);
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            caption = tabPane.getTitleAt(i);
            Component c = tabPane.getComponent(i);
            Icon icon = tabPane.getIconAt(i);
            if (c instanceof JTabbedPane) {
                JTabbedPane tp = (JTabbedPane) c;
                DataCategory dc = collectData(caption, tp);
                if (tp.getName() != null) {
                    dc.setId(UUID.fromString(tp.getName()));
                    dc.setIcon(icon);
                    data.addDataItem(dc);
                    if (tp.getSelectedIndex() != -1) {
                        dc.setActiveIndex(tp.getSelectedIndex());
                    }
                }
                dc.setPlacement(tp.getTabPlacement());
            } else if (c instanceof Extension) {
                Extension extension = (Extension) c;
                byte[] serializedData = null;
                try {
                    serializedData = extension.serializeData();
                } catch (Throwable t) {
                    displayErrorMessage(
                            "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize some data!\n" +
                            "Data that are failed to serialize will be lost! :(\n" + 
                            "This the most likely is an extension's bug.\n" +
                            "You can either:\n" +
                                "* check for new version of extension (the bug may be fixed in new version)\n" +
                                "* uninstall extension to avoid further instability and data loss\n" + 
                                "* refer to extension's author for further help", t);
                }
                byte[] serializedSettings = null;
                try {
                    serializedSettings = extension.serializeSettings();
                } catch (Throwable t) {
                    displayErrorMessage(
                            "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize some settings!\n" +
                            "Settings that are failed to serialize will be lost! :(\n" + 
                            "This the most likely is an extension's bug.\n" +
                            "You can either:\n" +
                                "* check for new version of extension (the bug may be fixed in new version)\n" +
                                "* uninstall extension to avoid further instability and data loss\n" + 
                                "* refer to extension's author for further help", t);
                }
                DataEntry dataEntry;
                String type = null;
                if (extension instanceof MissingExtensionInformer) {
                    dataEntry = ((MissingExtensionInformer) extension).getDataEntry();
                    if (BackEnd.getInstance().getExtensions().contains(dataEntry.getType())) {
                        type = dataEntry.getType().replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                    }
                } else {
                    type = extension.getClass().getPackage().getName()
                                .replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                }
                dataEntry = new DataEntry(extension.getId(), caption, icon, type, serializedData, serializedSettings);
                data.addDataItem(dataEntry);
            }
        }
        return data;
    }

    public UUID getSelectedVisualEntryID() {
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

    public void displayErrorMessage(Throwable t) {
        JOptionPane.showMessageDialog(FrontEnd.this, "Details: " + t, "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace();
    }

    public void displayErrorMessage(String message, Throwable t) {
        JOptionPane.showMessageDialog(FrontEnd.this, message, "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace();
    }

    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(FrontEnd.this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(FrontEnd.this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
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
                JLabel icLabel = new JLabel("Choose icon:");
                JComboBox iconChooser = new JComboBox();
                iconChooser.addItem(new ImageIcon(new byte[]{}, Constants.EMPTY_STR));
                for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                    iconChooser.addItem(icon);
                }
                ImageIcon ic = (ImageIcon) tabbedPane.getIconAt(tabbedPane.getSelectedIndex());
                if (ic != null) {
                    for (int i = 0; i < iconChooser.getItemCount(); i++) {
                    	if (((ImageIcon) iconChooser.getItemAt(i)).getDescription().equals(ic.getDescription())) {
                    		iconChooser.setSelectedIndex(i);
                    		break;
                    	}
                    }
                }
                JLabel cLabel = new JLabel("Item's caption:");
                caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser, icLabel, iconChooser, cLabel },
                        caption);
                if (caption != null) {
                	tabbedPane.setTitleAt(index, caption);
                	tabbedPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
                    ImageIcon icon = (ImageIcon) iconChooser.getSelectedItem();
                    if (icon != null) {
                    	tabbedPane.setIconAt(tabbedPane.getSelectedIndex(), icon);
                    }
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

            // get tabpane's components/captions/icons
            Component[] components = new Component[cnt];
            for (int i = 0; i< cnt; i++) {
                components[i] = tabPane.getComponent(i);
            }
            String[] captions = new String[cnt];
            for (int i = 0; i < cnt; i++) {
                captions[i] = tabPane.getTitleAt(i);
            }
            ImageIcon[] icons = new ImageIcon[cnt];
            for (int i = 0; i < cnt; i++) {
                icons[i] = (ImageIcon) tabPane.getIconAt(i);
            }

            // remember component/caption that has to be moved
            Component srcComp = components[srcIndex];
            String srcCap = captions[srcIndex];
            ImageIcon srcIcon = icons[srcIndex];
            
            // rearrange components/captions using shifting
            if (srcIndex > dstIndex) {
                for (int i = srcIndex; i > dstIndex; i--) {
                    components[i] = components[i-1];
                    captions[i] = captions[i-1];
                    icons[i] = icons[i-1];
                }
            } else {
                for (int i = srcIndex; i < dstIndex; i++) {
                    components[i] = components[i+1];
                    captions[i] = captions[i+1];
                    icons[i] = icons[i+1];
                }
            }

            // set moved component/caption to its new position
            components[dstIndex] = srcComp;
            captions[dstIndex] = srcCap;
            icons[dstIndex] = srcIcon;
            
            // remove everything from tabpane before repopulating it
            tabPane.removeAll();
            
            // repopulate tabpane with resulting components/captions
            for (int i = 0; i < cnt; i++) {
                tabPane.addTab(captions[i], icons[i], components[i]);
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
            jToolBar.add(getJButton10());
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
            jToolBar2.add(getJButton9()); // Generated
            jToolBar2.add(getJButton11()); // Generated
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
            jButton7.setToolTipText("save & exit"); // Generated
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
     * This method initializes jButton11
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton11() {
        if (jButton11 == null) {
            jButton11 = new JButton(manageLAFAction);
            jButton11.setToolTipText("manage look-&-feel"); // Generated
            jButton11.setIcon(ICON_LAF);
        }
        return jButton11;
    }

    /**
     * This method initializes jButton9
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton9() {
        if (jButton9 == null) {
            jButton9 = new JButton(manageIconsAction);
            jButton9.setToolTipText("manage icons"); // Generated
            jButton9.setIcon(ICON_ICONS);
        }
        return jButton9;
    }

    /**
     * This method initializes jButton10
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton10() {
        if (jButton10 == null) {
            jButton10 = new JButton(discardUnsavedChangesAction);
            jButton10.setToolTipText("exit & discard unsaved changes"); // Generated
            jButton10.setIcon(ICON_DISCARD_UNSAVED_CHANGES);
        }
        return jButton10;
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
        JLabel icLabel = new JLabel("Choose icon:");
        JComboBox iconChooser = new JComboBox();
        iconChooser.addItem(new ImageIcon(new byte[]{}, Constants.EMPTY_STR));
        for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
            iconChooser.addItem(icon);
        }
        String categoryCaption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser, icLabel, iconChooser },
                "New root category:", JOptionPane.QUESTION_MESSAGE);
        if (categoryCaption != null) {
            JTabbedPane categoryTabPane = new JTabbedPane();
            categoryTabPane.setName(UUID.randomUUID().toString());
            categoryTabPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
            addTabPaneListeners(categoryTabPane);
            getJTabbedPane().addTab(categoryCaption, categoryTabPane);
            getJTabbedPane().setSelectedComponent(categoryTabPane);
            ImageIcon icon = (ImageIcon) iconChooser.getSelectedItem();
            if (icon != null) {
            	getJTabbedPane().setIconAt(getJTabbedPane().getSelectedIndex(), icon);
            }
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
            for (String entryType : ExtensionFactory.getInstance().getAnnotatedExtensions().keySet()) {
                entryTypeComboBox.addItem(entryType);
            }
            if (lastAddedEntryType != null) {
                entryTypeComboBox.setSelectedItem(lastAddedEntryType);
            }
            entryTypeComboBox.setEditable(false);
            JLabel icLabel = new JLabel("Choose icon:");
            JComboBox iconChooser = new JComboBox();
            iconChooser.addItem(new ImageIcon(new byte[]{}, Constants.EMPTY_STR));
            for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                iconChooser.addItem(icon);
            }
            String caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { entryTypeLabel, entryTypeComboBox, icLabel, iconChooser },
                    "New entry:", JOptionPane.QUESTION_MESSAGE);
            if (caption != null) {
                String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                lastAddedEntryType = typeDescription;
                Class type = ExtensionFactory.getInstance().getAnnotatedExtensions().get(typeDescription);
                byte[] defSettings = BackEnd.getInstance().getExtensionSettings(type.getName());
                if (defSettings == null) {
                    // extension's first time usage
                    displayMessage(
                            "This is first time you use '" + type.getSimpleName() + "' extension.\n" +
                            "You can adjust extension's default settings...");
                    configureExtension(type.getName());
                }
                Extension extension = ExtensionFactory.getInstance().newExtension(type);
                if (extension != null) {
                    getJTabbedPane().addTab(caption, extension);
                    getJTabbedPane().setSelectedComponent(extension);
                    ImageIcon icon = (ImageIcon) iconChooser.getSelectedItem();
                    if (icon != null) {
                    	getJTabbedPane().setIconAt(getJTabbedPane().getSelectedIndex(), icon);
                    }
                }
            }
        } catch (Throwable t) {
            displayErrorMessage("Unable to add entry.\n" +
            					"Some extension(s) may be broken.\n" +
            					"Try to open extensions management dialog, " +
            					"it will autodetect and remove broken extensions.\n" +
            					"After that, try to add entry again.", t);
        }
    }
    
    private Action addEntryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
            try {
                if (getJTabbedPane().getTabCount() == 0) {
                    if (!defineRootPlacement()) {
                        return;
                    }
                }
                if (getJTabbedPane().getTabCount() == 0 || getJTabbedPane().getSelectedIndex() == -1) {
                    currentTabPane = getJTabbedPane();
                }
                JLabel entryTypeLabel = new JLabel("Type:");
                JComboBox entryTypeComboBox = new JComboBox();
                for (String entryType : ExtensionFactory.getInstance().getAnnotatedExtensions().keySet()) {
                    entryTypeComboBox.addItem(entryType);
                }
                if (lastAddedEntryType != null) {
                    entryTypeComboBox.setSelectedItem(lastAddedEntryType);
                }
                entryTypeComboBox.setEditable(false);
                JLabel icLabel = new JLabel("Choose icon:");
                JComboBox iconChooser = new JComboBox();
                iconChooser.addItem(new ImageIcon(new byte[]{}, Constants.EMPTY_STR));
                for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                    iconChooser.addItem(icon);
                }
                String caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { entryTypeLabel, entryTypeComboBox, icLabel, iconChooser },
                        "New entry:", JOptionPane.QUESTION_MESSAGE);
                if (caption != null) {
                    String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                    lastAddedEntryType = typeDescription;
                    Class type = ExtensionFactory.getInstance().getAnnotatedExtensions().get(typeDescription);
                    byte[] defSettings = BackEnd.getInstance().getExtensionSettings(type.getName());
                    if (defSettings == null) {
                        // extension's first time usage
                        displayMessage(
                                "This is first time you use '" + type.getSimpleName() + "' extension.\n" +
                                "You can adjust extension's default settings...");
                        configureExtension(type.getName());
                    }
                    Extension extension = ExtensionFactory.getInstance().newExtension(type);
                    if (extension != null) {
                        currentTabPane.addTab(caption, extension);
                        currentTabPane.setSelectedComponent(extension);
                        ImageIcon icon = (ImageIcon) iconChooser.getSelectedItem();
                        if (icon != null) {
                            currentTabPane.setIconAt(currentTabPane.getSelectedIndex(), icon);
                        }
                    }
                }
            } catch (Throwable t) {
                displayErrorMessage("Unable to add entry.\n" +
                                    "Some extension(s) may be broken.\n" +
                                    "Try to open extensions management dialog, " +
                                    "it will autodetect and remove broken extensions.\n" +
                                    "After that, try to add entry again.", t);
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
                    DataCategory data = BackEnd.getInstance().importData(jarFile, getVisualEntriesIDs());
                    if (!data.getData().isEmpty()) {
                        representData(data);
                        store();
                        displayMessage(
                                "Some data have been successfully imported.\n" +
                                "Bias needs to be restarted now...");
                        System.exit(0);
                    } else {
                        displayErrorMessage("Nothing to import!");
                    }
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
                if (getJTabbedPane().getTabCount() == 0) {
                    if (!defineRootPlacement()) {
                        return;
                    }
                }
                if (getJTabbedPane().getTabCount() == 0 || getJTabbedPane().getSelectedIndex() == -1) {
                    currentTabPane = getJTabbedPane();
                }
                JLabel pLabel = new JLabel("Choose placement:");
                JComboBox placementsChooser = new JComboBox();
                for (Placement placement : PLACEMENTS) {
                    placementsChooser.addItem(placement);
                }
                JLabel icLabel = new JLabel("Choose icon:");
                JComboBox iconChooser = new JComboBox();
                iconChooser.addItem(new ImageIcon(new byte[]{}, Constants.EMPTY_STR));
                for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                    iconChooser.addItem(icon);
                }
                String categoryCaption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser, icLabel, iconChooser },
                        "New category:", JOptionPane.QUESTION_MESSAGE);
                if (categoryCaption != null) {
                    JTabbedPane categoryTabPane = new JTabbedPane();
                    UUID id = UUID.randomUUID();
                    categoryTabPane.setName(id.toString());
                    categoryTabPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
                    addTabPaneListeners(categoryTabPane);
                    currentTabPane.addTab(categoryCaption, categoryTabPane);
                    JTabbedPane parentTabPane = ((JTabbedPane) categoryTabPane.getParent());
                    parentTabPane.setSelectedComponent(categoryTabPane);
                    ImageIcon icon = (ImageIcon) iconChooser.getSelectedItem();
                    if (icon != null) {
                        parentTabPane.setIconAt(parentTabPane.getSelectedIndex(), icon);
                    }
                    currentTabPane = (JTabbedPane) categoryTabPane.getParent();
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
                // show confirmation dialog
                if (JOptionPane.showConfirmDialog(FrontEnd.getInstance(), 
                        "The data are going to be saved now.\n" +
                        "This will finish your current Bias session.\n" +
                        "Are you sure you want save and exit?",
                        "Save data and exit",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    // store, then exit
                    store();
                    System.exit(0);
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    private Action discardUnsavedChangesAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
            // show confirmation dialog
            if (JOptionPane.showConfirmDialog(FrontEnd.getInstance(), 
                    "All unsaved data will be lost.\n" +
                    "Are you sure you want to discard chages?",
                    "Discard unsaved changes confirmation",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // store nothing, just exit
                System.exit(0);
            }
        }
    };
    
    private Action manageExtensionsAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        private Map<String, String> components;
        private DefaultListModel model;
        private JList extList;
        private boolean modified;
        
        public void actionPerformed(ActionEvent e) {
            try {
                JLabel extLabel = new JLabel("Extensions Management");
                extList = new JList(new DefaultListModel());
                model = (DefaultListModel) extList.getModel();
                components = new HashMap<String, String>();
                boolean brokenFixed = false;
                for (String extension : BackEnd.getInstance().getExtensions()) {
                    String annotationStr;
                    try {
                        Class<?> extClass = Class.forName(extension);
                        AddOnAnnotation extAnn = 
                            (AddOnAnnotation) extClass.getAnnotation(AddOnAnnotation.class);
                        if (extAnn != null) {
                            annotationStr = extClass.getSimpleName() + Constants.SPACE_STR 
                                            + ", version: " + extAnn.version() + Constants.SPACE_STR 
                                            + ", author: " + extAnn.author() + Constants.SPACE_STR
                                            + ", description: " + extAnn.description();
                        } else {
                            annotationStr = extClass.getSimpleName() + " [ Extension Info Is Missing ]";
                        }
                        // extension instantiation test
                        ExtensionFactory.getInstance().newExtension(extClass);
                        // extension is ok, add it to the list
                        model.addElement(annotationStr);
                        components.put(annotationStr, extClass.getName());
                    } catch (Throwable t) {
                        // broken extension found, inform user about that...
                        String extensionName = extension.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                        displayErrorMessage("Extension [ " + extensionName + " ] is broken and will be uninstalled!", t);
                        // ... and try uninstall broken extension...
                        try {
                            BackEnd.getInstance().uninstallExtension(extension);
                            displayMessage("Broken extension [ " + extensionName + " ] has been uninstalled");
                            brokenFixed = true;
                        } catch (Exception ex2) {
                            // ... if unsuccessfully - inform user about that, do nothing else
                            displayErrorMessage("Error occured while uninstalling broken extension [ " + extensionName + " ]!\n" + ex2);
                        }
                    }
                }
                if (brokenFixed) {
                    store();
                    displayMessage("Bias needs to be restarted now...");
                    System.exit(0);
                }
                JButton configButt = new JButton("Configure selected");
                configButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extList.getSelectedValues().length == 1) {
                            try {
                                String extension = (String) components.get(extList.getSelectedValue());
                                configureExtension(extension);
                            } catch (Exception ex) {
                                FrontEnd.getInstance().displayErrorMessage(ex);
                            }
                        } else {
                            displayMessage("Please, choose only one extension from the list");
                        }
                    }
                });
                JButton instButt = new JButton("Install new");
                instButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extensionFileChooser.showOpenDialog(FrontEnd.getInstance()) == JFileChooser.APPROVE_OPTION) {
                            try {
                                for (File file : extensionFileChooser.getSelectedFiles()) {
                                    BackEnd.getInstance().installExtension(file);
                                    model.addElement("Extension installed from: " + file.getAbsolutePath());
                                    modified = true;
                                }
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
                            if (extList.getSelectedValues().length > 0) {
                                for (Object extension : extList.getSelectedValues()) {
                                    String extClassName = components.get(extension);
                                    if (extClassName == null) {
                                        displayMessage(
                                                "This Extension can not be uninstalled now.\n" +
                                                "Restart Bias first.");
                                    } else {
                                        BackEnd.getInstance().uninstallExtension(extClassName);
                                        model.removeElement(extension);
                                        modified = true;
                                    }
                                }
                                FrontEnd.getInstance().displayMessage("Extension(s) have been successfully uninstalled!");
                            }
                        } catch (Exception ex) {
                            FrontEnd.getInstance().displayErrorMessage(ex);
                        }
                    }
                });
                modified = false;
                JOptionPane.showMessageDialog(
                    FrontEnd.this, 
                    new Component[]{
                            extLabel,
                            new JScrollPane(extList),
                            configButt,
                            instButt,
                            uninstButt
                    },
                    "Manage Extensions",
                    JOptionPane.INFORMATION_MESSAGE,
                    null
                );
                if (modified) {
                    FrontEnd.getInstance().displayMessage("Changes will take effect after Bias restart");
                    store();
                    System.exit(0);
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };

    private Action manageLAFAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        private Map<String, String> components;
        private DefaultListModel model;
        private JList lafList;
        private boolean modified;
        
        public void actionPerformed(ActionEvent e) {
            try {
                JLabel lafLabel = new JLabel("Look-&-Feel Management");
                lafList = new JList(new DefaultListModel());
                model = (DefaultListModel) lafList.getModel();
                model.addElement(DEFAULT_LOOK_AND_FEEL);
                components = new HashMap<String, String>();
                components.put(DEFAULT_LOOK_AND_FEEL, null);
                boolean brokenFixed = false;
                for (String laf : BackEnd.getInstance().getLAFs().keySet()) {
                    String annotationStr;
                    try {
                        Class<?> lafClass = Class.forName(laf);
                        AddOnAnnotation lafAnn = 
                            (AddOnAnnotation) lafClass.getAnnotation(AddOnAnnotation.class);
                        if (lafAnn != null) {
                            annotationStr = lafClass.getSimpleName() + Constants.SPACE_STR 
                                            + ", version: " + lafAnn.version() + Constants.SPACE_STR 
                                            + ", author: " + lafAnn.author() + Constants.SPACE_STR
                                            + ", description: " + lafAnn.description();
                        } else {
                            annotationStr = lafClass.getSimpleName() + " [ Look-&-Feel Info Is Missing ]";
                        }
                        // laf instantiation test
                        lafClass.newInstance();
                        // laf is ok, add it to the list
                        model.addElement(annotationStr);
                        components.put(annotationStr, lafClass.getName());
                    } catch (Throwable t) {
                        // broken laf found, inform user about that...
                        String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                        displayErrorMessage("Look-&-Feel [ " + lafName + " ] is broken and will be uninstalled!", t);
                        // ... and try uninstall broken laf...
                        try {
                            BackEnd.getInstance().uninstallLAF(laf);
                            displayMessage("Broken Look-&-Feel [ " + lafName + " ] has been uninstalled");
                            brokenFixed = true;
                        } catch (Exception ex2) {
                            // ... if unsuccessfully - inform user about that, do nothing else
                            displayErrorMessage("Error occured while uninstalling broken Look-&-Feel [ " + lafName + " ]!\n" + ex2);
                        }
                    }
                }
                if (brokenFixed) {
                    store();
                    displayMessage("Bias needs to be restarted now...");
                    System.exit(0);
                }
                JButton activateButt = new JButton("Activate Look-&-Feel");
                activateButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (lafList.getSelectedValue() != null) {
                            String description = (String) lafList.getSelectedValue();
                            if (DEFAULT_LOOK_AND_FEEL.equals(description)) {
                                try {
                                    setActiveLAF(null);
                                } catch (Exception t) {
                                    displayErrorMessage(t);
                                }
                            } else {
                                String lafClassName = components.get(description);
                                if (lafClassName == null) {
                                    displayMessage(
                                            "This Look-&-Feel can not be activated yet.\n" +
                                            "Restart Bias first.");
                                } else {
                                    try {
                                        setActiveLAF(lafClassName);
                                    } catch (Exception t) {
                                        displayErrorMessage(t);
                                    }
                                }
                            }
                        }
                    }
                });
                JButton configButt = new JButton("Configure Look-&-Feel");
                configButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (lafList.getSelectedValue() != null) {
                            if (DEFAULT_LOOK_AND_FEEL.equals(lafList.getSelectedValue())) {
                                displayErrorMessage(
                                        "Default Look-&-Feel is not configurable");
                            } else {
                                String lafClassName = components.get(lafList.getSelectedValue());
                                if (lafClassName == null) {
                                    displayMessage(
                                            "This Look-&-Feel can not be configured yet.\n" +
                                            "Restart Bias first.");
                                } else {
                                    try {
                                        configureLAF(lafClassName);
                                    } catch (Exception t) {
                                        displayErrorMessage(t);
                                    }
                                }
                            }
                        }
                    }
                });
                JButton instButt = new JButton("Install new");
                instButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extensionFileChooser.showOpenDialog(FrontEnd.getInstance()) == JFileChooser.APPROVE_OPTION) {
                            try {
                                for (File file : extensionFileChooser.getSelectedFiles()) {
                                    BackEnd.getInstance().installLAF(file);
                                    model.addElement("Look-&-Feel installed from: " + file.getAbsolutePath());
                                    modified = true;
                                }
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
                            if (lafList.getSelectedValues().length > 0) {
                                boolean uninstalled = false;
                                String currentLAF = settings.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
                                for (Object laf : lafList.getSelectedValues()) {
                                    if (!DEFAULT_LOOK_AND_FEEL.equals(laf)) {
                                        String lafClassName = components.get(laf);
                                        if (lafClassName == null) {
                                            displayMessage(
                                                    "This Look-&-Feel can not be uninstalled now.\n" +
                                                    "Restart Bias first.");
                                        } else {
                                            BackEnd.getInstance().uninstallLAF(lafClassName);
                                            model.removeElement(laf);
                                            String uninstalledClassName = lafClassName.replaceFirst(
                                                    Constants.PACKAGE_PREFIX_PATTERN, 
                                                    Constants.EMPTY_STR);
                                            // if look-&-feel that has been uninstalled was active one...
                                            if (uninstalledClassName.equals(currentLAF)) {
                                                //... unset it (default one will be used)
                                                settings.remove(Constants.PROPERTY_LOOK_AND_FEEL);
                                            }
                                            uninstalled = true;
                                            modified = true;
                                        }
                                    } else {
                                        FrontEnd.getInstance().displayMessage("Default Look-&-Feel can not be uninstalled!");
                                    }
                                }
                                if (uninstalled) {
                                    FrontEnd.getInstance().displayMessage("Look-&-Feel(s) have been successfully uninstalled!");
                                }
                            }
                        } catch (Exception ex) {
                            FrontEnd.getInstance().displayErrorMessage(ex);
                        }
                    }
                });
                modified = false;
                JOptionPane.showMessageDialog(
                    FrontEnd.this, 
                    new Component[]{
                            lafLabel,
                            new JScrollPane(lafList),
                            activateButt,
                            configButt,
                            instButt,
                            uninstButt
                    },
                    "Manage Look-&-Feel",
                    JOptionPane.INFORMATION_MESSAGE,
                    null
                );
                if (modified) {
                    FrontEnd.getInstance().displayMessage("Bias needs to be restarted now...");
                    store();
                    System.exit(0);
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };

    private Action manageIconsAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        private Collection<ImageIcon> icons;
        private JList icList;
        private DefaultListModel model;
        private boolean modified;
        
        public void actionPerformed(ActionEvent e) {
            try {
                JLabel icLabel = new JLabel("Icons Management");
                model = new DefaultListModel();
                icList = new JList(model);
                icons = new LinkedList<ImageIcon>();
                for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                    model.addElement(icon);
                    icons.add(icon);
                }
                JScrollPane jsp = new JScrollPane(icList);
                jsp.setPreferredSize(new Dimension(200,200));
                jsp.setMinimumSize(new Dimension(200,200));
                JButton addButt = new JButton("Add new");
                addButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (iconsFileChooser.showOpenDialog(FrontEnd.getInstance()) == JFileChooser.APPROVE_OPTION) {
                            try {
                                boolean added = false;
                                for (File file : iconsFileChooser.getSelectedFiles()) {
                                    Collection<ImageIcon> icons = BackEnd.getInstance().addIcons(file);
                                    if (!icons.isEmpty()) {
                                        for (ImageIcon icon : icons) {
                                            model.addElement(icon);
                                        }
                                        added = true;
                                        modified = true;
                                    }
                                }
                                if (added) {
                                    FrontEnd.getInstance().displayMessage("Icon(s) have been successfully installed!");
                                } else {
                                    FrontEnd.getInstance().displayErrorMessage("Nothing to install!");
                                }
                            } catch (Exception ex) {
                                FrontEnd.getInstance().displayErrorMessage(ex);
                            }
                        }
                    }
                });
                JButton removeButt = new JButton("Remove selected");
                removeButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (icList.getSelectedValues().length > 0) {
                                for (Object icon : icList.getSelectedValues()) {
                                    BackEnd.getInstance().removeIcon((ImageIcon)icon);
                                    model.removeElement(icon);
                                    modified = true;
                                }
                                FrontEnd.getInstance().displayMessage("Icon(s) have been successfully removed!");
                            }
                        } catch (Exception ex) {
                            FrontEnd.getInstance().displayErrorMessage(ex);
                        }
                    }
                });
                modified = false;
                JOptionPane.showMessageDialog(
                    FrontEnd.this, 
                    new Component[]{
                            icLabel,
                            jsp,
                            addButt,
                            removeButt
                    },
                    "Manage Icons",
                    JOptionPane.INFORMATION_MESSAGE,
                    null
                );
                if (modified) {
                    FrontEnd.getInstance().displayMessage("Changes will take effect after Bias restart");
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
