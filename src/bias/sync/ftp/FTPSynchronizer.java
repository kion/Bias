/**
 * Created on Oct 21, 2007
 */
package bias.sync.ftp;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import bias.Constants;
import bias.Preferences;
import bias.sync.Synchronizer;

/**
 * @author kion
 */
public class FTPSynchronizer extends Synchronizer {
    
    private static final String PROTOCOL_PREFIX = "ftp://";
    
    private String username = Preferences.getInstance().ftpUsername;
    private String password = Preferences.getInstance().ftpPassword;
    private String server = Preferences.getInstance().ftpServer;
    private String syncDirPath = Preferences.getInstance().ftpSyncDirPath;

    /* (non-Javadoc)
     * @see bias.sync.Synchronizer#checkOut(java.lang.String)
     */
    @Override
    protected byte[] checkOut(String filePath) throws Exception {
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + syncDirPath + Constants.PATH_SEPARATOR + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        try {
            InputStream is = urlc.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
            baos.close();
            is.close();
            return baos.toByteArray(); 
        } catch (FileNotFoundException fnfe) {
            // ignore non-existing files
        }
        return null;
    }

    /* (non-Javadoc)
     * @see bias.sync.Synchronizer#commit(java.lang.String, byte[])
     */
    @Override
    protected void commit(String filePath, byte[] data) throws Exception {
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + syncDirPath + Constants.PATH_SEPARATOR + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        OutputStream os = urlc.getOutputStream();
        os.write(data);
        os.close();
    }

    /* (non-Javadoc)
     * @see bias.sync.Synchronizer#delete(java.lang.String)
     */
    @Override
    protected void delete(String filePath) throws Exception {
        // TODO
    }

}
