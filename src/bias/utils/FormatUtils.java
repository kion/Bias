/**
 * Created on Feb 19, 2008
 */
package bias.utils;

import java.util.LinkedList;
import java.util.List;


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
    
    public static String formatTimeDuration(long duration) {
        List<String> list = new LinkedList<String>();
        StringBuffer lenStr = new StringBuffer();
        long sec = duration/1000;
        if (sec > 0) {
            long min = sec / 60;
            if (min > 0) {
                sec = sec % 60;
                long hr = min / 60;
                if (hr > 0) {
                    min = min % 60;
                    long days = hr / 24;
                    if (days > 0) {
                        hr = hr % 24;
                        list.add(days + " d ");
                    }
                    list.add(hr + " hr ");
                }
                list.add(min + " min ");
            }
            list.add(sec + " sec ");
        } else {
            list.add("none");
        }
        for (String s : list) {
            lenStr.append(s);
        }
        return lenStr.toString();
    }
    
}
