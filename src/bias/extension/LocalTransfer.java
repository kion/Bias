/**
 * Created on Oct 31, 2007
 */
package bias.extension;

import java.io.File;
import java.util.Properties;

import javax.swing.JFileChooser;

import bias.Constants;
import bias.gui.FrontEnd;
import bias.gui.ZipFileChooser;
import bias.utils.FSUtils;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */
public class LocalTransfer extends TransferExtension {

    private static final String TRANSFER_OPTION_FILEPATH = "FILEPATH";

    private static final String META_DATA_FILE_SUFIX = ".metadata";

    public LocalTransfer(byte[] options) {
        super(options);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#exportData(byte[], byte[], boolean)
     */
    @Override
    public void exportData(byte[] data, byte[] options, boolean transferMetaData) throws Throwable {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        File file = new File(filePath);
        if (transferMetaData) {
            file = new File(file.getParentFile(), file.getName() + META_DATA_FILE_SUFIX);
        }
        if (file.exists()) {
            file.delete();
        } else if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        FSUtils.writeFile(file, data);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#importData(byte[], boolean)
     */
    @Override
    public byte[] importData(byte[] options, boolean transferMetaData) throws Throwable {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        File file = new File(filePath);
        if (transferMetaData) {
            file = new File(file.getParentFile(), file.getName() + META_DATA_FILE_SUFIX);
        }
        return FSUtils.readFile(file);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#configure(bias.Constants.TRANSFER_OPERATION_TYPE)
     */
    @Override
    public TransferOptions configure(Constants.TRANSFER_TYPE transferType) throws Throwable {
        Properties options = null;
        ZipFileChooser zfc = new ZipFileChooser();
        int rVal = 0;
        switch (transferType) {
        case IMPORT:
            rVal = zfc.showOpenDialog(FrontEnd.getActiveWindow());
            break;
        case EXPORT:
            rVal = zfc.showSaveDialog(FrontEnd.getActiveWindow());
            break;
        }
        String filePath = null;
        if (rVal == JFileChooser.APPROVE_OPTION) {
            options = new Properties();
            filePath = zfc.getSelectedFile().getAbsolutePath();
            if (Constants.TRANSFER_TYPE.EXPORT.equals(transferType) && !filePath.matches(Constants.ZIP_FILE_PATTERN)) {
                filePath += ".zip";
            }
            options.setProperty(TRANSFER_OPTION_FILEPATH, filePath);
        }
        return new TransferOptions(PropertiesUtils.serializeProperties(options), filePath);
    }

}
