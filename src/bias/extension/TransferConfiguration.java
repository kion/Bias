/**
 * Created on Mar 9, 2008
 */
package bias.extension;

/**
 * @author kion
 */
public class TransferConfiguration {
    
    private byte[] options;
    
    private String fileLocation;

    public TransferConfiguration(byte[] options, String fileLocation) {
        this.options = options;
        this.fileLocation = fileLocation;
    }

    public byte[] getOptions() {
        return options;
    }

    public void setOptions(byte[] options) {
        this.options = options;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

}
