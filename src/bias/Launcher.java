package bias;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.Timer;

import bias.utils.FSUtils;

public class Launcher {

    private static final String UPDATE_FILE_PREFIX = "update_";
    private static final String APP_CORE_FILE_NAME = "appcore.jar";
    private static final String FILE_PROTOCOL_PREFIX = "file:";
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String UNINSTALL_CONFIG_FILE = "uninstall.conf";
    private static final String PROPERTY_VALUES_SEPARATOR = ",";
    private static final String APP_MAIN_CLASS = "bias.Bias";
    
    public static final URL SPLASH_IMAGE_LOAD = Launcher.class.getResource("/bias/res/load.gif");

    private static File rootDir;
    private static File addonsDir;
    private static File libsDir;
    private static File configDir;
    
    public static File getRootDir() {
        return rootDir;
    }

    private static void launchApp(String password) throws Throwable {
        Class.forName(APP_MAIN_CLASS).getMethod(
                "launchApp", new Class[]{String.class}).invoke(null, new Object[]{password});
    }

    public static void main(String[] args) throws Throwable {

        // find out root directory application is run from
        URL url = Launcher.class.getResource(Launcher.class.getSimpleName() + CLASS_FILE_SUFFIX);
        String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(Launcher.class.getName().replaceAll("\\.", "/")) - 2);
        jarFilePath = jarFilePath.substring(FILE_PROTOCOL_PREFIX.length(), jarFilePath.length());
        rootDir = new File(jarFilePath).getParentFile();
        addonsDir = new File(rootDir, "addons");
        libsDir = new File(rootDir, "libs");
        configDir = new File(rootDir, "conf");

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
                Splash.showSplash(SPLASH_IMAGE_LOAD, null);
                init();
                launchApp(password);
                Splash.hideSplash();
            } else {
                System.exit(0);
            }
        }
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
            FSUtils.duplicateFile(appCoreUpdateFile, appCoreFile);
            FSUtils.delete(appCoreUpdateFile);
        }
        addClassPathURL(FILE_PROTOCOL_PREFIX + appCoreFile.getAbsolutePath());
        // update addons and libs if updates found and add them to classpath
        for (File addonFile : addonsDir.listFiles()) {
            File updateFile = new File(addonsDir, UPDATE_FILE_PREFIX + addonFile.getName());
            if (updateFile.exists()) {
                FSUtils.duplicateFile(updateFile, addonFile);
                FSUtils.delete(updateFile);
            }
            addClassPathURL(FILE_PROTOCOL_PREFIX + addonFile.getAbsolutePath());
        }
        for (File libFile : libsDir.listFiles()) {
            addClassPathURL(FILE_PROTOCOL_PREFIX + libFile.getAbsolutePath());
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
