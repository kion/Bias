/**
 * Created on Mar 18, 2008
 */
package bias.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.swing.text.html.StyleSheet;

import bias.Constants;
import bias.extension.Extension;

/**
 * @author kion
 */
public class CommonUtils {
    
    private static Boolean isLinux;

    private static Boolean isMac;

    private static Boolean isWindows;

    public static String getFailureDetails(Throwable t) {
        StringBuffer msg = new StringBuffer();
        while (t != null) {
            if (t.getMessage() != null) {
                msg.append(Constants.NEW_LINE + t.getMessage());
            }
            t = t.getCause();
        }
        return msg.toString();
    }

    public static URL getResourceURL(Class<? extends Extension> extensionClass, String resourceName) {
        return extensionClass.getResource("/bias/res/" + extensionClass.getSimpleName() + "/" + resourceName);
    }

    public static InputStream getResourceAsStream(Class<? extends Extension> extensionClass, String resourceName) {
        return extensionClass.getResourceAsStream("/bias/res/" + extensionClass.getSimpleName() + "/" + resourceName);
    }
    
    public static final StyleSheet loadStyleSheet(Class<?> cls, String resourceName) {
        InputStream is = cls.getResourceAsStream("/bias/res/" + resourceName);
        StyleSheet styles = new StyleSheet();
        try {
            Reader r = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
            styles.loadRules(r, null);
            r.close();
        } catch (Throwable t) {
            // ignore, styles just won't be initialized
            t.printStackTrace(System.err);
        }
        return styles;
    }
    
    public static boolean isLinux() {
        if (isLinux == null) {
            isLinux = osNameMatches("Linux");
        }
        return isLinux;
    }
    
    public static boolean isMac() {
        if (isMac == null) {
            isMac = osNameMatches("Mac");
        }
        return isMac;
    }
    
    public static boolean isWindows() {
        if (isWindows == null) {
            isWindows = osNameMatches("Windows");
        }
        return isWindows;
    }
    
    private static boolean osNameMatches(String osId) {
        String osName = System.getProperty("os.name");
        return osName != null && osName.contains(osId);
    }

}
