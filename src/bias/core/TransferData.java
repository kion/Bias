/**
 * Created on Mar 9, 2008
 */
package bias.core;

import java.util.Properties;

/**
 * @author kion
 */
public class TransferData {
    
    private byte[] data;
    
    private Properties metaData;

    public TransferData(byte[] data, Properties metaData) {
        this.data = data;
        this.metaData = metaData;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Properties getMetaData() {
        return metaData;
    }

    public void setMetaData(Properties metaData) {
        this.metaData = metaData;
    }

}
