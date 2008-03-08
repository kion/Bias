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

    public LocalTransfer(byte[] settings) {
        super(settings);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#doExport(byte[], byte[])
     */
    @Override
    public void doExport(byte[] data, byte[] options) throws Exception {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        } else if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        FSUtils.writeFile(file, data);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#doImport(byte[])
     */
    @Override
    public byte[] doImport(byte[] options) throws Exception {
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        File file = new File(filePath);
        return FSUtils.readFile(file);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#configure(bias.extension.TransferExtension.OPERATION_TYPE)
     */
    @Override
    public byte[] configure(Constants.TRANSFER_OPERATION_TYPE opType) throws Throwable {
        Properties options = null;
        ZipFileChooser zfc = new ZipFileChooser();
        int rVal = 0;
        switch (opType) {
        case IMPORT:
            rVal = zfc.showOpenDialog(FrontEnd.getActiveWindow());
            break;
        case EXPORT:
            rVal = zfc.showSaveDialog(FrontEnd.getActiveWindow());
            break;
        }
        if (rVal == JFileChooser.APPROVE_OPTION) {
            options = new Properties();
            String filePath = zfc.getSelectedFile().getAbsolutePath();
            if (Constants.TRANSFER_OPERATION_TYPE.EXPORT.equals(opType) && !filePath.matches(Constants.ZIP_FILE_PATTERN)) {
                filePath += ".zip";
            }
            options.setProperty(TRANSFER_OPTION_FILEPATH, filePath);
        }
        return PropertiesUtils.serializeProperties(options);
    }

}
