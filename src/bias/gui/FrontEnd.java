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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import bias.Constants;
import bias.Launcher;
import bias.Preferences;
import bias.annotation.AddOnAnnotation;
import bias.annotation.PreferenceAnnotation;
import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.core.SearchCriteria;
import bias.core.SearchEngine;
import bias.core.SearchEngine.HighLightMarker;
import bias.extension.Extension;
import bias.extension.ExtensionFactory;
import bias.extension.MissingExtensionInformer;
import bias.gui.utils.ImageFileChooser;
import bias.laf.ControlIcons;
import bias.laf.LookAndFeel;
import bias.utils.AppManager;
import bias.utils.FSUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;


/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_LOOK_AND_FEEL = "DefaultLAF";

    /**
     * Application icon
     */
    private static final ImageIcon ICON_APP = new ImageIcon(Constants.class.getResource("/bias/res/app_icon.png"));
    private static final ImageIcon ICON_CLOSE = new ImageIcon(Constants.class.getResource("/bias/res/close.png"));

    private static final String RESTART_MESSAGE = "Changes will take effect after Bias restart";

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
    
    private static AddOnFileChooser extensionFileChooser = new AddOnFileChooser();
    private static AddOnFileChooser lafFileChooser = new AddOnFileChooser();
    private static class AddOnFileChooser extends JFileChooser {
        private static final long serialVersionUID = 1L;

        public AddOnFileChooser() {
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
    
    private static class IconViewPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int MAX_ICON_WIDTH = 32;
        private static final int MAX_ICON_HEIGHT = 32;
        private Image image;
        public IconViewPanel(Image image) {
            if (image != null) {
                this.image = image;
                int previewWidth;
                int previewHeight;
                int imWidth = image.getWidth(this);
                int imHeight = image.getHeight(this);
                if (imWidth > MAX_ICON_WIDTH || imHeight > MAX_ICON_HEIGHT) {
                    if (imWidth >= imHeight){
                        previewHeight = (int)(imHeight/((float)imWidth/MAX_ICON_WIDTH));
                        previewWidth = MAX_ICON_WIDTH;
                    } else {
                        previewWidth = (int)(imWidth/((float)imHeight/MAX_ICON_HEIGHT));
                        previewHeight = MAX_ICON_HEIGHT;
                    }
                } else {
                    previewWidth = imWidth;
                    previewHeight = imHeight;
                }
                this.image = image.getScaledInstance(previewWidth, previewHeight, Image.SCALE_FAST);
                this.setPreferredSize(new Dimension(previewWidth, previewHeight));
            }
        }     
            
        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(image, 0, 0, this);
        }
    }

    private static FrontEnd instance;

    private static final String ADDON_ANN_FIELD_VALUE_NA = "N/A";
    
    // use default control icons initially
    private static ControlIcons controlIcons = new ControlIcons();

    private static Properties config;
    
    private static Map<String, byte[]> initialLAFSettings = new HashMap<String, byte[]>();
    
    private String lastAddedEntryType = null;
    
    private SearchCriteria lastSearchCriteria = null;
    
    private static String activeLAF = null;

    private JTabbedPane currentTabPane = null;
    
    private JPanel jContentPane = null;

    private JTabbedPane jTabbedPane = null;

    private JToolBar jToolBar = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    private JButton jButton2 = null;

    private JButton jButton4 = null;

    private JButton jButton5 = null;

    private JButton jButton11 = null;

    private JButton jButton12 = null;
    
    private JPanel jPanel = null;

    private JPanel jPanel2 = null;

    private JPanel jPanel3 = null;

    private JPanel jPanel4 = null;

    private JToolBar jToolBar2 = null;

    private JButton jButton6 = null;

    private JButton jButton7 = null;

    private JButton jButton8 = null;

    private JButton jButton9 = null;

    private JButton jButton10 = null;

    private JButton jButton3 = null;

    /**
     * Default singleton's hidden constructor without parameters
     */
    private FrontEnd() {
        super();
        initialize();
    }

    public static void display() {
        getInstance().setVisible(true);
    }
    
    private static FrontEnd getInstance() {
        if (instance == null) {
            preInit();
            activateLAF();
            instance = new FrontEnd();
            if (Preferences.getInstance().useSysTrayIcon) {
                // setup system tray and tray icon
                if (!SystemTray.isSupported()) {
                    System.err.println("System tray API is not available on this platform!");
                } else {
                    try {
                        SystemTray sysTray = SystemTray.getSystemTray();
                        TrayIcon trayIcon = new TrayIcon(
                                ICON_APP.getImage(), 
                                "Bias :: Personal Information Manager");
                        trayIcon.setImageAutoSize(true);
                        trayIcon.addMouseListener(new MouseAdapter(){
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                instance.setVisible(!instance.isVisible());
                            }
                        });
                        sysTray.add(trayIcon);
                    } catch (Exception ex) {
                        System.err.println(
                                "Failed to initialize system tray!" + Constants.NEW_LINE 
                                + "Error details: " + Constants.NEW_LINE);
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
        return instance;
    }
    
    private static void preInit() {
        try {
            BackEnd.getInstance().load();
            config = BackEnd.getInstance().getConfig();
        } catch (Throwable t) {
            displayErrorMessage(
                    "Bias has failed to load data!" + Constants.NEW_LINE +
                    "It seems that you have typed wrong password...",
                    t);
            System.exit(1);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void activateLAF() {
        String laf = config.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
        if (laf != null) {
            try {
                String lafFullClassName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                Class<LookAndFeel> lafClass = (Class<LookAndFeel>) Class.forName(lafFullClassName);
                LookAndFeel lafInstance = lafClass.newInstance();
                byte[] lafSettings = BackEnd.getInstance().getLAFSettings(lafFullClassName);
                lafInstance.activate(lafSettings);
                // use control icons defined by LAF if available
                if (lafInstance.getControlIcons() != null) {
                    controlIcons = lafInstance.getControlIcons();
                }
                if (activeLAF == null) {
                    if (laf != null) {
                        activeLAF = laf;
                    } else {
                        activeLAF = DEFAULT_LOOK_AND_FEEL;
                    }
                }
            } catch (Throwable t) {
                activeLAF = DEFAULT_LOOK_AND_FEEL;
                System.err.println(
                        "Current Look-&-Feel '" + laf + "' is broken (failed to initialize)!" + Constants.NEW_LINE +
                        "It will be uninstalled." + Constants.NEW_LINE + "Error details: " + Constants.NEW_LINE);
                t.printStackTrace();
                try {
                    String lafFullClassName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                    BackEnd.getInstance().uninstallLAF(lafFullClassName);
                    config.remove(Constants.PROPERTY_LOOK_AND_FEEL);
                    System.out.println(
                            "Broken Look-&-Feel '" + laf + "' has been uninstalled ;)" + Constants.NEW_LINE +
                            RESTART_MESSAGE);
                } catch (Throwable t2) {
                    System.err.println("Broken Look-&-Feel '" + laf + "' failed to uninstall :(" + Constants.NEW_LINE 
                            + "Error details: " + Constants.NEW_LINE);
                    t2.printStackTrace();
                }
            }
        } else {
            activeLAF = DEFAULT_LOOK_AND_FEEL;
        }
    }
    
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
            String wpx = config.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_X);
            if (wpx == null) {
                wpxValue = getToolkit().getScreenSize().width / 4;
            } else {
                getToolkit().getScreenSize().getWidth();
                Double.valueOf(wpx);
                wpxValue = Math.round(Float
                        .valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
            }
            String wpy = config.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
            if (wpy == null) {
                wpyValue = getToolkit().getScreenSize().height / 4;
            } else {
                wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR
                        + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
            }
            String ww = config.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
            if (ww == null) {
                wwValue = (getToolkit().getScreenSize().width / 4) * 2;
            } else {
                wwValue = Math
                        .round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
            }
            String wh = config.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
            if (wh == null) {
                whValue = (getToolkit().getScreenSize().height / 4) * 2;
            } else {
                whValue = Math
                        .round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
            }

            this.setLocation(wpxValue, wpyValue);
            this.setSize(wwValue, whValue);

            representData(BackEnd.getInstance().getData());

            String lsid = config.getProperty(Constants.PROPERTY_LAST_SELECTED_ID);
            if (lsid != null) {
                this.switchToVisualEntry(getJTabbedPane(), UUID.fromString(lsid), new LinkedList<Component>());
            }

            this.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        store();
                        cleanUp();
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });

        } catch (Exception ex) {
            displayErrorMessage(ex);
        }
    }
    
    private boolean setActiveLAF(String laf) throws Exception {
        boolean lafChanged = false;
        String currentLAF = config.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
        if (laf != null) {
            String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            if (!lafName.equals(currentLAF)) {
                config.put(Constants.PROPERTY_LOOK_AND_FEEL, lafName);
                configureLAF(laf);
                lafChanged = true;
            } else {
                lafChanged = configureLAF(laf);
            }
        } else if (currentLAF != null) {
            config.remove(Constants.PROPERTY_LOOK_AND_FEEL);
            lafChanged = true;
        }
        return lafChanged;
    }
    
    @SuppressWarnings("unchecked")
    private boolean configureLAF(String laf) throws Exception {
        boolean lafChanged = false;
        if (laf != null) {
            Class<LookAndFeel> lafClass = (Class<LookAndFeel>) Class.forName(laf);
            LookAndFeel lafInstance = lafClass.newInstance();
            byte[] lafSettings = BackEnd.getInstance().getLAFSettings(laf);
            byte[] settings = lafInstance.configure(lafSettings);
            // store if differs from stored version
            if (!PropertiesUtils.deserializeProperties(settings).equals(PropertiesUtils.deserializeProperties(lafSettings))) {
                BackEnd.getInstance().storeLAFSettings(laf, settings);
            }
            // find out if differs from initial version
            byte[] initialSettings = initialLAFSettings.get(laf);
            if (initialSettings == null) {
                initialLAFSettings.put(laf, settings);
                initialSettings = settings;
            }
            lafChanged = !PropertiesUtils.deserializeProperties(settings).equals(PropertiesUtils.deserializeProperties(initialSettings));
        }
        return lafChanged;
    }
    
    @SuppressWarnings("unchecked")
    private void configureExtension(String extension, boolean showFirstTimeUsageMessage) throws Exception {
        if (extension != null) {
            if (showFirstTimeUsageMessage) {
                String extName = extension.replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                displayMessage(
                        "This is first time you use '" + extName + "' extension." + Constants.NEW_LINE +
                        "If extension is configurable, you can adjust its default settings...");
            }
            try {
                Class<Extension> extensionClass = (Class<Extension>) Class.forName(extension);
                Extension extensionInstance = ExtensionFactory.getInstance().newExtension(extensionClass);
                byte[] settings = extensionInstance.configure(null);
                if (settings == null) {
                    settings = new byte[]{};
                }
                BackEnd.getInstance().storeExtensionSettings(extension, settings);
            } catch (Throwable t) {
                displayErrorMessage(
                        "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize just configured settings!" + Constants.NEW_LINE +
                        "Settings that are failed to serialize will be lost! :(" + Constants.NEW_LINE + 
                        "This the most likely is an extension's bug." + Constants.NEW_LINE +
                        "You can either:" + Constants.NEW_LINE +
                            "* check for new version of extension (the bug may be fixed in new version)" + Constants.NEW_LINE +
                            "* uninstall extension to avoid further instability and data loss" + Constants.NEW_LINE + 
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
            System.err.println("Some entries (" + brokenExtensionsFound + ") have not been successfully represented." + Constants.NEW_LINE +
                    "Corresponding extensions seem to be broken/missing." + Constants.NEW_LINE +
                    "Try to open extensions management dialog, " +
                    "it will autodetect and remove broken extensions (if any)." + Constants.NEW_LINE +
                    "After that, try to (re)install broken/missing extensions." + Constants.NEW_LINE);
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
            displayErrorMessage("Critical error! :( Bias can not proceed further...");
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
        config.put(Constants.PROPERTY_WINDOW_COORDINATE_X, Constants.EMPTY_STR + getLocation().getX()
                / getToolkit().getScreenSize().getWidth());
        config.put(Constants.PROPERTY_WINDOW_COORDINATE_Y, Constants.EMPTY_STR + getLocation().getY()
                / getToolkit().getScreenSize().getHeight());
        config.put(Constants.PROPERTY_WINDOW_WIDTH, Constants.EMPTY_STR + getSize().getWidth()
                / getToolkit().getScreenSize().getHeight());
        config.put(Constants.PROPERTY_WINDOW_HEIGHT, Constants.EMPTY_STR + getSize().getHeight()
                / getToolkit().getScreenSize().getHeight());
        UUID lsid = getSelectedVisualEntryID();
        if (lsid != null) {
            config.put(Constants.PROPERTY_LAST_SELECTED_ID, lsid.toString());
        }
        BackEnd.getInstance().setConfig(config);
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
                            "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize some data!" + Constants.NEW_LINE +
                            "Data that are failed to serialize will be lost! :(" + Constants.NEW_LINE + 
                            "This the most likely is an extension's bug." + Constants.NEW_LINE +
                            "You can either:" + Constants.NEW_LINE +
                                "* check for new version of extension (the bug may be fixed in new version)" + Constants.NEW_LINE +
                                "* uninstall extension to avoid further instability and data loss" + Constants.NEW_LINE + 
                                "* refer to extension's author for further help", t);
                }
                byte[] serializedSettings = null;
                try {
                    serializedSettings = extension.serializeSettings();
                } catch (Throwable t) {
                    displayErrorMessage(
                            "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize some settings!" + Constants.NEW_LINE +
                            "Settings that are failed to serialize will be lost! :(" + Constants.NEW_LINE + 
                            "This the most likely is an extension's bug." + Constants.NEW_LINE +
                            "You can either:" + Constants.NEW_LINE +
                                "* check for new version of extension (the bug may be fixed in new version)" + Constants.NEW_LINE +
                                "* uninstall extension to avoid further instability and data loss" + Constants.NEW_LINE + 
                                "* refer to extension's author for further help", t);
                }
                DataEntry dataEntry;
                String type = null;
                if (extension instanceof MissingExtensionInformer) {
                    dataEntry = ((MissingExtensionInformer) extension).getDataEntry();
                    type = dataEntry.getType();
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
    
    public Map<VisualEntryDescriptor, Map<String, HighLightMarker>> search(SearchCriteria sc) throws Throwable {
        Map<VisualEntryDescriptor, Map<String, HighLightMarker>> result = new LinkedHashMap<VisualEntryDescriptor, Map<String, HighLightMarker>>();
        Map<UUID, Collection<String>> entries = getSearchEntries(getJTabbedPane());
        Map<UUID, Map<String, HighLightMarker>> matchesFound = SearchEngine.search(sc, entries);
        if (!matchesFound.isEmpty()) {
            Map<UUID, VisualEntryDescriptor> veMap = getVisualEntriesMap();
            for (Entry<UUID, Map<String, HighLightMarker>> matchesFoundEntry : matchesFound.entrySet()) {
                UUID id = matchesFoundEntry.getKey();
                VisualEntryDescriptor ved = veMap.get(id);
                if (ved != null) {
                    result.put(ved, matchesFoundEntry.getValue());
                }
            }
        }
        return result;
    }
    
    private Map<UUID, Collection<String>> getSearchEntries(JTabbedPane tabPane) throws Throwable {
        Map<UUID, Collection<String>> entries = new LinkedHashMap<UUID, Collection<String>>();
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            Component c = tabPane.getComponent(i);
            if (c instanceof Extension) {
                Extension entry = ((Extension) c);
                Collection<String> searchStrings = null;
                String caption = tabPane.getTitleAt(i);
                if (!Validator.isNullOrBlank(caption)) {
                    // add tab caption to entry search data,
                    // so it will be considered while searching
                    searchStrings = new LinkedList<String>();
                    searchStrings.add(caption);
                }
                Collection<String> entrySearchStrings = entry.getSearchData();
                if (entrySearchStrings != null) {
                    if (searchStrings != null) {
                        searchStrings.addAll(entrySearchStrings);
                    } else {
                        searchStrings = entrySearchStrings;
                    }
                }
                if (searchStrings != null) {
                    entries.put(entry.getId(), searchStrings);
                }
            } else if (c instanceof JTabbedPane) {
                String caption = tabPane.getTitleAt(i);
                if (!Validator.isNullOrBlank(caption)) {
                    // add tab caption to entry search data,
                    // so it will be considered while searching
                    Collection<String> searchStrings = new ArrayList<String>();
                    searchStrings.add(caption);
                    entries.put(UUID.fromString(((JTabbedPane) c).getName()), searchStrings);
                }
                entries.putAll(getSearchEntries((JTabbedPane) c));
            }
        }
        return entries;
    }
    
    private void cleanUp() {
        FSUtils.delete(Constants.TMP_DIR);
    }

    public static UUID getSelectedVisualEntryID() {
        return instance.getSelectedVisualEntryID(instance.getJTabbedPane());
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

    public static String getSelectedVisualEntryCaption() {
        return instance.getSelectedVisualEntryCaption(instance.getJTabbedPane());
    }

    private String getSelectedVisualEntryCaption(JTabbedPane tabPane) {
        if (tabPane.getTabCount() > 0) {
            if (tabPane.getSelectedIndex() != -1) {
                Component c = tabPane.getSelectedComponent();
                if (c instanceof JTabbedPane) {
                    return getSelectedVisualEntryCaption((JTabbedPane) c);
                } else if (c instanceof Extension) {
                    return tabPane.getTitleAt(tabPane.getSelectedIndex());
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

    public static Collection<VisualEntryDescriptor> getVisualEntryDescriptors() {
        return instance.getVisualEntryDescriptors(instance.getJTabbedPane(), new LinkedList<Recognizable>());
    }

    private Collection<VisualEntryDescriptor> getVisualEntryDescriptors(JTabbedPane tabPane, LinkedList<Recognizable> entryPath) {
        Collection<VisualEntryDescriptor> vDescriptors = new LinkedList<VisualEntryDescriptor>();
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            Component c = tabPane.getComponent(i);
            String caption = tabPane.getTitleAt(i);
            Icon icon = tabPane.getIconAt(i);
            if (c instanceof JTabbedPane) {
                String id = c.getName();
                Recognizable entry = new Recognizable(UUID.fromString(id), caption, icon);
                entryPath.addLast(entry);
                vDescriptors.add(new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath)));
                vDescriptors.addAll(getVisualEntryDescriptors((JTabbedPane) c, new LinkedList<Recognizable>(entryPath)));
                entryPath.removeLast();
            } else if (c instanceof Extension) {
                Recognizable entry = new Recognizable(((Extension) c).getId(), caption, icon);
                entryPath.addLast(entry);
                vDescriptors.add(new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath)));
                entryPath.removeLast();
            }
        }
        return vDescriptors;
    }

    public static Map<UUID, VisualEntryDescriptor> getVisualEntriesMap() {
        return instance.getVisualEntriesMap(instance.getJTabbedPane(), new LinkedList<Recognizable>());
    }

    private Map<UUID, VisualEntryDescriptor> getVisualEntriesMap(JTabbedPane tabPane, LinkedList<Recognizable> entryPath) {
        Map<UUID, VisualEntryDescriptor> veMap = new LinkedHashMap<UUID, VisualEntryDescriptor>();
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            Component c = tabPane.getComponent(i);
            String caption = tabPane.getTitleAt(i);
            Icon icon = tabPane.getIconAt(i);
            if (c instanceof JTabbedPane) {
                Recognizable entry = new Recognizable(UUID.fromString(c.getName()), caption, icon);
                entryPath.addLast(entry);
                veMap.put(entry.getId(), new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath)));
                veMap.putAll(getVisualEntriesMap((JTabbedPane) c, new LinkedList<Recognizable>(entryPath)));
                entryPath.removeLast();
            } else if (c instanceof Extension) {
                Recognizable entry = new Recognizable(((Extension) c).getId(), caption, icon);
                entryPath.addLast(entry);
                veMap.put(entry.getId(), new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath)));
                entryPath.removeLast();
            }
        }
        return veMap;
    }

    public static boolean switchToVisualEntry(UUID id) {
        return instance.switchToVisualEntry(instance.getJTabbedPane(), id, new LinkedList<Component>());
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

    public static void displayErrorMessage(Throwable t) {
        Launcher.hideSplash();
        JOptionPane.showMessageDialog(instance, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace();
    }

    public static void displayErrorMessage(String message, Throwable t) {
        Launcher.hideSplash();
        JOptionPane.showMessageDialog(instance, message, "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace();
    }

    public static void displayErrorMessage(String message) {
        Launcher.hideSplash();
        JOptionPane.showMessageDialog(instance, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void displayMessage(String message) {
        Launcher.hideSplash();
        JOptionPane.showMessageDialog(instance, message, "Information", JOptionPane.INFORMATION_MESSAGE);
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
                JLabel pLabel = new JLabel("Entry's category placement:");
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
                JLabel cLabel = new JLabel("Caption:");
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
            jContentPane.add(getJPanel(), BorderLayout.NORTH); // Generated
            jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
            jContentPane.add(getJPanel2(), BorderLayout.SOUTH); // Generated
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
            jToolBar.add(getJButton12()); // Generated
            jToolBar.add(getJButton11()); // Generated
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
            jButton.setIcon(controlIcons.getIconRootEntry());
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
            jButton5.setIcon(controlIcons.getIconEntry());
        }
        return jButton5;
    }

    /**
     * This method initializes jButton11
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton11() {
        if (jButton11 == null) {
            jButton11 = new JButton(changePasswordAction);
            jButton11.setToolTipText("change password"); // Generated
            jButton11.setIcon(controlIcons.getIconChangePassword());
        }
        return jButton11;
    }

    /**
     * This method initializes jButton12
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton12() {
        if (jButton12 == null) {
            jButton12 = new JButton(searchAction);
            jButton12.setToolTipText("search"); // Generated
            jButton12.setIcon(controlIcons.getIconSearch());
        }
        return jButton12;
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
            jButton1.setIcon(controlIcons.getIconDelete());
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
            jButton2.setIcon(controlIcons.getIconImport());
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
     * This method initializes jPanel2
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setVisible(false);
            jPanel2.setLayout(new BorderLayout()); // Generated
            jPanel2.add(getJPanel3(), BorderLayout.NORTH); // Generated
            jPanel2.add(new JScrollPane(getJPanel4()), BorderLayout.CENTER); // Generated
        }
        return jPanel2;
    }

    /**
     * This method initializes jPanel3
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            jPanel3 = new JPanel();
            jPanel3.setLayout(new BorderLayout()); // Generated
            jPanel3.add(new JLabel("<html><u>Search results</u></html>"), BorderLayout.CENTER); // Generated
            JButton closeSearchResultsButton = new JButton(new AbstractAction(){
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) {
                    getJPanel2().setVisible(false);
                }
            });
            closeSearchResultsButton.setIcon(ICON_CLOSE);
            closeSearchResultsButton.setPreferredSize(new Dimension(18, 18));
            jPanel3.add(closeSearchResultsButton, BorderLayout.EAST); // Generated
        }
        return jPanel3;
    }

    /**
     * This method initializes jPanel4
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            jPanel4 = new JPanel();
        }
        return jPanel4;
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
            jToolBar2.add(getJButton9()); // Generated
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
            jButton6.setIcon(controlIcons.getIconAbout());
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
            jButton7.setIcon(controlIcons.getIconSave());
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
            jButton8 = new JButton(manageAddOnsAction);
            jButton8.setToolTipText("manage add-ons"); // Generated
            jButton8.setIcon(controlIcons.getIconAddOns());
        }
        return jButton8;
    }

    /**
     * This method initializes jButton9
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton9() {
        if (jButton9 == null) {
            jButton9 = new JButton(preferencesAction);
            jButton9.setToolTipText("preferences"); // Generated
            jButton9.setIcon(controlIcons.getIconPreferences());
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
            jButton10.setIcon(controlIcons.getIconDiscard());
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
            jButton3.setIcon(controlIcons.getIconRootCategory());
        }
        return jButton3;
    }

    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton(addCategoryAction);
            jButton4.setIcon(controlIcons.getIconCategory());
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
        JLabel cLabel = new JLabel("Caption:");
        String categoryCaption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser, icLabel, iconChooser, cLabel },
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
            try {
                Map<String, Class<Extension>> extensions = ExtensionFactory.getInstance().getAnnotatedExtensions();
                if (extensions.isEmpty()) {
                    displayMessage(
                            "You have no any extensions installed currently." + Constants.NEW_LINE +
                            "You can't add entries before you have at least one extension installed.");
                } else {
                    if (getJTabbedPane().getTabCount() == 0) {
                        if (defineRootPlacement()) {
                            addRootEntryAction(extensions);
                        }
                    } else {
                        addRootEntryAction(extensions);
                    }
                }
            } catch (Throwable t) {
                displayErrorMessage("Unable to add entry." + Constants.NEW_LINE +
                        "Some extension(s) may be broken." + Constants.NEW_LINE +
                        "Try to open extensions management dialog, " +
                        "it will autodetect and remove broken extensions." + Constants.NEW_LINE +
                        "After that, try to add entry again.", t);
            }
        }
    };

    private void addRootEntryAction(Map<String, Class<Extension>> extensions) throws Throwable {
        JLabel entryTypeLabel = new JLabel("Type:");
        JComboBox entryTypeComboBox = new JComboBox();
        for (String entryType : extensions.keySet()) {
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
        JLabel cLabel = new JLabel("Caption:");
        String caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { entryTypeLabel, entryTypeComboBox, icLabel, iconChooser, cLabel },
                "New entry:", JOptionPane.QUESTION_MESSAGE);
        if (caption != null) {
            String typeDescription = (String) entryTypeComboBox.getSelectedItem();
            lastAddedEntryType = typeDescription;
            Class<Extension> type = extensions.get(typeDescription);
            byte[] defSettings = BackEnd.getInstance().getExtensionSettings(type.getName());
            if (defSettings == null) {
                // extension's first time usage
                configureExtension(type.getName(), true);
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
    }
    
    private Action changePasswordAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent evt) {
            JLabel currPassLabel = new JLabel("current password:");
            final JPasswordField currPassField = new JPasswordField();
            JLabel newPassLabel = new JLabel("new password:");
            final JPasswordField newPassField = new JPasswordField();
            JLabel newPassConfirmLabel = new JLabel("confirm new password:");
            final JPasswordField newPassConfirmField = new JPasswordField();
            ActionListener al = new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                    currPassField.requestFocusInWindow();
                }
            };
            Timer timer = new Timer(500,al);
            timer.setRepeats(false);
            timer.start();
            if (JOptionPane.showConfirmDialog(
                    null, 
                    new Component[]{
                            currPassLabel, currPassField,
                            newPassLabel, newPassField,
                            newPassConfirmLabel, newPassConfirmField
                            }, 
                    "Change password", 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String currPass = new String(currPassField.getPassword());            
                String newPass = new String(newPassField.getPassword()); 
                String newPassConfirmation = new String(newPassConfirmField.getPassword()); 
                if (!newPass.equals(newPassConfirmation)) {
                    displayErrorMessage("Failed to change password!" + Constants.NEW_LINE + "New password hasn't been correctly confirmed!");
                } else {
                    try {
                        BackEnd.setPassword(currPass, newPass);
                        displayMessage("Password has been successfully changed!");
                    } catch (Exception ex) {
                        displayErrorMessage("Failed to change password!" + Constants.NEW_LINE + ex.getMessage(), ex);
                    }
                }
            }
        }
    };

    private Action searchAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent evt) {
            try {
                JLabel searchExpressionL = new JLabel("search expression:");
                final JTextField searchExpressionTF = new JTextField();
                final Color normal = searchExpressionTF.getForeground();
                final JLabel isCaseSensitiveL = new JLabel("case sensitive:");
                final JCheckBox isCaseSensitiveCB = new JCheckBox();
                JPanel caseSensitivePanel = new JPanel(new BorderLayout());
                caseSensitivePanel.add(isCaseSensitiveL, BorderLayout.CENTER);
                caseSensitivePanel.add(isCaseSensitiveCB, BorderLayout.EAST);
                JLabel isRegularExpressionL = new JLabel("regular expression:");
                final JCheckBox isRegularExpressionCB = new JCheckBox();
                isRegularExpressionCB.addChangeListener(new ChangeListener(){
                    public void stateChanged(ChangeEvent e) {
                        if (isRegularExpressionCB.isSelected()) {
                            isCaseSensitiveL.setEnabled(false);
                            isCaseSensitiveCB.setEnabled(false);
                        } else {
                            isCaseSensitiveL.setEnabled(true);
                            isCaseSensitiveCB.setEnabled(true);
                        }
                    }
                });
                JPanel regularExpressionPanel = new JPanel(new BorderLayout());
                regularExpressionPanel.add(isRegularExpressionL, BorderLayout.CENTER);
                regularExpressionPanel.add(isRegularExpressionCB, BorderLayout.EAST);

                if (lastSearchCriteria != null) {
                    searchExpressionTF.setText(lastSearchCriteria.getSearchExpression());
                    isCaseSensitiveCB.setSelected(lastSearchCriteria.isCaseSensitive());
                    isRegularExpressionCB.setSelected(lastSearchCriteria.isRegularExpression());
                }
                
                searchExpressionTF.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        String searchExpression = searchExpressionTF.getText();
                        if (isRegularExpressionCB.isSelected() && !Validator.isNullOrBlank(searchExpression)) {
                            try {
                                Pattern.compile(searchExpression);
                                searchExpressionTF.setForeground(normal);
                            } catch (PatternSyntaxException pse) {
                                String errorMsg = "Pattern syntax error";
                                if (pse.getIndex() != -1) {
                                    errorMsg +=  " at the index " + pse.getIndex() + " - char '" + searchExpression.charAt(pse.getIndex()-1) + "'";
                                } else {
                                    errorMsg += "!";
                                }
                                searchExpressionTF.setForeground(Color.RED);
                                searchExpressionTF.setToolTipText(errorMsg);
                            }
                        } else {
                            searchExpressionTF.setForeground(normal);
                        }
                    }
                });
                
                int option = JOptionPane.showConfirmDialog(
                        FrontEnd.getInstance(), 
                        new Component[]{
                            searchExpressionL,
                            searchExpressionTF,
                            caseSensitivePanel,
                            regularExpressionPanel
                        }, 
                        "Search", 
                        JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION && !Validator.isNullOrBlank(searchExpressionTF.getText())) {
                    if (lastSearchCriteria == null) {
                        lastSearchCriteria = new SearchCriteria();
                    }
                    lastSearchCriteria.setSearchExpression(searchExpressionTF.getText()); 
                    lastSearchCriteria.setCaseSensitive(isCaseSensitiveCB.isSelected()); 
                    lastSearchCriteria.setRegularExpression(isRegularExpressionCB.isSelected());                    
                    SearchCriteria sc = new SearchCriteria(
                            searchExpressionTF.getText(),
                            isCaseSensitiveCB.isSelected(),
                            isRegularExpressionCB.isSelected());
                    Map<VisualEntryDescriptor, Map<String, HighLightMarker>> result = search(sc);
                    if (result.isEmpty()) {
                        JOptionPane.showMessageDialog(FrontEnd.getInstance(), "No items matching search criteria.");
                    } else {
                        getJPanel2().setPreferredSize(new Dimension(FrontEnd.getInstance().getWidth(), FrontEnd.getInstance().getHeight()/4));
                        JPanel entryPathItemsPanel = getJPanel4();
                        entryPathItemsPanel.setVisible(false);
                        entryPathItemsPanel.removeAll();
                        entryPathItemsPanel.setLayout(new GridLayout(result.size(), 1));
                        for (Entry<VisualEntryDescriptor, Map<String, HighLightMarker>> resultEntry : result.entrySet()) {
                            final VisualEntryDescriptor ved = resultEntry.getKey();
                            Collection<Recognizable> entryPath = ved.getEntryPath();
                            JPanel entryPathItemPanel = new JPanel(new FlowLayout());
                            Iterator<Recognizable> it = entryPath.iterator();
                            while (it.hasNext()) {
                                final Recognizable r = it.next();
                                MouseListener ml = new MouseAdapter(){
                                    @Override
                                    public void mouseClicked(MouseEvent e) {
                                        switchToVisualEntry(r.getId());                                        
                                    }
                                };
                                JPanel entryPathItemIcon = new IconViewPanel(((ImageIcon) r.getIcon()).getImage());
                                entryPathItemIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                entryPathItemIcon.addMouseListener(ml);
                                entryPathItemPanel.add(entryPathItemIcon);
                                JLabel entryPathItemLabel = new JLabel("<html><u><font color=blue>" + r.getCaption() + "</font></u></html>");
                                entryPathItemLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                entryPathItemLabel.addMouseListener(ml);
                                entryPathItemPanel.add(entryPathItemLabel);
                                if (it.hasNext()) {
                                    entryPathItemPanel.add(new JLabel(">"));
                                }
                            }
                            StringBuffer matchedSnippets = new StringBuffer();
                            Iterator<Entry<String, HighLightMarker>> it2 = resultEntry.getValue().entrySet().iterator();
                            while (it2.hasNext()) {
                                Entry<String, HighLightMarker> entry = it2.next();
                                String str = entry.getKey();
                                String hlStr = null;
                                HighLightMarker hlMarker = entry.getValue();
                                if (hlMarker != null) {
                                    Integer beginIndex = entry.getValue().getBeginIndex();
                                    Integer endIndex = entry.getValue().getEndIndex();
                                    if (beginIndex != null && endIndex != null) {
                                        hlStr = str.substring(0, beginIndex);
                                        hlStr += "<font bgcolor=yellow>";
                                        hlStr += str.substring(beginIndex, endIndex);
                                        hlStr += "</font>";
                                        hlStr += str.substring(endIndex, str.length());
                                    }
                                }
                                if (hlStr != null) {
                                    matchedSnippets.append(hlStr);
                                } else {
                                    matchedSnippets.append(str);
                                }
                                if (it2.hasNext()) {
                                    matchedSnippets.append(" :: ");
                                }
                            }
                            entryPathItemPanel.add(new JLabel("<html> &gt;&gt; <i>" + matchedSnippets.toString() + "</i></html>"));
                            JPanel p = new JPanel(new BorderLayout());
                            p.add(entryPathItemPanel, BorderLayout.WEST);
                            entryPathItemsPanel.add(p);
                        }
                        entryPathItemsPanel.setVisible(true);
                        getJPanel2().setVisible(true);
                    }
                }
            } catch (Throwable t) {
                displayErrorMessage("Error while processing search!", t);
            }
        }
    };
    
    private Action addEntryAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent evt) {
            try {
                Map<String, Class<Extension>> extensions = ExtensionFactory.getInstance().getAnnotatedExtensions();
                if (extensions.isEmpty()) {
                    displayMessage(
                            "You have no any extensions installed currently." + Constants.NEW_LINE +
                            "You can't add entries before you have at least one extension installed.");
                } else {
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
                    for (String entryType : extensions.keySet()) {
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
                    JLabel cLabel = new JLabel("Caption:");
                    String caption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { entryTypeLabel, entryTypeComboBox, icLabel, iconChooser, cLabel },
                            "New entry:", JOptionPane.QUESTION_MESSAGE);
                    if (caption != null) {
                        String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                        lastAddedEntryType = typeDescription;
                        Class<Extension> type = extensions.get(typeDescription);
                        byte[] defSettings = BackEnd.getInstance().getExtensionSettings(type.getName());
                        if (defSettings == null) {
                            // extension's first time usage
                            configureExtension(type.getName(), true);
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
                }
            } catch (Throwable t) {
                displayErrorMessage("Unable to add entry." + Constants.NEW_LINE +
                                    "Some extension(s) may be broken." + Constants.NEW_LINE +
                                    "Try to open extensions management dialog, " +
                                    "it will autodetect and remove broken extensions." + Constants.NEW_LINE +
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
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                    @Override
                    public String getDescription() {
                        return "Bias working directory";
                    }
                });
                File importDir = null;
                int rVal = jfc.showOpenDialog(FrontEnd.this);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    importDir = jfc.getSelectedFile();

                    JLabel label = new JLabel("password:");
                    final JPasswordField passField = new JPasswordField();
                    ActionListener al = new ActionListener(){
                        public void actionPerformed(ActionEvent ae){
                            passField.requestFocusInWindow();
                        }
                    };
                    Timer timer = new Timer(500,al);
                    timer.setRepeats(false);
                    timer.start();
                    if (JOptionPane.showConfirmDialog(
                            null, 
                            new Component[]{label, passField}, 
                            "Import authentification", 
                            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        String password = new String(passField.getPassword());            
                        if (password != null) {
                            try {
                                DataCategory data = BackEnd.getInstance().importData(importDir, getVisualEntriesIDs(), password);
                                if (!data.getData().isEmpty()) {
                                    representData(data);
                                    displayMessage("Data have been successfully imported");
                                } else {
                                    displayErrorMessage("Nothing to import!");
                                }
                            } catch (Exception ex) {
                                displayErrorMessage("Failed to import data!", ex);
                            }
                        }
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
                JLabel cLabel = new JLabel("Caption:");
                String categoryCaption = JOptionPane.showInputDialog(FrontEnd.this, new Component[] { pLabel, placementsChooser, icLabel, iconChooser, cLabel },
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
                store();
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    private Action discardUnsavedChangesAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent evt) {
            // show confirmation dialog
            if (JOptionPane.showConfirmDialog(FrontEnd.this, 
                    "All unsaved changes will be lost." + Constants.NEW_LINE +
                    "Are you sure you want to discard unsaved chages?",
                    "Discard unsaved changes confirmation",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // store nothing, just exit
                cleanUp();
                System.exit(0);
            }
        }
    };
    
    private Action preferencesAction = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
                byte[] before = Preferences.getInstance().serialize();
                JPanel prefsPanel = null;
                Collection<Component> prefComponents = new LinkedList<Component>();
                Field[] fields = Preferences.class.getDeclaredFields();
                for (final Field field : fields) {
                    PreferenceAnnotation prefAnn = field.getAnnotation(PreferenceAnnotation.class);
                    if (prefAnn != null) {
                        JLabel prefTitle = new JLabel(prefAnn.title());
                        prefTitle.setToolTipText(prefAnn.description());
                        JPanel prefPanel = new JPanel(new BorderLayout());
                        prefPanel.add(prefTitle, BorderLayout.WEST);
                        Component prefControl = null;
                        String type = field.getType().getSimpleName().toLowerCase();
                        if ("boolean".equals(type)) {
                            prefControl = new JCheckBox();
                            try {
                                ((JCheckBox) prefControl).setSelected(field.getBoolean(Preferences.getInstance()));
                                ((JCheckBox) prefControl).addActionListener(new ActionListener(){
                                    public void actionPerformed(ActionEvent e) {
                                        try {
                                            field.setBoolean(Preferences.getInstance(), ((JCheckBox) e.getSource()).isSelected());
                                        } catch (Exception ex) {
                                            displayErrorMessage(
                                                    "Failed to change preference!", ex);
                                        }
                                    }
                                });
                            } catch (Exception ex) {
                                prefControl = null;
                                displayErrorMessage(
                                        "Failed to get preference!", ex);
                            }
                        }
                        if (prefControl != null) {
                            prefPanel.add(prefControl, BorderLayout.CENTER);
                            prefComponents.add(prefPanel);
                        }
                    }
                }
                prefsPanel = new JPanel(new GridLayout(prefComponents.size(), 1));
                for (Component prefComponent : prefComponents) {
                    prefsPanel.add(prefComponent);
                }
                JOptionPane.showConfirmDialog(FrontEnd.this, prefsPanel, "Preferences", JOptionPane.OK_CANCEL_OPTION);
                byte[] after = Preferences.getInstance().serialize();
                if (!Arrays.equals(after, before)) {
                    displayMessage(RESTART_MESSAGE);
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
        
    };
    
    private Action manageAddOnsAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        private Collection<ImageIcon> icons;
        private JList icList;
        private DefaultListModel icModel;
        
        private boolean modified;
        
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {

            try {
                // extensions
                JLabel extLabel = new JLabel("Extensions Management");
                final DefaultTableModel extModel = new DefaultTableModel() {
                    private static final long serialVersionUID = 1L;
                    public boolean isCellEditable(int rowIndex, int mColIndex) {
                        return false;
                    }
                };
                final JTable extList = new JTable(extModel);
                final TableRowSorter<TableModel> extSorter = new TableRowSorter<TableModel>(extModel);
                extList.setRowSorter(extSorter);
                extModel.addColumn("Name");
                extModel.addColumn("Version");
                extModel.addColumn("Author");
                extModel.addColumn("Description");
                boolean brokenFixed = false;
                for (String extension : BackEnd.getInstance().getExtensions()) {
                    try {
                        Class<Extension> extClass = (Class<Extension>) Class.forName(extension);
                        // extension instantiation test
                        ExtensionFactory.getInstance().newExtension(extClass);
                        // extension is ok, add it to the list
                        AddOnAnnotation extAnn = 
                            (AddOnAnnotation) extClass.getAnnotation(AddOnAnnotation.class);
                        if (extAnn != null) {
                            extModel.addRow(new Object[]{
                                    extClass.getSimpleName(),
                                    extAnn.version(),
                                    extAnn.author(),
                                    extAnn.description()});
                        } else {
                            extModel.addRow(new Object[]{
                                    extClass.getSimpleName(),
                                    ADDON_ANN_FIELD_VALUE_NA,
                                    ADDON_ANN_FIELD_VALUE_NA,
                                    ADDON_ANN_FIELD_VALUE_NA});
                        }
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
                            displayErrorMessage("Error occured while uninstalling broken extension [ " + extensionName + " ]!" + Constants.NEW_LINE + ex2);
                        }
                    }
                }
                JButton extConfigButt = new JButton("Configure selected");
                extConfigButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extList.getSelectedRowCount() == 1) {
                            try {
                                String version = (String) extList.getValueAt(extList.getSelectedRow(), 1);
                                if (Validator.isNullOrBlank(version)) {
                                    displayMessage(
                                            "This Extension can not be configured yet." + Constants.NEW_LINE +
                                            "Restart Bias first.");
                                } else {
                                    String extension = (String) extList.getValueAt(extList.getSelectedRow(), 0);
                                    String extFullClassName = 
                                        Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                + extension + Constants.PACKAGE_PATH_SEPARATOR + extension;
                                    configureExtension(extFullClassName, false);
                                }
                            } catch (Exception ex) {
                                displayErrorMessage(ex);
                            }
                        } else {
                            displayMessage("Please, choose only one extension from the list");
                        }
                    }
                });
                JButton extInstButt = new JButton("Install more");
                extInstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extensionFileChooser.showOpenDialog(FrontEnd.this) == JFileChooser.APPROVE_OPTION) {
                            try {
                                for (File file : extensionFileChooser.getSelectedFiles()) {
                                    String installedExt = BackEnd.getInstance().installExtension(file);
                                    installedExt = installedExt.replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                                    extModel.addRow(new Object[]{installedExt,null,null,file.getName()});
                                    modified = true;
                                }
                            } catch (Exception ex) {
                                displayErrorMessage(ex);
                            }
                        }
                    }
                });
                JButton extUninstButt = new JButton("Uninstall selected");
                extUninstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (extList.getSelectedRowCount() != 0) {
                                int idx;
                                while ((idx = extList.getSelectedRow()) != -1) {
                                    String extension = (String) extList.getValueAt(extList.getSelectedRow(), 0);
                                    String extFullClassName = 
                                        Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                + extension + Constants.PACKAGE_PATH_SEPARATOR + extension;
                                    BackEnd.getInstance().uninstallExtension(extFullClassName);
                                    extModel.removeRow(idx);
                                    modified = true;
                                }
                                displayMessage("Extension(s) have been successfully uninstalled!");
                            }
                        } catch (Exception ex) {
                            displayErrorMessage(ex);
                        }
                    }
                });

                // look-&-feels
                JLabel lafLabel = new JLabel("Look-&-Feels Management");
                final DefaultTableModel lafModel = new DefaultTableModel() {
                    private static final long serialVersionUID = 1L;
                    public boolean isCellEditable(int rowIndex, int mColIndex) {
                        return false;
                    }
                };
                final JTable lafList = new JTable(lafModel) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                        Component c = super.prepareRenderer(renderer, row, column);
                        String currLAFName = config.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
                        if (currLAFName == null) {
                            currLAFName = DEFAULT_LOOK_AND_FEEL;
                        }
                        String name = (String) getModel().getValueAt(row, 0);
                        if (name.equals(activeLAF)) {
                            c.setForeground(Color.BLUE);
                            Font f = super.getFont();
                            f = new Font(f.getName(), Font.BOLD, f.getSize());
                            c.setFont(f);
                        } else if (!activeLAF.equals(currLAFName) && name.equals(currLAFName)) {
                            c.setForeground(Color.BLUE);
                        } else {
                            c.setForeground(super.getForeground());
                        }
                        return c;
                    }
                };
                final TableRowSorter<TableModel> lafSorter = new TableRowSorter<TableModel>(lafModel);
                lafList.setRowSorter(lafSorter);
                lafModel.addColumn("Name");
                lafModel.addColumn("Version");
                lafModel.addColumn("Author");
                lafModel.addColumn("Description");
                lafModel.addRow(new Object[]{DEFAULT_LOOK_AND_FEEL,Constants.EMPTY_STR,Constants.EMPTY_STR,"Default Look-&-Feel"});
                for (String laf : BackEnd.getInstance().getLAFs()) {
                    try {
                        Class<?> lafClass = Class.forName(laf);
                        // laf instantiation test
                        lafClass.newInstance();
                        // laf is ok, add it to the list
                        AddOnAnnotation lafAnn = 
                            (AddOnAnnotation) lafClass.getAnnotation(AddOnAnnotation.class);
                        if (lafAnn != null) {
                            lafModel.addRow(new Object[]{
                                    lafClass.getSimpleName(),
                                    lafAnn.version(),
                                    lafAnn.author(),
                                    lafAnn.description()});
                        } else {
                            lafModel.addRow(new Object[]{
                                    lafClass.getSimpleName(),
                                    ADDON_ANN_FIELD_VALUE_NA,
                                    ADDON_ANN_FIELD_VALUE_NA,
                                    ADDON_ANN_FIELD_VALUE_NA});
                        }
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
                            displayErrorMessage("Error occured while uninstalling broken Look-&-Feel [ " + lafName + " ]!" + Constants.NEW_LINE + ex2);
                        }
                    }
                }
                JButton lafActivateButt = new JButton("(Re)Activate Look-&-Feel");
                lafActivateButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (lafList.getSelectedRowCount() == 1) {
                            String laf = (String) lafList.getValueAt(lafList.getSelectedRow(), 0);
                            if (DEFAULT_LOOK_AND_FEEL.equals(laf)) {
                                try {
                                    modified = setActiveLAF(null);
                                    lafList.repaint();
                                } catch (Exception t) {
                                    displayErrorMessage(t);
                                }
                            } else {
                                String version = (String) lafList.getValueAt(lafList.getSelectedRow(), 1);
                                if (Validator.isNullOrBlank(version)) {
                                    displayMessage(
                                            "This Look-&-Feel can not be activated yet." + Constants.NEW_LINE +
                                            "Restart Bias first.");
                                } else {
                                    try {
                                        String fullLAFClassName = 
                                            Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                    + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                                        modified = setActiveLAF(fullLAFClassName);
                                        lafList.repaint();
                                    } catch (Exception t) {
                                        displayErrorMessage(t);
                                    }
                                }
                            }
                        } else {
                            displayMessage("Please, choose only one look-&-feel from the list");
                        }    
                    }
                });
                JButton lafInstButt = new JButton("Install more");
                lafInstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (lafFileChooser.showOpenDialog(FrontEnd.this) == JFileChooser.APPROVE_OPTION) {
                            try {
                                for (File file : lafFileChooser.getSelectedFiles()) {
                                    String installedLAF = BackEnd.getInstance().installLAF(file);
                                    installedLAF = installedLAF.replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                                    lafModel.addRow(new Object[]{installedLAF,null,null,file.getName()});
                                    modified = true;
                                }
                            } catch (Exception ex) {
                                displayErrorMessage(ex);
                            }
                        }
                    }
                });
                JButton lafUninstButt = new JButton("Uninstall selected");
                lafUninstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (lafList.getSelectedRowCount() > 0) {
                                boolean uninstalled = false;
                                String currentLAF = config.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
                                int idx;
                                while ((idx = lafList.getSelectedRow()) != -1) {
                                    String laf = (String) lafList.getValueAt(lafList.getSelectedRow(), 0);
                                    if (!DEFAULT_LOOK_AND_FEEL.equals(laf)) {
                                        String fullLAFClassName = 
                                            Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                    + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                                        BackEnd.getInstance().uninstallLAF(fullLAFClassName);
                                        lafModel.removeRow(idx);
                                        // if look-&-feel that has been uninstalled was active one...
                                        if (laf.equals(currentLAF)) {
                                            //... unset it (default one will be used)
                                            config.remove(Constants.PROPERTY_LOOK_AND_FEEL);
                                        }
                                        uninstalled = true;
                                        modified = true;
                                    } else {
                                        displayErrorMessage("Default Look-&-Feel can not be uninstalled!");
                                        if (lafList.getSelectedRowCount() == 1) {
                                            break;
                                        }
                                    }
                                }
                                if (uninstalled) {
                                    displayMessage("Look-&-Feel(s) have been successfully uninstalled!");
                                }
                            }
                        } catch (Exception ex) {
                            displayErrorMessage(ex);
                        }
                    }
                });
                
                // icons
                JLabel icLabel = new JLabel("Icons Management");
                icModel = new DefaultListModel();
                icList = new JList(icModel);
                icons = new LinkedList<ImageIcon>();
                for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                    icModel.addElement(icon);
                    icons.add(icon);
                }
                JScrollPane jsp = new JScrollPane(icList);
                jsp.setPreferredSize(new Dimension(200,200));
                jsp.setMinimumSize(new Dimension(200,200));
                JButton addIconButt = new JButton("Add more");
                addIconButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (iconsFileChooser.showOpenDialog(FrontEnd.this) == JFileChooser.APPROVE_OPTION) {
                            try {
                                boolean added = false;
                                for (File file : iconsFileChooser.getSelectedFiles()) {
                                    Collection<ImageIcon> icons = BackEnd.getInstance().addIcons(file);
                                    if (!icons.isEmpty()) {
                                        for (ImageIcon icon : icons) {
                                            icModel.addElement(icon);
                                        }
                                        added = true;
                                    }
                                }
                                if (added) {
                                    displayMessage("Icon(s) have been successfully installed!");
                                } else {
                                    displayErrorMessage("Nothing to install!");
                                }
                            } catch (Exception ex) {
                                displayErrorMessage(ex);
                            }
                        }
                    }
                });
                JButton removeIconButt = new JButton("Remove selected");
                removeIconButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (icList.getSelectedValues().length > 0) {
                                for (Object icon : icList.getSelectedValues()) {
                                    BackEnd.getInstance().removeIcon((ImageIcon)icon);
                                    icModel.removeElement(icon);
                                }
                                displayMessage("Icon(s) have been successfully removed!");
                            }
                        } catch (Exception ex) {
                            displayErrorMessage(ex);
                        }
                    }
                });
                
                // dialog
                JTabbedPane addOnsPane = new JTabbedPane();

                JPanel extControlsPanel = new JPanel(new GridLayout(1,3));
                extControlsPanel.add(extConfigButt);
                extControlsPanel.add(extInstButt);
                extControlsPanel.add(extUninstButt);
                JPanel extTopPanel = new JPanel(new BorderLayout());
                extTopPanel.add(extLabel, BorderLayout.NORTH);
                extTopPanel.add(new JLabel("Filter:"), BorderLayout.CENTER);
                final JTextField extFilterText = new JTextField();
                extFilterText.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        extSorter.setRowFilter(RowFilter.regexFilter(extFilterText.getText()));
                    }
                });
                extTopPanel.add(extFilterText, BorderLayout.SOUTH);
                JPanel extPanel = new JPanel(new BorderLayout());
                extPanel.add(extTopPanel, BorderLayout.NORTH);
                extPanel.add(new JScrollPane(extList), BorderLayout.CENTER);
                extPanel.add(extControlsPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Extensions", controlIcons.getIconExtensions(), extPanel);
                
                JPanel lafControlsPanel = new JPanel(new GridLayout(1,4));
                lafControlsPanel.add(lafActivateButt);
                lafControlsPanel.add(lafInstButt);
                lafControlsPanel.add(lafUninstButt);
                JPanel lafTopPanel = new JPanel(new BorderLayout());
                lafTopPanel.add(lafLabel, BorderLayout.NORTH);
                lafTopPanel.add(new JLabel("Filter:"), BorderLayout.CENTER);
                final JTextField lafFilterText = new JTextField();
                lafFilterText.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        lafSorter.setRowFilter(RowFilter.regexFilter(lafFilterText.getText()));
                    }
                });
                lafTopPanel.add(lafFilterText, BorderLayout.SOUTH);
                JPanel lafPanel = new JPanel(new BorderLayout());
                lafPanel.add(lafTopPanel, BorderLayout.NORTH);
                lafPanel.add(new JScrollPane(lafList), BorderLayout.CENTER);
                lafPanel.add(lafControlsPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Look-&-Feels", controlIcons.getIconLAFs(), lafPanel);
                
                JPanel icControlsPanel = new JPanel(new GridLayout(1,2));
                icControlsPanel.add(addIconButt);
                icControlsPanel.add(removeIconButt);
                JPanel icPanel = new JPanel(new BorderLayout());
                icPanel.add(icLabel, BorderLayout.NORTH);
                icPanel.add(new JScrollPane(jsp), BorderLayout.CENTER);
                icPanel.add(icControlsPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Icons", controlIcons.getIconIcons(), icPanel);
                
                modified = false;
                JOptionPane.showMessageDialog(
                    FrontEnd.this, 
                    addOnsPane,
                    "Manage Add-Ons",
                    JOptionPane.INFORMATION_MESSAGE
                );

                if (modified || brokenFixed) {
                    displayMessage(RESTART_MESSAGE);
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
        							"<html>Bias Personal Information Manager, version 1.0.0<br>" +
        							"(c) R. Kasianenko, 2007<br>"
        						);
        	JLabel linkLabel = new JLabel(
        							"<html><u><font color=blue>http://bias.sourceforge.net</font></u>"
        						);
        	linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	linkLabel.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						AppManager.getInstance().handleAddress("http://bias.sourceforge.net");
					} catch (Exception ex) {
						// do nothing
					}
				}
        	});
            JOptionPane.showMessageDialog(FrontEnd.this, new Component[]{aboutLabel, linkLabel} );
        }
    };

} // @jve:decl-index=0:visual-constraint="10,10"
