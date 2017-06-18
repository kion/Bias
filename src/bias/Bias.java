package bias;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileLock;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import bias.core.AddOnInfo;
import bias.core.pack.Dependency;
import bias.core.pack.PackType;
import bias.utils.CommonUtils;
import bias.utils.FSUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

public class Bias {

    private static final String UPDATE_FILE_PREFIX = "update_";
    private static final String APP_CORE_FILE_NAME = "appcore.jar";
    private static final String FILE_PROTOCOL_PREFIX = "file:";
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String BIAS_CONFIG_FILE = "config.properties";
    private static final String UNINSTALL_CONFIG_FILE = "uninstall.conf";
    private static final String PROPERTY_VALUES_SEPARATOR = ",";
    private static final String VALUES_SEPARATOR = "_";
    private static final String NEW_LINE = "\n";
    private static final String JAR_FILE_PATTERN = "(?i).+\\.jar$";
    private static final String APP_MAIN_CLASS = "bias.App";
    private static final String LOCK_FILE_NAME = ".lock";
    private static final String ADDON_EXTENSION_JAR_FILE_SUFFIX = ".ext.jar";
    private static final String ADDON_SKIN_JAR_FILE_SUFFIX = ".skin.jar";
    private static final String ADDON_LIB_JAR_FILE_SUFFIX = ".lib.jar";
    private static final String ADDON_EXTENSION_INFO_FILE_SUFFIX = ".ext.info";
    private static final String ADDON_SKIN_INFO_FILE_SUFFIX = ".skin.info";
    private static final String ADDON_LIB_INFO_FILE_SUFFIX = ".lib.info";
    private static final String ATTRIBUTE_ADD_ON_NAME = "Bias-Add-On-Name";
    private static final String ATTRIBUTE_ADD_ON_TYPE = "Bias-Add-On-Type";
    private static final String ATTRIBUTE_ADD_ON_VERSION = "Bias-Add-On-Version";
    private static final String ATTRIBUTE_ADD_ON_AUTHOR = "Bias-Add-On-Author";
    private static final String ATTRIBUTE_ADD_ON_DESCRIPTION = "Bias-Add-On-Description";
    private static final String ATTRIBUTE_ADD_ON_DEPENDENCIES = "Bias-Add-On-Dependencies";
    private static final String PROPERTY_FORCE_TEXT_ANTIALIASING_MODE = "FORCE_TEXT_ANTIALIASING_MODE";
    private static final String PROPERTY_CUSTOM_TEXT_ANTIALIASING_MODE = "CUSTOM_TEXT_ANTIALIASING_MODE";
    
    public static final URL SPLASH_IMAGE_LOAD = Bias.class.getResource("/bias/res/load.gif");
    
    private static final ImageIcon APP_ICON_SMALL = new ImageIcon(Bias.class.getResource("/bias/res/app_icon_small.png"));
    private static final Image APP_ICON = Toolkit.getDefaultToolkit().getImage(Bias.class.getResource("/bias/res/app_icon.png"));

    private static File rootDir;
    private static File addonsDir;
    private static File configDir;
    
    public static File getRootDir() {
        return rootDir;
    }
    
    private static void launchApp(String password) throws Throwable {
        Class.forName(APP_MAIN_CLASS).getMethod(
                "launch", new Class[]{String.class}).invoke(null, new Object[]{password});
    }

    public static void main(String[] args) throws Throwable {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Mac")) {
            // ====================================================================
            // quit-app hotkey (⌘-Q) on OS X forces app to quit without 
            // giving it a chance to trigger app-shutdown hooks, 
            // e.g. to save content before app shuts down; 
            // in addition to that, force-quitting app in such a way  
            // is not always a desired behavior, e.g. b/c preferences 
            // might be set to keep app in system tray after app window closes;
            // thus, need to disable sudden termination for the hotkey  
            // and set appropriate quit strategy to make sure app behaves 
            // the same way on all platforms;
            // note: this is implemented using reflection b/c classes required 
            // to make it work are OS X Java Runtime specific classes 
            // (i.e. are not included by Java Runtime on other platforms)
            // ====================================================================
            try {
                Class<?> appCls = Class.forName("com.apple.eawt.Application");
                Object appObj = appCls.getMethod("getApplication").invoke(appCls);
                appCls.getMethod("disableSuddenTermination").invoke(appObj);
                Class<?> qsCls = Class.forName("com.apple.eawt.QuitStrategy");
                appCls.getMethod("setQuitStrategy", qsCls)
                    .invoke(appObj, Enum.valueOf((Class<Enum>) qsCls, "CLOSE_ALL_WINDOWS"));
                appCls.getMethod("setDockIconImage", Image.class)
                    .invoke(appObj, APP_ICON);
            } catch (Throwable cause) {
                // ignore, ⌘-Q would still force-quit app
            }
            // ====================================================================
        }

