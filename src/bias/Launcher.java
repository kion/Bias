package bias;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileLock;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.Timer;

import bias.utils.CommonUtils;
import bias.utils.FSUtils;

public class Launcher {

    private static final String UPDATE_FILE_PREFIX = "update_";
    private static final String APP_CORE_FILE_NAME = "appcore.jar";
    private static final String FILE_PROTOCOL_PREFIX = "file:";
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String BIAS_CONFIG_FILE = "config.properties";
    private static final String UNINSTALL_CONFIG_FILE = "uninstall.conf";
    private static final String PROPERTY_VALUES_SEPARATOR = ",";
    private static final String APP_MAIN_CLASS = "bias.Bias";
    private static final String LOCK_FILE_NAME = ".lock";
    private static final String PROPERTY_FORCE_TEXT_ANTIALIASING_MODE = "FORCE_TEXT_ANTIALIASING_MODE";
    private static final String PROPERTY_CUSTOM_TEXT_ANTIALIASING_MODE = "CUSTOM_TEXT_ANTIALIASING_MODE";
    
    public static final URL SPLASH_IMAGE_LOAD = Launcher.class.getResource("/bias/res/load.gif");

    private static File rootDir;
    private static File addonsDir;
    private static File configDir;
    
    public static File getRootDir() {
        return rootDir;
    }
    
    private static void launchApp(String password) throws Throwable {
        Class.forName(APP_MAIN_CLASS).getMethod(
                "launchApp", new Class[]{String.class}).invoke(null, new Object[]{password});
    }

    public static void main(String[] args) throws Throwable {
        URL url = Launcher.class.getResource(Launcher.class.getSimpleName() + CLASS_FILE_SUFFIX);
        String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(Launcher.class.getName().replaceAll("\\.", "/")) - 2);
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
    	}

        singleAppInstanceCheck();

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
                "Bias :: Authorization", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
            // ignore, if we can't do anything - user should care by himself about having only one application instance running
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
                    FSUtils.delete(file);
                }
                addClassPathURL(FILE_PROTOCOL_PREFIX + file.getAbsolutePath());
            }
        }
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
