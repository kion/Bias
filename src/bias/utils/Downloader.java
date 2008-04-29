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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kion
 */
public class Downloader {
    
    public static abstract class DownloadListener {
        public void onStart(URL url, File file){};
        public void onFailure(URL url, File file, Throwable failure){};
        public void onTotalProgress(int itemNum, long downloadedBytesNum, long elapsedTime){};
        public void onSingleProgress(URL url, File file, long downloadedBytesNum, long elapsedTime){};
        public void onComplete(URL url, File file, long downloadedBytesNum, long elapsedTime){};
        public void onFinish(long downloadedBytesNum, long elapsedTime){};
        public void onCancel(URL url, File file, long downloadedBytesNum, long elapsedTime){};
    }

    private static AtomicInteger totalActiveDownloadsCount = new AtomicInteger(0);
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
                incrementTotalActiveDownloadsCount();
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
                    decrementTotalActiveDownloadsCount();
                    if (listener != null) {
                        try {
                            if (failure != null) {
                                listener.onFailure(url, file, failure);
                            } else if (cancel || cancelAll) {
                                listener.onCancel(url, file, downloadedBytesNum, elapsedTime);
                            } else {
                                listener.onComplete(url, file, downloadedBytesNum, elapsedTime);
                            }    
                        } finally {
                            listener.onFinish(downloadedBytesNum, elapsedTime);
                        }
                    }
                }
            }
        });
    }
    
    private Downloader(final Map<URL, File> urlFileMap, final int timeout) {
        thread = new Thread(new Runnable(){
            public void run() {
                incrementTotalActiveDownloadsCount();
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
                            try {
                                if (failure != null) {
                                    listener.onFailure(url, file, failure);
                                } else if (cancel || cancelAll) {
                                    decrementTotalActiveDownloadsCount();
                                    try {
                                        listener.onCancel(url, file, downloadedBytesNum, elapsedTime);
                                    } finally {
                                        listener.onFinish(downloadedBytesNum, elapsedTime);
                                    }
                                    break;
                                } else {
                                    listener.onComplete(url, file, downloadedBytesNum, elapsedTime);
                                }
                            } finally {
                                if (!it.hasNext()) {
                                    decrementTotalActiveDownloadsCount();
                                    listener.onFinish(downloadedBytesNum, elapsedTime);
                                }
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
        if (totalActiveDownloadsCount.get() > 0) {
            cancelAll = true;
        }
    }
    
    private static void incrementTotalActiveDownloadsCount() {
        totalActiveDownloadsCount.incrementAndGet();
    }
    
    private static void decrementTotalActiveDownloadsCount() {
        totalActiveDownloadsCount.decrementAndGet();
        if (totalActiveDownloadsCount.get() == 0) {
            cancelAll = false;
        }
    }
    
    public static int getTotalActiveDownloadsCount() {
        return totalActiveDownloadsCount.get();
    }

    public void setDownloadListener(DownloadListener l) {
        listener = l;
    }
    
    public void unsetDownloadListener() {
        listener = null;
    }
    
}