        URL url = Bias.class.getResource(Bias.class.getSimpleName() + CLASS_FILE_SUFFIX);
        String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(Bias.class.getName().replaceAll("\\.", "/")) - 2);
        URL jarFileURL = new URL(jarFilePath);
        rootDir = new File(jarFileURL.toURI()).getParentFile();
        addonsDir = new File(rootDir, "addons");
        configDir = new File(rootDir, "conf");
        
    	File configFile = new File(configDir, BIAS_CONFIG_FILE);
    	if (configFile.exists()) {
	    	FileReader fr = new FileReader(configFile);
	    	Properties props = new Properties();
	    	props.load(fr);
	    	fr.close();
	
	    	if (Boolean.valueOf(props.getProperty(PROPERTY_FORCE_TEXT_ANTIALIASING_MODE))) {
	        	System.setProperty("swing.aatext", "true");
	        	System.setProperty("awt.useSystemAAFontSettings", props.getProperty(PROPERTY_CUSTOM_TEXT_ANTIALIASING_MODE));
	    	}
    	} else {
            System.setProperty("swing.aatext", "true");
            System.setProperty("awt.useSystemAAFontSettings", "on");
    	}

        singleAppInstanceCheck();
        
        JLabel label = new JLabel("password:");
        final JPasswordField passField = new JPasswordField();
        boolean[] focused = new boolean[]{false};
        JFrame frame = new JFrame();
        frame.setIconImage(APP_ICON_SMALL.getImage());
        JOptionPane op = new JOptionPane(new Component[]{label, passField}, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, APP_ICON_SMALL);
        JDialog dlg = op.createDialog("Bias");
        dlg.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (!focused[0]) {
                    passField.requestFocusInWindow();
                    focused[0] = true;
                }
            }
        });
        dlg.setVisible(true);
        if (op.getValue() != null && op.getValue().equals(JOptionPane.OK_OPTION)) {
            String password = new String(passField.getPassword());            
            if (password != null) {
                try {
                    Splash.showSplash();
                    init();
                    launchApp(password);
                } catch (Throwable t) {
                    Splash.hideSplash();
                    t.printStackTrace(System.err);
                    JOptionPane.showMessageDialog(null, "Failed to load Bias! " + CommonUtils.getFailureDetails(t), "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                } finally {
                    Splash.hideSplash();
                }
            }
        } else {
            System.exit(0);
        }
    }

    private static void singleAppInstanceCheck() throws Throwable {
        // check if another application instance is not already running
        if (!lock()) {
            JOptionPane.showMessageDialog(null, "Application is already running!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static boolean lock() {
        try {
            final FileLock lock = new FileOutputStream(new File(rootDir, LOCK_FILE_NAME)).getChannel().tryLock();
            if (lock != null) {
                // Note: this is a trick to ensure there's only one instance of application is running at the same time; 
                // create dummy thread...
                Thread t = new Thread(new Runnable(){
                    public void run() {
                        while (true) {
                            try {
                                if (lock.isValid()) {}; // ... and access the lock inside it to guarantee it won't be disposed by GC
                                Thread.sleep(Long.MAX_VALUE); // now sleep "forever"
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                    }
                });
                t.setDaemon(true); // do not block main code
                t.start();
            }
            return lock != null;
        } catch (Exception ex) {
            // ignore, we can't do anything - user would need to take care of having only one application instance running on their own
        }
        return true;
    }
    
    private static void init() throws Throwable {
        // uninstall
        File uninstallConfigFile = new File(configDir, UNINSTALL_CONFIG_FILE);
        if (uninstallConfigFile.exists()) {
            byte[] cpData = FSUtils.readFile(uninstallConfigFile);
            if (cpData != null) {
                for (String cpEntry : new String(cpData).split(PROPERTY_VALUES_SEPARATOR)) {
                    File uninstallAddOnFile = new File(addonsDir, cpEntry);
                    FSUtils.delete(uninstallAddOnFile);
                    File uninstallUpdateAddOnFile = new File(addonsDir, UPDATE_FILE_PREFIX + cpEntry);
                    FSUtils.delete(uninstallUpdateAddOnFile);
                }
            }
        }
        // delete uninstall-config file
        FSUtils.delete(uninstallConfigFile);
        // update app core if update found and add it to classpath
        File appCoreFile = new File(rootDir, APP_CORE_FILE_NAME);
        File appCoreUpdateFile = new File(rootDir, UPDATE_FILE_PREFIX + APP_CORE_FILE_NAME);
        if (appCoreUpdateFile.exists()) {
            FSUtils.copy(appCoreUpdateFile, appCoreFile);
            FSUtils.delete(appCoreUpdateFile);
        }
        addClassPathURL(FILE_PROTOCOL_PREFIX + appCoreFile.getAbsolutePath());
        // update addons and libs if updates found and add them to classpath
        if (addonsDir.exists() && addonsDir.isDirectory()) {
            for (File file : addonsDir.listFiles()) {
                if (file.getName().startsWith(UPDATE_FILE_PREFIX)) {
                    File updatedFile = new File(addonsDir, file.getName().substring(UPDATE_FILE_PREFIX.length()));
                    FSUtils.copy(file, updatedFile);
                    // ensure updated addon info is up to date; 
                    // this might be needed in case of custom addon update scenario, 
                    // e.g. when update-JARs are copied to addons directory by external script 
                    // (for example - to support Bias updates via standard Linux package update mechanism), etc.
                    updateAddOnInfo(updatedFile);
                    FSUtils.delete(file);
                }
                addClassPathURL(FILE_PROTOCOL_PREFIX + file.getAbsolutePath());
            }
        }
    }
    
    private static void updateAddOnInfo(File addOnFile) throws Throwable {
        String suffix = null;
        PackType addOnType = null;
        String addOnFileName = addOnFile.getName();
        if (addOnFileName.endsWith(ADDON_EXTENSION_JAR_FILE_SUFFIX)) {
            suffix = ADDON_EXTENSION_INFO_FILE_SUFFIX;
            addOnType = PackType.EXTENSION;
        } else if (addOnFileName.endsWith(ADDON_SKIN_JAR_FILE_SUFFIX)) {
            suffix = ADDON_SKIN_INFO_FILE_SUFFIX;
            addOnType = PackType.SKIN;
        } else if (addOnFileName.endsWith(ADDON_LIB_JAR_FILE_SUFFIX)) {
            suffix = ADDON_LIB_INFO_FILE_SUFFIX;
            addOnType = PackType.LIBRARY;
        }
        if (suffix != null && addOnType != null) {
        	AddOnInfo addOnInfo = getAddOnInfoAndDependencies(addOnFile, addOnType);
            File addOnInfoFile = new File(configDir, addOnInfo.getName() + suffix);
            if (addOnInfoFile.exists()) {
                Properties info = new Properties();
                info.setProperty(ATTRIBUTE_ADD_ON_VERSION, addOnInfo.getVersion());
                info.setProperty(ATTRIBUTE_ADD_ON_AUTHOR, addOnInfo.getAuthor());
                info.setProperty(ATTRIBUTE_ADD_ON_DESCRIPTION, addOnInfo.getDescription());
                StringBuffer deps = new StringBuffer();
                if (addOnInfo.getDependencies() != null && !addOnInfo.getDependencies().isEmpty()) {
                    Iterator<Dependency> it = addOnInfo.getDependencies().iterator();
                    while (it.hasNext()) {
                        Dependency dep = it.next();
                        deps.append(dep.getType().value());
                        deps.append(VALUES_SEPARATOR);
                        deps.append(dep.getName());
                        if (!Validator.isNullOrBlank(dep.getVersion())) {
                            deps.append(VALUES_SEPARATOR);
                            deps.append(dep.getVersion());
                        }
                        if (it.hasNext()) {
                            deps.append(PROPERTY_VALUES_SEPARATOR);
                        }
                    }
                }
                if (!Validator.isNullOrBlank(deps)) {
                    info.setProperty(ATTRIBUTE_ADD_ON_DEPENDENCIES, deps.toString());
                }
                FSUtils.writeFile(addOnInfoFile, PropertiesUtils.serializeProperties(info));
            }
        }
    }

    private static AddOnInfo getAddOnInfoAndDependencies(File addOnFile, PackType addOnType) throws Throwable {
        AddOnInfo addOnInfo = null;
        if (addOnFile != null && addOnFile.exists() && !addOnFile.isDirectory()) {
            String name = addOnFile.getName();
            if (name.matches(JAR_FILE_PATTERN)) {
                JarInputStream in = new JarInputStream(new FileInputStream(addOnFile));
                Manifest manifest = in.getManifest();
                if (manifest == null) {
                    throw new Exception(
                            "Invalid Add-On-Package:" + NEW_LINE +
                            "MANIFEST.MF file is missing!");
                }
                String addOnTypeStr = manifest.getMainAttributes().getValue(ATTRIBUTE_ADD_ON_TYPE);
                if (Validator.isNullOrBlank(addOnTypeStr)) {
                    throw new Exception(
                            "Invalid Add-On-Package: " + NEW_LINE +
                            ATTRIBUTE_ADD_ON_TYPE 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                if (!addOnTypeStr.equals(addOnType.value())) {
                    throw new Exception(
                            "Invalid Add-On-Package type: " + NEW_LINE +
                            "(actual: '" + addOnTypeStr + "', expected: '" + addOnType.value() + "')!");
                }
                String addOnName = manifest.getMainAttributes().getValue(ATTRIBUTE_ADD_ON_NAME);
                if (Validator.isNullOrBlank(addOnName)) {
                    throw new Exception(
                            "Invalid Add-On-Package: " + NEW_LINE +
                            ATTRIBUTE_ADD_ON_NAME 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                String addOnVersion = manifest.getMainAttributes().getValue(ATTRIBUTE_ADD_ON_VERSION);
                if (Validator.isNullOrBlank(addOnVersion)) {
                    throw new Exception(
                            "Invalid Add-On-Package: " + NEW_LINE +
                            ATTRIBUTE_ADD_ON_VERSION 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                String addOnAuthor = manifest.getMainAttributes().getValue(ATTRIBUTE_ADD_ON_AUTHOR);
                String addOnDescription = manifest.getMainAttributes().getValue(ATTRIBUTE_ADD_ON_DESCRIPTION);
                addOnInfo = new AddOnInfo(addOnName, addOnVersion, addOnAuthor, addOnDescription);
                String addOnDependencies = manifest.getMainAttributes().getValue(ATTRIBUTE_ADD_ON_DEPENDENCIES);
                if (!Validator.isNullOrBlank(addOnDependencies)) {
                    String[] deps = addOnDependencies.split(PROPERTY_VALUES_SEPARATOR);
                    for (String dep : deps) {
                        String[] depInfo = dep.trim().split(VALUES_SEPARATOR);
                        if (depInfo.length != 2 && depInfo.length != 3) {
                            throw new Exception(
                                    "Invalid Add-On-Package: " + NEW_LINE +
                                    ATTRIBUTE_ADD_ON_DEPENDENCIES 
                                    + " attribute in MANIFEST.MF file has invalid value!" + NEW_LINE
                                    + "[At least dependency type and name should be specified (version is optional)]");
                        }
                        Dependency d = new Dependency();
                        d.setType(PackType.fromValue(depInfo[0]));
                        d.setName(depInfo[1]);
                        if (depInfo.length == 3) { 
                            d.setVersion(depInfo[2]);
                        }
                        addOnInfo.addDependency(d);
                    }
                }
                in.close();
            }
        } else {
            throw new Exception("Invalid Add-On-Package!");
        }
        return addOnInfo;
    }
    
    private static void addClassPathURL(String path) throws Throwable {
        URL u = new URL(path);
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{ URL.class });
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{ u });
    }

}
