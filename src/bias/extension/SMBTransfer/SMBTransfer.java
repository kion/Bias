/**
 * Created on Mar 27, 2008
 */
package bias.extension.SMBTransfer;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import bias.Preferences;
import bias.Constants.TRANSFER_TYPE;
import bias.extension.ObservableTransferExtension;
import bias.extension.TransferOptions;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;

/**
 * @author kion
 */
public class SMBTransfer extends ObservableTransferExtension {

    private static final String TRANSFER_OPTION_FILEPATH = "FILEPATH";
    private static final String TRANSFER_OPTION_HOST = "HOST";
    private static final String TRANSFER_OPTION_USERNAME = "USERNAME";
    private static final String TRANSFER_OPTION_PASSWORD = "PASSWORD";

    private static final String META_DATA_FILE_SUFIX = ".metadata";

    private static final String PATH_SEPARATOR = "/";

    private static final String PROTOCOL_PREFIX = "smb://";

    public SMBTransfer(byte[] settings) {
        super(settings);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#configure(bias.Constants.TRANSFER_TYPE)
     */
    @Override
    public TransferOptions configure(TRANSFER_TYPE transferType) throws Throwable {
        Properties options = new Properties();
        JLabel hostL = new JLabel("SMB host");
        JTextField hostTF = new JTextField();
        JLabel filepathL = new JLabel("Path to file on host");
        JTextField filepathTF = new JTextField();
        JLabel usernameL = new JLabel("Username (only if required)");
        JTextField usernameTF = new JTextField();
        JLabel passwordL = new JLabel("Password (only if required)");
        JTextField passwordTF = new JPasswordField();
        int opt = JOptionPane.showConfirmDialog(
                FrontEnd.getActiveWindow(), 
                new Component[]{
                        hostL,
                        hostTF,
                        filepathL,
                        filepathTF,
                        usernameL,
                        usernameTF,
                        passwordL,
                        passwordTF
                }, 
                "SMB transfer options", 
                JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            options = new Properties();
            String text = hostTF.getText();
            if (!Validator.isNullOrBlank(text)) {
                options.setProperty(TRANSFER_OPTION_HOST, text);
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

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#readData(byte[], boolean)
     */
    @Override
    public byte[] readData(byte[] settings, boolean transferMetaData) throws Throwable {
        long startTime = System.currentTimeMillis();
        long transferredBytesNum = 0;
        long elapsedTime = 0;
        Properties opts = PropertiesUtils.deserializeProperties(settings);
        String server = opts.getProperty(TRANSFER_OPTION_HOST);
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
        String url = PROTOCOL_PREFIX + server + filePath;

        Config.setProperty("jcifs.smb.client.responseTimeout", "" + (Preferences.getInstance().preferredTimeOut * 1000));

        NtlmPasswordAuthentication auth = null;
        if (!Validator.isNullOrBlank(username) && !Validator.isNullOrBlank(password)) {
            auth = new NtlmPasswordAuthentication(server, username, password);
        }
        ByteArrayOutputStream baos = null;
        SmbFile file = new SmbFile(url, auth);
        if (file.exists()) {
            baos = new ByteArrayOutputStream();
            SmbFileInputStream in = new SmbFileInputStream(file);
            byte[] b = new byte[1024];
            int br;
            while((br = in.read(b)) > 0) {
                baos.write(b, 0, br);
                if (!transferMetaData) {
                    transferredBytesNum += br;
                    elapsedTime = System.currentTimeMillis() - startTime;
                    fireOnProgressEvent(transferredBytesNum, elapsedTime);
                }
            }
            in.close();
            baos.close();
        }
        return baos != null ? baos.toByteArray() : null;
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#writeData(byte[], byte[], boolean)
     */
    @Override
    public void writeData(byte[] data, byte[] settings, boolean transferMetaData) throws Throwable {
        long startTime = System.currentTimeMillis();
        long transferredBytesNum = 0;
        long elapsedTime = 0;
        Properties opts = PropertiesUtils.deserializeProperties(settings);
        String server = opts.getProperty(TRANSFER_OPTION_HOST);
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
        String url = PROTOCOL_PREFIX + server + filePath;

        Config.setProperty("jcifs.smb.client.responseTimeout", "" + (Preferences.getInstance().preferredTimeOut * 1000));
        
        NtlmPasswordAuthentication auth = null;
        if (!Validator.isNullOrBlank(username) && !Validator.isNullOrBlank(password)) {
            auth = new NtlmPasswordAuthentication(server, username, password);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        SmbFile file = new SmbFile(url, auth);
        SmbFileOutputStream out = new SmbFileOutputStream(file, false);
        byte[] b = new byte[1024];
        int br;
        while((br = bais.read(b)) > 0) {
            out.write(b, 0, br);
            if (!transferMetaData) {
                transferredBytesNum += br;
                elapsedTime = System.currentTimeMillis() - startTime;
                fireOnProgressEvent(transferredBytesNum, elapsedTime);
            }
        }
        bais.close();
        out.close();
    }
    
}
