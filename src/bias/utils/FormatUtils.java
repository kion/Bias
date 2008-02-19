/**
 * Created on Feb 19, 2008
 */
package bias.utils;


/**
 * @author kion
 */
public class FormatUtils {

    public static String formatByteSize(long byteSize) {
        float size = (float) byteSize;
        String metrics = " b";
        if (size >= 1024) {
            size = size/1024;
            metrics = " Kb";
        }
        if (size >= 1024) {
            size = size/1024;
            metrics = " Mb";
        }
        String sizeStr = "" + size;
        int idx = sizeStr.indexOf(".");
        if (idx != -1) {
            if (sizeStr.charAt(idx + 1) != '0') {
                sizeStr = sizeStr.substring(0, idx + 2);
            } else {
                sizeStr = sizeStr.substring(0, idx);
            }
        }
        sizeStr += metrics;
        return sizeStr;
    }
    
}
