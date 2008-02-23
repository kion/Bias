/**
 * Created on Oct 15, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import bias.Constants;
import bias.Preferences;
import bias.Splash;
import bias.Preferences.PreferenceValidator;
import bias.annotation.AddOnAnnotation;
import bias.annotation.PreferenceAnnotation;
import bias.annotation.PreferenceEnableAnnotation;
import bias.annotation.PreferenceProtectAnnotation;
import bias.annotation.PreferenceValidationAnnotation;
import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.Recognizable;
import bias.extension.EntryExtension;
import bias.extension.Extension;
import bias.extension.ExtensionFactory;
import bias.extension.MissingExtensionInformer;
import bias.extension.ToolExtension;
import bias.extension.ToolRepresentation;
import bias.gui.VisualEntryDescriptor.ENTRY_TYPE;
import bias.laf.ControlIcons;
import bias.laf.LookAndFeel;
import bias.transfer.Transferrer;
import bias.transfer.Transferrer.TRANSFER_TYPE;
import bias.utils.AppManager;
import bias.utils.ArchUtils;
import bias.utils.FSUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;


/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    // TODO [P3] internationalization

    private static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_LOOK_AND_FEEL = "DefaultLAF";

    /**
     * Application icon
     */
    private static final ImageIcon ICON_APP = new ImageIcon(FrontEnd.class.getResource("/bias/res/app_icon.png"));

    private static final ImageIcon ICON_CLOSE = new ImageIcon(FrontEnd.class.getResource("/bias/res/close.png"));

    private static final String RESTART_MESSAGE = "Changes will take effect after Bias restart";
    
    private static enum DATA_OPERATION_TYPE {
        IMPORT,
        EXPORT
    }

    private static final Placement[] PLACEMENTS = new Placement[] { 
            new Placement(JTabbedPane.TOP),
            new Placement(JTabbedPane.LEFT), 
            new Placement(JTabbedPane.RIGHT),
            new Placement(JTabbedPane.BOTTOM)
        };

    private static final String STATUS_MESSAGE_PREFIX = "<html>&nbsp;";
    
    private static final String STATUS_MESSAGE_HTML_COLOR_NORMAL = "<font color=\"#000000\">";
    
    private static final String STATUS_MESSAGE_HTML_COLOR_HIGHLIGHTED = "<font color=\"#00A000\">";
    
    private static final String STATUS_MESSAGE_SUFFIX = "</font></html>";

    private static final String ADDON_ANN_FIELD_VALUE_NA = "N/A";
    
    private static final JLabel recursiveExportInfoLabel = new JLabel(
            "<html><b><i><font color=\"blue\">" +
            "Note: there're two export modes available:<br/>" +
            "<ul>" + 
            "<li>" +
            "static - only selected nodes will be exported" +
            "</li>" + 
            "<li>" +
            "dynamic - in addition to nodes selected for static export, collapsed nodes can be selected<br/>" +
            "to enable recursive export; in this case all nested nodes will be exported as well<br/>" +
            "(this is especially handy when stored configurations are used)" + 
            "</li>" + 
            "</ul>" + 
            "</font></i></b></html>");
    
    private static AddOnFilesChooser extensionFileChooser = new AddOnFilesChooser();

    private static AddOnFilesChooser lafFileChooser = new AddOnFilesChooser();

    private static IconsFileChooser iconsFileChooser = new IconsFileChooser();
    
    private static FrontEnd instance;
    
    private static Map<Class<? extends ToolExtension>, ToolExtension> tools;

    private static Map<Class<? extends ToolExtension>, JPanel> indicatorAreas = new HashMap<Class<? extends ToolExtension>, JPanel>();

    // use default control icons initially
    private static ControlIcons controlIcons = new ControlIcons();

    private static Properties config;
    
    private static String activeLAF = null;
    
    private static Map<String, byte[]> initialLAFSettings = new HashMap<String, byte[]>();
    
    private static Map<String, DataEntry> dataEntries = new HashMap<String, DataEntry>();

    private static Map<UUID, EntryExtension> entryExtensions = new LinkedHashMap<UUID, EntryExtension>();
    
    private static Stack<UUID> navigationHistory = new Stack<UUID>();
    
    private static int navigationHistoryIndex = -1;

    private static boolean navigating = false;

    private static Map<DefaultMutableTreeNode, Recognizable> nodeEntries;

    private static Map<DefaultMutableTreeNode, Collection<DefaultMutableTreeNode>> categoriesToExportRecursively;

    private static boolean sysTrayIconVisible = false;
    
    private static TrayIcon trayIcon = null;
    
    private SimpleDateFormat dateFormat;
    
    private TabMoveListener tabMoveListener = new TabMoveListener();
    
    private File exportFile;

    private int opt;
    
    private boolean hotKeysBindingsChanged = true;

    private String lastAddedEntryType = null;
    
    private JProgressBar memUsageProgressBar = null;
    
    private JList statusBarMessagesList = null;

    private Dialog dialog = null;
    
    private JScrollPane detailsPane = null;

    private JTextPane detailsTextPane = null;
    
    private JTabbedPane currentTabPane = null;
    
    private JPanel jContentPane = null;

    private JTabbedPane jTabbedPane = null;

    private JToolBar jToolBar = null;

    private JToolBar jToolBar3 = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    private JButton jButton2 = null;

    private JButton jButton12 = null;

    private JButton jButton4 = null;

    private JButton jButton5 = null;

    private JButton jButton11 = null;

    private JButton jButton13 = null;

    private JButton jButton14 = null;

    private JButton jButton15 = null;

    private JButton jButton16 = null;

    private JPanel jPanel = null;

    private JPanel jPanel4 = null;

    private JPanel jPanelStatusBar = null;

    private JPanel jPanelIndicators = null;

    private JLabel jLabelStatusBarMsg = null;

    private JPanel jPanel5 = null;

    private JPanel jPanel2 = null;

    private JPanel jPanel3 = null;

    private JSplitPane jSplitPane = null;

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
        getInstance().displayStatusBarMessage("loaded & ready");
        if (Preferences.getInstance().startHidden) {
            showSysTrayIcon();
        } else {
            getInstance().setVisible(true);
        }
    }
    
    private static FrontEnd getInstance() {
        if (instance == null) {
            preInit();
            activateLAF();
            instance = new FrontEnd();
            instance.applyPreferences();
            representTools();
        }
        return instance;
    }

    private void applyPreferences() {
        if (Preferences.getInstance().useSysTrayIcon) {
            showSysTrayIcon();
        } else {
            hideSysTrayIcon();
        }
        dateFormat = new SimpleDateFormat(Preferences.getInstance().preferredDateFormat);
        if (instance.hotKeysBindingsChanged) {
            instance.bindHotKeys();
            instance.hotKeysBindingsChanged = false;
        }
        if (memUsageIndicatorPanel == null) {
            memUsageIndicatorPanel = instance.createStatusBarIndicatorArea(null);
            instance.getJPanelIndicators().add(memUsageIndicatorPanel, BorderLayout.EAST);
        }
        if (Preferences.getInstance().showMemoryUsage) {
            memUsageProgressBar = new JProgressBar();
            memUsageProgressBar.setMinimum(0);
            memUsageProgressBar.setStringPainted(true);
            memUsageIndicatorPanel.add(memUsageProgressBar, BorderLayout.CENTER);
            memUsageIndicatorPanel.setVisible(true);
            startMemoryUsageMonitoring();
        } else {
            if (memUsageIndicatorPanel != null) {
                memUsageIndicatorPanel.setVisible(false);
            }
        }
        instance.displayStatusBarMessage("preferences applied");
    }
    
    private static JPanel memUsageIndicatorPanel = null;
    
    // TODO [P2] memory usage optimization: show memory usage info only when main window is visible
    private void startMemoryUsageMonitoring() {
        new Thread(new Runnable() {
            public void run() {
                while (Preferences.getInstance().showMemoryUsage) {
                    MemoryMXBean mmxb = ManagementFactory.getMemoryMXBean();
                    long bytes = mmxb.getHeapMemoryUsage().getUsed() + mmxb.getNonHeapMemoryUsage().getUsed();
                    long bytes2 = mmxb.getHeapMemoryUsage().getCommitted() + mmxb.getNonHeapMemoryUsage().getCommitted();
                    memUsageProgressBar.setMaximum((int) bytes2/1024);
                    memUsageProgressBar.setValue((int) bytes/1024);
                    memUsageProgressBar.setString(" Memory Usage: " + bytes/1024/1024 + " of " + bytes2/1024/1024 + " Mb" + Constants.BLANK_STR);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }).start();
    }
    
    // TODO [P2] GUI should contain information about hot-key-bindings (for example, in tooltip-text for button with appropriate action set)
    // TODO [P3] hot-keys-bindings should be customizable
    private void bindHotKeys() {
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), saveAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(saveAction.getValue(Action.NAME), saveAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK), importAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(importAction.getValue(Action.NAME), importAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK), exportAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(exportAction.getValue(Action.NAME), exportAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), preferencesAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(preferencesAction.getValue(Action.NAME), preferencesAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK), manageAddOnsAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(manageAddOnsAction.getValue(Action.NAME), manageAddOnsAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK), exitAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(exitAction.getValue(Action.NAME), exitAction);
        
    }
    
    private static void showSysTrayIcon() {
        if (!SystemTray.isSupported()) {
            displayErrorMessage("System tray API is not available on this platform!");
        } else if (!sysTrayIconVisible) {
            try {
                // initialize tray icon
                if (trayIcon == null) {
                    trayIcon = new TrayIcon(
                            ICON_APP.getImage(), 
                            "Bias :: Personal Information Manager");
                    trayIcon.setImageAutoSize(true);
                    trayIcon.addMouseListener(new MouseAdapter(){
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            instance.setVisible(!instance.isVisible());
                            if (!Preferences.getInstance().useSysTrayIcon) {
                                hideSysTrayIcon();
                            }
                            if (instance.isVisible()) {
                                instance.setExtendedState(JFrame.NORMAL);
                                // TODO [P3] window does not get focus always here... maybe there's another, more reliable way to force focused window state
                                instance.requestFocusInWindow();
                            }
                        }
                    });
                }
                // add icon to system tray
                if (SystemTray.getSystemTray().getTrayIcons().length == 0) {
                    SystemTray.getSystemTray().add(trayIcon);
                }
                sysTrayIconVisible = true;
            } catch (Exception ex) {
                displayErrorMessage("Failed to initialize system tray!", ex);
            }
        }
    }
    
    private static void hideSysTrayIcon() {
        if (sysTrayIconVisible == true && trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            sysTrayIconVisible = false;
        }
    }
    
    private static void preInit() {
        try {
            BackEnd.getInstance().load();
            initGlobalSettings();
        } catch (GeneralSecurityException gse) {
            displayErrorMessage(
                    "Bias has failed to load data!" + Constants.NEW_LINE +
                    "It seems that you have typed wrong password...", gse);
            BackEnd.getInstance().shutdown(-1);
        } catch (Throwable t) {
            displayErrorMessage(
                    "Bias has failed to load data!" + Constants.NEW_LINE +
                    "Terminating...", t);
            BackEnd.getInstance().shutdown(-1);
        }
    }
    
    private static void initGlobalSettings() {
        config = new Properties();
        config.putAll(BackEnd.getInstance().getConfig());
    }
    
    private void applyGlobalSettings() {
        String lsid = config.getProperty(Constants.PROPERTY_LAST_SELECTED_ID);
        if (lsid != null) {
            this.switchToVisualEntry(getJTabbedPane(), UUID.fromString(lsid), new LinkedList<Component>());
        }
        
        // TODO [P3] would be nice to have window state (maximized: both/vert/horiz) restored on load
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
            wpxValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
        }
        String wpy = config.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
        if (wpy == null) {
            wpyValue = getToolkit().getScreenSize().height / 4;
        } else {
            wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
        }
        String ww = config.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
        if (ww == null) {
            wwValue = (getToolkit().getScreenSize().width / 4) * 2;
        } else {
            wwValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
        }
        String wh = config.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
        if (wh == null) {
            whValue = (getToolkit().getScreenSize().height / 4) * 2;
        } else {
            whValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
        }

        this.setLocation(wpxValue, wpyValue);
        this.setSize(wwValue, whValue);
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
                t.printStackTrace(System.err);
                try {
                    String lafFullClassName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                    BackEnd.getInstance().uninstallLAF(lafFullClassName);
                    config.remove(Constants.PROPERTY_LOOK_AND_FEEL);
                    System.err.println(
                            "Broken Look-&-Feel '" + laf + "' has been uninstalled ;)" + Constants.NEW_LINE +
                            RESTART_MESSAGE);
                } catch (Throwable t2) {
                    System.err.println("Broken Look-&-Feel '" + laf + "' failed to uninstall :(" + Constants.NEW_LINE 
                            + "Error details: " + Constants.NEW_LINE);
                    t2.printStackTrace(System.err);
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
        this.setSize(new Dimension(772, 535));
        try {
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            this.setTitle("Bias");
            this.setIconImage(ICON_APP.getImage());
            this.setContentPane(getJContentPane());

            representData(BackEnd.getInstance().getData());
            
            applyGlobalSettings();

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        if (Preferences.getInstance().remainInSysTrayOnWindowClose) {
                            showSysTrayIcon();
                            setVisible(false);
                        } else {
                            exit();
                        }
                    } catch (Throwable t) {
                        displayErrorMessage(t);
                    }
                }
                @Override
                public void windowIconified(WindowEvent e) {
                    if (Preferences.getInstance().minimizeToSysTray) {
                        showSysTrayIcon();
                        setVisible(false);
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
                Class<? extends Extension> extensionClass = (Class<? extends Extension>) Class.forName(extension);
                Extension extensionInstance = tools.get(extensionClass);
                if (extensionInstance == null) {
                    extensionInstance = ExtensionFactory.getInstance().newExtension(extensionClass);
                }
                byte[] extSettings = BackEnd.getInstance().getExtensionSettings(extension);
                if (extSettings == null) {
                    extSettings = new byte[]{};
                }
                byte[] settings = extensionInstance.configure(extSettings);
                if (settings == null) {
                    settings = new byte[]{};
                }
                if (!Arrays.equals(extSettings, settings)) {
                    BackEnd.getInstance().storeExtensionSettings(extension, settings);
                    if (extensionInstance instanceof ToolExtension) {
                        getJPanelIndicators().setVisible(false);
                        JPanel panel = indicatorAreas.get(extensionClass);
                        JComponent indicator = ((ToolExtension) extensionInstance).getRepresentation().getIndicator();
                        if (indicator != null) {
                            if (panel == null) {
                                panel = createStatusBarIndicatorArea((Class<? extends ToolExtension>) extensionInstance.getClass());
                            }
                            panel.add(indicator);
                            getJPanelIndicators().add(panel);
                        } else {
                            if (panel != null) {
                                panel.remove(1);
                                getJPanelIndicators().remove(panel);
                            }
                        }
                        getJPanelIndicators().setVisible(true);
                    }
                }
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
    
    public static Window getActiveWindow() {
        if (instance == null) return null;
        if (instance.dialog == null) return instance;
        return instance.dialog.isVisible() ? instance.dialog : instance;
    }
    
    // TODO [P2] some memory-usage optimization would be nice (rea tools initialization after tools data import in overwrite mode)
    private static void representTools() {
        Map<String, Class<? extends ToolExtension>> extensions = ExtensionFactory.getInstance().getAnnotatedToolExtensions();
        if (extensions != null) {
            instance.getJToolBar3().removeAll();
            tools = new LinkedHashMap<Class<? extends ToolExtension>, ToolExtension>();
            Map<String, byte[]> toolsData = BackEnd.getInstance().getToolsData();
            int toolCnt = 0;
            for (Entry<String, Class<? extends ToolExtension>> ext : extensions.entrySet()) {
                try {
                    byte[] toolData = toolsData.get(ext.getValue().getName());
                    final ToolExtension tool = ExtensionFactory.getInstance().newToolExtension(ext.getValue(), toolData);
                    ToolRepresentation representation = tool.getRepresentation();
                    JButton toolButt = representation.getButton();
                    if (toolButt != null) {
                        if (Validator.isNullOrBlank(toolButt.getToolTipText())) {
                            toolButt.setToolTipText(ext.getKey());
                        }
                        instance.getJToolBar3().add(toolButt);
                    }
                    JComponent indicator = representation.getIndicator();
                    if (indicator != null) {
                        JPanel panel = instance.createStatusBarIndicatorArea(ext.getValue());
                        panel.add(indicator, BorderLayout.CENTER);
                    }
                    tools.put(ext.getValue(), tool);
                    toolCnt++;
                } catch (Throwable t) {
                    displayErrorMessage("Failed to initialize tool '" + ext.getValue().getCanonicalName() + "'", t);
                }
            }
            if (toolCnt != 0) {
                instance.getJPanel5().setVisible(true);
            }
        }
    }
    
    private JPanel createStatusBarIndicatorArea(Class<? extends ToolExtension> ext) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 18));
        panel.add(separator, BorderLayout.WEST);
        getJPanelIndicators().add(panel);
        if (ext != null) {
            indicatorAreas.put(ext, panel);
        }
        return panel;
    }
    
    private boolean tabsInitialized;
    private void representData(DataCategory data) {
        UUID id = getSelectedVisualEntryID();
        if (data.getPlacement() != null) {
            getJTabbedPane().setTabPlacement(data.getPlacement());
        }
        tabsInitialized = false;
        representData(getJTabbedPane(), data);
        tabsInitialized = true;
        if (data.getActiveIndex() != null) {
            try {
                getJTabbedPane().setSelectedIndex(data.getActiveIndex());
            } catch (IndexOutOfBoundsException ioobe) {
                // simply ignore incorrect index settings (this may happen on import and is allowable)
            }
            currentTabPane = getJTabbedPane();
        }
        if (id != null) {
            switchToVisualEntry(id);
        }
        initTabContent();
        handleNavigationHistory();
    }

    private void representData(JTabbedPane tabbedPane, DataCategory data) {
        try {
            for (Recognizable item : data.getData()) {
                if (item instanceof DataEntry) {
                    DataEntry de = (DataEntry) item;
                    String caption = de.getCaption();
                    dataEntries.put(de.getId().toString(), de);
                    putTab(tabbedPane, caption, item.getIcon(), getEntryExtensionPanel(de.getId(), null));
                } else if (item instanceof DataCategory) {
                    String caption = item.getCaption();
                    JTabbedPane categoryTabPane = new JTabbedPane();
                    if (item.getId() != null) {
                        categoryTabPane.setName(item.getId().toString());
                    }
                    DataCategory dc = (DataCategory) item;
                    categoryTabPane.setTabPlacement(dc.getPlacement());
                    addTabPaneListeners(categoryTabPane);
                    categoryTabPane = (JTabbedPane) putTab(tabbedPane, caption, item.getIcon(), categoryTabPane);
                    currentTabPane = categoryTabPane;
                    representData(categoryTabPane, dc);
                    if (dc.getActiveIndex() != null) {
                        if (categoryTabPane.getTabCount() - 1 < dc.getActiveIndex()) {
                            categoryTabPane.setSelectedIndex(Integer.valueOf(categoryTabPane.getTabCount() - 1));
                        } else {
                            categoryTabPane.setSelectedIndex(dc.getActiveIndex());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            displayErrorMessage("Failed to represented data correctly!", ex);
        }
    }
    
    private Component putTab(JTabbedPane tabbedPane, String caption, Icon icon, Component cmp) {
        String putId = cmp.getName();
        boolean overwrite = false;
        Component c = null;
        int i;
        for (i = 0; i < tabbedPane.getTabCount(); i++) {
            c = tabbedPane.getComponent(i);
            if (c != null) {
                String id = c.getName();
                if (putId.equals(id)) {
                    overwrite = true;
                    break;
                }
            }
        }
        if (overwrite) {
            if (cmp instanceof JTabbedPane) {
                tabbedPane.setTitleAt(i, caption);
                tabbedPane.setIconAt(i, icon);
                cmp = c;
            } else if (cmp instanceof JPanel) {
                tabbedPane.removeTabAt(i);
                tabbedPane.addTab(caption, icon, cmp);
                TabMoveUtil.moveTab(tabbedPane, tabbedPane.getTabCount() - 1, i);
            }
        } else {
            tabbedPane.addTab(caption, icon, cmp);
        }
        return cmp;
    }
    
    private JPanel getEntryExtensionPanel(UUID entryId, EntryExtension extension) {
        JPanel p = new JPanel(new BorderLayout());
        p.setName(entryId.toString());
        if (extension != null) {
            p.add(extension, BorderLayout.CENTER);
        }
        return p;
    }
    
    private static EntryExtension initEntryExtension(String entryId) throws Throwable {
        DataEntry de = dataEntries.get(entryId.toString());
        if (de.getData() == null) {
            BackEnd.getInstance().loadDataEntryData(de);
        }
        EntryExtension extension;
        try {
            extension = ExtensionFactory.getInstance().newEntryExtension(de);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            extension = new MissingExtensionInformer(de);
        }
        return extension;
    }
    
    private void store() throws Throwable {
        BackEnd.getInstance().setConfig(collectProperties());
        BackEnd.getInstance().setData(collectData());
        BackEnd.getInstance().setToolsData(collectToolsData());
        BackEnd.getInstance().store();
        displayStatusBarMessage("data saved");
    }
    
    private Map<String, byte[]> collectToolsData() throws Throwable {
        Map<String, byte[]> toolsData = new HashMap<String, byte[]>();
        if (tools != null) {
            for (ToolExtension tool : tools.values()) {
                toolsData.put(tool.getClass().getName(), tool.serializeData());
                BackEnd.getInstance().storeExtensionSettings(tool.getClass().getName(), tool.serializeSettings());
            }
        }
        return toolsData;
    }

    private Properties collectProperties() {
        config.put(Constants.PROPERTY_WINDOW_COORDINATE_X, 
                Constants.EMPTY_STR + getLocation().getX() / getToolkit().getScreenSize().getWidth());
        config.put(Constants.PROPERTY_WINDOW_COORDINATE_Y, 
                Constants.EMPTY_STR + getLocation().getY() / getToolkit().getScreenSize().getHeight());
        config.put(Constants.PROPERTY_WINDOW_WIDTH, 
                Constants.EMPTY_STR + getSize().getWidth() / getToolkit().getScreenSize().getHeight());
        config.put(Constants.PROPERTY_WINDOW_HEIGHT, 
                Constants.EMPTY_STR + getSize().getHeight() / getToolkit().getScreenSize().getHeight());
        UUID lsid = getSelectedVisualEntryID();
        if (lsid != null) {
            config.put(Constants.PROPERTY_LAST_SELECTED_ID, lsid.toString());
        }
        return config;
    }
    
    private DataCategory collectData() throws Exception {
        DataCategory data = collectData("root", getJTabbedPane());
        data.setPlacement(getJTabbedPane().getTabPlacement());
        if (getJTabbedPane().getSelectedIndex() != -1) {
            data.setActiveIndex(getJTabbedPane().getSelectedIndex());
        }
        return data;
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
            } else if (c instanceof JPanel) {
                EntryExtension extension = null;
                JPanel p = (JPanel) c;
                byte[] serializedData = null;
                byte[] serializedSettings = null;
                if (p.getComponentCount() != 0) {
                    extension = (EntryExtension) p.getComponent(0);
                    try {
                        serializedData = extension.serializeData();
                    } catch (Throwable t) {
                        displayErrorMessage(
                                "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize data!" + Constants.NEW_LINE +
                                "Data that are failed to serialize will be lost! :(" + Constants.NEW_LINE + 
                                "This the most likely is an extension's bug." + Constants.NEW_LINE +
                                "You can either:" + Constants.NEW_LINE +
                                    "* check for new version of extension (the bug may be fixed in new version)" + Constants.NEW_LINE +
                                    "* uninstall extension to avoid further instability and data loss" + Constants.NEW_LINE + 
                                    "* refer to extension's author for further help", t);
                    }
                    try {
                        serializedSettings = extension.serializeSettings();
                    } catch (Throwable t) {
                        displayErrorMessage(
                                "Extension '" + extension.getClass().getSimpleName() + "' failed to serialize settings!" + Constants.NEW_LINE +
                                "Settings that are failed to serialize will be lost! :(" + Constants.NEW_LINE + 
                                "This the most likely is an extension's bug." + Constants.NEW_LINE +
                                "You can either:" + Constants.NEW_LINE +
                                    "* check for new version of extension (the bug may be fixed in new version)" + Constants.NEW_LINE +
                                    "* uninstall extension to avoid further instability and data loss" + Constants.NEW_LINE + 
                                    "* refer to extension's author for further help", t);
                    }
                }
                DataEntry dataEntry;
                if (extension != null) {
                    String type;
                    if (extension instanceof MissingExtensionInformer) {
                        type = ((MissingExtensionInformer) extension).getDataEntry().getType();
                    } else {
                        type = extension.getClass().getPackage().getName().replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                    }
                    dataEntry = new DataEntry(extension.getId(), caption, icon, type, serializedData, serializedSettings);
                } else {
                    dataEntry = dataEntries.get(p.getName());
                }
                data.addDataItem(dataEntry);
            }
        }
        return data;
    }
    
    private void exitWithOptionalAutoSave() {
        if (Preferences.getInstance().autoSaveOnExit) {
            try {
                store();
                BackEnd.getInstance().shutdown(0);
            } catch (Throwable t) {
                displayErrorMessage("Failed to save!", t);
            }
        } else {
            BackEnd.getInstance().shutdown(0);
        }
    }
    
    private void exit() {
        if (Preferences.getInstance().exitWithoutConfirmation) {
            exitWithOptionalAutoSave();
        } else {
            Component[] cs = null;
            JLabel l = new JLabel();
            StringBuffer caption = new StringBuffer();
            if (!Preferences.getInstance().autoSaveOnExit) {
                caption.append("All unsaved changes will be lost. ");
                JButton b = new JButton("Save changes before exit");
                b.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            store();
                            BackEnd.getInstance().shutdown(0);
                        } catch (Throwable t) {
                            displayErrorMessage("Failed to save data!", t);
                        }
                    }
                });
                cs = new Component[]{l,b};
            } else {
                cs = new Component[]{l};
            }
            caption.append("Click OK to exit.");
            l.setText(caption.toString());
            if (JOptionPane.showConfirmDialog(FrontEnd.this, 
                    cs,
                    "Exit confirmation",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                exitWithOptionalAutoSave();
            }
        }
    }

    public static UUID getSelectedVisualEntryID() {
        if (instance != null) {
            return instance.getSelectedVisualEntryID(instance.getJTabbedPane());
        }
        return null;
    }

    private UUID getSelectedVisualEntryID(JTabbedPane tabPane) {
        if (tabPane.getTabCount() > 0) {
            if (tabPane.getSelectedIndex() != -1) {
                Component c = tabPane.getSelectedComponent();
                if (c instanceof JTabbedPane) {
                    return getSelectedVisualEntryID((JTabbedPane) c);
                } else if (c instanceof JPanel) {
                    return UUID.fromString(((JPanel) c).getName());
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
        return tabPane.getName() == null ? null : UUID.fromString(tabPane.getName());
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
                } else if (c instanceof JPanel) {
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
            } else if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                if (p.getName() != null) {
                    ids.add(UUID.fromString(p.getName()));
                }
            }
        }
        return ids;
    }

    // TODO [P2] some optimization might be needed here 
    //      (do not iterate over all tabs (to get full extensions list) each time, 
    //      some caching would be nice)
    
    public static Map<UUID, EntryExtension> getEntryExtensions() throws Throwable {
        if (instance != null) {
            entryExtensions.clear();
            return instance.getEntryExtensions(instance.getJTabbedPane(), null);
        }
        return null;
    }

    public static Map<UUID, EntryExtension> getEntryExtensions(Class<? extends EntryExtension> filterClass) throws Throwable {
        if (instance != null) {
            entryExtensions.clear();
            return instance.getEntryExtensions(instance.getJTabbedPane(), filterClass);
        }
        return null;
    }

    private Map<UUID, EntryExtension> getEntryExtensions(JTabbedPane tabPane, Class<? extends EntryExtension> filterClass) throws Throwable {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            Component c = tabPane.getComponent(i);
            if (c instanceof JTabbedPane) {
                entryExtensions.putAll(getEntryExtensions((JTabbedPane) c, filterClass));
            } else if (c instanceof JPanel) {
                UUID id = UUID.fromString(c.getName());
                JPanel p = ((JPanel) c);
                EntryExtension ext;
                if (p.getComponentCount() == 0) {
                    ext = initEntryExtension(id.toString());
                    p.add(ext);
                } else {
                    ext = (EntryExtension) p.getComponent(0);
                }
                if (filterClass == null) {
                    entryExtensions.put(id, ext);
                } else if (ext.getClass().getName().equals(filterClass.getName())) {
                    entryExtensions.put(id, ext);
                }
            }
        }
        return entryExtensions;
    }

    public static Map<UUID, VisualEntryDescriptor> getVisualEntryDescriptors() {
        if (instance != null) {
            return instance.getVisualEntryDescriptors(instance.getJTabbedPane(), null, new LinkedList<Recognizable>());
        }
        return null;
    }

    public static Map<UUID, VisualEntryDescriptor> getVisualEntryDescriptors(Class<? extends EntryExtension> filterClass) {
        if (instance != null) {
            return instance.getVisualEntryDescriptors(instance.getJTabbedPane(), filterClass, new LinkedList<Recognizable>());
        }
        return null;
    }

    private Map<UUID, VisualEntryDescriptor> getVisualEntryDescriptors(JTabbedPane tabPane, Class<? extends EntryExtension> filterClass, LinkedList<Recognizable> entryPath) {
        Map<UUID, VisualEntryDescriptor> entries = new LinkedHashMap<UUID, VisualEntryDescriptor>();
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            Component c = tabPane.getComponent(i);
            String caption = tabPane.getTitleAt(i);
            Icon icon = tabPane.getIconAt(i);
            if (c instanceof JTabbedPane) {
                UUID id = UUID.fromString(c.getName());
                Recognizable entry = new Recognizable(id, caption, icon);
                entryPath.addLast(entry);
                entries.put(id, new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath), ENTRY_TYPE.CATEGORY));
                entries.putAll(getVisualEntryDescriptors((JTabbedPane) c, filterClass, new LinkedList<Recognizable>(entryPath)));
                entryPath.removeLast();
            } else if (c instanceof JPanel) {
                UUID id = UUID.fromString(c.getName());
                Recognizable entry = new Recognizable(id, caption, icon);
                entryPath.addLast(entry);
                if (filterClass == null) {
                    entries.put(id, new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath), ENTRY_TYPE.ENTRY));
                } else if (((JPanel) c).getComponent(0).getClass().getName().equals(filterClass.getName())) {
                    entries.put(id, new VisualEntryDescriptor(entry, new LinkedList<Recognizable>(entryPath), ENTRY_TYPE.ENTRY));
                }
                entryPath.removeLast();
            }
        }
        return entries;
    }

    public static boolean switchToVisualEntry(UUID id) {
        return switchToVisualEntry(id, true);
    }

    private static boolean switchToVisualEntry(UUID id, boolean addToNavigationHistory) {
        if (instance != null) {
            navigating = true;
            boolean switched = instance.switchToVisualEntry(instance.getJTabbedPane(), id, new LinkedList<Component>());
            navigating = false;
            if (addToNavigationHistory) {
                instance.handleNavigationHistory();
            }
            return switched;
        }
        return false;
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
            } else if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                if (p.getName().equals(id.toString())) {
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
                currentTabPane = (JTabbedPane) selComp;
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
    
    public static void displayBottomPanel(JLabel title, JPanel content) {
        if (instance != null) {
            int dl = instance.getJPanel2().isVisible() ? instance.getJSplitPane().getDividerLocation() : instance.getHeight()/5*3;
            instance.getJPanel2().setVisible(false);
            instance.getJPanel2().removeAll();
            instance.getJPanel2().setLayout(new BorderLayout());
            instance.getJPanel3().setVisible(false);
            instance.getJPanel3().removeAll();
            instance.getJPanel3().setLayout(new BorderLayout());
            instance.getJPanel3().add(title, BorderLayout.CENTER);
            JButton closeButton = new JButton(new AbstractAction(){
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) {
                    instance.getJPanel2().setVisible(false);
                }
            });
            closeButton.setIcon(ICON_CLOSE);
            closeButton.setPreferredSize(new Dimension(18, 18));
            instance.getJPanel3().add(closeButton, BorderLayout.EAST);
            instance.getJPanel3().setVisible(true);
            instance.getJPanel2().add(instance.getJPanel3(), BorderLayout.NORTH);
            instance.getJPanel2().add(new JScrollPane(content), BorderLayout.CENTER);
            instance.getJPanel2().setVisible(true);
            instance.getJSplitPane().setDividerLocation(dl);
        }
    }
    
    public static void hideBottomPanel() {
        if (instance != null) {
            instance.getJPanel2().setVisible(false);
        }
    }
    
    public static void displayErrorMessage(Throwable t) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(instance, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace(System.err);
    }

    public static void displayErrorMessage(String message, Throwable t) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(instance, message, "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace(System.err);
    }

    public static void displayErrorMessage(String message) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(instance, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void displayMessage(String message) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(instance, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void displayAddOnsScreenErrorMessage(String message, Throwable t) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(dialog, message, "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace(System.err);
    }

    private void displayAddOnsScreenErrorMessage(String message) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(dialog, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void displayAddOnsScreenErrorMessage(Throwable t) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(dialog, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace(System.err);
    }

    private void displayAddOnsScreenMessage(String message) {
        Splash.hideSplash();
        JOptionPane.showMessageDialog(dialog, message, "Information", JOptionPane.INFORMATION_MESSAGE);
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
            initTabContent();
            handleNavigationHistory();
        }
    };
    
    private void handleNavigationHistory() {
        if (tabsInitialized && currentTabPane != null) {
            Component c = currentTabPane.getSelectedComponent();
            if (c == null) {
                c = currentTabPane;
            }
            if (c.getName() != null) {
                UUID id = UUID.fromString(c.getName());
                if (navigationHistory.isEmpty() || (!navigating && !navigationHistory.get(navigationHistoryIndex).equals(id))) {
                    while (!navigationHistory.isEmpty() && (navigationHistory.size() > navigationHistoryIndex + 1)) {
                        navigationHistory.pop();
                    }
                    navigationHistory.push(id);
                    navigationHistoryIndex++;
                }
            }
            if (!navigationHistory.isEmpty()) {
                if (navigationHistory.size() == 1) {
                    backToFirstAction.setEnabled(false);
                    backAction.setEnabled(false);
                    forwardAction.setEnabled(false);
                    forwardToLastAction.setEnabled(false);
                } else {
                    if (navigationHistoryIndex == 0) {
                        backToFirstAction.setEnabled(false);
                        backAction.setEnabled(false);
                    } else {
                        backToFirstAction.setEnabled(true);
                        backAction.setEnabled(true);
                    }
                    if (navigationHistoryIndex == navigationHistory.size() - 1) {
                        forwardAction.setEnabled(false);
                        forwardToLastAction.setEnabled(false);
                    } else {
                        forwardAction.setEnabled(true);
                        forwardToLastAction.setEnabled(true);
                    }
                }
            }
        }
    }
    
    private void initTabContent() {
        if (tabsInitialized && currentTabPane != null && currentTabPane.getSelectedIndex() != -1) {
            Component c = currentTabPane.getSelectedComponent();
            if (c instanceof JPanel) {
                try {
                    final JPanel p = ((JPanel) c);
                    final String id = p.getName();
                    if (p.getComponentCount() == 0) {
                        EntryExtension ee = initEntryExtension(id);
                        p.add(ee, BorderLayout.CENTER);
                    }
                } catch (Throwable t) {
                    displayErrorMessage("Failed to initialize extension!", t);
                }
            } else if (c instanceof JTabbedPane) {
                currentTabPane = (JTabbedPane) c;
                initTabContent();
            }
        }
    }
    
    private void autoscrollList(final JList list) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                list.ensureIndexIsVisible(list.getModel().getSize() - 1);
            }
        });
    }
    
    private JList getStatusBarMessagesList() {
        if (statusBarMessagesList == null) {
            statusBarMessagesList = new JList(new DefaultListModel());
        }
        return statusBarMessagesList;
    }
    
    public void displayStatusBarMessage(final String message) {
        new Thread(new Runnable(){
            public void run() {
                final String timestamp = dateFormat.format(new Date()) + " # ";
                getJLabelStatusBarMsg().setText(STATUS_MESSAGE_PREFIX + timestamp + STATUS_MESSAGE_HTML_COLOR_HIGHLIGHTED + message + STATUS_MESSAGE_SUFFIX);
                ((DefaultListModel) getStatusBarMessagesList().getModel()).addElement(timestamp + message);
                if (getJPanel2().isVisible()) {
                    autoscrollList(getStatusBarMessagesList());
                }
                ActionListener al = new ActionListener(){
                    public void actionPerformed(ActionEvent ae){
                        getJLabelStatusBarMsg().setText(STATUS_MESSAGE_PREFIX + timestamp + STATUS_MESSAGE_HTML_COLOR_NORMAL + message + STATUS_MESSAGE_SUFFIX);
                    }
                };
                Timer timer = new Timer(1000,al);
                timer.setRepeats(false);
                timer.start();
            }
        }).start();
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJPanel(), BorderLayout.NORTH);
            jContentPane.add(getJPanel4(), BorderLayout.CENTER);
            jContentPane.add(getJPanelStatusBar(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }
    
    /**
     * This method initializes jPanelStatusBar
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelStatusBar() {
        if (jPanelStatusBar == null) {
            jPanelStatusBar = new JPanel();
            jPanelStatusBar.setLayout(new BorderLayout());
            jPanelStatusBar.add(getJLabelStatusBarMsg(), BorderLayout.WEST);
            jPanelStatusBar.add(getJPanelIndicators(), BorderLayout.EAST);
        }
        return jPanelStatusBar;
    }

    /**
     * This method initializes jPanelIndicators
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelIndicators() {
        if (jPanelIndicators == null) {
            jPanelIndicators = new JPanel(new FlowLayout());
            jPanelIndicators.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        return jPanelIndicators;
    }

    /**
     * This method initializes jLabelStatusBarMsg
     * 
     * @return javax.swing.JLabel
     */
    private JLabel getJLabelStatusBarMsg() {
        if (jLabelStatusBarMsg == null) {
            jLabelStatusBarMsg = new JLabel();
            final JLabel title = new JLabel("Messages History");
            final JPanel panel = new JPanel(new BorderLayout());
            panel.add(getStatusBarMessagesList(), BorderLayout.CENTER);
            jLabelStatusBarMsg.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e) {
                    displayBottomPanel(title, panel);
                    autoscrollList(getStatusBarMessagesList());
                }
            });
        }
        return jLabelStatusBarMsg;
    }

    /**
     * This method initializes jTabbedPane
     * 
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setBackground(null);
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
            jToolBar.setFloatable(false);
            jToolBar.add(getJButton7());
            jToolBar.add(getJButton2());
            jToolBar.add(getJButton12());
            jToolBar.add(getJButton3());
            jToolBar.add(getJButton4());
            jToolBar.add(getJButton());
            jToolBar.add(getJButton5());
            jToolBar.add(getJButton1());
            jToolBar.add(getJButton11());
            jToolBar.addSeparator();
            jToolBar.add(getJButton13());
            jToolBar.add(getJButton14());
            jToolBar.add(getJButton15());
            jToolBar.add(getJButton16());
        }
        return jToolBar;
    }

    /**
     * This method initializes jToolBar3
     * 
     * @return javax.swing.JToolBar
     */
    private JToolBar getJToolBar3() {
        if (jToolBar3 == null) {
            jToolBar3 = new JToolBar(JToolBar.VERTICAL);
            jToolBar3.setFloatable(false);
        }
        return jToolBar3;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton(addRootEntryAction);
            jButton.setText(Constants.EMPTY_STR);
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
            jButton5.setText(Constants.EMPTY_STR);
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
            jButton11.setText(Constants.EMPTY_STR);
        }
        return jButton11;
    }

    /**
     * This method initializes jButton13
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton13() {
        if (jButton13 == null) {
            jButton13 = new JButton(backToFirstAction);
            jButton13.setText(Constants.EMPTY_STR);
        }
        return jButton13;
    }

    /**
     * This method initializes jButton14
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton14() {
        if (jButton14 == null) {
            jButton14 = new JButton(backAction);
            jButton14.setText(Constants.EMPTY_STR);
        }
        return jButton14;
    }

    /**
     * This method initializes jButton11
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton15() {
        if (jButton15 == null) {
            jButton15 = new JButton(forwardAction);
            jButton15.setText(Constants.EMPTY_STR);
        }
        return jButton15;
    }

    /**
     * This method initializes jButton16
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton16() {
        if (jButton16 == null) {
            jButton16 = new JButton(forwardToLastAction);
            jButton16.setText(Constants.EMPTY_STR);
        }
        return jButton16;
    }

    /**
     * This method initializes jButton1
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton(deleteEntryOrCategoryAction);
            jButton1.setText(Constants.EMPTY_STR);
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
            jButton2 = new JButton(importAction);
            jButton2.setText(Constants.EMPTY_STR);
        }
        return jButton2;
    }

    /**
     * This method initializes jButton12
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton12() {
        if (jButton12 == null) {
            jButton12 = new JButton(exportAction);
            jButton12.setText(Constants.EMPTY_STR);
        }
        return jButton12;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getJToolBar(), BorderLayout.CENTER);
            jPanel.add(getJToolBar2(), BorderLayout.EAST);
        }
        return jPanel;
    }

    /**
     * This method initializes jPanel5
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel5() {
        if (jPanel5 == null) {
            jPanel5 = new JPanel();
            jPanel5.setVisible(false);
            jPanel5.setLayout(new BorderLayout());
            jPanel5.add(getJToolBar3(), BorderLayout.CENTER);
        }
        return jPanel5;
    }

    /**
     * This method initializes jPanel4
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            jPanel4 = new JPanel();
            jPanel4.setLayout(new BorderLayout());
            jPanel4.add(getJPanel5(), BorderLayout.WEST);
            jPanel4.add(getJSplitPane(), BorderLayout.CENTER);
        }
        return jPanel4;
    }

    /**
     * This method initializes jSplitPane
     * 
     * @return javax.swing.JSplitPane
     */
    private JSplitPane getJSplitPane() {
        if (jSplitPane == null) {
            jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            jSplitPane.setDividerSize(3);
            jSplitPane.setTopComponent(getJTabbedPane());
            jSplitPane.setBottomComponent(getJPanel2());
        }
        return jSplitPane;
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
        }
        return jPanel3;
    }

    /**
     * This method initializes jToolBar2
     * 
     * @return javax.swing.JToolBar
     */
    private JToolBar getJToolBar2() {
        if (jToolBar2 == null) {
            jToolBar2 = new JToolBar();
            jToolBar2.setFloatable(false);
            jToolBar2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            jToolBar2.add(getJButton10());
            jToolBar2.add(getJButton6());
            jToolBar2.add(getJButton8());
            jToolBar2.add(getJButton9());
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
            jButton6.setText(Constants.EMPTY_STR);
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
            jButton7.setText(Constants.EMPTY_STR);
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
            jButton8.setText(Constants.EMPTY_STR);
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
            jButton9.setText(Constants.EMPTY_STR);
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
            jButton10 = new JButton(exitAction);
            jButton10.setText(Constants.EMPTY_STR);
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
            jButton3.setText(Constants.EMPTY_STR);
        }
        return jButton3;
    }

    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton(addCategoryAction);
            jButton4.setText(Constants.EMPTY_STR);
        }
        return jButton4;
    }

    private boolean confirmedDelete() {
        return confirmed("Delete confirmation", "Are you sure you want to delete active entry?");
    }
    
    private boolean confirmedUninstall() {
        return confirmed("Uninstall confirmation", "Are you sure you want to uninstall selected add-on(s)?");
    }
    
    private boolean confirmed(String title, String message) {
        if (Preferences.getInstance().displayConfirmationDialogs) {
            int opt = JOptionPane.showConfirmDialog(
                    getActiveWindow(), 
                    "<html>" + message + "<br/><br/>" +
                    		"<i>(Note: this dialog can be disabled via preferences option 'Display confirmation dialogs')</i><html>", 
                    title, 
                    JOptionPane.YES_NO_OPTION);
            return opt == JOptionPane.YES_OPTION;
        }
        return true;
    }
    
    private boolean autoconfirmed(String title, String message) {
        if (!Preferences.getInstance().autoMode) {
            int opt = JOptionPane.showConfirmDialog(
                    getActiveWindow(), 
                    "<html>" + message + "<br/><br/>" +
                            "<i>(Note: this dialog can be disabled via preferences option 'Auto-mode')</i><html>", 
                    title, 
                    JOptionPane.OK_CANCEL_OPTION);
            return opt == JOptionPane.OK_OPTION;
        }
        return true;
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

    private AddRootCategoryAction addRootCategoryAction = new AddRootCategoryAction();
    private class AddRootCategoryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AddRootCategoryAction() {
            putValue(Action.NAME, "addRootCategory");
            putValue(Action.SHORT_DESCRIPTION, "add root category");
            putValue(Action.SMALL_ICON, controlIcons.getIconRootCategory());
        }
        
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
            displayStatusBarMessage("root category '" + categoryCaption + "' added");
        }
    }

    private AddRootEntryAction addRootEntryAction = new AddRootEntryAction();
    private class AddRootEntryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AddRootEntryAction() {
            putValue(Action.NAME, "addRootEntry");
            putValue(Action.SHORT_DESCRIPTION, "add root entry");
            putValue(Action.SMALL_ICON, controlIcons.getIconRootEntry());
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                Map<String, Class<? extends EntryExtension>> extensions = ExtensionFactory.getInstance().getAnnotatedEntryExtensions();
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

    private void addRootEntryAction(Map<String, Class<? extends EntryExtension>> extensions) throws Throwable {
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
            Class<? extends EntryExtension> type = extensions.get(typeDescription);
            byte[] defSettings = BackEnd.getInstance().getExtensionSettings(type.getName());
            if (defSettings == null) {
                // extension's first time usage
                configureExtension(type.getName(), true);
            }
            EntryExtension extension = ExtensionFactory.getInstance().newEntryExtension(type);
            if (extension != null) {
                JPanel p = getEntryExtensionPanel(extension.getId(), extension);
                getJTabbedPane().addTab(caption, p);
                getJTabbedPane().setSelectedComponent(p);
                ImageIcon icon = (ImageIcon) iconChooser.getSelectedItem();
                if (icon != null) {
                    getJTabbedPane().setIconAt(getJTabbedPane().getSelectedIndex(), icon);
                }
                displayStatusBarMessage("root entry '" + caption + "' added");
            }
        }
    }
    
    private ChangePasswordAction changePasswordAction = new ChangePasswordAction();
    private class ChangePasswordAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ChangePasswordAction() {
            putValue(Action.NAME, "changePassword");
            putValue(Action.SHORT_DESCRIPTION, "change password");
            putValue(Action.SMALL_ICON, controlIcons.getIconChangePassword());
        }
        
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
                        displayStatusBarMessage("password changed");
                    } catch (Exception ex) {
                        displayErrorMessage("Failed to change password!" + Constants.NEW_LINE + ex.getMessage(), ex);
                    }
                }
            }
        }
    };

    private AddEntryAction addEntryAction = new AddEntryAction();
    private class AddEntryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AddEntryAction() {
            putValue(Action.NAME, "addEntry");
            putValue(Action.SHORT_DESCRIPTION, "add entry");
            putValue(Action.SMALL_ICON, controlIcons.getIconEntry());
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                Map<String, Class<? extends EntryExtension>> extensions = ExtensionFactory.getInstance().getAnnotatedEntryExtensions();
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
                        Class<? extends EntryExtension> type = extensions.get(typeDescription);
                        byte[] defSettings = BackEnd.getInstance().getExtensionSettings(type.getName());
                        if (defSettings == null) {
                            // extension's first time usage
                            configureExtension(type.getName(), true);
                        }
                        EntryExtension extension = ExtensionFactory.getInstance().newEntryExtension(type);
                        if (extension != null) {
                            JPanel p = getEntryExtensionPanel(extension.getId(), extension);
                            currentTabPane.addTab(caption, p);
                            currentTabPane.setSelectedComponent(p);
                            ImageIcon icon = (ImageIcon) iconChooser.getSelectedItem();
                            if (icon != null) {
                                currentTabPane.setIconAt(currentTabPane.getSelectedIndex(), icon);
                            }
                            displayStatusBarMessage("entry '" + caption + "' added");
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

    private DeleteAction deleteEntryOrCategoryAction = new DeleteAction();
    private class DeleteAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public DeleteAction() {
            putValue(Action.NAME, "delete");
            putValue(Action.SHORT_DESCRIPTION, "delete active entry/category");
            putValue(Action.SMALL_ICON, controlIcons.getIconDelete());
        }
        
        public void actionPerformed(ActionEvent evt) {
            if (getJTabbedPane().getTabCount() > 0) {
                try {
                    if (currentTabPane.getTabCount() > 0) {
                        if (confirmedDelete()) {
                            String caption = currentTabPane.getTitleAt(currentTabPane.getSelectedIndex());
                            currentTabPane.remove(currentTabPane.getSelectedIndex());
                            displayStatusBarMessage("entry '" + caption + "' deleted");
                            currentTabPane = getActiveTabPane(currentTabPane);
                        }
                    } else {
                        JTabbedPane parentTabPane = (JTabbedPane) currentTabPane.getParent();
                        if (parentTabPane != null) {
                            if (confirmedDelete()) {
                                String caption = parentTabPane.getTitleAt(parentTabPane.getSelectedIndex());
                                parentTabPane.remove(currentTabPane);
                                displayStatusBarMessage("category '" + caption + "' deleted");
                                currentTabPane = getActiveTabPane(parentTabPane);
                            }
                        }
                    }
                } catch (Exception ex) {
                    displayErrorMessage(ex);
                }
            }
        }
    };
    
    private void createDependentCheckboxChangeListener(final JCheckBox main, final JCheckBox dependent) {
        dependent.setEnabled(main.isEnabled() && main.isSelected());
        main.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                dependent.setEnabled(main.isEnabled() && main.isSelected());
            }
        });
        main.addPropertyChangeListener("enabled", new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                dependent.setEnabled(main.isEnabled() && main.isEnabled());
            }
        });
    }

    private ImportAction importAction = new ImportAction();
    private class ImportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ImportAction() {
            putValue(Action.NAME, "import");
            putValue(Action.SHORT_DESCRIPTION, "import...");
            putValue(Action.SMALL_ICON, controlIcons.getIconImport());
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                final JComboBox configsCB = new JComboBox();
                configsCB.addItem(Constants.EMPTY_STR);
                for (String configName : BackEnd.getInstance().getImportConfigurations().keySet()) {
                    configsCB.addItem(configName);
                }
                final JButton delButt = new JButton("Delete");
                delButt.setEnabled(false);
                delButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String name = (String) configsCB.getSelectedItem();
                            BackEnd.getInstance().removeImportConfiguration(name);
                            configsCB.removeItem(name);
                        } catch (Exception ex) {
                            displayErrorMessage("Failed to delete selected import-configuration!", ex);
                        }
                    }
                });
                final JButton renButt = new JButton("Rename");
                renButt.setEnabled(false);
                renButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String oldName = (String) configsCB.getSelectedItem();
                            String newName = JOptionPane.showInputDialog(FrontEnd.this, "New name:", oldName);
                            if (!Validator.isNullOrBlank(newName)) {
                                BackEnd.getInstance().renameImportConfiguration(oldName, newName);
                                configsCB.removeItem(oldName);
                                configsCB.addItem(newName);
                                configsCB.setSelectedItem(newName);
                            }
                        } catch (Exception ex) {
                            displayErrorMessage("Failed to rename selected import-configuration!", ex);
                        }
                    }
                });
                configsCB.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e) {
                        if (!Constants.EMPTY_STR.equals(configsCB.getSelectedItem())) {
                            delButt.setEnabled(true);
                            renButt.setEnabled(true);
                        } else {
                            delButt.setEnabled(false);
                            renButt.setEnabled(false);
                        }
                    }
                });
                JPanel p = new JPanel(new BorderLayout());
                p.add(configsCB, BorderLayout.CENTER);
                JPanel pb = new JPanel(new GridLayout(1, 2));
                pb.add(renButt);
                pb.add(delButt);
                p.add(pb, BorderLayout.SOUTH);
                Component[] c = new Component[] {
                        new JLabel("Choose existing import configuration to use,"),
                        new JLabel("or leave just press enter for custom import."),
                        p          
                };
                int opt = JOptionPane.showConfirmDialog(FrontEnd.this, c, "Import", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                        try {
                            final JPanel panel = new JPanel(new BorderLayout());
                            final JLabel processLabel = new JLabel("Importing data...");
                            panel.add(processLabel, BorderLayout.CENTER);
                            final JLabel label = new JLabel("Import data");
                            displayBottomPanel(label, panel);
                            Thread importThread = new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        Properties props = BackEnd.getInstance().getImportConfigurations().get(configsCB.getSelectedItem().toString());
                                        TRANSFER_TYPE type = TRANSFER_TYPE.valueOf(props.getProperty(Constants.OPTION_TRANSFER_TYPE));
                                        String password = props.getProperty(Constants.OPTION_DATA_PASSWORD);
                                        if (password == null) {
                                            password = Constants.EMPTY_STR;
                                        }
                                        Transferrer transferrer = Transferrer.getInstance(type);
                                        byte[] importedData = transferrer.doImport(props);
                                        if (importedData == null) {
                                            throw new Exception("Import source initialization failure!");
                                        } else {
                                            try {
                                                File importDir = new File(Constants.TMP_DIR, "importDir");
                                                FSUtils.delete(importDir);
                                                ArchUtils.extract(importedData, importDir);
                                                DataCategory data = BackEnd.getInstance().importData(
                                                        importDir, 
                                                        getVisualEntriesIDs(),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_DATA_ENTRIES)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_DATA_ENTRIES)), 
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_DATA_ENTRY_CONFIGS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_DATA_ENTRY_CONFIGS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_PREFERENCES)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_PREFERENCES)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_GLOBAL_CONFIG)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_TOOLS_DATA)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_TOOLS_DATA)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ICONS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_ICONS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ADDONS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ADDON_CONFIGS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_ADDON_CONFIGS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_IMPORT_EXPORT_CONFIGS)),
                                                        Boolean.valueOf(props.getProperty(Constants.OPTION_OVERWRITE_IMPORT_EXPORT_CONFIGS)),
                                                        props.getProperty(Constants.OPTION_DATA_PASSWORD));
                                                if (!data.getData().isEmpty()) {
                                                    representData(data);
                                                }
                                                if (Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_TOOLS_DATA))) {
                                                    representTools();
                                                }
                                                if (Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_PREFERENCES))) {
                                                    Preferences.getInstance().init();
                                                }
                                                if (Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG))) {
                                                    initGlobalSettings();
                                                    applyGlobalSettings();
                                                }
                                                label.setText("<html><font color=green>Data import - Completed</font></html>");
                                                processLabel.setText("Data have been successfully imported.");
                                                displayStatusBarMessage("import done");
                                            } catch (GeneralSecurityException gse) {
                                                processLabel.setText("Failed to import data! Error details: It seems that you have typed wrong password...");
                                                label.setText("<html><font color=red>Data import - Failed</font></html>");
                                                gse.printStackTrace(System.err);
                                            } catch (Throwable t) {
                                                String errMsg = "Failed to import data!";
                                                if (t.getMessage() != null) {
                                                    errMsg += " Error details: " + t.getClass().getSimpleName() + ": " + t.getMessage();
                                                }
                                                processLabel.setText(errMsg);
                                                label.setText("<html><font color=red>Data import - Failed</font></html>");
                                                t.printStackTrace(System.err);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        String errMsg = "Failed to import data!";
                                        if (ex.getMessage() != null) {
                                            errMsg += " Error details: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
                                        }
                                        processLabel.setText(errMsg);
                                        label.setText("<html><font color=red>Data import - Failed</font></html>");
                                        ex.printStackTrace(System.err);
                                    }
                                }
                            });
                            importThread.start();
                        } catch (Exception ex) {
                            displayErrorMessage("Failed to import data!", ex);
                        }
                    } else {
                        JComboBox cb = new JComboBox();
                        for (TRANSFER_TYPE type : Transferrer.TRANSFER_TYPE.values()) {
                            cb.addItem(type);
                        }
                        opt = JOptionPane.showConfirmDialog(FrontEnd.this, cb, "Choose import type", JOptionPane.OK_CANCEL_OPTION);
                        if (opt == JOptionPane.OK_OPTION) {
                            final TRANSFER_TYPE type = (TRANSFER_TYPE) cb.getSelectedItem();
                            final Properties options = displayTransferOptionsDialog(type, DATA_OPERATION_TYPE.IMPORT);
                            if (options != null) {
                                if (options.isEmpty()) {
                                    throw new Exception("Import source options are missing! Import canceled.");
                                } else {
                                    final JPanel panel = new JPanel(new BorderLayout());
                                    final DefaultListModel processModel = new DefaultListModel();
                                    final JList processList = new JList(processModel);
                                    panel.add(processList, BorderLayout.CENTER);
                                    final JLabel label = new JLabel("Data import");
                                    processModel.addElement("Transferring data to be imported...");
                                    displayBottomPanel(label, panel);
                                    autoscrollList(processList);
                                    Thread importThread = new Thread(new Runnable(){
                                        public void run() {
                                            try {
                                                Transferrer transferrer = Transferrer.getInstance(type);
                                                byte[] importedData = transferrer.doImport(options);
                                                if (importedData == null) {
                                                    processModel.addElement("Import source initialization failure: no data have been retrieved!");
                                                    autoscrollList(processList);
                                                    label.setText("<html><font color=red>Data import - Failed</font></html>");
                                                } else {
                                                    processModel.addElement("Data to be imported successfully transferred.");
                                                    autoscrollList(processList);
                                                    String oe = "Overwrite existing";
                                                    
                                                    JPanel p1 = new JPanel(new GridLayout(6, 2));
                                                    
                                                    JCheckBox importDataEntriesCB = new JCheckBox("Import data entries");
                                                    p1.add(importDataEntriesCB);
                                                    JCheckBox overwriteDataEntriesCB = new JCheckBox(oe);
                                                    p1.add(overwriteDataEntriesCB);
                                                    createDependentCheckboxChangeListener(importDataEntriesCB, overwriteDataEntriesCB);

                                                    JCheckBox importDataEntryConfigsCB = new JCheckBox("Import data entry configs"); 
                                                    p1.add(importDataEntryConfigsCB);
                                                    JCheckBox overwriteDataEntryConfigsCB = new JCheckBox(oe); 
                                                    p1.add(overwriteDataEntryConfigsCB);
                                                    createDependentCheckboxChangeListener(importDataEntryConfigsCB, overwriteDataEntryConfigsCB);
                                                    
                                                    JCheckBox importPreferencesCB = new JCheckBox("Import preferences");
                                                    p1.add(importPreferencesCB);
                                                    JCheckBox overwritePreferencesCB = new JCheckBox(oe); 
                                                    p1.add(overwritePreferencesCB);
                                                    createDependentCheckboxChangeListener(importPreferencesCB, overwritePreferencesCB);
                                                    
                                                    JCheckBox importGlobalConfigCB = new JCheckBox("Import global config"); 
                                                    p1.add(importGlobalConfigCB);
                                                    JCheckBox overwriteGlobalConfigCB = new JCheckBox(oe); 
                                                    p1.add(overwriteGlobalConfigCB);
                                                    createDependentCheckboxChangeListener(importGlobalConfigCB, overwriteGlobalConfigCB);

                                                    JCheckBox importToolsDataCB = new JCheckBox("Import tools data"); 
                                                    p1.add(importToolsDataCB);
                                                    JCheckBox overwriteToolsDataCB = new JCheckBox(oe); 
                                                    p1.add(overwriteToolsDataCB);
                                                    createDependentCheckboxChangeListener(importToolsDataCB, overwriteToolsDataCB);
                                                    
                                                    JCheckBox importIconsCB = new JCheckBox("Import icons");
                                                    p1.add(importIconsCB);
                                                    JCheckBox overwriteIconsCB = new JCheckBox(oe);
                                                    p1.add(overwriteIconsCB);
                                                    createDependentCheckboxChangeListener(importIconsCB, overwriteIconsCB);
                                                    
                                                    JCheckBox importAddOnsCB = new JCheckBox("Import addons (existing add-ons won't be overwritten)");

                                                    JPanel p2 = new JPanel(new GridLayout(2, 2));

                                                    JCheckBox importAddOnConfigsCB = new JCheckBox("Import addon configs");
                                                    p2.add(importAddOnConfigsCB);
                                                    JCheckBox overwriteAddOnConfigsCB = new JCheckBox(oe);
                                                    p2.add(overwriteAddOnConfigsCB);
                                                    createDependentCheckboxChangeListener(importAddOnConfigsCB, overwriteAddOnConfigsCB);
                                                    
                                                    JCheckBox importImportExportConfigsCB = new JCheckBox("Import import/export cofigs");
                                                    p2.add(importImportExportConfigsCB);
                                                    JCheckBox overwriteImportExportConfigsCB = new JCheckBox(oe);
                                                    p2.add(overwriteImportExportConfigsCB);
                                                    createDependentCheckboxChangeListener(importImportExportConfigsCB, overwriteImportExportConfigsCB);
                                                    
                                                    JLabel passwordL = new JLabel("Decrypt imported data using password:");
                                                    JPasswordField passwordTF = new JPasswordField();
                                                    if (JOptionPane.showConfirmDialog(
                                                            FrontEnd.this,
                                                            new Component[] {
                                                                    p1,
                                                                    importAddOnsCB,
                                                                    p2,
                                                                    passwordL,
                                                                    passwordTF
                                                            },
                                                            "Import data", 
                                                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                                                        hideBottomPanel();
                                                    } else {    
                                                        processModel.addElement("Extracting data to be imported...");
                                                        autoscrollList(processList);
                                                        File importDir = new File(Constants.TMP_DIR, "importDir");
                                                        FSUtils.delete(importDir);
                                                        ArchUtils.extract(importedData, importDir);
                                                        processModel.addElement("Data to be imported have been successfully extracted.");
                                                        autoscrollList(processList);
                                                        String password = new String(passwordTF.getPassword());            
                                                        try {
                                                            DataCategory data = BackEnd.getInstance().importData(
                                                                    importDir, 
                                                                    getVisualEntriesIDs(),
                                                                    importDataEntriesCB.isSelected(),
                                                                    overwriteDataEntriesCB.isSelected(),
                                                                    importDataEntryConfigsCB.isSelected(),
                                                                    overwriteDataEntryConfigsCB.isSelected(),
                                                                    importPreferencesCB.isSelected(),
                                                                    overwritePreferencesCB.isSelected(),
                                                                    importGlobalConfigCB.isSelected(),
                                                                    overwriteGlobalConfigCB.isSelected(),
                                                                    importToolsDataCB.isSelected(),
                                                                    overwriteToolsDataCB.isSelected(),
                                                                    importIconsCB.isSelected(),
                                                                    overwriteIconsCB.isSelected(),
                                                                    importAddOnsCB.isSelected(),
                                                                    importAddOnConfigsCB.isSelected(),
                                                                    overwriteAddOnConfigsCB.isSelected(),
                                                                    importImportExportConfigsCB.isSelected(),
                                                                    overwriteImportExportConfigsCB.isSelected(),
                                                                    password);
                                                            if (!data.getData().isEmpty()) {
                                                                representData(data);
                                                            }
                                                            if (importToolsDataCB.isSelected()) {
                                                                representTools();
                                                            }
                                                            if (importPreferencesCB.isSelected()) {
                                                                Preferences.getInstance().init();
                                                            }
                                                            if (importGlobalConfigCB.isSelected()) {
                                                                initGlobalSettings();
                                                                applyGlobalSettings();
                                                            }
                                                            configsCB.setEditable(true);
                                                            label.setText("<html><font color=green>Data import - Completed</font></html>");
                                                            processModel.addElement("Data have been successfully imported.");
                                                            autoscrollList(processList);
                                                            displayStatusBarMessage("import done");
                                                            Component[] c = new Component[] {
                                                                    new JLabel("Data have been successfully imported."),
                                                                    new JLabel("If you want to save this import configuration,"),
                                                                    new JLabel("input a name for it (or select existing one to overwrite):"),
                                                                    configsCB          
                                                            };
                                                            JOptionPane.showMessageDialog(FrontEnd.this, c);
                                                            if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                                                                String configName = configsCB.getSelectedItem().toString();
                                                                options.setProperty(Constants.OPTION_CONFIG_NAME, configName);
                                                                options.setProperty(Constants.OPTION_TRANSFER_TYPE, type.name());
                                                                
                                                                options.setProperty(Constants.OPTION_PROCESS_DATA_ENTRIES, "" + importDataEntriesCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_DATA_ENTRIES, "" + overwriteDataEntriesCB.isSelected()); 
                                                                options.setProperty(Constants.OPTION_PROCESS_DATA_ENTRY_CONFIGS, "" + importDataEntryConfigsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_DATA_ENTRY_CONFIGS, "" + overwriteDataEntryConfigsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_PROCESS_PREFERENCES, "" + importPreferencesCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_PREFERENCES, "" + overwritePreferencesCB.isSelected());
                                                                options.setProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG, "" + importGlobalConfigCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_GLOBAL_CONFIG, "" + overwriteGlobalConfigCB.isSelected());
                                                                options.setProperty(Constants.OPTION_PROCESS_TOOLS_DATA, "" + importToolsDataCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_TOOLS_DATA, "" + overwriteToolsDataCB.isSelected());
                                                                options.setProperty(Constants.OPTION_PROCESS_ICONS, "" + importIconsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_ICONS, "" + overwriteIconsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_PROCESS_ADDONS, "" + importAddOnsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_PROCESS_ADDON_CONFIGS, "" + importAddOnConfigsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_ADDON_CONFIGS, "" + overwriteAddOnConfigsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_PROCESS_IMPORT_EXPORT_CONFIGS, "" + importImportExportConfigsCB.isSelected());
                                                                options.setProperty(Constants.OPTION_OVERWRITE_IMPORT_EXPORT_CONFIGS, "" + overwriteImportExportConfigsCB.isSelected());

                                                                if (!Validator.isNullOrBlank(password)) {
                                                                    options.setProperty(Constants.OPTION_DATA_PASSWORD, password);
                                                                }
                                                                BackEnd.getInstance().storeImportConfiguration(configName, options);
                                                                processModel.addElement("Import configuration stored as '" + configName + "'");
                                                                autoscrollList(processList);
                                                            }
                                                        } catch (GeneralSecurityException gse) {
                                                            processModel.addElement("Failed to import data!");
                                                            processModel.addElement("Error details: It seems that you have typed wrong password...");
                                                            autoscrollList(processList);
                                                            label.setText("<html><font color=red>Data import - Failed</font></html>");
                                                            gse.printStackTrace(System.err);
                                                        } catch (Exception ex) {
                                                            processModel.addElement("Failed to import data!");
                                                            if (ex.getMessage() != null) {
                                                                processModel.addElement("Error details: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                                                            }
                                                            autoscrollList(processList);
                                                            label.setText("<html><font color=red>Data import - Failed</font></html>");
                                                            ex.printStackTrace(System.err);
                                                        }
                                                    }
                                                }
                                            } catch (Exception ex) {
                                                processModel.addElement("Failed to import data!");
                                                if (ex.getMessage() != null) {
                                                    processModel.addElement("Error details: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                                                }
                                                autoscrollList(processList);
                                                label.setText("<html><font color=red>Data import - Failed</font></html>");
                                                ex.printStackTrace(System.err);
                                            }
                                        }
                                    });
                                    importThread.start();
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    private ExportAction exportAction = new ExportAction();
    private class ExportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ExportAction() {
            putValue(Action.NAME, "export");
            putValue(Action.SHORT_DESCRIPTION, "export...");
            putValue(Action.SMALL_ICON, controlIcons.getIconExport());
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (!autoconfirmed("Save data before export", 
                        "Data need to be saved before export can be performed. Save now and proceed with export?")) {
                    return;
                }
                // store first
                store();
                // now proceed with export
                final JComboBox configsCB = new JComboBox();
                configsCB.addItem(Constants.EMPTY_STR);
                for (String configName : BackEnd.getInstance().getExportConfigurations().keySet()) {
                    configsCB.addItem(configName);
                }
                final JButton delButt = new JButton("Delete");
                delButt.setEnabled(false);
                delButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String name = (String) configsCB.getSelectedItem();
                            BackEnd.getInstance().removeExportConfiguration(name);
                            configsCB.removeItem(name);
                        } catch (Exception ex) {
                            displayErrorMessage("Failed to delete selected export-configuration!", ex);
                        }
                    }
                });
                final JButton renButt = new JButton("Rename");
                renButt.setEnabled(false);
                renButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String oldName = (String) configsCB.getSelectedItem();
                            String newName = JOptionPane.showInputDialog(FrontEnd.this, "New name:", oldName);
                            if (!Validator.isNullOrBlank(newName)) {
                                BackEnd.getInstance().renameExportConfiguration(oldName, newName);
                                configsCB.removeItem(oldName);
                                configsCB.addItem(newName);
                                configsCB.setSelectedItem(newName);
                            }
                        } catch (Exception ex) {
                            displayErrorMessage("Failed to rename selected export-configuration!", ex);
                        }
                    }
                });
                configsCB.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent e) {
                        if (!Constants.EMPTY_STR.equals(configsCB.getSelectedItem())) {
                            delButt.setEnabled(true);
                            renButt.setEnabled(true);
                        } else {
                            delButt.setEnabled(false);
                            renButt.setEnabled(false);
                        }
                    }
                });
                JPanel p = new JPanel(new BorderLayout());
                p.add(configsCB, BorderLayout.CENTER);
                JPanel pb = new JPanel(new GridLayout(1, 2));
                pb.add(renButt);
                pb.add(delButt);
                p.add(pb, BorderLayout.SOUTH);
                Component[] c = new Component[] {
                        new JLabel("Choose existing export configuration to use,"),
                        new JLabel("or leave just press enter for custom export."),
                        p          
                };
                opt = JOptionPane.showConfirmDialog(FrontEnd.this, c, "Export", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    final DataCategory data = collectData();
                    final Collection<UUID> selectedEntries = new LinkedList<UUID>();
                    final Collection<UUID> selectedRecursiveEntries = new LinkedList<UUID>();

                    if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                        try {
                            final JPanel panel = new JPanel(new BorderLayout());
                            final JLabel processLabel = new JLabel("Exporting data...");
                            panel.add(processLabel, BorderLayout.CENTER);
                            final JLabel label = new JLabel("Export data");
                            displayBottomPanel(label, panel);
                            Thread exportThread = new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        final Properties props = BackEnd.getInstance().getExportConfigurations().get(configsCB.getSelectedItem().toString());
                                        String exportAllStr = props.getProperty(Constants.OPTION_EXPORT_ALL);
                                        if (Validator.isNullOrBlank(exportAllStr) || !Boolean.valueOf(exportAllStr)) {
                                            String idsStr = props.getProperty(Constants.OPTION_SELECTED_IDS);
                                            if (!Validator.isNullOrBlank(idsStr)) {
                                                String[] ids = idsStr.split(Constants.PROPERTY_VALUES_SEPARATOR);
                                                for (String id : ids) {
                                                    selectedEntries.add(UUID.fromString(id));
                                                }
                                            }
                                            idsStr = props.getProperty(Constants.OPTION_SELECTED_RECURSIVE_IDS);
                                            if (!Validator.isNullOrBlank(idsStr)) {
                                                String[] ids = idsStr.split(Constants.PROPERTY_VALUES_SEPARATOR);
                                                for (String id : ids) {
                                                    selectedRecursiveEntries.add(UUID.fromString(id));
                                                }
                                            }
                                            filterData(data, selectedEntries, selectedRecursiveEntries);
                                        }
                                        final String password = props.getProperty(Constants.OPTION_DATA_PASSWORD) != null ? props.getProperty(Constants.OPTION_DATA_PASSWORD) : Constants.EMPTY_STR;
                                        File file = BackEnd.getInstance().exportData(
                                                data, 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_PREFERENCES)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_DATA_ENTRY_CONFIGS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ONLY_RELATED_DATA_ENTRY_CONFIGS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_TOOLS_DATA)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ICONS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ONLY_RELATED_ICONS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ADDONS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ADDON_CONFIGS)),
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_IMPORT_EXPORT_CONFIGS)),
                                                password);
                                        TRANSFER_TYPE type = TRANSFER_TYPE.valueOf(props.getProperty(Constants.OPTION_TRANSFER_TYPE));
                                        Transferrer transferrer = Transferrer.getInstance(type);
                                        byte[] exportedData = FSUtils.readFile(file);
                                        transferrer.doExport(exportedData, props);
                                        label.setText("<html><font color=green>Data export - Completed</font></html>");
                                        processLabel.setText("Data have been successfully exported.");
                                        displayStatusBarMessage("export done");
                                    } catch (Exception ex) {
                                        if (ex.getMessage() != null) {
                                            processLabel.setText("<html><font color=red>Failed to export data! Error details: " + ex.getClass().getSimpleName() + ": " + ex.getMessage() + "</font></html>");
                                        }
                                        ex.printStackTrace(System.err);
                                    }
                                }
                            });
                            exportThread.start();
                        } catch (Exception ex) {
                            displayErrorMessage("Failed to export data!", ex);
                        }
                    } else {
                        final JTree dataTree;
                        final CheckTreeManager checkTreeManager;
                        if (!data.getData().isEmpty()) {
                            dataTree = buildDataTree(data);
                            checkTreeManager = new CheckTreeManager(dataTree);
                            checkTreeManager.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener(){
                                public void valueChanged(TreeSelectionEvent e) {
                                    TreePath selectedPath = e.getNewLeadSelectionPath();
                                    TreePath treeSelPath = dataTree.getSelectionPath();
                                    if (selectedPath != null && selectedPath.equals(treeSelPath)) {
                                        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) selectedPath.getLastPathComponent());
                                        if (dataTree.isCollapsed(selectedPath) && !node.isLeaf()) {
                                            boolean isSelected = checkTreeManager.getSelectionModel().isPathSelected(selectedPath, true);
                                            if (isSelected) {
                                                Collection<DefaultMutableTreeNode> childs = new LinkedList<DefaultMutableTreeNode>();
                                                for (int i = 0; i < node.getChildCount(); i++) {
                                                    childs.add((DefaultMutableTreeNode) node.getChildAt(i));
                                                }
                                                categoriesToExportRecursively.put(node, childs);
                                                node.removeAllChildren();
                                            }
                                        }
                                    }
                                    if (treeSelPath != null) {
                                        boolean isSelected = checkTreeManager.getSelectionModel().isPathSelected(treeSelPath, true);
                                        if (!isSelected) {
                                            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) treeSelPath.getLastPathComponent());
                                            if (dataTree.isCollapsed(treeSelPath) && node.isLeaf()) {
                                                Collection<DefaultMutableTreeNode> childs = categoriesToExportRecursively.get(node);
                                                if (childs != null) {
                                                    for (DefaultMutableTreeNode child : childs) {
                                                        node.add(child);
                                                    }
                                                    categoriesToExportRecursively.remove(node);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            dataTree = null;
                            checkTreeManager = null;
                        }
                        final JCheckBox exportPreferencesCB = new JCheckBox("Export preferences"); 
                        final JCheckBox exportGlobalConfigCB = new JCheckBox("Export global config"); 
                        final JCheckBox exportDataEntryConfigsCB = new JCheckBox("Export data entry configs"); 
                        final JCheckBox exportOnlyRelatedDataEntryConfigsCB = new JCheckBox("Export data entry configs related to exported data entries only"); 
                        createDependentCheckboxChangeListener(exportDataEntryConfigsCB, exportOnlyRelatedDataEntryConfigsCB);
                        final JCheckBox exportToolsDataCB = new JCheckBox("Export tools data"); 
                        final JCheckBox exportIconsCB = new JCheckBox("Export icons");
                        final JCheckBox exportOnlyRelatedIconsCB = new JCheckBox("Export icons related to exported data entries only");
                        createDependentCheckboxChangeListener(exportIconsCB, exportOnlyRelatedIconsCB);
                        final JCheckBox exportAddOnsCB = new JCheckBox("Export addons"); 
                        final JCheckBox exportAddOnConfigsCB = new JCheckBox("Export addon configs");
                        final JCheckBox exportImportExportConfigsCB = new JCheckBox("Export import/export configs");
                        final JLabel passwordL1 = new JLabel("Encrypt exported data using password:");
                        final JPasswordField passwordTF1 = new JPasswordField();
                        final String cpText = "Confirm password:";
                        final JLabel passwordL2 = new JLabel(cpText);
                        final JPasswordField passwordTF2 = new JPasswordField();
                        passwordTF2.addCaretListener(new CaretListener(){
                            public void caretUpdate(CaretEvent e) {
                                if (!Arrays.equals(passwordTF1.getPassword(), passwordTF2.getPassword())) {
                                    passwordL2.setText(cpText + " [INVALID]");
                                    passwordL2.setForeground(Color.RED);
                                } else {
                                    passwordL2.setText(cpText + " [DONE]");
                                    passwordL2.setForeground(Color.BLUE);
                                }
                            }
                        });
                        Component[] comps = new Component[]{
                                exportPreferencesCB, 
                                exportGlobalConfigCB, 
                                exportDataEntryConfigsCB,
                                exportOnlyRelatedDataEntryConfigsCB,
                                exportToolsDataCB, 
                                exportIconsCB,
                                exportOnlyRelatedIconsCB,
                                exportAddOnsCB, 
                                exportAddOnConfigsCB,
                                exportImportExportConfigsCB,
                                dataTree != null ? recursiveExportInfoLabel : null,
                                dataTree != null ? new JScrollPane(dataTree) : null,
                                passwordL1,
                                passwordTF1,
                                passwordL2,
                                passwordTF2
                        };
                        opt = JOptionPane.showConfirmDialog(
                                FrontEnd.this, 
                                comps,
                                "Export data",
                                JOptionPane.OK_CANCEL_OPTION);
                        if (opt == JOptionPane.OK_OPTION) {
                            if (!Arrays.equals(passwordTF1.getPassword(), passwordTF2.getPassword())) {
                                throw new Exception("Password confirmation failure!");
                            }
                            final JPanel panel = new JPanel(new BorderLayout());
                            final DefaultListModel processModel = new DefaultListModel();
                            final JList processList = new JList(processModel);
                            panel.add(processList, BorderLayout.CENTER);
                            final JLabel label = new JLabel("Data export");
                            processModel.addElement("Compressing data to be exported...");
                            displayBottomPanel(label, panel);
                            autoscrollList(processList);
                            Thread exportThread = new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        boolean exportAll = false;
                                        if (checkTreeManager != null) {
                                            TreePath[] checkedPaths = checkTreeManager.getSelectionModel().getSelectionPaths();
                                            if (checkedPaths != null) {
                                                Iterator<DefaultMutableTreeNode> it = categoriesToExportRecursively.keySet().iterator();
                                                while (it.hasNext()) {
                                                    DefaultMutableTreeNode node = it.next();
                                                    if (node.isRoot()) {
                                                        exportAll = true;
                                                        break;
                                                    } else {
                                                        selectedRecursiveEntries.add(nodeEntries.get(node).getId());
                                                    }
                                                }
                                                if (!exportAll) {
                                                    for (TreePath tp : checkedPaths) {
                                                        DefaultMutableTreeNode lastNodeInPath = (DefaultMutableTreeNode) tp.getLastPathComponent();
                                                        for (Object o : tp.getPath()) {
                                                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                                                            Recognizable entry = nodeEntries.get(node);
                                                            if (entry != null) {
                                                                selectedEntries.add(entry.getId());
                                                            }
                                                            if (node.equals(lastNodeInPath)) {
                                                                selectDescenantEntries(node, selectedEntries);
                                                            }
                                                        }
                                                    }
                                                    filterData(data, selectedEntries, selectedRecursiveEntries);
                                                }
                                            }
                                        }
                                        exportFile = BackEnd.getInstance().exportData(
                                                data, 
                                                exportPreferencesCB.isSelected(), 
                                                exportGlobalConfigCB.isSelected(), 
                                                exportDataEntryConfigsCB.isSelected(),
                                                exportOnlyRelatedDataEntryConfigsCB.isSelected(),
                                                exportToolsDataCB.isSelected(), 
                                                exportIconsCB.isSelected(), 
                                                exportOnlyRelatedIconsCB.isSelected(),
                                                exportAddOnsCB.isSelected(), 
                                                exportAddOnConfigsCB.isSelected(),
                                                exportImportExportConfigsCB.isSelected(),
                                                new String(passwordTF1.getPassword()));
                                        processModel.addElement("Data to be exported have been successfully compressed.");
                                        autoscrollList(processList);
                                        JComboBox cb = new JComboBox();
                                        for (TRANSFER_TYPE type : Transferrer.TRANSFER_TYPE.values()) {
                                            cb.addItem(type);
                                        }
                                        opt = JOptionPane.showConfirmDialog(FrontEnd.this, cb, "Choose export type", JOptionPane.OK_CANCEL_OPTION);
                                        if (opt != JOptionPane.OK_OPTION) {
                                            hideBottomPanel();
                                        } else {    
                                            TRANSFER_TYPE type = (TRANSFER_TYPE) cb.getSelectedItem();
                                            final Properties options = displayTransferOptionsDialog(type, DATA_OPERATION_TYPE.EXPORT);
                                            if (options != null) {
                                                if (options.isEmpty()) {
                                                    hideBottomPanel();
                                                    throw new Exception("Export target options are missing! Export canceled.");
                                                } else {
                                                    try {
                                                        final Transferrer transferrer = Transferrer.getInstance(type);
                                                        final byte[] exportedData = FSUtils.readFile(exportFile);
                                                        processModel.addElement("Data is being transferred...");
                                                        autoscrollList(processList);
                                                        transferrer.doExport(exportedData, options);
                                                        label.setText("<html><font color=green>Data export - Completed</font></html>");
                                                        processModel.addElement("Data have been successfully transferred.");
                                                        autoscrollList(processList);
                                                        displayStatusBarMessage("export done");
                                                        configsCB.setEditable(true);
                                                        Component[] c = new Component[] {
                                                                new JLabel("Data have been successfully exported."),
                                                                new JLabel("If you want to save this export configuration,"),
                                                                new JLabel("input a name for it (or select existing one to overwrite):"),
                                                                configsCB          
                                                        };
                                                        JOptionPane.showMessageDialog(FrontEnd.this, c);
                                                        if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                                                            String configName = configsCB.getSelectedItem().toString();
                                                            options.setProperty(Constants.OPTION_CONFIG_NAME, configName);
                                                            options.setProperty(Constants.OPTION_TRANSFER_TYPE, type.name());
                                                            options.setProperty(Constants.OPTION_PROCESS_PREFERENCES, "" + exportPreferencesCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG, "" + exportGlobalConfigCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_DATA_ENTRY_CONFIGS, "" + exportDataEntryConfigsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_ONLY_RELATED_DATA_ENTRY_CONFIGS, "" + exportOnlyRelatedDataEntryConfigsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_TOOLS_DATA, "" + exportToolsDataCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_ICONS, "" + exportIconsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_ONLY_RELATED_ICONS, "" + exportOnlyRelatedIconsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_ADDONS, "" + exportAddOnsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_ADDON_CONFIGS, "" + exportAddOnConfigsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_IMPORT_EXPORT_CONFIGS, "" + exportImportExportConfigsCB.isSelected());
                                                            String password = new String(passwordTF1.getPassword());
                                                            if (!Validator.isNullOrBlank(password)) {
                                                                options.setProperty(Constants.OPTION_DATA_PASSWORD, password);
                                                            }
                                                            if (exportAll) {
                                                                options.setProperty(Constants.OPTION_EXPORT_ALL, "" + true);
                                                            } else {
                                                                if (!selectedEntries.isEmpty()) {
                                                                    StringBuffer ids = new StringBuffer();
                                                                    Iterator<UUID> it = selectedEntries.iterator();
                                                                    while (it.hasNext()) {
                                                                        ids.append(it.next());
                                                                        if (it.hasNext()) {
                                                                            ids.append(Constants.PROPERTY_VALUES_SEPARATOR);
                                                                        }
                                                                    }
                                                                    options.setProperty(Constants.OPTION_SELECTED_IDS, ids.toString());
                                                                }
                                                                if (!selectedRecursiveEntries.isEmpty()) {
                                                                    StringBuffer ids = new StringBuffer();
                                                                    Iterator<UUID> it = selectedRecursiveEntries.iterator();
                                                                    while (it.hasNext()) {
                                                                        ids.append(it.next());
                                                                        if (it.hasNext()) {
                                                                            ids.append(Constants.PROPERTY_VALUES_SEPARATOR);
                                                                        }
                                                                    }
                                                                    options.setProperty(Constants.OPTION_SELECTED_RECURSIVE_IDS, ids.toString());
                                                                }
                                                            }
                                                            BackEnd.getInstance().storeExportConfiguration(configName, options);
                                                            processModel.addElement("Export configuration stored as '" + configName + "'");
                                                            autoscrollList(processList);
                                                        }
                                                    } catch (Exception ex) {
                                                        processModel.addElement("Failed to export data!");
                                                        if (ex.getMessage() != null) {
                                                            processModel.addElement("Error details: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                                                        }
                                                        autoscrollList(processList);
                                                        label.setText("<html><font color=red>Data export - Failed</font></html>");
                                                        ex.printStackTrace(System.err);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        processModel.addElement("Failed to export data!");
                                        if (ex.getMessage() != null) {
                                            processModel.addElement("Error details: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                                        }
                                        autoscrollList(processList);
                                        label.setText("<html><font color=red>Data export - Failed</font></html>");
                                        ex.printStackTrace(System.err);
                                    }
                                }
                            });
                            exportThread.start();
                        }
                    }
                }
            } catch (Throwable t) {
                displayErrorMessage(t);
            }
        }
    };
    
    @SuppressWarnings("unchecked")
    private void selectDescenantEntries(DefaultMutableTreeNode node, Collection<UUID> selectedEntries) {
        if (node.getChildCount() != 0) {
            Enumeration<DefaultMutableTreeNode> childs = node.children();
            while (childs.hasMoreElements()) {
                DefaultMutableTreeNode childNode = childs.nextElement();
                Recognizable childEntry = nodeEntries.get(childNode);
                if (childEntry != null) {
                    selectedEntries.add(childEntry.getId());
                    selectDescenantEntries(childNode, selectedEntries);
                }
            }
        }
    }
    
    private void filterData(DataCategory data, Collection<UUID> filterEntries, Collection<UUID> selectedRecursiveEntries) {
        Collection<Recognizable> initialData = new ArrayList<Recognizable>(data.getData());
        for (Recognizable r : initialData) {
            if (!filterEntries.contains(r.getId())) {
                data.removeDataItem(r);
            } else if (r instanceof DataCategory) {
                if (!selectedRecursiveEntries.contains(r.getId())) {
                    filterData((DataCategory) r, filterEntries, selectedRecursiveEntries);
                }
            }
        }
    }
    
    public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;
        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Recognizable r = nodeEntries.get(node);
            if (r != null && r.getIcon() != null) {
                setIcon(r.getIcon());
            }
            return this;
        }
    }
    
    private JTree buildDataTree(DataCategory data) throws Throwable {
        nodeEntries = new HashMap<DefaultMutableTreeNode, Recognizable>();
        categoriesToExportRecursively = new HashMap<DefaultMutableTreeNode, Collection<DefaultMutableTreeNode>>();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(Constants.DATA_TREE_ROOT_NODE_CAPTION);
        DefaultTreeModel model = new DefaultTreeModel(root);
        final JTree dataTree = new JTree(model);
        buildDataTree(root, data);
        expandTreeNodes(dataTree, root);
        dataTree.setCellRenderer(new CustomTreeCellRenderer());
        return dataTree;
    }
    
    private void buildDataTree(DefaultMutableTreeNode node, DataCategory data) throws Throwable {
        for (Recognizable item : data.getData()) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
            if (item instanceof DataEntry) {
                DataEntry de = (DataEntry) item;
                childNode.setUserObject(de.getCaption());
                nodeEntries.put(childNode, de);
                node.add(childNode);
            } else if (item instanceof DataCategory) {
                DataCategory dc = (DataCategory) item;
                childNode.setUserObject(dc.getCaption());
                nodeEntries.put(childNode, dc);
                node.add(childNode);
                buildDataTree(childNode, dc);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void expandTreeNodes(JTree tree, DefaultMutableTreeNode node) {
        if (node.getChildCount() != 0) {
            tree.expandPath(new TreePath(node.getPath()));
            Enumeration<DefaultMutableTreeNode> e = node.children();
            while (e.hasMoreElements()) {
                DefaultMutableTreeNode n = e.nextElement();
                expandTreeNodes(tree, n);
            }
        }
    }
    
    // TODO [P2] add special variables syntax ? (to be able to backup using date-based filenames for example)
    private Properties displayTransferOptionsDialog(TRANSFER_TYPE transferType, DATA_OPERATION_TYPE operationType) {
        Properties options = null;
        switch (transferType) {
        case LOCAL:
            ZipFileChooser zfc = new ZipFileChooser();
            int rVal = 0;
            switch (operationType) {
            case IMPORT:
                rVal = zfc.showOpenDialog(FrontEnd.this);
                break;
            case EXPORT:
                rVal = zfc.showSaveDialog(FrontEnd.this);
                break;
            }
            if (rVal == JFileChooser.APPROVE_OPTION) {
                options = new Properties();
                String filePath = zfc.getSelectedFile().getAbsolutePath();
                if (DATA_OPERATION_TYPE.EXPORT.equals(operationType) && !filePath.matches(Constants.ZIP_FILE_PATTERN)) {
                    filePath += ".zip";
                }
                options.setProperty(Constants.TRANSFER_OPTION_FILEPATH, filePath);
            }
            break;
        case FTP:
            JLabel serverL = new JLabel("FTP Server (domain name or IP, including port if using non-default one)");
            JTextField serverTF = new JTextField();
            JLabel filepathL = new JLabel("Path to import file on server");
            JTextField filepathTF = new JTextField();
            JLabel usernameL = new JLabel("Username to login");
            JTextField usernameTF = new JTextField();
            JLabel passwordL = new JLabel("Password to login");
            JTextField passwordTF = new JPasswordField();
            int opt = JOptionPane.showConfirmDialog(
                    FrontEnd.this, 
                    new Component[]{
                            serverL,
                            serverTF,
                            filepathL,
                            filepathTF,
                            usernameL,
                            usernameTF,
                            passwordL,
                            passwordTF
                    }, 
                    "FTP transfer options", 
                    JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                options = new Properties();
                String text = serverTF.getText();
                if (!Validator.isNullOrBlank(text)) {
                    options.setProperty(Constants.TRANSFER_OPTION_SERVER, text);
                }
                text = filepathTF.getText();
                if (!Validator.isNullOrBlank(text)) {
                    options.setProperty(Constants.TRANSFER_OPTION_FILEPATH, text);
                }
                text = usernameTF.getText();
                if (!Validator.isNullOrBlank(text)) {
                    options.setProperty(Constants.TRANSFER_OPTION_USERNAME, text);
                }
                text = passwordTF.getText();
                if (!Validator.isNullOrBlank(text)) {
                    options.setProperty(Constants.TRANSFER_OPTION_PASSWORD, text);
                }
            }
            break;
        }
        return options;
    }

    private AddCategoryAction addCategoryAction = new AddCategoryAction();
    private class AddCategoryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AddCategoryAction() {
            putValue(Action.NAME, "addCategory");
            putValue(Action.SHORT_DESCRIPTION, "add category");
            putValue(Action.SMALL_ICON, controlIcons.getIconCategory());
        }
        
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
                    displayStatusBarMessage("category '" + categoryCaption + "' added");
                    currentTabPane = (JTabbedPane) categoryTabPane.getParent();
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };

    private Action saveAction = new SaveAction();
    private class SaveAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public SaveAction() {
            putValue(Action.NAME, "save");
            putValue(Action.SHORT_DESCRIPTION, "save");
            putValue(Action.SMALL_ICON, controlIcons.getIconSave());
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                store();
            } catch (Throwable t) {
                displayErrorMessage("Failed to save!", t);
            }
        }
    };
    
    private ExitAction exitAction = new ExitAction();
    private class ExitAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ExitAction() {
            putValue(Action.NAME, "exit");
            putValue(Action.SHORT_DESCRIPTION, "exit");
            putValue(Action.SMALL_ICON, controlIcons.getIconExit());
        }

        public void actionPerformed(ActionEvent evt) {
            exit();
        }
    };
    
    private BackToFirstAction backToFirstAction = new BackToFirstAction();
    private class BackToFirstAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public BackToFirstAction() {
            putValue(Action.NAME, "backToFirst");
            putValue(Action.SHORT_DESCRIPTION, "back to first");
            putValue(Action.SMALL_ICON, controlIcons.getIconFirst());
        }

        public void actionPerformed(ActionEvent evt) {
            navigationHistoryIndex = 0;
            UUID id = navigationHistory.get(navigationHistoryIndex);
            switchToVisualEntry(id, false);
        }
    };
    
    private BackAction backAction = new BackAction();
    private class BackAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public BackAction() {
            putValue(Action.NAME, "back");
            putValue(Action.SHORT_DESCRIPTION, "back");
            putValue(Action.SMALL_ICON, controlIcons.getIconPrevious());
        }

        public void actionPerformed(ActionEvent evt) {
            if (navigationHistoryIndex > 0) {
                navigationHistoryIndex--;
                UUID id = navigationHistory.get(navigationHistoryIndex);
                switchToVisualEntry(id, false);
            }
        }
    };
    
    private ForwardAction forwardAction = new ForwardAction();
    private class ForwardAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ForwardAction() {
            putValue(Action.NAME, "forward");
            putValue(Action.SHORT_DESCRIPTION, "forward");
            putValue(Action.SMALL_ICON, controlIcons.getIconNext());
        }

        public void actionPerformed(ActionEvent evt) {
            if (!navigationHistory.isEmpty() && navigationHistory.size() > navigationHistoryIndex + 1) {
                navigationHistoryIndex++;
                UUID id = navigationHistory.get(navigationHistoryIndex);
                switchToVisualEntry(id, false);
            }
        }
    };
    
    private ForwardToLastAction forwardToLastAction = new ForwardToLastAction();
    private class ForwardToLastAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ForwardToLastAction() {
            putValue(Action.NAME, "forwardToLast");
            putValue(Action.SHORT_DESCRIPTION, "forward to last");
            putValue(Action.SMALL_ICON, controlIcons.getIconLast());
        }

        public void actionPerformed(ActionEvent evt) {
            navigationHistoryIndex = navigationHistory.size() - 1;
            UUID id = navigationHistory.get(navigationHistoryIndex);
            switchToVisualEntry(id, false);
        }
    };
    
    private PreferencesAction preferencesAction = new PreferencesAction();
    private class PreferencesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public PreferencesAction() {
            putValue(Action.NAME, "preferences");
            putValue(Action.SHORT_DESCRIPTION, "preferences");
            putValue(Action.SMALL_ICON, controlIcons.getIconPreferences());
        }

        boolean prefsErr;
        
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            try {
                prefsErr = false;
                byte[] before = Preferences.getInstance().serialize();
                JPanel prefsPanel = null;
                Map<Component, Field> prefEntries = new HashMap<Component, Field>();
                Collection<JPanel> prefPanels = new LinkedList<JPanel>();
                Field[] fields = Preferences.class.getDeclaredFields();
                try {
                    for (final Field field : fields) {
                        PreferenceAnnotation prefAnn = field.getAnnotation(PreferenceAnnotation.class);
                        if (prefAnn != null) {
                            JPanel prefPanel = null;
                            Component prefControl = null;
                            String type = field.getType().getSimpleName().toLowerCase();
                            if ("string".equals(type)) {
                                prefPanel = new JPanel(new GridLayout(1, 2));
                                JLabel prefTitle = new JLabel(prefAnn.title() + Constants.BLANK_STR);
                                prefTitle.setToolTipText(prefAnn.description());
                                prefPanel.add(prefTitle);
                                if (field.isAnnotationPresent(PreferenceProtectAnnotation.class)) {
                                    prefControl = new JPasswordField();
                                } else {
                                    prefControl = new JTextField();
                                }
                                prefControl.setPreferredSize(new Dimension(150, 20));
                                PreferenceValidationAnnotation prefValAnn = field.getAnnotation(PreferenceValidationAnnotation.class);
                                if (prefValAnn != null) {
                                    // TODO [P3] optimization: there should be only one instance of certain validation class
                                    final PreferenceValidator<String> validator = (PreferenceValidator<String>) prefValAnn.validationClass().newInstance();
                                    final JTextComponent textControl = ((JTextComponent) prefControl);
                                    final Color normal = textControl.getForeground();
                                    textControl.addCaretListener(new CaretListener(){
                                        public void caretUpdate(CaretEvent e) {
                                            String value = textControl.getText();
                                            try {
                                                validator.validate(value);
                                                textControl.setForeground(normal);
                                                textControl.setToolTipText(null);
                                                prefsErr = prefsErr | false;
                                            } catch (Exception ex) {
                                                String errorMsg = "Invalid field value: " + ex.getMessage();
                                                textControl.setForeground(Color.RED);
                                                textControl.setToolTipText(errorMsg);
                                                prefsErr = prefsErr | true;
                                            }
                                        }
                                    });
                                    
                                }
                                String text = (String) field.get(Preferences.getInstance());
                                if (text == null) {
                                    text = Constants.EMPTY_STR;
                                }
                                ((JTextField) prefControl).setText(text);
                            } else if ("boolean".equals(type)) {
                                prefPanel = new JPanel(new GridLayout(1, 1));
                                prefControl = new JCheckBox(prefAnn.title());
                                ((JCheckBox) prefControl).setSelected(field.getBoolean(Preferences.getInstance()));
                            }
                            if (prefPanel != null && prefControl != null) {
                                prefEntries.put(prefControl, field);
                                prefPanel.add(prefControl);
                                prefPanels.add(prefPanel);
                            }
                        }
                    }
                    prefsPanel = new JPanel(new GridLayout(prefPanels.size(), 1));
                    for (JPanel prefPanel : prefPanels) {
                        prefsPanel.add(prefPanel);
                    }
                    for (Entry<Component, Field> pref : prefEntries.entrySet()) {
                        PreferenceEnableAnnotation prefEnableAnn = pref.getValue().getAnnotation(PreferenceEnableAnnotation.class);
                        if (prefEnableAnn != null) {
                            createPrefChangeListener(pref.getKey(), prefEnableAnn, prefEntries);
                        }
                    }
                } catch (Exception ex) {
                    displayErrorMessage("Failed to load preferences!", ex);
                }
                int opt = JOptionPane.showConfirmDialog(FrontEnd.this, prefsPanel, "Preferences", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    if (prefsErr) {
                        displayErrorMessage("Preference configuration contains error(s)! Preferences won't be saved, until that is fixed.");
                    } else {
                        try {
                            for (Entry<Component, Field> pref : prefEntries.entrySet()) {
                                if (pref.getKey() instanceof JTextField) {
                                    pref.getValue().set(Preferences.getInstance(), ((JTextField) pref.getKey()).getText());
                                } else if (pref.getKey() instanceof JCheckBox) {
                                    pref.getValue().setBoolean(Preferences.getInstance(), ((JCheckBox) pref.getKey()).isSelected());
                                } else if (pref.getKey() instanceof JComboBox) {
                                    pref.getValue().set(Preferences.getInstance(), ((JComboBox) pref.getKey()).getSelectedItem());
                                }
                            }
                            byte[] after = Preferences.getInstance().serialize();
                            if (!Arrays.equals(after, before)) {
                                BackEnd.getInstance().storePreferences();
                                applyPreferences();
                            }
                        } catch (Exception ex) {
                            displayErrorMessage("Failed to save preferences!", ex);
                        }
                    }
                }
            } catch (Exception ex) {
                displayErrorMessage(ex);
            }
        }
    };
    
    private void createPrefChangeListener(final Component c, final PreferenceEnableAnnotation ann, Map<Component, Field> prefEntries) {
        for (final Entry<Component, Field> pref : prefEntries.entrySet()) {
            if (ann.enabledByField().equals(pref.getValue().getName())) {
                if (pref.getKey() instanceof JTextField) {
                    c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JTextField) pref.getKey()).getText()));
                    if (!c.isEnabled() && c instanceof JCheckBox) {
                        ((JCheckBox) c).setSelected(false);
                    }
                    ((JTextField) pref.getKey()).addPropertyChangeListener("text", new PropertyChangeListener(){
                        public void propertyChange(PropertyChangeEvent evt) {
                            c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JTextField) pref.getKey()).getText()));
                            if (!c.isEnabled() && c instanceof JCheckBox) {
                                ((JCheckBox) c).setSelected(false);
                            }
                        }
                    });
                    ((JTextField) pref.getKey()).addPropertyChangeListener("enabled", new PropertyChangeListener(){
                        public void propertyChange(PropertyChangeEvent evt) {
                            c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JTextField) pref.getKey()).getText()));
                            if (!c.isEnabled() && c instanceof JCheckBox) {
                                ((JCheckBox) c).setSelected(false);
                            }
                        }
                    });
                } else if (pref.getKey() instanceof JCheckBox) {
                    c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals("" + ((JCheckBox) pref.getKey()).isSelected()));
                    if (!c.isEnabled() && c instanceof JCheckBox) {
                        ((JCheckBox) c).setSelected(false);
                    }
                    ((JCheckBox) pref.getKey()).addChangeListener(new ChangeListener(){
                        public void stateChanged(ChangeEvent e) {
                            c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals("" + ((JCheckBox) pref.getKey()).isSelected()));
                            if (!c.isEnabled() && c instanceof JCheckBox) {
                                ((JCheckBox) c).setSelected(false);
                            }
                        }
                    });
                    ((JCheckBox) pref.getKey()).addPropertyChangeListener("enabled", new PropertyChangeListener(){
                        public void propertyChange(PropertyChangeEvent evt) {
                            c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals("" + ((JCheckBox) pref.getKey()).isSelected()));
                            if (!c.isEnabled() && c instanceof JCheckBox) {
                                ((JCheckBox) c).setSelected(false);
                            }
                        }
                    });
                } else if (pref.getKey() instanceof JComboBox) {
                    c.setEnabled(pref.getKey().isEnabled() && ((JComboBox) pref.getKey()).getSelectedItem() != null && ann.enabledByValue().equals(((JComboBox) pref.getKey()).getSelectedItem().toString()));
                    if (!c.isEnabled() && c instanceof JCheckBox) {
                        ((JCheckBox) c).setSelected(false);
                    }
                    ((JComboBox) pref.getKey()).addItemListener(new ItemListener(){
                        public void itemStateChanged(ItemEvent e) {
                            c.setEnabled(pref.getKey().isEnabled() && ann.enabledByValue().equals(((JComboBox) pref.getKey()).getSelectedItem().toString()));
                            if (!c.isEnabled() && c instanceof JCheckBox) {
                                ((JCheckBox) c).setSelected(false);
                            }
                        }
                    });
                    ((JComboBox) pref.getKey()).addPropertyChangeListener("enabled", new PropertyChangeListener(){
                        public void propertyChange(PropertyChangeEvent evt) {
                            c.setEnabled(pref.getKey().isEnabled() && ((JComboBox) pref.getKey()).getSelectedItem() != null && ann.enabledByValue().equals(((JComboBox) pref.getKey()).getSelectedItem().toString()));
                            if (!c.isEnabled() && c instanceof JCheckBox) {
                                ((JCheckBox) c).setSelected(false);
                            }
                        }
                    });
                }
                break;
            }
        }
    }
    
    // TODO [P2] memory-usage optimization needed (instantiate extensions used in addons-management dialog only once)
    private ManageAddOnsAction manageAddOnsAction = new ManageAddOnsAction();
    private class ManageAddOnsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ManageAddOnsAction() {
            putValue(Action.NAME, "manageAddOns");
            putValue(Action.SHORT_DESCRIPTION, "manage add-ons");
            putValue(Action.SMALL_ICON, controlIcons.getIconAddOns());
        }

        private boolean modified;
        
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {

            try {
                // extensions
                boolean brokenFixed = false;
                JLabel extLabel = new JLabel("Extensions Management");
                final DefaultTableModel extModel = new DefaultTableModel() {
                    private static final long serialVersionUID = 1L;
                    public boolean isCellEditable(int rowIndex, int mColIndex) {
                        return false;
                    }
                };
                final JTable extList = new JTable(extModel);
                final TableRowSorter<TableModel> extSorter = new TableRowSorter<TableModel>(extModel);
                extSorter.setSortsOnUpdates(true);
                extList.setRowSorter(extSorter);
                extModel.addColumn("Name");
                extModel.addColumn("Version");
                extModel.addColumn("Author");
                extModel.addColumn("Description");
                final Map<String, String> extDetails = new HashMap<String, String>();
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
                            String detailsInfo = extAnn.details();
                            if (!Validator.isNullOrBlank(detailsInfo)) {
                                extDetails.put(extClass.getSimpleName(), detailsInfo);
                            }
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
                        displayAddOnsScreenErrorMessage("Extension [ " + extensionName + " ] is broken and will be uninstalled!", t);
                        // ... and try uninstall broken extension...
                        try {
                            BackEnd.getInstance().uninstallExtension(extension);
                            displayAddOnsScreenMessage("Broken extension [ " + extensionName + " ] has been uninstalled");
                            brokenFixed = true;
                        } catch (Exception ex2) {
                            // ... if unsuccessfully - inform user about that, do nothing else
                            displayAddOnsScreenErrorMessage("Error occured while uninstalling broken extension [ " + extensionName + " ]!" + Constants.NEW_LINE + ex2);
                        }
                    }
                }
                for (Entry<String, String> extensionEntry : BackEnd.getInstance().getNewExtensions().entrySet()) {
                    String extension = extensionEntry.getKey().replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                    extModel.addRow(new Object[]{extension, null, null, extensionEntry.getValue()});
                }
                JButton extDetailsButt = new JButton("Show Extension's details");
                extDetailsButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extList.getSelectedRowCount() == 1) {
                            try {
                                String version = (String) extList.getValueAt(extList.getSelectedRow(), 1);
                                if (Validator.isNullOrBlank(version)) {
                                    displayAddOnsScreenMessage(
                                            "Detailed information about this extension can not be shown yet." + Constants.NEW_LINE +
                                            "Restart Bias first.");
                                } else {
                                    String extension = (String) extList.getValueAt(extList.getSelectedRow(), 0);
                                    String detailsInfo = extDetails.get(extension);
                                    if (detailsInfo != null) {
                                        JOptionPane.showMessageDialog(dialog, getDetailsPane(detailsInfo), extension + " :: Extension's details", JOptionPane.INFORMATION_MESSAGE);
                                    } else {
                                        displayAddOnsScreenMessage("No detailed information provided with this extension.");
                                    }
                                }
                            } catch (Exception ex) {
                                displayAddOnsScreenErrorMessage(ex);
                            }
                        } else {
                            displayAddOnsScreenMessage("Please, choose only one extension from the list");
                        }
                    }
                });
                JButton extConfigButt = new JButton("Configure selected");
                extConfigButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extList.getSelectedRowCount() == 1) {
                            try {
                                String version = (String) extList.getValueAt(extList.getSelectedRow(), 1);
                                if (Validator.isNullOrBlank(version)) {
                                    displayAddOnsScreenMessage(
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
                                displayAddOnsScreenErrorMessage(ex);
                            }
                        } else {
                            displayAddOnsScreenMessage("Please, choose only one extension from the list");
                        }
                    }
                });
                JButton extInstButt = new JButton("Install more");
                extInstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (extensionFileChooser.showOpenDialog(FrontEnd.this) == JFileChooser.APPROVE_OPTION) {
                            Splash.showSplash(Splash.SPLASH_IMAGE_INSTALL, dialog);
                            Thread installThread = new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        for (File file : extensionFileChooser.getSelectedFiles()) {
                                            String installedExt = BackEnd.getInstance().installExtension(file);
                                            installedExt = installedExt.replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                                            int idx = findDataRowIndex(extModel, 0, installedExt);
                                            if (idx != -1) {
                                                extModel.removeRow(idx);
                                                extModel.insertRow(idx, new Object[]{installedExt, null, null, Constants.COMMENT_ADDON_UPDATED});
                                            } else {
                                                extModel.addRow(new Object[]{installedExt, null, null, Constants.COMMENT_ADDON_INSTALLED});
                                            }
                                            modified = true;
                                        }
                                    } catch (Throwable t) {
                                        displayAddOnsScreenErrorMessage(t);
                                    } finally {
                                        Splash.hideSplash();
                                    }
                                }
                            });
                            installThread.start();
                        }
                    }
                });
                JButton extUninstButt = new JButton("Uninstall selected");
                extUninstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (extList.getSelectedRowCount() != 0) {
                                if (confirmedUninstall()) {
                                    int idx;
                                    while ((idx = extList.getSelectedRow()) != -1) {
                                        String extension = (String) extList.getValueAt(extList.getSelectedRow(), 0);
                                        String extFullClassName = 
                                            Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                    + extension + Constants.PACKAGE_PATH_SEPARATOR + extension;
                                        BackEnd.getInstance().uninstallExtension(extFullClassName);
                                        idx = extSorter.convertRowIndexToModel(idx);
                                        extModel.removeRow(idx);
                                        modified = true;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            displayAddOnsScreenErrorMessage(ex);
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
                lafSorter.setSortsOnUpdates(true);
                lafList.setRowSorter(lafSorter);
                lafModel.addColumn("Name");
                lafModel.addColumn("Version");
                lafModel.addColumn("Author");
                lafModel.addColumn("Description");
                lafModel.addRow(new Object[]{DEFAULT_LOOK_AND_FEEL,Constants.EMPTY_STR,Constants.EMPTY_STR,"Default Look-&-Feel"});
                final Map<String, String> lafDetails = new HashMap<String, String>();
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
                            String detailsInfo = lafAnn.details();
                            if (!Validator.isNullOrBlank(detailsInfo)) {
                                lafDetails.put(lafClass.getSimpleName(), detailsInfo);
                            }
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
                        displayAddOnsScreenErrorMessage("Look-&-Feel [ " + lafName + " ] is broken and will be uninstalled!", t);
                        // ... and try uninstall broken laf...
                        try {
                            BackEnd.getInstance().uninstallLAF(laf);
                            displayAddOnsScreenMessage("Broken Look-&-Feel [ " + lafName + " ] has been uninstalled");
                            brokenFixed = true;
                        } catch (Exception ex2) {
                            // ... if unsuccessfully - inform user about that, do nothing else
                            displayAddOnsScreenErrorMessage("Error occured while uninstalling broken Look-&-Feel [ " + lafName + " ]!" + Constants.NEW_LINE + ex2);
                        }
                    }
                }
                for (Entry<String, String> lafEntry : BackEnd.getInstance().getNewLAFs().entrySet()) {
                    String laf = lafEntry.getKey().replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                    lafModel.addRow(new Object[]{laf, null, null, lafEntry.getValue()});
                }
                JButton lafDetailsButt = new JButton("Show Look-&-Feel's details");
                lafDetailsButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (lafList.getSelectedRowCount() == 1) {
                            try {
                                String laf = (String) lafList.getValueAt(lafList.getSelectedRow(), 0);
                                if (DEFAULT_LOOK_AND_FEEL.equals(laf)) {
                                    displayAddOnsScreenMessage("This is a default native Java's cross-platform Look-&-Feel.");
                                } else {
                                    String version = (String) lafList.getValueAt(lafList.getSelectedRow(), 1);
                                    if (Validator.isNullOrBlank(version)) {
                                        displayAddOnsScreenMessage(
                                                "Detailed information about this look-&-feel can not be shown yet." + Constants.NEW_LINE +
                                                "Restart Bias first.");
                                    } else {
                                        String detailsInfo = lafDetails.get(laf);
                                        if (detailsInfo != null) {
                                            JOptionPane.showMessageDialog(dialog, getDetailsPane(detailsInfo), laf + " :: Look-&-Feel's details", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            displayAddOnsScreenMessage("No detailed information provided with this look-&-feel.");
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                displayAddOnsScreenErrorMessage(ex);
                            }
                        } else {
                            displayAddOnsScreenMessage("Please, choose only one extension from the list");
                        }
                    }
                });
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
                                    displayAddOnsScreenErrorMessage(t);
                                }
                            } else {
                                String version = (String) lafList.getValueAt(lafList.getSelectedRow(), 1);
                                if (Validator.isNullOrBlank(version)) {
                                    displayAddOnsScreenMessage(
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
                                        displayAddOnsScreenErrorMessage(t);
                                    }
                                }
                            }
                        } else {
                            displayAddOnsScreenMessage("Please, choose only one look-&-feel from the list");
                        }    
                    }
                });
                JButton lafInstButt = new JButton("Install more");
                lafInstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (lafFileChooser.showOpenDialog(FrontEnd.this) == JFileChooser.APPROVE_OPTION) {
                            Splash.showSplash(Splash.SPLASH_IMAGE_INSTALL, dialog);
                            Thread installThread = new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        for (File file : lafFileChooser.getSelectedFiles()) {
                                            String installedLAF = BackEnd.getInstance().installLAF(file);
                                            installedLAF = installedLAF.replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                                            int idx = findDataRowIndex(lafModel, 0, installedLAF);
                                            if (idx != -1) {
                                                lafModel.removeRow(idx);
                                                lafModel.addRow(new Object[]{installedLAF, null, null, Constants.COMMENT_ADDON_UPDATED});
                                            } else {
                                                lafModel.addRow(new Object[]{installedLAF, null, null, Constants.COMMENT_ADDON_INSTALLED});
                                            }
                                            modified = true;
                                        }
                                    } catch (Exception ex) {
                                        displayAddOnsScreenErrorMessage(ex);
                                    } finally {
                                        Splash.hideSplash();
                                    }
                                }
                            });
                            installThread.start();
                        }
                    }
                });
                JButton lafUninstButt = new JButton("Uninstall selected");
                lafUninstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (lafList.getSelectedRowCount() > 0) {
                                if (confirmedUninstall()) {
                                    String currentLAF = config.getProperty(Constants.PROPERTY_LOOK_AND_FEEL);
                                    int idx;
                                    while ((idx = lafList.getSelectedRow()) != -1) {
                                        String laf = (String) lafList.getValueAt(lafList.getSelectedRow(), 0);
                                        if (!DEFAULT_LOOK_AND_FEEL.equals(laf)) {
                                            String fullLAFClassName = 
                                                Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                        + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                                            BackEnd.getInstance().uninstallLAF(fullLAFClassName);
                                            idx = lafSorter.convertRowIndexToModel(idx);
                                            lafModel.removeRow(idx);
                                            // if look-&-feel that has been uninstalled was active one...
                                            if (laf.equals(currentLAF)) {
                                                //... unset it (default one will be used)
                                                config.remove(Constants.PROPERTY_LOOK_AND_FEEL);
                                            }
                                            modified = true;
                                        } else {
                                            displayAddOnsScreenErrorMessage("Default Look-&-Feel can not be uninstalled!");
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            displayAddOnsScreenErrorMessage(ex);
                        }
                    }
                });
                
                // icons
                JLabel icLabel = new JLabel("Icons Management");
                final DefaultListModel icModel = new DefaultListModel();
                final JList icList = new JList(icModel);
                for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                    icModel.addElement(icon);
                }
                JScrollPane jsp = new JScrollPane(icList);
                jsp.setPreferredSize(new Dimension(200,200));
                jsp.setMinimumSize(new Dimension(200,200));
                JButton addIconButt = new JButton("Add more");
                addIconButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (iconsFileChooser.showOpenDialog(FrontEnd.this) == JFileChooser.APPROVE_OPTION) {
                            Splash.showSplash(Splash.SPLASH_IMAGE_INSTALL, dialog);
                            Thread installThread = new Thread(new Runnable(){
                                public void run() {
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
                                            displayAddOnsScreenMessage("Icon(s) have been successfully installed!");
                                            icList.repaint();
                                        } else {
                                            displayAddOnsScreenErrorMessage("Nothing to install!");
                                        }
                                    } catch (Exception ex) {
                                        displayAddOnsScreenErrorMessage(ex);
                                    } finally {
                                        Splash.hideSplash();
                                    }
                                }
                            });
                            installThread.start();
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
                                displayAddOnsScreenMessage("Icon(s) have been successfully removed!");
                            }
                        } catch (Exception ex) {
                            displayAddOnsScreenErrorMessage(ex);
                        }
                    }
                });
                
                // dialog
                JTabbedPane addOnsPane = new JTabbedPane();

                JPanel extControlsPanel = new JPanel(new GridLayout(1,4));
                extControlsPanel.add(extDetailsButt);
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
                lafControlsPanel.add(lafDetailsButt);
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
                
                if (BackEnd.getInstance().unusedAddOnsFound()) {
                    JPanel advPanel = new JPanel(new BorderLayout());
                    JPanel cleanPanel = new JPanel(new GridLayout(2,1));
                    final JButton cleanButt = new JButton("Clean unused data and config files!");
                    JLabel cleanLabel = new JLabel(
                            "<html>" +
                            "<body>" +
                            "<div color=\"red\">" +
                            "NOTE: This will remove all unused data and configuration files<br>" +
                            "that were used by extensions/LAFs that are not currently installed.<br>" +
                            "Do that only if you don't plan to install these extensions/LAFs again<br>" +
                            "or want to reset their data/settings." +
                            "</div>" +
                            "</body>" +
                            "</html>");
                    cleanButt.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            BackEnd.getInstance().removeUnusedAddOnDataAndConfigFiles();
                            cleanButt.setText("Done!");
                            cleanButt.setEnabled(false);
                        }
                    });
                    cleanPanel.add(cleanButt);
                    cleanPanel.add(cleanLabel);
                    advPanel.add(cleanPanel, BorderLayout.NORTH);
                    addOnsPane.addTab("Advanced", controlIcons.getIconPreferences(), advPanel);
                }
                
                modified = false;
                JOptionPane op = new JOptionPane();
                op.setMessage(addOnsPane);
                op.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                dialog = op.createDialog(FrontEnd.this, "Manage Add-Ons");
                dialog.setVisible(true);

                if (modified || brokenFixed) {
                    displayMessage(RESTART_MESSAGE);
                    displayStatusBarMessage("add-ons configuration changed");
                }
                
            } catch (Throwable t) {
                displayErrorMessage(t);
            }
        	
        }
        
    };
    
    private int findDataRowIndex(DefaultTableModel model, int colIdx, String data) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (data.equals(model.getValueAt(i, colIdx))) {
                return i;
            }
        }
        return -1;
    }
    
    private JScrollPane getDetailsPane(String detailsInfo) {
        if (detailsPane == null) {
            detailsTextPane = new JTextPane();
            detailsTextPane.setEditable(false);
            detailsTextPane.setEditorKit(new HTMLEditorKit());
            detailsTextPane.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                        try {
                            AppManager.getInstance().handleAddress(e.getDescription());
                        } catch (Exception ex) {
                            FrontEnd.displayErrorMessage(ex);
                        }
                    }
                }
            });
            detailsPane = new JScrollPane(detailsTextPane);
        }
        detailsTextPane.setText(detailsInfo);
        return detailsPane;
    }
    
    private AboutAction displayAboutInfoAction = new AboutAction();
    private class AboutAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AboutAction() {
            putValue(Action.NAME, "about");
            putValue(Action.SHORT_DESCRIPTION, "about...");
            putValue(Action.SMALL_ICON, controlIcons.getIconAbout());
        }

        public void actionPerformed(ActionEvent evt) {
            JLabel title1Label = new JLabel("Bias Personal Information Manager, version 1.0.0");
            JLabel link1Label = new JLabel("<html><u><font color=blue>http://bias.sourceforge.net/");
            JLabel title2Label = new JLabel("(c) Roman Kasianenko, 2007");
        	JLabel link2Label = new JLabel("<html><u><font color=blue>http://kion.name/");
            JLabel title3Label = new JLabel("EtweeSoft (Software Development Organization)");
            JLabel link3Label = new JLabel("<html><u><font color=blue>http://etweesoft.org/");
        	link1Label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	link1Label.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						AppManager.getInstance().handleAddress("http://bias.sourceforge.net/");
					} catch (Exception ex) {
						// do nothing
					}
				}
        	});
        	link2Label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	link2Label.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        AppManager.getInstance().handleAddress("http://kion.name/");
                    } catch (Exception ex) {
                        // do nothing
                    }
                }
            });
            link3Label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            link3Label.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        AppManager.getInstance().handleAddress("http://etweesoft.org/");
                    } catch (Exception ex) {
                        // do nothing
                    }
                }
            });
            JOptionPane.showMessageDialog(
                    FrontEnd.this, 
                    new Component[]{
                            title1Label, link1Label, 
                            title2Label, link2Label,
                            title3Label, link3Label
                            });
        }
    };

}
