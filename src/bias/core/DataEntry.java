/**
 * Created on Oct 23, 2006
 */
package bias.core;

import java.util.UUID;

/**
 * @author kion
 */
public class DataEntry extends Recognizable {
    
    private String type;
    
    private byte[] data;
    
    public DataEntry() {
        super();
    }
    
    public DataEntry(UUID id, String caption, String type, byte[] data) {
        super(id, caption);
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
