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
import bias.transfer.Transferrer;

/**
 * @author kion
 */
public class FTPTransferrer extends Transferrer {

    private static final String PROTOCOL_PREFIX = "ftp://";

    /* (non-Javadoc)
     * @see bias.transfer.Transferrer#doExport(byte[], java.util.Properties)
     */
    @Override
    protected void doExport(byte[] data, Properties settings) throws Exception {
        String server = settings.getProperty(Constants.TRANSFER_PROPERTY_SERVER);
        String username = settings.getProperty(Constants.TRANSFER_PROPERTY_USERNAME);
        String password = settings.getProperty(Constants.TRANSFER_PROPERTY_PASSWORD);
        String filePath = settings.getProperty(Constants.TRANSFER_PROPERTY_FILEPATH);
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        OutputStream os = urlc.getOutputStream();
        os.write(data);
        os.close();
    }

    /* (non-Javadoc)
     * @see bias.transfer.Transferrer#doImport(java.util.Properties)
     */
    @Override
    protected byte[] doImport(Properties settings) throws Exception {
        String server = settings.getProperty(Constants.TRANSFER_PROPERTY_SERVER);
        String username = settings.getProperty(Constants.TRANSFER_PROPERTY_USERNAME);
        String password = settings.getProperty(Constants.TRANSFER_PROPERTY_PASSWORD);
        String filePath = settings.getProperty(Constants.TRANSFER_PROPERTY_FILEPATH);
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
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
