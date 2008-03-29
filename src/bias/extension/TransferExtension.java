/**
 * Created on Oct 31, 2007
 */
package bias.extension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import bias.Constants;
import bias.Constants.TRANSFER_TYPE;
import bias.core.BackEnd;
import bias.core.TransferData;


/**
 * @author kion
 */
public abstract class TransferExtension implements Extension {
    
    private byte[] settings;
    
    /**
     * The only allowed constructor that is aware of initialization data and settings.
     * 
     * @param settings extension instance settings
     */
    public TransferExtension(byte[] settings) {
        this.settings = settings;
    }

    public byte[] getSettings() {
        return settings;
    }

    public void setSettings(byte[] settings) {
        this.settings = settings;
    }
    
    /**
     * Checks if connection is available for certain transfer-extension class instance using transfer options provided.
     * Should be overridden by certain transfer-extension class.
     * 
     * @throws Throwable if failure occurred while trying to establish connection 
     */
    public void checkConnection(byte[] options) throws Throwable {};

    /**
     * Imports data using provided import options.
     * This template method imports metadata first, then imports actual data
     * 
     * @return TransferData instance representing imported data and it's metadata, or null if data is up to date and import has been discarded
     */
    public TransferData importData(TransferOptions options, boolean force) throws Throwable {
        TransferData td = new TransferData(null, null);
        String checkSum = null;
        Properties meta = null;
        byte[] metaBytes = readData(options.getOptions(), true);
        if (metaBytes != null && metaBytes.length != 0) {
            ByteArrayInputStream bais = new ByteArrayInputStream(metaBytes);
            meta = new Properties();
            meta.load(bais);
            bais.close();
            checkSum = meta.getProperty(Constants.META_DATA_CHECKSUM);
        }
        if (force || BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.IMPORT, this.getClass(), options.getFileLocation(), checkSum)) {
            byte[] data = readData(options.getOptions(), false);
            BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.IMPORT, this.getClass(), options.getFileLocation(), checkSum);
            td.setData(data);
            td.setMetaData(meta);
        }
        return td;
    }

    /**
     * Reads data using provided transfer options.
     * Should be overridden by certain transfer-extension class.
     * 
     * @param transferMetaData defines whether meta- (true) or main-data (false) should be transferred 
     * @return data read
     */
    public abstract byte[] readData(byte[] options, boolean transferMetaData) throws Throwable;

    /**
     * Exports given data using provided export options.
     * This template method exports metadata first, then exports actual data
     * 
     * @return boolean true if data have been successfully exported, or false if data is up to date and export has been discarded
     */
    public boolean exportData(TransferData td, TransferOptions options, boolean force) throws Throwable {
        String checkSum = null;
        Properties meta = td.getMetaData();
        if (meta != null) {
            checkSum = meta.getProperty(Constants.META_DATA_CHECKSUM);
        }
        if (force || BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.EXPORT, this.getClass(), options.getFileLocation(), checkSum)) {
            ByteArrayOutputStream baos = null;
            if (meta != null) {
                baos = new ByteArrayOutputStream();
                meta.store(baos, null);
                baos.close();
            }
            if (baos != null) writeData(baos.toByteArray(), options.getOptions(), true);
            writeData(td.getData(), options.getOptions(), false);
            BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.EXPORT, this.getClass(), options.getFileLocation(), checkSum);
            return true;
        }
        return false;
    }

    /**
     * Writes given data using provided transfer options.
     * Should be overridden by certain transfer-extension class.
     * 
     * @param transferMetaData defines whether meta- (true) or main-data (false) should be transferred
     *  
     */
    public abstract void writeData(byte[] data, byte[] options, boolean transferMetaData) throws Throwable;

    /**
     * Performs general transfer-extension configuration.
     * Should be overridden to return settings for certain transfer-extension.
     * By default returns null (no configuration).
     * 
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure() throws Throwable {
        return null;
    }

    /**
     * Configures transfer-extension right before import/export operation is to be performed.
     * Should be implemented to return options for certain transfer-extension.
     * 
     * @param operation type (import/export) to be performed after configuration
     * @return options byte array containing serialized configuration options
     */
    public abstract TransferOptions configure(Constants.TRANSFER_TYPE transferType) throws Throwable;

    /**
     * Defines whether extension's configuration should be skipped on export
     * By default returns false (configuration will be exported).
     */
    public boolean skipConfigExport() {
        return false;
    }

}
