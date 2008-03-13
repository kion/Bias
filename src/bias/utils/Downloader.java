/**
 * Created on Feb 28, 2008
 */
package bias.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kion
 */
public class Downloader {
    
    private static volatile Integer totalActiveDownloadsCount = 0;
    private static volatile boolean cancelAll = false;
    private boolean cancel = false;
    private DownloadListener listener;
    private Thread thread = null;
    
    public static Downloader createSingleFileDownloader(final URL url, final File file, final int timeout) {
        return new Downloader(url, file, timeout);
    }

    public static Downloader createMultipleFilesDownloader(final Map<URL, File> urlFileMap, final int timeout) {
        return new Downloader(urlFileMap, timeout);
    }
    
    private Downloader(final URL url, final File file, final int timeout) {
        thread = new Thread(new Runnable(){
            public void run() {
                if (listener != null) {
                    listener.onStart(url, file);
                }
                increaseTotalActiveDownloadsCount();
                long startTime = System.currentTimeMillis();
                long downloadedBytesNum = 0;
                long elapsedTime = 0;
                OutputStream out = null;
                InputStream  in = null;
                Throwable failure = null;
                try {
                    URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(timeout * 1000);
                    conn.setReadTimeout(timeout * 1000);
                    in = conn.getInputStream();
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    byte[] buffer = new byte[1024];
                    int readBytesNum;
                    while (!cancel && !cancelAll && (readBytesNum = in.read(buffer)) != -1) {
                        out.write(buffer, 0, readBytesNum);
                        if (listener != null) {
                            downloadedBytesNum += readBytesNum;
                            elapsedTime = System.currentTimeMillis() - startTime;
                            listener.onSingleProgress(url, file, downloadedBytesNum, elapsedTime);
                        }
                    }
                } catch (Throwable t) {
                    failure = t;
                } finally {
                    try {
                        if (in != null) in.close();
                        if (out != null) out.close();
                    } catch (IOException ioe) {
                        // ignore
                    }
                    if (listener != null) {
                        if (failure != null) {
                            listener.onFailure(url, file, failure);
                        } else if (cancel || cancelAll) {
                            listener.onCancel(url, file, downloadedBytesNum, elapsedTime);
                        } else {
                            listener.onComplete(url, file, downloadedBytesNum, elapsedTime);
                        }    
                        listener.onFinish(downloadedBytesNum, elapsedTime);
                    }
                    decreaseTotalActiveDownloadsCount();
                }
            }
        });
    }
    
    private Downloader(final Map<URL, File> urlFileMap, final int timeout) {
        thread = new Thread(new Runnable(){
            public void run() {
                increaseTotalActiveDownloadsCount();
                long startTime = System.currentTimeMillis();
                long downloadedBytesNum = 0;
                long elapsedTime = 0;
                int itemNum = 0;
                Iterator<URL> it = urlFileMap.keySet().iterator();
                while (it.hasNext()) {
                    URL url = it.next();
                    File file = urlFileMap.get(url);
                    if (listener != null) {
                        listener.onStart(url, file);
                    }
                    long currStartTime = System.currentTimeMillis();
                    long currentDownloadedBytesNum = 0;
                    long currentElapsedTime = 0;
                    itemNum++;
                    OutputStream out = null;
                    InputStream  in = null;
                    Throwable failure = null;
                    try {
                        URLConnection conn = url.openConnection();
                        conn.setConnectTimeout(timeout * 1000);
                        conn.setReadTimeout(timeout * 1000);
                        in = conn.getInputStream();
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        out = new BufferedOutputStream(new FileOutputStream(file));
                        byte[] buffer = new byte[1024];
                        int readBytesNum;
                        while (!cancel && !cancelAll && (readBytesNum = in.read(buffer)) != -1) {
                            out.write(buffer, 0, readBytesNum);
                            if (listener != null) {
                                currentDownloadedBytesNum += readBytesNum;
                                currentElapsedTime = System.currentTimeMillis() - currStartTime;
                                listener.onSingleProgress(url, file, currentDownloadedBytesNum, currentElapsedTime);
                                downloadedBytesNum += readBytesNum;
                                elapsedTime = System.currentTimeMillis() - startTime;
                                listener.onTotalProgress(itemNum, downloadedBytesNum, elapsedTime);
                            }
                        }
                    } catch (Throwable t) {
                        failure = t;
                    } finally {
                        try {
                            if (in != null) in.close();
                            if (out != null) out.close();
                        } catch (IOException ioe) {
                            // ignore
                        }
                        if (listener != null) {
                            if (failure != null) {
                                listener.onFailure(url, file, failure);
                            } else if (cancel || cancelAll) {
                                listener.onCancel(url, file, downloadedBytesNum, elapsedTime);
                                listener.onFinish(downloadedBytesNum, elapsedTime);
                                decreaseTotalActiveDownloadsCount();
                                break;
                            } else {
                                listener.onComplete(url, file, downloadedBytesNum, elapsedTime);
                            }
                            if (!it.hasNext()) {
                                listener.onFinish(downloadedBytesNum, elapsedTime);
                                decreaseTotalActiveDownloadsCount();
                            }
                        }
                    }
                }
            }
        });
    }
    
    public void start() {
        if (thread != null) {
            thread.start();
        }
    }
    
    public void cancel() {
        cancel = true;
    }
    
    public static void cancelAll() {
        if (totalActiveDownloadsCount > 0) {
            cancelAll = true;
        }
    }
    
    private static synchronized void increaseTotalActiveDownloadsCount() {
        totalActiveDownloadsCount++;
    }
    
    private static synchronized void decreaseTotalActiveDownloadsCount() {
        totalActiveDownloadsCount--;
        if (totalActiveDownloadsCount == 0) {
            cancelAll = false;
        }
    }
    
    public synchronized static int getTotalActiveDownloadsCount() {
        return totalActiveDownloadsCount;
    }

    public void setDownloadListener(DownloadListener l) {
        listener = l;
    }
    
    public void unsetDownloadListener() {
        listener = null;
    }
    
}
