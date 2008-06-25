/**
 * Created on Mar 18, 2008
 */
package bias.utils;

import java.io.InputStream;
import java.net.URL;

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

}
