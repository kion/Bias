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
        return CommonUtils.class.getResource("/bias/res/" + extensionClass.getSimpleName() + "/" + resourceName);
    }

    public static InputStream getResourceAsStream(Class<? extends Extension> extensionClass, String resourceName) {
        return CommonUtils.class.getResourceAsStream("/bias/res/" + extensionClass.getSimpleName() + "/" + resourceName);
    }
    
    public static final StyleSheet loadStyleSheet(String resourceName) {
        return loadStyleSheet(null, resourceName);
    }
    
    public static final StyleSheet loadStyleSheet(Class<? extends Extension> extensionClass, String resourceName) {
        InputStream is = CommonUtils.class.getResourceAsStream("/bias/res/" + (extensionClass != null ? extensionClass.getSimpleName() + "/" : "") + resourceName);
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

}
