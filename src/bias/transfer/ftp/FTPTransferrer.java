/**
 * Created on Oct 31, 2007
 */
package bias.transfer.ftp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import bias.Constants;
import bias.Preferences;
import bias.transfer.Transferrer;

/**
 * @author kion
 */
public class FTPTransferrer extends Transferrer {
    
    // TODO [P2] would be nice to be able to use SFTP protocol...

    private static final String PROTOCOL_PREFIX = "ftp://";

    private static final String PORT_SEPARATOR = ":";
    
    private static final int DEFAULT_PORT = 21;
    
    /* (non-Javadoc)
     * @see bias.transfer.Transferrer#doExport(byte[], java.util.Properties)
     */
    @Override
    public void doExport(byte[] data, Properties options) throws Exception {
        String server = options.getProperty(Constants.TRANSFER_OPTION_SERVER);
        String username = options.getProperty(Constants.TRANSFER_OPTION_USERNAME);
        String password = options.getProperty(Constants.TRANSFER_OPTION_PASSWORD);
        String filePath = options.getProperty(Constants.TRANSFER_OPTION_FILEPATH);
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        urlc.setConnectTimeout(Preferences.getInstance().preferredTimeOut);
        urlc.setReadTimeout(Preferences.getInstance().preferredTimeOut);
        OutputStream os = urlc.getOutputStream();
        os.write(data);
        os.close();
    }

    /* (non-Javadoc)
     * @see bias.transfer.Transferrer#doImport(java.util.Properties)
     */
    @Override
    public byte[] doImport(Properties options) throws Exception {
        String server = options.getProperty(Constants.TRANSFER_OPTION_SERVER);
        String username = options.getProperty(Constants.TRANSFER_OPTION_USERNAME);
        String password = options.getProperty(Constants.TRANSFER_OPTION_PASSWORD);
        String filePath = options.getProperty(Constants.TRANSFER_OPTION_FILEPATH);
        if (!server.contains(PORT_SEPARATOR)) {
            server += PORT_SEPARATOR + DEFAULT_PORT;
        }
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        urlc.setConnectTimeout(Preferences.getInstance().preferredTimeOut);
        urlc.setReadTimeout(Preferences.getInstance().preferredTimeOut);
        InputStream is = urlc.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        baos.close();
        is.close();
        return baos.toByteArray();
    }

}
