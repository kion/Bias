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
    public void doExport(byte[] data, byte[] options) throws Exception {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String server = opts.getProperty(TRANSFER_OPTION_SERVER);
        String username = opts.getProperty(TRANSFER_OPTION_USERNAME);
        String password = opts.getProperty(TRANSFER_OPTION_PASSWORD);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        URL url = new URL(PROTOCOL_PREFIX + username + ":" + password + "@" + server + filePath + ";type=i");
        URLConnection urlc = url.openConnection();
        urlc.setConnectTimeout(Preferences.getInstance().preferredTimeOut);
        urlc.setReadTimeout(Preferences.getInstance().preferredTimeOut);
        OutputStream os = urlc.getOutputStream();
        os.write(data);
        os.close();
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#doImport(byte[])
     */
    @Override
    public byte[] doImport(byte[] options) throws Exception {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String server = opts.getProperty(TRANSFER_OPTION_SERVER);
        String username = opts.getProperty(TRANSFER_OPTION_USERNAME);
        String password = opts.getProperty(TRANSFER_OPTION_PASSWORD);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
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
    
    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#configure(bias.extension.TransferExtension.OPERATION_TYPE)
     */
    @Override
    public byte[] configure(Constants.TRANSFER_OPERATION_TYPE opType) throws Throwable {
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
        return PropertiesUtils.serializeProperties(options);
    }

}
