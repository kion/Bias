/**
 * Created on Mar 11, 2008
 */
package bias.core;

import bias.Constants;

/**
 * @author kion
 */
public abstract class TransferConfiguration {
    
    private String transferProvider;
    private String fileLocation; 
    private String password;

    public TransferConfiguration() {}

    protected TransferConfiguration(String transferProvider, String fileLocation, String password) {
        this.transferProvider = transferProvider;
        this.fileLocation = fileLocation;
        this.password = password;
    }

    public String getTransferProvider() {
        return transferProvider;
    }

    public void setTransferProvider(String transferProvider) {
        this.transferProvider = transferProvider;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getPassword() {
        return password != null ? password : Constants.EMPTY_STR;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
