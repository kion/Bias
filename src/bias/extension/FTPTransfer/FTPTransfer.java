/**
 * Created on Oct 31, 2007
 */
package bias.extension.FTPTransfer;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import bias.Constants;
import bias.Preferences;
import bias.extension.TransferOptions;
import bias.extension.TransferExtension;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class FTPTransfer extends TransferExtension {
    
    private static final String TRANSFER_OPTION_FILEPATH = "FILEPATH";
    private static final String TRANSFER_OPTION_SERVER = "SERVER";
    private static final String TRANSFER_OPTION_USERNAME = "USERNAME";
    private static final String TRANSFER_OPTION_PASSWORD = "TRANSFER_PASSWORD";

    private static final String CHECKSUM_FILE_SUFIX = ".checksum";

    private static final String PATH_SEPARATOR = "/";

    private static final String PROTOCOL_PREFIX = "ftp://";

    private static final String PORT_SEPARATOR = ":";
    
    private static final int DEFAULT_PORT = 21;
    
    public FTPTransfer(byte[] settings) {
        super(settings);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#doExport(byte[], byte[])
     */
    @Override
    public void doExport(byte[] data, byte[] options) throws Throwable {
        performExport(data, options, false);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#doExportCheckSum(byte[], byte[])
     */
    @Override
    public void doExportCheckSum(byte[] data, byte[] options) throws Throwable {
        performExport(data, options, true);
    }
    
    private void performExport(byte[] data, byte[] options, boolean checksum) throws Throwable {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String server = opts.getProperty(TRANSFER_OPTION_SERVER);
        String username = opts.getProperty(TRANSFER_OPTION_USERNAME);
        String password = opts.getProperty(TRANSFER_OPTION_PASSWORD);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        if (checksum) {
            int idx = filePath.lastIndexOf(PATH_SEPARATOR);
            if (idx != -1) {
                String fileName = filePath.substring(idx + 1) + CHECKSUM_FILE_SUFIX;
                filePath = filePath.substring(0, idx);
                filePath = filePath + PATH_SEPARATOR + fileName;
            } else {
                filePath += CHECKSUM_FILE_SUFIX;
            }
        }
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        urlc.setConnectTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        urlc.setReadTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        OutputStream os = urlc.getOutputStream();
        os.write(data);
        os.close();
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#doImport(byte[])
     */
    @Override
    public byte[] doImport(byte[] options) throws Throwable {
        return performImport(options, false);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#doImportCheckSum(byte[])
     */
    @Override
    public byte[] doImportCheckSum(byte[] options) throws Throwable {
        return performImport(options, true);
    }
    
    private byte[] performImport(byte[] options, boolean checksum) throws Throwable {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String server = opts.getProperty(TRANSFER_OPTION_SERVER);
        String username = opts.getProperty(TRANSFER_OPTION_USERNAME);
        String password = opts.getProperty(TRANSFER_OPTION_PASSWORD);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        if (checksum) {
            int idx = filePath.lastIndexOf(PATH_SEPARATOR);
            if (idx != -1) {
                String fileName = filePath.substring(idx + 1) + CHECKSUM_FILE_SUFIX;
                filePath = filePath.substring(0, idx);
                filePath = filePath + PATH_SEPARATOR + fileName;
            } else {
                filePath += CHECKSUM_FILE_SUFIX;
            }
        }
        if (!server.contains(PORT_SEPARATOR)) {
            server += PORT_SEPARATOR + DEFAULT_PORT;
        }
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        urlc.setConnectTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        urlc.setReadTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        InputStream is = urlc.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int br;
        while ((br = is.read(buffer)) > 0) {
            baos.write(buffer, 0, br);
        }
        baos.close();
        is.close();
        return baos.toByteArray();
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#configure(bias.Constants.TRANSFER_OPERATION_TYPE)
     */
    @Override
    public TransferOptions configure(Constants.TRANSFER_TYPE transferType) throws Throwable {
        Properties options = new Properties();
        JLabel serverL = new JLabel("FTP Server (domain name or IP, including port if using non-default one)");
        JTextField serverTF = new JTextField();
        JLabel filepathL = new JLabel("Path to file on server");
        JTextField filepathTF = new JTextField();
        JLabel usernameL = new JLabel("Username to login");
        JTextField usernameTF = new JTextField();
        JLabel passwordL = new JLabel("Password to login");
        JTextField passwordTF = new JPasswordField();
        int opt = JOptionPane.showConfirmDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{
                        serverL,
                        serverTF,
                        filepathL,
                        filepathTF,
                        usernameL,
                        usernameTF,
                        passwordL,
                        passwordTF
                }, 
                "FTP transfer options", 
                JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            options = new Properties();
            String text = serverTF.getText();
            if (!Validator.isNullOrBlank(text)) {
                options.setProperty(TRANSFER_OPTION_SERVER, text);
            }
            text = filepathTF.getText();
            if (!Validator.isNullOrBlank(text)) {
                options.setProperty(TRANSFER_OPTION_FILEPATH, text);
            }
            text = usernameTF.getText();
            if (!Validator.isNullOrBlank(text)) {
                options.setProperty(TRANSFER_OPTION_USERNAME, text);
            }
            text = passwordTF.getText();
            if (!Validator.isNullOrBlank(text)) {
                options.setProperty(TRANSFER_OPTION_PASSWORD, text);
            }
        }
        return new TransferOptions(PropertiesUtils.serializeProperties(options), filepathTF.getText());
    }

}
