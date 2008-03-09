/**
 * Created on Oct 15, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import bias.Constants;
import bias.Preferences;
import bias.Splash;
import bias.Constants.TRANSFER_TYPE;
import bias.Preferences.PreferenceValidator;
import bias.annotation.PreferenceAnnotation;
import bias.annotation.PreferenceEnableAnnotation;
import bias.annotation.PreferenceProtectAnnotation;
import bias.annotation.PreferenceValidationAnnotation;
import bias.core.AddOnInfo;
import bias.core.BackEnd;
import bias.core.DataCategory;
import bias.core.DataEntry;
import bias.core.FileInfo;
import bias.core.Recognizable;
import bias.core.pack.Dependency;
import bias.core.pack.ObjectFactory;
import bias.core.pack.Pack;
import bias.core.pack.PackType;
import bias.core.pack.Repository;
import bias.event.AfterSaveEventListener;
import bias.event.BeforeExitEventListener;
import bias.event.BeforeSaveEventListener;
import bias.event.EventListener;
import bias.event.SaveEvent;
import bias.event.StartUpEventListener;
import bias.event.TransferEvent;
import bias.event.TransferEventListener;
import bias.extension.EntryExtension;
import bias.extension.Extension;
import bias.extension.ExtensionFactory;
import bias.extension.MissingExtensionInformer;
import bias.extension.ToolExtension;
import bias.extension.ToolRepresentation;
import bias.extension.TransferConfiguration;
import bias.extension.TransferExtension;
import bias.gui.VisualEntryDescriptor.ENTRY_TYPE;
import bias.skin.Skin;
import bias.skin.UIIcons;
import bias.utils.AppManager;
import bias.utils.ArchUtils;
import bias.utils.Downloader;
import bias.utils.FSUtils;
import bias.utils.FormatUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;
import bias.utils.VersionComparator;
import bias.utils.Downloader.DownloadListener;


/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    // TODO [P3] internationalization

    private static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_SKIN = "DefaultSkin";

    /**
     * Application icon
     */
    private static final ImageIcon ICON_APP = new ImageIcon(FrontEnd.class.getResource("/bias/res/app_icon.png"));

    private static final ImageIcon ICON_CLOSE = new ImageIcon(FrontEnd.class.getResource("/bias/res/close.png"));

    private static final ImageIcon ICON_PROCESS = new ImageIcon(FrontEnd.class.getResource("/bias/res/process.gif"));

    private static final String RESTART_MESSAGE = "Changes will take effect after Bias restart";
    
    private static final Placement[] PLACEMENTS = new Placement[] { 
            new Placement(JTabbedPane.TOP),
            new Placement(JTabbedPane.LEFT), 
            new Placement(JTabbedPane.RIGHT),
            new Placement(JTabbedPane.BOTTOM)
        };
    
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

    private static AddOnFilesChooser skinFileChooser = new AddOnFilesChooser();

    private static IconsFileChooser iconsFileChooser = new IconsFileChooser();
    
    private static FrontEnd instance;
    
    private static Map<String, ImageIcon> icons;
    
    private static Map<Class<? extends ToolExtension>, ToolExtension> tools;

    private static Map<Class<? extends TransferExtension>, TransferExtension> transferrers;

    private static Map<Class<? extends ToolExtension>, JPanel> indicatorAreas = new HashMap<Class<? extends ToolExtension>, JPanel>();

    // use default control icons initially
    private static UIIcons uiIcons = new UIIcons();

    private static Properties config;
    
    private static String activeSkin = null;
    
    private static Map<String, byte[]> initialSkinSettings = new HashMap<String, byte[]>();
    
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

    private boolean addOnsManagementDialogLocked = false;
    
    private String lastAddedEntryType = null;
    
    private Map<String, Integer> depCounters;
    
    private JTable onlineList;
    
    private DefaultTableModel extModel;
    
    private DefaultTableModel skinModel;

    private DefaultTableModel libModel;
    
    private DefaultTableModel icSetModel;
    
    private DefaultListModel icModel;
    
    private DefaultTableModel onlineModel;
    
    private JProgressBar onlineSingleProgressBar;
    
    private JProgressBar onlineTotalProgressBar;
    
    private JTable extList;
    
    private JTable skinList;
    
    private JTable icSetList;
    
    private JList icList;
    
    private JTable libsList;
    
    private JTabbedPane addOnsPane;

    private JProgressBar memUsageProgressBar = null;
    
    private JList statusBarMessagesList = null;

    private JFrame addOnsManagementDialog = null;
    
    private JButton closeAddOnsManagementDialogButt;
    
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

    private JButton jButton17 = null;

    private JButton jButton18 = null;

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

    private static boolean unusedAddOnDataAndConfigFilesCleanedUp = false;
    
    private static Map<String, Pack> availableOnlinePackages;
    
    private static Map<String, Pack> getAvailableOnlinePackages() {
        if (availableOnlinePackages == null) {
            availableOnlinePackages = new HashMap<String, Pack>();
        }
        return availableOnlinePackages;
    }
    
    private static Unmarshaller unmarshaller;

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        if (unmarshaller == null) {
            unmarshaller = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName()).createUnmarshaller();
        }
        return unmarshaller;
    }
    
    private static Map<Class<? extends StartUpEventListener>, StartUpEventListener> startUpEventListeners;
    public static void addStartUpEventListener(StartUpEventListener l) {
        if (startUpEventListeners == null) {
            startUpEventListeners = new HashMap<Class<? extends StartUpEventListener>, StartUpEventListener>();
        }
        addEventListener(startUpEventListeners, l);
    }
    public static void removeStartUpEventListener(StartUpEventListener l) {
        removeEventListener(startUpEventListeners, l);
    }
    private static void fireStartUpEvent() {
        if (startUpEventListeners != null) {
            for (StartUpEventListener l : startUpEventListeners.values()) {
                try {
                    l.onEvent();
                } catch (Throwable t) {
                    displayErrorMessage("start-up event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                }
            }
        }
    }
    
    private static Map<Class<? extends TransferEventListener>, TransferEventListener> transferEventListeners;
    public static void addTransferEventListener(TransferEventListener l) {
        if (transferEventListeners == null) {
            transferEventListeners = new HashMap<Class<? extends TransferEventListener>, TransferEventListener>();
        }
        addEventListener(transferEventListeners, l);
    }
    public static void removeTransferEventListener(TransferEventListener l) {
        removeEventListener(transferEventListeners, l);
    }
    private static void fireTransferEvent(TransferEvent e) {
        if (transferEventListeners != null) {
            for (TransferEventListener l : transferEventListeners.values()) {
                try {
                    l.onEvent(e);
                } catch (Throwable t) {
                    displayErrorMessage("transfer event listener (" + e.getTransferType().name() + ") '" + l.getClass().getSimpleName() + "' failed!", t);
                }
            }
        }
    }
    
    private static Map<Class<? extends BeforeSaveEventListener>, BeforeSaveEventListener> beforeSaveEventListeners;
    public static void addBeforeSaveEventListener(BeforeSaveEventListener l) {
        if (beforeSaveEventListeners == null) {
            beforeSaveEventListeners = new HashMap<Class<? extends BeforeSaveEventListener>, BeforeSaveEventListener>();
        }
        addEventListener(beforeSaveEventListeners, l);
    }
    public static void removeBeforeSaveEventListener(StartUpEventListener l) {
        removeEventListener(beforeSaveEventListeners, l);
    }
    private static void fireBeforeSaveEvent(SaveEvent e) {
        if (beforeSaveEventListeners != null) {
            for (BeforeSaveEventListener l : beforeSaveEventListeners.values()) {
                try {
                    l.onEvent(e);
                } catch (Throwable t) {
                    displayErrorMessage("before-save event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                }
            }
        }
    }
    
    private static Map<Class<? extends AfterSaveEventListener>, AfterSaveEventListener> afterSaveEventListeners;
    public static void addAfterSaveEventListener(AfterSaveEventListener l) {
        if (afterSaveEventListeners == null) {
            afterSaveEventListeners = new HashMap<Class<? extends AfterSaveEventListener>, AfterSaveEventListener>();
        }
        addEventListener(afterSaveEventListeners, l);
    }
    public static void removeAfterSaveEventListener(StartUpEventListener l) {
        removeEventListener(afterSaveEventListeners, l);
    }
    private static void fireAfterSaveEvent(SaveEvent e) {
        if (afterSaveEventListeners != null) {
            for (AfterSaveEventListener l : afterSaveEventListeners.values()) {
                try {
                    l.onEvent(e);
                } catch (Throwable t) {
                    displayErrorMessage("after-save event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                }
            }
        }
    }
    
    private static Map<Class<? extends BeforeExitEventListener>, BeforeExitEventListener> beforeExitEventListeners;
    public static void addBeforeExitEventListener(BeforeExitEventListener l) {
        if (beforeExitEventListeners == null) {
            beforeExitEventListeners = new HashMap<Class<? extends BeforeExitEventListener>, BeforeExitEventListener>();
        }
        addEventListener(beforeExitEventListeners, l);
    }
    public static void removeBeforeExitEventListener(StartUpEventListener l) {
        removeEventListener(beforeExitEventListeners, l);
    }
    private static void fireBeforeExitEvent() {
        if (beforeExitEventListeners != null) {
            for (BeforeExitEventListener l : beforeExitEventListeners.values()) {
                try {
                    l.onEvent();
                } catch (Throwable t) {
                    displayErrorMessage("before exit event listener '" + l.getClass().getSimpleName() + "' failed!", t);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void addEventListener(Map listeners, EventListener l) {
        if (listeners.get(l.getClass()) == null) {
            listeners.put(l.getClass(), l);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void removeEventListener(Map listeners, EventListener l) {
        if (listeners != null) {
            listeners.remove(l.getClass());
        }
    }
    
    /**
     * Default singleton's hidden constructor without parameters
     */
    private FrontEnd() {
        super();
        initialize();
    }

    public static void startup() {
        getInstance().displayStatusBarMessage("loaded & ready");
        if (Preferences.getInstance().startHidden) {
            showSysTrayIcon();
            if (!sysTrayIconVisible) {
                getInstance().setVisible(true);
            }
        } else {
            getInstance().setVisible(true);
        }
        fireStartUpEvent();
    }
    
    private static FrontEnd getInstance() {
        if (instance == null) {
            preInit();
            activateSkin();
            instance = new FrontEnd();
            instance.applyPreferences();
            representTools();
            initTransferrers();
            instance.performAutoUpdate();
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
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), backAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(backAction.getValue(Action.NAME), backAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), forwardAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(forwardAction.getValue(Action.NAME), forwardAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK), backToFirstAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(backToFirstAction.getValue(Action.NAME), backToFirstAction);
        
        getJPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK), forwardToLastAction.getValue(Action.NAME));
        getJPanel().getActionMap().put(forwardToLastAction.getValue(Action.NAME), forwardToLastAction);
        
    }
    
    private static boolean sysTrayIconNotSupportedInformed = false;
    private static void showSysTrayIcon() {
        if (!SystemTray.isSupported()) {
            if (sysTrayIconNotSupportedInformed == false) {
                displayErrorMessage("System tray API is not available on this platform!");
                sysTrayIconNotSupportedInformed = true;
            }
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
            Splash.hideSplash();
            displayErrorMessage(
                    "Bias has failed to load data!" + Constants.NEW_LINE +
                    "It seems that you have typed wrong password...", gse);
            BackEnd.getInstance().shutdown(-1);
        } catch (Throwable t) {
            Splash.hideSplash();
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
    
    private void performAutoUpdate() {
        if (Preferences.getInstance().enableAutoUpdate) {
            boolean update = false;
            if (Preferences.getInstance().autoUpdateInterval == 0) {
                update = true;
            } else {
                String timeStr = config.getProperty(Constants.PROPERTY_LAST_UPDATE_DATE);
                if (!Validator.isNullOrBlank(timeStr)) {
                    long lastUpdateTime = Long.valueOf(timeStr);
                    long currentTime = System.currentTimeMillis();
                    int interval = (int) ((currentTime - lastUpdateTime) / 1000 / 60 / 60 / 24);
                    // check if specified number of days from last update date have passed...
                    if (interval >= Preferences.getInstance().autoUpdateInterval) {
                        // ... and perform update, if yes
                        update = true;
                    }
                } else {
                    update = true;
                }
            }
            if (update) {
                new Thread(new Runnable(){
                    public void run() {
                        try {
                            // sleep for 5 minutes before update...
                            Thread.sleep(1000 * 60 * 5);
                            // ... then perform update
                            downloadAndInstallAllUpdates(new Runnable(){
                                public void run() {
                                    // remember last update date
                                    config.setProperty(Constants.PROPERTY_LAST_UPDATE_DATE, "" + System.currentTimeMillis());
                                    // inform user about update complete
                                    JOptionPane.showMessageDialog(
                                            getActiveWindow(), 
                                            "<html>Automatic update complete<br/><br/>" +
                                                    "<i>(Note: automatic update can be disabled via preferences option 'Enable automatic updates',<br>" +
                                                    "update interval can be adjusted via preferences option 'Automatic update interval')</i><html>");
                                }
                            });
                        } catch (InterruptedException ex) {
                            // ignore, update just won't be performed this time
                        }
                    }
                }).start();
            }
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
                            if (sysTrayIconVisible) {
                                getInstance().setVisible(false);
                            }
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
                        if (sysTrayIconVisible) {
                            getInstance().setVisible(false);
                        }
                    }
                }
            });
            
        } catch (Exception ex) {
            displayErrorMessage(ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void activateSkin() {
        String skin = config.getProperty(Constants.PROPERTY_SKIN);
        if (skin != null) {
            try {
                String skinFullClassName = Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + skin + Constants.PACKAGE_PATH_SEPARATOR + skin;
                Class<Skin> skinClass = (Class<Skin>) Class.forName(skinFullClassName);
                Skin skinInstance = skinClass.newInstance();
                byte[] skinSettings = BackEnd.getInstance().getAddOnSettings(skinFullClassName, PackType.SKIN);
                skinInstance.activate(skinSettings);
                // use control icons defined by Skin if available
                if (skinInstance.getUIIcons() != null) {
                    uiIcons = skinInstance.getUIIcons();
                }
                if (activeSkin == null) {
                    if (skin != null) {
                        activeSkin = skin;
                    } else {
                        activeSkin = DEFAULT_SKIN;
                    }
                }
            } catch (Throwable t) {
                activeSkin = DEFAULT_SKIN;
                config.remove(Constants.PROPERTY_SKIN);
                System.err.println(
                        "Current Skin '" + skin + "' failed to initialize!" + Constants.NEW_LINE +
                        "(Preferences will be auto-modified to use default Skin)" + Constants.NEW_LINE + 
                        "Error details: " + Constants.NEW_LINE);
                t.printStackTrace(System.err);
            }
        } else {
            activeSkin = DEFAULT_SKIN;
        }
    }
    
    private boolean setActiveSkin(String skin) throws Throwable {
        boolean skinChanged = false;
        String currentSkin = config.getProperty(Constants.PROPERTY_SKIN);
        if (skin != null) {
            String skinName = skin.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            if (!skinName.equals(currentSkin)) {
                config.put(Constants.PROPERTY_SKIN, skinName);
                configureSkin(skin);
                skinChanged = true;
            } else {
                skinChanged = configureSkin(skin);
            }
        } else if (currentSkin != null) {
            config.remove(Constants.PROPERTY_SKIN);
            skinChanged = true;
        }
        return skinChanged;
    }
    
    @SuppressWarnings("unchecked")
    private boolean configureSkin(String skin) throws Throwable {
        boolean skinChanged = false;
        if (skin != null) {
            Class<Skin> skinClass = (Class<Skin>) Class.forName(skin);
            Skin skinInstance = skinClass.newInstance();
            byte[] skinSettings = BackEnd.getInstance().getAddOnSettings(skin, PackType.SKIN);
            byte[] settings = skinInstance.configure(skinSettings);
            // store if differs from stored version
            if (!PropertiesUtils.deserializeProperties(settings).equals(PropertiesUtils.deserializeProperties(skinSettings))) {
                BackEnd.getInstance().storeAddOnSettings(skin, PackType.SKIN, settings);
            }
            // find out if differs from initial version
            byte[] initialSettings = initialSkinSettings.get(skin);
            if (initialSettings == null) {
                initialSkinSettings.put(skin, settings);
                initialSettings = settings;
            }
            skinChanged = !PropertiesUtils.deserializeProperties(settings).equals(PropertiesUtils.deserializeProperties(initialSettings));
        }
        return skinChanged;
    }
    
    @SuppressWarnings("unchecked")
    private void configureExtension(String extension, boolean showFirstTimeUsageMessage) throws Exception {
        if (extension != null) {
            String extName = extension.replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            if (showFirstTimeUsageMessage) {
                displayMessage(
                        "This is first time you use '" + extName + "' extension." + Constants.NEW_LINE +
                        "If extension is configurable, you can adjust its default settings...");
            }
            try {
                Class<? extends Extension> extensionClass = (Class<? extends Extension>) Class.forName(extension);
                Extension extensionInstance = null;
                byte[] extSettings = BackEnd.getInstance().getAddOnSettings(extension, PackType.EXTENSION);
                byte[] settings = null;
                if (ToolExtension.class.isAssignableFrom(extensionClass)) {
                    extensionInstance = tools.get(extensionClass);
                    settings = ((ToolExtension) extensionInstance).configure();
                } else if (TransferExtension.class.isAssignableFrom(extensionClass)) {
                    extensionInstance = transferrers.get(extensionClass);
                    settings = ((TransferExtension) extensionInstance).configure();
                } else if (EntryExtension.class.isAssignableFrom(extensionClass)) {
                    extensionInstance = ExtensionFactory.newEntryExtension(extensionClass);
                    if (extSettings == null) {
                        extSettings = new byte[]{};
                    }
                    settings = ((EntryExtension) extensionInstance).configure(extSettings);
                }
                if (settings == null) {
                    settings = new byte[]{};
                }
                if (!Arrays.equals(extSettings, settings)) {
                    BackEnd.getInstance().storeAddOnSettings(extension, PackType.EXTENSION, settings);
                    if (extensionInstance instanceof ToolExtension) {
                        getJPanelIndicators().setVisible(false);
                        JPanel panel = indicatorAreas.get(extensionClass);
                        ToolRepresentation tr = ((ToolExtension) extensionInstance).getRepresentation();
                        boolean removeIndicator = false;
                        if (tr != null) {
                            JComponent indicator = tr.getIndicator();
                            if (indicator != null) {
                                if (panel == null) {
                                    panel = createStatusBarIndicatorArea((Class<? extends ToolExtension>) extensionInstance.getClass());
                                }
                                panel.add(indicator);
                                getJPanelIndicators().add(panel);
                            } else {
                                removeIndicator = true;
                            }
                        } else {
                            removeIndicator = true;
                        }
                        if (panel != null && removeIndicator) {
                            panel.remove(1);
                            getJPanelIndicators().remove(panel);
                        }
                        getJPanelIndicators().setVisible(true);
                    }
                }
            } catch (Throwable t) {
                displayErrorMessage(
                        "<html>Extension <i>" + extName + "</i> failed to serialize settings!<br/>" +
                        "Settings that have failed to serialize will be lost! :(<br/>" + 
                        "This the most likely is an extension's bug or 3rd-party library dependency problem.<br/>" +
                        "You can either:<br/>" +
                            "<ul><li>check for new version of extension (the bug may be fixed in new version)</li>" +
                            "<li>check whether all 3rd-party libraries extension depends on are installed</li>" +
                            "<li>uninstall extension to avoid further instability and data loss</li>" + 
                            "<li>contact extension's author for further help</li>" +
                            "</ul></html>", t);
            }
        }
    }
    
    public static Window getActiveWindow() {
        if (instance == null) return null;
        if (instance.addOnsManagementDialog == null) return instance;
        return instance.addOnsManagementDialog.isVisible() ? instance.addOnsManagementDialog : instance;
    }
    
    private static void representTools() {
        Map<ToolExtension, String> extensions = null;
        try {
            extensions = ExtensionFactory.getAnnotatedToolExtensions();
        } catch (Throwable t) {
            displayErrorMessage("Failed to initialize tools! ", t);
        }
        if (extensions != null) {
            instance.getJToolBar3().removeAll();
            tools = new LinkedHashMap<Class<? extends ToolExtension>, ToolExtension>();
            int toolCnt = 0;
            for (Entry<ToolExtension, String> ext : extensions.entrySet()) {
                ToolExtension tool = ext.getKey();
                try {
                    ToolRepresentation representation = tool.getRepresentation();
                    if (representation != null) {
                        JButton toolButt = representation.getButton();
                        if (toolButt != null) {
                            if (Validator.isNullOrBlank(toolButt.getToolTipText())) {
                                toolButt.setToolTipText(ext.getValue());
                            }
                            instance.getJToolBar3().add(toolButt);
                        }
                        JComponent indicator = representation.getIndicator();
                        if (indicator != null) {
                            JPanel panel = instance.createStatusBarIndicatorArea(tool.getClass());
                            panel.add(indicator, BorderLayout.CENTER);
                        }
                    }
                    tools.put(tool.getClass(), tool);
                    toolCnt++;
                } catch (Throwable t) {
                    displayErrorMessage("Failed to initialize tool '" + tool.getClass().getCanonicalName() + "'", t);
                }
            }
            if (toolCnt != 0) {
                instance.getJPanel5().setVisible(true);
            }
        }
    }
    
    private static void initTransferrers() {
        Map<String, TransferExtension> extensions = null;
        try {
            extensions = ExtensionFactory.getAnnotatedTransferExtensions();
        } catch (Throwable t) {
            displayErrorMessage("Failed to initialize transferrers! ", t);
        }
        if (extensions != null) {
            transferrers = new LinkedHashMap<Class<? extends TransferExtension>, TransferExtension>();
            for (Entry<String, TransferExtension> ext : extensions.entrySet()) {
                TransferExtension transferrer = ext.getValue();
                try {
                    transferrers.put(transferrer.getClass(), transferrer);
                } catch (Throwable t) {
                    displayErrorMessage("Failed to initialize transferrer '" + transferrer.getClass().getCanonicalName() + "'", t);
                }
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
            extension = ExtensionFactory.newEntryExtension(de);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            extension = new MissingExtensionInformer(de);
        }
        return extension;
    }
    
    private void store(boolean beforeExit) throws Throwable {
        fireBeforeSaveEvent(new SaveEvent(beforeExit));
        BackEnd.getInstance().setConfig(collectProperties());
        BackEnd.getInstance().setData(collectData());
        BackEnd.getInstance().setToolsData(collectToolsData());
        BackEnd.getInstance().store();
        displayStatusBarMessage("data saved");
        fireAfterSaveEvent(new SaveEvent(beforeExit));
    }
    
    private Map<String, byte[]> collectToolsData() throws Throwable {
        Map<String, byte[]> toolsData = new HashMap<String, byte[]>();
        if (tools != null) {
            for (ToolExtension tool : tools.values()) {
                toolsData.put(tool.getClass().getName(), tool.serializeData());
                BackEnd.getInstance().storeAddOnSettings(tool.getClass().getName(), PackType.EXTENSION, tool.serializeSettings());
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
                                "<html>Extension <i>" + extension.getClass().getSimpleName() + "</i> failed to serialize data!<br/>" +
                                "Data that have failed to serialize will be lost! :(<br/>" + 
                                "This the most likely is an extension's bug or 3rd-party library dependency problem.<br/>" +
                                "You can either:<br/>" +
                                    "<ul><li>check for new version of extension (the bug may be fixed in new version)</li>" +
                                    "<li>check whether all 3rd-party libraries extension depends on are installed</li>" +
                                    "<li>uninstall extension to avoid further instability and data loss</li>" + 
                                    "<li>contact extension's author for further help</li>" +
                                    "</ul></html>", t);
                    }
                    try {
                        serializedSettings = extension.serializeSettings();
                    } catch (Throwable t) {
                        displayErrorMessage(
                                "<html>Extension <i>" + extension.getClass().getSimpleName() + "</i> failed to serialize settings!<br/>" +
                                "Settings that have failed to serialize will be lost! :(<br/>" + 
                                "This the most likely is an extension's bug or 3rd-party library dependency problem.<br/>" +
                                "You can either:<br/>" +
                                    "<ul><li>check for new version of extension (the bug may be fixed in new version)</li>" +
                                    "<li>check whether all 3rd-party libraries extension depends on are installed</li>" +
                                    "<li>uninstall extension to avoid further instability and data loss</li>" + 
                                    "<li>contact extension's author for further help</li>" +
                                    "</ul></html>", t);
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
    
    private void shutdown() {
        fireBeforeExitEvent();
        BackEnd.getInstance().shutdown(0);
    }
    
    private void exitWithOptionalAutoSave() {
        if (Preferences.getInstance().autoSaveOnExit) {
            try {
                store(true);
                shutdown();
            } catch (Throwable t) {
                displayErrorMessage("Failed to save!", t);
            }
        } else {
            shutdown();
        }
    }
    
    private void exit() {
        if (Downloader.getTotalActiveDownloadsCount() > 0) {
            JLabel message = new JLabel("Some downloads are still in progress. Are you sure you want to cancel all active downloads and exit?");
            JButton button = new JButton("Show active downloads");
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    addOnsManagementDialog.setVisible(true);
                    addOnsPane.setSelectedIndex(3);
                }
            });
            int opt = JOptionPane.showConfirmDialog(
                    getActiveWindow(),
                    new Component[] {message, button},
                    "Cancel downloads confirmation", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (opt == JOptionPane.NO_OPTION) {
                return;
            }
        }
        if (!Preferences.getInstance().displayConfirmationDialogs) {
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
                            store(true);
                            shutdown();
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

    // TODO [P2] optimization: this method can reuse getVisualEntryDescriptors and get keys from map returned by it
    //           alternatively, it can be optimized the same way as mentioned method (is LinkedList really needed here?)
    
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

    // TODO [P2] optimization: do not iterate over all tabs (to get full extensions list) each time, some caching would be nice
    
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

    // TODO [P2] optimization: visual entries map should be cached until collection of categories/entries is not changed
    
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

    // TODO [P2] optimization: categories map should be cached until collection of categories is not changed
    
    public static Map<UUID, VisualEntryDescriptor> getCategoryDescriptors() {
        if (instance != null) {
            return instance.getCategoryDescriptors(instance.getJTabbedPane(), new LinkedList<Recognizable>());
        }
        return null;
    }

    private Map<UUID, VisualEntryDescriptor> getCategoryDescriptors(JTabbedPane tabPane, LinkedList<Recognizable> entryPath) {
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
                entries.putAll(getCategoryDescriptors((JTabbedPane) c, new LinkedList<Recognizable>(entryPath)));
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
            instance.handleNavigationActionsStates();
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

    private JTabbedPane getActiveTabPane() {
        return getActiveTabPane(getJTabbedPane());
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
    
    private Component getComponentById(UUID id) {
        return getComponentById(getJTabbedPane(), id);
    }
    
    private Component getComponentById(JTabbedPane rootTabPane, UUID id) {
        if (rootTabPane.getTabCount() > 0) {
            for (Component c : rootTabPane.getComponents()) {
                if (!Validator.isNullOrBlank(c.getName()) && UUID.fromString(c.getName()).equals(id)) {
                    return c;
                } else {
                    if (c instanceof JTabbedPane) {
                        Component cmp = getComponentById((JTabbedPane) c, id);
                        if (cmp != null) {
                            return cmp;
                        }
                    }
                }
            }
        }
        return null;
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
        JOptionPane.showMessageDialog(getActiveWindow(), getFailureDetails(t), "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace(System.err);
    }

    public static void displayErrorMessage(String message, Throwable t) {
        JOptionPane.showMessageDialog(getActiveWindow(), message, "Error", JOptionPane.ERROR_MESSAGE);
        t.printStackTrace(System.err);
    }

    public static void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(getActiveWindow(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void displayMessage(String message) {
        JOptionPane.showMessageDialog(getActiveWindow(), message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addTabPaneListeners(JTabbedPane tabPane) {
        tabPane.addMouseListener(tabClickListener);
        tabPane.addChangeListener(tabChangeListener);
        tabPane.addMouseListener(tabMoveListener);
        tabPane.addMouseMotionListener(tabMoveListener);
    }

    private static String getFailureDetails(Throwable t) {
        StringBuffer msg = new StringBuffer();
        while (t != null) {
            if (t.getMessage() != null) {
                msg.append(Constants.NEW_LINE + t.getMessage());
            }
            t = t.getCause();
        }
        return msg.toString();
    }

    private MouseListener tabClickListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            currentTabPane = (JTabbedPane) e.getSource();
            if (currentTabPane.getSelectedIndex() != -1) {
                if (currentTabPane.getSelectedComponent() instanceof JTabbedPane) {
                    currentTabPane = (JTabbedPane) currentTabPane.getSelectedComponent();
                }
            }
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                int index = tabbedPane.getSelectedIndex();
                String caption = tabbedPane.getTitleAt(index);
                
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
                
                caption = JOptionPane.showInputDialog(
                        FrontEnd.this, 
                        new Component[] { icLabel, iconChooser, cLabel },
                        caption);
                if (caption != null) {
                	tabbedPane.setTitleAt(index, caption);
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
            handleNavigationActionsStates();
        }
    }
    
    private void handleNavigationActionsStates() {
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
    
    public void displayStatusBarErrorMessage(final String message) {
        displayStatusBarMessage(message, true);
    }
    
    public void displayStatusBarMessage(final String message) {
        displayStatusBarMessage(message, false);
    }
    
    private void displayStatusBarMessage(final String message, final boolean isError) {
        new Thread(new Runnable(){
            public void run() {
                final String timestamp = dateFormat.format(new Date()) + " # ";
                getJLabelStatusBarMsg().setText(Constants.HTML_PREFIX + "&nbsp;" + timestamp + (isError ? Constants.HTML_COLOR_HIGHLIGHT_ERROR : Constants.HTML_COLOR_HIGHLIGHT_OK) + message + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                ((DefaultListModel) getStatusBarMessagesList().getModel()).addElement(timestamp + message);
                if (getJPanel2().isVisible()) {
                    autoscrollList(getStatusBarMessagesList());
                }
                ActionListener al = new ActionListener(){
                    public void actionPerformed(ActionEvent ae){
                        getJLabelStatusBarMsg().setText(Constants.HTML_PREFIX + "&nbsp;" + timestamp + Constants.HTML_COLOR_NORMAL + message + Constants.HTML_COLOR_SUFFIX + Constants.HTML_SUFFIX);
                    }
                };
                Timer timer = new Timer(500, al);
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
            jToolBar.addSeparator();
            jToolBar.add(getJButton3());
            jToolBar.add(getJButton4());
            jToolBar.add(getJButton17());
            jToolBar.add(getJButton());
            jToolBar.add(getJButton5());
            jToolBar.add(getJButton18());
            jToolBar.add(getJButton1());
            jToolBar.addSeparator();
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
     * This method initializes jButton17
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton17() {
        if (jButton17 == null) {
            jButton17 = new JButton(adjustCategoryAction);
            jButton17.setText(Constants.EMPTY_STR);
        }
        return jButton17;
    }

    /**
     * This method initializes jButton18
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton18() {
        if (jButton18 == null) {
            jButton18 = new JButton(adjustEntryAction);
            jButton18.setText(Constants.EMPTY_STR);
        }
        return jButton18;
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
            putValue(Action.SMALL_ICON, uiIcons.getIconRootCategory());
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
        JLabel pLabel = new JLabel("Choose tabs placement:");
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
            putValue(Action.SMALL_ICON, uiIcons.getIconRootEntry());
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                Map<String, Class<? extends EntryExtension>> extensions = ExtensionFactory.getAnnotatedEntryExtensionClasses();
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
            byte[] defSettings = BackEnd.getInstance().getAddOnSettings(type.getName(), PackType.EXTENSION);
            if (defSettings == null) {
                // extension's first time usage
                configureExtension(type.getName(), true);
            }
            EntryExtension extension = ExtensionFactory.newEntryExtension(type);
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
            putValue(Action.SMALL_ICON, uiIcons.getIconChangePassword());
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
            putValue(Action.SMALL_ICON, uiIcons.getIconEntry());
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                Map<String, Class<? extends EntryExtension>> extensions = ExtensionFactory.getAnnotatedEntryExtensionClasses();
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
                        byte[] defSettings = BackEnd.getInstance().getAddOnSettings(type.getName(), PackType.EXTENSION);
                        if (defSettings == null) {
                            // extension's first time usage
                            configureExtension(type.getName(), true);
                        }
                        EntryExtension extension = ExtensionFactory.newEntryExtension(type);
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

    private AdjustCategoryAction adjustCategoryAction = new AdjustCategoryAction();
    private class AdjustCategoryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AdjustCategoryAction() {
            putValue(Action.NAME, "adjustCategory");
            putValue(Action.SHORT_DESCRIPTION, "adjust active category");
            putValue(Action.SMALL_ICON, uiIcons.getIconAdjustCategory());
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                JLabel pLabel = new JLabel("Tabs placement:");
                JComboBox placementsChooser = new JComboBox();
                for (Placement placement : PLACEMENTS) {
                    placementsChooser.addItem(placement);
                }
                for (int i = 0; i < placementsChooser.getItemCount(); i++) {
                    if (((Placement) placementsChooser.getItemAt(i)).getInteger().equals(currentTabPane.getTabPlacement())) {
                        placementsChooser.setSelectedIndex(i);
                        break;
                    }
                }
                int opt = JOptionPane.showConfirmDialog(FrontEnd.this, new Component[]{ pLabel, placementsChooser }, "Category adjustment", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    currentTabPane.setTabPlacement(((Placement) placementsChooser.getSelectedItem()).getInteger());
                }
            } catch (Exception ex) {
                displayErrorMessage("Failed to adjust category!", ex);
            }
        }
    };
    
    private AdjustEntryAction adjustEntryAction = new AdjustEntryAction();
    private class AdjustEntryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AdjustEntryAction() {
            putValue(Action.NAME, "adjustEntry");
            putValue(Action.SHORT_DESCRIPTION, "adjust active entry");
            putValue(Action.SMALL_ICON, uiIcons.getIconAdjustEntry());
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                JLabel ecLabel = new JLabel("Entry's category (will be moved on change):");
                JComboBox ecCB = new JComboBox();
                ecCB.addItem(Constants.EMPTY_STR);
                Map<UUID, VisualEntryDescriptor> veds = getCategoryDescriptors();
                for (VisualEntryDescriptor veDescriptor : veds.values()) {
                    ecCB.addItem(veDescriptor);
                }
                if (!Validator.isNullOrBlank(currentTabPane.getName())) {
                    ecCB.setSelectedItem(veds.get(UUID.fromString(currentTabPane.getName())));
                }
                Object eCat = ecCB.getSelectedItem();
                int opt = JOptionPane.showConfirmDialog(FrontEnd.this, new Component[]{ ecLabel, ecCB }, "Entry adjustment", JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION) {
                    if (!eCat.equals(ecCB.getSelectedItem())) {
                        JTabbedPane sourcePane = getActiveTabPane();
                        JTabbedPane destinationPane;
                        if (ecCB.getSelectedItem().equals(Constants.EMPTY_STR)) {
                            destinationPane = getJTabbedPane();
                        } else {
                            VisualEntryDescriptor ve = (VisualEntryDescriptor) ecCB.getSelectedItem();
                            destinationPane = (JTabbedPane) getComponentById(ve.getEntry().getId());
                        }
                        TabMoveUtil.moveTab(sourcePane, sourcePane.getSelectedIndex(), destinationPane);
                    }
                }
            } catch (Exception ex) {
                displayErrorMessage("Failed to adjust entry!", ex);
            }
        }
    };

    private DeleteAction deleteEntryOrCategoryAction = new DeleteAction();
    private class DeleteAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public DeleteAction() {
            putValue(Action.NAME, "delete");
            putValue(Action.SHORT_DESCRIPTION, "delete active entry/category");
            putValue(Action.SMALL_ICON, uiIcons.getIconDelete());
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
                    displayErrorMessage("Failed to delete entry!", ex);
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
    
    // TODO [P1] both import and export should have option 'force import/export unchanged data'

    private ImportAction importAction = new ImportAction();
    private class ImportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ImportAction() {
            putValue(Action.NAME, "import");
            putValue(Action.SHORT_DESCRIPTION, "import...");
            putValue(Action.SMALL_ICON, uiIcons.getIconImport());
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                final JComboBox configsCB = new JComboBox();
                configsCB.addItem(Constants.EMPTY_STR);
                for (String configName : BackEnd.getInstance().getTransferConfigurations(TRANSFER_TYPE.IMPORT).keySet()) {
                    configsCB.addItem(configName);
                }
                final JButton delButt = new JButton("Delete");
                delButt.setEnabled(false);
                delButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String name = (String) configsCB.getSelectedItem();
                            BackEnd.getInstance().removeTransferConfiguration(name, TRANSFER_TYPE.IMPORT);
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
                                BackEnd.getInstance().renameTransferConfiguration(oldName, newName, TRANSFER_TYPE.IMPORT);
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
                final JCheckBox importUnchangedDataCB = new JCheckBox("Force import even if data haven't been changed since last import");
                JPanel p = new JPanel(new BorderLayout());
                p.add(configsCB, BorderLayout.NORTH);
                JPanel pb = new JPanel(new GridLayout(1, 2));
                pb.add(renButt);
                pb.add(delButt);
                p.add(pb, BorderLayout.CENTER);
                p.add(importUnchangedDataCB, BorderLayout.SOUTH);
                Component[] c = new Component[] {
                        new JLabel("<html>Choose existing import configuration to use, <br/>" + 
                                   "or leave selection empty and press OK for custom export.</html>"),
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
                                        String configName = configsCB.getSelectedItem().toString();
                                        Properties props = BackEnd.getInstance().getTransferConfigurations(TRANSFER_TYPE.IMPORT).get(configName);
                                        String type = props.getProperty(Constants.OPTION_TRANSFER_TYPE);
                                        String password = props.getProperty(Constants.OPTION_DATA_PASSWORD);
                                        if (password == null) {
                                            password = Constants.EMPTY_STR;
                                        }
                                        TransferExtension transferrer = ExtensionFactory.getTransferExtension(type);
                                        if (transferrer == null) {
                                            throw new Exception("It looks like transfer type used in this stored import configuration is no longer available (extension uninstalled?).");
                                        }
                                        String fileLocation = props.getProperty(Constants.OPTION_FILE_LOCATION);
                                        byte[] transferOptions = BackEnd.getInstance().getTransferOptions(configName, TRANSFER_TYPE.IMPORT);
                                        String checkSum = null;
                                        if (!importUnchangedDataCB.isSelected()) {
                                            checkSum = new String(transferrer.doImportCheckSum(transferOptions));
                                        }
                                        if (checkSum != null && !BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.IMPORT, transferrer.getClass(), fileLocation, checkSum)) {
                                            label.setText("<html><font color=green>Data import - Completed</font></html>");
                                            processLabel.setText("Import discarded: data haven't changed since last import.");
                                        } else {
                                            byte[] importedData = transferrer.doImport(transferOptions);
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
                                                            Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_APP_CORE)),
                                                            Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ADDONS_AND_LIBS)),
                                                            Boolean.valueOf(props.getProperty(Constants.OPTION_UPDATE_ADDONS_AND_LIBS)),
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
                                                    if (Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_IMPORT_EXPORT_CONFIGS))) {
                                                        initTransferrers();
                                                    }
                                                    if (Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_PREFERENCES))) {
                                                        Preferences.getInstance().init();
                                                        applyPreferences();
                                                    }
                                                    if (Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG))) {
                                                        initGlobalSettings();
                                                        applyGlobalSettings();
                                                    }
                                                    label.setText("<html><font color=green>Data import - Completed</font></html>");
                                                    processLabel.setText("Data have been successfully imported.");
                                                    displayStatusBarMessage("import done");
                                                    BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.IMPORT, transferrer.getClass(), fileLocation, checkSum);
                                                    fireTransferEvent(new TransferEvent(TRANSFER_TYPE.IMPORT, transferrer.getClass(), configName));
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
                                        }
                                    } catch (Throwable ex) {
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
                        for (String annotation : ExtensionFactory.getAnnotatedTransferExtensions().keySet()) {
                            cb.addItem(annotation);
                        }
                        opt = JOptionPane.showConfirmDialog(FrontEnd.this, cb, "Choose import type", JOptionPane.OK_CANCEL_OPTION);
                        if (opt == JOptionPane.OK_OPTION) {
                            final String annotation = (String) cb.getSelectedItem();
                            final TransferExtension transferrer = ExtensionFactory.getAnnotatedTransferExtensions().get(annotation);
                            TransferConfiguration transferConfiguration = transferrer.configure(TRANSFER_TYPE.IMPORT);
                            if (transferConfiguration == null) {
                                hideBottomPanel();
                                throw new Exception("Invalid transfer configuration (null)!");
                            }
                            final byte[] transferOptions = transferConfiguration.getOptions();
                            if (transferOptions == null || transferOptions.length == 0) {
                                hideBottomPanel();
                                throw new Exception("Import options are missing! Import canceled.");
                            }
                            final String fileLocation = transferConfiguration.getFileLocation();
                            if (Validator.isNullOrBlank(fileLocation)) {
                                hideBottomPanel();
                                throw new Exception("Import file location is missing!");
                            }
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
                                        String checkSum = null;
                                        if (!importUnchangedDataCB.isSelected()) {
                                            checkSum = new String(transferrer.doImportCheckSum(transferOptions));
                                        }
                                        if (checkSum != null && !BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.IMPORT, transferrer.getClass(), fileLocation, checkSum)) {
                                            label.setText("<html><font color=green>Data import - Completed</font></html>");
                                            processModel.addElement("Import discarded: data haven't changed since last import.");
                                            autoscrollList(processList);
                                        } else {
                                            byte[] importedData = transferrer.doImport(transferOptions);
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

                                                JCheckBox importAppCoreCB = new JCheckBox("Import and update application core");
                                                
                                                JPanel p2 = new JPanel(new GridLayout(3, 2));
                                                
                                                JCheckBox importAddOnsAndLibsCB = new JCheckBox("Import addons and libraries");
                                                p2.add(importAddOnsAndLibsCB);
                                                JCheckBox updateAddOnsAndLibsCB = new JCheckBox("Update installed");
                                                p2.add(updateAddOnsAndLibsCB);
                                                createDependentCheckboxChangeListener(importAddOnsAndLibsCB, updateAddOnsAndLibsCB);
                                                
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
                                                                importAppCoreCB,
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
                                                                importAppCoreCB.isSelected(),
                                                                importAddOnsAndLibsCB.isSelected(),
                                                                updateAddOnsAndLibsCB.isSelected(),
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
                                                        if (importImportExportConfigsCB.isSelected()) {
                                                            initTransferrers();
                                                        }
                                                        if (importPreferencesCB.isSelected()) {
                                                            Preferences.getInstance().init();
                                                            applyPreferences();
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
                                                        BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.IMPORT, transferrer.getClass(), fileLocation, checkSum);
                                                        fireTransferEvent(new TransferEvent(TRANSFER_TYPE.IMPORT, transferrer.getClass()));
                                                        Component[] c = new Component[] {
                                                                new JLabel("Data have been successfully imported."),
                                                                new JLabel("If you want to save this import configuration,"),
                                                                new JLabel("input a name for it (or select existing one to overwrite):"),
                                                                configsCB          
                                                        };
                                                        JOptionPane.showMessageDialog(FrontEnd.this, c);
                                                        if (!Validator.isNullOrBlank(configsCB.getSelectedItem())) {
                                                            String configName = configsCB.getSelectedItem().toString();
                                                            Properties options = new Properties();
                                                            options.setProperty(Constants.OPTION_FILE_LOCATION, fileLocation);
                                                            options.setProperty(Constants.OPTION_CONFIG_NAME, configName);
                                                            options.setProperty(Constants.OPTION_TRANSFER_TYPE, annotation.substring(0, annotation.indexOf(Constants.BLANK_STR)));
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
                                                            options.setProperty(Constants.OPTION_PROCESS_APP_CORE, "" + importAppCoreCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_ADDONS_AND_LIBS, "" + importAddOnsAndLibsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_UPDATE_ADDONS_AND_LIBS, "" + updateAddOnsAndLibsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_ADDON_CONFIGS, "" + importAddOnConfigsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_OVERWRITE_ADDON_CONFIGS, "" + overwriteAddOnConfigsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_PROCESS_IMPORT_EXPORT_CONFIGS, "" + importImportExportConfigsCB.isSelected());
                                                            options.setProperty(Constants.OPTION_OVERWRITE_IMPORT_EXPORT_CONFIGS, "" + overwriteImportExportConfigsCB.isSelected());

                                                            if (!Validator.isNullOrBlank(password)) {
                                                                options.setProperty(Constants.OPTION_DATA_PASSWORD, password);
                                                            }
                                                            BackEnd.getInstance().storeTransferConfigurationAndOptions(configName, options, transferOptions, TRANSFER_TYPE.IMPORT);
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
                                        }
                                    } catch (Throwable ex) {
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
            } catch (Throwable ex) {
                displayErrorMessage("Failed to import: " + getFailureDetails(ex), ex);
            }
        }
    };
    
    private ExportAction exportAction = new ExportAction();
    private class ExportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ExportAction() {
            putValue(Action.NAME, "export");
            putValue(Action.SHORT_DESCRIPTION, "export...");
            putValue(Action.SMALL_ICON, uiIcons.getIconExport());
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (!autoconfirmed("Save data before export", 
                        "Data need to be saved before export can be performed. Save now and proceed with export?")) {
                    return;
                }
                // store first
                store(false);
                // now proceed with export
                final JComboBox configsCB = new JComboBox();
                configsCB.addItem(Constants.EMPTY_STR);
                for (String configName : BackEnd.getInstance().getTransferConfigurations(TRANSFER_TYPE.EXPORT).keySet()) {
                    configsCB.addItem(configName);
                }
                final JButton delButt = new JButton("Delete");
                delButt.setEnabled(false);
                delButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String name = (String) configsCB.getSelectedItem();
                            BackEnd.getInstance().removeTransferConfiguration(name, TRANSFER_TYPE.EXPORT);
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
                                BackEnd.getInstance().renameTransferConfiguration(oldName, newName, TRANSFER_TYPE.EXPORT);
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
                final JCheckBox exportUnchangedDataCB = new JCheckBox("Force export even if data haven't been changed since last export");
                JPanel p = new JPanel(new BorderLayout());
                p.add(configsCB, BorderLayout.NORTH);
                JPanel pb = new JPanel(new GridLayout(1, 2));
                pb.add(renButt);
                pb.add(delButt);
                p.add(pb, BorderLayout.CENTER);
                p.add(exportUnchangedDataCB, BorderLayout.SOUTH);
                Component[] c = new Component[] {
                        new JLabel("<html>Choose existing export configuration to use, <br/>" + 
                                   "or leave selection empty and press OK for custom export.</html>"),
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
                                        String configName = configsCB.getSelectedItem().toString();
                                        final Properties props = BackEnd.getInstance().getTransferConfigurations(TRANSFER_TYPE.EXPORT).get(configName);
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
                                        FileInfo fileInfo = BackEnd.getInstance().exportData(
                                                data, 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_PREFERENCES)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_DATA_ENTRY_CONFIGS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ONLY_RELATED_DATA_ENTRY_CONFIGS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_TOOLS_DATA)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ICONS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ONLY_RELATED_ICONS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_APP_CORE)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ADDONS_AND_LIBS)), 
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_ADDON_CONFIGS)),
                                                Boolean.valueOf(props.getProperty(Constants.OPTION_PROCESS_IMPORT_EXPORT_CONFIGS)),
                                                password);
                                        String type = props.getProperty(Constants.OPTION_TRANSFER_TYPE);
                                        TransferExtension transferrer = ExtensionFactory.getTransferExtension(type);
                                        if (transferrer == null) {
                                            throw new Exception("It looks like transfer type used in this stored export configuration is no longer available (extension uninstalled?).");
                                        }
                                        String fileLocation = props.getProperty(Constants.OPTION_FILE_LOCATION);
                                        if (!exportUnchangedDataCB.isSelected() && !BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.EXPORT, transferrer.getClass(), fileLocation, fileInfo.getCheckSum())) {
                                            label.setText("<html><font color=green>Data export - Completed</font></html>");
                                            processLabel.setText("Export discarded: data haven't changed since last export.");
                                        } else {
                                            byte[] transferOptions = BackEnd.getInstance().getTransferOptions(configName, TRANSFER_TYPE.EXPORT);
                                            transferrer.doExportCheckSum(fileInfo.getCheckSum().getBytes(), transferOptions);
                                            byte[] exportedData = FSUtils.readFile(fileInfo.getFile());
                                            transferrer.doExport(exportedData, transferOptions);
                                            label.setText("<html><font color=green>Data export - Completed</font></html>");
                                            processLabel.setText("Data have been successfully exported.");
                                            displayStatusBarMessage("export done");
                                            BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.EXPORT, transferrer.getClass(), fileLocation, fileInfo.getCheckSum());
                                            fireTransferEvent(new TransferEvent(TRANSFER_TYPE.EXPORT, transferrer.getClass(), configName));
                                        }
                                    } catch (Throwable ex) {
                                        String errMsg = "Failed to export data!";
                                        if (ex.getMessage() != null) {
                                            errMsg += " Error details: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
                                        }
                                        processLabel.setText(errMsg);
                                        label.setText("<html><font color=red>Data export - Failed</font></html>");
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
                        final JCheckBox exportAppCoreCB = new JCheckBox("Export application core"); 
                        final JCheckBox exportAddOnsCB = new JCheckBox("Export addons and libraries"); 
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
                                exportAppCoreCB,
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
                                        FileInfo fileInfo = BackEnd.getInstance().exportData(
                                                data, 
                                                exportPreferencesCB.isSelected(), 
                                                exportGlobalConfigCB.isSelected(), 
                                                exportDataEntryConfigsCB.isSelected(),
                                                exportOnlyRelatedDataEntryConfigsCB.isSelected(),
                                                exportToolsDataCB.isSelected(), 
                                                exportIconsCB.isSelected(), 
                                                exportOnlyRelatedIconsCB.isSelected(),
                                                exportAppCoreCB.isSelected(),
                                                exportAddOnsCB.isSelected(), 
                                                exportAddOnConfigsCB.isSelected(),
                                                exportImportExportConfigsCB.isSelected(),
                                                new String(passwordTF1.getPassword()));
                                        exportFile = fileInfo.getFile();
                                        processModel.addElement("Data to be exported have been successfully compressed.");
                                        autoscrollList(processList);
                                        JComboBox cb = new JComboBox();
                                        for (String annotation : ExtensionFactory.getAnnotatedTransferExtensions().keySet()) {
                                            cb.addItem(annotation);
                                        }
                                        opt = JOptionPane.showConfirmDialog(FrontEnd.this, cb, "Choose export type", JOptionPane.OK_CANCEL_OPTION);
                                        if (opt != JOptionPane.OK_OPTION) {
                                            hideBottomPanel();
                                        } else {    
                                            String annotation = (String) cb.getSelectedItem();
                                            final TransferExtension transferrer = ExtensionFactory.getAnnotatedTransferExtensions().get(annotation);
                                            TransferConfiguration transferConfiguration = transferrer.configure(TRANSFER_TYPE.IMPORT);
                                            if (transferConfiguration == null) {
                                                hideBottomPanel();
                                                throw new Exception("Invalid transfer configuration (null)!");
                                            }
                                            final byte[] transferOptions = transferConfiguration.getOptions();
                                            if (transferOptions == null || transferOptions.length == 0) {
                                                hideBottomPanel();
                                                throw new Exception("Export options are missing! Import canceled.");
                                            }
                                            final String fileLocation = transferConfiguration.getFileLocation();
                                            if (Validator.isNullOrBlank(fileLocation)) {
                                                hideBottomPanel();
                                                throw new Exception("Export file location is missing!");
                                            }
                                            try {
                                                if (!exportUnchangedDataCB.isSelected() && !BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.EXPORT, transferrer.getClass(), fileLocation, fileInfo.getCheckSum())) {
                                                    label.setText("<html><font color=green>Data export - Completed</font></html>");
                                                    processModel.addElement("Export discarded: data haven't changed since last export.");
                                                    autoscrollList(processList);
                                                } else {
                                                    processModel.addElement("Data checksum is being transferred...");
                                                    autoscrollList(processList);
                                                    transferrer.doExportCheckSum(fileInfo.getCheckSum().getBytes(), transferOptions);
                                                    processModel.addElement("Data checksum has been successfully transferred.");
                                                    autoscrollList(processList);
                                                    final byte[] exportedData = FSUtils.readFile(exportFile);
                                                    processModel.addElement("Data is being transferred...");
                                                    autoscrollList(processList);
                                                    transferrer.doExport(exportedData, transferOptions);
                                                    label.setText("<html><font color=green>Data export - Completed</font></html>");
                                                    processModel.addElement("Data have been successfully transferred.");
                                                    autoscrollList(processList);
                                                    displayStatusBarMessage("export done");
                                                    BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.EXPORT, transferrer.getClass(), fileLocation, fileInfo.getCheckSum());
                                                    fireTransferEvent(new TransferEvent(TRANSFER_TYPE.EXPORT, transferrer.getClass()));
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
                                                        Properties options = new Properties();
                                                        options.setProperty(Constants.OPTION_FILE_LOCATION, fileLocation);
                                                        options.setProperty(Constants.OPTION_CONFIG_NAME, configName);
                                                        options.setProperty(Constants.OPTION_TRANSFER_TYPE, annotation.substring(0, annotation.indexOf(Constants.BLANK_STR)));
                                                        options.setProperty(Constants.OPTION_PROCESS_PREFERENCES, "" + exportPreferencesCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_GLOBAL_CONFIG, "" + exportGlobalConfigCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_DATA_ENTRY_CONFIGS, "" + exportDataEntryConfigsCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_ONLY_RELATED_DATA_ENTRY_CONFIGS, "" + exportOnlyRelatedDataEntryConfigsCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_TOOLS_DATA, "" + exportToolsDataCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_ICONS, "" + exportIconsCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_ONLY_RELATED_ICONS, "" + exportOnlyRelatedIconsCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_ADDONS_AND_LIBS, "" + exportAddOnsCB.isSelected());
                                                        options.setProperty(Constants.OPTION_PROCESS_APP_CORE, "" + exportAppCoreCB.isSelected());
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
                                                        BackEnd.getInstance().storeTransferConfigurationAndOptions(configName, options, transferOptions, TRANSFER_TYPE.EXPORT);
                                                        processModel.addElement("Export configuration stored as '" + configName + "'");
                                                        autoscrollList(processList);
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
                                    } catch (Throwable ex) {
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
                displayErrorMessage("Failed to export: " + getFailureDetails(t), t);
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
    
    private AddCategoryAction addCategoryAction = new AddCategoryAction();
    private class AddCategoryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AddCategoryAction() {
            putValue(Action.NAME, "addCategory");
            putValue(Action.SHORT_DESCRIPTION, "add category");
            putValue(Action.SMALL_ICON, uiIcons.getIconCategory());
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
                JLabel pLabel = new JLabel("Choose tabs placement:");
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
            putValue(Action.SMALL_ICON, uiIcons.getIconSave());
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                store(false);
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
            putValue(Action.SMALL_ICON, uiIcons.getIconExit());
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
            putValue(Action.SMALL_ICON, uiIcons.getIconBackToFirst());
            setEnabled(false);
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
            putValue(Action.SMALL_ICON, uiIcons.getIconBack());
            setEnabled(false);
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
            putValue(Action.SMALL_ICON, uiIcons.getIconForward());
            setEnabled(false);
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
            putValue(Action.SMALL_ICON, uiIcons.getIconForwardToLast());
            setEnabled(false);
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
            putValue(Action.SMALL_ICON, uiIcons.getIconPreferences());
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
                                prefPanel = new JPanel(new GridLayout(2, 1));
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
                                ((JCheckBox) prefControl).setToolTipText(prefAnn.description());
                                ((JCheckBox) prefControl).setSelected(field.getBoolean(Preferences.getInstance()));
                            } else if ("int".equals(type)) {
                                prefPanel = new JPanel(new GridLayout(2, 1));
                                JLabel prefTitle = new JLabel(prefAnn.title() + Constants.BLANK_STR);
                                prefTitle.setToolTipText(prefAnn.description());
                                prefPanel.add(prefTitle);
                                SpinnerNumberModel sm = new SpinnerNumberModel();
                                sm.setMinimum(0);
                                sm.setStepSize(1);
                                sm.setValue(field.getInt(Preferences.getInstance()));
                                prefControl = new JSpinner(sm);
                                prefPanel.add(prefControl);
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
                                } else if (pref.getKey() instanceof JSpinner) {
                                    pref.getValue().set(Preferences.getInstance(), ((JSpinner) pref.getKey()).getValue());
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
    
    private boolean modified;
    
    private ManageAddOnsAction manageAddOnsAction = new ManageAddOnsAction();
    private class ManageAddOnsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public ManageAddOnsAction() {
            putValue(Action.NAME, "manageAddOns");
            putValue(Action.SHORT_DESCRIPTION, "manage add-ons");
            putValue(Action.SMALL_ICON, uiIcons.getIconAddOns());
        }

        public void actionPerformed(ActionEvent e) {
            modified = false;
            initAddOnsManagementDialog();
            addOnsManagementDialog.setVisible(true);
            if (modified) {
                displayMessage(RESTART_MESSAGE);
                displayStatusBarMessage("add-ons configuration changed");
            }
        }
        
    };
    
    private Object[] getInstallAddOnInfoRow(AddOnInfo addOnInfo) {
        return new Object[] {
                Boolean.FALSE,
                addOnInfo.getName(),
                addOnInfo.getVersion(),
                addOnInfo.getAuthor() != null ? addOnInfo.getAuthor() : Constants.ADDON_FIELD_VALUE_NA,
                addOnInfo.getDescription() != null ? addOnInfo.getDescription() : Constants.ADDON_FIELD_VALUE_NA };
    }
    
    private Object[] getAddOnInfoRow(Boolean selected, AddOnInfo addOnInfo, String status) {
        if (selected != null) {
            return new Object[] {
                    selected,
                    addOnInfo.getName(),
                    addOnInfo.getVersion(),
                    addOnInfo.getAuthor() != null ? addOnInfo.getAuthor() : Constants.ADDON_FIELD_VALUE_NA,
                    addOnInfo.getDescription() != null ? addOnInfo.getDescription() : Constants.ADDON_FIELD_VALUE_NA,
                    status };
        } else {
            return new Object[] {
                    addOnInfo.getName(),
                    addOnInfo.getVersion(),
                    addOnInfo.getAuthor() != null ? addOnInfo.getAuthor() : Constants.ADDON_FIELD_VALUE_NA,
                    addOnInfo.getDescription() != null ? addOnInfo.getDescription() : Constants.ADDON_FIELD_VALUE_NA,
                    status };
        }
    }
    
    private Object[] getPackRow(Pack pack, String status) {
        return new Object[]{
                Boolean.FALSE,
                pack.getType().value(), 
                pack.getName(), 
                pack.getVersion(), 
                pack.getAuthor() != null ? pack.getAuthor() : Constants.ADDON_FIELD_VALUE_NA, 
                pack.getDescription() != null ? pack.getDescription() : Constants.ADDON_FIELD_VALUE_NA,
                FormatUtils.formatByteSize(pack.getFileSize()),
                status};
    }
    
    private Map<String, Integer> getDepCounters() {
        if (depCounters == null) {
            depCounters = new HashMap<String, Integer>();
        }
        return depCounters;
    }
    
    @SuppressWarnings("unchecked")
    private void initAddOnsManagementDialog() {
        if (addOnsManagementDialog == null) {
            try {
                // extensions
                extList = new JTable(getExtensionsModel());
                final TableRowSorter<TableModel> extSorter = new TableRowSorter<TableModel>(getExtensionsModel());
                extSorter.setSortsOnUpdates(true);
                extList.setRowSorter(extSorter);
                extList.getColumnModel().getColumn(0).setPreferredWidth(30);
                extList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                for (AddOnInfo extension : BackEnd.getInstance().getAddOns(PackType.EXTENSION)) {
                    String status;
                    try {
                        String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + extension.getName() 
                                                + Constants.PACKAGE_PATH_SEPARATOR + extension.getName();
                        // extension class load test
                        Class<Extension> extClass = (Class<Extension>) Class.forName(fullExtName);
                        // extension instantiation test
                        ExtensionFactory.newExtension(extClass);
                        status = Constants.ADDON_STATUS_LOADED;
                    } catch (Throwable t) {
                        // extension is broken
                        System.err.println("Extension [ " + extension.getName() + " ] failed to initialize!");
                        t.printStackTrace(System.err);
                        status = Constants.ADDON_STATUS_BROKEN;
                    }
                    getExtensionsModel().addRow(getAddOnInfoRow(Boolean.FALSE, extension, status));
                }
                JButton extDetailsButt = new JButton("Extension details");
                extDetailsButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        int idx = extList.getSelectedRow();
                        if (idx != -1) {
                            try {
                                String extension = (String) extList.getValueAt(idx, 1);
                                String version = (String) extList.getValueAt(idx, 2);
                                try {
                                    File addOnInfoFile = new File(
                                            new File(Constants.ADDON_INFO_DIR, extension), 
                                            extension + Constants.VALUES_SEPARATOR + version + Constants.ADDON_DETAILS_FILENAME_SUFFIX);
                                    if (addOnInfoFile.exists()) {
                                        URL baseURL = addOnInfoFile.getParentFile().toURI().toURL();
                                        URL addOnURL = addOnInfoFile.toURI().toURL();
                                        loadAndDisplayPackageDetails(baseURL, addOnURL, extension);
                                    } else {
                                        displayMessage("Detailed information is not provided with this extension.");
                                    }
                                } catch (MalformedURLException ex) {
                                    displayErrorMessage("Invalid URL! " + getFailureDetails(ex), ex);
                                }
                            } catch (Throwable t) {
                                displayErrorMessage("Failed to display Extensions details!", t);
                            }
                        }
                    }
                });
                JButton extConfigButt = new JButton("Configure");
                extConfigButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        int idx = extList.getSelectedRow();
                        if (idx != -1) {
                            try {
                                String ext = (String) extList.getValueAt(idx, 1);
                                Map<AddOnInfo, String> newExts = BackEnd.getInstance().getNewAddOns(PackType.EXTENSION);
                                if (newExts != null && newExts.containsKey(new AddOnInfo(ext))) {
                                    displayMessage(
                                            "This Extension can not be (re)configured yet." + Constants.NEW_LINE +
                                            "Restart Bias first.");
                                } else {
                                    String extFullClassName = 
                                        Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                + ext + Constants.PACKAGE_PATH_SEPARATOR + ext;
                                    configureExtension(extFullClassName, false);
                                }
                            } catch (Exception ex) {
                                displayErrorMessage(ex);
                            }
                        }
                    }
                });
                JButton extInstButt = new JButton("Install/Update...");
                extInstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        installLocalPackages(extensionFileChooser, PackType.EXTENSION, getExtensionsModel());
                    }
                });
                JButton extUninstButt = new JButton("Uninstall");
                extUninstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            boolean selected = false;
                            for (int i = 0; i < getExtensionsModel().getRowCount(); i++) {
                                if ((Boolean) getExtensionsModel().getValueAt(i, 0)) {
                                    selected = true;
                                    break;
                                }
                            }
                            if (selected) {
                                if (confirmedUninstall()) {
                                    int i = 0;
                                    while  (i < getExtensionsModel().getRowCount()) {
                                        if ((Boolean) getExtensionsModel().getValueAt(i, 0)) {
                                            String extension = (String) getExtensionsModel().getValueAt(i, 1);
                                            String extFullClassName = 
                                                Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                        + extension + Constants.PACKAGE_PATH_SEPARATOR + extension;
                                            BackEnd.getInstance().uninstallAddOn(extFullClassName, PackType.EXTENSION);
                                            getExtensionsModel().removeRow(i);
                                            modified = true;
                                            i = 0;
                                        } else {
                                            i++;
                                        }
                                    }
                                }
                            }
                        } catch (Throwable ex) {
                            displayErrorMessage("Failed to uninstall extension(s)! " + getFailureDetails(ex), ex);
                        }
                    }
                });

                // skins
                skinList = new JTable(getSkinModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                        Component c = super.prepareRenderer(renderer, row, column);
                        String currSkinName = config.getProperty(Constants.PROPERTY_SKIN);
                        if (currSkinName == null) {
                            currSkinName = DEFAULT_SKIN;
                        }
                        String name = (String) getModel().getValueAt(row, 1);
                        if (name.equals(activeSkin)) {
                            c.setForeground(Color.BLUE);
                            Font f = super.getFont();
                            f = new Font(f.getName(), Font.BOLD, f.getSize());
                            c.setFont(f);
                        } else if (!activeSkin.equals(currSkinName) && name.equals(currSkinName)) {
                            c.setForeground(Color.BLUE);
                        } else {
                            c.setForeground(super.getForeground());
                        }
                        return c;
                    }
                };
                final TableRowSorter<TableModel> skinSorter = new TableRowSorter<TableModel>(getSkinModel());
                skinSorter.setSortsOnUpdates(true);
                skinList.setRowSorter(skinSorter);
                skinList.getColumnModel().getColumn(0).setPreferredWidth(30);
                skinList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                getSkinModel().addRow(new Object[]{Boolean.FALSE, DEFAULT_SKIN,Constants.EMPTY_STR,Constants.EMPTY_STR,"Default Skin"});
                for (AddOnInfo skin : BackEnd.getInstance().getAddOns(PackType.SKIN)) {
                    String status;
                    try {
                        String fullSkinName = Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + skin.getName() 
                                                + Constants.PACKAGE_PATH_SEPARATOR + skin.getName();
                        // extension class load test
                        Class<Skin> skinClass = (Class<Skin>) Class.forName(fullSkinName);
                        // extension instantiation test
                        skinClass.newInstance();
                        status = Constants.ADDON_STATUS_LOADED;
                    } catch (Throwable t) {
                        // extension is broken
                        System.err.println("Extension [ " + skin.getName() + " ] failed to initialize!");
                        t.printStackTrace(System.err);
                        status = Constants.ADDON_STATUS_BROKEN;
                    }
                    getSkinModel().addRow(getAddOnInfoRow(Boolean.FALSE, skin, status));
                }
                JButton skinDetailsButt = new JButton("Skin details");
                skinDetailsButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        int idx = skinList.getSelectedRow();
                        if (idx != -1) {
                            try {
                                String skin = (String) skinList.getValueAt(idx, 1);
                                if (DEFAULT_SKIN.equals(skin)) {
                                    displayMessage("This is a default native Java cross-platform Skin.");
                                } else {
                                    String version = (String) skinList.getValueAt(idx, 2);
                                    try {
                                        File addOnInfoFile = new File(
                                                new File(Constants.ADDON_INFO_DIR, skin), 
                                                skin + Constants.VALUES_SEPARATOR + version + Constants.ADDON_DETAILS_FILENAME_SUFFIX);
                                        if (addOnInfoFile.exists()) {
                                            URL baseURL = addOnInfoFile.getParentFile().toURI().toURL();
                                            URL addOnURL = addOnInfoFile.toURI().toURL();
                                            loadAndDisplayPackageDetails(baseURL, addOnURL, skin);
                                        } else {
                                            displayMessage("Detailed information is not provided with this Skin.");
                                        }
                                    } catch (MalformedURLException ex) {
                                        displayErrorMessage("Invalid URL! " + getFailureDetails(ex), ex);
                                    }
                                }
                            } catch (Throwable t) {
                                displayErrorMessage("Failed to display Skin details!", t);
                            }
                        }
                    }
                });
                JButton skinActivateButt = new JButton("(Re)Activate Skin");
                skinActivateButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        int idx = skinList.getSelectedRow();
                        if (idx != -1) {
                            String skin = (String) skinList.getValueAt(idx, 1);
                            if (DEFAULT_SKIN.equals(skin)) {
                                try {
                                    modified = setActiveSkin(null);
                                    skinList.repaint();
                                } catch (Throwable t) {
                                    displayErrorMessage("Failed to (re)activate Skin!", t);
                                }
                            } else {
                                Map<AddOnInfo, String> newSkins = BackEnd.getInstance().getNewAddOns(PackType.SKIN);
                                if (newSkins != null && newSkins.containsKey(new AddOnInfo(skin))) {
                                    displayMessage(
                                            "This Skin can not be (re)activated yet." + Constants.NEW_LINE +
                                            "Restart Bias first.");
                                } else {
                                    try {
                                        String fullSkinClassName = 
                                            Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                    + skin + Constants.PACKAGE_PATH_SEPARATOR + skin;
                                        modified = setActiveSkin(fullSkinClassName);
                                        skinList.repaint();
                                    } catch (Throwable t) {
                                        displayErrorMessage("Failed to (re)activate Skin!", t);
                                    }
                                }
                            }
                        }
                    }
                });
                JButton skinInstButt = new JButton("Install/Update...");
                skinInstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        installLocalPackages(skinFileChooser, PackType.SKIN, getSkinModel());
                    }
                });
                JButton skinUninstButt = new JButton("Uninstall");
                skinUninstButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            boolean selected = false;
                            for (int i = 0; i < getSkinModel().getRowCount(); i++) {
                                if ((Boolean) getSkinModel().getValueAt(i, 0)) {
                                    selected = true;
                                    break;
                                }
                            }
                            if (selected) {
                                if (confirmedUninstall()) {
                                    String currentSkin = config.getProperty(Constants.PROPERTY_SKIN);
                                    int i = 0;
                                    while  (i < getSkinModel().getRowCount()) {
                                        String skin = (String) getSkinModel().getValueAt(i, 1);
                                        if ((Boolean) getSkinModel().getValueAt(i, 0)) {
                                            String fullSkinClassName = 
                                                Constants.SKIN_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR
                                                                        + skin + Constants.PACKAGE_PATH_SEPARATOR + skin;
                                            BackEnd.getInstance().uninstallAddOn(fullSkinClassName, PackType.SKIN);
                                            getSkinModel().removeRow(i);
                                            // if skin that has been uninstalled was active one...
                                            if (skin.equals(currentSkin)) {
                                                //... unset it (default one will be used)
                                                config.remove(Constants.PROPERTY_SKIN);
                                            }
                                            modified = true;
                                            i = 0;
                                        } else {
                                            i++;
                                        }
                                    }
                                }
                            }
                        } catch (Throwable ex) {
                            displayErrorMessage("Failed to uninstall skin(s)! " + getFailureDetails(ex), ex);
                        }
                    }
                });
                
                // icons
                icSetList = new JTable(getIconSetModel());
                final TableRowSorter<TableModel> icSetSorter = new TableRowSorter<TableModel>(getIconSetModel());
                icSetSorter.setSortsOnUpdates(true);
                icSetList.setRowSorter(icSetSorter);
                icSetList.getColumnModel().getColumn(0).setPreferredWidth(30);
                icSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                for (AddOnInfo iconSetInfo : BackEnd.getInstance().getIconSets()) {
                    getIconSetModel().addRow(getAddOnInfoRow(Boolean.FALSE, iconSetInfo, null));
                }
                icons = new HashMap<String, ImageIcon>();
                icModel = new DefaultListModel();
                icList = new JList(icModel);
                for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
                    icModel.addElement(icon);
                    icons.put(icon.getDescription(), icon);
                }
                JScrollPane jsp = new JScrollPane(icList);
                jsp.setPreferredSize(new Dimension(200,200));
                jsp.setMinimumSize(new Dimension(200,200));
                JButton icSetDetailsButt = new JButton("IconSet details");
                icSetDetailsButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        int idx = icSetList.getSelectedRow();
                        if (idx != -1) {
                            try {
                                String ic = (String) icSetList.getValueAt(idx, 1);
                                String version = (String) icSetList.getValueAt(idx, 2);
                                try {
                                    File addOnInfoFile = new File(
                                            new File(Constants.ADDON_INFO_DIR, ic), 
                                            ic + Constants.VALUES_SEPARATOR + version + Constants.ADDON_DETAILS_FILENAME_SUFFIX);
                                    if (addOnInfoFile.exists()) {
                                        URL baseURL = addOnInfoFile.getParentFile().toURI().toURL();
                                        URL addOnURL = addOnInfoFile.toURI().toURL();
                                        loadAndDisplayPackageDetails(baseURL, addOnURL, ic);
                                    } else {
                                        displayMessage("Detailed information is not provided with this IconSet.");
                                    }
                                } catch (MalformedURLException ex) {
                                    displayErrorMessage("Invalid URL! " + getFailureDetails(ex), ex);
                                }
                            } catch (Throwable t) {
                                displayErrorMessage("Failed to display IconSet details!", t);
                            }
                        }
                    }
                });
                JButton addIconButt = new JButton("Add/Install...");
                addIconButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        if (iconsFileChooser.showOpenDialog(getActiveWindow()) == JFileChooser.APPROVE_OPTION) {
                            lockAddOnsManagementDialog();
                            Thread installThread = new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        boolean added = false;
                                        for (File file : iconsFileChooser.getSelectedFiles()) {
                                            Collection<ImageIcon> icons = BackEnd.getInstance().addIcons(file);
                                            if (!icons.isEmpty()) {
                                                for (ImageIcon icon : icons) {
                                                    icModel.addElement(icon);
                                                    FrontEnd.icons.put(icon.getDescription(), icon);
                                                }
                                                Collection<AddOnInfo> iconSets = BackEnd.getInstance().getIconSets();
                                                while (getIconSetModel().getRowCount() > 0) {
                                                    getIconSetModel().removeRow(0);
                                                }
                                                for (AddOnInfo iconSetInfo : iconSets) {
                                                    getIconSetModel().addRow(getAddOnInfoRow(Boolean.FALSE, iconSetInfo, null));
                                                }
                                                added = true;
                                            }
                                        }
                                        if (added) {
                                            icList.repaint();
                                            displayMessage("Icon(s) successfully installed!");
                                        } else {
                                            displayErrorMessage("Nothing to install!");
                                        }
                                    } catch (Throwable t) {
                                        displayErrorMessage("Failed to install icon(s)! " + getFailureDetails(t), t);
                                    } finally {
                                        unlockAddOnsManagementDialog();
                                    }
                                }
                            });
                            installThread.start();
                        }
                    }
                });
                JButton removeIconButt = new JButton("Remove selected icon(s)");
                removeIconButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (icList.getSelectedValues().length > 0) {
                                Collection<String> removeIds = new ArrayList<String>();
                                for (Object icon : icList.getSelectedValues()) {
                                    removeIds.add(((ImageIcon) icon).getDescription());
                                }
                                BackEnd.getInstance().removeIcons(removeIds);
                                for (Object icon : icList.getSelectedValues()) {
                                    icModel.removeElement(icon);
                                }
                                displayMessage("Icon(s) have been successfully removed!");
                            }
                        } catch (Throwable t) {
                            displayErrorMessage("Failed to remove icon(s)! " + getFailureDetails(t), t);
                        }
                    }
                });
                JButton removeIconSetButt = new JButton("Uninstall selected IconSet(s)");
                removeIconSetButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            boolean selected = false;
                            for (int i = 0; i < getIconSetModel().getRowCount(); i++) {
                                if ((Boolean) getIconSetModel().getValueAt(i, 0)) {
                                    selected = true;
                                    break;
                                }
                            }
                            if (selected) {
                                if (confirmedUninstall()) {
                                    boolean changed = false;
                                    int i = 0;
                                    while  (i < getIconSetModel().getRowCount()) {
                                        if ((Boolean) getIconSetModel().getValueAt(i, 0)) {
                                            String icSet = (String) getIconSetModel().getValueAt(i, 1);
                                            Collection<String> removedIds = BackEnd.getInstance().removeIconSet(icSet);
                                            for (String removedId : removedIds) {
                                                icModel.removeElement(icons.get(removedId));
                                            }
                                            getIconSetModel().removeRow(i);
                                            changed = true;
                                            i = 0;
                                        } else {
                                            i++;
                                        }
                                    }
                                    if (changed) {
                                        icList.repaint();
                                    }
                                }
                            }
                        } catch (Throwable ex) {
                            displayErrorMessage("Failed to uninstall IsonSet(s)! " + getFailureDetails(ex), ex);
                        }
                    }
                });

                // list of loaded libs
                libsList = getLibsList(BackEnd.getInstance().getAddOns(PackType.LIBRARY));
                libModel = (DefaultTableModel) libsList.getModel();
                
                // online list of available addons
                onlineList = new JTable(getOnlineModel());
                final TableRowSorter<TableModel> onlineSorter = new TableRowSorter<TableModel>(getOnlineModel());
                onlineSorter.setSortsOnUpdates(true);
                onlineList.setRowSorter(onlineSorter);
                onlineList.getColumnModel().getColumn(0).setPreferredWidth(30);
                TableModelListener dependencyResolver = new TableModelListener(){
                    public void tableChanged(TableModelEvent e) {
                        if (e.getColumn() == 0) {
                            try {
                                Pack pack = getAvailableOnlinePackages().get((String) getOnlineModel().getValueAt(e.getFirstRow(), 2));
                                if (pack.getDependency() != null && !pack.getDependency().isEmpty()) {
                                    for (Dependency dep : pack.getDependency()) {
                                        if (!BackEnd.getInstance().getAddOns().contains(new AddOnInfo(dep.getName()))) {
                                            int idx = findDataRowIndex(getOnlineModel(), 2, dep.getName());
                                            if (idx == -1) {
                                                getOnlineModel().setValueAt(Boolean.FALSE, e.getFirstRow(), e.getColumn());
                                                throw new Exception("Failed to resolve dependency for package '" + pack.getName() + "': " +
                                                                        dep.getType().value() + " '" + dep.getName() + "' " + 
                                                                        (dep.getVersion() != null ? 
                                                                        " (version " + dep.getVersion() + " or later) " : Constants.EMPTY_STR) + 
                                                                        " is not available!");
                                            } else {
                                                Integer counter = getDepCounters().get(dep.getName());
                                                if (counter == null) {
                                                    counter = 0;
                                                }
                                                if ((Boolean) getOnlineModel().getValueAt(e.getFirstRow(), e.getColumn())) {
                                                    counter++;
                                                    if (counter == 1) {
                                                        getOnlineModel().setValueAt(Boolean.TRUE, idx, 0);
                                                    }
                                                } else {
                                                    counter--;
                                                    if (counter == 0) {
                                                        getOnlineModel().setValueAt(Boolean.FALSE, idx, 0);
                                                    }
                                                }
                                                getDepCounters().put(dep.getName(), counter);
                                            }
                                        }
                                    }
                                }
                            } catch (Throwable t) {
                                displayErrorMessage("Failed to handle/resolve dependencies! " + getFailureDetails(t), t);
                            }
                        }
                    }
                };
                getOnlineModel().addTableModelListener(dependencyResolver);
                onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                final JPanel onlineProgressPanel = new JPanel(new GridLayout(2,1));
                onlineProgressPanel.add(getOnlineSingleProgressBar());
                onlineProgressPanel.add(getOnlineTotalProgressBar());
                JButton onlineRefreshButt = new JButton("Refresh");
                onlineRefreshButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        refreshOnlinePackagesList(null);
                    }
                });
                JButton onlineDetailsButt = new JButton("Package details");
                onlineDetailsButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        int idx = onlineList.getSelectedRow();
                        if (idx != -1) {
                            String addOnName = (String) onlineList.getValueAt(idx, 2);
                            try {
                                final Pack pack = getAvailableOnlinePackages().get(addOnName);
                                String fileName = pack.getName() + (pack.getVersion() != null ? Constants.VALUES_SEPARATOR + pack.getVersion() : Constants.EMPTY_STR) + Constants.ADDON_DETAILS_FILENAME_SUFFIX;
                                final URL addOnURL = new URL(BackEnd.getInstance().getRepositoryBaseURL() + fileName);
                                try {
                                    loadAndDisplayPackageDetails(BackEnd.getInstance().getRepositoryBaseURL(), addOnURL, pack.getName());
                                } catch (MalformedURLException ex) {
                                    displayErrorMessage("Failure while resolving repository URL! " + getFailureDetails(ex), ex);
                                }
                            } catch (Exception ex) {
                                displayErrorMessage("Failure while resolving repository URL! " + getFailureDetails(ex), ex);
                            }
                        }
                    }
                });
                JButton onlineInstallButt = new JButton("Download/install");
                onlineInstallButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        downloadAndInstallOnlinePackages(null);
                    }
                });
                JButton onlineSelectAllUpdatesButt = new JButton("Select all updates");
                onlineSelectAllUpdatesButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        selectAllUpdates();
                    }
                });
                JButton onlineCancelInstallButt = new JButton("Cancel download/installation");
                onlineCancelInstallButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Downloader.cancelAll();
                    }
                });
                
                // dialog
                addOnsPane = new JTabbedPane();

                JPanel extControlsPanel = new JPanel(new GridLayout(1,4));
                extControlsPanel.add(extDetailsButt);
                extControlsPanel.add(extConfigButt);
                extControlsPanel.add(extInstButt);
                extControlsPanel.add(extUninstButt);
                JPanel extTopPanel = new JPanel(new BorderLayout());
                extTopPanel.add(new JLabel("Filter:"), BorderLayout.CENTER);
                final JTextField extFilterText = new JTextField();
                extFilterText.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        extSorter.setRowFilter(RowFilter.regexFilter("(?i)" + extFilterText.getText()));
                    }
                });
                extTopPanel.add(extFilterText, BorderLayout.SOUTH);
                JPanel extPanel = new JPanel(new BorderLayout());
                extPanel.add(extTopPanel, BorderLayout.NORTH);
                extPanel.add(new JScrollPane(extList), BorderLayout.CENTER);
                extPanel.add(extControlsPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Extensions", uiIcons.getIconExtensions(), extPanel);
                
                JPanel skinControlsPanel = new JPanel(new GridLayout(1,4));
                skinControlsPanel.add(skinDetailsButt);
                skinControlsPanel.add(skinActivateButt);
                skinControlsPanel.add(skinInstButt);
                skinControlsPanel.add(skinUninstButt);
                JPanel skinTopPanel = new JPanel(new BorderLayout());
                skinTopPanel.add(new JLabel("Filter:"), BorderLayout.CENTER);
                final JTextField skinFilterText = new JTextField();
                skinFilterText.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        skinSorter.setRowFilter(RowFilter.regexFilter("(?i)" + skinFilterText.getText()));
                    }
                });
                skinTopPanel.add(skinFilterText, BorderLayout.SOUTH);
                JPanel skinPanel = new JPanel(new BorderLayout());
                skinPanel.add(skinTopPanel, BorderLayout.NORTH);
                skinPanel.add(new JScrollPane(skinList), BorderLayout.CENTER);
                skinPanel.add(skinControlsPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Skins", uiIcons.getIconSkins(), skinPanel);
                
                JPanel icControlsPanel = new JPanel(new GridLayout(1,4));
                icControlsPanel.add(icSetDetailsButt);
                icControlsPanel.add(addIconButt);
                icControlsPanel.add(removeIconButt);
                icControlsPanel.add(removeIconSetButt);
                JPanel icTopPanel = new JPanel(new BorderLayout());
                icTopPanel.add(new JLabel("Filter:"), BorderLayout.NORTH);
                final JTextField icSetFilterText = new JTextField();
                icSetFilterText.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        icSetSorter.setRowFilter(RowFilter.regexFilter("(?i)" + icSetFilterText.getText()));
                    }
                });
                icTopPanel.add(icSetFilterText, BorderLayout.CENTER);
                JPanel icPanel = new JPanel(new BorderLayout());
                icPanel.add(icTopPanel, BorderLayout.NORTH);
                icPanel.add(new JScrollPane(icSetList), BorderLayout.CENTER);
                icPanel.add(jsp, BorderLayout.EAST);
                icPanel.add(icControlsPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Icons", uiIcons.getIconIcons(), icPanel);
                
                JPanel onlineControlsPanel = new JPanel(new GridLayout(1,5));
                onlineControlsPanel.add(onlineRefreshButt);
                onlineControlsPanel.add(onlineDetailsButt);
                onlineControlsPanel.add(onlineInstallButt);
                onlineControlsPanel.add(onlineSelectAllUpdatesButt);
                onlineControlsPanel.add(onlineCancelInstallButt);
                JPanel onlinePanel = new JPanel(new BorderLayout());
                JPanel onlineTopPanel = new JPanel(new BorderLayout());
                onlineTopPanel.add(new JLabel("Filter:"), BorderLayout.NORTH);
                final JTextField onlineFilterText = new JTextField();
                onlineFilterText.addCaretListener(new CaretListener(){
                    public void caretUpdate(CaretEvent e) {
                        onlineSorter.setRowFilter(RowFilter.regexFilter("(?i)" + onlineFilterText.getText()));
                    }
                });
                onlineTopPanel.add(onlineFilterText, BorderLayout.CENTER);
                onlineTopPanel.add(new JScrollPane(onlineList), BorderLayout.SOUTH);
                onlinePanel.add(onlineTopPanel, BorderLayout.NORTH);
                onlinePanel.add(onlineProgressPanel, BorderLayout.CENTER);
                onlinePanel.add(onlineControlsPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Online", uiIcons.getIconOnline(), onlinePanel);
                
                JPanel advPanel = new JPanel(new BorderLayout());

                JPanel libsPanel = new JPanel(new BorderLayout());
                libsPanel.add(new JLabel("Registered libraries:"), BorderLayout.NORTH);
                libsPanel.add(new JScrollPane(libsList), BorderLayout.CENTER);
                advPanel.add(libsPanel, BorderLayout.CENTER);
                
                JPanel advBottomPanel = new JPanel();
                JPanel cleanPanel = null;
                if (BackEnd.getInstance().unusedAddOnDataAndConfigFilesFound() && !unusedAddOnDataAndConfigFilesCleanedUp) {
                    cleanPanel = new JPanel(new BorderLayout());
                    final JButton cleanButt = new JButton("Clean unused data and config files!");
                    JLabel cleanLabel = new JLabel(
                            Constants.HTML_PREFIX +
                            Constants.HTML_COLOR_HIGHLIGHT_WARNING +
                            "NOTE: This will remove all unused data and configuration files that were used by extensions/skins that are not currently loaded<br>" +
                            "(Do that only if you don't plan to install these extensions/skins again or want to reset their data/settings)" +
                            Constants.HTML_COLOR_SUFFIX +
                            Constants.HTML_SUFFIX);
                    cleanButt.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            BackEnd.getInstance().removeUnusedAddOnDataAndConfigFiles();
                            cleanButt.setText("Clean unused data and config files! [Done]");
                            cleanButt.setEnabled(false);
                            unusedAddOnDataAndConfigFilesCleanedUp = true;
                        }
                    });
                    cleanPanel.add(cleanButt, BorderLayout.NORTH);
                    cleanPanel.add(cleanLabel, BorderLayout.CENTER);
                }

                JPanel uninstLisbPanel = new JPanel(new BorderLayout());
                final JButton detectButt = new JButton("Detect unused libraries");
                final JButton cleanButt = new JButton("Uninstall unused libraries!");
                cleanButt.setEnabled(false);
                final JLabel cleanLabel = new JLabel(
                        Constants.HTML_PREFIX +
                        Constants.HTML_COLOR_HIGHLIGHT_WARNING +
                        "NOTE: deleting unused libriaries will remove all libraries that some not currently loaded extensions/skins were dependent on<br>" +
                        "(Do that only if you don't plan to install these extensions/skins again)" +
                        Constants.HTML_COLOR_SUFFIX +
                        Constants.HTML_SUFFIX);
                cleanLabel.setVisible(false);
                detectButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Collection<String> deps = new ArrayList<String>();
                            for (AddOnInfo addOnInfo : BackEnd.getInstance().getAddOns()) {
                                if (addOnInfo.getDependencies() != null) {
                                    for (Dependency dep : addOnInfo.getDependencies()) {
                                        deps.add(dep.getName());
                                    }
                                }
                            }
                            cleanButt.setEnabled(false);
                            cleanLabel.setVisible(false);
                            for (int i = 0; i < libModel.getRowCount(); i++) {
                                String libName = (String) libModel.getValueAt(i, 0);
                                if (!deps.contains(libName)) {
                                    libModel.setValueAt(Constants.ADDON_STATUS_UNUSED, i, 4);
                                    cleanButt.setEnabled(true);
                                    cleanLabel.setVisible(true);
                                }
                            }
                        } catch (Throwable t) {
                            displayErrorMessage("Failed to detect unused librarires! " + getFailureDetails(t), t);
                        }
                    }
                });
                cleanButt.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        try {
                            int i = 0;
                            while  (i < libModel.getRowCount()) {
                                if (libModel.getValueAt(i, 4).equals(Constants.ADDON_STATUS_UNUSED)) {
                                    String lib = (String) libModel.getValueAt(i, 0);
                                    BackEnd.getInstance().uninstallAddOn(lib, PackType.LIBRARY);
                                    libModel.removeRow(i);
                                    i = 0;
                                } else {
                                    i++;
                                }
                            }
                            cleanButt.setEnabled(false);
                            cleanLabel.setVisible(false);
                        } catch (Throwable t) {
                            displayErrorMessage("Failed to uninstall unused librarires! " + getFailureDetails(t), t);
                        }
                    }
                });
                JPanel bp = new JPanel(new GridLayout(1,2));
                bp.add(detectButt);
                bp.add(cleanButt);
                uninstLisbPanel.add(bp, BorderLayout.CENTER);
                uninstLisbPanel.add(cleanLabel, BorderLayout.SOUTH);
                
                advBottomPanel.setLayout(new GridLayout(cleanPanel != null ? 2 : 1, 1));
                advBottomPanel.add(uninstLisbPanel);
                if (cleanPanel != null) advBottomPanel.add(cleanPanel);
                
                advPanel.add(advBottomPanel, BorderLayout.SOUTH);
                
                addOnsPane.addTab("Advanced", uiIcons.getIconPreferences(), advPanel);
                
                addOnsManagementDialog = new JFrame("Bias :: Add-Ons Management");
                addOnsManagementDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                addOnsManagementDialog.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (!addOnsManagementDialogLocked) {
                            addOnsManagementDialog.setVisible(false);
                        }
                    }
                });
                JPanel p = new JPanel(new BorderLayout());
                p.add(addOnsPane, BorderLayout.CENTER);
                p.add(getCloseAddOnsManagementDialogButt(), BorderLayout.SOUTH);
                addOnsManagementDialog.add(p);
                addOnsManagementDialog.pack();
                int x = (getToolkit().getScreenSize().width - addOnsManagementDialog.getWidth()) / 2;
                int y = (getToolkit().getScreenSize().height - addOnsManagementDialog.getHeight()) / 2;
                addOnsManagementDialog.setLocation(x, y);
                addOnsManagementDialog.setVisible(true);
                
            } catch (Throwable t) {
                displayErrorMessage("Failed to initialize add-ons configuration screen!", t);
            }
        }
    }
    
    private DefaultTableModel getIconSetModel() {
        if (icSetModel == null) {
            icSetModel = new DefaultTableModel() {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isCellEditable(int rowIndex, int mColIndex) {
                    return mColIndex == 0 ? true : false;
                }
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    } else {
                        return super.getColumnClass(columnIndex);
                    }
                }
            };
            icSetModel.addColumn(Constants.EMPTY_STR);
            icSetModel.addColumn("Name");
            icSetModel.addColumn("Version");
            icSetModel.addColumn("Author");
            icSetModel.addColumn("Description");
        }
        return icSetModel;
    }
    
    private DefaultTableModel getSkinModel() {
        if (skinModel == null) {
            skinModel = new DefaultTableModel() {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isCellEditable(int rowIndex, int mColIndex) {
                    return mColIndex == 0 && !getValueAt(rowIndex, 1).equals(DEFAULT_SKIN) ? true : false;
                }
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    } else {
                        return super.getColumnClass(columnIndex);
                    }
                }
            };
            skinModel.addColumn(Constants.EMPTY_STR);
            skinModel.addColumn("Name");
            skinModel.addColumn("Version");
            skinModel.addColumn("Author");
            skinModel.addColumn("Description");
            skinModel.addColumn("Status");
        }
        return skinModel;
    }
    
    private DefaultTableModel getExtensionsModel() {
        if (extModel == null) {
            extModel = new DefaultTableModel() {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isCellEditable(int rowIndex, int mColIndex) {
                    return mColIndex == 0 ? true : false;
                }
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    } else {
                        return super.getColumnClass(columnIndex);
                    }
                }
            };
            extModel.addColumn(Constants.EMPTY_STR);
            extModel.addColumn("Name");
            extModel.addColumn("Version");
            extModel.addColumn("Author");
            extModel.addColumn("Description");
            extModel.addColumn("Status");
        }
        return extModel;
    }
    
    private DefaultTableModel getOnlineModel() {
        if (onlineModel == null) {
            onlineModel = new DefaultTableModel() {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isCellEditable(int rowIndex, int mColIndex) {
                    return mColIndex == 0 
                                    && (!getValueAt(rowIndex, 1).equals(PackType.LIBRARY.value()) 
                                    || getValueAt(rowIndex, 7).equals(Constants.ADDON_STATUS_UPDATE)) ? true : false;
                }
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    } else {
                        return super.getColumnClass(columnIndex);
                    }
                }
            };
            onlineModel.addColumn(Constants.EMPTY_STR);
            onlineModel.addColumn("Type");
            onlineModel.addColumn("Name");
            onlineModel.addColumn("Version");
            onlineModel.addColumn("Author");
            onlineModel.addColumn("Description");
            onlineModel.addColumn("Size");
            onlineModel.addColumn("Status");
        }
        return onlineModel;
    }
    
    private JProgressBar getOnlineSingleProgressBar() {
        if (onlineSingleProgressBar == null) {
            onlineSingleProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
            onlineSingleProgressBar.setStringPainted(true);
            onlineSingleProgressBar.setMinimum(0);
            onlineSingleProgressBar.setString(Constants.EMPTY_STR);
        }
        return onlineSingleProgressBar;
    }
    
    private JProgressBar getOnlineTotalProgressBar() {
        if (onlineTotalProgressBar == null) {
            onlineTotalProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
            onlineTotalProgressBar.setStringPainted(true);
            onlineTotalProgressBar.setMinimum(0);
            onlineTotalProgressBar.setString(Constants.EMPTY_STR);
        }
        return onlineTotalProgressBar;
    }
    
    private JButton getCloseAddOnsManagementDialogButt() {
        if (closeAddOnsManagementDialogButt == null) {
            closeAddOnsManagementDialogButt = new JButton("Close");
            closeAddOnsManagementDialogButt.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if (!addOnsManagementDialogLocked) {
                        addOnsManagementDialog.setVisible(false);
                    }
                }
            });
        }
        return closeAddOnsManagementDialogButt;
    }
    
    private void lockAddOnsManagementDialog() {
        getCloseAddOnsManagementDialogButt().setText(null);
        getCloseAddOnsManagementDialogButt().setIcon(ICON_PROCESS);
        addOnsManagementDialogLocked = true;
    }
    
    private void unlockAddOnsManagementDialog() {
        getCloseAddOnsManagementDialogButt().setText("Close");
        getCloseAddOnsManagementDialogButt().setIcon(null);
        addOnsManagementDialogLocked = false;
    }
    
    private void downloadAndInstallAllUpdates(final Runnable onFinishAction) {
        Runnable updateTask = new Runnable(){
            public void run() {
                if (updatesAvailable()) {
                    selectAllUpdates();
                    downloadAndInstallOnlinePackages(onFinishAction);
                }
            }
        };
        if (!onlineListRefreshed) {
            refreshOnlinePackagesList(updateTask);
        } else {
            updateTask.run();
        }
    }
    
    private boolean updatesAvailable() {
        for (int i = 0; i < getOnlineModel().getRowCount(); i++) {
            if (getOnlineModel().getValueAt(i, 7).equals(Constants.ADDON_STATUS_UPDATE)) {
                return true;
            }
        }
        return false;
    }
    
    private void selectAllUpdates() {
        for (int i = 0; i < getOnlineModel().getRowCount(); i++) {
            if (getOnlineModel().getValueAt(i, 7).equals(Constants.ADDON_STATUS_UPDATE)) {
                getOnlineModel().setValueAt(Boolean.TRUE, i, 0);
            }
        }
    }
    
    private void installLocalPackages(final AddOnFilesChooser addOnFileChooser, final PackType addOnType, final DefaultTableModel addOnModel) {
        if (addOnFileChooser.showOpenDialog(getActiveWindow()) == JFileChooser.APPROVE_OPTION) {
            Thread installThread = new Thread(new Runnable(){
                public void run() {
                    try {
                        lockAddOnsManagementDialog();
                        final Map<AddOnInfo, File> proposedAddOnsToInstall = new HashMap<AddOnInfo, File>();
                        StringBuffer sb = new StringBuffer(Constants.HTML_PREFIX + "<ul>");
                        boolean error = false;
                        for (File file : addOnFileChooser.getSelectedFiles()) {
                            try {
                                AddOnInfo installedAddOn = BackEnd.getInstance().getAddOnInfoAndDependencies(file, addOnType);
                                proposedAddOnsToInstall.put(installedAddOn, file);
                            } catch (Throwable t) {
                                error = true;
                                sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + "Failure on reading add-on's info from file '" + file.getName() + "': " + getFailureDetails(t) + Constants.HTML_COLOR_SUFFIX + "</li>");
                                t.printStackTrace(System.err);
                            }
                        }
                        unlockAddOnsManagementDialog();
                        if (error) {
                            sb.append("</ul>" + Constants.HTML_SUFFIX);
                            JOptionPane.showMessageDialog(getActiveWindow(), new JScrollPane(new JLabel(sb.toString())));
                        }
                        if (!proposedAddOnsToInstall.isEmpty()) {
                            Collection<AddOnInfo> confirmedAddOnsToInstall = confirmAddOnsInstallation(proposedAddOnsToInstall.keySet());
                            proposedAddOnsToInstall.keySet().retainAll(confirmedAddOnsToInstall);
                            if (!proposedAddOnsToInstall.isEmpty()) {
                                // check if there're dependency-packages present...
                                boolean depsPresent = false;
                                for (Integer i : getDepCounters().values()) {
                                    if (i > 0) {
                                        depsPresent = true;
                                        break;
                                    }
                                }
                                // ... and if yes...
                                if (depsPresent) {
                                    // ... remember currently active tab...
                                    final int activeTabIdx = addOnsPane.getSelectedIndex();
                                    // ... then switch to "Online" tab...
                                    addOnsPane.setSelectedIndex(3);
                                    // ... download dependency-packages...
                                    downloadAndInstallOnlinePackages(new Runnable(){
                                        public void run() {
                                            // ... then switch back to the previously active tab...
                                            addOnsPane.setSelectedIndex(activeTabIdx);
                                            // ... and finally, install local packages...
                                            installLocalPackages(proposedAddOnsToInstall, addOnType, addOnModel);
                                        }
                                    });
                                } else {
                                    installLocalPackages(proposedAddOnsToInstall, addOnType, addOnModel);
                                }
                            }
                        }
                    } finally {
                        unlockAddOnsManagementDialog();
                    }
                }
            });
            installThread.start();
        }
    }
    
    private void installLocalPackages(Map<AddOnInfo, File> proposedAddOnsToInstall, PackType addOnType, DefaultTableModel addOnModel) {
        try {
            lockAddOnsManagementDialog();
            StringBuffer sb = new StringBuffer(Constants.HTML_PREFIX + "<ul>");
            for (Entry<AddOnInfo, File> addons : proposedAddOnsToInstall.entrySet()) {
                try {
                    AddOnInfo installedAddOn = BackEnd.getInstance().installAddOn(addons.getValue(), addOnType);
                    String status = BackEnd.getInstance().getNewAddOns(addOnType).get(installedAddOn);
                    int idx = findDataRowIndex(addOnModel, 1, installedAddOn.getName());
                    if (idx != -1) {
                        addOnModel.removeRow(idx);
                        addOnModel.insertRow(idx, getAddOnInfoRow(Boolean.FALSE, installedAddOn, status));
                    } else {
                        addOnModel.addRow(getAddOnInfoRow(Boolean.FALSE, installedAddOn, status));
                    }
                    sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + "Add-On '" + installedAddOn.getName() + Constants.BLANK_STR + installedAddOn.getVersion() + "' has been successfully installed!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                    modified = true;
                } catch (Throwable t) {
                    sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + "Failed to install add-on '" + addons.getKey().getName() + Constants.BLANK_STR + addons.getKey().getVersion() + "' from file!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                    t.printStackTrace(System.err);
                }
            }
            unlockAddOnsManagementDialog();
            sb.append("</ul>" + Constants.HTML_SUFFIX);
            JOptionPane.showMessageDialog(getActiveWindow(), new JScrollPane(new JLabel(sb.toString())));
        } finally {
            unlockAddOnsManagementDialog();
        }
    }
    
    private boolean onlineListRefreshed = false;
    
    private void refreshOnlinePackagesList(final Runnable onFinishAction) {
        while (getOnlineModel().getRowCount() > 0) {
            getOnlineModel().removeRow(0);
        }
        try {
            URL addonsListURL = new URL(BackEnd.getInstance().getRepositoryBaseURL().toString() + Constants.ONLINE_REPOSITORY_DESCRIPTOR_FILE_NAME);
            final File file = new File(Constants.TMP_DIR, Constants.ONLINE_REPOSITORY_DESCRIPTOR_FILE_NAME);
            lockAddOnsManagementDialog();
            Downloader d = Downloader.createSingleFileDownloader(addonsListURL, file, Preferences.getInstance().preferredTimeOut);
            d.setDownloadListener(new DownloadListener(){
                @Override
                public void onComplete(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                    try {
                        for (Pack pack : ((Repository) getUnmarshaller().unmarshal(file)).getPack()) {
                            if (pack.getType() != null 
                                    && !Validator.isNullOrBlank(pack.getName()) 
                                    && pack.getFileSize() != null
                                    && pack.getVersion() != null 
                                    && pack.getVersion().matches(VersionComparator.VERSION_PATTERN)) {
                                Boolean isInstalledAndUpToDate = BackEnd.getInstance().isAddOnInstalledAndUpToDate(pack);
                                Object[] row = null;
                                if (isInstalledAndUpToDate == null) {
                                    row = getPackRow(pack, Constants.ADDON_STATUS_NEW);
                                } else if (!isInstalledAndUpToDate) {
                                    row = getPackRow(pack, Constants.ADDON_STATUS_UPDATE);
                                }
                                if (row != null) {
                                    getAvailableOnlinePackages().put(pack.getName(), pack);
                                    getOnlineModel().addRow(row);
                                }
                            }
                        }
                        onlineListRefreshed = true;
                        unlockAddOnsManagementDialog();
                        if (onFinishAction != null) {
                            onFinishAction.run();
                        }
                    } catch (Throwable t) {
                        displayErrorMessage("Failed to parse downloaded list of available addons!", t);
                    } finally {
                        unlockAddOnsManagementDialog();
                    }
                }
                @Override
                public void onFailure(URL url, File file, Throwable failure) {
                    unlockAddOnsManagementDialog();
                    displayErrorMessage("Failed to retrieve online list of available addons!", failure);
                }
                @Override
                public void onCancel(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                    unlockAddOnsManagementDialog();
                    JOptionPane.showMessageDialog(getActiveWindow(), "Online packages list refresh canceled by user!");
                }
            });
            d.start();
        } catch (Exception ex) {
            displayErrorMessage("Failure while resolving repository URL! " + getFailureDetails(ex), ex);
        }
    }
    
    private void downloadAndInstallOnlinePackages(final Runnable onFinishAction) {
        try {
            final Map<URL, Pack> urlPackageMap = new HashMap<URL, Pack>();
            final Map<URL, File> urlFileMap = new LinkedHashMap<URL, File>();
            Collection<Integer> depIndexes = new ArrayList<Integer>();
            long tSize = 0;
            for (String dep : getDepCounters().keySet()) {
                int idx = findDataRowIndex(getOnlineModel(), 2, dep);
                if (idx != -1) {
                    if ((Boolean) getOnlineModel().getValueAt(idx, 0)) {
                        Pack pack = getAvailableOnlinePackages().get((String) getOnlineModel().getValueAt(idx, 2));
                        String fileName = pack.getName() + (pack.getVersion() != null ? Constants.VALUES_SEPARATOR + pack.getVersion() : Constants.EMPTY_STR) + Constants.JAR_FILE_SUFFIX;
                        URL url;
                        if (!Validator.isNullOrBlank(pack.getUrl())) {
                            url = new URL(pack.getUrl());
                        } else {
                            url = new URL(BackEnd.getInstance().getRepositoryBaseURL() + fileName);
                        }
                        File file = new File(Constants.TMP_DIR, fileName);
                        urlFileMap.put(url, file);
                        urlPackageMap.put(url, pack);
                        tSize += pack.getFileSize();
                        depIndexes.add(idx);
                    }
                }
            }
            for (int i = 0; i < getOnlineModel().getRowCount(); i++) {
                if ((Boolean) getOnlineModel().getValueAt(i, 0) && !depIndexes.contains(i)) {
                    Pack pack = getAvailableOnlinePackages().get((String) getOnlineModel().getValueAt(i, 2));
                    String fileName = pack.getName() + (pack.getVersion() != null ? Constants.VALUES_SEPARATOR + pack.getVersion() : Constants.EMPTY_STR) + Constants.JAR_FILE_SUFFIX;
                    URL url;
                    if (!Validator.isNullOrBlank(pack.getUrl())) {
                        url = new URL(pack.getUrl());
                    } else {
                        url = new URL(BackEnd.getInstance().getRepositoryBaseURL() + fileName);
                    }
                    File file = new File(Constants.TMP_DIR, fileName);
                    urlFileMap.put(url, file);
                    urlPackageMap.put(url, pack);
                    tSize += pack.getFileSize();
                }
            }
            final Long totalSize = new Long(tSize);
            if (!urlFileMap.isEmpty()) {
                lockAddOnsManagementDialog();
                Downloader d = Downloader.createMultipleFilesDownloader(urlFileMap, Preferences.getInstance().preferredTimeOut);
                d.setDownloadListener(new DownloadListener(){
                    private StringBuffer sb = new StringBuffer();
                    boolean success = true;
                    @Override
                    public void onStart(URL url, File file) {
                        Pack pack = urlPackageMap.get(url);
                        if (pack.getFileSize() != null) {
                            getOnlineSingleProgressBar().setMaximum(pack.getFileSize().intValue());
                        }
                    };
                    @Override
                    public void onSingleProgress(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                        Pack pack = urlPackageMap.get(url);
                        getOnlineSingleProgressBar().setValue((int) downloadedBytesNum);
                        getOnlineSingleProgressBar().setString(pack.getName() + Constants.BLANK_STR + pack.getVersion() 
                                + " (" + FormatUtils.formatByteSize(downloadedBytesNum) + " / " + FormatUtils.formatByteSize(pack.getFileSize()) + ")");
                    };
                    @Override
                    public void onTotalProgress(int itemNum, long downloadedBytesNum, long elapsedTime) {
                        getOnlineTotalProgressBar().setValue((int) downloadedBytesNum);
                        double estimationCoef = ((double) totalSize) / ((double) downloadedBytesNum);
                        long estimationTime = (long) (elapsedTime * estimationCoef - elapsedTime);
                        getOnlineTotalProgressBar().setString(itemNum + " / " + urlFileMap.size() 
                                + " (" + FormatUtils.formatByteSize(downloadedBytesNum) + " / " + FormatUtils.formatByteSize(totalSize) + ")"
                                + ", elapsed time: " + FormatUtils.formatTimeDuration(elapsedTime) 
                                + ", estimated time left: " + FormatUtils.formatTimeDuration(estimationTime));
                    };
                    @Override
                    public void onComplete(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                        Pack pack = urlPackageMap.get(url);
                        try {
                            if (pack.getType() == PackType.ICON_SET) {
                                Collection<ImageIcon> icons = BackEnd.getInstance().addIcons(file);
                                if (!icons.isEmpty()) {
                                    for (ImageIcon icon : icons) {
                                        icModel.addElement(icon);
                                        FrontEnd.icons.put(icon.getDescription(), icon);
                                    }
                                    Collection<AddOnInfo> iconSets = BackEnd.getInstance().getIconSets();
                                    while (getIconSetModel().getRowCount() > 0) {
                                        getIconSetModel().removeRow(0);
                                    }
                                    for (AddOnInfo iconSetInfo : iconSets) {
                                        getIconSetModel().addRow(getAddOnInfoRow(Boolean.FALSE, iconSetInfo, null));
                                    }
                                    sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + "IconSet '" + pack.getName() + Constants.BLANK_STR + pack.getVersion() + "' has been successfully downloaded and installed!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                                    icList.repaint();
                                } else {
                                    sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + "IconSet '" + pack.getName() + Constants.BLANK_STR + pack.getVersion() + "' - nothing to install!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                                }
                            } else if (pack.getType() == PackType.LIBRARY) {
                                AddOnInfo libInfo = new AddOnInfo();
                                libInfo.setName(pack.getName());
                                libInfo.setVersion(pack.getVersion());
                                libInfo.setDescription(pack.getDescription());
                                libInfo.setAuthor(pack.getAuthor());
                                if (pack.getDependency() != null) {
                                    libInfo.addAllDependencies(pack.getDependency());
                                }
                                BackEnd.getInstance().installLibrary(file, libInfo);
                                String status = BackEnd.getInstance().getNewAddOns(PackType.LIBRARY).get(libInfo);
                                int idx = findDataRowIndex(libModel, 0, libInfo.getName());
                                if (idx != -1) {
                                    libModel.removeRow(idx);
                                    libModel.insertRow(idx, getAddOnInfoRow(null, libInfo, status));
                                } else {
                                    libModel.addRow(getAddOnInfoRow(null, libInfo, status));
                                }
                                sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + "Library '" + pack.getName() + Constants.BLANK_STR + pack.getVersion() + "' has been successfully downloaded and installed!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                                modified = true;
                            } else if (pack.getType() == PackType.APP_CORE) {
                                BackEnd.getInstance().installAppCoreUpdate(file);
                                sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + "AppCore '" + pack.getVersion() + "' has been successfully installed!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                                modified = true;
                            } else {
                                PackType addOnType = PackType.fromValue(pack.getType().value());
                                AddOnInfo installedAddOn = BackEnd.getInstance().installAddOn(file, addOnType);
                                String status = BackEnd.getInstance().getNewAddOns(addOnType).get(installedAddOn);
                                DefaultTableModel model = addOnType == PackType.EXTENSION ? getExtensionsModel() : getSkinModel();
                                int idx = findDataRowIndex(model, 1, installedAddOn.getName());
                                if (idx != -1) {
                                    model.removeRow(idx);
                                    model.insertRow(idx, getAddOnInfoRow(Boolean.FALSE, installedAddOn, status));
                                } else {
                                    model.addRow(getAddOnInfoRow(Boolean.FALSE, installedAddOn, status));
                                }
                                sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_OK + addOnType.value() + " '" + pack.getName() + Constants.BLANK_STR + pack.getVersion() + "' has been successfully downloaded and installed!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                                modified = true;
                            }
                        } catch (Throwable t) {
                            sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + "Failed to install " + pack.getType() + " '" + pack.getName() + Constants.BLANK_STR + pack.getVersion() + "' from downloaded file!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                            t.printStackTrace(System.err);
                        }
                    }
                    @Override
                    public void onFailure(URL url, File file, Throwable failure) {
                        success = false;
                        Pack pack = urlPackageMap.get(url);
                        sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + "'" + pack.getName() + "' - failed to retrieve installation file!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                        failure.printStackTrace(System.err);
                    }
                    @Override
                    public void onFinish(long downloadedBytesNum, long elapsedTime) {
                        unlockAddOnsManagementDialog();
                        if (!Validator.isNullOrBlank(sb)) {
                            JOptionPane.showMessageDialog(
                                    getActiveWindow(), 
                                    new JScrollPane(new JLabel(Constants.HTML_PREFIX + "<ul>" + sb.toString() + "</ul>" + Constants.HTML_SUFFIX)));
                            if (success && onFinishAction != null) {
                                onFinishAction.run();
                            }
                        }
                        getDepCounters().clear();
                        refreshOnlinePackagesList(null);
                    }
                    @Override
                    public void onCancel(URL url, File file, long downloadedBytesNum, long elapsedTime) {
                        success = false;
                        Pack pack = urlPackageMap.get(url);
                        sb.append("<li>" + Constants.HTML_COLOR_HIGHLIGHT_ERROR + "Download/installation of pack '" + pack.getName() + Constants.BLANK_STR + pack.getVersion() + "' cancelled!" + Constants.HTML_COLOR_SUFFIX + "</li>");
                        JOptionPane.showMessageDialog(getActiveWindow(), "Packages download/installation canceled by user!");
                    }
                });
                getOnlineTotalProgressBar().setMaximum(totalSize.intValue());
                d.start();
            }    
        } catch (Exception ex) {
            displayErrorMessage("Failure while resolving repository URL! " + getFailureDetails(ex), ex);
        }
    }
    
    private Collection<AddOnInfo> confirmAddOnsInstallation(Collection<AddOnInfo> addOnInfos) {
        final Map<String, AddOnInfo> proposedAddOnsToInstall = new HashMap<String, AddOnInfo>();
        Collection<AddOnInfo> addOnsToInstall = new ArrayList<AddOnInfo>();
        final DefaultTableModel addOnModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return mColIndex == 0 ? true : false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else {
                    return super.getColumnClass(columnIndex);
                }
            }
        };
        final JTable addOnList = new JTable(addOnModel);
        
        TableModelListener dependencyResolver = new TableModelListener(){
            public void tableChanged(final TableModelEvent e) {
                if (e.getColumn() == 0) {
                    final AddOnInfo pack = proposedAddOnsToInstall.get(addOnModel.getValueAt(e.getFirstRow(), 1));
                    if (pack.getDependencies() != null && !pack.getDependencies().isEmpty()) {
                        Runnable task = new Runnable() {
                            public void run() {
                                // ... and when done, try to resolve dependencies
                                try {
                                    lockAddOnsManagementDialog();
                                    for (Dependency dep : pack.getDependencies()) {
                                        if (!BackEnd.getInstance().getAddOns().contains(new AddOnInfo(dep.getName()))) {
                                            int idx = findDataRowIndex(getOnlineModel(), 2, dep.getName());
                                            if (idx == -1) {
                                                addOnModel.setValueAt(Boolean.FALSE, e.getFirstRow(), e.getColumn());
                                                throw new Exception("Failed to resolve dependency for package '" + pack.getName() + "': " +
                                                                        dep.getType().value() + " '" + dep.getName() + "' " + 
                                                                        (dep.getVersion() != null ? 
                                                                        " (version " + dep.getVersion() + " or later) " : Constants.EMPTY_STR) + 
                                                                        " is not available!");
                                            } else {
                                                Integer counter = getDepCounters().get(dep.getName());
                                                if (counter == null) {
                                                    counter = 0;
                                                }
                                                if ((Boolean) addOnModel.getValueAt(e.getFirstRow(), e.getColumn())) {
                                                    counter++;
                                                    if (counter == 1) {
                                                        getOnlineModel().setValueAt(Boolean.TRUE, idx, 0);
                                                    }
                                                } else {
                                                    counter--;
                                                    if (counter == 0) {
                                                        getOnlineModel().setValueAt(Boolean.FALSE, idx, 0);
                                                    }
                                                }
                                                getDepCounters().put(dep.getName(), counter);
                                            }
                                        }
                                    }
                                } catch (Throwable t) {
                                    displayErrorMessage("Failed to handle/resolve dependencies! " + getFailureDetails(t), t);
                                } finally {
                                    unlockAddOnsManagementDialog();
                                }
                            }
                        };
                        // refresh online packages list, if needed, first...
                        if (!onlineListRefreshed) {
                            refreshOnlinePackagesList(task);
                        } else {
                            task.run();
                        }
                    }
                }
            }
        };
        addOnModel.addTableModelListener(dependencyResolver);
        
        final TableRowSorter<TableModel> addOnSorter = new TableRowSorter<TableModel>(addOnModel);
        addOnSorter.setSortsOnUpdates(true);
        addOnList.setRowSorter(addOnSorter);
        addOnModel.addColumn(Constants.EMPTY_STR);
        addOnModel.addColumn("Name");
        addOnModel.addColumn("Version");
        addOnModel.addColumn("Author");
        addOnModel.addColumn("Description");
        for (AddOnInfo addOnInfo : addOnInfos) {
            addOnModel.addRow(getInstallAddOnInfoRow(addOnInfo));
            proposedAddOnsToInstall.put(addOnInfo.getName(), addOnInfo);
        }
        int opt = JOptionPane.showConfirmDialog(getActiveWindow(), new JScrollPane(addOnList), "Add-On(s) Installation Confirmation", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            for (int i = 0; i < addOnList.getRowCount(); i++) {
                if ((Boolean) addOnList.getValueAt(i, 0)) {
                    addOnsToInstall.add(proposedAddOnsToInstall.get(addOnList.getValueAt(i, 1)));
                }
            }
        }
        return addOnsToInstall;
    }
    
    private JTable getLibsList(Collection<AddOnInfo> addOnInfos) {
        final DefaultTableModel addOnModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        final JTable addOnList = new JTable(addOnModel);
        final TableRowSorter<TableModel> addOnSorter = new TableRowSorter<TableModel>(addOnModel);
        addOnSorter.setSortsOnUpdates(true);
        addOnList.setRowSorter(addOnSorter);
        addOnModel.addColumn("Name");
        addOnModel.addColumn("Version");
        addOnModel.addColumn("Author");
        addOnModel.addColumn("Description");
        addOnModel.addColumn("Status");
        addOnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (AddOnInfo addOnInfo : addOnInfos) {
            String status;
            Map<AddOnInfo, String> libStatuses = BackEnd.getInstance().getNewAddOns(PackType.LIBRARY);
            if (libStatuses != null) {
                status = libStatuses.get(addOnInfo);
            } else {
                status = Constants.ADDON_STATUS_LOADED;
            }
            addOnModel.addRow(getAddOnInfoRow(null, addOnInfo, status));
        }
        return addOnList;
    }

    private void loadAndDisplayPackageDetails(final URL baseURL, final URL addOnURL, final String addOnName) {
        Thread loadDetailsThread = new Thread(new Runnable(){
            public void run() {
                lockAddOnsManagementDialog();
                boolean loaded = false;
                InputStream is = null;
                ByteArrayOutputStream baos = null;
                try {
                    is = addOnURL.openStream();
                    baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int readBytesNum;
                    while ((readBytesNum = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, readBytesNum);
                    }
                    loaded = true;
                } catch (Throwable t) {
                    displayErrorMessage("Failed to load package details page! " + getFailureDetails(t), t);
                } finally {
                    unlockAddOnsManagementDialog();
                        try {
                            if (is != null) is.close();
                            if (baos != null) baos.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    if (loaded) {
                        JOptionPane op = new JOptionPane();
                        op.setMessage(getDetailsPane(new String(baos.toByteArray()), baseURL));
                        op.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                        final Dialog d = op.createDialog(getActiveWindow(), addOnName + " :: Details");
                        d.setLocation(getActiveWindow().getLocation());
                        d.setSize(getActiveWindow().getSize());
                        d.setVisible(true);
                    }
                }
            }
        });
        loadDetailsThread.start();
    }
    
    private int findDataRowIndex(DefaultTableModel model, int colIdx, String data) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (data.equals(model.getValueAt(i, colIdx))) {
                return i;
            }
        }
        return -1;
    }
    
    private JScrollPane getDetailsPane(String detailsInfo, URL baseURL) {
        if (detailsPane == null) {
            detailsTextPane = new JTextPane();
            detailsTextPane.setEditable(false);
            detailsTextPane.setEditorKit(new CustomHTMLEditorKit());
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
        ((HTMLDocument) detailsTextPane.getDocument()).setBase(baseURL);
        detailsTextPane.setText(detailsInfo);
        return detailsPane;
    }
    
    private AboutAction displayAboutInfoAction = new AboutAction();
    private class AboutAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AboutAction() {
            putValue(Action.NAME, "about");
            putValue(Action.SHORT_DESCRIPTION, "about...");
            putValue(Action.SMALL_ICON, uiIcons.getIconAbout());
        }

        public void actionPerformed(ActionEvent evt) {
            JLabel title1Label = new JLabel("Bias Personal Information Manager, version 0.9.7");
            JLabel link1Label = new LinkLabel("http://bias.sourceforge.net/");
            JLabel title2Label = new JLabel("(c) Roman Kasianenko, 2007");
        	JLabel link2Label = new LinkLabel("http://kion.name/");
            JLabel title3Label = new JLabel("EtweeSoft (Software Development Organization)");
            JLabel link3Label = new LinkLabel("http://etweesoft.org/");
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
