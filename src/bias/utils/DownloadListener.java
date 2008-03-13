/**
 * Created on Mar 13, 2008
 */
package bias.utils;

import java.io.File;
import java.net.URL;

/**
 * @author kion
 */
public abstract class DownloadListener {

    public void onStart(URL url, File file){};
    public void onFailure(URL url, File file, Throwable failure){};
    public void onTotalProgress(int itemNum, long downloadedBytesNum, long elapsedTime){};
    public void onSingleProgress(URL url, File file, long downloadedBytesNum, long elapsedTime){};
    public void onComplete(URL url, File file, long downloadedBytesNum, long elapsedTime){};
    public void onFinish(long downloadedBytesNum, long elapsedTime){};
    public void onCancel(URL url, File file, long downloadedBytesNum, long elapsedTime){};

}
