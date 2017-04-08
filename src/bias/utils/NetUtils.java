/**
 * Created on Apr 8, 2017
 */
package bias.utils;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import bias.core.BackEnd;

/**
 * @author kion
 */
public class NetUtils {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.7 (KHTML, like Gecko) Ubuntu/11.10 Chromium/16.0.912.77 Chrome/16.0.912.77 Safari/535.7";
    
    private NetUtils() {
        // hidden default constructor
    }
    
    public static URLConnection openConnection(URL url, int timeout) throws IOException {
        URLConnection conn = url.openConnection(BackEnd.getProxy(Proxy.Type.HTTP));
        conn.setConnectTimeout(timeout * 1000);
        conn.setReadTimeout(timeout * 1000);
        conn.addRequestProperty("User-Agent", USER_AGENT);
        Map<String, List<String>> header = conn.getHeaderFields();
        int attemptsLeft = 5;
        while (isRedirected(header) && attemptsLeft > 0) {
            url = new URL(header.get("Location").get(0));
            conn = (URLConnection) url.openConnection();
            conn.setConnectTimeout(timeout * 1000);
            conn.setReadTimeout(timeout * 1000);
            conn.addRequestProperty("User-Agent", USER_AGENT);
            header = conn.getHeaderFields();
            attemptsLeft--;
        }
        return conn;
    }
    
    private static boolean isRedirected(Map<String, List<String>> header) {
        for (String hv : header.get(null)) {
            if (hv.contains(" 301 ") || hv.contains(" 302 ")) return true;
        }
        return false;
    }

}
