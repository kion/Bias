/**
 * Created on Oct 31, 2007
 */
package bias.extension;

import bias.Constants;


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

    /**
     * @return the settings
     */
    public byte[] getSettings() {
        return settings;
    }
    
    // TODO [P2] optimization (memory usage): looks like it's better to use Input/Output streams instead of byte arrays during transfer

    /**
     * Imports data using provided import settings.
     * Should be overridden to perform import for certain transfer-extension's instance.
     * 
     * @return imported data
     */
    public abstract byte[] doImport(byte[] options) throws Exception;

    /**
     * Exports given data using provided import settings.
     * Should be overridden to perform export for certain transfer-extension's instance.
     */
    public abstract void doExport(byte[] data, byte[] options) throws Exception;

    /**
     * Performs general transfer-extension configuration.
     * Should be overridden to return settings for certain transfer-extension.
     * By default returns null (no configuration).
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure() throws Throwable {
        return null;
    }

    /**
     * Configures transfer-extension right before import/export operation is to be performed.
     * Should be overridden to return settings for certain transfer-extension.
     * By default returns null (no configuration).
     * 
     * @param operation type (import/export) to be performed after configuration
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure(Constants.TRANSFER_OPERATION_TYPE opType) throws Throwable {
        return null;
    }

    /**
     * Defines whether extension's configuration should be skipped on export
     * By default returns false (configuration will be exported).
     */
    public boolean skipConfigExport() {
        return false;
    }

}
