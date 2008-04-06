/**
 * Created on Oct 31, 2007
 */
package bias.extension;

import java.util.Properties;

import bias.Constants;
import bias.Constants.TRANSFER_TYPE;
import bias.core.BackEnd;
import bias.core.TransferData;
import bias.i18n.I18nService;
import bias.utils.PropertiesUtils;


/**
 * @author kion
 */
public abstract class TransferExtension implements Extension {
    
    /**
     * Internationalization support
     */
    protected String getMessage(String key) {
        return I18nService.getInstance().getMessages(getClass()).get(key);
    }
    
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
     * Checks if checksum for the import defined by specified options and meta-data has changed since last import.
     * This method is useful when import is to be performed - it allows to check if data to be imported
     * have changed since last time it had been imported (and if not, import can be simply discarded to avoid excessiveness).
     *  
     * @return boolean true if checksum has changed, false - otherwise
     */
    public boolean importCheckSumChanged(TransferOptions options, byte[] metaDataBytes) throws Throwable {
        String checkSum = null;
        if (metaDataBytes != null && metaDataBytes.length != 0) {
            checkSum = PropertiesUtils.deserializeProperties(metaDataBytes).getProperty(Constants.META_DATA_CHECKSUM);
        }
        return BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.IMPORT, this.getClass(), options.getFileLocation(), checkSum);
    }

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
            meta = PropertiesUtils.deserializeProperties(metaBytes);
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
     * Checks if checksum for the export defined by specified options and transfer-data has changed since last export.
     * This method is useful when export is to be performed - it allows to check if data to be exported
     * have changed since last time it had been exported (and if not, export can be simply discarded to avoid excessiveness).
     *  
     * @return boolean true if checksum has changed, false - otherwise
     */
    public boolean exportCheckSumChanged(TransferOptions options, TransferData td) throws Throwable {
        String checkSum = null;
        if (td.getMetaData() != null) {
            checkSum = td.getMetaData().getProperty(Constants.META_DATA_CHECKSUM);
        }
        return BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.EXPORT, this.getClass(), options.getFileLocation(), checkSum);
    }

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
            if (meta != null) writeData(PropertiesUtils.serializeProperties(meta), options.getOptions(), true);
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
