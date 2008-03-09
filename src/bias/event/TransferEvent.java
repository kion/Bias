/**
 * Created on Mar 10, 2008
 */
package bias.event;

import bias.Constants.TRANSFER_TYPE;
import bias.extension.TransferExtension;

/**
 * @author kion
 */
public class TransferEvent {
    
    private TRANSFER_TYPE transferType;

    private Class<? extends TransferExtension> transferClass;

    private String configName;

    public TransferEvent(TRANSFER_TYPE transferType, Class<? extends TransferExtension> transferClass) {
        this.transferType = transferType;
        this.transferClass = transferClass;
    }

    public TransferEvent(TRANSFER_TYPE transferType, Class<? extends TransferExtension> transferClass, String configName) {
        this.transferType = transferType;
        this.transferClass = transferClass;
        this.configName = configName;
    }

    public TRANSFER_TYPE getTransferType() {
        return transferType;
    }

    public void setTransferType(TRANSFER_TYPE transferType) {
        this.transferType = transferType;
    }

    public Class<? extends TransferExtension> getTransferClass() {
        return transferClass;
    }

    public void setTransferClass(Class<? extends TransferExtension> transferClass) {
        this.transferClass = transferClass;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }
    
}
