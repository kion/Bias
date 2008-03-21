/**
 * Created on Oct 31, 2007
 */
package bias.extension;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JFileChooser;

import bias.Constants;
import bias.gui.FrontEnd;
import bias.gui.ZipFileChooser;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */
public class LocalTransfer extends ObservableTransferExtension {

    private static final String TRANSFER_OPTION_FILEPATH = "FILEPATH";

    private static final String META_DATA_FILE_SUFIX = ".metadata";

    public LocalTransfer(byte[] options) {
        super(options);
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#writeData(byte[], byte[], boolean)
     */
    @Override
    public void writeData(byte[] data, byte[] options, boolean transferMetaData) throws Throwable {
        long startTime = System.currentTimeMillis();
        long transferredBytesNum = 0;
        long elapsedTime = 0;
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
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buffer = new byte[1024];
        int br;
        while ((br = bais.read(buffer)) > 0) {
            bos.write(buffer, 0, br);
            if (!transferMetaData) {
                transferredBytesNum += br;
                elapsedTime = System.currentTimeMillis() - startTime;
                fireOnProgressEvent(transferredBytesNum, elapsedTime);
            }
        }
        bos.close();
        bais.close();
    }

    /* (non-Javadoc)
     * @see bias.extension.TransferExtension#readData(byte[], boolean)
     */
    @Override
    public byte[] readData(byte[] options, boolean transferMetaData) throws Throwable {
        byte[] data = null;
        long startTime = System.currentTimeMillis();
        long transferredBytesNum = 0;
        long elapsedTime = 0;
        Properties opts = PropertiesUtils.deserializeProperties(options);
        String filePath = opts.getProperty(TRANSFER_OPTION_FILEPATH);
        File file = new File(filePath);
        if (transferMetaData) {
            file = new File(file.getParentFile(), file.getName() + META_DATA_FILE_SUFIX);
        }
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int br;
            while ((br = bis.read(buffer)) > 0) {
                baos.write(buffer, 0, br);
                if (!transferMetaData) {
                    transferredBytesNum += br;
                    elapsedTime = System.currentTimeMillis() - startTime;
                    fireOnProgressEvent(transferredBytesNum, elapsedTime);
                }
            }
            baos.close();
            bis.close();
            data = baos.toByteArray();
        }
        return data;
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
            return new TransferOptions(PropertiesUtils.serializeProperties(options), filePath);
        }
        return null;
    }

}
