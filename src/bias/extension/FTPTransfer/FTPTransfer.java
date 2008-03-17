/**
 * Created on Oct 31, 2007
 */
package bias.extension.FTPTransfer;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
import bias.extension.ObservableTransferExtension;
import bias.extension.TransferOptions;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class FTPTransfer extends ObservableTransferExtension {
    
    private static final String TRANSFER_OPTION_FILEPATH = "FILEPATH";
    private static final String TRANSFER_OPTION_SERVER = "SERVER";
    private static final String TRANSFER_OPTION_USERNAME = "USERNAME";
    private static final String TRANSFER_OPTION_PASSWORD = "TRANSFER_PASSWORD";

    private static final String META_DATA_FILE_SUFIX = ".metadata";

    private static final String PATH_SEPARATOR = "/";

    private static final String PROTOCOL_PREFIX = "ftp://";

    private static final String PORT_SEPARATOR = ":";
    
    private static final int DEFAULT_PORT = 21;
    
    public FTPTransfer(byte[] options) {
        super(options);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#exportData(byte[], byte[], boolean)
     */
    @Override
    public void exportData(byte[] data, byte[] options, boolean transferMetaData) throws Throwable {
        long startTime = System.currentTimeMillis();
        long transferredBytesNum = 0;
        long elapsedTime = 0;
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String server = opts.getProperty(TRANSFER_OPTION_SERVER);
        String username = opts.getProperty(TRANSFER_OPTION_USERNAME);
        String password = opts.getProperty(TRANSFER_OPTION_PASSWORD);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        if (transferMetaData) {
            int idx = filePath.lastIndexOf(PATH_SEPARATOR);
            if (idx != -1) {
                String fileName = filePath.substring(idx + 1) + META_DATA_FILE_SUFIX;
                filePath = filePath.substring(0, idx);
                filePath = filePath + PATH_SEPARATOR + fileName;
            } else {
                filePath += META_DATA_FILE_SUFIX;
            }
        }
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        urlc.setConnectTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        urlc.setReadTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        OutputStream os = urlc.getOutputStream();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        byte[] buffer = new byte[1024];
        int br;
        while ((br = bis.read(buffer)) > 0) {
            os.write(buffer, 0, br);
            if (!transferMetaData) {
                transferredBytesNum += br;
                elapsedTime = System.currentTimeMillis() - startTime;
                fireOnProgressEvent(transferredBytesNum, elapsedTime);
            }
        }
        bis.close();
        os.close();
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#importData(byte[], boolean)
     */
    @Override
    public byte[] importData(byte[] options, boolean transferMetaData) throws Throwable {
        long startTime = System.currentTimeMillis();
        long transferredBytesNum = 0;
        long elapsedTime = 0;
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String server = opts.getProperty(TRANSFER_OPTION_SERVER);
        String username = opts.getProperty(TRANSFER_OPTION_USERNAME);
        String password = opts.getProperty(TRANSFER_OPTION_PASSWORD);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        if (transferMetaData) {
            int idx = filePath.lastIndexOf(PATH_SEPARATOR);
            if (idx != -1) {
                String fileName = filePath.substring(idx + 1) + META_DATA_FILE_SUFIX;
                filePath = filePath.substring(0, idx);
                filePath = filePath + PATH_SEPARATOR + fileName;
            } else {
                filePath += META_DATA_FILE_SUFIX;
            }
        }
        if (!server.contains(PORT_SEPARATOR)) {
            server += PORT_SEPARATOR + DEFAULT_PORT;
        }
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        urlc.setConnectTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        urlc.setReadTimeout(Preferences.getInstance().preferredTimeOut * 1000);
        ByteArrayOutputStream baos = null;
        try {
            InputStream is = urlc.getInputStream();
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int br;
            while ((br = is.read(buffer)) > 0) {
                baos.write(buffer, 0, br);
                if (!transferMetaData) {
                    transferredBytesNum += br;
                    elapsedTime = System.currentTimeMillis() - startTime;
                    fireOnProgressEvent(transferredBytesNum, elapsedTime);
                }
            }
            baos.close();
            is.close();
        } catch (FileNotFoundException fnfe) {
            
        }
        return baos != null ? baos.toByteArray() : null;
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
            return new TransferOptions(PropertiesUtils.serializeProperties(options), filepathTF.getText());
        }
        return null;
    }

}
