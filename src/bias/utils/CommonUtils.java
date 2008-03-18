/**
 * Created on Mar 18, 2008
 */
package bias.utils;

import bias.Constants;

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

}
