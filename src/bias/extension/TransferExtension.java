/**
 * Created on Oct 31, 2007
 */
package bias.extension;

import bias.Constants;
import bias.Constants.TRANSFER_TYPE;
import bias.core.BackEnd;
import bias.core.FileInfo;
import bias.utils.FSUtils;


/**
 * @author kion
 */
public abstract class TransferExtension implements Extension {
    
    private byte[] options;
    
    /**
     * The only allowed constructor that is aware of initialization data and options.
     * 
     * @param options extension instance options
     */
    public TransferExtension(byte[] options) {
        this.options = options;
    }

    /**
     * @return the options
     */
    public byte[] getSettings() {
        return options;
    }
    
    // TODO [P2] optimization (memory usage): looks like it's better to use Input/Output streams instead of byte arrays during transfer

    /**
     * Imports data using provided import options.
     * This template method imports checksum first, then imports actual data
     * @return array of bytes representing imported data, or null if data is up to data and import has been discarded
     */
    public byte[] importData(TransferOptions options, boolean force) throws Throwable {
        byte[] importedData = null;
        String checkSum = new String(doImportCheckSum(options.getOptions()));
        if (force || BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.IMPORT, this.getClass(), options.getFileLocation(), checkSum)) {
            importedData = doImport(options.getOptions());
            BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.IMPORT, this.getClass(), options.getFileLocation(), checkSum);
        }
        return importedData;
    }

    /**
     * Imports data using provided import options.
     * Should be overridden to perform import for certain transfer-extension's instance.
     * 
     * @return imported data
     */
    protected abstract byte[] doImport(byte[] options) throws Throwable;

    /**
     * Imports data checksum using provided import options.
     * Should be overridden to perform checksum-import for certain transfer-extension's instance.
     * 
     * @return imported data
     */
    protected abstract byte[] doImportCheckSum(byte[] options) throws Throwable;
    
    /**
     * Exports given data using provided import options.
     * This template method exports checksum first, then exports actual data
     * @return boolean true if data have been successfully exported, or false if data is up to date and export has been discarded
     */
    public boolean exportData(FileInfo exportedFileInfo, TransferOptions options, boolean force) throws Throwable {
        if (force || BackEnd.getInstance().isTransferFileLocationCheckSumChanged(TRANSFER_TYPE.EXPORT, this.getClass(), options.getFileLocation(), exportedFileInfo.getCheckSum())) {
            doExportCheckSum(exportedFileInfo.getCheckSum().getBytes(), options.getOptions());
            doExport(FSUtils.readFile(exportedFileInfo.getFile()), options.getOptions());
            BackEnd.getInstance().storeTransferFileLocationCheckSum(TRANSFER_TYPE.EXPORT, this.getClass(), options.getFileLocation(), exportedFileInfo.getCheckSum());
            return true;
        }
        return false;
    }

    /**
     * Exports given data using provided import options.
     * Should be overridden to perform export for certain transfer-extension's instance.
     */
    protected abstract void doExport(byte[] data, byte[] options) throws Throwable;

    /**
     * Exports given data checksum using provided import options.
     * Should be overridden to perform checksum-export for certain transfer-extension's instance.
     */
    protected abstract void doExportCheckSum(byte[] data, byte[] options) throws Throwable;

    /**
     * Performs general transfer-extension configuration.
     * Should be overridden to return options for certain transfer-extension.
     * By default returns null (no configuration).
     * 
     * @param options initial options
     * @return options byte array containing serialized configuration options
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
